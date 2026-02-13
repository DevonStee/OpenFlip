---
name: Skill Creator
description: Guidance for creating effective modular skills for Claude. Use when creating or refining skills to ensure they are concise, follow progressive disclosure patterns, and provide appropriate degrees of freedom.
---

# Skill: Skill Creator

This skill provides a comprehensive framework for designing and implementing effective skills. Skills are modular packages that extend capabilities by providing specialized knowledge and workflows.

## Core Principles

### Concise is Key

The context window is a limited resource. Claude is already smart; only add context it doesn't already have.

- **Challenge every paragraph**: Does this justify its token cost?
- **Prefer examples**: Use concise code/text snippets over verbose explanations.

### Set Appropriate Degrees of Freedom

Match the level of specificity to the task's fragility:

- **High Freedom (Text/Heuristics)**: Use when multiple approaches are valid or decisions are context-dependent.
- **Medium Freedom (Pseudocode/Templates)**: Use when a preferred pattern exists but some variation is acceptable.
- **Low Freedom (Specific Scripts)**: Use when operations are fragile, consistency is critical, or a specific sequence must be followed.

---

## Anatomy of a Skill

A skill consists of a required `SKILL.md` and optional bundled resources.

- **`SKILL.md`**: Main entry point with metadata and core workflow instructions.
- **`scripts/`**: Executable code for deterministic reliability.
- **`references/`**: Heavy-lifting documentation loaded only when needed.
- **`assets/`**: Files used in output (templates, icons, boilerplate) not loaded into context.

---

## Design Patterns

To keep skills lean and effective, follow these patterns:

- **Progressive Disclosure**: Use a multi-level loading system to manage context efficiently.
  - See [Progressive Disclosure Guide](references/progressive-disclosure.md)
- **Resource Management**: Properly utilize scripts, references, and assets.
  - See [Resource Guidelines](references/resource-guidelines.md)

---

## Skill Creation Workflow

The creation process follows a systematic 6-step approach:

1. **Understand** with concrete examples.
2. **Plan** reusable contents.
3. **Initialize** the skill structure.
4. **Edit** and implement resources.
5. **Package** into a `.skill` file.
6. **Iterate** based on usage.

Detailed instructions: See [Creation Workflow](references/creation-workflow.md)
