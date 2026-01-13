---
description: Coderabbit Setup
alwaysApply: true
tags:
  - java
---

# CodeRabbit MCP Setup

## Overview

CodeRabbit CLI integrates code review capabilities into your development workflow. It analyzes code changes and identifies issues like race conditions, memory leaks, security vulnerabilities, and logic errors.

**Key integration points:**

- **Claude Code**: Autonomous AI workflows using CodeRabbit + Claude
- **Cursor**: Agentic development loop with automatic fixes
- **Terminal CLI**: Direct code review in your shell

## Prerequisites

### System Requirements

- **macOS**
- **Git repository** initialized in your project
- **Shell access** (zsh or bash)

### Language-Specific Requirements

#### JavaScript/TypeScript Projects

- **Node.js** (v14+): `node --version`
- **npm** (v6+): `npm --version`

#### Java Projects

- **Java Development Kit** (JDK 21+): `java -version`
- **Gradle** (8.14.3+): `gradle --version`

#### Python Projects

- **Python 3.8+**: `python3 --version`

## Installation

### Step 1: Install CodeRabbit CLI

Install CodeRabbit CLI globally on your system:

```bash
curl -fsSL https://cli.coderabbit.ai/install.sh | sh
```

### Step 2: Restart Your Shell

After installation, restart your shell:

```bash
source ~/.zshrc
```

Verify installation:

```bash
coderabbit --version
which coderabbit
```

### Step 3: Authentication

Authenticate with GitHub:

```bash
coderabbit auth login
```

**What happens:**

1. CodeRabbit opens a browser window
2. You log in with your GitHub account
3. CodeRabbit generates an authentication token
4. Copy the token and paste it back into your terminal

### Step 4: Verify Authentication

Confirm setup was successful:

```bash
coderabbit auth status
```

### Step 5: Run Readiness Check

Verify your complete setup:

```bash
bash global/scripts/check-coderabbit-readiness.sh
```

This validates:

- ✓ CodeRabbit CLI installed and functional
- ✓ Authentication configured and valid
- ✓ Git available and repository initialized
- ✓ Language-specific dependencies

## Configuration

CodeRabbit stores authentication in `~/.coderabbit/config.json`. You typically don't need to edit this manually - use `coderabbit auth` commands instead.

## Common Commands

### Review Code Changes

```bash
# Review uncommitted changes
coderabbit --prompt-only --type uncommitted

# Review all changes (committed + uncommitted)
coderabbit --prompt-only

# Review against specific base branch
coderabbit --prompt-only --base develop

# Review specific files
coderabbit --prompt-only --include "src/**/*.ts"
```

### Authentication Management

```bash
# Check authentication status
coderabbit auth status

# Re-authenticate
coderabbit auth login

# Logout
coderabbit auth logout
```

### Help

```bash
# Show all commands
coderabbit --help
```

## Integration with AI Tools

### Claude Code Integration

CodeRabbit works seamlessly with Claude Code:

```markdown
Tell Claude Code: "Run coderabbit --prompt-only --type uncommitted, 
let it run in the background, and fix any issues it finds"
```

Claude Code will:

1. Execute CodeRabbit in the background
2. Parse the `--prompt-only` output
3. Create a task list of issues
4. Automatically implement fixes

**Setup required:**

- CodeRabbit CLI installed globally
- Authentication configured
- Claude Code can execute terminal commands

### Cursor Integration

CodeRabbit integrates with Cursor for AI-driven development:

**Cursor Rule (recommended):**

```markdown
# Running the CodeRabbit CLI

CodeRabbit is already installed in the terminal. Run it as a way to review your code. 
Run the command: cr -h for details on commands available. 

In general, I want you to run coderabbit with the `--prompt-only` flag. 
To review uncommitted changes, run: `coderabbit --prompt-only -t uncommitted`.

IMPORTANT: When running CodeRabbit to review code changes, don't run it more 
than 3 times in a given set of changes.
```

## Troubleshooting

### "CodeRabbit not found" after installation

```bash
source ~/.zshrc
coderabbit --version
```

### "Not authenticated" error

```bash
coderabbit auth login
coderabbit auth status
```

### CodeRabbit takes too long

```bash
# Review only uncommitted changes (faster)
coderabbit --prompt-only --type uncommitted

# Review specific files only
coderabbit --prompt-only --include "src/new-feature/**/*.ts"
```

### CodeRabbit not finding issues

1. Verify you have changes:

   ```bash
   git status
   ```

2. Check authentication:

   ```bash
   coderabbit auth status
   ```

3. Try different scope:

   ```bash
   coderabbit --prompt-only --type uncommitted
   coderabbit --prompt-only --base develop
   ```

## Best Practices

1. **Create feature branch** before making changes
2. **Commit changes** to track them
3. **Run CodeRabbit** - let it analyze (7-30 minutes)
4. **Review output** - prioritize by severity
5. **Fix in batches** - address 3-5 issues at a time
6. **Run tests** - verify fixes locally
7. **Commit and push** - push fixed changes

## Related Resources

- **Command Reference**: `global/commands/fix-coderabbit-issues.md`
- **Readiness Check**: `global/scripts/check-coderabbit-readiness.sh`
- **Official Docs**: <https://docs.coderabbit.ai/cli/overview>

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/coderabbit-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
