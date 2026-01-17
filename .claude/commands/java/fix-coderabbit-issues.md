# Address CodeRabbit review comments systematically

**Description:** Address CodeRabbit review comments systematically

# Fix CodeRabbit Issues

Address CodeRabbit review comments systematically.

## Skill Location

Full documentation: `.claude/skills/coderabbit-interactions/`

## Quick Start

1. Export comments: `node .agent-skills/coderabbit-interactions/scripts/export-comments.js --pr <number>`
2. Review by severity: critical → major → minor
3. Apply patterns from the skill:
   - `.claude/skills/coderabbit-interactions/SKILL.md` - Main instructions
   - `.claude/skills/coderabbit-interactions/references/workflow-examples.md` - Workflows
   - `.claude/skills/coderabbit-interactions/references/commit-formats.md` - Commit templates
4. Commit with CodeRabbit co-author attribution
5. Push and reply to threads

## Commit Template

```text
🤖 fix: address CodeRabbit review feedback

- [file.js:42]: Description of fix

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

## Available Scripts

| Script | Purpose |
|--------|---------|
| `export-comments.js` | Export PR comments to JSON |
| `reply-to-threads.js` | Batch reply to threads |
| `run-local-review.js` | Run CodeRabbit CLI locally |

## Related

- **CLI Docs**: [CodeRabbit Cursor Integration](https://docs.coderabbit.ai/cli/cursor-integration)
- **Skill**: `.claude/skills/coderabbit-interactions/SKILL.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/fix-coderabbit-issues.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
