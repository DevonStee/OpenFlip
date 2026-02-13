package com.bokehforu.openflip.feature.clock.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bokehforu.openflip.core.controller.interfaces.ElapsedTimeSource
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.domain.usecase.StartSleepTimerUseCase
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import com.bokehforu.openflip.core.settings.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class FullscreenClockViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val timeSource: TimeSource,
    private val elapsedTimeSource: ElapsedTimeSource,
    private val haptics: HapticsProvider,
    private val sound: SoundProvider,
    private val toggleThemeUseCase: ToggleThemeUseCase,
    private val updateShowSecondsUseCase: UpdateShowSecondsUseCase,
    private val startSleepTimerUseCase: StartSleepTimerUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val appLifecycleMonitor: AppLifecycleMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    // Event to notify Activity that timer finished (to reset WakeLock settings)
    private val _timerFinishedEvent = MutableSharedFlow<Unit>()
    val timerFinishedEvent = _timerFinishedEvent.asSharedFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    private var bulbJob: kotlinx.coroutines.Job? = null

    private var settingsObserverJob: kotlinx.coroutines.Job? = null
    private var cachedSettings: Settings = settingsRepository.settingsFlow.value

    init {
        appLifecycleMonitor.initialize()
        observeSettings()
        observeTime()
        observeSeconds()
        restoreInteractionState()
        restoreBulbState()
    }
    
    override fun onCleared() {
        super.onCleared()
        appLifecycleMonitor.cleanup()
    }

    private fun observeSettings() {
        settingsObserverJob?.cancel()
        settingsObserverJob = viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                cachedSettings = settings
                _uiState.update { current ->
                    current.copy(
                        theme = if (settings.isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
                        timeFormatMode = settings.timeFormatMode,
                        showSeconds = settings.showSeconds,
                        showFlaps = settings.showFlaps,
                        swipeToDimEnabled = settings.isSwipeToDimEnabled,
                        isScaleEnabled = settings.isScaleEnabled,
                        hapticEnabled = settings.isHapticEnabled,
                        soundEnabled = settings.isSoundEnabled,
                        orientationMode = settings.orientationMode,
                        wakeLockMode = settings.wakeLockMode,
                        isHourlyChimeEnabled = settings.isHourlyChimeEnabled,
                        oledProtectionEnabled = settings.isOledProtectionEnabled,
                        brightnessOverride = settings.brightnessOverride
                    )
                }
            }
        }
    }

    private fun observeTime() {
        viewModelScope.launch {
            settingsRepository.settingsFlow
                .map { it.is24Hour }
                .distinctUntilChanged()
                .flatMapLatest { is24Hour -> timeSource.timeFlow(is24Hour) }
                .collect { time -> _uiState.update { it.copy(time = time) } }
        }
    }

    private fun observeSeconds() {
        viewModelScope.launch {
            // Combine all conditions that allow ticking:
            // 1. User enabled seconds
            // 2. App is in foreground (ProcessLifecycle)
            // 3. Screen is interactive (Screen On)
            kotlinx.coroutines.flow.combine(
                uiState.map { it.showSeconds }.distinctUntilChanged(),
                appLifecycleMonitor.isForeground,
                appLifecycleMonitor.isScreenInteractive,
                settingsRepository.settingsFlow.map { it.is24Hour }.distinctUntilChanged()
            ) { showSeconds, isForeground, isInteractive, is24Hour ->
                Pair(showSeconds && isForeground && isInteractive, is24Hour)
            }
            .distinctUntilChanged()
            .flatMapLatest { (allowTick, is24Hour) ->
                if (allowTick) {
                    // When allowing tick, immediately emit current time to avoid stale state,
                    // then switch to the periodic ticker.
                    kotlinx.coroutines.flow.merge(
                        kotlinx.coroutines.flow.flowOf(timeSource.getCurrentTime(is24Hour)),
                        timeSource.secondsFlow(is24Hour)
                    )
                } else {
                    // Strictly cancel the upstream ticker when not allowed
                    emptyFlow()
                }
            }
            .collect { time ->
                _uiState.update { it.copy(time = time) }
            }
        }
    }

    private fun restoreInteractionState() {
        val savedInteracting = savedStateHandle.get<Boolean>(KEY_IS_INTERACTING) ?: false
        _uiState.update { it.copy(isInteracting = savedInteracting) }
    }

    private fun restoreBulbState() {
        val isOn = savedStateHandle.get<Boolean>(KEY_BULB_IS_ON) ?: false
        val endElapsed = savedStateHandle.get<Long?>(KEY_BULB_END_ELAPSED_REALTIME_MS)

        if (!isOn) {
            setBulbStateOff()
            return
        }

        // If endElapsed == null => long-on mode
        if (endElapsed == null) {
            _uiState.update { it.copy(bulb = BulbState.ON(endElapsedRealtimeMs = null), bulbCountdownSeconds = 0) }
            return
        }

        val remainingMs = endElapsed - elapsedTimeSource.elapsedRealtimeMs()
        if (remainingMs <= 0L) {
            setBulbStateOff()
            return
        }

        _uiState.update {
            it.copy(
                bulb = BulbState.ON(endElapsedRealtimeMs = endElapsed),
                bulbCountdownSeconds = computeCountdownSeconds(endElapsed)
            )
        }
        startBulbCountdown(endElapsed)
    }

    fun onThemeToggle() {
        toggleThemeUseCase.toggle()
        haptics.performToggle()
        sound.playToggleSound()
    }

    fun onSecondsToggle() {
        updateShowSecondsUseCase.toggle()
        haptics.performClick()
        sound.playClickSound()
    }

    fun onLightToggle() {
        // This toggles the *bulb* UI (the glow button), not the ambient LightEffectManager.
        val current = _uiState.value.bulb
        when (current) {
            is BulbState.OFF -> setBulbStateOn()
            is BulbState.ON -> setBulbStateOff()
        }
        haptics.performClick()
        sound.playClickSound()
    }

    fun performClickFeedback() {
        haptics.performClick()
    }

    fun performToggleFeedback() {
        haptics.performClick()
    }

    private fun startLightEffectCountdown(duration: kotlin.time.Duration) {
        viewModelScope.launch {
            var remaining = duration
            while (remaining > kotlin.time.Duration.ZERO) {
                delay(1000)
                remaining -= kotlin.time.Duration.parse("1s")
                _uiState.update { current ->
                    when (current.lightEffect) {
                        is LightEffectState.ON -> current.copy(
                            lightEffect = LightEffectState.ON(remaining)
                        )
                        else -> current
                    }
                }
            }
            _uiState.update { it.copy(lightEffect = LightEffectState.OFF) }
        }
    }

    private fun setBulbStateOn() {
        val endElapsed = if (cachedSettings.isTimedBulbOffEnabled) {
            elapsedTimeSource.elapsedRealtimeMs() + BULB_AUTO_OFF_DURATION_MS
        } else {
            null
        }

        savedStateHandle[KEY_BULB_IS_ON] = true
        savedStateHandle[KEY_BULB_END_ELAPSED_REALTIME_MS] = endElapsed

        _uiState.update {
            it.copy(
                bulb = BulbState.ON(endElapsedRealtimeMs = endElapsed),
                bulbCountdownSeconds = if (endElapsed == null) 0 else computeCountdownSeconds(endElapsed)
            )
        }

        if (endElapsed != null) {
            startBulbCountdown(endElapsed)
        } else {
            bulbJob?.cancel()
            bulbJob = null
        }
    }


    private fun setBulbStateOffInternal() {
        bulbJob?.cancel()
        bulbJob = null

        savedStateHandle[KEY_BULB_IS_ON] = false
        savedStateHandle[KEY_BULB_END_ELAPSED_REALTIME_MS] = null

        _uiState.update { it.copy(bulb = BulbState.OFF, bulbCountdownSeconds = 0) }
    }

    private fun setBulbStateOff() {
        setBulbStateOffInternal()
    }

    private fun computeCountdownSeconds(endElapsedRealtimeMs: Long): Int {
        val remainingMs = endElapsedRealtimeMs - elapsedTimeSource.elapsedRealtimeMs()
        if (remainingMs <= 0L) return 0
        // Match the previous CountDownTimer UI: show 15..1 (ceil)
        return ((remainingMs + 999L) / 1000L).toInt()
    }

    private fun startBulbCountdown(endElapsedRealtimeMs: Long) {
        bulbJob?.cancel()
        bulbJob = viewModelScope.launch {
            while (true) {
                val nowMs = elapsedTimeSource.elapsedRealtimeMs()
                val remainingMs = endElapsedRealtimeMs - nowMs
                if (remainingMs <= 0L) {
                    setBulbStateOffInternal()
                    break
                }

                // Keep state in sync so it survives any subsequent rotation.
                savedStateHandle[KEY_BULB_IS_ON] = true
                savedStateHandle[KEY_BULB_END_ELAPSED_REALTIME_MS] = endElapsedRealtimeMs

                val secondsLeft = computeCountdownSeconds(endElapsedRealtimeMs)

                _uiState.update { current ->
                    when (current.bulb) {
                        is BulbState.ON -> current.copy(
                            bulb = BulbState.ON(endElapsedRealtimeMs = endElapsedRealtimeMs),
                            bulbCountdownSeconds = secondsLeft
                        )
                        else -> current
                    }
                }

                val nextSecondBoundary = ((nowMs / 1000) + 1) * 1000
                val delayMs = (nextSecondBoundary - nowMs).coerceAtLeast(0)
                delay(delayMs)
            }
        }
    }

    fun onTimedBulbModeChanged(isTimedEnabled: Boolean) {
        val current = _uiState.value.bulb
        if (current !is BulbState.ON) return

        if (!isTimedEnabled) {
            // Turning timed mode OFF while counting down: stop countdown and become long-on.
            if (current.endElapsedRealtimeMs != null) {
                bulbJob?.cancel()
                bulbJob = null

                savedStateHandle[KEY_BULB_IS_ON] = true
                savedStateHandle[KEY_BULB_END_ELAPSED_REALTIME_MS] = null

                _uiState.update { it.copy(bulb = BulbState.ON(endElapsedRealtimeMs = null), bulbCountdownSeconds = 0) }
            }
            return
        }

        // Turning timed mode ON while currently long-on: start a fresh 15s countdown.
        if (current.endElapsedRealtimeMs == null) {
            val endElapsed = elapsedTimeSource.elapsedRealtimeMs() + BULB_AUTO_OFF_DURATION_MS

            savedStateHandle[KEY_BULB_IS_ON] = true
            savedStateHandle[KEY_BULB_END_ELAPSED_REALTIME_MS] = endElapsed

            _uiState.update { it.copy(bulb = BulbState.ON(endElapsedRealtimeMs = endElapsed)) }
            startBulbCountdown(endElapsed)
        }
    }

    fun onInteractionToggle() {
        isInteracting = !isInteracting
    }

    fun onSettingsOpen() {
        isInteracting = true
        _uiState.update { it.copy(gearRotationTrigger = it.gearRotationTrigger + 1) }
    }

    fun onScaleChange(scale: Float) {
        _uiState.update { it.copy(scale = scale) }
    }

    fun onBrightnessChange(brightness: Float) {
        _uiState.update { it.copy(brightnessOverride = brightness) }
        settingsRepository.setBrightnessOverride(brightness)
    }

    fun startSleepTimer(minutes: Int): Result<Unit> {
        val startSpecResult = startSleepTimerUseCase.execute(minutes)
        if (startSpecResult is Result.Failure) {
            return startSpecResult
        }

        val spec = (startSpecResult as Result.Success).value
        stopSleepTimer()
        _sleepTimerState.value = SleepTimerState(
            isActive = true,
            remainingSeconds = spec.durationSeconds,
            originalDurationMinutes = spec.originalDurationMinutes
        )

        timerJob = viewModelScope.launch {
            val endTime = spec.endTimeMillis

            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = ((endTime - now) / 1000)

                if (remaining <= 0) {
                    // Timer Finished
                    onTimerFinished()
                    break
                }

                _sleepTimerState.value = _sleepTimerState.value.copy(remainingSeconds = remaining)

                // Calculate next second boundary to avoid accumulating drift
                val nextSecondBoundary = ((now / 1000) + 1) * 1000
                val delayMillis = (nextSecondBoundary - now).coerceAtLeast(0)
                delay(delayMillis)
            }
        }
        return Result.Success(Unit)
    }

    fun stopSleepTimer() {
        timerJob?.cancel()
        timerJob = null
        _sleepTimerState.value = SleepTimerState(isActive = false)
    }

    private fun onTimerFinished() {
        stopSleepTimer()
        viewModelScope.launch {
            _timerFinishedEvent.emit(Unit)
        }
    }

    private val _settingsButtonAnimState = MutableStateFlow(SettingsButtonAnimState())
    val settingsButtonAnimState: StateFlow<SettingsButtonAnimState> = _settingsButtonAnimState.asStateFlow()

    fun updateSettingsButtonAnim(update: (SettingsButtonAnimState) -> SettingsButtonAnimState) {
        _settingsButtonAnimState.value = update(_settingsButtonAnimState.value)
    }

    // Event to trigger Gear Rotation Integration
    private val _gearRotationTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val gearRotationTrigger = _gearRotationTrigger.asSharedFlow()

    fun triggerGearRotation() {
        _gearRotationTrigger.tryEmit(Unit)
    }

    // Simple state to track if user is interacting (showing gear/settings vs seconds)
    // We persist this via SavedStateHandle so it survives rotation
    var isInteracting: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_IS_INTERACTING) ?: false
        set(value) {
            savedStateHandle.set(KEY_IS_INTERACTING, value)
            _uiState.update { it.copy(isInteracting = value) }
        }
    
    val isInteractingFlow: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_IS_INTERACTING, false)

    companion object {
        private const val KEY_IS_INTERACTING = "is_interacting"

        private const val KEY_BULB_IS_ON = "bulb_is_on"
        private const val KEY_BULB_END_ELAPSED_REALTIME_MS = "bulb_end_elapsed_realtime_ms"

        private const val BULB_AUTO_OFF_DURATION_MS = 15_000L
    }
}
