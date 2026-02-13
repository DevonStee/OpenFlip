# LightToggleController 重构方案

## 当前问题

- 嵌套 if-else 导致逻辑分散
- 倒计时策略与开关逻辑耦合
- 难以扩展新的倒计时模式（比如未来加入 30s、60s 选项）

## 重构方案：策略模式

### 1. 定义倒计时策略接口

```kotlin
sealed interface CountdownStrategy {
    fun start(onTick: (Int) -> Unit, onFinish: () -> Unit): CountDownTimer?
    
    object Disabled : CountdownStrategy {
        override fun start(onTick: (Int) -> Unit, onFinish: () -> Unit) = null
    }
    
    data class Timed(val durationMs: Long, val tickMs: Long = 1000L) : CountdownStrategy {
        override fun start(onTick: (Int) -> Unit, onFinish: () -> Unit): CountDownTimer {
            return object : CountDownTimer(durationMs, tickMs) {
                override fun onTick(millisUntilFinished: Long) {
                    onTick(((millisUntilFinished / 1000) + 1).toInt())
                }
                override fun onFinish() = onFinish()
            }.apply { start() }
        }
    }
}
```

### 2. 重构后的 LightToggleController

```kotlin
class LightToggleController(
    private val stateToggleButton: StateToggleGlowView,
    private val stateToggleIcon: ImageView,
    private val clockView: FullscreenFlipClockView,
    private val settingsManager: AppSettingsManager,
    private val onLightStateChanged: () -> Unit
) {
    private var lightTimer: CountDownTimer? = null
    
    companion object {
        private const val AUTO_OFF_DURATION_MS = 15000L
    }

    fun bind() {
        stateToggleIcon.visibility = View.GONE
        stateToggleButton.setOnClickListener {
            toggleLight(!stateToggleButton.isSelected)
        }
    }

    private fun toggleLight(turnOn: Boolean) {
        updateButtonState(turnOn)
        updateClockLight(turnOn)
        
        lightTimer?.cancel()
        
        when {
            turnOn -> startCountdownIfEnabled()
            else -> clearCountdown()
        }
        
        notifyStateChanged()
    }

    private fun updateButtonState(selected: Boolean) {
        stateToggleButton.isSelected = selected
        stateToggleIcon.isSelected = selected
        stateToggleButton.setGlowEnabled(selected)
    }

    private fun updateClockLight(on: Boolean) {
        clockView.setLightIntensity(if (on) 1f else 0f)
    }

    private fun startCountdownIfEnabled() {
        val strategy = if (settingsManager.isTimedBulbOffEnabled) {
            CountdownStrategy.Timed(AUTO_OFF_DURATION_MS)
        } else {
            CountdownStrategy.Disabled
        }
        
        lightTimer = strategy.start(
            onTick = { seconds -> stateToggleButton.setCountdown(seconds) },
            onFinish = { forceTurnOffLight() }
        )
        
        // 如果是手动模式，清除倒计时显示
        if (strategy is CountdownStrategy.Disabled) {
            stateToggleButton.setCountdown(0)
        }
    }

    private fun clearCountdown() {
        stateToggleButton.setCountdown(0)
    }

    private fun notifyStateChanged() {
        onLightStateChanged()
        stateToggleButton.post { onLightStateChanged() }
    }

    fun forceTurnOffLight() {
        if (stateToggleButton.isSelected || stateToggleButton.isGlowEnabled) {
            toggleLight(turnOn = false)
        }
    }

    // ... 其他方法保持不变
}
```

## 优势

1. **消除嵌套**：所有 if-else 都是单层的
2. **职责清晰**：每个私有方法只做一件事
3. **易于测试**：可以单独测试 `CountdownStrategy`
4. **易于扩展**：未来加入新的倒计时模式只需新增 `CountdownStrategy` 子类

## 权衡

- **代码行数增加**：从 ~120 行变成 ~150 行
- **抽象层次提升**：需要理解策略模式
- **适用场景**：如果未来不会扩展倒计时模式，当前代码已经足够好

## 建议

对于这个项目：

- **当前代码（if-else 版本）**：对于一个个人项目或小团队项目，**完全可以接受**
- **重构版本**：如果你计划开源或者团队协作，**值得投入时间重构**
