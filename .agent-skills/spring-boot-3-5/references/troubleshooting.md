<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/spring-boot-3-5/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

# Spring Boot 3.5.x Upgrade Troubleshooting

Common issues and solutions when upgrading to Spring Boot 3.5.x.

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
Spring Boot [3.5.8] is not compatible with this Spring Cloud release train
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
