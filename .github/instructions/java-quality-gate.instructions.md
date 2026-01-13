---
applyTo: "**/*.ts,**/*.tsx,**/*.js,**/*.jsx,**/*.java,**/*.py,**/*.go"
description: Quality checks for code changes
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

Full procedure: `.skills/quality-gateway/`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/quality-gate.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
