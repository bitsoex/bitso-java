# Commit Java 25 toolchain change after successful local validation with /prepare-to-java-25

> Commit Java 25 toolchain change after successful local validation with /prepare-to-java-25

# 🤖 📦 Upgrade to Java 25

> **📚 Full documentation**: See the [upgrade-java-25 skill](.claude/skills/upgrade-java-25/SKILL.md) for complete upgrade patterns and troubleshooting.

**IMPORTANT**: Run `/prepare-to-java-25` first to validate locally before using this command.

## Prerequisites

1. **`/prepare-to-java-25` completed** - All dependency upgrades done
2. **Local tests pass** - All tests pass with Java 25 toolchain
3. **Java 25 Temurin installed** - `sdk use java 25.0.1-tem`

## Workflow

### 1. Verify Preparation Complete

```bash
# Ensure using Java 25
sdk use java 25.0.1-tem

# Verify tests pass
./gradlew clean test 2>&1 | tail -20
```

### 2. Commit the Toolchain Change

After successful validation, commit the Java toolchain configuration:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
```

### 3. Create PR

Follow the Jira ticket workflow to create a proper PR with the Java 25 upgrade.

## Related

- [Full Skill](.claude/skills/upgrade-java-25/SKILL.md)
- [Preparation Command](.cursor/commands/prepare-to-java-25.md)
- [Golden Path](../golden-paths/java-25-upgrade.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-java-25.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
