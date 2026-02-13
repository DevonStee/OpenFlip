package com.bokehforu.openflip.data.settings

import android.content.Context
import androidx.core.content.edit
import com.bokehforu.openflip.core.settings.Settings
import com.bokehforu.openflip.core.settings.SettingsDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppSettingsManager @Inject constructor(context: Context) : SettingsStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadCurrentSettings(): Settings = Settings(
        timeFormatMode = prefs.getInt(KEY_TIME_FORMAT_MODE, DEFAULT_TIME_FORMAT_MODE),
        isDarkTheme = prefs.getBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME),
        isHapticEnabled = prefs.getBoolean(KEY_IS_HAPTIC_ENABLED, DEFAULT_HAPTIC_ENABLED),
        isSoundEnabled = prefs.getBoolean(KEY_IS_SOUND_ENABLED, DEFAULT_SOUND_ENABLED),
        showSeconds = prefs.getBoolean(KEY_IS_SHOW_SECONDS, DEFAULT_SHOW_SECONDS),
        showFlaps = prefs.getBoolean(KEY_IS_SHOW_FLAPS, DEFAULT_SHOW_FLAPS),
        isSwipeToDimEnabled = prefs.getBoolean(KEY_IS_SWIPE_TO_DIM, DEFAULT_SWIPE_TO_DIM),
        isScaleEnabled = prefs.getBoolean(KEY_IS_SCALE_ENABLED, DEFAULT_SCALE_ENABLED),
        orientationMode = prefs.getInt(KEY_ORIENTATION_MODE, DEFAULT_ORIENTATION_MODE),
        wakeLockMode = prefs.getInt(KEY_WAKE_LOCK_MODE, DEFAULT_WAKE_LOCK_MODE),
        isOledProtectionEnabled = prefs.getBoolean(KEY_OLED_PROTECTION, DEFAULT_OLED_PROTECTION),
        isTimedBulbOffEnabled = prefs.getBoolean(KEY_IS_TIMED_BULB_OFF_ENABLED, DEFAULT_TIMED_BULB_OFF),
        isHourlyChimeEnabled = prefs.getBoolean(KEY_IS_HOURLY_CHIME_ENABLED, DEFAULT_HOURLY_CHIME),
        brightnessOverride = prefs.getFloat(KEY_BRIGHTNESS_OVERRIDE, -1.0f)
    )

    private val _settingsFlow = MutableStateFlow(loadCurrentSettings())
    override val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()

    interface Listener {
        fun onFormatChanged(is24Hour: Boolean)
        fun onThemeChanged(isDark: Boolean)
        fun onHapticChanged(isEnabled: Boolean) {}
        fun onSoundChanged(isEnabled: Boolean) {}
        fun onShowSecondsChanged(isShow: Boolean) {}
        fun onShowFlapsChanged(isShow: Boolean) {}
        fun onOrientationChanged(mode: Int) {}
        fun onWakeLockModeChanged(mode: Int) {}
        fun onTimedBulbOffChanged(isEnabled: Boolean) {}
        fun onHourlyChimeChanged(isEnabled: Boolean) {}
        fun onSettingsReset() {}
    }

    var listener: Listener? = null
    var suppressListeners = false
    
    override var timeFormatMode: Int
        get() = prefs.getInt(KEY_TIME_FORMAT_MODE, DEFAULT_TIME_FORMAT_MODE)
        set(value) {
            prefs.edit { putInt(KEY_TIME_FORMAT_MODE, value) }
            _settingsFlow.value = _settingsFlow.value.copy(timeFormatMode = value)
            if (!suppressListeners) listener?.onFormatChanged(value != 0)
        }

    override val is24Hour: Boolean
        get() = timeFormatMode != 0

    private val _isDarkThemeFlow = kotlinx.coroutines.flow.MutableStateFlow(isDarkTheme)
    fun isDarkThemeFlow() = _isDarkThemeFlow.asStateFlow()

    override var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_DARK_THEME, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isDarkTheme = value)
            _isDarkThemeFlow.value = value
            if (!suppressListeners) listener?.onThemeChanged(value)
        }

    override var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_HAPTIC_ENABLED, DEFAULT_HAPTIC_ENABLED)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_HAPTIC_ENABLED, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isHapticEnabled = value)
            _isHapticEnabledFlow.value = value
            if (!suppressListeners) listener?.onHapticChanged(value)
        }
    private val _isHapticEnabledFlow = MutableStateFlow(isHapticEnabled)
    fun isHapticEnabledFlow() = _isHapticEnabledFlow.asStateFlow()

    override var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_SOUND_ENABLED, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isSoundEnabled = value)
            _isSoundEnabledFlow.value = value
            if (!suppressListeners) listener?.onSoundChanged(value)
        }
    private val _isSoundEnabledFlow = MutableStateFlow(isSoundEnabled)
    fun isSoundEnabledFlow() = _isSoundEnabledFlow.asStateFlow()

    private val _showSecondsFlow = MutableStateFlow(showSeconds)
    fun showSecondsFlow() = _showSecondsFlow.asStateFlow()

    override var showSeconds: Boolean
        get() = prefs.getBoolean(KEY_IS_SHOW_SECONDS, DEFAULT_SHOW_SECONDS)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_SHOW_SECONDS, value) }
            _settingsFlow.value = _settingsFlow.value.copy(showSeconds = value)
            _showSecondsFlow.value = value
            if (!suppressListeners) listener?.onShowSecondsChanged(value)
        }

    override var showFlaps: Boolean
        get() = prefs.getBoolean(KEY_IS_SHOW_FLAPS, DEFAULT_SHOW_FLAPS)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_SHOW_FLAPS, value) }
            _settingsFlow.value = _settingsFlow.value.copy(showFlaps = value)
            _showFlapsFlow.value = value
            if (!suppressListeners) listener?.onShowFlapsChanged(value)
        }
    private val _showFlapsFlow = MutableStateFlow(showFlaps)
    fun showFlapsFlow() = _showFlapsFlow.asStateFlow()

    override var isSwipeToDimEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_SWIPE_TO_DIM, DEFAULT_SWIPE_TO_DIM)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_SWIPE_TO_DIM, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isSwipeToDimEnabled = value)
            _isSwipeToDimEnabledFlow.value = value
        }
    private val _isSwipeToDimEnabledFlow = MutableStateFlow(isSwipeToDimEnabled)
    fun isSwipeToDimEnabledFlow() = _isSwipeToDimEnabledFlow.asStateFlow()

    override var isScaleEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_SCALE_ENABLED, DEFAULT_SCALE_ENABLED)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_SCALE_ENABLED, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isScaleEnabled = value)
            _isScaleEnabledFlow.value = value
        }
    private val _isScaleEnabledFlow = MutableStateFlow(isScaleEnabled)
    fun isScaleEnabledFlow() = _isScaleEnabledFlow.asStateFlow()

    override var orientationMode: Int
        get() = prefs.getInt(KEY_ORIENTATION_MODE, DEFAULT_ORIENTATION_MODE)
        set(value) {
            prefs.edit { putInt(KEY_ORIENTATION_MODE, value) }
            _settingsFlow.value = _settingsFlow.value.copy(orientationMode = value)
            if (!suppressListeners) listener?.onOrientationChanged(value)
        }

    override var wakeLockMode: Int
        get() = prefs.getInt(KEY_WAKE_LOCK_MODE, DEFAULT_WAKE_LOCK_MODE)
        set(value) {
            prefs.edit { putInt(KEY_WAKE_LOCK_MODE, value) }
            _settingsFlow.value = _settingsFlow.value.copy(wakeLockMode = value)
            if (!suppressListeners) listener?.onWakeLockModeChanged(value)
        }

    override var isOledProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_OLED_PROTECTION, DEFAULT_OLED_PROTECTION)
        set(value) {
            prefs.edit { putBoolean(KEY_OLED_PROTECTION, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isOledProtectionEnabled = value)
            _isOledProtectionEnabledFlow.value = value
        }
    private val _isOledProtectionEnabledFlow = MutableStateFlow(isOledProtectionEnabled)
    fun isOledProtectionEnabledFlow() = _isOledProtectionEnabledFlow.asStateFlow()

    override var isTimedBulbOffEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_TIMED_BULB_OFF_ENABLED, DEFAULT_TIMED_BULB_OFF)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_TIMED_BULB_OFF_ENABLED, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isTimedBulbOffEnabled = value)
            _isTimedBulbOffEnabledFlow.value = value
            if (!suppressListeners) listener?.onTimedBulbOffChanged(value)
        }
    private val _isTimedBulbOffEnabledFlow = MutableStateFlow(isTimedBulbOffEnabled)
    fun isTimedBulbOffEnabledFlow() = _isTimedBulbOffEnabledFlow.asStateFlow()

    private val _isHourlyChimeEnabledFlow = MutableStateFlow(isHourlyChimeEnabled)
    fun isHourlyChimeEnabledFlow() = _isHourlyChimeEnabledFlow.asStateFlow()

    override var isHourlyChimeEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_HOURLY_CHIME_ENABLED, DEFAULT_HOURLY_CHIME)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_HOURLY_CHIME_ENABLED, value) }
            _settingsFlow.value = _settingsFlow.value.copy(isHourlyChimeEnabled = value)
            _isHourlyChimeEnabledFlow.value = value
            if (!suppressListeners) listener?.onHourlyChimeChanged(value)
        }

    override var brightnessOverride: Float
        get() = prefs.getFloat(KEY_BRIGHTNESS_OVERRIDE, -1.0f)
        set(value) {
            prefs.edit { putFloat(KEY_BRIGHTNESS_OVERRIDE, value) }
            _settingsFlow.value = _settingsFlow.value.copy(brightnessOverride = value)
        }

    /**
     * Resets all settings to their default values.
     * Uses batch write for better performance (single disk operation instead of 12+).
     * Suppresses individual listeners and triggers a single onSettingsReset() callback.
     */
    override fun resetToDefaults() {
        suppressListeners = true
        try {
            prefs.edit().apply {
                putInt(KEY_TIME_FORMAT_MODE, DEFAULT_TIME_FORMAT_MODE)
                putBoolean(KEY_IS_SHOW_SECONDS, DEFAULT_SHOW_SECONDS)
                putBoolean(KEY_IS_SHOW_FLAPS, DEFAULT_SHOW_FLAPS)
                putBoolean(KEY_IS_SWIPE_TO_DIM, DEFAULT_SWIPE_TO_DIM)
                putBoolean(KEY_IS_SCALE_ENABLED, DEFAULT_SCALE_ENABLED)
                putBoolean(KEY_IS_HAPTIC_ENABLED, DEFAULT_HAPTIC_ENABLED)
                putBoolean(KEY_IS_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)
                putBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME)
                putInt(KEY_ORIENTATION_MODE, DEFAULT_ORIENTATION_MODE)
                putInt(KEY_WAKE_LOCK_MODE, DEFAULT_WAKE_LOCK_MODE)
                putBoolean(KEY_OLED_PROTECTION, DEFAULT_OLED_PROTECTION)
                putBoolean(KEY_IS_TIMED_BULB_OFF_ENABLED, DEFAULT_TIMED_BULB_OFF)
                putBoolean(KEY_IS_HOURLY_CHIME_ENABLED, DEFAULT_HOURLY_CHIME)
                putFloat(KEY_BRIGHTNESS_OVERRIDE, -1.0f)
                apply()
            }

            _settingsFlow.value = loadCurrentSettings()
            
            _isHapticEnabledFlow.value = DEFAULT_HAPTIC_ENABLED
            _isSoundEnabledFlow.value = DEFAULT_SOUND_ENABLED
            _showSecondsFlow.value = DEFAULT_SHOW_SECONDS
            _showFlapsFlow.value = DEFAULT_SHOW_FLAPS
            _isSwipeToDimEnabledFlow.value = DEFAULT_SWIPE_TO_DIM
            _isScaleEnabledFlow.value = DEFAULT_SCALE_ENABLED
            _isOledProtectionEnabledFlow.value = DEFAULT_OLED_PROTECTION
            _isTimedBulbOffEnabledFlow.value = DEFAULT_TIMED_BULB_OFF
            _isHourlyChimeEnabledFlow.value = DEFAULT_HOURLY_CHIME

            listener?.onSettingsReset()
        } finally {
            suppressListeners = false
        }
    }

    companion object {
        private const val PREFS_NAME = "openflip_settings"
        
        // SharedPreferences keys
        private const val KEY_TIME_FORMAT_MODE = "time_format_mode"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_IS_HAPTIC_ENABLED = "is_haptic_enabled"
        private const val KEY_IS_SOUND_ENABLED = "is_sound_enabled"
        private const val KEY_IS_SHOW_SECONDS = "is_show_seconds"
        private const val KEY_IS_SHOW_FLAPS = "is_show_flaps"
        private const val KEY_IS_SWIPE_TO_DIM = "is_swipe_to_dim_enabled"
        private const val KEY_IS_SCALE_ENABLED = "is_scale_enabled"
        private const val KEY_ORIENTATION_MODE = "orientation_mode"
        private const val KEY_WAKE_LOCK_MODE = "wake_lock_mode"
        private const val KEY_OLED_PROTECTION = "oled_screen_protection"
        private const val KEY_IS_TIMED_BULB_OFF_ENABLED = "is_timed_bulb_off_enabled"
        private const val KEY_IS_HOURLY_CHIME_ENABLED = "is_hourly_chime_enabled"
        private const val KEY_BRIGHTNESS_OVERRIDE = "brightness_override"
        
        // Default values (single source of truth)
        const val DEFAULT_TIME_FORMAT_MODE = SettingsDefaults.TIME_FORMAT_MODE
        const val DEFAULT_SHOW_SECONDS = SettingsDefaults.SHOW_SECONDS
        const val DEFAULT_SHOW_FLAPS = SettingsDefaults.SHOW_FLAPS
        const val DEFAULT_SWIPE_TO_DIM = SettingsDefaults.SWIPE_TO_DIM
        const val DEFAULT_SCALE_ENABLED = SettingsDefaults.SCALE_ENABLED
        const val DEFAULT_HAPTIC_ENABLED = SettingsDefaults.HAPTIC_ENABLED
        const val DEFAULT_SOUND_ENABLED = SettingsDefaults.SOUND_ENABLED
        const val DEFAULT_DARK_THEME = SettingsDefaults.DARK_THEME
        const val DEFAULT_ORIENTATION_MODE = SettingsDefaults.ORIENTATION_MODE
        const val DEFAULT_WAKE_LOCK_MODE = SettingsDefaults.WAKE_LOCK_MODE
        const val DEFAULT_OLED_PROTECTION = SettingsDefaults.OLED_PROTECTION
        const val DEFAULT_TIMED_BULB_OFF = SettingsDefaults.TIMED_BULB_OFF
        const val DEFAULT_HOURLY_CHIME = SettingsDefaults.HOURLY_CHIME
    }
}
