# Merge Workflow for Stacked PRs

## Contents

- [Core Principle: Merge, Never Rebase](#core-principle-merge-never-rebase)
- [Propagating Changes Through the Stack](#propagating-changes-through-the-stack)
- [Merge Commit Messages](#merge-commit-messages)
- [Handling Merge Conflicts](#handling-merge-conflicts)
- [Order of Operations](#order-of-operations)
- [Verification After Merge](#verification-after-merge)
- [Common Mistakes to Avoid](#common-mistakes-to-avoid)
- [Diagram: Merge Flow](#diagram-merge-flow)

---
## Core Principle: Merge, Never Rebase

When working with stacked PRs, **always use merge commits** to propagate changes through the stack. Never rebase or force-push.

### Why Merge Instead of Rebase?

| Aspect | Merge | Rebase |
|--------|-------|--------|
| History | Preserves full history | Rewrites history |
| Force Push | Not required | Required |
| Collaboration | Safe for shared branches | Dangerous for shared branches |
| CodeRabbit | Incremental reviews | Forces full re-review |
| Conflict Resolution | Once per merge | Potentially multiple times |

## Propagating Changes Through the Stack

When you make fixes in a lower PR, propagate them upward:

```bash
# Example: Fixed issues in PR #79 (refactor/shell-to-js-scripts)
# Need to propagate to PR #80 and #81

# Step 1: Push fixes to PR #79
git checkout refactor/shell-to-js-scripts
git add .
git commit -m "fix: address CodeRabbit feedback

- Fixed issue 1
- Fixed issue 2

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
git push origin refactor/shell-to-js-scripts

# Step 2: Merge into PR #80
git checkout feat/skills-content-features
git merge refactor/shell-to-js-scripts --no-edit
pnpm run lint && pnpm test  # Verify tests pass
git push origin feat/skills-content-features

# Step 3: Merge into PR #81
git checkout feat/validation-and-ci-updates
git merge feat/skills-content-features --no-edit
pnpm run lint && pnpm test  # Verify tests pass
git push origin feat/validation-and-ci-updates
```

## Merge Commit Messages

Git will auto-generate merge commit messages. Use `--no-edit` to accept them:

```text
Merge branch 'refactor/shell-to-js-scripts' into feat/skills-content-features
```

This clearly shows the merge relationship in git history.

## Handling Merge Conflicts

If conflicts occur during merge:

1. **Resolve conflicts** in the affected files
2. **Run tests** to ensure nothing broke
3. **Commit the merge** with resolved conflicts
4. **Continue propagating** up the stack

```bash
git checkout feat/skills-content-features
git merge refactor/shell-to-js-scripts

# If conflicts:
# 1. Edit conflicted files
# 2. git add <resolved-files>
# 3. git commit  # Completes the merge

pnpm run lint && pnpm test
git push origin feat/skills-content-features
```

## Order of Operations

Always work from the **bottom of the stack upward**:

```text
1. Fix issues in PR #79 (bottom of open stack)
   ↓ merge
2. Update PR #80
   ↓ merge
3. Update PR #81 (top of stack)
```

Never try to:

- Fix issues in a higher PR that should be fixed lower
- Rebase a PR onto its updated base
- Force-push to any PR branch

## Verification After Merge

After each merge, verify the PR is still healthy:

```bash
# Run local validation
pnpm run lint
pnpm test

# Check that the PR diff looks correct
gh pr diff <PR-NUMBER> --repo owner/repo | head -50
```

## Common Mistakes to Avoid

### ❌ Don't: Rebase onto updated base

```bash
# WRONG - This rewrites history
git checkout feat/pr-80
git rebase refactor/pr-79
git push --force  # Dangerous!
```

### ✅ Do: Merge the updated base

```bash
# CORRECT - This preserves history
git checkout feat/pr-80
git merge refactor/pr-79 --no-edit
git push  # Safe push
```

### ❌ Don't: Cherry-pick fixes

```bash
# WRONG - Creates duplicate commits
git checkout feat/pr-80
git cherry-pick abc123
```

### ✅ Do: Merge the entire branch

```bash
# CORRECT - Brings all changes with proper history
git checkout feat/pr-80
git merge refactor/pr-79 --no-edit
```

## Diagram: Merge Flow

```text
main ─────────────────────────────────────────────►
       │
       └── PR #79 (base)
              │  ← fixes committed here
              │
              └── PR #80 (depends on #79)
                     │  ← merge from #79
                     │
                     └── PR #81 (depends on #80)
                            │  ← merge from #80
                            ▼
```

Each arrow represents a `git merge` operation, preserving history and allowing incremental CodeRabbit reviews.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/references/merge-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

