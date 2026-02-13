# Implementation Guide: App Shortcuts

## Overview

Add quick actions when user long-presses the app icon.

## Implementation Steps

### 1. Create Shortcuts Definition

**File**: `app/src/main/res/xml/shortcuts.xml` (new file)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Dark Theme Shortcut -->
    <shortcut
        android:shortcutId="open_dark_theme"
        android:enabled="true"
        android:icon="@drawable/icon_dark_theme"
        android:shortcutShortLabel="@string/shortcut_dark_theme"
        android:shortcutLongLabel="@string/shortcut_dark_theme_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.bokehforu.openflip"
            android:targetClass="com.bokehforu.openflip.ui.MainActivity">
            <extra android:name="theme" android:value="dark" />
        </intent>
    </shortcut>
    
    <!-- Light Theme Shortcut -->
    <shortcut
        android:shortcutId="open_light_theme"
        android:enabled="true"
        android:icon="@drawable/icon_light_theme"
        android:shortcutShortLabel="@string/shortcut_light_theme"
        android:shortcutLongLabel="@string/shortcut_light_theme_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.bokehforu.openflip"
            android:targetClass="com.bokehforu.openflip.ui.MainActivity">
            <extra android:name="theme" android:value="light" />
        </intent>
    </shortcut>
    
    <!-- Settings Shortcut -->
    <shortcut
        android:shortcutId="open_settings"
        android:enabled="true"
        android:icon="@drawable/icon_settings"
        android:shortcutShortLabel="@string/shortcut_settings"
        android:shortcutLongLabel="@string/shortcut_settings_long">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.bokehforu.openflip"
            android:targetClass="com.bokehforu.openflip.ui.MainActivity">
            <extra android:name="open_settings" android:value="true" />
        </intent>
    </shortcut>
    
</shortcuts>
```

### 2. Add String Resources

**File**: `app/src/main/res/values/strings.xml`

```xml
<!-- App Shortcuts -->
<string name="shortcut_dark_theme">Dark Theme</string>
<string name="shortcut_dark_theme_long">Open with Dark Theme</string>
<string name="shortcut_light_theme">Light Theme</string>
<string name="shortcut_light_theme_long">Open with Light Theme</string>
<string name="shortcut_settings">Settings</string>
<string name="shortcut_settings_long">Open Settings</string>
```

### 3. Register in AndroidManifest.xml

**File**: `app/src/main/AndroidManifest.xml`

Add inside `<activity android:name=".ui.MainActivity">`:

```xml
<meta-data
    android:name="android.app.shortcuts"
    android:resource="@xml/shortcuts" />
```

### 4. Handle Shortcuts in MainActivity

**File**: `app/src/main/java/com/bokehforu/openflip/ui/MainActivity.kt`

Add in `onCreate()` after `setContentView()`:

```kotlin
// Handle app shortcuts
intent?.let { handleShortcutIntent(it) }

private fun handleShortcutIntent(intent: Intent) {
    when {
        intent.hasExtra("theme") -> {
            val theme = intent.getStringExtra("theme")
            val isDark = theme == "dark"
            settingsManager.isDarkTheme = isDark
            onThemeChanged(isDark)
        }
        intent.getBooleanExtra("open_settings", false) -> {
            // Open settings bottom sheet
            SettingsBottomSheet().show(supportFragmentManager, TAG_SETTINGS)
        }
    }
}
```

### 5. Create Shortcut Icons

Create 3 simple icons (24x24dp) in `res/drawable/`:

- `icon_dark_theme.xml` - Moon icon
- `icon_light_theme.xml` - Sun icon  
- `icon_settings.xml` - Gear icon (reuse existing if available)

## Testing

1. Build and install app
2. Long-press app icon on launcher
3. Verify 3 shortcuts appear
4. Tap each shortcut to verify behavior

## Estimated Time

15-30 minutes
