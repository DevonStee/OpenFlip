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

import com.bokehforu.openflip.data.settings.SettingsStore
import com.bokehforu.openflip.core.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsStore : SettingsStore {
    private val _settingsFlow = MutableStateFlow(Settings())
    override val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()
    
    override var timeFormatMode: Int
        get() = _settingsFlow.value.timeFormatMode
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(timeFormatMode = value)
        }
    
    override var isDarkTheme: Boolean
        get() = _settingsFlow.value.isDarkTheme
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isDarkTheme = value)
        }
    
    override var isHapticEnabled: Boolean
        get() = _settingsFlow.value.isHapticEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isHapticEnabled = value)
        }
    
    override var isSoundEnabled: Boolean
        get() = _settingsFlow.value.isSoundEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isSoundEnabled = value)
        }
    
    override var showSeconds: Boolean
        get() = _settingsFlow.value.showSeconds
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(showSeconds = value)
        }
    
    override var showFlaps: Boolean
        get() = _settingsFlow.value.showFlaps
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(showFlaps = value)
        }
    
    override var isSwipeToDimEnabled: Boolean
        get() = _settingsFlow.value.isSwipeToDimEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isSwipeToDimEnabled = value)
        }
    
    override var isScaleEnabled: Boolean
        get() = _settingsFlow.value.isScaleEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isScaleEnabled = value)
        }
    
    override var orientationMode: Int
        get() = _settingsFlow.value.orientationMode
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(orientationMode = value)
        }
    
    override var wakeLockMode: Int
        get() = _settingsFlow.value.wakeLockMode
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(wakeLockMode = value)
        }
    
    override var isOledProtectionEnabled: Boolean
        get() = _settingsFlow.value.isOledProtectionEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isOledProtectionEnabled = value)
        }
    
    override var isTimedBulbOffEnabled: Boolean
        get() = _settingsFlow.value.isTimedBulbOffEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isTimedBulbOffEnabled = value)
        }

    override var isHourlyChimeEnabled: Boolean
        get() = _settingsFlow.value.isHourlyChimeEnabled
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isHourlyChimeEnabled = value)
        }
    
    override var brightnessOverride: Float
        get() = _settingsFlow.value.brightnessOverride
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(brightnessOverride = value)
        }
    
    override val is24Hour: Boolean
        get() = timeFormatMode != 0
    
    override fun resetToDefaults() {
        _settingsFlow.value = Settings()
    }
}
