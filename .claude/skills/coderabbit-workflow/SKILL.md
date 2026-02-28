---
name: coderabbit-workflow
description: Systematic workflow for CodeRabbit reviews - local CLI, PR threads, and commit attribution
compatibility: All repositories with CodeRabbit enabled

metadata:
  version: "2.0.0"
  category: code-quality
  tags:
    - code-review
    - coderabbit
    - quality
  triggers:
    - before-push
    - on-demand
---

# CodeRabbit Workflow

Address CodeRabbit review comments systematically. Workflows for local CLI usage, PR thread processing, and commit attribution.

## When to use this skill

- When addressing CodeRabbit review comments on a PR
- Before push, to get early feedback with local CLI
- When asked to "fix coderabbit issues" or "address coderabbit comments"

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill)
- [Two Review Modes](#two-review-modes)
- [Quick Start](#quick-start)
- [Scripts](#scripts)
- [References](#references)
- [Key Requirement: Co-Author](#key-requirement-co-author)
- [Waiting for CodeRabbit Review](#waiting-for-coderabbit-review)
- [Related](#related)

### Available Resources

**📚 references/** - Detailed documentation
- [cli integration](references/cli-integration.md)
- [commit formats](references/commit-formats.md)
- [setup](references/setup.md)
- [workflow examples](references/workflow-examples.md)

**🔧 scripts/** - Automation scripts
- [batch reply](scripts/batch-reply.ts)
- [export comments](scripts/export-comments.ts)
- [lib](scripts/lib)
- [monitor approval](scripts/monitor-approval.ts)
- [parse review body](scripts/parse-review-body.ts)
- [reply to threads](scripts/reply-to-threads.ts)
- [run local review](scripts/run-local-review.ts)

---

## Two Review Modes

| Mode | When | Reference |
|------|------|-----------|
| **Local CLI** | Before push, get early feedback | `references/cli-integration.md` |
| **PR Threads** | After CodeRabbit reviews your PR | `references/workflow-examples.md` |

## Quick Start

1. Export comments: `node .claude/skills/coderabbit-workflow/scripts/export-comments.ts --pr <number>`
2. Review by severity: critical, major, minor
3. Apply fixes following patterns in `references/workflow-examples.md`
4. Commit with CodeRabbit co-author attribution (see below)
5. Update exported JSON to mark comments as fixed (set `status: 'fixed'`)
6. Push and reply to threads: `node .claude/skills/coderabbit-workflow/scripts/reply-to-threads.ts --file .tmp/coderabbit-*.json`

**Note:** The reply script only processes comments with `status !== 'pending'`. After applying fixes, update the JSON file to change status from `'pending'` to `'fixed'` before running the reply script.

## Scripts

| Script | Purpose |
|--------|---------|
| `scripts/run-local-review.ts` | Run CodeRabbit CLI and save findings |
| `scripts/export-comments.ts` | Export PR comments to local JSON |
| `scripts/reply-to-threads.ts` | Batch reply to threads after fixes |
| `scripts/monitor-approval.ts` | Poll for approval, request review/approval automatically |

## References

| Reference | Content |
|-----------|---------|
| `references/setup.md` | Installation, authentication, troubleshooting |
| `references/cli-integration.md` | CLI commands and async workflow |
| `references/commit-formats.md` | All commit message templates |
| `references/workflow-examples.md` | Complete workflow examples |

## Key Requirement: Co-Author

All CodeRabbit fix commits must include:

```text
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

See `references/commit-formats.md` for all templates.

## Waiting for CodeRabbit Review

Use the monitor script to automatically wait for and request CodeRabbit approval:

```bash
# Monitor PR and automatically request review/approval when appropriate
node scripts/monitor-approval.ts --pr <number>

# With custom poll interval (default: 60s)
node scripts/monitor-approval.ts --pr <number> --interval 30

# Dry-run mode (show what would be done)
node scripts/monitor-approval.ts --pr <number> --dry-run
```

The monitor script:
- Polls every 60 seconds (configurable via `--interval`)
- Checks for open CodeRabbit comments that need addressing
- Waits 15 minutes before requesting a review if none received
- Waits 30 minutes before requesting approval if not approved
- Exits with code 0 on approval, code 2 if comments need addressing

**Timing requirements (automated by monitor script):**

| Time Since Last Commit | Action |
|------------------------|--------|
| 0-15 minutes | Wait for CodeRabbit to review automatically |
| 15+ minutes (no review) | Request review: `@coderabbitai review` |
| 30+ minutes (no approval) | Request approval: `@coderabbitai approve` |

**Manual commands (if not using monitor script):**

```bash
# Check time of last commit
git log -1 --format='%ci'

# Request review (after 15 min with no activity)
gh pr comment <PR_NUMBER> --body "@coderabbitai review"

# Request approval (after 30 min with no approval)
gh pr comment <PR_NUMBER> --body "@coderabbitai approve"
```

## Related

- [pr-workflow](.claude/skills/pr-workflow/SKILL.md) - PR lifecycle including CodeRabbit integration
- [CodeRabbit CLI Docs](https://docs.coderabbit.ai/cli/overview)
- [Cursor Integration](https://docs.coderabbit.ai/cli/cursor-integration)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

