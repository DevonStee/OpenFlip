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

package com.bokehforu.openflip.domain.repository

import com.bokehforu.openflip.core.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settingsFlow: StateFlow<Settings>
    val isDarkThemeFlow: Flow<Boolean>
    val showSecondsFlow: Flow<Boolean>
    val showFlapsFlow: Flow<Boolean>
    val isHapticEnabledFlow: Flow<Boolean>
    val isSoundEnabledFlow: Flow<Boolean>
    val isSwipeToDimEnabledFlow: Flow<Boolean>
    val isScaleEnabledFlow: Flow<Boolean>
    val orientationModeFlow: Flow<Int>
    val wakeLockModeFlow: Flow<Int>
    val isOledProtectionEnabledFlow: Flow<Boolean>
    val isTimedBulbOffEnabledFlow: Flow<Boolean>
    val isHourlyChimeEnabledFlow: Flow<Boolean>

    fun isDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)

    fun showSeconds(): Boolean
    fun setShowSeconds(enabled: Boolean)

    fun showFlaps(): Boolean
    fun setShowFlaps(enabled: Boolean)

    fun isHapticEnabled(): Boolean
    fun setHapticEnabled(enabled: Boolean)

    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)

    fun isSwipeToDimEnabled(): Boolean
    fun setSwipeToDimEnabled(enabled: Boolean)

    fun isScaleEnabled(): Boolean
    fun setScaleEnabled(enabled: Boolean)

    fun getOrientationMode(): Int
    fun setOrientationMode(mode: Int)

    fun getWakeLockMode(): Int
    fun setWakeLockMode(mode: Int)

    fun getTimeFormatMode(): Int
    fun setTimeFormatMode(mode: Int)

    fun isOledProtectionEnabled(): Boolean
    fun setOledProtectionEnabled(enabled: Boolean)

    fun isTimedBulbOffEnabled(): Boolean
    fun setTimedBulbOffEnabled(enabled: Boolean)

    fun isHourlyChimeEnabled(): Boolean
    fun setHourlyChimeEnabled(enabled: Boolean)

    fun getBrightnessOverride(): Float
    fun setBrightnessOverride(value: Float)

    fun resetToDefaults()
}
