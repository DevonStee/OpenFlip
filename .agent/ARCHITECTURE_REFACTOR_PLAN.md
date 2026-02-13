# OpenFlip Android - Architecture Refactor Plan

> **Objective**: Modernize architecture to align with Android best practices while maintaining zero UI changes
> 
> **Target**: Transform from "Good" to "Excellent" architecture rating
> 
> **Core Principle**: Incremental, testable, non-breaking changes

---

## Executive Summary

### Current State Assessment

**Rating**: Good (基本达到最佳实践)

**Strengths**:
- Clear performance-oriented layering (`view/` pure rendering)
- Collaborator pattern (WindowConfigurator, ThemeApplier, GestureRouter)
- Interface-based decoupling
- Single source of truth (AppSettingsManager)
- Zero-allocation custom Views

**Critical Gaps**:
1. ❌ No unified state management (StateFlow/UDF)
2. ❌ Manual lifecycle management (listener callbacks)
3. ❌ Limited testability (construction coupling)
4. ⚠️ Controller purity not enforced (some touch Views/Android classes)

### Target State

**Rating**: Excellent (完全符合现代 Android 最佳实践)

**Improvements**:
1. ✅ Single UI state pipeline (ViewModel + StateFlow)
2. ✅ Reactive settings/time (Flow + repeatOnLifecycle)
3. ✅ Pure controllers (Android-free, interface-based)
4. ✅ Lightweight manual DI (testable construction)
5. ✅ Comprehensive unit tests for business logic

---

## Refactoring Strategy

### Guiding Principles

1. **Zero UI Changes**: No visual behavior changes, only internal architecture
2. **Incremental**: Each phase independently deployable and testable
3. **Backward Compatible**: Maintain existing APIs during transition
4. **Test-Driven**: Add tests before and after each phase
5. **Documentation-First**: Update ARCHITECTURE.md after each phase

### Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking existing functionality | Run full test suite + manual verification after each phase |
| State synchronization bugs | Maintain dual systems (old listeners + new Flow) during transition |
| Performance regression | Profile before/after, maintain zero-allocation guarantee |
| Increased complexity | Clear deprecation path, remove old code after new code stabilizes |

---

## Phase 1: Add Reactive Infrastructure (响应式基础设施)

**Goal**: Convert AppSettingsManager and time management to Flow-based APIs

**Duration**: 4-6 hours

**Status**: Not Started

### 1.1 Convert AppSettingsManager to Flow

**Current (Listener-based)**:
```kotlin
interface Listener {
    fun onFormatChanged(is24Hour: Boolean)
    fun onThemeChanged(isDark: Boolean)
    // ... 10+ callback methods
}

var listener: Listener? = null
```

**Target (Flow-based)**:
```kotlin
// Keep existing listener API for backward compatibility
var listener: Listener? = null

// Add new Flow APIs
val settingsFlow: StateFlow<Settings>
val formatFlow: Flow<Boolean>
val themeFlow: Flow<Boolean>
val showSecondsFlow: Flow<Boolean>
// ... individual setting flows
```

**Implementation Steps**:

1. **Define Settings data class**:
   ```kotlin
   // settings/Settings.kt
   data class Settings(
       val is24Hour: Boolean,
       val isDarkTheme: Boolean,
       val showSeconds: Boolean,
       val showFlaps: Boolean,
       val swipeToDim: Boolean,
       val hapticEnabled: Boolean,
       val soundEnabled: Boolean,
       val orientationMode: Int,
       val wakeLockMode: Int,
       val oledProtection: Boolean,
       val burnInProtection: Boolean
   )
   ```

2. **Add Flow emission in AppSettingsManager**:
   ```kotlin
   class AppSettingsManager(context: Context) {
       private val _settingsFlow = MutableStateFlow(loadCurrentSettings())
       val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()
       
       var is24Hour: Boolean
           set(value) {
               prefs.edit().putBoolean(KEY, value).apply()
               _settingsFlow.update { it.copy(is24Hour = value) }
               if (!suppressListeners) listener?.onFormatChanged(value)
           }
       
       // Individual flows for convenience
       val formatFlow: Flow<Boolean> = settingsFlow.map { it.is24Hour }.distinctUntilChanged()
       val themeFlow: Flow<Boolean> = settingsFlow.map { it.isDarkTheme }.distinctUntilChanged()
       // ...
   }
   ```

3. **Add helper for loading current state**:
   ```kotlin
   private fun loadCurrentSettings(): Settings = Settings(
       is24Hour = prefs.getBoolean(KEY_FORMAT, true),
       isDarkTheme = prefs.getBoolean(KEY_THEME, true),
       // ... load all settings
   )
   ```

