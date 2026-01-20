---
title: Version Centralization
description: Core principles for dependency version management
---

# Version Centralization

Core principles and policies for managing dependency versions in Java/Gradle projects.

## Contents

- [Critical Requirements](#critical-requirements) (L20-L52)
- [Approved Locations](#approved-locations) (L53-L64)
- [Anti-Patterns](#anti-patterns) (L65-L108)
- [Never-Downgrade Policy](#never-downgrade-policy) (L109-L153)
- [Why This Matters](#why-this-matters) (L154-L168)
- [Related](#related) (L169-L172)

---
## Critical Requirements

**All dependency versions MUST be centralized.** Never hardcode versions in individual `build.gradle` files.

### The Golden Rule

```groovy
// ❌ NEVER: Hardcode versions in build.gradle
dependencies {
    implementation "com.bitso.commons:redis:3.1.0"
    implementation "redis.clients:jedis:4.3.1"
    implementation "org.springframework.boot:spring-boot-starter-web:3.2.0"
}

// ❌ NEVER: Define versions inline
def redisVersion = "3.1.0"
dependencies {
    implementation "com.bitso.commons:redis:${redisVersion}"
}

// ❌ NEVER: Mix version sources
dependencies {
    implementation libs.spring.boot.starter.web  // from catalog
    implementation "com.bitso.commons:redis:3.1.0"  // hardcoded - BAD!
}

// ✅ ALWAYS: Use version catalog
dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.bitso.commons.redis
}
```

## Approved Locations

All versions must be defined in one of these locations:

| Location | Use Case | Priority |
|----------|----------|----------|
| `gradle/libs.versions.toml` | **Primary** - All new projects | 1st |
| `gradle.properties` | Legacy projects transitioning | 2nd |
| `versions.gradle` | Legacy - Migrate to TOML | 3rd |

**Goal**: All projects should use `gradle/libs.versions.toml` as the single source of truth.

## Anti-Patterns

### 1. Hardcoded Versions

```groovy
// ❌ BAD: Version in build.gradle
implementation "org.springframework.boot:spring-boot-starter-web:3.5.8"
```

### 2. Inline Version Variables

```groovy
// ❌ BAD: Variable in build.gradle
def springBootVersion = "3.5.8"
implementation "org.springframework.boot:spring-boot-starter-web:${springBootVersion}"
```

### 3. Mixed Sources

```groovy
// ❌ BAD: Some from catalog, some hardcoded
implementation libs.spring.boot.starter.web
implementation "com.example:library:1.2.3"  // Why not from catalog?
```

### 4. Duplicated Versions

```toml
# ❌ BAD: Same version defined multiple times
[versions]
spring-web = "3.5.8"
spring-actuator = "3.5.8"  # Should use same ref!
```

```toml
# ✅ GOOD: Single version reference
[versions]
spring-boot = "3.5.8"

[libraries]
spring-boot-starter-web = { module = "...", version.ref = "spring-boot" }
spring-boot-starter-actuator = { module = "...", version.ref = "spring-boot" }
```

## Never-Downgrade Policy

**Policy**: Never replace a library version with an older version that was pre-existing in the repository.

### Rules

1. **Pre-existing versions are protected** - Do not downgrade versions that existed before your PR
2. **Only YOUR versions can be adjusted** - If you introduce a version that causes issues, try a different newer version
3. **BOM-managed versions are exempt** - Never pin lower than what a BOM provides
4. **When in doubt, warn** - Add a comment instead of downgrading

### What's Allowed

| Scenario | Allowed | Not Allowed |
|----------|---------|-------------|
| Upgrade a library | ✅ | - |
| Downgrade YOUR new version | ✅ (try different newer) | - |
| Downgrade pre-existing version | - | ❌ |
| Pin BOM-managed to older | - | ❌ |
| Add warning comment | ✅ | - |

### Real Example (PR 643)

```toml
# ❌ WRONG: Pinning Jedis to older version than Spring Boot provides
[versions]
jedis = "4.4.8"  # DOWNGRADE - caused NoSuchMethodError
```

```groovy
# ✅ CORRECT: Use resolution strategy for Bitso libraries only
if (details.requested.group == 'com.bitso.commons' && details.requested.name == 'redis') {
    details.useVersion libs.versions.bitso.commons.redis.get()
}
```

### When You Suspect Incompatibility

Add a warning comment instead of downgrading:

```groovy
// ⚠️ WARNING: Potential compatibility issue - investigate root cause
// DO NOT downgrade - see java/golden-paths/redis-jedis-compatibility.md
```

## Why This Matters

Hardcoded versions cause:

1. **NoSuchMethodError at runtime**: Different modules use incompatible versions
2. **Version drift**: Hard to track what versions are used
3. **Upgrade failures**: Must find versions scattered across files
4. **Security gaps**: Miss security patches buried in code

**Real example**: Spring Boot 3.5.x upgrades Jedis to 6.x, but hardcoded `bitso-commons-redis:3.1.0` still expects Jedis 4.x, causing:

```
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams SetParams.px(long)'
```

## Related

- [bundle-patterns.md](bundle-patterns.md) - Using bundles for dependencies
- [../SKILL.md](../SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/version-centralization.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

