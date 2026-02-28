# Hook Migration Patterns

This document provides detailed patterns for migrating from existing hook managers to hk.

## Contents

- [From Husky](#from-husky)
- [From pre-commit (Python)](#from-pre-commit-python)
- [From lefthook](#from-lefthook)
- [Parallel Execution](#parallel-execution)
- [Handling Custom Scripts](#handling-custom-scripts)
- [Environment Variables](#environment-variables)
- [Troubleshooting Migration](#troubleshooting-migration)

---
## From Husky

### Common Husky Setup

Typical Husky configuration:

```
.husky/
├── _/
│   └── husky.sh
├── pre-commit
└── pre-push
```

With `package.json`:

```json
{
  "scripts": {
    "prepare": "husky install"
  },
  "lint-staged": {
    "*.{js,ts}": ["eslint --fix", "prettier --write"]
  }
}
```

### Migration Steps

1. **Generate hk configuration:**

   ```bash
   hk init --interactive --mise
   ```

2. **Map Husky hooks to hk:**

   | Husky file | hk hook |
   |------------|---------|
   | `.husky/pre-commit` | `hooks["pre-commit"]` |
   | `.husky/pre-push` | `hooks["pre-push"]` |
   | `.husky/commit-msg` | `hooks["commit-msg"]` |

3. **Migrate lint-staged:**

   If using lint-staged, you have two options:

   **Option A: Keep lint-staged**

   ```pkl
   hooks {
     ["pre-commit"] {
       steps {
         ["lint-staged"] {
           check = "npx lint-staged"
         }
       }
     }
   }
   ```

   **Option B: Use hk builtins (recommended)**

   ```pkl
   hooks {
     ["pre-commit"] {
       fix = true  // Enable auto-fix
       stash = "git"  // Stash unstaged changes
       steps {
         ["eslint"] = Builtins.eslint
         ["prettier"] = Builtins.prettier
       }
     }
   }
   ```

4. **Remove Husky:**

   ```bash
   # Remove prepare script
   npm pkg delete scripts.prepare

   # Remove Husky
   npm uninstall husky
   rm -rf .husky

   # Remove lint-staged (if migrated to hk)
   npm uninstall lint-staged
   rm lint-staged.config.js
   ```

5. **Install hk hooks:**

   ```bash
   hk install
   ```

### Example: Complete Migration

**Before (Husky):**

`.husky/pre-commit`:

```bash
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"
npx lint-staged
```

**After (hk):**

`hk.pkl`:

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

hooks {
  ["pre-commit"] {
    fix = true
    stash = "git"
    steps {
      ["eslint"] = Builtins.eslint
      ["prettier"] = Builtins.prettier
    }
  }
}
```

## From pre-commit (Python)

### Common pre-commit Setup

`.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.5.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
      - id: check-yaml
      - id: check-added-large-files
  - repo: https://github.com/psf/black
    rev: 24.1.0
    hooks:
      - id: black
  - repo: https://github.com/charliermarsh/ruff-pre-commit
    rev: v0.1.0
    hooks:
      - id: ruff
```

### Migration Mapping

| pre-commit hook | hk builtin |
|-----------------|------------|
| `trailing-whitespace` | `Builtins.trailing_whitespace` |
| `end-of-file-fixer` | `Builtins.newlines` |
| `check-yaml` | `Builtins.yamllint` |
| `check-added-large-files` | `Builtins.check_added_large_files` |
| `check-merge-conflict` | `Builtins.check_merge_conflict` |
| `detect-private-key` | `Builtins.detect_private_key` |
| `black` | `Builtins.black` |
| `ruff` | `Builtins.ruff` |
| `ruff-format` | `Builtins.ruff_format` |
| `flake8` | `Builtins.flake8` |
| `isort` | `Builtins.isort` |
| `mypy` | `Builtins.mypy` |
| `pylint` | `Builtins.pylint` |

### Migration Steps

1. **Generate hk configuration:**

   ```bash
   hk init --interactive --mise
   ```

2. **Map hooks:**

   ```pkl
   hooks {
     ["pre-commit"] {
       fix = true
       stash = "git"
       steps {
         // pre-commit-hooks equivalents
         ["trailing-whitespace"] = Builtins.trailing_whitespace
         ["newlines"] = Builtins.newlines
         ["yamllint"] = Builtins.yamllint
         ["check-added-large-files"] = Builtins.check_added_large_files

         // Python linters
         ["ruff"] = Builtins.ruff
         ["ruff-format"] = Builtins.ruff_format
         ["mypy"] = Builtins.mypy
       }
     }
   }
   ```

3. **Remove pre-commit:**

   ```bash
   pre-commit uninstall
   rm .pre-commit-config.yaml
   pip uninstall pre-commit
   ```

4. **Install hk hooks:**

   ```bash
   hk install
   ```

## From lefthook

### Common lefthook Setup

`lefthook.yml`:

```yaml
pre-commit:
  parallel: true
  commands:
    eslint:
      glob: "*.{js,ts,tsx}"
      run: eslint {staged_files}
    prettier:
      glob: "*.{js,ts,tsx,json,md}"
      run: prettier --check {staged_files}

pre-push:
  commands:
    test:
      run: npm test
```

### Migration Mapping

lefthook concepts map closely to hk:

| lefthook | hk |
|----------|-----|
| `commands` | `steps` |
| `glob` | `glob` |
| `run` | `check` / `fix` |
| `parallel: true` | Default in hk |
| `{staged_files}` | `{{files}}` |
| `{all_files}` | `{{all_files}}` |

### Migration Steps

1. **Generate hk configuration:**

   ```bash
   hk init --interactive --mise
   ```

2. **Translate lefthook.yml:**

   **Before (lefthook):**

   ```yaml
   pre-commit:
     commands:
       eslint:
         glob: "*.{js,ts}"
         run: eslint {staged_files}
   ```

   **After (hk):**

   ```pkl
   hooks {
     ["pre-commit"] {
       steps {
         ["eslint"] {
           glob = List("**/*.js", "**/*.ts")
           check = "eslint {{files}}"
           fix = "eslint --fix {{files}}"
         }
       }
     }
   }
   ```

   Or use builtins:

   ```pkl
   ["eslint"] = Builtins.eslint
   ```

3. **Remove lefthook:**

   ```bash
   lefthook uninstall
   rm lefthook.yml
   ```

4. **Install hk hooks:**

   ```bash
   hk install
   ```

## Parallel Execution

hk runs steps in parallel by default. To enforce sequential execution:

```pkl
["step1"] {
  check = "..."
  exclusive = true  // Blocks other steps until complete
}
["step2"] {
  depends = "step1"  // Waits for step1
}
```

## Handling Custom Scripts

For hooks that run custom scripts:

**Before:**

```bash
#!/bin/bash
./scripts/custom-check.sh
```

**After:**

```pkl
["custom-check"] {
  check = "./scripts/custom-check.sh"
}
```

## Environment Variables

Set environment variables for hooks:

```pkl
["my-step"] {
  check = "my-command"
  env {
    ["MY_VAR"] = "value"
  }
}
```

Or globally in mise.toml:

```toml
[env]
MY_VAR = "value"
```

## Troubleshooting Migration

### Scripts Not Found

Ensure scripts are executable:

```bash
chmod +x scripts/*.sh
```

### Different Exit Codes

hk treats any non-zero exit as failure. Adjust scripts if needed:

```bash
# Force success
my-command || true

# Or in hk.pkl
["my-step"] {
  check = "my-command || true"
}
```

### Timeout Issues

For slow hooks, use profiles:

```pkl
["slow-check"] {
  check = "..."
  profile = "slow"
}
```

Skip with: `HK_PROFILE=!slow hk run pre-commit`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/enable-quality-gate/references/migration-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

