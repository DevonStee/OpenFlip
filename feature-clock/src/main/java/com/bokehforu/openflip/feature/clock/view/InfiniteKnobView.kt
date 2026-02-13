package com.bokehforu.openflip.feature.clock.view

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.core.R as CoreR
import com.bokehforu.openflip.core.util.resolveThemeColor
import android.os.Build
import android.os.SystemClock

import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.ui.feedback.performSystemHapticClick
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Infinite rotation knob view
 *
 * Features:
 * - Infinite rotation (no 0-360 limits)
 * - Touch rotation + Inertial scrolling (Fling)
 * - Haptic feedback every N degrees (Angle Tick)
 * - Custom visual design with shadows, bevels, and tick marks
 * - Interactive states (Normal vs Pressed)
 * - Theme support (Dark/Light)
 */
class InfiniteKnobView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val PRESSED_SCALE = 0.99f
    }
    
    // Loaded from resources
    private val tickCount = context.resources.getInteger(CoreR.integer.knob_tick_count)
    private val anglePerTick = 360f / tickCount
    private val vibrationThresholdDegrees =
        context.resources.getInteger(CoreR.integer.knob_vibration_threshold_degrees).toFloat()

    // Layout Dimensions (dp values loaded in onSizeChanged)
    private var shadowOffsetY = 0f
    private var shadowRadius = 0f
    private var knobPadding = 0f
    private var bevelStroke = 0f
    private var tickStroke = 0f
    private var indicatorStroke = 0f

    // Cached density for performance
    private val density = context.resources.displayMetrics.density

    // ========== Rotation State ==========

    /** Accumulated rotation degrees (unbounded) */
    private var totalRotationDegrees = 0f

    /** Last touch angle for delta calculation */
    private var lastTouchAngle = 0f
    private var lastTouchTime = 0L

    // ========== Components ==========

    private val scroller = Scroller(context)
    private var lastVibrateAngle = 0f

    /** Callback for rotation changes */
    var onRotationChangedListener: ((totalDegrees: Float) -> Unit)? = null

    /** Callback for each mechanical tick (vibration + sound) */
    var onTickListener: (() -> Unit)? = null
    
    /** Whether the knob is currently being interacted with (finger down) */
    val isInteracting: Boolean get() = isActive

    // ========== Drawing Tools ==========

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val tickHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // [New] Realism Paints - PHYSICAL ACRYLIC / GLASS STYLE
    private val indicatorShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        // Reduced from #4D (30%) to #33 (20%) - Brightened Shadow
        color = context.resolveThemeColor(CoreR.attr.knobIndicatorShadowColor, CoreR.style.Theme_OpenFlip_Light)
    }

    private val indicatorBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var redBodyShader: Shader? = null
    private var greenBodyShader: Shader? = null
    private var highlightShader: Shader? = null

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        // Reduced intensity by 50% (was #30)
        color = context.resolveThemeColor(CoreR.attr.knobPressedOverlayColor, CoreR.style.Theme_OpenFlip_Light)
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    // ========== View State ==========

    private var isActive = false
    private var isDarkTheme = false
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    // Geometry
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    // ========== Initialization ==========

    // ========== Accessibility Support ==========

    private var lastAnnouncedRotation = 0f
    private val announcementThreshold = 30f // Announce every 30 degrees
    private var rotationMinutes = 0

    init {
        backgroundPaint.color = context.resolveThemeColor(CoreR.attr.knobBaseColor, CoreR.style.Theme_OpenFlip_Light)
        tickPaint.color = Color.BLACK

        // Accessibility Support
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true
        isFocusableInTouchMode = false
        contentDescription = context.getString(CoreR.string.titleSelectDuration)
    }

    private var cachedThemedContext: Context? = null
    private var lastThemeResId: Int = -1

    // ========== Gesture Handling ==========

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            parent?.requestDisallowInterceptTouchEvent(true)
            scroller.forceFinished(true)

            // Tracks the specific pointer that triggered the gesture
            val pointerIndex = e.findPointerIndex(activePointerId)
            if (pointerIndex == -1) return false

            lastTouchAngle = calculateAngle(e.getX(pointerIndex), e.getY(pointerIndex))
            lastTouchTime = SystemClock.uptimeMillis()

            triggerVibration()
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val pointerIndex = e2.findPointerIndex(activePointerId)
            if (pointerIndex == -1) return false

            val currentAngle = calculateAngle(e2.getX(pointerIndex), e2.getY(pointerIndex))
            var deltaAngle = currentAngle - lastTouchAngle

            // Handle boundary crossing (0 <-> 360)
            if (deltaAngle > 180) deltaAngle -= 360
            if (deltaAngle < -180) deltaAngle += 360

            totalRotationDegrees += deltaAngle
            checkAndTriggerVibration()

            onRotationChangedListener?.invoke(totalRotationDegrees)
            lastTouchAngle = currentAngle

            // Announce rotation value for accessibility
            announceRotationForAccessibility()

            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val pointerIndex = e2.findPointerIndex(activePointerId)
            if (pointerIndex == -1) return false
            
            val angularVelocity = calculateAngularVelocity(
                velocityX, velocityY, 
                e2.getX(pointerIndex), e2.getY(pointerIndex), 
                radius
            )

            scroller.fling(
                totalRotationDegrees.toInt(), 0,
                angularVelocity.toInt(), 0,
                Int.MIN_VALUE, Int.MAX_VALUE, 0, 0
            )
            postInvalidateOnAnimation()
            return true
        }
    }

    private val gestureDetector = GestureDetector(context, GestureListener()).apply {
        setIsLongpressEnabled(false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            activePointerId = event.getPointerId(0)
            isActive = true
            invalidate()
        }

        // Always feed events to GestureDetector (for scroll + fling)
        val handled = gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Ignore additional pointers for now
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    // Our active finger left! Transfer to another finger.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = event.getPointerId(newPointerIndex)
                    // Reset lastTouchAngle to the new finger's position to prevent jump
                    lastTouchAngle = calculateAngle(event.getX(newPointerIndex), event.getY(newPointerIndex))
                }
            }
            MotionEvent.ACTION_UP -> {
                isActive = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isActive = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }

        // For MOVE and other actions, keep consuming so interaction continues
        return handled || isActive || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Keep default accessibility / click sound behavior
        super.performClick()
        performSystemHapticClick()
        // No extra click behavior for now (rotation is handled via gestures)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        when (keyCode) {
            android.view.KeyEvent.KEYCODE_DPAD_LEFT,
            android.view.KeyEvent.KEYCODE_MINUS -> {
                // Rotate counter-clockwise by 6 degrees (1 minute)
                totalRotationDegrees -= anglePerTick
                checkAndTriggerVibration()
                onRotationChangedListener?.invoke(totalRotationDegrees)
                announceRotationForAccessibility()
                invalidate()
                return true
            }
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
            android.view.KeyEvent.KEYCODE_PLUS,
            android.view.KeyEvent.KEYCODE_EQUALS -> {
                // Rotate clockwise by 6 degrees (1 minute)
                totalRotationDegrees += anglePerTick
                checkAndTriggerVibration()
                onRotationChangedListener?.invoke(totalRotationDegrees)
                announceRotationForAccessibility()
                invalidate()
                return true
            }
            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                // Large increment: 30 degrees (5 minutes)
                totalRotationDegrees += anglePerTick * 5
                checkAndTriggerVibration()
                onRotationChangedListener?.invoke(totalRotationDegrees)
                announceRotationForAccessibility()
                invalidate()
                return true
            }
            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                // Large decrement: 30 degrees (5 minutes)
                totalRotationDegrees -= anglePerTick * 5
                checkAndTriggerVibration()
                onRotationChangedListener?.invoke(totalRotationDegrees)
                announceRotationForAccessibility()
                invalidate()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            totalRotationDegrees = scroller.currX.toFloat()
            checkAndTriggerVibration()
            onRotationChangedListener?.invoke(totalRotationDegrees)
            postInvalidateOnAnimation()
        }
    }

    fun stopFling() {
        scroller.forceFinished(true)
    }

    // ========== Lifecycle & Drawing ==========

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // Stop scroller
        scroller.forceFinished(true)

        // Clear listeners to prevent memory leaks
        onRotationChangedListener = null
        onTickListener = null
        hapticManager = null

        // Clear shaders
        backgroundPaint.shader = null
        bevelPaint.shader = null
        indicatorBodyPaint.shader = null
        indicatorHighlightPaint.shader = null
        redBodyShader = null
        greenBodyShader = null
        highlightShader = null

        // Clear cached context
        cachedThemedContext = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f

        // Initialize Dimen Values from resources
        shadowOffsetY = context.resources.getDimension(CoreR.dimen.knob_shadow_offset_y)
        shadowRadius = context.resources.getDimension(CoreR.dimen.knob_shadow_radius)
        knobPadding = context.resources.getDimension(CoreR.dimen.knob_padding)
        bevelStroke = context.resources.getDimension(CoreR.dimen.knob_bevel_stroke)
        tickStroke = context.resources.getDimension(CoreR.dimen.knob_tick_stroke)
        indicatorStroke = context.resources.getDimension(CoreR.dimen.knob_indicator_stroke)

        radius = (min(w, h) / 2f) - knobPadding

        // Dynamically adjust bevel thickness based on radius (keeps proportions on different sizes)
        val dynamicBevelStroke = radius * 0.06f
        bevelStroke = max(bevelStroke, dynamicBevelStroke)

        // Update Paint Widths for Realism - ACRYLIC STYLE
        // 1. Shadow: Same width as body
        indicatorShadowPaint.strokeWidth = indicatorStroke

        // 2. Body: Standard width
        indicatorBodyPaint.strokeWidth = indicatorStroke

        // 3. Highlight: Same width as body for overlay consistency
        indicatorHighlightPaint.strokeWidth = indicatorStroke

        // radius depends on padding and view size
        radius = (min(w, h) / 2f) - knobPadding

        val themeResId = if (isDarkTheme) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light

        // Prepare Shaders for Indicator (Vertical Gradient)
        val tickStartRadius = radius * 0.90f
        val indicatorLength = radius / 9f
        val indTopY = centerY - tickStartRadius
        val indBottomY = centerY - tickStartRadius + indicatorLength

        // [Body Gradient]: Bright Cherry -> Deep Red (Cylinder Volume)
        redBodyShader = LinearGradient(
            centerX, indTopY, centerX, indBottomY,
            intArrayOf(
                context.resolveThemeColor(CoreR.attr.knobIndicatorRedBrightColor, themeResId),
                context.resolveThemeColor(CoreR.attr.knobIndicatorRedDeepColor, themeResId)
            ),
            null, Shader.TileMode.CLAMP
        )

        // [Body Gradient]: Bright Green -> Deep Green
        greenBodyShader = LinearGradient(
            centerX, indTopY, centerX, indBottomY,
            intArrayOf(
                context.resolveThemeColor(CoreR.attr.knobIndicatorGreenBrightColor, themeResId),
                context.resolveThemeColor(CoreR.attr.knobIndicatorGreenDeepColor, themeResId)
            ),
            null, Shader.TileMode.CLAMP
        )

        // [Highlight Gradient]: White(50%) -> Transparent
        val highlightHeight = indicatorLength * 0.5f
        highlightShader = LinearGradient(
            centerX, indTopY, centerX, indTopY + highlightHeight,
            intArrayOf(context.resolveThemeColor(CoreR.attr.knobIndicatorHighlightColor, themeResId), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        indicatorHighlightPaint.shader = highlightShader

        updateColors()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        if (isActive) {
            canvas.scale(PRESSED_SCALE, PRESSED_SCALE, centerX, centerY)
        }

        // 1. Main Body (Background) — no outer shadow
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // if Pressed, draw a darkened overlay
        if (isActive) {
            canvas.drawCircle(centerX, centerY, radius, pressedPaint)
        }

        // 3. Bevel Ring — subtle inner ring just outside ticks
        // Light theme: press state uses lower alpha so the ring is less obvious
        val ringAlpha = if (!isDarkTheme && isActive) 100 else 255
        bevelPaint.alpha = ringAlpha
        canvas.drawCircle(centerX, centerY, radius - (2f * density), bevelPaint)

        // 3.1. Border (still disabled to avoid harsh outer rim)
        // if (isDarkTheme) {
        //     canvas.drawCircle(centerX, centerY, radius, borderPaint)
        // }

        // 4. Ticks (Rotated)
        canvas.save()
        canvas.rotate(totalRotationDegrees % 360, centerX, centerY)
        drawTicks(canvas)
        canvas.restore()

        canvas.restore()
    }

    private fun drawTicks(canvas: Canvas) {
        // Pull ticks slightly inward so they don't collide with the outer ring
        val tickStartRadius = radius * 0.88f
        val tickEndRadius = radius * 0.80f

        // Colors based on state
        tickPaint.color = if (isActive) cachedTickActive else cachedTickNormal
        indicatorTickPaint.color = if (isActive) cachedIndicatorActive else cachedIndicatorNormal

        for (i in 0 until tickCount) {
            // Skip neighbors to avoid overlap with the indicator
            if (i == 1 || i == tickCount - 1) continue

            // Angle: -90 ensures index 0 is at top (12 o'clock)
            val angle = (i * 360f / tickCount) - 90f
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()

            val cosA = cos(angleRad)
            val sinA = sin(angleRad)

            if (i == 0) {
                val indicatorLength = radius / 9f
                val indStartX = centerX + tickStartRadius * cosA
                val indStartY = centerY + tickStartRadius * sinA

                // End point inward
                val indEndX = centerX + (tickStartRadius - indicatorLength) * cosA
                val indEndY = centerY + (tickStartRadius - indicatorLength) * sinA

                // Indicator: keep subtle body gradient, remove explicit shadow/highlight overlays
                if (isActive) {
                    indicatorBodyPaint.shader = greenBodyShader
                } else {
                    indicatorBodyPaint.shader = redBodyShader
                }

                // Draw only the indicator body line (no separate shadow or highlight)
                canvas.drawLine(indStartX, indStartY, indEndX, indEndY, indicatorBodyPaint)
            } else {
                val startX = centerX + tickStartRadius * cosA
                val startY = centerY + tickStartRadius * sinA
                val endX = centerX + tickEndRadius * cosA
                val endY = centerY + tickEndRadius * sinA

                canvas.drawLine(startX, startY, endX, endY, tickPaint)
            }
        }
    }

    // ========== Helpers ==========

    private fun calculateAngle(touchX: Float, touchY: Float): Float {
        val dx = touchX - centerX
        val dy = touchY - centerY
        // degrees in [-180, 180] -> [0, 360]
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        // Adjust so 0 is at 12 o'clock (standard usually starts at 3 o'clock)
        angle += 90f
        if (angle < 0) angle += 360f
        return angle
    }

    private fun calculateAngularVelocity(
        vx: Float,
        vy: Float,
        tx: Float,
        ty: Float,
        r: Float
    ): Float {
        val dx = tx - centerX
        val dy = ty - centerY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 1f) return 0f

        // Tangent unit vector
        val tangentX = -dy / dist
        val tangentY = dx / dist

        // Tangential velocity = dot product
        val vTangential = vx * tangentX + vy * tangentY

        // omega (rad/s) = v / r -> deg/s
        return Math.toDegrees((vTangential / r).toDouble()).toFloat()
    }

    private fun checkAndTriggerVibration() {
        val currentSegment = (totalRotationDegrees / anglePerTick).toInt()
        val lastSegment = (lastVibrateAngle / anglePerTick).toInt()

        if (currentSegment != lastSegment) {
            triggerVibration()
            onTickListener?.invoke()
            lastVibrateAngle = totalRotationDegrees
        }
    }

    var hapticManager: HapticsProvider? = null

    private fun triggerVibration() {
        hapticManager?.performClick()
    }

    // ========== Theming ==========

    fun setColors(isDark: Boolean) {
        isDarkTheme = isDark
        updateColors()
    }

    private fun updateColors() {
        val themeResId = if (isDarkTheme) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light

        if (cachedThemedContext == null || lastThemeResId != themeResId) {
            cachedThemedContext = android.view.ContextThemeWrapper(context, themeResId)
            lastThemeResId = themeResId
        }
        val themedContext = cachedThemedContext!!

        val baseColor = context.resolveThemeColor(CoreR.attr.knobBaseColor, themeResId)

        // Resolve Attributes
        val tickColor = resolveColor(themedContext, CoreR.attr.knobTickColor)
        val tickActiveColor = resolveColor(themedContext, CoreR.attr.knobTickActiveColor)
        val tickHighlightColor = resolveColor(themedContext, CoreR.attr.knobTickHighlightColor)
        val indicatorColor = resolveColor(themedContext, CoreR.attr.knob_indicatorColor)
        val indicatorActiveColor = context.resolveThemeColor(CoreR.attr.knobIndicatorActiveColor, themeResId)

        // 1. Radial Gradient Background (volume-like), symmetric, works for light & dark
        if (radius > 0) {
            val hsv = FloatArray(3)
            Color.colorToHSV(baseColor, hsv)

            if (isDarkTheme) {
                hsv[0] = (hsv[0] - 5f).coerceIn(0f, 360f)
                hsv[1] *= 0.7f
                hsv[2] = min(1.0f, hsv[2] * 1.15f)
            } else {
                hsv[1] *= 0.8f
                hsv[2] = min(1.0f, hsv[2] * 1.1f)
            }

            val centerColor = Color.HSVToColor(hsv)

            backgroundPaint.shader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(centerColor, baseColor),
                floatArrayOf(0.3f, 1.0f),
                Shader.TileMode.CLAMP
            )

            if (isDarkTheme) {
                // Dark theme: subtle symmetric bevel gradient (maintain current volume perception)
                val bevelLight = context.resolveThemeColor(CoreR.attr.knobBevelLightColor, themeResId)
                val bevelShadow = context.resolveThemeColor(CoreR.attr.knobBevelShadowColor, themeResId)
                bevelPaint.shader = RadialGradient(
                    centerX, centerY, radius,
                    intArrayOf(bevelLight, Color.TRANSPARENT, bevelShadow),
                    floatArrayOf(0.0f, 0.6f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            } else {
                // Light theme: uniform, very light ring based on baseColor
                // Slightly brighter than dial surface with uniform color to avoid "gray dirty line" artifact
                val ringHsv = FloatArray(3)
                Color.colorToHSV(baseColor, ringHsv)
                ringHsv[1] *= 0.9f               // Slightly reduce saturation
                ringHsv[2] = min(1.0f, ringHsv[2] * 1.08f) // Brightness +8%
                val ringColor = Color.HSVToColor(ringHsv)

                bevelPaint.shader = null
                bevelPaint.color = ringColor
            }
        } else {
            backgroundPaint.shader = null
            backgroundPaint.color = baseColor
            bevelPaint.shader = null
        }

        // Paints Setup
        bevelPaint.strokeWidth = bevelStroke

        // Border Color
        borderPaint.color = context.resolveThemeColor(CoreR.attr.knobBorderColor, themeResId)

        // Shadow Color (still configured but not drawn outside the button)
        val shadowColor = context.resolveThemeColor(CoreR.attr.knobShadowColor, themeResId)
        shadowPaint.color = shadowColor
        shadowPaint.maskFilter = if (shadowRadius > 0f) {
            BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
        } else {
            null
        }

        tickHighlightPaint.color = tickHighlightColor
        tickHighlightPaint.strokeWidth = tickStroke

        tickPaint.strokeWidth = tickStroke
        cachedTickNormal = tickColor
        cachedTickActive = tickActiveColor

        cachedIndicatorNormal = indicatorColor
        cachedIndicatorActive = indicatorActiveColor

        indicatorTickPaint.strokeWidth = indicatorStroke

        invalidate()
    }

    // Cached colors for drawTicks
    private var cachedTickNormal = Color.BLACK
    private var cachedTickActive = Color.BLACK
    private var cachedIndicatorNormal = Color.RED
    private var cachedIndicatorActive = Color.GREEN

    private fun resolveColor(context: Context, attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        val theme = context.theme
        if (theme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                return androidx.core.content.ContextCompat.getColor(context, typedValue.resourceId)
            }
            return typedValue.data
        }
        return Color.MAGENTA
    }

    // ========== Accessibility Methods ==========

    /**
     * Announces rotation value to screen readers when threshold is crossed.
     */
    private fun announceRotationForAccessibility() {
        val rotationDiff = kotlin.math.abs(totalRotationDegrees - lastAnnouncedRotation)

        if (rotationDiff >= announcementThreshold) {
            // Calculate minutes based on rotation (assuming full rotation = 60 minutes)
            val minutes = ((totalRotationDegrees % 360) / 360f * 60f).toInt().coerceIn(0, 59)

            if (minutes != rotationMinutes) {
                rotationMinutes = minutes
                announceForAccessibility("$minutes ${context.getString(CoreR.string.optionSleep15Min).substringAfter(" ")}")
                lastAnnouncedRotation = totalRotationDegrees
            }
        }
    }

    /**
     * Sets the knob rotation value (useful for external control or accessibility).
     */
    fun setKnobRotation(degrees: Float) {
        totalRotationDegrees = degrees
        onRotationChangedListener?.invoke(totalRotationDegrees)
        invalidate()
        announceRotationForAccessibility()
    }

    /**
     * Gets the current knob rotation in degrees.
     */
    fun getKnobRotation(): Float = totalRotationDegrees

    /**
     * Resets the knob rotation to zero.
     */
    fun resetKnobRotation() {
        totalRotationDegrees = 0f
        lastVibrateAngle = 0f
        lastAnnouncedRotation = 0f
        rotationMinutes = 0
        onRotationChangedListener?.invoke(totalRotationDegrees)
        invalidate()
    }
}
