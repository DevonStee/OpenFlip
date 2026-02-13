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

## ✅ Features to Implement

### 1. Dream/Screensaver Mode ⭐⭐⭐

**Priority**: High
**Difficulty**: Medium
**User Demand**: High

#### What is Dream Mode?

Android's DreamService (screensaver) that activates when:

- Device is charging
- Device is docked
- User manually enables in "Settings → Display → Screen saver"

#### Why Important?

- Classic use case for flip clocks (desk clock while charging)
- Native Android API with good ROM compatibility
- Users expect this feature in clock apps

#### Implementation Overview

1. Create `FlipClockDreamService` extending `DreamService`
2. Reuse existing `FlipClockView` component
3. Register service in `AndroidManifest.xml`
4. Add dream settings preview

**Estimated Time**: 2-3 hours

---

### 2. App Shortcuts ⭐

**Priority**: Medium
**Difficulty**: Very Low
**User Demand**: Medium

#### What are App Shortcuts?

Quick actions shown when long-pressing the app icon:

- "Open with Dark Theme"
- "Open with Light Theme"
- "Open Settings"

#### Why Implement?

- 5-minute implementation
- Improves perceived app quality
- Enhances user experience with quick access

#### App Shortcuts Implementation Overview

1. Create `res/xml/shortcuts.xml`
2. Define static shortcuts with icons and intents
3. Handle shortcut intents in `MainActivity`

**Estimated Time**: 15-30 minutes

---

## Implementation Priority

1. **App Shortcuts** (Quick win - do first)
2. **Dream/Screensaver Mode** (High value feature)
