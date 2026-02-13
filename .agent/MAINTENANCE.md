# .agent Directory Maintenance Guide

**Purpose:** Keep AI assistant documentation current and accurate.

---

## Maintenance Triggers

Update `.agent/` documentation when:

### 1. SDK/Platform Upgrades

```bash
# When upgrading compileSdk, targetSdk, or AGP version
# Update: AGENTS.md, relevant skills in skills/

Example:
- compileSdk 34 → 35
- Action: Review Android 15 behavior changes
- Files: skills/android-rotation-antiflicker/, skills/android-widget-development/
```

**Checklist:**
- [ ] Update AGENTS.md "Project Environment" section
- [ ] Update skill "Last Verified" dates
- [ ] Add new platform behavior notes
- [ ] Test code examples still work

---

### 2. Architectural Changes

```bash
# When project architecture changes (e.g., add MVVM, Hilt, Compose)
# Update: ARCHITECTURE.md, AGENTS.md, relevant skills

Example:
- Add ViewModel layer
- Action: Document new pattern, update implementation skills
- Files: ARCHITECTURE.md, skills/codebase-aware-implementation/
```

**Checklist:**
- [ ] Update ARCHITECTURE.md with new patterns
- [ ] Update AGENTS.md coding standards
- [ ] Create new skill if pattern is reusable
- [ ] Update cross-references in skills/README.md

---

### 3. New Best Practices Discovered

```bash
# When finding new patterns or anti-patterns
# Update: Relevant skills, LESSONS_LEARNED → skills/

Example:
- Discover better animation pattern
- Action: Create/update skill with new approach
- Files: skills/android-highperf-customview/
```

**Checklist:**
- [ ] Document pattern in appropriate skill
- [ ] Add examples from project
- [ ] Update "Last Verified" date
- [ ] Cross-reference related skills

---

### 4. LOCKED Rules Changes

```bash
# When modifying critical algorithms or patterns
# Update: AGENTS.md "Critical Rules" section

Example:
- Change FlipCard centering logic
- Action: Update LOCKED rule with new formula
- Files: AGENTS.md
```

**Checklist:**
- [ ] Update LOCKED rule in AGENTS.md
- [ ] Explain why change was necessary
- [ ] Update related code comments
- [ ] Test thoroughly before marking LOCKED

---

## Regular Reviews

### Quarterly (Every 3 Months)

- [ ] Review all skill "Last Verified" dates
- [ ] Check for outdated SDK references
- [ ] Validate code examples still compile
- [ ] Update cross-reference map if needed

### After Major Features

- [ ] Move task.md completed items to history/
- [ ] Update FUTURE_FEATURES.md with new ideas
- [ ] Check if new patterns need skills
- [ ] Verify AGENTS.md still matches codebase

---

## Skill Maintenance Workflow

### When Updating a Skill

1. **Update Metadata**
   ```markdown
   **Last Verified:** [Current Date]
   **Applicable SDK:** [Min-Max SDK range]
   ```

2. **Test Code Examples**
   ```bash
   # Copy-paste code examples into project
   # Verify they compile and run
   ./gradlew build
   ```

3. **Update Cross-References**
   ```bash
   # If skill dependencies change
   # Update skills/README.md cross-reference map
   ```

4. **Commit Changes**
   ```bash
   git add .agent/skills/[skill-name]/
   git commit -m "docs(skills): update [skill-name] for SDK 35

   - Update last verified date
   - Add Android 15 compatibility notes
   - Refresh code examples

   Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
   ```

---

## Skill Lifecycle

### Creating a New Skill

1. **Identify Reusable Pattern**
   - Pattern used 2+ times in project
   - Applicable to other Android projects
   - Clear problem/solution structure

2. **Structure**
   ```markdown
   # Skill: [Name]

   **Last Verified:** [Date]
   **Applicable SDK:** [Range]
   **Dependencies:** [Related skills]

   ## Purpose
   ## When to Use
   ## Prerequisites
   ## Step-by-Step Guide
   ## Examples
   ## Related Skills
   ```

3. **Register Skill**
   - Add to `skills/README.md`
   - Add to `.agent/README.md` table
   - Add to `AGENTS.md` essential skills if mandatory
   - Update cross-reference map

### Archiving a Skill

**When to Archive:**
- Pattern no longer used in project
- Superseded by better approach
- Platform deprecated the technique

**How to Archive:**
```bash
# Move to history instead of deleting
mv .agent/skills/old-skill/ .agent/history/skills/
# Update README.md to remove reference
# Add deprecation note explaining why
```

---

## Quick Reference

### Files That Require Updates

| Trigger | Files to Update |
| ------- | --------------- |
| SDK upgrade | AGENTS.md, skills/android-*/ |
| Architecture change | ARCHITECTURE.md, AGENTS.md, skills/codebase-aware-*/ |
| New pattern discovered | Create new skill or update existing |
| LOCKED rule change | AGENTS.md "Critical Rules" |
| Feature complete | task.md → history/, git commit |
| New tool/workflow | workflows/ |

### Verification Commands

```bash
# Check skill dates
grep -r "Last Verified:" .agent/skills/

# Find outdated references
grep -r "API 3[0-3]" .agent/  # Find old SDK refs

# List all skills
ls -la .agent/skills/*/SKILL.md

# Validate markdown
find .agent -name "*.md" -exec echo "Checking {}" \;
```

---

## AI Assistant Role

When maintaining `.agent/`:

1. **Proactive Updates**
   - After SDK upgrade: Suggest skill updates
   - After new feature: Ask if pattern should become skill
   - After bug fix: Check if LOCKED rule needs update

2. **Quality Checks**
   - Verify code examples compile
   - Check cross-references are valid
   - Ensure "Last Verified" dates are current

3. **User Communication**
   ```
   "I noticed we upgraded to SDK 35. Should I update the
   android-rotation-antiflicker skill with Android 15 notes?"
   ```

---

## Example: Full Update Cycle

**Scenario:** Upgraded from SDK 34 → 35

```bash
# 1. Update project metadata
# Edit: AGENTS.md
- **Target SDK**: 34 → 35

# 2. Check Android 15 changes
# Research: https://developer.android.com/about/versions/15

# 3. Update affected skills
# Edit: skills/android-rotation-antiflicker/SKILL.md
**Last Verified:** 2026-01-23
**Applicable SDK:** Android 8+ (API 26+), tested through Android 15 (API 35)

## Android 15+ Considerations
- Predictive back gesture affects window transitions
- ...

# 4. Test examples
./gradlew build
./gradlew installDebug

# 5. Commit
git commit -m "docs: update skills for Android 15 (SDK 35)

- Update AGENTS.md target SDK
- Add Android 15 notes to rotation and widget skills
- Verify all code examples still work

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Summary

**Key Principles:**
- Keep skills synchronized with project state
- Update "Last Verified" dates when testing
- Archive obsolete patterns instead of deleting
- Document why LOCKED rules changed
- Commit documentation changes with meaningful messages

**Goal:** AI assistants always have current, accurate information for effective collaboration.
