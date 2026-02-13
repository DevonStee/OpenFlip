# Draft: Fix tap interaction showing 4 buttons

## Requirements (confirmed)
- Single tap on the main screen should toggle the interaction UI (4 buttons) into view.
- Current behavior: tapping does nothing (buttons stay hidden).
- New issues: (1) Pinch (two-finger) zoom conflicts with vertical swipe-to-dim; (2) After scaling near limit, opening menu shows oversized clock.

## Technical Decisions
- Investigate gesture routing first (GestureRouter, dispatchTouchEvent/onTouchEvent in Activity).
- Verify view-level click handling (FullscreenFlipClockView should not consume taps; overlay views should be non-clickable when hidden).
- Check UI state toggling path: GestureRouter.onSingleTapUp → Activity.toggleInteractionState → viewModel.isInteracting → UIStateController updates visibility.

## Research Findings
- GestureRouter currently handles onSingleTapUp and directly calls onToggleInteraction; activeGesture state no longer blocks tap.
- Activity dispatchTouchEvent was removed; onTouchEvent delegates to GestureRouter unless InfiniteKnob is interacting.
- FullscreenFlipClockView has isClickable removed; should not intercept.
- UIStateController shows buttons based on viewModel.isInteracting.

## Open Questions
- Is any overlay (e.g., swipeHintIcon or dialogs) intercepting touches at startup?
- Does viewModel.isInteracting default to false and never updated due to SavedStateHandle persistence?
- Are pinch and swipe sharing the same gesture stream without pointer-count gating?
- Is scale clamping applied after layout recomputation (onSizeChanged) when reopening menu?

## Scope Boundaries
- INCLUDE: Input handling (GestureRouter, Activity touch dispatch), interaction state toggle (viewModel/UIStateController), hidden overlays blocking taps.
- EXCLUDE: Visual styling of buttons, animations beyond visibility toggle.
- INCLUDE (new): Gesture priority between pinch vs swipe-to-dim, scale clamping on reopen/menu toggle.
