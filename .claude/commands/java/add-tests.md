# Generate missing tests for code that lacks coverage

**Description:** Generate missing tests for code that lacks coverage

# Add Tests

Generate missing tests to improve code coverage.

## Skill Location

Full documentation: `.claude/skills/test-augmentation/`

## Quick Start

1. Run coverage report for your technology:
   - JavaScript/TypeScript: `pnpm run test:coverage`
   - Python: `pytest --cov=. --cov-report=html`
   - Java: `./gradlew jacocoTestReport`
   - Go: `go test -cover ./...`
2. Review uncovered files and functions
3. Apply patterns from the skill:
   - `.claude/skills/test-augmentation/SKILL.md` - Main instructions
   - `.claude/skills/test-augmentation/references/test-patterns.md` - Test patterns
4. Write tests for uncovered code paths
5. Re-run coverage to verify improvement

## Coverage Targets

| Type | Target |
|------|--------|
| Line coverage | 80%+ |
| Branch coverage | 70%+ |
| Critical paths | 100% |

## Related

- **Quality Gate**: `/quality-check` command
- **Doc Sync**: `/sync-docs` command
- **Java Testing**: `.claude/skills/java-testing/SKILL.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/add-tests.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
