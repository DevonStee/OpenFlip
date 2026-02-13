package com.bokehforu.openflip.feature.clock.view.card

import android.graphics.Color
import android.graphics.Typeface

/**
 * Immutable configuration for card styling.
 * Configuration object holding constants and calculated values
 * for the card rendering.
 */
data class FlipCardConfig(
    // Dimensions
    var cardWidth: Float = 0f,
    // Dimension Ratios (relative to card size)
    val cornerRadiusRatio: Float = 0.12f,
    val splitGapRatio: Float = 0.012f,
    val textSizeRatio: Float = 0.90f,
    val amPmSizeRatio: Float = 0.11f,
    val amPmHorizontalPaddingRatio: Float = 0.06f,
    val amPmVerticalShiftRatio: Float = 0.03f,
    val textVerticalOffsetRatio: Float = 0.005f,
    
    // 3D Effect
    val squirclePushFactor: Float = 0.82f,
    val cameraLocationZ: Float = -8f,
    val rotationTranslateRatio: Float = 0.012f,
    
    // Shadow Intensities (0-255 alpha range)
    val darkThemeShadowAlpha: Int = 60,
    val lightThemeShadowAlpha: Int = 30,
    val darkThemeMaxFlapShadow: Int = 200,
    val lightThemeMaxFlapShadow: Int = 100,
    val topHalfMaxShadow: Int = 200,
    val bottomHalfMaxShadow: Int = 150,
    
    // Gradient factors for dark theme
    val darkTopLightenFactor: Float = 1.15f,
    val darkTopDarkenFactor: Float = 0.96f,
    val darkBottomDarkenFactor: Float = 0.90f,
    val darkBottomLightenFactor: Float = 1.1f,
    
    // Animation thresholds
    val restThresholdDegrees: Float = 0.5f
)

/**
 * Mutable color state for the card.
 * Separated from config because colors can change at runtime (theme switching).
 */
data class CardColors(
    var textColor: Int = Color.WHITE,
    var cardColor: Int = Color.DKGRAY,
    var shadowColor: Int = Color.BLACK,
    var shadowEdgeColor: Int = Color.BLACK,
    var rimHighlightColor: Int = Color.TRANSPARENT,
    var cutHighlightColor: Int = Color.TRANSPARENT,
    var cutShadowColor: Int = Color.TRANSPARENT,
    var noiseColor: Int = Color.TRANSPARENT
) {
    val isDarkTheme: Boolean
        get() = (Color.red(cardColor) + Color.green(cardColor) + Color.blue(cardColor)) / 3 < 128
}

/**
 * Text styling configuration.
 */
data class CardTextStyle(
    var typeface: Typeface? = null
)
