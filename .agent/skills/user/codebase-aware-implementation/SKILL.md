---
name: Codebase-Aware Implementation
description: Ensure new features follow existing project patterns by studying current code and docs before coding.
last_verified: 2026-01-23
applicable_sdk: Android 14+ (API 34+)
dependencies:
  - best-practice-check
  - git-commit-awareness
---

# Skill: Codebase-Aware Implementation

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** best-practice-check, git-commit-awareness

## Purpose

Always implement new features by studying existing project patterns first, rather than defaulting to "comfortable" but inconsistent approaches.

---

## The Problem

### ❌ Pattern-Blind Development

```kotlin
// AI implements a new feature using generic Android patterns
class NewFeatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewFeatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
```

#### But the project uses

- Custom View architecture (not Activities)
- Manual XML layouts (not ViewBinding)
- Specific initialization patterns

**Result:** Inconsistent code that requires refactoring

---

## The Solution: Pattern Discovery First

### Mandatory Pre-Implementation Phase

**Before writing ANY new code:**

1. **Search for similar features** → Understand existing patterns
2. **Read architectural docs** → Check `.agent/ARCHITECTURE.md`
3. **Identify conventions** → Review `.agent/AGENTS.md`
4. **Match the style** → Implement consistently

---

## Implementation Workflow

### Phase 1: Discovery (ALWAYS DO THIS FIRST)

#### Step 1.1: Find Similar Features

```bash
# Example: Adding a new settings toggle

# 1. Find existing toggles
grep -r "SwitchMaterial" app/src/main/res/layout/

# 2. Find toggle handling code
grep -r "setOnCheckedChangeListener" app/src/main/kotlin/

# 3. Examine settings architecture
ls -la app/src/main/kotlin/**/settings/
```

#### Questions to Answer

- How are settings stored? (SharedPreferences? DataStore? Repository?)
- What naming convention is used? (`is*Enabled`? `*Setting`?)
- Where is UI logic? (Activity? Fragment? BottomSheet?)

#### Step 1.2: Read Architecture Docs

```bash
# Check for architectural guidance
cat .agent/ARCHITECTURE.md
cat .agent/AGENTS.md
cat .agent/DEVELOPMENT_NOTES.md
```

#### Look for

- Package structure rules
- Naming conventions
- Design patterns in use
- LOCKED patterns (must not change)

#### Step 1.3: Analyze Existing Implementation

**Read at least 2-3 similar features before implementing:**

```kotlin
// Read existing code to understand patterns
// Example: Before adding new theme option

// 1. Read existing theme toggle
Read: SettingsBottomSheet.kt
Read: ThemeManager.kt

// 2. Understand state management
Read: SettingsDataStore.kt or SharedPreferences usage

// 3. Check UI patterns
Read: layout/bottom_sheet_settings.xml
```

---

### Phase 2: Pattern Matching

#### Identify Core Patterns

| Aspect | Pattern Discovery |
| ------ | ----------------- |
| **Architecture** | MVC? MVP? MVVM? Custom View-based? |
| **State Management** | SharedPreferences? DataStore? Repository? |
| **UI Framework** | Activities? Fragments? Custom Views? Compose? |
| **Dependency Injection** | Manual? Hilt? Koin? |
| **Resource Naming** | `icon_*_*_24dp`? `ic_*`? Check existing resources |
| **Code Structure** | Where do similar features live? |

#### Document Your Findings

Before implementing, write down:

```markdown
## Implementation Plan

### Existing Patterns Discovered
- Settings stored in: SharedPreferences (`SettingsManager.kt`)
- Toggles defined in: `bottom_sheet_settings.xml`
- Naming: `is[Feature]Enabled` (e.g., `isOledProtectionEnabled`)
- UI Pattern: Material SwitchMaterial with custom styling

### My Implementation Will
- [ ] Store setting in SharedPreferences via SettingsManager
- [ ] Add SwitchMaterial to bottom_sheet_settings.xml
- [ ] Follow `is*Enabled` naming convention
- [ ] Use existing toggle listener pattern
- [ ] Match existing toggle spacing/styling
```

---

### Phase 3: Consistent Implementation

#### Follow Project Conventions Exactly

#### Example: Adding "Auto-Brightness" Toggle

