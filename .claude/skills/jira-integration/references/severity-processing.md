# Severity-Based Processing

AI agents must process issues by severity level, one level at a time.

## Contents

- [Vulnerability Severity Order](#vulnerability-severity-order)
- [SonarQube Severity Order](#sonarqube-severity-order)
- [Processing Rules](#processing-rules)
- [Example Workflow](#example-workflow)
- [Commit Examples by Severity](#commit-examples-by-severity)
- [When to Stop](#when-to-stop)
- [One Severity Per PR](#one-severity-per-pr)

---
## Vulnerability Severity Order

| Priority | Severity | When to Process |
|----------|----------|-----------------|
| 1 | CRITICAL | Always process first if any exist |
| 2 | HIGH | Only after NO CRITICAL remain |
| 3 | MEDIUM/MODERATE | Only after NO HIGH remain |
| 4 | LOW | Only after NO MEDIUM remain |

## SonarQube Severity Order

| Priority | Severity | When to Process |
|----------|----------|-----------------|
| 1 | BLOCKER | Always process first if any exist |
| 2 | CRITICAL | Only after NO BLOCKER remain |
| 3 | MAJOR | Only after NO CRITICAL remain |
| 4 | MINOR | Only after NO MAJOR remain |
| 5 | INFO | Only after NO MINOR remain |

## Processing Rules

1. **Query current severity** - Check what's the highest severity present
2. **Focus on one level** - Only fix issues of that severity
3. **Atomic commits** - Each commit should address related issues
4. **Create new ticket for next level** - After completing one severity, create a new ticket for the next

## Example Workflow

```text
1. Query: Found 2 CRITICAL, 5 HIGH, 10 MEDIUM vulnerabilities
2. Create ticket: "🤖🛡️ Fix CRITICAL vulnerabilities in repo-name"
3. Fix only CRITICAL issues
4. Commit, push, create PR
5. After merge, create new ticket: "🤖🛡️ Fix HIGH vulnerabilities in repo-name"
6. Repeat until all severities addressed
```

## Commit Examples by Severity

### CRITICAL/BLOCKER

```bash
git commit -m "🤖 🛡️ fix(security): [PROJ-123] resolve critical CVE-2024-xxxxx

- Updated commons-compress to 1.27.1
- Added dependency substitution for transitive deps

Severity: CRITICAL

Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command."
```

### HIGH/CRITICAL

```bash
git commit -m "🤖 ✅ fix(quality): [PROJ-456] resolve CRITICAL SonarQube issues

- Fixed SQL injection vulnerability in UserRepository
- Added input validation in PaymentController

Severity: CRITICAL
Rules: java:S3649, java:S5131

Generated with the Security Agent by the /fix-sonarqube-issues command."
```

### MEDIUM/MAJOR

```bash
git commit -m "🤖 ✅ fix(quality): [PROJ-789] resolve MAJOR SonarQube issues

- Reduced cognitive complexity in OrderService
- Removed unused parameters in processPayment

Severity: MAJOR
Rules: java:S3776, java:S1172"
```

## When to Stop

Stop processing current severity when:

- All issues of that severity are resolved
- Quality gate passes
- Tests pass

Then create a new ticket for the next severity level and repeat.

## One Severity Per PR

Keep PRs focused and reviewable:

- Each PR should address one severity level
- Create separate tickets/PRs for each severity level
- This makes review and rollback easier
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-integration/references/severity-processing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

