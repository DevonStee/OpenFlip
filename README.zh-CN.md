# OpenFlip（Android）

[![Android CI](https://img.shields.io/github/actions/workflow/status/DevonStee/OpenFlip/android.yml?style=for-the-badge&logo=github&label=CI)](https://github.com/DevonStee/OpenFlip/actions/workflows/android.yml)
[![下载 APK](https://img.shields.io/badge/下载-最新_APK-green?style=for-the-badge&logo=android)](https://github.com/DevonStee/OpenFlip/releases/latest/download/app-release.apk)

[English Version](README.md)

OpenFlip 是一个受 Braun 设计风格启发的 Android 翻页时钟，专注于机械翻页质感、视觉精度和可维护架构。

如果你喜欢这个项目，欢迎给我一个 star！⭐

核心特性：
- **自定义渲染引擎**：基于 `Canvas` + 3D 变换实现物理级翻页动画
- **像素级排版**：精确的文字定位与光效补偿，适配各种屏幕密度
- **模块化架构**：Hilt DI + 多模块分层，保证性能与可扩展性

## 项目亮点

- 使用自定义 `Canvas` + 3D 变换实现高拟真翻页动画。
- 极简全屏时钟界面，强调交互触感。
- **零功耗后台驻留**：屏幕关闭或后台运行时完全停止渲染与计算。
- **精致设置面板**：72% 默认高度，支持丝滑手势与全屏过渡。
- 混合 UI 技术栈：
  - Jetpack Compose：设置页与现代 UI 组件。
  - Custom View：高性能时钟渲染引擎。
- OLED 防烧屏保护（细微像素位移）。
- Light Overlay 在位移场景下做了补偿处理，避免边缘漏光。
- 基于 Hilt 的依赖注入，编译期校验依赖图。

## 目录

- [截图展示](#截图展示)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [构建与开发命令](#构建与开发命令)
- [架构说明](#架构说明)
- [依赖注入（Hilt）](#依赖注入hilt)
- [关键运行组件](#关键运行组件)
- [测试与验收](#测试与验收)
- [性能说明](#性能说明)
- [近期维护记录（2026-02）](#近期维护记录2026-02)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 截图展示

以下截图由已连接真机通过 ADB 抓取。

### 全屏时钟

![OpenFlip 全屏时钟](docs/images/openflip-clock-dark.png)

### Light 效果开启

<img src="docs/images/openflip-light-on.png" width="316" alt="OpenFlip Light 效果开启">

### 深色主题 + Light 效果

<img src="docs/images/openflip-light-on-in-dark-theme.png" width="316" alt="OpenFlip 深色主题下的 Light 效果">

### Settings Sheet

<img src="docs/images/openflip-settings-sheet.png" width="316" alt="OpenFlip 设置页">

### 倒计时设置弹窗

<img src="docs/images/openflip-sleep-timer-dialog.png" width="316" alt="OpenFlip 倒计时设置弹窗">

### 秒数显示开启

<img src="docs/images/openflip-show-seconds.png" width="316" alt="OpenFlip 秒数显示开启">

### 深色主题 Light Bulb 点亮（GIF）

<img src="docs/images/openflip-dark-light-toggle.gif" width="316" alt="OpenFlip 深色主题 Light Bulb 点亮">

### 秒数跳动（GIF）

<img src="docs/images/openflip-seconds-ticking.gif" width="316" alt="OpenFlip 秒数跳动">

### Knob 旋钮调时交互

<img src="docs/images/openflip-knob-fast-flip.png" width="316" alt="OpenFlip Knob 旋钮调时交互">

### Vertical Dim 演示（GIF）

<img src="docs/images/openflip-vertical-dim.gif" width="316" alt="OpenFlip Vertical Dim 演示">

### Knob 旋转 2000°（GIF）

<img src="docs/images/openflip-knob-2000deg.gif" width="316" alt="OpenFlip Knob 旋转 2000 度演示">

## 项目结构

```text
OpenFlip/
├── app/                 # 应用入口、DI 模块、资源
├── core/                # 通用契约/模型/工具
├── data/                # Repository 实现与持久化
├── domain/              # UseCase 与 Repository 接口
├── feature-clock/       # 时钟运行时、渲染引擎、Activity/控制器
├── feature-settings/    # 设置页（Compose）、设置状态管理
├── feature-chime/       # 整点报时功能
└── docs/                # 架构文档、ADR、回归基线
```

## 技术栈

- 语言：Kotlin
- UI：
  - Jetpack Compose（设置页与现代组件）
  - Custom Views（时钟渲染与 3D 交互）
- 架构：MVVM + `StateFlow` 响应式更新 + Hilt
- 关键依赖：
  - Hilt `2.55`
  - Material Components / Material 3
  - Cloudy（液态玻璃模糊效果）

## 快速开始

### 环境要求

- JDK 17+
- Android Studio Ladybug 或更新版本
- 本地已配置 Android SDK

### 克隆与运行

```bash
# 请替换为你的实际仓库地址
git clone https://github.com/<your-username>/fliqlo_android.git
cd fliqlo_android
./gradlew installDebug
```

若改动过依赖注入或底层配置，建议先执行一次：

```bash
./gradlew clean build
```

## 构建与开发命令

```bash
# 安装 Debug 包到已连接设备/模拟器
./gradlew installDebug

# 运行单元测试
./gradlew test

# 运行静态检查
./gradlew lint

# 完整编译验证（含 Hilt 依赖图）
./gradlew build
```

## 架构说明

OpenFlip 采用模块化分层与特性化运行时协作模式。

### 模块职责

- `:app`：Android 入口、DI 组合、资源。
- `:core`：跨层共享契约、模型与工具。
- `:domain`：业务用例与仓储接口。
- `:data`：仓储实现与 `AppSettingsManager` 持久化。
- `:feature-clock`：全屏时钟运行时、渲染、交互控制器。
- `:feature-settings`：设置 UI 与状态流。
- `:feature-chime`：整点报时逻辑。

### 状态流向

`UI -> ViewModel -> Repository -> SettingsStore(AppSettingsManager)`

`feature-clock` 侧通过观察 settings flow，将变更以增量方式应用到 UI 与运行时组件。

### 相关文档

- 架构基线：`docs/architecture-baseline.md`
- ADR 决策记录：`docs/adr/`

## 依赖注入（Hilt）

项目已从手动组装迁移到 Hilt，获得更好的编译期安全与生命周期管理。

### DI 结构

```text
app/src/main/java/com/bokehforu/openflip/
├── OpenFlipApplication.kt          # @HiltAndroidApp
└── di/module/
    ├── CoreModule.kt               # Context、CoroutineScope、时间工具
    ├── ManagerModule.kt            # Manager 与接口绑定
    └── ControllerModule.kt         # Activity 作用域绑定
```

### 常见注入对象

- `AppSettingsManager`（`@Singleton`）
- `HapticFeedbackManager`（`@Singleton`）
- `FeedbackSoundManager`（`@Singleton`）
- `TimeProvider`（`@Singleton`）
- `FullscreenClockViewModel`（`@HiltViewModel`）
- `SettingsViewModel`（`@HiltViewModel`）
- `LightToggleController`（AssistedInject + Factory）

## 关键运行组件

### 时钟与渲染

- `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/view/FullscreenFlipClockView.kt`
  - 主时钟自定义 View，负责翻页渲染调度。
- `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/view/renderer/LightOverlayRenderer.kt`
  - Light Overlay 渲染与覆盖补偿。
- `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/manager/DisplayBurnInProtectionManager.kt`
  - OLED 防烧屏位移管理与回调应用。

### Activity 与控制器

- `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/ui/FullscreenClockActivity.kt`
- `feature-clock/src/main/java/com/bokehforu/openflip/feature/clock/ui/controller/LightToggleController.kt`

### 设置与持久化

- `feature-settings/src/main/java/com/bokehforu/openflip/feature/settings/viewmodel/SettingsViewModel.kt`
- `data/src/main/java/com/bokehforu/openflip/data/settings/AppSettingsManager.kt`

## 测试与验收

### 自动化检查

- 单元测试：`app/src/test`
- DI 图编译期验证：`./gradlew build`
- 自定义 Gradle 验证任务（在 `check` 阶段自动执行）：
  - `checkModuleBoundaries` — 校验模块依赖关系是否符合允许的依赖图。
  - `checkSharedPreferencesIsolation` — 确保 `SharedPreferences` 使用限定在 `:data` 模块。
  - `checkResourceOwnershipBoundaries` — 检测 `:app` 和 `:feature-settings` 之间的重复资源路径。
  - `checkResourceSymbolBoundaries` — 检测跨模块的重复资源符号。
  - `checkAppResourceReferenceBoundaries` — 阻止 `:app` 引用 `:feature-settings` 的资源。
  - `checkNoFeatureSettingsRUsageInApp` — 阻止 `:app` 直接导入 `:feature-settings` 的 R 类。
- 推荐 PR 前本地门禁：

```bash
./gradlew test lint build
```

### 手工验收清单

- 横竖屏下翻页动画是否稳定。
- 设置页切换深浅主题是否平滑。
- OLED 位移开启后时钟视觉是否连续。
- OLED 位移 + Light Overlay 同时开启时是否无边缘缺口。
- 所有按钮是否有触觉反馈。
- 仅 Light 按钮是否触发音效。

## 性能说明

- 时钟渲染路径以稳定 60fps 为目标优化。
- `onDraw` 热路径避免对象分配。
- 文本/主题/噪声等高成本资源尽量缓存。
- Compose 与自定义渲染职责分离，降低回归风险。

## 近期维护记录（2026-02）

- 设置面板交互优化：
  - 默认 72% 高度，支持丝滑上滑全屏。
  - 优化手势处理（拖拽关闭、滚动同步）。
- 功耗优化：
  - 实现零后台活动，屏幕关闭或应用隐藏时停止所有渲染与计算。
- App Shortcuts：
  - 修复深浅模式及设置快捷方式的响应问题。
- 文档：
  - 新增简体中文 README（`README.zh-CN.md`）。
- 替换翻页音效：`app/src/main/res/raw/flip_sound.mp3`。
- 调整音量平衡（翻页更轻、整点报时略提升）。
- 在 `FullscreenFlipClockView` 缓存主题背景色，减少逐帧解析。
- 无障碍增强：
  - 时钟可播报当前时间。
  - Light 按钮可播报开/关并保持可点击。
- 文本渲染优化：
  - 为数字和 AM/PM 的 ink-center 引入 LRU 缓存。
- 渲染优化：
  - `FlipCardRenderer` 中缓存噪声 shader，减少主题切换时 GC。
- 手势安全增强：
  - 亮度变暗仅在单指滚动触发。
  - 双指 pinch 不再触发亮度变暗。
- Light Overlay 渲染调整：
  - 去除 PorterDuff `ADD`，避免 GPU 到 software fallback。
- Bug 修复：
  - 修复倒计时（Sleep Timer）崩溃问题。
  - 修复反色按钮音效缺失。

## 贡献指南

### 分支策略

- 从 `main` 创建特性分支。
- 每个 PR 聚焦单一行为改动或重构目标。

### PR 检查项

- 保持模块边界与依赖方向（见 `docs/architecture-baseline.md`）。
- 为行为改动补充测试，或在 PR 中说明测试缺口。
- 提交前执行：

```bash
./gradlew test lint build installDebug
```

- 涉及架构、行为、设置项变化时同步更新文档。

### 提交建议

- 遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范（`feat:`、`fix:`、`refactor:`、`docs:`、`chore:` 等）。
- 以小步可审查的原子提交为主。
- 提交标题清晰描述行为影响。

## 许可证

本项目代码及设计资源在以下条款下提供：

- **授予许可**：本项目对个人学习、教学研究及非营利性开源社区完全免费。您可以自由地访问、修改并分发其源代码。
- **商用限制**：严禁任何形式的商业利用。商业利用包括但不限于：
  - 将本项目或其衍生版本上架至任何应用商店进行销售或展示。
  - 在本项目中集成广告、内购或其他营利性插件。
  - 将本项目作为收费课程、商业外包项目或付费软件的组成部分。
- **相同方式共享**：任何基于本项目的修改版本或衍生作品，必须继承本协议，且必须保持源代码公开及非商业化限制。

详情请参阅 [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh-hans)。