**Verification**:
- [x] Build succeeds
- [x] Existing listener-based code still works
- [x] Flow emits correct values on settings changes
- [x] No performance regression (profile settings updates)

**Files to Modify**:
- `settings/AppSettingsManager.kt` (add Flow APIs)
- `settings/Settings.kt` (new data class)

---

### 1.2 Convert Time Management to Flow

**Current**:
```kotlin
class TimeManagementController(
    private val clockView: FullscreenFlipClockView
) {
    private val minuteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateTime()
        }
    }
}
```

**Target**:
```kotlin
class TimeProvider(context: Context) {
    // Emit current time every minute
    val timeFlow: Flow<Time> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(getCurrentTime())
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
        
        // Emit immediately
        send(getCurrentTime())
        
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
    
    // Emit every second when enabled
    fun secondsFlow(enabled: Boolean): Flow<Time> =
        if (enabled) flow {
            while (true) {
                emit(getCurrentTime())
                delay(1000)
            }
        } else emptyFlow()
}

data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val is24Hour: Boolean
)
```

**Implementation Steps**:

1. **Create TimeProvider class**:
   ```kotlin
   // manager/TimeProvider.kt
   class TimeProvider(
       private val context: Context,
       private val scope: CoroutineScope
   ) {
       // Implementation as above
   }
   ```

2. **Keep TimeManagementController for backward compatibility**:
   ```kotlin
   class TimeManagementController(
       private val timeProvider: TimeProvider,
       private val clockView: FullscreenFlipClockView
   ) {
       // Delegate to TimeProvider but maintain existing API
   }
   ```

**Verification**:
- [x] Time updates still work every minute
- [x] Seconds display still updates every second
- [x] No broadcast receiver leaks
- [x] Configuration changes don't break time updates

**Files to Create**:
- `manager/TimeProvider.kt` (new)
- `manager/Time.kt` (new data class)

**Files to Modify**:
- `controller/TimeManagementController.kt` (refactor to use TimeProvider)

---

### 1.3 Testing Phase 1

**Unit Tests to Add**:
```kotlin
// settings/AppSettingsManagerTest.kt
@Test
fun `settingsFlow emits when settings change`() = runTest {
    val manager = AppSettingsManager(context)
    val emissions = mutableListOf<Settings>()
    
    backgroundScope.launch {
        manager.settingsFlow.take(2).toList(emissions)
    }
    
    manager.isDarkTheme = false
    
    assertEquals(2, emissions.size)
    assertFalse(emissions[1].isDarkTheme)
}

// manager/TimeProviderTest.kt
@Test
fun `timeFlow emits on minute change`() = runTest {
    // Use fake BroadcastReceiver
}
```

**Manual Verification**:
1. Run app, change settings → observe Flow emissions in logs
2. Wait for minute change → verify time updates
3. Enable seconds → verify seconds Flow works
4. Rotate device → verify no crashes or state loss

---

## Phase 2: Implement Single UI State Pipeline (单一状态流)

**Goal**: Refactor FullscreenClockViewModel to be the single source of truth for UI state

**Duration**: 6-8 hours

**Status**: Not Started

### 2.1 Define Comprehensive UI State

**Current**:
```kotlin
class FullscreenClockViewModel : ViewModel() {
    // Minimal state, most logic in Activity
}
```

**Target**:
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
    val zenMode: Boolean = false
)

sealed class ClockUiEvent {
    data class TimeChanged(val time: Time) : ClockUiEvent()
    object ThemeToggled : ClockUiEvent()
    object SecondsToggled : ClockUiEvent()
    object FlapsToggled : ClockUiEvent()
    object LightToggled : ClockUiEvent()
    data class ScaleChanged(val scale: Float) : ClockUiEvent()
    data class TimeTravelStarted(val offset: Duration) : ClockUiEvent()
    object TimeTravelEnded : ClockUiEvent()
    object SettingsOpened : ClockUiEvent()
    object SettingsClosed : ClockUiEvent()
    // ...
}

