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
