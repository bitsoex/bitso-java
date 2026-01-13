<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/git-hooks/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: git-hooks
description: >
  Set up, validate, and maintain Git hooks (pre-commit, pre-push, etc.) following 
  best practices. Ensures hooks are version-controlled, automatically installed, 
  and follow consistent patterns across the team. Use when creating new hooks,
  debugging hook issues, or ensuring hook compliance.
compatibility: Requires Node.js 24+; works with any Git repository
metadata:
  version: "1.0"
  targeting:
    include:
      - repo: "bitsoex/ai-code-instructions"
---

# Git Hooks

This skill provides guidance for implementing and maintaining Git hooks that enforce code quality standards before commits and pushes reach the repository.

## When to use this skill

- Setting up Git hooks in a new repository
- Creating new pre-commit or pre-push hooks
- Debugging hook installation or execution issues
- Ensuring hooks follow team standards
- Migrating from manual hooks to version-controlled hooks

## Architecture

### Recommended Directory Structure

```
project/
├── .git-hooks/              # Version-controlled hooks directory
│   ├── pre-commit           # Symlink → ../.scripts/pre-commit-hook.sh
│   └── pre-push             # Symlink → ../.scripts/pre-push-hook.sh
├── .scripts/
│   ├── setup-hooks.js       # Hook installation script (runs on npm install)
│   ├── pre-commit-hook.sh   # Pre-commit hook implementation
│   ├── pre-push-hook.sh     # Pre-push hook implementation
│   └── lib/skills/          # Skill modules for hook operations
└── package.json             # Contains "prepare": "node .scripts/setup-hooks.js"
```

### Why This Architecture?

1. **Version-controlled**: Hooks live in `.git-hooks/`, tracked by Git
2. **Automatic installation**: `npm install` configures hooks via `prepare` script
3. **Team consistency**: Everyone gets the same hooks automatically
4. **Implementation separation**: Actual logic in `.scripts/`, symlinks in `.git-hooks/`
5. **Skippable in CI**: Setup script detects CI environment and skips

## Instructions

### Step 1: Create the Hooks Directory

```bash
mkdir -p .git-hooks
```

### Step 2: Create the Setup Script

Create `.scripts/setup-hooks.js`:

```javascript
#!/usr/bin/env node
/**
 * Setup Git Hooks
 * 
 * Runs on `npm install` via the "prepare" script.
 * Configures git to use .git-hooks/ for hooks.
 */

if (process.env.CI || process.env.SKIP_HOOKS) {
  console.log('⏭️  Skipping hook setup (CI or SKIP_HOOKS=true)');
  process.exit(0);
}

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const ROOT_DIR = path.join(__dirname, '..');
const HOOKS_DIR = '.git-hooks';

function setupHooks() {
  if (!fs.existsSync(path.join(ROOT_DIR, '.git'))) {
    console.log('⚠️  Not a git repository, skipping hook setup');
    return;
  }

  const githooksPath = path.join(ROOT_DIR, HOOKS_DIR);
  if (!fs.existsSync(githooksPath)) {
    console.error(`❌ Hooks directory not found: ${HOOKS_DIR}`);
    process.exit(1);
  }

  // Set core.hooksPath
  try {
    execSync(`git config core.hooksPath ${HOOKS_DIR}`, { cwd: ROOT_DIR });
    console.log(`✅ Git hooks configured: core.hooksPath → ${HOOKS_DIR}`);
  } catch (error) {
    console.error('❌ Failed to set core.hooksPath:', error.message);
    process.exit(1);
  }
}

setupHooks();
```

### Step 3: Configure package.json

Add the prepare script to automatically set up hooks on install:

```json
{
  "scripts": {
    "prepare": "node .scripts/setup-hooks.js"
  }
}
```

### Step 4: Create Hook Implementation

Create hook scripts in `.scripts/` following the template in the References section.

### Step 5: Create Symlinks

Create symlinks in `.git-hooks/` pointing to the implementation:

```bash
cd .git-hooks
ln -sf ../.scripts/pre-commit-hook.sh pre-commit
ln -sf ../.scripts/pre-push-hook.sh pre-push
chmod +x pre-commit pre-push
```

### Step 6: Validate Hook Setup

```bash
# Run validation
npm run skills:hooks

# Or use CLI directly
node .scripts/skills-cli.js git-hooks validate
```

## Hook Types

