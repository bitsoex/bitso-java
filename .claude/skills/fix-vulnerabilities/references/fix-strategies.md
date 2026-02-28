# Fix Strategies for Dependabot Vulnerabilities

Use this hierarchy - always prefer higher-level solutions.

## Contents

- [Strategy 1: Update BOM/Platform Version (PREFERRED)](#strategy-1-update-bomplatform-version-preferred)
- [Strategy 1b: Override BOM-Managed Version Property](#strategy-1b-override-bom-managed-version-property)
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

## Strategy 1b: Override BOM-Managed Version Property

> **Prerequisite**: This strategy requires the `io.spring.dependency-management` Gradle plugin. If your project does not apply this plugin, skip to Strategy 2 or 3.

When upgrading the entire BOM (Strategy 1) isn't feasible — e.g., the latest BOM hasn't patched the vulnerability yet, or bumping the BOM introduces too many unrelated changes — you can override just the specific version property that the BOM uses internally.

This works because Spring Boot's BOM (`spring-boot-dependencies`) and other BOMs define versions via properties (e.g., `jackson-bom.version`, `snakeyaml.version`). The [Spring dependency-management-plugin](https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/#dependency-management-configuration-bom-import-override-property) allows overriding these properties.

### Step 1: Check if the dependency is managed by Spring Boot

Before using this strategy, verify the vulnerable dependency is actually managed by the Spring Boot BOM. Check the managed coordinates page for your Spring Boot version:

- **Spring Boot 3.5.x**: https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html
- **Spring Boot 4.0.x**: https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html

Search for the vulnerable dependency's `groupId:artifactId` on that page. If it appears, Spring Boot manages its version and this strategy applies. If it does **not** appear, the dependency is not managed by the BOM — skip to Strategy 2, 3, or 4 instead.

You can also verify from the command line:

```bash
# List all dependencies managed by Spring Boot's BOM
./gradlew dependencyManagement

# Check if a specific dependency is managed
./gradlew dependencyManagement | grep "commons-compress"
```

### Step 2: Apply the override

Choose one of the options below.

### Option A: Override via `ext` property (simplest)

```groovy
// In root build.gradle
ext['jackson-bom.version'] = '2.18.3'
ext['snakeyaml.version'] = '2.4'
```

### Option B: Override via `bomProperty` during BOM import

```groovy
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}") {
            // Security fix for CVE-XXXX-XXXXX
            bomProperty 'jackson-bom.version', '2.18.3'
            bomProperty 'snakeyaml.version', '2.4'
        }
    }
}
```

### Option C: Override multiple properties at once (map variant)

```groovy
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}") {
            bomProperties([
                'jackson-bom.version': '2.18.3',
                'snakeyaml.version': '2.4'
            ])
        }
    }
}
```

### Option D: Override via `dependencyManagement.dependencies` block

For BOMs that don't use properties, or when you need the override to appear in generated POMs:

```groovy
plugins.withId('io.spring.dependency-management') {
    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"
        }
        dependencies {
            // Overrides the BOM-managed version for security fix
            dependency "com.fasterxml.jackson.core:jackson-databind:2.18.3"
            dependency "org.yaml:snakeyaml:2.4"
        }
    }
}
```

### Finding the right property name

Spring Boot's BOM property names are documented in the dependency versions appendix for your Spring Boot version (e.g., [3.5.x properties](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/properties.html) or [4.0.x properties](https://docs.spring.io/spring-boot/appendix/dependency-versions/properties.html)). Common ones:

| Property | Controls |
|----------|----------|
| `jackson-bom.version` | All Jackson modules |
| `snakeyaml.version` | SnakeYAML |
| `tomcat.version` | Embedded Tomcat |
| `netty.version` | Netty |
| `logback.version` | Logback |
| `commons-compress.version` | commons-compress |
| `commons-lang3.version` | Apache Commons Lang |

**When to use**: Project uses the `io.spring.dependency-management` plugin, the vulnerable dependency is managed by a BOM, but you can't or don't want to upgrade the entire BOM version. The BOM uses a version property you can override.

**Advantage over Strategy 3 (Substitution)**: Works with the BOM's dependency management natively — all transitive dependencies of the overridden library are also resolved consistently. No need for `resolutionStrategy` workarounds.

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

For enforcing minimum versions. Constraints use the `implementation` configuration, which **only exists in projects that apply a Java plugin** (`java`, `java-library`, etc.). Using `allprojects { dependencies { constraints { implementation(...) } } }` will fail with `Configuration with name 'implementation' not found` in subprojects that don't have a Java plugin.

Instead, place constraints inside the existing `subprojects` block, guarded by a Java plugin check (e.g., `java-library` or `java`).

### Step 1: Add the library to the version catalog

```toml
# gradle/libs.versions.toml
[libraries]
commons-compress = { module = "org.apache.commons:commons-compress", version = "1.27.1" }
```

### Step 2: Add the constraint inside the existing `subprojects` block

Look for the existing `subprojects` block in the root `build.gradle` and add the constraint inside the `afterEvaluate` / Java plugin guard (typically `java-library`, but may be `java` in some projects):

```groovy
subprojects { Project subproject ->
    subproject.afterEvaluate {
        if (subproject.plugins.hasPlugin('java-library')) {
            // ... existing code (archivesName, bootJar config, etc.) ...

            dependencies {
                constraints {
                    implementation(libs.commons.compress) {
                        because "Security fix for CVE-2024-25710"
                    }
                }
            }
        }
    }
}
```

> **Important**: Do NOT use `allprojects { dependencies { constraints { implementation(...) } } }`. The `implementation` configuration does not exist in subprojects without a Java plugin, causing compilation errors. Always guard with a plugin check (e.g., `plugins.hasPlugin('java-library')` or `plugins.hasPlugin('java')`).

**When to use**: Set a floor version without forcing an exact version. The vulnerable dependency is transitive and not managed by a BOM or direct dependency.

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
| **BOM Property Override** | ✅ | ✅ | ✅ | Low |
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

- [Native Dependency Locking](.claude/skills/gradle-standards/references/native-dependency-locking.md) - Full locking documentation
- [Dependency Graph](dependency-graph.md) - Verification layer setup

## Document Your Strategy Choice

Always add comments explaining why a strategy was chosen:

```groovy
// Security fix for CVE-2025-48924 (commons-lang3)
// Strategy: Dependency substitution used because:
// - BOM property override: commons-lang3.version override didn't resolve transitive conflict
// - Version catalog: Already at 3.18.0, but transitive 3.16.0 still appears
substitute(module("org.apache.commons:commons-lang3"))
    .using(module("org.apache.commons:commons-lang3:3.18.0"))
    .because("Security fix for CVE-2025-48924")
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-vulnerabilities/references/fix-strategies.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

