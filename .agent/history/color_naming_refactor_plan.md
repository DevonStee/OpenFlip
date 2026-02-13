# Color Naming Refactor Plan

**Goal**: Unify all color names to use snake_case naming convention (like `app_background_dark`)

**Current Status**: Mixed naming styles across 184 color definitions

---

## Naming Style Analysis

### ✅ Already Using Snake Case (Keep)
```xml
<color name="app_background_dark">#FF000000</color>
<color name="shadow_light">#AAAAAA</color>
<color name="glow_on">#FF39FF14</color>
<color name="glow_off_dark">#40808080</color>
<color name="shortcut_background_dark">#000000</color>
```

### ❌ Using camelCase (Need to Rename)

#### Card Colors
```xml
<!-- Current -->
<color name="cardBackgroundDark">#FF1C1C1C</color>
<color name="cardTextDark">#FFE6E6E6</color>
<color name="cardBackgroundLight">#FFE8E8E0</color>
<color name="cardTextLight">#FF1A1A1A</color>
<color name="cardShadowFillDark">#FF000000</color>
<color name="cardShadowFillLight">#FF888888</color>

<!-- Should be -->
<color name="card_background_dark">#FF1C1C1C</color>
<color name="card_text_dark">#FFE6E6E6</color>
<color name="card_background_light">#FFE8E8E0</color>
<color name="card_text_light">#FF1A1A1A</color>
<color name="card_shadow_fill_dark">#FF000000</color>
<color name="card_shadow_fill_light">#FF888888</color>
```

#### Settings Colors
```xml
<!-- Current -->
<color name="settingsBackgroundDark">#FF1C1C1C</color>
<color name="settingsTextPrimaryDark">#FFE6E6E6</color>
<color name="settingsTextSecondaryDark">#FF909090</color>
<color name="settingsSectionDark">#FFD0D0D0</color>
<color name="settingsIconDark">#FFB8B8B8</color>
<color name="settingsBackgroundLight">#FFF5F5F0</color>
<color name="settingsTextPrimaryLight">#FF1A1A1A</color>
<color name="settingsTextSecondaryLight">#FF666660</color>
<color name="settingsSectionLight">#FF505050</color>
<color name="settingsIconLight">#FF505050</color>

<!-- Should be -->
<color name="settings_background_dark">#FF1C1C1C</color>
<color name="settings_text_primary_dark">#FFE6E6E6</color>
<color name="settings_text_secondary_dark">#FF909090</color>
<color name="settings_section_dark">#FFD0D0D0</color>
<color name="settings_icon_dark">#FFB8B8B8</color>
<color name="settings_background_light">#FFF5F5F0</color>
<color name="settings_text_primary_light">#FF1A1A1A</color>
<color name="settings_text_secondary_light">#FF666660</color>
<color name="settings_section_light">#FF505050</color>
<color name="settings_icon_light">#FF505050</color>
```

#### Button Colors
```xml
<!-- Current -->
<color name="buttonBackgroundInteractiveDark">#0DFFFFFF</color>
<color name="buttonBackgroundSettingsDark">#2A2A28</color>
<color name="buttonBackgroundSettingsLight">#E8E7DE</color>
<color name="rippleColorWhite">#26FFFFFF</color>
<color name="rippleColorBlack">#1A000000</color>

<!-- Should be -->
<color name="button_background_interactive_dark">#0DFFFFFF</color>
<color name="button_background_settings_dark">#2A2A28</color>
<color name="button_background_settings_light">#E8E7DE</color>
<color name="ripple_color_white">#26FFFFFF</color>
<color name="ripple_color_black">#1A000000</color>
```

#### Control Colors
```xml
<!-- Current -->
<color name="controlDefaultDark">#323230</color>
<color name="controlDefaultLight">#D8D8D0</color>
<color name="controlSelectedDark">#5A5A57</color>
<color name="controlSelectedLight">#A0A098</color>
<color name="controlPressedDark">#4A4A47</color>
<color name="controlPressedLight">#C0C0B8</color>

<!-- Should be -->
<color name="control_default_dark">#323230</color>
<color name="control_default_light">#D8D8D0</color>
<color name="control_selected_dark">#5A5A57</color>
<color name="control_selected_light">#A0A098</color>
<color name="control_pressed_dark">#4A4A47</color>
<color name="control_pressed_light">#C0C0B8</color>
```

#### Text Colors
```xml
<!-- Current -->
<color name="overlayTextColor">#D0FFFFFF</color>
<color name="textTertiaryDark">#B0B0A0</color>
<color name="textTertiaryRed">#FFFF3B30</color>

<!-- Should be -->
<color name="overlay_text_color">#D0FFFFFF</color>
<color name="text_tertiary_dark">#B0B0A0</color>
<color name="text_tertiary_red">#FFFF3B30</color>
```

#### Action Colors
```xml
<!-- Current -->
<color name="actionRed">#FFFF3B30</color>
<color name="onActionRed">#FFFFFFFF</color>
<color name="actionRedTransparent">#D9FF3B30</color>

<!-- Should be -->
<color name="action_red">#FFFF3B30</color>
<color name="on_action_red">#FFFFFFFF</color>
<color name="action_red_transparent">#D9FF3B30</color>
```

#### Knob Colors
```xml
<!-- Current -->
<color name="knobBackground">#FFD7CBAB</color>
<color name="knobTickUnselected">#FF746551</color>
<color name="knobTickSelected">#FFA99573</color>
<color name="knobIndicator">#FFFF3B30</color>

<!-- Should be -->
<color name="knob_background">#FFD7CBAB</color>
<color name="knob_tick_unselected">#FF746551</color>
<color name="knob_tick_selected">#FFA99573</color>
<color name="knob_indicator">#FFFF3B30</color>
```

