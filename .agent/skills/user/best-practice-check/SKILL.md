---
name: Best Practice Check
description: Analyze a feature or component to ensure it follows project best practices before implementation or review.
last_verified: 2026-01-23
applicable_sdk: Android 14+ (API 34+)
dependencies:
  - codebase-aware-implementation
  - code-quality-audit
---

# Skill: Best Practice Check

**Last Verified:** 2026-01-23
**Applicable SDK:** Android 14+ (API 34+)
**Dependencies:** codebase-aware-implementation, code-quality-audit

## Purpose

This skill analyzes a specific feature or component in the project to determine if it follows best practices.

## Usage

```text
/best-practice-check <feature-name-or-file-path>
```

## Analysis Areas

This skill will analyze:

1. **Code Organization**
   - Separation of concerns
   - Single Responsibility Principle
   - Component/class size and complexity

2. **State Management**
   - State synchronization patterns
   - Data flow consistency
   - Side effect handling

3. **Architecture Patterns**
   - MVC/MVVM/MVP adherence
   - Dependency injection usage
   - Controller/Manager separation

4. **Error Handling**
   - Edge case coverage
   - Null safety
   - Exception handling

5. **Performance**
   - Memory leaks prevention
   - Unnecessary recomposition/redraws
   - Resource cleanup

6. **User Experience**
   - Interaction consistency
   - Visual feedback
   - State persistence

## Output Format

The skill will provide:

- ✅ **Strengths**: What is well-implemented
- ⚠️ **Concerns**: Potential issues or non-optimal patterns
- 💡 **Recommendations**: Concrete suggestions for improvement
- 📊 **Comparison**: Alternative approaches if applicable

## Example

```text
/best-practice-check light toggle feature
```

Will analyze:

- LightToggleController.kt
- UIStateController.kt (light button visibility logic)
- StateToggleGlowView.kt
- Related state management in SettingsCoordinator.kt

---

## Implementation Instructions

When this skill is invoked:

1. **Identify the feature scope**
   - Ask user to clarify if needed
   - Find all related files using Grep/Glob
   - Read the main implementation files

2. **Analyze the code**
   - Check state management patterns
   - Identify potential race conditions
   - Look for coupling issues
   - Verify lifecycle management
   - Check consistency with similar features

3. **Compare with best practices**
   - Android/Kotlin best practices
   - Project-specific patterns
   - Common anti-patterns

4. **Generate report**
   - Structure findings clearly
   - Include code references (file:line)
   - Provide actionable recommendations
   - Show alternative implementations if applicable

5. **Write analysis to file**
   - Save to `.agent/<feature-name>_best_practice_analysis.md`
   - Include timestamp and version info

## Analysis Template

```markdown
# Best Practice Analysis: <Feature Name>

**Date**: YYYY-MM-DD
**Analyzed Files**:
- file1.kt
- file2.kt

## Executive Summary
Brief overview of findings.

## Detailed Analysis

### 1. Code Organization
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

### 2. State Management
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

### 3. Architecture Patterns
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

### 4. Error Handling
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

### 5. Performance
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

### 6. User Experience
- Current approach: ...
- Evaluation: ✅/⚠️/❌
- Details: ...

## Strengths
1. ...
2. ...

## Concerns
1. Issue description
   - Location: file.kt:line
   - Impact: ...
   - Risk level: Low/Medium/High

## Recommendations

### High Priority
1. Recommendation
   - Why: ...
   - How: ...
   - Code example: ...

### Medium Priority
...

### Low Priority / Nice to Have
...

## Alternative Approaches

### Approach A: <Name>
**Pros**:
- ...

**Cons**:
- ...

**Code example**:
```kotlin
// ...
```

### Approach B: Name

**Pros**:

- ...

**Cons**:

- ...

## Conclusion

Final assessment and recommended action.

```text
```

## Tips

- Be objective and constructive
- Reference actual code with file:line notation
- Provide concrete examples, not just theory
- Consider the project's existing patterns
- Balance idealism with pragmatism
- Highlight what's working well, not just problems
