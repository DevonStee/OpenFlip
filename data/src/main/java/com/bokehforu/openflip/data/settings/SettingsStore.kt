package com.bokehforu.openflip.data.settings

import com.bokehforu.openflip.core.settings.Settings
import kotlinx.coroutines.flow.StateFlow

interface SettingsStore {
    val settingsFlow: StateFlow<Settings>
    
    var timeFormatMode: Int
    var isDarkTheme: Boolean
    var isHapticEnabled: Boolean
    var isSoundEnabled: Boolean
    var showSeconds: Boolean
    var showFlaps: Boolean
    var isSwipeToDimEnabled: Boolean
    var isScaleEnabled: Boolean
    var orientationMode: Int
    var wakeLockMode: Int
    var isOledProtectionEnabled: Boolean
    var isTimedBulbOffEnabled: Boolean
    var isHourlyChimeEnabled: Boolean
    var brightnessOverride: Float
    
    val is24Hour: Boolean
    
    fun resetToDefaults()
}
