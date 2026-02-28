# Claude Code Quality Hooks

This document provides Claude Code hook configurations specific to the quality gateway.

## Contents

- [Configuration](#configuration)
- [Hook Scripts](#hook-scripts)
- [Setup Instructions](#setup-instructions)
- [Customization](#customization)
- [Related](#related)

---
## Configuration

Add to `.claude/settings.json`:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write|Edit|MultiEdit",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/quality-pre-edit.sh",
            "timeout": 30
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit|MultiEdit",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/quality-post-edit.sh",
            "timeout": 60
          }
        ]
      }
    ],
    "Stop": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/quality-gate.sh",
            "timeout": 120
          }
        ]
      }
    ]
  }
}
```

## Hook Scripts

### Pre-Edit Hook

`.claude/hooks/quality-pre-edit.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

# Capture baseline metrics
BASELINE_FILE=".claude/.quality-baseline.json"

# Baseline signal only (not % coverage): lines-hit for this file if lcov exists
LINES_HIT=0
if [[ -f "coverage/lcov.info" ]]; then
  LINES_HIT=$(grep -A 20 -F "SF:$FILE_PATH" coverage/lcov.info 2>/dev/null | grep "LH:" | cut -d: -f2 || echo "0")
fi

# Store baseline
echo "{\"file\": \"$FILE_PATH\", \"linesHit\": $LINES_HIT, \"timestamp\": \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}" > "$BASELINE_FILE"

exit 0
```

### Post-Edit Hook

`.claude/hooks/quality-post-edit.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

ISSUES=""

# Check 1: Run linter on the changed file
case "$FILE_PATH" in
  *.ts|*.tsx|*.js|*.jsx)
    if ! npx eslint "$FILE_PATH" --quiet 2>/dev/null; then
      ISSUES="${ISSUES}Linting errors in $FILE_PATH\n"
    fi
    ;;
  *.py)
    if ! python -m flake8 "$FILE_PATH" --quiet 2>/dev/null; then
      ISSUES="${ISSUES}Linting errors in $FILE_PATH\n"
    fi
    ;;
esac

# Check 2: Verify file is properly formatted
case "$FILE_PATH" in
  *.ts|*.tsx|*.js|*.jsx|*.json|*.md)
    if ! npx prettier --check "$FILE_PATH" 2>/dev/null; then
      # Auto-format instead of failing
      npx prettier --write "$FILE_PATH" 2>/dev/null || true
    fi
    ;;
esac

# Report issues to Claude (non-blocking)
if [[ -n "$ISSUES" ]]; then
  echo "Quality issues detected:"
  echo -e "$ISSUES"
fi

exit 0
```

### Quality Gate Hook (Stop)

`.claude/hooks/quality-gate.sh`:

```bash
#!/bin/bash
set -euo pipefail

INPUT=$(cat)
STOP_HOOK_ACTIVE=$(echo "$INPUT" | jq -r '.stop_hook_active')

# Prevent infinite loops
if [[ "$STOP_HOOK_ACTIVE" == "true" ]]; then
  exit 0
fi

cd "$CLAUDE_PROJECT_DIR"

ISSUES=""

# Check 1: Linting
if command -v npm &> /dev/null && [[ -f "package.json" ]]; then
  if npm run lint --if-present 2>&1 | grep -q "error"; then
    ISSUES="${ISSUES}- Linting errors detected\n"
  fi
fi

# Check 2: Tests
if command -v npm &> /dev/null && [[ -f "package.json" ]]; then
  if ! npm test --if-present 2>/dev/null; then
    ISSUES="${ISSUES}- Test failures detected\n"
  fi
fi

# Check 3: TypeScript compilation
if [[ -f "tsconfig.json" ]]; then
  if ! npx tsc --noEmit 2>/dev/null; then
    ISSUES="${ISSUES}- TypeScript errors detected\n"
  fi
fi

# Return result
if [[ -n "$ISSUES" ]]; then
  echo "{\"decision\":\"block\",\"reason\":\"Quality gate failed:\\n$ISSUES\\nPlease fix these issues before completing.\"}"
else
  echo "{}"
fi
```

## Setup Instructions

1. Create hooks directory:
   ```bash
   mkdir -p .claude/hooks
   ```

2. Copy hook scripts to `.claude/hooks/`

3. Make scripts executable:
   ```bash
   chmod +x .claude/hooks/*.sh
   ```

4. Add hooks configuration to `.claude/settings.json`

5. Test hooks by making a code change

## Customization

### Adjust Timeouts

Increase timeouts for slower CI environments:

```json
{
  "timeout": 120
}
```

### Add Custom Checks

Add project-specific quality checks to the hook scripts.

### Skip Certain Files

Add file filtering to skip generated files:

```bash
# Skip generated files
if [[ "$FILE_PATH" == *".generated."* ]]; then
  exit 0
fi
```

## Related

- General hook patterns: See `agent-hooks` skill
- Test coverage checks: See `test-augmentation` skill
- Documentation validation: See `doc-sync` skill
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/quality-checks/assets/claude-quality-hooks.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

