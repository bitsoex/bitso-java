#!/bin/bash
#
# Gradle Lint Analysis Script
#
# This script runs the Nebula Gradle Lint Plugin and parses the results.
# It provides a summary of unused and undeclared dependencies.
#
# Prerequisites:
#   - Gradle Lint Plugin must be configured (run gradle-lint-setup.sh first)
#
# Usage:
#   ./gradle-lint-analyze.sh [project-dir] [options]
#
# Options:
#   --report-only     Generate report without showing detailed output
#   --fix             Run fixGradleLint (CAUTION: review changes!)
#   --dry-run         Show what fixGradleLint would do without applying
#
# Examples:
#   ./gradle-lint-analyze.sh                    # Analyze current project
#   ./gradle-lint-analyze.sh /path/to/project   # Analyze specific project
#   ./gradle-lint-analyze.sh --fix              # Auto-fix (with caution)
#

# Don't exit on grep failures
# set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Determine the root directory
if [ -n "$1" ] && [ -d "$1" ]; then
    ROOT_DIR="$1"
    shift
else
    ROOT_DIR=$(pwd)
    while [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ] && [ "$ROOT_DIR" != "/" ]; do
        ROOT_DIR=$(dirname "$ROOT_DIR")
    done

    if [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ]; then
        echo -e "${RED}Error: Could not find Gradle project root${NC}"
        exit 1
    fi
fi

cd "$ROOT_DIR" || { echo -e "${RED}Error: Could not change to project directory${NC}"; exit 1; }

# Parse options
MODE="analyze"
if [[ "$1" == "--report-only" ]]; then
    MODE="report"
elif [[ "$1" == "--fix" ]]; then
    MODE="fix"
elif [[ "$1" == "--dry-run" ]]; then
    MODE="dry-run"
fi

echo ""
echo "======================================================================="
echo "  GRADLE LINT ANALYSIS"
echo "  Project: $ROOT_DIR"
echo "======================================================================="
echo ""

# Check if plugin is configured
check_plugin_configured() {
    if ! grep -q "gradleLintPluginVersion" "$ROOT_DIR/gradle.properties" 2>/dev/null; then
        echo -e "${RED}Error: Gradle Lint Plugin is not configured${NC}"
        echo ""
        echo "Run the setup script first:"
        echo "  bash /path/to/ai-code-instructions/java/scripts/gradle-lint-setup.sh"
        echo ""
        exit 1
    fi
    
    local version=$(grep "gradleLintPluginVersion=" "$ROOT_DIR/gradle.properties" | cut -d= -f2)
    echo -e "${CYAN}Plugin version:${NC} ${version}"
    echo ""
}

# Run lint analysis
run_lint_analysis() {
    echo -e "${BLUE}Running lint analysis...${NC}"
    echo ""
    
    local temp_output=$(mktemp)
    
    # Run lintGradle and capture output
    if ./gradlew lintGradle 2>&1 | tee "$temp_output"; then
        echo ""
        echo -e "${GREEN}Lint analysis completed successfully${NC}"
    else
        echo ""
        echo -e "${YELLOW}Lint analysis completed with violations${NC}"
    fi
    
    echo ""
    echo "======================================================================="
    echo -e "${BLUE}SUMMARY${NC}"
    echo "======================================================================="
    echo ""
    
    # Parse and summarize results
    local unused_count=$(grep -c "unused-dependency" "$temp_output" 2>/dev/null || echo "0")
    local undeclared_count=$(grep -c "undeclared-dependency" "$temp_output" 2>/dev/null || echo "0")
    local total_violations=$(grep -oE "[0-9]+ problems" "$temp_output" | grep -oE "[0-9]+" || echo "0")
    
    echo -e "  ${RED}Unused dependencies:${NC}     $unused_count"
    echo -e "  ${YELLOW}Undeclared dependencies:${NC} $undeclared_count"
    echo -e "  ${CYAN}Total violations:${NC}        $total_violations"
    echo ""
    
    # Extract unique dependencies mentioned
    echo -e "${BLUE}Unused Dependencies (removable):${NC}"
    grep "unused-dependency" "$temp_output" | grep "unused and can be removed" | sort -u | while read -r line; do
        local dep=$(echo "$line" | grep -oE "Src=\[.*\]" | sed 's/Src=\[//' | sed 's/\]//')
        if [ -n "$dep" ]; then
            echo -e "  ${RED}❌${NC} $dep"
        fi
    done
    echo ""
    
    echo -e "${BLUE}Empty Artifacts (Spring Starters - safe to ignore):${NC}"
    grep "unused-dependency" "$temp_output" | grep "artifact is empty" | sort -u | while read -r line; do
        local dep=$(echo "$line" | grep -oE "Src=\[.*\]" | sed 's/Src=\[//' | sed 's/\]//')
        if [ -n "$dep" ]; then
            echo -e "  ${YELLOW}⚠${NC} $dep (starter - no classes, just dependencies)"
        fi
    done
    echo ""
    
    echo -e "${BLUE}Undeclared Dependencies (should be added explicitly):${NC}"
    grep "undeclared-dependency" "$temp_output" | grep -oE "in [a-zA-Z0-9.:_-]+ are required" | sed 's/in //' | sed 's/ are required//' | sort -u | while read -r dep; do
        echo -e "  ${CYAN}➕${NC} $dep"
    done
    echo ""
    
    rm -f "$temp_output"
}

