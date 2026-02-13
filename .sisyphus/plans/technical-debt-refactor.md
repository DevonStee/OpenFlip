# 工作计划：OpenFlip 技术债务重构

## 目标
解决三个核心技术债务问题：
1. FullscreenClockActivity "上帝 Activity" 问题（571 行，15+ 职责）
2. 手动依赖注入（迁移到 Hilt，减少 30%+ 样板代码）
3. SharedPreferences 短期优化（批处理写入，暂不迁移 DataStore）

## 执行策略
**增量重构** - 5 个阶段，独立 PR，风险可控

---

## Phase 1: Hilt 基础设施搭建 ⭐ 低风险

### 目标
配置 Hilt 依赖，创建 Application 类，建立基础 Module 结构

### 任务清单

- [ ] **1.1 添加 Hilt Gradle 配置**
  - [ ] 修改 `build.gradle.kts` (project level) - 添加 Hilt 插件
  - [ ] 修改 `app/build.gradle.kts` - 添加 Hilt 插件和依赖
  - [ ] 同步 Gradle，验证编译通过
  - **验证**: `./gradlew build` 成功

- [ ] **1.2 创建 Application 类**
  - [ ] 创建 `OpenFlipApplication.kt` 并添加 `@HiltAndroidApp`
  - [ ] 修改 `AndroidManifest.xml` 注册 Application
  - [ ] 验证应用正常启动
  - **验证**: App 启动无崩溃

- [ ] **1.3 创建基础 Hilt Modules**
  - [ ] 创建 `di/module/CoreModule.kt` - 提供 Application Context
  - [ ] 创建 `di/module/ManagerModule.kt` - 框架结构（暂不填充）
  - [ ] 创建 `di/module/ControllerModule.kt` - 框架结构（暂不填充）
  - **验证**: Hilt 编译成功，无 Dagger 错误

### 交付物
- Hilt 完整配置
- Application 类
- Module 框架结构
- 编译通过的代码库

### 风险
- **低** - 纯基础设施，不影响现有功能

---

## Phase 2: 管理器/提供者迁移 ⭐⭐ 中低风险

### 目标
将核心管理器迁移到 Hilt，消除手动构造

### 任务清单

- [ ] **2.1 迁移 AppSettingsManager**
  - [ ] 修改 `AppSettingsManager` 构造函数添加 `@Inject`
  - [ ] 在 `CoreModule` 中提供 `@Singleton` 绑定
  - [ ] 修改 `FullscreenClockActivity` 使用 `@Inject lateinit var`
  - [ ] 删除 Activity 中的手动构造代码
  - **验证**: 设置功能正常，主题切换工作

- [ ] **2.2 迁移 HapticFeedbackManager**
  - [ ] 添加 `@Inject` 构造函数
  - [ ] 在 `ManagerModule` 提供 Vibrator 依赖
  - [ ] 更新 Activity 注入
  - **验证**: 触觉反馈正常工作

- [ ] **2.3 迁移 FeedbackSoundManager**
  - [ ] 添加 `@Inject` 构造函数
  - [ ] 更新 Activity 注入
  - **验证**: 声音反馈正常工作

- [ ] **2.4 迁移 TimeProvider**
  - [ ] 添加 `@Inject` 构造函数
  - [ ] 提供 CoroutineScope 依赖（使用 `@ActivityRetainedScope`）
  - **验证**: 时间更新正常工作

### 交付物
- 4 个核心管理器通过 Hilt 注入
- Activity 中删除对应的手动构造代码
- 功能验证通过

### 风险
- **中低** - 管理器依赖简单，主要是 Context 和系统服务

---

## Phase 3: 控制器迁移 ⭐⭐⭐ 中等风险

### 目标
将 UI 控制器迁移到 Hilt，同时保持 Activity 作为组合根

### 任务清单

- [ ] **3.1 分析控制器依赖关系**
  - [ ] 列出所有控制器及其依赖：
    - LightToggleController (需要 View 引用)
    - ThemeToggleController
    - FlipAnimationsController
    - GearAnimationController
    - UIStateController
    - TimeManagementController
    - SettingsCoordinator
    - SystemIntegrationController
    - ShortcutIntentHandler
    - LightEffectManager
  - [ ] 分类：纯依赖注入 vs 需要 View/Activity 引用

