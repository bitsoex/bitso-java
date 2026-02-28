# CodeRabbit Integration

Monitor CodeRabbit reviews and respond to comments in PRs.

## Contents

- [Check CodeRabbit Status](#check-coderabbit-status)
- [Get All Unresolved CodeRabbit Comments](#get-all-unresolved-coderabbit-comments)
- [Responding to CodeRabbit Comments](#responding-to-coderabbit-comments)
- [Reply Format Best Practices](#reply-format-best-practices)
- [Resolve Threads After Addressing](#resolve-threads-after-addressing)
- [Complete CodeRabbit Workflow](#complete-coderabbit-workflow)

---
## Check CodeRabbit Status

```bash
# Quick status check
gh pr checks 123 --repo owner/repo 2>&1 | grep -i "coderabbit"
# States: "pending/Review in progress" or "pass/Review completed"
```

## Get All Unresolved CodeRabbit Comments

Use this GraphQL query to get all unresolved CodeRabbit review threads:

```bash
OWNER="bitsoex"
REPO="ai-code-instructions"
PR_NUMBER=38

gh api graphql -f query='
query($owner: String!, $repo: String!, $pr: Int!) {
  repository(owner: $owner, name: $repo) {
    pullRequest(number: $pr) {
      reviewThreads(first: 50) {
        nodes {
          id
          isResolved
          path
          line
          comments(first: 10) {
            nodes {
              id
              author { login }
              body
              createdAt
            }
          }
        }
      }
    }
  }
}' -f owner="$OWNER" -f repo="$REPO" -F pr="$PR_NUMBER" 2>&1 | \
jq '.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved == false) | select(.comments.nodes[0].author.login == "coderabbitai")'
```

**Output includes:**

- `id` - Thread ID needed for replies (e.g., `PRRT_kwDOxxxxxx`)
- `path` - File path where comment was made
- `line` - Line number in the file
- `comments.nodes[0].body` - The CodeRabbit comment content

## Responding to CodeRabbit Comments

### Strategy 1: Fix and Reply (Recommended)

When the comment is valid and you fix the issue:

```bash
# 1. Make the fix in code
# 2. Commit with reference to the issue
git commit -m "fix: address CodeRabbit feedback

- Fixed spacing inconsistency (line 27)
- Standardized format across all examples"

# 3. Reply to the thread acknowledging the fix
THREAD_ID="PRRT_kwDOxxxxxx"  # From the query above
COMMIT_SHA=$(git rev-parse --short HEAD)

gh api graphql -f query='
mutation($threadId: ID!, $body: String!) {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: $threadId,
    body: $body
  }) {
    comment { id url }
  }
}' -f threadId="$THREAD_ID" -f body="Fixed in commit $COMMIT_SHA. Standardized format across all examples. Thanks for catching this!" 2>&1 | cat
```

### Strategy 2: Explain Why Not Applicable

When the comment is not applicable or incorrect:

```bash
THREAD_ID="PRRT_kwDOxxxxxx"

gh api graphql -f query='
mutation($threadId: ID!, $body: String!) {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: $threadId,
    body: $body
  }) {
    comment { id url }
  }
}' -f threadId="$THREAD_ID" -f body="This suggestion doesn't apply here because [specific reason]. The current implementation is correct because [explanation]." 2>&1 | cat
```

### Strategy 3: Acknowledge for Future

When valid but out of scope:

```bash
THREAD_ID="PRRT_kwDOxxxxxx"

gh api graphql -f query='
mutation($threadId: ID!, $body: String!) {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: $threadId,
    body: $body
  }) {
    comment { id url }
  }
}' -f threadId="$THREAD_ID" -f body="Good point! This is out of scope for this PR but I've noted it for a follow-up. Created [JIRA-KEY] to track this." 2>&1 | cat
```

## Reply Format Best Practices

Always include in CodeRabbit replies:

1. **Commit reference** - If fixed: `Fixed in commit abc1234`
2. **Brief explanation** - What was changed or why not changed
3. **Gratitude** - `Thanks for catching this!` or similar

**Reply templates:**

```text
# Fixed
Fixed in commit abc1234. [Brief description of change]. Thanks!

# Not applicable
This doesn't apply here because [reason]. The current approach is [explanation].

# Out of scope
Valid point! Out of scope for this PR. Created [JIRA-KEY] to track this.

# Clarification needed
Could you clarify what you mean by [specific part]? The current implementation [explanation].
```

## Resolve Threads After Addressing

After replying, you can resolve the thread:

```bash
THREAD_ID="PRRT_kwDOxxxxxx"

gh api graphql -f query='
mutation($threadId: ID!) {
  resolveReviewThread(input: {
    threadId: $threadId
  }) {
    thread { id isResolved }
  }
}' -f threadId="$THREAD_ID" 2>&1 | cat
```

**Note**: Only resolve if you've genuinely addressed the concern. Let reviewers resolve if unsure.

## Complete CodeRabbit Workflow

```bash
# 1. Get all unresolved CodeRabbit comments
OWNER="owner"
REPO="repo"
PR_NUMBER=123

COMMENTS=$(gh api graphql -f query='...' 2>&1 | jq '...')

# 2. For each comment:
#    a. Read the file and understand the issue
#    b. Make the fix (if valid)
#    c. Commit with descriptive message

git add -A
git commit -m "fix: address CodeRabbit review feedback

- Issue 1: [description]
- Issue 2: [description]"

# 3. Reply to each thread
COMMIT_SHA=$(git rev-parse --short HEAD)
# ... reply to each thread with the commit reference

# 4. Push changes
git push

# 5. Check if CodeRabbit re-reviews
echo "Waiting for CodeRabbit..." ; sleep 60
gh pr checks $PR_NUMBER --repo $OWNER/$REPO 2>&1 | grep -i "coderabbit"
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-workflow/references/coderabbit-integration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

