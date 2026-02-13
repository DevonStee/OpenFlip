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
