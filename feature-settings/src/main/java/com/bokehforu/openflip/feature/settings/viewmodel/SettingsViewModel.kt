package com.bokehforu.openflip.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateHapticEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateOledProtectionEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateOrientationModeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateScaleEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowFlapsUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.domain.usecase.UpdateSoundEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateSwipeToDimEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateTimedBulbOffEnabledUseCase
import com.bokehforu.openflip.domain.usecase.UpdateTimeFormatUseCase
import com.bokehforu.openflip.domain.usecase.UpdateWakeLockModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val updateTimeFormatUseCase: UpdateTimeFormatUseCase,
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val updateShowSecondsUseCase: UpdateShowSecondsUseCase,
    private val updateShowFlapsUseCase: UpdateShowFlapsUseCase,
    private val updateHapticEnabledUseCase: UpdateHapticEnabledUseCase,
    private val updateSoundEnabledUseCase: UpdateSoundEnabledUseCase,
    private val updateOrientationModeUseCase: UpdateOrientationModeUseCase,
    private val updateWakeLockModeUseCase: UpdateWakeLockModeUseCase,
    private val updateSwipeToDimEnabledUseCase: UpdateSwipeToDimEnabledUseCase,
    private val updateScaleEnabledUseCase: UpdateScaleEnabledUseCase,
    private val updateTimedBulbOffEnabledUseCase: UpdateTimedBulbOffEnabledUseCase,
    private val updateOledProtectionEnabledUseCase: UpdateOledProtectionEnabledUseCase
) : ViewModel() {

    data class SettingsUiState(
        val timeFormatMode: Int,
        val orientationMode: Int,
        val wakeLockMode: Int,
        val isDarkTheme: Boolean,
        val showSeconds: Boolean,
        val showFlaps: Boolean,
        val isHapticEnabled: Boolean,
        val isSoundEnabled: Boolean,
        val isSwipeToDimEnabled: Boolean,
        val isScaleEnabled: Boolean,
        val isTimedBulbOffEnabled: Boolean,
        val isOledProtectionEnabled: Boolean,
        val isHourlyChimeEnabled: Boolean
    )

    sealed interface SettingsUiAction {
        data class SetTimeFormatMode(val mode: Int) : SettingsUiAction
        data class SetOrientationMode(val mode: Int) : SettingsUiAction
        data class SetWakeLockMode(val mode: Int) : SettingsUiAction
        data class SetDarkTheme(val isDark: Boolean) : SettingsUiAction
        data class SetShowSeconds(val enabled: Boolean) : SettingsUiAction
        data class SetShowFlaps(val enabled: Boolean) : SettingsUiAction
        data class SetHapticEnabled(val enabled: Boolean) : SettingsUiAction
        data class SetSoundEnabled(val enabled: Boolean) : SettingsUiAction
        data class SetSwipeToDimEnabled(val enabled: Boolean) : SettingsUiAction
        data class SetScaleEnabled(val enabled: Boolean) : SettingsUiAction
        data class SetTimedBulbOffEnabled(val enabled: Boolean) : SettingsUiAction
        data class SetOledProtectionEnabled(val enabled: Boolean) : SettingsUiAction
        data object ResetToDefaults : SettingsUiAction
    }

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsFlow
        .map { settings ->
            SettingsUiState(
                timeFormatMode = settings.timeFormatMode,
                orientationMode = settings.orientationMode,
                wakeLockMode = settings.wakeLockMode,
                isDarkTheme = settings.isDarkTheme,
                showSeconds = settings.showSeconds,
                showFlaps = settings.showFlaps,
                isHapticEnabled = settings.isHapticEnabled,
                isSoundEnabled = settings.isSoundEnabled,
                isSwipeToDimEnabled = settings.isSwipeToDimEnabled,
                isScaleEnabled = settings.isScaleEnabled,
                isTimedBulbOffEnabled = settings.isTimedBulbOffEnabled,
                isOledProtectionEnabled = settings.isOledProtectionEnabled,
                isHourlyChimeEnabled = settings.isHourlyChimeEnabled
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), currentUiState())

    val timeFormatSelection = uiState
        .map { it.timeFormatMode }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.getTimeFormatMode())

    val orientationSelection = uiState
        .map { it.orientationMode }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.getOrientationMode())

    val wakeLockSelection = uiState
        .map { it.wakeLockMode }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.getWakeLockMode())

    val isDarkTheme = uiState
        .map { it.isDarkTheme }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isDarkTheme())

    val showSeconds = uiState
        .map { it.showSeconds }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.showSeconds())

    val showFlaps = uiState
        .map { it.showFlaps }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.showFlaps())

    val isHapticEnabled = uiState
        .map { it.isHapticEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isHapticEnabled())

    val isSoundEnabled = uiState
        .map { it.isSoundEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isSoundEnabled())

    val isSwipeToDimEnabled = uiState
        .map { it.isSwipeToDimEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isSwipeToDimEnabled())

    val isScaleEnabled = uiState
        .map { it.isScaleEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isScaleEnabled())

    val isTimedBulbOffEnabled = uiState
        .map { it.isTimedBulbOffEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isTimedBulbOffEnabled())

    val isOledProtectionEnabled = uiState
        .map { it.isOledProtectionEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isOledProtectionEnabled())

    val isHourlyChimeEnabled = uiState
        .map { it.isHourlyChimeEnabled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsRepository.isHourlyChimeEnabled())

    fun dispatch(action: SettingsUiAction) {
        when (action) {
            is SettingsUiAction.SetTimeFormatMode -> updateTimeFormatUseCase.execute(action.mode)
            is SettingsUiAction.SetOrientationMode -> updateOrientationModeUseCase.execute(action.mode)
            is SettingsUiAction.SetWakeLockMode -> updateWakeLockModeUseCase.execute(action.mode)
            is SettingsUiAction.SetDarkTheme -> toggleThemeUseCase.set(action.isDark)
            is SettingsUiAction.SetShowSeconds -> updateShowSecondsUseCase.execute(action.enabled)
            is SettingsUiAction.SetShowFlaps -> updateShowFlapsUseCase.execute(action.enabled)
            is SettingsUiAction.SetHapticEnabled -> updateHapticEnabledUseCase.execute(action.enabled)
            is SettingsUiAction.SetSoundEnabled -> updateSoundEnabledUseCase.execute(action.enabled)
            is SettingsUiAction.SetSwipeToDimEnabled -> updateSwipeToDimEnabledUseCase.execute(action.enabled)
            is SettingsUiAction.SetScaleEnabled -> updateScaleEnabledUseCase.execute(action.enabled)
            is SettingsUiAction.SetTimedBulbOffEnabled -> updateTimedBulbOffEnabledUseCase.execute(action.enabled)
            is SettingsUiAction.SetOledProtectionEnabled -> updateOledProtectionEnabledUseCase.execute(action.enabled)
            SettingsUiAction.ResetToDefaults -> settingsRepository.resetToDefaults()
        }
    }

    fun setTimeFormatMode(mode: Int) = dispatch(SettingsUiAction.SetTimeFormatMode(mode))

    fun setOrientationMode(mode: Int) = dispatch(SettingsUiAction.SetOrientationMode(mode))

    fun setWakeLockMode(mode: Int) = dispatch(SettingsUiAction.SetWakeLockMode(mode))

    fun setDarkTheme(isDark: Boolean) = dispatch(SettingsUiAction.SetDarkTheme(isDark))

    fun setShowSeconds(enabled: Boolean) = dispatch(SettingsUiAction.SetShowSeconds(enabled))

    fun setShowFlaps(enabled: Boolean) = dispatch(SettingsUiAction.SetShowFlaps(enabled))

    fun setHapticEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetHapticEnabled(enabled))

    fun setSoundEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetSoundEnabled(enabled))

    fun setSwipeToDimEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetSwipeToDimEnabled(enabled))

    fun setScaleEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetScaleEnabled(enabled))

    fun setTimedBulbOffEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetTimedBulbOffEnabled(enabled))

    fun setOledProtectionEnabled(enabled: Boolean) = dispatch(SettingsUiAction.SetOledProtectionEnabled(enabled))

    fun resetToDefaults() = dispatch(SettingsUiAction.ResetToDefaults)

    private fun currentUiState(): SettingsUiState {
        return SettingsUiState(
            timeFormatMode = settingsRepository.getTimeFormatMode(),
            orientationMode = settingsRepository.getOrientationMode(),
            wakeLockMode = settingsRepository.getWakeLockMode(),
            isDarkTheme = settingsRepository.isDarkTheme(),
            showSeconds = settingsRepository.showSeconds(),
            showFlaps = settingsRepository.showFlaps(),
            isHapticEnabled = settingsRepository.isHapticEnabled(),
            isSoundEnabled = settingsRepository.isSoundEnabled(),
            isSwipeToDimEnabled = settingsRepository.isSwipeToDimEnabled(),
            isScaleEnabled = settingsRepository.isScaleEnabled(),
            isTimedBulbOffEnabled = settingsRepository.isTimedBulbOffEnabled(),
            isOledProtectionEnabled = settingsRepository.isOledProtectionEnabled(),
            isHourlyChimeEnabled = settingsRepository.isHourlyChimeEnabled()
        )
    }
}
