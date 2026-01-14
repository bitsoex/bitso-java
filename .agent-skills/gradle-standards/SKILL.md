<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: gradle-standards
description: >
  Gradle build configuration standards for Java projects. Covers version catalogs,
  dependency bundles, multi-module setup, BOM management, and common troubleshooting.
  Use when configuring Gradle builds or reviewing dependency management.
compatibility: Java projects using Gradle 8.x or 9.x
metadata:
  version: "1.0.0"
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
| [references/troubleshooting.md](references/troubleshooting.md) | Common issues and solutions |

## Related Rules

- `java/rules/java-gradle-best-practices.md` - Full Gradle configuration reference
- `java/rules/java-versions-and-dependencies.md` - Version management policies
- `java/golden-paths/redis-jedis-compatibility.md` - Dependency conflict examples

## Related Skills

| Skill | Purpose |
|-------|---------|
| [java-coverage](../java-coverage/SKILL.md) | JaCoCo configuration in Gradle |
| [java-testing](../java-testing/SKILL.md) | Test configuration with Spock |
