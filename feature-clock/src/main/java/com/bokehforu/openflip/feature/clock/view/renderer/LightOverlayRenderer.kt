package com.bokehforu.openflip.feature.clock.view.renderer

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Renders the dynamic light overlay effect on the clock view.
 * Handles the radial gradient generation and animation logic.
 */
class LightOverlayRenderer(
    private val onInvalidate: () -> Unit
) {

    var lightIntensity = 0f
        private set
    
    // Track if source button is visible (hidden during rotation)
    private var sourceVisible = true
        
    private var lightAnimator: ValueAnimator? = null
    private var currentRadius = 0f
    private var lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Default xfermode
        xfermode = null
    }
    
    private var lightGradientDirty = true
    private var cachedIsDarkTheme: Boolean? = null
    
    // Light source position (view-relative)
    private var lightSourceX: Float = -1f
    private var lightSourceY: Float = -1f

    fun setLightIntensity(intensity: Float, animate: Boolean = true) {
        val targetIntensity = intensity.coerceIn(0f, 1f)
        
        lightAnimator?.cancel()
        
        if (animate) {
            lightAnimator = ValueAnimator.ofFloat(lightIntensity, targetIntensity).apply {
                duration = 300L
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    lightIntensity = it.animatedValue as Float
                    onInvalidate()
                }
                start()
            }
        } else {
            lightIntensity = targetIntensity
            onInvalidate()
        }
    }

    fun setLightSourcePosition(x: Float, y: Float) {
        if (lightSourceX != x || lightSourceY != y) {
            lightSourceX = x
            lightSourceY = y
            lightGradientDirty = true
            if (lightIntensity > 0f) {
                onInvalidate()
            }
        }
    }

    fun setSourceVisible(visible: Boolean) {
        if (sourceVisible != visible) {
            sourceVisible = visible
            if (lightIntensity > 0f) {
                onInvalidate()
            }
        }
    }

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        isDarkTheme: Boolean,
        minuteCardCenterX: Float,
        minuteCardCenterY: Float
    ) {
        // Only draw if light is on AND source button is visible
        if (lightIntensity <= 0f || !sourceVisible) return
        
        // Check theme change
        if (cachedIsDarkTheme != isDarkTheme) {
            lightGradientDirty = true
            cachedIsDarkTheme = isDarkTheme
        }
        
        if (lightGradientDirty) {
            updateGradient(width, height, isDarkTheme, minuteCardCenterX, minuteCardCenterY)
        }
        
        // Scale precomputed gradient by intensity without rebuilding shaders.
        lightPaint.alpha = (lightIntensity * 255f).toInt().coerceIn(0, 255)

        // Renders using drawRect as gradient fades to transparent within bounds.
        // The color stops ensure 1.0 position (transparent) is BEYOND all corners.
        canvas.drawRect(0f, 0f, width, height, lightPaint)
    }
    
    private fun updateGradient(
        width: Float, 
        height: Float, 
        isDarkTheme: Boolean,
        minuteCardCenterX: Float,
        minuteCardCenterY: Float
    ) {
        val sourceX = if (lightSourceX >= 0f) lightSourceX else width * 0.15f
        val sourceY = if (lightSourceY >= 0f) lightSourceY else height * 0.95f
        
        // Calculate the farthest corner from the light source to ensure full coverage
        val dTopLeft = kotlin.math.hypot(sourceX.toDouble(), sourceY.toDouble())
        val dTopRight = kotlin.math.hypot((width - sourceX).toDouble(), sourceY.toDouble())
        val dBottomLeft = kotlin.math.hypot(sourceX.toDouble(), (height - sourceY).toDouble())
        val dBottomRight = kotlin.math.hypot((width - sourceX).toDouble(), (height - sourceY).toDouble())
        val maxCornerDistance = maxOf(dTopLeft, dTopRight, dBottomLeft, dBottomRight).toFloat()

        // Add a small buffer to ensure coverage even with OLED shifts
        val bufferSize = maxCornerDistance * 0.20f
        currentRadius = maxCornerDistance + bufferSize
        
        if (isDarkTheme) {
            // Dark Mode
            val centerAlpha = (255 * 0.85f).toInt()
            val nearAlpha = (255 * 0.45f).toInt()
            val midAlpha = (255 * 0.15f).toInt()
            val farAlpha = (255 * 0.02f).toInt()

            val colorCenter = Color.argb(centerAlpha, 255, 200, 80)
            val colorNear = Color.argb(nearAlpha, 255, 180, 60)
            val colorMid = Color.argb(midAlpha, 255, 165, 40)
            val colorFar = Color.argb(farAlpha, 255, 150, 30)

            lightPaint.shader = RadialGradient(
                sourceX, sourceY, currentRadius,
                intArrayOf(colorCenter, colorNear, colorMid, colorFar, Color.TRANSPARENT),
                floatArrayOf(0.0f, 0.15f, 0.40f, 0.85f, 1.0f), // Fade to transparent at 85%-100%
                Shader.TileMode.CLAMP
            )
            lightPaint.xfermode = null // avoid software fallback
        } else {
            // Light Mode
            val centerAlpha = (255 * 0.60f).toInt()
            val nearAlpha = (255 * 0.28f).toInt()
            val midAlpha = (255 * 0.10f).toInt()
            val farAlpha = (255 * 0.02f).toInt()

            val colorCenter = Color.argb(centerAlpha, 255, 210, 120)
            val colorNear = Color.argb(nearAlpha, 255, 200, 110)
            val colorMid = Color.argb(midAlpha, 255, 190, 100)
            val colorFar = Color.argb(farAlpha, 255, 180, 90)

            lightPaint.shader = RadialGradient(
                sourceX, sourceY, currentRadius,
                intArrayOf(colorCenter, colorNear, colorMid, colorFar, Color.TRANSPARENT),
                floatArrayOf(0.0f, 0.20f, 0.50f, 0.85f, 1.0f), // Fade to transparent at 85%-100%
                Shader.TileMode.CLAMP
            )
            lightPaint.xfermode = null // SRC_OVER
        }
        
        lightGradientDirty = false
    }

    fun cleanup() {
        lightAnimator?.cancel()
        lightAnimator = null
    }
}
