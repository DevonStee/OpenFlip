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
