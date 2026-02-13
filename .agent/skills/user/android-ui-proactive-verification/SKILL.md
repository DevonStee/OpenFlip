---
name: Android UI Proactive Verification
description: Automatically verify UI changes using ADB, screenshots, and interaction simulation without waiting for user requests.
---

# Skill: Android UI Proactive Verification

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** None

## Objective

To ensure every UI-related code change is verified on a real device or emulator immediately after implementation, providing visual proof and self-correction.

## When to Trigger

As an agent, you MUST trigger this verification flow automatically after any of the following:

1. **Layout Changes**: Modifying XML layout files or programmatic view creation.
2. **Visual Styling**: Changing colors, dimensions, themes, or drawables.
3. **Animation Logic**: Modifying `AnimationController` or any animation-related code.
4. **Interaction Logic**: Adding or fixing click listeners, touch handling, or gesture routing.
5. **Critical Fixes**: Any fix related to visual glitches or UI freezes.

## Verification Protocol

### 1. Auto-Deploy and Start

Immediately run the deployment workflow to get the latest code onto the device.

```bash
./gradlew installDebug && \
adb shell am start -n com.bokehforu.openflip.debug/com.bokehforu.openflip.ui.FullscreenClockActivity
```

### 2. Interaction Simulation (If Applicable)

If the change involves a button, menu, or gesture:

- Use `adb shell uiautomator dump /sdcard/view.xml && adb pull /sdcard/view.xml .` to find view coordinates if needed.
- Use `adb shell input tap <x> <y>` to simulate clicks.
- Use `adb shell input swipe <x1> <y1> <x2> <y2> <duration>` for gestures.

### 3. Visual Capture

Capture screenshots or videos to verify the state.

- **Screenshot**: `adb shell screencap -p /sdcard/verify.png && adb pull /sdcard/verify.png ./ui_verify_$(date +%s).png`
- **Video (For Animations)**: `adb shell screenrecord --time-limit 5 /sdcard/verify.mp4 && adb pull /sdcard/verify.mp4 ./ui_verify_$(date +%s).mp4`

### 4. Self-Correction & Verification

- **Analyze the Output**: Look at the screenshot/video. Does it match the design intent? Are there any unexpected layout shifts or artifacts?
- **Iterate**: If the UI is wrong, apply fixes and repeat the verification flow immediately.
- **Reporting**: Inform the user of the result, providing the filename of the successful verification.

### 5. Cleanup

- Once you are confident the UI is correct and the user acknowledges the result, delete the local `ui_verify_*.png` and `ui_verify_*.mp4` files.
- Always delete temporary files on the device: `adb shell rm /sdcard/verify.png /sdcard/verify.mp4`.

## Best Practices

- **Wait for Rendering**: Always include a `sleep 2` before capturing a screenshot to allow for animations and rendering to settle.
- **Orientation Testing**: If the app supports rotation, test both Portrait and Landscape using `adb shell content insert --uri content://settings/system --bind name:s:user_rotation --bind value:i:0` (0 for portrait, 1 for landscape).
- **Proactive Thinking**: Don't wait for the user to ask "how does it look?". Show them.
