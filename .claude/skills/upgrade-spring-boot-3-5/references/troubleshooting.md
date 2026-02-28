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
- [Multiple Spring Boot Versions Detected](#multiple-spring-boot-versions-detected)
- [Redis/Jedis Compatibility Matrix](#redisjedis-compatibility-matrix)
- [Upgrade Patterns](#upgrade-patterns)

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

## Multiple Spring Boot Versions Detected

**Symptom:** Estate Catalog shows multiple Spring Boot versions for an entity, or version detection reports unexpected values.

**Causes:**

1. Plugin version differs from catalog version
2. Build file has hardcoded version alongside catalog reference
3. SBOM contains multiple versions from submodules
4. Plugin references wrong version key (e.g., `grpc-client-spring-boot` instead of `spring-boot`)

### Detection Logic

The Estate Catalog crawler:

- Detects explicit `spring-boot` key in `[versions]` section (highest priority)
- Resolves `version.ref` from plugin declarations
- Falls back to `springBoot` (camelCase) or `spring6-boot` patterns
- Filters out Spring Framework versions (5.x, 6.x) as implausible Spring Boot versions
- Reports multiple versions if found in different sources

### Common Misconfigurations

```toml
# ❌ BAD: Plugin references wrong version key
[versions]
grpc-client-spring-boot = "3.1.0.RELEASE"  # This is a library version, not Spring Boot!
spring-boot = "3.5.9"

[plugins]
springBoot = { id = "org.springframework.boot", version.ref = "grpc-client-spring-boot" }  # Wrong!
```

```toml
# ✅ CORRECT: Plugin references the right version
[versions]
spring-boot = "3.5.9"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

### Fix Steps

1. **Audit** - Find all Spring Boot version definitions:

   ```bash
   grep -rn "org.springframework.boot\|spring-boot\|springBoot" --include="*.gradle" --include="*.toml" .
   ```

2. **Unify** - Move version to single `spring-boot` key in `libs.versions.toml`

3. **Update plugins** - Ensure plugin references correct version key

4. **Verify** - Check resolved version:

   ```bash
   ./gradlew dependencies --configuration runtimeClasspath | grep spring-boot
   ```

### Version Validation

The crawler validates detected versions:

- Accepts versions 3.x - 4.x as plausible Spring Boot versions
- Rejects versions 5.x+ (these are Spring Framework, not Spring Boot)
- Rejects versions with `.RELEASE` suffix (library versions, not framework)

## Redis/Jedis Compatibility Matrix

| Spring Boot | Jedis Version | bitso-commons-redis | jedis4-utils |
|-------------|---------------|---------------------|--------------|
| 3.1.x | 4.x | 3.x | 1.x-2.x |
| 3.2.x-3.4.x | 5.x | 3.6.x | 2.x |
| **3.5.x** | **6.x** | **4.2.1** | **3.0.0** |

## Upgrade Patterns

### With Spring Cloud

Full upgrade requires Spring Cloud 2025.0.0 for Spring Boot 3.5.x compatibility.

### Without Spring Cloud

Simpler upgrades only require Spring Boot version update.

### With Redis

Redis upgrades require `bitso-commons-redis:4.2.1` and `jedis4-utils:3.0.0`.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/upgrade-spring-boot-3-5/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

