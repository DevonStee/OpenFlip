/*
 * Copyright (C) 2026 DevonStee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.bokehforu.openflip.feature.clock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.bokehforu.openflip.feature.clock.R

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF3B30),
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1C),
    surfaceContainer = Color(0xFF333333),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFF9B9B9B),
    outline = Color(0xFF323230)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF3B30),
    background = Color(0xFFF5F5F0),
    surface = Color(0xFFF5F5F0),
    surfaceContainer = Color(0xFFE8E7DE),
    onPrimary = Color.White,
    onBackground = Color(0xFF505050),
    onSurface = Color(0xFF505050),
    onSurfaceVariant = Color(0xFF676968),
    outline = Color(0xFFD8D8D0)
)

val OpenFlipClockFontFamily = FontFamily(Font(R.font.openflip_font))
val SecondsBackgroundDark = Color(0x66808080)
val SecondsBackgroundLight = Color(0xFFE8E7DE)
val SecondsTextDark = Color(0xFFE6E6E6)
val SecondsTextLight = Color(0xFF505050)

@Composable
fun OpenFlipTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
