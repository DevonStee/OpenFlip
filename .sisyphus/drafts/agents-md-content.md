# AGENTS.md 更新内容草案

## 需要插入到 Tech Stack 部分

在 Architecture 行更新为：
```
- **Architecture**: MVVM with reactive StateFlow updates + Hilt Dependency Injection.
```

在 Dependency 部分添加 Hilt：
```markdown
- **Dependency Injection**: 
  - `Hilt 2.55`: Google's compile-time DI framework for Android
```

## 需要插入到 Known Technical Details 之后的新章节

```markdown
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

### Testing with Hilt

Unit tests use Robolectric for Android dependencies:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FullscreenClockViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
}
```
```

## 需要更新的 Testing Instructions

在现有内容后添加：
```markdown
- **Hilt Testing**: Use `RobolectricTestRunner` for ViewModel tests requiring Android Context
- **DI Verification**: Run `./gradlew build` to validate dependency graph at compile time
```

## 需要更新的 Setup & Development

添加：
```markdown
4. **Hilt Code Generation**: After modifying DI modules, run `./gradlew clean build` to regenerate Hilt components
```