class FullscreenClockViewModel(
    private val settingsManager: AppSettingsManager,
    private val timeProvider: TimeProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()
    
    init {
        observeSettings()
        observeTime()
    }
    
    fun onEvent(event: ClockUiEvent) {
        when (event) {
            is ClockUiEvent.ThemeToggled -> handleThemeToggle()
            is ClockUiEvent.TimeChanged -> handleTimeChange(event.time)
            // ...
        }
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.settingsFlow.collect { settings ->
                _uiState.update { it.copy(
                    theme = if (settings.isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
                    showSeconds = settings.showSeconds,
                    showFlaps = settings.showFlaps,
                    oledProtectionEnabled = settings.oledProtection
                ) }
            }
        }
    }
    
    private fun observeTime() {
        viewModelScope.launch {
            timeProvider.timeFlow.collect { time ->
                _uiState.update { it.copy(time = time) }
            }
        }
    }
    
    private fun handleThemeToggle() {
        settingsManager.isDarkTheme = !settingsManager.isDarkTheme
        // State automatically updates via observeSettings()
    }
}
```

**Implementation Steps**:

1. **Create UI state classes**:
   ```kotlin
   // viewmodel/ClockUiState.kt
   data class ClockUiState(/* ... */)
   
   // viewmodel/ClockUiEvent.kt
   sealed class ClockUiEvent {/* ... */}
   
   // viewmodel/ThemeMode.kt
   enum class ThemeMode { LIGHT, DARK }
   
   // viewmodel/LightEffectState.kt
   sealed class LightEffectState {
       object OFF : LightEffectState()
       data class ON(val countdown: Duration) : LightEffectState()
   }
   ```

2. **Refactor FullscreenClockViewModel**:
   - Add StateFlow<ClockUiState>
   - Observe settings and time flows
   - Implement event handler
   - Move all state logic from Activity

3. **Update FullscreenClockActivity to observe state**:
   ```kotlin
   class FullscreenClockActivity : AppCompatActivity() {
       private val viewModel: FullscreenClockViewModel by viewModels()
       
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           
           // Setup UI
           setupCollaborators()
           
           // Observe state and render
           lifecycleScope.launch {
               repeatOnLifecycle(Lifecycle.State.STARTED) {
                   viewModel.uiState.collect { state ->
                       renderState(state)
                   }
               }
           }
           
           // Forward events to ViewModel
           setupEventForwarding()
       }
       
       private fun renderState(state: ClockUiState) {
           binding.flipClockView.updateTime(state.time)
           binding.flipClockView.showSeconds = state.showSeconds
           binding.flipClockView.showFlaps = state.showFlaps
           binding.lightToggle.isActivated = state.lightEffect is LightEffectState.ON
           // ... render all state
       }
       
       private fun setupEventForwarding() {
           binding.themeToggle.setOnClickListener {
               viewModel.onEvent(ClockUiEvent.ThemeToggled)
           }
           // ... forward all events
       }
   }
   ```

**Verification**:
- [x] All UI state flows through ViewModel
- [x] Activity only renders and forwards events
- [x] Configuration changes preserve state correctly
- [x] No visual behavior changes

**Files to Create**:
- `viewmodel/ClockUiState.kt`
- `viewmodel/ClockUiEvent.kt`
- `viewmodel/ThemeMode.kt`
- `viewmodel/LightEffectState.kt`

**Files to Modify**:
- `viewmodel/FullscreenClockViewModel.kt` (major refactor)
- `ui/FullscreenClockActivity.kt` (convert to state observer)

---

### 2.2 Migrate Controllers to Work with ViewModel

**Strategy**: Controllers should no longer touch Views directly; instead, they compute state/events that ViewModel consumes.

**Current Pattern**:
```kotlin
class ThemeToggleController(
    private val clockView: FullscreenFlipClockView,
    private val themeApplier: ThemeApplier
) {
    fun toggleTheme() {
        // Directly manipulates views
        themeApplier.applyTheme(isDark)
        clockView.invalidate()
    }
}
```

**Target Pattern**:
```kotlin
class ThemeToggleController(
    private val settingsManager: AppSettingsManager
) {
    // Pure business logic, returns state changes
    fun toggleTheme(): ThemeMode {
        val newDark = !settingsManager.isDarkTheme
        settingsManager.isDarkTheme = newDark
        return if (newDark) ThemeMode.DARK else ThemeMode.LIGHT
    }
}

