---
name: Code Quality Audit
description: Systematic code review after feature development - structure, hygiene, separation of concerns, and performance
---

# Skill: Code Quality Audit

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** best-practice-check, codebase-aware-implementation

## Purpose

After completing a development phase, perform a comprehensive code quality audit focusing on production code (excluding test files). This skill provides a systematic approach to identify technical debt, architectural issues, and potential bugs before they become problems.

## When to Use

- After completing a major feature or development milestone
- Before releasing a new version
- When preparing for code review or handoff
- When technical debt needs assessment
- Periodically (e.g., monthly) for ongoing projects

## Audit Dimensions

### 1. File Structure & Organization (Project Structure)

**Objective**: Ensure logical, maintainable project organization

**Check Points**:

- [ ] Files are in correct packages matching their purpose
- [ ] Package naming follows conventions (`feature`, `ui`, `data`, `domain`, etc.)
- [ ] No orphaned files in wrong locations
- [ ] Resource files properly organized by type and qualifier
- [ ] Clear separation between layers (presentation, domain, data)
- [ ] Consistent naming conventions across packages

**Commands to Run**:

```bash
# List all Kotlin/Java files with their packages
find ./app/src/main -type f \( -name "*.kt" -o -name "*.java" \) -exec head -1 {} \; -print

# Find files that might be misplaced (e.g., Activities not in ui package)
grep_search for class definitions and check package declarations
```

**Review Questions**:

- Does each package have a clear, single responsibility?
- Are feature modules properly isolated?
- Should any files be moved to better reflect their purpose?
- Is the resource organization intuitive?

---

### 2. Code Hygiene (Code Cleanliness)

**Objective**: Remove clutter and ensure code quality standards

**Check Points**:

- [ ] No commented-out code blocks (use version control instead)
- [ ] No TODO/FIXME comments older than current sprint
- [ ] No hardcoded strings (should be in `strings.xml`)
- [ ] No hardcoded colors (should be in `colors.xml` or theme)
- [ ] No hardcoded dimensions (should be in `dimens.xml`)
- [ ] No magic numbers (extract to constants)
- [ ] No unused imports
- [ ] No unused resources (layouts, drawables, strings)
- [ ] Consistent code formatting
- [ ] Meaningful variable/function names matching project conventions
- [ ] No debug logs left in production code
- [ ] No temporary files (`.tmp`, backup files)

**Commands to Run**:

```bash
# Find hardcoded strings in Kotlin files
grep_search for string literals in .kt files (exclude test files)

# Find TODO/FIXME comments
grep_search for "TODO" and "FIXME" in source files

# Find commented code blocks
grep_search for "//.*fun " or "//.*val " patterns

# Find debug logs
grep_search for "Log.d", "Log.v", "println" in source files

# Check for unused resources (use Android Lint)
./gradlew lint
```

**Review Questions**:

- Are all string resources properly named and organized?
- Do constant names follow UPPER_SNAKE_CASE convention?
- Are dimension values semantic (e.g., `button_height` not `size_48dp`)?
- Is there dead code that can be removed?

---

### 3. Separation of Concerns (Architecture)

**Objective**: Ensure proper layering and responsibility distribution

**Check Points**:

- [ ] No business logic in Activities/Fragments
- [ ] No direct database/network calls in UI layer
- [ ] ViewModels don't hold Context references
- [ ] Custom Views handle only rendering, not business logic
- [ ] Repositories abstract data sources properly
- [ ] Use cases/interactors contain reusable business logic
- [ ] No God classes (classes doing too much)
- [ ] Proper dependency injection (not manual instantiation)
- [ ] Clear data flow (UI → ViewModel → Repository → DataSource)

**Commands to Run**:

```bash
# Find Activities/Fragments with potential business logic
view_file_outline for each Activity/Fragment and check method complexity

# Find ViewModels holding Context
grep_search for "Context" in ViewModel files

# Find large classes (potential God classes)
# Check line counts and method counts per class
```

**Review Questions**:

- Can each class be described in one sentence?
- Are responsibilities clearly separated between layers?
- Would a new developer understand the architecture from file organization?
- Are there any circular dependencies?
- Is dependency injection used consistently?

**Red Flags**:

- Activities/Fragments > 300 lines
- Methods > 50 lines
- Classes with > 10 dependencies
- Direct use of `Context` in non-UI classes
- Database queries in ViewModels
- Network calls in UI layer

---

### 4. Performance & Memory (Critical Issues)

**Objective**: Identify memory leaks and performance bottlenecks

**Check Points**:

#### Memory Leaks

