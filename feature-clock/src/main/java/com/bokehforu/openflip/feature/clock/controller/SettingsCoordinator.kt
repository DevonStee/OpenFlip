package com.bokehforu.openflip.feature.clock.controller

import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.core.settings.Settings
import com.bokehforu.openflip.feature.clock.ui.theme.ThemeApplier
import com.bokehforu.openflip.feature.clock.ui.WindowConfigurator
import com.bokehforu.openflip.feature.clock.view.StateToggleGlowView
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Coordinates application of settings changes to the various UI components and controllers.
 */
class SettingsCoordinator(
    private val settingsRepository: SettingsRepository,
    private val timeController: TimeManagementController,
    private val uiStateController: UIStateController,
    private val themeApplier: ThemeApplier,
    private val windowConfigurator: WindowConfigurator,
    private val haptics: HapticsProvider,
    private val sound: SoundProvider,
    private val clockView: FullscreenFlipClockView,
    private val stateToggleButton: StateToggleGlowView,
    private val applyOrientationAction: (Int) -> Unit,
    private val applyWakeLockModeAction: () -> Unit,
    private val forceTurnOffLight: () -> Unit,
    private val setLightToggleTheme: (Boolean) -> Unit,
    private val ensureInteractingState: () -> Unit,
    private val applyThemeAction: (isDark: Boolean) -> Unit,
    private val applyOledProtectionAction: (enabled: Boolean) -> Unit,
    private val resetBrightnessAction: () -> Unit,
    private val onTimedBulbModeChanged: (Boolean) -> Unit,
    private val isThemeTransitioning: () -> Boolean = { false }
) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var observersJob: Job? = null

    fun bind() {
        if (observersJob != null) return
        var previousSettings: Settings? = null
        observersJob = scope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                val previous = previousSettings
                if (previous == null) {
                    applyInitialState(settings)
                } else {
                    applyDiff(previous, settings)
                }
                previousSettings = settings
            }
        }
    }

    fun unbind() {
        observersJob?.cancel()
        observersJob = null
    }

    private fun onFormatChanged() {
        timeController.updateTime(animate = false)
    }

    fun onThemeChanged(isDark: Boolean) {
        themeApplier.applyTheme(isDark)
        // Skip window background update while a color transition animation is running;
        // the animator drives the background color directly during that period.
        if (!isThemeTransitioning()) {
            windowConfigurator.applyBackgroundColor(isDark)
        }
        stateToggleButton.setTheme(isDark)
        setLightToggleTheme(isDark)
        uiStateController.onThemeChanged(isDark)
    }

    private fun onHapticChanged(isEnabled: Boolean) {
        haptics.setHapticEnabled(isEnabled)
    }

    private fun onSoundChanged(isEnabled: Boolean) {
        sound.setSoundEnabled(isEnabled)
    }

    private fun onShowSecondsChanged(isShow: Boolean) {
         clockView.showSeconds = isShow
         
         if (isShow) {
             // Priority: Seconds mode takes absolute precedence - force light off immediately
             forceTurnOffLight()
             timeController.startSecondsTimer()
         } else {
             timeController.stopSecondsTimer()
         }
         uiStateController.updateSecondsVisibility()
    }

    private fun onShowFlapsChanged(isShow: Boolean) {
        clockView.showFlaps = isShow
    }

    private fun onOrientationChanged(mode: Int) {
        applyOrientationAction(mode)
    }

    private fun onWakeLockModeChanged(mode: Int) {
        applyWakeLockModeAction()
    }

    private fun onTimedBulbOffChanged(isEnabled: Boolean) {
        // Persisted in SettingsStore; notify ViewModel so it can adjust any in-flight countdown.
        onTimedBulbModeChanged(isEnabled)
    }

    private fun onHourlyChimeChanged(isEnabled: Boolean) {
        clockView.isHourlyChimeEnabled = isEnabled
    }

    private fun applyInitialState(settings: Settings) {
        onThemeChanged(settings.isDarkTheme)
        onHapticChanged(settings.isHapticEnabled)
        onSoundChanged(settings.isSoundEnabled)
        onShowSecondsChanged(settings.showSeconds)
        onShowFlapsChanged(settings.showFlaps)
        onOrientationChanged(settings.orientationMode)
        onWakeLockModeChanged(settings.wakeLockMode)
        onTimedBulbOffChanged(settings.isTimedBulbOffEnabled)
        onHourlyChimeChanged(settings.isHourlyChimeEnabled)
    }

    private fun applyDiff(previous: Settings, current: Settings) {
        val defaults = Settings()
        val isResetToDefaults = current == defaults && previous != defaults
        if (isResetToDefaults) {
            onSettingsReset()
            return
        }

        if (previous.timeFormatMode != current.timeFormatMode) {
            onFormatChanged()
        }
        if (previous.isDarkTheme != current.isDarkTheme) {
            onThemeChanged(current.isDarkTheme)
        }
        if (previous.isHapticEnabled != current.isHapticEnabled) {
            onHapticChanged(current.isHapticEnabled)
        }
        if (previous.isSoundEnabled != current.isSoundEnabled) {
            onSoundChanged(current.isSoundEnabled)
        }
        if (previous.showSeconds != current.showSeconds) {
            onShowSecondsChanged(current.showSeconds)
        }
        if (previous.showFlaps != current.showFlaps) {
            onShowFlapsChanged(current.showFlaps)
        }
        if (previous.orientationMode != current.orientationMode) {
            onOrientationChanged(current.orientationMode)
        }
        if (previous.wakeLockMode != current.wakeLockMode) {
            onWakeLockModeChanged(current.wakeLockMode)
        }
        if (previous.isTimedBulbOffEnabled != current.isTimedBulbOffEnabled) {
            onTimedBulbOffChanged(current.isTimedBulbOffEnabled)
        }
        if (previous.isHourlyChimeEnabled != current.isHourlyChimeEnabled) {
            onHourlyChimeChanged(current.isHourlyChimeEnabled)
        }
        if (previous.isOledProtectionEnabled != current.isOledProtectionEnabled) {
            applyOledProtectionAction(current.isOledProtectionEnabled)
        }
    }

    private fun onSettingsReset() {
        val defaults = Settings()
        timeController.updateTime(animate = false)
        clockView.showSeconds = false
        timeController.stopSecondsTimer()
        clockView.showFlaps = true
        clockView.resetScale()
        resetBrightnessAction()
        haptics.setHapticEnabled(true)
        sound.setSoundEnabled(true)
        clockView.isHourlyChimeEnabled = false
        applyOrientationAction(0)
        applyWakeLockModeAction()
        applyOledProtectionAction(false)
        clockView.backgroundColorOverride = null
        onThemeChanged(defaults.isDarkTheme)
        ensureInteractingState()
        uiStateController.updateSecondsVisibility()
    }
}
