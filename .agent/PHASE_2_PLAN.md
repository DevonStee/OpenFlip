# Phase 2: ViewModel Refactor - Detailed Implementation Plan

## Executive Summary

**Goal**: Transform FullscreenClockActivity from a state-managing coordinator into a thin UI layer that observes ViewModel state.

**Duration Estimate**: 8-12 hours (2-3 work sessions)

**Risk Level**: HIGH - Touches core Activity logic, animations, and timing

**Success Criteria**:
- ✅ All UI state flows through single `ClockUiState` stream
- ✅ Activity < 250 lines (from current 378)
- ✅ ViewModel holds all state logic
- ✅ Zero visual behavior changes
- ✅ All tests pass
- ✅ Configuration changes preserve state correctly

---

## Current State Assessment

### FullscreenClockActivity (378 lines)

**State Held**:
```kotlin
// 17 lateinit dependencies created in Activity
private lateinit var binding: ActivityMainBinding
private lateinit var settingsManager: AppSettingsManager
private lateinit var haptics: HapticsProvider
private lateinit var sound: SoundProvider
private lateinit var windowConfigurator: WindowConfigurator
private lateinit var themeApplier: ThemeApplier
private lateinit var gearAnimationController: GearAnimationController
private lateinit var flipAnimationsController: FlipAnimationsController
private lateinit var systemIntegrationController: SystemIntegrationController
private lateinit var knobInteractionController: KnobInteractionController
private lateinit var lightToggleController: LightToggleController
private lateinit var themeToggleController: ThemeToggleController
private lateinit var timeManagementController: TimeManagementController
private lateinit var shortcutIntentHandler: ShortcutIntentHandler
private lateinit var gestureRouter: GestureRouter
private lateinit var settingsCoordinator: SettingsCoordinator
private lateinit var uiStateController: UIStateController
private lateinit var lightEffectManager: LightEffectManager
```

**Responsibilities**:
1. Creates and initializes 17 controllers/collaborators
2. Manages lifecycle (onCreate, onResume, onDestroy)
3. Handles touch events (gestures)
4. Implements 4 callback interfaces
5. Observes `viewModel.isInteractingFlow` (partial state observation)
6. Directly mutates `viewModel.isInteracting`

**Critical Concerns**:
- Complex initialization order (lines 84-203)
- `setupControllers()` method creates deep dependency graph
- Controllers have circular dependencies (e.g., timeManagementController ↔ flipAnimationsController)
- Direct View manipulation instead of state-driven rendering

### FullscreenClockViewModel (126 lines)

**Current Scope** (minimal):
```kotlin
// Sleep Timer State
val sleepTimerState: StateFlow<SleepTimerState>
fun startSleepTimer(minutes: Int)
fun stopSleepTimer()

// Settings Button Animation
val settingsButtonAnimState: StateFlow<SettingsButtonAnimState>
fun updateSettingsButtonAnim(...)

// Gear Rotation Event
val gearRotationTrigger: SharedFlow<Unit>
fun triggerGearRotation()

// Interaction State
var isInteracting: Boolean  // SavedStateHandle-backed
val isInteractingFlow: StateFlow<Boolean>
```

**Missing** (should be here):
- Time state (current time, show seconds)
- Settings state (theme, flaps, haptic, sound, etc.)
- UI visibility state (buttons, hints)
- Scale/brightness state
- OLED protection state
- Light effect state
- Time travel state

---

## Architecture Design

### Target ViewModel Structure

