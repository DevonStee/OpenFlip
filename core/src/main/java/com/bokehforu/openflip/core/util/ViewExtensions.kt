package com.bokehforu.openflip.core.util

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * View extension functions for smooth animations.
 */

private const val ANIMATION_DURATION = 250L
private val ANIMATION_INTERPOLATOR = AccelerateDecelerateInterpolator()

/**
 * Smoothly fade in and show the view with optional scale animation.
 * 
 * @param withScale If true, adds a subtle scale-up effect (0.9 -> 1.0)
 * @param duration Animation duration in milliseconds
 */
fun View.fadeIn(withScale: Boolean = true, duration: Long = ANIMATION_DURATION) {
    // Cancel any ongoing animations
    animate().cancel()
    
    // Set initial state if view is not visible
    if (visibility != View.VISIBLE) {
        alpha = 0f
        if (withScale) {
            scaleX = 0.9f
            scaleY = 0.9f
        }
        visibility = View.VISIBLE
    }
    
    // Animate to visible state
    animate()
        .alpha(1f)
        .apply {
            if (withScale) {
                scaleX(1f)
                scaleY(1f)
            }
        }
        .setDuration(duration)
        .setInterpolator(ANIMATION_INTERPOLATOR)
        .start()
}

/**
 * Smoothly fade out and hide the view with optional scale animation.
 * 
 * @param withScale If true, adds a subtle scale-down effect (1.0 -> 0.9)
 * @param duration Animation duration in milliseconds
 * @param gone If true, sets visibility to GONE instead of INVISIBLE
 */
fun View.fadeOut(withScale: Boolean = true, duration: Long = ANIMATION_DURATION, gone: Boolean = false) {
    // Cancel any ongoing animations
    animate().cancel()
    
    // Animate to hidden state
    animate()
        .alpha(0f)
        .apply {
            if (withScale) {
                scaleX(0.9f)
                scaleY(0.9f)
            }
        }
        .setDuration(duration)
        .setInterpolator(ANIMATION_INTERPOLATOR)
        .withEndAction {
            // Lifecycle check: View may be detached during animation
            if (!isAttachedToWindow) return@withEndAction
            
            visibility = if (gone) View.GONE else View.INVISIBLE
            // Reset scale for next show
            if (withScale) {
                scaleX = 1f
                scaleY = 1f
            }
        }
        .start()
}

/**
 * Set visibility with smooth animation.
 * 
 * @param visible Target visibility state
 * @param withScale Whether to include scale animation
 * @param duration Animation duration in milliseconds
 * @param gone If true and visible is false, sets visibility to GONE instead of INVISIBLE
 */
fun View.setVisibilityAnimated(
    visible: Boolean,
    withScale: Boolean = true,
    duration: Long = ANIMATION_DURATION,
    gone: Boolean = false
) {
    if (visible) {
        fadeIn(withScale, duration)
    } else {
        fadeOut(withScale, duration, gone)
    }
}

/**
 * Specialized animation for labels: slides in from a specified offset.
 */
fun View.setLabelVisibilityAnimated(
    visible: Boolean,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    duration: Long = ANIMATION_DURATION,
    gone: Boolean = false
) {
    animate().cancel()
    
    if (visible) {
        if (visibility != View.VISIBLE) {
            alpha = 0f
            translationX = offsetX
            translationY = offsetY
            visibility = View.VISIBLE
        }
        animate()
            .alpha(1f)
            .translationX(0f)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(ANIMATION_INTERPOLATOR)
            .start()
    } else {
        animate()
            .alpha(0f)
            .translationX(offsetX)
            .translationY(offsetY)
            .setDuration(duration)
            .setInterpolator(ANIMATION_INTERPOLATOR)
            .withEndAction {
                if (!isAttachedToWindow) return@withEndAction
                visibility = if (gone) View.GONE else View.INVISIBLE
                translationX = 0f
                translationY = 0f
            }
            .start()
    }
}

/**
 * Specialized animation for dividers: scales (grows) in from the center.
 */
