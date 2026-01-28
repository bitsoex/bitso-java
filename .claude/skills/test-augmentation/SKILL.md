---
name: test-augmentation
description: >
  Validates and improves test coverage for code changes. Checks coverage thresholds,
  identifies untested code paths, and provides guidance on writing effective tests.
  Use after making code changes or when improving test quality.
compatibility: Works with Java (JaCoCo), JavaScript/TypeScript (Jest/NYC), Python (coverage.py), Go (go test)
metadata:
  version: "2.0.0"
  technology: global
---

# Test Augmentation

Generate missing tests to improve code coverage.

## When to use this skill

- After implementing new features
- When refactoring existing code
- To identify gaps in test coverage
- During test quality improvements
- When asked to "add tests" or "improve coverage"

## Skill Contents

### Available Resources

**references/** - Detailed documentation
- [go](references/go)
- [java](references/java)
- [python](references/python)
- [test patterns](references/test-patterns.md)
- [typescript](references/typescript)

---

## Quick Start

1. Run coverage report for your technology:
   - JavaScript/TypeScript: `pnpm run test:coverage`
   - Python: `pytest --cov=. --cov-report=html`
   - Java: `./gradlew jacocoTestReport`
   - Go: `go test -cover ./...`
2. Review uncovered files and functions
3. Apply patterns from the references:
   - `references/test-patterns.md` - General test patterns
   - `references/{technology}/test-patterns.md` - Technology-specific patterns
4. Write tests for uncovered code paths
5. Re-run coverage to verify improvement

## Coverage Targets

| Type | Target |
|------|--------|
| Line coverage | 80%+ |
| Branch coverage | 70%+ |
| Critical paths | 100% |

## Coverage Tools

| Technology | Tool |
|------------|------|
| Java | JaCoCo |
| JavaScript/TypeScript | Jest, NYC |
| Python | coverage.py, pytest-cov |
| Go | go test -cover |

## References

| Technology | Reference |
|------------|-----------|
| Java | `references/java/test-patterns.md` |
| TypeScript | `references/typescript/test-patterns.md` |
| Python | `references/python/test-patterns.md` |
| Go | `references/go/test-patterns.md` |

## Related Skills

- [quality-gateway](../quality-gateway/SKILL.md) - Quality gate orchestration
- [doc-sync](../doc-sync/SKILL.md) - Documentation synchronization
- `java-testing` - Java-specific testing (for Java projects)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/test-augmentation/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

