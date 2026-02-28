# Readiness Checklist for Stacked PRs

## Contents

- [The Golden Rule](#the-golden-rule)
- [Readiness Checklist](#readiness-checklist)
- [Checking Readiness](#checking-readiness)
- [Marking Ready for Review](#marking-ready-for-review)
- [DO NOT Request CodeRabbit Approval](#do-not-request-coderabbit-approval)
- [Stack Readiness Order](#stack-readiness-order)
- [Common Mistakes](#common-mistakes)
- [Verification Script](#verification-script)
- [After Marking Ready](#after-marking-ready)

---
## The Golden Rule

> **A PR should only be marked "Ready for Review" when ALL conditions are met.**

Do NOT mark a PR ready prematurely. Draft status protects against:

- Premature human reviews
- Wasted reviewer time
- Merge conflicts from parallel reviews
- Confusion about PR dependencies

## Readiness Checklist

### ✅ Previous PRs in Stack

- [ ] All previous PRs in the stack are **merged**
- [ ] This PR's base branch is up-to-date with main

### ✅ CI Status

- [ ] All CI checks pass (green)
- [ ] No pending CI jobs
- [ ] No flaky test failures

### ✅ CodeRabbit Status

- [ ] CodeRabbit review completed (not pending)
- [ ] All CodeRabbit comments addressed
- [ ] All nitpicks addressed (not just blocking issues)
- [ ] CodeRabbit has approved (automatically, not requested)

### ✅ Local Validation

- [ ] `pnpm run lint` passes
- [ ] `pnpm test` passes
- [ ] No uncommitted changes

## Checking Readiness

### Check CI Status

```bash
echo "Checking CI..." ; gh pr checks <PR-NUMBER> --repo owner/repo ; echo "Done"
```

All checks should show `pass`.

### Check CodeRabbit Approval

```bash
gh pr view <PR-NUMBER> --repo owner/repo --json reviews | \
  jq '.reviews[] | select(.author.login == "coderabbitai") | .state' | tail -1
```

Should show `"APPROVED"`.

### Check Open Comments

```bash
gh api graphql -f query='
query($owner: String!, $repo: String!, $pr: Int!) {
  repository(owner: $owner, name: $repo) {
    pullRequest(number: $pr) {
      reviewThreads(first: 100) {
        nodes {
          isResolved
          comments(first: 1) { nodes { author { login } } }
        }
      }
    }
  }
}' -f owner="owner" -f repo="repo" -F pr=<PR-NUMBER> | \
jq '[.data.repository.pullRequest.reviewThreads.nodes[] |
    select(.isResolved == false) |
    select(.comments.nodes[0].author.login == "coderabbitai")] | length'
```

Should show `0`.

## Marking Ready for Review

When all conditions are met:

```bash
gh pr ready <PR-NUMBER> --repo owner/repo
```

## DO NOT Request CodeRabbit Approval

**Important**: Never explicitly request approval from CodeRabbit.

### ❌ Wrong

```text
@coderabbitai please approve this PR
```

### ✅ Correct

Simply address all feedback. CodeRabbit approves automatically when satisfied.

## Stack Readiness Order

PRs must become ready in order:

```text
1. PR #79 becomes ready ← Only when #78 is merged
   ↓ merge
2. PR #80 becomes ready ← Only when #79 is merged
   ↓ merge
3. PR #81 becomes ready ← Only when #80 is merged
```

Never mark a higher PR ready before lower ones are merged.

## Common Mistakes

### ❌ Marking ready while CI is running

```bash
# WRONG - CI might fail
gh pr ready 80 --repo owner/repo  # CI still running!
```

Wait for CI to complete first.

### ❌ Marking ready with open CodeRabbit comments

```bash
# WRONG - Comments not addressed
gh pr ready 80 --repo owner/repo  # 5 unresolved comments!
```

Address all comments first.

### ❌ Marking ready before previous PR merged

```bash
# WRONG - PR #79 not merged yet
gh pr ready 80 --repo owner/repo  # #79 still open!
```

Wait for #79 to merge first.

## Verification Script

```bash
#!/bin/bash
PR_NUMBER=$1
REPO="owner/repo"

echo "=== Checking readiness for PR #$PR_NUMBER ==="

# Check CI
echo ""
echo "CI Status:"
gh pr checks $PR_NUMBER --repo $REPO 2>&1 | grep -E "pass|fail|pending"

# Check CodeRabbit approval
echo ""
echo "CodeRabbit Status:"
gh pr view $PR_NUMBER --repo $REPO --json reviews 2>&1 | \
  jq '.reviews[] | select(.author.login == "coderabbitai") | .state' | tail -1

# Check open comments
echo ""
echo "Open CodeRabbit Comments:"
gh api graphql -f query='...' | jq '...'

echo ""
echo "=== End of readiness check ==="
```

## After Marking Ready

Once marked ready:

1. **Add human reviewers** if required
2. **Monitor for feedback** from human reviewers
3. **Respond promptly** to any questions
4. **Merge when approved** by required reviewers
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/references/readiness-checklist.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

