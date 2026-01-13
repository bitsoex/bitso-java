---
applyTo: "gradle/libs.versions.toml,build.gradle,settings.gradle"
description: Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
---

# Java Library Versions & Dependency Management

Standards for managing library versions, dependency constraints, and Bill of Materials (BOM) in Java/Gradle projects.

## CRITICAL: Version Centralization Requirements

**All dependency versions MUST be centralized.** Never hardcode versions in individual `build.gradle` files.

### Approved Locations for Version Definitions

| Location | Use Case | Priority |
|----------|----------|----------|
| `gradle/libs.versions.toml` | **Primary** - All new projects | 1st |
| `gradle.properties` | Legacy projects transitioning | 2nd |
| `versions.gradle` | Legacy - Migrate to TOML | 3rd |

### Anti-Patterns: NEVER Do This

```groovy
// ❌ NEVER: Hardcode versions in build.gradle
dependencies {
    implementation "com.bitso.commons:redis:3.1.0"
    implementation "redis.clients:jedis:4.3.1"
    implementation "org.springframework.boot:spring-boot-starter-web:3.2.0"
}

// ❌ NEVER: Define versions inline in build.gradle
def redisVersion = "3.1.0"
dependencies {
    implementation "com.bitso.commons:redis:${redisVersion}"
}

// ❌ NEVER: Mix version sources
dependencies {
    implementation libs.spring.boot.starter.web  // from catalog
    implementation "com.bitso.commons:redis:3.1.0"  // hardcoded - BAD!
}
```

### Why This Matters

Hardcoded versions cause:

1. **NoSuchMethodError at runtime**: Different modules use incompatible versions
2. **Version drift**: Hard to track what versions are actually used
3. **Upgrade failures**: Must find and update versions scattered across files
4. **Security gaps**: Miss security patches because versions are buried in code

**Real example**: Spring Boot 3.5.x upgrades Jedis to 6.x, but a hardcoded `bitso-commons-redis:3.1.0` still expects Jedis 4.x, causing `NoSuchMethodError: SetParams.px(long)`.

---

## CRITICAL: Never Downgrade Pre-existing Versions

**Policy**: Never replace a library version with an older version that was pre-existing in the repository.

### Rules

1. **Pre-existing versions are protected** - If a version already exists in the repo before your PR, do not downgrade it
2. **Only versions your PR introduces can be adjusted** - If you upgrade a library and it causes issues during local validation, you may try a different (but still newer) version
3. **BOM-managed versions are exempt from override** - Never manually pin a version lower than what a BOM (Spring Boot, gRPC) provides
4. **When in doubt, warn the user** - Add a comment explaining potential incompatibility instead of downgrading

### Allowed Actions

| Scenario | Allowed | Not Allowed |
|----------|---------|-------------|
| Upgrade a library | ✅ Yes | - |
| Downgrade a version YOUR PR introduced | ✅ Yes (try different newer version) | - |
| Downgrade a version that existed before your PR | - | ❌ No |
| Pin BOM-managed dependency to older version | - | ❌ No |
| Add comment about potential incompatibility | ✅ Yes | - |

### Anti-Pattern Example (from PR 643)

```toml
# ❌ WRONG: Pinning Jedis to older version than Spring Boot provides
[versions]
jedis = "4.4.8"  # DOWNGRADE - caused NoSuchMethodError
```

```groovy
# ✅ CORRECT: Remove Jedis pin, use version catalog for Bitso libraries only
if (details.requested.group == 'com.bitso.commons' && details.requested.name == 'redis') {
    details.useVersion libs.versions.bitso.commons.redis.get()
}
```

### If You Suspect Version Incompatibility

Add a warning comment instead of downgrading:

```groovy
// ⚠️ WARNING: Potential compatibility issue - investigate root cause
// DO NOT downgrade - see java/golden-paths/redis-jedis-compatibility.md
```

See `java/golden-paths/redis-jedis-compatibility.md` for real-world examples.

---

