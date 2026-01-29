---
name: pr-workflow
description: >
  Create, update, and manage pull requests using GitHub CLI. Includes branch
  protection, auto-assignment, and commit formats. For stacked PRs, see the
  stacked-prs skill.
compatibility: All repositories with GitHub
metadata:
  version: "1.0.0"
  category: workflow
  tags:
    - pull-requests
    - git
    - github-cli
    - branch-protection
  triggers:
    - on-demand
  uses:
    - coderabbit-workflow
    - jira-integration
---

# PR Workflow Management

Workflows for managing pull requests using GitHub CLI (`gh`), including CI checks, branch protection, and commit formatting.

## Core Principles

1. **Never Work on Main** - All work must happen on feature/fix/chore branches
2. **Auto-Assignment** - Always assign PRs to the current user after creation
3. **Prevent Terminal Buffering** - Use echo wrappers for `gh` commands
4. **Conventional Commits** - Follow conventional commit format for all commits

## When to use this skill

- Creating and managing single pull requests
- Setting up branch protection workflows
- Understanding commit message formats
- Managing CI checks on PRs

For stacked (dependent) PRs, see the [stacked-prs](.claude/skills/stacked-prs/SKILL.md) skill.

## Skill Contents

### Sections

- [Core Principles](#core-principles) (L27-L33)
- [When to use this skill](#when-to-use-this-skill) (L34-L42)
- [Workflow Overview](#workflow-overview) (L67-L75)
- [Quick Reference](#quick-reference) (L76-L120)
- [CodeRabbit Integration](#coderabbit-integration) (L121-L136)
- [References](#references) (L137-L146)
- [Best Practices](#best-practices) (L147-L155)
- [Related Skills](#related-skills) (L156-L162)

### Available Resources

**📚 references/** - Detailed documentation
- [branch protection](references/branch-protection.md)
- [coderabbit integration](references/coderabbit-integration.md)
- [commit formats](references/commit-formats.md)
- [pr creation](references/pr-creation.md)
- [troubleshooting](references/troubleshooting.md)

---

## Workflow Overview

| Phase | Description | Reference |
|-------|-------------|-----------|
| **Branch Setup** | Create feature branches, never commit to main | `references/branch-protection.md` |
| **PR Creation** | Create draft PRs with proper formatting | `references/pr-creation.md` |
| **CI & Reviews** | Monitor CI, respond to CodeRabbit | `references/coderabbit-integration.md` |
| **Commits** | Use conventional commit format | `references/commit-formats.md` |

## Quick Reference

### Branch Naming Convention

```text
{type}/{JIRA-KEY}-{description}
```

- `feat/` - New features
- `fix/` - Bug fixes
- `chore/` - Maintenance
- `docs/` - Documentation
- `refactor/` - Code restructuring

### Prevent Terminal Buffering

```bash
# Always use echo wrapper for gh commands
echo "Checking..." ; gh pr checks 123 --repo owner/repo ; echo "Done"
```

### Create and Assign PR

```bash
CURRENT_USER=$(gh api user --jq '.login')

PR_URL=$(gh pr create --draft \
    --title "[JIRA-KEY] fix(security): description" \
    --body "## Summary..." \
    --repo owner/repo 2>&1)

PR_NUMBER=$(echo "$PR_URL" | grep -oE '[0-9]+$')
gh pr edit $PR_NUMBER --repo owner/repo --add-assignee "$CURRENT_USER"
```

### Check PR Status

```bash
# Check CI status
gh pr checks <PR_NUMBER> --repo owner/repo

# View PR details
gh pr view <PR_NUMBER> --repo owner/repo
```

## CodeRabbit Integration

### Key Requirements

1. All CI checks must pass before marking ready
2. Address all CodeRabbit comments including nitpicks
3. CodeRabbit approves automatically after addressing feedback

### Co-Author for CodeRabbit Fixes

```text
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

See the [coderabbit-workflow](.claude/skills/coderabbit-workflow/SKILL.md) skill for detailed patterns.

## References

| Reference | Content |
|-----------|---------|
| `references/branch-protection.md` | Branch naming, never commit to main |
| `references/pr-creation.md` | PR creation with auto-assignment |
| `references/coderabbit-integration.md` | CodeRabbit status checks and thread replies |
| `references/commit-formats.md` | Commit message templates with attribution |
| `references/troubleshooting.md` | Common issues and solutions |

## Best Practices

1. **Auto-assign PRs** to current user using `gh api user --jq '.login'`
2. **Include Jira key** in branch names, commits, and PR titles
3. **Always use echo wrapper** for `gh` commands to prevent buffering
4. **Always specify `--repo owner/repo`** to avoid directory context issues
5. **Use GraphQL API** for review threads, comments, and replies
6. **Work on other tasks while CI runs** - Don't block on CodeRabbit reviews

## Related Skills

| Skill | Purpose |
|-------|---------|
| [coderabbit-workflow](.claude/skills/coderabbit-workflow/SKILL.md) | Thread replies, comment export, local CLI reviews |
| [jira-integration](.claude/skills/jira-integration/SKILL.md) | Ticket creation and Jira key integration |
| [stacked-prs](.claude/skills/stacked-prs/SKILL.md) | Stacked PR management and merge workflows |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-workflow/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

