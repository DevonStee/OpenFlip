package com.bokehforu.openflip.feature.clock.viewmodel

import com.bokehforu.openflip.core.manager.Time
import kotlin.time.Duration

sealed class ClockUiEvent {
    data class TimeChanged(val time: Time) : ClockUiEvent()
    data class SettingsChanged(val field: String, val value: Any) : ClockUiEvent()
    
    object ThemeToggled : ClockUiEvent()
    object SecondsToggled : ClockUiEvent()
    object FlapsToggled : ClockUiEvent()
    object LightToggled : ClockUiEvent()
    
    data class ScaleChanged(val scale: Float) : ClockUiEvent()
    data class TimeTravelStarted(val offset: Duration) : ClockUiEvent()
    object TimeTravelEnded : ClockUiEvent()
    
    object SettingsOpened : ClockUiEvent()
    object SettingsClosed : ClockUiEvent()
    
    object InteractionStarted : ClockUiEvent()
    object InteractionEnded : ClockUiEvent()
    
    data class SleepTimerSet(val duration: Duration) : ClockUiEvent()
    object SleepTimerCancelled : ClockUiEvent()
}
