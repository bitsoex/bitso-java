# CodeRabbit Workflow Examples

Complete examples of both local CLI and PR-based CodeRabbit workflows.

## Contents

- [Example 1: Local CLI Review Before Push](#example-1-local-cli-review-before-push)
- [Example 2: Processing PR Comments in Batches](#example-2-processing-pr-comments-in-batches)
- [Example 3: Mixed Resolution Types](#example-3-mixed-resolution-types)
- [Example 4: Cursor/AI Agent Workflow](#example-4-cursorai-agent-workflow)
- [Quick Command Reference](#quick-command-reference)

---
## Example 1: Local CLI Review Before Push

### Scenario

You've implemented a new feature and want CodeRabbit feedback before pushing to PR.

### Workflow

```bash
# 1. Check current state
git status
# Shows: 5 files changed, 200 lines added

# 2. Start CodeRabbit review (async - takes 7-30 min)
coderabbit --prompt-only --type uncommitted > .tmp/coderabbit-review.txt 2>&1 &
echo "Review started, PID: $!"

# 3. Continue other work while waiting...

# 4. Check if complete
jobs  # Shows: [1]+  Done

# 5. View results
cat .tmp/coderabbit-review.txt

# Output shows:
# 🔴 Critical: SQL injection in src/api/users.js:45
# 🟠 Major: Missing null check in src/utils/parser.js:23
# 🟡 Minor: Unused variable in src/models/user.js:12
```

### Fixing and Committing

```bash
# 6. Fix the issues in order of severity

# Fix critical
vim src/api/users.js  # Fix SQL injection
git add src/api/users.js

# Fix major
vim src/utils/parser.js  # Add null check
git add src/utils/parser.js

# Fix minor (optional)
vim src/models/user.js  # Remove unused variable
git add src/models/user.js

# 7. Commit with proper attribution
git commit -m "fix: address CodeRabbit CLI review findings" \
  -m "- [src/api/users.js:45]: Fixed SQL injection vulnerability" \
  -m "- [src/utils/parser.js:23]: Added null check before access" \
  -m "- [src/models/user.js:12]: Removed unused variable" \
  -m "Reviewed-by: CodeRabbit CLI (local)" \
  -m "Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"

# 8. Push to PR
git push
```

### What CodeRabbit Sees

When CodeRabbit reviews the PR, it sees:

- The co-author trailer → Knows it contributed
- "Reviewed-by: CodeRabbit CLI (local)" → Knows local review was done
- Clean code → The issues were already fixed

## Example 2: Processing PR Comments in Batches

### Scenario

Your PR has 15 CodeRabbit comments across multiple files.

### Workflow

```bash
# 1. Export comments to local file
node .claude/skills/coderabbit-workflow/scripts/export-comments.ts --pr 69

# Output:
# Total comments: 15
#   - Critical: 2
#   - Major: 5
#   - Minor: 8
# Output: .tmp/coderabbit-pr-69-20260112-160000.json (timestamp will vary)
```

### Processing Critical Issues First

```bash
# 2. View critical issues
jq -r '.comments[] | select(.severity == "critical") | "\(.path):\(.line) - \(.title)"' .tmp/coderabbit-pr-69-*.json

# Output:
# src/auth/login.js:45 - Race condition in session handling
# src/api/payments.js:128 - Missing input validation

# 3. Fix both critical issues
vim src/auth/login.js
vim src/api/payments.js

# 4. Update JSON with status
# (manually or via script)
# Set status: "fixed", resolution: "Added mutex lock"

# 5. Commit critical fixes
git add -A
git commit -m "fix: address 2 critical CodeRabbit issues

Threads:
- PRRT_kwDOxxx1: Fixed race condition with mutex lock
- PRRT_kwDOxxx2: Added input validation

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

### Processing Major Issues

```bash
# 6. View major issues
jq -r '.comments[] | select(.severity == "major") | "\(.path):\(.line) - \(.title)"' .tmp/coderabbit-pr-69-*.json

# 7. Fix major issues
# ... edit files ...

# 8. Commit major fixes
git commit -m "fix: address 5 major CodeRabbit issues

Threads:
- PRRT_kwDOyyy1: Improved error handling
- PRRT_kwDOyyy2: Fixed memory leak
- PRRT_kwDOyyy3: Added type checking
- PRRT_kwDOyyy4: Fixed edge case
- PRRT_kwDOyyy5: Updated deprecated API

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

### Push and Reply

```bash
# 9. Push all commits
git push

# 10. Reply to threads
node .claude/skills/coderabbit-workflow/scripts/reply-to-threads.ts --file .tmp/coderabbit-pr-69-*.json

# Output:
# ✅ src/auth/login.js:45
# ✅ src/api/payments.js:128
# ✅ src/utils/helper.js:23
# ... (all 15 threads replied)
```

## Example 3: Mixed Resolution Types

### Scenario

PR has 10 comments: 6 to fix, 2 to acknowledge (wontfix), 2 not-applicable.

### Workflow

```bash
# 1. Export and review
node .claude/skills/coderabbit-workflow/scripts/export-comments.ts --pr 42

# 2. Process and categorize (update JSON)
# - 6 comments: status: "fixed"
# - 2 comments: status: "wontfix", resolution: "Out of scope, tracking in JIRA-123"
# - 2 comments: status: "not-applicable", resolution: "Code was removed"

# 3. Fix the 6 issues
# ... edit files ...

# 4. Commit fixes
git commit -m "fix: address 6 CodeRabbit issues

Fixed:
- PRRT_1: Input validation
- PRRT_2: Null check
- PRRT_3: Error handling
- PRRT_4: Type safety
- PRRT_5: Async/await
- PRRT_6: Memory cleanup

Acknowledged (tracking in JIRA-123):
- PRRT_7: Refactor suggestion
- PRRT_8: Architecture improvement

Not applicable (code removed):
- PRRT_9: Deprecated function
- PRRT_10: Removed feature

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"

# 5. Push and reply
git push
node .claude/skills/coderabbit-workflow/scripts/reply-to-threads.ts --file .tmp/coderabbit-pr-42-*.json
```

## Example 4: Cursor/AI Agent Workflow

### Scenario

Using Cursor with CodeRabbit for autonomous development.

### Workflow

```text
User prompt to Cursor:
"Implement the payment webhook handler from the spec, then run coderabbit
--prompt-only --type uncommitted, review findings and fix any critical issues."
```

### What Happens

1. **Cursor implements feature** - Creates webhook handler code
2. **Cursor runs CodeRabbit** - Executes CLI in terminal
3. **Cursor waits** - 7-30 minutes for analysis
4. **Cursor reviews output** - Parses findings
5. **Cursor creates task list** - Prioritizes by severity
6. **Cursor fixes issues** - Implements suggested changes
7. **Cursor commits** - With proper co-author attribution

### Expected Commit

```text
fix: address CodeRabbit CLI review findings

- [src/webhooks/payment.js:45]: Added signature verification
- [src/webhooks/payment.js:78]: Fixed race condition
- [src/webhooks/payment.js:112]: Added idempotency check

Reviewed-by: CodeRabbit CLI (local)
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

## Quick Command Reference

| Task | Command |
|------|---------|
| Run local review | `coderabbit --prompt-only --type uncommitted` |
| Export PR comments | `node .claude/skills/coderabbit-workflow/scripts/export-comments.ts --pr N` |
| View pending issues | `jq '.comments[] \| select(.status == "pending")' .tmp/coderabbit-*.json` |
| View by severity | `jq '.comments[] \| select(.severity == "critical")' .tmp/coderabbit-*.json` |
| Reply to threads | `node .claude/skills/coderabbit-workflow/scripts/reply-to-threads.ts --file .tmp/coderabbit-*.json` |
| Dry run replies | `node .claude/skills/coderabbit-workflow/scripts/reply-to-threads.ts --file .tmp/coderabbit-*.json --dry-run` |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/references/workflow-examples.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

