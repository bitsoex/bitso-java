---
title: Compatibility Matrices
description: Version compatibility tables for Java ecosystems
---

# Compatibility Matrices

Version compatibility tables for Java projects.

## Contents

- [Core Components](#core-components) (L22-L34)
- [Testing Libraries (Java 21)](#testing-libraries-java-21) (L35-L53)
- [Testing Libraries (Java 25)](#testing-libraries-java-25) (L54-L76)
- [Build Plugins (Java 25)](#build-plugins-java-25) (L77-L93)
- [Redis/Jedis Compatibility](#redisjedis-compatibility) (L94-L111)
- [Jackson Compatibility](#jackson-compatibility) (L112-L139)
- [Java 25 Migration](#java-25-migration) (L140-L169)
- [Related](#related) (L170-L173)

---
## Core Components

Standard compatibility for Bitso projects:

| Component | Version | Java | Notes |
|-----------|---------|------|-------|
| **Spring Boot** | **3.5.8** | 21+ | **REQUIRED** - 3.4.x EOL end of 2025 |
| **Spring Cloud** | **2025.0.0** | 21+ | Required for Boot 3.5.x |
| **gRPC** | 1.77.0 | 11+ | High performance |
| **Protobuf** | 4.33.0 | 8+ | Wire format compatible |
| **Gradle** | 8.14.3+ | 11+ | Build tool |
| **Develocity** | 0.2.8+ | - | Build insights |

## Testing Libraries (Java 21)

For Gradle 8.x with Java 21:

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | **2.4-groovy-4.0** | Stable release (Dec 2025), requires Groovy 4.0.x |
| **JUnit Jupiter** | **5.14.1** | Released Oct 2025 |
| **JUnit Platform** | **1.14.1** | Released Oct 2025 |
| **JaCoCo** | **0.8.14** | Released Oct 2025 |
| **Testcontainers** | **1.21.4** | Stable 1.x; use **2.0.3** for 2.x repos |
| **Groovy** | **4.0.29** | Required for Spock 2.4-groovy-4.0 |
| **Pitest** | **1.22.0** | Mutation testing core |
| **Pitest Plugin** | **1.19.0-rc.2** | Released Oct 2025 |
| **Pitest JUnit5** | **1.2.3** | For JUnit 5 support |
| **SonarQube Plugin** | **7.2.0.6526** | Released Dec 2025 |

Use `/improve-test-setup` command to upgrade testing libraries.

## Testing Libraries (Java 25)

For Gradle 9.x with Java 25:

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | **2.4-groovy-5.0** | Must match Groovy 5.x |
| **JUnit Jupiter** | **5.14.1** | Released Oct 2025 |
| **JUnit Platform** | **1.14.1** | **Required** for Gradle 9 |
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

**NOTE**: Build plugins require careful upgrade planning.

| Plugin/Tool | Version | Notes |
|-------------|---------|-------|
| **Lombok** | **1.18.42** | Required for Java 25 bytecode |
| **Lombok Plugin** | **9.1.0** | Freefair for Gradle 9 |
| **SonarQube Plugin** | **7.2.1.6560** | Gradle 9 compatible |
| **Spotless Plugin** | **8.1.0** | Gradle 9 compatible |
| **palantir-java-format** | **2.74.0** | Java 25 bytecode support |
| **Flyway Plugin** | **11.19.0** | Gradle 9 compatible |
| **jOOQ Plugin** | **10.1.1** | Gradle 9 compatible |
| **Protobuf Plugin** | **0.9.5** | Gradle 9 compatible |

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

Full compatibility table for Java 25 upgrade:

| Component | Java 21 | Java 25 | Notes |
|-----------|---------|---------|-------|
| **Gradle** | 8.14.3 | **9.2.1** | Major version required |
| **Groovy** | 4.0.x | **5.0.3** | Java 25 requires Groovy 5.x |
| **Spock** | 2.4-groovy-4.0 | **2.4-groovy-5.0** | Must match Groovy |
| **Lombok** | 1.18.x | **1.18.42** | Java 25 bytecode |
| **Lombok Plugin** | 8.14.2 | **9.1.0** | Freefair for Gradle 9 |
| **Spotless** | 6.x | **8.1.0** | Gradle 9 compatible |
| **SonarQube** | 6.x | **7.2.1.6560** | Gradle 9 compatible |
| **Testcontainers 1.x** | 1.21.x | **1.21.4** | Latest 1.x |
| **Testcontainers 2.x** | 2.0.2 | **2.0.3** | Docker socket fix |
| **ByteBuddy** | 1.14.x | **1.17.5+** | ASM 9.8 for Java 25 |
| **Flyway Plugin** | 10.x | **11.19.0** | Gradle 9 compatible |
| **jOOQ Plugin** | 9.x | **10.1.1** | Gradle 9 compatible |

**Groovy Strategy:**

- **Primary**: Groovy 5.0.3 with Spock 2.4-groovy-5.0
- **Fallback**: Groovy 4.0.29 with Spock 2.4-groovy-4.0

**ByteBuddy Note**: If using Mockito, ensure ByteBuddy ≥ 1.17.5:

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available
```

## Related

- [version-centralization.md](version-centralization.md) - Version policies
- [../SKILL.md](../SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/compatibility-matrices.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

