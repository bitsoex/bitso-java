# Prepare Java projects for Gradle 9 compatibility - address deprecations and update plugins

> Prepare Java projects for Gradle 9 compatibility - address deprecations and update plugins

# 🤖 🔧 Prepare to Gradle 9

> **📚 Full documentation**: See the [upgrade-gradle-9 skill](.claude/skills/upgrade-gradle-9/SKILL.md) for complete upgrade patterns, plugin compatibility, and troubleshooting.

**IMPORTANT**: This command prepares projects for Gradle 9 by addressing deprecations and updating plugins.

## Key Changes in Gradle 9

| Change | Impact | Action |
|--------|--------|--------|
| Groovy 5.x | Spock compatibility | Use `spock-bom:2.4-groovy-5.0` |
| Plugin API changes | Build script updates | Update build plugins |
| Deprecated APIs removed | Build failures | Fix deprecation warnings first |

## Quick Preparation Steps

```bash
# Run deprecation check
./gradlew help --warning-mode all 2>&1 | grep -i deprec

# Upgrade wrapper to Gradle 9
./gradlew wrapper --gradle-version 9.2.1

# Test build
./gradlew clean test
```

## Plugin Compatibility

| Plugin | Minimum Version | Notes |
|--------|----------------|-------|
| Lombok Plugin | **9.2.0** | For Gradle 9.2.1 |
| Spotless | 8.1.0 | Updated for Gradle 9 |
| SonarQube | 7.2.2.6593 | Updated for Gradle 9 |
| Protobuf Plugin | **0.9.6** | Gradle 9 compatible |
| Spring Dependency Management | 1.1.7 | Required for Spring Boot 3.5.x |

## Related

- [Full Skill](.claude/skills/upgrade-gradle-9/SKILL.md)
- [Plugin Compatibility](.claude/skills/upgrade-gradle-9/references/plugin-compatibility.md)
- [Java 25 Skill](.claude/skills/upgrade-java-25/SKILL.md) - Often done together

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/prepare-to-gradle-9.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
