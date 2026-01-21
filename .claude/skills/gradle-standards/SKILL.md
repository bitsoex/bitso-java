---
name: gradle-standards
description: >
  Gradle build configuration standards for Java projects. Covers version catalogs,
  dependency bundles, multi-module setup, BOM management, and common troubleshooting.
  Use when configuring Gradle builds or reviewing dependency management.
compatibility: Java projects using Gradle 8.x or 9.x
metadata:
  version: "1.1.0"
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
- [Quick Start](#quick-start) (L60-L88)
- [Key Principles](#key-principles) (L89-L101)
- [Version Alignment](#version-alignment) (L102-L128)
- [References](#references) (L129-L140)
- [Related Rules](#related-rules) (L141-L145)
- [Dependency Resolution Stack](#dependency-resolution-stack) (L146-L184)
- [Related Skills](#related-skills) (L185-L192)

### Available Resources

**📚 references/** - Detailed documentation
- [cleanup workflow](references/cleanup-workflow.md)
- [multi module](references/multi-module.md)
- [native dependency locking](references/native-dependency-locking.md)
- [optimization](references/optimization.md)
- [scope optimization](references/scope-optimization.md)
- [troubleshooting](references/troubleshooting.md)
- [unused detection](references/unused-detection.md)
- [version catalogs](references/version-catalogs.md)

---

## Quick Start

### 1. Version Centralization (Required)

All versions MUST be centralized in `gradle/libs.versions.toml`:

```toml
[versions]
spring-boot = "3.5.9"
grpc = "1.78.0"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-starter-actuator = { module = "org.springframework.boot:spring-boot-starter-actuator", version.ref = "spring-boot" }
```

### 2. Use in build.gradle

```groovy
dependencies {
    // ✅ CORRECT: Use version catalog with explicit dependencies
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.actuator

    // ❌ NEVER: Hardcode versions
    // implementation "org.springframework.boot:spring-boot-starter-web:3.5.9"
}
```

## Key Principles

| Principle | Description |
|-----------|-------------|
| **Centralize Versions** | All versions in `libs.versions.toml`, never inline |
| **Explicit Dependencies** | Declare each dependency explicitly for clarity |
| **Use Align Rules** | Nebula align rules ensure version consistency across module groups |
| **Never Downgrade** | Don't replace existing versions with older ones |
| **Trust BOMs** | Spring Boot BOM manages transitive dependencies |
| **Platform Over Enforce** | Use `platform()`, never `enforcedPlatform()` |
| **Use Nebula for Resolution** | Use resolution rules + lock files, not force/constraints |
| **Lock Dependencies** | Generate `gradle.lockfile` for ALL submodules (use `build --write-locks`) |

## Version Alignment

Use Nebula `align` rules to ensure all modules in a library group use the same version. This is preferred over bundles because:
- Subprojects can declare exactly what they need
- Dependencies are explicit and visible in build files
- Version consistency is enforced at resolution time

```json
// gradle/resolution-rules.json
{
  "align": [
    {
      "name": "jackson-alignment",
      "group": "com\\.fasterxml\\.jackson\\.core",
      "reason": "Jackson modules must use same version"
    },
    {
      "name": "grpc-alignment",
      "group": "io\\.grpc",
      "reason": "gRPC modules must align for binary compatibility"
    }
  ]
}
```

See [Nebula resolution rules documentation](https://github.com/nebula-plugins/gradle-resolution-rules-plugin) for complete align rule reference.

## References

| Reference | Description |
|-----------|-------------|
| [references/version-catalogs.md](references/version-catalogs.md) | Complete version catalog guide |
| [references/multi-module.md](references/multi-module.md) | Multi-module project setup |
| [references/native-dependency-locking.md](references/native-dependency-locking.md) | **Gradle native locking (Gradle 9+ recommended)** |
| [references/cleanup-workflow.md](references/cleanup-workflow.md) | Dependency cleanup process |
| [references/unused-detection.md](references/unused-detection.md) | Finding unused dependencies |
| [references/optimization.md](references/optimization.md) | Build optimization techniques |
| [references/troubleshooting.md](references/troubleshooting.md) | Common issues and solutions |

## Related Rules

- `.cursor/rules/java-gradle-best-practices.mdc` - Full Gradle configuration reference
- `.cursor/rules/java-versions-and-dependencies.mdc` - Version management policies

## Dependency Resolution Stack

```
┌─────────────────────────────────────────────────────────────────────┐
│  1. VERSION CATALOG (libs.versions.toml)                            │
│     Single source of truth for declared versions                    │
├─────────────────────────────────────────────────────────────────────┤
│  2. RESOLUTION RULES (optional: resolution-rules.json)              │
│     Policies: substitute, align, deny, exclude                      │
│     Use substitute for security fixes, align for module groups      │
├─────────────────────────────────────────────────────────────────────┤
│  3. LOCK FILE (captures EXACT resolved versions)                    │
│     Option A: gradle.lockfile (native - Gradle 9+ recommended)      │
│     Option B: dependencies.lock (Nebula plugin)                     │
│                                                                     │
│  ⚠️  CRITICAL: Multi-module projects need lockfiles for ALL modules │
│     Use: ./gradlew build --write-locks -x test                      │
│     NOT: ./gradlew dependencies --write-locks (root only!)          │
└─────────────────────────────────────────────────────────────────────┘
```

**Lock File Options:**
- [Native locking](references/native-dependency-locking.md) - Built-in, no plugins, recommended for Gradle 9+
- [Nebula resolution rules](https://github.com/nebula-plugins/gradle-resolution-rules-plugin) - Declarative rules for alignment, substitution, and replacement

**Multi-Module Lockfile Generation:**

```bash
# ✅ CORRECT: Generates lockfiles for ALL submodules
./gradlew build --write-locks -x test

# ❌ WRONG: Only generates for ROOT project
./gradlew dependencies --write-locks

# Verify coverage (lockfiles should ≈ build.gradle files)
find . -name "gradle.lockfile" | wc -l
find . -name "build.gradle" | wc -l
```

## Related Skills

| Skill | Purpose |
|-------|---------|
| `dependency-management` | Version catalogs and BOMs |
| `dependabot-security` | Security vulnerability fixes |
| `java-coverage` | JaCoCo configuration in Gradle |
| `java-testing` | Test configuration with Spock |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

