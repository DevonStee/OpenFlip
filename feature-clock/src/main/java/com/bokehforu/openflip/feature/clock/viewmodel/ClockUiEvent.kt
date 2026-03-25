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