```kotlin
class FullscreenClockViewModel(
    private val settingsStore: SettingsStore,
    private val timeSource: TimeSource,
    private val haptics: HapticsProvider,
    private val sound: SoundProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // === SINGLE UI STATE ===
    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()
    
    // === SIDE EFFECTS / EVENTS ===
    private val _events = MutableSharedFlow<ClockUiEvent>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<ClockUiEvent> = _events.asSharedFlow()
    
    init {
        observeSettings()
        observeTime()
        restoreStateFromProcessDeath(savedStateHandle)
    }
    
    // === STATE OBSERVERS ===
    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        theme = if (settings.isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
                        showSeconds = settings.showSeconds,
                        showFlaps = settings.showFlaps,
                        hapticEnabled = settings.isHapticEnabled,
                        soundEnabled = settings.isSoundEnabled,
                        // ... map all settings
                    )
                }
            }
        }
    }
    
    private fun observeTime() {
        viewModelScope.launch {
            timeSource.timeFlow(settingsStore.is24Hour).collect { time ->
                _uiState.update { it.copy(time = time) }
            }
        }
    }
    
    // === USER INTERACTIONS ===
    fun onThemeToggle() {
        settingsStore.isDarkTheme = !settingsStore.isDarkTheme
        // State automatically updates via observeSettings()
        haptics.performToggle()
        sound.playToggleSound()
        _events.tryEmit(ClockUiEvent.ThemeChanged)
    }
    
    fun onLightToggle() {
        // Toggle light effect logic
        _uiState.update { current ->
            current.copy(
                lightEffect = when (current.lightEffect) {
                    is LightEffectState.OFF -> LightEffectState.ON(Duration.seconds(10))
                    is LightEffectState.ON -> LightEffectState.OFF
                }
            )
        }
        haptics.performClick()
        sound.playClickSound()
    }
    
    fun onInteractionChange(interacting: Boolean) {
        _uiState.update { it.copy(isInteracting = interacting) }
    }
    
    // ... other user actions
}
```

### Target Activity Structure

```kotlin
class FullscreenClockActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // ViewModelFactory provides dependencies
    private val viewModel: FullscreenClockViewModel by viewModels {
        FullscreenClockViewModelFactory(
            settingsStore = settingsManager,
            timeSource = timeProvider,
            haptics = haptics,
            sound = sound
        )
    }
    
    // UI Collaborators (ONLY for rendering, no state management)
    private lateinit var windowConfigurator: WindowConfigurator
    private lateinit var themeApplier: ThemeApplier
    private lateinit var colorTransitionController: ColorTransitionController
    private lateinit var gestureRouter: GestureRouter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Create managers (these provide data, not state)
        initializeManagers()
        
        // Create UI collaborators (rendering helpers)
        initializeUICollaborators()
        
        // Setup event forwarding (Activity → ViewModel)
        setupEventForwarding()
        
        // Observe state (ViewModel → Activity)
        observeState()
        
        // Observe side effects
        observeEvents()
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }
    
    private fun renderState(state: ClockUiState) {
        // Pure rendering - no logic, just apply state to views
        binding.flipClockView.apply {
            updateTime(state.time)
            showSeconds = state.showSeconds
            showFlaps = state.showFlaps
            setDarkTheme(state.theme == ThemeMode.DARK)
        }
        
        // Apply theme changes
        if (state.theme == ThemeMode.DARK) {
            themeApplier.applyDarkTheme()
        } else {
            themeApplier.applyLightTheme()
        }
        
        // Update button visibility based on interaction state
        binding.settingsButton.alpha = if (state.isInteracting) 1f else 0f
        
        // ... all other rendering
    }
    
    private fun setupEventForwarding() {
        binding.themeToggle.setOnClickListener {
            viewModel.onThemeToggle()
        }
        
        binding.stateToggleButton.setOnClickListener {
            viewModel.onLightToggle()
        }
        
        // ... all other events
    }
}
```

---

## Implementation Strategy

### Phase 2.1: Add Dependency Injection Infrastructure

**Why First**: ViewModel needs dependencies injected via Factory

**Steps**:
1. Create `ViewModelFactory` for FullscreenClockViewModel
2. Update Activity to use Factory pattern
3. Verify existing functionality still works

**Files to Create**:
- `viewmodel/FullscreenClockViewModelFactory.kt`

