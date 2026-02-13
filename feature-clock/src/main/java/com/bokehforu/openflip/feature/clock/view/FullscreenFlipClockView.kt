package com.bokehforu.openflip.feature.clock.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.view.card.FlipCardComponent
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.feature.clock.view.animation.FlipAnimationManager
import com.bokehforu.openflip.feature.clock.view.renderer.LightOverlayRenderer
import com.bokehforu.openflip.feature.clock.view.theme.FlipClockThemeApplier
import com.bokehforu.openflip.core.util.resolveThemeColor
import com.bokehforu.openflip.core.util.FontProvider
import com.bokehforu.openflip.core.R as CoreR

class FullscreenFlipClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hourCard = FlipCardComponent()
    private val minuteCard = FlipCardComponent()
    private val animationManager: FlipAnimationManager
    private val themeApplier: FlipClockThemeApplier
    private val lightOverlayRenderer: LightOverlayRenderer

    private var cardSpacing = 0f
    private var lastUpdateWidth = -1f
    private var lastUpdateHeight = -1f
    
     // Dependency Injection
     var sound: SoundProvider? = null
         set(value) {
             field = value
             animationManager.soundManager = value
         }
         
     var haptics: HapticsProvider? = null
         set(value) {
             field = value
             animationManager.hapticManager = value
         }
        
    var showFlaps = true
        set(value) {
            field = value
            animationManager.showFlaps = value
        }
        
    var isHourlyChimeEnabled = false
        set(value) {
            field = value
            animationManager.isHourlyChimeEnabled = value
        }
        
    var showSeconds = false
        set(value) {
            if (field != value) {
                field = value
                onSizeChanged(width, height, width, height)
                invalidate()
            }
        }
    private var currentScale = 1.0f
    private var cachedIsDarkTheme = true
    private var cachedBgColor: Int = Color.BLACK
    private var accessibilityTime: String = "--:--"

    /**
     * External background color override for smooth transitions.
     * When null, the theme-based color is used.
     */
    var backgroundColorOverride: Int? = null
        set(value) {
            field = value
            invalidate()
        }

    init {
        animationManager = FlipAnimationManager(context, hourCard, minuteCard, ::invalidate)
        themeApplier = FlipClockThemeApplier(context, hourCard, minuteCard)
        lightOverlayRenderer = LightOverlayRenderer(::invalidate)

        setDarkTheme(cachedIsDarkTheme)
        
        // Default to software layer to save memory; enable hardware only during animation
        setLayerType(LAYER_TYPE_NONE, null)

        runCatching {
            FontProvider.getClockTypeface(context).also { typeface ->
                hourCard.setTypeface(typeface)
                minuteCard.setTypeface(typeface)
            }
        }
    }
    
    fun applyScale(factor: Float) {
        val minScale = resources.getInteger(CoreR.integer.flip_clock_min_scale) / 100f
        val maxScale = resources.getInteger(CoreR.integer.flip_clock_max_scale) / 100f
        currentScale = (currentScale * factor).coerceIn(minScale, maxScale)

        scaleX = 1.0f
        scaleY = 1.0f
        onSizeChanged(width, height, width, height)
        invalidate()
    }

    fun resetScale() {
        currentScale = 1.0f
        scaleX = 1.0f
        scaleY = 1.0f
        onSizeChanged(width, height, width, height)
        invalidate()
    }

    fun setScale(scale: Float) {
        val minScale = resources.getInteger(CoreR.integer.flip_clock_min_scale) / 100f
        val maxScale = resources.getInteger(CoreR.integer.flip_clock_max_scale) / 100f
        val clamped = scale.coerceIn(minScale, maxScale)
        if (clamped != currentScale) {
            currentScale = clamped
            scaleX = 1.0f
            scaleY = 1.0f
            onSizeChanged(width, height, width, height)
            invalidate()
        }
    }

    fun getCurrentScale(): Float = currentScale

    fun setDarkTheme(isDark: Boolean) {
        cachedIsDarkTheme = isDark
        val themeRes = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        cachedBgColor = context.resolveThemeColor(CoreR.attr.appBackgroundColor, themeRes)
        themeApplier.applyTheme(isDark)
        invalidate()
    }

    /**
     * Set the intensity of the light casting effect.
     * @param intensity 0f = off, 1f = full brightness
     * @param animate if true, animate the transition
     */
    fun setLightIntensity(intensity: Float, animate: Boolean = true) {
        lightOverlayRenderer.setLightIntensity(intensity, animate)
    }

    /**
     * Set the light source position in view-local coordinates.
     * Call this whenever the button position changes (e.g., on rotation).
     * @param x X coordinate of the light source relative to this view
     * @param y Y coordinate of the light source relative to this view
     */
    fun setLightSourcePosition(x: Float, y: Float) {
        lightOverlayRenderer.setLightSourcePosition(x, y)
    }

    fun setLightSourceVisible(visible: Boolean) {
        lightOverlayRenderer.setSourceVisible(visible)
    }

    fun updateTheme(isDark: Boolean) {
        setDarkTheme(isDark)
    }

    private var isVertical = false
    private var hourCenterOffsetX = 0f
    private var hourCenterOffsetY = 0f
    private var minuteCenterOffsetX = 0f
    private var minuteCenterOffsetY = 0f
    private var isRotating = false
    private var lastDisplayRotation = -1
    private var animatedHourCenterOffsetX = 0f
    private var animatedHourCenterOffsetY = 0f
    private var animatedMinuteCenterOffsetX = 0f
    private var animatedMinuteCenterOffsetY = 0f
    private var animatedCardWidth = 0f
    private var animatedCardHeight = 0f
    private var targetCardWidth = 0f
    private var targetCardHeight = 0f

    private fun getDisplayRotation(): Int = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        context.display?.rotation ?: 0
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.rotation
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        lastDisplayRotation = getDisplayRotation()
        val newIsVertical = h > w
        val padding = if (newIsVertical) w * 0.1f else w * 0.05f
        cardSpacing = (if (newIsVertical) h * 0.032f else w * 0.032f) * currentScale

        val squareSize = if (newIsVertical) {
            val maxCardW = (w - (2 * padding)) * currentScale
            val maxCardH = (h - (2 * padding) - cardSpacing) / 2f
            minOf(maxCardW, maxCardH)
        } else {
            val maxCardH = (h - (2 * padding)) * currentScale
            val maxCardW = (w - (2 * padding) - cardSpacing) / 2f
            minOf(maxCardW, maxCardH)
        }

        targetCardWidth = squareSize
        targetCardHeight = squareSize
        val targetHourOffsetX = if (newIsVertical) 0f else -(squareSize + cardSpacing) / 2f
        val targetHourOffsetY = if (newIsVertical) -(squareSize + cardSpacing) / 2f else 0f
        val targetMinuteOffsetX = if (newIsVertical) 0f else (squareSize + cardSpacing) / 2f
        val targetMinuteOffsetY = if (newIsVertical) (squareSize + cardSpacing) / 2f else 0f

        hourCard.setDimensions(squareSize, squareSize, resources.displayMetrics.density)
        minuteCard.setDimensions(squareSize, squareSize, resources.displayMetrics.density)

        isVertical = newIsVertical
        hourCenterOffsetX = targetHourOffsetX
        hourCenterOffsetY = targetHourOffsetY
        minuteCenterOffsetX = targetMinuteOffsetX
        minuteCenterOffsetY = targetMinuteOffsetY

        snapToNewLayout()

        lastUpdateWidth = squareSize
        lastUpdateHeight = squareSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bgColor = backgroundColorOverride ?: cachedBgColor
        canvas.drawColor(bgColor)

        if (width <= 0 || height <= 0) return

        val centerX = width / 2f
        val centerY = height / 2f

        if (animatedCardWidth > 0 && animatedCardHeight > 0) {
            if (kotlin.math.abs(animatedCardWidth - lastUpdateWidth) > 0.5f ||
                kotlin.math.abs(animatedCardHeight - lastUpdateHeight) > 0.5f) {
                hourCard.setDimensions(animatedCardWidth, animatedCardHeight, resources.displayMetrics.density)
                minuteCard.setDimensions(animatedCardWidth, animatedCardHeight, resources.displayMetrics.density)
                lastUpdateWidth = animatedCardWidth
                lastUpdateHeight = animatedCardHeight
            }
        }
        canvas.save()
        canvas.translate(centerX + animatedHourCenterOffsetX, centerY + animatedHourCenterOffsetY)
        canvas.translate(-animatedCardWidth / 2f, -animatedCardHeight / 2f)
        hourCard.draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(centerX + animatedMinuteCenterOffsetX, centerY + animatedMinuteCenterOffsetY)
        canvas.translate(-animatedCardWidth / 2f, -animatedCardHeight / 2f)
        minuteCard.draw(canvas)
        canvas.restore()

        // Light overlay: compensate for OLED shift so overlay stays in screen coordinates
        // This prevents edge gaps when DisplayBurnInProtectionManager shifts the view
        canvas.save()
        canvas.translate(-translationX, -translationY)
        lightOverlayRenderer.draw(
            canvas,
            width.toFloat(),
            height.toFloat(),
            cachedIsDarkTheme,
            centerX,
            centerY
        )
        canvas.restore()
    }

    private fun snapToNewLayout() {
        animatedHourCenterOffsetX = hourCenterOffsetX
        animatedHourCenterOffsetY = hourCenterOffsetY
        animatedMinuteCenterOffsetX = minuteCenterOffsetX
        animatedMinuteCenterOffsetY = minuteCenterOffsetY
        animatedCardWidth = targetCardWidth
        animatedCardHeight = targetCardHeight
        invalidate()
    }
                    

    fun setTime(hour: String, minute: String, animate: Boolean = true, amPm: String? = null) {
        setTimeWithDirection(hour, minute, isDecreasing = false, animate = animate, amPm = amPm)
    }

    fun playEntranceAnimation() {
        animationManager.playEntranceAnimation(isAttachedToWindow)
    }

    private fun setHardwareLayerEnabled(enabled: Boolean) {
        val type = if (enabled) LAYER_TYPE_HARDWARE else LAYER_TYPE_NONE
        if (layerType != type) {
            setLayerType(type, null)
        }
    }
    fun setTimeWithDirection(
        hour: String,
        minute: String,
        isDecreasing: Boolean,
        animate: Boolean = true,
        amPm: String? = null
    ) {
        if (hour != hourCard.currentValue || amPm != hourCard.amPmText) {
            hourCard.amPmText = amPm
            if (animate && hour != hourCard.currentValue) {
                setHardwareLayerEnabled(true)
                animationManager.flipHour(hour, isReverse = isDecreasing) { setHardwareLayerEnabled(false) }
            } else {
                hourCard.apply {
                    currentValue = hour
                    nextValue = hour
                    flipDegree = 0f
                }
                invalidate()
            }
        }

        if (minute != minuteCard.currentValue) {
            if (animate) {
                setHardwareLayerEnabled(true)
                animationManager.flipMinute(minute, isReverse = isDecreasing) { setHardwareLayerEnabled(false) }
            } else {
                minuteCard.apply {
                    currentValue = minute
                    nextValue = minute
                    flipDegree = 0f
                }
                invalidate()
            }
        }

        accessibilityTime = if (amPm.isNullOrEmpty()) {
            "$hour:$minute"
        } else {
            "$hour:$minute $amPm"
        }
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.contentDescription = context.getString(CoreR.string.accessibility_current_time_template, accessibilityTime)
        info.isClickable = true
    }

    fun pauseAnimations() {
        animationManager.cancelAll()
        lightOverlayRenderer.cleanup()
        setHardwareLayerEnabled(false)
    }

    fun resumeAnimations() {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationManager.cancelAll()

        lightOverlayRenderer.cleanup()
        setHardwareLayerEnabled(false)
    }
}
