---
applyTo: "**/*.java"
description: Java SonarQube Setup Guide
---

# Java SonarQube Setup Guide

SonarQube MCP integration is pre-configured and requires no local setup.

## How It Works

The SonarQube MCP server runs remotely at `https://sonarqube-mcp.bitso.io/mcp` and is automatically configured in all supported IDEs through the repository's MCP configuration files.

**No setup required:**

- No tokens to generate
- No environment variables to configure
- No Docker containers to run

## Using SonarQube

Just use natural language in your AI assistant:

- "Find HIGH severity issues in project-name"
- "Analyze this Java code for SonarQube issues"
- "Show me details about rule java:S1128"
- "What's the quality gate status for project-name?"

**Full tool reference**: See `java/rules/java-sonarqube-mcp.md`

## Supported IDEs

The SonarQube MCP server is automatically available in:

- **Cursor** - via `.cursor/mcp.json`
- **VS Code** (with GitHub Copilot) - via `.vscode/mcp.json`
- **Claude Code** - via `.mcp.json`
- **IntelliJ IDEA** (with GitHub Copilot) - see `java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md`
- **GitHub Copilot CLI** - see `java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md`

## Troubleshooting

### MCP tools not available

1. Ensure your IDE has loaded the repository's MCP configuration
2. Restart your AI assistant
3. Verify the remote server is accessible: `curl -s https://sonarqube-mcp.bitso.io/mcp`

### Can't find project

Ask the AI: "List all my SonarQube projects"

## Related

- **MCP Tools**: `java/rules/java-sonarqube-mcp.md`
- **Fix Command**: `java/commands/fix-sonarqube-issues.md`
- **MCP Config**: `java/mcp/mcp.json`
- **SonarQube**: <https://sonarqube.bitso.io>

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-sonarqube-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
