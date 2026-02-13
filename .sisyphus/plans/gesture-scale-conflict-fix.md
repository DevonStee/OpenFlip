# Plan: Fix pinch vs brightness swipe conflicts and scale overshoot

## TL;DR
> Resolve gesture conflicts by gating swipe-to-dim to single-finger scrolls and reserving two-finger input for pinch zoom. Clamp and reapply scale consistently on menu/show overlays to prevent oversize.

## Context
- Current issues:
  1) Two-finger pinch sometimes triggers vertical swipe-to-dim (conflict).
  2) After scaling near limit, opening the menu shows the flip card oversized (scale clamp not re-applied on layout/menu toggle).

## Goals
- Single-finger vertical swipe adjusts brightness; two-finger gestures solely perform pinch zoom.
- Scale remains clamped to min/max across layout passes and menu toggles; no oversize when UI shows.

## Tasks

1) Gesture disambiguation (pinch vs swipe-to-dim)
   - Enforce pointer-count gating: only handle swipe-to-dim when `pointerCount == 1` (or ACTION_DOWN pointerCount==1) and not during ScaleGestureDetector in-progress.
   - Ensure ScaleGestureDetector consumes two-finger events before scroll; do not mark activeGesture TAP/SWIPE when pointerCount > 1.
   - Acceptance: Two-finger pinch never changes brightness; single-finger vertical swipe still adjusts brightness.

2) Scale clamp consistency across layout/menu
   - Clamp scale on every restore/apply path: restoreScale, setDimensions/onSizeChanged, menu toggle callbacks that re-layout the clock.
   - After menu/overlay show, reapply clamped scale to geometry (cardSpacing, maxCardW/H) and invalidate.
   - Acceptance: Scaling to max then opening menu does NOT grow further; value stays within configured min/max.

3) QA scenarios (adb/manual)
   - Pinch-only: two-finger pinch → clock scales; brightness unchanged.
   - Swipe-only: single-finger vertical swipe → brightness changes; scale unchanged.
   - Mixed: pinch then swipe → both work; no flicker/flash.
   - Scale clamp: pinch to max, open menu (tap) → size remains within bounds.

## Acceptance Criteria
- Two-finger gestures never trigger brightness changes.
- Single-finger swipe still adjusts brightness.
- Scale never exceeds min/max after menu open/close or layout changes.

## References
- `GestureRouter`: swipe-to-dim handling, ScaleGestureDetector usage.
- `FullscreenFlipClockView`: `applyScale`, `restoreScale`, `setDimensions` (geometry updates).
- Layout/menu: `UIStateController` and Activity tap toggle.
