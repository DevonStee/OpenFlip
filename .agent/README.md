# .agent Directory Structure

This directory contains AI assistant configuration and project documentation.

---

## Files

| File | Purpose |
| ------ | --------- |
| **AGENTS.md** | AI developer guide - coding standards, naming conventions, LOCKED rules |
| **ARCHITECTURE.md** | Multi-module structure, architectural patterns, dependency rules |
| **DEVELOPMENT_NOTES.md** | Project status, settings schema, development environment |
| **FUTURE_FEATURES.md** | Feature decisions: what to implement, what not to, what's done |
| **MAINTENANCE.md** | Guidelines for keeping .agent/ documentation current |
| **task.md** | Current task tracking checklist |

---

## Guides

Project-specific implementation guides:

| Guide | Description |
| ------- | ------------- |
| [App Shortcuts](guides/app_shortcuts_implementation.md) | Long-press app icon quick actions (implemented) |
| [Dream Mode](guides/dream_mode_implementation.md) | Screensaver/DreamService integration (implemented) |

---

## Skills

Skills are organized into root-level and user-created categories. See [skills/README.md](skills/README.md) for details.

### Root-Level Skills

| Skill | Description |
| ------- | ------------- |
| [android-theme-transition-safety](skills/android-theme-transition-safety/SKILL.md) | Race-condition-free theme transitions |
| [commit-batching](skills/commit-batching/SKILL.md) | Organizing atomic git commits |

### User Skills (`skills/user/`)

| Skill | Description |
| ------- | ------------- |
| [android-rotation-antiflicker](skills/user/android-rotation-antiflicker/SKILL.md) | Eliminate black flash during rotation |
| [android-highperf-customview](skills/user/android-highperf-customview/SKILL.md) | 60fps custom View rendering |
| [android-widget-development](skills/user/android-widget-development/SKILL.md) | AppWidget/RemoteViews patterns |
| [android-native-design](skills/user/android-native-design/SKILL.md) | Native Android design patterns |
| [android-robustness-bestpractices](skills/user/android-robustness-bestpractices/SKILL.md) | Robustness best practices |
| [android-button-intent-clarification](skills/user/android-button-intent-clarification/SKILL.md) | Button intent clarification |
| [android-button-touch-strategy](skills/user/android-button-touch-strategy/SKILL.md) | Button touch handling strategies |
| [android-ui-proactive-verification](skills/user/android-ui-proactive-verification/SKILL.md) | Proactive UI verification |
| [ai-collab-workflow](skills/user/ai-collab-workflow/SKILL.md) | Human-AI pair programming |
| [color-tokens](skills/user/color-tokens/SKILL.md) | Color naming and token system |
| [best-practice-check](skills/user/best-practice-check/SKILL.md) | Code quality audits |
| [git-commit-awareness](skills/user/git-commit-awareness/SKILL.md) | Proactive commit recognition |
| [codebase-aware-implementation](skills/user/codebase-aware-implementation/SKILL.md) | Pattern discovery before implementation |
| [code-cleanup-methodology](skills/user/code-cleanup-methodology/SKILL.md) | Code cleanup methodology |
| [code-quality-audit](skills/user/code-quality-audit/SKILL.md) | Code quality auditing |
| [options-button-visual-guidelines](skills/user/options-button-visual-guidelines/SKILL.md) | Options button visual guidelines |
| [readme-architecture-sync](skills/user/readme-architecture-sync/SKILL.md) | README-architecture synchronization |
| [skill-creator](skills/user/skill-creator/SKILL.md) | Creating new skills |

---

## Quick Start for AI Assistants

1. Read **AGENTS.md** for project rules and conventions
2. Check **skills/** for relevant best practices
3. Review **ARCHITECTURE.md** for multi-module structure
4. Track progress in **task.md**

---

## Maintenance

See **MAINTENANCE.md** for:

- When to update documentation
- Skill maintenance workflow
- SDK upgrade procedures
- Archiving obsolete patterns
