<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/references/review-cycles.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

# Review Cycles for Stacked PRs

## Multi-Cycle Review Process

CodeRabbit reviews are iterative. After each push, CodeRabbit may find new issues or acknowledge fixes. Plan for multiple review cycles.

## Cycle Structure

```text
Cycle 1: Initial CodeRabbit review
    ↓
Address feedback, commit, push
    ↓
Cycle 2: CodeRabbit re-reviews
    ↓
Address new feedback (if any)
    ↓
Cycle N: CodeRabbit approves
```

## Processing Order: Bottom-Up

Always process the stack from bottom to top:

```text
1. Check PR #79 (lowest open PR)
   - Address all CodeRabbit comments
   - Push fixes
   - Merge to #80
   
2. Check PR #80
   - Address CodeRabbit comments (may have new ones from merged code)
   - Push fixes
   - Merge to #81
   
3. Check PR #81
   - Address CodeRabbit comments
   - Push fixes
```

## Addressing ALL Comments

### Including Nitpicks

CodeRabbit categorizes issues by severity:

| Severity | Icon | Action |
|----------|------|--------|
| Critical | 🔴 | **Must fix** |
| Major | 🟠 | **Must fix** |
| Minor | 🟡 | **Should fix** |
| Nitpick | ⚪ | **Fix anyway** |

**Important**: In multi-cycle reviews, fix nitpicks too. They:

- Improve code quality
- May become blocking in future reviews
- Show thoroughness to reviewers
- Prevent accumulation of technical debt

### Nitpick Examples Worth Fixing

```text
⚪ Nitpick: "Consider adding parentheses for clarity"
   → Fix it: Improves readability

⚪ Nitpick: "Capitalize 'Markdown' as proper noun"
   → Fix it: Consistent documentation

⚪ Nitpick: "Trailing whitespace on line 42"
   → Fix it: Clean code
```

## Reply Strategy

After fixing issues, reply to each CodeRabbit thread:

### Fixed Issues

```text
🤖 Fixed in commit abc1234. [Brief description]. Thanks!
```

### Not Applicable

```text
🤖 This doesn't apply here because [reason]. 
The current approach is correct because [explanation].
```

### Acknowledged for Later

```text
🤖 Good point! Out of scope for this PR. 
Created [JIRA-KEY] to track this.
```

## Batch Reply Script

Use the coderabbit-interactions skill to batch reply:

```bash
# Get all unresolved threads
gh api graphql -f query='...' | jq '...'

# Reply to each with commit SHA
COMMIT_SHA=$(git rev-parse --short HEAD)
gh api graphql -f query='mutation...' -f body="🤖 Fixed in $COMMIT_SHA..."
```

See `.skills/coderabbit-interactions/scripts/reply-to-threads.js` for automation.

## Waiting for CodeRabbit

After pushing fixes, CodeRabbit will re-review. Status indicators:

| Status | Meaning |
|--------|---------|
| `pending` / `Review in progress` | CodeRabbit is analyzing |
| `pass` / `Review completed` | CodeRabbit finished |

Check status:

```bash
gh pr checks <PR-NUMBER> --repo owner/repo | grep -i coderabbit
```

## CodeRabbit Approval

CodeRabbit approves automatically when:

1. All issues are addressed
2. No new issues found in latest push
3. Code meets quality standards

**Do NOT explicitly request approval.** It happens automatically.

### Checking Approval Status

```bash
gh pr view <PR-NUMBER> --repo owner/repo --json reviews | \
  jq '.reviews[] | select(.author.login == "coderabbitai") | {state: .state}'
```

States:

- `COMMENTED` - Has feedback, not approved
- `APPROVED` - Ready for human review
- `CHANGES_REQUESTED` - Blocking issues remain

## Example Review Cycle

```text
Day 1:
  09:00 - Push PR #79
  09:15 - CodeRabbit reviews (finds 5 issues)
  09:30 - Fix 5 issues, push
  09:45 - CodeRabbit re-reviews (finds 2 more issues)
  10:00 - Fix 2 issues, push
  10:15 - CodeRabbit approves ✓

  10:20 - Merge fixes to PR #80
  10:30 - CodeRabbit reviews PR #80 (finds 3 issues)
  10:45 - Fix 3 issues, push
  11:00 - CodeRabbit approves ✓

  11:05 - Merge fixes to PR #81
  11:15 - CodeRabbit reviews PR #81 (no issues)
  11:20 - CodeRabbit approves ✓
```

## Parallel Work

While waiting for CodeRabbit:

- Work on other tasks
- Review other PRs
- Update documentation
- Don't block on CI/reviews

```text
1. Push fixes to PR #79
2. While waiting for CodeRabbit:
   - Start working on unrelated task
   - Or address comments on PR #80
3. Check back after 5-10 minutes
```
