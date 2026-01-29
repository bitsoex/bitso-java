# Prepare Java projects for Java 25 compatibility (Gradle 9, Groovy 5, updated plugins) - LOCAL VALIDATION ONLY

> Prepare Java projects for Java 25 compatibility (Gradle 9, Groovy 5, updated plugins) - LOCAL VALIDATION ONLY

# 🤖 📦 Prepare to Java 25

> **📚 Full documentation**: See the [upgrade-java-25 skill](.claude/skills/upgrade-java-25/SKILL.md) for complete preparation patterns and troubleshooting.

**IMPORTANT**: This command is for **local validation only**. The Java 25 toolchain change should **NOT** be committed.

## Workflow

1. `/prepare-to-java-25` - Upgrade dependencies, validate locally with Java 25 toolchain
2. `/upgrade-to-java-25` - After validation passes, commit the toolchain change

## Target Versions for Java 25

| Component | Version | Notes |
|-----------|---------|-------|
| **Java** | **25** | Target JDK version |
| **Gradle** | **9.2.1** | Major version bump required |
| **Groovy** | **5.0.3** | Java 25 requires Groovy 5.x |
| **Spock** | **2.4-groovy-5.0** | Must match Groovy version |
| **Lombok** | **1.18.42** | Required for Java 25 bytecode |

## Prerequisites

1. **Spring Boot 3.5.x** - Project should already be on Spring Boot 3.5.x
2. **sdkman** - For managing Java versions
3. **Java 25 Temurin** - Install via sdkman: `sdk install java 25.0.1-tem`

## Quick Commands

```bash
# Install Java 25
sdk install java 25.0.1-tem
sdk use java 25.0.1-tem

# Upgrade Gradle wrapper
./gradlew wrapper --gradle-version 9.2.1

# Test locally
./gradlew clean test
```

## Related

- [Full Skill](.claude/skills/upgrade-java-25/SKILL.md)
- [Preparation Guide](.claude/skills/upgrade-java-25/references/preparation.md)
- [Golden Path](../golden-paths/java-25-upgrade.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/prepare-to-java-25.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
