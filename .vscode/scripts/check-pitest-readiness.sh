#!/bin/bash
# Check readiness for PIT Mutation Testing
# This script validates the environment and project configuration for mutation testing
#
# Usage:
#   bash check-pitest-readiness.sh [project-directory]
#
# If no directory specified, uses current directory.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PROJECT_DIR="${1:-.}"

echo "========================================"
echo " PIT Mutation Testing Readiness Check"
echo "========================================"
echo ""
echo "Project directory: $PROJECT_DIR"
echo ""

cd "$PROJECT_DIR"

ERRORS=0
WARNINGS=0

# Check 1: Gradle wrapper exists
echo "1. Checking Gradle wrapper..."
if [ -f "./gradlew" ]; then
    GRADLE_VERSION=$(./gradlew --version 2>/dev/null | grep "^Gradle" | head -1 || echo "unknown")
    echo -e "   ${GREEN}✓${NC} Gradle wrapper found: $GRADLE_VERSION"
    
    # Check Gradle version is 8.x+
    VERSION_NUM=$(echo "$GRADLE_VERSION" | grep -oE '[0-9]+\.[0-9]+' | head -1)
    MAJOR_VERSION=$(echo "$VERSION_NUM" | cut -d. -f1)
    if [ "$MAJOR_VERSION" -ge 8 ] 2>/dev/null; then
        echo -e "   ${GREEN}✓${NC} Gradle version 8.x+ (compatible)"
    else
        echo -e "   ${YELLOW}⚠${NC} Gradle version < 8.x - consider upgrading"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "   ${RED}✗${NC} Gradle wrapper not found (./gradlew)"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: Java version
echo ""
echo "2. Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    echo -e "   ${GREEN}✓${NC} Java found: $JAVA_VERSION"
    
    # Check for Java 21
    if echo "$JAVA_VERSION" | grep -qE '(21|22|23)'; then
        echo -e "   ${GREEN}✓${NC} Java 21+ detected (recommended)"
    else
        echo -e "   ${YELLOW}⚠${NC} Java 21+ recommended for best compatibility"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "   ${RED}✗${NC} Java not found"
    ERRORS=$((ERRORS + 1))
fi