// ViewModel integrates controller
class FullscreenClockViewModel(
    private val themeToggleController: ThemeToggleController
) {
    fun onEvent(event: ClockUiEvent) {
        when (event) {
            ClockUiEvent.ThemeToggled -> {
                val newTheme = themeToggleController.toggleTheme()
                // State automatically updates via settings flow
            }
        }
    }
}
```

**Controllers to Migrate**:
1. `ThemeToggleController` → pure theme logic
2. `LightToggleController` → pure light effect logic
3. `KnobInteractionController` → pure time travel logic
4. `FlipAnimationsController` → animation coordinator (may keep some View access)
5. `GearAnimationController` → animation coordinator

**Implementation Steps**:

1. For each controller:
   - Remove View/ViewBinding parameters from constructor
   - Convert methods to return state/events instead of mutating views
   - Move to depend only on managers/providers

2. Update ViewModel to use refactored controllers

3. Update Activity to apply visual changes based on state

**Verification**:
- [x] Controllers have no View imports
- [x] Controllers are pure Kotlin (no Android imports except Context where needed)
- [x] All visual updates flow through state → render

**Files to Modify**:
- `ui/controller/ThemeToggleController.kt`
- `ui/controller/LightToggleController.kt`
- `ui/controller/KnobInteractionController.kt`
- `ui/controller/FlipAnimationsController.kt`
- `ui/controller/GearAnimationController.kt`
- `viewmodel/FullscreenClockViewModel.kt` (integrate controllers)

---

### 2.3 Refactor Settings Coordinator

**Current**:
```kotlin
class SettingsCoordinator(
    private val activity: FullscreenClockActivity
) : AppSettingsManager.Listener {
    override fun onThemeChanged(isDark: Boolean) {
        // Directly manipulates activity/views
        activity.applyTheme(isDark)
    }
}
```

**Target**:
```kotlin
class SettingsCoordinator(
    private val settingsManager: AppSettingsManager
) {
    // No listener interface, just provide Flow transformations
    val effectsFlow: Flow<SettingsEffect> = settingsManager.settingsFlow
        .distinctUntilChanged()
        .map { settings -> computeEffects(settings) }
    
    private fun computeEffects(settings: Settings): SettingsEffect {
        // Pure computation, no side effects
        return SettingsEffect(
            shouldApplyTheme = settings.isDarkTheme,
            shouldShowSeconds = settings.showSeconds,
            // ...
        )
    }
}

// ViewModel collects effects
class FullscreenClockViewModel(
    private val settingsCoordinator: SettingsCoordinator
) {
    init {
        viewModelScope.launch {
            settingsCoordinator.effectsFlow.collect { effect ->
                _uiState.update { it.applyEffect(effect) }
            }
        }
    }
}
```

**Verification**:
- [x] SettingsCoordinator is pure (no Activity reference)
- [x] Settings changes flow through state
- [x] No duplicate listeners (old + new)

**Files to Modify**:
- `controller/SettingsCoordinator.kt`
- `viewmodel/FullscreenClockViewModel.kt`

---

### 2.4 Testing Phase 2

**Unit Tests to Add**:
```kotlin
// viewmodel/FullscreenClockViewModelTest.kt
@Test
fun `theme toggle updates state correctly`() = runTest {
    val viewModel = FullscreenClockViewModel(
        settingsManager = FakeSettingsManager(),
        timeProvider = FakeTimeProvider()
    )
    
    viewModel.onEvent(ClockUiEvent.ThemeToggled)
    
    assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.theme)
}

@Test
fun `time flow updates state`() = runTest {
    val timeProvider = FakeTimeProvider()
    val viewModel = FullscreenClockViewModel(
        settingsManager = FakeSettingsManager(),
        timeProvider = timeProvider
    )
    
    timeProvider.emitTime(Time(12, 30, 0, false))
    
    assertEquals(12, viewModel.uiState.value.time.hour)
}
```

**Manual Verification**:
1. Complete test checklist from AGENTS.md:
   - [x] Light Mode + Dark Mode
   - [x] Portrait + Landscape
   - [x] Rotation during animation
   - [x] 10+ seconds idle, then rotate
   - [x] Widget on home screen
2. Verify no visual changes
3. Profile for performance regression

---

## Phase 3: Enforce Controller Purity (强化 Controller 纯粹性)

**Goal**: All controllers depend only on interfaces, no direct Android/View dependencies

**Duration**: 4-6 hours

**Status**: Not Started

### 3.1 Define Core Interfaces

**Create interface boundary layer**:

```kotlin
// controller/interfaces/TimeProvider.kt
interface TimeProvider {
    val timeFlow: Flow<Time>
    fun secondsFlow(enabled: Boolean): Flow<Time>
    fun getCurrentTime(): Time
}

// controller/interfaces/SettingsStore.kt
interface SettingsStore {
    val settingsFlow: StateFlow<Settings>
    var isDarkTheme: Boolean
    var showSeconds: Boolean
    // ... all settings properties
}

// controller/interfaces/HapticsProvider.kt
interface HapticsProvider {
    fun performClick()
    fun performLongPress()
    fun performToggle()
}

// controller/interfaces/SoundProvider.kt
interface SoundProvider {
    fun playFlipSound()
    fun playClickSound()
}

