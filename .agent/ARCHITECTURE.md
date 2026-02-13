# OpenFlip Android - Technical Architecture

> **Version**: 1.5-beta | **Target SDK**: 35 | **Min SDK**: 26

---

## 1. Package Structure (current)

```text
com.bokehforu.openflip/
├── ui/                      # UI layer: Activity, window, controllers, dialogs, helpers, Compose settings
│   ├── FullscreenClockActivity.kt, WindowConfigurator.kt
│   ├── controller/ FlipAnimationsController.kt, GearAnimationController.kt, KnobInteractionController.kt
│   │               LightToggleController.kt, ShortcutIntentHandler.kt, SleepWakeController.kt, ThemeToggleController.kt
│   ├── dialog/ SleepTimerDialogManager.kt
│   ├── effect/ ConvexLensEffect.kt, GlassMagnificationEffect.kt
│   ├── helper/ SystemBarStyleHelper.kt, GestureRouter.kt, WaterfallAnimationHelper.kt
│   ├── settings/ SettingsMenuBottomSheet.kt, SettingsMenuSections.kt, SettingsMenuClickHelper.kt
│   ├── compose/ SettingsButtons.kt, SettingsListItems.kt, SettingsItemComponents.kt, SettingsShapes.kt, GlassEffects.kt
│   ├── theme/ OpenFlipTheme.kt, SettingsThemeHelper.kt, ThemeApplier.kt
│   └── transition/ ColorTransitionController.kt
├── controller/              # Coordination & interaction logic (non-UI rendering)
│   ├── SettingsCoordinator.kt, UIStateController.kt, TimeManagementController.kt, TimeTravelController.kt
│   ├── SystemIntegrationController.kt
│   ├── interfaces/ HapticsProvider.kt, SoundProvider.kt, TimeSource.kt, SettingsStore.kt
│   └── settings/ SettingsNavigationController.kt, SettingsExternalActionController.kt
├── view/                    # Rendering layer: custom Views and renderers (no ui/widget imports)
│   ├── FullscreenFlipClockView.kt, CircularTimerView.kt, InfiniteKnobView.kt, StateToggleGlowView.kt
│   ├── outline providers: CircularOutlineProvider.kt, RoundedRectOutlineProvider.kt
│   ├── card/ FlipCardComponent.kt, FlipCardConfig.kt, FlipCardState.kt, FlipCardGeometry.kt, FlipCardRenderer.kt
│   ├── animation/ FlipAnimationManager.kt
│   ├── renderer/ LightOverlayRenderer.kt
│   └── theme/ FlipClockThemeApplier.kt
├── manager/                 # System/service managers
│   ├── DisplayBurnInProtectionManager.kt, TimeSecondsTicker.kt, TimeProvider.kt, Time.kt
│   ├── FeedbackSoundManager.kt, HapticFeedbackManager.kt, LightEffectManager.kt
├── settings/                # Settings source of truth
│   └── AppSettingsManager.kt
├── viewmodel/               # ViewModels
│   ├── FullscreenClockViewModel.kt, FullscreenClockViewModelFactory.kt, SettingsViewModel.kt
│   └── ClockUiState.kt, ClockUiEvent.kt
├── widget/                  # AppWidgets (RemoteViews)
│   ├── WidgetClockBaseProvider.kt
│   ├── WidgetClock{Classic,Glass,Solid,Split,White}Provider.kt
│   └── WidgetLeakDebugHelper.kt
├── util/                    # Utilities
│   ├── ThemeColorResolver.kt, FontProvider.kt, ViewExtensions.kt, QuitGuard.kt
├── dream/                   # DreamService
│   └── ScreensaverClockService.kt
└── ui/theme/Color.kt        # Compose color tokens
```

---

## 2. Architectural Patterns

### 2.1 Interface-Based Decoupling

```
FullscreenClockActivity
    ├── implements SettingsProvider / ThemeTransitionProvider / SleepTimerDialogProvider
    └── implements OledProtectionController → SettingsMenuBottomSheet applies OLED mode
```

**Benefits**:

- `SettingsMenuBottomSheet` never directly depends on `FullscreenClockActivity`
- Testable with mock implementations
- Reduces coupling between UI components

### 2.2 Controller Split (Enforce Layer Responsibilities)

- **Business/System Controllers** (stay in `controller/`): coordinate settings/time/system integration; **must not hold View/ViewBinding or perform animations**; depend on interfaces exposed by Activities.
- **UI Controllers/Collaborators** (live in `ui/controller` or `ui/helper`): may hold View/ViewBinding and drive animations/gestures/window/state; keep lifecycle-safe checks and zero allocations in render paths.
- **View Layer Purity**: `view/` remains render-only; no imports from `ui/` or `widget/`; no ViewBinding references.
- When adding a new controller, decide upfront: if it touches UI/animations/gestures → place in `ui/controller`; if it coordinates state/business/system → place in `controller/` and keep it view-free.

#### SystemIntegrationController (current usage)

