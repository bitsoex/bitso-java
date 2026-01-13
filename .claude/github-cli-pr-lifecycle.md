# Github Cli Pr Lifecycle

**Applies to:** All files

# GitHub CLI PR Lifecycle

Best practices for AI agents using GitHub CLI (`gh`) to manage pull requests, CI checks, CodeRabbit reviews, and reviewer interactions.

## AI-Assisted Work Identification

All AI-assisted commits, PR titles, and PR bodies must include the 🤖 emoji for easy tracking:

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

**Format**: `🤖 [TYPE_EMOJI] type(scope): description` (note the space after 🤖)

**See `global/rules/jira-ticket-workflow.md` for complete Jira integration guidelines.**

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

### Get Current User (For Auto-Assignment)

**CRITICAL**: Always get the current user to assign PRs:

```bash
# Get current authenticated user
CURRENT_USER=$(gh api user --jq '.login')
echo "Current user: $CURRENT_USER"

# Alternative: from gh auth status
gh auth status 2>&1 | grep "account" | head -1 | awk '{print $NF}'
```

### PR Status & CI Checks

```bash
# Check CI status
echo "Checking CI..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# List workflow runs
echo "Runs..." ; gh run list --repo owner/repo --branch feature-branch --limit 5 ; echo "Done"
```

### PR Lifecycle with Auto-Assignment

**IMPORTANT**: Always assign the PR to the current user after creation:

```bash
# Get current user for assignment
CURRENT_USER=$(gh api user --jq '.login')

# Create draft PR with AI emoji
PR_URL=$(gh pr create --draft \
    --title "🤖 🛡️ [JIRA-KEY] fix(security): resolve critical vulnerabilities" \
    --body "## 🤖 AI-Assisted Changes

Jira: [JIRA-KEY](https://bitsomx.atlassian.net/browse/JIRA-KEY)

## Summary
[Description]

## Severity Level
CRITICAL

## Validation
- [ ] Build passes locally
- [ ] Tests pass locally" \
    --repo owner/repo 2>&1)

# Extract PR number and assign to current user
PR_NUMBER=$(echo "$PR_URL" | grep -oE '[0-9]+$')
gh pr edit $PR_NUMBER --repo owner/repo --add-assignee "$CURRENT_USER"

echo "Created PR #$PR_NUMBER and assigned to $CURRENT_USER"
```

**One-liner for creating and assigning:**

```bash
CURRENT_USER=$(gh api user --jq '.login') && \
gh pr create --draft --title "🤖 [JIRA-KEY] feat: description" --body "..." --repo owner/repo 2>&1 | \
tee /dev/stderr | grep -oE '[0-9]+$' | xargs -I {} gh pr edit {} --repo owner/repo --add-assignee "$CURRENT_USER"
```

### Mark Ready and Add Reviewers

```bash
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

### Get All CodeRabbit Comments (Unresolved)

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

### Responding to CodeRabbit Comments

#### Strategy 1: Fix and Reply (Recommended)

When the comment is valid and you fix the issue:

```bash
# 1. Make the fix in code
# 2. Commit with reference to the issue
git commit -m "🤖 fix: address CodeRabbit feedback

- Fixed emoji spacing inconsistency (line 27)
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
}' -f threadId="$THREAD_ID" -f body="🤖 Fixed in commit $COMMIT_SHA. Standardized emoji spacing format across all examples. Thanks for catching this!" 2>&1 | cat
```

#### Strategy 2: Explain Why Not Applicable

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
}' -f threadId="$THREAD_ID" -f body="🤖 This suggestion doesn't apply here because [specific reason]. The current implementation is correct because [explanation]." 2>&1 | cat
```

#### Strategy 3: Acknowledge for Future

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
}' -f threadId="$THREAD_ID" -f body="🤖 Good point! This is out of scope for this PR but I've noted it for a follow-up. Created [JIRA-KEY] to track this." 2>&1 | cat
```

### Reply Format Best Practices

Always include in CodeRabbit replies:

1. **🤖 emoji** - Indicates AI response
2. **Commit reference** - If fixed: `Fixed in commit abc1234`
3. **Brief explanation** - What was changed or why not changed
4. **Gratitude** - `Thanks for catching this!` or similar

**Reply templates:**

```text
# Fixed
🤖 Fixed in commit abc1234. [Brief description of change]. Thanks!

# Not applicable
🤖 This doesn't apply here because [reason]. The current approach is [explanation].

# Out of scope
🤖 Valid point! Out of scope for this PR. Created [JIRA-KEY] to track this.

