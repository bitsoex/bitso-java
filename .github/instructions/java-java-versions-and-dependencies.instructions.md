---
applyTo: "gradle/libs.versions.toml,build.gradle,settings.gradle"
description: Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
---

# Java Library Versions & Dependency Management

Standards for managing library versions, dependency constraints, and Bill of Materials (BOM) in Java/Gradle projects.

## Version Catalog (`gradle/libs.versions.toml`)

Central repository for all dependency versions. Never hardcode versions in build files.

### Structure

```groovy
[versions]
gradle = "8.14.3"
java = "21"
spring-boot = "3.5.8"
jacoco = "0.8.14"
protobuf = "4.33.0"
grpc = "1.76.0"
junit-jupiter = "5.10.x"

# Security updates
commons-lang3 = "3.18.0"

[libraries]
spring-boot-bom = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }
grpc-bom = { module = "io.grpc:grpc-bom", version.ref = "grpc" }
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }

[bundles]
spring-boot-web = ["spring-boot-starter-web"]
testing = ["junit-jupiter"]
```

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

```groovy
// ❌ DON'T: Multiple individual declarations
dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.spring.boot.starter.security
}

// ✅ DO: Use bundles
dependencies {
    implementation libs.bundles.spring.boot.web
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

```groovy
[versions]
java = "21"              # JDK version
spring-boot = "3.5.8"    # Spring Boot (3.4.x EOL end of 2025)
mockito = "5.10.0"       # Mockito
grpc = "1.76.0"          # gRPC
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

```groovy
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
| Spring Cloud | 2024.0.x | 21+ | Compatible with Boot 3.5+ |
| gRPC | 1.76.0 | 11+ | High performance |
| Protobuf | 4.33.0 | 8+ | Wire format compatible |
| JaCoCo | 0.8.14+ | 8+ | Code coverage |
| Gradle | 8.14.3+ | 11+ | Build tool |
| Develocity | 0.2.8+ | - | Build insights |

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

```groovy
// Update this first
spring-boot = "3.4.0"

// Then individual overrides if needed
spring-boot-starter-web = { ... }  # Usually not needed
```

### 2. Use Explicit Versions

```groovy
[versions]
// ✅ Explicit versions - full control
spring-boot = "3.5.7"
mockito = "5.10.0"

// ❌ Ranges - unpredictable updates
spring-boot = "3.5.+"
mockito = "5.+"
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

## Links & References

- **Gradle Version Catalog**: <https://docs.gradle.org/current/userguide/platforms.html>
- **Dependency Management**: <https://docs.gradle.org/current/userguide/dependency_management.html>
- **Spring Boot BOM**: <https://docs.spring.io/spring-boot/docs/current/gradle-plugin/>
- **gRPC Java**: <https://grpc.io/docs/languages/java/>
- **Protobuf Java**: <https://protobuf.dev/reference/java/>

## Related Rules

- **Upgrade Command**: java/commands/upgrade-to-recommended-versions.md
- **Vulnerability Golden Paths**: java/rules/java-vulnerability-golden-paths.md
- **Gradle Build Best Practices**: java/rules/java-gradle-best-practices.md
- **JaCoCo Code Coverage**: java/rules/java-jacoco-coverage.md
- **Java Testing Guidelines**: java/rules/java-testing-guidelines.md
