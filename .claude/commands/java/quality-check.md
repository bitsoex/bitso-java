# Run full quality gate checks on recent changes

**Description:** Run full quality gate checks on recent changes

# Quality Check

Run comprehensive quality gate checks on recent changes.

## Purpose

This command invokes the **quality-gateway skill** to run all quality checks:
- Linting and code style
- Test execution and coverage
- Type checking
- Documentation validation

## Skill Location

```
.skills/quality-gateway/
```

## Quick Workflow

### Step 1: Run Linting

```bash
# JavaScript/TypeScript
npm run lint

# Python
python -m flake8 .

# Java
./gradlew check
```

### Step 2: Run Tests

```bash
# JavaScript/TypeScript
npm test

# Python
pytest

# Java
./gradlew test
```

### Step 3: Check Types

```bash
# TypeScript
npx tsc --noEmit

# Python (with mypy)
mypy .
```

### Step 4: Verify Coverage

```bash
npm run test:coverage
```

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
| Type errors | Fix type issues in reported files |
| Coverage drop | Add tests with `/add-tests` |

## Related Commands

| Command | Purpose |
|---------|---------|
| `/add-tests` | Generate missing tests |
| `/sync-docs` | Update documentation |

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full quality gateway documentation |
| `assets/claude-quality-hooks.md` | Claude Code hook configurations |
| `assets/cursor-quality-integration.md` | Cursor IDE integration guide |

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/quality-check.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
