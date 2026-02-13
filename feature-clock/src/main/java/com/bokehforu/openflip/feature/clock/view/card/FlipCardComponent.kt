package com.bokehforu.openflip.feature.clock.view.card

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface

/**
 * Facade class that provides a simple API for the flip card.
 * 
 * This class combines FlipCardState, FlipCardGeometry, and FlipCardRenderer
 * into a single easy-to-use interface that's compatible with
 * the existing FullscreenFlipClockView.
 * 
 * Internally, it uses the clean separation of concerns:
 * - State management (what to show)
 * - Geometry (size and shape calculations)  
 * - Rendering (how to draw)
 */
class FlipCardComponent {
    
    private val config = FlipCardConfig()
    private val geometry = FlipCardGeometry(config)
    private val renderer = FlipCardRenderer(config, geometry)
    
    // Mutable state (updated by FullscreenFlipClockView during animation)
    private var _state = FlipCardState()
    
    // Public accessors for backward compatibility with FullscreenFlipClockView
    var currentValue: String
        get() = _state.currentValue
        set(value) { _state = _state.copy(currentValue = value) }
    
    var nextValue: String
        get() = _state.nextValue
        set(value) { _state = _state.copy(nextValue = value) }
    
    /**
     * Flip progress from 0 (rest) to 180 (complete).
     * Converts to normalized 0-1 internally.
     */
    var flipDegree: Float
        get() = _state.flipProgress * 180f
        set(value) { _state = _state.copy(flipProgress = value / 180f) }
    
    var amPmText: String?
        get() = _state.amPmText
        set(value) { _state = _state.copy(amPmText = value) }

    /**
     * Control flip direction: true for upward (reverse), false for downward (forward).
     */
    var isReverseFlip: Boolean
        get() = _state.isReversing
        set(value) { _state = _state.copy(isReversing = value) }
    
    /**
     * Set card dimensions. Call when size changes.
     */
    fun setDimensions(width: Float, height: Float, density: Float = 1f) {
        renderer.setDimensions(width, height, density)
    }
    
    /**
     * Update card colors. Call when theme changes.
     */
    fun updateColors(
        textColor: Int, 
        cardColor: Int, 
        shadowColor: Int, 
        shadowEdgeColor: Int,
        rimHighlightColor: Int = Color.TRANSPARENT,
        cutHighlightColor: Int = Color.TRANSPARENT,
        cutShadowColor: Int = Color.TRANSPARENT,
        noiseColor: Int = Color.TRANSPARENT
    ) {
        renderer.setColors(textColor, cardColor, shadowColor, shadowEdgeColor, rimHighlightColor, cutHighlightColor, cutShadowColor, noiseColor)
    }
    
    /**
     * Set the typeface for digit rendering.
     */
    fun setTypeface(typeface: Typeface?) {
        renderer.setTypeface(typeface)
    }
    
    /**
     * Draw the card to the canvas.
     */
    fun draw(canvas: Canvas) {
        renderer.draw(canvas, _state)
    }
}
