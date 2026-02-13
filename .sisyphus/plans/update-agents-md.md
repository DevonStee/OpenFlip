# AGENTS.md 文档更新计划

## 目标
更新项目根目录的 AGENTS.md 文件，添加 Hilt 依赖注入架构的完整文档。

## 需要添加的内容

### 1. Tech Stack 更新
在 Dependency 部分添加 Hilt：
```markdown
- **Dependency Injection**: 
  - `Hilt 2.55`: Compile-time DI framework with `@AndroidEntryPoint`, `@HiltViewModel`, `@AssistedInject`
```

### 2. 新增 "Dependency Injection Architecture" 章节
在 "Known Technical Details" 之后添加：

```markdown
## Dependency Injection Architecture (Hilt)

OpenFlip uses **Hilt** (Dagger 2) for compile-time dependency injection, replacing manual ViewModel factories and Activity-level object construction.

### 2.1 DI Layer Structure

```
di/
├── module/
│   ├── CoreModule.kt          # ApplicationContext, CoroutineScope, ElapsedTimeSource
│   ├── ManagerModule.kt       # Vibrator, interface bindings (HapticsProvider, SoundProvider, etc.)
│   └── ControllerModule.kt    # Activity-scoped bindings (future expansion)
└── CompositionRoot.kt         # (optional) Manual construction for complex graphs
```

### 2.2 Injected Components

| Component | Scope | Injection Method | Notes |
| :--- | :--- | :--- | :--- |
| `AppSettingsManager` | `@Singleton` | Constructor | Settings persistence |
| `HapticFeedbackManager` | `@Singleton` | Constructor | Haptic effects |
| `FeedbackSoundManager` | `@Singleton` | Constructor | Sound effects |
| `TimeProvider` | `@Singleton` | Constructor | Time source |
| `FullscreenClockViewModel` | `@HiltViewModel` | Field (`by viewModels()`) | SavedStateHandle auto-injected |
| `SettingsViewModel` | `@HiltViewModel` | Field (`by viewModels()`) | Simple ViewModel |
| `LightToggleController` | Activity | AssistedInject Factory | Requires View references |

### 2.3 Key DI Patterns

**@AndroidEntryPoint Activities**:
```kotlin
@AndroidEntryPoint
class FullscreenClockActivity : AppCompatActivity() {
    @Inject lateinit var appSettingsManager: AppSettingsManager
    @Inject lateinit var haptics: HapticsProvider
    @Inject lateinit var lightToggleControllerFactory: LightToggleController.Factory
}
```

**@HiltViewModel ViewModels**:
```kotlin
@HiltViewModel
class FullscreenClockViewModel @Inject constructor(
    private var settingsStore: SettingsStore,
    private val timeSource: TimeSource,
    @ApplicationContext appContext: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel()
```

**AssistedInject for View-dependent Controllers**:
```kotlin
class LightToggleController @AssistedInject constructor(
    private val settingsStore: SettingsStore,
    @Assisted private val stateToggleButton: StateToggleGlowView,
    @Assisted private val onToggleRequested: () -> Unit
) {
    @AssistedFactory
    interface Factory {
        fun create(...): LightToggleController
    }
}
```

### 2.4 Migration from Manual DI

**Before (FullscreenClockActivity.onCreate)**:
```kotlin
settingsManager = AppSettingsManager(this)
val vibrator = ...
haptics = HapticFeedbackManager(vibrator)
sound = FeedbackSoundManager(this)
timeProvider = TimeProvider(context, lifecycleScope)
```

**After**:
```kotlin
@Inject lateinit var appSettingsManager: AppSettingsManager
@Inject lateinit var haptics: HapticsProvider
@Inject lateinit var sound: SoundProvider
@Inject lateinit var timeProvider: TimeProvider
```

### 2.5 Benefits

- **Reduced Boilerplate**: ~30% less code in Activity/ViewModel construction
- **Testability**: Easy to mock dependencies in unit tests
- **Compile-time Safety**: DI graph validated at build time
- **Lifecycle Awareness**: Hilt provides Activity/Fragment/ViewModel-scoped dependencies
- **SavedStateHandle**: Automatic injection without manual factory creation
```

### 3. Testing Instructions 更新
添加关于 Hilt 测试的说明：
```markdown
- **Unit Tests with Hilt**: Use `@RunWith(RobolectricTestRunner::class)` for ViewModel tests requiring `ApplicationProvider`
- **Test Fakes**: Update fakes to implement all interface methods (e.g., `FakeHapticsProvider.performSecondsTick()`)
```

### 4. Setup & Development 更新
添加 Hilt 相关命令：
```markdown
- **Clean Build after DI changes**: `./gradlew clean build` (Hilt code generation)
- **Verify DI graph**: Build will fail with clear error messages if bindings are missing
```

## 修改文件
- `/Users/black_knife_air/Projects/fliqlo_android/AGENTS.md`

## 验证步骤
1. 更新后检查 Markdown 格式正确
2. 确认所有链接和代码块格式正确
3. 与 README.md 中的架构描述保持一致