# Check 3: Tests exist
echo ""
echo "3. Checking for test sources..."
TEST_COUNT=$(find . -type f \( -name "*Test.java" -o -name "*Spec.groovy" -o -name "*Test.groovy" -o -name "*Spec.java" \) 2>/dev/null | wc -l | tr -d ' ')
if [ "$TEST_COUNT" -gt 0 ]; then
    echo -e "   ${GREEN}✓${NC} Found $TEST_COUNT test files"
    
    # Check for Spock tests
    SPOCK_COUNT=$(find . -type f -name "*Spec.groovy" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$SPOCK_COUNT" -gt 0 ]; then
        echo -e "   ${GREEN}✓${NC} Spock framework detected ($SPOCK_COUNT specs)"
    fi
    
    # Check for JUnit tests
    JUNIT_COUNT=$(find . -type f -name "*Test.java" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$JUNIT_COUNT" -gt 0 ]; then
        echo -e "   ${GREEN}✓${NC} JUnit tests detected ($JUNIT_COUNT tests)"
    fi
else
    echo -e "   ${RED}✗${NC} No test files found"
    ERRORS=$((ERRORS + 1))
fi

# Check 4: JaCoCo configuration (indicates test infrastructure exists)
echo ""
echo "4. Checking JaCoCo configuration..."
if [ -f "./gradle/jacoco.gradle" ]; then
    echo -e "   ${GREEN}✓${NC} JaCoCo configuration found: gradle/jacoco.gradle"
elif grep -rq "jacoco" build.gradle 2>/dev/null || grep -rq "jacoco" settings.gradle 2>/dev/null; then
    echo -e "   ${GREEN}✓${NC} JaCoCo plugin configured in build files"
else
    echo -e "   ${YELLOW}⚠${NC} JaCoCo not detected - recommended for coverage baseline"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 5: Version catalog
echo ""
echo "5. Checking version catalog..."
if [ -f "./gradle/libs.versions.toml" ]; then
    echo -e "   ${GREEN}✓${NC} Version catalog found: gradle/libs.versions.toml"
    
    # Check if pitest is already configured
    if grep -qi "pitest" gradle/libs.versions.toml 2>/dev/null; then
        echo -e "   ${GREEN}✓${NC} PIT already configured in version catalog"
    else
        echo -e "   ${YELLOW}ℹ${NC} Add PIT versions to catalog:"
        echo ""
        echo "      [versions]"
        echo "      pitest = \"1.22.0\""
        echo "      pitest-plugin = \"1.19.0-rc.2\""
        echo "      pitest-junit5 = \"1.2.3\""
        echo ""
    fi
else
    echo -e "   ${YELLOW}⚠${NC} Version catalog not found (optional)"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 6: Build compiles
echo ""
echo "6. Checking if project compiles..."
if ./gradlew classes testClasses --quiet 2>/dev/null; then
    echo -e "   ${GREEN}✓${NC} Project compiles successfully"
else
    echo -e "   ${RED}✗${NC} Project compilation failed"
    echo "      Run: ./gradlew classes testClasses"
    ERRORS=$((ERRORS + 1))
fi

# Check 7: Tests pass
echo ""
echo "7. Checking if tests pass..."
echo "   (Running tests with -x codeCoverageReport for speed...)"
if ./gradlew test -x codeCoverageReport --quiet 2>/dev/null; then
    echo -e "   ${GREEN}✓${NC} All tests pass"
else
    echo -e "   ${RED}✗${NC} Some tests are failing"
    echo "      Mutation testing requires all tests to pass first."
    echo "      Run: ./gradlew test -x codeCoverageReport"
    ERRORS=$((ERRORS + 1))
fi

# Check 8: Network access
echo ""
echo "8. Checking network access..."
if curl -s --head --max-time 5 https://repo1.maven.org 2>/dev/null | head -1 | grep -qE "200|301|302"; then
    echo -e "   ${GREEN}✓${NC} Maven Central accessible"
else
    echo -e "   ${YELLOW}⚠${NC} Cannot reach Maven Central"
    echo "      Check network/proxy settings"
    WARNINGS=$((WARNINGS + 1))
fi

if curl -s --head --max-time 5 https://plugins.gradle.org 2>/dev/null | head -1 | grep -qE "200|301|302"; then
    echo -e "   ${GREEN}✓${NC} Gradle Plugin Portal accessible"
else
    echo -e "   ${YELLOW}⚠${NC} Cannot reach Gradle Plugin Portal"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 9: Memory availability
echo ""
echo "9. Checking system resources..."
if command -v sysctl &> /dev/null; then
    # macOS
    TOTAL_MEM=$(sysctl -n hw.memsize 2>/dev/null | awk '{print int($1/1024/1024/1024)}')
    if [ "$TOTAL_MEM" -ge 8 ]; then
        echo -e "   ${GREEN}✓${NC} System memory: ${TOTAL_MEM}GB (sufficient)"
    else
        echo -e "   ${YELLOW}⚠${NC} System memory: ${TOTAL_MEM}GB (8GB+ recommended)"
        WARNINGS=$((WARNINGS + 1))
    fi
elif command -v free &> /dev/null; then
    # Linux
    TOTAL_MEM=$(free -g | awk '/^Mem:/{print $2}')
    if [ "$TOTAL_MEM" -ge 8 ]; then
        echo -e "   ${GREEN}✓${NC} System memory: ${TOTAL_MEM}GB (sufficient)"
    else
        echo -e "   ${YELLOW}⚠${NC} System memory: ${TOTAL_MEM}GB (8GB+ recommended)"
        WARNINGS=$((WARNINGS + 1))
    fi
fi

# Summary
echo ""
echo "========================================"
echo " Summary"
echo "========================================"
if [ $ERRORS -eq 0 ]; then
    if [ $WARNINGS -eq 0 ]; then
        echo -e "${GREEN}✓ All checks passed!${NC}"
    else
        echo -e "${GREEN}✓ Ready with ${WARNINGS} warning(s)${NC}"
    fi
    echo ""
    echo "Next steps:"
    echo ""
    echo "1. Copy pitest.gradle to your project:"
    echo "   cp /path/to/ai-code-instructions/java/templates/pitest.gradle gradle/"
    echo ""
    echo "2. Apply in build.gradle (see command documentation)"
    echo ""
    echo "3. Run mutation testing:"
    echo "   ./gradlew pitest -Ppitest.mutators=DEFAULTS     # QUICK"
    echo "   ./gradlew pitest -Ppitest.mutators=STRONGER     # STANDARD"
    echo "   ./gradlew pitest -Ppitest.mutators=ALL          # COMPREHENSIVE"
    echo ""
    echo "4. View report:"
    echo "   open build/reports/pitest/index.html"
    echo ""
else
    echo -e "${RED}✗ ${ERRORS} error(s) found${NC}"
    echo ""
    echo "Please fix the errors above before running mutation testing."
    exit 1
fi
# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → java/scripts/check-pitest-readiness.sh
# To modify, edit the source file and run the distribution workflow

