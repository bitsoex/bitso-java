# Disable VS Code MCP Discovery to Prevent Configuration Conflicts

> Disable VS Code MCP Discovery to Prevent Configuration Conflicts

# Disable VS Code MCP Discovery

Disable VS Code's MCP discovery feature to prevent configuration conflicts between tools.

## Purpose

VS Code's MCP discovery scans configuration files from multiple applications (Claude Desktop, Windsurf, Cursor), which can cause:
- Configuration conflicts
- Unexpected behavior from auto-discovered servers
- Errors from subtle differences between tool configs

## Skill Location

```
.skills/mcp-configuration/
```

## Quick Check

```bash
cat ~/Library/Application\ Support/Code/User/settings.json | grep -A 5 "chat.mcp.discovery.enabled"
```

## Quick Disable

See `.skills/mcp-configuration/references/vscode-discovery.md` for the complete disable script.

## Expected Result

```json
"chat.mcp.discovery.enabled": {
    "claude-desktop": false,
    "windsurf": false,
    "cursor-global": false,
    "cursor-workspace": false
}
```

## Skill Contents

| Resource | Description |
|----------|-------------|
| `references/vscode-discovery.md` | Complete disable script and re-enable options |
| `references/ide-configurations.md` | Configuration paths for each IDE |
| `references/troubleshooting.md` | Common issues and solutions |

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/disable-vscode-mcp-discovery.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
