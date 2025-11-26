---
applyTo: "**/*"
description: Github Cli Pr Lifecycle
---

# GitHub CLI PR Lifecycle

Best practices for AI agents using GitHub CLI (`gh`) to manage pull requests, CI checks, CodeRabbit reviews, and reviewer interactions.

## Critical: Preventing Terminal Buffering

When running `gh` commands from AI agents, output may not return immediately. **Always use one of these patterns:**

```bash
# Pattern 1: Echo wrapper (recommended - preserves formatting)
echo "Checking..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# Pattern 2: Pipe to cat (loses formatting but reliable)
gh pr checks 123 --repo owner/repo 2>&1 | cat
```

**Commands prone to buffering:** `gh pr checks`, `gh pr view`, `gh api`, `gh run list/view`

## Essential Commands

### PR Status & CI Checks

```bash
# Check CI status
echo "Checking CI..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# List workflow runs
echo "Runs..." ; gh run list --repo owner/repo --branch feature-branch --limit 5 ; echo "Done"
```

### PR Lifecycle

```bash
# Create draft PR
gh pr create --draft --title "feat: Feature" --body "Description" --repo owner/repo 2>&1 | cat

# Mark ready for review
echo "Ready..." ; gh pr ready 123 --repo owner/repo ; echo "Done"

# Add reviewers
echo "Reviewers..." ; gh pr edit 123 --repo owner/repo --add-reviewer user1,user2 ; echo "Done"
```

## CodeRabbit Integration

### Check CodeRabbit Status

```bash
# Quick status check
gh pr checks 123 --repo owner/repo 2>&1 | grep -i "coderabbit"
# States: "pending/Review in progress" or "pass/Review completed"
```

### Get Unresolved Review Threads (CodeRabbit & Human Reviews)

```bash
gh api graphql -f query='
query {
  repository(owner: "OWNER", name: "REPO") {
    pullRequest(number: PR_NUMBER) {
      reviewThreads(first: 30) {
        nodes {
          id
          isResolved
          comments(first: 5) {
            nodes {
              id
              author { login }
              body
              path
              line
            }
          }
        }
      }
    }
  }
}' 2>&1 | jq '.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved == false)'
```

### Reply to Review Comments (In Thread)

After addressing review comments in code, reply to acknowledge the fix:

> **⚠️ IMPORTANT:** Always reply within the review thread, not as a standalone PR comment. Use `addPullRequestReviewThreadReply` GraphQL mutation (✅ replies in thread). Do NOT use `gh pr comment` or `gh issue comment` (❌ creates standalone comments).

```bash
gh api graphql -f query='
mutation {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: "PRRT_kwDOxxxxxx",
    body: "Fixed in commit abc1234. Updated X to Y as suggested. Thanks! 👍"
  }) {
    comment { id }
  }
}' 2>&1 | cat
```

**Best practices for replies:**

- Reference the commit hash where the fix was made
- Briefly describe what was changed
- Thank the reviewer

### Re-request Review After Fixes

```bash
echo "Re-requesting..." ; gh pr edit 123 --repo owner/repo --add-reviewer username ; echo "Done"
```

## Complete PR Review Workflow

```bash
# 1. Get unresolved comments
gh api graphql -f query='...' 2>&1 | jq '...'

# 2. Address each comment in code, commit fixes

# 3. Reply to each comment thread
gh api graphql -f query='mutation { addPullRequestReviewThreadReply(...) }' 2>&1 | cat

# 4. Check CI status
echo "CI..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# 5. Mark ready & re-request review
echo "Ready..." ; gh pr ready 123 --repo owner/repo ; echo "Done"
echo "Review..." ; gh pr edit 123 --repo owner/repo --add-reviewer reviewer1 ; echo "Done"
```

## Troubleshooting

| Problem                                | Solution                                                                      |
| -------------------------------------- | ----------------------------------------------------------------------------- |
| Command hangs                          | Use echo wrapper: `echo "..." ; gh command ; echo "Done"`                     |
| "Could not resolve to a PullRequest"   | Always specify `--repo owner/repo`                                            |
| CodeRabbit taking too long (5-15 min)  | Proceed if other CI checks pass; CodeRabbit continues after PR is ready       |
| Auth issues                            | Run `gh auth status` then `gh auth login` if needed                           |

## Best Practices Summary

1. **Always use echo wrapper** for `gh` commands to prevent buffering
2. **Always specify `--repo owner/repo`** to avoid directory context issues
3. **Use GraphQL API** for review threads, comments, and replies
4. **Reply to comments** after fixing, referencing commit hash
5. **Re-request review** after addressing all comments
6. **Don't wait indefinitely** for CodeRabbit - proceed after critical checks pass
