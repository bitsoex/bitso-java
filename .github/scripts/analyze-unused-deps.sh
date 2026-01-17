#!/bin/bash
#
# Gradle Unused Dependencies Analyzer
#
# This script analyzes unused dependencies in a Gradle project.
# It can be run standalone without modifying build.gradle files.
#
# Usage:
#   ./analyze-unused-deps.sh [project-dir] [options]
#
# Options:
#   --toml-only     Only analyze libs.versions.toml
#   --versions-only Only analyze versions.gradle  
#   --all           Analyze both (default)
#
# Examples:
#   ./analyze-unused-deps.sh                          # Analyze current project
#   ./analyze-unused-deps.sh /path/to/project         # Analyze specific project
#   ./analyze-unused-deps.sh --toml-only              # Only analyze version catalog
#   ./analyze-unused-deps.sh /path/to/project --all   # Analyze specific project with all checks
#

# Don't use set -e because grep returns non-zero when no matches found
# set -e

# Determine the root directory
if [ -n "$1" ] && [ -d "$1" ]; then
    ROOT_DIR="$1"
    shift
else
    # Try to find the root by looking for settings.gradle or settings.gradle.kts (Kotlin DSL)
    ROOT_DIR=$(pwd)
    while [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ] && [ "$ROOT_DIR" != "/" ]; do
        ROOT_DIR=$(dirname "$ROOT_DIR")
    done

    if [ ! -f "$ROOT_DIR/settings.gradle" ] && [ ! -f "$ROOT_DIR/settings.gradle.kts" ]; then
        echo "Error: Could not find Gradle project root (settings.gradle or settings.gradle.kts)"
        echo "Usage: $0 [project-root-dir] [--toml-only|--versions-only|--all]"
        exit 1
    fi
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Common false positives - libraries often used indirectly
# These are flagged as "Needs Review" instead of "Likely Unused"
KNOWN_INDIRECT_LIBS=(
    # Annotation processors
    "lombok"               # Used via annotation processor
    "mapstruct"            # Used via annotation processor
    
    # Logging (transitive)
    "slf4j-api"            # Transitive logging dependency
    "slf4j-simple"         # Test logging
    "logback"              # Logging implementation
    
    # Testing frameworks
    "junit-bom"            # Used as platform/BOM
    "junit-jupiter"        # Used via test framework
    "junit-vintage"        # JUnit 4 compatibility
    "spock"                # Spock test framework
    "groovy"               # Required by Spock
    "groovy-json"          # Used by Spock tests
    "mockito"              # Mocking framework
    "assertj"              # Assertion library
    
    # Spring (managed by BOM)
    "spring-core"          # Managed by Spring Boot BOM
    "spring-beans"         # Managed by Spring Boot BOM
    "spring-context"       # Managed by Spring Boot BOM
    "spring-expression"    # Managed by Spring Boot BOM
    "spring-web"           # Managed by Spring Boot BOM
    "spring-boot-starter"  # Often pulled transitively
    
    # gRPC (managed by BOM or starter)
    "protobuf-java"        # Often managed by gRPC BOM
    "grpc-stub"            # Managed by gRPC starter
    "grpc-netty"           # Managed by gRPC starter
    "grpc-protobuf"        # Managed by gRPC starter
    "grpc-api"             # Managed by gRPC starter
    
    # Database
    "jooq"                 # Often generated or managed
    "hikari"               # Connection pool, managed
    "postgresql"           # JDBC driver
    "flyway"               # Database migrations
    
    # Common utilities
    "guava"                # Google utilities
    "commons-lang"         # Apache commons
    "jackson"              # JSON processing
    "validation"           # Bean validation
    "hibernate-validator"  # Validation impl
    
    # gRPC Spring starters
    "spring-grpc"          # gRPC Spring integration
    "grpc-spring"          # gRPC Spring integration
    
    # Kotlin
    "kotlin-stdlib"        # Kotlin runtime
    "kotlin-reflect"       # Kotlin reflection
    
    # Protobuf
    "protobuf"             # Protocol buffers
    
    # Netty (transitive)
    "netty"                # Network framework
)

