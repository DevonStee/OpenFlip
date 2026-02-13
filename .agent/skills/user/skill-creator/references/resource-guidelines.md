# Resource Management Guidelines

## Scripts (`scripts/`)

极 deterministic reliability or repeated code writing.

- **Benefits**: Token efficient, deterministic.
- **Rule**: Added scripts **must** be tested by actually running them.

## References (`references/`)

Use for documentation intended to be loaded as needed.

- **Typical contents**: Database schemas, API docs, detailed workflow guides.
- **Best Practice**: If files are large (>10k words), include grep search patterns in `SKILL.md`.
- **Avoid Duplication**: Information should live in either `SKILL.md` or a reference file, not both.

## Assets (`assets/`)

Files used in final output, not intended to be loaded into context.

- **Examples**: Templates, images, icons, boilerplate projects.
- **Benefit**: Separate output resources from documentation bloat.

---

## What NOT to Include

To minimize clutter and confusion, do **NOT** include:

- `README.md`, `CHANGELOG.md`, `INSTALLATION_GUIDE.md`, etc.
- Auxiliary context about the creation process, setup/testing procedures (for people), or user-facing documentation.

The skill should only contain what an **AI agent** needs to perform the task.
