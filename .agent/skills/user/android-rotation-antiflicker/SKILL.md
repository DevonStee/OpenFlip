---
name: Android Rotation Anti-Flicker
description: Eliminate black flash and visual glitches during screen rotation in Android apps
last_verified: 2026-01-23
applicable_sdk: Android 8+ (API 26+)
---

# Android Rotation Anti-Flicker Protocol

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 8+ (API 26+), tested through Android 15 (API 35)
**Project Context:** OpenFlip (Target SDK 35, Min SDK 26)

This skill provides a proven protocol to eliminate black screen flashes and visual glitches during screen rotation in Android applications.

---

## Prerequisites

**Required Skills:**

- [color-tokens](../color-tokens/SKILL.md) - For proper windowBackground color references

**Related Skills:**

- [android-highperf-customview](../android-highperf-customview/SKILL.md) - For smooth rotation animations

---

## Android 15+ Considerations (API 35)

**New Behaviors:**

- Predictive back gesture affects window transitions
- Enhanced edge-to-edge enforcement
- Window size class changes during rotation

**Compatibility**: All techniques in this skill remain valid for Android 15.

---

## When to Use This Skill

- App has full-screen custom views (clocks, games, media players)
- Users report black flash on rotation
- Need smooth 60fps rotation animations

---

## Step 1: Manifest Configuration

Add `configChanges` to prevent Activity recreation:

```xml
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
    android:screenOrientation="fullSensor" />
```

**Why**: Prevents Activity destruction/recreation cycle that causes black screen.

---

## Step 2: Window Background (CRITICAL)

### Light Theme (`values/themes.xml`)

```xml
<style name="AppTheme" parent="Theme.Material3.Light.NoActionBar">
    <item name="android:windowBackground">@color/white</item>
</style>
```

### Dark Theme (`values-night/themes.xml`)

```xml
<style name="AppTheme" parent="Theme.Material3.Dark.NoActionBar">
    <item name="android:windowBackground">@color/black</item>
</style>
```

> [!CAUTION]
> **NEVER** use `?attr/colorSurface` or any dynamic attribute for `windowBackground`.
> Dynamic attributes are resolved during rotation, causing a brief flash.

---

## Step 3: Hardware Layer Persistence

Apply to animation-heavy custom views:

```kotlin
class MyCustomView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
}
```

**Why**: Prevents GPU resource deallocation after idle periods. Without this, rotating after 10+ seconds of inactivity causes black flash as GPU re-rasterizes.

---

## Step 4: Handle Configuration Changes

Override `onConfigurationChanged` instead of relying on Activity recreation:

```kotlin
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    // Recalculate dimensions, refresh layouts
    myCustomView.requestLayout()
}
```

---

## Verification Checklist

- [ ] Rotate rapidly 10+ times - no flicker
- [ ] Leave app idle 30 seconds, then rotate - no black flash
- [ ] Test both Light and Dark modes
- [ ] Test Portrait → Landscape and reverse

---

## Common Pitfalls

| Symptom | Cause | Fix |
| --------- | ----- | --- |
| Black flash on first rotation | Missing `windowBackground` | Add hardcoded color in themes.xml |
| Flash after idle | GPU layer reclaimed | Add `LAYER_TYPE_HARDWARE` |
| Delayed rotation response | Activity recreating | Add `configChanges` in manifest |
