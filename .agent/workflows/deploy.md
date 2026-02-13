---
description: Build, deploy, and verify app functionality with automated UI testing
---

# Deploy & Verify Workflow

## Quick Deploy (Basic Verification)

1. Build and Install Debug APK
// turbo
./gradlew installDebug

2. Open the app on the connected device
// turbo
adb shell am start -n com.bokehforu.openflip.debug/com.bokehforu.openflip.ui.FullscreenClockActivity

3. Wait 2 seconds for UI to render and capture screenshot
// turbo
sleep 2 && adb shell screencap -p /sdcard/ui_verify.png && adb pull /sdcard/ui_verify.png ./ui_verify_$(date +%Y%m%d_%H%M%S).png

4. Cleanup screenshot on device
// turbo
adb shell rm /sdcard/ui_verify.png

---

## Full UI Testing (Interactive Verification)

### Prerequisites

```bash
# Verify device connection
adb devices

# Install and start app (if not already done)
./gradlew installDebug && adb shell am start -n com.bokehforu.openflip.debug/com.bokehforu.openflip.ui.FullscreenClockActivity
```

### Test Scenarios

#### 1. Open Settings Bottom Sheet

```bash
# Get screen size for tap coordinates
adb shell wm size

# Tap settings gear icon (adjust coordinates based on screen size)
# Example for 1080x2400 screen: gear is typically at (60, 150)
adb shell input tap 60 150

# Wait for animation
sleep 1

# Capture settings screen
adb shell screencap -p /sdcard/settings_open.png && adb pull /sdcard/settings_open.png ./verify_settings_$(date +%Y%m%d_%H%M%S).png
```

#### 2. Test Theme Toggle

```bash
# Toggle theme switch (coordinates vary by device)
# Example: theme switch at (540, 800)
adb shell input tap 540 800

# Wait for theme transition
sleep 0.5

# Capture theme change
adb shell screencap -p /sdcard/theme_toggle.png && adb pull /sdcard/theme_toggle.png ./verify_theme_$(date +%Y%m%d_%H%M%S).png
```

#### 3. Test OLED Protection Toggle

```bash
# Scroll down to OLED section if needed
adb shell input swipe 540 1200 540 600 300

# Toggle OLED protection switch
adb shell input tap 540 1000

# Capture state
adb shell screencap -p /sdcard/oled_toggle.png && adb pull /sdcard/oled_toggle.png ./verify_oled_$(date +%Y%m%d_%H%M%S).png
```

#### 4. Close Settings (Back Button)

```bash
# Press back button
adb shell input keyevent KEYCODE_BACK

# Wait for animation
sleep 0.5

# Capture main screen restored
adb shell screencap -p /sdcard/settings_closed.png && adb pull /sdcard/settings_closed.png ./verify_back_$(date +%Y%m%d_%H%M%S).png
```

#### 5. Test Screen Rotation

```bash
# Force landscape orientation
adb shell settings put system user_rotation 1

# Wait for rotation animation
sleep 2

# Capture landscape mode
adb shell screencap -p /sdcard/landscape.png && adb pull /sdcard/landscape.png ./verify_landscape_$(date +%Y%m%d_%H%M%S).png

# Restore portrait orientation
adb shell settings put system user_rotation 0

# Capture portrait mode
sleep 2 && adb shell screencap -p /sdcard/portrait.png && adb pull /sdcard/portrait.png ./verify_portrait_$(date +%Y%m%d_%H%M%S).png
```

#### 6. Find UI Element Coordinates (Helper)

```bash
# Enable pointer location overlay
adb shell settings put system pointer_location 1

# Now tap on device to see coordinates in the overlay
# Disable when done:
adb shell settings put system pointer_location 0
```

### Cleanup All Screenshots

```bash
adb shell rm /sdcard/*.png
```

---

## Widget Testing

### Add Widget to Home Screen

```bash
# Open widget picker (requires manual interaction, but you can automate home button)
adb shell input keyevent KEYCODE_HOME
adb shell input keyevent KEYCODE_APP_SWITCH

# Manual: Long-press home screen → Widgets → OpenFlip
# Then capture result:
sleep 2 && adb shell screencap -p /sdcard/widget_added.png && adb pull /sdcard/widget_added.png ./verify_widget_$(date +%Y%m%d_%H%M%S).png
```

### Click Widget to Open App

```bash
# Tap widget center (adjust coordinates to widget location)
adb shell input tap 540 1200

# Verify app opened
sleep 1 && adb shell screencap -p /sdcard/widget_click.png && adb pull /sdcard/widget_click.png ./verify_widget_click_$(date +%Y%m%d_%H%M%S).png
```

---

## Automated Test Script

For repeated testing, save this as `test_ui.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 Starting Full UI Test Suite..."

# 1. Build and Install
echo "📦 Building and installing..."
./gradlew installDebug

# 2. Launch app
echo "▶️  Launching app..."
adb shell am start -n com.bokehforu.openflip.debug/com.bokehforu.openflip.ui.FullscreenClockActivity
sleep 2

# 3. Initial screenshot
echo "📸 Capturing initial state..."
adb shell screencap -p /sdcard/test_01_initial.png
adb pull /sdcard/test_01_initial.png ./

# 4. Open settings
echo "⚙️  Opening settings..."
adb shell input tap 60 150
sleep 1
adb shell screencap -p /sdcard/test_02_settings.png
adb pull /sdcard/test_02_settings.png ./

# 5. Toggle theme
echo "🎨 Testing theme toggle..."
adb shell input tap 540 800
sleep 0.5
adb shell screencap -p /sdcard/test_03_theme.png
adb pull /sdcard/test_03_theme.png ./

# 6. Close settings
echo "🔙 Closing settings..."
adb shell input keyevent KEYCODE_BACK
sleep 1
adb shell screencap -p /sdcard/test_04_closed.png
adb pull /sdcard/test_04_closed.png ./

# 7. Test rotation
echo "🔄 Testing rotation..."
adb shell settings put system user_rotation 1
sleep 2
adb shell screencap -p /sdcard/test_05_landscape.png
adb pull /sdcard/test_05_landscape.png ./

adb shell settings put system user_rotation 0
sleep 2
adb shell screencap -p /sdcard/test_06_portrait.png
adb pull /sdcard/test_06_portrait.png ./

# 8. Cleanup
echo "🧹 Cleaning up..."
adb shell rm /sdcard/test_*.png

echo "✅ Test suite complete! Check test_*.png files."
```

Make executable: `chmod +x test_ui.sh`

---

## Troubleshooting

### Find Package Name

```bash
adb shell pm list packages | grep openflip
```

### Check Current Activity

```bash
adb shell dumpsys window | grep mCurrentFocus
```

### View Logcat During Testing

```bash
adb logcat | grep OpenFlip
```

### Clear App Data

```bash
adb shell pm clear com.bokehforu.openflip.debug
```