- [ ] **3.2 迁移纯依赖控制器**
  - [ ] 为无 View 依赖的控制器添加 `@Inject`
  - [ ] 创建 Provider 方法处理需要参数的
  - **包括**: ThemeToggleController, SettingsCoordinator, etc.

- [ ] **3.3 处理需要 View 引用的控制器**
  - [ ] 设计 Factory 模式（ AssistedInject 或手动 Factory）
  - [ ] 为 LightToggleController 创建 Factory
  - [ ] 为需要 View 的控制器创建统一 Factory 接口

- [ ] **3.4 更新 Activity 控制器创建**
  - [ ] 使用 Hilt 注入替代手动构造（纯依赖的）
  - [ ] 使用 Factory 创建需要 View 的
  - [ ] 删除旧的手动构造代码

### 交付物
- 控制器通过 Hilt 注入或 Factory 创建
- Activity 控制器创建逻辑简化
- 所有控制器功能正常

### 风险
- **中等** - 控制器依赖复杂，部分需要 View 引用，需要仔细设计

---

## Phase 4: ViewModel 迁移 ⭐⭐⭐ 中等风险

### 目标
迁移 ViewModels 到 Hilt，处理 SavedStateHandle

### 任务清单

- [ ] **4.1 迁移 SettingsViewModel**
  - [ ] 添加 `@HiltViewModel` 注解
  - [ ] 修改构造函数使用 `@Inject`
  - [ ] 删除内部 Factory 类
  - [ ] 更新 `SettingsMenuBottomSheet` 使用 `by viewModels()`
  - **验证**: 设置菜单正常显示和工作

- [ ] **4.2 迁移 FullscreenClockViewModel**
  - [ ] 添加 `@HiltViewModel` 注解
  - [ ] 修改构造函数使用 `@Inject`
  - [ ] 处理 SavedStateHandle（Hilt 自动提供）
  - [ ] 删除 `FullscreenClockViewModelFactory`
  - [ ] 更新 `FullscreenClockActivity` 使用 `by viewModels()`
  - **验证**: 
    - 时钟正常显示
    - 配置变更（旋转）状态保持
    - 主题切换正常

- [ ] **4.3 创建 ViewModel 所需的 Module 绑定**
  - [ ] 确保所有 ViewModel 依赖都有 Hilt 绑定
  - [ ] 为接口（SettingsStore, TimeSource 等）创建 Binds

### 交付物
- 两个 ViewModel 使用 `@HiltViewModel`
- 删除手动 Factory 类
- 配置变更状态保持正常工作

### 风险
- **中等** - SavedStateHandle 处理需要验证，确保状态恢复正常

---

## Phase 5: Activity 瘦身 + SharedPreferences 优化 ⭐⭐⭐⭐ 高风险

### 目标
大幅简化 FullscreenClockActivity，优化 SharedPreferences 写入

### 任务清单

- [ ] **5.1 创建 CompositionRoot**
  - [ ] 创建 `CompositionRoot.kt` 类
  - [ ] 提取 Activity 中所有依赖创建逻辑
  - [ ] 设计清晰的依赖提供接口
  - [ ] 保持 Activity 仅负责 UI 绑定

- [ ] **5.2 简化 FullscreenClockActivity**
  - [ ] 删除所有手动构造代码（已迁移到 Hilt）
  - [ ] 保留仅 UI 相关的绑定逻辑
  - [ ] 提取 setupUI() 中的子逻辑到专用方法
  - [ ] 目标：将 Activity 从 571 行减少到 <300 行
  - **验证**: 
    - 所有功能正常
    - 代码行数减少 >40%
    - 职责清晰分离

- [ ] **5.3 优化 SharedPreferences 批处理写入**
  - [ ] 修改 `AppSettingsManager.resetToDefaults()`
  - [ ] 使用单次 `prefs.edit()` 块写入所有默认值
  - [ ] 添加 `applyBatch {}` 辅助方法
  - [ ] 确保 suppressListeners 逻辑仍然正确
  - **验证**:
    - 重置功能正常
    - 写入次数从 12 次减少到 1 次

- [ ] **5.4 清理和验证**
  - [ ] 删除所有未使用的导入
  - [ ] 运行 `./gradlew lint` 检查
  - [ ] 运行单元测试 `./gradlew test`
  - [ ] 完整功能测试清单：
    - [ ] 应用启动
    - [ ] 时钟显示
    - [ ] 主题切换
    - [ ] 设置菜单
    - [ ] 手势操作
    - [ ] 方向变更
    - [ ] 防烧屏保护
    - [ ] 睡眠定时器
    - [ ] 重置应用

