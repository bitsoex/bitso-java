# Branch Naming

Create branches with Jira key after ticket creation or discovery.

## Contents

- [Branch Format](#branch-format)
- [Branch Types](#branch-types)
- [Examples by Work Type](#examples-by-work-type)
- [Multiple Iterations](#multiple-iterations)
- [Before Creating Branch](#before-creating-branch)

---
## Branch Format

```text
{type}/{JIRA-KEY}-{short-description}[-part-N]
```

Where:
- `{type}` - Branch type prefix
- `{JIRA-KEY}` - Actual ticket key (e.g., PROJ-123)
- `{short-description}` - Brief description with hyphens
- `[-part-N]` - Optional suffix for multiple iterations

## Branch Types

| Type | Purpose | Example |
|------|---------|---------|
| `fix/` | Bug fixes, vulnerabilities | `fix/PROJ-123-critical-vulnerabilities` |
| `feat/` | New features | `feat/PROJ-456-user-authentication` |
| `chore/` | Maintenance, dependencies | `chore/PROJ-789-update-spring-boot` |
| `test/` | Test improvements | `test/PROJ-101-coverage-payment-service` |
| `docs/` | Documentation | `docs/PROJ-102-api-documentation` |
| `refactor/` | Code restructuring | `refactor/PROJ-103-simplify-validation` |

## Examples by Work Type

### Security Fix

```bash
git checkout -b fix/${JIRA_KEY}-critical-vulnerabilities
# Example: fix/PROJ-123-critical-vulnerabilities
```

### Quality Fix (SonarQube)

```bash
git checkout -b fix/${JIRA_KEY}-blocker-sonar-issues
# Example: fix/PROJ-456-blocker-sonar-issues
```

### Test Coverage

```bash
git checkout -b test/${JIRA_KEY}-coverage-payment-service
# Example: test/PROJ-789-coverage-payment-service
```

### Dependency Update

```bash
git checkout -b chore/${JIRA_KEY}-update-spring-boot
# Example: chore/PROJ-101-update-spring-boot
```

## Multiple Iterations

When work spans multiple PRs:

```bash
git checkout -b fix/${JIRA_KEY}-critical-vulnerabilities-part-1
git checkout -b fix/${JIRA_KEY}-critical-vulnerabilities-part-2
```

## Before Creating Branch

1. Ensure you have a Jira key (from ticket search or creation)
2. Fetch latest from remote: `git fetch --all`
3. Ensure main is up to date: `git checkout main && git pull`
4. Create and switch to branch

```bash
# Complete workflow
git fetch --all
git checkout main
git pull origin main
git checkout -b fix/${JIRA_KEY}-description
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-integration/references/branch-naming.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

