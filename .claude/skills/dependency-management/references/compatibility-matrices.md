---
title: Compatibility Matrices
description: Version compatibility tables for Java ecosystems
---

# Compatibility Matrices

Version compatibility tables for Java projects.

## Contents

- [For Code Reviewers](#for-code-reviewers)
- [Core Components](#core-components)
- [Testing Libraries (Java 21)](#testing-libraries-java-21)
- [Testing Libraries (Java 25)](#testing-libraries-java-25)
- [Build Plugins (Java 25)](#build-plugins-java-25)
- [Redis/Jedis Compatibility](#redisjedis-compatibility)
- [Jackson Compatibility](#jackson-compatibility)
- [Java 25 Migration](#java-25-migration)
- [Protobuf Java Version Support](#protobuf-java-version-support)
- [CodeRabbit Compatibility Warnings](#coderabbit-compatibility-warnings)
- [Related](#related)

---
## For Code Reviewers

**CRITICAL**: Java is backward compatible by design. When reviewing dependency versions:

1. **Never suggest downgrades** of working versions
2. **Minimum versions**: Tables below show **minimum required**, not maximum allowed
3. **Newer is better**: Always prefer latest stable versions
4. **Library compatibility**: Libraries compatible with Java 25 have a baseline (e.g., Java 8+) that also supports Java 21
5. **No coupling**: PostgreSQL driver versions are NOT tied to Gradle versions

### How to Read Version Tables

| What the table says | What it means |
|---------------------|---------------|
| "Java 25: Groovy 5.0.3" | Java 25 **requires at minimum** Groovy 5.0.3 |
| "Gradle 9.x: Lombok 9.2.0" | Gradle 9.x **requires at minimum** Lombok plugin 9.2.0 |
| "Testing: JUnit 5.14.2" | Use JUnit **5.14.2 or newer** |

**Key insight**: A project on Java 21 can use Groovy 5.x, Lombok plugin 9.2.0, Spock 2.4-groovy-5.0, etc. Libraries that support Java 25 have a baseline version (e.g., Java 8+, Java 11+) that also covers Java 21, so they work on both.

---

## Core Components

Standard compatibility for Bitso projects:

| Component | Version | Java | Notes |
|-----------|---------|------|-------|
| **Spring Boot** | **3.5.9** | 21+ | Latest 3.5.x (min 3.5.9) - preparing for Spring Boot 4 |
| **Spring Cloud** | **2025.0.0** | 21+ | Required for Boot 3.5.x |
| **gRPC** | 1.78.0 | 11+ | Uses Protobuf 4.33.x ([release](https://github.com/grpc/grpc-java/releases/tag/v1.78.0)), Spring gRPC 0.12.0 for Boot 3.5.x |
| **Protobuf** | 4.33.4 | 8+ | Wire format compatible, gRPC 1.78.0 uses 4.33.x |
| **Gradle** | **9.2.1** | 11+ | Recommended for all projects |
| **Develocity** | 0.2.8+ | - | Build insights |

## Testing Libraries (Java 21)

For Gradle 8.x with Java 21:

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | **2.4-groovy-4.0** | Stable release (Dec 2025), requires Groovy 4.0.x |
| **JUnit Jupiter** | **5.14.2** | Released Jan 2026 |
| **JUnit Platform** | **1.14.2** | Released Jan 2026 |
| **JaCoCo** | **0.8.14** | Released Oct 2025 |
| **Testcontainers** | **1.21.4** | Stable 1.x; use **2.0.3** for 2.x repos |
| **Groovy** | **4.0.29** | Required for Spock 2.4-groovy-4.0 |
| **Pitest** | **1.22.0** | Mutation testing core |
| **Pitest Plugin** | **1.19.0-rc.2** | Released Oct 2025 |
| **Pitest JUnit5** | **1.2.3** | For JUnit 5 support |
| **SonarQube Plugin** | **7.2.2.6593** | Released Dec 2025 |

Use `/improve-test-setup` command to upgrade testing libraries.

## Testing Libraries (Java 25)

For Gradle 9.x with Java 25:

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | **2.4-groovy-5.0** | Must match Groovy 5.x |
| **JUnit Jupiter** | **5.14.2** | Released Jan 2026 |
| **JUnit Platform** | **1.14.2** | **Required** for Gradle 9 |
| **JaCoCo** | **0.8.14** | Released Oct 2025 |
| **Testcontainers** | **1.21.4** | Stable 1.x; use **2.0.3** for 2.x repos |
| **Groovy** | **5.0.3** | Required for Java 25 bytecode |

**⚠️ CRITICAL**: Never use `groovy-all` - rely on spock-core transitives.

```groovy
// ❌ NEVER: groovy-all has broken transitive deps
testImplementation 'org.apache.groovy:groovy-all:5.0.3'

// ✅ CORRECT: Let spock-core bring in Groovy transitively
testImplementation libs.spock.core
```

## Build Plugins (Java 25)

**NOTE**: Build plugins require careful upgrade planning. Versions below are **minimum required** - newer versions are preferred.

| Plugin/Tool | Version | Notes |
|-------------|---------|-------|
| **Lombok** | **1.18.42** | Required for Java 25 bytecode |
| **Lombok Plugin** | **9.2.0** | Freefair for Gradle 9.2.1 |
| **SonarQube Plugin** | **7.2.2.6593** | Gradle 9 compatible |
| **Spotless Plugin** | **8.2.0** | Gradle 9 compatible |
| **palantir-java-format** | **2.83.0** | Java 25 bytecode support |
| **Flyway Plugin** | **11.20.2** | Gradle 9 compatible |
| **jOOQ Plugin** | **10.1.1** | Gradle 9 compatible |
| **Protobuf Plugin** | **0.9.6** | Gradle 9 compatible |

Use `/prepare-to-java-25` command for upgrade workflow.

## Redis/Jedis Compatibility

For Spring Boot 3.5.x:

| Library | Version | Notes |
|---------|---------|-------|
| **bitso-commons-redis** | **4.2.1** | Required for Jedis 6.x |
| **jedis4-utils** | **3.0.0** | Required for locking/Lua scripts |
| Jedis (managed by Boot) | 6.x | Do not override manually |

**WARNING**: Using older `bitso-commons-redis` with Spring Boot 3.5.x causes:

```
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams SetParams.px(long)'
```

See `java/golden-paths/redis-jedis-compatibility.md` for patterns.

## Jackson Compatibility

**IMPORTANT**: Jackson 3.x will be a major rewrite. Stay on Jackson 2.x.

| Library | Version | Notes |
|---------|---------|-------|
| **jackson-bom** | **2.20.1** | Use BOM for all modules |
| **jackson-core** | **2.20.1** | Core parsing/generation |
| **jackson-databind** | **2.20.1** | Object binding |
| **jackson-annotations** | **2.20** | **Frozen** - no patch version |

**Override Pattern**:

In `build.gradle`:

```groovy
allprojects {
    ext['jackson-bom.version'] = libs.versions.jackson.get()
}
```

In `gradle/libs.versions.toml`:

```toml
[versions]
jackson = "2.20.1"
```

## Java 25 Migration

Full compatibility table for Java 25 upgrade. **Note**: Java 25 versions also work with Java 21 projects.

| Component | Java 21 | Java 25 | Notes |
|-----------|---------|---------|-------|
| **Gradle** | 8.14.3 | **9.2.1** | Major version required |
| **Groovy** | 4.0.x | **5.0.3** | Java 25 requires Groovy 5.x (also works on Java 21) |
| **Spock** | 2.4-groovy-4.0 | **2.4-groovy-5.0** | Must match Groovy (groovy-5.0 works on Java 21) |
| **Lombok** | 1.18.x | **1.18.42** | Java 25 bytecode |
| **Lombok Plugin** | 8.14.2 | **9.2.0** | Freefair for Gradle 9.2.1 |
| **Spotless** | 6.x | **8.2.0** | Gradle 9 compatible |
| **SonarQube** | 6.x | **7.2.2.6593** | Gradle 9 compatible |
| **Testcontainers 1.x** | 1.21.x | **1.21.4** | Latest 1.x |
| **Testcontainers 2.x** | 2.0.2 | **2.0.3** | Docker socket fix |
| **ByteBuddy** | 1.14.x | **1.17.5+** | ASM 9.8 for Java 25 |
| **Flyway Plugin** | 10.x | **11.20.2** | Gradle 9 compatible |
| **jOOQ Plugin** | 9.x | **10.1.1** | Gradle 9 compatible |

**Groovy Strategy:**

- **Primary**: Groovy 5.0.3 with Spock 2.4-groovy-5.0
- **Fallback**: Groovy 4.0.29 with Spock 2.4-groovy-4.0

**ByteBuddy Note**: If using Mockito, ensure ByteBuddy ≥ 1.17.5:

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available
```

## Protobuf Java Version Support

Official version support from [protobuf.dev/support/version-support](https://protobuf.dev/support/version-support/).

### Version Mapping

Protobuf Java uses a different major version than C++. The **minor version** corresponds to protoc version.

| Protobuf Java | protoc | Release | End of Support |
|---------------|--------|---------|----------------|
| **3.x** | 21.x-25.x | May 2022 | Mar 2027 |
| **4.x** | 26.x-33.x | Mar 2024 | Mar 2028 |
| **5.x** | 34.x+ | Q1 2027 (planned) | Mar 2029 |

### Protobuf 4.x Release Timeline

| Quarter | Protobuf Java | protoc |
|---------|---------------|--------|
| 24Q1 | 4.26 | 26.x |
| 24Q2 | 4.27 | 27.x |
| 24Q3 | 4.28 | 28.x |
| 24Q4 | 4.29 | 29.x |
| 25Q1 | 4.30 | 30.x |
| 25Q2 | 4.31 | 31.x |
| 25Q3 | 4.32 | 32.x |
| 25Q4 | 4.33 | 33.x |

### gRPC Java and Protobuf Compatibility

gRPC Java versions and their Protobuf dependencies:

| gRPC Java | Protobuf | Notes |
|-----------|----------|-------|
| 1.77.0 | 3.25.8 | Uses protoc 26.1 for codegen |
| **1.78.0** | **4.33.x** | [Upgraded to C++ protobuf 33.1](https://github.com/grpc/grpc-java/releases/tag/v1.78.0) |

**Key insight**: gRPC 1.78.0 (Dec 2025) is the first gRPC Java release to officially use Protobuf 4.x.

### Spring gRPC Compatibility

| Spring gRPC | Spring Boot | gRPC Java | Notes |
|-------------|-------------|-----------|-------|
| **0.12.0** | **3.5.x** | 1.78.0 | Use for Spring Boot 3.5.x |
| 1.0.0 | 4.0.x | 1.78.0+ | Requires Spring Boot 4 |

## CodeRabbit Compatibility Warnings

Code reviewers may generate false positives about version compatibility. **Always verify before acting on downgrade suggestions.**

### Library Version Compatibility

**Libraries have baseline JDK versions, not ceiling versions.** This means:

- A library that "supports Java 25" has a baseline (e.g., Java 8+) that covers all versions up to 25
- Groovy 5.x has a baseline of Java 8+, so it works with Java 8, 11, 17, 21, and 25
- Spock 2.4-groovy-5.0 works with any Java version - the suffix refers to Groovy version, not Java
- Libraries already working with Java 21 will continue working when you upgrade to Java 25

### Known False Positives

| Incorrect Claim | Reality |
|-----------------|---------|
| "Groovy 5.x incompatible with Java 21" | Groovy 5.x has a Java 8+ baseline, so it works with Java 21 |
| "Spock 2.4-groovy-5.0 requires Java 25" | Works with any Java version - the suffix refers to Groovy version, not Java |
| "PostgreSQL 42.7.9 requires downgrade for Gradle 8.x" | No coupling exists between PostgreSQL driver and Gradle versions |
| "Lombok plugin 9.2.0 should be 9.1.0 for Gradle 9" | 9.2.0 works fine with Gradle 9.2.1 |
| "Protobuf plugin 0.9.6 incompatible with Gradle 9" | [v0.9.6](https://github.com/google/protobuf-gradle-plugin/releases/tag/v0.9.6) specifically fixes Gradle 9 compatibility |
| "Netty/Tomcat overrides break Spring Boot" | `ext['version']` overrides are valid Spring Boot patterns |
| "gRPC 1.76+ incompatible with Protobuf 4.33.x" | gRPC 1.78.0 upgraded to Protobuf 4.33.x ([release notes](https://github.com/grpc/grpc-java/releases/tag/v1.78.0)) |
| "Use exact version X per guidelines" | Guidelines show **minimum required**, not exact pins |
| "grpc-classpath-linter 1.x should be 0.0.4" | Never downgrade - use latest (currently 1.0.3) |

### Real-World Examples of False Positives

These were actual incorrect suggestions from code reviews:

**From dependency upgrade PRs:**
- "Groovy 5.0.4 with Spock 2.4-groovy-5.0 is incompatible with Java 21" - **FALSE**: Works perfectly
- "PostgreSQL 42.7.9 should be downgraded to 42.7.8 for Gradle 8.x compatibility" - **FALSE**: No such coupling
- "Netty 4.2.9.Final is incompatible with Spring Boot 3.5.8" - **FALSE**: Developer confirmed working

### How to Handle Review Suggestions

1. **Never accept downgrades** without testing - prefer latest stable versions
2. **Test builds locally** before accepting version change suggestions
3. **Dismiss verified false positives** - don't waste time on incorrect suggestions
4. **Reference this matrix** for known-good version combinations
5. **Remember**: Tables show **minimum required versions**, not maximums

### Why This Matters

Reviewers often misinterpret version compatibility tables. They may see:
- "Java 25 requires Groovy 5.0.3" and incorrectly conclude "Groovy 5.x requires Java 25"
- "Gradle 9.x minimum" and incorrectly conclude "incompatible with Gradle 8.x"

The correct interpretation: These are **minimum versions needed**, not exclusive requirements.

## Related

- [version-centralization.md](version-centralization.md) - Version policies
- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/compatibility-matrices.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

