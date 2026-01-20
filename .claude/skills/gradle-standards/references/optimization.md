---
title: Build Optimization
description: Gradle build optimization techniques for faster builds
---

# Build Optimization

Gradle build optimization techniques for faster and more reliable builds.

## Contents

- [Dependency Optimization](#dependency-optimization) (L20-L66)
- [Build Performance](#build-performance) (L67-L97)
- [Dependency Analysis](#dependency-analysis) (L98-L127)
- [Version Catalog Best Practices](#version-catalog-best-practices) (L128-L155)
- [Troubleshooting](#troubleshooting) (L156-L182)
- [Related](#related) (L183-L187)

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

## Version Catalog Best Practices

### Group Related Dependencies

```toml
[versions]
spring-boot = "3.5.8"
grpc = "1.65.1"

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

