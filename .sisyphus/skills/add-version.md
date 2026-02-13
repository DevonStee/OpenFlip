# Skill: Add New Version to OpenFlip

## Description
快速添加新版本记录到 OpenFlip 应用的 Version 界面。自动更新版本号、添加发布说明到设置菜单的版本历史页面。

## Usage

```bash
# 基本用法 - 添加新版本
/start-work add-version <version> <title> <summary>

# 完整用法 - 带详细更新列表
/start-work add-version <version> <title> <summary> --details "<detailed_changelog>"
```

## Parameters

- `version`: 版本号 (例如: "0.7.0", "0.6.1")
- `title`: 版本标题，简短描述主要更新 (例如: "Widget Support & Performance")
- `summary`: 一句话总结最重要的更新 (例如: "[New] Added home screen widgets with live clock updates")
- `details`: (可选) 详细更新列表，使用 \n 分隔每条更新

## Examples

### 示例 1: 基础版本更新
```
/start-work add-version 0.7.0 "Widget Support" "[New] Added 5 home screen widget styles for quick time checking"
```

### 示例 2: 带详细更新列表
```
/start-work add-version 0.7.0 "Widget Support & Performance" "[New] Added home screen widgets with live clock updates" --details "[New] 5 distinct widget styles: Classic, Glass, Minimal, Solid, Split.
[New] Real-time clock updates via TextClock for all widgets.
[Improved] Optimized widget rendering performance and reduced battery usage.
[Fixed] Widget font compatibility issues on Android 12+ devices.
[Improved] Enhanced widget preview in system picker with accurate theming."
```

### 示例 3: 小版本修复
```
/start-work add-version 0.6.1 "Bug Fixes" "[Fixed] Resolved occasional crash when switching themes rapidly"
```

## What This Skill Does

1. **更新 build.gradle.kts**: 修改 `versionName` 字段
2. **更新 strings.xml**: 
   - 修改 `labelVersionValue` 显示版本
   - 添加 `titleVXXX` 版本标题字符串
   - 添加 `descriptionVXXX_summary` 简要描述
   - 添加 `descriptionVXXX_details` 详细更新列表
3. **更新 layout_settings_information.xml**: 
   - 在版本历史顶部插入新版本的 UI 元素
   - 保持与现有版本一致的样式和间距

## File Locations

- `app/build.gradle.kts` - 版本号定义
- `app/src/main/res/values/strings.xml` - 版本字符串资源
- `app/src/main/res/layout/layout_settings_information.xml` - 版本历史 UI 布局

## Version Format

- 开发中版本: `0.6.0-beta`, `0.7.0-beta`
- 正式版本: `1.0.0`, `1.1.0`
- 小版本修复: `0.6.1-beta`, `0.6.2-beta`

## Changelog Categories

使用标准前缀让更新记录更清晰：

- `[New]` - 新功能
- `[Improved]` - 功能改进/优化
- `[Fixed]` - Bug 修复
- `[Refactor]` - 代码重构
- `[Removed]` - 移除的功能
- `[Notes]` - 重要说明

## Formatting Rules

### 换行规范（重要）

**每个更新项目之间必须使用双换行（`\n\n`）分隔**，确保在 Version 页面有清晰的视觉分隔：

```xml
<!-- 正确格式 - 使用 \n\n 分隔 -->
<string name="descriptionV070_details">[New] Added widget support for home screen.\n\n[New] 5 distinct widget styles available.\n\n[Improved] Optimized battery usage for widgets.\n\n[Fixed] Resolved widget update issues on Android 12.</string>

<!-- 错误格式 - 不要使用单换行 -->
<string name="descriptionV070_details">[New] Added widget support.\n[New] 5 widget styles.\n[Improved] Battery optimization.</string>
```

**为什么需要双换行？**
- 单换行 `\n` 在 TextView 中只显示为普通换行，项目之间没有空行
- 双换行 `\n\n` 会在每个 `[Category]` 项目之间创建空行，视觉更清晰
- 所有历史版本（v0.6.0、v0.5.8 等）都已统一使用双换行格式

### 自动格式化

脚本会自动将单换行替换为双换行，但你应该**在输入时就使用双换行**：

```bash
# 推荐：手动使用 \n\n
.sisyphus/skills/add-version.sh "0.7.0" "Widget Support" "[New] Added widgets" "[New] 5 widget styles.\n\n[Improved] Performance.\n\n[Fixed] Compatibility."
```

## ⚠️ Critical: Theme Color Registration (重要！)

OpenFlip 使用 **硬编码 ID 列表** 来应用主题颜色。添加新版本后，**必须**手动将新版本的 TextView ID 注册到 `SettingsThemeHelper.kt`，否则文字颜色不会跟随主题变化！

### 问题现象
如果在白色主题下，新版本的文字显示为白色（看不见），说明忘记注册 ID。

### 解决步骤

**1. 找到 SettingsThemeHelper.kt**
```
app/src/main/java/com/bokehforu/openflip/ui/theme/SettingsThemeHelper.kt
```

**2. 添加标题 ID 到主色列表**
在 `applyStaticViewStyles()` 方法中，找到标题列表，添加新版本的 title ID：

```kotlin
listOf(
    R.id.textInformationAppName,
    R.id.textInformationTitleV060,  // ← 添加新版本标题 ID
    R.id.textInformationTitleV058,
    // ... 其他版本
).forEach { id ->
    (rootView.findViewById<View>(id) as? TextView)?.setTextColor(primaryColor)
}
```

**3. 添加描述 ID 到次要色列表**
在同一个方法中，找到描述列表，添加新版本的 description ID：

```kotlin
listOf(
     R.id.textInformationInspiredBy,
     R.id.textInformationDescriptionV060,  // ← 添加新版本描述 ID
     R.id.textInformationDescriptionV058,
     // ... 其他版本
).forEach { id ->
    (rootView.findViewById<View>(id) as? TextView)?.setTextColor(secondaryColor)
}
```

### 命名规则
- 标题 ID: `textInformationTitleVXXX` (如 V060 对应 v0.6.0)
- 描述 ID: `textInformationDescriptionVXXX` (如 V060 对应 v0.6.0)

### 为什么需要这个步骤？
这是 OpenFlip 的架构限制：
- 使用硬编码 ID 列表手动应用主题颜色
- 没有使用自动遍历或统一命名模式
- 每次添加新版本都需要手动更新

**忘记此步骤的后果**：新版本在白色主题下文字显示为白色，用户看不见版本记录。

---

## Notes

- 新版本会自动显示在 Version 页面的最顶部
- 旧版本会自动向下移动
- 保持版本历史的时间倒序排列（最新版本在最上面）
- 编译后会立即生效，无需额外配置
- **记得更新 SettingsThemeHelper.kt 注册 ID！**
