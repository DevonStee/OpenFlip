---
name: Git Commit Awareness
description: Detect when work is commit-ready and suggest Conventional Commit messages automatically.
last_verified: 2026-01-23
applicable_sdk: Android 14+ (API 34+)
dependencies:
  - codebase-aware-implementation
  - ai-collab-workflow
---

# Skill: Git Commit Awareness

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** codebase-aware-implementation, ai-collab-workflow

## Purpose

Proactively recognize when a feature is complete and automatically suggest or create git commits with meaningful messages.

---

## When to Commit

### ✅ Commit Triggers (Automatic Recognition)

A feature is "commit-ready" when ALL of these are true:

1. **Functional Completeness**
   - Feature works as intended
   - No compilation errors
   - No runtime crashes in basic testing

2. **Code Quality**
   - No obvious bugs or TODO comments left behind
   - Follows project naming conventions
   - No debug `println()` or temporary code

3. **Testing Done**
   - Manual testing completed (if applicable)
   - App builds and runs successfully
   - Core functionality verified

4. **Scope Boundary**
   - Single logical change completed
   - Not mixed with unrelated changes
   - Can be described in one concise sentence

### ❌ Do NOT Commit When

- Feature is partially implemented
- Contains temporary debug code
- Breaking changes without migration path
- Mixed concerns (e.g., "add feature X + fix bug Y" → split into 2 commits)
- Build is broken or tests are failing

---

## Commit Message Standards

Follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```text
<type>(<scope>): <subject>

[optional body]

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### Types

| Type | When to Use | Example |
| ---- | ----------- | ------- |
| `feat` | New feature or enhancement | `feat(settings): add OLED burn-in protection toggle` |
| `fix` | Bug fix | `fix(rotation): eliminate black flash on orientation change` |
| `refactor` | Code restructuring (no behavior change) | `refactor(widget): extract base WidgetProvider class` |
| `perf` | Performance improvement | `perf(rendering): cache text bounds to reduce allocations` |
| `style` | Code style/formatting (no logic change) | `style: apply ktlint formatting` |
| `docs` | Documentation only | `docs: update README with widget installation steps` |
| `test` | Add or fix tests | `test(clock): add unit tests for time formatting` |
| `chore` | Build/tooling changes | `chore: update Gradle to 8.10` |

### Scope Guidelines

Use project-specific scopes:

- `settings` - Settings UI/logic
- `widget` - Widget providers
- `clock` - Clock rendering
- `theme` - Theme system
- `animation` - Animation logic
- `build` - Build configuration

### Subject Rules

- Use imperative mood ("add", not "added" or "adds")
- No capitalization of first letter
- No period at the end
- Max 50 characters
- Be specific but concise

---

## AI Assistant Workflow

### Step 1: Detect Completion

After implementing a feature, ask yourself:

```text
✓ Does the app build?
✓ Did I test the feature?
✓ Is this a logical stopping point?
✓ Are there no temporary hacks left?
```

If all YES → **Trigger commit awareness**

### Step 2: Prepare Commit

```bash
# 1. Check git status
git status

# 2. Review changes
git diff

# 3. Check recent commits for message style
git log --oneline -5
```

### Step 3: Suggest to User

**Template:**

```text
I've completed implementing [feature description]. This is a good commit point.

Should I create a commit with this message?

---
[proposed commit message]
---

If you approve, I'll:
1. Stage the relevant files
2. Create the commit
3. Verify with git log
```

### Step 4: Execute Commit (After Approval)

```bash
# Stage files (be selective!)
git add [specific files]

# Create commit with heredoc for multi-line message
git commit -m "$(cat <<'EOF'
feat(scope): concise description

Optional body explaining why this change was made.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
EOF
)"

# Verify
git log -1 --stat
```

---

## Examples

### Example 1: Single Feature

**Scenario:** Just added OLED protection toggle

```bash
# Detect: Feature complete, tested, no TODOs
# Propose:
feat(settings): add OLED burn-in protection toggle

Implements pixel shifting with configurable interval to prevent
OLED burn-in on always-on clock displays.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### Example 2: Bug Fix

**Scenario:** Fixed rotation black flash

```bash
fix(rotation): eliminate black flash during orientation change

Use windowBackground with hardcoded color instead of theme attribute
to prevent flash when activity recreates.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### Example 3: Refactor (No Behavior Change)

**Scenario:** Extracted widget base class

```bash
refactor(widget): extract BaseWidgetProvider class

Reduces duplication across 5 widget providers by extracting
common update logic into shared base class.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

---

## Red Flags (Hold the Commit)

### 🚨 Multiple Unrelated Changes

**Bad:**

```text
feat: add OLED toggle, fix rotation bug, update README
```

**Good:** Split into 3 commits:

```text
feat(settings): add OLED burn-in protection toggle
fix(rotation): eliminate black flash
docs: update README with new OLED feature
```

### 🚨 Temporary Code Still Present

```kotlin
// TODO: Remove debug logging
Log.d("DEBUG", "This is temporary")
```

**Action:** Remove debug code BEFORE committing

### 🚨 Broken Build

```bash
./gradlew build
# > Task :app:compileDebugKotlin FAILED
```

**Action:** Fix build errors BEFORE committing

---

## Integration with Project Workflow

### After Each Feature Implementation

1. ✅ **Run build**: `./gradlew installDebug`
2. ✅ **Test on device**: Deploy and verify
3. ✅ **Review changes**: `git diff`
4. ✅ **Propose commit**: Ask user for approval
5. ✅ **Execute commit**: Stage + commit + verify

### Commit Frequency Guidelines

- **Too Frequent**: Every file change (noisy history)
- **Too Infrequent**: Multiple features in one commit (hard to review/revert)
- **Just Right**: One logical feature per commit (atomic, revertable)

**Rule of Thumb:** If you can't describe the change in one sentence → it's too big, split it.

---

## Tools and Helpers

### Check for Uncommitted Work

```bash
# Quick status check
git status --short

# See what changed
git diff --stat

# Check for untracked files
git ls-files --others --exclude-standard
```

### Validate Commit Message

```bash
# Check if message follows conventions
echo "feat(settings): add toggle" | grep -E "^(feat|fix|refactor|perf|style|docs|test|chore)(\(.+\))?: .+"
```

### Review Commit Before Push

```bash
# See last commit details
git show HEAD

# See commit with file changes
git log -1 --stat

# Amend last commit if needed (ONLY if not pushed!)
git commit --amend
```

---

## Anti-Patterns to Avoid

### ❌ "WIP" Commits

```text
git commit -m "WIP"
git commit -m "more changes"
git commit -m "fix"
```

**Why Bad:** Pollutes history, hard to understand what changed

### ❌ Massive Multi-File Commits

```text
100 files changed, 5000 insertions(+), 3000 deletions(-)
```

**Why Bad:** Impossible to review, risky to revert

### ❌ Vague Messages

```text
git commit -m "update code"
git commit -m "fix bug"
git commit -m "changes"
```

**Why Bad:** Future developers (including you) won't understand what changed

---

## Summary Checklist

Before creating a commit, verify:

- [ ] Feature is functionally complete
- [ ] App builds without errors
- [ ] Basic testing done
- [ ] No debug code left behind
- [ ] Commit message follows Conventional Commits format
- [ ] Changes are focused on single logical unit
- [ ] User has approved the commit (if AI-assisted)

---

## Related Skills

- [AI Collaboration Workflow](../ai-collab-workflow/SKILL.md) - Communication patterns
- [Best Practice Check](../best-practice-check/SKILL.md) - Code quality verification
