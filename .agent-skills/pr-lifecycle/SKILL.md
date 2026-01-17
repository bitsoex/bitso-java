---
name: pr-lifecycle
description: GitHub CLI commands and workflows for managing pull requests, CI checks, CodeRabbit reviews, and branch protection
version: 1.0.0
compatibility: All repositories with GitHub

metadata:
  category: workflow
  tags:
    - pull-requests
    - git
    - github-cli
    - code-review
    - branch-protection
  triggers:
    - on-demand
  uses:
    - coderabbit-interactions
    - jira-workflow
---

# PR Lifecycle Management

Provides comprehensive workflows for managing pull requests using GitHub CLI (`gh`), including CI checks, CodeRabbit reviews, and branch protection.

## Core Principles

1. **Never Work on Main** - All work must happen on feature/fix/chore branches
2. **AI-Assisted Work Identification** - Use 🤖 emoji in all AI-generated commits and PRs
3. **Auto-Assignment** - Always assign PRs to the current user after creation
4. **Prevent Terminal Buffering** - Use echo wrappers for `gh` commands

## Skill Contents

### Sections

- [Core Principles](#core-principles) (L26-L32)
- [Workflow Overview](#workflow-overview) (L56-L64)
- [Quick Reference](#quick-reference) (L65-L112)
- [References](#references) (L113-L122)
- [Best Practices Summary](#best-practices-summary) (L123-L132)
- [Skill Dependencies](#skill-dependencies) (L133-L139)
- [Related](#related) (L140-L144)

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
| **Commits** | Use proper emoji and attribution format | `references/commit-formats.md` |

## Quick Reference

### AI-Assisted Work Emojis

| Work Type | Emoji Prefix | Example |
|-----------|-------------|---------|
| Security/Vulnerability | 🤖 🛡️ | `🤖 🛡️ fix(security): resolve critical CVE` |
| Code Quality/SonarQube | 🤖 ✅ | `🤖 ✅ fix(quality): resolve BLOCKER issues` |
| Test Coverage | 🤖 🧪 | `🤖 🧪 test: improve coverage for Service` |
| Dependency Updates | 🤖 📦 | `🤖 📦 chore(deps): update Spring Boot` |
| Documentation | 🤖 📝 | `🤖 📝 docs: update API documentation` |
| Performance | 🤖 ⚡ | `🤖 ⚡ perf: optimize queries` |
| Refactoring | 🤖 ♻️ | `🤖 ♻️ refactor: simplify error handling` |
| General AI work | 🤖 | `🤖 feat: add new feature` |

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
    --title "🤖 🛡️ [JIRA-KEY] fix(security): description" \
    --body "## 🤖 AI-Assisted Changes..." \
    --repo owner/repo 2>&1)

PR_NUMBER=$(echo "$PR_URL" | grep -oE '[0-9]+$')
gh pr edit $PR_NUMBER --repo owner/repo --add-assignee "$CURRENT_USER"
```

## References

| Reference | Content |
|-----------|---------|
| `references/branch-protection.md` | Branch naming, never commit to main |
| `references/pr-creation.md` | PR creation with auto-assignment |
| `references/coderabbit-integration.md` | CodeRabbit status checks and thread replies |
| `references/commit-formats.md` | Commit message templates with attribution |
| `references/troubleshooting.md` | Common issues and solutions |

## Best Practices Summary

1. **Always use 🤖 emoji** in commits, PR titles, and PR bodies for AI-assisted work
2. **Auto-assign PRs** to current user using `gh api user --jq '.login'`
3. **Include Jira key** in branch names, commits, and PR titles
4. **Always use echo wrapper** for `gh` commands to prevent buffering
5. **Always specify `--repo owner/repo`** to avoid directory context issues
6. **Use GraphQL API** for review threads, comments, and replies
7. **Work on other tasks while CI runs** - Don't block on CodeRabbit reviews

## Skill Dependencies

| Skill | Purpose |
|-------|---------|
| `coderabbit-interactions` | Thread replies, comment export, local CLI reviews |
| `jira-workflow` | Ticket creation and Jira key integration |

## Related

- `.agent-skills/coderabbit-interactions` - Detailed CodeRabbit interaction patterns
- `.agent-skills/jira-workflow` - Jira ticket workflow
- `.agent-skills/stacked-prs` - Stacked PR management
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-lifecycle/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