```kotlin
// ❌ Generic implementation (ignore existing patterns)
class BrightnessManager {
    fun saveAutoBrightness(enabled: Boolean) {
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_brightness", enabled).apply()
    }
}

// ✅ Pattern-matched implementation (matches existing code)
// After reading SettingsManager.kt and discovering:
// - Project uses singleton SettingsManager
// - Uses "is*Enabled" naming
// - Central SharedPreferences instance

object SettingsManager {
    // ... existing code ...

    var isAutoBrightnessEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BRIGHTNESS, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BRIGHTNESS, value).apply()

    private const val KEY_AUTO_BRIGHTNESS = "is_auto_brightness_enabled"
}
```

---

## Balancing: Best Practice vs. Agile Speed

### The Dilemma

**Scenario:** You need to add a feature quickly (agile), but the codebase has technical debt.

**Options:**

1. **Quick & Dirty**: Implement fastest way → Technical debt grows
2. **Perfect Refactor**: Refactor everything first → Slow, risky
3. **Pattern Match**: Follow existing patterns, even if imperfect → **Recommended**

### Decision Framework

```text
┌─────────────────────────────────────────┐
│ Is the existing pattern DANGEROUS?     │
│ (security issue, crashes, data loss)   │
└─────────────────┬───────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
       YES                 NO
        │                   │
        v                   v
  ┌───────────┐      ┌────────────────┐
  │ STOP      │      │ Follow existing│
  │ Discuss   │      │ pattern        │
  │ with user │      └────────────────┘
  └───────────┘
```

### Pattern Quality Assessment

**Ask these questions about existing patterns:**

| Question | If YES | If NO |
| -------- | ------ | ----- |
| Is it consistent across the codebase? | ✅ Follow it | 🤔 Discuss with user |
| Is it documented in AGENTS.md as LOCKED? | ✅ Must follow | ⚠️ Can propose changes |
| Would changing it require touching 10+ files? | ✅ Follow it | 💡 Can improve incrementally |
| Does it cause bugs or crashes? | 🚨 Must fix | ✅ Follow it |
| Is it just "not ideal" but functional? | ✅ Follow it | ✅ Follow it |

### The "Good Enough" Rule

当需要跟随不完美的模式时：

```text
Follow existing pattern if:
✓ Pattern is consistent across codebase
✓ Pattern works without bugs
✓ Changing it requires major refactoring
✓ Team is in "delivery mode" (agile sprint)

Propose improvements if:
✗ Pattern causes frequent bugs
✗ Pattern violates SOLID principles severely
✗ Better alternative requires < 5 file changes
✗ You're in "refactor mode" (tech debt sprint)
```

---

## Refactoring Strategy

### Incremental Improvement

**Instead of big-bang refactors:**

```text
❌ BAD: Refactor entire settings system before adding toggle
Time: 2 weeks, Risk: High, Value: ?

✅ GOOD: Add toggle using existing pattern + note tech debt
Time: 1 hour, Risk: Low, Value: Feature shipped

✅ BETTER: Add toggle + improve pattern in that area only
Time: 2 hours, Risk: Low, Value: Feature + local improvement
```

### Technical Debt Documentation

**When you follow a suboptimal pattern, document it:**

```kotlin
// TODO(tech-debt): Settings stored in SharedPreferences directly.
// Consider migrating to DataStore for type safety and Flow support.
// Affects: SettingsManager.kt, ThemeController.kt, OledProtectionController.kt
object SettingsManager {
    var isOledProtectionEnabled: Boolean
        get() = prefs.getBoolean("is_oled_protection_enabled", false)
        set(value) = prefs.edit().putBoolean("is_oled_protection_enabled", value).apply()
}
```

**Then add to `.agent/FUTURE_FEATURES.md`:**

```markdown
## Technical Debt

- [ ] Migrate SharedPreferences to DataStore
  - Provides type safety
  - Supports Kotlin Flow for reactive updates
  - Affects ~8 files in settings package
```

---

## Examples from OpenFlip Project

### Example 1: Adding Widget Style

**Scenario:** Add new "Minimal" widget style

#### ❌ Wrong Approach (Pattern-Blind)

```kotlin
// Just implement without research
class MinimalWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(...) {
        // Generic implementation
    }
}
```

#### ✅ Right Approach (Codebase-Aware)

#### Step 1: Discovery (Example 1)

```bash
# Find existing widgets
ls app/src/main/kotlin/widget/

# Output:
# - WidgetClockBaseProvider.kt  ← Base class pattern!
# - WidgetClockClassicProvider.kt
# - WidgetClockGlassProvider.kt
# - WidgetClockSolidProvider.kt
```

#### Step 2: Pattern Analysis

