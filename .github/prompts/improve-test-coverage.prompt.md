# Write tests to improve code coverage for Java projects

> Write tests to improve code coverage for Java projects

# 🤖 🧪 Improve Test Coverage

Write tests to improve code coverage to meet the 82% threshold.

## Skill Location

Full documentation: [java-coverage](.claude/skills/java-coverage/SKILL.md)

## Prerequisites

Run `/improve-test-setup` first to configure test infrastructure.

## Quick Start

1. Generate coverage reports: `./gradlew test jacocoTestReport`
2. Analyze HTML report at `build/reports/jacoco/test/html/`
3. Apply patterns from the skill:
   - [java-coverage](.claude/skills/java-coverage/SKILL.md) - Main instructions
   - [improvement-workflow](.claude/skills/java-coverage/references/improvement-workflow.md) - Step-by-step guide
   - [prioritization](.claude/skills/java-coverage/references/prioritization.md) - What to test first
   - [coverage-targets](.claude/skills/java-coverage/references/coverage-targets.md) - Target percentages
4. Iterate until 82% threshold met

## Key Actions

- Focus on business logic classes first
- Cover all branches (if/else, switch)
- Test edge cases and error handling
- Use data tables for parameterized tests

## Related

- **Test Setup**: `/improve-test-setup` command
- **Mutation Testing**: `/improve-test-quality-with-mutation-testing`
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/improve-test-coverage.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
