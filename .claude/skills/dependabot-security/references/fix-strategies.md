# Fix Strategies for Dependabot Vulnerabilities

Use this hierarchy - always prefer higher-level solutions.

## Contents

- [Strategy 1: Update BOM/Platform Version (PREFERRED)](#strategy-1-update-bomplatform-version-preferred)
- [Strategy 2: Update Direct Dependency in Version Catalog](#strategy-2-update-direct-dependency-in-version-catalog)
- [Strategy 3: Dependency Substitution (For Transitive Not in BOM)](#strategy-3-dependency-substitution-for-transitive-not-in-bom)
- [Strategy 3b: GAV Substitution (For Discontinued Packages)](#strategy-3b-gav-substitution-for-discontinued-packages)
- [Strategy 4: Dependency Constraints](#strategy-4-dependency-constraints)
- [Strategy 5: Force Rules (Use With Caution)](#strategy-5-force-rules-use-with-caution)
- [Strategy 6: Exclude and Add (Last Resort)](#strategy-6-exclude-and-add-last-resort)
- [Strategy Comparison](#strategy-comparison)
- [Verify Fix with Lockfiles](#verify-fix-with-lockfiles)
- [Document Your Strategy Choice](#document-your-strategy-choice)

---
## Strategy 1: Update BOM/Platform Version (PREFERRED)

If the vulnerable dependency is managed by a BOM (Spring Boot, gRPC, Protobuf), update the BOM version first.

```toml
# gradle/libs.versions.toml
[versions]
spring-boot = "3.5.9"   # Includes patched tomcat, jackson, logback
grpc = "1.78.0"          # Includes patched netty, protobuf
protobuf = "4.33.0"      # Patched protobuf-java
```

**When to use**: Vulnerable package is transitive of Spring Boot, gRPC, Protobuf, or other BOM.

## Strategy 2: Update Direct Dependency in Version Catalog

If it's a direct dependency (not transitive):

```toml
[versions]
# Security fix for CVE-XXXX-XXXXX
commons-lang3 = "3.18.0"    # Updated from 3.14.0
commons-compress = "1.27.1" # Updated from 1.26.0
```

**When to use**: The vulnerable package is declared directly in a module's `build.gradle`.

## Strategy 3: Dependency Substitution (For Transitive Not in BOM)

When the vulnerable dependency is transitive but NOT managed by any BOM:

```groovy
// In root build.gradle - applies to ALL configurations
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // Substitute old version with new - removes old from graph entirely
            substitute module("org.apache.commons:commons-compress")
                using module("org.apache.commons:commons-compress:${libs.versions.commons.compress.get()}")
                because "Security fix for CVE-2024-25710, CVE-2024-26308"

            // For artifact replacement (e.g., jdk15on -> jdk18on)
            substitute module("org.bouncycastle:bcprov-jdk15on")
                using module("org.bouncycastle:bcprov-jdk18on:${libs.versions.bouncycastle.get()}")
                because "Security fix - migrate to jdk18on"
        }
    }
}
```

**When to use**: Transitive dependency not covered by any BOM.

## Strategy 3b: GAV Substitution (For Discontinued Packages)

Some packages are discontinued with no upstream fix. Check the vulnerability golden paths rule for known cases (distributed as `.cursor/rules/java-vulnerability-golden-paths.mdc` in target repos).

**Example: lz4-java** (org.lz4:lz4-java is discontinued):

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute module("org.lz4:lz4-java")
                using module("at.yawk.lz4:lz4-java:${libs.versions.lz4java.get()}")
                because "Security fix - org.lz4:lz4-java is discontinued"
        }
    }
}
```

Also add to version catalog:

```toml
[versions]
lz4java = "1.10.1"

[libraries]
lz4-java = { module = "at.yawk.lz4:lz4-java", version.ref = "lz4java" }
```

## Strategy 4: Dependency Constraints

For enforcing minimum versions:

```groovy
allprojects {
    dependencies {
        constraints {
            implementation("org.apache.commons:commons-compress:1.27.1") {
                because "Security fix for CVE-2024-25710"
            }
        }
    }
}
```

**When to use**: Set a floor version without forcing an exact version.

## Strategy 5: Force Rules (Use With Caution)

Force rules affect runtime resolution but may not remove old versions from the graph:

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            force libs.commons.lang3
            force libs.commons.compress
        }
    }
}
```

**Warning**: Force rules alone may NOT fix dependency-review failures. Combine with substitution.

## Strategy 6: Exclude and Add (Last Resort)

When nothing else works:

```groovy
configurations.configureEach {
    exclude group: "org.apache.commons", module: "commons-compress"
}

dependencies {
    implementation "org.apache.commons:commons-compress:1.27.1"
}
```

## Strategy Comparison

| Strategy | Runtime | Dependency Graph | Lockfile | Complexity |
|----------|---------|------------------|----------|------------|
| **BOM Update** | ✅ | ✅ | ✅ | Low |
| **Version Catalog** | ✅ | ✅ | ✅ | Low |
| **Substitution** | ✅ | ✅ | ✅ | Medium |
| **Constraints** | ✅ | ⚠️ May vary | ✅ | Medium |
| **Force** | ✅ | ❌ May show both | ✅ | Low |
| **Exclude and Add** | ✅ | ✅ | ✅ | High |

## Verify Fix with Lockfiles

**Always regenerate and verify lockfiles after applying a security fix.**

### Verification Workflow

```bash
# 1. Check current vulnerable version in lockfile
grep "vulnerable-package" **/gradle.lockfile
# Example: org.apache.commons:commons-compress:1.26.0=runtimeClasspath

# 2. Apply fix (BOM update, substitution, force, etc.)

# 3. Regenerate lockfiles
./gradlew resolveAndLockAll --write-locks --refresh-dependencies --no-daemon --no-scan

# 4. PRIMARY: Verify patched version in lockfile
grep "vulnerable-package" **/gradle.lockfile
# Should show: org.apache.commons:commons-compress:1.27.1=runtimeClasspath

# 5. VERIFICATION LAYER: Run dependency graph to confirm
./gradlew -I gradle/dependency-graph-init.gradle \
    :ForceDependencyResolutionPlugin_resolveAllDependencies
# Check output matches lockfile version

# 6. Commit lockfiles
git add "**/gradle.lockfile"
git commit -m "fix: update commons-compress to 1.27.1 (CVE-2024-25710)"
```

### Two-Layer Verification

| Layer | Tool | Purpose |
|-------|------|---------|
| **Primary** | Lockfile (`grep`) | Source of truth - shows locked version |
| **Verification** | Dependency Graph | Confirms build resolves to locked version |

### Why This Matters

| Without Verification | With Two-Layer Verification |
|---------------------|----------------------------|
| Fix may not apply to all configurations | Lockfile shows exact configs fixed |
| Transitive may still pull old version | Lockfile captures resolved version |
| Build may resolve differently than locked | Dependency graph confirms resolution |

### Related

- [Native Dependency Locking](../../gradle-standards/references/native-dependency-locking.md) - Full locking documentation
- [Dependency Graph](dependency-graph.md) - Verification layer setup

## Document Your Strategy Choice

Always add comments explaining why a strategy was chosen:

```groovy
// Security fix for CVE-2025-48924 (commons-lang3)
// Strategy: Dependency substitution used because:
// - BOM update: Spring Boot BOM doesn't manage commons-lang3 directly
// - Version catalog: Already at 3.18.0, but transitive 3.16.0 still appears
substitute(module("org.apache.commons:commons-lang3"))
    .using(module("org.apache.commons:commons-lang3:3.18.0"))
    .because("Security fix for CVE-2025-48924")
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependabot-security/references/fix-strategies.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

