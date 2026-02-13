package com.bokehforu.openflip.feature.clock.ui.compose

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.skydoves.cloudy.cloudy
import kotlin.math.pow
import kotlin.math.sign
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

fun Modifier.glassyDecorations(
    params: GlassParams,
    highlightShift: Float,
    rimRadiusPx: Float
): Modifier = this.drawWithContent {
    drawContent()
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

    val noiseColor = if (params.noiseAlpha > 0) Color.White.copy(alpha = params.noiseAlpha) else Color.Transparent
    val seed = (size.width.toBits() * 31 + size.height.toBits()).toInt()
    val rnd = Random(seed)
    repeat(36) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        drawCircle(color = noiseColor, radius = 0.55f, center = Offset(x, y))
    }
}

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
            .glassyDecorations(params, highlightShift, rimRadiusPx)
    } else {
        base
            .cloudy(radius = blurRadius)
            .background(params.baseTint, shape)
            .glassyDecorations(params, highlightShift, rimRadiusPx)
    }
    Box(modifier = decorated, contentAlignment = Alignment.Center) { content() }
}

class SquircleShape(private val cornerRadiusDp: Float = 14f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val width = size.width
        val height = size.height
        val n = 6.0f
        val points = mutableListOf<Pair<Float, Float>>()
        for (i in 0..100) {
            val t = (i.toFloat() / 100f) * 2 * Math.PI.toFloat()
            val cosT = kotlin.math.cos(t.toDouble()).toFloat()
            val sinT = kotlin.math.sin(t.toDouble()).toFloat()
            val x = sign(cosT) * kotlin.math.abs(cosT).pow(2f / n)
            val y = sign(sinT) * kotlin.math.abs(sinT).pow(2f / n)
            points.add(width / 2 + x * (width / 2) to height / 2 + y * (height / 2))
        }
        if (points.isNotEmpty()) {
            path.moveTo(points[0].first, points[0].second)
            for (i in 1 until points.size) path.lineTo(points[i].first, points[i].second)
            path.close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun InteractionSource.collectIsPressedAsState(): State<Boolean> {
    val isPressed = remember { mutableStateOf(false) }
    LaunchedEffect(this) {
        interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed.value = true
                is PressInteraction.Release -> isPressed.value = false
                is PressInteraction.Cancel -> isPressed.value = false
            }
        }
    }
    return isPressed
}
