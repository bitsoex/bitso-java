---
title: BOM Strategy
description: Bill of Materials configuration and best practices
---

# BOM Strategy

Bill of Materials (BOM) configuration for transitive dependency management.

## Contents

- [What is a BOM](#what-is-a-bom)
- [BOM Configuration](#bom-configuration)
- [Platform vs Enforce](#platform-vs-enforce)
- [Common BOMs](#common-boms)
- [Troubleshooting](#troubleshooting)
- [Related](#related)

---
## What is a BOM

A Bill of Materials (BOM) is a special POM that manages versions for a set of related dependencies.

### Benefits

| Benefit | Description |
|---------|-------------|
| **Automatic resolution** | BOM handles all transitive dependencies |
| **No conflicts** | Related libraries stay compatible |
| **Easy updates** | Update BOM version once, all deps follow |
| **Team alignment** | Everyone uses same dependency versions |

### How It Works

When you import a BOM, Gradle uses its version recommendations for all matching dependencies, including transitives you didn't declare directly.

## BOM Configuration

### In Version Catalog

```toml
[libraries]
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }
grpc-bom = { module = "io.grpc:grpc-bom", version.ref = "grpc" }
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }
```

### In Root build.gradle

```groovy
// ❌ WRONG: Direct dependency on BOM
dependencies {
    implementation libs.spring.boot.bom  // Doesn't enforce transitives!
}

// ✅ CORRECT: Import via dependencyManagement
dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom)
        mavenBom(libs.grpc.bom)
        mavenBom(libs.testcontainers.bom)
    }
}
```

### With Subprojects

```groovy
// In root build.gradle
subprojects {
    dependencyManagement {
        imports {
            mavenBom(libs.spring.boot.bom)
        }
    }
}
```

## Platform vs Enforce

Gradle offers two ways to import BOMs:

### platform() - Recommended

```groovy
dependencies {
    implementation platform(libs.spring.boot.bom)
}
```

- Applies version recommendations
- **Allows overrides** if you need a specific version
- Flexible, less strict

### enforcedPlatform() - Avoid

```groovy
dependencies {
    implementation enforcedPlatform(libs.spring.boot.bom)
}
```

- **Strictly forces** all versions from BOM
- Overrides even explicit version declarations
- Can cause unexpected behavior

### Recommendation

```groovy
// ✅ USE: platform() for flexibility
implementation platform(libs.spring.boot.bom)

// ⚠️ AVOID: enforcedPlatform() unless absolutely necessary
// implementation enforcedPlatform(libs.spring.boot.bom)
```

## Common BOMs

| BOM | Purpose | Key Dependencies |
|-----|---------|------------------|
| **spring-boot-dependencies** | Spring ecosystem | Spring Framework, Jackson, Tomcat, Jetty |
| **grpc-bom** | gRPC libraries | grpc-core, grpc-netty, grpc-protobuf |
| **testcontainers-bom** | Testcontainers | PostgreSQL, Kafka, LocalStack modules |
| **junit-bom** | JUnit 5 | Jupiter, Platform, Vintage |
| **jackson-bom** | Jackson JSON | Core, Databind, Annotations |
| **aws-sdk-bom** | AWS SDK v2 | All AWS service clients |

### Example: Full BOM Stack

```groovy
dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom)
        mavenBom(libs.grpc.bom)
        mavenBom(libs.testcontainers.bom)
        mavenBom(libs.aws.sdk.bom)
    }
}
```

## Troubleshooting

### BOM Not Applied

**Symptom**: Dependencies use wrong versions despite BOM import.

**Check**:
1. Is BOM imported via `dependencyManagement`?
2. Is BOM version current?
3. Any explicit version overriding BOM?

```bash
./gradlew dependencies --configuration compileClasspath
```

### Version Conflicts Between BOMs

**Symptom**: Two BOMs want different versions of same library.

**Solution**: Order matters - later BOMs override earlier:

```groovy
dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom)  // Lower priority
        mavenBom(libs.custom.bom)        // Higher priority (wins)
    }
}
```

### Transitive Not Resolved

**Symptom**: Transitive dependency has wrong version.

**Debug**:

```bash
./gradlew dependencyInsight --dependency commons-lang3
```

**Fix**: Add explicit dependency with BOM version:

```groovy
dependencies {
    implementation libs.commons.lang3  // Picks up BOM version
}
```

## Related

- [version-centralization.md](version-centralization.md) - Version management
- [resolution-strategies.md](resolution-strategies.md) - Conflict resolution
- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/bom-strategy.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

