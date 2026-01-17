# Spring Boot 3.5.x Upgrade Troubleshooting

Common issues and solutions when upgrading to Spring Boot 3.5.x.

## Contents

- [JUnit Test Discovery Fails](#junit-test-discovery-fails)
- [JUnit Version Mismatch](#junit-version-mismatch)
- [Spring Cloud Compatibility Error](#spring-cloud-compatibility-error)
- [Develocity Plugin Fails](#develocity-plugin-fails)
- [RDS IAM Authentication Fails](#rds-iam-authentication-fails)
- [Redis NoSuchMethodError](#redis-nosuchmethoderror)
- [Never Downgrade Jedis](#never-downgrade-jedis)
- [Testing Bundle Pattern](#testing-bundle-pattern)
- [Jacoco Report Generation Fails](#jacoco-report-generation-fails)
- [Spock Tests Fail to Discover](#spock-tests-fail-to-discover)
- [Redis/Jedis Compatibility Matrix](#redisjedis-compatibility-matrix)
- [Real PR Examples](#real-pr-examples)

---
## JUnit Test Discovery Fails

**Error:**

```text
OutputDirectoryProvider not available
```

**Cause:** JUnit 5.11+ requires `junit-platform-launcher` in testRuntimeOnly.

**Fix:**

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        }
    }
}
```

## JUnit Version Mismatch

**Error:** Various JUnit compatibility errors

**Cause:** `bitso.java.module` plugin forces JUnit 5.10.1, but Spring Boot 3.5.x brings JUnit 5.12.2+.

**Fix:** Force JUnit version alignment:

```groovy
configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == 'org.junit.jupiter') {
            details.useVersion libs.versions.junit.jupiter.get()
        }
        if (details.requested.group == 'org.junit.platform') {
            details.useVersion libs.versions.junit.platform.get()
        }
    }
}
```

## Spring Cloud Compatibility Error

**Error:**

```text
Spring Boot [3.5.9] is not compatible with this Spring Cloud release train
```

**Cause:** Spring Cloud 2024.0.x is only compatible with Spring Boot 3.4.x.

**Fix:** Update Spring Cloud to 2025.0.0:

```toml
# gradle/libs.versions.toml
springCloud = "2025.0.0"
```

## Develocity Plugin Fails

**Error:** Plugin incompatibility with Gradle 8.14.3

**Fix:** Upgrade `bitso.develocity` to `0.2.8` in `settings.gradle`.

## RDS IAM Authentication Fails

**Cause:** Hikari 6 changed credential handling.

**Fix:** Upgrade `bitso-rds-iam-authn` to `2.0.0`.

## Redis NoSuchMethodError

**Error:**

```text
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams
redis.clients.jedis.params.SetParams.px(long)'
```

**Cause:** Spring Boot 3.5.x uses Jedis 6.x, incompatible with older `bitso-commons-redis`.

**Fix:**

```toml
# gradle/libs.versions.toml
[versions]
bitso-commons-redis = "4.2.1"
jedis4-utils = "3.0.0"
```

**Verification:**

```bash
./gradlew dependencies | grep -i "jedis\|redis"
```

## Never Downgrade Jedis

- Do NOT add Jedis to version catalog - let Spring Boot BOM manage it
- Do NOT pin Jedis to older versions (e.g., 4.4.8)
- Remove any `libs.jedis` references from individual `build.gradle` files

## Testing Bundle Pattern

**Recommended:** Use bundles instead of individual dependencies:

```toml
# gradle/libs.versions.toml
[bundles]
testing = ["junit-jupiter", "junit-platform-launcher"]
testing-spock = ["spock-core", "spock-spring"]
testing-full = ["junit-jupiter", "junit-platform-launcher", "spock-core", "spock-spring"]
```

```groovy
dependencies {
    testImplementation libs.bundles.testing.full
}
```

## Jacoco Report Generation Fails

**Cause:** Old Jacoco version incompatible with Java 21 / new bytecode

**Fix:** Update Jacoco to 0.8.14:

```properties
jacocoVersion=0.8.14
```

## Spock Tests Fail to Discover

**Cause:** Spock version incompatible with JUnit Platform version

**Fix:** Update Spock to 2.4-groovy-4.0:

```toml
spock = "2.4-groovy-4.0"
```

## Redis/Jedis Compatibility Matrix

| Spring Boot | Jedis Version | bitso-commons-redis | jedis4-utils |
|-------------|---------------|---------------------|--------------|
| 3.1.x | 4.x | 3.x | 1.x-2.x |
| 3.2.x-3.4.x | 5.x | 3.6.x | 2.x |
| **3.5.x** | **6.x** | **4.2.1** | **3.0.0** |

## Real PR Examples

> **Note**: These examples reference internal Bitso repositories (private access required).

### PRs with Spring Cloud Upgrade

- `aum-reconciliation-v2#730` - Full Spring Boot 3.5.9 + Spring Cloud 2025.0.0 upgrade

### PRs without Spring Cloud (simpler upgrades)

- `treasury-management#291` - Basic upgrade
- `reconciliation-engine#1444` - Multi-module upgrade
- `proof-of-solvency#560` - Simple upgrade

### PRs with Redis/Jedis Fix

- `assets#643` - Fix Redis SetParams.px(long) issue
- `consumer-wallet#770` - Bump redis library to 4.2.0
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/spring-boot-3-5/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

