# Regression Checklist

## Smoke Paths
- App launch to main clock.
- Minute/hour flip animation continuity.
- Knob interaction and haptic feedback.
- Theme switch (dark/light) with transition.
- Sleep timer start/stop/countdown.
- Hourly chime toggle and test chime.

## Settings Paths
- Settings sheet first open height is stable (portrait + landscape).
- Settings main/subpage switch does not resize or jump sheet height.
- Time format updates clock output.
- Show seconds toggle behavior.
- Show flaps toggle behavior.
- Haptic/sound toggle behavior.
- Orientation mode switch.
- Wake lock mode switch.
- Time format / orientation open subpages (not inline cycling).
- Header title keeps original uppercase style and spacing.

## Visual Integrity
- Landscape dark theme: options-button spine segments do not overlap (no double-dark line).
- Landscape dark theme: options-button short spines touch button edge consistently.
- Landscape light theme: light/options spine behavior matches dark-theme geometry.

## Gesture / Dim Hint
- Vertical dim hint first show latency is acceptable after 70ms timing update.
- Min/max hint icon switching responsiveness is acceptable after 70ms timing update.

## Build Gates
- `./gradlew :app:compileDebugKotlin`
- `./gradlew test`
- `./gradlew lint`
- `./gradlew installDebug`
