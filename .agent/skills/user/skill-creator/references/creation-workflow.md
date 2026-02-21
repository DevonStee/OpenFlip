# Skill Creation Workflow

## Step 1: Understand with Concrete Examples

Identify what functionality is needed. Ask specific questions about triggers and use cases. Conclude when you have a clear sense of the "When to use".

## Step 2: Plan Reusable Contents

Analyze examples to identify required scripts (code reused often), references (data needed), and assets (boilerplate/resources for output).

## Step 3: Initialize the Skill

Run the initialization script (if available) or create the standard directory structure in the appropriate location:

### Directory Selection

- **`user/`**: Default location for all custom skills.

### Structure

```text
[category]/skill-name/
├── SKILL.md (required)
├── scripts/
├── references/
└── assets/
```

## Step 4: Implement and Edit

Build the reusable resources first. Test all scripts. Update `SKILL.md` with a strong description in frontmatter and a concise, imperative body.

## Step 5: Package and Validate

Validate the structure, metadata, and functionality. Package the skill if distribution tools are available.

## Step 6: Iterate

After testing the skill, users may request improvements. Often this happens right after using the skill, with fresh context of how the skill performed.
