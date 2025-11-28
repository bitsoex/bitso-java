# Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

# Add SonarQube MCP to IntelliJ and Copilot CLI

Configures the SonarQube MCP server (remote HTTP/SSE) for IntelliJ IDEA's GitHub Copilot extension and the GitHub Copilot CLI tool.

## Overview

This command sets up SonarQube MCP integration using the remote server at `https://sonarqube-mcp.bitso.io/mcp` for:

- **IntelliJ IDEA**: GitHub Copilot extension MCP configuration
- **Copilot CLI**: Command-line Copilot MCP configuration

**No tokens or environment variables required** - the remote server handles authentication.

## Configuration Locations

### IntelliJ IDEA

- **File**: `~/.config/github-copilot/intellij/mcp.json`
- **Format**: HTTP/SSE server URL

### Copilot CLI

- **File**: `~/.copilot/mcp-config.json`
- **Format**: HTTP/SSE server URL

## Workflow

### Step 1: Check for Existing Configurations

```bash
echo "=== Checking for existing MCP configurations ==="
echo

# Check IntelliJ
if [ -f ~/.config/github-copilot/intellij/mcp.json ]; then
  echo "✓ IntelliJ config found at ~/.config/github-copilot/intellij/mcp.json"
  # Validate JSON
  if jq empty ~/.config/github-copilot/intellij/mcp.json 2>/dev/null; then
    intellij_exists=true
  else
    echo "❌ ERROR: IntelliJ config file is not valid JSON"
    echo "   Please fix or remove: ~/.config/github-copilot/intellij/mcp.json"
    exit 1
  fi
else
  echo "→ IntelliJ config not found (will create)"
  intellij_exists=false
fi

echo

# Check Copilot CLI
if [ -f ~/.copilot/mcp-config.json ]; then
  echo "✓ Copilot CLI config found at ~/.copilot/mcp-config.json"
  # Validate JSON
  if jq empty ~/.copilot/mcp-config.json 2>/dev/null; then
    cli_exists=true
  else
    echo "❌ ERROR: Copilot CLI config file is not valid JSON"
    echo "   Please fix or remove: ~/.copilot/mcp-config.json"
    exit 1
  fi
else
  echo "→ Copilot CLI config not found (will create)"
  cli_exists=false
fi

echo
```

### Step 2: Backup Existing Configurations

```bash
# Backup IntelliJ if exists
if [ "$intellij_exists" = true ]; then
  cp ~/.config/github-copilot/intellij/mcp.json ~/.config/github-copilot/intellij/mcp.json.backup
  echo "✓ Backed up IntelliJ config to mcp.json.backup"
fi

# Backup Copilot CLI if exists
if [ "$cli_exists" = true ]; then
  cp ~/.copilot/mcp-config.json ~/.copilot/mcp-config.json.backup
  echo "✓ Backed up Copilot CLI config to mcp-config.json.backup"
fi

echo
```

### Step 3: Configure IntelliJ IDEA

Create or update the IntelliJ MCP configuration:

```bash
# Create directory if it doesn't exist
mkdir -p ~/.config/github-copilot/intellij

if [ "$intellij_exists" = false ]; then
  # Create new config file
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
  echo "✓ Created new IntelliJ MCP configuration"

else
  # Merge with existing config (preserve other server configs)
  existing=$(cat ~/.config/github-copilot/intellij/mcp.json)

  if ! echo "$existing" | jq '.servers.sonarqube = {"type": "http", "url": "https://sonarqube-mcp.bitso.io/mcp"}' > ~/.config/github-copilot/intellij/mcp.json.tmp; then
    echo "❌ ERROR: Failed to merge IntelliJ config (jq error)"
    echo "   Backup available at: ~/.config/github-copilot/intellij/mcp.json.backup"
    exit 1
  fi

  mv ~/.config/github-copilot/intellij/mcp.json.tmp ~/.config/github-copilot/intellij/mcp.json
  echo "✓ Merged SonarQube config into existing IntelliJ configuration (preserving other servers)"
fi

echo "✓ IntelliJ MCP configuration at ~/.config/github-copilot/intellij/mcp.json"
```

**After configuring IntelliJ:**

1. Restart IntelliJ IDEA for the configuration to take effect
2. Test the connection by using SonarQube features in Copilot

### Step 4: Configure Copilot CLI

Create or update the Copilot CLI MCP configuration:

```bash
# Create directory if it doesn't exist
mkdir -p ~/.copilot

if [ "$cli_exists" = false ]; then
  # Create new config file
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
  echo "✓ Created new Copilot CLI MCP configuration"

else
  # Merge with existing config (preserve other server configs)
  existing=$(cat ~/.copilot/mcp-config.json)

  if ! echo "$existing" | jq '.mcpServers.sonarqube = {"type": "http", "url": "https://sonarqube-mcp.bitso.io/mcp", "tools": ["*"]}' > ~/.copilot/mcp-config.json.tmp; then
    echo "❌ ERROR: Failed to merge Copilot CLI config (jq error)"
    echo "   Backup available at: ~/.copilot/mcp-config.json.backup"
    exit 1
  fi

  mv ~/.copilot/mcp-config.json.tmp ~/.copilot/mcp-config.json
  echo "✓ Merged SonarQube config into existing Copilot CLI configuration (preserving other servers)"
fi

echo "✓ Copilot CLI MCP configuration at ~/.copilot/mcp-config.json"
```

### Step 5: Verify Configurations

```bash
echo "=== Configuration Verification ==="
echo

errors=0

# Verify IntelliJ config
if [ -f ~/.config/github-copilot/intellij/mcp.json ]; then
  echo "✓ IntelliJ MCP config exists at ~/.config/github-copilot/intellij/mcp.json"
  
  # Validate JSON syntax
  if jq empty ~/.config/github-copilot/intellij/mcp.json 2>/dev/null; then
    echo "  ✓ JSON syntax valid"
    if command -v jq &> /dev/null; then
      url=$(jq -r '.servers.sonarqube.url' ~/.config/github-copilot/intellij/mcp.json 2>/dev/null)
      if [ -n "$url" ] && [ "$url" != "null" ]; then
        echo "  ✓ SonarQube URL configured: $url"
      else
        echo "  ⚠ Warning: SonarQube configuration not found"
        ((errors++))
      fi
    fi
  else
    echo "  ❌ JSON syntax invalid - please fix manually"
    ((errors++))
  fi
else
  echo "❌ IntelliJ MCP config not found"
  ((errors++))
fi

echo

# Verify Copilot CLI config
if [ -f ~/.copilot/mcp-config.json ]; then
  echo "✓ Copilot CLI MCP config exists at ~/.copilot/mcp-config.json"
  
  # Validate JSON syntax
  if jq empty ~/.copilot/mcp-config.json 2>/dev/null; then
    echo "  ✓ JSON syntax valid"
    if command -v jq &> /dev/null; then
      url=$(jq -r '.mcpServers.sonarqube.url' ~/.copilot/mcp-config.json 2>/dev/null)
      if [ -n "$url" ] && [ "$url" != "null" ]; then
        echo "  ✓ SonarQube URL configured: $url"
      else
        echo "  ⚠ Warning: SonarQube configuration not found"
        ((errors++))
      fi
    fi
  else
    echo "  ❌ JSON syntax invalid - please fix manually"
    ((errors++))
  fi
else
  echo "❌ Copilot CLI MCP config not found"
  ((errors++))
fi

echo
echo "=== Next Steps ==="
echo
echo "IntelliJ IDEA:"
echo "  1. Restart IntelliJ IDEA"
echo "  2. Test by using Copilot inline suggestions in your IDE"
echo
echo "Copilot CLI:"
echo "  1. Test with: gh copilot suggest 'analyze this code'"
echo

if [ "$errors" -gt 0 ]; then
  echo "⚠ Configuration completed with $errors warning(s). Review above."
else
  echo "✓ Configuration verified successfully!"
fi
echo
```

## Configuration Formats

### IntelliJ IDEA Format

HTTP/SSE configuration. Uses `servers` root key:

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

### Copilot CLI Format

HTTP/SSE configuration with tools array:

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

## Troubleshooting

### IntelliJ IDEA

**Issue**: MCP server not connecting

- **Solution**: Verify the remote server is accessible:

  ```bash
  curl -s https://sonarqube-mcp.bitso.io/mcp
  ```

**Issue**: Configuration not loading

- **Solution**: Restart IntelliJ IDEA after making configuration changes

### Copilot CLI

**Issue**: SonarQube tools not available

- **Solution**: Verify the configuration file exists and is valid:

  ```bash
  cat ~/.copilot/mcp-config.json | jq '.'
  ```

**Issue**: Connection errors

- **Solution**: Check network connectivity to the remote server:

  ```bash
  curl -s https://sonarqube-mcp.bitso.io/mcp
  ```

## Related

- See `java/rules/java-sonarqube-setup.md` for SonarQube MCP overview
- See `java/commands/fix-sonarqube-issues.md` for using SonarQube MCP tools