**Files to Modify**:
- `ui/FullscreenClockActivity.kt` (add factory, pass dependencies)

**Verification**:
- App still launches
- Existing ViewModel features (sleep timer, interaction) still work

**Time Estimate**: 30 minutes

---

### Phase 2.2: Expand ClockUiState

**Goal**: Add all missing state fields to ClockUiState

**Current ClockUiState** (from Phase 1):
```kotlin
data class ClockUiState(
    val time: Time = Time(0, 0, 0, true),
    val theme: ThemeMode = ThemeMode.DARK,
    val showSeconds: Boolean = false,
    val showFlaps: Boolean = true,
    val isInteracting: Boolean = false,
    val isTimeTraveling: Boolean = false,
    val virtualTimeOffset: Duration = Duration.ZERO,
    val lightEffect: LightEffectState = LightEffectState.OFF,
    val sleepTimerRemaining: Duration? = null,
    val scale: Float = 1.0f,
    val oledProtectionEnabled: Boolean = false,
    val zenMode: Boolean = false,
    val swipeToDimEnabled: Boolean = false,
    val isScaleEnabled: Boolean = false,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val orientationMode: Int = 0,
    val wakeLockMode: Int = 2
)
```

**Add to ClockUiState**:
```kotlin
data class ClockUiState(
    // ... existing fields ...
    
    // Button visibility states
    val showSettingsButton: Boolean = true,
    val showThemeButton: Boolean = true,
    val showLightButton: Boolean = true,
    val showKnobButton: Boolean = true,
    val showSwipeHint: Boolean = false,
    
    // Animation states
    val settingsButtonAnim: SettingsButtonAnimState = SettingsButtonAnimState(),
    val gearRotationTrigger: Int = 0,  // Increment to trigger rotation
    
    // Sleep timer
    val sleepTimerState: SleepTimerState = SleepTimerState(),
    
    // Brightness override
    val brightnessOverride: Float = -1f,  // -1 means system default
)
```

**Files to Modify**:
- `viewmodel/ClockUiState.kt`

**Verification**:
- Compilation succeeds
- No runtime errors

**Time Estimate**: 15 minutes

---

### Phase 2.3: Move State Observers to ViewModel

**Goal**: ViewModel observes `settingsFlow` and `timeFlow`, updates `_uiState`

**Implementation**:
```kotlin
class FullscreenClockViewModel(
    private val settingsStore: SettingsStore,
    private val timeSource: TimeSource,
    // ...
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()
    
    init {
        observeSettings()
        observeTime()
        observeSeconds()
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        theme = if (settings.isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
                        showSeconds = settings.showSeconds,
                        showFlaps = settings.showFlaps,
                        swipeToDimEnabled = settings.isSwipeToDimEnabled,
                        isScaleEnabled = settings.isScaleEnabled,
                        hapticEnabled = settings.isHapticEnabled,
                        soundEnabled = settings.isSoundEnabled,
                        orientationMode = settings.orientationMode,
                        wakeLockMode = settings.wakeLockMode,
                        oledProtectionEnabled = settings.isOledProtectionEnabled,
                        brightnessOverride = settings.brightnessOverride
                    )
                }
            }
        }
    }
    
    private fun observeTime() {
        viewModelScope.launch {
            timeSource.timeFlow(settingsStore.is24Hour).collect { time ->
                _uiState.update { it.copy(time = time) }
            }
        }
    }
    
    private fun observeSeconds() {
        viewModelScope.launch {
            uiState
                .map { it.showSeconds }
                .distinctUntilChanged()
                .flatMapLatest { showSeconds ->
                    if (showSeconds) {
                        timeSource.secondsFlow(settingsStore.is24Hour)
                    } else {
                        emptyFlow()
                    }
                }
                .collect { time ->
                    _uiState.update { it.copy(time = time) }
                }
        }
    }
}
```

**Files to Modify**:
- `viewmodel/FullscreenClockViewModel.kt`

