# Cursor IDE Quality Integration

This document describes how to integrate the quality gateway with Cursor IDE using **native hooks** (preferred) and **rules + commands** (supplementary).

## Contents

- [Overview](#overview)
- [Native Hooks Configuration](#native-hooks-configuration)
- [Quality Gate Rule (Supplementary)](#quality-gate-rule-supplementary)
- [Quality Commands](#quality-commands)
- [Setup Instructions](#setup-instructions)
- [Cursor Hook Events Reference](#cursor-hook-events-reference)
- [Related](#related)

---
## Overview

Cursor now supports native hooks similar to Claude Code. The quality gateway integrates via:

1. **Native Hooks** (Primary) - Automated quality checks at lifecycle points
2. **Rules** (Supplementary) - Passive guidance during edits
3. **Commands** (User-invoked) - On-demand quality checks

---

## Native Hooks Configuration

Create `.cursor/hooks.json`:

```json
{
  "version": 1,
  "hooks": {
    "afterFileEdit": [
      {
        "command": "./.cursor/hooks/quality-post-edit.sh"
      }
    ],
    "beforeShellExecution": [
      {
        "command": "./.cursor/hooks/validate-shell.sh"
      }
    ],
    "stop": [
      {
        "command": "./.cursor/hooks/quality-gate.sh"
      }
    ]
  }
}
```

### Hook Scripts

Copy scripts from `targeted/skills/agent-hooks/scripts/` or create:

#### Post-Edit Quality Check

`.cursor/hooks/quality-post-edit.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.file_path // empty')

if [[ -z "$FILE_PATH" ]]; then
  echo '{}'
  exit 0
fi

# Validate path is relative and within repo (prevent path traversal)
if [[ "$FILE_PATH" = /* ]] || [[ "$FILE_PATH" = *../* ]]; then
  echo '{}' # Invalid path, skip silently
  exit 0
fi

# Ensure file exists
if [[ ! -f "$FILE_PATH" ]]; then
  echo '{}'
  exit 0
fi

# Skip generated/vendor files
if [[ "$FILE_PATH" == *"/node_modules/"* ]] || [[ "$FILE_PATH" == *"/dist/"* ]]; then
  echo '{}'
  exit 0
fi

# Auto-format
case "$FILE_PATH" in
  *.ts|*.tsx|*.js|*.jsx|*.json|*.md)
    npx prettier --write "$FILE_PATH" 2>/dev/null || true
    ;;
  *.py)
    black "$FILE_PATH" --quiet 2>/dev/null || true
    ;;
  *.go)
    gofmt -w "$FILE_PATH" 2>/dev/null || true
    ;;
esac

# Run linter (non-blocking)
case "$FILE_PATH" in
  *.ts|*.tsx|*.js|*.jsx)
    if npx eslint "$FILE_PATH" 2>&1 | grep -q "error"; then
      echo "Linting issues in $FILE_PATH" >&2
    fi
    ;;
esac

echo '{}'
```

#### Shell Command Validation

`.cursor/hooks/validate-shell.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.command // empty')

# Block dangerous patterns (basic guard - comprehensive shell validation is complex)
# Consider sandboxing or allowlist approach for production use
DANGEROUS_PATTERNS=(
  'rm[[:space:]]+-[rf][rf][[:space:]]+/'         # rm -rf / or rm -fr /
  'rm[[:space:]]+-[rf][rf][[:space:]]+/\*'       # rm -rf /*
  '>[[:space:]]*/'                                # redirect to root
  'dd[[:space:]]+.*of=/dev/(sd|hd|nvme)'         # dd to block device
  'chmod[[:space:]]+-R[[:space:]]+777'           # dangerous permissions
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if [[ "$COMMAND" =~ $pattern ]]; then
    cat << 'EOF'
{
  "permission": "deny",
  "user_message": "Blocked: Potentially dangerous command",
  "agent_message": "This command matches a dangerous pattern and was blocked."
}
EOF
    exit 0
  fi
done

echo '{"permission": "allow"}'
```

#### Quality Gate (Stop Hook)

`.cursor/hooks/quality-gate.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
LOOP_COUNT=$(echo "$INPUT" | jq -r '.loop_count // 0')

# Prevent infinite loops
if [[ "$LOOP_COUNT" -ge 3 ]]; then
  echo '{}'
  exit 0
fi

ISSUES=""

# Run tests
if [[ -f "package.json" ]]; then
  if ! npm test --if-present; then
    ISSUES="${ISSUES}- Test failures detected\n"
  fi
fi

# Run linting (check exit code, not grep for "error")
if [[ -f "package.json" ]]; then
  if ! npm run lint --if-present; then
    ISSUES="${ISSUES}- Linting errors detected\n"
  fi
fi

# TypeScript check
if [[ -f "tsconfig.json" ]]; then
  if ! npx tsc --noEmit; then
    ISSUES="${ISSUES}- TypeScript errors\n"
  fi
fi

if [[ -n "$ISSUES" ]]; then
  # Use jq for safe JSON construction to handle special characters
  jq -n --arg issues "$ISSUES" '{followup_message: ("Quality gate failed. Please fix:\n" + $issues)}'
else
  echo '{"decision": "allow"}'
fi
```

### Setup

```bash
# Create hooks directory
mkdir -p .cursor/hooks

# Copy scripts
cp targeted/skills/agent-hooks/scripts/quality-post-edit.sh .cursor/hooks/
cp targeted/skills/agent-hooks/scripts/validate-shell.sh .cursor/hooks/
cp targeted/skills/agent-hooks/scripts/quality-gate.sh .cursor/hooks/

# Make executable
chmod +x .cursor/hooks/*.sh

# Restart Cursor to load hooks
```

---

## Quality Gate Rule (Supplementary)

Even with hooks, a rule provides additional guidance. Create `.cursor/rules/quality-gate/quality-gate.mdc`:

```yaml
---
description: Quality checks for code changes
alwaysApply: false
globs:
  - "**/*.ts"
  - "**/*.tsx"
  - "**/*.js"
  - "**/*.jsx"
  - "**/*.java"
  - "**/*.py"
  - "**/*.go"
tags:
  - quality
---

# Quality Gate

When editing code files, ensure quality standards are maintained.

## Before Changes

- Note existing test coverage for affected code
- Check for existing linting errors
- Review related documentation

## During Changes

- Follow project coding standards
- Write or update tests for changes
- Keep functions small and focused

## After Changes

Verify quality checks pass:

```bash
# Linting
npm run lint        # JavaScript/TypeScript
python -m flake8    # Python
./gradlew check     # Java

# Tests
npm test            # JavaScript/TypeScript
pytest              # Python
./gradlew test      # Java

# Type checking
npx tsc --noEmit    # TypeScript
```

## Quality Thresholds

- Test coverage: Maintain or improve
- No new linting errors
- All existing tests pass
- Documentation updated for API changes

## Commands

For comprehensive checks:
- `/quality-check` - Run full quality gate
- `/add-tests` - Generate missing tests
- `/sync-docs` - Update documentation

## Related

Full procedure: `.claude/skills/quality-checks/`
```

---

## Quality Commands

### /quality-check

Create `.claude/commands/quality-check.md`:

```markdown
---
description: Run full quality gate checks on recent changes
---

# Quality Check

Run comprehensive quality gate checks on recent changes.

## What This Does

1. **Linting** - Check for code style issues
2. **Tests** - Run test suite and verify coverage
3. **Type Checking** - Verify TypeScript/type errors
4. **Documentation** - Check docs are in sync

## How to Run

Execute these commands in order:

### Step 1: Linting

```bash
npm run lint
```

### Step 2: Tests

```bash
npm test
```

### Step 3: Type Checking

```bash
npx tsc --noEmit
```

### Step 4: Coverage Report

```bash
npm run test:coverage
```

## Expected Results

- All lint checks pass
- All tests pass
- No type errors
- Coverage maintained or improved

## If Checks Fail

1. **Linting errors**: Run `npm run lint:fix` to auto-fix
2. **Test failures**: Investigate and fix failing tests
3. **Type errors**: Fix type issues in reported files
4. **Coverage drop**: Add tests for uncovered code

## Related

- Full skill: `.claude/skills/quality-checks/`
- Test generation: `/add-tests`
- Documentation sync: `/sync-docs`
```

### /add-tests

Create `.claude/commands/add-tests.md`:

```markdown
---
description: Generate missing tests for recent changes
---

# Add Tests

Generate missing tests for code that lacks coverage.

## What This Does

Uses the `testing-standards` skill to:
1. Identify untested code paths
2. Generate appropriate test cases
3. Maintain or improve coverage

## How to Use

1. Review the coverage report:
   ```bash
   npm run test:coverage
   ```

2. Identify files with low coverage

3. For each file, generate tests following patterns in:
   `.claude/skills/testing-standards/`

## Test Patterns

### Unit Tests

- Test public functions and methods
- Cover happy path and edge cases
- Mock external dependencies

### Integration Tests

- Test component interactions
- Verify API contracts
- Test with real dependencies where safe

## Related

- Full skill: `.claude/skills/testing-standards/`
- Quality check: `/quality-check`
```

### /sync-docs

Create `.claude/commands/sync-docs.md`:

```markdown
---
description: Update documentation to match code changes
---

# Sync Docs

Update documentation to reflect recent code changes.

## What This Does

Uses the `doc-validation-rfc-37` skill to:
1. Identify documentation gaps
2. Update API documentation
3. Sync README with current state

## How to Use

1. Review what changed:
   ```bash
   git diff --name-only HEAD~5
   ```

2. For each changed file, check:
   - Are there JSDoc/docstrings?
   - Is the README accurate?
   - Are API docs current?

3. Update documentation following patterns in:
   `.claude/skills/doc-validation-rfc-37/`

## Documentation Checklist

- [ ] Public API functions have docstrings/JSDoc
- [ ] README reflects current functionality
- [ ] Configuration options are documented
- [ ] Breaking changes are noted

## Related

- Full skill: `.claude/skills/doc-validation-rfc-37/`
- Quality check: `/quality-check`
```

---

## Setup Instructions

### Option 1: Full Setup (Hooks + Rules + Commands)

```bash
# 1. Create directories
mkdir -p .cursor/hooks .cursor/rules .claude/commands

# 2. Copy hook scripts
cp targeted/skills/agent-hooks/scripts/{quality-post-edit,validate-shell,quality-gate}.sh .cursor/hooks/
chmod +x .cursor/hooks/*.sh

# 3. Create hooks.json
cat > .cursor/hooks.json << 'EOF'
{
  "version": 1,
  "hooks": {
    "afterFileEdit": [{ "command": "./.cursor/hooks/quality-post-edit.sh" }],
    "beforeShellExecution": [{ "command": "./.cursor/hooks/validate-shell.sh" }],
    "stop": [{ "command": "./.cursor/hooks/quality-gate.sh" }]
  }
}
EOF

# 4. Copy rules and commands (from this file's examples)
# Create quality-gate/quality-gate.mdc, quality-check.md, add-tests.md, sync-docs.md

# 5. Restart Cursor
```

### Option 2: Commands Only (Lightweight)

If you prefer manual control without automated hooks:

```bash
mkdir -p .claude/commands
# Create quality-check.md, add-tests.md, sync-docs.md in .claude/commands/
```

---

## Cursor Hook Events Reference

| Event | When | Use Case |
|-------|------|----------|
| `afterFileEdit` | After agent edits file | Formatting, linting |
| `beforeShellExecution` | Before shell command | Block dangerous commands |
| `afterShellExecution` | After shell command | Audit logging |
| `beforeMCPExecution` | Before MCP tool | Governance |
| `afterMCPExecution` | After MCP tool | Logging |
| `beforeSubmitPrompt` | Before prompt | PII detection |
| `stop` | Agent completion | Quality gate |

---

## Related

- General hook patterns: See `agent-hooks` skill
- Claude Code integration: See `claude-quality-hooks.md`
- Testing checklist: See `agent-hooks/references/testing-checklist.md`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/quality-checks/assets/cursor-quality-integration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

