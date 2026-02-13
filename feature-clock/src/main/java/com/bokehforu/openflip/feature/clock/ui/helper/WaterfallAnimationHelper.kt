package com.bokehforu.openflip.feature.clock.ui.helper

/**
 * Logic helper for the waterfall seconds animation.
 * Refined for mechanical precision with stronger center-point emphasis:
 * - Crossfade centered at 30% progress (matching haptic trigger)
 * - Tighter alpha curves for crisper visual handover
 * - Reduced overlap for flowing number effect
 */
object WaterfallAnimationHelper {
    const val PREVIEW_ALPHA = 0f

    fun getIncomingAlpha(progress: Float): Float {
        return when {
            progress < 0.10f -> 0.0f
            progress < 0.50f -> (progress - 0.10f) / 0.40f
            else -> 1.0f
        }
    }

    fun getOutgoingAlpha(progress: Float): Float {
        return when {
            progress < 0.10f -> 1.0f
            progress < 0.50f -> 1.0f - ((progress - 0.10f) / 0.40f)
            else -> 0.0f
        }
    }
}
