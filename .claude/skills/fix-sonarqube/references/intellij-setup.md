---
title: IntelliJ IDEA Setup
description: Configure SonarQube MCP for IntelliJ IDEA GitHub Copilot extension
---

# IntelliJ IDEA Setup

Configure the SonarQube MCP server for IntelliJ IDEA's GitHub Copilot extension.

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

IntelliJ IDEA uses the GitHub Copilot extension for MCP integration. The SonarQube MCP server runs remotely at `https://sonarqube-mcp.bitso.io/mcp`.

**No tokens or environment variables required** - the remote server handles authentication.

## Configuration Location

- **File**: `~/.config/github-copilot/intellij/mcp.json`
- **Format**: HTTP/SSE server URL
- **Scope**: User-level (applies to all IntelliJ instances)

## Setup Steps

### Step 1: Check Existing Configuration

```bash
if [ -f ~/.config/github-copilot/intellij/mcp.json ]; then
  echo "✓ IntelliJ config found"
  # Validate JSON
  if jq empty ~/.config/github-copilot/intellij/mcp.json 2>/dev/null; then
    echo "  ✓ Valid JSON"
  else
    echo "  ❌ Invalid JSON - fix or remove file"
    exit 1
  fi
else
  echo "→ IntelliJ config not found (will create)"
fi
```

### Step 2: Backup (if exists)

```bash
if [ -f ~/.config/github-copilot/intellij/mcp.json ]; then
  cp ~/.config/github-copilot/intellij/mcp.json ~/.config/github-copilot/intellij/mcp.json.backup
  echo "✓ Backed up to mcp.json.backup"
fi
```

### Step 3: Create or Update Configuration

```bash
# Create directory if needed
mkdir -p ~/.config/github-copilot/intellij

# Check if file exists
if [ ! -f ~/.config/github-copilot/intellij/mcp.json ]; then
  # Create new config
  cat > ~/.config/github-copilot/intellij/mcp.json << 'EOF'
{
  "servers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp"
    }
  }
}
EOF
  echo "✓ Created new configuration"
else
  # Merge with existing config
  existing=$(cat ~/.config/github-copilot/intellij/mcp.json)
  echo "$existing" | jq '.servers.sonarqube = {"type": "http", "url": "https://sonarqube-mcp.bitso.io/mcp"}' \
    > ~/.config/github-copilot/intellij/mcp.json
  echo "✓ Merged SonarQube config (preserving other servers)"
fi
```

### Step 4: Restart IntelliJ

Configuration changes require an IDE restart to take effect.

## Configuration Format

IntelliJ uses HTTP/SSE configuration with `servers` root key:

```json
{
  "servers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp"
    }
  }
}
```

**Notes**:
- The `type` must be `"http"` for SSE servers
- Multiple servers can coexist (Atlas, SonarQube, etc.)

## Verification

```bash
echo "=== Verifying IntelliJ Configuration ==="

if [ -f ~/.config/github-copilot/intellij/mcp.json ]; then
  echo "✓ Config file exists"

  # Validate JSON
  if jq empty ~/.config/github-copilot/intellij/mcp.json 2>/dev/null; then
    echo "✓ JSON syntax valid"

    # Check SonarQube entry
    url=$(jq -r '.servers.sonarqube.url' ~/.config/github-copilot/intellij/mcp.json 2>/dev/null)
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

**Test in IntelliJ**:
1. Restart IntelliJ IDEA
2. Open a Java file
3. Use Copilot to ask: "Find SonarQube issues in this project"

## Troubleshooting

### MCP Server Not Connecting

Verify the remote server is accessible:

```bash
curl -s https://sonarqube-mcp.bitso.io/mcp
```

### Configuration Not Loading

- Ensure IntelliJ IDEA has been restarted after configuration changes
- Check file permissions: `ls -la ~/.config/github-copilot/intellij/mcp.json`

### Multiple Servers

You can have multiple MCP servers configured:

```json
{
  "servers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp"
    },
    "atlas": {
      "type": "http",
      "url": "https://atlas-mcp.bitso.io/mcp"
    }
  }
}
```

## Related

- [copilot-cli-setup.md](copilot-cli-setup.md) - CLI configuration
- [mcp-tools.md](mcp-tools.md) - Available MCP tools
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-sonarqube/references/intellij-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