**Verification**:
- Time updates every minute
- Seconds updates when enabled
- Settings changes reflect in UI state

**Time Estimate**: 1 hour

---

### Phase 2.4: Add User Interaction Handlers to ViewModel

**Goal**: Move all user interaction logic from Activity/Controllers to ViewModel

**Implementation**:
```kotlin
class FullscreenClockViewModel(...) : ViewModel() {
    
    // === USER INTERACTIONS ===
    
    fun onThemeToggle() {
        settingsStore.isDarkTheme = !settingsStore.isDarkTheme
        haptics.performToggle()
        sound.playToggleSound()
    }
    
    fun onSecondsToggle() {
        settingsStore.showSeconds = !settingsStore.showSeconds
        haptics.performClick()
        sound.playClickSound()
    }
    
    fun onLightToggle() {
        val currentState = _uiState.value.lightEffect
        val newState = when (currentState) {
            is LightEffectState.OFF -> {
                // Start countdown timer
                startLightEffectCountdown(Duration.seconds(10))
                LightEffectState.ON(Duration.seconds(10))
            }
            is LightEffectState.ON -> LightEffectState.OFF
        }
        _uiState.update { it.copy(lightEffect = newState) }
        haptics.performClick()
        sound.playClickSound()
    }
    
    private fun startLightEffectCountdown(duration: Duration) {
        viewModelScope.launch {
            var remaining = duration
            while (remaining > Duration.ZERO) {
                delay(1.seconds)
                remaining -= 1.seconds
                _uiState.update { current ->
                    when (current.lightEffect) {
                        is LightEffectState.ON -> current.copy(
                            lightEffect = LightEffectState.ON(remaining)
                        )
                        else -> current
                    }
                }
            }
            // Turn off when timer expires
            _uiState.update { it.copy(lightEffect = LightEffectState.OFF) }
        }
    }
    
    fun onInteractionToggle() {
        _uiState.update { it.copy(isInteracting = !it.isInteracting) }
    }
    
    fun onSettingsOpen() {
        _uiState.update { it.copy(isInteracting = true) }
        // Trigger gear rotation animation
        _uiState.update { it.copy(gearRotationTrigger = it.gearRotationTrigger + 1) }
    }
    
    fun onSettingsClose() {
        // Keep isInteracting = true after settings close
        // (matches current behavior)
    }
    
    fun onScaleChange(scale: Float) {
        _uiState.update { it.copy(scale = scale) }
    }
    
    fun onBrightnessChange(brightness: Float) {
        _uiState.update { it.copy(brightnessOverride = brightness) }
        settingsStore.brightnessOverride = brightness
    }
    
    // ... more interaction handlers
}
```

**Files to Modify**:
- `viewmodel/FullscreenClockViewModel.kt`

**Verification**:
- All user interactions work
- Haptics/sounds play correctly
- State updates as expected

**Time Estimate**: 2 hours

---

### Phase 2.5: Refactor Activity to Observe State

**Goal**: Transform Activity into thin rendering layer

**Before (current)**:
```kotlin
// Activity directly manipulates views
fun toggleTheme() {
    settingsManager.isDarkTheme = !settingsManager.isDarkTheme
    themeApplier.applyTheme(settingsManager.isDarkTheme)
    binding.flipClockView.invalidate()
}
```

