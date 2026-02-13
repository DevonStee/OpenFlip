package com.bokehforu.openflip.feature.clock.ui.controller

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bokehforu.openflip.feature.clock.controller.TimeTravelController
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.bokehforu.openflip.feature.clock.view.InfiniteKnobView
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView

/**
 * Controller responsible for the Infinite Knob interaction and Time Travel logic.
 */
class KnobInteractionController(
    private val lifecycleOwner: LifecycleOwner,
    private val settingsRepository: SettingsRepository,
    private val viewModel: FullscreenClockViewModel,
    private val sound: SoundProvider,
    private val haptics: HapticsProvider,
    private val knobView: InfiniteKnobView,
    private val clockView: FullscreenFlipClockView
) {
    lateinit var timeTravelController: TimeTravelController
        private set

    fun initialize() {
        setupKnob()
        setupTimeTravel()
    }

     private fun setupKnob() {
         knobView.hapticManager = haptics

         knobView.onRotationChangedListener = { totalDegrees: Float ->
             if (::timeTravelController.isInitialized) {
                 timeTravelController.onRotationChanged(totalDegrees)
             }
         }

         knobView.onTickListener = {
             sound.playClickSound()
         }
     }

     private fun setupTimeTravel() {
        timeTravelController = TimeTravelController(
            timeFormatModeProvider = { viewModel.uiState.value.timeFormatMode },
            onTimeUpdate = { hour, minute, isDecreasing, amPm ->
                clockView.setTimeWithDirection(
                    hour = hour,
                    minute = minute,
                    isDecreasing = isDecreasing,
                     animate = viewModel.uiState.value.showFlaps,
                     amPm = amPm
                 )
             }
         )
        // Verify if TimeTravelController implements DefaultLifecycleObserver before adding
        // Assuming it does based on original code usage: lifecycle.addObserver(timeTravelController)
        lifecycleOwner.lifecycle.addObserver(timeTravelController)
    }

    fun stopKnobFling() {
        knobView.stopFling()
    }

    /**
     * Cleanup for manual removal before re-inflation (orientation change)
     */
    fun cleanup() {
        if (::timeTravelController.isInitialized) {
            lifecycleOwner.lifecycle.removeObserver(timeTravelController)
        }
        knobView.onRotationChangedListener = null
        knobView.onTickListener = null
    }
}
