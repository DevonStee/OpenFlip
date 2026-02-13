# OpenFlip 电池优化工作计划

## 项目概述

针对 OpenFlip 应用的电池消耗问题进行系统性优化，在**不影响核心功能和视觉效果**的前提下，降低 CPU/GPU 负载，延长设备续航时间。

---

## 优化目标

- **降低后台功耗**：应用后台时完全停止动画和定时器
- **减少无效重绘**：合并重复的重绘请求，降低渲染频率
- **优化渲染管线**：缓存昂贵的着色器计算，简化阴影复杂度
- **预期效果**：待机状态下功耗降低 30-50%，动画期间降低 20-30%

---

## 优化策略

### 三阶段实施

| 阶段 | 内容 | 风险等级 | 预期省电效果 |
|------|------|----------|--------------|
| **Phase 1** | 安全优化（合并重绘 + 后台暂停） | 🟢 零风险 | 40-50% |
| **Phase 2** | 渲染优化（缓存 + 惯性滚动优化） | 🟡 低风险 | 15-20% |
| **Phase 3** | 进阶优化（低电量模式 + 阴影简化） | 🟠 中风险 | 10-15% |

---

## Phase 1: 安全优化（零功能影响）

### Task 1: 合并 FullscreenFlipClockView 重绘调用

**问题分析**：
- 当前代码在多个 setter 中独立调用 `invalidate()`
- 连续的状态变更会导致多次重绘

**优化方案**：
```kotlin
// 添加重绘标记，延迟到下一帧统一处理
private var pendingInvalidate = false

private fun requestInvalidate() {
    if (!pendingInvalidate) {
        pendingInvalidate = true
        post {
            pendingInvalidate = false
            invalidate()
        }
    }
}
```

**修改位置**：
- `FullscreenFlipClockView.kt` 第 57-64 行（showSeconds setter）
- `FullscreenFlipClockView.kt` 第 72-76 行（backgroundColorOverride setter）
- `FullscreenFlipClockView.kt` 第 97-114 行（applyScale/resetScale）

**验收标准**：
- [ ] 连续调用多个状态变更 setter 只触发一次重绘
- [ ] 翻页动画流畅度不受影响
- [ ] 主题切换、缩放功能正常

---

### Task 2: 实现后台自动暂停机制

**问题分析**：
- 应用进入后台时，动画和定时器仍在运行
- OLED 保护机制、秒级定时器持续消耗电量

**优化方案**：

1. **在 FullscreenClockActivity 中添加生命周期管理**：
```kotlin
override fun onPause() {
    super.onPause()
    // 暂停所有动画
    animationManager.cancelAll()
    flipAnimationsController.cleanup()
    gearAnimationController.stop()
    
    // 暂停秒级定时器
    secondsTicker.setEnabled(false)
    
    // 暂停 OLED 保护
    burnInProtectionManager.stop()
    
    // 停止光效动画
    lightOverlayRenderer.cleanup()
}

override fun onResume() {
    super.onResume()
    // 恢复秒级定时器（如果用户开启）
    secondsTicker.setEnabled(settings.showSeconds)
    
    // 恢复 OLED 保护（如果用户开启）
    if (settings.oledProtection) {
        burnInProtectionManager.start()
    }
    
    // 触发一次重绘以更新显示
    invalidate()
}
```

2. **确保所有控制器支持暂停/恢复**：
- `FlipAnimationManager.cancelAll()` 已存在，确保清理彻底
- `TimeSecondsTicker.setEnabled()` 已存在
- `DisplayBurnInProtectionManager.stop()/start()` 已存在

**修改位置**：
- `FullscreenClockActivity.kt` 生命周期方法

**验收标准**：
- [ ] 应用切换到后台后，CPU 使用率降至接近零
- [ ] 返回前台时，时间显示自动更新到当前时间
- [ ] 翻页动画、光效等功能恢复正常
- [ ] 设置中的"保持屏幕开启"选项仍然有效

---

## Phase 2: 渲染优化（低风险）

### Task 3: 缓存 LightOverlayRenderer 渐变着色器

**问题分析**：
- `LightOverlayRenderer.updateGradient()` 在每次光强变化时都重新创建 `RadialGradient`
- 光效动画期间（300ms），每帧都创建新的着色器对象

