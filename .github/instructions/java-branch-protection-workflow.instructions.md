---
applyTo: "**/*"
description: Never work directly on main. Always verify your branch and create a new one if needed before making changes.
---

# Always Work on Feature Branches

**Rule**: Never commit directly to `main`. All work must happen on a feature/fix/chore branch.

## Before Starting

Fetch all remotes to ensure latest:

```bash
git fetch --all
```

Check your current branch:

```bash
git branch --show-current
```

If on `main`, update and create a new branch:

```bash
git checkout main
git pull origin main
git checkout -b feat/your-feature-name
```

## Branch Naming: `{type}/{description}`

- `feat/` - New features
- `fix/` - Bug fixes
- `chore/` - Maintenance
- `docs/` - Documentation
- `refactor/` - Code restructuring

## AI Assistant Requirement

**AI must:**

- Verify branch before accepting changes
- Block if on `main` - refuse to proceed
- Suggest: `git fetch --all` before creating branch
- Ask to create new branch if needed
- Ensure main is up-to-date before branching

**Never push directly to `main`. Use pull requests.**

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/branch-protection-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
