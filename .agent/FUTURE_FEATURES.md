# Future Features & Decisions

## ❌ Features NOT to Implement

### Always-On Display (AOD)

**Reason**: ROM adaptation hell

- No unified Android API for AOD
- Each manufacturer (MIUI, OneUI, ColorOS, etc.) has different implementation
- Requires special system permissions that may be rejected
- High development cost with low success rate across devices

### Wear OS Version

**Reason**: Low ROI (Return on Investment)

- Requires separate Wear OS module
- UI needs complete redesign for circular screens
- Small user base
- High maintenance cost for limited benefit

---

## ✅ Implemented Features

### Dream/Screensaver Mode ✅

**Status**: Implemented in `ScreensaverClockService.kt` (`:app` module)

- Activates when device is charging/docked or user enables in system settings
- Reuses existing flip clock view component

### App Shortcuts ✅

**Status**: Implemented in `res/xml/shortcuts.xml`

- "Open with Dark Theme"
- "Open with Light Theme"
- "Open Settings"

---

## 💡 Future Ideas

### Settings Bottom Buttons UX/UI Refactoring

**Status**: Planned

Currently, the four buttons at the bottom of the Settings UI (`light`, `tuning`, `invert`, `options`) have inconsistent designs. Future refactoring should address the following visual consistency vectors:

1. **Unify Shapes (Visual Weight)**: Change the `options` button's background shape from a rounded rectangle (squircle) to a perfect circle to match the baseline of the other three buttons.
2. **Unify Design Language (Flat vs. Skeuomorphic)**: Remove the skeuomorphic (realistic 3D) design from the `tuning` button. Replace it with a flat design (e.g., a simple gear, dial, or stroke icon with a solid circular background) to match the minimalist, non-distracting aesthetic of the flip clock.
3. **Migrate `light` Icon to Compose Canvas/Vector**: Replace the static XML vector drawable of the `light` button with pure Compose `Canvas` drawing or `ImageVector`. This programmatic approach unlocks the potential for deep micro-animations. Tying the geometry (like the radius of the center circle or the length/rotation of the outer rays) directly to user slider gestures will provide significantly better tactile feedback when adjusting brightness.
