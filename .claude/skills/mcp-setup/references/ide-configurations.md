# IDE Configurations

MCP server configurations for each IDE and tool.

## Repository-Based (Automatic)

These IDEs automatically detect and use MCP configurations from the repository.

### Cursor

**Configuration File:** `.cursor/mcp.json`

Cursor reads MCP configuration from the repository's `.cursor` directory.

### VS Code (with GitHub Copilot)

**Configuration File:** `.vscode/mcp.json`

VS Code with GitHub Copilot reads MCP configuration from the repository's `.vscode` directory.

### Claude Code

**Configuration File:** `.mcp.json`

Claude Code reads MCP configuration from the project root.

## User-Based (Manual)

These tools use configurations from the user's home directory and require manual setup.

### IntelliJ IDEA (with GitHub Copilot)

**Configuration File:** `~/.config/github-copilot/intellij/mcp.json`

For setup instructions, see:
`java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md`

### GitHub Copilot CLI

**Configuration File:** `~/.copilot/mcp-config.json`

For setup instructions, see:
`java/commands/add-sonarqube-mcp-to-intellij-and-copilot-cli.md`

## Configuration Format

All MCP configurations use the same JSON format:

```json
{
    "mcpServers": {
        "server-name": {
            "command": "docker",
            "args": ["run", "-i", "--rm", "..."],
            "env": {
                "ENV_VAR": "value"
            }
        }
    }
}
```

## Common MCP Servers

| Server | Purpose |
|--------|---------|
| `github` | GitHub API access for PRs, security, etc. |
| `sonarqube` | SonarQube code quality analysis |
| `atlassian` | Jira and Confluence integration |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/mcp-setup/references/ide-configurations.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

