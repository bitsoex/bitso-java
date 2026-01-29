# Gradle 9 Plugin Compatibility

Complete plugin version mappings for Gradle 9.x upgrade.

## Contents

- [Core Build Plugins](#core-build-plugins) (L18-L27)
- [Database Plugins](#database-plugins) (L28-L34)
- [Code Generation Plugins](#code-generation-plugins) (L35-L40)
- [Deprecated Gradle APIs](#deprecated-gradle-apis) (L41-L62)
- [JUnit Platform Launcher](#junit-platform-launcher) (L63-L77)
- [Known Issues](#known-issues) (L78-L105)
- [CodeRabbit False Positives](#coderabbit-false-positives) (L106-L127)
- [Version Catalog Example](#version-catalog-example) (L128-L158)
- [Verification Commands](#verification-commands) (L159-L173)

---
## Core Build Plugins

| Plugin | Gradle 8.x | Gradle 9.x | Notes |
|--------|------------|------------|-------|
| `io.freefair.lombok` | 8.14.2 | **9.2.0** | Major version aligned with Gradle 9.2.1 |
| `com.diffplug.spotless` | 6.x | 8.1.0 | Major bump required |
| `org.sonarqube` | 6.x | 7.2.2.6593 | Major bump required |
| `bitso.develocity` | 0.1.x | 0.2.8 | Compatibility update |
| `bitso.java.module` | 0.x | 0.x | Check for updates |

## Database Plugins

| Plugin | Gradle 8.x | Gradle 9.x | Notes |
|--------|------------|------------|-------|
| `org.flywaydb.flyway` | 10.x | 11.19.0 | Major bump |
| `nu.studer.jooq` | 9.x | 10.1.1 | Major bump |

## Code Generation Plugins

| Plugin | Gradle 8.x | Gradle 9.x | Notes |
|--------|------------|------------|-------|
| `com.google.protobuf` | 0.9.4 | **0.9.6** | Gradle 9 compatible |

## Deprecated Gradle APIs

Gradle 9 removed several deprecated APIs. Common issues:

### Convention Mappings

**Error:** `The Convention type has been deprecated`

**Fix:** Use property-based configuration instead of conventions.

### Task Configuration Avoidance

**Error:** Eager task creation warnings become errors

**Fix:** Use `tasks.register` instead of `tasks.create`

### Build Listener API Changes

**Error:** `BuildListener` API changes

**Fix:** Use `BuildServiceRegistry` for build-scoped services.

## JUnit Platform Launcher

Gradle 9 with JUnit 5.11+ requires explicit launcher dependency:

```groovy
// Required for ALL projects using JUnit 5
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        }
    }
}
```

## Known Issues

### Spotless + Google Java Format

**Issue:** Google Java Format may not support Java 25 bytecode

**Fix:** Use palantir-java-format 2.74.0:

```groovy
spotless {
    java {
        palantirJavaFormat(libs.versions.palantir.java.format.get())
    }
}
```

### Lombok + Java 25

**Issue:** Older Lombok versions fail with Java 25 bytecode

**Fix:** Use Lombok 1.18.42 with plugin 9.2.0

### Develocity Build Scans

**Issue:** Old Develocity plugin incompatible with Gradle 9

**Fix:** Update to 0.2.8 in settings.gradle

## CodeRabbit False Positives

CodeRabbit may generate incorrect warnings about plugin compatibility. Always verify against this matrix.

### Known Incorrect Warnings

| Warning | Reality |
|---------|---------|
| "Protobuf plugin 0.9.6 not compatible with Gradle 9" | [v0.9.6](https://github.com/google/protobuf-gradle-plugin/releases/tag/v0.9.6) specifically adds Gradle 9 compatibility |
| "Need to downgrade protobuf plugin to 0.9.5" | 0.9.6 is required for Gradle 9.1+ |
| "Netty/Tomcat overrides break Spring Boot" | `ext['version']` overrides are valid |

**Protobuf plugin 0.9.6 changes** (from [release notes](https://github.com/google/protobuf-gradle-plugin/releases/tag/v0.9.6)):
- Removes Gradle-internal API references
- Fixes deprecated multi-string dependency notation for Gradle 9.1+

### How to Handle

1. Check this matrix first - version combinations here are tested
2. Test locally before accepting suggestions
3. Dismiss verified false positives

## Version Catalog Example

Complete version catalog for Gradle 9:

```toml
[versions]
# Build tools
gradle = "9.2.1"

# Plugins
lombok-plugin = "9.2.0"
spotless = "8.1.0"
sonarqube = "7.2.2.6593"
develocity = "0.2.8"
flyway-plugin = "11.19.0"
jooq-plugin = "10.1.1"
protobuf-plugin = "0.9.6"

# Libraries for Java 25
lombok = "1.18.42"
palantir-java-format = "2.74.0"

[plugins]
lombok = { id = "io.freefair.lombok", version.ref = "lombok-plugin" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
sonarqube = { id = "org.sonarqube", version.ref = "sonarqube" }
flyway = { id = "org.flywaydb.flyway", version.ref = "flyway-plugin" }
jooq = { id = "nu.studer.jooq", version.ref = "jooq-plugin" }
protobuf = { id = "com.google.protobuf", version.ref = "protobuf-plugin" }
```

## Verification Commands

```bash
# Check Gradle version
./gradlew --version

# List all plugins and versions
./gradlew buildEnvironment

# Check for deprecated usages
./gradlew help --warning-mode all

# Full build validation
./gradlew clean build test
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/upgrade-gradle-9/references/plugin-compatibility.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

