# Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

> Configure SonarQube MCP server for IntelliJ IDEA and GitHub Copilot CLI

# Add SonarQube MCP to IntelliJ and Copilot CLI

Configures the SonarQube MCP server (Docker-based) for IntelliJ IDEA's GitHub Copilot extension and the GitHub Copilot CLI tool.

## Overview

This command sets up SonarQube MCP integration using Docker containerization for:

- **IntelliJ IDEA**: GitHub Copilot extension MCP configuration (Docker)
- **Copilot CLI**: Command-line Copilot MCP configuration (Docker)

## Configuration Locations

### IntelliJ IDEA

- **File**: `~/.config/github-copilot/intellij/mcp.json`
- **Format**: Docker command with stdio transport
- **Token Setup**: Configure environment variables via IDE settings or shell exports
- **Docker Image**: Uses `mcp/sonarqube` Docker image (must be available locally or on Docker Hub)

### Copilot CLI

- **File**: `~/.copilot/mcp-config.json`
- **Format**: Docker command with stdio transport
- **Token Setup**: Configure environment variables via shell exports
- **Docker Image**: Uses `mcp/sonarqube` Docker image

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
      "type": "stdio",
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SONARQUBE_TOKEN",
        "-e",
        "SONARQUBE_URL",
        "mcp/sonarqube"
      ],
      "env": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}",
        "SONARQUBE_URL": "https://sonarqube.bitso.io"
      }
    }
  }
}
EOF
  echo "✓ Created new IntelliJ MCP configuration (Docker-based)"

else
  # Merge with existing config (avoid duplicates)
  existing=$(cat ~/.config/github-copilot/intellij/mcp.json)

  if ! echo "$existing" | jq '.servers.sonarqube = {"type": "stdio", "command": "docker", "args": ["run", "-i", "--rm", "-e", "SONARQUBE_TOKEN", "-e", "SONARQUBE_URL", "mcp/sonarqube"], "env": {"SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}", "SONARQUBE_URL": "https://sonarqube.bitso.io"}}' > ~/.config/github-copilot/intellij/mcp.json.tmp; then
    echo "❌ ERROR: Failed to merge IntelliJ config (jq error)"
    echo "   Backup available at: ~/.config/github-copilot/intellij/mcp.json.backup"
    exit 1
  fi

  mv ~/.config/github-copilot/intellij/mcp.json.tmp ~/.config/github-copilot/intellij/mcp.json
  echo "✓ Merged SonarQube config into existing IntelliJ configuration (Docker-based)"
fi

echo "✓ IntelliJ MCP configuration at ~/.config/github-copilot/intellij/mcp.json"
```

**Important**: After configuring IntelliJ:

1. Ensure Docker is installed and running: `docker --version`
2. Pull the SonarQube MCP image: `docker pull mcp/sonarqube`
3. Set environment variables in your shell (or IntelliJ IDE settings):

   ```bash
   export SONARQUBE_TOKEN="your-sonarqube-token"
   ```

   **Note:** `SONARQUBE_URL` is hardcoded in the configuration (`https://sonarqube.bitso.io`) and does not need to be exported. Only `SONARQUBE_TOKEN` is read from the environment.

4. Restart IntelliJ IDEA for the configuration to take effect
5. Test the connection by using SonarQube features in Copilot

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
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SONARQUBE_TOKEN",
        "-e",
        "SONARQUBE_URL",
        "mcp/sonarqube"
      ],
      "tools": ["*"],
      "env": {
        "SONARQUBE_TOKEN": "${env:SONARQUBE_TOKEN}",
        "SONARQUBE_URL": "https://sonarqube.bitso.io"
      }
    }
  }
}
EOF
  echo "✓ Created new Copilot CLI MCP configuration (Docker-based)"

else
  # Merge with existing config (avoid duplicates)
  existing=$(cat ~/.copilot/mcp-config.json)

  if ! echo "$existing" | jq '.mcpServers.sonarqube = {"command": "docker", "args": ["run", "-i", "--rm", "-e", "SONARQUBE_TOKEN", "-e", "SONARQUBE_URL", "mcp/sonarqube"], "tools": ["*"], "env": {"SONARQUBE_TOKEN": "${env:SONARQUBE_TOKEN}", "SONARQUBE_URL": "https://sonarqube.bitso.io"}}' > ~/.copilot/mcp-config.json.tmp; then
    echo "❌ ERROR: Failed to merge Copilot CLI config (jq error)"
    echo "   Backup available at: ~/.copilot/mcp-config.json.backup"
    exit 1
  fi

  mv ~/.copilot/mcp-config.json.tmp ~/.copilot/mcp-config.json
  echo "✓ Merged SonarQube config into existing Copilot CLI configuration (Docker-based)"
