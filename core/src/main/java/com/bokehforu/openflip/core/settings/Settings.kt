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
