package com.bokehforu.openflip.feature.clock.ui.compose

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.ui.theme.SecondsBackgroundDark
import com.bokehforu.openflip.feature.clock.ui.theme.SecondsBackgroundLight
import com.bokehforu.openflip.feature.clock.ui.theme.SecondsTextDark
import com.bokehforu.openflip.feature.clock.ui.theme.SecondsTextLight
import com.bokehforu.openflip.feature.clock.ui.theme.OpenFlipClockFontFamily
import com.bokehforu.openflip.core.util.resolveThemeColor
import kotlinx.coroutines.flow.SharedFlow
import com.bokehforu.openflip.core.R as CoreR
import com.bokehforu.openflip.core.ui.TestTags

/**
 * Main options button that shows either seconds display or gear icon.
 * Features press animation and gear rotation on trigger.
 */
@Composable
fun MainOptionsButton(
    isDark: Boolean,
    showSeconds: Boolean,
    secondsText: String,
    nextSecondsText: String = "",
    gearRotationTrigger: SharedFlow<Unit>?,
    // Waterfall Animation States
    activeTranslationY: Float = 0f,
    incomingTranslationY: Float = 0f,
    activeAlpha: Float = 1f,
    incomingAlpha: Float = 0f,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val themeRes = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
    val backgroundColor = Color(context.resolveThemeColor(CoreR.attr.settingsControlBackground, themeRes))
    val contentColor = if (isDark) SecondsTextDark else SecondsTextLight

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Physics-based scale animation - faster press, slower release for natural feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (isPressed) 60 else 120,
            easing = if (isPressed) LinearOutSlowInEasing else FastOutSlowInEasing
        ),
        label = "buttonScale"
    )

    // Material-style fade-through for options icon
    val iconAlpha = remember { Animatable(1f) }
    val iconScale = remember { Animatable(1f) }
    val iconTranslation = remember { Animatable(0f) }

    if (gearRotationTrigger != null) {
        LaunchedEffect(gearRotationTrigger) {
            gearRotationTrigger.collect {
                iconScale.snapTo(0.96f)
                iconTranslation.snapTo(6f)

                iconAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
                iconTranslation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .testTag(TestTags.MAIN_OPTIONS_BUTTON)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner background with 5dp inset (54dp inner box)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(15.5.dp))
                .background(color = backgroundColor)
        )
        if (showSeconds) {
            SecondsDisplay(
                isDark = isDark,
                contentColor = contentColor,
                secondsText = secondsText,
                nextSecondsText = nextSecondsText,
                activeTranslationY = activeTranslationY,
                incomingTranslationY = incomingTranslationY,
                activeAlpha = activeAlpha,
                incomingAlpha = incomingAlpha,
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.icon_options_24dp),
                contentDescription = "Open Settings",
                tint = contentColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        alpha = iconAlpha.value
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                        translationY = iconTranslation.value
                    }
            )
        }
    }
}

/**
 * Glassy seconds display with layered lighting and background blur.
 */
@Composable
private fun SecondsDisplay(
    isDark: Boolean,
    contentColor: Color,
    secondsText: String,
    nextSecondsText: String,
    activeTranslationY: Float,
    incomingTranslationY: Float,
    activeAlpha: Float,
    incomingAlpha: Float,
) {
    val innerSquircle = remember { SquircleShape(8f) }
    val blurRadius = if (isDark) 6 else 4
    val params = glassParamsForTheme(isDark)
    val highlightShift = ((activeTranslationY * activeAlpha) + (incomingTranslationY * incomingAlpha)) * 0.12f
    
    val density = LocalDensity.current
    val widthPx = with(density) { 28.dp.toPx() }
    val heightPx = with(density) { 28.dp.toPx() }
    
    val lensEffect = remember(widthPx, heightPx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            com.bokehforu.openflip.feature.clock.ui.effect.ConvexLensEffect.create(
                widthPx = widthPx,
                heightPx = heightPx,
                lensStrength = 0.30f
            )
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 28.dp)
            .graphicsLayer {
                renderEffect = lensEffect
            },
        contentAlignment = Alignment.Center
    ) {
        GlassySurface(
            modifier = Modifier.matchParentSize(),
            shape = innerSquircle,
            params = params,
            blurRadius = blurRadius,
            highlightShift = highlightShift
        )

        // Foreground text layer (not blurred)
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (secondsText.isEmpty()) "00" else secondsText,
                color = contentColor.copy(alpha = activeAlpha),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFeatureSettings = "tnum",
                    textAlign = TextAlign.Center
                ),
                fontFamily = OpenFlipClockFontFamily,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = activeTranslationY
                        val centerProgress = activeAlpha
                        scaleX = 0.50f + (0.65f * centerProgress)
                        scaleY = 0.50f + (0.65f * centerProgress)
                        
                        val exitProgress = 1f - activeAlpha
                        rotationX = -30f * exitProgress
                    }
            )

            if (incomingAlpha > 0f) {
                Text(
                    text = if (nextSecondsText.isEmpty()) "01" else nextSecondsText,
                    color = contentColor.copy(alpha = incomingAlpha),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFeatureSettings = "tnum",
                        textAlign = TextAlign.Center
                    ),
                    fontFamily = OpenFlipClockFontFamily,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = incomingTranslationY
                            scaleX = 0.50f + (0.65f * incomingAlpha)
                            scaleY = 0.50f + (0.65f * incomingAlpha)
                            
                            val enterProgress = 1f - incomingAlpha
                            rotationX = 30f * enterProgress
                        }
                )
            }
        }
    }
}
