---
name: Android Widget Development
description: Best practices for AppWidget development with RemoteViews, time display, and known limitations
last_verified: 2026-01-23
applicable_sdk: Android 8+ (API 26+)
---

# Android Widget Development

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 8+ (API 26+), tested through Android 15 (API 35)
**Project Context:** OpenFlip uses 5 widget styles (Classic, Glass, Solid, Split, White)

This skill covers AppWidget (home screen widget) development, including time synchronization, RemoteViews limitations, and proven architectural patterns.

---

## Prerequisites

**Related Skills:**

- [android-highperf-customview](../android-highperf-customview/SKILL.md) - Rendering optimization principles
- [codebase-aware-implementation](../codebase-aware-implementation/SKILL.md) - For adding new widget styles

---

## Android 15+ Considerations (API 35)

**New Features:**

- Improved widget preview generation
- Enhanced widget configuration flow
- Better widget update scheduling

**Known Limitations (Still Apply)**:

- RemoteViews view hierarchy limits (unchanged)
- TextClock remains the best solution for time display
- Bitmap rendering antialiasing issues persist

---

## When to Use This Skill

- Building home screen widgets
- Displaying real-time information (clocks, weather, etc.)
- Need to understand RemoteViews constraints

---

## Time Display: Use TextClock

For widgets showing time, **always** use `TextClock` instead of custom solutions:

```xml
<TextClock
    android:id="@+id/clock_hours"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:format12Hour="h"
    android:format24Hour="HH"
    android:timeZone="@null" />
```

### Why TextClock?

| Approach | Result |
| ---------- | -------- |
| Bitmap screenshot stream | ❌ Background execution limits cause freezing |
| AlarmManager periodic updates | ❌ Inaccurate, delays from seconds to minutes |
| TextClock (system native) | ✅ System-level precision, zero battery drain |

---

## Widget Provider Base Class Pattern

Create an abstract base class to reduce duplication:

```kotlin
abstract class BaseWidget : AppWidgetProvider() {
    
    abstract val layoutId: Int
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }
    
    protected open fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, layoutId)
        setupClickHandlers(context, views)
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    private fun setupClickHandlers(context: Context, views: RemoteViews) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
    }
}
```

---

## RemoteViews Limitations

> [!CAUTION]
> RemoteViews has strict view hierarchy limits. Complex overlays may cause "Problem loading widget" errors.

### Known Limitations

1. **Limited View Types**: Only specific Views are supported (TextView, ImageView, LinearLayout, etc.)
2. **No Custom Views**: Cannot use custom View classes
3. **Hierarchy Depth**: Deep nesting may cause loading failures
4. **No Path Operations**: Cannot dynamically modify drawable shapes

---

### Anti-Aliasing Edge Problem

**Problem**: Split-flap style widgets may show white anti-aliasing edges at seams.

**Attempted Fix**: Overlay a thin View to cover the seam → **FAILED**

**Reason**: Adding overlay views may exceed RemoteViews complexity limits.

**Decision**: Accept the visual artifact. Prioritize widget reliability over pixel-perfect rendering.

---

## Split-Flap Visual Effect

To create a split-flap clock effect in widgets:

```xml
<!-- Container clips the oversized TextClock -->
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="59dp"
    android:clipChildren="true"
    android:clipToPadding="true">
    
    <!-- TextClock is taller than container -->
    <TextClock
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:layout_gravity="top|center_horizontal"
        android:gravity="center" />
</FrameLayout>
```

The top half shows upper portion of digits; a separate container shows the bottom half.

---

## Widget Configuration

In `res/xml/widget_info.xml`:

```xml
<appwidget-provider
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:initialLayout="@layout/widget_layout"
    android:previewImage="@drawable/widget_preview"
    android:updatePeriodMillis="0" />
```

**Note**: `updatePeriodMillis="0"` when using TextClock (no manual updates needed).

---

## Back Stack Handling

Prevent Activity duplication when launching from widget:

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
            Intent.FLAG_ACTIVITY_CLEAR_TOP
}
```

---

## Verification Checklist

- [ ] Widget loads without "Problem loading" error
- [ ] Time updates correctly (minute changes)
- [ ] Tapping widget opens app without duplicate Activities
- [ ] Widget survives device reboot
- [ ] Widget displays correctly across different DPI screens