**After (target)**:
```kotlin
// Activity observes state and renders
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
            renderState(state)
        }
    }
}

private fun renderState(state: ClockUiState) {
    // Pure rendering - no logic
    binding.flipClockView.apply {
        updateTime(state.time)
        showSeconds = state.showSeconds
        showFlaps = state.showFlaps
        setDarkTheme(state.theme == ThemeMode.DARK)
        scaleX = state.scale
        scaleY = state.scale
    }
    
    // Apply theme
    when (state.theme) {
        ThemeMode.DARK -> themeApplier.applyDarkTheme()
        ThemeMode.LIGHT -> themeApplier.applyLightTheme()
    }
    
    // Button visibility
    with(binding) {
        settingsButton.alpha = if (state.showSettingsButton) 1f else 0f
        themeToggle.alpha = if (state.showThemeButton) 1f else 0f
        stateToggle.alpha = if (state.showLightButton) 1f else 0f
        knobButton.alpha = if (state.showKnobButton) 1f else 0f
        swipeHint.isVisible = state.showSwipeHint
    }
    
    // Light effect
    binding.stateToggle.isActivated = state.lightEffect is LightEffectState.ON
    
    // Sleep timer
    if (state.sleepTimerState.isActive) {
        // Update timer UI
    }
    
    // ... all other state rendering
}
```

**Implementation Steps**:

1. **Add `renderState()` method** that maps ClockUiState → View updates
2. **Setup state observation** in onCreate with repeatOnLifecycle
3. **Remove direct state manipulation** from Activity
4. **Forward all events to ViewModel** (button clicks → viewModel.onXxx())
5. **Keep only rendering collaborators** (ThemeApplier, ColorTransitionController, GestureRouter)
6. **Remove state-managing controllers** (TimeManagementController, UIStateController, etc.)

**Files to Modify**:
- `ui/FullscreenClockActivity.kt` (major refactor)

**Critical Concerns**:
- Animation timing must match exactly (flip animations on time change)
- Gear rotation timing (on hour change, on settings open)
- Light effect countdown must be smooth
- OLED protection must activate/deactivate correctly

**Verification Checklist**:
- [ ] App launches without crashes
- [ ] Time updates every minute
- [ ] Seconds display works when enabled
- [ ] Theme toggle animates smoothly
- [ ] Light effect toggles and counts down
- [ ] Settings button shows gear icon when interacting
- [ ] Knob interaction works for time travel
- [ ] Rotation preserves state (isInteracting, scale, brightness)
- [ ] Sleep timer works correctly
- [ ] OLED protection activates after idle timeout

**Time Estimate**: 4-5 hours

---

### Phase 2.6: Add ViewModel Tests

**Goal**: Comprehensive unit tests for ViewModel business logic

**Test Cases**:
```kotlin
class FullscreenClockViewModelTest {
    
    @Test
    fun `uiState emits initial state`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        
        assertEquals(ThemeMode.DARK, state.theme)
        assertFalse(state.showSeconds)
        assertTrue(state.showFlaps)
    }
    
    @Test
    fun `observes settings changes and updates state`() = runTest {
        val settingsStore = FakeSettingsStore()
        val viewModel = createViewModel(settingsStore = settingsStore)
        
        settingsStore.isDarkTheme = false
        
        advanceUntilIdle()
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.theme)
    }
    
    @Test
    fun `onThemeToggle changes theme and plays feedback`() = runTest {
        val settingsStore = FakeSettingsStore()
        val haptics = FakeHapticsProvider()
        val sound = FakeSoundProvider()
        val viewModel = createViewModel(
            settingsStore = settingsStore,
            haptics = haptics,
            sound = sound
        )
        
        viewModel.onThemeToggle()
        advanceUntilIdle()
        
        assertFalse(settingsStore.isDarkTheme)
        assertTrue(haptics.performToggleCalled)
        assertTrue(sound.playToggleSoundCalled)
    }
    
    @Test
    fun `light effect countdown reduces remaining time every second`() = runTest {
        val viewModel = createViewModel()
        
        viewModel.onLightToggle()
        
        val emissions = mutableListOf<LightEffectState>()
        backgroundScope.launch {
            viewModel.uiState
                .map { it.lightEffect }
                .take(3)
                .toList(emissions)
        }
        
        advanceTimeBy(2000)
        
        assertEquals(3, emissions.size)
        assertEquals(LightEffectState.ON(Duration.seconds(10)), emissions[0])
        assertEquals(LightEffectState.ON(Duration.seconds(9)), emissions[1])
        assertEquals(LightEffectState.ON(Duration.seconds(8)), emissions[2])
    }
    
    @Test
    fun `sleep timer counts down and emits finish event`() = runTest {
        val viewModel = createViewModel()
        
        val events = mutableListOf<Unit>()
        backgroundScope.launch {
            viewModel.timerFinishedEvent.toList(events)
        }
        
        viewModel.startSleepTimer(minutes = 1)
        advanceTimeBy(60_000)
        
        assertEquals(1, events.size)
        assertFalse(viewModel.sleepTimerState.value.isActive)
    }
    
    @Test
    fun `onInteractionToggle changes interaction state`() = runTest {
        val viewModel = createViewModel()
        
        assertFalse(viewModel.uiState.value.isInteracting)
        
        viewModel.onInteractionToggle()
        assertTrue(viewModel.uiState.value.isInteracting)
        
        viewModel.onInteractionToggle()
        assertFalse(viewModel.uiState.value.isInteracting)
    }
    
    // ... more test cases
}
```

