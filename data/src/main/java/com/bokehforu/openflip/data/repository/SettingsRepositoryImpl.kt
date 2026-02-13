package com.bokehforu.openflip.data.repository

import com.bokehforu.openflip.data.settings.SettingsStore
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.core.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsStore: SettingsStore
) : SettingsRepository {

    override val settingsFlow: StateFlow<Settings> = settingsStore.settingsFlow

    override val isDarkThemeFlow: Flow<Boolean> = settingsFlow
        .map { it.isDarkTheme }
        .distinctUntilChanged()

    override val showSecondsFlow: Flow<Boolean> = settingsFlow
        .map { it.showSeconds }
        .distinctUntilChanged()

    override val showFlapsFlow: Flow<Boolean> = settingsFlow
        .map { it.showFlaps }
        .distinctUntilChanged()

    override val isHapticEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isHapticEnabled }
        .distinctUntilChanged()

    override val isSoundEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isSoundEnabled }
        .distinctUntilChanged()

    override val isSwipeToDimEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isSwipeToDimEnabled }
        .distinctUntilChanged()

    override val isScaleEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isScaleEnabled }
        .distinctUntilChanged()

    override val orientationModeFlow: Flow<Int> = settingsFlow
        .map { it.orientationMode }
        .distinctUntilChanged()

    override val wakeLockModeFlow: Flow<Int> = settingsFlow
        .map { it.wakeLockMode }
        .distinctUntilChanged()

    override val isOledProtectionEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isOledProtectionEnabled }
        .distinctUntilChanged()

    override val isTimedBulbOffEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isTimedBulbOffEnabled }
        .distinctUntilChanged()

    override val isHourlyChimeEnabledFlow: Flow<Boolean> = settingsFlow
        .map { it.isHourlyChimeEnabled }
        .distinctUntilChanged()

    override fun isDarkTheme(): Boolean = settingsStore.isDarkTheme

    override fun setDarkTheme(isDark: Boolean) {
        if (settingsStore.isDarkTheme == isDark) return
        settingsStore.isDarkTheme = isDark
    }

    override fun showSeconds(): Boolean = settingsStore.showSeconds

    override fun setShowSeconds(enabled: Boolean) {
        if (settingsStore.showSeconds == enabled) return
        settingsStore.showSeconds = enabled
    }

    override fun showFlaps(): Boolean = settingsStore.showFlaps

    override fun setShowFlaps(enabled: Boolean) {
        if (settingsStore.showFlaps == enabled) return
        settingsStore.showFlaps = enabled
    }

    override fun isHapticEnabled(): Boolean = settingsStore.isHapticEnabled

    override fun setHapticEnabled(enabled: Boolean) {
        if (settingsStore.isHapticEnabled == enabled) return
        settingsStore.isHapticEnabled = enabled
    }

    override fun isSoundEnabled(): Boolean = settingsStore.isSoundEnabled

    override fun setSoundEnabled(enabled: Boolean) {
        if (settingsStore.isSoundEnabled == enabled) return
        settingsStore.isSoundEnabled = enabled
    }

    override fun isSwipeToDimEnabled(): Boolean = settingsStore.isSwipeToDimEnabled

    override fun setSwipeToDimEnabled(enabled: Boolean) {
        if (settingsStore.isSwipeToDimEnabled == enabled) return
        settingsStore.isSwipeToDimEnabled = enabled
    }

    override fun isScaleEnabled(): Boolean = settingsStore.isScaleEnabled

    override fun setScaleEnabled(enabled: Boolean) {
        if (settingsStore.isScaleEnabled == enabled) return
        settingsStore.isScaleEnabled = enabled
    }

    override fun getOrientationMode(): Int = settingsStore.orientationMode

    override fun setOrientationMode(mode: Int) {
        if (settingsStore.orientationMode == mode) return
        settingsStore.orientationMode = mode
    }

    override fun getWakeLockMode(): Int = settingsStore.wakeLockMode

    override fun setWakeLockMode(mode: Int) {
        if (settingsStore.wakeLockMode == mode) return
        settingsStore.wakeLockMode = mode
    }

    override fun getTimeFormatMode(): Int = settingsStore.timeFormatMode

    override fun setTimeFormatMode(mode: Int) {
        if (settingsStore.timeFormatMode == mode) return
        settingsStore.timeFormatMode = mode
    }

    override fun isOledProtectionEnabled(): Boolean = settingsStore.isOledProtectionEnabled

    override fun setOledProtectionEnabled(enabled: Boolean) {
        if (settingsStore.isOledProtectionEnabled == enabled) return
        settingsStore.isOledProtectionEnabled = enabled
    }

    override fun isTimedBulbOffEnabled(): Boolean = settingsStore.isTimedBulbOffEnabled

    override fun setTimedBulbOffEnabled(enabled: Boolean) {
        if (settingsStore.isTimedBulbOffEnabled == enabled) return
        settingsStore.isTimedBulbOffEnabled = enabled
    }

    override fun isHourlyChimeEnabled(): Boolean = settingsStore.isHourlyChimeEnabled

    override fun setHourlyChimeEnabled(enabled: Boolean) {
        if (settingsStore.isHourlyChimeEnabled == enabled) return
        settingsStore.isHourlyChimeEnabled = enabled
    }

    override fun getBrightnessOverride(): Float = settingsStore.brightnessOverride

    override fun setBrightnessOverride(value: Float) {
        if (settingsStore.brightnessOverride == value) return
        settingsStore.brightnessOverride = value
    }

    override fun resetToDefaults() {
        settingsStore.resetToDefaults()
    }
}
