---
name: gradle-standards
description: >
  Gradle build configuration standards for Java projects. Covers version catalogs,
  dependency bundles, multi-module setup, BOM management, and common troubleshooting.
  Use when configuring Gradle builds or reviewing dependency management.
compatibility: Java projects using Gradle 8.x or 9.x
metadata:
  version: "1.0.0"
  technology: java
  category: build
  tags:
    - gradle
    - java
    - dependencies
    - build
---

# Gradle Standards

Standards for Gradle configuration in Java projects, including version catalogs, dependency bundles, and multi-module setup.

## When to use this skill

- Setting up a new Gradle project
- Adding or updating dependencies
- Configuring multi-module builds
- Troubleshooting dependency conflicts
- Migrating to version catalogs
- Cleaning up unused dependencies
- Optimizing build performance

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill) (L23-L32)
- [Quick Start](#quick-start) (L57-L87)
- [Key Principles](#key-principles) (L88-L97)
- [Common Bundles](#common-bundles) (L98-L117)
- [References](#references) (L118-L128)
- [Related Rules](#related-rules) (L129-L133)
- [Related Skills](#related-skills) (L134-L139)

### Available Resources

**📚 references/** - Detailed documentation
- [cleanup workflow](references/cleanup-workflow.md)
- [multi module](references/multi-module.md)
- [optimization](references/optimization.md)
- [troubleshooting](references/troubleshooting.md)
- [unused detection](references/unused-detection.md)
- [version catalogs](references/version-catalogs.md)

---

## Quick Start

### 1. Version Centralization (Required)

All versions MUST be centralized in `gradle/libs.versions.toml`:

```toml
[versions]
spring-boot = "3.5.8"
grpc = "1.65.1"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }

[bundles]
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]
```

### 2. Use in build.gradle

```groovy
dependencies {
    // ✅ CORRECT: Use version catalog
    implementation libs.spring.boot.starter.web
    implementation libs.bundles.spring.boot.service

    // ❌ NEVER: Hardcode versions
    // implementation "org.springframework.boot:spring-boot-starter-web:3.5.8"
}
```

## Key Principles

| Principle | Description |
|-----------|-------------|
| **Centralize Versions** | All versions in `libs.versions.toml`, never inline |
| **Use Bundles** | Group related dependencies (e.g., `testing-spring`) |
| **Never Downgrade** | Don't replace existing versions with older ones |
| **Trust BOMs** | Spring Boot BOM manages transitive dependencies |
| **Platform Over Enforce** | Use `platform()`, never `enforcedPlatform()` |

## Common Bundles

```toml
[bundles]
# Testing
testing-spock = ["spock-core", "spock-spring"]
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]

# Service essentials
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]

# Code generation
codegen = ["lombok", "mapstruct"]
codegen-processors = ["lombok", "mapstruct-processor", "lombok-mapstruct-binding"]

# gRPC
grpc-core = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub"]
```

## References

| Reference | Description |
|-----------|-------------|
| [references/version-catalogs.md](references/version-catalogs.md) | Complete version catalog guide |
| [references/multi-module.md](references/multi-module.md) | Multi-module project setup |
| [references/cleanup-workflow.md](references/cleanup-workflow.md) | Dependency cleanup process |
| [references/unused-detection.md](references/unused-detection.md) | Finding unused dependencies |
| [references/optimization.md](references/optimization.md) | Build optimization techniques |
| [references/troubleshooting.md](references/troubleshooting.md) | Common issues and solutions |

## Related Rules

- `.cursor/rules/java-gradle-best-practices.mdc` - Full Gradle configuration reference
- `.cursor/rules/java-versions-and-dependencies.mdc` - Version management policies

## Related Skills

| Skill | Purpose |
|-------|---------|
| [java-coverage](../java-coverage/SKILL.md) | JaCoCo configuration in Gradle |
| [java-testing](../java-testing/SKILL.md) | Test configuration with Spock |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

