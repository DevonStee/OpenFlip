package com.bokehforu.openflip.feature.clock.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.core.ui.feedback.performSystemHapticClick
import com.bokehforu.openflip.core.util.resolveThemeColor
import kotlin.math.min
import com.bokehforu.openflip.core.R as CoreR

/**
 * A custom view for the state toggle button that mimics the "Resin" look of InfiniteKnobView.
 * Features a beveled edge, matte surface, and an internal glow ring.
 */
class StateToggleGlowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var glowEnabled = false
    
    companion object {
        const val GAP_WIDTH_DP = 0.5f        // Precision seam
        const val PRESSED_SCALE = 0.97f      // Physical press depth
        const val ANIM_DURATION = 350L       // Rhythmic light transition
    }


    /** Public read-only access to glow state for UI visibility logic */
    val isGlowEnabled: Boolean
        get() = glowEnabled
    private var isDarkTheme = true

    // Animation states
    private var glowFraction = 0f
    private var pressFraction = 0f
    private val argbEvaluator = ArgbEvaluator()
    private val interpolator = DecelerateInterpolator()

    // Animators
    private var glowAnimator: ValueAnimator? = null
    private var pressAnimator: ValueAnimator? = null

    // Countdown display
    private var countdownSeconds = 0
    private var countdownText = "0"
    private var countdownTypeface: android.graphics.Typeface? = null
    private val countdownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    // === PAINTS ===
    
    // Outer Bezel Ring (matches InfiniteKnobView aesthetic)
    private val bezelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE 
        isDither = true
    }
    
    // Bezel Bevel (subtle 3D edge)
    private val bezelBevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    
    // Inner Lens (frosted glass surface)
    private val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }
    
    // Inner Shadow (creates recessed look inside lens)
    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    
    // Halo Glow (for ON state, drawn behind bezel)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
        maskFilter = android.graphics.BlurMaskFilter(12f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    // Rim Light (sharp glass edge highlight for ON state)
    private val rimLightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = context.resolveThemeColor(CoreR.attr.stateToggleRimLightColor, CoreR.style.Theme_OpenFlip_Dark)
        maskFilter = android.graphics.BlurMaskFilter(8f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    // Drop Shadow (for physical lift)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Bloom Spill (simulates light eating the bezel edge)
    private val bloomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.resolveThemeColor(CoreR.attr.stateTogglePressedOverlayColor, CoreR.style.Theme_OpenFlip_Dark)
    }

    private var isPressedState = false

    // Geometry
    private var centerX = 0f
    private var centerY = 0f
    private var outerRadius = 0f    // Full button radius (matches knob)
    private var bezelWidth = 0f     // Thickness of the dark rim
    private var lensRadius = 0f     // Inner glass surface radius
    private var density = 1f

    // Extra paint for "Active" lens overlay to enable Layering animation
    private val activeLensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }

    // New: Ambient Highlight for surface reflection
    private val ambientHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }

    // New: Specific Inner Shadow for button volume depth
    private val lensInnerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }

    init {
        setWillNotDraw(false)
        updateColors()
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "灯光按钮"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animatePress(1f)
                return true
            }
            MotionEvent.ACTION_UP -> {
                animatePress(0f)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                animatePress(0f)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        performSystemHapticClick()
        return true
    }

    private fun animatePress(target: Float) {
        pressAnimator?.cancel()
        pressAnimator = ValueAnimator.ofFloat(pressFraction, target).apply {
            duration = 150L
            interpolator = this@StateToggleGlowView.interpolator
            addUpdateListener {
                pressFraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        density = resources.displayMetrics.density
        centerX = w / 2f
        centerY = h / 2f

        // 3. Layout Flexibility: Adaptive Size
        // Match InfiniteKnobView sizing logic: (64dp - 2 * 3.75dp) / 2 = 28.25dp
        // To get lensRadius = 28.25dp, we need outerRadius = 28.25 + 0.5 = 28.75dp
        val targetLensRadius = 28.25f * density
        lensRadius = targetLensRadius
        outerRadius = lensRadius + (0.5f * density)
        
        // Geometric Gap
        bezelWidth = GAP_WIDTH_DP * density 

        countdownPaint.textSize = lensRadius * 0.254f
        
        updatePaints()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        applyPressScale(canvas)

        drawHaloGlow(canvas)
        drawRecessedGapShadow(canvas)
        drawMainButtonSurface(canvas)
        drawPressedOverlay(canvas)
        drawCountdownText(canvas)

        canvas.restore()
    }

    private fun applyPressScale(canvas: Canvas) {
        val currentScale = 1f - (pressFraction * (1f - PRESSED_SCALE))
        canvas.scale(currentScale, currentScale, centerX, centerY)
    }

    private fun drawHaloGlow(canvas: Canvas) {
        if (glowFraction > 0f) {
            haloPaint.alpha = (glowFraction * 255).toInt()
            canvas.drawCircle(centerX, centerY, outerRadius, haloPaint)
        }
    }

    private fun drawRecessedGapShadow(canvas: Canvas) {
        // Physical edge lines removed for a cleaner look
    }

    private fun drawMainButtonSurface(canvas: Canvas) {
        // 3a. Draw "Off" Lens (Base - Milky White)
        lensPaint.alpha = 255
        canvas.drawCircle(centerX, centerY, lensRadius, lensPaint)

        // 3b. Dark Mode Physicality (Off State Only)
        if (glowFraction < 1f) {
            val physicalityAlpha = ((1f - glowFraction) * 255).toInt()

            // Inner Shadow for recessed volume
            lensInnerShadowPaint.alpha = physicalityAlpha
            canvas.drawCircle(centerX, centerY, lensRadius, lensInnerShadowPaint)

            // Ambient highlight (Top-Left)
            ambientHighlightPaint.alpha = (physicalityAlpha * 0.4f).toInt()
            canvas.drawCircle(centerX, centerY, lensRadius, ambientHighlightPaint)
        }

        // 3c. Draw "Active" Lens Overlay (Volumetric Solid Light)
        if (glowFraction > 0f) {
            activeLensPaint.alpha = (glowFraction * 255).toInt()
            canvas.drawCircle(centerX, centerY, lensRadius, activeLensPaint)
        }
    }

    private fun drawPressedOverlay(canvas: Canvas) {
        if (pressFraction > 0f) {
            pressedPaint.alpha = (pressFraction * 16).toInt()
            canvas.drawCircle(centerX, centerY, lensRadius, pressedPaint)
        }
    }

    private fun drawCountdownText(canvas: Canvas) {
        if (countdownSeconds > 0) {
            countdownPaint.typeface = countdownTypeface
            val textY = centerY - (countdownPaint.descent() + countdownPaint.ascent()) / 2f
            canvas.drawText(countdownText, centerX, textY, countdownPaint)
        }
    }

    fun setGlowEnabled(enabled: Boolean) {
        if (glowEnabled != enabled) {
            glowEnabled = enabled
            animateGlow(if (enabled) 1f else 0f)
            contentDescription = if (enabled) "灯光按钮，已开启" else "灯光按钮，已关闭"
        }
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.contentDescription = contentDescription
        info.isClickable = true
    }

    private fun animateGlow(target: Float) {
        glowAnimator?.cancel()
        glowAnimator = ValueAnimator.ofFloat(glowFraction, target).apply {
            duration = ANIM_DURATION
            interpolator = this@StateToggleGlowView.interpolator
            addUpdateListener {
                glowFraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setTheme(isDark: Boolean) {
        if (isDarkTheme != isDark) {
            isDarkTheme = isDark
            updateColors()
            invalidate()
        }
    }

    fun setCountdown(seconds: Int) {
        if (countdownSeconds != seconds) {
            countdownSeconds = seconds
            countdownText = seconds.toString()
            invalidate()
        }
    }

    fun setTypeface(tf: android.graphics.Typeface) {
        countdownTypeface = tf
        invalidate()
    }

    /**
     * Recreates Shaders and Paints based on current size and theme.
     * Called only when size changes or theme changes. Avoids allocation in onDraw.
     */
    private fun updatePaints() {
        val density = resources.displayMetrics.density

        // --- 1. Material Palette (Dieter Rams / Braun) ---
        val themeRes = if (isDarkTheme) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val colorOff = context.resolveThemeColor(CoreR.attr.stateToggleOffColor, themeRes)
        
        // --- 2. Button Surface (Frosted Acrylic) ---
        if (outerRadius > 0) {
            // Fine Bezel (Physical edge definition)
            bezelPaint.style = Paint.Style.STROKE
            bezelPaint.strokeWidth = 0.5f * density
            bezelPaint.color = Color.WHITE // Alpha handled in onDraw (40% base)

            lensPaint.shader = RadialGradient(
                centerX, centerY, outerRadius,
                intArrayOf(colorOff, colorOff),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )

            // --- 3. Recessed Gap Shadow (AO) ---
            // Refined to "Assembly Gap": Ultra-thin, near-invisible seam.
            innerShadowPaint.shader = null 
            innerShadowPaint.color = Color.BLACK
            innerShadowPaint.strokeWidth = 0.5f * density
            innerShadowPaint.maskFilter = null

            // --- 4. Active State (Volumetric Light / Bloom) ---
            // Centers the shape when the gap shadow is hidden.
            val colorWhite = Color.WHITE
            val colorPaleYellow = context.resolveThemeColor(CoreR.attr.glassCoverCoreCenterColor, themeRes)
            val colorYellow = context.resolveThemeColor(CoreR.attr.glassCoverCoreEdgeColor, themeRes)
            
            activeLensPaint.shader = RadialGradient(
                centerX, centerY, lensRadius + (0.5f * density),
                intArrayOf(colorWhite, colorWhite, colorYellow), 
                floatArrayOf(0.0f, 0.2f, 1.0f), // Smoother transition from core to edge
                Shader.TileMode.CLAMP
            )

            // --- 5. Ambient Halo (Physics-based spill) ---
            val haloColor = context.resolveThemeColor(CoreR.attr.glassCoverHaloColor, themeRes)
            // Halo radius must stay within view bounds (32dp from center, view is 64x64dp)
            val haloRadius = outerRadius + (2f * density) // ~30.75dp, leaves room for blur
            haloPaint.shader = RadialGradient(
                centerX, centerY, haloRadius,
                intArrayOf(Color.argb(50, Color.red(haloColor), Color.green(haloColor), Color.blue(haloColor)), Color.TRANSPARENT),
                floatArrayOf(0.6f, 1.0f), // Start fade earlier, ensure complete fade at edge
                Shader.TileMode.CLAMP
            )

            // --- 6. Dark Mode Physicality Details ---
            
            // Inner Shadow for volume (Center light, Edge dark)
            lensInnerShadowPaint.shader = RadialGradient(
                centerX, centerY, lensRadius,
                intArrayOf(Color.TRANSPARENT, Color.argb(if (isDarkTheme) 45 else 15, 0, 0, 0)),
                floatArrayOf(0.85f, 1.0f),
                Shader.TileMode.CLAMP
            )

            // Ambient Highlight (Soft 5% White reflection in Top-Left)
            ambientHighlightPaint.shader = LinearGradient(
                centerX - lensRadius, centerY - lensRadius, 
                centerX, centerY,
                intArrayOf(Color.argb(if (isDarkTheme) 15 else 30, 255, 255, 255), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }

        // --- 6. Inner Rim Highlight (Crisp Precision) ---
        bezelBevelPaint.strokeWidth = 0.5f * density // Ultra-fine precision line
        bezelBevelPaint.color = Color.WHITE // Alpha handled in onDraw

        // Countdown Text - Always yellow for contrast on light glow button
        rimLightPaint.color = context.resolveThemeColor(CoreR.attr.stateToggleRimLightColor, themeRes)
        pressedPaint.color = context.resolveThemeColor(CoreR.attr.stateTogglePressedOverlayColor, themeRes)
        countdownPaint.color = context.resolveThemeColor(CoreR.attr.stateToggleCountdownTextColor, themeRes)
    }

    private fun updateColors() {
        updatePaints()
        invalidate()
    }

}
