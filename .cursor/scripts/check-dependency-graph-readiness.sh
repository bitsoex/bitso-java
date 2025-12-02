#!/bin/bash
# Check readiness for GitHub Dependency Graph Gradle Plugin
# This script validates the environment for local vulnerability analysis

set -e

echo "🔍 Checking Dependency Graph Plugin Readiness..."
echo ""

ERRORS=0

# Check 1: Gradle wrapper exists
echo "1. Checking Gradle wrapper..."
if [ -f "./gradlew" ]; then
    GRADLE_VERSION=$(./gradlew --version 2>/dev/null | grep "Gradle" | head -1 || echo "unknown")
    echo "   ✅ Gradle wrapper found: $GRADLE_VERSION"
else
    echo "   ❌ Gradle wrapper not found (./gradlew)"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: Version catalog exists
echo ""
echo "2. Checking version catalog..."
if [ -f "./gradle/libs.versions.toml" ]; then
    echo "   ✅ Version catalog found: gradle/libs.versions.toml"
else
    echo "   ⚠️  Version catalog not found (gradle/libs.versions.toml)"
    echo "      This is optional but recommended for version management"
fi

# Check 3: Dependency graph init script
echo ""
echo "3. Checking dependency graph init script..."
if [ -f "./gradle/dependency-graph-init.gradle" ]; then
    echo "   ✅ Init script found: gradle/dependency-graph-init.gradle"
else
    echo "   ❌ Init script not found: gradle/dependency-graph-init.gradle"
    echo ""
    echo "   Create it with this content:"
    echo ""
    echo '   initscript {'
    echo '       repositories {'
    echo '           gradlePluginPortal()'
    echo '       }'
    echo '       dependencies {'
    echo '           classpath "org.gradle:github-dependency-graph-gradle-plugin:1.4.0"'
    echo '       }'
    echo '   }'
    echo ''
    echo '   apply plugin: org.gradle.dependencygraph.simple.SimpleDependencyGraphPlugin'
    echo ""
    ERRORS=$((ERRORS + 1))
fi

# Check 4: GitHub CLI
echo ""
echo "4. Checking GitHub CLI..."
if command -v gh &> /dev/null; then
    GH_VERSION=$(gh --version | head -1)
    echo "   ✅ GitHub CLI found: $GH_VERSION"
    
    # Check auth status
    if gh auth status &> /dev/null; then
        echo "   ✅ GitHub CLI authenticated"
    else
        echo "   ❌ GitHub CLI not authenticated"
        echo "      Run: gh auth login"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo "   ❌ GitHub CLI not found"
    echo "      Install: brew install gh"
    ERRORS=$((ERRORS + 1))
fi

# Check 5: jq for JSON parsing
echo ""
echo "5. Checking jq..."
if command -v jq &> /dev/null; then
    JQ_VERSION=$(jq --version)
    echo "   ✅ jq found: $JQ_VERSION"
else
    echo "   ⚠️  jq not found (optional but recommended)"
    echo "      Install: brew install jq"
fi

# Check 6: Network access to Gradle Plugin Portal
echo ""
echo "6. Checking network access..."
if curl -s --head https://plugins.gradle.org | head -1 | grep -q "200\|301\|302"; then
    echo "   ✅ Gradle Plugin Portal accessible"
else
    echo "   ⚠️  Cannot reach Gradle Plugin Portal"
    echo "      Check network/proxy settings"
fi

# Summary
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $ERRORS -eq 0 ]; then
    echo "✅ All checks passed! Ready to use dependency graph plugin."
    echo ""
    echo "Run dependency analysis with:"
    echo ""
    echo "  ./gradlew -I gradle/dependency-graph-init.gradle \\"
    echo "      --dependency-verification=off \\"
    echo "      --no-configuration-cache \\"
    echo "      --no-configure-on-demand \\"
    echo "      :ForceDependencyResolutionPlugin_resolveAllDependencies"
    echo ""
    echo "Reports will be in: build/reports/dependency-graph-snapshots/"
else
    echo "❌ $ERRORS check(s) failed. Please fix the issues above."
    exit 1
fi