| Hook | When It Runs | Typical Checks |
|------|-------------|----------------|
| `pre-commit` | Before commit is created | Linting, formatting, tests, validation |
| `pre-push` | Before push to remote | Full test suite, coverage, build verification |
| `commit-msg` | After commit message written | Message format validation |
| `prepare-commit-msg` | Before editor opens | Template insertion |
| `post-checkout` | After checkout completes | Dependency updates, cache clearing |
| `post-merge` | After merge completes | Dependency updates |

## Best Practices

### 1. Exit Codes Matter

```bash
# Exit 0 = success, commit/push proceeds
# Exit non-zero = failure, operation aborted
if ! npm test; then
  echo "Tests failed"
  exit 1
fi
```

### 2. Provide Clear Feedback

```bash
# Use colors and emojis for visibility
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔍 Running tests...${NC}"
if npm test --silent; then
  echo -e "${GREEN}   ✓ Tests passed${NC}"
else
  echo -e "${RED}   ✗ Tests failed${NC}"
  exit 1
fi
```

### 3. Keep Hooks Fast

Pre-commit hooks should complete in seconds, not minutes:
- Run only essential checks
- Use incremental/cached operations where possible
- Move heavy checks to pre-push

### 4. Allow Emergency Bypass

```bash
# Document how to skip in emergencies
git commit --no-verify  # Skip pre-commit
git push --no-verify    # Skip pre-push
```

### 5. Fail Early, Fail Fast

Order checks from fastest to slowest:

```bash
# 1. Fast checks first
echo "Checking for debug statements..."
if grep -r "console.log" src/; then
  echo "Remove debug statements before committing"
  exit 1
fi

# 2. Medium checks
echo "Running linter..."
npm run lint

# 3. Slow checks last
echo "Running tests..."
npm test
```

### 6. Handle Auto-Fixes

If a hook auto-fixes files, stage them:

```bash
if git diff --name-only | grep -q "formatted-file.js"; then
  git add formatted-file.js
  echo "Auto-formatted file added to commit"
fi
```

### 7. Never Add Coverage Exclusions as First Approach

**IMPORTANT**: When pre-push hooks fail due to coverage thresholds, the correct approach is:

1. **Add tests** to increase coverage (preferred)
2. Use `--no-verify` as a **temporary emergency bypass** if absolutely necessary
3. **Never add exclusions** to `.c8rc.json`, `.nycrc`, or coverage config

**Why?**

- Exclusions hide untested code and accumulate over time
- They defeat the purpose of coverage thresholds
- They make it harder to identify actual coverage gaps

**Correct approach when coverage fails:**

```bash
# 1. Run coverage report to identify gaps
npm run test:coverage:report

# 2. Add tests for uncovered lines
# ... write tests ...

# 3. Verify coverage now passes
npm run test:coverage

# 4. Commit and push normally
git push
```

**Emergency bypass (use sparingly):**

```bash
# Only when you MUST push immediately and will add tests in follow-up
git push --no-verify

# Document why in commit message or PR
# Create a ticket to add missing tests
```

**Never do this:**

```json
// ❌ DON'T add exclusions to avoid writing tests
{
  "exclude": [
    ".scripts/new-module.js"  // ❌ WRONG - write tests instead
  ]
}
```

## References

Technology-specific hook patterns are available in the `references/` folder:

| Technology | Reference |
|------------|-----------|
| Java | `references/java/hook-patterns.md` |
| TypeScript/JavaScript | `references/typescript/hook-patterns.md` |
| Python | `references/python/hook-patterns.md` |
| Go | `references/go/hook-patterns.md` |

## Troubleshooting

### Hooks not running

1. Verify hooks are installed:

   ```bash
   git config --get core.hooksPath
   # Should output: .git-hooks
   ```

2. Check symlinks are valid:

   ```bash
   ls -la .git-hooks/
   # Should show symlinks pointing to .scripts/*-hook.sh
   ```

3. Verify execute permissions:

   ```bash
   chmod +x .git-hooks/*
   chmod +x .scripts/*-hook.sh
   ```

### Hooks running but failing

1. Run hooks manually to see full output:

   ```bash
   ./.scripts/pre-commit-hook.sh
   ```

2. Check for missing dependencies:

   ```bash
   npm install
   ```

3. Run with DEBUG mode:

   ```bash
   DEBUG=1 ./.scripts/pre-commit-hook.sh
   ```

### Hooks too slow

1. Profile each check:

   ```bash
   time npm run lint
   time npm test
   ```

2. Move slow checks to pre-push
3. Use incremental/cached operations
4. Consider staged-files-only validation

### Different behavior locally vs CI

1. CI should skip hooks (set `CI=true`)
2. CI runs validations directly, not via hooks
3. Ensure setup-hooks.js checks for CI environment
