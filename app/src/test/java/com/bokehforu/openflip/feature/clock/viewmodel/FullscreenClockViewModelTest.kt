package com.bokehforu.openflip.feature.clock.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.domain.usecase.StartSleepTimerUseCase
import com.bokehforu.openflip.domain.usecase.StartSleepTimerError
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import com.bokehforu.openflip.test.fakes.FakeHapticsProvider
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import com.bokehforu.openflip.test.fakes.FakeSoundProvider
import com.bokehforu.openflip.test.fakes.FakeTimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FullscreenClockViewModelTest {

    private lateinit var settingsStore: FakeSettingsStore
    private lateinit var settingsRepository: SettingsRepositoryImpl
    private lateinit var timeSource: FakeTimeSource
    private lateinit var haptics: FakeHapticsProvider
    private lateinit var sound: FakeSoundProvider
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: FullscreenClockViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        settingsStore = FakeSettingsStore()
        settingsRepository = SettingsRepositoryImpl(settingsStore)
        timeSource = FakeTimeSource()
        haptics = FakeHapticsProvider()
        sound = FakeSoundProvider()
        savedStateHandle = SavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FullscreenClockViewModel {
        return FullscreenClockViewModel(
            settingsRepository = settingsRepository,
            timeSource = timeSource,
            elapsedTimeSource = com.bokehforu.openflip.core.controller.interfaces.ElapsedTimeSource { testDispatcher.scheduler.currentTime },
            haptics = haptics,
            sound = sound,
            toggleThemeUseCase = ToggleThemeUseCase(settingsRepository),
            updateShowSecondsUseCase = UpdateShowSecondsUseCase(settingsRepository),
            startSleepTimerUseCase = StartSleepTimerUseCase(),
            savedStateHandle = savedStateHandle,
            appLifecycleMonitor = AppLifecycleMonitor(ApplicationProvider.getApplicationContext())
        )
    }

    @Test
    fun `ViewModel initializes with default state`() {
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, state.theme)
        assertFalse(state.showSeconds)
        assertTrue(state.showFlaps)
    }

    @Test
    fun `onThemeToggle updates settings and triggers feedback`() {
        viewModel = createViewModel()
        val initialTheme = settingsStore.isDarkTheme
        
        viewModel.onThemeToggle()
        
        assertEquals(!initialTheme, settingsStore.isDarkTheme)
        assertTrue(haptics.performToggleCalled)
        assertTrue(sound.playToggleSoundCalled)
    }

    @Test
    fun `onSecondsToggle updates settings and triggers feedback`() {
        viewModel = createViewModel()
        
        viewModel.onSecondsToggle()
        
        assertTrue(settingsStore.showSeconds)
        assertTrue(haptics.performClickCalled)
        assertTrue(sound.playClickSoundCalled)
    }

    @Test
    fun `onLightToggle turns bulb on (timed mode) and persists end time`() = runTest {
        settingsStore.isTimedBulbOffEnabled = true
        viewModel = createViewModel()

        viewModel.onLightToggle()
        runCurrent()

        assertTrue(haptics.performClickCalled)
        assertTrue(sound.playClickSoundCalled)

        val bulb = viewModel.uiState.value.bulb
        assertTrue("Expected BulbState.ON, got: $bulb", bulb is BulbState.ON)
        bulb as BulbState.ON
        assertTrue("Expected endElapsedRealtimeMs != null", bulb.endElapsedRealtimeMs != null)
    }

    @Test
    fun `turning off timed mode while bulb counting down clears end time and countdown immediately`() = runTest {
        settingsStore.isTimedBulbOffEnabled = true
        viewModel = createViewModel()

        viewModel.onLightToggle()
        runCurrent()

        // Let it tick down a bit so we get a non-initial, non-zero value.
        advanceTimeBy(3_000)
        runCurrent()

        val beforeCountdown = viewModel.uiState.value.bulbCountdownSeconds
        assertTrue("Expected countdown > 0 before mode change, got: $beforeCountdown", beforeCountdown > 0)

        val beforeBulb = viewModel.uiState.value.bulb
        assertTrue("Expected BulbState.ON before mode change, got: $beforeBulb", beforeBulb is BulbState.ON)
        beforeBulb as BulbState.ON
        assertTrue("Expected endElapsedRealtimeMs != null before mode change", beforeBulb.endElapsedRealtimeMs != null)

        viewModel.onTimedBulbModeChanged(isTimedEnabled = false)
        runCurrent()

        val afterBulb = viewModel.uiState.value.bulb
        assertTrue("Expected BulbState.ON after mode change, got: $afterBulb", afterBulb is BulbState.ON)
        afterBulb as BulbState.ON
        assertTrue("Expected endElapsedRealtimeMs == null after mode change", afterBulb.endElapsedRealtimeMs == null)

        // Countdown number should disappear immediately in long-on mode
        assertEquals(0, viewModel.uiState.value.bulbCountdownSeconds)
    }

    @Test
    fun `bulb countdown seconds is pushed each second in timed mode`() = runTest {
        settingsStore.isTimedBulbOffEnabled = true
        viewModel = createViewModel()

        viewModel.onLightToggle()
        runCurrent()

        // Immediately after turning on, it should show 15
        assertEquals(15, viewModel.uiState.value.bulbCountdownSeconds)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(14, viewModel.uiState.value.bulbCountdownSeconds)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(13, viewModel.uiState.value.bulbCountdownSeconds)

        // Jump to the end
        advanceTimeBy(13_001)
        runCurrent()

        assertTrue(viewModel.uiState.value.bulb is BulbState.OFF)
        assertEquals(0, viewModel.uiState.value.bulbCountdownSeconds)
    }

    @Test
    fun `turning on timed mode while bulb is long-on starts a new 15s countdown`() = runTest {
        // Start in long-on mode
        settingsStore.isTimedBulbOffEnabled = false
        viewModel = createViewModel()

        viewModel.onLightToggle()
        runCurrent()

        val longOn = viewModel.uiState.value.bulb
        assertTrue("Expected BulbState.ON(long-on), got: $longOn", longOn is BulbState.ON)
        longOn as BulbState.ON
        assertTrue("Expected endElapsedRealtimeMs == null for long-on", longOn.endElapsedRealtimeMs == null)

        // User enables timed mode while bulb is already on
        viewModel.onTimedBulbModeChanged(isTimedEnabled = true)
        runCurrent()

        val timed = viewModel.uiState.value.bulb
        assertTrue("Expected BulbState.ON(timed), got: $timed", timed is BulbState.ON)
        timed as BulbState.ON
        assertTrue("Expected endElapsedRealtimeMs != null for timed", timed.endElapsedRealtimeMs != null)

        // It should auto-off after 15 seconds from the moment timed mode was enabled
        advanceTimeBy(15_001)
        runCurrent()

        assertTrue(viewModel.uiState.value.bulb is BulbState.OFF)
    }

    @Test
    fun `onInteractionToggle changes interaction state`() {
        viewModel = createViewModel()
        
        assertFalse(viewModel.uiState.value.isInteracting)
        
        viewModel.onInteractionToggle()
        assertTrue(viewModel.uiState.value.isInteracting)
        
        viewModel.onInteractionToggle()
        assertFalse(viewModel.uiState.value.isInteracting)
    }

    @Test
    fun `onSettingsOpen sets interaction state`() {
        viewModel = createViewModel()
        
        val initialTrigger = viewModel.uiState.value.gearRotationTrigger
        
        viewModel.onSettingsOpen()
        
        assertTrue(viewModel.uiState.value.isInteracting)
        assertEquals(initialTrigger + 1, viewModel.uiState.value.gearRotationTrigger)
    }

    @Test
    fun `onScaleChange updates scale in state`() {
        viewModel = createViewModel()
        
        viewModel.onScaleChange(1.5f)
        
        assertEquals(1.5f, viewModel.uiState.value.scale, 0.01f)
    }

    @Test
    fun `onBrightnessChange updates brightness in state and settings`() {
        viewModel = createViewModel()
        
        viewModel.onBrightnessChange(0.7f)
        
        assertEquals(0.7f, viewModel.uiState.value.brightnessOverride, 0.01f)
        assertEquals(0.7f, settingsStore.brightnessOverride, 0.01f)
    }

    @Test
    fun `startSleepTimer activates timer with correct duration`() {
        viewModel = createViewModel()
        
        val result = viewModel.startSleepTimer(5)
        
        assertTrue(result is Result.Success)
        val state = viewModel.sleepTimerState.value
        assertTrue(state.isActive)
        assertEquals(300L, state.remainingSeconds)
        assertEquals(5, state.originalDurationMinutes)
    }

    @Test
    fun `startSleepTimer with invalid duration keeps previous timer state`() {
        viewModel = createViewModel()
        viewModel.startSleepTimer(5)
        val previousState = viewModel.sleepTimerState.value

        val result = viewModel.startSleepTimer(0)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is StartSleepTimerError.InvalidDuration)
        assertEquals(previousState, viewModel.sleepTimerState.value)
    }

    @Test
    fun `stopSleepTimer deactivates timer`() {
        viewModel = createViewModel()
        
        viewModel.startSleepTimer(1)
        assertTrue(viewModel.sleepTimerState.value.isActive)
        
        viewModel.stopSleepTimer()
        assertFalse(viewModel.sleepTimerState.value.isActive)
    }

    @Test
    fun `isInteracting persists in SavedStateHandle`() {
        viewModel = createViewModel()
        
        viewModel.isInteracting = true
        
        val newViewModel = createViewModel()
        
        assertTrue(newViewModel.isInteracting)
        assertTrue(newViewModel.uiState.value.isInteracting)
    }

    @Test
    fun `multiple interaction methods work correctly`() {
        viewModel = createViewModel()
        val initialTheme = settingsStore.isDarkTheme
        
        viewModel.onThemeToggle()
        assertEquals(!initialTheme, settingsStore.isDarkTheme)
        
        viewModel.onSecondsToggle()
        assertTrue(settingsStore.showSeconds)
        
        viewModel.onInteractionToggle()
        assertTrue(viewModel.uiState.value.isInteracting)
    }
}
