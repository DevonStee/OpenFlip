package com.bokehforu.openflip.feature.settings.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bokehforu.openflip.feature.settings.R

// Colors are defined in Color.kt

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface, // In light mode, surface usually matches background for this app
    surfaceContainer = LightSurfaceContainer,
    onPrimary = Color.White,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

val OpenFlipUiFontFamily = FontFamily.SansSerif
val OpenFlipClockFontFamily = FontFamily(Font(R.font.openflip_font))
private val BaseTypography = androidx.compose.material3.Typography()
val OpenFlipTypography = androidx.compose.material3.Typography(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = OpenFlipUiFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = OpenFlipUiFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = OpenFlipUiFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = OpenFlipUiFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = OpenFlipUiFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = OpenFlipUiFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = OpenFlipUiFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = OpenFlipUiFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = OpenFlipUiFontFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = OpenFlipUiFontFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = OpenFlipUiFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = OpenFlipUiFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = OpenFlipUiFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = OpenFlipUiFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = OpenFlipUiFontFamily)
)

@Composable
fun OpenFlipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OpenFlipTypography,
        content = content
    )
}