- [ ] No Activity/Fragment references held in static fields
- [ ] No Context leaks in singletons
- [ ] Listeners/callbacks properly unregistered
- [ ] Coroutines properly scoped (viewModelScope, lifecycleScope)
- [ ] Handlers use WeakReference or are cleared
- [ ] No anonymous inner classes holding implicit references
- [ ] Bitmaps properly recycled (if not using modern libraries)
- [ ] Streams/Cursors/Files properly closed (use `use {}`)

#### Performance Issues

- [ ] No blocking operations on main thread
- [ ] Database queries use proper indexing
- [ ] RecyclerView ViewHolders properly recycled
- [ ] Images loaded with appropriate libraries (Glide/Coil)
- [ ] No N+1 query problems
- [ ] Proper use of `lazy` for expensive initializations
- [ ] No unnecessary object allocations in loops
- [ ] Custom View `onDraw()` has zero allocations

**Commands to Run**:

```bash
# Find static Context references
grep_search for "companion object.*Context" or "static.*Context"

# Find Handler usage without WeakReference
grep_search for "Handler(" in source files

# Find potential main thread blocking
grep_search for "Thread.sleep", ".get()" on futures, blocking IO

# Find unclosed resources
grep_search for "FileInputStream", "Cursor", "InputStream" without ".use"

# Find allocations in onDraw
view_code_item for custom Views and check onDraw methods
```

**Review Questions**:

- Are all lifecycle-aware components properly scoped?
- Are background operations using coroutines or WorkManager?
- Is image loading optimized with caching?
- Are there any potential ANR (Application Not Responding) risks?

**Critical Patterns to Check**:

```kotlin
// ❌ BAD: Context leak
companion object {
    lateinit var context: Context
}

// ✅ GOOD: Application context if needed
companion object {
    lateinit var appContext: Application
}

// ❌ BAD: Listener not unregistered
class MyFragment : Fragment() {
    override fun onViewCreated(...) {
        someManager.addListener(this)
    }
}

// ✅ GOOD: Proper cleanup
class MyFragment : Fragment() {
    override fun onViewCreated(...) {
        someManager.addListener(this)
    }
    override fun onDestroyView() {
        someManager.removeListener(this)
        super.onDestroyView()
    }
}

// ❌ BAD: Allocation in onDraw
override fun onDraw(canvas: Canvas) {
    val paint = Paint() // Creates new object every frame!
    canvas.drawCircle(x, y, radius, paint)
}

// ✅ GOOD: Reuse objects
private val paint = Paint()
override fun onDraw(canvas: Canvas) {
    canvas.drawCircle(x, y, radius, paint)
}
```

---

## Execution Workflow

### Step 1: Preparation

1. Ensure all code is committed to version control
2. Run a clean build: `./gradlew clean build`
3. Run lint: `./gradlew lint` and review the report
4. Note current project state (branch, last commit)

### Step 2: Automated Checks

Run these commands and collect output:

```bash
# Find all production source files
find ./app/src/main -type f -name "*.kt" | grep -v Test

# Check for common issues
grep -r "TODO" app/src/main --include="*.kt"
grep -r "FIXME" app/src/main --include="*.kt"
grep -r "Log\.[dv]" app/src/main --include="*.kt"
grep -r "println" app/src/main --include="*.kt"

# Find large files (potential refactoring candidates)
find ./app/src/main -name "*.kt" -exec wc -l {} \; | sort -rn | head -20

# Check for unused resources (requires Android Studio or gradlew)
./gradlew lint
```

### Step 3: Manual Review

For each dimension (1-4 above):

1. Review check points systematically
2. Use `view_file_outline` to understand class structure
3. Use `view_code_item` to examine specific methods
4. Use `grep_search` to find patterns
5. Document findings with file names and line numbers

### Step 4: Generate Report

Create a markdown report with:

```markdown
# Code Quality Audit Report
**Date**: [Current Date]
**Commit**: [Git commit hash]
**Auditor**: [Your name or "AI Assistant"]

## Executive Summary
[Brief overview of findings]

## 1. File Structure & Organization
### Issues Found
- [ ] **[Severity]** `path/to/File.kt` - [Description]
  - **Line**: [Line number if applicable]
  - **Suggestion**: [How to fix]

## 2. Code Hygiene
### Issues Found
- [ ] **[Severity]** `path/to/File.kt:123` - [Description]
  - **Suggestion**: [How to fix]

## 3. Separation of Concerns
### Issues Found
- [ ] **[Severity]** `path/to/File.kt:45-67` - [Description]
  - **Suggestion**: [How to fix]

## 4. Performance & Memory
### Critical Issues
- [ ] **HIGH** `path/to/File.kt:89` - [Memory leak description]
  - **Risk**: [Explain the risk]
  - **Fix**: [How to fix]

### Performance Issues
- [ ] **MEDIUM** `path/to/File.kt:123` - [Performance issue]
  - **Impact**: [Explain impact]
  - **Fix**: [How to fix]

## Recommendations
1. [Priority 1 recommendation]
2. [Priority 2 recommendation]
...

## Metrics
- Total files reviewed: X
- Issues found: Y
- Critical issues: Z
- Estimated effort: [hours/days]
```