**Test Fakes to Create**:
```kotlin
// test/fakes/FakeSettingsStore.kt
class FakeSettingsStore : SettingsStore {
    private val _settingsFlow = MutableStateFlow(Settings())
    override val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()
    
    override var isDarkTheme: Boolean
        get() = _settingsFlow.value.isDarkTheme
        set(value) {
            _settingsFlow.value = _settingsFlow.value.copy(isDarkTheme = value)
        }
    // ... implement all properties
}

// test/fakes/FakeTimeSource.kt
class FakeTimeSource : TimeSource {
    private val _timeFlow = MutableStateFlow(Time(12, 0, 0, false))
    
    override fun getCurrentTime(is24Hour: Boolean) = _timeFlow.value
    override fun timeFlow(is24Hour: Boolean) = _timeFlow.asStateFlow()
    override fun secondsFlow(is24Hour: Boolean) = flow {
        while (true) {
            emit(_timeFlow.value)
            delay(1000)
        }
    }
    
    fun setTime(hour: Int, minute: Int, second: Int) {
        _timeFlow.value = Time(hour, minute, second, false)
    }
}

// test/fakes/FakeHapticsProvider.kt
class FakeHapticsProvider : HapticsProvider {
    var performToggleCalled = false
    var performClickCalled = false
    
    override fun setHapticEnabled(enabled: Boolean) {}
    override fun performClick() { performClickCalled = true }
    override fun performLongPress() {}
    override fun performToggle() { performToggleCalled = true }
    override fun performScale() {}
}

// test/fakes/FakeSoundProvider.kt
class FakeSoundProvider : SoundProvider {
    var playToggleSoundCalled = false
    var playClickSoundCalled = false
    
    override fun setSoundEnabled(enabled: Boolean) {}
    override fun playFlipSound() {}
    override fun playClickSound() { playClickSoundCalled = true }
    override fun playToggleSound() { playToggleSoundCalled = true }
}
```

**Files to Create**:
- `app/src/test/java/com/bokehforu/openflip/viewmodel/FullscreenClockViewModelTest.kt`
- `app/src/test/java/com/bokehforu/openflip/test/fakes/FakeSettingsStore.kt`
- `app/src/test/java/com/bokehforu/openflip/test/fakes/FakeTimeSource.kt`
- `app/src/test/java/com/bokehforu/openflip/test/fakes/FakeHapticsProvider.kt`
- `app/src/test/java/com/bokehforu/openflip/test/fakes/FakeSoundProvider.kt`

**Verification**:
- [ ] All tests pass
- [ ] Test coverage > 80% for ViewModel

**Time Estimate**: 2 hours

---

## Risk Mitigation Plan

### High-Risk Areas

