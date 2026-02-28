# Migrate from AWS SDK v1 (com.amazonaws) to AWS SDK v2 (software.amazon.awssdk) in Java/Gradle projects

> Migrate from AWS SDK v1 (com.amazonaws) to AWS SDK v2 (software.amazon.awssdk) in Java/Gradle projects

# 🤖 ☁️ Upgrade to AWS SDK v2

> **📚 Full documentation**: See the [upgrade-aws-sdk-v2 skill](.claude/skills/upgrade-aws-sdk-v2/SKILL.md) for complete migration patterns, code examples, and troubleshooting.

## Quick Reference

Migrate Java projects from AWS SDK v1 (`com.amazonaws`) to v2 (`software.amazon.awssdk`).

### Migration Strategy (Priority Order)

| Priority | Strategy | When to Use |
|----------|----------|-------------|
| 1 | **Update library** | A newer version uses v2 |
| 2 | **Update BOM** | v1 comes from Spring Boot BOM |
| 3 | **Dependency substitution** | Replace v1 with v2 artifact |
| 4 | **Direct code migration** | Only if no library update available |

### Key Commands

```bash
# Find v1 imports
grep -r "import com.amazonaws" --include="*.java" . | grep -v "/build/"

# Check dependency tree
./gradlew dependencies --configuration runtimeClasspath | grep -B5 "com.amazonaws"
```

### Dependency Substitution Pattern

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute module("com.amazonaws:aws-java-sdk-s3")
                using module("software.amazon.awssdk:s3:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
        }
    }
}
```

## Related

- [Full Skill](.claude/skills/upgrade-aws-sdk-v2/SKILL.md)
- [Migration Patterns](.claude/skills/upgrade-aws-sdk-v2/references/migration-patterns.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-aws-sdk-v2.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
