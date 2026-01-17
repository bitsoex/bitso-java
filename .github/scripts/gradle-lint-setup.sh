#!/bin/bash
#
# Gradle Lint Plugin Setup Script
#
# This script integrates the Nebula Gradle Lint Plugin into a Gradle project.
# The plugin provides static analysis for unused dependencies, undeclared dependencies,
# and other build hygiene issues.
#
# Usage:
#   ./gradle-lint-setup.sh [project-dir]
#
# What it does:
#   1. Detects Gradle version and selects appropriate plugin version
#   2. Adds plugin to settings.gradle
#   3. Configures lint rules in build.gradle
#   4. Configures JVM memory for lint analysis
#
# Plugin versions:
#   - Gradle 9.x: Uses v21.1.3
#   - Gradle 8.x: Uses v20.6.2
#
# Repository: https://github.com/nebula-plugins/gradle-lint-plugin
#

set -e

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
else
    ROOT_DIR=$(pwd)
    while [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ] && [ "$ROOT_DIR" != "/" ]; do
        ROOT_DIR=$(dirname "$ROOT_DIR")
    done

    if [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ]; then
        echo -e "${RED}Error: Could not find Gradle project root (settings.gradle)${NC}"
        exit 1
    fi
fi

echo ""
echo "======================================================================="
echo "  GRADLE LINT PLUGIN SETUP"
echo "  Project: $ROOT_DIR"
echo "======================================================================="
echo ""

# Detect Gradle version
detect_gradle_version() {
    local wrapper_props="$ROOT_DIR/gradle/wrapper/gradle-wrapper.properties"
    if [ -f "$wrapper_props" ]; then
        local version=$(grep -oE 'gradle-[0-9]+\.[0-9]+(\.[0-9]+)?' "$wrapper_props" | head -1 | grep -oE '[0-9]+\.[0-9]+(\.[0-9]+)?')
        echo "$version"
    else
        echo ""
    fi
}

GRADLE_VERSION=$(detect_gradle_version)
GRADLE_MAJOR=$(echo "$GRADLE_VERSION" | cut -d. -f1)

echo -e "${CYAN}Detected Gradle version:${NC} ${GRADLE_VERSION:-unknown}"

# Select plugin version based on Gradle version
if [ "$GRADLE_MAJOR" -ge 9 ] 2>/dev/null; then
    LINT_PLUGIN_VERSION="21.1.3"
    echo -e "${GREEN}Using plugin version ${LINT_PLUGIN_VERSION} (for Gradle 9.x)${NC}"
else
    # Gradle 8.x or earlier - use 20.6.2
    LINT_PLUGIN_VERSION="20.6.2"
    if [ "$GRADLE_MAJOR" -ge 8 ] 2>/dev/null; then
        echo -e "${GREEN}Using plugin version ${LINT_PLUGIN_VERSION} (for Gradle 8.x)${NC}"
    else
        echo -e "${YELLOW}Gradle version ${GRADLE_VERSION:-unknown} detected, using ${LINT_PLUGIN_VERSION}${NC}"
        echo -e "${YELLOW}Note: Minimum supported Gradle version is 8.x${NC}"
    fi
fi

echo ""

# Check if plugin is already configured
check_existing_config() {
    if grep -q "gradleLintPluginVersion" "$ROOT_DIR/gradle.properties" 2>/dev/null; then
        local existing_version=$(grep "gradleLintPluginVersion=" "$ROOT_DIR/gradle.properties" | cut -d= -f2)
        echo -e "${YELLOW}Plugin already configured with version ${existing_version}${NC}"
        read -p "Update to version ${LINT_PLUGIN_VERSION}? (y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${CYAN}Keeping existing configuration${NC}"
            return 1
        fi
    fi
    return 0
}

# Step 1: Add version to gradle.properties
setup_gradle_properties() {
    echo -e "${BLUE}Step 1: Configuring gradle.properties${NC}"
    
    local props_file="$ROOT_DIR/gradle.properties"
    
    # Create if doesn't exist
    if [ ! -f "$props_file" ]; then
        touch "$props_file"
    fi
    
    # Add or update plugin version
    if grep -q "gradleLintPluginVersion=" "$props_file"; then
        # Update existing
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s/gradleLintPluginVersion=.*/gradleLintPluginVersion=${LINT_PLUGIN_VERSION}/" "$props_file"
        else
            sed -i "s/gradleLintPluginVersion=.*/gradleLintPluginVersion=${LINT_PLUGIN_VERSION}/" "$props_file"
        fi
        echo -e "  ${GREEN}✓${NC} Updated gradleLintPluginVersion to ${LINT_PLUGIN_VERSION}"
    else
        # Add new
        echo "" >> "$props_file"
        echo "# Gradle Lint Plugin - https://github.com/nebula-plugins/gradle-lint-plugin" >> "$props_file"
        echo "gradleLintPluginVersion=${LINT_PLUGIN_VERSION}" >> "$props_file"
        echo -e "  ${GREEN}✓${NC} Added gradleLintPluginVersion=${LINT_PLUGIN_VERSION}"
    fi
    
    # Ensure adequate JVM memory for lint analysis
    if ! grep -q "org.gradle.jvmargs" "$props_file"; then
        echo "" >> "$props_file"
        echo "# JVM memory for Gradle (required for lint plugin)" >> "$props_file"
        echo "org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError" >> "$props_file"
        echo -e "  ${GREEN}✓${NC} Added JVM memory settings"
    else
        echo -e "  ${CYAN}ℹ${NC} JVM memory settings already configured"
    fi
}

