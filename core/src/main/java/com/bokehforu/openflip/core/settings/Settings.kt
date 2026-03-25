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

package com.bokehforu.openflip.core.settings

/**
 * Immutable data class representing all app settings.
 * This serves as the single source of truth for UI state derived from settings.
 */
data class Settings(
    val timeFormatMode: Int = SettingsDefaults.TIME_FORMAT_MODE,
    val isDarkTheme: Boolean = SettingsDefaults.DARK_THEME,
    val isHapticEnabled: Boolean = SettingsDefaults.HAPTIC_ENABLED,
    val isSoundEnabled: Boolean = SettingsDefaults.SOUND_ENABLED,
    val showSeconds: Boolean = SettingsDefaults.SHOW_SECONDS,
    val showFlaps: Boolean = SettingsDefaults.SHOW_FLAPS,
    val isSwipeToDimEnabled: Boolean = SettingsDefaults.SWIPE_TO_DIM,
    val isScaleEnabled: Boolean = SettingsDefaults.SCALE_ENABLED,
    val orientationMode: Int = SettingsDefaults.ORIENTATION_MODE,
    val wakeLockMode: Int = SettingsDefaults.WAKE_LOCK_MODE,
    val isOledProtectionEnabled: Boolean = SettingsDefaults.OLED_PROTECTION,
    val isTimedBulbOffEnabled: Boolean = SettingsDefaults.TIMED_BULB_OFF,
    val isHourlyChimeEnabled: Boolean = SettingsDefaults.HOURLY_CHIME,
    val brightnessOverride: Float = SettingsDefaults.BRIGHTNESS_OVERRIDE
) {
    /**
     * Computed property for convenience.
     * Returns true if time format is 24-hour (non-zero mode).
     */
    val is24Hour: Boolean
        get() = timeFormatMode != 0
}
