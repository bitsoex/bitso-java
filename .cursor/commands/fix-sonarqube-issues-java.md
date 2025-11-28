# Fix SonarQube issues in Java projects using MCP tools and CI feedback

Fix SonarQube issues in Java projects using MCP tools and CI feedback

# Fix SonarQube Issues (Java)

## Prerequisites

- Project analyzed at least once in CI
- SonarQube MCP is pre-configured and available automatically in supported IDEs

## Workflow

### Get Context

```bash
git branch --show-current
```

### Search for Issues

**Use `search_sonar_issues_in_projects` - ALWAYS filter by project and use small page size.**

**Required:**

- `projects: ["project-key"]` - NEVER omit, prevents context overload
- `ps: 10` - **Start with 10** (default is 100 which causes context overload), increase only if needed

**Optional:**

- `severities: ["HIGH", "BLOCKER"]` - Filter by severity (array format)
- `pullRequestId: "123"` - For PR-specific issues

**Examples:**

"Find BLOCKER issues in business-reports"
→ `projects: ["business-reports"], severities: ["BLOCKER"], ps: 10`

"Show issues for PR #456 in dynamic-pricing-tool"
→ `projects: ["dynamic-pricing-tool"], pullRequestId: "456", ps: 10`

**Priority:** BLOCKER → HIGH → Security vulnerabilities → Auto-fixable

**Note:** Search returns component keys like `"project:path/to/File.java"` - use with `get_component_measures` for file metrics.

### Fix in Small Batches

Work on **max 5 issues** at a time:

1. Read file at component path from search results
2. If needed: "Show me details about rule [RULE_KEY]"
3. Apply fix at line number shown
4. Run tests locally: `./gradlew test`
5. Move to next issue

### Common Java Auto-Fixable Rules

| Rule ID | Issue | Fix |
|---------|-------|-----|
| java:S1128 | Unused imports | Remove import |
| java:S1161 | Missing @Override | Add annotation |
| java:S3740 | Raw types | Add `<>` |
| java:S1643 | String concat in loop | Use StringBuilder |
| java:S1192 | Duplicated strings | Extract constant |
| java:S1135 | TODO comments | Address or remove |

### Commit and Push

```bash
git add -A
git commit -m "fix: resolve SonarQube issues [RULE_KEYS]"
git push
```

SonarQube analysis runs in CI.

### Verify

Check metrics: "Show coverage and violations for [PROJECT_KEY]"
(Uses `get_component_measures`)

Or check quality gate: "What's the quality gate status for [PROJECT_KEY]?"

If failing, repeat from Search for Issues.

## Guidelines

- **ALWAYS use projectKey** to filter searches
- **Start with ps: 10** - increase only if you need more results
- Work in small batches (max 5 issues)
- Commit and push after each batch
- Rely on CI for verification
- Stop when: Quality gate passes, no BLOCKER/HIGH remain, tests pass

## Related

- **MCP Tools**: `java/rules/java-sonarqube-mcp.md`
- **Setup**: `java/rules/java-sonarqube-setup.md`
- **Gradle**: `java/rules/java-gradle-best-practices.md`
