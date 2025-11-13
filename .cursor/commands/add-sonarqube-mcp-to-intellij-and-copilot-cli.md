# Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

# Add SonarQube MCP to IntelliJ and Copilot CLI

Configures the SonarQube MCP server for IntelliJ IDEA's GitHub Copilot extension and the GitHub Copilot CLI tool.

## Overview

This command sets up SonarQube MCP integration for:

- **IntelliJ IDEA**: GitHub Copilot extension MCP configuration
- **Copilot CLI**: Command-line Copilot MCP configuration

## Configuration Locations

### IntelliJ IDEA

- **File**: `~/.config/github-copilot/intellij/mcp.json`
- **Format**: VS Code-compatible with `${input:TOKEN}` syntax
- **Token Setup**: Click the token reference in the editor → redirects to **Tools > GitHub Copilot > Model Context Protocol (MCP) > Special Tokens**

### Copilot CLI

- **File**: `~/.copilot/mcp-config.json`
- **Format**: Custom format with `tools: ["*"]` array
- **Token Syntax**: `${input:SONARQUBE_TOKEN}`

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
    intellij_has_sonarqube=$(grep -c "sonarqube" ~/.config/github-copilot/intellij/mcp.json 2>/dev/null || echo 0)
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
    cli_has_sonarqube=$(grep -c "sonarqube" ~/.copilot/mcp-config.json 2>/dev/null || echo 0)
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
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "type": "http",
      "headers": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"
      }
    }
  },
  "inputs": [
    {
      "id": "SONARQUBE_TOKEN",
      "type": "promptString",
      "description": "SonarQube Server User Token",
      "password": true
    }
  ]
}
EOF
  echo "✓ Created new IntelliJ MCP configuration"

else
  # Merge with existing config (avoid duplicates)
  existing=$(cat ~/.config/github-copilot/intellij/mcp.json)
  
  if ! echo "$existing" | jq '.servers.sonarqube = {"url": "https://sonarqube-mcp.bitso.io/mcp", "type": "http", "headers": {"SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"}} | .inputs |= (. + [{"id": "SONARQUBE_TOKEN", "type": "promptString", "description": "SonarQube Server User Token", "password": true}] | unique_by(.id))' > ~/.config/github-copilot/intellij/mcp.json.tmp; then
    echo "❌ ERROR: Failed to merge IntelliJ config (jq error)"
    echo "   Backup available at: ~/.config/github-copilot/intellij/mcp.json.backup"
    exit 1
  fi
  
  mv ~/.config/github-copilot/intellij/mcp.json.tmp ~/.config/github-copilot/intellij/mcp.json
  echo "✓ Merged SonarQube config into existing IntelliJ configuration (deduped)"
fi

echo "✓ IntelliJ MCP configuration at ~/.config/github-copilot/intellij/mcp.json"
```

**Important**: After configuring IntelliJ:

1. Open `~/.config/github-copilot/intellij/mcp.json` in IntelliJ
2. Click on `${input:SONARQUBE_TOKEN}` in the editor
3. This will redirect you to: **Tools > GitHub Copilot > Model Context Protocol (MCP)**
4. Scroll to **Special Tokens** section at the bottom
5. Add your SonarQube token value for `SONARQUBE_TOKEN`

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
      "headers": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"
      },
      "tools": [
        "*"
      ]
    }
  }
}
EOF
  echo "✓ Created new Copilot CLI MCP configuration"

else
  # Merge with existing config (avoid duplicates)
  existing=$(cat ~/.copilot/mcp-config.json)
  
  if ! echo "$existing" | jq '.mcpServers.sonarqube = {"type": "http", "url": "https://sonarqube-mcp.bitso.io/mcp", "headers": {"SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"}, "tools": ["*"]}' > ~/.copilot/mcp-config.json.tmp; then
    echo "❌ ERROR: Failed to merge Copilot CLI config (jq error)"
    echo "   Backup available at: ~/.copilot/mcp-config.json.backup"
    exit 1
  fi
  
  mv ~/.copilot/mcp-config.json.tmp ~/.copilot/mcp-config.json
  echo "✓ Merged SonarQube config into existing Copilot CLI configuration"
fi

echo "✓ Copilot CLI MCP configuration at ~/.copilot/mcp-config.json"
```