**优化方案**：
```kotlin
class LightOverlayRenderer {
    // 添加缓存
    private var cachedGradient: RadialGradient? = null
    private var cachedSourceX = -1f
    private var cachedSourceY = -1f
    private var cachedRadius = -1f
    private var cachedIntensity = -1f
    
    private fun updateGradient(...) {
        // 检查是否需要重新创建
        if (sourceX == cachedSourceX && 
            sourceY == cachedSourceY && 
            radius == cachedRadius &&
            intensity == cachedIntensity) {
            return // 使用缓存
        }
        
        // 创建新的渐变并缓存
        cachedGradient = RadialGradient(...)
        lightPaint.shader = cachedGradient
        
        // 更新缓存键
        cachedSourceX = sourceX
        cachedSourceY = sourceY
        cachedRadius = radius
        cachedIntensity = intensity
    }
}
```

**修改位置**：
- `LightOverlayRenderer.kt` 第 110-179 行

**验收标准**：
- [ ] 光效开关动画流畅度不变
- [ ] 快速多次开关光效不会导致内存抖动
- [ ] 暗/亮主题切换时渐变正确更新

---

### Task 4: 优化 InfiniteKnobView 惯性滚动

**问题分析**：
- `computeScroll()` 使用 `postInvalidateOnAnimation()` 形成持续重绘循环
- 惯性滚动期间，即使视图内容无变化也每帧重绘

**优化方案**：

1. **添加脏检查机制**：
```kotlin
override fun computeScroll() {
    if (scroller.computeScrollOffset()) {
        val newRotation = scroller.currX.toFloat()
        
        // 只有旋转角度变化超过阈值才重绘
        if (kotlin.math.abs(newRotation - totalRotationDegrees) > 0.5f) {
            totalRotationDegrees = newRotation
            checkAndTriggerVibration()
            onRotationChangedListener?.invoke(totalRotationDegrees)
            postInvalidateOnAnimation()
        } else {
            // 继续检查滚动状态但不重绘
            postInvalidateOnAnimation()
        }
    }
}
```

2. **滚动结束时立即停止**：
```kotlin
override fun computeScroll() {
    if (scroller.computeScrollOffset()) {
        // ... 更新逻辑
        postInvalidateOnAnimation()
    } else {
        // 滚动结束，确保最终状态正确
        if (scroller.isFinished) {
            // 触发最终回调
            onRotationChangedListener?.invoke(totalRotationDegrees)
        }
    }
}
```

**修改位置**：
- `InfiniteKnobView.kt` 第 362-369 行

**验收标准**：
- [ ] 旋钮惯性滚动流畅度不变
- [ ] 滚动停止后不再持续重绘
- [ ] 时间旅行功能正常，旋转角度正确映射到时间

---

## Phase 3: 进阶优化（可选）

### Task 5: 添加低电量模式（30fps 动画）

**问题分析**：
- 当前动画以 60fps 运行，对电池压力较大
- 低电量时用户更愿意牺牲流畅度换取续航

**优化方案**：

1. **添加电池状态监听**：
```kotlin
class BatteryOptimizationManager {
    private var isLowBatteryMode = false
    
    fun checkBatteryLevel(context: Context) {
        val batteryIntent = context.registerReceiver(null, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()
        
        isLowBatteryMode = batteryPct < 20
    }
}
```

2. **动画帧率控制**：
```kotlin
class FlipAnimationManager {
    private val normalFrameDelay = 16L // 60fps
    private val lowBatteryFrameDelay = 33L // 30fps
    
    private fun getFrameDelay(): Long {
        return if (batteryOptimizationManager.isLowBatteryMode) {
            lowBatteryFrameDelay
        } else {
            normalFrameDelay
        }
    }
}
```

**修改位置**：
- 新建 `BatteryOptimizationManager.kt`
- 修改 `FlipAnimationManager.kt`

**验收标准**：
- [ ] 电量低于 20% 时自动启用低电量模式
- [ ] 低电量模式下动画以 30fps 运行，视觉可接受
- [ ] 充电后自动恢复正常帧率
- [ ] 设置中添加"强制低电量模式"开关（用户可选）

---

### Task 6: 简化阴影渲染复杂度

