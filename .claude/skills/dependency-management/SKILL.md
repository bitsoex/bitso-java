---
name: dependency-management
description: >
  Version catalog strategy, dependency management, BOMs, and version constraints
  for Java/Gradle projects. Covers version centralization, never-downgrade policy,
  bundle patterns, resolution strategies, and compatibility matrices.
compatibility: Java projects using Gradle 8.x or 9.x with version catalogs
metadata:
  version: "1.0.0"
  technology: java
  category: build
  tags:
    - java
    - gradle
    - dependencies
    - versions
    - bom
---

# Dependency Management

Standards for managing library versions, dependency constraints, and Bill of Materials (BOM) in Java/Gradle projects.

## When to use this skill

- Adding or updating dependencies
- Managing library versions in version catalogs
- Resolving dependency conflicts
- Upgrading Spring Boot or other frameworks
- Setting up BOM-based dependency management
- Understanding version compatibility matrices

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill)
- [Critical Policies](#critical-policies)
- [Version Catalog Structure](#version-catalog-structure)
- [Bundle Patterns](#bundle-patterns)
- [Platform Dependency Management](#platform-dependency-management)
- [References](#references)
- [Related Rules](#related-rules)
- [Related Skills](#related-skills)

### Available Resources

**📚 references/** - Detailed documentation
- [bom strategy](references/bom-strategy.md)
- [bundle patterns](references/bundle-patterns.md)
- [compatibility matrices](references/compatibility-matrices.md)
- [resolution strategies](references/resolution-strategies.md)
- [security updates](references/security-updates.md)
- [version centralization](references/version-centralization.md)

---

## Critical Policies

### 1. Version Centralization (Mandatory)

**All dependency versions MUST be centralized in `gradle/libs.versions.toml`.**

```groovy
// ❌ NEVER: Hardcode versions in build.gradle
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web:3.5.9"
}

// ✅ ALWAYS: Use version catalog
dependencies {
    implementation libs.spring.boot.starter.web
}
```

See [references/version-centralization.md](references/version-centralization.md) for anti-patterns and approved locations.

### 2. Never Downgrade Pre-existing Versions

**Never replace a library version with an older version that pre-existed in the repository.**

| Allowed | Not Allowed |
|---------|-------------|
| Upgrade a library | Downgrade a pre-existing version |
| Adjust a version YOUR PR introduced | Pin BOM-managed dependency lower |
| Add warning comment | Remove security patches |

See [references/version-centralization.md](references/version-centralization.md) for the full policy.

## Version Catalog Structure

The version catalog (`gradle/libs.versions.toml`) is the single source of truth:

```toml
[versions]
spring-boot = "3.5.9"
grpc = "1.78.0"
spock = "2.4-groovy-4.0"
junit-jupiter = "5.14.2"

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }

[bundles]
testing-spock = ["spock-core", "spock-spring"]
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

### Key Principles

| Principle | Description |
|-----------|-------------|
| **Single Source** | All versions in one file |
| **BOMs First** | Use BOMs for transitive management |
| **Type-Safe** | Gradle generates type-safe accessors |
| **Semantic Groups** | Organize by framework/purpose |

## Bundle Patterns

Bundles group related dependencies for cleaner build files:

```groovy
// ❌ Verbose: Multiple declarations
dependencies {
    testImplementation libs.spock.core
    testImplementation libs.spock.spring
    testImplementation libs.testcontainers.spock
    testImplementation libs.testcontainers.postgresql
}

// ✅ Clean: Use bundles
dependencies {
    testImplementation libs.bundles.testing.spock
    testImplementation libs.bundles.testing.integration
}
```

### Common Bundles

| Bundle | Contents | Use Case |
|--------|----------|----------|
| `testing-spock` | spock-core, spock-spring | Most test suites |
| `testing-integration` | testcontainers-spock, postgres | Integration tests |
| `spring-boot-service` | web, actuator | Web services |
| `grpc-core` | netty-shaded, protobuf, stub | gRPC services |
| `codegen` | lombok, mapstruct | Code generation |

See [references/bundle-patterns.md](references/bundle-patterns.md) for all bundles and usage.

## Platform Dependency Management

Use Gradle's native `platform()` to import BOMs. The `io.spring.dependency-management` plugin is also used in Spring Boot projects (it is applied automatically by the Spring Boot plugin), but when importing additional BOMs prefer `platform()` over `mavenBom` directives.

```groovy
dependencies {
    // Use platform() to import managed versions from BOMs
    implementation platform(libs.spring.boot.bom)
    implementation platform(libs.grpc.bom)

    // Dependencies managed by the platform don't need explicit versions
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.actuator
}
```

### Key Rules

- Use `platform()` to import BOMs, never `enforcedPlatform()` (prevents necessary overrides)
- Prefer `platform()` over `mavenBom` directives for BOM imports -- `platform()` is the native Gradle approach
- The `io.spring.dependency-management` plugin is applied automatically by the Spring Boot plugin and manages many versions; additional BOMs should be imported via `platform()`
- `platform()` allows overriding when needed (e.g., for security patches)

See [references/bom-strategy.md](references/bom-strategy.md) for complete patterns.

## References

| Reference | Description |
|-----------|-------------|
| [version-centralization.md](references/version-centralization.md) | Core principles, anti-patterns, policies |
| [bundle-patterns.md](references/bundle-patterns.md) | All bundle definitions and usage |
| [bom-strategy.md](references/bom-strategy.md) | Bill of Materials setup |
| [compatibility-matrices.md](references/compatibility-matrices.md) | Java/Spring/testing version tables |
| [resolution-strategies.md](references/resolution-strategies.md) | Conflict resolution, substitutions |
| [security-updates.md](references/security-updates.md) | CVE fixes, forced versions |

## Related Rules

- [java-versions-and-dependencies](.cursor/rules/java-versions-and-dependencies/java-versions-and-dependencies.mdc) - Original comprehensive rule
- [java-gradle-best-practices](.cursor/rules/java-gradle-best-practices/java-gradle-best-practices.mdc) - Gradle configuration patterns

## Related Skills

| Skill | Purpose |
|-------|---------|
| [gradle-standards](.claude/skills/gradle-standards/SKILL.md) | Gradle build configuration |
| [fix-vulnerabilities](.claude/skills/fix-vulnerabilities/SKILL.md) | Vulnerability management |
| [upgrade-gradle-9](.claude/skills/upgrade-gradle-9/SKILL.md) | Gradle 9 migration |
| [upgrade-java-25](.claude/skills/upgrade-java-25/SKILL.md) | Java 25 compatibility |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

