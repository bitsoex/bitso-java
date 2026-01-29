# Branch Protection

Never commit directly to `main`. All work must happen on a feature/fix/chore branch.

## Before Starting Any Work

### 1. Fetch Latest

```bash
git fetch --all
```

### 2. Check Current Branch

```bash
git branch --show-current
```

### 3. If on Main, Create Feature Branch

```bash
git checkout main
git pull origin main
git checkout -b feat/your-feature-name
```

## Branch Naming Convention

Format: `{type}/{JIRA-KEY}-{description}` or `{type}/{description}`

| Type | Purpose | Example |
|------|---------|---------|
| `feat/` | New features | `feat/PROJ-123-user-auth` |
| `fix/` | Bug fixes | `fix/PROJ-456-null-pointer` |
| `chore/` | Maintenance | `chore/PROJ-789-update-deps` |
| `docs/` | Documentation | `docs/update-readme` |
| `refactor/` | Code restructuring | `refactor/simplify-validation` |
| `test/` | Test improvements | `test/PROJ-101-coverage` |

## AI Assistant Requirements

When working with AI assistants, they must:

1. **Verify branch** before accepting changes
2. **Block if on `main`** - refuse to proceed with changes
3. **Suggest `git fetch --all`** before creating new branch
4. **Ask to create new branch** if currently on main
5. **Ensure main is up-to-date** before branching

## Never Push Directly to Main

All changes must go through pull requests:

```bash
# Create PR after pushing feature branch
git push -u origin $(git branch --show-current)
gh pr create --draft --title "feat: description" --body "..."
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/pr-workflow/references/branch-protection.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