#### Toggle Colors (Mixed style)
```xml
<!-- Current -->
<color name="toggleBgOn">#FFE8D15A</color>
<color name="toggleBgOff">#FFD7CBAB</color>
<color name="toggleStrokeOn">#FFB8860B</color>
<color name="toggleStrokeOff">#FF746551</color>
<color name="toggleGlowOn">#66F2DA55</color>
<color name="toggleGlowOff">#00000000</color>
<color name="toggleIconOn">#FF2B1A00</color>
<color name="toggleIconOnPressed">#FF1E1200</color>
<color name="toggleIconOff">#9942331F</color>

<!-- Should be -->
<color name="toggle_bg_on">#FFE8D15A</color>
<color name="toggle_bg_off">#FFD7CBAB</color>
<color name="toggle_stroke_on">#FFB8860B</color>
<color name="toggle_stroke_off">#FF746551</color>
<color name="toggle_glow_on">#66F2DA55</color>
<color name="toggle_glow_off">#00000000</color>
<color name="toggle_icon_on">#FF2B1A00</color>
<color name="toggle_icon_on_pressed">#FF1E1200</color>
<color name="toggle_icon_off">#9942331F</color>
```

---

## Full Refactor Mapping

Total colors to rename: **~80 colors**

| Current Name | New Name |
|--------------|----------|
| `cardBackgroundDark` | `card_background_dark` |
| `cardTextDark` | `card_text_dark` |
| `settingsBackgroundDark` | `settings_background_dark` |
| `settingsTextPrimaryDark` | `settings_text_primary_dark` |
| `settingsTextSecondaryDark` | `settings_text_secondary_dark` |
| `settingsSectionDark` | `settings_section_dark` |
| `settingsIconDark` | `settings_icon_dark` |
| `cardBackgroundLight` | `card_background_light` |
| `cardTextLight` | `card_text_light` |
| `settingsBackgroundLight` | `settings_background_light` |
| `settingsTextPrimaryLight` | `settings_text_primary_light` |
| `settingsTextSecondaryLight` | `settings_text_secondary_light` |
| `settingsSectionLight` | `settings_section_light` |
| `settingsIconLight` | `settings_icon_light` |
| `cardShadowFillDark` | `card_shadow_fill_dark` |
| `cardShadowFillLight` | `card_shadow_fill_light` |
| `buttonBackgroundInteractiveDark` | `button_background_interactive_dark` |
| `rippleColorWhite` | `ripple_color_white` |
| `rippleColorBlack` | `ripple_color_black` |
| `buttonBackgroundSettingsDark` | `button_background_settings_dark` |
| `buttonBackgroundSettingsLight` | `button_background_settings_light` |
| `controlDefaultDark` | `control_default_dark` |
| `controlDefaultLight` | `control_default_light` |
| `controlSelectedDark` | `control_selected_dark` |
| `controlSelectedLight` | `control_selected_light` |
| `controlPressedDark` | `control_pressed_dark` |
| `controlPressedLight` | `control_pressed_light` |
| `overlayTextColor` | `overlay_text_color` |
| `textTertiaryDark` | `text_tertiary_dark` |
| `textTertiaryRed` | `text_tertiary_red` |
| `actionRed` | `action_red` |
| `onActionRed` | `on_action_red` |
| `actionRedTransparent` | `action_red_transparent` |
| `knobBackground` | `knob_background` |
| `knobTickUnselected` | `knob_tick_unselected` |
| `knobTickSelected` | `knob_tick_selected` |
| `knobIndicator` | `knob_indicator` |
| `toggleBgOn` | `toggle_bg_on` |
| `toggleBgOff` | `toggle_bg_off` |
| `toggleStrokeOn` | `toggle_stroke_on` |
| `toggleStrokeOff` | `toggle_stroke_off` |
| `toggleGlowOn` | `toggle_glow_on` |
| `toggleGlowOff` | `toggle_glow_off` |
| `toggleIconOn` | `toggle_icon_on` |
| `toggleIconOnPressed` | `toggle_icon_on_pressed` |
| `toggleIconOff` | `toggle_icon_off` |

... (继续列出所有需要重命名的颜色)

---

## Implementation Steps

### 1. **Find All References**
Use grep to find all files referencing each color:
```bash
grep -r "settingsBackgroundDark" app/src/main/
```

### 2. **Rename in colors.xml**
Update the color definitions in `colors.xml`

### 3. **Update All References**
Files that will need updates:
- Kotlin files (*.kt)
- XML layouts (*.xml)
- Theme files (themes.xml)
- Drawable files (*.xml)

### 4. **Testing**
- Build and run the app
- Test all screens (light + dark theme)
- Verify no color references are broken

---

## Estimated Impact

**Files to modify**: ~50-100 files
**Time estimate**: 2-3 hours
**Risk**: Medium (breaking references if missed)

---

## Recommendation

**Option 1**: Do the full refactor now
- Pro: Clean, consistent codebase
- Con: Large changeset, potential for breaking changes

**Option 2**: Gradual refactor
- Create aliases (old names pointing to new names)
- Slowly migrate files
- Remove old names later

**Option 3**: Leave as-is, only fix new colors
- Pro: No breaking changes
- Con: Technical debt remains

---

## My Recommendation: Option 2 (Gradual)

1. Add new snake_case names in colors.xml
2. Keep old names as aliases:
   ```xml
   <!-- New preferred name -->
   <color name="settings_background_dark">#FF1C1C1C</color>

   <!-- Deprecated - use settings_background_dark -->
   <color name="settingsBackgroundDark">@color/settings_background_dark</color>
   ```
3. Update code files gradually
4. Mark old names with deprecation comments
5. Remove old names in a future cleanup

This approach is **safer** and allows for **incremental testing**.

Would you like me to proceed with this approach?
