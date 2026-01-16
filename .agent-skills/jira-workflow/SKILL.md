<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-workflow/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: jira-workflow
description: Jira ticket creation and management for AI agent commands, with severity-based processing and proper integration
version: 1.0.0
compatibility: All repositories with Jira integration

metadata:
  category: workflow
  tags:
    - jira
    - tickets
    - tracking
    - severity-processing
  triggers:
    - on-demand
  uses:
    - pr-lifecycle
---

# Jira Workflow

Best practices for AI agents to create and manage Jira tickets when performing automated work like fixing vulnerabilities, resolving SonarQube issues, or improving test coverage.

## Core Principles

1. **Create Ticket Before Work** - Always create/find a Jira ticket before starting
2. **Discover Project Key** - Never hardcode project keys
3. **Search Before Creating** - Check for existing tickets first
4. **Severity-Based Processing** - Process issues one severity level at a time
5. **Link Everything** - Connect Jira → Branch → Commits → PR

## Workflow Overview

| Step | Description | Reference |
|------|-------------|-----------|
| **0. Discover** | Find user's Jira project key | `references/project-discovery.md` |
| **1. Search** | Check for existing open tickets | `references/ticket-search.md` |
| **2. Create** | Create ticket if none exists | `references/ticket-creation.md` |
| **3. Branch** | Create branch with Jira key | `references/branch-naming.md` |
| **4. Process** | Fix by severity level | `references/severity-processing.md` |

## Quick Reference

### Emoji Conventions

| Work Type | Emoji | Example |
|-----------|-------|---------|
| AI-assisted (all) | 🤖 | Required in ALL AI commits/PRs |
| Security/Vulnerability | 🛡️ | `🤖 🛡️ fix(security): resolve critical CVE` |
| Code Quality/SonarQube | ✅ | `🤖 ✅ fix(quality): resolve BLOCKER issues` |
| Test Coverage | 🧪 | `🤖 🧪 test: improve coverage` |
| Dependency Updates | 📦 | `🤖 📦 chore(deps): update Spring Boot` |
| Documentation | 📝 | `🤖 📝 docs: update API documentation` |
| Performance | ⚡ | `🤖 ⚡ perf: optimize queries` |
| Refactoring | ♻️ | `🤖 ♻️ refactor: simplify error handling` |

### Ticket Summary Format

```text
🤖🛡️ Fix [SEVERITY] Dependabot vulnerabilities in [repo-name]
🤖✅ Resolve [SEVERITY] SonarQube issues in [repo-name]
🤖🧪 Improve test coverage for [module/class]
🤖📦 Update [dependency] to [version]
```

### Branch Naming

```text
{type}/{JIRA-KEY}-{short-description}
```

Examples:
- `fix/PROJ-123-critical-vulnerabilities`
- `fix/PROJ-456-blocker-sonar-issues`
- `test/PROJ-789-coverage-payment-service`

## References

| Reference | Content |
|-----------|---------|
| `references/project-discovery.md` | How to discover user's Jira project key |
| `references/ticket-search.md` | JQL queries to find existing tickets |
| `references/ticket-creation.md` | Create tickets with proper format |
| `references/branch-naming.md` | Branch naming with Jira keys |
| `references/severity-processing.md` | Process by severity level |

## Severity-Based Processing

### Vulnerability Severity Order

1. **CRITICAL** - Fix first
2. **HIGH** - Only after no CRITICAL remain
3. **MEDIUM/MODERATE** - Only after no HIGH remain
4. **LOW** - Only after no MEDIUM remain

### SonarQube Severity Order

1. **BLOCKER** - Fix first
2. **CRITICAL** - Only after no BLOCKER remain
3. **MAJOR** - Only after no CRITICAL remain
4. **MINOR** - Only after no MAJOR remain
5. **INFO** - Only after no MINOR remain

## Best Practices

1. **One severity per PR** - Keep PRs focused and reviewable
2. **Batch related fixes** - Group similar issues in one commit
3. **Clear descriptions** - Document what was fixed and why
4. **Link everything** - Jira ticket ↔ Branch ↔ Commits ↔ PR
5. **Update ticket status** - Move ticket through workflow as work progresses

## Skill Dependencies

| Skill | Purpose |
|-------|---------|
| `pr-lifecycle` | PR creation, commit formats, GitHub CLI |

## Related

- `.agent-skills/pr-lifecycle` - PR creation and management
- `.agent-skills/stacked-prs` - Stacked PR workflows
