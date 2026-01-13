---
description: SonarQube MCP Tools Reference
alwaysApply: false
globs: **/*.java
tags:
  - java
---

# SonarQube MCP Tools Reference

The SonarQube MCP server is pre-configured and available automatically in supported IDEs.

## Primary Tools

Use these tools in order of preference:

1. **`search_sonar_issues_in_projects`** - Find issues (ALWAYS use with projectKey and ps: 10)
2. **`get_component_measures`** - Get metrics for project or specific files
3. **`search_metrics`** - List available metrics (use ps: 10)
4. **`show_rule`** - Get rule details (only when working on specific issue)

**Avoid:** `analyze_code_snippet`, `search_my_sonarqube_projects`, other tools unless specifically needed.

## search_sonar_issues_in_projects

**ALWAYS filter by project to avoid context overload.**

**Required parameters:**

- `projects` - Array: `["project-key"]` (REQUIRED - never omit)
- `ps` - Page size: **Start with `10`** (default is 100 which causes context overload), increase only if needed

**Optional parameters:**

- `severities` - Array: `["HIGH", "BLOCKER"]` (valid: INFO, LOW, MEDIUM, HIGH, BLOCKER)
- `pullRequestId` - String: `"123"`
- `p` - Page number (default: 1)

**Examples:**

```text
Show BLOCKER issues in business-reports
→ projects: ["business-reports"], severities: ["BLOCKER"], ps: 10

Show issues for PR #123 in dynamic-pricing-tool
→ projects: ["dynamic-pricing-tool"], pullRequestId: "123", ps: 10
```

## get_component_measures

Get metrics for a project, directory, or specific file.

**Parameters:**

- `component` (required) - Component key:
  - Project: `"project-key"`
  - File: `"project-key:path/to/File.java"` (from search results)
- `metricKeys` (required) - Array: `["coverage", "complexity", "violations", "ncloc"]`
- `branch` (optional) - Branch name
- `pullRequest` (optional) - PR ID

**Examples:**

```text
Show coverage for entire business-reports project
→ component: "business-reports", metricKeys: ["coverage", "violations"]

Show complexity for specific file
→ component: "business-reports:src/main/java/com/bitso/Service.java", metricKeys: ["complexity", "ncloc"]
```

## search_metrics

List available SonarQube metrics.

**Parameters:**

- `ps` - Page size: **Start with `10`** (default is 100 which is too much), increase only if needed
- `p` - Page number (default: 1)

## show_rule

Get detailed rule information. **Use only when actively working on an issue.**

**Parameters:**

- `key` (required) - Rule key (e.g., "java:S1128")

## Other Tools (Use Sparingly)

### analyze_code_snippet

**Only for new code before commit.** Do NOT use for existing projects.

- `codeSnippet` (required) - Code to analyze
- `language` (optional) - e.g., "java"

### change_sonar_issue_status

- `key` (required) - Issue key
- `status` (required) - "accept", "falsepositive", or "reopen"

### get_project_quality_gate_status

- `projectKey` (required) - Project key
- `branch` (optional) - Branch key
- `pullRequest` (optional) - PR ID

## Common Auto-Fixable Rules

| Rule ID | Issue | Fix |
|---------|-------|-----|
| java:S1128 | Unused imports | Remove import |
| java:S1161 | Missing @Override | Add annotation |
| java:S3740 | Raw types | Add `<>` |
| java:S1643 | String concat in loop | Use StringBuilder |

## Related

- **Fix Command**: `java/commands/fix-sonarqube-issues.md`
- **Setup**: `java/rules/java-sonarqube-setup.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-sonarqube-mcp.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