## Version Catalog (`gradle/libs.versions.toml`)

Central repository for all dependency versions. Never hardcode versions in build files.

### Structure

```toml
[versions]
gradle = "8.14.3"
java = "21"
spring-boot = "3.5.8"
protobuf = "4.33.0"
grpc = "1.77.0"

# Testing Libraries
# See java/commands/improve-test-setup.md for upgrade workflow
spock = "2.4-groovy-4.0"
junit-jupiter = "5.14.1"
junit-platform = "1.14.1"
jacoco = "0.8.14"
testcontainers = "1.21.4"
groovy = "4.0.29"

# Mutation Testing
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

# Code Quality
sonar-plugin = "7.2.2.6593"

# Security updates
commons-lang3 = "3.18.0"

[libraries]
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }
grpc-bom = { module = "io.grpc:grpc-bom", version.ref = "grpc" }
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }

# Testing Libraries
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }
spock-core = { module = "org.spockframework:spock-core", version.ref = "spock" }
spock-spring = { module = "org.spockframework:spock-spring", version.ref = "spock" }
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
sonarqube = { id = "org.sonarqube", version.ref = "sonar-plugin" }

[bundles]
# ===========================================
# TESTING BUNDLES (based on common patterns)
# ===========================================
# Basic JUnit testing
testing = ["junit-jupiter", "junit-platform-launcher"]
# Spock testing (most common at Bitso - spock-core + spock-spring always together)
testing-spock = ["spock-core", "spock-spring"]
# Full testing - JUnit + Spock (recommended for most services)
testing-full = ["junit-jupiter", "junit-platform-launcher", "spock-core", "spock-spring"]
# Spring Boot test starter + Spock (very common pattern)
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]
# Integration testing with Testcontainers (postgres + kafka most common)
testing-integration = ["testcontainers-spock", "testcontainers-postgresql", "testcontainers-kafka"]
# Testcontainers with LocalStack for AWS testing
testing-aws = ["testcontainers-spock", "testcontainers-localstack"]
# Database testing with Flyway (core + postgres always together)
testing-flyway = ["flyway-core", "flyway-database-postgresql"]

# ===========================================
# SPRING BOOT SERVICE BUNDLES
# ===========================================
# Web service essentials (actuator almost always included)
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]
# Kafka messaging services
spring-boot-kafka = ["spring-kafka", "spring-kafka-test"]

# ===========================================
# CODE GENERATION BUNDLES
# ===========================================
# Lombok + MapStruct (very common combination)
codegen = ["lombok", "mapstruct"]
# Annotation processors for codegen
codegen-processors = ["lombok", "mapstruct-processor", "lombok-mapstruct-binding"]

# ===========================================
# gRPC BUNDLES (based on actual usage patterns)
# ===========================================
# gRPC client/server essentials
grpc-core = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub"]
# gRPC with API (for advanced usage)
grpc-full = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub", "grpc-api"]
# gRPC testing
grpc-testing = ["grpc-testing", "grpc-inprocess"]

# ===========================================
# OBSERVABILITY BUNDLES
# ===========================================
# OpenTelemetry essentials (common pattern across services)
otel = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp"]
# OpenTelemetry with gRPC instrumentation
otel-grpc = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc"]
# Full observability with JDBC and Kafka
otel-full = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc", "opentelemetry-jdbc", "opentelemetry-kafka"]

# ===========================================
# PROTOBUF BUNDLES
# ===========================================
# Protobuf essentials
protobuf = ["protobuf-java", "protobuf-java-util"]

# ===========================================
# JOOQ BUNDLES
# ===========================================
# JOOQ code generation
jooq-codegen = ["jooq", "jooq-codegen", "jooq-meta"]
```

