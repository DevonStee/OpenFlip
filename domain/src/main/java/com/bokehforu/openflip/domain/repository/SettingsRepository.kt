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
