#!/bin/bash
set -euo pipefail

# check-naming-conventions.sh
# Validates naming conventions across the codebase
#
# Usage: ./check-naming-conventions.sh [directory]
# Default directory: current directory (.)

SEARCH_DIR="${1:-.}"
ERRORS=0
FILES_CHECKED=0

echo "Checking naming conventions in ${SEARCH_DIR}..."
echo ""

# Check for PascalCase class names in Java files
check_java_classes() {
    local file="$1"
    local basename
    basename=$(basename "$file" .java)
    
    # Class name should match filename and be PascalCase
    if ! [[ "$basename" =~ ^[A-Z][a-zA-Z0-9]*$ ]]; then
        echo "ERROR: $file - Class name '$basename' should be PascalCase"
        ((++ERRORS))
    fi
}

# Check for snake_case in Python files (allows leading underscore for private modules)
check_python_modules() {
    local file="$1"
    local basename
    basename=$(basename "$file" .py)
    
    # Module name should be snake_case (optionally with leading underscore for private)
    if ! [[ "$basename" =~ ^_?[a-z][a-z0-9_]*$ ]] && [[ "$basename" != "__init__" ]]; then
        echo "ERROR: $file - Module name '$basename' should be snake_case"
        ((++ERRORS))
    fi
}

# Check for kebab-case in shell scripts
check_shell_scripts() {
    local file="$1"
    local basename
    basename=$(basename "$file" .sh)
    
    # Script name should be kebab-case
    if ! [[ "$basename" =~ ^[a-z][a-z0-9-]*$ ]]; then
        echo "WARNING: $file - Script name '$basename' should be kebab-case"
    fi
}

# Find and check Java files
if find "$SEARCH_DIR" -name "*.java" -type f 2>/dev/null | head -1 | grep -q .; then
    while IFS= read -r file; do
        check_java_classes "$file"
        ((++FILES_CHECKED))
    done < <(find "$SEARCH_DIR" -name "*.java" -type f 2>/dev/null)
fi

# Find and check Python files
if find "$SEARCH_DIR" -name "*.py" -type f 2>/dev/null | head -1 | grep -q .; then
    while IFS= read -r file; do
        check_python_modules "$file"
        ((++FILES_CHECKED))
    done < <(find "$SEARCH_DIR" -name "*.py" -type f 2>/dev/null)
fi

# Find and check shell scripts
if find "$SEARCH_DIR" -name "*.sh" -type f 2>/dev/null | head -1 | grep -q .; then
    while IFS= read -r file; do
        check_shell_scripts "$file"
        ((++FILES_CHECKED))
    done < <(find "$SEARCH_DIR" -name "*.sh" -type f 2>/dev/null)
fi

echo ""
echo "✓ ${FILES_CHECKED} files checked"

if [[ $ERRORS -eq 0 ]]; then
    echo "✓ All naming conventions followed"
    exit 0
else
    echo "✗ ${ERRORS} naming convention violations found"
    exit 1
fi