### Step 5: Prioritization

Categorize findings by severity:

- **CRITICAL**: Memory leaks, crashes, security issues
- **HIGH**: Performance problems, architectural violations
- **MEDIUM**: Code hygiene, minor refactoring
- **LOW**: Naming conventions, formatting

---

## Output Format

For each issue found, provide:

1. **Severity**: CRITICAL | HIGH | MEDIUM | LOW
2. **Category**: Structure | Hygiene | Architecture | Performance
3. **File**: Full path to the file
4. **Line(s)**: Specific line numbers
5. **Issue**: Clear description of the problem
6. **Impact**: Why this matters
7. **Suggestion**: Concrete fix with code example if applicable

**Example**:

```markdown
❌ **HIGH** - Performance & Memory
📁 `app/src/main/java/com/example/ui/MainActivity.kt:45-52`
🔍 **Issue**: Coroutine launched with GlobalScope instead of lifecycleScope
💥 **Impact**: Potential memory leak - coroutine continues after Activity destroyed
✅ **Fix**: Replace `GlobalScope.launch` with `lifecycleScope.launch`

// Before
GlobalScope.launch {
    repository.loadData()
}

// After
lifecycleScope.launch {
    repository.loadData()
}
```

---

## Best Practices

1. **Exclude Test Files**: Use grep/find patterns to exclude `*Test.kt`, `androidTest/`, `test/`
2. **Focus on Impact**: Prioritize issues that affect users or stability
3. **Be Specific**: Always provide file paths and line numbers
4. **Provide Examples**: Show before/after code for fixes
5. **Consider Context**: Some patterns are acceptable in specific contexts
6. **Batch Similar Issues**: Group similar issues together for efficiency
7. **Verify Suggestions**: Ensure suggested fixes are compatible with project architecture

---

## Tools Integration

### Android Lint

```bash
./gradlew lint
# Review: app/build/reports/lint-results.html
```

### Detekt (if configured)

```bash
./gradlew detekt
```

### Manual Inspection Tools

- `view_file_outline`: Get class structure overview
- `view_code_item`: Examine specific methods
- `grep_search`: Find patterns across codebase
- `find_by_name`: Locate files by name/pattern

---

## Common Anti-Patterns to Check

### Android-Specific

1. **Context Leaks**: Static references, singletons holding Activity context
2. **Lifecycle Violations**: Operations after lifecycle destroyed
3. **Main Thread Blocking**: Network/DB on UI thread
4. **Resource Leaks**: Unclosed Cursors, Streams, Bitmaps
5. **Memory Churn**: Allocations in `onDraw()`, `onMeasure()`

### General Code Quality

1. **God Classes**: Classes > 500 lines or > 20 methods
2. **Long Methods**: Methods > 50 lines
3. **Deep Nesting**: > 3 levels of indentation
4. **Magic Numbers**: Unexplained numeric literals
5. **Hardcoded Values**: Strings, colors, dimensions in code
6. **Dead Code**: Unused methods, classes, resources
7. **Inconsistent Naming**: Mixed conventions in same project

---

## Exclusions

**Do NOT audit**:

- Test files (`*Test.kt`, `androidTest/`, `test/`)
- Generated code (`build/`, `.gradle/`)
- Third-party libraries (`libs/`)
- Gradle configuration (unless specifically requested)
- Documentation files (unless checking for outdated info)

---

## Success Criteria

A successful audit should:

- ✅ Cover all production source files
- ✅ Identify critical issues (memory leaks, crashes)
- ✅ Provide actionable, specific suggestions
- ✅ Include file paths and line numbers
- ✅ Prioritize findings by severity
- ✅ Estimate effort required for fixes
- ✅ Be delivered in clear, structured format

---

## Follow-Up Actions

After audit completion:

1. Create GitHub issues or task tickets for each finding
2. Prioritize fixes in upcoming sprints
3. Address CRITICAL issues immediately
4. Schedule refactoring for HIGH/MEDIUM issues
5. Consider adding lint rules to prevent recurrence
6. Update coding guidelines based on findings
7. Schedule next audit (recommend: after each major feature)