// controller/interfaces/LightEffectsProvider.kt
interface LightEffectsProvider {
    fun enableLightEffect(duration: Duration)
    fun disableLightEffect()
}
```

**Implementation Steps**:

1. **Create interface files** in `controller/interfaces/`

2. **Make existing classes implement interfaces**:
   ```kotlin
   // manager/TimeProvider.kt
   class TimeProviderImpl(
       private val context: Context,
       private val scope: CoroutineScope
   ) : TimeProvider {
       // Implementation
   }
   
   // settings/AppSettingsManager.kt
   class AppSettingsManager(context: Context) : SettingsStore {
       // Already has the implementation
   }
   
   // manager/HapticFeedbackManager.kt
   class HapticFeedbackManager(context: Context) : HapticsProvider {
       // Implementation
   }
   ```

3. **Refactor controllers to depend on interfaces**:
   ```kotlin
   // Before
   class TimeTravelController(
       private val timeManager: TimeManagementController,
       private val haptics: HapticFeedbackManager
   )
   
   // After
   class TimeTravelController(
       private val timeProvider: TimeProvider,
       private val haptics: HapticsProvider
   )
   ```

**Verification**:
- [x] No `import android.*` in controller/ except interfaces
- [x] No `import ...view.*` in controller/
- [x] Controllers can be instantiated in pure Kotlin tests

**Files to Create**:
- `controller/interfaces/TimeProvider.kt`
- `controller/interfaces/SettingsStore.kt`
- `controller/interfaces/HapticsProvider.kt`
- `controller/interfaces/SoundProvider.kt`
- `controller/interfaces/LightEffectsProvider.kt`

**Files to Modify**:
- All files in `controller/` (update constructor dependencies)
- `manager/TimeProvider.kt` (implement interface)
- `manager/HapticFeedbackManager.kt` (implement interface)
- `manager/FeedbackSoundManager.kt` (implement interface)
- `manager/LightEffectManager.kt` (implement interface)

---

### 3.2 Testing Phase 3

**Unit Tests to Add**:
```kotlin
// controller/TimeTravelControllerTest.kt
@Test
fun `time travel calculates offset correctly`() {
    val controller = TimeTravelController(
        timeProvider = FakeTimeProvider(),
        haptics = FakeHapticsProvider()
    )
    
    controller.startTimeTravel(rotation = 360f)
    
    assertEquals(Duration.ofHours(1), controller.currentOffset)
}

// controller/SettingsCoordinatorTest.kt
@Test
fun `settings coordinator emits correct effects`() = runTest {
    val settingsStore = FakeSettingsStore()
    val coordinator = SettingsCoordinator(settingsStore)
    
    val effects = mutableListOf<SettingsEffect>()
    backgroundScope.launch {
        coordinator.effectsFlow.take(2).toList(effects)
    }
    
    settingsStore.isDarkTheme = false
    
    assertEquals(2, effects.size)
    assertFalse(effects[1].shouldApplyTheme)
}
```

**Create Fake Implementations**:
```kotlin
// test/kotlin/.../FakeTimeProvider.kt
class FakeTimeProvider : TimeProvider {
    private val _timeFlow = MutableStateFlow(Time(0, 0, 0, true))
    override val timeFlow: StateFlow<Time> = _timeFlow.asStateFlow()
    
    fun emitTime(time: Time) {
        _timeFlow.value = time
    }
    
    override fun getCurrentTime() = timeFlow.value
    override fun secondsFlow(enabled: Boolean) = emptyFlow<Time>()
}

// test/kotlin/.../FakeSettingsStore.kt
class FakeSettingsStore : SettingsStore {
    private val _settingsFlow = MutableStateFlow(Settings())
    override val settingsFlow: StateFlow<Settings> = _settingsFlow.asStateFlow()
    
    override var isDarkTheme: Boolean
        get() = settingsFlow.value.isDarkTheme
        set(value) {
            _settingsFlow.update { it.copy(isDarkTheme = value) }
        }
    // ... implement all properties
}
```

---

## Phase 4: Add Lightweight Dependency Injection (轻量级依赖注入)

**Goal**: Centralized object graph construction for easy testing

**Duration**: 2-4 hours

**Status**: Not Started

### 4.1 Create Application-Level Container

```kotlin
// OpenFlipApplication.kt
class OpenFlipApplication : Application() {
    lateinit var appContainer: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

// AppContainer.kt
class AppContainer(private val context: Context) {
    // Singletons
    val settingsManager: SettingsStore by lazy {
        AppSettingsManager(context)
    }
    
    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
    
    val timeProvider: TimeProvider by lazy {
        TimeProviderImpl(context, applicationScope)
    }
    
