package com.bokehforu.openflip.feature.clock.ui.controller

import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel

class GearAnimationController(
    private val viewModel: FullscreenClockViewModel
) {
    // ValueAnimator removed in favor of Compose-native animation via ViewModel trigger

    fun rotateOnce() {
        viewModel.triggerGearRotation()
    }

    fun stop() {
        // No-op for now, or could signal stop if needed
    }

    fun cancel() {
        // No-op
    }
}
