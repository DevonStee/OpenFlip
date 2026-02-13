package com.bokehforu.openflip.feature.settings.ui.compose

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.skydoves.cloudy.cloudy
import kotlin.random.Random

data class GlassParams(
    val baseTint: Color,
    val rimBaseAlpha: Float,
    val rimCool: Color,
    val rimWarm: Color,
    val innerShadowAlpha: Float,
    val innerShadowAlphaStrong: Float,
    val bottomHighlightAlpha: Float,
    val topWarmAlpha: Float,
    val noiseAlpha: Float
)

fun glassParamsForTheme(isDark: Boolean): GlassParams = GlassParams(
    baseTint = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.40f),
    rimBaseAlpha = if (isDark) 0.42f else 0.68f,
    rimCool = Color.Transparent,
    rimWarm = Color.Transparent,
    innerShadowAlpha = if (isDark) 0.17f else 0.08f,
    innerShadowAlphaStrong = if (isDark) 0.24f else 0.10f,
    bottomHighlightAlpha = if (isDark) 0.16f else 0.34f,
    topWarmAlpha = 0f,
    noiseAlpha = if (isDark) 0.015f else 0.02f
)

/**
 * Backdrop blur + saturation boost (S+) and base tint fill.
 */
fun Modifier.glassyBackdrop(params: GlassParams, blurRadius: Float): Modifier =
    this.graphicsLayer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
            val blur = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
            val cm = ColorMatrix().apply { setSaturation(1.12f) }
            val colorize = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(cm))
            renderEffect = RenderEffect.createChainEffect(colorize, blur).asComposeRenderEffect()
        }
    }.drawWithContent {
        drawRect(params.baseTint)
        drawContent()
    }

/**
 * Decorations: rims, inner shadows, highlights, noise.
 */
fun Modifier.glassyDecorations(
    params: GlassParams,
    highlightShift: Float,
    rimRadiusPx: Float
): Modifier = this.drawWithContent {
    drawContent()

    // Rim lights
    drawRoundRect(
        color = Color.White.copy(alpha = params.rimBaseAlpha),
        style = Stroke(width = rimRadiusPx),
        cornerRadius = CornerRadius(size.minDimension / 4f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(0f to Color.Transparent, 1f to params.rimCool),
        style = Stroke(width = rimRadiusPx * 0.8f),
        cornerRadius = CornerRadius(size.minDimension / 4f)
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(params.rimWarm, Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.4f, size.height * 0.4f)
        ),
        style = Stroke(width = rimRadiusPx * 0.9f),
        cornerRadius = CornerRadius(size.minDimension / 4f)
    )

    // Inner shadows
    val shadowWidth = 4.dp.toPx()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = params.innerShadowAlphaStrong), Color.Transparent),
            startY = 0f, endY = shadowWidth
        )
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = params.innerShadowAlpha)),
            startY = size.height - shadowWidth, endY = size.height
        )
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Black.copy(alpha = params.innerShadowAlpha), Color.Transparent),
            startX = 0f, endX = shadowWidth
        )
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = params.innerShadowAlphaStrong)),
            startX = size.width - shadowWidth, endX = size.width
        )
    )

    // Bottom reflective highlight
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to Color.Transparent,
                0.65f to Color.White.copy(alpha = params.bottomHighlightAlpha * 0.82f),
                1.0f to Color.White.copy(alpha = params.bottomHighlightAlpha),
            )
        ),
        topLeft = Offset(
            x = 0f,
            y = (size.height - 10.dp.toPx() + highlightShift).coerceIn(0f, size.height)
        ),
        size = Size(width = size.width, height = size.height)
    )

    // Top warm cast
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(params.rimWarm.copy(alpha = params.topWarmAlpha), Color.Transparent),
            startY = 0f,
            endY = size.height * 0.38f
        )
    )

    // Fine noise
    val noiseColor = if (params.noiseAlpha > 0) Color.White.copy(alpha = params.noiseAlpha) else Color.Transparent
    val seed = (size.width.toBits() * 31 + size.height.toBits()).toInt()
    val rnd = Random(seed)
    repeat(36) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        drawCircle(color = noiseColor, radius = 0.55f, center = Offset(x, y))
    }
}

/**
 * Convenience composable to apply glass effect with backdrop + decorations.
 */
@Composable
fun GlassySurface(
    modifier: Modifier = Modifier,
    shape: Shape,
    params: GlassParams,
    blurRadius: Int,
    highlightShift: Float,
    content: @Composable () -> Unit = {}
) {
    val rimRadiusPx = with(LocalDensity.current) { 1.5.dp.toPx() }
    val base = modifier.clip(shape)
    val decorated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        base
            .glassyBackdrop(params, blurRadius.toFloat() * 2f)
            .glassyDecorations(params, highlightShift, rimRadiusPx = rimRadiusPx)
    } else {
        base
            .cloudy(radius = blurRadius)
            .background(params.baseTint, shape)
            .glassyDecorations(params, highlightShift, rimRadiusPx = rimRadiusPx)
    }
    Box(modifier = decorated, contentAlignment = Alignment.Center) { content() }
}