echo ""
echo "======================================================================="
echo "  GRADLE UNUSED DEPENDENCIES ANALYZER"
echo "  Project: $ROOT_DIR"
echo "======================================================================="
echo ""

# Collect all build.gradle and build.gradle.kts content
collect_build_content() {
    find "$ROOT_DIR" \( -name "build.gradle" -o -name "build.gradle.kts" \) \
        -not -path "*/.gradle/*" \
        -not -path "*/build/*" \
        -exec cat {} \; 2>/dev/null
}

# Also collect gradle/*.gradle files (but not versions.gradle itself)
collect_gradle_scripts() {
    if [ -d "$ROOT_DIR/gradle" ]; then
        find "$ROOT_DIR/gradle" -name "*.gradle" \
            -not -name "versions.gradle" \
            -exec cat {} \; 2>/dev/null || true
    fi
}

# Collect settings.gradle content (may have plugin dependencies)
collect_settings_content() {
    if [ -f "$ROOT_DIR/settings.gradle" ]; then
        cat "$ROOT_DIR/settings.gradle" 2>/dev/null || true
    fi
    if [ -f "$ROOT_DIR/settings.gradle.kts" ]; then
        cat "$ROOT_DIR/settings.gradle.kts" 2>/dev/null || true
    fi
}

ALL_BUILD_CONTENT=$(collect_build_content)
ALL_GRADLE_SCRIPTS=$(collect_gradle_scripts)
SETTINGS_CONTENT=$(collect_settings_content)
COMBINED_CONTENT="$ALL_BUILD_CONTENT
$ALL_GRADLE_SCRIPTS
$SETTINGS_CONTENT"

# Check if a library is in the known indirect list
is_known_indirect() {
    local lib_key="$1"
    for known in "${KNOWN_INDIRECT_LIBS[@]}"; do
        if [[ "$lib_key" == *"$known"* ]]; then
            return 0
        fi
    done
    return 1
}

