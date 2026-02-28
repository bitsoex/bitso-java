---
title: Version Centralization
description: Core principles for dependency version management
---

# Version Centralization

Core principles and policies for managing dependency versions in Java/Gradle projects.

## Contents

- [Critical Requirements](#critical-requirements)
- [Approved Locations](#approved-locations)
- [Anti-Patterns](#anti-patterns)
- [Spring Boot Version Unification](#spring-boot-version-unification)
- [Never-Downgrade Policy](#never-downgrade-policy)
- [Why This Matters](#why-this-matters)
- [Related](#related)

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
implementation "org.springframework.boot:spring-boot-starter-web:3.5.9"
```

### 2. Inline Version Variables

```groovy
// ❌ BAD: Variable in build.gradle
def springBootVersion = "3.5.9"
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
spring-web = "3.5.9"
spring-actuator = "3.5.9"  # Should use same ref!
```

```toml
# ✅ GOOD: Single version reference
[versions]
spring-boot = "3.5.9"

[libraries]
spring-boot-starter-web = { module = "...", version.ref = "spring-boot" }
spring-boot-starter-actuator = { module = "...", version.ref = "spring-boot" }
```

## Spring Boot Version Unification

**CRITICAL**: Projects may inadvertently define Spring Boot versions in multiple places, causing version mismatches and build issues.

### Problem: Multiple Version Definitions

Spring Boot versions can be defined in:

1. `gradle/libs.versions.toml` - version catalog (correct location)
2. `settings.gradle` - plugin management block
3. `build.gradle` - direct plugin declaration
4. Individual module `build.gradle` files

### Anti-Pattern

```groovy
// ❌ BAD: Different versions in different places
// settings.gradle: id 'org.springframework.boot' version '3.4.10'
// libs.versions.toml: springBoot = "3.5.7"
// build.gradle: id 'org.springframework.boot' version '3.5.9' apply false
```

This causes multiple Spring Boot versions in Estate Catalog and inconsistent dependency resolution.

### Required: Single Source of Truth

```toml
# gradle/libs.versions.toml - THE ONLY PLACE for Spring Boot version
[versions]
spring-boot = "3.5.9"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
```

```groovy
// build.gradle - Use alias, NOT hardcoded version
plugins {
    alias libs.plugins.spring.boot
    alias libs.plugins.spring.dependency.management
}
```

```groovy
// settings.gradle - Do NOT declare Spring Boot version here
pluginManagement {
    plugins {
        // ❌ WRONG: id 'org.springframework.boot' version '3.5.9'
        // Let version catalog handle this
    }
}
```

### Verification Command

Check for multiple Spring Boot version definitions:

```bash
# Find all Spring Boot version references
grep -rn "org.springframework.boot" --include="*.gradle" --include="*.toml" . | grep -E "version|3\.[0-9]+\.[0-9]+"
```

### Migration Steps

1. **Audit** - Find all Spring Boot version definitions
2. **Unify** - Move version to `libs.versions.toml`
3. **Replace** - Use `alias libs.plugins.spring.boot` everywhere
4. **Verify** - Run `./gradlew dependencies | grep spring-boot`

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

### Example

```toml
# ❌ WRONG: Pinning Jedis to older version than Spring Boot provides
[versions]
jedis = "4.4.8"  # DOWNGRADE - causes NoSuchMethodError
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

- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
- [../../gradle-standards/references/native-dependency-locking.md](.claude/skills/gradle-standards/references/native-dependency-locking.md) - Native Gradle locking (recommended)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/version-centralization.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

