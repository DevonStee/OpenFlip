package com.bokehforu.openflip.core.controller.interfaces

import com.bokehforu.openflip.core.manager.Time
import kotlinx.coroutines.flow.Flow

interface TimeSource {
    fun getCurrentTime(is24Hour: Boolean): Time
    fun timeFlow(is24Hour: Boolean): Flow<Time>
    fun secondsFlow(is24Hour: Boolean): Flow<Time>
}
