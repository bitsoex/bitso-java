# SonarQube MCP Tools Reference

## Available Tools

### list_projects

List all SonarQube projects you have access to.

**Usage**: "List all my SonarQube projects"

### get_issues

Get issues for a specific project.

**Parameters**:
- `project_key` (required): Project key in SonarQube
- `severity` (optional): BLOCKER, CRITICAL, MAJOR, MINOR, INFO
- `type` (optional): BUG, VULNERABILITY, CODE_SMELL
- `status` (optional): OPEN, CONFIRMED, REOPENED, RESOLVED, CLOSED

**Usage**: "Find CRITICAL BUG issues in payment-service"

### get_issue_details

Get details for a specific issue.

**Parameters**:
- `issue_key` (required): The issue key (e.g., AYx123...)

**Usage**: "Get details for issue AYx12345"

### get_rule

Get documentation for a SonarQube rule.

**Parameters**:
- `rule_key` (required): The rule key (e.g., java:S1128)

**Usage**: "Explain rule java:S2259"

### get_quality_gate

Get quality gate status for a project.

**Parameters**:
- `project_key` (required): Project key

**Usage**: "Check quality gate for my-service"

### analyze_code

Analyze a code snippet for issues.

**Parameters**:
- `code` (required): Code to analyze
- `language` (optional): Programming language

**Usage**: "Analyze this Java code for issues: [paste code]"

## Severity Levels

| Severity | Priority | Description |
|----------|----------|-------------|
| BLOCKER | 1st | Production-breaking issues |
| CRITICAL | 2nd | Security or major bugs |
| MAJOR | 3rd | Significant code smells |
| MINOR | 4th | Minor improvements |
| INFO | 5th | Informational issues |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/sonarqube-integration/references/mcp-tools.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

