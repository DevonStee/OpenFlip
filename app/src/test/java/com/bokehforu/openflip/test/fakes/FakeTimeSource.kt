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

package com.bokehforu.openflip.test.fakes

import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import com.bokehforu.openflip.core.manager.Time
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class FakeTimeSource : TimeSource {
    private val _timeFlow = MutableStateFlow(Time(12, 0, 0, false))
    
    override fun getCurrentTime(is24Hour: Boolean) = _timeFlow.value.copy(is24Hour = is24Hour)
    
    override fun timeFlow(is24Hour: Boolean) = _timeFlow.asStateFlow()
    
    override fun secondsFlow(is24Hour: Boolean): Flow<Time> = flow {
        while (true) {
            emit(_timeFlow.value.copy(is24Hour = is24Hour))
            delay(1000)
        }
    }
    
    fun setTime(hour: Int, minute: Int, second: Int) {
        _timeFlow.value = Time(hour, minute, second, false)
    }
}
