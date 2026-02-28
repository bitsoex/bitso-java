# Identify and remove unused dependencies using Gradle Lint Plugin

> Identify and remove unused dependencies using Gradle Lint Plugin

# 🤖 📦 Gradle Dependencies Cleanup

Identify and remove unused dependencies in Gradle projects.

## Skill Location

Full documentation: [gradle-standards](.claude/skills/gradle-standards/SKILL.md)

## Quick Start

1. Choose analysis method (Gradle Lint preferred)
2. Apply patterns from skill references:
   - `references/cleanup-workflow.md` - Step-by-step guide
   - `references/unused-detection.md` - Detection methods
   - `references/optimization.md` - Build optimization
3. Review findings before removing
4. Validate build and tests

## Key Actions

- Integrate Nebula Gradle Lint Plugin
- Run `./gradlew lintGradle` for analysis
- Review false positives (starters, BOMs, annotation processors)
- Replace hardcoded versions with catalog references
- Verify build works after changes

## Related

- **Lint Plugin**: `java/rules/java-gradle-lint-plugin.md`
- **Version Management**: `java/rules/java-versions-and-dependencies.md`
- **Best Practices**: `java/rules/java-gradle-best-practices.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/gradle-dependencies-cleanup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
