---
title: Copilot CLI Setup
description: Configure SonarQube MCP for GitHub Copilot CLI
---

# Copilot CLI Setup

Configure the SonarQube MCP server for the GitHub Copilot CLI tool.

## Contents

- [Overview](#overview)
- [Configuration Location](#configuration-location)
- [Setup Steps](#setup-steps)
- [Configuration Format](#configuration-format)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Related](#related)

---
## Overview

The GitHub Copilot CLI (`gh copilot`) supports MCP servers for enhanced code analysis. The SonarQube MCP server runs remotely at `https://sonarqube-mcp.bitso.io/mcp`.

**No tokens or environment variables required** - the remote server handles authentication.

## Configuration Location

- **File**: `~/.copilot/mcp-config.json`
- **Format**: HTTP/SSE server URL with tools array
- **Scope**: User-level (applies to all CLI invocations)

## Setup Steps

### Step 1: Check Existing Configuration

```bash
if [ -f ~/.copilot/mcp-config.json ]; then
  echo "✓ Copilot CLI config found"
  # Validate JSON
  if jq empty ~/.copilot/mcp-config.json 2>/dev/null; then
    echo "  ✓ Valid JSON"
  else
    echo "  ❌ Invalid JSON - fix or remove file"
    exit 1
  fi
else
  echo "→ Copilot CLI config not found (will create)"
fi
```

### Step 2: Backup (if exists)

```bash
if [ -f ~/.copilot/mcp-config.json ]; then
  cp ~/.copilot/mcp-config.json ~/.copilot/mcp-config.json.backup
  echo "✓ Backed up to mcp-config.json.backup"
fi
```

### Step 3: Create or Update Configuration

```bash
# Create directory if needed
mkdir -p ~/.copilot

# Check if file exists
if [ ! -f ~/.copilot/mcp-config.json ]; then
  # Create new config
  cat > ~/.copilot/mcp-config.json << 'EOF'
{
  "mcpServers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "tools": ["*"]
    }
  }
}
EOF
  echo "✓ Created new configuration"
else
  # Merge with existing config
  existing=$(cat ~/.copilot/mcp-config.json)
  echo "$existing" | jq '.mcpServers.sonarqube = {"type": "http", "url": "https://sonarqube-mcp.bitso.io/mcp", "tools": ["*"]}' \
    > ~/.copilot/mcp-config.json
  echo "✓ Merged SonarQube config (preserving other servers)"
fi
```

## Configuration Format

Copilot CLI uses HTTP/SSE configuration with `mcpServers` root key and a `tools` array:

```json
{
  "mcpServers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "tools": ["*"]
    }
  }
}
```

**Notes**:
- The `type` must be `"http"` for SSE servers
- `tools: ["*"]` enables all available tools
- You can restrict tools: `["get_issues", "get_rule"]`

## Verification

```bash
echo "=== Verifying Copilot CLI Configuration ==="

if [ -f ~/.copilot/mcp-config.json ]; then
  echo "✓ Config file exists"

  # Validate JSON
  if jq empty ~/.copilot/mcp-config.json 2>/dev/null; then
    echo "✓ JSON syntax valid"

    # Check SonarQube entry
    url=$(jq -r '.mcpServers.sonarqube.url' ~/.copilot/mcp-config.json 2>/dev/null)
    if [ -n "$url" ] && [ "$url" != "null" ]; then
      echo "✓ SonarQube URL: $url"
    else
      echo "⚠ SonarQube not configured"
    fi
  else
    echo "❌ Invalid JSON"
  fi
else
  echo "❌ Config file not found"
fi
```

**Test the CLI**:

```bash
gh copilot suggest "analyze this code for SonarQube issues"
```

## Troubleshooting

### SonarQube Tools Not Available

Verify the configuration file is valid:

```bash
cat ~/.copilot/mcp-config.json | jq '.'
```

### Connection Errors

Check network connectivity to the remote server:

```bash
curl -s https://sonarqube-mcp.bitso.io/mcp
```

### Multiple Servers

You can have multiple MCP servers configured:

```json
{
  "mcpServers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "tools": ["*"]
    },
    "atlas": {
      "type": "http",
      "url": "https://atlas-mcp.bitso.io/mcp",
      "tools": ["*"]
    }
  }
}
```

## Related

- [intellij-setup.md](intellij-setup.md) - IntelliJ configuration
- [mcp-tools.md](mcp-tools.md) - Available MCP tools
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-sonarqube/references/copilot-cli-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

