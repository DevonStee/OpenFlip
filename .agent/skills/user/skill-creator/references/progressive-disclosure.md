# Progressive Disclosure Patterns

Skills use a three-level loading system to manage context efficiently:

1. **Metadata (name + description)**: Always in context (~100 words).
2. **SKILL.md body**: Loaded when the skill triggers (<500 lines).
3. **Bundled resources**: Loaded as needed by Claude.

## Key Patterns

### Pattern 1: High-level Guide with References

Keep only the "Quick Start" and core logic in `SKILL.md`. Move advanced features, API references, and comprehensive examples to separate reference files.

### Pattern 2: Domain-Specific Organization

For complex skills, organize content by domain (e.g., `references/finance.md`, `references/sales.md`) so that only relevant context is loaded for a specific query.

### Pattern 3: Framework/Variant Selection

When supporting multiple variants (e.g., `aws.md`, `gcp.md`), keep the selection guidance in `SKILL.md` and the implementation details in dedicated references.

## Guidelines

- **Avoid deep nesting**: Keep references only one level deep from `SKILL.md`.
- **TOC for long files**: Files >100 lines should have a Table of Contents.
