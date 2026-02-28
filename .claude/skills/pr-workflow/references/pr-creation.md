# PR Creation

Create and manage pull requests with proper formatting and auto-assignment.

## Contents

- [Get Current User for Auto-Assignment](#get-current-user-for-auto-assignment)
- [Create Draft PR with Auto-Assignment](#create-draft-pr-with-auto-assignment)
- [One-Liner for Create and Assign](#one-liner-for-create-and-assign)
- [PR Title Format](#pr-title-format)
- [PR Body Template](#pr-body-template)
- [Mark Ready and Add Reviewers](#mark-ready-and-add-reviewers)
- [PR Status & CI Checks](#pr-status--ci-checks)

---
> **IMPORTANT**: Always create PRs as drafts using the `--draft` flag. Mark ready for review only after CI passes and CodeRabbit provides initial feedback.

## Get Current User for Auto-Assignment

**CRITICAL**: Always get the current user to assign PRs:

```bash
# Get current authenticated user
CURRENT_USER=$(gh api user --jq '.login')
echo "Current user: $CURRENT_USER"

# Alternative: from gh auth status
gh auth status 2>&1 | grep "account" | head -1 | awk '{print $NF}'
```

## Create Draft PR with Auto-Assignment

```bash
# Get current user for assignment
CURRENT_USER=$(gh api user --jq '.login')

# Create draft PR
PR_URL=$(gh pr create --draft \
    --title "[JIRA-KEY] fix(security): resolve critical vulnerabilities" \
    --body "## Summary

Jira: [JIRA-KEY](https://bitsomx.atlassian.net/browse/JIRA-KEY)

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

## One-Liner for Create and Assign

```bash
CURRENT_USER=$(gh api user --jq '.login') && \
gh pr create --draft --title "[JIRA-KEY] feat: description" --body "..." --repo owner/repo 2>&1 | \
tee /dev/stderr | grep -oE '[0-9]+$' | xargs -I {} gh pr edit {} --repo owner/repo --add-assignee "$CURRENT_USER"
```

## PR Title Format

```text
[JIRA-KEY] type(scope): description
```

Examples:
- `[PROJ-123] fix(security): resolve critical CVE-2024-xxxxx`
- `[PROJ-456] fix(quality): resolve BLOCKER SonarQube issues`
- `[PROJ-789] test: improve coverage for PaymentService`

## PR Body Template

```markdown
## Summary

Jira: [PROJ-123](https://bitsomx.atlassian.net/browse/PROJ-123)

[Brief description of changes]

## Changes
- [ ] Change 1
- [ ] Change 2

## Severity Level
[CRITICAL | HIGH | MEDIUM | LOW]

## Validation
- [ ] Build passes locally
- [ ] Tests pass locally
- [ ] [Specific validation for this type of work]

## Agent Details (if applicable)
- **Command**: /fix-dependabot-vulnerabilities
- **Iteration**: 1 of N
```

## Mark Ready and Add Reviewers

```bash
# Mark ready for review
echo "Ready..." ; gh pr ready 123 --repo owner/repo ; echo "Done"

# Add reviewers
echo "Reviewers..." ; gh pr edit 123 --repo owner/repo --add-reviewer user1,user2 ; echo "Done"
```

## PR Status & CI Checks

```bash
# Check CI status
echo "Checking CI..." ; gh pr checks 123 --repo owner/repo ; echo "Done"

# List workflow runs
echo "Runs..." ; gh run list --repo owner/repo --branch feature-branch --limit 5 ; echo "Done"
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-workflow/references/pr-creation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

