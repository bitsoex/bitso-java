# Address CodeRabbit review comments systematically

> Address CodeRabbit review comments systematically

# Fix CodeRabbit Issues

This command uses the **`coderabbit-interactions` skill** to systematically address CodeRabbit review comments.

## Skill Location

```
global/skills/coderabbit-interactions/
```

> **Note**: Scripts reference `.agent-skills/` which is the distributed output folder generated from `global/skills/` by CI.

## Quick Workflow

### 1. Export Comments

```bash
node .agent-skills/coderabbit-interactions/scripts/export-comments.js --pr 123
```

### 2. Review and Fix by Severity

```bash
# View pending issues
jq -r '.comments[] | select(.status == "pending") | "\(.severity) | \(.path):\(.line)"' .tmp/coderabbit-pr-*.json
```

Fix critical → major → minor.

### 3. Commit with Co-Author

```bash
git commit -m "🤖 fix: address CodeRabbit review feedback" \
  -m "- [file.js]: Description of fix" \
  -m "Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

### 4. Push and Reply

```bash
git push
node .agent-skills/coderabbit-interactions/scripts/reply-to-threads.js --file .tmp/coderabbit-pr-*.json
```

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Complete skill documentation |
| `scripts/run-local-review.js` | Run CodeRabbit CLI locally |
| `scripts/export-comments.js` | Export PR comments to JSON |
| `scripts/reply-to-threads.js` | Batch reply to threads |
| `references/setup.md` | Installation and authentication |
| `references/commit-formats.md` | All commit message templates |
| `references/cli-integration.md` | CLI commands and async workflow |
| `references/workflow-examples.md` | Complete workflow examples |

## Commit Formats

### Local CLI Review

```text
🤖 fix: address CodeRabbit CLI review findings

- [path/file.js]: Description

Reviewed-by: CodeRabbit CLI (local)
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

### PR Thread Fix

```text
🤖 fix: address CodeRabbit PR feedback

Thread: PRRT_kwDOxxxxxx
- [path/file.js:42]: Description

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

See `references/commit-formats.md` in the skill for all templates.

## Related

- **Skill**: `global/skills/coderabbit-interactions/`
- **CLI Docs**: [CodeRabbit Cursor Integration](https://docs.coderabbit.ai/cli/cursor-integration)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/fix-coderabbit-issues.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
