# Skills Directory

## Overview

This directory contains reusable knowledge applicable to Android development, with specific focus on patterns used in OpenFlip.

---

## Skill Metadata Standard

Each skill must include:

```markdown
# Skill: [Name]

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** [List of related skills]

## Purpose
...
```

---

## Skill Categories

### Android Platform Skills

- [android-rotation-antiflicker](android-rotation-antiflicker/SKILL.md)
- [android-highperf-customview](android-highperf-customview/SKILL.md)
- [android-widget-development](android-widget-development/SKILL.md)
- [android-native-design](android-native-design/SKILL.md)
- [android-robustness-bestpractices](android-robustness-bestpractices/SKILL.md)
- [android-ui-proactive-verification](android-ui-proactive-verification/SKILL.md)
- [android-button-intent-clarification](android-button-intent-clarification/SKILL.md)
- [android-button-touch-strategy](android-button-touch-strategy/SKILL.md)

### Development Process Skills

- [ai-collab-workflow](ai-collab-workflow/SKILL.md)
- [git-commit-awareness](git-commit-awareness/SKILL.md)
- [codebase-aware-implementation](codebase-aware-implementation/SKILL.md)
- [readme-architecture-sync](readme-architecture-sync/SKILL.md)
- [skill-creator](skill-creator/SKILL.md)

### Code Quality Skills

- [best-practice-check](best-practice-check/SKILL.md)
- [code-quality-audit](code-quality-audit/SKILL.md)
- [code-cleanup-methodology](code-cleanup-methodology/SKILL.md)
- [color-tokens](color-tokens/SKILL.md)

### UI Design Skills

- [options-button-visual-guidelines](options-button-visual-guidelines/SKILL.md)

---

## Maintenance Guidelines

### When to Update Skills

- SDK/AGP version upgrade
- New Android platform release
- Project architectural changes
- Discovery of new best practices

### Update Checklist

- [ ] Update "Last Verified" date
- [ ] Check SDK compatibility
- [ ] Verify code examples still work
- [ ] Update cross-references
- [ ] Test on current project version

---

## Cross-Reference Map

```text
android-rotation-antiflicker
  ├─ Requires: color-tokens (for windowBackground colors)
  └─ Related: android-highperf-customview (for animation performance)

android-highperf-customview
  ├─ Requires: ai-collab-workflow (for testing methodology)
  └─ Related: android-widget-development (for RemoteViews optimization)

android-widget-development
  └─ Related: android-highperf-customview (for rendering optimization)

android-button-intent-clarification
  ├─ Requires: ai-collab-workflow (for intent clarification)
  └─ Related: best-practice-check (for UX validation)

android-button-touch-strategy
  ├─ Requires: android-button-intent-clarification (for intent alignment)
  └─ Related: best-practice-check (for UX validation)

android-ui-proactive-verification
  └─ Related: android-highperf-customview (for performance verification)

git-commit-awareness
  ├─ Requires: codebase-aware-implementation (for commit scope)
  └─ Related: ai-collab-workflow (for communication patterns)

codebase-aware-implementation
  ├─ Requires: best-practice-check (for pattern validation)
  └─ Related: git-commit-awareness (for commit boundaries)

best-practice-check
  ├─ Requires: codebase-aware-implementation (for pattern validation)
  └─ Related: code-quality-audit (for comprehensive analysis)

code-quality-audit
  ├─ Requires: best-practice-check (for validation criteria)
  └─ Related: codebase-aware-implementation (for pattern matching)

code-cleanup-methodology
  ├─ Requires: code-quality-audit (for issue identification)
  └─ Related: best-practice-check (for quality standards)

color-tokens
  └─ Implementation: app/src/main/res/values/colors.xml

readme-architecture-sync
  ├─ Requires: codebase-aware-implementation (for component discovery)
  └─ Related: code-quality-audit (for architecture verification)

skill-creator
  └─ Purpose: Guidelines for creating future skills. Link directly from SKILL.md.
```
