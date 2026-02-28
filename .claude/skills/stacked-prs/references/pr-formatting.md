# PR Formatting for Stacked PRs

## Contents

- [PR Title Format](#pr-title-format)
- [PR Description Template](#pr-description-template)
- [Stack Visualization Status Icons](#stack-visualization-status-icons)
- [dpulls Compatibility](#dpulls-compatibility)
- [Examples](#examples)
- [Updating Stack Status](#updating-stack-status)

---
## PR Title Format

All stacked PRs must include position indicators:

```text
[JIRA-KEY] type(scope): description (PR N/M)
```

### Components

| Component | Description | Example |
|-----------|-------------|---------|
| `[JIRA-KEY]` | Jira ticket reference | `[EN-113]` |
| `type(scope)` | Conventional commit format | `feat(skills):` |
| `(PR N/M)` | Stack position | `(PR 2/4)` |

### Examples by Type

| Type | Example |
|------|---------|
| Dependencies | `[EN-113] chore(deps): update pnpm (PR 1/4)` |
| Refactoring | `[EN-113] refactor: convert scripts (PR 2/4)` |
| New Feature | `[EN-113] feat: add skills (PR 3/4)` |
| Cleanup | `[EN-113] chore: cleanup (PR 4/4)` |
| Security | `[SEC-45] fix(security): patch CVE (PR 1/2)` |
| Quality | `[QA-12] fix(quality): resolve issues (PR 1/1)` |
| Testing | `[TEST-8] test: improve coverage (PR 1/1)` |

## PR Description Template

```markdown
## Summary

Jira: [JIRA-KEY](https://bitsomx.atlassian.net/browse/JIRA-KEY)

[Brief description of what this PR accomplishes]

## PR Stack

| # | PR | Title | Status |
|---|-----|-------|--------|
| 1 | #78 | Description of PR 1 | Merged |
| 2 | **#79** | Description of PR 2 | **This PR** |
| 3 | #80 | Description of PR 3 | Waiting |
| 4 | #81 | Description of PR 4 | Waiting |

## Changes

- Change 1
- Change 2
- Change 3

## Validation

- [ ] Build passes locally
- [ ] Tests pass locally
- [ ] Lint passes locally

## Merge Order

PRs must be merged in order: #78 -> #79 -> #80 -> #81
```

## Stack Visualization Status Icons

| Status | Meaning |
|--------|---------|
| Merged | PR has been merged to main |
| **This PR** | Current PR being viewed |
| Waiting | Waiting for previous PR to merge |
| In Review | Ready for review, waiting on reviewers |
| Draft | Still in draft, not ready for review |

## dpulls Compatibility

[dpulls](https://www.dpulls.com/docs#/dependencies) automatically detects PR dependencies using keywords like "Depends on #N" or "Blocked by #N". This can cause issues with stacked PRs.

**Problem**: Using "Depends on #79" in the stack table triggers dpulls to mark the PR as blocked, even when #79 is already merged.

**Solution**: Use neutral language in the stack visualization:

| Avoid | Use Instead |
|-------|-------------|
| `Depends on #79` | `Waiting` or `After #79` |
| `Blocked by #80` | `Waiting` |
| `This PR depends on #78` | `Merges after #78` or omit entirely |

**Only use dependency language** when you actually want dpulls to track the dependency (i.e., the blocking PR is still open).

## Examples

### Good PR Title

```text
[EN-113] refactor: convert shell scripts to JavaScript (PR 2/4)
```

### Good Stack Visualization

```markdown
## PR Stack

| # | PR | Title | Status |
|---|-----|-------|--------|
| 1 | #78 | PNPM migration | Merged |
| 2 | **#79** | Shell to JS conversions | **This PR** |
| 3 | #80 | Skills content and features | Waiting |
| 4 | #81 | Validation and CI updates | Waiting |

> **Merge Order**: #78 -> #79 -> #80 -> #81 (each PR builds on the previous).
```

## Updating Stack Status

When a PR is merged, update the stack visualization in all remaining PRs:

1. Change merged PR status to `Merged`
2. Update "This PR" indicator to next PR in stack
3. Adjust dependency references

**Always use merge commits to propagate changes, never rebase.**
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/references/pr-formatting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

