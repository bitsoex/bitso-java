#!/bin/bash
# Check SonarQube readiness for MCP integration
# Verifies environment variables, token type, and authentication

set -e

echo "🔍 Checking SonarQube Configuration Readiness..."
echo ""

# Check environment variables
echo "1️⃣  Environment Variables"
if [ -z "$SONARQUBE_TOKEN" ]; then
    echo "   ❌ SONARQUBE_TOKEN not set"
    exit 1
fi
echo "   ✓ SONARQUBE_TOKEN is set"

if [ -z "$SONARQUBE_URL" ]; then
    echo "   ❌ SONARQUBE_URL not set"
    exit 1
fi
echo "   ✓ SONARQUBE_URL is set to: $SONARQUBE_URL"

# Check authentication
echo ""
echo "2️⃣  Authentication"
if curl -s -u "$SONARQUBE_TOKEN:" "$SONARQUBE_URL/api/authentication/validate" | grep -q '"valid":true'; then
    echo "   ✓ Token authentication successful"
else
    echo "   ❌ Token authentication failed"
    echo "   Please verify:"
    echo "   - Token is a User Token (not Project Analysis or Global Analysis)"
    echo "   - Token is valid and not expired"
    echo "   - SONARQUBE_URL is correct"
    exit 1
fi

# Check token type
echo ""
echo "3️⃣  Token Type Verification"
TOKEN_INFO=$(curl -s -u "$SONARQUBE_TOKEN:" "$SONARQUBE_URL/api/user_tokens/list" | jq '.userTokens[] | select(.token == env.SONARQUBE_TOKEN)')
if echo "$TOKEN_INFO" | jq -e 'select(.type == "USER_TOKEN")' > /dev/null 2>&1; then
    echo "   ✓ User Token confirmed"
else
    echo "   ⚠️  Could not verify token type (may be OK if token is valid)"
fi

echo ""
echo "✅ SonarQube is ready for MCP integration!"
echo ""
echo "Next: Use /fix-sonarqube-issues command in your IDE to analyze issues"
