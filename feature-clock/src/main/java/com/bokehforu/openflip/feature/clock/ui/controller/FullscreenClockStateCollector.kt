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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bokehforu.openflip.feature.clock.viewmodel.ClockUiState
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import kotlinx.coroutines.launch

class FullscreenClockStateCollector(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: FullscreenClockViewModel,
    private val onRenderState: (ClockUiState) -> Unit,
    private val onInteractionChanged: () -> Unit,
    private val onLightSourceUpdate: () -> Unit
) {

    fun start() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    onRenderState(state)
                }
            }
        }

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isInteractingFlow.collect {
                    onInteractionChanged()
                    onLightSourceUpdate()
                }
            }
        }
    }
}
