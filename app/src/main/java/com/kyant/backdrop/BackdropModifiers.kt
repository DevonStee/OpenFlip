package com.kyant.backdrop

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import com.kyant.backdrop.effects.BackdropEffectScope

/**
 * No-op modifier to keep call sites intact.
 */
fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier = this

/**
 * Simplified drawBackdrop implementation:
 * - Executes onDrawSurface before content (so background/tint is drawn).
 * - Effects lambda is accepted for API compatibility but ignored.
 */
fun Modifier.drawBackdrop(
    backdrop: LayerBackdrop,
    shape: () -> Shape,
    effects: BackdropEffectScope.() -> Unit = {},
    onDrawSurface: DrawScope.() -> Unit = {}
): Modifier {
    val scope = BackdropEffectScope().apply { effects() }
    val blurRadius = scope.blurRadius ?: 0f

    // Compose graphicsLayer-based blur + slight vibrancy for S+; graceful fallback otherwise.
    return this
        .graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
                val blur = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                val cm = ColorMatrix().apply {
                    // Subtle saturation boost for glassy feel
                    setSaturation(1.12f)
                }
                val colorize = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(cm))
                renderEffect = RenderEffect
                    .createChainEffect(colorize, blur)
                    .asComposeRenderEffect()
            }
        }
        .drawWithContent {
            onDrawSurface()
            drawContent()
        }
}
