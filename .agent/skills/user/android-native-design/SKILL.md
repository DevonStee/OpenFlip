---
name: Android Native Design
description: Expert guidance for creating soulful, industrial-grade Android native interfaces with Jetpack Compose, focusing on tactility, typography, and motion.
---

# SKILL: Android-Native-Design

此 Skill 旨在指导创建具有**独特灵魂、工业级水准**的安卓原生界面。拒绝平庸的 Material Design 模板化设计，追求在移动端小屏幕上实现极具视觉冲击力和丝滑交互的体验。

## 1. 核心工业美学 (Core Industrial Aesthetics)

在编写任何代码之前，必须确立审美品位与视觉广度：

*   **物理触感 (Tactility):** 定义材质（磨砂金属、流体玻璃、粗糙网格）。
*   **极端基调 (Extreme Tone):**
    *   **Neo-Skeuomorphism:** 利用 `RenderEffect` 和多层阴影营造触控深度。
    *   **Cyber-Industrial:** 紧凑网格 (8dp grid strict), 等宽字体 (Monospace), 极高对比度。
    *   **Editorial Layout:** 巨大的 Heading 与超小 Body 的字号映射关系 (e.g., 96sp vs 10sp)。

---

## 2. 技术实现深挖 (Technical implementation Deep-dives)

### A. 突破性的排版：动态字重映射 (Variable Fonts)

利用原生字重轴实现平滑过渡，而非简单的开关切换。

```kotlin
// 映射滑动进度到字重 (e.g., 从 300 到 800)
val dynamicWeight by animateIntAsState(
    targetValue = if (isExpanded) 800 else 300,
    animationSpec = spring(stiffness = Spring.StiffnessLow)
)

Text(
    text = "TERMINAL",
    style = TextStyle(
        fontFamily = FontFamily(Font(R.font.bebas_neue_variable, FontWeight(dynamicWeight))),
        fontSize = 48.sp,
        letterSpacing = 4.sp
    )
)
```

### B. 材质与深度：新拟物化软阴影 (Soft Shadow)

严禁使用默认 `elevation`。通过 `drawBehind` 实现精细的光影模拟。

```kotlin
fun Modifier.neoShadow(
    cornerRadius: Dp = 16.dp,
    spread: Dp = 8.dp,
    blur: Dp = 12.dp
) = this.drawBehind {
    val lightColor = Color.White.copy(alpha = 0.5f)
    val darkColor = Color.Black.copy(alpha = 0.2f)
    
    // 高光 (Top-Left)
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = lightColor.toArgb()
            setShadowLayer(blur.toPx(), -spread.toPx(), -spread.toPx(), lightColor.toArgb())
        }
        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cornerRadius.toPx(), cornerRadius.toPx(), paint)
    }
    
    // 暗影 (Bottom-Right)
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            color = darkColor.toArgb()
            setShadowLayer(blur.toPx(), spread.toPx(), spread.toPx(), darkColor.toArgb())
        }
        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cornerRadius.toPx(), cornerRadius.toPx(), paint)
    }
}
```

### C. 实时滤镜：AGSL 噪点着色器 (Grain & Noise)

使用 Android 13+ 的 `RuntimeShader` 注入工业质感。

```kotlin
val GRAIN_SHADER = """
    uniform float2 resolution;
    uniform float time;
    
    half4 main(float2 fragCoord) {
        float x = (fragCoord.x / resolution.x + time) * 10.0;
        float y = (fragCoord.y / resolution.y) * 10.0;
        float noise = fract(sin(dot(float2(x, y), float2(12.9898, 78.233))) * 43758.5453);
        return half4(half3(noise) * 0.05, 0.0); // 微弱的叠加噪点
    }
""".trimIndent()

// 使用示例
Modifier.graphicsLayer {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        renderEffect = RenderEffect.createRuntimeShaderEffect(
            RuntimeShader(GRAIN_SHADER), "inputShader"
        ).asComposeRenderEffect()
    }
}
```

### D. 响应式动力学 (Motion Spec Library)

预设的物理参数库，禁止使用线性动画。

| 风格 | Damping Ratio | Stiffness | 场景 |
| :--- | :--- | :--- | :--- |
| **Fluid (液态)** | `Spring.DampingRatioMediumBouncy` | `Spring.StiffnessLow` | 页面转场/大规模位移 |
| **Snappy (干脆)** | `Spring.DampingRatioNoBouncy` | `Spring.StiffnessHigh` | 按钮点击/开关切换 |
| **Organic (有机)** | `0.6f` | `150f` | 拖拽跟随/列表弹性 |

---

## 3. 布局战略 (Layout Strategy)

*   **打破网格 (Break the Grid):** 
    *   使用 `Box` 实现重叠率 > 20% 的视觉效果。
    *   使用 `Modifier.offset` 或 `graphicsLayer` 制造元素逸出屏幕的错觉。
*   **全沉浸模式 (Immersion):**
    *   强制处理 `WindowInsets.navigationBars` 和 `WindowInsets.statusBars`。
    *   UI 元素必须能够延伸到摄像头打孔区域 (Cutout) 下方。

---

## 4. 色彩与工业方案 (Industrial Palette)

| Theme | Background | Primary Accents | Surface Detail |
| :--- | :--- | :--- | :--- |
| **Cyber-Industrial** | `#080808` | `#00FF41` (Matrix Green) | `#1A1A1A` |
| **Monochrome Pro** | `#FFFFFF` | `#000000` | `#F5F5F5` (OLED Friendly Gray) |
| **Tactile Sand** | `#F2EDE4` | `#2D2926` | `#E8E2D6` (Paper Texture) |

---

## 5. 严禁行为 (Anti-Patterns)

*   **禁止默认 Elevation:** 除非经过 `brush` 或 `layered path` 优化，否则禁止使用 `shadow(elevation)`.
*   **禁止过度圆角:** 工业感通常来自直角或微圆角（4dp-8dp），除非是在模仿液态效果。
*   **禁止静态静态列表:** 任何滚动都应伴随微妙的 `TranslationY` 或 `Scale` 变化。
