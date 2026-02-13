---
name: Android Button Touch Strategy
description: Choose whether the outer container acts as the touch proxy or remains a layout placeholder to control hit targets.
last_verified: 2026-01-23
applicable_sdk: Android 14+ (API 34+)
dependencies:
  - android-button-intent-clarification
  - best-practice-check
---

# Skill: Android Button Touch Strategy (Proxy vs Placeholder)

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** android-button-intent-clarification, best-practice-check

## Purpose

Choose the correct interaction strategy when a visual element sits inside a
larger layout container. This prevents accidental touches and clarifies whether
the outer shell should act as a touch proxy or remain a layout placeholder.

---

## Two Strategies

### Strategy A: Outer Container Is the Touch Proxy

**Use when:** You want a larger touch target for accessibility or ease of use.

Behavior:

- Outer container handles clicks
- Inner visual element only reflects state

Example:

```xml
<FrameLayout
    android:id="@+id/buttonContainer"
    android:layout_width="64dp"
    android:layout_height="64dp"
    android:background="@android:color/transparent">

    <FrameLayout
        android:id="@+id/buttonInner"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="center"
        android:background="@drawable/shape_circle" />
</FrameLayout>
```

```kotlin
binding.buttonContainer.setOnClickListener { /* action */ }
```

---

### Strategy B: Outer Container Is a Layout Placeholder (No Touch)

**Use when:** You want alignment only and must prevent accidental touches in
the transparent area.

Behavior:

- Outer container is non-clickable and non-focusable
- Inner circle handles all click events
- Touch outside the circle should not trigger action

Required XML:

```xml
<FrameLayout
    android:id="@+id/buttonContainer"
    android:layout_width="64dp"
    android:layout_height="64dp"
    android:background="@android:color/transparent"
    android:clickable="false"
    android:focusable="false">

    <FrameLayout
        android:id="@+id/buttonInner"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="center"
        android:background="@drawable/shape_circle"
        android:clickable="true"
        android:focusable="true">

        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_gravity="center" />
    </FrameLayout>
</FrameLayout>
```

Required Kotlin:

```kotlin
binding.buttonInner.setOnClickListener { /* action */ }
binding.buttonContainer.isClickable = false
```

Note: `duplicateParentState="true"` is unnecessary because the outer container
never owns interaction state in this strategy.

---

## Decision Guide

| Goal | Choose |
| --- | --- |
| Prevent mis-taps in transparent area | Strategy B |
| Maximize touch tolerance | Strategy A |
| Outer view is only for alignment | Strategy B |
| Accessibility requires larger hitbox | Strategy A |

---

## Summary (Preferred Wording)

If your core requirement is "prevent touches in the transparent area" or "outer
shell is for layout alignment only":

- XML: outer `clickable="false"`, inner `clickable="true"`
- Kotlin: bind `setOnClickListener` to `binding.buttonInner`
- Interaction: do not use `duplicateParentState="true"`
