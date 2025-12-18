# Upgrade Java services to recommended versions (Spring Boot 3.5.8, Gradle 8.14.3+, Java 21)

**Description:** Upgrade Java services to recommended versions (Spring Boot 3.5.8, Gradle 8.14.3+, Java 21)

# 🤖 📦 Upgrade to Recommended Versions

**URGENT**: Spring Boot 3.4.x has reached end-of-life. All projects should be upgraded to Spring Boot 3.5.8.

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking for confirmation.

## Related Golden Paths

- **Spring Boot 3.5 Upgrade**: `java/golden-paths/spring-boot-3.5-upgrade.md` - Detailed upgrade patterns
- **JUnit Alignment**: `java/golden-paths/junit-version-alignment.md` - Resolving JUnit conflicts
- **Redis/Jedis Compatibility**: `java/golden-paths/redis-jedis-compatibility.md` - Resolving Redis version conflicts
- **Golden Paths Index**: `java/golden-paths/java-upgrades-golden-paths.md` - All upgrade patterns

## Target Versions

| Component | Version | Notes |
|-----------|---------|-------|
| **Spring Boot** | **3.5.8** | CRITICAL - 3.4.x EOL end of 2025 |
| **Spring Cloud** | **2025.0.0** | CRITICAL - Required for Spring Boot 3.5.x |
| **Spring Dependency Management** | **1.1.7** | Required with Spring Boot 3.5.x |
| **Gradle** | **8.14.3** | Build tool |
| **JUnit** | **5.14.1** | Testing (via BOM) |
| **JUnit Platform** | **1.14.1** | Testing platform |
| **Spock** | **2.4-groovy-4.0** | Groovy testing framework |
| **JaCoCo** | **0.8.14** | Code coverage |
| **SonarQube Plugin** | **7.2.2.6593** | Code analysis |
| **Develocity Plugin** | **0.2.8** | Build insights |
| **Publish Plugin** | **0.3.6** | Publishing |
| **bitso-rds-iam-authn** | **2.0.0** | If using RDS IAM (Hikari 6) |
| **bitso-commons-redis** | **4.2.1** | If using Redis (Jedis 6 compatibility) |
| **jedis4-utils** | **3.0.0** | If using jedis4-utils for locking/Lua scripts |

## ⚠️ CRITICAL: DO NOT UPGRADE

These versions should **NOT** be changed unless explicitly requested:

- **protobuf**: Keep existing version (3.x → 4.x is breaking)
- **grpc**: Keep existing version

## Workflow

### 1. Create Jira Ticket

Search for existing tickets, create if none found:

- **Summary**: `🤖 📦 Upgrade [repo-name] to Spring Boot 3.5.8`

### 2. Create Feature Branch

```bash
git fetch --all && git pull origin main
git checkout -b "chore/${JIRA_KEY}-upgrade-spring-boot-3.5.8"
```

### 3. Apply Version Updates

#### A. Update `gradle/libs.versions.toml` (if exists)

```toml
[versions]
springBoot = "3.5.8"
# CRITICAL: Spring Cloud must be 2025.0.0 for Spring Boot 3.5.x compatibility
springCloud = "2025.0.0"
# Keep protobuf and grpc versions unchanged!
# Update spock if present:
spock = "2.4-groovy-4.0"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
```

#### B. Update `gradle.properties` (if versions are there)

```properties
springBootVersion=3.5.8
springDependencyManagementVersion=1.1.7
sonarqubePluginVersion=7.2.2.6593
jacocoVersion=0.8.14
develocityPluginVersion=0.2.8
bitsoPublishPluginVersion=0.3.6
# Do NOT change grpcVersion or protobufVersion
```

#### C. Update `settings.gradle` plugins (use centralized versions)

**In `gradle.properties`:**

```properties
develocityPluginVersion=0.2.8
sonarqubePluginVersion=7.2.2.6593
```

**In `settings.gradle`:**

```groovy
plugins {
    id 'bitso.develocity' version "${develocityPluginVersion}"
    id 'org.sonarqube' version "${sonarqubePluginVersion}"
    // Other plugins as needed
}
```

#### D. Update Gradle Wrapper

```bash
./gradlew wrapper --gradle-version=8.14.3
```

#### E. Add JUnit Platform Launcher (REQUIRED for Gradle 8.14.3)

In root `build.gradle`, add to subprojects block:

```groovy
subprojects {
    // ... existing config ...
    
    // Required for JUnit 5.11+ with Gradle 8.14.3
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly libs.junit.platform.launcher
        }
    }
}
```

**RECOMMENDED**: Use testing bundles instead of individual dependencies. Add to version catalog:

```toml
# gradle/libs.versions.toml
[bundles]
testing = ["junit-jupiter", "junit-platform-launcher"]
testing-spock = ["spock-core", "spock-spring"]
testing-full = ["junit-jupiter", "junit-platform-launcher", "spock-core", "spock-spring"]
```

Then in modules:

```groovy
dependencies {
    testImplementation libs.bundles.testing.full  // Includes platform-launcher
}
```

