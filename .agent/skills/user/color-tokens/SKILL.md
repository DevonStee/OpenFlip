---
name: Color Tokens
description: Reference for project color tokens to avoid hardcoded hex values and keep themes consistent.
last_verified: 2026-01-23
applicable_sdk: Android 8+ (API 26+)
---

# Color Tokens Skill

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 8+ (API 26+)
**Implementation Files:**
- `app/src/main/res/values/colors.xml` - Color definitions
- `app/src/main/res/values/themes.xml` - Theme attributes

This skill provides a comprehensive list of color tokens used in the project, following the snake_case naming convention. Use these tokens instead of hardcoded hex values to maintain UI consistency and support theme switching.

---

## Prerequisites

**Related Skills:**
- [android-rotation-antiflicker](../android-rotation-antiflicker/SKILL.md) - Uses these colors for windowBackground
- [best-practice-check](../best-practice-check/SKILL.md) - Validates color usage

---

## Naming Convention

- **Style**: snake_case (e.g., `app_background_dark`, `shadow_light`)
- **Suffixes**: `_dark`, `_light`, `_on`, `_off`, `_pressed`, `_selected` are used to indicate state or theme.

## Color Token List

### Core Colors

- `black`: #FF000000
- `white`: #FFFFFFFF

### Dark Theme Colors

- `app_background_dark`: Main app background
- `card_background_dark`: Background for clock cards
- `card_text_dark`: Text color on clock cards
- `settings_background_dark`: Background for settings menu
- `settings_text_primary_dark`: Main text in settings
- `settings_text_secondary_dark`: Secondary/hint text in settings
- `settings_section_dark`: Section headers in settings
- `settings_icon_dark`: Icons in settings

### Light Theme Colors

- `card_background_light`: Background for clock cards
- `card_text_light`: Text color on clock cards
- `settings_background_light`: Background for settings menu
- `settings_text_primary_light`: Main text in settings
- `settings_text_secondary_light`: Secondary/hint text in settings
- `settings_section_light`: Section headers in settings
- `settings_icon_light`: Icons in settings

### Shadow & Elevation

- `shadow_light`: Generic light shadow
- `card_shadow_fill_dark`: Shadow fill for dark mode cards
- `card_shadow_fill_light`: Shadow fill for light mode cards
- `knob_shadow_light`: Shadow for knob in light mode
- `knob_shadow_dark`: Shadow for knob in dark mode
- `glass_cover_inner_shadow`: Generic inner shadow for glass covers
- `glass_cover_inner_shadow_light`: Inner shadow for light mode glass covers
- `glass_cover_inner_shadow_dark`: Inner shadow for dark mode glass covers
- `frosted_pill_inner_shadow_light`: Inner shadow for frosted pill in light mode

### Interactive Elements

- `button_background_interactive_dark`: Hover/pressed state background for dark buttons
- `ripple_color_white`: White ripple effect (for dark backgrounds)
- `ripple_color_black`: Black ripple effect (for light backgrounds)
- `button_background_settings_dark`: Background for settings button (dark)
- `button_background_settings_light`: Background for settings button (light)
- `button_group_pill_background_dark`: Persistent pill container background (dark)
- `button_group_pill_background_light`: Persistent pill container background (light)
- `button_group_pill_background`: Themed pill container background
- `frosted_pill_bg_light`: Background for frosted pill in light mode
- `frosted_pill_border_light`: Border for frosted pill in light mode
- `pressed_overlay_color`: Overlay for pressed states
- `knob_pressed_overlay`: Overlay for pressed knob

### Controls (Radio/Toggle)

- `control_default_dark`: Default state for radio/controls (dark)
- `control_default_light`: Default state for radio/controls (light)
- `control_selected_dark`: Selected state for radio/controls (dark)
- `control_selected_light`: Selected state for radio/controls (light)
- `control_pressed_dark`: Pressed state for radio/controls (dark)
- `control_pressed_light`: Pressed state for radio/controls (light)
- `toggle_bg_on`: Toggle background when ON
- `toggle_bg_off`: Toggle background when OFF
- `toggle_stroke_on`: Toggle border when ON
- `toggle_stroke_off`: Toggle border when OFF
- `toggle_glow_on`: Glow effect when toggle is ON
- `toggle_glow_off`: No glow when toggle is OFF
- `toggle_icon_on`: Icon color when toggle is ON
- `toggle_icon_on_pressed`: Icon color when toggle is ON and pressed
- `toggle_icon_off`: Icon color when toggle is OFF
- `state_toggle_off`: Base color for toggle in OFF state
- `state_toggle_on`: Base color for toggle in ON state

