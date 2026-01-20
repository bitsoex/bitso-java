# Fix SonarQube issues in Java projects using MCP tools and CI feedback

**Description:** Fix SonarQube issues in Java projects using MCP tools and CI feedback

# Fix SonarQube Issues (Java)

Fix SonarQube issues with severity-based processing for Java projects.

## Prerequisites

- Project analyzed at least once in CI
- SonarQube MCP is pre-configured

## Skill Location

```
.claude/skills/sonarqube-integration/
```

## Quick Workflow

1. **Create Jira ticket** before any code changes
2. **Create branch** with Jira key: `fix/PROJ-123-blocker-sonar-issues`
3. **Search issues** by severity using SonarQube MCP
4. **Fix in batches** of max 5 issues
5. **Run tests** with `-x codeCoverageReport` for speed
6. **Create PR** and wait for CI

## Severity Processing Order

| Priority | Severity | When to Process |
|----------|----------|-----------------|
| 1 | BLOCKER | Always process first |
| 2 | CRITICAL | Only after NO BLOCKER remain |
| 3 | MAJOR | Only after NO CRITICAL remain |
| 4 | MINOR | Only after NO MAJOR remain |

## Common Java Rules

| Rule ID | Issue | Fix |
|---------|-------|-----|
| java:S1128 | Unused imports | Remove import |
| java:S1161 | Missing @Override | Add annotation |
| java:S3740 | Raw types | Add `<>` |
| java:S1643 | String concat in loop | Use StringBuilder |
| java:S2259 | Null pointer | Add null check |
| java:S2583 | Condition always true/false | Fix logic |

## Test Command

```bash
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Tests passed"
```

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full sonarqube-integration documentation |
| `references/mcp-tools.md` | MCP tool reference |
| `references/common-rules.md` | Common Java rules and fixes |

## Related

- `.claude/skills/jira-workflow` - Jira ticket workflow
- `.claude/skills/pr-lifecycle` - PR creation and management
- `java/rules/java-gradle-commands.md` - Gradle commands reference

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/fix-sonarqube-issues-java.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
