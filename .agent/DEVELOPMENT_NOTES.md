# OpenFlip Android - 开发认知与关键记录

## 📋 项目概览

**项目性质**: 非官方时钟 Android 复刻版  
**当前版本**: v0.6.0-beta  
**架构**: MVVM + Hilt DI + UseCase 层  
**模块**: 7 个 Gradle 模块 (`:app`, `:core`, `:data`, `:domain`, `:feature-clock`, `:feature-chime`, `:feature-settings`)

---

## 🎯 核心设计原则

### 1. **视觉精准度 (LOCKED)**

- **光学居中算法**: FlipCard.kt 中的视觉居中逻辑**禁止修改**
- 使用墨水边界 (ink bounds) 而非字体度量 (font metrics)
- 公式: `val inkCenterX = (textBounds.left + textBounds.right) / 2f`
- 原因: 确保不同字符（如 "1" vs "8"）视觉重心一致

### 2. **物理动画模型**

- **刚体运动**: 小时/分钟卡片独立旋转，绕各自几何中心
- **中心相对定位**: 避免旋转时的跳变/漂移
- **边界限制**: 实时计算 Bounding Box，防止超出屏幕

### 3. **接口解耦**

- `OledProtectionController`: OLED 保护控制
- `SettingsProvider`: 设置管理器访问
- 目的: SettingsBottomSheet 可在任意宿主 Activity 中使用

---

## 🔒 已知限制 (KNOWN LIMITATIONS)

### Widget RemoteViews 限制

- **问题**: Solid Widget 中间缝隙有白边（抗锯齿残留）
- **尝试方案**: 叠加 1dp View 遮罩 → **失败**
- **原因**: RemoteViews 对视图层级有严格限制
- **决策**: 保留白边，优先保证功能可用性
- **状态**: 不允许再次尝试修复

> 详见 [Android Widget Development Skill](skills/android-widget-development/SKILL.md)

---

## 📦 数据持久化

### SharedPreferences 存储

- **文件**: `/data/data/com.bokehforu.openflip/shared_prefs/openflip_settings.xml`
- **生命周期**: 独立于 App 进程
- **保留策略**:
  - ✅ App 关闭后保留
  - ✅ 设备重启后保留
  - ❌ 卸载后删除

### 存储的设置 (11 项)

| 设置 | Key | 默认值 |
| ------ | ----- | -------- |
| 时间格式 | `time_format_mode` | 0 (12h) |
| 显示秒针 | `is_show_seconds` | false |
| 显示翻页 | `is_show_flaps` | true |
| 滑动调光 | `is_swipe_to_dim_enabled` | true |
| 缩放手势 | `is_scale_enabled` | false |
| 触觉反馈 | `is_haptic_enabled` | true |
| 声音反馈 | `is_sound_enabled` | false |
| 深色主题 | `is_dark_theme` | true |
| 屏幕方向 | `orientation_mode` | 0 (自动) |
| 唤醒锁定 | `wake_lock_mode` | 2 (系统默认) |
| OLED 保护 | `oled_screen_protection` | false |

---

## 🔧 开发环境

### 构建系统

- **Gradle**: 8.10.2
- **JDK**: JBR 21 (Android Studio 内置)
- **最低 SDK**: API 26 (Android 8.0)
- **目标 SDK**: API 35 (Android 15)

### 调试命令

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.bokehforu.openflip/.feature.clock.ui.FullscreenClockActivity
```

---

## 📊 项目统计

- **Gradle 模块**: 7 个
- **Widget 类型**: 5 个 (Classic, Glass, Solid, Split, White)
- **UseCases**: 14 个
- **DreamService**: ScreensaverClockService (已实现)
- **App Shortcuts**: 已实现 (Dark/Light/Settings)

---

## 🎨 主题系统架构 (2026-01-28 更新)

### 关键问题：主题切换的竞态条件

**问题现象**：从黑色主题切换到白色后，约1秒后大部分 UI 变回黑色，只有 light bulb 和 options 按钮保持白色。

**根本原因**：主题有两条更新路径在竞争：

1. **命令式路径**（快）：
   ```
   isDarkTheme = x → listener.onThemeChanged() → settingsCoordinator → 立即应用主题
   ```

2. **响应式路径**（慢，有延迟）：
   ```
   isDarkTheme = x → settingsFlow 更新 → ViewModel 收到 → _uiState 更新 → renderState() 被调用
   ```

每秒的时间更新会触发 `renderState()`，而此时 `state.theme` 可能还是旧值（因为 Flow 传播有延迟），导致 `setDarkTheme()` 把主题改回去。

### 解决方案

**决策**：使用命令式路径作为唯一的主题应用入口。

**修改的文件**：
1. `FullscreenClockActivity.kt` - `renderState()` 中移除：
   - `setDarkTheme(state.theme == ThemeMode.DARK)`
   - `themeApplier.applyTheme(state.theme == ThemeMode.DARK)`

2. `ThemeToggleController.kt` - 重组 `requestThemeChange()`：
   - 将 `isDarkTheme = isDark` 移到 `onApplyTheme` 回调内部
   - 避免双重触发 `onThemeChanged`

### 设计原则

- **主题切换**：只通过 `SettingsCoordinator.onThemeChanged()` 应用
- **触发机制**：`AppSettingsManager.isDarkTheme` setter 触发 listener
- **响应式 Flow**：只用于 Compose UI（Settings 底部菜单），不用于主时钟界面

### XML 主题属性限制

Android 的 `?attr/` 在 XML inflate 时一次性解析，运行时切换主题不会自动更新。

**解决方案**：
- `Theme.OpenFlip` 默认值设为 dark（app 默认是暗色）
- 切换到 light 时通过代码覆盖

---

## 🛡️ OLED 保护白边问题 (2026-01-28 修复)

**问题**：开启 OLED 保护后，clockView 偏移时露出白色细线。

**原因**：`WindowConfigurator.applyBackgroundColor()` 未在启动时调用。

**修复**：在 `FullscreenClockActivity.onCreate()` 中添加：
```kotlin
windowConfigurator.applyBackgroundColor(settingsManager.isDarkTheme)
```

---

## 🚀 下一步建议

### 短期

- [ ] 添加更多 TextView ID 以完善主题染色
- [ ] 实现性能优化（缓存 textBounds）
- [ ] 添加 UI 自动化测试

### 中期

- [ ] 国际化支持（中文、日文等）
- [ ] 更多 Widget 样式
- [ ] 自定义字体支持

### 长期

- [ ] Jetpack Compose 重构
- [ ] Material You 动态颜色
- [ ] Wear OS 支持

---

**最后更新**: 2026-02-13  
**维护者**: black_knife_air
