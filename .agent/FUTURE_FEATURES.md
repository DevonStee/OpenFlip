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

_(No planned features at this time)_
