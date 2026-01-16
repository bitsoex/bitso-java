<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-25/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: java-25
description: >
  Upgrade Java projects from Java 21 to Java 25 with required dependency updates.
  Requires Gradle 9.x, Groovy 5.x, and updated plugins for compatibility.
compatibility: Java projects using Gradle with Java 21 or earlier
metadata:
  version: "1.0.0"
  technology: java
  category: modernization
  tags:
    - java
    - java-25
    - gradle-9
    - groovy
---

# Java 25 Upgrade

Upgrade Java projects to Java 25 with all required dependency updates.

## When to Use

- Project needs to upgrade from Java 21 to Java 25
- Need features from Java 25 (records, pattern matching improvements, etc.)
- Planning to stay on latest LTS-adjacent releases

## Prerequisites

1. **Spring Boot 3.5.x** - Upgrade to Spring Boot 3.5.x first
2. **sdkman** - For managing Java versions
3. **Java 25 Temurin** - Install via: `sdk install java 25.0.1-tem`

## Target Versions

| Component | Version | Notes |
|-----------|---------|-------|
| **Java** | **25** | Target JDK |
| **Gradle** | **9.2.1** | Major version required |
| **Groovy** | **5.0.3** | Java 25 requires Groovy 5.x |
| **Spock** | **2.4-groovy-5.0** | Must match Groovy version |
| **Lombok** | **1.18.42** | Java 25 bytecode support |
| **Lombok Plugin** | **9.1.0** | Freefair plugin for Gradle 9 |
| **Spotless** | **8.1.0** | Gradle 9 compatible |
| **SonarQube Plugin** | **7.2.2.6593** | Gradle 9 compatible |
| **ByteBuddy** | **1.17.5+** | ASM 9.8 for Java 25 |

## ⚠️ DO NOT Upgrade

- **protobuf**: Keep existing version
- **grpc**: Keep existing version
- **Spring Boot**: Keep at 3.5.x (already upgraded)

## Workflow

### Phase 1: Prepare (Local Validation)

```bash
# Install and use Java 25
sdk install java 25.0.1-tem
sdk use java 25.0.1-tem

# Upgrade Gradle wrapper
./gradlew wrapper --gradle-version=9.2.1

# Update version catalog (see below)
# Build and test locally
./gradlew clean build test
```

### Phase 2: Commit (After Tests Pass)

Only commit the toolchain change after all tests pass locally.

## Version Catalog Updates

```toml
# gradle/libs.versions.toml
[versions]
groovy = "5.0.3"
spock = "2.4-groovy-5.0"
lombok = "1.18.42"
bytebuddy = "1.17.5"  # If defined

[plugins]
lombok = "io.freefair.lombok:9.1.0"
spotless = "com.diffplug.spotless:8.1.0"
sonarqube = "org.sonarqube:7.2.2.6593"
```

## Java Toolchain Configuration

```groovy
// root build.gradle
allprojects {
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

## ⚠️ Critical: Do NOT Use groovy-all

The `groovy-all:5.0.3` artifact has broken transitive dependencies:

```groovy
// ❌ NEVER: groovy-all has broken transitive deps
testImplementation 'org.apache.groovy:groovy-all:5.0.3'

// ✅ CORRECT: Let spock-core bring in Groovy 5.0.3 transitively
testImplementation libs.spock.core
testImplementation libs.spock.spring  // if using Spring
```

## Override Spring Boot's Groovy Version

```groovy
// root build.gradle
allprojects {
    ext['groovy.version'] = libs.versions.groovy.get()
}
```

## References

| Reference | Content |
|-----------|---------|
| [references/preparation.md](references/preparation.md) | Detailed preparation steps and prerequisites |

## Related Commands

This skill is referenced by:

- [`/prepare-to-java-25`](../../commands/prepare-to-java-25.md) - Local validation and preparation
- [`/upgrade-to-java-25`](../../commands/upgrade-to-java-25.md) - Commit toolchain change

## Related Skills

- `spring-boot-3-5` - Must upgrade first
- `gradle-9` - Gradle 9 upgrade patterns
