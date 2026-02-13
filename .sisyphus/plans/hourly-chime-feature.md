# Hourly Chime 功能实现计划

## 功能概述
在 Settings Sheet 的 "INTERACTION FEEDBACK" 部分添加 Hourly Chime（整点报时）开关，支持每15分钟报时，使用 Bell_sound.mp3 作为声音。

## 需要修改的文件

### 1. 数据模型层
- **Settings.kt** - 添加 `isHourlyChimeEnabled: Boolean` 字段
- **SettingsStore.kt** - 添加接口属性 `isHourlyChimeEnabled`
- **AppSettingsManager.kt** - 添加：
  - KEY_IS_HOURLY_CHIME_ENABLED 常量
  - DEFAULT_HOURLY_CHIME 默认值 (false)
  - _isHourlyChimeEnabledFlow 和对应方法
  - isHourlyChimeEnabled 属性
  - resetToDefaults() 中的重置逻辑
  - Listener 接口添加 onHourlyChimeChanged 方法

### 2. UI 层
- **SettingsMenuSections.kt** - 在 setupFeedbackSection() 中 Mechanical Sound 下方添加：
  - SettingsDivider()
  - SettingsSwitchItem 用于 Hourly Chime
  - 使用 R.drawable.ic_hourly_chime 图标
  - 使用 R.string.labelHourlyChime 标题

### 3. 业务逻辑层
- **HourlyChimeManager.kt** (新建) - 管理报时逻辑：
  - 监听时间变化（每分钟检查）
  - 在 :00, :15, :30, :45 播放声音
  - 使用 FeedbackSoundManager 播放 chime_sound.mp3
  - 根据设置开关启用/禁用

### 4. 资源文件
- **strings.xml** - 添加：
  - `<string name="labelHourlyChime">Hourly Chime</string>`
- **drawable/ic_hourly_chime.xml** - 保存提供的矢量图标
- **raw/chime_sound.mp3** - 已复制完成

### 5. DI 配置
- **ManagerModule.kt** - 添加 HourlyChimeManager 的绑定

## 实现步骤

1. ✅ 复制声音文件到 raw/chime_sound.mp3
2. 创建 drawable/ic_hourly_chime.xml
3. 添加字符串资源 labelHourlyChime
4. 修改 Settings.kt 添加字段
5. 修改 SettingsStore.kt 添加接口属性
6. 修改 AppSettingsManager.kt 添加完整逻辑
7. 修改 SettingsMenuSections.kt 添加开关
8. 创建 HourlyChimeManager.kt
9. 在 DI 模块中注册 HourlyChimeManager
10. 编译验证

## 验收标准
- [ ] 设置界面显示 Hourly Chime 开关
- [ ] 开关可以正常打开/关闭
- [ ] 打开后每到 :00, :15, :30, :45 播放报时声音
- [ ] 关闭后不再播放
- [ ] 重置应用后恢复默认关闭状态