**Note**: The Copilot CLI uses server properties directly without a separate inputs array. Token will be prompted when you use Copilot CLI.

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
        
        # Optional connectivity test (non-blocking)
        if command -v curl &> /dev/null; then
          if curl --head --fail --silent --show-error --max-time 3 "$url" &>/dev/null; then
            echo "  ✓ Server is reachable"
          else
            echo "  ⚠ Server not reachable (may be offline or require auth)"
          fi
        fi
      else
        echo "  ⚠ Warning: SonarQube URL not found"
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
        
        # Optional connectivity test (non-blocking)
        if command -v curl &> /dev/null; then
          if curl --head --fail --silent --show-error --max-time 3 "$url" &>/dev/null; then
            echo "  ✓ Server is reachable"
          else
            echo "  ⚠ Server not reachable (may be offline or require auth)"
          fi
        fi
      else
        echo "  ⚠ Warning: SonarQube URL not found"
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
echo "  1. Open ~/.config/github-copilot/intellij/mcp.json in IntelliJ"
echo "  2. Click on \${input:SONARQUBE_TOKEN} text"
echo "  3. Configure token in: Tools > GitHub Copilot > MCP > Special Tokens"
echo "  4. Test by using Copilot inline suggestions in your IDE"
echo
echo "Copilot CLI:"
echo "  1. Token will be prompted when using Copilot CLI"
echo "  2. Test with: gh copilot suggest 'analyze this code'"
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

Similar to VS Code with `servers` root key:

```json
{
  "servers": {
    "sonarqube": {
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "type": "http",
      "headers": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"
      }
    }
  },
  "inputs": [
    {
      "id": "SONARQUBE_TOKEN",
      "type": "promptString",
      "description": "SonarQube Server User Token",
      "password": true
    }
  ]
}
```

### Copilot CLI Format

Custom format with `mcpServers` root key and `tools` array:

```json
{
  "mcpServers": {
    "sonarqube": {
      "type": "http",
      "url": "https://sonarqube-mcp.bitso.io/mcp",
      "headers": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}"
      },
      "tools": [
        "*"
      ]
    }
  }
}
```

## Troubleshooting

### IntelliJ IDEA

**Issue**: Token not being used

- **Solution**: Ensure you clicked `${input:SONARQUBE_TOKEN}` in the editor and configured it in Special Tokens

**Issue**: MCP server not showing in IntelliJ

- **Solution**: Restart IntelliJ IDEA after creating/updating the config file

**Issue**: Cannot find Special Tokens section

- **Solution**: Navigate to **Tools > GitHub Copilot > Model Context Protocol (MCP)** and scroll to the bottom

### Copilot CLI

**Issue**: MCP server not available

- **Solution**: Ensure `~/.copilot/mcp-config.json` exists and is valid JSON

**Issue**: Tools not working

- **Solution**: Verify the `tools: ["*"]` array is present (required for Copilot CLI)

**Issue**: Authentication errors

- **Solution**: The token will be prompted at runtime; ensure you have a valid SonarQube token ready

## Related

- See `java/rules/java-sonarqube-setup.md` for SonarQube MCP setup in VS Code, Cursor, and Claude
- See `java/commands/fix-sonarqube-issues.md` for using SonarQube MCP tools

## References

- [IntelliJ Copilot MCP Support Issue](https://github.com/microsoft/copilot-intellij-feedback/issues/653)
- SonarQube MCP Server: `https://sonarqube-mcp.bitso.io/mcp`
