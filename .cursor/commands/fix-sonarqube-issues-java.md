# Fix SonarQube issues in Java projects using MCP tools and CI feedback

Fix SonarQube issues in Java projects using MCP tools and CI feedback

# 🤖 ✅ Fix SonarQube Issues (Java)

## Prerequisites

- Project analyzed at least once in CI
- SonarQube MCP is pre-configured and available automatically in supported IDEs

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first (Step 1), then create if none found**
- **MCP Tools**: `java/rules/java-sonarqube-mcp.md`
- **Setup**: `java/rules/java-sonarqube-setup.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Use `-x codeCoverageReport` for faster tests

## Severity-Based Processing (CRITICAL)

**Process issues by severity, one level at a time:**

| Priority | Severity | When to Process |
|----------|----------|-----------------|
| 1 | BLOCKER | Always process first if any exist |
| 2 | CRITICAL | Only after NO BLOCKER remain |
| 3 | MAJOR | Only after NO CRITICAL remain |
| 4 | MINOR | Only after NO MAJOR remain |
| 5 | INFO | Only after NO MINOR remain |

**Workflow**:

1. Query all issues for the project
2. Identify highest severity present
3. Fix ONLY that severity level
4. Create separate ticket/PR for next severity level

## Workflow

### 1. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, create a Jira ticket for tracking:

Use `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 ✅ Fix [SEVERITY] SonarQube issues in [repo-name]`
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Description**: Include count of issues by severity

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 2. Create Branch with Jira Key

```bash
# JIRA_KEY is the actual ticket key from Step 1 (e.g., PROJ-123)
SEVERITY="blocker"  # or critical, major, minor

git checkout -b fix/${JIRA_KEY}-${SEVERITY}-sonar-issues
```

### 3. Get Context

```bash
git branch --show-current
```

### 4. Search for Issues by Severity

**Use `search_sonar_issues_in_projects` - ALWAYS filter by project, severity, and use small page size.**

**Required:**

- `projects: ["project-key"]` - NEVER omit, prevents context overload
- `ps: 10` - **Start with 10** (default is 100 which causes context overload), increase only if needed
- `severities: ["BLOCKER"]` - **Filter by current target severity**

**Optional:**

- `pullRequestId: "123"` - For PR-specific issues

**Severity Order (process one at a time):**

```text
1. severities: ["BLOCKER"]   - Fix these FIRST
2. severities: ["CRITICAL"]  - Only after NO BLOCKER remain
3. severities: ["MAJOR"]     - Only after NO CRITICAL remain
4. severities: ["MINOR"]     - Only after NO MAJOR remain
5. severities: ["INFO"]      - Only after NO MINOR remain
```

**Examples:**

"Find BLOCKER issues in business-reports"
→ `projects: ["business-reports"], severities: ["BLOCKER"], ps: 10`

"Show CRITICAL issues for PR #456 in dynamic-pricing-tool"
→ `projects: ["dynamic-pricing-tool"], severities: ["CRITICAL"], pullRequestId: "456", ps: 10`

**Note:** Search returns component keys like `"project:path/to/File.java"` - use with `get_component_measures` for file metrics.

### 5. Fix in Small Batches

Work on **max 5 issues** at a time:

1. Read file at component path from search results
2. If needed: "Show me details about rule [RULE_KEY]"
3. Apply fix at line number shown
4. Run tests locally (use `-x codeCoverageReport` for speed)
5. Move to next issue

**Test command:**

```bash
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Tests passed"
```

### 6. Common Java Auto-Fixable Rules

| Rule ID | Issue | Fix |
|---------|-------|-----|
| java:S1128 | Unused imports | Remove import |
| java:S1161 | Missing @Override | Add annotation |
| java:S3740 | Raw types | Add `<>` |
| java:S1643 | String concat in loop | Use StringBuilder |
| java:S1192 | Duplicated strings | Extract constant |
| java:S1135 | TODO comments | Address or remove |
| java:S2259 | Null pointer | Add null check |
| java:S2583 | Condition always true/false | Fix logic |
| java:S2589 | Redundant boolean | Simplify |

### 7. Commit with Emojis and Jira Key

```bash
# JIRA_KEY is the actual ticket key (e.g., PROJ-123)
TARGET_SEVERITY="BLOCKER"

git add -A
git commit -m "🤖 ✅ fix(quality): [$JIRA_KEY] resolve $TARGET_SEVERITY SonarQube issues

- Fixed null pointer in PaymentService (java:S2259)
- Added missing @Override annotations (java:S1161)
- Removed unused imports (java:S1128)

Severity: $TARGET_SEVERITY
Rules: java:S2259, java:S1161, java:S1128

Generated with the Security Agent by the /fix-sonarqube-issues command."
```

### 8. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

# JIRA_KEY is the actual ticket key (e.g., PROJ-123)
TARGET_SEVERITY="BLOCKER"

gh pr create --draft \
    --title "🤖 ✅ [$JIRA_KEY] fix(quality): resolve $TARGET_SEVERITY SonarQube issues" \
    --body "## 🤖 AI-Assisted Quality Fixes

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Severity Level
$TARGET_SEVERITY

## Issues Fixed

| Rule | Description | Files |
|------|-------------|-------|
| java:S2259 | Null pointer | PaymentService.java |
| java:S1161 | Missing @Override | 3 files |

## Validation
- [x] Build passes locally
- [x] Tests pass locally
- [ ] SonarQube analysis passes

## AI Agent Details
- **Agent**: Security Agent
- **Command**: /fix-sonarqube-issues

Generated with the Security Agent by the /fix-sonarqube-issues command.

## References
- SonarQube project: [link]"
```

### 9. Verify

Check metrics: "Show coverage and violations for [PROJECT_KEY]"
(Uses `get_component_measures`)

Or check quality gate: "What's the quality gate status for [PROJECT_KEY]?"

If failing, repeat from Search for Issues.

### 10. Create Ticket for Next Severity (If Applicable)

After this PR is merged, if there are remaining issues of lower severity, create a new Jira ticket and repeat the process.

## Guidelines

- **Create Jira ticket FIRST** before any code changes
- **ALWAYS use projectKey** to filter searches
- **Process ONE severity at a time** - BLOCKER → CRITICAL → MAJOR → MINOR → INFO
- **Start with ps: 10** - increase only if you need more results
- Work in small batches (max 5 issues)
- **Use `-x codeCoverageReport`** for faster test execution
- Commit and push after each batch
- Rely on CI for verification
- Stop when: Quality gate passes, no issues of current severity remain, tests pass

## Related

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **Required** - Ticket creation and emoji conventions
- **PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md` - PR creation with emojis
- **MCP Tools**: `java/rules/java-sonarqube-mcp.md`
- **Setup**: `java/rules/java-sonarqube-setup.md`
- **Gradle**: `java/rules/java-gradle-best-practices.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md`