| Risk | Mitigation | Rollback Plan |
|------|-----------|---------------|
| **Animation timing changes** | Test flip animations frame-by-frame | Keep old TimeManagementController logic as reference |
| **State synchronization bugs** | Comprehensive tests for state observers | Maintain dual systems during migration |
| **Configuration change issues** | Test rotation 20+ times during implementation | Use SavedStateHandle for critical state |
| **Performance regression** | Profile before/after, monitor GC | Ensure Flow operators are efficient (distinctUntilChanged, etc.) |
| **Circular dependencies** | Dependency graph analysis | ViewModel should only depend on interfaces |

### Testing Protocol

**After Each Sub-Phase**:
1. `./gradlew compileDebugKotlin` - Must succeed
2. `./gradlew testDebugUnitTest` - All tests pass
3. `./gradlew lintDebug` - Zero new violations
4. Manual device test - 5-minute smoke test

**Before Final Commit**:
1. Full test suite pass
2. Device testing checklist (rotation, animations, timers, all features)
3. Performance profiling (no GC spikes, 60fps maintained)
4. Code review (check for anti-patterns)

### Rollback Triggers

Abort Phase 2 and revert if:
- Unable to fix animation timing after 2 hours of debugging
- Configuration changes cause data loss
- Performance degrades by >10%
- Test coverage drops below 70%

---

## Timeline

| Sub-Phase | Estimated Time | Cumulative |
|-----------|---------------|------------|
| 2.1 Add DI Infrastructure | 30 min | 30 min |
| 2.2 Expand ClockUiState | 15 min | 45 min |
| 2.3 Move State Observers | 1 hour | 1h 45m |
| 2.4 Add Interaction Handlers | 2 hours | 3h 45m |
| 2.5 Refactor Activity | 4-5 hours | 8-9h |
| 2.6 Add ViewModel Tests | 2 hours | 10-11h |
| **Testing & Debugging** | 1-2 hours | **11-13h** |

**Realistic Total**: 12-15 hours (account for unexpected issues)

**Recommended Approach**: Split into 3 work sessions of 4-5 hours each

---

## Success Metrics

### Code Metrics

- [ ] FullscreenClockActivity: **< 250 lines** (from 378)
- [ ] FullscreenClockViewModel: **~300 lines** (from 126)
- [ ] Test coverage: **> 80%** for ViewModel
- [ ] Zero new lint violations

### Functional Metrics

- [ ] All features work identically to before
- [ ] Configuration changes preserve state
- [ ] No visual behavior changes
- [ ] No performance regression (60fps maintained)
- [ ] No memory leaks (LeakCanary clean)

### Architecture Metrics

- [ ] Single UI state stream (`ClockUiState`)
- [ ] Activity is thin rendering layer
- [ ] ViewModel is testable (no Android imports except lifecycle)
- [ ] Clear separation: ViewModel (state) ↔ Activity (rendering)

---

## Post-Implementation

### Documentation Updates

After Phase 2 completion, update:
1. **ARCHITECTURE.md** - New ViewModel-first pattern
2. **AGENTS.md** - ViewModel testing patterns
3. **ARCHITECTURE_REFACTOR_PLAN.md** - Mark Phase 2 complete

### Next Steps

Phase 2 completion unlocks:
- **Phase 3** (if needed): Controller purity enforcement
- **Phase 4** (optional): Dependency injection framework (Hilt/Koin)
- **Phase 5** (optional): Compose migration for UI

---

## Conclusion

Phase 2 is **high-risk, high-reward**. It touches the core Activity logic and timing-sensitive animations. However, the architectural benefits are significant:

✅ **Testability**: ViewModel can be unit tested without Android framework
✅ **Maintainability**: Clear separation of concerns
✅ **Reliability**: Configuration changes handled properly
✅ **Scalability**: Easy to add new features

**Recommendation**: Proceed carefully, test frequently, and be prepared to rollback if critical issues arise. The existing Phase 1 foundation (reactive infrastructure, interfaces) makes this refactor feasible and safer.
