---
applyTo: "**/*"
description: Mcp Setup
---

# MCP Setup

## Overview

MCP (Model Context Protocol) server configurations are distributed to IDEs in different ways:

## IDE Configuration Behavior

### Automatic (Repository-based)

These IDEs automatically detect and use MCP configurations from the repository:

- **Cursor** - reads `.cursor/mcp.json`
- **VS Code** (with GitHub Copilot) - reads `.vscode/mcp.json`
- **Claude Code** - reads `.mcp.json` from project root

### Manual (User-based)

These tools use configurations from the user's home directory:

- **IntelliJ IDEA** (with GitHub Copilot) - reads `~/.config/github-copilot/intellij/mcp.json`
- **GitHub Copilot CLI** - reads `~/.copilot/mcp-config.json`

## Setup IntelliJ & Copilot CLI

For these tools, follow the setup instructions in:

`java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md`

This guide contains step-by-step instructions to configure both tools to use the SonarQube MCP server (remote HTTP/SSE - no tokens or environment variables required).

## GitHub MCP Server Setup

The GitHub MCP server runs via Docker and requires a GitHub Personal Access Token.

### Prerequisites

1. **Docker** - Must be installed and running
2. **GITHUB_TOKEN** - Environment variable with your GitHub PAT

### Readiness Check

Run the readiness script to verify your setup:

```bash
./global/scripts/check-github-mcp-readiness.sh
```

### Manual Setup (if needed)

If the image is not available locally:

```bash
# Pull the image
docker pull ghcr.io/github/github-mcp-server:0.24.0

# If authentication is required
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

### Configuration

The GitHub MCP server is configured in read-only mode with lockdown enabled, providing access to:

- Dependabot alerts
- Code security features
- Secret protection
- Security advisories
- Labels management
- Pull requests
