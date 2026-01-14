---
description: Quality checks for code changes
alwaysApply: false
globs: **/*.ts,**/*.tsx,**/*.js,**/*.jsx,**/*.java,**/*.py,**/*.go
tags:
  - java
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

Verify quality checks pass and **save output to `.tmp/`** with context for reuse:

```bash
# Get context (PR number or branch)
CONTEXT=$(git rev-parse --abbrev-ref HEAD | tr '/' '-')
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

# Linting
npm run lint        # JavaScript/TypeScript
python -m flake8    # Python
./gradlew check     # Java

# Tests - ALWAYS save to .tmp/ with context to avoid re-running
pnpm test 2>&1 | tee ".tmp/pnpm-test-${CONTEXT}-${TIMESTAMP}.txt"
pytest 2>&1 | tee ".tmp/pytest-test-${CONTEXT}-${TIMESTAMP}.txt"
./gradlew test 2>&1 | tee ".tmp/gradle-test-${CONTEXT}-${TIMESTAMP}.txt"

# Then grep from file instead of running again
grep "FAIL" .tmp/pnpm-test-*.txt | tail -20

# Type checking
npx tsc --noEmit    # TypeScript
```

Files are named with PR/branch and timestamp (e.g., `.tmp/pnpm-test-pr83-20260113-181500.txt`).

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

Full procedure: `.agent-skills/quality-gateway/`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/quality-gate.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