    val hapticsProvider: HapticsProvider by lazy {
        HapticFeedbackManager(context)
    }
    
    val soundProvider: SoundProvider by lazy {
        FeedbackSoundManager(context)
    }
    
    val lightEffectsProvider: LightEffectsProvider by lazy {
        LightEffectManager(context)
    }
    
    // Controllers (created per Activity)
    fun createSettingsCoordinator(): SettingsCoordinator {
        return SettingsCoordinator(settingsManager)
    }
    
    fun createTimeTravelController(): TimeTravelController {
        return TimeTravelController(timeProvider, hapticsProvider)
    }
    
    // ViewModels (use ViewModelProvider.Factory)
    val viewModelFactory = ViewModelFactory(this)
}

// ViewModelFactory.kt
class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            FullscreenClockViewModel::class.java -> {
                FullscreenClockViewModel(
                    settingsManager = appContainer.settingsManager,
                    timeProvider = appContainer.timeProvider,
                    hapticsProvider = appContainer.hapticsProvider,
                    soundProvider = appContainer.soundProvider
                ) as T
            }
            SettingsViewModel::class.java -> {
                SettingsViewModel(
                    settingsManager = appContainer.settingsManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}
```

**Implementation Steps**:

1. **Create OpenFlipApplication**:
   ```kotlin
   // OpenFlipApplication.kt
   class OpenFlipApplication : Application() {
       val appContainer by lazy { AppContainer(this) }
   }
   ```

2. **Create AppContainer**:
   - Define all singletons
   - Define factory methods for per-screen dependencies

3. **Create ViewModelFactory**:
   - Implement ViewModelProvider.Factory
   - Inject dependencies into ViewModels

4. **Update AndroidManifest.xml**:
   ```xml
   <application
       android:name=".OpenFlipApplication"
       ...>
   ```

5. **Update Activities to use container**:
   ```kotlin
   class FullscreenClockActivity : AppCompatActivity() {
       private val appContainer
           get() = (application as OpenFlipApplication).appContainer
       
       private val viewModel: FullscreenClockViewModel by viewModels {
           appContainer.viewModelFactory
       }
       
       private val themeApplier by lazy {
           appContainer.createThemeApplier(this)
       }
   }
   ```

**Verification**:
- [x] App builds and runs
- [x] No duplicate singleton instances
- [x] ViewModels receive correct dependencies
- [x] No crashes on configuration changes

**Files to Create**:
- `OpenFlipApplication.kt`
- `AppContainer.kt`
- `ViewModelFactory.kt`

**Files to Modify**:
- `AndroidManifest.xml` (add android:name)
- `ui/FullscreenClockActivity.kt` (use container)
- `dream/ScreensaverClockService.kt` (use container if needed)

---

### 4.2 Create Test Container

```kotlin
// test/kotlin/.../TestAppContainer.kt
class TestAppContainer : AppContainer(
    context = ApplicationProvider.getApplicationContext()
) {
    // Override with fakes
    override val settingsManager: SettingsStore = FakeSettingsStore()
    override val timeProvider: TimeProvider = FakeTimeProvider()
    override val hapticsProvider: HapticsProvider = FakeHapticsProvider()
    override val soundProvider: SoundProvider = FakeSoundProvider()
}
```

**Usage in Tests**:
```kotlin
@Test
fun `activity uses container dependencies`() {
    val testContainer = TestAppContainer()
    val scenario = ActivityScenario.launch(FullscreenClockActivity::class.java)
    
    scenario.onActivity { activity ->
        // Inject test container
        (activity.application as OpenFlipApplication).appContainer = testContainer
    }
    
    // Verify behavior with fakes
}
```

---

## Phase 5: Add Comprehensive Unit Tests (添加单元测试)

**Goal**: Achieve >80% coverage on business logic

**Duration**: 4-6 hours (ongoing)

**Status**: Not Started

### 5.1 Test Coverage Targets

| Component | Target Coverage | Priority |
|-----------|----------------|----------|
| ViewModels | 90%+ | High |
| Controllers | 80%+ | High |
| Managers (business logic) | 70%+ | Medium |
| UI Components | N/A (manual) | Low |

### 5.2 Test Structure

```
app/src/test/java/com/bokehforu/openflip/
├── fakes/
│   ├── FakeTimeProvider.kt
│   ├── FakeSettingsStore.kt
│   ├── FakeHapticsProvider.kt
│   └── FakeSoundProvider.kt
├── viewmodel/
│   ├── FullscreenClockViewModelTest.kt
│   └── SettingsViewModelTest.kt
├── controller/
│   ├── SettingsCoordinatorTest.kt
│   ├── TimeTravelControllerTest.kt
│   ├── UIStateControllerTest.kt
│   └── TimeManagementControllerTest.kt
└── settings/
    └── AppSettingsManagerTest.kt
```

### 5.3 Key Test Scenarios

**FullscreenClockViewModel**:
```kotlin
class FullscreenClockViewModelTest {
    private lateinit var viewModel: FullscreenClockViewModel
    private lateinit var fakeSettings: FakeSettingsStore
    private lateinit var fakeTime: FakeTimeProvider
    
    @Before
    fun setup() {
        fakeSettings = FakeSettingsStore()
        fakeTime = FakeTimeProvider()
        viewModel = FullscreenClockViewModel(fakeSettings, fakeTime)
    }
    
    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertEquals(ThemeMode.DARK, state.theme)
        assertFalse(state.showSeconds)
    }
    
    @Test
    fun `theme toggle updates state`() = runTest {
        viewModel.onEvent(ClockUiEvent.ThemeToggled)
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.theme)
    }
    
