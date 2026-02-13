package com.bokehforu.openflip.feature.clock.view.card

import android.graphics.Path
import android.graphics.RectF

/**
 * Handles all geometric calculations for the flip card.
 * Calculates rectangles, radii, and paths based on the card size and config.
 */
class FlipCardGeometry(private val config: FlipCardConfig = FlipCardConfig()) {
    
    // Calculated dimensions
    var width: Float = 0f; private set
    var height: Float = 0f; private set
    var centerX: Float = 0f; private set
    var centerY: Float = 0f; private set
    var cornerRadius: Float = 0f; private set
    var splitGap: Float = 0f; private set
    var density: Float = 1f; private set
    
    // Pre-calculated rectangles
    val cardRect = RectF()
    val topRect = RectF()
    val bottomRect = RectF()
    
    // Pre-calculated paths (expensive to create, so we cache them)
    val fullCardPath = Path()
    val topClipPath = Path()
    val bottomClipPath = Path()
    
    // Temporary path for calculations (avoids allocations)
    private val tempPath = Path()
    
    // Proportional values derived from dimensions
    var proportionalStrokeWidth: Float = 0f; private set
    var proportionalRotationTranslate: Float = 0f; private set
    
    /**
     * Update all geometric calculations based on new dimensions.
     * Call this when the card size changes.
     */
    fun updateDimensions(w: Float, h: Float, displayDensity: Float = 1f) {
        if (w <= 0 || h <= 0) return
        
        width = w
        height = h
        centerX = w / 2f
        centerY = h / 2f
        density = displayDensity
        
        // Corner radius with safety cap
        val maxRadius = kotlin.math.min(w, h) / 2f
        cornerRadius = (w * config.cornerRadiusRatio).coerceAtMost(maxRadius * 0.95f)
        
        // Split gap between top and bottom halves
        splitGap = h * config.splitGapRatio
        
        // Update rectangles
        cardRect.set(0f, 0f, w, h)
        topRect.set(0f, 0f, w, h / 2 - splitGap / 2)
        bottomRect.set(0f, h / 2 + splitGap / 2, w, h)
        
        // Proportional values
        proportionalStrokeWidth = h * 0.002f
        proportionalRotationTranslate = h * config.rotationTranslateRatio
        
        // Build paths
        buildSuperellipsePath(fullCardPath, cardRect, cornerRadius)
        buildClipPath(topClipPath, topRect)
        buildClipPath(bottomClipPath, bottomRect)
    }
    
    /**
     * Constructs a superellipse (squircle) path for smooth, iOS-style corners.
     * Uses cubic Bezier curves for continuous curvature.
     */
    private fun buildSuperellipsePath(path: Path, rect: RectF, radius: Float) {
        path.reset()
        val left = rect.left
        val top = rect.top
        val right = rect.right
        val bottom = rect.bottom
        val push = radius * config.squirclePushFactor
        
        path.moveTo(left + radius, top)
        
        // Top Right corner
        path.lineTo(right - radius, top)
        path.cubicTo(right - radius + push, top, right, top + radius - push, right, top + radius)
        
        // Bottom Right corner
        path.lineTo(right, bottom - radius)
        path.cubicTo(right, bottom - radius + push, right - radius + push, bottom, right - radius, bottom)
        
        // Bottom Left corner
        path.lineTo(left + radius, bottom)
        path.cubicTo(left + radius - push, bottom, left, bottom - radius + push, left, bottom - radius)
        
        // Top Left corner
        path.lineTo(left, top + radius)
        path.cubicTo(left, top + radius - push, left + radius - push, top, left + radius, top)
        
        path.close()
    }
    
    /**
     * Create a clip path that is the intersection of the full card path and a rectangle.
     */
    private fun buildClipPath(clipPath: Path, clipRect: RectF) {
        clipPath.set(fullCardPath)
        tempPath.reset()
        tempPath.addRect(clipRect, Path.Direction.CW)
        clipPath.op(tempPath, Path.Op.INTERSECT)
    }
    
    /**
     * Get the appropriate clip path for a given rectangle.
     */
    fun getClipPath(rect: RectF): Path {
        return when {
            rect === topRect -> topClipPath
            rect === bottomRect -> bottomClipPath
            else -> {
                // Fallback for custom rects
                val customPath = Path()
                buildSuperellipsePath(customPath, cardRect, cornerRadius)
                tempPath.reset()
                tempPath.addRect(rect, Path.Direction.CW)
                customPath.op(tempPath, Path.Op.INTERSECT)
                customPath
            }
        }
    }
    
    /**
     * Build left edge path for the top half (from bottom seam to top corner along squircle edge).
     */
    fun buildTopLeftEdgePath(path: Path) {
        path.reset()
        val left = cardRect.left
        val top = cardRect.top
        val push = cornerRadius * config.squirclePushFactor
        
        // Start from bottom of left edge (seam position)
        path.moveTo(left, centerY - splitGap / 2)
        // Draw up along left edge
        path.lineTo(left, top + cornerRadius)
        // Curve through top-left corner
        path.cubicTo(left, top + cornerRadius - push, left + cornerRadius - push, top, left + cornerRadius, top)
    }
    
    /**
     * Build right edge path for the top half (from bottom seam to top corner along squircle edge).
     */
    fun buildTopRightEdgePath(path: Path) {
        path.reset()
        val right = cardRect.right
        val top = cardRect.top
        val push = cornerRadius * config.squirclePushFactor
        
        // Start from bottom of right edge (seam position)
        path.moveTo(right, centerY - splitGap / 2)
        // Draw up along right edge
        path.lineTo(right, top + cornerRadius)
        // Curve through top-right corner
        path.cubicTo(right, top + cornerRadius - push, right - cornerRadius + push, top, right - cornerRadius, top)
    }
    
    /**
     * Build left edge path for the bottom half (from top seam to bottom corner along squircle edge).
     */
    fun buildBottomLeftEdgePath(path: Path) {
        path.reset()
        val left = cardRect.left
        val bottom = cardRect.bottom
        val push = cornerRadius * config.squirclePushFactor
        
        // Start from top of left edge (seam position)
        path.moveTo(left, centerY + splitGap / 2)
        // Draw down along left edge
        path.lineTo(left, bottom - cornerRadius)
        // Curve through bottom-left corner
        path.cubicTo(left, bottom - cornerRadius + push, left + cornerRadius - push, bottom, left + cornerRadius, bottom)
    }
    
    /**
     * Build right edge path for the bottom half (from top seam to bottom corner along squircle edge).
     */
    fun buildBottomRightEdgePath(path: Path) {
        path.reset()
        val right = cardRect.right
        val bottom = cardRect.bottom
        val push = cornerRadius * config.squirclePushFactor
        
        // Start from top of right edge (seam position)
        path.moveTo(right, centerY + splitGap / 2)
        // Draw down along right edge
        path.lineTo(right, bottom - cornerRadius)
        // Curve through bottom-right corner
        path.cubicTo(right, bottom - cornerRadius + push, right - cornerRadius + push, bottom, right - cornerRadius, bottom)
    }
}