fun View.setDividerVisibilityAnimated(
    visible: Boolean,
    isVertical: Boolean,
    duration: Long = ANIMATION_DURATION,
    gone: Boolean = false
) {
    animate().cancel()
    
    if (visible) {
        if (visibility != View.VISIBLE) {
            alpha = 0f
            if (isVertical) scaleY = 0f else scaleX = 0f
            visibility = View.VISIBLE
        }
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(ANIMATION_INTERPOLATOR)
            .start()
    } else {
        animate()
            .alpha(0f)
            .apply {
                if (isVertical) scaleY(0f) else scaleX(0f)
            }
            .setDuration(duration)
            .setInterpolator(ANIMATION_INTERPOLATOR)
            .withEndAction {
                if (!isAttachedToWindow) return@withEndAction
                visibility = if (gone) View.GONE else View.INVISIBLE
                scaleX = 1f
                scaleY = 1f
            }
            .start()
    }
}

/**
 * Instantly set visibility without animation (for initial setup).
 */
fun View.setVisibilityInstant(visible: Boolean, gone: Boolean = false) {
    animate().cancel()
    visibility = when {
        visible -> View.VISIBLE
        gone -> View.GONE
        else -> View.INVISIBLE
    }
    alpha = if (visible) 1f else 0f
    scaleX = 1f
    scaleY = 1f
}

/**
 * Rotates the view 360 degrees.
 * Used for theme toggle button and settings button animations.
 *
 * @param duration Animation duration in milliseconds
 * @param clockwise Direction of rotation
 * @param interpolator Custom interpolator, defaults to AccelerateDecelerateInterpolator (set in constants)
 */
fun View.rotate360(
    duration: Long = 900,
    clockwise: Boolean = true,
    interpolator: android.animation.TimeInterpolator = ANIMATION_INTERPOLATOR
) {
    // Basic check: ensure View is attached
    if (!isAttachedToWindow) {
        return
    }

    // Enable hardware layer for smooth animation
    val originalLayerType = layerType
    if (layerType != View.LAYER_TYPE_HARDWARE) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    animate().cancel()
    rotation = 0f
    animate()
        .rotation(if (clockwise) 360f else -360f)
        .setDuration(duration)
        .setInterpolator(interpolator)
        .setListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                cleanup()
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {
                cleanup()
            }

            private fun cleanup() {
                setLayerType(originalLayerType, null)
                animate().setListener(null)
            }
        })
        .start()
}

/**
 * Rotates the view 180 degrees.
 * Used for settings button animations.
 *
 * @param duration Animation duration in milliseconds
 * @param clockwise Direction of rotation
 * @param interpolator Custom interpolator, defaults to DecelerateInterpolator
 */
fun View.rotate180(
    duration: Long = 300,
    clockwise: Boolean = true,
    interpolator: android.animation.TimeInterpolator = android.view.animation.DecelerateInterpolator()
) {
    animate().cancel()
    val currentRotation = rotation % 360f
    val targetRotation = currentRotation + if (clockwise) 180f else -180f
    animate()
        .rotation(targetRotation)
        .setDuration(duration)
        .setInterpolator(interpolator)
        .start()
}

/**
 * Plays a "radiate and fade" animation: scales up while fading out,
 * then resets and fades back in at original size.
 * Used for light bulb rays icon when panel appears.
 *
 * @param peakScale Maximum scale during the radiate phase
 * @param durationOut Duration of the scale-up + fade-out phase
 * @param durationIn Duration of the fade-in phase
 */
fun View.pulsePop(
    peakScale: Float = 1.4f,
    durationOut: Long = 400L,
    durationIn: Long = 500L
) {
    if (!isAttachedToWindow) return

    animate().cancel()
    scaleX = 1f
    scaleY = 1f
    alpha = 1f

    // Phase 1: Scale up while fading out (radiate outward)
    animate()
        .scaleX(peakScale)
        .scaleY(peakScale)
        .alpha(0f)
        .setDuration(durationOut)
        .setInterpolator(ANIMATION_INTERPOLATOR)
        .withEndAction {
            if (!isAttachedToWindow) return@withEndAction
            // Reset to original scale instantly
            scaleX = 1f
            scaleY = 1f
            alpha = 0f
            // Phase 2: Fade back in at original position
            animate()
                .alpha(1f)
                .setDuration(durationIn)
                .setInterpolator(ANIMATION_INTERPOLATOR)
                .start()
        }
        .start()
}