### Action & Status

- `action_red`: Standard red for actions (e.g., reset, delete)
- `on_action_red`: Text/icon color on top of action red
- `action_red_transparent`: Transparent version of action red
- `text_tertiary_red`: Tertiary text in red
- `glow_on`: Neon/active glow color
- `glow_off_dark`: Inactive glow color (dark)
- `glow_off_light`: Inactive glow color (light)
- `light_button_glow_inner`: Inner glow for light button
- `light_button_glow_outer`: Outer glow for light button
- `glass_cover_halo`: Halo effect for active glass cover
- `glass_cover_bezel_lit`: Lit state for glass cover bezel
- `rim_light_color`: Rim light effect

### Specific Component Colors

#### Knob View

- `knob_background`: Main knob color
- `knob_tick_unselected`: Tick color when not selected
- `knob_tick_selected`: Tick color when selected
- `knob_indicator`: Main indicator color
- `color_knob_base_light`: Base color for knob in light mode
- `color_knob_base_dark`: Base color for knob in dark mode
- `knob_tick_normal`: Standard tick color
- `knob_tick_light`: Light tick color
- `knob_tick_active`: Active tick color
- `knob_tick_active_light`: Active tick color (light)
- `knob_tick_highlight`: Highlight on tick
- `knob_indicator_normal`: Normal indicator state
- `knob_indicator_active`: Active indicator state
- `knob_bevel_light`: Bevel highlight
- `knob_bevel_shadow`: Bevel shadow
- `knob_border_dark`: Knob border (dark)
- `knob_glow_light`: Knob glow (light)
- `knob_glow_dark`: Knob glow (dark)
- `knob_indicator_shadow`: Shadow under knob indicator
- `knob_indicator_red_bright`: Bright red for indicator
- `knob_indicator_red_deep`: Deep red for indicator
- `knob_indicator_green_bright`: Bright green for indicator
- `knob_indicator_green_deep`: Deep green for indicator
- `knob_indicator_highlight`: Highlight on indicator

#### Glass Cover

- `glass_cover_lens_off_light`: Lens color when OFF (light)
- `glass_cover_lens_off_dark`: Lens color when OFF (dark)
- `glass_cover_lens_on_edge`: Lens edge color when ON
- `glass_cover_bezel_off_light`: Bezel color when OFF (light)
- `glass_cover_bezel_off_dark`: Bezel color when OFF (dark)
- `glass_cover_bezel_on_light`: Bezel color when ON (light)
- `glass_cover_bezel_on_dark`: Bezel color when ON (dark)
- `glass_cover_spill_white`: Light spill effect
- `glass_cover_core_center`: Center of the lit core
- `glass_cover_core_edge`: Edge of the lit core

#### Timer

- `timer_track_light`: Track color in light mode
- `timer_track_dark`: Track color in dark mode
- `timer_progress_light`: Progress color
- `countdown_text_color`: Text color for countdown

### Text Colors

- `overlay_text_color`: Text color for overlays
- `text_tertiary_dark`: Tertiary text in dark theme
- `widget_text_color`: Primary widget text
- `widget_white_text`: Text for white widget variant
- `widget_glass_text_color`: Text for glass widget variant
- `widget_text_secondary`: Secondary widget text

### Widgets & Shortcuts

- `shortcut_background_dark`: Dark background for shortcuts
- `shortcut_background_light`: Light background for shortcuts
- `shortcut_background_settings`: Settings background for shortcuts
- `widget_background_dark_gray`: Dark gray widget background
- `widget_white_divider`: Divider for white widget

## Usage in XML

Always reference these colors using `@color/token_name`:

```xml
<View
    android:background="@color/app_background_dark"
    android:textColor="@color/card_text_dark" />
```

## Usage in Code

Access via `ContextCompat.getColor(context, R.color.token_name)`:

```kotlin
val backgroundColor = ContextCompat.getColor(context, R.color.app_background_dark)
```
