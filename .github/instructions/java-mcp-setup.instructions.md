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
