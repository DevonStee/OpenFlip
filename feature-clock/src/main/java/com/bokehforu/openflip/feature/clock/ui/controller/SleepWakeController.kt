package com.bokehforu.openflip.feature.clock.ui.controller

import android.app.Activity
import android.os.BatteryManager
import android.view.Window
import android.view.WindowManager
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.bokehforu.openflip.feature.clock.manager.DisplayBurnInProtectionManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SleepWakeController(
    private val activity: Activity,
    private val window: Window,
    private val viewModel: FullscreenClockViewModel,
    private val burnInProtectionManager: DisplayBurnInProtectionManager
) {
    private var sleepTimerJob: Job? = null
    private var timerFinishedJob: Job? = null

    fun bind(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        unbind()

        // Observe Sleep Timer State for WakeLock updates
        sleepTimerJob = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.sleepTimerState.collect {
                    applyWakeLockMode()
                }
            }
        }

        // Observe Timer Finished Event
        timerFinishedJob = lifecycleOwner.lifecycleScope.launch {
            viewModel.timerFinishedEvent.collectLatest {
                applyWakeLockMode()
                activity.finishAndRemoveTask()
            }
        }
    }

    fun unbind() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        timerFinishedJob?.cancel()
        timerFinishedJob = null
    }

    fun applyWakeLockMode() {
        if (viewModel.sleepTimerState.value.isActive) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            applyFrameRate(true)
            return
        }

         when (viewModel.uiState.value.wakeLockMode) {
            0 -> { // Always
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                applyFrameRate(true)
            }
            1 -> { // Charging Only
                if (isCharging()) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    applyFrameRate(true)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    applyFrameRate(false)
                }
            }
            else -> { // System Default
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                applyFrameRate(false)
            }
        }
    }

     fun onResume() {
         if (viewModel.uiState.value.oledProtectionEnabled) {
            burnInProtectionManager.start()
        }
    }

    fun onPause() {
        burnInProtectionManager.stop()
    }

    private fun isCharging(): Boolean {
        val batteryManager = activity.getSystemService(Activity.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.isCharging ?: false
    }

    private fun applyFrameRate(highPerformance: Boolean) {
        val layoutParams = window.attributes
        if (!highPerformance) {
            layoutParams.preferredRefreshRate = 0f
            layoutParams.preferredDisplayModeId = 0
        }
        window.attributes = layoutParams
    }
}