```kotlin
// Read WidgetClockBaseProvider.kt
abstract class WidgetClockBaseProvider : AppWidgetProvider() {
    protected abstract fun getLayoutId(): Int
    protected abstract fun getWidgetStyle(): WidgetStyle
    // ... common logic ...
}
```

#### Step 3: Pattern-Matched Implementation (Example 1)

```kotlin
// Match the existing pattern exactly
class WidgetClockMinimalProvider : WidgetClockBaseProvider() {
    override fun getLayoutId() = R.layout.widget_minimal
    override fun getWidgetStyle() = WidgetStyle.MINIMAL
}
```

**Result:** Consistent with 4 other widgets, no refactoring needed

---

### Example 2: Adding Color to Theme

**Scenario:** Add accent color to theme system

#### ❌ Wrong Approach

```xml
<!-- Just add colors without checking project style -->
<color name="accent">#FF5722</color>
<color name="accentDark">#E64A19</color>
```

#### ✅ Right Approach

#### Step 1: Discovery (Example 2)

```bash
# Check existing color naming
grep -r "color name=" app/src/main/res/values/colors.xml

# Find naming convention documentation
cat .agent/skills/color-tokens/SKILL.md
```

#### Step 2: Read Conventions

```xml
<!-- Discovered pattern from colors.xml -->
<color name="bg_primary_light">#FFFFFF</color>
<color name="bg_primary_dark">#000000</color>
<color name="text_primary_light">#000000</color>
<color name="text_primary_dark">#FFFFFF</color>
```

**Pattern:** `[category]_[level]_[theme]`

#### Step 3: Pattern-Matched Implementation (Example 2)

```xml
<!-- Follow discovered pattern -->
<color name="accent_primary_light">#FF5722</color>
<color name="accent_primary_dark">#E64A19</color>
```

---

## AI Assistant Checklist

Before implementing ANY new feature:

```markdown
### Pre-Implementation Checklist

- [ ] **Search for similar features**
  - [ ] Used Grep to find similar code
  - [ ] Used Glob to find similar files
  - [ ] Read at least 2 similar implementations

- [ ] **Read architectural docs**
  - [ ] Checked .agent/ARCHITECTURE.md
  - [ ] Checked .agent/AGENTS.md
  - [ ] Checked relevant skills/

- [ ] **Identify patterns**
  - [ ] Package structure convention
  - [ ] Naming conventions
  - [ ] Architectural patterns (MVC/MVVM/etc)
  - [ ] Resource naming patterns

- [ ] **Document findings**
  - [ ] Wrote down discovered patterns
  - [ ] Noted any LOCKED rules
  - [ ] Identified files to match

- [ ] **Plan implementation**
  - [ ] Implementation plan matches existing patterns
  - [ ] Will not require immediate refactoring
  - [ ] Follows project conventions exactly

### Only After Above: Proceed with Implementation
```

---

## Communication with User

### When Patterns Are Unclear

**Template:**

```text
I found two different patterns for [feature] in the codebase:

Pattern A (used in FileX.kt, FileY.kt):
[code example]

Pattern B (used in FileZ.kt):
[code example]

Which pattern should I follow for this new feature?

Or should I refactor to unify these patterns first?
```

### When Patterns Are Suboptimal

**Template:**

```text
I discovered the existing [feature] pattern:
[code example]

This pattern works but has limitations:
- [limitation 1]
- [limitation 2]

I can:
1. Follow the existing pattern (fast, consistent, ships feature)
2. Improve the pattern locally (medium effort, better code)
3. Refactor all usages (slow, high risk, best quality)

In agile mode, I recommend Option 1 or 2. Which do you prefer?
```

---

## Summary

### Golden Rules

1. **Never code before research** - Always discover patterns first
2. **Consistency > perfection** - Match existing style even if imperfect
3. **Document tech debt** - Note what should be improved later
4. **Incremental improvement** - Small refactors > big rewrites
5. **Communicate trade-offs** - Explain agile vs. quality choices

### Time Budget

For typical feature implementation:

```text
20% - Pattern discovery (search, read, analyze)
10% - Planning (document findings, match patterns)
60% - Implementation (write code matching patterns)
10% - Verification (test, review consistency)
```

**Investing time in discovery PREVENTS refactoring loops.**

---

## Related Skills

- [Git Commit Awareness](../git-commit-awareness/SKILL.md) - When to commit
- [Best Practice Check](../best-practice-check/SKILL.md) - Code quality audit
- [AI Collaboration Workflow](../ai-collab-workflow/SKILL.md) - Communication patterns
