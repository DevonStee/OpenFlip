# Skill: Commit Batching & Scale

## Intent
Give coding agents a repeatable rule set for when and how to commit changes, so work stays traceable for multi-agent handoff without creating noisy micro-commits.

## Scale (pick one per batch)
- **Micro**: Single-file hotfix (typo/import/revert), only when unblocking a failing build or CI red. Avoid stacking unrelated micro-commits.
- **Small** (default): One clear intent, explainable in a sentence. Examples: “normalize resource names + update references”; “move UI controllers to ui/controller”; “update architecture doc and check script”.
- **Medium**: Multiple files but same domain/intent (e.g., refactor all UI collaborators, or migrate a feature end-to-end). Must include validation (assemble/lint/tests as relevant).
- **Never**: Mixed grab-bag across domains (resources + business logic + docs). Split instead.

## Rules
1) Commit **only** on explicit request.
2) One intent per commit; if you can’t title it clearly, split.
3) Run matching verification before committing (in this repo: at least `./gradlew assembleDebug`; if touching resources/layouts/themes, add `./gradlew lintDebug`). Note pre-existing failures if any.
4) Write the message as “what/why”, not “misc fixes”. Prefer Conventional Commit style when allowed (e.g., `chore: normalize resource naming`).
5) Don’t batch unrelated cleanups “because you’re there”; log them for later or separate commits.
6) For large refactors, land in slices that keep the app building/usable between commits.

## Handoff Checklist
- Note which batch(es) are pending and their intended scope.
- If verification not run, say why and what to run.
- Point to high-risk areas (cross-package moves, resource renames) so the next agent knows where to re-verify.

## Examples (for this project)
- **Small**: `chore: normalize widget preview assets` (rename previews + update xml references; lint/assemble run).
- **Small**: `refactor: move ui controllers under ui/controller` (imports fixed; assemble/lint run).
- **Small**: `chore: sync architecture docs and scripts to openflip namespace` (ARCHITECTURE.md, check_architecture.sh, ProGuard keep rules; assemble optional).
