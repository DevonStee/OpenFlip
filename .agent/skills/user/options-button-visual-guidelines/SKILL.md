---
name: options-button-visual-guidelines
description: Visual spec for the “options” settings button (three-line icon). Use when adjusting its size, background, icon spacing, or nearby guide lines in both portrait and landscape layouts.
---

# Options Button Visual Guidelines

Use this spec whenever you edit the options/settings button visuals or nearby guide lines (portrait `activity_main.xml`, landscape `layout-land/activity_main.xml`, and `layout_button_settings.xml`).

## Core Visuals
- Outer frame: 56dp × 56dp (`@dimen/settingsButtonSize`).
- Background: inset 2dp inside frame; rounded corners 15.5dp; color comes from theme (`backgroundColor` in `SettingsButtons.kt`).
- Icon: three horizontal lines, width 16dp, height 2dp each; vertical gap 5dp. No scale change during animation. Tint ~85% alpha of content color.

## Animations (Compose `SettingsButtons.kt`)
- Press/appear: optional downward nudge only; keep alpha visible (no fade-out). If re-enabling legacy fade, ensure icon never goes fully invisible unless desired.
- Keep default scale (no press shrink) unless explicitly requested.

## Layout & Guides
- Portrait (`activity_main.xml`): `settingsDivider` bottom margin 0dp. `settingsModulesContainer` paddingBottom 5dp (default). Options include width/height 56dp.
- Landscape (`layout-land/activity_main.xml`): `settingsButtonInclude` width/height 56dp. Dividers may be adjusted separately if needed.
- Internal spines (`layout_button_settings.xml`): `spineTopSettings` and `spineBottomSettings` height 1dp, no extra margin; leave `visibility="gone"` unless explicitly shown.

## When adjusting gaps/lines
- If lines must “touch” the button: tweak only margins on the surrounding divider or spine. Start with ±1dp increments; avoid changing button size.
- Prefer moving guide lines, not changing the Compose button inset/size.

## Verification Checklist
- [ ] Options button frame remains 56dp with 2dp inset and 15.5dp corner radius.
- [ ] Icon lines stay 16×2dp with 5dp gap; remain visible during animation.
- [ ] Portrait top divider sits at 0dp margin above modules.
- [ ] Inner spines are 1dp and align visually with the outer divider (no overshoot past button edge).

## Files to touch
- `app/src/main/java/com/bokehforu/openflip/ui/compose/SettingsButtons.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout-land/activity_main.xml`
- `app/src/main/res/layout/layout_button_settings.xml`

