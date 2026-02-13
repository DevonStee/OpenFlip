# 设置界面 Switch 颜色定制计划

## 任务概述
自定义设置界面中特定开关的颜色，使其不跟随主题变化：
1. **Light Mode 开关**: 开启时底色变为 Braun 米白色 (LightSurface)
2. **Auto-Off Bulb 开关**: 开启时底色变为 Braun 绿色 (BraunGreen)
3. **OLED Screen Protection 开关**: 开启时底色变为 Braun 绿色 (BraunGreen)

## 修改步骤

### Step 1: 修改 SettingsSwitchItem 组件
**文件**: `app/src/main/java/com/bokehforu/openflip/ui/compose/SettingsListItems.kt`

在 `SettingsSwitchItem` 函数中添加可选的自定义颜色参数：
- `checkedTrackColor: Color? = null` - 自定义开启时轨道颜色
- `checkedThumbColor: Color? = null` - 自定义开启时滑块颜色

当传入自定义颜色时，使用自定义值；否则使用主题默认颜色。

### Step 2: 修改 Light Mode 开关
**文件**: `app/src/main/java/com/bokehforu/openflip/ui/settings/SettingsMenuSections.kt` (约第 87-95 行)

为 Light Mode 的 `SettingsSwitchItem` 添加：
- `checkedTrackColor = LightSurface` (Braun 米白色 0xFFF5F5F0)
- `checkedThumbColor = Color.White` (纯白色滑块)

### Step 3: 修改 Auto-Off Bulb 开关
**文件**: `app/src/main/java/com/bokehforu/openflip/ui/settings/SettingsMenuSections.kt` (约第 125-134 行)

为 Auto-Off Bulb 的 `SettingsSwitchItem` 添加：
- `checkedTrackColor = BraunGreen` (Braun 绿色 0xFF358A52)
- `checkedThumbColor = Color.White`

### Step 4: 修改 OLED Screen Protection 开关
**文件**: `app/src/main/java/com/bokehforu/openflip/ui/settings/SettingsMenuSections.kt` (约第 158-169 行)

为 OLED Screen Protection 的 `SettingsSwitchItem` 添加：
- `checkedTrackColor = BraunGreen` (Braun 绿色 0xFF358A52)
- `checkedThumbColor = Color.White`

### Step 5: 验证编译
运行 `./gradlew :app:compileDebugKotlin` 确保修改无误。

## 颜色参考
- **BraunGreen**: `Color(0xFF358A52)` - 用于 Auto-Off Bulb 和 OLED Screen Protection
- **LightSurface**: `Color(0xFFF5F5F0)` - 用于 Light Mode (Braun 米白色)
- **Color.White**: 纯白色，用于滑块

## 预期效果
- Light Mode 开关开启时显示米白色底色
- Auto-Off Bulb 和 OLED Screen Protection 开关开启时显示绿色底色
- 这些颜色不随暗色/亮色主题变化而改变

---

**执行命令**: 运行 `/start-work` 开始执行此计划