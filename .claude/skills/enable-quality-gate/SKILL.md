---
name: enable-quality-gate
description: >
  Enable mise and hk quality gates in a repository. Detects project type,
  generates configuration files, and migrates existing hooks. Use when
  onboarding a repository to the Bitso quality gate infrastructure.
compatibility: Requires Node.js 22+ (preferably 24+), Homebrew for hk installation
metadata:
  version: "1.0.0"
  category: setup
  tags:
    - mise
    - hk
    - git-hooks
    - quality-gate
    - onboarding
  triggers:
    - on-demand
---

# Enable Quality Gate

This skill helps you set up mise and hk quality gates in a repository. It leverages `hk init --interactive --mise` to detect project type, generate configuration, and optionally migrate existing hooks.

## When to use this skill

- Onboarding a new repository to Bitso's quality gate infrastructure
- Setting up mise and hk in an existing repository
- Migrating from Husky, pre-commit, or lefthook to hk
- Configuring project-specific linters and formatters

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Instructions](#instructions)
- [Project-Specific Configuration](#project-specific-configuration)
- [Hook Migration](#hook-migration)
- [Bitso Environment Configuration](#bitso-environment-configuration)
- [Validation](#validation)
- [Troubleshooting](#troubleshooting)
- [Skill Self-Improvement](#skill-self-improvement)
- [Related Skills](#related-skills)

### Available Resources

**📚 references/** - Detailed documentation
- [java](references/java)
- [migration patterns](references/migration-patterns.md)
- [nodejs](references/nodejs)

---

## Prerequisites

Before enabling quality gates, ensure:

1. **mise** is installed and activated in your shell
2. **hk** is installed (via mise or package manager)
3. **pkl CLI** is available (for hk configuration)
4. Repository is cloned locally

### Installation by Platform

**macOS (Homebrew recommended due to AirLock):**

```bash
brew install mise hk
eval "$(mise activate zsh)"
mise use pkl
```

**Linux (distro package managers):**

```bash
# Debian/Ubuntu
curl https://mise.run | sh
eval "$(mise activate bash)"

# Fedora/RHEL
dnf copr enable jdxcode/mise
dnf install mise

# Arch
sudo pacman -S mise

# Then install hk via mise (recommended)
mise use hk pkl
```

**Alternative installation (any platform):**

```bash
# Install hk via cargo
cargo install hk

# Install hk via aqua
aqua g -i jdx/hk
```

### Verify Prerequisites

```bash
mise --version  # Should be 2026.2.2+
hk --version    # Should be 1.34.0+
pkl --version   # Required for hk.pkl
```

## Quick Start

For most repositories:

```bash
# 1. Install tools if needed (see Prerequisites for platform-specific options)
# macOS: brew install mise hk
# Linux: curl https://mise.run | sh && mise use hk pkl

# 2. Initialize with interactive mode
hk init --interactive --mise

# 3. Install hooks
hk install

# 4. Verify
hk check
```

## Instructions

### Step 1: Detect Project Type

Run `hk init` in interactive mode to detect your project:

```bash
hk init --interactive --mise
```

This command:

1. Scans the repository for project indicators (package.json, build.gradle, go.mod, etc.)
2. Presents a list of 90+ available linters relevant to your project
3. Lets you select which linters to enable
4. Generates `hk.pkl` configuration
5. Generates `mise.toml` with hk integration (if `--mise` flag used)

### Step 2: Review Generated Configuration

After running `hk init`, review the generated files:

**hk.pkl** - Git hook configuration:

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

hooks {
  ["pre-commit"] {
    fix = true
    stash = "git"
    steps {
      // Selected linters will appear here
    }
  }
}
```

**mise.toml** - Development environment:

```toml
[tools]
hk = "latest"

[tasks]
pre-commit = "hk run pre-commit"
```

### Step 3: Add Bitso-Specific Configuration

Enhance the generated configuration with Bitso standards:

**Add to mise.toml:**

```toml
[env]
# Enable hk integration
HK_MISE = "1"

# Hook mode (skip, info, warn, full)
BITSO_MISE_MODE = "full"

# Logging
HK_LOG = "warn"
HK_LOG_FILE = ".hk-logs/hk.log"
```

**Add security checks to hk.pkl:**

```pkl
hooks {
  ["pre-commit"] {
    steps {
      // Security checks (always include)
      ["no-commit-to-branch"] = Builtins.no_commit_to_branch
      ["detect-private-key"] = Builtins.detect_private_key
      ["check-merge-conflict"] = Builtins.check_merge_conflict

      // Hygiene checks
      ["trailing-whitespace"] = Builtins.trailing_whitespace
      ["newlines"] = Builtins.newlines

      // Your project-specific linters
      // ...
    }
  }
}
```

### Step 4: Install Hooks

```bash
# Install hk hooks
hk install

# Or with mise integration
hk install --mise
```

### Step 5: Configure Git Hooks Path

**Note:** hk by default uses `.git/hooks`, but Bitso Java repositories use `.git-hooks` for compatibility with existing infrastructure. Choose based on your repository's conventions:

```bash
# Option A: Bitso standard (for Java repos and repos with existing .git-hooks)
git config core.hooksPath .git-hooks
hk install  # Will use .git-hooks

# Option B: hk default (for new repos or repos without existing hook infrastructure)
hk install  # Uses .git/hooks by default

# Verify the hooks path
git config --get core.hooksPath
```

If your repository already has hooks in `.git-hooks/`, use that path. Otherwise, you can use hk's default of `.git/hooks`.

### Step 6: Add to .gitignore

```bash
# Add hk logs to .gitignore
echo ".hk-logs/" >> .gitignore
```

### Step 7: Commit Configuration

```bash
git add hk.pkl mise.toml .gitignore
git commit -m "chore: enable mise and hk quality gates"
```

## Project-Specific Configuration

### Java/Gradle Projects

Java projects require custom steps for Gradle-based tooling:

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

local javaLinters = new Mapping<String, Step> {
  ["spotless-check"] {
    glob = List("**/*.java", "**/*.kt")
    check = "./gradlew spotlessCheck --quiet"
    fix = "./gradlew spotlessApply --quiet"
  }
  ["gradle-check"] {
    glob = List("**/*.java", "**/*.kt")
    check = "./gradlew check --quiet"
  }
}

hooks {
  ["pre-commit"] {
    stash = "git"
    steps {
      // Security
      ["no-commit-to-branch"] = Builtins.no_commit_to_branch
      ["detect-private-key"] = Builtins.detect_private_key

      // Java linters
      ...javaLinters
    }
  }
  ["pre-push"] {
    steps {
      ["gradle-build"] {
        check = "./gradlew build --quiet"
      }
    }
  }
}
```

See [references/java/setup.md](references/java/setup.md) for complete Java configuration.

### Node.js/TypeScript Projects

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

hooks {
  ["pre-commit"] {
    fix = true
    stash = "git"
    steps {
      // Security
      ["no-commit-to-branch"] = Builtins.no_commit_to_branch
      ["detect-private-key"] = Builtins.detect_private_key

      // Node.js linters
      ["eslint"] = Builtins.eslint
      ["prettier"] = Builtins.prettier
      ["tsc"] = Builtins.tsc
    }
  }
  ["pre-push"] {
    steps {
      ["test"] {
        check = "npm test"
      }
    }
  }
}
```

See [references/nodejs/setup.md](references/nodejs/setup.md) for complete Node.js configuration.

### Python Projects

```pkl
hooks {
  ["pre-commit"] {
    fix = true
    stash = "git"
    steps {
      ["ruff"] = Builtins.ruff
      ["ruff-format"] = Builtins.ruff_format
      ["mypy"] = Builtins.mypy
    }
  }
}
```

### Go Projects

```pkl
hooks {
  ["pre-commit"] {
    fix = true
    stash = "git"
    steps {
      ["golangci-lint"] = Builtins.golangci_lint
      ["go-fmt"] = Builtins.go_fmt
      ["go-vet"] = Builtins.go_vet
    }
  }
}
```

## Hook Migration

### From Husky

1. **Backup existing hooks:**

   ```bash
   cp -r .husky .husky.backup
   ```

2. **Run hk init:**

   ```bash
   hk init --interactive --mise
   ```

3. **Migrate lint-staged configuration:**

   If using lint-staged, add it as a step:

   ```pkl
   ["lint-staged"] {
     check = "npx lint-staged"
   }
   ```

4. **Remove Husky:**

   ```bash
   # Remove from package.json
   npm pkg delete scripts.prepare

   # Remove Husky directory
   rm -rf .husky
   ```

### From pre-commit (Python)

1. **Map hooks to hk builtins:**

   | pre-commit hook | hk builtin |
   |-----------------|------------|
   | `trailing-whitespace` | `Builtins.trailing_whitespace` |
   | `end-of-file-fixer` | `Builtins.newlines` |
   | `check-yaml` | `Builtins.yamllint` |
   | `black` | `Builtins.black` |
   | `flake8` | `Builtins.flake8` |
   | `isort` | `Builtins.isort` |

2. **Remove pre-commit:**

   ```bash
   pre-commit uninstall
   rm .pre-commit-config.yaml
   ```

### From lefthook

1. **Translate lefthook.yml to hk.pkl:**

   lefthook commands map directly to hk steps. File patterns are similar.

2. **Remove lefthook:**

   ```bash
   lefthook uninstall
   rm lefthook.yml
   ```

See [references/migration-patterns.md](references/migration-patterns.md) for detailed migration patterns.

## Bitso Environment Configuration

### Hook Mode System

Configure hook behavior via environment variables:

| Variable | Scope | Default |
|----------|-------|---------|
| `BITSO_MISE_MODE` | Global | `full` |
| `BITSO_MISE_GIT_HOOKS` | All git hooks | (from global) |
| `BITSO_MISE_GIT_HOOKS_COMMIT` | pre-commit | (from git hooks) |
| `BITSO_MISE_GIT_HOOKS_PUSH` | pre-push | (from git hooks) |

### Local Developer Override

Create `mise.local.toml` (git-ignored) for local preferences:

```toml
[env]
# Don't fail commits locally, but enforce on push
BITSO_MISE_GIT_HOOKS_COMMIT = "warn"
BITSO_MISE_GIT_HOOKS_PUSH = "full"
```

### CI Configuration

In CI, hooks should be skipped (CI runs validations directly):

```yaml
env:
  CI: true
  BITSO_MISE_GIT_HOOKS: skip
```

## Validation

### Verify Installation

```bash
# Check tools
mise --version  # Should be 2026.2.2+
hk --version    # Should be 1.34.0+

# Validate configuration
hk validate

# Run checks
hk check

# Test pre-commit
hk run pre-commit
```

### Checklist

- [ ] `hk.pkl` exists and is valid
- [ ] `mise.toml` exists (if using mise integration)
- [ ] `hk validate` passes
- [ ] `hk run pre-commit` executes successfully
- [ ] `git commit` triggers hooks
- [ ] Configuration files are committed
- [ ] `.hk-logs/` is in `.gitignore`

## Troubleshooting

### hk not found

```bash
# Ensure Homebrew version is used
echo "alias hk='$(brew --prefix)/bin/hk'" >> ~/.zshrc
source ~/.zshrc
```

### pkl package download fails

```bash
# Download with CA certificate for Cloudflare WARP
pkl download-package --ca-certificates=$HOME/cloudflare_ca_certificate.pem \
  package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0
```

### Hooks not running

```bash
# Reinstall hooks
hk install

# Check git config
git config --get core.hooksPath  # Should be .git-hooks (Bitso Java) or empty (hk default .git/hooks)

# Manual test
hk run pre-commit
```

### Gradle timeout

For slow Gradle builds, increase timeout or move to pre-push:

```pkl
["gradle-check"] {
  check = "./gradlew check --quiet"
  profile = "slow"  // Only runs with HK_PROFILE=slow
}
```

## Skill Self-Improvement

This skill should evolve based on learnings from actual migrations. After executing a migration:

### Document Learnings

If you encounter issues or discover better patterns during a migration:

1. **Note the learning** - What worked, what didn't, what was missing from this skill
2. **Identify the improvement** - Which section needs updating (Prerequisites, Instructions, Troubleshooting, etc.)
3. **Propose a PR** - Create a PR to update this skill in `ai-code-instructions`

### Creating Improvement PRs

```bash
# 1. Clone ai-code-instructions (if not already)
cd ~/bitsoex
git clone https://github.com/bitsoex/ai-code-instructions.git

# 2. Create a branch for the improvement
cd ai-code-instructions
git checkout main && git pull
git checkout -b docs/improve-enable-quality-gate-skill

# 3. Edit the skill
# - global/skills/enable-quality-gate/SKILL.md
# - global/skills/enable-quality-gate/references/*.md

# 4. Commit with context about the learning
git add global/skills/enable-quality-gate/
git commit -m "docs(enable-quality-gate): add learning from [repo] migration

- [What was learned]
- [What was added/changed]

Context: Discovered during migration of [repo-name]"

# 5. Create PR
git push -u origin HEAD
gh pr create --draft --title "docs(enable-quality-gate): [improvement title]" \
  --body "## Learning from migration

**Repository:** \`bitsoex/[repo-name]\`
**Issue encountered:** [description]
**Solution added:** [what this PR adds]

## Changes

- [list of changes to the skill]"
```

### Types of Improvements

| Category | Example |
|----------|---------|
| **New pattern** | Migration from a new hook system not documented |
| **Troubleshooting** | Error message not covered in troubleshooting section |
| **Configuration** | Project type needs special handling |
| **Prerequisite** | Additional tool or setup step required |
| **Clarification** | Instructions were ambiguous or incomplete |

### Feedback Loop

The goal is continuous improvement:

```
Migration attempt → Learn → Update skill → Next migration benefits
```

When proposing improvements, include:
- The specific repository where the issue was encountered
- The exact error or confusion point
- The solution that worked
- Suggested documentation update

## Related Skills

| Skill | Purpose |
|-------|---------|
| mise | mise configuration details |
| hk | hk configuration details |
| git-hooks | General hook patterns |
| quality-checks | Runtime quality orchestration |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/enable-quality-gate/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

