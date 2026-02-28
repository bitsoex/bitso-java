# Coverage Checking via SonarQube MCP

Use SonarQube MCP tools to check and improve code coverage without running local JaCoCo builds.

## Contents

- [When to Use](#when-to-use)
- [MCP vs Local Coverage](#mcp-vs-local-coverage)
- [Workflow: Identify and Improve Coverage](#workflow-identify-and-improve-coverage)
- [Tool Reference](#tool-reference)
- [Example Prompts](#example-prompts)
- [PR Coverage Workflow](#pr-coverage-workflow)

---

## When to Use

- **Before writing tests**: identify which files need coverage the most
- **During PR review**: check coverage impact without waiting for CI
- **Quality gate failing**: quickly find which files are dragging coverage down
- **Coverage planning**: prioritize test efforts across the project
- **Avoiding slow local builds**: skip `./gradlew test jacocoTestReport` just to check current state

## MCP vs Local Coverage

| Approach | Best For | Limitation |
|----------|----------|------------|
| **SonarQube MCP** | Checking current state, finding gaps, PR-specific analysis | Reflects last CI analysis, not local uncommitted changes |
| **JaCoCo (local)** | Running tests, generating new coverage data, verifying thresholds | Requires local Gradle execution, slower feedback |

Use MCP to **discover** coverage gaps, then JaCoCo to **generate** and **verify** new coverage.

## Workflow: Identify and Improve Coverage

### 1. Find Low-Coverage Files

Use `search_files_by_coverage` to identify files needing tests:

```text
projectKey: "payment-service"
```

For PR-specific coverage:

```text
projectKey: "payment-service", pullRequest: "247"
```

### 2. Get Line-Level Coverage Details

Use `get_file_coverage_details` to see which lines are covered:

```text
key: "payment-service:src/main/java/com/bitso/payment/PaymentService.java"
```

### 3. Check Project-Level Metrics

Use `get_component_measures` for overall coverage:

```text
component: "payment-service", metricKeys: ["coverage", "ncloc", "violations"]
```

For a specific module or package:

```text
component: "payment-service:src/main/java/com/bitso/payment", metricKeys: ["coverage"]
```

### 4. Write Tests for Uncovered Code

Based on the line-level details from step 2, write tests targeting the uncovered lines:

```bash
# Run tests with coverage locally to verify
./gradlew clean test jacocoTestReport

# Check HTML report for visual confirmation
open build/reports/jacoco/test/html/index.html
```

### 5. Verify Quality Gate

After pushing, check the quality gate via MCP:

```text
get_project_quality_gate_status: projectKey: "payment-service", pullRequest: "247"
```

## Tool Reference

| Tool | Purpose | Key Parameter |
|------|---------|---------------|
| `search_files_by_coverage` | Find files with lowest coverage | `projectKey` |
| `get_file_coverage_details` | Line-by-line coverage for a file | `key` (file component key) |
| `get_component_measures` | Coverage metric for project/dir/file | `component`, `metricKeys: ["coverage"]` |
| `get_project_quality_gate_status` | Check if coverage gate passes | `projectKey` |
| `list_pull_requests` | Discover PR IDs for PR-specific queries | `projectKey` |

## Example Prompts

Use these prompts with your AI agent:

```text
"What files have the lowest coverage in payment-service?"

"Show me line-by-line coverage for PaymentService.java in payment-service"

"Is the quality gate passing for PR #247 in payment-service?"

"Our code coverage dropped below 82%. Which files are dragging it down?"

"Check the coverage impact of my pull request #123 on order-service"
```

## PR Coverage Workflow

When reviewing a PR for coverage:

1. **Discover PR ID**: use `list_pull_requests` with `projectKey` to find the PR
2. **Check PR coverage**: use `search_files_by_coverage` with `pullRequest` parameter
3. **Review specific files**: use `get_file_coverage_details` for changed files
4. **Check quality gate**: use `get_project_quality_gate_status` with `pullRequest`

This replaces the need to run `./gradlew test jacocoTestReport` locally just to check coverage status.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-sonarqube/references/coverage-via-mcp.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