# Clarification needed
🤖 Could you clarify what you mean by [specific part]? The current implementation [explanation].
```

### Resolve Threads After Addressing

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

### Complete CodeRabbit Workflow

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
git commit -m "🤖 fix: address CodeRabbit review feedback

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

## Commit Message Format for AI Agents

All AI-generated commits must follow this format:

```text
🤖 [TYPE_EMOJI] type(scope): [JIRA-KEY] short description

- Detail 1
- Detail 2

[Additional context if needed]

Generated with the [Agent Name] by the /[command-name] command.
```

**Agent Attribution (REQUIRED):**

| Command | Attribution Line |
|---------|-----------------|
| `/fix-dependabot-vulnerabilities` | Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command. |
| `/fix-test-coverage` | Generated with the Quality Agent by the /fix-test-coverage command. |
| `/fix-sonarqube-issues` | Generated with the Security Agent by the /fix-sonarqube-issues command. |

**Examples:**

```bash
# Security fix (use actual Jira key, e.g., PROJ-52)
git commit -m "🤖 🛡️ fix(security): [PROJ-52] resolve critical CVE-2024-xxxxx

- Updated commons-compress to 1.27.1
- Added dependency substitution for transitive deps

Severity: CRITICAL

Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command."

# Quality fix
git commit -m "🤖 ✅ fix(quality): [PROJ-53] resolve BLOCKER SonarQube issues

- Fixed null pointer in PaymentService
- Added missing @Override annotations

Rules: java:S2259, java:S1161

Generated with the Security Agent by the /fix-sonarqube-issues command."

# Test coverage
git commit -m "🤖 🧪 test: [PROJ-54] improve coverage for PaymentService

- Added tests for edge cases
- Increased coverage from 65% to 82%

Generated with the Quality Agent by the /fix-test-coverage command."

# CodeRabbit feedback
git commit -m "🤖 fix: address CodeRabbit review feedback

- Fixed emoji spacing inconsistency
- Standardized format across examples"
```

## Troubleshooting

| Problem                                | Solution                                                                      |
| -------------------------------------- | ----------------------------------------------------------------------------- |
| Command hangs                          | Use echo wrapper: `echo "..." ; gh command ; echo "Done"`                     |
| "Could not resolve to a PullRequest"   | Always specify `--repo owner/repo`                                            |
| CodeRabbit taking too long (5-15 min)  | Proceed if other CI checks pass; CodeRabbit continues after PR is ready       |
| Auth issues                            | Run `gh auth status` then `gh auth login` if needed                           |
| PR not assigned                        | Use `gh pr edit --add-assignee $(gh api user --jq '.login')`                  |

## Best Practices Summary

1. **Always use 🤖 emoji** in commits, PR titles, and PR bodies for AI-assisted work
2. **Auto-assign PRs** to current user using `gh api user --jq '.login'`
3. **Include Jira key** in branch names, commits, and PR titles (e.g., `[PROJ-52]`)
4. **Always use echo wrapper** for `gh` commands to prevent buffering
5. **Always specify `--repo owner/repo`** to avoid directory context issues
6. **Use GraphQL API** for review threads, comments, and replies
7. **Reply to CodeRabbit comments** after fixing, referencing commit hash
8. **Re-request review** after addressing all comments
9. **Don't wait indefinitely** for CodeRabbit - proceed after critical checks pass
10. **Work on other tasks while CI runs** - After pushing changes, continue with other PRs or unfinished work instead of waiting for CodeRabbit reviews and CI checks

## Parallel Work During CI/Review (IMPORTANT)

When working on multiple PRs or orchestrating work across repositories:

1. **Push and move on** - After pushing changes to a PR, immediately continue with other pending work
2. **Don't block on CI** - CodeRabbit reviews and CI checks can take 2-10 minutes; use this time productively
3. **Batch check status** - Periodically check all pending PRs together rather than waiting on each one
4. **Prioritize unblocked work** - If one PR is waiting for review, work on the next task

**Example workflow for multiple PRs:**

```text
1. Push PR #1 changes → Don't wait
2. Start working on PR #2 → Push changes → Don't wait
3. Check status of PR #1 → Address feedback if any
4. Check status of PR #2 → Address feedback if any
5. Repeat until all PRs are ready
```

This maximizes productivity and reduces total time to complete multiple tasks.

## Related

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - Complete Jira integration and ticket creation
- **CodeRabbit Setup**: `global/rules/coderabbit-setup.md` - CodeRabbit CLI installation and configuration
- **Fix CodeRabbit Issues**: `global/commands/fix-coderabbit-issues.md` - Command for fixing CodeRabbit findings
- **Branch Protection**: `global/rules/branch-protection-workflow.md` - Branch naming and protection rules

---
*This rule is part of the java category.*
*Source: global/rules/github-cli-pr-lifecycle.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/github-cli-pr-lifecycle.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