fi

echo "✓ Copilot CLI MCP configuration at ~/.copilot/mcp-config.json"
```

**Note**: The Copilot CLI uses Docker to run the SonarQube MCP server. Make sure environment variables are exported in your shell:

```bash
export SONARQUBE_TOKEN="your-sonarqube-token"
```

**Note:** `SONARQUBE_URL` is hardcoded in the configuration (`https://sonarqube.bitso.io`) and does not need to be exported. Only `SONARQUBE_TOKEN` is read from the environment.

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
      command=$(jq -r '.servers.sonarqube.command' ~/.config/github-copilot/intellij/mcp.json 2>/dev/null)
      if [ -n "$command" ] && [ "$command" != "null" ]; then
        echo "  ✓ SonarQube Docker command configured: $command"
        
        # Check if Docker is available
        if command -v docker &> /dev/null; then
          echo "  ✓ Docker is available"
        else
          echo "  ⚠ Warning: Docker not found in PATH"
        fi
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
      command=$(jq -r '.mcpServers.sonarqube.command' ~/.copilot/mcp-config.json 2>/dev/null)
      if [ -n "$command" ] && [ "$command" != "null" ]; then
        echo "  ✓ SonarQube Docker command configured: $command"
        
        # Check if Docker is available
        if command -v docker &> /dev/null; then
          echo "  ✓ Docker is available"
        else
          echo "  ⚠ Warning: Docker not found in PATH"
        fi
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

Docker-based configuration with stdio transport. Uses `servers` root key (not `mcpServers`) and `${input:VAR}` syntax:

```json
{
  "servers": {
    "sonarqube": {
      "type": "stdio",
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SONARQUBE_TOKEN",
        "-e",
        "SONARQUBE_URL",
        "mcp/sonarqube"
      ],
      "env": {
        "SONARQUBE_TOKEN": "${input:SONARQUBE_TOKEN}",
        "SONARQUBE_URL": "https://sonarqube.bitso.io"
      }
    }
  }
}
```

### Copilot CLI Format

Docker-based configuration with stdio transport and tools array:

```json
{
  "mcpServers": {
    "sonarqube": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "SONARQUBE_TOKEN",
        "-e",
        "SONARQUBE_URL",
        "mcp/sonarqube"
      ],
      "tools": ["*"],
      "env": {
        "SONARQUBE_TOKEN": "${env:SONARQUBE_TOKEN}",
        "SONARQUBE_URL": "https://sonarqube.bitso.io"
      }
    }
  }
}
```

## Troubleshooting

### IntelliJ IDEA

**Issue**: Docker image not found

- **Solution**: Pull the image with `docker pull mcp/sonarqube` or build it locally

**Issue**: MCP server not starting

- **Solution**: Ensure Docker is running and environment variables are set:

  ```bash
  export SONARQUBE_TOKEN="your-token"
  docker --version  # Verify Docker is installed
  ```

  **Note:** `SONARQUBE_URL` is hardcoded in the configuration and does not need to be exported.

**Issue**: Permission denied when running Docker

- **Solution**: Add your user to the docker group:

  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

### Copilot CLI

**Issue**: Docker command not found

- **Solution**: Ensure Docker is installed and in your PATH:

  ```bash
  which docker
  docker --version
  ```

**Issue**: Environment variables not being picked up

- **Solution**: Ensure variables are exported in your shell:

  ```bash
  export SONARQUBE_TOKEN="your-token"
  env | grep SONARQUBE_TOKEN  # Verify it's set
  ```

  **Note:** `SONARQUBE_URL` is hardcoded in the configuration (`https://sonarqube.bitso.io`) and does not need to be exported.

**Issue**: Connection refused errors

- **Solution**: Verify the SonarQube server URL is correct and reachable:

  ```bash
  curl -s "https://sonarqube.bitso.io/api/system/status" | jq .
  ```

## Related

- See `java/rules/java-sonarqube-setup.md` for SonarQube MCP setup in VS Code, Cursor, and Claude
- See `java/commands/fix-sonarqube-issues.md` for using SonarQube MCP tools

## References

- [IntelliJ Copilot MCP Support Issue](https://github.com/microsoft/copilot-intellij-feedback/issues/653)
- SonarQube MCP Docker Image: `mcp/sonarqube` (runs locally via Docker)
