---
name: Android Button Intent Clarification
description: Clarify touch-target vs visual size when users ask to resize buttons without changing icons.
last_verified: 2026-01-23
applicable_sdk: Android 14+ (API 34+)
dependencies:
  - ai-collab-workflow
  - best-practice-check
---

# Skill: Android Button Intent Clarification (Touch vs Visual)

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** ai-collab-workflow, best-practice-check

## Purpose

Translate ambiguous user phrasing like "make the button bigger but keep the icon"
into the correct distinction between touch target and visual size. This reduces
misalignment between intent and implementation.

---

## When to Use

- User asks to "resize buttons" while saying "do not change icon/background"
- UI elements need alignment but should not expand visual weight
- You suspect a touch target vs visual size mismatch

---

## Key Recognition Pattern

### User Phrase
> "调整按钮大小，但不改变图标/圆形大小"

### Hidden Intent
- **Touch target** should get larger (layout alignment or usability)
- **Visual area** should stay the same (design consistency)

---

## Clarifying Question Template (Required)

Ask one explicit question before changing layout sizes:

> "确认一下：你是希望触摸区域扩大到 64dp，但圆形视觉仍保持 48dp，对吗？
> 如果是，我会用外层透明容器 + 内层圆形的结构。"

If the user confirms, proceed with the nested layout pattern (see related skill).

---

## Common Misinterpretation

- **Wrong assumption:** "Change container size is enough."
- **Actual result:** Background drawable scales with container and grows visually.

---

## Outcome Mapping

| User intent | Correct implementation |
| --- | --- |
| Bigger touch area, same look | Outer 64dp transparent + inner 48dp visual |
| Bigger visual button | Single container size increase |
| Unchanged touch area | Keep existing container size |

---

## Notes

- Treat "do not change icon size" as a signal to protect **visual area**, not
  just the ImageView dimensions.
- If there is any ambiguity, ask before editing layout files.
