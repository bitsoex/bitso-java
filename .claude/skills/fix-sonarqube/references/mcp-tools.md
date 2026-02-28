# SonarQube MCP Tools Reference

Complete reference for SonarQube MCP Server tools (v1.10+), organized by category.

## Contents

- [Analysis](#analysis)
- [Issues](#issues)
- [Coverage](#coverage)
- [Duplications](#duplications)
- [Security Hotspots](#security-hotspots)
- [Quality Gates](#quality-gates)
- [Projects and Pull Requests](#projects-and-pull-requests)
- [Measures and Metrics](#measures-and-metrics)
- [Rules](#rules)
- [Sources](#sources)
- [Dependency Risks](#dependency-risks)
- [System](#system)
- [Severity Model](#severity-model)

---

## Analysis

### analyze_code_snippet

Analyze a file or code snippet with SonarQube analyzers.

**Parameters:**
- `fileContent` (string, required): Complete file content
- `projectKey` (string, required): Project key
- `language` (string, optional): Language hint (e.g., `"java"`)
- `codeSnippet` (string, optional): Snippet to filter results to
- `scope` (string, optional): `"MAIN"` or `"TEST"` (default: MAIN)

Pass complete `fileContent` for full file analysis. Add `codeSnippet` to report only issues within that snippet.

**Usage:** "Analyze this Java code for SonarQube issues"

### analyze_file_list

Analyze files in the current working directory using SonarQube for IDE (requires IDE integration).

**Parameters:**
- `file_absolute_paths` (array of strings, required): Absolute file paths to analyze

## Issues

### search_sonar_issues_in_projects

Search for SonarQube issues across projects.

**Parameters:**
- `projects` (array, optional): Project key(s) - always include to avoid overload
- `severities` (array, optional): `["INFO", "LOW", "MEDIUM", "HIGH", "BLOCKER"]`
- `impactSoftwareQualities` (array, optional): `["MAINTAINABILITY", "RELIABILITY", "SECURITY"]`
- `issueStatuses` (array, optional): `["OPEN", "CONFIRMED", "FALSE_POSITIVE", "ACCEPTED", "FIXED"]`
- `issueKey` (string, optional): Fetch a specific issue
- `pullRequestId` (string, optional): PR-specific issues
- `ps` (integer, optional): Page size (max 500, default 100)
- `p` (integer, optional): Page number (default 1)

**Usage:** "Find BLOCKER issues in payment-service"

### change_sonar_issue_status

Change the status of a SonarQube issue.

**Parameters:**
- `key` (string, required): Issue key
- `status` (enum, required): `"accept"`, `"falsepositive"`, `"reopen"`

**Usage:** "Mark this issue as false positive"

## Coverage

### search_files_by_coverage

Find files with the lowest test coverage. Auto-fetches all pages by default (up to 10,000 files).

**Parameters:**
- `projectKey` (string, required): Project key
- `pullRequest` (string, optional): Pull request ID
- `pageSize` (integer, optional): Results per page (max 500)
- `pageIndex` (integer, optional): Page number (starts at 1)

**Usage:** "What files have the lowest coverage in my-service?"

### get_file_coverage_details

Get line-by-line coverage information for a file.

**Parameters:**
- `key` (string, required): File component key (e.g., `"project:src/main/java/Service.java"`)
- `pullRequest` (string, optional): Pull request ID

**Usage:** "Show me line-by-line coverage for PaymentService.java"

## Duplications

### search_duplicated_files

Find files with code duplications. Auto-fetches all pages by default (up to 10,000 files).

**Parameters:**
- `projectKey` (string, required): Project key
- `pullRequest` (string, optional): Pull request ID
- `pageSize` (integer, optional): Results per page (max 500)
- `pageIndex` (integer, optional): Page number (starts at 1)

**Usage:** "Find the most duplicated files in my-service"

### get_duplications

Get line-by-line duplication details for a file.

**Parameters:**
- `key` (string, required): File component key
- `pullRequest` (string, optional): Pull request ID

**Usage:** "Show duplication details for this file"

## Security Hotspots

### search_security_hotspots

Search for Security Hotspots in a project.

**Parameters:**
- `projectKey` (string, required): Project or application key
- `pullRequest` (string, optional): Pull request key
- `status` (string, optional): `"TO_REVIEW"` or `"REVIEWED"`
- `resolution` (string, optional): `"FIXED"`, `"SAFE"`, `"ACKNOWLEDGED"`
- `files` (array, optional): File paths to filter
- `sinceLeakPeriod` (boolean, optional): New code only
- `onlyMine` (boolean, optional): Assigned to me only
- `ps` (integer, optional): Page size (max 500)

**Usage:** "Search for security hotspots to review in payment-service"

### show_security_hotspot

Get detailed information about a specific Security Hotspot.

**Parameters:**
- `hotspotKey` (string, required): Security Hotspot key

**Usage:** "Show me details about this security hotspot"

### change_security_hotspot_status

Review a Security Hotspot by changing its status.

**Parameters:**
- `hotspotKey` (string, required): Security Hotspot key
- `status` (enum, required): `"TO_REVIEW"` or `"REVIEWED"`
- `resolution` (enum, when REVIEWED): `"FIXED"`, `"SAFE"`, `"ACKNOWLEDGED"`
- `comment` (string, optional): Review comment

**Usage:** "Mark this hotspot as safe"

## Quality Gates

### get_project_quality_gate_status

Get the quality gate status for a project.

**Parameters:**
- `projectKey` (string, optional): Project key
- `pullRequest` (string, optional): Pull request ID

**Usage:** "Check quality gate for my-service"

### list_quality_gates

List all quality gates in the SonarQube instance. No parameters required.

## Projects and Pull Requests

### search_my_sonarqube_projects

Find SonarQube projects. Paginated response.

**Parameters:**
- `page` (string, optional): Page number

**Usage:** "List all my SonarQube projects"

### list_pull_requests

List all pull requests for a project. Use to discover PR IDs for other tools.

**Parameters:**
- `projectKey` (string, required): Project key

**Usage:** "List pull requests for payment-service"

## Measures and Metrics

### get_component_measures

Get SonarQube measures for a component (project, directory, or file).

**Parameters:**
- `component` (string, optional): Component key
- `metricKeys` (array, optional): Metrics to fetch (e.g., `["coverage", "violations", "ncloc"]`)
- `pullRequest` (string, optional): Pull request ID

**Common Metrics:**

| Metric | Description |
|--------|-------------|
| `coverage` | Code coverage percentage |
| `ncloc` | Non-comment lines of code |
| `violations` | Total violations count |
| `blocker_violations` | BLOCKER count |
| `complexity` | Cyclomatic complexity |
| `duplicated_lines_density` | Duplication percentage |

**Usage:** "Show coverage and violations for my-service"

### search_metrics

Search for available SonarQube metrics.

**Parameters:**
- `ps` (integer, optional): Page size (max 500)
- `p` (integer, optional): Page number

## Rules

### show_rule

Get detailed information about a SonarQube rule.

**Parameters:**
- `key` (string, required): Rule key (e.g., `"java:S1128"`)

Returns rule description, how to fix, violation examples, and compliant code.

**Usage:** "Explain rule java:S2259"

## Sources

### get_raw_source

Get source code as raw text from SonarQube. Requires See Source Code permission.

**Parameters:**
- `key` (string, required): File key
- `pullRequest` (string, optional): Pull request ID

### get_scm_info

Get SCM (git blame) information for source files.

**Parameters:**
- `key` (string, required): File key
- `from` (number, optional): First line (starts at 1)
- `to` (number, optional): Last line (inclusive)

## Dependency Risks

### search_dependency_risks

Search for software composition analysis issues (SCA/dependency risks). Requires SonarQube Server 2025.4 Enterprise with Advanced Security.

**Parameters:**
- `projectKey` (string): Project key
- `branchKey` (string): Branch key
- `pullRequestKey` (string, optional): Pull request key

## System

System tools are only available when connecting to SonarQube Server.

| Tool | Purpose | Parameters |
|------|---------|------------|
| `get_system_health` | Instance health (GREEN/YELLOW/RED) | None |
| `get_system_status` | Status, version, and ID | None |
| `get_system_info` | Full system config (requires admin) | None |
| `get_system_logs` | System logs (requires admin) | `name`: app, access, ce, web, es |
| `ping_system` | Liveness check (returns "pong") | None |

## Severity Model

SonarQube supports two severity models depending on instance configuration.

### MQR Mode (default in SonarQube 2025.1+)

| Priority | Severity | Description |
|----------|----------|-------------|
| 1 | BLOCKER | Production-breaking, immediate fix required |
| 2 | HIGH | Critical impact, urgent fix required |
| 3 | MEDIUM | Significant code quality impact |
| 4 | LOW | Minor improvements |
| 5 | INFO | Informational, no expected impact |

Use `impactSoftwareQualities` to filter by quality dimension:
- `MAINTAINABILITY` - Code smells, technical debt
- `RELIABILITY` - Bugs, potential crashes
- `SECURITY` - Vulnerabilities, security risks

### Standard Experience Mode (legacy)

| Priority | Severity | Description |
|----------|----------|-------------|
| 1 | BLOCKER | Production-breaking issues |
| 2 | CRITICAL | Security or major bugs |
| 3 | MAJOR | Significant code smells |
| 4 | MINOR | Minor improvements |
| 5 | INFO | Informational only |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-sonarqube/references/mcp-tools.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

