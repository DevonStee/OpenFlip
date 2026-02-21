# Project Overview: OpenFlip (Android)

OpenFlip is a Braun-inspired minimalist flip clock application for Android, focusing on aesthetic purity and realistic mechanical animations. It is a tribute project inspired by the Fliqlo design.

> For detailed AI assistant skills, guides, and architectural documentation, see [`.agent/`](.agent/README.md).

## Tech Stack
- **Language**: Kotlin
- **UI Frameworks**: 
  - **Jetpack Compose**: Settings interface and modern UI components.
  - **Custom Views (XML/Canvas)**: High-performance flip clock engine and interactive 3D elements (Knob).
- **Architecture**: MVVM with reactive StateFlow updates + Hilt Dependency Injection.
- **Dependencies**: 
  - `Cloudy`: Liquid glass blur effects.
  - `Material Components`: Standard M3 design tokens.
  - `Hilt 2.55`: Google's compile-time DI framework for Android

## Setup & Development
1. **JDK**: Version 17+ recommended.
2. **Android Studio**: Ladybug or newer.
3. **Gradle**: Use included wrapper (`./gradlew`).
4. **Hilt Code Generation**: After modifying DI modules, run `./gradlew clean build` to regenerate Hilt components.

## Key Commands
- **Install Debug**: `./gradlew installDebug`
- **Run Tests**: `./gradlew test`
- **Lint**: `./gradlew lint`

## Testing Instructions
- **Unit Tests**: Located in `app/src/test`. Cover ViewModel logic and state transitions.
- **Hilt Testing**: Use `RobolectricTestRunner` for ViewModel tests requiring Android Context
- **DI Verification**: Run `./gradlew build` to validate dependency graph at compile time
- **Manual Verification**:
  - Verify flip animations in various orientations.
  - Test theme transitions (Dark/Light) in Settings.
  - Verify OLED protection shifting (subtle pixel movement).
  - Test Light effect with OLED protection enabled (no edge gaps).
  - Verify haptic feedback on all buttons (only Light button plays sound).

## Known Technical Details

### OLED Protection + Light Overlay
The light overlay uses canvas translation compensation to prevent edge gaps when OLED burn-in protection shifts the clock view. Key files:
- `FullscreenFlipClockView.onDraw()`: Applies `-translationX/Y` before drawing overlay
- `DisplayBurnInProtectionManager`: Accepts `shiftApplier` callback for custom shift handling
- `LightOverlayRenderer`: Uses 20% radius buffer for complete coverage

## Dependency Injection Architecture (Hilt)

OpenFlip migrated from manual dependency construction to **Hilt** (Dagger 2) for compile-time dependency injection. This refactoring reduced ~30% of boilerplate code and improved testability.

### DI Structure

```
di/
├── module/
│   ├── CoreModule.kt          # ApplicationContext, CoroutineScope, ElapsedTimeSource
│   ├── ManagerModule.kt       # Vibrator, interface bindings
│   └── ControllerModule.kt    # Activity-scoped bindings
OpenFlipApplication.kt         # @HiltAndroidApp entry point
```

### Injected Components

| Component | Scope | Notes |
| :--- | :--- | :--- |
| `AppSettingsManager` | `@Singleton` | Settings persistence |
| `HapticFeedbackManager` | `@Singleton` | Haptic feedback effects |
| `FeedbackSoundManager` | `@Singleton` | Sound effects |
| `TimeProvider` | `@Singleton` | Time source with Flow |
| `FullscreenClockViewModel` | `@HiltViewModel` | Auto SavedStateHandle injection |
| `SettingsViewModel` | `@HiltViewModel` | Simple settings ViewModel |
| `LightToggleController` | Activity | AssistedInject with Factory |

### Key Patterns

**Activity Injection**:
```kotlin
@AndroidEntryPoint
class FullscreenClockActivity : AppCompatActivity() {
    @Inject lateinit var appSettingsManager: AppSettingsManager
    @Inject lateinit var haptics: HapticsProvider
}
```

**ViewModel Injection**:
```kotlin
@HiltViewModel
class FullscreenClockViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    @ApplicationContext appContext: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel()
```

**AssistedInject for Views**:
```kotlin
class LightToggleController @AssistedInject constructor(
    private val settingsStore: SettingsStore,
    @Assisted private val button: StateToggleGlowView
) {
    @AssistedFactory
    interface Factory { fun create(button: StateToggleGlowView): LightToggleController }
}
```

### Migration Impact

- **Before**: Manual construction in Activity.onCreate() - 571 lines
- **After**: Field injection with Hilt - ~400 lines
- **Deleted**: FullscreenClockViewModelFactory.kt (48 lines)
- **Performance**: SharedPreferences batch write optimization (12 writes → 1 write)

### Benefits

- **Compile-Time Safety**: DI graph validated at build time, catching circular dependencies early
- **Reduced Boilerplate**: No manual factory classes or service locators
- **Improved Testability**: Easy to inject mock dependencies in tests
- **Lifecycle Management**: Hilt handles scope lifecycle automatically (@Singleton, @HiltViewModel, etc.)
- **SavedStateHandle Integration**: ViewModels automatically receive SavedStateHandle without factory boilerplate

### Recent Maintenance (2026-02)
- Replaced flip sound asset (`app/src/main/res/raw/flip_sound.mp3`) and rebalanced volumes (flip softer, chime slightly louder).
- Cached theme background color in `FullscreenFlipClockView` to avoid per-frame resolve.
- Accessibility: clock announces current time; light button announces on/off and is clickable.
- Text rendering optimizations: LRU cache for ink center (0-9/AM/PM).
- Noise shader caching in `FlipCardRenderer` to reduce GC on theme changes.
- Gesture safety: brightness dim only on single-finger scroll; pinch (multi-finger) no longer triggers dim.
- Light overlay: removed PorterDuff ADD to avoid GPU→software fallback.
