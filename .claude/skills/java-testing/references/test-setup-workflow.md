---
title: Test Setup Workflow
description: Step-by-step guide for upgrading testing libraries and configuring test infrastructure
---

# Test Setup Workflow

Step-by-step guide for upgrading testing libraries and configuring JaCoCo for Java projects.

## Contents

- [Prerequisites](#prerequisites)
- [Target Testing Library Versions](#target-testing-library-versions)
- [Workflow](#workflow)
- [Related](#related)

---
## Prerequisites

1. **Gradle 8.14+** (Java 21) or **Gradle 9.2.1+** (Java 25)
2. **Java 21 or Java 25** project
3. **Access to `gradle/libs.versions.toml`** - Version catalog must exist

## Target Testing Library Versions

### Java 21 Projects (Gradle 8.x)

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | `2.4-groovy-4.0` | Stable release |
| **JUnit Jupiter** | `5.14.1` | Released Oct 2025 |
| **JUnit Platform Launcher** | `1.14.1` | Released Oct 2025 |
| **JaCoCo** | `0.8.14` | Released Oct 2025 |
| **SonarQube Gradle Plugin** | `7.2.2.6593` | Released Dec 2025 |
| **Pitest** | `1.22.0` | Core library |
| **Pitest Gradle Plugin** | `1.19.0-rc.2` | Released Oct 2025 |
| **Testcontainers** | `1.21.4` | Stable 1.x |
| **Groovy** | `4.0.29` | Required for Spock 2.4-groovy-4.0 |

### Java 25 Projects (Gradle 9.x)

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | `2.4-groovy-5.0` | Must match Groovy 5.x |
| **JUnit Jupiter** | `5.14.1` | Released Oct 2025 |
| **JUnit Platform Launcher** | `1.14.1` | **Required** for Gradle 9 |
| **JaCoCo** | `0.8.14` | Released Oct 2025 |
| **SonarQube Gradle Plugin** | `7.2.2.6593` | Gradle 9 compatible |
| **Testcontainers** | `1.21.4` | Stable 1.x |
| **Groovy** | `5.0.3` | Required for Java 25 bytecode |
| **Lombok** | `1.18.42` | Required for Java 25 bytecode |

> **⚠️ CRITICAL for Java 25**: Never use `groovy-all` - rely on spock-core transitives.

## Workflow

### 1. Update Version Catalog

Update `gradle/libs.versions.toml`:

```toml
[versions]
# Testing Libraries
spock = "2.4-groovy-4.0"
junit-jupiter = "5.14.1"
junit-platform = "1.14.1"
jacoco = "0.8.14"
testcontainers = "1.21.4"
groovy = "4.0.29"

# Mutation Testing
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

# Code Quality
sonar-plugin = "7.2.2.6593"

[libraries]
# JUnit BOM
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }

# Spock
spock-core = { module = "org.spockframework:spock-core", version.ref = "spock" }
spock-spring = { module = "org.spockframework:spock-spring", version.ref = "spock" }

# Testcontainers BOM
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
sonarqube = { id = "org.sonarqube", version.ref = "sonar-plugin" }

[bundles]
testing-spock = ["spock-core", "spock-spring"]
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]
```

### 2. Configure JUnit Version Alignment

Add resolution strategy to prevent version conflicts:

```groovy
// In root build.gradle
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        configurations.configureEach {
            resolutionStrategy.eachDependency { details ->
                if (details.requested.group == 'org.junit.jupiter') {
                    details.useVersion libs.versions.junit.jupiter.get()
                }
                if (details.requested.group == 'org.junit.platform') {
                    details.useVersion libs.versions.junit.platform.get()
                }
            }
        }

        dependencies {
            testRuntimeOnly libs.junit.platform.launcher
        }
    }
}
```

### 3. Configure JaCoCo

Create `gradle/jacoco.gradle`:

```groovy
apply plugin: 'jacoco'

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

test {
    finalizedBy jacocoTestReport
    jacoco {
        enabled = true
        destinationFile = layout.buildDirectory.file("jacoco/test.exec").get().asFile
    }
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
}
```

### 4. Verify Setup

```bash
# Verify JUnit version alignment
./gradlew dependencies --configuration testRuntimeClasspath | grep -i junit

# Run tests with coverage
./gradlew clean test jacocoTestReport

# Verify reports generated
ls -lh build/reports/jacoco/test/
```

## Related

- [JUnit 5 Migration](junit5-migration.md) - Migration patterns from JUnit 4
- [Test Utilities](test-utilities.md) - Common test utility patterns
- [Spock Patterns](spock-patterns.md) - Advanced Spock testing patterns
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/test-setup-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