# Step 2: Add plugin to settings.gradle
setup_settings_gradle() {
    echo -e "${BLUE}Step 2: Configuring settings.gradle${NC}"
    
    local settings_file="$ROOT_DIR/settings.gradle"
    
    if [ ! -f "$settings_file" ]; then
        echo -e "  ${YELLOW}⚠${NC} settings.gradle not found, checking for Kotlin DSL..."
        settings_file="$ROOT_DIR/settings.gradle.kts"
        if [ ! -f "$settings_file" ]; then
            echo -e "  ${RED}✗${NC} No settings file found"
            return 1
        fi
        echo -e "  ${YELLOW}⚠${NC} Kotlin DSL detected - manual configuration required"
        echo ""
        echo "Add to settings.gradle.kts pluginManagement block:"
        echo ""
        echo '  plugins {'
        echo '      id("nebula.lint") version(extra["gradleLintPluginVersion"] as String)'
        echo '  }'
        echo ""
        return 0
    fi
    
    # Check if already configured
    if grep -q "nebula.lint" "$settings_file"; then
        echo -e "  ${CYAN}ℹ${NC} Plugin already registered in settings.gradle"
        return 0
    fi
    
    # Check for pluginManagement block
    if grep -q "pluginManagement" "$settings_file"; then
        # Add to existing pluginManagement > plugins block
        if grep -q "plugins {" "$settings_file"; then
            # Insert after the plugins { line inside pluginManagement
            if [[ "$OSTYPE" == "darwin"* ]]; then
                sed -i '' "/pluginManagement/,/^}$/ {
                    /plugins {/a\\
        id 'nebula.lint' version gradleLintPluginVersion
                }" "$settings_file"
            else
                sed -i "/pluginManagement/,/^}$/ {
                    /plugins {/a\\        id 'nebula.lint' version gradleLintPluginVersion
                }" "$settings_file"
            fi
            echo -e "  ${GREEN}✓${NC} Added plugin to existing pluginManagement block"
        else
            echo -e "  ${YELLOW}⚠${NC} pluginManagement exists but no plugins block found"
            echo "  Add manually: id 'nebula.lint' version gradleLintPluginVersion"
        fi
    else
        # Create new pluginManagement block at the beginning
        local temp_file=$(mktemp)
        cat > "$temp_file" << 'EOF'
pluginManagement {
    plugins {
        id 'nebula.lint' version gradleLintPluginVersion
    }
}

EOF
        cat "$settings_file" >> "$temp_file"
        mv "$temp_file" "$settings_file"
        echo -e "  ${GREEN}✓${NC} Added pluginManagement block with plugin"
    fi
}

# Step 3: Configure build.gradle with lint rules
setup_build_gradle() {
    echo -e "${BLUE}Step 3: Configuring build.gradle${NC}"
    
    local build_file="$ROOT_DIR/build.gradle"
    
    if [ ! -f "$build_file" ]; then
        echo -e "  ${YELLOW}⚠${NC} build.gradle not found, checking for Kotlin DSL..."
        build_file="$ROOT_DIR/build.gradle.kts"
        if [ ! -f "$build_file" ]; then
            echo -e "  ${RED}✗${NC} No build file found"
            return 1
        fi
        echo -e "  ${YELLOW}⚠${NC} Kotlin DSL detected - manual configuration required"
        return 0
    fi
    
    # Check if already configured
    if grep -q "gradleLint {" "$build_file"; then
        echo -e "  ${CYAN}ℹ${NC} gradleLint block already exists"
        return 0
    fi
    
    # Add plugin application and configuration
    cat >> "$build_file" << 'EOF'

// =============================================================================
// Gradle Lint Plugin Configuration
// https://github.com/nebula-plugins/gradle-lint-plugin
// =============================================================================
plugins {
    id 'nebula.lint'
}

gradleLint {
    // Dependency hygiene rules
    rules = [
        'unused-dependency',      // Find unused declared dependencies
        'undeclared-dependency',  // Find used but undeclared dependencies
    ]
    
    // Output format: text, html, or xml
    reportFormat = 'text'
    
    // Don't fail build on violations (warnings only)
    criticalRules = []
    
    // Don't run lint automatically on every build
    // Run manually with: ./gradlew lintGradle
    alwaysRun = false
}

// Apply lint plugin to all subprojects (root already has it via plugins block)
subprojects {
    apply plugin: 'nebula.lint'
}
EOF
    
    echo -e "  ${GREEN}✓${NC} Added gradleLint configuration"
}

# Main
echo -e "${CYAN}Checking existing configuration...${NC}"
if ! check_existing_config; then
    echo ""
    echo "======================================================================="
    echo "  Setup cancelled - existing configuration preserved"
    echo "======================================================================="
    exit 0
fi

echo ""
setup_gradle_properties
echo ""
setup_settings_gradle
echo ""
setup_build_gradle
echo ""

echo "======================================================================="
echo -e "${GREEN}  SETUP COMPLETE${NC}"
echo "======================================================================="
echo ""
echo "Available commands:"
echo ""
echo -e "  ${CYAN}./gradlew lintGradle${NC}"
echo "    Run lint analysis and show violations"
echo ""
echo -e "  ${CYAN}./gradlew generateGradleLintReport${NC}"
echo "    Generate detailed report in build/reports/gradleLint/"
echo ""
echo -e "  ${CYAN}./gradlew fixGradleLint${NC}"
echo "    Attempt to auto-fix violations (review changes carefully!)"
echo ""
echo -e "${YELLOW}WARNING:${NC} fixGradleLint may incorrectly remove Lombok and other"
echo "annotation processor dependencies. Always review git diff before committing."
echo ""
echo "Report location: build/reports/gradleLint/<project-name>.txt"
echo ""

# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → java/scripts/gradle-lint-setup.sh
# To modify, edit the source file and run the distribution workflow