# Generate report
generate_report() {
    echo -e "${BLUE}Generating detailed report...${NC}"
    echo ""
    
    if ./gradlew generateGradleLintReport 2>&1 | grep -E "Generated|BUILD"; then
        echo ""
        local report_dir="$ROOT_DIR/build/reports/gradleLint"
        if [ -d "$report_dir" ]; then
            echo -e "${GREEN}Report generated at:${NC}"
            ls -la "$report_dir"/*.txt 2>/dev/null | while read -r line; do
                local file=$(echo "$line" | awk '{print $NF}')
                echo -e "  ${CYAN}$file${NC}"
            done
        fi
    else
        echo -e "${YELLOW}Report generation had issues - check Gradle output${NC}"
    fi
    echo ""
}

# Run fix
run_fix() {
    echo -e "${BLUE}=======================================================================${NC}"
    echo -e "${BLUE}  AUTO-FIX WORKFLOW${NC}"
    echo -e "${BLUE}=======================================================================${NC}"
    echo ""
    echo -e "${CYAN}This will:${NC}"
    echo "  1. Run fixGradleLint to apply automatic fixes"
    echo "  2. Show you what changed"
    echo "  3. Guide you through cleanup steps"
    echo ""
    echo -e "${YELLOW}Note:${NC} After auto-fix, you'll need to:"
    echo "  - Replace hardcoded versions with libs.* references"
    echo "  - Verify Lombok compileOnly wasn't removed"
    echo ""
    read -p "Continue with auto-fix? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${CYAN}Fix cancelled${NC}"
        return
    fi
    
    echo ""
    echo -e "${BLUE}Step 1: Running fixGradleLint...${NC}"
    echo ""
    
    ./gradlew fixGradleLint 2>&1 | grep -E "fixed|needs fixing|BUILD" || true
    
    echo ""
    echo -e "${GREEN}Step 2: Changes applied:${NC}"
    echo ""
    git diff --stat 2>/dev/null || echo "(not a git repository)"
    
    echo ""
    echo -e "${YELLOW}Step 3: Find hardcoded versions to replace:${NC}"
    echo ""
    echo "Run this command to find hardcoded dependencies:"
    echo ""
    echo -e "  ${CYAN}grep -rn \"implementation '[a-zA-Z]\" --include=\"build.gradle\" . | grep -v \"libs\\.\" | grep \":\"${NC}"
    echo ""
    
    # Actually run the grep to show results
    local hardcoded=$(grep -rn "implementation '[a-zA-Z]" --include="build.gradle" . 2>/dev/null | grep -v "libs\." | grep ":" || true)
    if [ -n "$hardcoded" ]; then
        echo -e "${RED}Found hardcoded dependencies:${NC}"
        echo "$hardcoded" | head -20
        echo ""
        echo -e "${YELLOW}Replace these with version catalog references (libs.*)${NC}"
    else
        echo -e "${GREEN}No hardcoded dependencies found!${NC}"
    fi
    
    echo ""
    echo -e "${YELLOW}Step 4: Check Lombok wasn't broken:${NC}"
    echo ""
    
    # Check for missing Lombok compileOnly
    local lombok_issues=""
    while IFS= read -r line; do
        local file=$(echo "$line" | cut -d: -f1)
        if [ -n "$file" ] && ! grep -q "compileOnly.*lombok" "$file" 2>/dev/null; then
            lombok_issues="${lombok_issues}${file}\n"
        fi
    done < <(grep -rn "annotationProcessor.*lombok" --include="build.gradle" . 2>/dev/null || true)
    
    if [ -n "$lombok_issues" ]; then
        echo -e "${RED}Missing Lombok compileOnly in:${NC}"
        echo -e "$lombok_issues"
        echo ""
        echo "Add this line to each file:"
        echo "  compileOnly 'org.projectlombok:lombok'"
    else
        echo -e "${GREEN}Lombok configuration looks correct!${NC}"
    fi
    
    echo ""
    echo -e "${BLUE}Step 5: Verify build:${NC}"
    echo ""
    echo "  ./gradlew build -x test"
    echo ""
}

# Dry run
run_dry_run() {
    echo -e "${BLUE}Running dry-run (no changes will be made)...${NC}"
    echo ""
    
    ./gradlew fixGradleLint --dry-run 2>&1 | grep -E "SKIPPED|:fixGradleLint"
    
    echo ""
    echo -e "${CYAN}Dry-run complete - no files were modified${NC}"
    echo ""
}

# Main
check_plugin_configured

case "$MODE" in
    analyze)
        run_lint_analysis
        generate_report
        ;;
    report)
        generate_report
        ;;
    fix)
        run_fix
        ;;
    dry-run)
        run_dry_run
        ;;
esac

echo "======================================================================="
echo ""
echo -e "${CYAN}Next steps:${NC}"
echo ""
echo "  1. Run auto-fix:  ./gradlew fixGradleLint"
echo "  2. Find hardcoded versions:"
echo "     grep -rn \"implementation '[a-zA-Z]\" --include=\"build.gradle\" . | grep -v \"libs\\.\" | grep \":\""
echo "  3. Replace hardcoded versions with libs.* references"
echo "  4. Add missing libraries to gradle/libs.versions.toml"
echo "  5. Check Lombok compileOnly wasn't removed"
echo "  6. Verify build: ./gradlew build -x test"
echo ""
echo -e "${YELLOW}Ignore these warnings (they're expected):${NC}"
echo "  - 'empty artifact' (Spring Boot starters)"
echo "  - Lombok 'unused' warnings"
echo ""
echo "View detailed report:"
echo "  cat build/reports/gradleLint/*.txt"
echo ""

# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → java/scripts/gradle-lint-analyze.sh
# To modify, edit the source file and run the distribution workflow

