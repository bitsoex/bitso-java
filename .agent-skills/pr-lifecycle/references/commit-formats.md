# Commit Message Formats

All AI-generated commits must follow these formats for proper attribution and tracking.

## Contents

- [Standard Format](#standard-format) (L14-L26)
- [Agent Attribution (REQUIRED)](#agent-attribution-required) (L27-L36)
- [Examples by Work Type](#examples-by-work-type) (L37-L128)
- [Emoji Quick Reference](#emoji-quick-reference) (L129-L141)
- [Format Rules](#format-rules) (L142-L149)

---
## Standard Format

```text
🤖 [TYPE_EMOJI] type(scope): [JIRA-KEY] short description

- Detail 1
- Detail 2

[Additional context if needed]

Generated with the [Agent Name] by the /[command-name] command.
```

## Agent Attribution (REQUIRED)

All AI-assisted commits and PRs must include attribution:

| Command | Attribution Line |
|---------|-----------------|
| `/fix-dependabot-vulnerabilities` | Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command. |
| `/fix-test-coverage` | Generated with the Quality Agent by the /fix-test-coverage command. |
| `/fix-sonarqube-issues` | Generated with the Security Agent by the /fix-sonarqube-issues command. |

## Examples by Work Type

### Security Fix

```bash
git commit -m "🤖 🛡️ fix(security): [PROJ-52] resolve critical CVE-2024-xxxxx

- Updated commons-compress to 1.27.1
- Added dependency substitution for transitive deps

Severity: CRITICAL

Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command."
```

### Quality Fix (SonarQube)

```bash
git commit -m "🤖 ✅ fix(quality): [PROJ-53] resolve BLOCKER SonarQube issues

- Fixed null pointer in PaymentService
- Added missing @Override annotations

Rules: java:S2259, java:S1161

Generated with the Security Agent by the /fix-sonarqube-issues command."
```

### Test Coverage

```bash
git commit -m "🤖 🧪 test: [PROJ-54] improve coverage for PaymentService

- Added tests for edge cases
- Increased coverage from 65% to 82%

Generated with the Quality Agent by the /fix-test-coverage command."
```

### CodeRabbit Feedback

```bash
git commit -m "🤖 fix: address CodeRabbit review feedback

- Fixed emoji spacing inconsistency
- Standardized format across examples

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

### Dependency Updates

```bash
git commit -m "🤖 📦 chore(deps): [PROJ-55] update Spring Boot to 3.5.8

- Updated parent POM version
- Verified compatibility with existing plugins

Generated with the Quality Agent by the /update-dependencies command."
```

### Documentation

```bash
git commit -m "🤖 📝 docs: [PROJ-56] update API documentation

- Added new endpoint documentation
- Updated authentication section
- Fixed broken links"
```

### Performance

```bash
git commit -m "🤖 ⚡ perf: [PROJ-57] optimize database queries

- Added index on frequently queried columns
- Implemented query caching for static data

Performance improvement: 40% reduction in p95 latency"
```

### Refactoring

```bash
git commit -m "🤖 ♻️ refactor: [PROJ-58] simplify error handling

- Consolidated exception handling in base class
- Removed redundant try-catch blocks
- Improved error messages"
```

## Emoji Quick Reference

| Emoji | Type | Usage |
|-------|------|-------|
| 🤖 | AI-assisted | **Required** in ALL AI commits |
| 🛡️ | Security | Vulnerability fixes |
| ✅ | Quality | SonarQube, linting fixes |
| 🧪 | Test | Test coverage improvements |
| 📦 | Dependencies | Dependency updates |
| 📝 | Documentation | Doc updates |
| ⚡ | Performance | Performance improvements |
| ♻️ | Refactoring | Code restructuring |

## Format Rules

1. **Always start with 🤖** - Required for all AI-assisted work
2. **Include type emoji after 🤖** - Helps categorize the change
3. **Use conventional commit format** - `type(scope): description`
4. **Include Jira key** - In brackets: `[PROJ-123]`
5. **Add attribution footer** - For commands that have specific agents
6. **Add co-author for CodeRabbit** - When fixing CodeRabbit issues
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-lifecycle/references/commit-formats.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

