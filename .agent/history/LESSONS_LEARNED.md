# OpenFlip 项目开发经验总结 (Lessons Learned)

这份文档总结了 OpenFlip 项目中的经验教训，为未来 Android 高性能视图开发提供参考。

---

## 第一章：高效协作工作流

### 1.1 配置先行 (Config-First)

在后期提取 colors 和 dimens 会浪费大量重构时间。**新项目启动步骤**：

1. **建立 `colors.xml`**：定义语义化颜色（如 `bg_primary`, `text_secondary`）
2. **建立 `themes.xml`**：明确 Day/Night 模式的 `windowBackground`
3. **铺设工具类**：`Utils.dp2px`、`HapticManager`、`SoundManager`

### 1.2 明确的需求描述

**❌ 模糊**: "把那个时钟改一下，要防烧屏。"
**✅ 清晰**: "增加防烧屏功能。规则：每分钟 X/Y 轴偏移 1px，最大范围 4px，使用 ValueAnimator。"

### 1.3 沟通黄金公式

> **[位置/区域] + [当前异常表现] + [对比参照物/感官描述] = 精准执行**

*示例*："White 小组件的 [中间线条] 太长了，应该 [匹配卡片最窄处] 的宽度。"

---

## 第二章：屏幕旋转抗闪烁协议

这是本项目最大的学费，总结为**铁律**：

### 2.1 Manifest 配置

```xml
android:configChanges="orientation|screenSize|screenLayout"
```

**原因**：拒绝 Activity 重建，拒绝黑屏重载。

### 2.2 Window Background

- **原则**：`android:windowBackground` 必须在 XML 中硬编码，**严禁**依赖 `?attr/` 动态属性
- **Light Mode**: `@color/white`
- **Dark Mode**: `@color/black` (通过 `values-night/themes.xml`)

### 2.3 硬件层持久化

```kotlin
setLayerType(LAYER_TYPE_HARDWARE, null)
```

**现象**：静置 10s 后旋转闪黑
**原因**：GPU 资源被回收，重新光栅化导致掉帧

---

## 第三章：高性能自定义 View 渲染

### 3.1 零内存分配 onDraw

- **禁止**：在 `onDraw` 中 `new Paint()`, `new Path()`
- **做法**：所有对象在 `init` 或 `onSizeChanged` 中创建/重置

### 3.2 Path 预计算

不要在 onDraw 里算圆角矩形。在 `onSizeChanged` 里算好路径，绘制时只做 render。

### 3.3 Shader 条件刷新

```kotlin
if (lastShaderWidth != w || lastShaderHeight != h) {
    refreshGradients()
    lastShaderWidth = w
    lastShaderHeight = h
}
```

---

## 第四章：Widget 开发教训

### 4.1 方案演变

| 方案 | 结果 |
|------|------|
| Bitmap 截图流 | ❌ 后台限制，经常卡死 |
| AlarmManager 定时 | ❌ 不准时，延迟几秒到几分钟 |
| TextClock 原生 | ✅ 系统级精准对时，零耗电 |

### 4.2 RemoteViews 限制

**已确认无法**通过叠加 View 层修复 Widget 中间缝隙的抗锯齿白边。
**原因**：Android AppWidget 对视图层级有严格限制，叠加可能导致 Widget 加载失败。
**决策**：保留白线，优先保证功能可用性。

---

## 第五章：代码质量反思

### 5.1 避免"补丁式编程"

本项目中 `FlipCard.draw()` 充满了 `if (静止) else if (前半段) else (后半段)`。

**问题**：

- 逻辑割裂，容易漏条件分支
- 魔法数字散落各处
- 一个类承担太多职责

**解决**：重构为 `CardState + CardRenderer` 分离架构。

### 5.2 浮点数比较教训

动画结束后 `flipDegree` 可能是 `0.00001f` 而非精确的 `0f`。

```kotlin
// ❌ 错误
if (flipDegree == 0f) { ... }

// ✅ 正确
val isAtRest = flipDegree < 0.5f
```

---

## 第六章：与 AI 协作的技巧

### 6.1 提供上下文

不仅是"改这个文件"，而是"我在做 Settings 页面，现在要加一个开关"。

### 6.2 明确约束

例如"不要引入新库"、"必须兼容 API 31+"、"不能改动现有动画时长"。

### 6.3 描述感官而非技术

与其猜技术术语，不如描述物理现象：

- "像物理折叠"
- "像玻璃"
- "两头胖中间细"

### 6.4 截图+文字

这是纠偏的最强武器，能解决 90% 的理解偏差。

---

## 总结

OpenFlip 是典型的**从 0 到 1 再到精**的过程：

- 0→1 做得快
- 1→精（解决黑闪、纹理对齐、性能优化）虽然痛苦，但产出的**旋转抗闪烁协议**和**高性能 View 规范**是无价的

**下一个项目**，直接从"配置先行"和"硬件层开启"开始，起步即巅峰。
