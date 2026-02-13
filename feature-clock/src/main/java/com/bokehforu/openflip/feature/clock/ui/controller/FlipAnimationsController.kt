package com.bokehforu.openflip.feature.clock.ui.controller

import android.animation.Animator
import android.animation.ValueAnimator
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.bokehforu.openflip.feature.clock.ui.helper.WaterfallAnimationHelper
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider

class FlipAnimationsController(
    private val viewModel: FullscreenClockViewModel,
    private val haptics: HapticsProvider?,
    private val resources: android.content.res.Resources
) {
    private val waterfallInterpolator = android.view.animation.PathInterpolator(0.33f, 0f, 0.1f, 1f)
    private val activeAnimators = mutableListOf<ValueAnimator>()

    // Distance for waterfall scroll. Button height is 56dp.
    // Moving ~24dp from center is enough for a smooth transition without total exit.
    private val animDistance = resources.displayMetrics.density * 24f

     fun animateIfNeeded(formattedSeconds: String) {
         if (!viewModel.uiState.value.showSeconds) return

         val currentState = viewModel.settingsButtonAnimState.value
         
         if (currentState.currentSeconds.isEmpty()) {
             val nextSecText = try {
                 val sec = formattedSeconds.toInt()
                 String.format(java.util.Locale.US, "%02d", (sec + 1) % 60)
             } catch (e: Exception) { "01" }
             
             viewModel.updateSettingsButtonAnim { state ->
                 state.copy(
                     currentSeconds = formattedSeconds,
                     nextSeconds = nextSecText,
                     activeTranslationY = 0f,
                     incomingTranslationY = animDistance,
                     activeAlpha = 1f,
                     incomingAlpha = WaterfallAnimationHelper.PREVIEW_ALPHA
                 )
             }
             return
         }
         
         if (currentState.currentSeconds == formattedSeconds) return

         runWaterfallAnimation(formattedSeconds, 320L)
     }

     private fun runWaterfallAnimation(newSeconds: String, duration: Long) {
         cancelExistingAnimations()

         val currentState = viewModel.settingsButtonAnimState.value
         val oldSecondsText = currentState.currentSeconds

         // Predict next second for preview
         val nextSecText = try {
             val sec = newSeconds.toInt()
             String.format(java.util.Locale.US, "%02d", (sec + 1) % 60)
         } catch (e: Exception) { "" }

         // Track whether haptic has been fired at visual center
         var hapticFiredAtCenter = false

         val animator = ValueAnimator.ofFloat(0f, 1f).apply {
             this.duration = 650L
             interpolator = android.view.animation.DecelerateInterpolator(1.2f)
            addUpdateListener { anim ->
                val progress = anim.animatedValue as Float

                // Fire haptic at visual center (30% progress = ~195ms into animation)
                // This is when the new digit is most visible in the "small window"
                // Use performSecondsTick() for lighter haptic feedback
                if (progress >= 0.30f && !hapticFiredAtCenter) {
                    haptics?.performSecondsTick()
                    hapticFiredAtCenter = true
                }

                viewModel.updateSettingsButtonAnim { state ->
                    state.copy(
                        currentSeconds = oldSecondsText,
                        nextSeconds = newSeconds,
                        activeTranslationY = -animDistance * progress,
                        incomingTranslationY = animDistance * (1f - progress),
                        activeAlpha = WaterfallAnimationHelper.getOutgoingAlpha(progress),
                        incomingAlpha = WaterfallAnimationHelper.getIncomingAlpha(progress)
                    )
                }
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    finalize(newSeconds, nextSecText)
                }
                override fun onAnimationCancel(animation: Animator) {
                    finalize(newSeconds, nextSecText)
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        activeAnimators.add(animator)
        animator.start()
    }

    private fun finalize(current: String, next: String) {
        viewModel.updateSettingsButtonAnim { state ->
            state.copy(
                currentSeconds = current,
                nextSeconds = next,
                activeTranslationY = 0f,
                incomingTranslationY = animDistance, // Positioned at bottom for preview
                activeAlpha = 1f,
                incomingAlpha = WaterfallAnimationHelper.PREVIEW_ALPHA
            )
        }
    }

    private fun cancelExistingAnimations() {
        activeAnimators.forEach { it.cancel() }
        activeAnimators.clear()
    }

    fun cleanup() {
        cancelExistingAnimations()
    }
}