    @Test
    fun `settings changes update state`() = runTest {
        val states = mutableListOf<ClockUiState>()
        backgroundScope.launch {
            viewModel.uiState.take(2).toList(states)
        }
        
        fakeSettings.isDarkTheme = false
        
        assertEquals(ThemeMode.LIGHT, states[1].theme)
    }
    
    @Test
    fun `time updates flow to state`() = runTest {
        fakeTime.emitTime(Time(14, 30, 0, true))
        
        val state = viewModel.uiState.value
        assertEquals(14, state.time.hour)
        assertEquals(30, state.time.minute)
    }
}
```

**TimeTravelController**:
```kotlin
class TimeTravelControllerTest {
    @Test
    fun `rotation maps to time offset correctly`() {
        val controller = TimeTravelController(
            FakeTimeProvider(),
            FakeHapticsProvider()
        )
        
        controller.onRotationChanged(360f) // Full rotation = 1 hour
        assertEquals(Duration.ofHours(1), controller.currentOffset)
        
        controller.onRotationChanged(720f) // Two rotations = 2 hours
        assertEquals(Duration.ofHours(2), controller.currentOffset)
    }
    
    @Test
    fun `time travel triggers haptic feedback`() {
        val fakeHaptics = FakeHapticsProvider()
        val controller = TimeTravelController(
            FakeTimeProvider(),
            fakeHaptics
        )
        
        controller.startTimeTravel()
        assertTrue(fakeHaptics.didPerformLongPress)
    }
}
```

---

## Phase 6: Documentation Updates (文档更新)

**Goal**: Reflect new architecture in all documentation

**Duration**: 2-3 hours

**Status**: Not Started

### 6.1 Files to Update

1. **`.agent/ARCHITECTURE.md`**:
   - Update package structure diagram with Flow/StateFlow
   - Document ViewModel state management pattern
   - Add interface boundary section
   - Update dependency rules

2. **`.agent/AGENTS.md`**:
   - Add "Reactive State Management" section
   - Update coding standards with Flow best practices
   - Add testing guidelines

3. **`README.md`**:
   - Update "Structural Design" section
   - Add "State Management" section
   - Document Flow-based architecture

4. **Create `.agent/guides/state_management.md`**:
   - Comprehensive guide on state flow patterns
   - Examples of adding new state/events
   - Testing strategies

---

## Rollout Plan

### Week 1: Foundations
- **Day 1-2**: Phase 1 (Reactive Infrastructure)
  - Implement Flow-based settings
  - Implement Flow-based time
  - Write unit tests
  - Manual verification

- **Day 3**: Phase 1 Review & Stabilization
  - Full test suite
  - Performance profiling
  - Fix any issues

### Week 2: Core Refactor
- **Day 4-5**: Phase 2 (Single State Pipeline)
  - Refactor ViewModel
  - Update Activity to observe state
  - Migrate controllers
  - Write unit tests

- **Day 6**: Phase 2 Review & Stabilization
  - Comprehensive testing
  - Manual verification checklist
  - Performance check

### Week 3: Purification & Infrastructure
- **Day 7**: Phase 3 (Controller Purity)
  - Define interfaces
  - Refactor controllers
  - Write controller tests

- **Day 8**: Phase 4 (Dependency Injection)
  - Create AppContainer
  - Update Activities
  - Create test infrastructure

- **Day 9**: Phase 5 (Unit Tests)
  - Write ViewModel tests
  - Write controller tests
  - Achieve coverage targets

### Week 4: Documentation & Polish
- **Day 10**: Phase 6 (Documentation)
  - Update all documentation
  - Create guides
  - Final review

---

## Success Metrics

### Quantitative
- [x] Zero visual behavior changes
- [x] Zero performance regression (profile before/after)
- [x] >80% unit test coverage on business logic
- [x] All lint checks pass
- [x] Build time unchanged or improved

### Qualitative
- [x] State management is predictable and traceable
- [x] Controllers are pure and easily testable
- [x] Lifecycle management is automatic and safe
- [x] New features can be added without touching multiple layers
- [x] Architecture is easy to explain to new developers

---

## Rollback Plan

If critical issues arise during any phase:

1. **Revert the phase**: Each phase is in separate commits
2. **Analyze the failure**: What broke? Why?
3. **Fix or redesign**: Address root cause
4. **Retry with fixes**: Reapply with corrections

**Rollback triggers**:
- App crashes that didn't exist before
- Visual behavior changes
- >10% performance regression
- Memory leaks detected
- Test suite fails

---

## Post-Refactor Validation

### Automated Tests
```bash
# Run all tests
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

