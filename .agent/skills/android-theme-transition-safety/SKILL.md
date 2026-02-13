---
name: Android Theme Transition Safety
description: Guidelines for ensuring smooth and race-condition-free theme transitions in Android, specifically regarding icon tinting and state management.
---

# Android Theme Transition Safety

Theme transitions in Android can be tricky due to potential race conditions between the application of new theme values and the UI state during an animation. Follow these guidelines to ensure a "seamless" feel.

## 1. Explicit State Passing

Avoid relying on global state managers (e.g., `settingsManager.isDarkTheme`) within theme update callbacks or animation end actions. During a transition, the manager's state might be updated *after* the UI needs to know the target state.

### Anti-Pattern: Relying on manager state

```kotlin
fun onThemeChanged() {
    // Race condition: settingsManager.isDarkTheme might not be target state yet
    val color = if (settingsManager.isDarkTheme) R.color.dark else R.color.light
    updateIcon(color)
}
```

### Best Practice: Passing explicit state

```kotlin
fun onThemeChanged(isDark: Boolean) {
    // Safe: isDark is the explicitly requested target state
    val color = if (isDark) R.color.dark else R.color.light
    updateIcon(color)
}
```

## 2. Reliable Icon Tinting

Use `setColorFilter` with `PorterDuff.Mode.SRC_IN` for icon tinting. It is more reliable than `imageTintList` across different Android versions and ensures the tint is applied immediately and correctly to the drawable.

### Best Practice: Standard Tinting Pattern

```kotlin
val tintColor = ContextCompat.getColor(context, colorRes)
imageView.setColorFilter(tintColor, android.graphics.PorterDuff.Mode.SRC_IN)
```

## 3. Synchronization with Background Transitions

If the app uses a custom background color transition (e.g., `ColorTransitionController`), perform UI element updates (tints, text colors) **immediately** before or at the start of the background animation. This prevents icons from "popping" or staying in the old theme's color while the background is already fading.

## 4. Resource Redirection

Ensure relevant colors are defined semantically in `colors.xml` (e.g., `light_bulb_rays_dark` vs `light_bulb_rays_light`) and mapped correctly in themes if using `?attr` references. If switching themes manually, use explicit resource selection logic matching the target state.
