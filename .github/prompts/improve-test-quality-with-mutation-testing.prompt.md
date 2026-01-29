# Use PIT mutation testing to identify weak tests and improve quality

> Use PIT mutation testing to identify weak tests and improve quality

# Improve Test Quality with Mutation Testing

Use PIT mutation testing to identify tests that pass but don't verify behavior.

## Skill Location

```
java/skills/mutation-testing/
```

## Quick Start

1. **Check readiness**: `bash java/scripts/check-pitest-readiness.sh`
2. **Apply PIT config**: Copy `java/templates/pitest.gradle` to `gradle/`
3. **Run mutation testing**: `./gradlew pitest -Ppitest.mutators=STRONGER`
4. **Analyze results**: View report at `build/reports/pitest/index.html` (use `open` on macOS or `xdg-open` on Linux)
5. **Fix surviving mutants**: Add specific assertions

## Intensity Levels

| Level | Command | Use Case |
|-------|---------|----------|
| QUICK | `-Ppitest.mutators=DEFAULTS` | Initial analysis |
| STANDARD | `-Ppitest.mutators=STRONGER` | Regular use |
| COMPREHENSIVE | `-Ppitest.mutators=ALL` | Critical logic |

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full workflow and concepts |
| `references/intensity-levels.md` | Detailed mutator descriptions |
| `references/improving-tests.md` | Patterns for killing mutants |
| `references/configuration.md` | PIT configuration options |
| `references/troubleshooting.md` | Common issues and solutions |

## Related

- `java/rules/java-testing-guidelines.md` - Test patterns
- `java/templates/pitest.gradle` - Gradle template
- `java/scripts/check-pitest-readiness.sh` - Readiness check

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/improve-test-quality-with-mutation-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
