# CodeRabbit CLI Integration

Based on [CodeRabbit CLI Cursor Integration](https://docs.coderabbit.ai/cli/cursor-integration).

## Contents

- [Overview](#overview)
- [Installation](#installation)
- [Authentication](#authentication)
- [Key Commands](#key-commands)
- [Expected Duration](#expected-duration)
- [Async Workflow (Recommended)](#async-workflow-recommended)
- [Integration with Cursor/IDE](#integration-with-cursoride)
- [When to Use CLI vs PR Review](#when-to-use-cli-vs-pr-review)
- [Commit Attribution After CLI Review](#commit-attribution-after-cli-review)
- [Troubleshooting](#troubleshooting)
- [Links](#links)

---
## Overview

CodeRabbit CLI enables local code review before pushing to a PR, providing the same analysis that runs on PRs but earlier in the development cycle.

## Installation

```bash
# Install globally
curl -fsSL https://cli.coderabbit.ai/install.sh | sh

# Restart shell
source ~/.zshrc  # or ~/.bashrc

# Verify installation
coderabbit --version
```

## Authentication

```bash
# Login (opens browser for GitHub OAuth)
coderabbit auth login

# Verify authentication
coderabbit auth status
```

## Key Commands

### Review Uncommitted Changes (Most Common)

```bash
coderabbit --prompt-only --type uncommitted
```

This reviews only your working directory changes - fast and focused.

### Review Against Branch

```bash
coderabbit --prompt-only --base main
```

Reviews all changes between current branch and base branch.

### Review Types

| Flag | What It Reviews |
|------|-----------------|
| `--type uncommitted` | Only uncommitted changes (working directory) |
| `--type committed` | Only committed changes (not pushed) |
| `--type all` | Both committed and uncommitted (default) |

### Output Modes

| Flag | Output Format |
|------|---------------|
| `--prompt-only` | AI-optimized output (recommended for agents) |
| (default) | Full human-readable output |

## Expected Duration

CodeRabbit analysis takes **7-30 minutes** depending on:

- Amount of code changed
- Complexity of the codebase
- Number of files affected

**Recommendation:** Run asynchronously and continue other work while waiting.

## Async Workflow (Recommended)

Instead of blocking on reviews, use this async approach:

```text
1. Commit changes locally
2. Start: coderabbit --prompt-only --type uncommitted &
3. Continue working on other tasks
4. Check results when ready (output saved to terminal or file)
5. Fix issues with proper commit format
6. Push to PR
```

### Running in Background

```bash
# Run in background, save output
coderabbit --prompt-only --type uncommitted > .tmp/coderabbit-output.txt 2>&1 &
echo "Review started in background, PID: $!"

# Check if complete
jobs

# View results when done
cat .tmp/coderabbit-output.txt
```

## Integration with Cursor/IDE

According to the [official docs](https://docs.coderabbit.ai/cli/cursor-integration):

> Ask Cursor to implement a feature, run a code review, and fix any issues, as a natural part of agentic development.

**Cursor rule suggestion:**

```text
# Running the CodeRabbit CLI

CodeRabbit is already installed in the terminal. Run it as a way to review your code.
Run the command: cr -h for details on commands available.

In general, I want you to run coderabbit with the `--prompt-only` flag.
To review uncommitted changes, run: `coderabbit --prompt-only -t uncommitted`.

IMPORTANT: When running CodeRabbit to review code changes, don't run it more than 3 times in a given set of changes.
```

## When to Use CLI vs PR Review

| Scenario | Use CLI | Use PR Review |
|----------|---------|---------------|
| Early feedback before push | ✅ | |
| Complex changes, want review first | ✅ | |
| Already pushed to PR | | ✅ |
| Want permanent review record | | ✅ |
| Quick iteration cycle | ✅ | |
| Formal code review process | | ✅ |

## Commit Attribution After CLI Review

When fixing issues found by CLI review, use this commit format:

```bash
git commit -m "fix: address CodeRabbit CLI review findings

- [file.js]: Fixed issue description
- [other.js]: Fixed issue description

Reviewed-by: CodeRabbit CLI (local)
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

The `Reviewed-by: CodeRabbit CLI (local)` line tells CodeRabbit (when it reviews the PR) that you already ran local review.

## Troubleshooting

### "coderabbit: command not found"

```bash
source ~/.zshrc
which coderabbit
```

### "not authenticated"

```bash
coderabbit auth login
coderabbit auth status
```

### Review taking too long

- Use `--type uncommitted` for faster reviews
- Review smaller changesets
- Check if many files are changed: `git status --short | wc -l`

### No issues found but expected

1. Verify changes are tracked: `git status`
2. Check authentication: `coderabbit auth status`
3. Try different scope: `--type all`
4. Check base branch: `--base develop`

## Links

- [CodeRabbit CLI Overview](https://docs.coderabbit.ai/cli/overview)
- [Cursor Integration](https://docs.coderabbit.ai/cli/cursor-integration)
- [Claude Code Integration](https://docs.coderabbit.ai/cli/claude-code-integration)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/references/cli-integration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