> **📚 Reference**: See [Gradle Version Catalogs - Bundles](https://docs.gradle.org/current/userguide/version_catalogs.html) for official documentation on creating and using bundles.

**Note**: These bundle patterns are based on analysis of actual Bitso service repositories and represent the most common dependency groupings.

### Key Principles

1. **Single Source of Truth**: All versions defined once, referenced everywhere
2. **Semantic Grouping**: Organize by framework/module for easy navigation
3. **BOMs First**: Use Bill of Materials for transitive dependency management
4. **Type-Safe Access**: Gradle generates type-safe accessors from this file
5. **Version Ranges**: Use ranges for patch versions (e.g., `3.3.x`) when safe

## Bill of Materials (BOM) Strategy

BOMs manage transitive dependency versions automatically.

### Enforced Platforms in Root `build.gradle`

```groovy
dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom)
        mavenBom(libs.grpc.bom)
        mavenBom(libs.protobuf.bom)
    }
}
```

### Benefits

- **Automatic transitive resolution**: BOMs handle all indirect dependencies
- **Conflict prevention**: No version mismatch between related libraries
- **Update efficiency**: Update BOM version once, all transitive deps follow
- **Team alignment**: Everyone uses same dependency versions

## Dependency Declaration

### Use Bundles for Related Dependencies

Bundles group related libraries that are commonly used together. This makes build files cleaner, reduces duplication, and ensures consistent dependency sets across modules.

> **📚 Reference**: See [Gradle Version Catalogs - Bundles](https://docs.gradle.org/current/userguide/version_catalogs.html) for official documentation.

#### Why Use Bundles?

| Benefit | Description |
|---------|-------------|
| **Cleaner build files** | One line instead of many |
| **Consistency** | Same library set across all modules |
| **Easy updates** | Change bundle definition once, applies everywhere |
| **Self-documenting** | Bundle name describes purpose |

#### Bundle Declaration Examples (Based on Bitso Patterns)

These examples are based on analysis of actual Bitso service repositories:

```toml
[bundles]
# ===========================================
# TESTING BUNDLES
# ===========================================
# Most services use Spock with Spring (spock-core + spock-spring always together)
testing-spock = ["spock-core", "spock-spring"]

# Spring Boot test starter + Spock (most common pattern in Bitso services)
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]

# Integration testing with Testcontainers
# PostgreSQL, Kafka, and Spock are the most common Testcontainers modules
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]
testing-integration-kafka = ["testcontainers-spock", "testcontainers-postgresql", "testcontainers-kafka"]
testing-integration-aws = ["testcontainers-spock", "testcontainers-localstack"]

# Database migrations for testing (Flyway core + postgres always together)
testing-flyway = ["flyway-core", "flyway-database-postgresql"]

# ===========================================
# SERVICE BUNDLES
# ===========================================
# Web service essentials (actuator is almost always included with web)
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]

# Kafka messaging (spring-kafka + spring-kafka-test usually together)
spring-boot-kafka = ["spring-kafka", "spring-kafka-test"]

# ===========================================
# CODE GENERATION BUNDLES
# ===========================================
# Lombok + MapStruct (very common combination in Bitso services)
codegen = ["lombok", "mapstruct"]

# Annotation processors (with binding for Lombok+MapStruct interop)
codegen-processors = ["lombok", "mapstruct-processor", "lombok-mapstruct-binding"]

# ===========================================
# gRPC BUNDLES
# ===========================================
# gRPC essentials (netty-shaded + protobuf + stub always together)
grpc-core = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub"]

# gRPC testing
grpc-testing = ["grpc-testing", "grpc-inprocess"]

# ===========================================
# OBSERVABILITY BUNDLES (OpenTelemetry)
# ===========================================
# OpenTelemetry basics (spring-boot-starter + exporter)
otel = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp"]

# OpenTelemetry with gRPC instrumentation
otel-grpc = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc"]

# Full observability stack
otel-full = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc", "opentelemetry-jdbc"]

# ===========================================
# PROTOBUF BUNDLES
# ===========================================
# Protobuf essentials (java + util usually together)
protobuf = ["protobuf-java", "protobuf-java-util"]
```

#### Bundle Usage in Build Files

```groovy
// ❌ DON'T: Multiple individual declarations (verbose, error-prone)
dependencies {
    // Service dependencies - 2 lines
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.actuator
    
    // gRPC dependencies - 3 lines
    implementation libs.grpc.netty.shaded
    implementation libs.grpc.protobuf
    implementation libs.grpc.stub
    
    // Code generation - 2 lines
    annotationProcessor libs.lombok
    annotationProcessor libs.mapstruct.processor
    
    // Test dependencies - 7+ lines!
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.spock.core
    testImplementation libs.spock.spring
    testImplementation libs.testcontainers.spock
    testImplementation libs.testcontainers.postgresql
    testImplementation libs.flyway.core
    testImplementation libs.flyway.database.postgresql
}

// ✅ DO: Use bundles (clean, maintainable) - Same dependencies in 5 lines
dependencies {
    implementation libs.bundles.spring.boot.service
    implementation libs.bundles.grpc.core
    
    annotationProcessor libs.bundles.codegen.processors
    
    testImplementation libs.bundles.testing.spring
    testImplementation libs.bundles.testing.integration
    testImplementation libs.bundles.testing.flyway
}
```

#### Bundle Selection Guide

Choose the right bundles based on your module type:

| Module Type | Recommended Bundles |
|-------------|---------------------|
| **Web Service** | `spring-boot-service`, `grpc-core`, `otel`, `testing-spring`, `testing-integration` |
| **Library** | `codegen`, `testing-spock` |
| **gRPC Service** | `grpc-core`, `grpc-testing`, `otel-grpc` |
| **Kafka Consumer** | `spring-boot-kafka`, `testing-integration-kafka` |
| **Database Module** | `testing-flyway`, `testing-integration` |

```groovy
// Example: Typical Bitso gRPC Service
dependencies {
    // Core service
    implementation libs.bundles.spring.boot.service
    implementation libs.bundles.grpc.core
    implementation libs.bundles.protobuf
    
    // Code generation
    implementation libs.bundles.codegen
    annotationProcessor libs.bundles.codegen.processors
    
    // Observability
    implementation libs.bundles.otel.grpc
    
    // Testing
    testImplementation libs.bundles.testing.spring
    testImplementation libs.bundles.testing.integration
    testImplementation libs.bundles.testing.flyway
    testImplementation libs.bundles.grpc.testing
}
```

#### Combining Bundles with Individual Libraries

Bundles can be combined with individual libraries when needed:

```groovy
dependencies {
    // Use bundle for common libraries
    testImplementation libs.bundles.testing.full
    
    // Add specific libraries not in the bundle
    testImplementation libs.mockito.core
    testImplementation libs.awaitility
}
```

### Import BOMs Correctly

```groovy
// ❌ DON'T: Direct dependency on BOM (doesn't enforce transitive versions)
dependencies {
    implementation libs.spring.boot.bom
}

// ✅ DO: Import BOM via dependencyManagement
dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom)
    }
}
```

### Specify Versions Only When Needed

```groovy
// ❌ DON'T: Hardcode versions in build files
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web:3.5.7"
}

// ✅ DO: Use version catalog
dependencies {
    implementation libs.spring.boot.starter.web
}
```

## Version Management Strategies

### Pin Explicit Versions

Always use explicit versions for full control and predictability:

```toml
[versions]
java = "21"              # JDK version
spring-boot = "3.5.8"    # Spring Boot (3.4.x EOL end of 2025)
mockito = "5.10.0"       # Mockito
grpc = "1.77.0"          # gRPC
protobuf = "4.33.0"      # Protobuf
commons-lang3 = "3.18.0" # Apache Commons (CVE-2025-48924 fix)
```

**Benefits:**

- Full control over updates
- Reproducible builds
- No unexpected behavior from automatic updates
- Easier to track when dependencies change

### Override Transitive Versions (Last Resort)

Sometimes transitive dependencies conflict. Override only when necessary:

```groovy
dependencies {
    // Override transitive version
    implementation("some.lib:transitive-dep:1.2.3") {
        because "Fix for security issue XYZ"
    }
}
```

## Dependency Verification

### Check What Version Is Used

```bash
# Show dependency tree
./gradlew dependencies

# Show specific module dependencies
./gradlew :module:dependencies

# Show transitive dependency path
./gradlew :module:dependencyInsight --dependency org.springframework
```

### Verify No Conflicts

```bash
# Check for version conflicts
./gradlew dependencyInsight --dependency commons-lang3

# Show dependency tree with conflicts highlighted
./gradlew dependencies --warning-mode all
```

## Security Considerations

### Vulnerable Dependencies

Always use minimum secure versions in version catalog:

```toml
[versions]
# Security updates
commons-compress = "1.24.0"   # CVE-2023-42503 fix
commons-lang3 = "3.14.0"      # Includes security patches
jakarta-el = "2.0.1"          # CVE-2021-33813 fix
bouncycastle = "1.76"         # Cryptography security
```

### Force Security Patches

In root `build.gradle`:

```groovy
configurations.configureEach {
    resolutionStrategy {
        // Force specific versions for security
        force libs.commons.lang3
        force libs.commons.compress
        force libs.jakarta.el
        force libs.bouncycastle.bcprov.jdk18on
        force libs.bouncycastle.bcpkix.jdk18on
    }
}
```

## Version Compatibility Matrix

Typical compatibility for Bitso projects:

| Component | Version | Java | Notes |
|-----------|---------|------|-------|
| Spring Boot | **3.5.8** | 21+ | **REQUIRED** - 3.4.x EOL end of 2025 |
| Spring Cloud | **2025.0.0** | 21+ | Required for Boot 3.5.x |
| gRPC | 1.76.0 | 11+ | High performance |
| Protobuf | 4.33.0 | 8+ | Wire format compatible |
| Gradle | 8.14.3+ | 11+ | Build tool |
| Develocity | 0.2.8+ | - | Build insights |

### Jackson Compatibility

**IMPORTANT**: Jackson 3.x will be a major rewrite with new `tools.jackson.core` GAV. For now, stay on Jackson 2.x.

| Library | Version | Notes |
|---------|---------|-------|
| **jackson-bom** | **2.20.1** | Use BOM for all Jackson modules |
| **jackson-core** | **2.20.1** | Core parsing/generation |
| **jackson-databind** | **2.20.1** | Object binding |
| **jackson-datatype-*** | **2.20.1** | All data type modules |
| **jackson-annotations** | **2.20** | **Frozen** - no patch version (just `2.20`) |

**Override Pattern for Spring Boot projects:**

Use the Jackson BOM version to manage all Jackson dependencies consistently:

In `build.gradle`:

```groovy
allprojects {
    // Force Jackson 2.20.1 via BOM (annotations frozen at 2.20 without patch)
    ext['jackson-bom.version'] = libs.versions.jackson.get()
}
```

In `gradle/libs.versions.toml`:

```toml
[versions]
# Jackson BOM - use 2.20.1 for all modules (annotations is frozen at 2.20, no patch version)
jackson = "2.20.1"
```

**Note**: When Jackson 2.19.x is in use, upgrade to 2.20.1 via the BOM. The annotations module is frozen at `2.20` (no patch version - not `2.20.0` but just `2.20`).

### Redis/Jedis Compatibility (Spring Boot 3.5.x)

| Library | Version | Notes |
|---------|---------|-------|
| **bitso-commons-redis** | **4.2.1** | Required for Jedis 6.x (Spring Boot 3.5.x) |
| **jedis4-utils** | **3.0.0** | Required for locking/Lua scripts |
| Jedis (managed by Boot) | 6.x | Do not override manually |

**WARNING**: Using older `bitso-commons-redis` versions with Spring Boot 3.5.x causes:

```
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams redis.clients.jedis.params.SetParams.px(long)'
```

See `java/golden-paths/redis-jedis-compatibility.md` for detailed compatibility patterns.

### Testing and Build Tool Versions (December 2025)

#### Java 21 (Gradle 8.x) - Testing Libraries

| Library | Version | Notes |
|---------|---------|-------|
| Spock Framework | **2.4-groovy-4.0** | Stable release (Dec 11, 2025), requires Groovy 4.0.x |
| JUnit Jupiter | **5.14.1** | Released Oct 2025 |
| JUnit Platform | **1.14.1** | Released Oct 2025 |
| JaCoCo | **0.8.14** | Released Oct 2025 |
| Testcontainers | **1.21.4** | Stable 1.x; use **2.0.3** for 2.x repos |
| Groovy | **4.0.29** | Required for Spock 2.4-groovy-4.0 |
| Pitest | **1.22.0** | Mutation testing core |
| Pitest Gradle Plugin | **1.19.0-rc.2** | Released Oct 2025 |
| Pitest JUnit5 Plugin | **1.2.3** | For JUnit 5 support |
| SonarQube Plugin | **7.2.0.6526** | Released Dec 2025 |

#### Java 25 (Gradle 9.x) - Testing Libraries

| Library | Version | Notes |
|---------|---------|-------|
| Spock Framework | **2.4-groovy-5.0** | Must match Groovy 5.x |
| JUnit Jupiter | **5.14.1** | Released Oct 2025 |
| JUnit Platform | **1.14.1** | **Required** for Gradle 9 |
| JaCoCo | **0.8.14** | Released Oct 2025 |
| Testcontainers | **1.21.4** | Stable 1.x; use **2.0.3** for 2.x repos |
| Groovy | **5.0.3** | Required for Java 25 bytecode |

#### Java 25 (Gradle 9.x) - Build Plugins & Tools

**NOTE**: Build plugins require careful upgrade planning - they affect the entire build.

| Plugin/Tool | Version | Notes |
|-------------|---------|-------|
| Lombok | **1.18.42** | Required for Java 25 bytecode |
| Lombok Plugin | **9.1.0** | Freefair for Gradle 9 |
| SonarQube Plugin | **7.2.1.6560** | Gradle 9 compatible |
| Spotless Plugin | **8.1.0** | Gradle 9 compatible |
| palantir-java-format | **2.74.0** | Java 25 bytecode support |
| Flyway Plugin | **11.19.0** | Gradle 9 compatible |
| jOOQ Plugin | **10.1.1** | Gradle 9 compatible |
| Protobuf Plugin | **0.9.5** | Gradle 9 compatible |

**⚠️ CRITICAL for Java 25**: Never use `groovy-all` - rely on spock-core transitives.

Use `/improve-test-setup` command to upgrade testing libraries.
Use `/prepare-to-java-25` command for Java 25 upgrade workflow.

### Java 25 Compatibility Versions

**For projects upgrading to Java 25**, these versions are required:

| Component | Java 21 Version | Java 25 Version | Notes |
|-----------|-----------------|-----------------|-------|
| **Gradle** | 8.14.3 | **9.2.1** | Major version required |
| **Groovy** | 4.0.x | **5.0.3** | Java 25 requires Groovy 5.x |
| **Spock** | 2.4-groovy-4.0 | **2.4-groovy-5.0** | Must match Groovy version |
| **Lombok** | 1.18.x | **1.18.42** | Java 25 bytecode support |
| **Lombok Plugin** | 8.14.2 | **9.1.0** | Freefair plugin for Gradle 9 |
| **Spotless** | 6.x | **8.1.0** | Gradle 9 compatibility |
| **palantir-java-format** | - | **2.74.0** | Java 25 bytecode support (use with Spotless) |
| **SonarQube Plugin** | 6.x | **7.2.1.6560** | Gradle 9 compatibility |
| **Testcontainers 1.x** | 1.21.x | **1.21.4** | Latest 1.x version |
| **Testcontainers 2.x** | 2.0.2 | **2.0.3** | Docker socket fix |
| **ByteBuddy** | 1.14.x | **1.17.5+** | ASM 9.8 for Java 25 |
| **Flyway Plugin** | 10.x | **11.19.0** | Gradle 9 compatibility |
| **jOOQ Plugin** | 9.x | **10.1.1** | Gradle 9 compatibility |
| **Protobuf Plugin** | 0.9.4 | **0.9.5** | Gradle 9 compatibility |

**Groovy Version Strategy:**

- **Primary (recommended)**: Groovy 5.0.3 with Spock 2.4-groovy-5.0
- **Fallback (if issues)**: Groovy 4.0.29 with Spock 2.4-groovy-4.0

**⚠️ CRITICAL: Do NOT use `groovy-all`:**

The `groovy-all:5.0.3` artifact has broken transitive dependencies that reference Groovy 4.0.29 sub-modules. This causes class incompatibilities and test failures.

```groovy
// ❌ NEVER: groovy-all has broken transitive deps
testImplementation 'org.apache.groovy:groovy-all:5.0.3'

// ✅ CORRECT: Let spock-core bring in Groovy 5.0.3 transitively
testImplementation libs.spock.core
```

**Lombok Note**: The Freefair Lombok plugin requires Lombok 1.18.42 for Java 25. Configure via version catalog or in `build.gradle`:

```groovy
lombok {
    version = "1.18.42"
}
```

**ByteBuddy Note**: If your project uses Mockito or other libraries that depend on ByteBuddy, ensure ByteBuddy is at least 1.17.5. Earlier versions will fail with:

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available on current VM: net.bytebuddy.jar.asmjdkbridge.JdkClassWriter
```

Use `/prepare-to-java-25` command for the complete Java 25 upgrade workflow.

See `java/golden-paths/java-25-upgrade.md` for detailed patterns and troubleshooting.

## Spring Boot 3.5.x Upgrade Requirements

**IMPORTANT**: Spring Boot 3.4.x reaches end-of-life by end of 2025. All projects should upgrade to 3.5.8.

For complete upgrade workflow including priority order, side-by-side library upgrades, and dependency graph verification, use:

```text
/upgrade-to-recommended-versions
```

See `java/commands/upgrade-to-recommended-versions.md` for the full workflow with:

- Priority-ordered upgrades (Spring Boot → Endurance plugins → Security)
- Side-by-side library upgrades (bitso-rds-iam-authn 2.0.0, bitso-commons-redis 4.2.1)
- Dependency graph verification to prevent version downgrades
- Reference PRs with real-world examples

## Best Practices

### 1. Update BOMs Before Individual Libraries

```toml
# In gradle/libs.versions.toml - update this first
[versions]
spring-boot = "3.4.0"

# Then individual overrides if needed (usually not needed)
# spring-boot-starter-web = { ... }
```

### 2. Use Explicit Versions

```toml
[versions]
# ✅ Explicit versions - full control
spring-boot = "3.5.7"
mockito = "5.10.0"

# ❌ Ranges - unpredictable updates (DON'T use these)
# spring-boot = "3.5.+"
# mockito = "5.+"
```

### 3. Document Override Reasons

```groovy
dependencies {
    implementation("com.example:lib:1.2.3") {
        because "Override to fix CVE-2023-12345"
    }
}
```

### 4. Keep Version Catalog DRY

- Don't repeat versions
- Use version references consistently
- Group related versions together

### 5. Regular Updates

Run periodically:

```bash
# Check for outdated dependencies
./gradlew dependencyUpdates

# Update version catalog with recommendations, then validate
./gradlew build
```

## Common Version Issues

### Issue: "Could not find dependency"

**Cause**: Version catalog not defined or typo

**Fix**:

1. Check `gradle/libs.versions.toml` has the library
2. Verify spelling: `libs.some.lib.name` matches `[libraries]` section
3. Run `./gradlew help` to see generated accessors

### Issue: "Dependency version conflict"

**Cause**: Two libraries require different transitive versions

**Fix**:

1. Use BOM to manage conflict
2. If BOM unavailable, force version in `resolutionStrategy`
3. Check compatibility matrix before forcing

### Issue: "Security vulnerability detected"

**Cause**: Transitive dependency has known CVE

**Fix**:

1. Add force rule in `resolutionStrategy`
2. Update BOM if vulnerability in transitive dep
3. Document reason in commit message

## Migration: Hardcoded to Centralized Versions

If your project has hardcoded versions, follow this migration guide.

### Step 1: Find All Hardcoded Versions

```bash
# Find hardcoded versions in build files
grep -rE '"[a-zA-Z0-9.-]+:[a-zA-Z0-9.-]+:[0-9]+\.' --include="*.gradle" .

# Find versions defined as variables in build files
grep -rE "def .*Version\s*=" --include="*.gradle" .
```

### Step 2: Add to Version Catalog

Move each version to `gradle/libs.versions.toml`:

```toml
# Before: build.gradle had
# implementation "com.bitso.commons:redis:3.1.0"

# After: gradle/libs.versions.toml
[versions]
bitso-commons-redis = "4.2.1"

[libraries]
bitso-commons-redis = { module = "com.bitso.commons:redis", version.ref = "bitso-commons-redis" }
```

### Step 3: Update build.gradle Files

```groovy
// Before
implementation "com.bitso.commons:redis:3.1.0"

// After
implementation libs.bitso.commons.redis
```

**Note on accessor naming**: Gradle normalizes library keys from the version catalog.
Hyphens (`-`) become dots (`.`) in the accessor:

- `bitso-commons-redis` in TOML → `libs.bitso.commons.redis` in Groovy
- `spring-boot-starter-web` in TOML → `libs.spring.boot.starter.web` in Groovy

### Step 4: Verify

```bash
./gradlew clean build test --no-daemon
```

### Common Migration Patterns

| Before (Hardcoded) | After (Centralized) |
|--------------------|---------------------|
| `"org.springframework.boot:spring-boot-starter-web:3.5.8"` | `libs.spring.boot.starter.web` |
| `"com.bitso.commons:redis:3.1.0"` | `libs.bitso.commons.redis` |
| `"redis.clients:jedis:${jedisVersion}"` | Managed by Spring Boot BOM |
| `def redisVersion = "3.1.0"` | Delete - use version catalog |

---

## Links & References

- **Gradle Version Catalog**: <https://docs.gradle.org/current/userguide/platforms.html>
- **Dependency Management**: <https://docs.gradle.org/current/userguide/dependency_management.html>
- **Spring Boot BOM**: <https://docs.spring.io/spring-boot/docs/current/gradle-plugin/>
- **gRPC Java**: <https://grpc.io/docs/languages/java/>
- **Protobuf Java**: <https://protobuf.dev/reference/java/>

## Related Rules

- **Upgrade Command**: java/commands/upgrade-to-recommended-versions.md
- **Java 25 Upgrade**: java/commands/prepare-to-java-25.md - Java 25 upgrade workflow
- **Java 25 Golden Path**: java/golden-paths/java-25-upgrade.md - Java 25 patterns
- **Redis/Jedis Compatibility**: java/golden-paths/redis-jedis-compatibility.md
- **Spring Boot 3.5 Upgrade**: java/golden-paths/spring-boot-3.5-upgrade.md
- **Improve Test Setup**: java/commands/improve-test-setup.md - Testing library upgrades
- **Improve Test Coverage**: java/commands/improve-test-coverage.md - Write tests
- **Mutation Testing**: java/commands/improve-test-quality-with-mutation-testing.md
- **Vulnerability Golden Paths**: java/rules/java-vulnerability-golden-paths.md
- **Gradle Build Best Practices**: java/rules/java-gradle-best-practices.md
- **JaCoCo Code Coverage**: java/rules/java-jacoco-coverage.md
- **Java Testing Guidelines**: java/rules/java-testing-guidelines.md

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-versions-and-dependencies.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
