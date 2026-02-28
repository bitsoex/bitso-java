# Troubleshooting

Common issues and solutions when working with GitHub CLI and PRs.

## Contents

- [Common Problems](#common-problems)
- [Terminal Buffering Issues](#terminal-buffering-issues)
- [Authentication Issues](#authentication-issues)
- [PR Creation Failures](#pr-creation-failures)
- [CodeRabbit Issues](#coderabbit-issues)
- [Parallel Work During CI/Review](#parallel-work-during-cireview)

---
## Common Problems

| Problem | Solution |
|---------|----------|
| Command hangs | Use echo wrapper: `echo "..." ; gh command ; echo "Done"` |
| "Could not resolve to a PullRequest" | Always specify `--repo owner/repo` |
| CodeRabbit taking too long (5-15 min) | Proceed if other CI checks pass; CodeRabbit continues after PR is ready |
| Auth issues | Run `gh auth status` then `gh auth login` if needed |
| PR not assigned | Use `gh pr edit --add-assignee $(gh api user --jq '.login')` |

## Terminal Buffering Issues

When running `gh` commands from AI agents, output may not return immediately.

**Commands prone to buffering:**
- `gh pr checks`
- `gh pr view`
- `gh api`
- `gh run list/view`

**Solution - Use echo wrapper:**

```bash
# Pattern 1: Echo wrapper (recommended - preserves formatting)
echo "Checking..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# Pattern 2: Pipe to cat (loses formatting but reliable)
gh pr checks 123 --repo owner/repo 2>&1 | cat
```

## Authentication Issues

### Check Current Status

```bash
gh auth status
```

### Re-authenticate

```bash
gh auth login
```

### Verify Token Scopes

Ensure your token has the required scopes:
- `repo` - Full control of private repositories
- `read:org` - Read organization membership
- `workflow` - Update GitHub Action workflows

## PR Creation Failures

### Missing Repository Context

Always specify the repository:

```bash
# Wrong - may fail if not in repo directory
gh pr create --title "..."

# Correct - explicit repository
gh pr create --title "..." --repo owner/repo
```

### Draft PR Not Created

Ensure you're on a feature branch:

```bash
# Check current branch
git branch --show-current

# If on main, create feature branch first
git checkout -b feat/my-feature
git push -u origin feat/my-feature
```

## CodeRabbit Issues

### Review Not Starting

1. Check if CodeRabbit is enabled for the repository
2. Verify the PR has changes to review
3. Check CodeRabbit status in GitHub checks

### Comments Not Appearing in Query

The GraphQL query filters by author. Ensure you're using the correct login:

```bash
# Should be "coderabbitai"
jq '... | select(.comments.nodes[0].author.login == "coderabbitai")'
```

## Parallel Work During CI/Review

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
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-workflow/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

