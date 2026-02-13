# OpenFlip Android - AI Developer Guide (AGENTS.md)

---

## Project Environment

- **Language**: Kotlin 1.9+
- **UI**: XML Layouts + Custom Views
- **Design System**: Material Components 3 (1.13.0)
- **Build**: Gradle 8.10, AGP 8.10+
- **Target SDK**: 35 | **Min SDK**: 26

---

## Essential Skills

Before working on this project, review these skills in `.agent/skills/`:

| Skill | When to Use |
| ------- | ------------- |
| [Android Rotation Anti-Flicker](skills/android-rotation-antiflicker/SKILL.md) | Screen rotation issues |
| [Android High-Performance Custom View](skills/android-highperf-customview/SKILL.md) | Custom View rendering, animation optimization |
| [Android Widget Development](skills/android-widget-development/SKILL.md) | AppWidget / RemoteViews work |
| [AI Collaboration Workflow](skills/ai-collab-workflow/SKILL.md) | Communication best practices |
| [Color Tokens](skills/color-tokens/SKILL.md) | Color naming and token references |
| [Best Practice Check](skills/best-practice-check/SKILL.md) | Code quality and architectural audits |
| [Git Commit Awareness](skills/git-commit-awareness/SKILL.md) | **MANDATORY: Proactive commit detection after feature completion** |
| [Codebase-Aware Implementation](skills/codebase-aware-implementation/SKILL.md) | **MANDATORY: Pattern discovery before implementing** |

---

## Critical Rules (DO NOT MODIFY)

### [FlipCard] Visual Centering Logic

**LOCKED**: The optical centering calculation formula must not be modified:

```kotlin
val visualCenterOffsetX = -(textBounds.centerX() - measureWidth / 2f)
```

This compensates for `Paint.Align.CENTER` limitations, ensuring "ink center" aligns with "physical card center".

### [UI] Optical Alignment & Shadow Compensation

**Rule**: When aligning or centering UI elements (buttons, knobs, cards), ALWAYS account for the **actual visual content** rather than just the container dimensions.

- **Shadows & Glows**: If a component has an outer shadow (like `InfiniteKnobView`), the container is often larger than the visible circle to accommodate the blur. Do NOT use raw container centers. Use compensated margins or offsets to align the *visible* core.
- **Internal Padding**: Large touch targets (like 56dp or 64dp buttons) often have internal padding for accessibility. Center calculations must consider the "ink" or "icon" center.
- **Transparency**: Ignore transparent outer areas of a View when calculating symmetry.

### [Widget] RemoteViews Rendering Limitations

**KNOWN LIMITATION**: Cannot fix antialiasing white edges in Solid Widget middle gap by overlaying Views.

**Reason**: Android AppWidget (`RemoteViews`) has strict view hierarchy limits. Overlays may cause "Problem loading widget" errors.

**Decision**: Accept the white edge. Do not attempt overlay fixes.

---

## Coding Standards

### Naming Conventions

| Type | Pattern | Example |
| ------ | --------- | --------- |
| Drawable (icon) | `icon_<category>_<name>_<size>` | `icon_settings_about_24dp` |
| Drawable (shape) | `shape_<purpose>` | `shape_card_background` |
| Drawable (selector) | `selector_<name>` | `selector_button_primary` |
| Layout | `<type>_<feature>` | `activity_main`, `widget_glass` |
| Color | Semantic naming | `bg_primary`, `text_secondary` |
| Source File | `[UsageContext][Feature][Type]` | `SettingsOledToggle`, `HomeClockWidget` |

### Resource Hardcoding

- ❌ NEVER use `#RRGGBB` in layout files
- ✅ ALWAYS use `@color/` references
- ❌ NEVER use `?attr/colorSurface` for `windowBackground`
- ✅ ALWAYS use hardcoded `@color/` for window backgrounds

---

## Git Workflow

### Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat: add OLED burn-in protection
fix: resolve rotation black flash
refactor: extract FlipCard rendering logic
```

### Version Bumping

After completing a feature set, update version in `app/build.gradle.kts`:

```kotlin
versionName = "0.5.0-beta"
```

---

## Testing Checklist

Before declaring a feature "complete":

- [ ] Light Mode + Dark Mode
- [ ] Portrait + Landscape
- [ ] Rotation during animation
- [ ] 10+ seconds idle, then rotate
- [ ] Widget on home screen

## Workflow Automation

### Feature Development Cycle (MANDATORY)

After implementing ANY feature, follow this cycle:

1. **Pattern Discovery** (BEFORE coding)
   - Search for similar features in codebase
   - Read `.agent/ARCHITECTURE.md` and `.agent/AGENTS.md`
   - Match existing patterns (see [Codebase-Aware Implementation](skills/codebase-aware-implementation/SKILL.md))

2. **Implementation**
   - Follow discovered patterns exactly
   - Write consistent code

3. **Deployment & Testing**
   - Build and deploy: `./gradlew installDebug`
   - Test on device
   - Verify functionality

4. **Commit Recognition** (AUTOMATIC)
   - AI must detect when feature is complete
   - Propose git commit with meaningful message
   - Follow [Git Commit Awareness](skills/git-commit-awareness/SKILL.md)
   - Use Conventional Commits format

**Example Flow:**

```bash
# 1. Pattern discovery (before coding)
grep -r "similar_feature" app/src/

# 2. Implementation (write code)

# 3. Deploy & test
./gradlew installDebug

# 4. Commit (after user approval)
git add [files]
git commit -m "feat(scope): description

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```
