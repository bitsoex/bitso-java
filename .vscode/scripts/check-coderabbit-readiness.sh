#!/bin/bash
# Check CodeRabbit readiness for AI integration
# Verifies CLI installation, authentication, and configuration

set -e

echo "🔍 Checking CodeRabbit Configuration Readiness..."
echo ""

# Check CodeRabbit CLI installation
echo "1️⃣  CodeRabbit CLI Installation"
if ! command -v coderabbit &> /dev/null; then
    echo "   ❌ CodeRabbit CLI not found"
    echo ""
    echo "   Install CodeRabbit CLI globally:"
    echo "   curl -fsSL https://cli.coderabbit.ai/install.sh | sh"
    echo ""
    echo "   Then restart your shell:"
    echo "   source ~/.zshrc"
    exit 1
fi

CODERABBIT_VERSION=$(coderabbit --version 2>/dev/null || echo "unknown")
echo "   ✓ CodeRabbit CLI installed"
echo "   Version: $CODERABBIT_VERSION"

# Check authentication
echo ""
echo "2️⃣  Authentication Status"
if coderabbit auth status &> /dev/null; then
    AUTH_STATUS=$(coderabbit auth status 2>&1)
    if echo "$AUTH_STATUS" | grep -qi "authenticated\|logged in"; then
        echo "   ✓ CodeRabbit authentication verified"
        echo "   Status: $(echo "$AUTH_STATUS" | head -1)"
    else
        echo "   ⚠️  CodeRabbit may need authentication"
        echo "   Run: coderabbit auth login"
        echo "   Then restart your shell and try again"
        exit 1
    fi
else
    echo "   ❌ CodeRabbit authentication not configured"
    echo ""
    echo "   Authenticate with GitHub:"
    echo "   coderabbit auth login"
    echo ""
    echo "   This will:"
    echo "   1. Open a browser window"
    echo "   2. Ask you to log in with your GitHub account"
    echo "   3. Generate an auth token"
    echo "   4. Paste the token back into your terminal"
    exit 1
fi

# Check git configuration
echo ""
echo "3️⃣  Git Configuration"
if ! command -v git &> /dev/null; then
    echo "   ❌ Git not found - required for CodeRabbit"
    exit 1
fi
echo "   ✓ Git is available"

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "   ⚠️  Not currently in a git repository"
    echo "   CodeRabbit works best within a git repository"
else
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
    echo "   ✓ Git repository detected"
    echo "   Current branch: $CURRENT_BRANCH"
fi

# Check Node.js (optional but recommended)
echo ""
echo "4️⃣  Optional Dependencies"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo "   ✓ Node.js installed: $NODE_VERSION"
fi

if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm --version)
    echo "   ✓ npm installed: $NPM_VERSION"
fi

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    echo "   ✓ Java available: $JAVA_VERSION"
fi

if command -v gradle &> /dev/null; then
    GRADLE_VERSION=$(gradle --version 2>&1 | head -1)
    echo "   ✓ Gradle available: $GRADLE_VERSION"
fi

# Final verification
echo ""
echo "5️⃣  Verification Test"
echo "   Testing CodeRabbit CLI access..."
if coderabbit --help > /dev/null 2>&1; then
    echo "   ✓ CodeRabbit CLI is functional"
else
    echo "   ❌ CodeRabbit CLI not responding"
    echo "   Try: coderabbit --help"
    exit 1
fi

echo ""
echo "✅ CodeRabbit is ready for AI integration!"
echo ""
echo "Next steps:"
echo "1. Navigate to your project repository"
echo "2. Create or switch to a feature branch"
echo "3. Use /fix-coderabbit-issues command in your IDE"
echo ""
echo "Quick test:"
echo "   cd /path/to/your/project"
echo "   coderabbit --prompt-only --type uncommitted"
echo ""
