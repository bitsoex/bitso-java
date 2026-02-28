# Git Hooks Bridge Strategy

This document describes how our distributed git hooks coexist with existing hook setups in target repositories.

## Contents

- [The Critical Problem We Solve](#the-critical-problem-we-solve)
- [Our Solution: The Bridge Strategy](#our-solution-the-bridge-strategy)
- [Complete Flow Diagram](#complete-flow-diagram)
- [Real-World Example: react-web (Husky)](#real-world-example-react-web-husky)
- [What If Husky Fails?](#what-if-husky-fails)
- [Hook Technologies Landscape](#hook-technologies-landscape)
- [Directory Structure in Target Repos](#directory-structure-in-target-repos)
- [How core.hooksPath Works](#how-corehookspath-works)
- [How the Bridge Works (Technical Details)](#how-the-bridge-works-technical-details)
- [Coexistence Patterns (Automatic via Bridge)](#coexistence-patterns-automatic-via-bridge)
- [Integration Test Coverage](#integration-test-coverage)
- [Installation During Distribution](#installation-during-distribution)
- [Informative Mode](#informative-mode)
- [Node.js Requirement](#nodejs-requirement)
- [Enabling Our Hooks](#enabling-our-hooks)
- [Disabling Our Hooks](#disabling-our-hooks)
- [What Developers Need to Know](#what-developers-need-to-know)
- [FAQ](#faq)
- [Future Work](#future-work)

---
## The Critical Problem We Solve

When you set `git config core.hooksPath .git-hooks`, Git **ONLY** looks in that directory for hooks. This means:

```
❌ WITHOUT BRIDGE STRATEGY:

   git commit → Git checks core.hooksPath → runs .git-hooks/pre-commit
                                           ↓
                                    Our checks run
                                           ↓
                              Existing hooks NEVER run!
                              (.husky/, pre-commit, lefthook)
```

**This would break the developer's existing workflow!**

## Our Solution: The Bridge Strategy

Our hooks act as a **BRIDGE** - they run our checks first, then call the existing hooks:

```
✅ WITH BRIDGE STRATEGY:

   git commit → Git checks core.hooksPath → runs .git-hooks/pre-commit
                                                     ↓
                                           ┌────────────────────┐
                                           │ hooks-bootstrap.sh │
                                           └────────────────────┘
                                                     ↓
                                           ┌────────────────────┐
                                           │  1. OUR CHECKS     │
                                           │  (Informative -    │
                                           │   always exit 0)   │
                                           └────────────────────┘
                                                     ↓
                                           ┌────────────────────┐
                                           │  2. BRIDGE CALLS   │
                                           │  existing hooks    │
                                           └────────────────────┘
                                                     ↓
                            ┌────────────┬───────────┬────────────┬────────────┐
                            ↓            ↓           ↓            ↓            ↓
                       .husky/      pre-commit   lefthook    .git/hooks/    (none)
                       pre-commit     run         run        pre-commit
                            ↓            ↓           ↓            ↓
                     (may block)    (may block)  (may block) (may block)
```

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DEVELOPER RUNS: git commit                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  GIT CHECKS: Is core.hooksPath set?                                         │
│                                                                             │
│  If YES → Use .git-hooks/pre-commit                                         │
│  If NO  → Use .git/hooks/pre-commit (or Husky, etc.)                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                          (core.hooksPath = .git-hooks)
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  .git-hooks/pre-commit                                                      │
│  ───────────────────                                                        │
│  #!/bin/bash                                                                │
│  exec "$SCRIPT_DIR/hooks-bootstrap.sh" pre-commit                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  hooks-bootstrap.sh                                                         │
│  ─────────────────────────────────────────────────────────────────────────  │
│                                                                             │
│  PHASE 1: ENSURE NODE.JS                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │ source ensure-node.sh                                                   ││
│  │  → Try nvm (Bitso standard - activates .nvmrc)                          ││
│  │  → Try fnm (Fast Node Manager)                                          ││
│  │  → Check system Node                                                    ││
│  │  → If none: show setup message, exit 0 (don't block)                    ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                      │                                      │
│                                      ▼                                      │
│  PHASE 2: RUN OUR QUALITY CHECKS (INFORMATIVE)                              │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │ node hooks-checks.js pre-commit                                         ││
│  │                                                                         ││
│  │  → Detect project types (node, gradle, python, go, etc.)                ││
│  │  → Run linting checks                                                   ││
│  │  → Run type checks                                                      ││
│  │  → Show results                                                         ││
│  │  → ALWAYS EXIT 0 (informative mode)                                     ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                      │                                      │
│                                      ▼                                      │
│  PHASE 3: BRIDGE TO EXISTING HOOKS                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │ # Check and call each existing hook system:                             ││
│  │                                                                         ││
│  │ if [ -x ".husky/pre-commit" ]; then                                     ││
│  │     echo "[hooks] Running existing Husky hook..."                       ││
│  │     .husky/pre-commit || EXISTING_HOOK_EXIT=$?                          ││
│  │ fi                                                                      ││
│  │                                                                         ││
│  │ if [ -f ".pre-commit-config.yaml" ] && command -v pre-commit; then      ││
│  │     echo "[hooks] Running existing pre-commit hooks..."                 ││
│  │     pre-commit run --hook-stage pre-commit || EXISTING_HOOK_EXIT=$?     ││
│  │ fi                                                                      ││
│  │                                                                         ││
│  │ if [ -f "lefthook.yml" ] && command -v lefthook; then                   ││
│  │     echo "[hooks] Running existing lefthook hooks..."                   ││
│  │     lefthook run pre-commit || EXISTING_HOOK_EXIT=$?                    ││
│  │ fi                                                                      ││
│  │                                                                         ││
│  │ if [ -x ".git/hooks/pre-commit" ]; then                                 ││
│  │     echo "[hooks] Running native git hook..."                           ││
│  │     .git/hooks/pre-commit || EXISTING_HOOK_EXIT=$?                      ││
│  │ fi                                                                      ││
│  └─────────────────────────────────────────────────────────────────────────┘│
│                                      │                                      │
│                                      ▼                                      │
│  exit $EXISTING_HOOK_EXIT  ← Propagate exit code from existing hooks        │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  RESULT                                                                     │
│                                                                             │
│  • Our checks: ALWAYS shown (informative, never block)                      │
│  • Existing hooks: RUN AS NORMAL (can block if they fail)                   │
│  • Final exit code: From existing hooks (not ours)                          │
│                                                                             │
│  Developer experience is UNCHANGED - existing hooks still work!             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Real-World Example: react-web (Husky)

```
Developer: git commit -m "Add feature"

┌───────────────────────────────────────────────────────────────────┐
│ Terminal Output:                                                  │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│ ============================================================      │
│   Bitso Quality Checks (Informative)                              │
│ ============================================================      │
│                                                                   │
│   Detected project types: node                                    │
│   Running pre-commit checks...                                    │
│                                                                   │
│   [✓] All checks passed                                           │
│                                                                   │
│   Your commit will proceed.                                       │
│                                                                   │
│ ============================================================      │
│                                                                   │
│ [hooks] Running existing Husky hook...    ← BRIDGE CALLS HUSKY    │
│                                                                   │
│ > lint-staged                             ← HUSKY'S CHECKS RUN    │
│ ✔ Running tasks for staged files...                               │
│ ✔ ESLint passed                                                   │
│ ✔ Prettier passed                                                 │
│                                                                   │
│ [main abc1234] Add feature                ← COMMIT SUCCEEDS       │
└───────────────────────────────────────────────────────────────────┘
```

## What If Husky Fails?

```
Developer: git commit -m "Add feature with lint errors"

┌───────────────────────────────────────────────────────────────────┐
│ Terminal Output:                                                  │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│ ============================================================      │
│   Bitso Quality Checks (Informative)                              │
│ ============================================================      │
│                                                                   │
│   [!] Linting: ESLint errors detected     ← OUR CHECK WARNS       │
│       Run: mise run lint:fix                                      │
│                                                                   │
│   These are recommendations. Your commit will proceed.            │
│                                                                   │
│ ============================================================      │
│                                                                   │
│ [hooks] Running existing Husky hook...                            │
│                                                                   │
│ > lint-staged                                                     │
│ ✖ ESLint found errors                     ← HUSKY FAILS           │
│   src/component.tsx:15 Unexpected console statement               │
│                                                                   │
│ husky - pre-commit hook exited with code 1                        │
│                                                                   │
│ hint: The commit was aborted.             ← COMMIT BLOCKED        │
└───────────────────────────────────────────────────────────────────┘
```

**Key Point**: Our checks warn, Husky blocks. The existing workflow is preserved!

## Hook Technologies Landscape

Based on analysis of 146+ Bitso repositories:

| Technology | Repos | Mechanism | Our Bridge Handles It? |
|------------|-------|-----------|------------------------|
| **Husky** | ~7 | `.husky/` directory | ✅ Yes - Calls `.husky/$HOOK` |
| **pre-commit (Python)** | ~7 | `.pre-commit-config.yaml` | ✅ Yes - Runs `pre-commit run` |
| **lefthook** | ~2 | `lefthook.yml` | ✅ Yes - Runs `lefthook run` |
| **Native git hooks** | varies | `.git/hooks/` | ✅ Yes - Calls `.git/hooks/$HOOK` |
| **Spotless** | ~44 | Gradle plugin (build time) | N/A - Not git hooks |
| **None** | ~80+ | No hooks | ✅ Just runs our checks |

## Directory Structure in Target Repos

After our hooks are distributed:

```
target-repo/
├── .git-hooks/                    ← OUR HOOKS (via core.hooksPath)
│   ├── pre-commit                 ← Entry point
│   ├── pre-push                   ← Entry point
│   ├── hooks-bootstrap.sh         ← Bridge logic
│   ├── hooks-checks.js            ← Quality checks
│   └── ensure-node.sh             ← Node.js detection
│
├── .husky/                        ← EXISTING HUSKY (still works!)
│   ├── _/
│   │   └── husky.sh
│   ├── pre-commit                 ← Called by our bridge
│   └── pre-push
│
├── .pre-commit-config.yaml        ← EXISTING PRE-COMMIT (still works!)
│
├── lefthook.yml                   ← EXISTING LEFTHOOK (still works!)
│
└── .git/
    └── hooks/                     ← NATIVE HOOKS (still works!)
        └── pre-commit             ← Called by our bridge
```

## How core.hooksPath Works

```
Git's Hook Resolution Order:
────────────────────────────

1. Check if core.hooksPath is configured
   └─→ git config --get core.hooksPath

2. If SET: Use that directory exclusively
   └─→ .git-hooks/pre-commit

3. If NOT SET: Use default location
   └─→ .git/hooks/pre-commit
```

**Important**: `core.hooksPath` is a FULL OVERRIDE - Git won't check `.git/hooks/` at all when it's set. That's why our bridge must explicitly call other hook systems.

## How the Bridge Works (Technical Details)

The bridge is implemented in `hooks-bootstrap.sh`. After running our checks, it:

```bash
# 1. Try Husky hooks
if [ -x "$REPO_ROOT/.husky/$HOOK_TYPE" ]; then
    echo "[hooks] Running existing Husky hook..."
    "$REPO_ROOT/.husky/$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# 2. Try pre-commit framework
if [ -f "$REPO_ROOT/.pre-commit-config.yaml" ] && command -v pre-commit &>/dev/null; then
    echo "[hooks] Running existing pre-commit hooks..."
    pre-commit run --hook-stage "$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# 3. Try lefthook
if [ -f "$REPO_ROOT/lefthook.yml" ] && command -v lefthook &>/dev/null; then
    echo "[hooks] Running existing lefthook hooks..."
    lefthook run "$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# 4. Try native git hooks
if [ -x "$REPO_ROOT/.git/hooks/$HOOK_TYPE" ]; then
    echo "[hooks] Running native git hook..."
    "$REPO_ROOT/.git/hooks/$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# Propagate existing hook exit code
exit $EXISTING_HOOK_EXIT
```

## Coexistence Patterns (Automatic via Bridge)

### Pattern 1: Husky Repos (e.g., react-web)

**How it works now**:

```
git commit
    │
    ▼
.git-hooks/pre-commit (our hook via core.hooksPath)
    │
    ├─── OUR CHECKS (informative, exit 0)
    │
    └─── BRIDGE: .husky/pre-commit (Husky's hook)
              │
              └─── lint-staged, etc.
                        │
                        └─── Exit code propagated (can block)
```

**Result**: Developer sees our informative checks + Husky runs normally!

### Pattern 2: pre-commit (Python) Repos (e.g., estate-catalog)

**How it works now**:

```
git commit
    │
    ▼
.git-hooks/pre-commit (our hook via core.hooksPath)
    │
    ├─── OUR CHECKS (informative, exit 0)
    │
    └─── BRIDGE: pre-commit run --hook-stage pre-commit
              │
              └─── Runs all hooks from .pre-commit-config.yaml
                        │
                        └─── Exit code propagated (can block)
```

**Note**: Only works if `pre-commit` CLI is installed on the system.

### Pattern 3: lefthook Repos (e.g., openapi-toolkit)

**How it works now**:

```
git commit
    │
    ▼
.git-hooks/pre-commit (our hook via core.hooksPath)
    │
    ├─── OUR CHECKS (informative, exit 0)
    │
    └─── BRIDGE: lefthook run pre-commit
              │
              └─── Runs all commands from lefthook.yml
                        │
                        └─── Exit code propagated (can block)
```

**Note**: Only works if `lefthook` CLI is installed on the system.

### Pattern 4: No Existing Hooks (e.g., aum-reconciliation-v2)

**How it works now**:

```
git commit
    │
    ▼
.git-hooks/pre-commit (our hook via core.hooksPath)
    │
    └─── OUR CHECKS (informative, exit 0)
              │
              └─── No bridge needed (nothing to call)
                        │
                        └─── Commit proceeds
```

## Integration Test Coverage

Our integration tests verify bridge behavior on real repositories:

| Repository | Hook System | What We Test |
|------------|-------------|--------------|
| react-web | Husky | `.husky/` exists, bridge attempts to call it |
| estate-catalog | pre-commit | `.pre-commit-config.yaml` is valid, bridge detects it |
| openapi-toolkit | lefthook | `lefthook.yml` is valid, bridge detects it |
| aum-reconciliation-v2 | None | Our hooks run standalone |
| estate-catalog-mcp | None | Our hooks detect Python project |

## Installation During Distribution

When ci-distribute runs, it should:

1. **Distribute files** to `.git-hooks/` (all assets in same directory)
2. **NOT set `core.hooksPath`** automatically (leave to repo owners)
3. **Document** how to enable in the distributed README

```
Distributed files:
.git-hooks/
├── pre-commit          # Executable hook entry point
├── pre-push            # Executable hook entry point
├── hooks-bootstrap.sh  # Bridge logic + Node loading
├── hooks-checks.js     # Quality check implementation
└── ensure-node.sh      # Node.js detection (nvm, fnm)
```

## Informative Mode

All our hooks run in **informative mode**:

- Always exit 0 (never block commits/pushes)
- Show clear warnings with fix commands
- Designed to guide both humans and AI agents

```
============================================================
  Bitso Quality Checks (Informative)
============================================================

  Pre-commit checks found some issues:

  [!] Linting: ESLint errors detected
      Run: mise run lint:fix

  [!] TypeScript: Type errors detected
      Run: npx tsc --noEmit

  These are recommendations. Your commit will proceed.
  For AI agents: Please address these issues before completing.

============================================================
```

## Node.js Requirement

Our hooks require Node.js 20+. The `ensure-node.sh` script handles this by:

1. **Try nvm first** (Bitso standard - activates `.nvmrc` if present)
2. **Try fnm** (Fast Node Manager) as alternative
3. **Check system Node** (Homebrew, system-installed)
4. **Show setup message** if none available (never blocks)

```
Node.js Detection Order:
────────────────────────
1. nvm (loads ~/.nvm/nvm.sh, uses .nvmrc)
2. fnm (loads from shell profile)
3. System Node (PATH lookup)
4. Fail gracefully (show instructions, exit 0)
```

**Important**: We do NOT auto-install Node.js. We only activate existing installations.
This prevents the hook from blocking for minutes during Homebrew/nvm installs.

## Enabling Our Hooks

For repos that want to use our hooks:

```bash
# Enable our hooks
git config core.hooksPath .git-hooks

# Verify
git config --get core.hooksPath
# Should output: .git-hooks

# Test
.git-hooks/pre-commit
```

## Disabling Our Hooks

```bash
# Remove the config
git config --unset core.hooksPath

# Or bypass temporarily
git commit --no-verify
```

## What Developers Need to Know

### Enabling Our Hooks (One-Time Setup)

```bash
# Enable our hooks to run
git config core.hooksPath .git-hooks

# Verify it's set
git config --get core.hooksPath
# Output: .git-hooks
```

### What Changes for the Developer?

| Before | After |
|--------|-------|
| Run `git commit` | Run `git commit` (same!) |
| Husky/pre-commit/lefthook runs | **Our checks run first**, then Husky/pre-commit/lefthook runs |
| Commit blocked on failure | **Our checks never block**, existing hooks still block on failure |

### Bypass Options (Emergency)

```bash
# Skip all hooks (ours AND existing)
git commit --no-verify

# Disable our hooks temporarily
git config --unset core.hooksPath
# (existing hooks will work again if they were installed to .git/hooks/)
```

## FAQ

### Q: Will my existing Husky hooks still block commits on lint errors?

**Yes!** Our bridge calls `.husky/pre-commit` after our checks. If Husky fails, the commit is blocked.

### Q: What if I don't have pre-commit/lefthook CLI installed?

The bridge checks if the CLI is available before calling it. If not installed, it just skips that bridge and continues. Your `.pre-commit-config.yaml` or `lefthook.yml` is still respected when you do have the CLI.

### Q: Can I have multiple hook systems active at once?

Yes! If you have both Husky and pre-commit configured, the bridge will call both:

```
.git-hooks/pre-commit
    ├─── Our checks (informative)
    ├─── .husky/pre-commit (if exists)
    └─── pre-commit run (if config exists)
```

### Q: Why don't your checks block commits?

Our checks are **informative** - designed to guide developers and AI agents without disrupting workflow. For blocking behavior, use your existing Husky/pre-commit/lefthook configuration.

### Q: How do I add blocking checks?

Add them to your existing hook system:

```bash
# For Husky: Edit .husky/pre-commit
# For pre-commit: Edit .pre-commit-config.yaml
# For lefthook: Edit lefthook.yml
```

## Future Work

1. **Detection script**: Automatically detect existing hook setup and provide tailored migration guidance
2. **Phase 2 distribution**: Add hooks to `managed-paths.json` for automatic distribution
3. **Enforcement mode**: Optional mode that blocks on critical issues (for AI agent hooks)
4. **Tool availability check**: Warn if pre-commit/lefthook config exists but CLI is not installed
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/git-hooks/assets/hooks-bridge-strategy.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

