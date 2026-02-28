# Run full quality gate checks on recent changes

> Run full quality gate checks on recent changes

# Quality Check

Run comprehensive quality gate checks on recent changes.

## Skill Location

Full documentation: [quality-checks](.claude/skills/quality-checks/SKILL.md)

## Quick Start

1. Run quality checks for your technology:
   - Linting: `npm run lint` / `./gradlew check` / `flake8`
   - Tests: `npm test` / `./gradlew test` / `pytest`
   - Types: `npx tsc --noEmit` / `mypy`
2. Save test output to `.tmp/` (don't run tests repeatedly)
3. Apply fixes from skill references:
   - [quality-checks](.claude/skills/quality-checks/SKILL.md) - Main instructions
   - [quality-checks/references/](.claude/skills/quality-checks/references) - Caching and other references
4. Verify coverage maintained or improved

## Expected Results

| Check | Expected |
|-------|----------|
| Linting | No errors |
| Tests | All pass |
| Types | No errors |
| Coverage | Maintained or improved |

## If Checks Fail

| Issue | Solution |
|-------|----------|
| Linting errors | Run `npm run lint:fix` or fix manually |
| Test failures | Fix failing tests |
| Coverage drop | Add tests with `/add-tests` |

## Related

- **Add Tests**: `/add-tests` command
- **Doc Sync**: `/sync-docs` command
- **Test Augmentation**: [testing-standards](.claude/skills/testing-standards/SKILL.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/quality-check.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