# =============================================================================
# Analyze libs.versions.toml
# =============================================================================
analyze_toml() {
    local TOML_FILE="$ROOT_DIR/gradle/libs.versions.toml"

    if [ ! -f "$TOML_FILE" ]; then
        echo -e "${YELLOW}⚠  libs.versions.toml not found at $TOML_FILE${NC}"
        return
    fi

    echo -e "${BLUE}--- Analyzing libs.versions.toml ---${NC}"
    echo ""

    # Read the TOML file and include in combined content for version.ref checks
    TOML_CONTENT=$(cat "$TOML_FILE")
    FULL_CONTENT="$COMBINED_CONTENT
$TOML_CONTENT"

    # Extract libraries used in bundles (these count as "used")
    local BUNDLE_LIBS=""
    local IN_BUNDLES=false
    while IFS= read -r line; do
        if [[ "$line" =~ ^\[bundles\] ]]; then
            IN_BUNDLES=true
            continue
        elif [[ "$line" =~ ^\[ ]]; then
            IN_BUNDLES=false
            continue
        fi
        if $IN_BUNDLES && [[ "$line" =~ = ]]; then
            # Extract library references from bundle definition
            # Bundle format: name = ["lib1", "lib2", ...]
            BUNDLE_LIBS="$BUNDLE_LIBS $(echo "$line" | grep -oE '"[a-zA-Z0-9_-]+"' | tr -d '"')"
        fi
    done <"$TOML_FILE"

    # Extract library names from [libraries] section
    local IN_LIBRARIES=false
    local UNUSED_LIBS=()
    local MAYBE_UNUSED_LIBS=()
    local USED_LIBS=()

    while IFS= read -r line; do
        # Check section headers
        if [[ "$line" =~ ^\[libraries\] ]]; then
            IN_LIBRARIES=true
            continue
        elif [[ "$line" =~ ^\[ ]]; then
            IN_LIBRARIES=false
            continue
        fi

        # Skip comments and empty lines
        if [[ "$line" =~ ^#.* ]] || [[ -z "${line// }" ]]; then
            continue
        fi

        # Extract library key from lines like: key = { module = "..." }
        if $IN_LIBRARIES && [[ "$line" =~ ^([a-zA-Z0-9_-]+)[[:space:]]*= ]]; then
            local lib_key="${BASH_REMATCH[1]}"
            # Convert key to accessor format (replace - with .)
            local accessor="${lib_key//-/.}"

            # Check if this library is used (direct reference or in a bundle)
            if echo "$FULL_CONTENT" | grep -q "libs\.${accessor}" ||
                echo "$FULL_CONTENT" | grep -q "libs\.${lib_key}" ||
                echo "$BUNDLE_LIBS" | grep -qw "$lib_key"; then
                USED_LIBS+=("$lib_key")
            else
                # Check if it's a known indirect dependency
                if is_known_indirect "$lib_key"; then
                    MAYBE_UNUSED_LIBS+=("$lib_key")
                else
                    UNUSED_LIBS+=("$lib_key")
                fi
            fi
        fi
    done <"$TOML_FILE"

    # Report results
    if [ ${#UNUSED_LIBS[@]} -eq 0 ] && [ ${#MAYBE_UNUSED_LIBS[@]} -eq 0 ]; then
        echo -e "${GREEN}  ✅ All libraries are referenced${NC}"
    else
        if [ ${#UNUSED_LIBS[@]} -gt 0 ]; then
            echo -e "${RED}  Potentially Unused Libraries:${NC}"
            for lib in "${UNUSED_LIBS[@]}"; do
                local accessor="${lib//-/.}"
                echo -e "    ${RED}❌ ${lib}${NC} (libs.${accessor})"
            done
            echo ""
            echo -e "  Found ${#UNUSED_LIBS[@]} potentially unused libraries"
            echo ""
        fi

        if [ ${#MAYBE_UNUSED_LIBS[@]} -gt 0 ]; then
            echo -e "${YELLOW}  Possibly Indirect Dependencies (verify before removing):${NC}"
            for lib in "${MAYBE_UNUSED_LIBS[@]}"; do
                local accessor="${lib//-/.}"
                echo -e "    ${YELLOW}⚠ ${lib}${NC} (may be used via annotation processor, BOM, or test framework)"
            done
            echo ""
        fi
    fi

    echo -e "${GREEN}  Used Libraries: ${#USED_LIBS[@]}${NC}"
    echo -e "${RED}  Likely Unused: ${#UNUSED_LIBS[@]}${NC}"
    echo -e "${YELLOW}  Needs Review: ${#MAYBE_UNUSED_LIBS[@]}${NC}"
    echo ""

    # Also check for unused versions
    echo -e "${BLUE}--- Checking Unused Versions ---${NC}"
    echo ""

    local IN_VERSIONS=false
    local UNUSED_VERSIONS=()
    local USED_VERSIONS=()

    while IFS= read -r line; do
        if [[ "$line" =~ ^\[versions\] ]]; then
            IN_VERSIONS=true
            continue
        elif [[ "$line" =~ ^\[ ]]; then
            IN_VERSIONS=false
            continue
        fi

        if [[ "$line" =~ ^#.* ]] || [[ -z "${line// }" ]]; then
            continue
        fi

        if $IN_VERSIONS && [[ "$line" =~ ^([a-zA-Z0-9_-]+)[[:space:]]*= ]]; then
            local ver_key="${BASH_REMATCH[1]}"
            local ver_accessor="${ver_key//-/.}"

            # Check if version is referenced
            if echo "$FULL_CONTENT" | grep -qE "version\.ref[[:space:]]*=[[:space:]]*['\"]${ver_key}['\"]" ||
                echo "$FULL_CONTENT" | grep -q "libs\.versions\.${ver_accessor}" ||
                echo "$FULL_CONTENT" | grep -q "libs\.versions\.${ver_key}" ||
                echo "$FULL_CONTENT" | grep -q "\.ref[[:space:]]*=[[:space:]]*['\"]${ver_key}['\"]"; then
                USED_VERSIONS+=("$ver_key")
            else
                UNUSED_VERSIONS+=("$ver_key")
            fi
        fi
    done <"$TOML_FILE"

    if [ ${#UNUSED_VERSIONS[@]} -eq 0 ]; then
        echo -e "${GREEN}  ✅ All versions are referenced${NC}"
    else
        echo -e "${RED}  Potentially Unused Versions:${NC}"
        for ver in "${UNUSED_VERSIONS[@]}"; do
            echo -e "    ${RED}❌ ${ver}${NC}"
        done
        echo ""
        echo -e "  Found ${#UNUSED_VERSIONS[@]} potentially unused versions"
    fi

    echo ""
    echo -e "${GREEN}  Used Versions: ${#USED_VERSIONS[@]}${NC}"
    echo -e "${RED}  Potentially Unused: ${#UNUSED_VERSIONS[@]}${NC}"
    echo ""
}

# =============================================================================
# Analyze versions.gradle
# =============================================================================
analyze_versions_gradle() {
    local VERSIONS_FILE="$ROOT_DIR/versions.gradle"

    if [ ! -f "$VERSIONS_FILE" ]; then
        echo -e "${YELLOW}⚠  versions.gradle not found${NC}"
        echo -e "${YELLOW}   This is expected for modern projects using libs.versions.toml${NC}"
        return
    fi

    echo -e "${BLUE}--- Analyzing versions.gradle ---${NC}"
    echo ""

    # Find all versions.* patterns used in build files (excluding versions.gradle itself)
    # Patterns can be:
    #   - versions.XXX (simple dot notation)
    #   - ${versions.XXX} (interpolated)
    #   - versions["key"] or versions['key'] (bracket notation)
    #   - versions."quoted-key" (quoted key with dots)
    #   - versions.bitso["key"] (nested with brackets)
    local USED_PATTERNS_DOT
    USED_PATTERNS_DOT=$(echo "$COMBINED_CONTENT" | \
        grep -oE 'versions\.[a-zA-Z0-9_.-]+' 2>/dev/null | \
        sed 's/\.$//' | \
        sort -u | \
        grep -v "^versions\.$" | \
        grep -v "^$" || true)

    # Also capture bracket/quoted notation: versions["key"] or versions.'key'
    local USED_PATTERNS_BRACKET
    USED_PATTERNS_BRACKET=$(echo "$COMBINED_CONTENT" | \
        grep -oE "versions\[?['\"][a-zA-Z0-9_.-]+['\"]" 2>/dev/null | \
        sed "s/versions[\['\"]*/versions./" | \
        sed "s/['\"]$//" | \
        sort -u || true)

    # Also capture quoted key notation: versions."quoted-key"
    local USED_PATTERNS_QUOTED
    USED_PATTERNS_QUOTED=$(echo "$COMBINED_CONTENT" | \
        grep -oE 'versions\."[a-zA-Z0-9_.-]+"' 2>/dev/null | \
        sed 's/versions\."/versions./' | \
        sed 's/"$//' | \
        sort -u || true)

    # Combine all patterns
    local USED_PATTERNS
    USED_PATTERNS=$(echo -e "${USED_PATTERNS_DOT}\n${USED_PATTERNS_BRACKET}\n${USED_PATTERNS_QUOTED}" | \
        sort -u | \
        grep -v "^$" || true)

    local PATTERN_COUNT
    PATTERN_COUNT=$(echo "$USED_PATTERNS" | grep -c . 2>/dev/null || echo "0")
    PATTERN_COUNT=$(echo "$PATTERN_COUNT" | tr -d '[:space:]')

    if [ "$PATTERN_COUNT" -gt 0 ]; then
        echo -e "  ${CYAN}Used version patterns (${PATTERN_COUNT}):${NC}"
        echo "$USED_PATTERNS" | head -30 | while read -r pattern; do
            if [ -n "$pattern" ]; then
                echo -e "    ${GREEN}✓${NC} $pattern"
            fi
        done

        if [ "$PATTERN_COUNT" -gt 30 ]; then
            echo -e "    ${CYAN}... and $((PATTERN_COUNT - 30)) more${NC}"
        fi
    else
        echo -e "  ${YELLOW}No version patterns found in build files${NC}"
    fi

    echo ""

    # Extract top-level keys from versions.gradle
    echo -e "  ${CYAN}Analyzing top-level keys in versions.gradle...${NC}"
    local VERSIONS_CONTENT
    VERSIONS_CONTENT=$(cat "$VERSIONS_FILE")

    # Find top-level keys (those at the first level of ext.versions = [...])
    # Support any amount of leading whitespace (spaces or tabs)
    local TOP_LEVEL_KEYS
    TOP_LEVEL_KEYS=$(echo "$VERSIONS_CONTENT" | grep -E "^[[:space:]]+['\"]?[a-zA-Z0-9_-]+['\"]?[[:space:]]*:" | \
        grep -v "^[[:space:]]\{12,\}" | \
        sed 's/^[[:space:]]*//' | \
        sed 's/[[:space:]]*:.*//' | \
        sed "s/['\"]//g" | \
        sort -u)

    local UNUSED_TOP_LEVEL=()
    local USED_TOP_LEVEL=()

    while IFS= read -r key; do
        if [ -z "$key" ]; then continue; fi

        # Check if this key is used in any version pattern (exact match with word boundaries)
        if echo "$USED_PATTERNS" | grep -qE "(^|[^a-zA-Z0-9_-])versions\.${key}([^a-zA-Z0-9_-]|$)"; then
            USED_TOP_LEVEL+=("$key")
        else
            UNUSED_TOP_LEVEL+=("$key")
        fi
    done <<< "$TOP_LEVEL_KEYS"

    echo ""
    if [ ${#UNUSED_TOP_LEVEL[@]} -gt 0 ]; then
        echo -e "  ${RED}Potentially Unused Top-Level Keys:${NC}"
        local count=0
        for key in "${UNUSED_TOP_LEVEL[@]}"; do
            if [ $count -lt 30 ]; then
                echo -e "    ${RED}❌ ${key}${NC}"
            fi
            ((count++))
        done
        if [ $count -gt 30 ]; then
            echo -e "    ${CYAN}... and $((count - 30)) more${NC}"
        fi
        echo ""
        echo -e "  Found ${#UNUSED_TOP_LEVEL[@]} potentially unused top-level keys"
    else
        echo -e "  ${GREEN}✅ All top-level keys appear to be used${NC}"
    fi

    echo ""
    echo -e "${GREEN}  Used Top-Level Keys: ${#USED_TOP_LEVEL[@]}${NC}"
    echo -e "${RED}  Potentially Unused: ${#UNUSED_TOP_LEVEL[@]}${NC}"
    echo ""

    echo -e "${YELLOW}  Note: versions.gradle has nested structures that require manual review.${NC}"
    echo -e "${YELLOW}  For comprehensive analysis, use the Gradle task in unused-dependencies.gradle${NC}"
    echo ""
}

# =============================================================================
# Main
# =============================================================================
MODE="${1:---all}"

case "$MODE" in
--toml-only)
    analyze_toml
    ;;
--versions-only)
    analyze_versions_gradle
    ;;
--all)
    analyze_toml
    echo ""
    analyze_versions_gradle
    ;;
*)
    if [[ "$MODE" == --* ]]; then
        echo "Error: Unknown option '$MODE'"
        echo "Usage: $0 [project-root-dir] [--toml-only|--versions-only|--all]"
        exit 1
    fi
    # Default to --all if no option specified
    analyze_toml
    echo ""
    analyze_versions_gradle
    ;;
esac

echo "======================================================================="
echo ""
echo -e "${CYAN}Next steps:${NC}"
echo "  1. Review each potentially unused entry before removing"
echo "  2. Search project: grep -r 'entry-name' --include='*.gradle' ."
echo "  3. Check for indirect usage (annotation processors, BOMs, test frameworks)"
echo "  4. Validate build: ./gradlew clean build -x test"
echo "  5. Validate tests: ./gradlew test -x codeCoverageReport"
echo ""
echo -e "${YELLOW}Common false positives:${NC}"
echo "  - lombok: Used via annotation processor, not direct reference"
echo "  - slf4j-*: Often transitive or used by logging framework"
echo "  - junit-bom: Used as platform(), not direct dependency"
echo "  - spring-*: Often managed by Spring Boot BOM"
echo "  - groovy-*: Used by Spock test framework"
echo ""
# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → java/scripts/analyze-unused-deps.sh
# To modify, edit the source file and run the distribution workflow