### 交付物
- FullscreenClockActivity < 300 行
- CompositionRoot 类
- 批处理 SharedPreferences 写入
- 完整功能验证

### 风险
- **高** - Activity 是核心，重构可能影响多个功能，需要全面测试

---

## 测试策略

### 每个 Phase 的测试要求

**Phase 1**:
- `./gradlew build` 编译成功
- 应用启动无崩溃

**Phase 2-4**:
- `./gradlew test` 单元测试通过
- 手动测试对应功能
- 配置变更测试（旋转屏幕）

**Phase 5**:
- 完整功能测试清单（见上文）
- `./gradlew lint` 无严重警告
- 代码行数统计对比

### 回归测试清单

每次 PR 后必须验证：
- [ ] 应用正常启动
- [ ] 时钟显示正确时间
- [ ] 秒数显示切换正常
- [ ] 主题切换（明暗）正常
- [ ] 设置菜单打开/关闭
- [ ] 所有设置项可修改
- [ ] 手势调节亮度正常
- [ ] 旋钮时间旅行正常
- [ ] 方向锁定/自动切换正常
- [ ] 防烧屏保护开关正常
- [ ] 睡眠定时器设置正常
- [ ] 重置应用功能正常
- [ ] 屏幕旋转后状态保持

---

## 项目结构变更

### 新增文件
```
app/src/main/java/com/bokehforu/openflip/
├── OpenFlipApplication.kt              # Phase 1
├── di/
│   ├── module/
│   │   ├── CoreModule.kt               # Phase 1
│   │   ├── ManagerModule.kt            # Phase 2
│   │   ├── ControllerModule.kt         # Phase 3
│   │   └── ViewModelModule.kt          # Phase 4
│   └── CompositionRoot.kt              # Phase 5
```

### 修改文件
```
app/src/main/java/com/bokehforu/openflip/
├── ui/FullscreenClockActivity.kt       # Phase 2-5 (大幅简化)
├── viewmodel/FullscreenClockViewModel.kt    # Phase 4
├── viewmodel/FullscreenClockViewModelFactory.kt  # Phase 4 (删除)
├── viewmodel/SettingsViewModel.kt      # Phase 4
├── settings/AppSettingsManager.kt      # Phase 2, 5
├── manager/HapticFeedbackManager.kt    # Phase 2
├── manager/FeedbackSoundManager.kt     # Phase 2
├── manager/TimeProvider.kt             # Phase 2
└── ui/controller/*                     # Phase 3

app/
├── build.gradle.kts                    # Phase 1
└── src/main/AndroidManifest.xml        # Phase 1

build.gradle.kts                        # Phase 1 (project level)
```

---

## 时间估算

| Phase | 预估时间 | 风险级别 | 建议 review 人员 |
|-------|---------|---------|-----------------|
| Phase 1 | 2-3 小时 | ⭐ 低 | 任何团队成员 |
| Phase 2 | 4-6 小时 | ⭐⭐ 中低 | 资深 Android 开发 |
| Phase 3 | 6-8 小时 | ⭐⭐⭐ 中等 | 架构师 |
| Phase 4 | 4-6 小时 | ⭐⭐⭐ 中等 | 资深 Android 开发 |
| Phase 5 | 8-12 小时 | ⭐⭐⭐⭐ 高 | 架构师 + 全团队 |
| **总计** | **24-35 小时** | | |

---

## 回滚策略

每个 Phase 都是独立的 Git commit/PR：
- 如果发现问题，可以单独回滚某个 Phase
- 保持 `main` 分支始终可发布
- 每个 Phase 完成后打 tag

---

## 成功标准

- [ ] FullscreenClockActivity < 300 行（当前 571 行）
- [ ] 删除 2 个手动 ViewModelFactory
- [ ] 所有核心类使用 Hilt 注入
- [ ] SharedPreferences reset 批处理写入
- [ ] `./gradlew build test lint` 全部通过
- [ ] 完整功能测试清单 100% 通过
- [ ] 代码审查通过

---

**计划生成时间**: 2026-01-31
**计划版本**: v1.0
**执行方式**: 增量重构，5 个独立 Phase
