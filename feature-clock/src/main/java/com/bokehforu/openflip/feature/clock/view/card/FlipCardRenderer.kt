package com.bokehforu.openflip.feature.clock.view.card

import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint

/**
 * Pure rendering class for the flip card.
 * 
 * This class has ONE job: draw things on a Canvas.
 * It does NOT manage state, animations, or business logic.
 * 
 * All decisions about WHAT to draw are made by the caller (via FlipCardState).
 * This class only handles HOW to draw it.
 */
class FlipCardRenderer(
    private val config: FlipCardConfig = FlipCardConfig(),
    private val geometry: FlipCardGeometry = FlipCardGeometry(config)
) {
    private companion object {
        const val DEFAULT_CUT_HIGHLIGHT_COLOR = "#30FFFFFF"
        const val DEFAULT_CUT_SHADOW_COLOR = "#38000000"
    }

    // Paints (pre-allocated to avoid GC during draw)
    private val solidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val topGradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val bottomGradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
        style = Paint.Style.FILL
        color = Color.BLACK
    }
    private val shadowEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
        color = Color.BLACK
        alpha = 35
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        isFakeBoldText = false
    }
    private val amPmPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    
    // Detailed Rendering Paints
    private val noisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isDither = true; isFilterBitmap = true }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f // Reduced from 2f for a sharper, single-layer look
        isDither = true
    }
    private val highlightCutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        color = Color.parseColor(DEFAULT_CUT_HIGHLIGHT_COLOR) // Position 1: Cut Highlight (Safe default)
    }
    private val shadowCutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        color = Color.parseColor(DEFAULT_CUT_SHADOW_COLOR) // Position 2: Cut Shadow (Safe default)
    }
    
    // Side gradient stroke paint for bottom half (vertical fade from center to corners)
    private val sideStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }
    private var topSideStrokeShader: android.graphics.Shader? = null
    private var bottomSideStrokeShader: android.graphics.Shader? = null
    
    // 3D transformation
    private val camera = Camera()
    private val matrix = Matrix()
    
    // Temporary path for edge stroke drawing (reused to avoid allocations)
    private val edgeStrokePath = Path()
    
    // Text measurement cache
    private val textBounds = Rect()

    /**
     * Cached vertical text offset to center digits.
     * Calculated based on "0" and applied to all digits for consistent baseline.
     * Recalculated when dimensions change to avoid per-frame measurement overhead.
     */
    private var cachedTextOffset = 0f
    private var cachedNumberVisualTop = 0f
    private var cachedAmPmSize = 0f
    private var cachedAmBoundsTop = 0f
    private var cachedPmBoundsTop = 0f

    // Ink centering cache (per-value)
    private var cachedCurrentValue: String? = null
    private var cachedCurrentInkCenterX = 0f
    private var cachedNextValue: String? = null
    private var cachedNextInkCenterX = 0f

    // Color state
    private var colors = CardColors()
    private var lastShaderWidth = 0f
    private var lastShaderHeight = 0f
    
    init {
        // Initialize noise shader with a default. 
        // Actual noise color will be set via setColors() which is called on theme initialization.
        noisePaint.shader = createNoiseShader(intensity = 20, noiseColor = Color.DKGRAY)
    }

    /**
     * Update dimensions. Call when size changes.
     */
    fun setDimensions(width: Float, height: Float, density: Float = 1f) {
        geometry.updateDimensions(width, height, density)
        camera.setLocation(0f, 0f, config.cameraLocationZ * density)

        // Update text sizing
        val h = geometry.height
        val w = geometry.width
        textPaint.textSize = h * config.textSizeRatio
        val textWidth = textPaint.measureText("00")
        if (textWidth > w * 0.9f) textPaint.textSize *= (w * 0.9f / textWidth)

        // Cache vertical text offset
        textPaint.getTextBounds("0", 0, 1, textBounds)
        cachedTextOffset = -(textBounds.top + textBounds.bottom) / 2f + h * config.textVerticalOffsetRatio
        cachedNumberVisualTop = textBounds.top.toFloat()

        // Cache AM/PM measurements
        cachedAmPmSize = textPaint.textSize * config.amPmSizeRatio
        amPmPaint.textSize = cachedAmPmSize
        amPmPaint.getTextBounds("AM", 0, 2, textBounds)
        cachedAmBoundsTop = textBounds.top.toFloat()
        amPmPaint.getTextBounds("PM", 0, 2, textBounds)
        cachedPmBoundsTop = textBounds.top.toFloat()
        
        // Refresh shaders if size changed
        if (lastShaderWidth != w || lastShaderHeight != h) {
            refreshGradients()
            lastShaderWidth = w
            lastShaderHeight = h
        }
        
        // Invalidate text caches
        cachedCurrentValue = null
        cachedNextValue = null
    }
    
    /**
     * Update colors and theme. Call when theme changes.
     */
    fun setColors(
        textColor: Int, 
        cardColor: Int, 
        shadowColor: Int, 
        shadowEdgeColor: Int,
        rimHighlightColor: Int = Color.TRANSPARENT,
        cutHighlightColor: Int = Color.TRANSPARENT,
        cutShadowColor: Int = Color.TRANSPARENT,
        noiseColor: Int = Color.TRANSPARENT
    ) {
        val oldNoiseColor = colors.noiseColor
        colors = CardColors(
            textColor, cardColor, shadowColor, shadowEdgeColor, 
            rimHighlightColor, cutHighlightColor, cutShadowColor, noiseColor
        )
        
        textPaint.color = textColor
        textPaint.alpha = 242 // ~95% opacity to blend with noise
        solidPaint.color = cardColor
        shadowPaint.color = shadowColor
        shadowEdgePaint.color = shadowEdgeColor
        shadowEdgePaint.alpha = if (colors.isDarkTheme) config.darkThemeShadowAlpha else config.lightThemeShadowAlpha
        
        highlightCutPaint.color = cutHighlightColor
        shadowCutPaint.color = cutShadowColor
        
        // Side stroke uses rim highlight color for consistent edge lighting
        sideStrokePaint.color = rimHighlightColor
        
        // Regenerate noise shader if the color changed (e.g., from white to black dots)
        if (noiseColor != oldNoiseColor) {
            noisePaint.shader = createNoiseShader(intensity = 20, noiseColor = noiseColor)
        }
        
        // Reset border shader to force regeneration with new rim colors
        borderPaint.shader = null
        
        refreshGradients()
    }
    
    /**
     * Set the typeface for digit rendering.
     */
    fun setTypeface(typeface: android.graphics.Typeface?) {
        typeface?.let { textPaint.typeface = it }
    }
    
    /**
     * Main draw entry point.
     * Takes a FlipCardState and renders the appropriate visuals.
     */
    fun draw(canvas: Canvas, state: FlipCardState) {
        if (state.isAtRest) {
            drawStaticCard(canvas, state)
        } else {
            drawAnimatedCard(canvas, state)
        }
    }
    
    /**
     * Draw the card in static (rest) state.
     * Both halves show the same value with identical solid color.
     */
    private fun drawStaticCard(canvas: Canvas, state: FlipCardState) {
        // Top half
        drawHalf(canvas, geometry.topRect, state.currentValue, isTop = true, solidPaint, state.amPmText)
        // Bottom half
        drawHalf(canvas, geometry.bottomRect, state.currentValue, isTop = false, solidPaint, null)
    }
    
    /**
     * Draw the card during flip animation.
     */
    private fun drawAnimatedCard(canvas: Canvas, state: FlipCardState) {
        // Layer 1: Draw the "revealed" background layers
        drawRevealedLayers(canvas, state)
        
        // Layer 2: Draw the rotating flap on top
        drawRotatingFlap(canvas, state)
    }
    
    /**
     * Draw the static layers that are revealed as the flap rotates.
     */
    private fun drawRevealedLayers(canvas: Canvas, state: FlipCardState) {
        // Top half: shows topLayerValue (respects isReversing)
        val topPaint = if (colors.isDarkTheme) topGradientPaint else solidPaint
        drawHalf(canvas, geometry.topRect, state.topLayerValue, isTop = true, topPaint, state.amPmText)
        applyShadowOverlay(canvas, geometry.topRect, state.topRevealedShadowIntensity, config.topHalfMaxShadow)
        
        // Bottom half: shows bottomLayerValue (respects isReversing)
        val bottomPaint = if (colors.isDarkTheme) bottomGradientPaint else solidPaint
        drawHalf(canvas, geometry.bottomRect, state.bottomLayerValue, isTop = false, bottomPaint, null)
        applyShadowOverlay(canvas, geometry.bottomRect, state.bottomRevealedShadowIntensity, config.bottomHalfMaxShadow)
    }
    
    /**
     * Draw the rotating flap with 3D perspective.
     */
    private fun drawRotatingFlap(canvas: Canvas, state: FlipCardState) {
        val rect = if (state.isFlapTop) geometry.topRect else geometry.bottomRect
        val value = state.flapValue
        val rotationDegrees = state.flapRotationDegrees
        
        canvas.save()
        
        // Apply 3D rotation
        camera.save()
        camera.rotateX(rotationDegrees)
        camera.getMatrix(matrix)
        matrix.preTranslate(-geometry.centerX, -geometry.centerY)
        matrix.postTranslate(geometry.centerX, geometry.centerY)
        canvas.concat(matrix)
        
        // Draw flap shadow (the shadow cast BY the flap onto what's behind it)
        drawFlapCastShadow(canvas, rect)
        
        // Draw the flap itself
        val flapPaint = if (colors.isDarkTheme) {
            if (state.isFlapTop) topGradientPaint else bottomGradientPaint
        } else {
            solidPaint
        }
        drawHalf(canvas, rect, value, state.isFlapTop, flapPaint, if (state.isFlapTop) state.amPmText else null)
        
        // Draw self-shadow on the flap (darkening as it rotates)
        applyShadowOverlay(canvas, rect, state.flapShadowIntensity, 
            if (colors.isDarkTheme) config.darkThemeMaxFlapShadow else config.lightThemeMaxFlapShadow)
        
        camera.restore()
        canvas.restore()
    }
    
    /**
     * Draw the shadow cast by the rotating flap onto the revealed layer behind it.
     */
    private fun drawFlapCastShadow(canvas: Canvas, rect: RectF) {
        canvas.save()
        canvas.translate(0f, geometry.proportionalRotationTranslate)
        // High quality rectangle clip
        canvas.clipRect(rect)
        shadowPaint.alpha = if (colors.isDarkTheme) config.darkThemeShadowAlpha else config.lightThemeShadowAlpha
        canvas.drawPath(geometry.fullCardPath, shadowPaint)
        canvas.restore()
    }
    
    /**
     * Draw a single half of the card (top or bottom).
     */
    private fun drawHalf(canvas: Canvas, rect: RectF, text: String, isTop: Boolean, paint: Paint, amPm: String?) {
        canvas.save()
        // Allow a margin for outer strokes (rim light) while strictly clipping at the split line.
        val margin = 5f
        if (isTop) {
            canvas.clipRect(-margin, -margin, geometry.width + margin, rect.bottom)
        } else {
            canvas.clipRect(-margin, rect.top, geometry.width + margin, geometry.height + margin)
        }
        
        // Draw card background (Base Color)
        canvas.drawPath(geometry.fullCardPath, paint)
        
        // Apply realistic details (Noise, Border, Depth Lines)
        // We draw these BEFORE text so text sits "on top" (printed) 
        // but text alpha will let some noise bleed through naturally.
        drawRealisticDetails(canvas, rect, isTop)
        
        // Draw split line shadow (Classic shadow, keeping for legacy depth)
        if (isTop) {
            canvas.drawLine(
                rect.left, 
                rect.bottom - geometry.proportionalStrokeWidth, 
                rect.right, 
                rect.bottom - geometry.proportionalStrokeWidth, 
                shadowEdgePaint
            )
        }
        
        // Draw text (optically centered)
        drawCenteredText(canvas, text)
        
        // Draw AM/PM indicator if present
        if (isTop && amPm != null) {
            drawAmPm(canvas, amPm)
        }
        
        canvas.restore()
    }

    /**
     * Draw realistic details: Border gradient, Surface Noise, and Split Depth.
     */
    private fun drawRealisticDetails(canvas: Canvas, rect: RectF, isTop: Boolean) {
        val radius = geometry.cornerRadius
        
        // 1. Draw Noise Overlay (Seamless Texture)
        noisePaint.alpha = if (colors.isDarkTheme) 60 else 40
        // Draw using the full card path; the clipRect in drawHalf ensures it only shows in the half.
        // This avoids Path.op intersection jaggies.
        canvas.drawPath(geometry.fullCardPath, noisePaint)
        
        // 2. Draw Gradient Border (Position 0: Rim Light)
        // Using a linear gradient to simulate top-down lighting on the beveled edge
        val height = geometry.height
        if (borderPaint.shader == null || lastShaderHeight != height) {
             borderPaint.shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height,
                intArrayOf(
                    colors.rimHighlightColor, // Top: Theme-resolved highlight
                    Color.TRANSPARENT         // Bottom: Sharp fade to transparent
                ),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        // Draw using the full card path; let clipRect handle the split.
        canvas.drawPath(geometry.fullCardPath, borderPaint)

        // 3. Draw side gradient strokes on both halves for consistent rim light effect
        // Use rim highlight color to match the outer border edge lighting
        if (isTop) {
            // Top half: gradient from seam (bottom of rect) up to top corners
            // Direction: fade from rim color at seam to transparent at top
            sideStrokePaint.shader = topSideStrokeShader
            geometry.buildTopLeftEdgePath(edgeStrokePath)
            canvas.drawPath(edgeStrokePath, sideStrokePaint)
            
            geometry.buildTopRightEdgePath(edgeStrokePath)
            canvas.drawPath(edgeStrokePath, sideStrokePaint)
        } else {
            // Bottom half: draw seam highlight line
            val y = rect.top + 1f
            canvas.drawLine(rect.left, y, rect.right, y, highlightCutPaint)
            
            // Side strokes - fade from seam (top of rect) down to bottom corners
            sideStrokePaint.shader = bottomSideStrokeShader
            geometry.buildBottomLeftEdgePath(edgeStrokePath)
            canvas.drawPath(edgeStrokePath, sideStrokePaint)
            
            geometry.buildBottomRightEdgePath(edgeStrokePath)
            canvas.drawPath(edgeStrokePath, sideStrokePaint)
        }
    }
    
    /**
     * Procedurally generate a seamless noise shader.
     * Prevents the need for external image resources and allows runtime tuning.
     */
    private fun createNoiseShader(intensity: Int, noiseColor: Int = Color.WHITE): android.graphics.BitmapShader {
        val width = 64
        val height = 64
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        val random = java.util.Random()
        val r = Color.red(noiseColor)
        val g = Color.green(noiseColor)
        val b = Color.blue(noiseColor)

        for (i in pixels.indices) {
            val alpha = random.nextInt(intensity + 1)
            pixels[i] = Color.argb(alpha, r, g, b)
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
    }
    
    /**
     * Draw text with optical (ink) centering.
     */
    private fun drawCenteredText(canvas: Canvas, text: String) {
        val inkCenterX = getInkCenterX(text)
        val drawX = geometry.centerX - inkCenterX
        val drawY = geometry.centerY + cachedTextOffset
        canvas.drawText(text, drawX, drawY, textPaint)
    }
    
    /**
     * Get the horizontal ink center for a text string (cached).
     */
    private fun getInkCenterX(text: String): Float {
        // Check cache first
        if (text == cachedCurrentValue) return cachedCurrentInkCenterX
        if (text == cachedNextValue) return cachedNextInkCenterX

        // Calculate and cache
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val center = (textBounds.left + textBounds.right) / 2f
        cacheTextCenter(text, center)
        return center
    }

    private fun cacheTextCenter(text: String, center: Float) {
        when {
            cachedCurrentValue == null -> {
                cachedCurrentValue = text
                cachedCurrentInkCenterX = center
            }
            cachedNextValue == null -> {
                cachedNextValue = text
                cachedNextInkCenterX = center
            }
        }
    }
    
    /**
     * Draw the AM/PM indicator text.
     */
    private fun drawAmPm(canvas: Canvas, amPm: String) {
        amPmPaint.set(textPaint)
        amPmPaint.textSize = cachedAmPmSize
        amPmPaint.alpha = 255
        amPmPaint.textAlign = Paint.Align.LEFT
        
        val hPadding = geometry.width * config.amPmHorizontalPaddingRatio
        val mainBaselineY = geometry.centerY + cachedTextOffset
        val numberVisualTopY = mainBaselineY + cachedNumberVisualTop
        val amVisualTopOffset = if (amPm == "AM") cachedAmBoundsTop else cachedPmBoundsTop
        val amPmBaseline = numberVisualTopY - amVisualTopOffset
        val amPmShiftPx = geometry.height * config.amPmVerticalShiftRatio
        
        canvas.drawText(amPm, hPadding, amPmBaseline + amPmShiftPx, amPmPaint)
    }
    
    /**
     * Apply a shadow overlay on a rectangular area.
     */
    private fun applyShadowOverlay(canvas: Canvas, rect: RectF, intensity: Float, maxAlpha: Int) {
        if (intensity <= 0f) return
        
        canvas.save()
        canvas.clipRect(rect)
        shadowPaint.alpha = (intensity * maxAlpha).toInt().coerceIn(0, 255)
        // Draw the full path to maintain squircle edge quality under the shadow
        canvas.drawPath(geometry.fullCardPath, shadowPaint)
        canvas.restore()
    }
    
    /**
     * Refresh gradient shaders based on current colors and dimensions.
     */
    private fun refreshGradients() {
        val h = geometry.height
        if (h <= 0) return

        if (colors.isDarkTheme) {
            applyDarkThemeGradients(h)
        } else {
            applyLightThemeGradients(h)
        }

        refreshSideStrokeShaders()
    }

    private fun refreshSideStrokeShaders() {
        val rimColor = colors.rimHighlightColor
        val topRect = geometry.topRect
        val bottomRect = geometry.bottomRect
        if (geometry.height <= 0f) {
            topSideStrokeShader = null
            bottomSideStrokeShader = null
            return
        }
        topSideStrokeShader = android.graphics.LinearGradient(
            0f, topRect.bottom, 0f, topRect.top,
            intArrayOf(rimColor, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        bottomSideStrokeShader = android.graphics.LinearGradient(
            0f, bottomRect.top, 0f, bottomRect.bottom,
            intArrayOf(rimColor, Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
    }

    private fun applyDarkThemeGradients(height: Float) {
        val base = colors.cardColor
        val topColors = intArrayOf(
            lightenColor(base, config.darkTopLightenFactor),
            base,
            darkenColor(base, config.darkTopDarkenFactor)
        )
        val bottomColors = intArrayOf(
            darkenColor(base, config.darkBottomDarkenFactor),
            base,
            lightenColor(base, config.darkBottomLightenFactor)
        )

        topGradientPaint.shader = createGradientShader(0f, 0f, 0f, height / 2, topColors)
        bottomGradientPaint.shader = createGradientShader(0f, height / 2, 0f, height, bottomColors)
    }

    private fun applyLightThemeGradients(height: Float) {
        val solidColors = intArrayOf(colors.cardColor, colors.cardColor, colors.cardColor)
        topGradientPaint.shader = createGradientShader(0f, 0f, 0f, height / 2, solidColors)
        bottomGradientPaint.shader = createGradientShader(0f, height / 2, 0f, height, solidColors)
    }

    private fun createGradientShader(x0: Float, y0: Float, x1: Float, y1: Float, colors: IntArray): android.graphics.LinearGradient {
        return android.graphics.LinearGradient(
            x0, y0, x1, y1,
            colors, floatArrayOf(0f, 0.5f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    
    private fun lightenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
    
    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
}