**问题分析**：
- `FlipCardRenderer` 使用多层阴影（阴影 Paint + 边缘 Paint + 裁剪）
- 每层阴影都增加 GPU 合成开销

**优化方案**：

1. **合并阴影层**：
```kotlin
// 当前：多层绘制
private fun drawFlapCastShadow(canvas: Canvas, rect: RectF) {
    canvas.save()
    canvas.clipRect(rect)
    shadowPaint.alpha = ...
    canvas.drawPath(geometry.fullCardPath, shadowPaint)
    canvas.restore()
}

// 优化：单层绘制，通过 alpha 控制强度
private fun drawFlapCastShadowOptimized(canvas: Canvas, rect: RectF, intensity: Float) {
    if (intensity <= 0.01f) return // 跳过不可见的阴影
    
    shadowPaint.alpha = (intensity * maxShadowAlpha).toInt()
    canvas.drawPath(geometry.fullCardPath, shadowPaint)
}
```

2. **降低阴影分辨率**：
```kotlin
// 在暗色主题下降低阴影复杂度
private fun getShadowQuality(): Float {
    return if (colors.isDarkTheme && batteryOptimizationManager.isLowBatteryMode) {
        0.7f // 降低阴影质量
    } else {
        1.0f // 完整质量
    }
}
```

**修改位置**：
- `FlipCardRenderer.kt` 第 278-286 行

**验收标准**：
- [ ] 3D 翻转效果仍然明显
- [ ] 阴影质量降低后用户几乎无法察觉差异
- [ ] GPU 负载降低（可通过 Android Studio Profiler 验证）

---

## 实施顺序

### 第一阶段（立即实施）
1. **Task 1**: 合并重绘调用 - 预计 2 小时
2. **Task 2**: 后台暂停机制 - 预计 3 小时

### 第二阶段（验证第一阶段后）
3. **Task 3**: 缓存渐变着色器 - 预计 2 小时
4. **Task 4**: 优化惯性滚动 - 预计 2 小时

### 第三阶段（根据效果评估）
5. **Task 5**: 低电量模式 - 预计 4 小时
6. **Task 6**: 简化阴影 - 预计 3 小时

---

## 测试验证方案

### 功能测试
- [ ] 翻页动画正常（小时/分钟翻转）
- [ ] 主题切换正常（暗/亮模式）
- [ ] 缩放功能正常（双指捏合）
- [ ] 光效开关正常
- [ ] 旋钮时间旅行正常
- [ ] 秒显示开关正常
- [ ] OLED 保护功能正常
- [ ] 设置菜单正常

### 性能测试
- [ ] 使用 Android Studio Profiler 监测 CPU/GPU 使用率
- [ ] 后台状态 CPU 使用率 < 5%
- [ ] 动画期间 GPU 负载降低 20%+
- [ ] 电池消耗降低 30%+

### 回归测试
- [ ] 不同 Android 版本（API 26-34）
- [ ] 不同屏幕尺寸（手机/平板）
- [ ] 不同主题（暗/亮）
- [ ] 不同方向（横屏/竖屏）

---

## 风险评估

| 风险项 | 概率 | 影响 | 缓解措施 |
|--------|------|------|----------|
| 动画卡顿 | 低 | 高 | 保留原始实现作为 fallback |
| 功能回归 | 低 | 高 | 完整的功能测试覆盖 |
| 视觉效果下降 | 中 | 中 | A/B 测试，可配置开关 |
| 代码复杂度增加 | 中 | 低 | 详细注释，保持代码清晰 |

---

## 成功标准

1. **电池续航**：待机状态下功耗降低 40%+
2. **用户体验**：所有功能正常，视觉效果无明显下降
3. **代码质量**：优化代码有完整注释，可维护性好
4. **可回滚**：每个优化都可单独禁用，方便问题排查

---

## 下一步行动

1. ✅ 创建此工作计划
2. 🔄 实施 Phase 1 - Task 1（合并重绘）
3. ⏳ 实施 Phase 1 - Task 2（后台暂停）
4. ⏳ 功能测试和性能基准测试
5. ⏳ 根据结果决定是否继续 Phase 2

---

**计划创建时间**: 2026-01-30  
**预计总工期**: 16 小时（分阶段实施）  
**风险等级**: 低-中
