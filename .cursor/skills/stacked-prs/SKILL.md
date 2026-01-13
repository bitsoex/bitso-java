<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: stacked-prs
description: Manage stacked PRs with proper visualization, merge-based updates, and iterative CodeRabbit feedback cycles
version: 1.0.0
compatibility: All repositories with GitHub

metadata:
  category: workflow
  tags:
    - pull-requests
    - git
    - code-review
    - coderabbit
  triggers:
    - on-demand
  uses:
    - coderabbit-interactions
---

# Stacked PRs Workflow

Provides workflows for managing stacked (dependent) pull requests with proper visualization, merge-based updates, and iterative CodeRabbit review cycles.

## Core Principles

1. **No Force Pushes** - Always use merge commits, never rebase/force-push
2. **Draft First** - PRs start as drafts until CI passes and CodeRabbit approves
3. **Bottom-Up Processing** - Address feedback from lowest PR in stack first
4. **Comprehensive Reviews** - Address all CodeRabbit comments including nitpicks
5. **Visual Stack** - Use PR titles and descriptions to show stack relationships

## Workflow Overview

| Phase | Description | Reference |
|-------|-------------|-----------|
| **Creation** | Create stacked PRs with proper titles and descriptions | `references/pr-formatting.md` |
| **Updates** | Merge changes through the stack (no rebasing) | `references/merge-workflow.md` |
| **Reviews** | Process CodeRabbit feedback in cycles | `references/review-cycles.md` |
| **Readiness** | Mark ready only after CI + CodeRabbit approval | `references/readiness-checklist.md` |

## Quick Reference

### PR Title Format

```text
🤖 [TYPE_EMOJI] [JIRA-KEY] type(scope): description (PR N/M)
```

### Stack Visualization (in PR Description)

```markdown
## 📚 PR Stack

| # | PR | Title | Status |
|---|-----|-------|--------|
| 1 | #78 | PNPM migration | ✅ Merged |
| 2 | **#79** | Shell to JS | 👈 This PR |
| 3 | #80 | Skills content | ⏳ Depends on #79 |
| 4 | #81 | Validation & CI | ⏳ Depends on #80 |
```

### Merge Flow (Not Rebase)

```bash
# After fixing issues in PR #79
git checkout feat/pr-80-branch
git merge feat/pr-79-branch --no-edit
git push origin feat/pr-80-branch
# Repeat for subsequent PRs in stack
```

## Scripts

| Script | Purpose |
|--------|---------|
| `scripts/check-stack-status.js` | Check CI and CodeRabbit status for all PRs in stack |

## References

| Reference | Content |
|-----------|---------|
| `references/pr-formatting.md` | PR title and description templates |
| `references/merge-workflow.md` | How to propagate changes through the stack |
| `references/review-cycles.md` | Processing CodeRabbit feedback iteratively |
| `references/readiness-checklist.md` | When to mark PRs ready for review |
| `references/automation-patterns.md` | Polling loops, callbacks, and autonomous operation |

## Key Requirement: CodeRabbit Approval

PRs should only be marked "Ready for Review" when:

1. ✅ All CI checks pass
2. ✅ All CodeRabbit comments addressed (including nitpicks)
3. ✅ CodeRabbit has approved (happens automatically after addressing feedback)
4. ✅ All previous PRs in the stack are merged

**Important**: Do NOT explicitly request CodeRabbit approval. It approves automatically after you've addressed all its comments and pushed fixes.

## Programmatic Automation

For autonomous AI agents, use polling loops to monitor status:

```javascript
// Poll until CI passes and CodeRabbit approves
async function waitForPRReady(prNumber, repo, maxAttempts = 30, intervalMs = 60000) {
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    const status = await checkPRStatus(prNumber, repo);
    
    if (status.ciPassed && status.coderabbitApproved && status.openComments === 0) {
      return { ready: true, status };
    }
    
    console.log(`Attempt ${attempt}/${maxAttempts}: CI=${status.ciPassed}, CR=${status.coderabbitApproved}, Comments=${status.openComments}`);
    
    if (attempt < maxAttempts) {
      await sleep(intervalMs);
    }
  }
  return { ready: false, reason: 'timeout' };
}
```

See `references/automation-patterns.md` for complete polling implementations.

## Skill Dependencies

This skill uses:

| Skill | Purpose |
|-------|---------|
| `coderabbit-interactions` | Thread replies, comment export, local CLI reviews |

Reference the dependent skill for detailed patterns rather than duplicating content.

## Related

- `.skills/coderabbit-interactions` - Detailed CodeRabbit interaction patterns
- `global/rules/github-cli-pr-lifecycle.md` - GitHub CLI commands for PR management
