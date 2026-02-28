---
title: Build Optimization
description: Gradle build optimization techniques for faster builds
---

# Build Optimization

Gradle build optimization techniques for faster and more reliable builds.

## Contents

- [Dependency Optimization](#dependency-optimization)
- [Build Performance](#build-performance)
- [Dependency Analysis](#dependency-analysis)
- [Dependency Scope Optimization](#dependency-scope-optimization)
- [Version Catalog Best Practices](#version-catalog-best-practices)
- [Troubleshooting](#troubleshooting)
- [Related](#related)

---
## Dependency Optimization

### Use BOMs for Version Management

```groovy
dependencies {
    // Use BOMs to manage transitive versions
    implementation platform(libs.spring.boot.dependencies)
    implementation platform(libs.grpc.bom)

    // Dependencies without explicit versions
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### Use `api` vs `implementation` Correctly

```groovy
dependencies {
    // api: Exposes to consumers (use sparingly)
    api libs.guava

    // implementation: Internal only (prefer this)
    implementation libs.jackson.core

    // runtimeOnly: Not needed at compile time
    runtimeOnly libs.postgresql

    // compileOnly: Provided at runtime
    compileOnly libs.lombok
}
```

### Avoid Dependency Conflicts

```groovy
configurations.all {
    resolutionStrategy {
        // Force specific versions
        force 'com.google.guava:guava:32.1.2-jre'

        // Fail on version conflicts
        failOnVersionConflict()
    }
}
```

## Build Performance

### Enable Configuration Cache

```properties
# gradle.properties
org.gradle.configuration-cache=true
```

### Enable Parallel Execution

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

### Optimize JVM Settings

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC
```

### Use Build Cache

```properties
# gradle.properties
org.gradle.caching=true
```

## Dependency Analysis

### Find Dependency Tree

```bash
# Full dependency tree
./gradlew dependencies

# Specific configuration
./gradlew dependencies --configuration runtimeClasspath

# Filter to specific dependency
./gradlew dependencies --configuration runtimeClasspath | grep -A 5 "spring-boot"
```

### Find Dependency Conflicts

```bash
# Show conflict resolution
./gradlew dependencyInsight --dependency guava --configuration runtimeClasspath
```

### Find Unused Dependencies

```bash
# Using Gradle Lint Plugin
./gradlew lintGradle
./gradlew generateGradleLintReport
```

## Dependency Scope Optimization

Use the **Dependency Analysis Gradle Plugin** to identify over-broad scopes and optimize dependency declarations.

### Setup

```groovy
// build.gradle
plugins {
    id 'com.autonomousapps.dependency-analysis' version '3.5.1'
}
```

### Analyze Scopes

```bash
# Generate advice report
./gradlew buildHealth

# Project-specific advice
./gradlew :my-module:projectHealth
```

### Scope Selection Guide

| Scope | Use When | Exposes to Consumers |
|-------|----------|---------------------|
| `implementation` | Internal use only | No |
| `api` | Exposed in public API | Yes |
| `compileOnly` | Compile-time only (Lombok, annotations) | No |
| `runtimeOnly` | Runtime only (JDBC drivers, logging) | No |
| `testImplementation` | Test dependencies | N/A |

### Common Migrations

```groovy
// ❌ BEFORE: Over-broad scopes
implementation libs.lombok
implementation libs.postgresql
implementation libs.logback.classic

// ✅ AFTER: Minimal scopes
compileOnly libs.lombok           // Only needed at compile time
annotationProcessor libs.lombok   // Annotation processing
runtimeOnly libs.postgresql       // Only needed at runtime
runtimeOnly libs.logback.classic  // Logging implementation
```

### API vs Implementation

For **library modules** that expose types to consumers:

```groovy
// If your public API returns/accepts Spring types
api libs.spring.boot.starter.web

// If you only use Spring internally
implementation libs.spring.boot.starter.web
```

### Automatic Fixes

```bash
# Apply suggested fixes (review carefully!)
./gradlew fixDependencies
```

### Configuration

```groovy
// build.gradle
dependencyAnalysis {
    issues {
        all {
            onAny {
                severity('fail')  // fail, warn, or ignore
            }
        }
    }
}
```

## Version Catalog Best Practices

### Group Related Dependencies

```toml
[versions]
spring-boot = "3.5.9"
grpc = "1.78.0"

[libraries]
# Group by purpose
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator", version.ref = "spring-boot" }

[bundles]
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]
```

### Use Bundles for Common Combinations

```groovy
dependencies {
    // Instead of listing individually
    implementation libs.bundles.spring.boot.service
    testImplementation libs.bundles.testing.spring
}
```

## Troubleshooting

### Slow Builds

1. Check Gradle daemon status: `./gradlew --status`
2. Enable parallel builds and caching
3. Review dependency tree for unnecessary transitives
4. Use `--profile` to identify slow tasks

### Out of Memory

Increase JVM memory in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

### Plugin Compatibility

Check plugin versions for Gradle compatibility:

```bash
./gradlew --version
```

Refer to plugin documentation for compatible versions.

## Related

- [Cleanup Workflow](cleanup-workflow.md) - Dependency cleanup
- [Unused Detection](unused-detection.md) - Finding unused dependencies
- [Version Catalogs](version-catalogs.md) - Catalog configuration
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/optimization.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

