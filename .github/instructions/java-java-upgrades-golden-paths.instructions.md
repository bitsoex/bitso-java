---
applyTo: "gradle/libs.versions.toml,build.gradle,settings.gradle,gradle.properties"
description: Index of proven upgrade patterns for Java/Gradle projects
---

# Java Upgrades Golden Paths

This document serves as an index for all Java upgrade golden paths. Each golden path documents proven patterns from real PRs for specific upgrade scenarios.

## Related Documents

- **Vulnerability Fixes**: `java/rules/java-vulnerability-golden-paths.md` - Security fix patterns
- **Upgrade Command**: `java/commands/upgrade-to-recommended-versions.md` - Automated upgrade workflow
- **Java 25 Command**: `java/commands/prepare-to-java-25.md` - Java 25 upgrade workflow
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - BOMs, version catalog

---

## Quick Reference: Current Recommended Versions

### Java 21 (Gradle 8.x)

| Component | Version | Notes |
|-----------|---------|-------|
| **Spring Boot** | **3.5.8** | CRITICAL - 3.4.x EOL end of 2025 |
| **Spring Cloud** | **2025.0.0** | Required for Spring Boot 3.5.x |
| **Spring Dependency Management** | **1.1.7** | Required with Spring Boot 3.5.x |
| **Gradle** | **8.14.3** | Build tool |
| **Java** | **21** | LTS version |
| **JUnit** | **5.14.1** | Testing (via BOM) |
| **JUnit Platform** | **1.14.1** | Testing platform |
| **Spock** | **2.4-groovy-4.0** | Groovy testing framework |
| **Groovy** | **4.0.29** | Required for Spock 2.4-groovy-4.0 |
| **JaCoCo** | **0.8.14** | Code coverage |
| **SonarQube Plugin** | **7.2.0.6526** | Code analysis |
| **Develocity Plugin** | **0.2.8** | Build insights |
| **Publish Plugin** | **0.3.6** | Publishing |
| **bitso-commons-redis** | **4.2.1** | Redis (Jedis 6 compatibility) |
| **jedis4-utils** | **3.0.0** | Jedis utilities |

### Java 25 (Gradle 9.x)

| Component | Version | Notes |
|-----------|---------|-------|
| **Gradle** | **9.2.1** | Major version required for Java 25 |
| **Java** | **25** | Early access |
| **Spring Boot** | **3.5.8** | Compatible with Java 25 |
| **Spring Cloud** | **2025.0.0** | Required for Boot 3.5.x |
| **Spring Dependency Management** | **1.1.7** | BOM management |
| **Groovy** | **5.0.3** | Required for Java 25 bytecode |
| **Spock** | **2.4-groovy-5.0** | Must match Groovy 5.x |
| **Lombok** | **1.18.42** | Java 25 bytecode support |
| **Lombok Plugin** | **9.1.0** | Freefair plugin for Gradle 9 |
| **Spotless** | **8.1.0** | Gradle 9 compatibility |
| **palantir-java-format** | **2.74.0** | Java 25 bytecode support |
| **SonarQube Plugin** | **7.2.1.6560** | Gradle 9 compatibility |
| **Testcontainers 1.x** | **1.21.4** | Latest 1.x version |
| **Testcontainers 2.x** | **2.0.3** | Docker socket fix |
| **ByteBuddy** | **1.17.5+** | ASM 9.8 for Java 25 |
| **Flyway Plugin** | **11.19.0** | Gradle 9 compatibility |
| **jOOQ Plugin** | **10.1.1** | Gradle 9 compatibility |
| **Protobuf Plugin** | **0.9.5** | Gradle 9 compatibility |

**⚠️ CRITICAL for Java 25**: Never use `groovy-all` - rely on spock-core transitives. Add `groovy-json` explicitly if tests use JsonSlurper/JsonOutput.

---

## Golden Paths Index

### 1. Spring Boot 3.5.x Upgrade

**File**: `java/golden-paths/spring-boot-3.5-upgrade.md`

**When to use**: Upgrading from Spring Boot 3.4.x or earlier to 3.5.x

**Key considerations**:

- Spring Cloud compatibility (requires 2025.0.0)
- JUnit version alignment (5.14.1)
- Spock compatibility (2.4-groovy-4.0 for Java 21, 2.4-groovy-5.0 for Java 25)
- Jacoco compatibility (0.8.14)

### 2. JUnit Version Alignment

**File**: `java/golden-paths/junit-version-alignment.md`

**When to use**: Test failures due to JUnit version conflicts

**Key considerations**:

- bitso.java.module plugin forces JUnit 5.10.1
- Spring Boot 3.5.x brings JUnit 5.12.2+
- Resolution strategy to force consistent versions

### 3. Gradle Upgrade (Covered in Spring Boot Upgrade)

Gradle upgrade guidance is included in the Spring Boot 3.5 upgrade golden path, as Gradle 8.14.3 is required for Spring Boot 3.5.x compatibility.

**Key considerations**:

- Plugin compatibility
- Develocity plugin version (0.2.8)
- JUnit Platform Launcher requirement

### 4. Redis/Jedis Version Compatibility

**File**: `java/golden-paths/redis-jedis-compatibility.md`

