# 技术债务分析草案：OpenFlip Android

## 当前状态概览

### 1. FullscreenClockActivity - "上帝 Activity" 问题

**文件**: `/Users/black_knife_air/Projects/fliqlo_android/app/src/main/java/com/bokehforu/openflip/ui/FullscreenClockActivity.kt`

**当前规模**:
- 571 行代码
- 实现 4 个接口: `OledProtectionController`, `SettingsProvider`, `SleepTimerDialogProvider`, `ThemeTransitionProvider`

**当前职责**:
1. **组合根 (Composition Root)** - 创建所有核心实例
   - AppSettingsManager
   - HapticFeedbackManager
   - FeedbackSoundManager
   - TimeProvider
   - FullscreenClockViewModelFactory

2. **UI 管理**
   - Layout 膨胀和 ViewBinding
   - Compose 设置按钮
   - 手势路由 (GestureRouter)
   - 旋钮交互 (KnobInteractionController)

3. **控制器协调** (~15 个控制器)
   - LightToggleController
   - ThemeToggleController
   - FlipAnimationsController
   - GearAnimationController
   - UIStateController
   - TimeManagementController
   - SettingsCoordinator
   - SystemIntegrationController
   - ShortcutIntentHandler
   - LightEffectManager

4. **生命周期管理**
   - onCreate/onResume/onPause/onDestroy
   - 配置变更处理 (onConfigurationChanged)
   - 方向变更重新膨胀逻辑
   - 控制器清理

5. **系统集成**
   - OLED 防烧屏保护
   - 睡眠/唤醒控制
   - 睡眠定时器对话框
   - 亮度覆盖
   - Intent/快捷方式处理

**手动依赖注入模式**:
```kotlin
// 手动实例化
settingsManager = AppSettingsManager(this)
haptics = HapticFeedbackManager(vibrator)
sound = FeedbackSoundManager(this)
timeProvider = TimeProvider(context, lifecycleScope)

// 手动 ViewModel 工厂
val viewModel: FullscreenClockViewModel by viewModels {
    FullscreenClockViewModelFactory(
        settingsStore = settingsManager,
        timeSource = timeProvider,
        // ... 6 个依赖项
    )
}

// 控制器构造函数注入
LightToggleController(
    stateToggleButton = stateToggleButton,
    stateToggleIcon = stateToggleIcon,
    // ... 多个参数
)
```

---

### 2. 依赖注入现状

**手动工厂数量**: 2 个

1. **FullscreenClockViewModelFactory**
   - 扩展 `AbstractSavedStateViewModelFactory`
   - 传递 6 个依赖项到 ViewModel
   - 文件: 49 行

2. **SettingsViewModel.Factory** (内部类)
   - 实现 `ViewModelProvider.Factory`
   - 传递 AppSettingsManager

**手动构造统计**:
- FullscreenClockActivity 中: ~15 个显式构造
- 整个项目: ~20-30 个手动构造/连接

**Hilt 配置状态**: ❌ 未配置
- build.gradle.kts 中没有 Hilt 插件
- 没有 Hilt 依赖项
- 没有 @HiltAndroidApp Application 类

**潜在收益**:
- 引入 Hilt 可减少 30%+ 的样板代码
- 消除手动工厂
- 简化测试 (依赖注入测试替身)

---

### 3. SharedPreferences 现状

**使用位置**:
1. **AppSettingsManager.kt** - 主设置存储 (13 个键)
2. **WidgetLeakDebugHelper.kt** - Widget 调试计数器
3. **QuitGuard.kt** - 最后退出时间戳

**主设置键** (13 个):
- time_format_mode
- is_dark_theme
- is_haptic_enabled
- is_sound_enabled
- is_show_seconds
- is_show_flaps
- is_swipe_to_dim
- is_scale_enabled
- orientation_mode
- wake_lock_mode
- oled_protection
- is_timed_bulb_off_enabled
- brightness_override

**读写模式**:
```kotlin
// 读取 - 同步，主线程
get() = prefs.getBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME)

// 写入 - apply() 异步
set(value) {
    prefs.edit().putBoolean(KEY, value).apply()
    _settingsFlow.value = _settingsFlow.value.copy(...)
}
```

**性能问题**:
- ✅ 读取: 同步，但 SharedPreferences 有内存缓存，通常很快
- ⚠️ 启动时: loadCurrentSettings() 在构造时读取 13 个键
- ⚠️ resetToDefaults(): 多次单独的 apply() 调用 (应批处理)
- ⚠️ Widget 调试: 高频更新可能触发多次磁盘写入

**DataStore 状态**: ❌ 未使用
- 没有 Preferences DataStore
- 没有 Proto DataStore

**迁移建议**:
- **短期**: 批处理 resetToDefaults() 写入
- **中期**: 迁移到 DataStore 获得异步 IO 和 Flow 支持
- **触发条件**: 用户说"App 启动变慢"时才考虑迁移

---

## 用户决策 ✅

### 最终选择

| 问题 | 决策 |
|------|------|
| **优先级** | 三者同时进行（全面重构） |
| **DI 框架** | 引入 Hilt（官方推荐） |
| **DataStore** | 启动很快，暂不迁移（只做短期优化） |
| **执行策略** | 增量重构（3-5 个独立 PR，风险可控） |

### 重构策略

采用**增量重构**方式，分为 5 个阶段：

**Phase 1: Hilt 基础设施** (低风险)
- 添加 Gradle 配置
- 创建 Application 类
- 提供基础 Module

**Phase 2: 管理器/提供者迁移** (中低风险)
- 迁移 HapticFeedbackManager
- 迁移 FeedbackSoundManager
- 迁移 TimeProvider
- 迁移 AppSettingsManager

**Phase 3: 控制器迁移** (中等风险)
- 迁移 UI 控制器到 Hilt
- 保持 Activity 作为组合根

**Phase 4: ViewModel 迁移** (中等风险)
- 迁移 SettingsViewModel
- 迁移 FullscreenClockViewModel (处理 SavedStateHandle)

**Phase 5: Activity 瘦身** (高风险)
- 提取 CompositionRoot
- 简化 FullscreenClockActivity
- 批处理 SharedPreferences 写入优化

**DataStore 决策**: 暂不迁移，只做短期优化
- 批处理 resetToDefaults() 写入
- 保持现有 SharedPreferences 架构