> **📚 Reference**: See [Gradle Version Catalogs - Bundles](https://docs.gradle.org/current/userguide/version_catalogs.html) for official documentation.

#### F. Update bitso-rds-iam-authn (if using RDS IAM)

```toml
# gradle/libs.versions.toml
[versions]
bitso-rds-iam-authn = "2.0.0"
```

Or in `build.gradle`:

```groovy
implementation "com.bitso.aws.rds-iam-authn:spring-boot:2.0.0"
```

#### G. Update Redis Libraries (if using Redis)

**CRITICAL**: Spring Boot 3.5.x uses Jedis 6.x. Older `bitso-commons-redis` versions will cause `NoSuchMethodError`.

Check if project uses Redis:

```bash
grep -r "bitso.commons:redis\|jedis\|JedisWrapper" --include="*.gradle" --include="*.toml" .
```

If Redis is used, update version catalog:

```toml
# gradle/libs.versions.toml
[versions]
bitso-commons-redis = "4.2.1"
jedis4-utils = "3.0.0"

[libraries]
bitso-commons-redis = { module = "com.bitso.commons:redis", version.ref = "bitso-commons-redis" }
jedis4-utils = { module = "com.bitso.commons:jedis4-utils", version.ref = "jedis4-utils" }
```

#### CRITICAL: Never Downgrade Jedis

- Do NOT add Jedis to version catalog - let Spring Boot BOM manage it
- Do NOT pin Jedis to older versions (e.g., 4.4.8)
- Remove any `libs.jedis` references from individual `build.gradle` files

If version constraints are needed, use version catalog references:

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency { details ->
            if (details.requested.group == 'com.bitso.commons' && details.requested.name == 'redis') {
                details.useVersion libs.versions.bitso.commons.redis.get()
            }
        }
    }
}
```

See `java/golden-paths/redis-jedis-compatibility.md` for detailed compatibility matrix and examples.

### 4. Validate Build

```bash
./gradlew clean build -x codeCoverageReport -x test --no-daemon
./gradlew test -x codeCoverageReport --no-daemon
```

### 5. Commit and Push

```bash
git add -A
git commit -m "🤖 📦 chore(deps): [$JIRA_KEY] upgrade to Spring Boot 3.5.8

Upgrades:
- Spring Boot: X.X.X -> 3.5.8
- Gradle: X.X.X -> 8.14.3
- SonarQube: X.X.X -> 7.2.0.6526
- JaCoCo: X.X.X -> 0.8.14
- Develocity: X.X.X -> 0.2.8
- Added junit-platform-launcher for Gradle 8.14.3 compatibility

Generated with the Quality Agent."
git push -u origin $(git branch --show-current)
```

### 6. Create PR and Monitor CI

```bash
gh pr create --draft --title "🤖 📦 [$JIRA_KEY] chore(deps): upgrade to Spring Boot 3.5.8"
```

## Troubleshooting

### JUnit test discovery fails with "OutputDirectoryProvider not available"

**Cause**: JUnit 5.11+ requires `junit-platform-launcher` in testRuntimeOnly.

**Fix**: Add to root `build.gradle`:

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        }
    }
}
```

### JUnit version mismatch errors

**Cause**: `bitso.java.module` plugin forces JUnit 5.10.1, but Spring Boot 3.5.x brings JUnit 5.12.2+.

**Fix**: Force JUnit version alignment using version catalog. See `java/golden-paths/junit-version-alignment.md` for detailed patterns.

For modules with Spock tests, add to each module's `build.gradle`:

```groovy
// Versions should be defined in gradle/libs.versions.toml:
// [versions]
// junit-jupiter = "5.14.1"
// junit-platform = "1.14.1"
// [libraries]
// junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }

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

dependencies {
    // ✅ RECOMMENDED: Use testing bundles
    testImplementation libs.bundles.testing.full
    
    // Or individual dependencies if bundles not available
    // testImplementation platform(libs.junit.bom)
    // testImplementation libs.junit.jupiter
    // testImplementation libs.spock.core
}

test {
    useJUnitPlatform()
}
```

**If using `bitsoJavaModule.testRuntime()`**: Remove it and configure manually as shown above.

### Spring Cloud compatibility error

**Error**: `Spring Boot [3.5.8] is not compatible with this Spring Cloud release train`

**Cause**: Spring Cloud 2024.0.x is only compatible with Spring Boot 3.4.x.

**Fix**: Update Spring Cloud to 2025.0.0:

```toml
# gradle/libs.versions.toml
springCloud = "2025.0.0"
```

### Develocity plugin fails with Gradle 8.14.3

**Cause**: Old Develocity plugin version incompatible with Gradle 8.14.3.

**Fix**: Upgrade `bitso.develocity` to `0.2.8` in `settings.gradle`.

### RDS IAM authentication fails

**Cause**: Hikari 6 changed credential handling.

**Fix**: Upgrade `bitso-rds-iam-authn` to `2.0.0`.

### Redis connection fails or NoSuchMethodError

**Error**: `java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams redis.clients.jedis.params.SetParams.px(long)'`

**Cause**: Spring Boot 3.5.x uses Jedis 6.x, which is incompatible with older `bitso-commons-redis` versions.

**Fix**: Update Redis libraries in version catalog:

```toml
# gradle/libs.versions.toml
[versions]
bitso-commons-redis = "4.2.1"
jedis4-utils = "3.0.0"  # If using jedis4-utils
```

**Verification**:

```bash
./gradlew dependencies | grep -i "jedis\|redis"
```

See `java/golden-paths/redis-jedis-compatibility.md` for the full compatibility matrix and additional patterns.

## Reference

- [jvm-generic-libraries#651](https://github.com/bitsoex/jvm-generic-libraries/pull/651) - rds-iam-authn Hikari 6 fix
