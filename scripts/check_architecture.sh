#!/bin/bash
# Architecture Check Script for OpenFlip Android
# This script is intended to run in CI to catch architecture violations early.

echo "Running Architecture Compliance Check..."

VIOLATIONS=0

# Rule 1: 'view' should not depend on 'ui' or 'widget'
# Checking for imports of .ui or .widget inside .view package
echo "Checking Rule 1: Package 'view' must not import 'ui' or 'widget' packages..."
VIEW_LEAKS=$(grep -rE "import com.bokehforu.openflip\.(ui|widget)" app/src/main/java/com/bokehforu/openflip/view/)
if [ ! -z "$VIEW_LEAKS" ]; then
    echo "[FAIL] Architecture Violation: 'view' package is importing 'ui' or 'widget' types:"
    echo "$VIEW_LEAKS"
    VIOLATIONS=$((VIOLATIONS+1))
else
    echo "[PASS] 'view' package is clean."
fi

# Rule 2: Sub-packages should not be accessed by Main package unless they are managers/interfaces
# (Simple count check for now, can be expanded)
echo "Checking Rule 2: Minimizing direct access to sub-package implementation details..."
SUB_ACCESS=$(grep -r "com.bokehforu.openflip\." app/src/main/java/com/bokehforu/openflip/ | grep -v "import" | grep "view\." | wc -l)
if [ "$SUB_ACCESS" -gt 100 ]; then
    echo "[WARN] High volume of direct sub-package access detected ($SUB_ACCESS hits). Consider using more interfaces."
fi

# Summary
if [ $VIOLATIONS -eq 0 ]; then
    echo "SUCCESS: No critical architecture violations found."
    exit 0
else
    echo "FAILURE: $VIOLATIONS critical violation(s) found. Please refer to .agent/ARCHITECTURE.md"
    exit 1
fi