- **Owner**: constructed in `FullscreenClockActivity` and registered as a `LifecycleObserver`.
- **Role**: centralizes system-level behaviors (wake lock mode, burn-in protection, sleep/wake, persistent brightness).
- **Dependencies**: `AppCompatActivity`, `Window`, `SettingsStore`, `FullscreenClockViewModel`, `FullscreenFlipClockView`, `HapticsProvider`.
- **Exceptions**: holds a `FullscreenFlipClockView` reference for OLED shift updates (allowed here, but must not do drawing/animation).
- **Delegation**: Activity implements `SleepTimerDialogProvider`/`OledProtectionController` and forwards calls to this controller.

### 2.3 Single Source of Truth

`AppSettingsManager` is the **only** place where user preferences are stored and read.

```kotlin
class AppSettingsManager(context: Context) : SettingsStore {
    private val prefs = context.applicationContext
        .getSharedPreferences("openflip_settings", Context.MODE_PRIVATE)
    
    override var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_DARK_THEME, value).apply()
            _settingsFlow.value = _settingsFlow.value.copy(isDarkTheme = value)
            _isDarkThemeFlow.value = value
            if (!suppressListeners) listener?.onThemeChanged(value)
        }
}
```

### 2.4 Separation of Concerns (FlipCard)

The flip card now follows clean architecture:

| Component | Responsibility |
|-----------|---------------|
| `FlipCardConfig` | Style constants (no magic numbers) |
| `FlipCardState` | Pure data + computed properties |
| `FlipCardGeometry` | Size/path calculations |
| `FlipCardRenderer` | Drawing only |
| `FlipCardComponent` | Facade for existing API |

### 2.5 Runtime Dependencies (brief)

```mermaid
graph TD
    Activity[FullscreenClockActivity] --> VM[FullscreenClockViewModel]
    Activity --> SettingsVM[SettingsViewModel]
    Activity --> SysCtrl[SystemIntegrationController]
    Activity --> UiCtrl[UI Controllers]
    VM --> SettingsStore[SettingsStore (AppSettingsManager)]
    VM --> TimeSource[TimeSource (TimeProvider)]
    VM --> Haptics[HapticsProvider (HapticFeedbackManager)]
    VM --> Sound[SoundProvider (FeedbackSoundManager)]
    SysCtrl --> BurnIn[DisplayBurnInProtectionManager]
    SysCtrl --> SleepWake[SleepWakeController]
    SysCtrl --> SleepTimer[SleepTimerDialogManager]
```

---

## 3. Rendering Performance

### 3.1 Zero-Allocation onDraw

**Rules**:

- ❌ NEVER create `Paint()`, `Path()`, or `Rect()` in `onDraw()`
- ✅ Pre-allocate all objects at class level
- ✅ Use `path.reset()` for reuse

### 3.2 Path Pre-computation

```kotlin
// In setDimensions() - NOT in draw()
topClipPath.set(superellipsePath)
tempPath.addRect(topRect, Path.Direction.CW)
topClipPath.op(tempPath, Path.Op.INTERSECT)
```

### 3.3 Hardware Layer

```kotlin
setLayerType(LAYER_TYPE_HARDWARE, null)
```

**Why**: Prevents GPU resource reclaim causing black flash during rotation. Clear to `LAYER_TYPE_NONE` after animation (see AGENTS.md rules).

---

## 4. Rotation Anti-Flicker Protocol

### Manifest Configuration

```xml
<activity
    android:configChanges="orientation|screenSize|keyboardHidden|screenLayout"
    android:screenOrientation="fullSensor" />
```

### Theme Configuration

```xml
<!-- values/themes.xml (Light) -->
<item name="android:windowBackground">@color/white</item>

<!-- values-night/themes.xml (Dark) -->
<item name="android:windowBackground">@color/black</item>
```

**⚠️ CRITICAL**: Use hardcoded `@color/` references, NOT `?attr/` dynamic values (aligns with AGENTS.md hard rule).

---

## 5. Widget Architecture

```text
WidgetClockBaseProvider (abstract)
    ├── layoutId: Int (abstract) → Points to layout resource
    └── updateAppWidget() → Called on update/resize

TextClock (system-level time sync)
    ├── format12Hour="h" / format24Hour="HH"
    └── Automatic time updates without AlarmManager
```

**Split Effect**: 120dp TextClock in 59dp container with `clipChildren="true"`.

---

## 6. Dependency Rules

1. **No circular deps**: `view/` MUST NOT import from `ui/` or `widget/`.
2. **Interface boundaries**: Cross-package access via small interfaces (e.g., `SettingsProvider`, `OledProtectionController`, dialog providers) exposed by Activity; controllers consume interfaces, not concrete Activities.
3. **Settings hub**: All prefs access flows through `AppSettingsManager` (via `SettingsStore`); controllers listen via its callbacks/flows.
4. **Widgets isolated**: RemoteViews providers only use widget-safe resources/layouts; no direct dependency on `ui/` classes.
5. **Compose scope**: Compose limited to settings components (`ui/compose`); primary clock remains XML + custom Views.
