package com.bokehforu.openflip.feature.clock.viewmodel

import com.bokehforu.openflip.core.manager.Time
import kotlin.time.Duration

data class ClockUiState(
    val time: Time = Time(0, 0, 0, true),
    val theme: ThemeMode = ThemeMode.DARK,
    val timeFormatMode: Int = 0,
    val showSeconds: Boolean = false,
    val showFlaps: Boolean = true,
    val isInteracting: Boolean = false,
    val isTimeTraveling: Boolean = false,
    val virtualTimeOffset: Duration = Duration.ZERO,
    val lightEffect: LightEffectState = LightEffectState.OFF,

    // Bulb (light toggle) runtime state. This is separate from the Settings switch
    // (timed vs long-on). We persist the *current* state in ViewModel/SavedStateHandle.
    val bulb: BulbState = BulbState.OFF,
    val bulbCountdownSeconds: Int = 0,
    val sleepTimerRemaining: Duration? = null,
    val scale: Float = 1.0f,
    val oledProtectionEnabled: Boolean = false,
    val zenMode: Boolean = false,
    val swipeToDimEnabled: Boolean = false,
    val isScaleEnabled: Boolean = false,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val orientationMode: Int = 0,
    val wakeLockMode: Int = 2,
    val isHourlyChimeEnabled: Boolean = false,
    
    val showSettingsButton: Boolean = true,
    val showThemeButton: Boolean = true,
    val showLightButton: Boolean = true,
    val showKnobButton: Boolean = true,
    val showSwipeHint: Boolean = false,
    
    val settingsButtonAnim: SettingsButtonAnimState = SettingsButtonAnimState(),
    val gearRotationTrigger: Int = 0,
    
    val sleepTimerState: SleepTimerState = SleepTimerState(),
    
    val brightnessOverride: Float = -1f
)

enum class ThemeMode {
    LIGHT, DARK
}

sealed class LightEffectState {
    object OFF : LightEffectState()
    data class ON(val remainingTime: Duration) : LightEffectState()
}

sealed class BulbState {
    object OFF : BulbState()
    /**
     * @param endElapsedRealtimeMs null means "long-on" (no auto-off).
     */
    data class ON(val endElapsedRealtimeMs: Long?) : BulbState()
}

data class SettingsButtonAnimState(
    val activeTranslationY: Float = 0f,
    val incomingTranslationY: Float = 0f,
    val activeAlpha: Float = 1f,
    val incomingAlpha: Float = 0f,
    val currentSeconds: String = "",
    val nextSeconds: String = ""
)

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0,
    val originalDurationMinutes: Int = 0
)