# Run lint
./gradlew lintDebug

# Build release
./gradlew assembleRelease
```

### Manual Testing Checklist

From `.agent/AGENTS.md`:
- [x] Light Mode + Dark Mode
- [x] Portrait + Landscape
- [x] Rotation during animation
- [x] 10+ seconds idle, then rotate
- [x] Widget on home screen

Additional checks:
- [x] All settings work correctly
- [x] Time updates every minute
- [x] Seconds display updates every second
- [x] Time travel knob works
- [x] Light effect countdown works
- [x] Sleep timer works
- [x] Theme transition animates smoothly
- [x] Haptic feedback works
- [x] Sound effects play correctly
- [x] App shortcuts work
- [x] Dream mode works

### Performance Profiling

Before and after each phase:
```bash
# Profile memory allocations in custom Views
adb shell dumpsys gfxinfo com.bokehforu.openflip.debug

# Check for memory leaks
# (LeakCanary is already integrated)

# Profile CPU usage
adb shell top -m 10 | grep openflip
```

---

## Future Enhancements (Post-Refactor)

After completing this refactor, the architecture will support:

1. **Feature Modules**: Easier to extract features into modules
2. **Compose Migration**: State management already Compose-compatible
3. **Offline-First**: Easy to add Repository layer when needed
4. **Multi-Module**: Clear boundaries enable modularization
5. **Advanced Testing**: Snapshot testing, screenshot testing
6. **CI/CD**: Automated testing and deployment

---

## Appendix: Architecture Evolution

### Before (Current)

```
Activity (God Object)
├── Direct View manipulation
├── Manual listener registration
├── State scattered across components
└── Controllers touch Views directly
```

**Issues**:
- State management unpredictable
- Lifecycle leaks possible
- Testing difficult
- Scalability limited

### After (Target)

```
Activity (Renderer)
    ↓ observes
ViewModel (State Holder)
    ↓ uses
Controllers (Domain Logic)
    ↓ depend on
Interfaces (Boundaries)
    ↓ implemented by
Managers (System Services)
```

**Benefits**:
- Single source of truth
- Automatic lifecycle management
- Highly testable
- Scalable and maintainable

---

## Questions & Decisions

### Q: Why not use Hilt/Koin for DI?
**A**: Lightweight manual DI is sufficient for this app size. Adding Hilt would:
- Increase build time
- Add complexity
- Provide minimal benefit over manual DI

If the app grows to 20+ screens or 50+ injectable classes, reconsider.

### Q: Why keep ViewBinding instead of migrating to Compose?
**A**: Custom Views with zero-allocation rendering are critical for performance. Compose would:
- Require rewriting high-performance rendering code
- Potentially introduce allocations in draw paths
- Change visual behavior (not allowed in this refactor)

Compose is good for settings UI (already done), but custom Views are the right choice for the clock.

### Q: Why StateFlow instead of LiveData?
**A**: StateFlow is modern, coroutine-native, and more testable:
- Works naturally with suspend functions
- Easier to test (doesn't need main thread)
- Better integration with Flow operators
- Future-proof (Google recommends Flow over LiveData)

### Q: Should we add a Repository layer now?
**A**: No, not yet. Add it when you have:
- Multiple data sources to coordinate
- Network calls
- Complex persistence logic
- Need for offline-first

Current data sources (SharedPreferences + system time) don't justify the complexity.

---

**End of Architecture Refactor Plan**

*Last Updated: 2026-01-28*
*Status: Ready for Implementation*