**When to use**: Redis `NoSuchMethodError` during Spring Boot upgrade, or any Jedis version conflicts

**Key considerations**:

- Spring Boot 3.5.x uses Jedis 6.x
- bitso-commons-redis must be 4.2.1+ for Jedis 6 compatibility
- jedis4-utils must be 3.0.0+ for Jedis 6 compatibility
- Common error: `SetParams.px(long)` NoSuchMethodError

**Real PR Examples**:

- [assets/pull/640](https://github.com/bitsoex/assets/pull/640) - Redis SetParams.px(long) fix
- [consumer-wallet/pull/770](https://github.com/bitsoex/consumer-wallet/pull/770) - Bump redis library to 4.2.0
- [bff-services/pull/1428](https://github.com/bitsoex/bff-services/pull/1428) - Update Jedis version

### 5. Bitso Libraries Upgrade (Covered in Vulnerability Fixes)

Bitso library upgrade guidance is included in the vulnerability golden path.

**Key considerations**:

- bitso-rds-iam-authn (Hikari 6 compatibility)
- bitso-commons-redis (Jedis 6 compatibility) - See Redis golden path
- bitso.publish plugin versions (0.3.6)

### 6. Java 25 Upgrade

**File**: `java/golden-paths/java-25-upgrade.md`

**Command**: `java/commands/prepare-to-java-25.md`

**When to use**: Upgrading from Java 21 to Java 25

**Key considerations**:

- Gradle 9.2.1+ required (major version bump)
- Groovy 5.0.3 required (Java 25 bytecode compatibility)
- Spock 2.4-groovy-5.0 (must match Groovy version)
- Lombok 1.18.42 (Java 25 bytecode support)
- Lombok plugin 9.1.0 (Freefair plugin for Gradle 9)
- Spotless 8.1.0, SonarQube 7.2.x (Gradle 9 compatibility)
- Testcontainers 1.21.4 (1.x) or 2.0.3 (2.x)
- ByteBuddy 1.17.5+ (ASM 9.8 for Java 25 class files)

**Real PR Examples**:

- [motorsito/tree/java25](https://github.com/bitsoex/motorsito/tree/java25) - Complete Java 25 upgrade

**Fallback option**: If Groovy 5.x causes issues, use Groovy 4.0.29 (last 4.x version) with Spock 2.4-groovy-4.0

---

## Decision Tree: Which Golden Path?

```
What are you upgrading?
├── Java version (21 → 25)
│   └── Use: java-25-upgrade.md
│       Command: /prepare-to-java-25
│
├── Spring Boot version
│   └── Use: spring-boot-3.5-upgrade.md
│
├── Gradle version
│   ├── To Gradle 8.x (for Spring Boot 3.5.x)
│   │   └── Use: spring-boot-3.5-upgrade.md
│   └── To Gradle 9.x (for Java 25)
│       └── Use: java-25-upgrade.md
│
├── Test framework (JUnit/Spock)
│   └── Use: junit-version-alignment.md
│
├── Groovy version (4.x → 5.x)
│   └── Use: java-25-upgrade.md (Groovy 5.x required for Java 25)
│
├── Redis/Jedis libraries
│   └── Use: redis-jedis-compatibility.md
│
├── NoSuchMethodError with Redis/Jedis?
│   └── Use: redis-jedis-compatibility.md
│
├── ByteBuddy/ASM errors on Java 25?
│   └── Use: java-25-upgrade.md (ByteBuddy 1.17.5+ required)
│
├── Bitso internal libraries
│   ├── Redis-related (bitso-commons-redis, jedis4-utils)
│   │   └── Use: redis-jedis-compatibility.md
│   └── Other Bitso libraries
│       └── Use: java/rules/java-vulnerability-golden-paths.md
│
└── Security vulnerabilities
    └── Use: java/rules/java-vulnerability-golden-paths.md
```

---

## Verification: Dependency Graph Check

**CRITICAL**: Always verify dependency resolution using the dependency graph plugin:

```bash
# Generate dependency graph - this is what GitHub will actually see
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Check for version conflicts
grep -i "junit\|spock\|spring" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

---

## Common Upgrade Patterns

### Pattern 1: Force Version Alignment

When multiple dependencies bring different versions of the same library:

```groovy
// Versions should be defined in gradle/libs.versions.toml:
// [versions]
// junit-jupiter = "5.14.1"
// junit-platform = "1.14.1"

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

### Pattern 2: BOM Platform Import

For libraries managed by BOMs:

```groovy
// Use version catalog reference for BOM
dependencies {
    testImplementation platform(libs.junit.bom)
    testImplementation 'org.junit.jupiter:junit-jupiter'
}
```

### Pattern 3: Bypass Plugin Defaults

When a plugin forces incompatible versions:

```groovy
// Instead of using plugin's testRuntime configuration
// bitsoJavaModule.testRuntime(BitsoTestRuntime.SPOCK_2_4_M1_GROOVY_4)

// Configure manually with correct versions from version catalog
dependencies {
    testImplementation platform(libs.junit.bom)
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation libs.spock.core
}

test {
    useJUnitPlatform()
}
```

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/golden-paths/java-upgrades-golden-paths.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
