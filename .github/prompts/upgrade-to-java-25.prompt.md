# Commit Java 25 toolchain change after successful local validation with /prepare-to-java-25

> Commit Java 25 toolchain change after successful local validation with /prepare-to-java-25

# 🤖 📦 Upgrade to Java 25

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking for confirmation.

This command commits the Java 25 toolchain change after successful local validation. It should be run **after** `/prepare-to-java-25` has been completed and all tests pass locally.

## Related Documents

- **Preparation Command**: `java/commands/prepare-to-java-25.md` - Must be run first
- **Golden Path**: `java/golden-paths/java-25-upgrade.md` - Detailed upgrade patterns
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Version catalog strategy

## Prerequisites

1. **`/prepare-to-java-25` completed** - All dependency upgrades done
2. **Local tests pass** - All tests pass with Java 25 toolchain
3. **Java 25 Temurin installed** - `sdk use java 25.0.1-tem`

## Workflow

### 1. Verify Preparation Complete

Ensure `/prepare-to-java-25` was run and tests pass:

```bash
# Ensure using Java 25
sdk use java 25.0.1-tem

# Verify tests pass
./gradlew clean test 2>&1 | tail -20
```

If tests fail, return to `/prepare-to-java-25` troubleshooting.

### 2. Create Jira Ticket (if not already created)

Search for existing tickets, create if none found:

- **Summary**: `🤖 📦 Upgrade [repo-name] to Java 25`
- **Parent**: Current Sprint/Cycle KTLO Epic

### 3. Create Feature Branch (if not already created)

```bash
git fetch --all && git pull origin main
git checkout -b "chore/${JIRA_KEY}-upgrade-java-25"
```

### 4. Verify Java 25 Toolchain Configuration

Ensure `build.gradle` has the toolchain configuration:

```groovy
allprojects {
    // Configure Java 25 toolchain for all Java projects
    plugins.withType(JavaPlugin).configureEach {
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
                vendor = JvmVendorSpec.ADOPTIUM
            }
        }
    }
}
```

### 5. Final Validation

```bash
# Clean build with tests
./gradlew clean build 2>&1 | tee /tmp/gradle-build-java25.log

# Verify no failures
grep -E "FAILED|BUILD FAILED" /tmp/gradle-build-java25.log && echo "❌ Build failed" || echo "✅ Build successful"
```

### 6. Commit and Push

```bash
git add -A
git commit -m "🤖 📦 chore(deps): [$JIRA_KEY] upgrade to Java 25

Upgrades:
- Gradle: 8.x -> 9.2.1
- Spock: 2.4-groovy-4.0 -> 2.4-groovy-5.0
- Lombok plugin: 8.x -> 9.1.0
- Spotless: 6.x -> 8.1.0
- SonarQube plugin: 6.x -> 7.2.1.6560
- Testcontainers: updated to latest
- Java toolchain: 21 -> 25

Key changes:
- Removed groovy-all dependency (use spock-core transitives)
- Added junit-platform-launcher for Gradle 9
- Updated all plugins for Gradle 9 compatibility

Generated with the Quality Agent by the /upgrade-to-java-25 command."

git push -u origin $(git branch --show-current)
```

### 7. Create PR

```bash
gh pr create --draft --title "🤖 📦 [$JIRA_KEY] chore(deps): upgrade to Java 25"
```

## Version Checklist

Verify these versions are in `gradle/libs.versions.toml`:

| Component | Version | Notes |
|-----------|---------|-------|
| Gradle | 9.2.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Groovy | 5.0.3 | Required for Java 25 bytecode |
| Spock | 2.4-groovy-5.0 | Matches Groovy 5.x |
| Lombok | 1.18.42 | Java 25 bytecode support |
| Lombok Plugin | 9.1.0 | Freefair plugin for Gradle 9 |
| Spotless | 8.1.0 | Gradle 9 compatibility |
| palantir-java-format | 2.74.0 | Java 25 bytecode support (use with Spotless) |
| SonarQube | 7.2.1.6560 | Gradle 9 compatibility |
| Testcontainers 1.x | 1.21.4 | Latest 1.x version |
| Testcontainers 2.x | 2.0.3 | Docker socket fix |
| ByteBuddy | 1.17.5+ | If defined - ASM 9.8 for Java 25 |
| Flyway Plugin | 11.19.0 | If defined - Gradle 9 compatibility |
| jOOQ Plugin | 10.1.1 | If defined - Gradle 9 compatibility |
| Protobuf Plugin | 0.9.5 | If defined - Gradle 9 compatibility |

## ⚠️ Critical Reminders

### Do NOT Use `groovy-all`

The `groovy-all:5.0.3` artifact has broken transitive dependencies. Use `spock-core` transitives instead:

```groovy
// ✅ CORRECT
testImplementation libs.spock.core

// ❌ NEVER
testImplementation libs.groovy.all
```

### All Versions in Version Catalog

Never hardcode versions. All versions must be in `gradle/libs.versions.toml`:

```groovy
// ❌ NEVER
testImplementation 'org.spockframework:spock-core:2.4-groovy-5.0'

// ✅ CORRECT
testImplementation libs.spock.core
```

### Add `groovy-json` for JsonSlurper/JsonOutput

If tests use `groovy.json.JsonSlurper` or `groovy.json.JsonOutput`:

```groovy
testImplementation libs.groovy.json
```

### Remove Hardcoded Java 21 Toolchain from Submodules

Check ALL submodules for hardcoded Java 21:

```bash
grep -r "JavaLanguageVersion.of(21)" --include="*.gradle" .
```

Remove any found - let root build.gradle set the toolchain.

### Use Dependency Graph for Troubleshooting

If issues arise, use dependency insight:

```bash
./gradlew :module:dependencyInsight --dependency groovy --configuration testCompileClasspath
```

## Rollback

If issues are found after merge:

1. Revert the toolchain change to Java 21
2. Keep Gradle 9.x and updated plugins (they work with Java 21)
3. Investigate root cause before re-attempting

```groovy
// Rollback toolchain
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // Rollback
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
```

## Related Commands

- `/prepare-to-java-25` - Prepare dependencies and validate locally (run first)
- `/upgrade-to-recommended-versions` - Upgrade to Spring Boot 3.5.x
- `/improve-test-setup` - Configure JaCoCo and testing libraries

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-java-25.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
