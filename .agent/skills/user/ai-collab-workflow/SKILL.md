---
name: AI Collaboration Workflow
description: Best practices for effective human-AI pair programming and communication
---

# Skill: AI Collaboration Workflow

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** None

## Purpose

This skill provides proven techniques for effective collaboration with AI coding assistants, maximizing productivity and reducing misunderstandings.

---

## When to Use This Skill

- Starting a new project with AI assistance
- Experiencing misunderstandings or incorrect implementations
- Want to improve AI collaboration efficiency

---

## Principle 1: Config-First Project Setup

Before writing features, establish the foundation:

1. **Create `colors.xml`**: Define semantic colors (`bg_primary`, `text_secondary`)
2. **Create `themes.xml`**: Configure Day/Night mode with explicit `windowBackground`
3. **Create utility classes**: `Utils.dp2px`, `HapticManager`, `SoundManager`

**Why**: Extracting colors and dimensions later wastes significant refactoring time.

---

## Principle 2: Clear Requirement Description

### The Golden Formula

> **[Location/Area] + [Current Behavior] + [Reference/Sensory Description] = Precise Execution**

### Examples

| ❌ Vague | ✅ Clear |
| ---------- | ---------- |
| "Fix that clock thing" | "The clock digits in `FlipCard.kt` are not centered. They should align to the visual ink center, not the font metrics center." |
| "Add burn-in protection" | "Add OLED burn-in protection: shift X/Y by 1px every minute, max 4px range, use ValueAnimator, reset to center on app restart." |
| "The line is too long" | "The middle divider line in White Widget should match the width of the card's narrowest point (the 'waist')." |

---

## Principle 3: Provide Context, Not Just Commands

### Bad

> "Edit MainActivity.kt"

### Good

> "I'm building the Settings page. I need to add a toggle for OLED protection. The toggle should:
>
> - Default to OFF
>
> - Persist to SharedPreferences  
> - Immediately apply when changed"

---

## Principle 4: State Constraints Explicitly

Always mention:

- **Library restrictions**: "Don't add new dependencies"
- **API level**: "Must work on API 26+"
- **Performance**: "Must maintain 60fps during animation"
- **Locked code**: "Don't modify the centering formula in FlipCard.kt"

---

## Principle 5: Describe Sensory, Not Technical

Instead of guessing technical terms, describe physical phenomena:

| Sensory Description | AI Understands |
| ------------------- | ---------------- |
| "Like physical paper folding" | 3D flip animation with perspective |
| "Glass-like, see-through" | Translucent overlay with blur |
| "Fat at ends, thin in middle" | Bezier curve with pinched center ("waist" shape) |
| "Jagged edges" | Anti-aliasing issue |

---

## Principle 6: Screenshot + Text = Maximum Clarity

Screenshots solve 90% of visual misunderstandings:

1. **Capture current state**
2. **Annotate with arrows/circles** pointing to problem areas
3. **Add brief text** describing expected vs actual

This is especially critical for:

- UI alignment issues
- Color/contrast problems
- Animation timing
- Layout spacing

---

## Principle 7: Iterative Refinement

### Pattern for Complex Features

1. **Describe the goal** (what it should look like/do)
2. **Review first implementation**
3. **Provide specific feedback** using the Golden Formula
4. **Repeat until satisfied**

### When Stuck

If AI keeps misunderstanding:

1. Try different sensory descriptions
2. Provide a reference image (screenshot from another app, design mockup)
3. Break the request into smaller pieces
4. State what you DON'T want

---

## Anti-Patterns to Avoid

| Anti-Pattern | Why It Fails | Better Approach |
| ------------ | ------------ | ----------------- |
| "Make it better" | No actionable criteria | "Increase contrast between X and Y" |
| "Like the other one" | Ambiguous reference | "Like the Classic widget's shadow effect" |
| "Fix the bug" | No reproduction steps | "When I rotate after 10s idle, black flash appears" |
| Technical guessing | Often wrong terminology | Describe what you see/want |

---

## Communication Checklist

Before sending a request, verify:

- [ ] Location/file specified
- [ ] Current vs expected behavior described
- [ ] Constraints mentioned (API, performance, dependencies)
- [ ] Screenshot attached (if visual issue)
- [ ] Sensory description used (if complex visual effect)
