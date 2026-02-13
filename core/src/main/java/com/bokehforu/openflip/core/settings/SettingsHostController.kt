package com.bokehforu.openflip.core.settings

import kotlinx.coroutines.flow.StateFlow

data class SettingsSleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0
)

interface SettingsHostController {
    val sleepTimerState: StateFlow<SettingsSleepTimerState>

    fun performSettingsClickFeedback()

    fun setSettingsInteracting(interacting: Boolean)

    fun startSleepTimer(minutes: Int)

    fun stopSleepTimer()
}
