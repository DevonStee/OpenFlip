# .agent Directory Structure

This directory contains AI assistant configuration and project documentation.

---

## Files

| File | Purpose |
| ------ | --------- |
| **AGENTS.md** | AI developer guide - coding standards, naming conventions, LOCKED rules |
| **ARCHITECTURE.md** | Package structure, architectural patterns, dependency rules |
| **DEVELOPMENT_NOTES.md** | Project status, settings schema, development environment |
| **MAINTENANCE.md** | Guidelines for keeping .agent/ documentation current |
| **task.md** | Current task tracking checklist |

---

---

## Guides

Project-specific implementation guides for future features:

| Guide | Description |
| ------- | ------------- |
| [App Shortcuts](guides/app_shortcuts_implementation.md) | Long-press app icon quick actions |
| [Dream Mode](guides/dream_mode_implementation.md) | Screensaver/DreamService integration |

---

## Skills

Reusable knowledge applicable to any Android project. See [skills/README.md](skills/README.md) for maintenance guidelines and cross-reference map.

| Skill | Description | Last Verified |
| ------- | ------------- | ------------- |
| [android-rotation-antiflicker](skills/android-rotation-antiflicker/SKILL.md) | Eliminate black flash during screen rotation | 2026-01-23 |
| [android-highperf-customview](skills/android-highperf-customview/SKILL.md) | 60fps custom View rendering best practices | 2026-01-23 |
| [android-widget-development](skills/android-widget-development/SKILL.md) | AppWidget/RemoteViews patterns and limitations | 2026-01-23 |
| [ai-collab-workflow](skills/ai-collab-workflow/SKILL.md) | Effective human-AI pair programming | 2026-01-23 |
| [color-tokens](skills/color-tokens/SKILL.md) | Standardized color naming and token system | 2026-01-23 |
| [best-practice-check](skills/best-practice-check/SKILL.md) | Automated architectural and code quality audit | 2026-01-23 |
| [git-commit-awareness](skills/git-commit-awareness/SKILL.md) | Proactive commit recognition and meaningful messages | 2026-01-23 |
| [codebase-aware-implementation](skills/codebase-aware-implementation/SKILL.md) | Pattern discovery before implementation | 2026-01-23 |

---

## Quick Start for AI Assistants

1. Read **AGENTS.md** for project rules and conventions
2. Check **skills/** for relevant best practices
3. Review **ARCHITECTURE.md** for package structure
4. Track progress in **task.md**

---

## Maintenance

See **MAINTENANCE.md** for:

- When to update documentation
- Skill maintenance workflow
- SDK upgrade procedures
- Archiving obsolete patterns

**Key:** Update "Last Verified" dates in skills table above when testing patterns.
