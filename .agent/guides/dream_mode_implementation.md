# Implementation Guide: Dream/Screensaver Mode

## Overview

Implement Android DreamService to display flip clock as screensaver when device is charging or docked.

## Architecture

```text
FlipClockDreamService (new)
    ↓ uses
FlipClockView (existing - reuse!)
    ↓ uses
FlipCard, CardRenderer, etc. (existing)
```

**Key Insight**: We can reuse 90% of existing code!

---

## Implementation Steps

### 1. Create Dream Service Class

**File**: `app/src/main/java/com/bokehforu/openflip/dream/FlipClockDreamService.kt` (new)

```kotlin
package com.bokehforu.openflip.dream

import android.service.dreams.DreamService
import android.view.View
import com.bokehforu.openflip.R
import com.bokehforu.openflip.settings.SettingsManager
import com.bokehforu.openflip.view.FlipClockView
import com.bokehforu.openflip.manager.SecondsTicker
import com.bokehforu.openflip.manager.BurnInProtectionManager

class FlipClockDreamService : DreamService() {
    
    private lateinit var flipClockView: FlipClockView
    private lateinit var settingsManager: SettingsManager
    private val secondsTicker = SecondsTicker { updateTime() }
    private lateinit var burnInManager: BurnInProtectionManager
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        
        // Dream configuration
        isInteractive = false  // Non-interactive screensaver
        isFullscreen = true    // Hide status bar
        isScreenBright = false // Dim screen for power saving
        
        // Set layout
        setContentView(R.layout.dream_flip_clock)
        
        // Initialize components
        settingsManager = SettingsManager(this)
        flipClockView = findViewById(R.id.dreamFlipClockView)
        
        // Apply settings
        flipClockView.setDarkTheme(settingsManager.isDarkTheme)
        flipClockView.showSeconds = settingsManager.showSeconds
        flipClockView.showFlaps = true
        
        // Set initial time
        updateTime()
        
        // Start ticker
        lifecycle.addObserver(secondsTicker)
        
        // Enable burn-in protection (important for OLED screens!)
        burnInManager = BurnInProtectionManager(flipClockView, lifecycle)
    }
    
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        // Dream is now visible
    }
    
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        // Clean up if needed
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycle.removeObserver(secondsTicker)
    }
    
    private fun updateTime() {
        val calendar = java.util.Calendar.getInstance()
        val hour = if (settingsManager.is24HourFormat) {
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        } else {
            calendar.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
        }
        val minute = calendar.get(java.util.Calendar.MINUTE)
        
        flipClockView.setTime(hour, minute, animate = true)
    }
}
```

### 2. Create Dream Layout

**File**: `app/src/main/res/layout/dream_flip_clock.xml` (new)

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/black">
    
    <com.bokehforu.openflip.view.FlipClockView
        android:id="@+id/dreamFlipClockView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
</FrameLayout>
```

### 3. Register Service in AndroidManifest.xml

**File**: `app/src/main/AndroidManifest.xml`

Add inside `<application>` tag:

```xml
<!-- Dream/Screensaver Service -->
<service
    android:name=".dream.FlipClockDreamService"
    android:exported="true"
    android:label="@string/dream_name"
    android:permission="android.permission.BIND_DREAM_SERVICE">
    <intent-filter>
        <action android:name="android.service.dreams.DreamService" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    
    <!-- Optional: Preview/Settings activity -->
    <meta-data
        android:name="android.service.dream"
        android:resource="@xml/dream_info" />
</service>
```

### 4. Add Dream Info (Optional but Recommended)

**File**: `app/src/main/res/xml/dream_info.xml` (new)

```xml
<?xml version="1.0" encoding="utf-8"?>
<dream xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="com.bokehforu.openflip.ui.MainActivity" />
```

### 5. Add String Resources

**File**: `app/src/main/res/values/strings.xml`

```xml
<!-- Dream/Screensaver -->
<string name="dream_name">Flip Clock</string>
```

---

## Testing

### Enable Dream Mode

1. Go to **Settings → Display → Screen saver**
2. Select **"Flip Clock"**
3. Tap **"Preview"** to test immediately
4. Or plug in charger to activate automatically

### Test Checklist

- [ ] Dream activates when charging
- [ ] Time updates every minute
- [ ] Theme matches app settings
- [ ] Burn-in protection shifts position
- [ ] Dream exits cleanly when screen touched/unlocked

---

## Advanced Features (Optional)

### Add Dream Settings

Create a dedicated settings screen for dream-specific options:

- Brightness level
- Enable/disable burn-in protection
- Override theme for dream mode

### Interactive Dream

Change `isInteractive = true` to allow:

- Tap to show seconds
- Swipe to change theme
- Long-press to open settings

---

## Estimated Time

**2-3 hours** (including testing)

## Benefits

- ✅ Reuses existing `FlipClockView` (no duplication)
- ✅ Respects user settings (theme, time format)
- ✅ Includes burn-in protection for OLED screens
- ✅ Native Android API (good ROM compatibility)
- ✅ Classic use case for flip clocks
