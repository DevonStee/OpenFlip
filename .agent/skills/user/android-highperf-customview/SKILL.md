---
name: Android High-Performance Custom View
description: Best practices for 60fps custom View rendering with zero allocation in onDraw
---

# Skill: Android High-Performance Custom View

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** None

## Purpose

This skill covers techniques for achieving smooth 60fps rendering in custom Android Views, focusing on memory efficiency and GPU optimization.

---

## When to Use This Skill

- Building custom views with complex drawing logic
- Experiencing jank or dropped frames during animation
- Profiler shows GC pauses during drawing

---

## Rule 1: Zero-Allocation onDraw

**NEVER** allocate objects inside `onDraw()`:

```kotlin
// ❌ BAD - allocates every frame
override fun onDraw(canvas: Canvas) {
    val paint = Paint()
    val path = Path()
    val rect = RectF()
    // ...
}

// ✅ GOOD - pre-allocate at class level
private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
private val path = Path()
private val rect = RectF()

override fun onDraw(canvas: Canvas) {
    path.reset()  // Reuse, don't recreate
    // ...
}
```

---

## Rule 2: Path Pre-computation

Calculate complex paths in `onSizeChanged`, not `onDraw`:

```kotlin
private val clipPath = Path()
private val cardPath = Path()

override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    
    // Pre-compute rounded rectangle path
    cardPath.reset()
    cardPath.addRoundRect(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius, cornerRadius, Path.Direction.CW)
    
    // Pre-compute clip regions
    clipPath.set(cardPath)
    tempPath.addRect(0f, 0f, w.toFloat(), h / 2f, Path.Direction.CW)
    clipPath.op(tempPath, Path.Op.INTERSECT)
}

override fun onDraw(canvas: Canvas) {
    canvas.clipPath(clipPath)  // Use pre-computed path
    // ...
}
```

---

## Rule 3: Conditional Shader/Gradient Refresh

Only recreate expensive objects when dimensions actually change:

```kotlin
private var lastWidth = 0
private var lastHeight = 0

private fun refreshGradientsIfNeeded(w: Int, h: Int) {
    if (w == lastWidth && h == lastHeight) return
    
    lastWidth = w
    lastHeight = h
    
    // Expensive shader creation
    paint.shader = LinearGradient(
        0f, 0f, 0f, h.toFloat(),
        topColor, bottomColor,
        Shader.TileMode.CLAMP
    )
}
```

---

## Rule 4: Hardware Layer for Animations

Enable hardware acceleration for animation-heavy views:

```kotlin
init {
    setLayerType(LAYER_TYPE_HARDWARE, null)
}
```

**When to use**:

- Views with continuous animations
- Complex layered drawing
- Shadow/blur effects

**When NOT to use**:

- Static content
- Memory-constrained devices

---

## Rule 5: Text Bounds Caching

For views displaying text, cache measurement results:

```kotlin
private val textBoundsCache = mutableMapOf<String, Rect>()

private fun getTextBounds(text: String): Rect {
    return textBoundsCache.getOrPut(text) {
        Rect().also { paint.getTextBounds(text, 0, text.length, it) }
    }
}
```

---

## Rule 6: Threshold-Based Updates

Avoid redundant recalculations for minor changes:

```kotlin
private var lastDimWidth = 0f

fun setDimensions(width: Float, height: Float) {
    // Only update if change exceeds threshold
    if (abs(width - lastDimWidth) < 0.5f) return
    
    lastDimWidth = width
    // Expensive recalculation...
}
```

---

## Performance Checklist

- [ ] No `new` keywords inside `onDraw()`
- [ ] Paths calculated in `onSizeChanged()`
- [ ] Shaders cached with dimension checks
- [ ] Text bounds cached per character/string
- [ ] Hardware layer enabled for animated views

---

## Profiling Tips

1. **GPU Profiler**: Check for overdraw (Settings → Developer → Debug GPU overdraw)
2. **Allocation Tracker**: Verify zero allocations during `onDraw`
3. **Frame Timing**: Target <16ms per frame for 60fps
