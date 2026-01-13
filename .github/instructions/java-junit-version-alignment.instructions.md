---
applyTo: "build.gradle,gradle/libs.versions.toml"
description: Resolving JUnit version conflicts in Java/Gradle projects
---

# JUnit Version Alignment Golden Path

This golden path documents how to resolve JUnit version conflicts that commonly occur when upgrading Spring Boot or using the `bitso.java.module` plugin.

## The Problem

When upgrading to Spring Boot 3.5.x, you may encounter test failures with errors like:

```
java.lang.NoClassDefFoundError: org/junit/jupiter/api/extension/TestInstantiationAwareExtension$ExtensionContextScope
```

or

```
java.lang.ClassNotFoundException: org.junit.jupiter.api.extension.TestInstantiationAwareExtension$ExtensionContextScope
```

### Root Cause

The `bitso.java.module` plugin forces JUnit 5.10.1, but Spring Boot 3.5.x brings JUnit 5.12.2+. This creates a version mismatch where some JUnit components are at 5.10.1 and others at 5.12.2+, causing class loading failures.

Typical dependency conflict:

```
+--- org.junit:junit-bom:5.11.4
|    +--- org.junit.jupiter:junit-jupiter:5.11.4 -> 5.10.1 (c)  ← CONFLICT
|    +--- org.junit.jupiter:junit-jupiter-api:5.11.4 -> 5.10.1 (c)  ← CONFLICT
|    +--- org.junit.platform:junit-platform-launcher:1.11.4 -> 1.12.2 (c)
|    +--- org.junit.jupiter:junit-jupiter-params:5.11.4 -> 5.12.2 (c)
|    \--- org.junit.jupiter:junit-jupiter-engine:5.11.4 -> 5.12.2 (c)
```

---

## Target Versions (December 2025)

| Component | Version | Notes |
|-----------|---------|-------|
| **JUnit Jupiter** | **5.14.1** | Released Oct 31, 2025 |
| **JUnit Platform** | **1.14.1** | Released Oct 31, 2025 |
| **Spock** | **2.4-groovy-4.0** | Stable release (Dec 11, 2025) |
| **Groovy** | **4.0.29** | Required for Spock 2.4 |

Use the `/improve-test-setup` command (see `java/commands/improve-test-setup.md`) to upgrade all testing libraries.

---

## Solution: Force Version Alignment

**IMPORTANT**: Always reference versions from the version catalog (`gradle/libs.versions.toml`). Never hardcode version strings in build files.

### Option 1: Module-Level Fix (Recommended for modules with Spock)

Add to each module's `build.gradle` that uses Spock tests:

```groovy
// Force JUnit version alignment across all configurations using version catalog
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
    // Test dependencies - use version catalog references
    testImplementation platform(libs.junit.bom)
    testImplementation libs.junit.jupiter
    testImplementation libs.spock.core
}

test {
    useJUnitPlatform()
}
```

### Option 2: Root-Level Fix (For all subprojects)

Add to root `build.gradle`:

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        configurations.configureEach {
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
            testRuntimeOnly libs.junit.platform.launcher
        }
    }
}
```

---

## When Using bitsoJavaModule.testRuntime()

If the module uses `bitsoJavaModule.testRuntime()`, you need to **remove** it and configure manually:

### Before (Causes Conflict)

```groovy
import bitso.endurance.java.BitsoTestRuntime

plugins {
    id 'java-library'
    id 'groovy'
    alias(libs.plugins.bitso.publish)
}

bitsoJavaModule {
    testRuntime(BitsoTestRuntime.SPOCK_2_4_M1_GROOVY_4)
}

dependencies {
    testImplementation libs.spock.core
}
```

### After (Fixed)

```groovy
plugins {
    id 'java-library'
    id 'groovy'
    alias(libs.plugins.bitso.publish)
}

// Note: Not using bitsoJavaModule.testRuntime() to avoid JUnit version conflicts

// Force JUnit version alignment using version catalog - NEVER hardcode versions
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
    // Test dependencies - use version catalog references
    testImplementation platform(libs.junit.bom)
    testImplementation libs.junit.jupiter
    testImplementation libs.spock.core
}

test {
    useJUnitPlatform()
}
```

---

## Verification

### Step 1: Check Dependency Resolution

```bash
./gradlew :module-name:dependencies --configuration testRuntimeClasspath | grep -i junit
```

**Expected Output** (all versions should be 5.14.1 / 1.14.1):

```
+--- org.junit:junit-bom:5.14.1
|    +--- org.junit.jupiter:junit-jupiter:5.14.1 (c)
|    +--- org.junit.jupiter:junit-jupiter-api:5.14.1 (c)
|    +--- org.junit.platform:junit-platform-launcher:1.14.1 (c)
|    +--- org.junit.jupiter:junit-jupiter-params:5.14.1 (c)
|    \--- org.junit.jupiter:junit-jupiter-engine:5.14.1 (c)
```

### Step 2: Run Tests

```bash
./gradlew :module-name:test --no-daemon
```

### Step 3: Full Dependency Graph Check

```bash
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Verify only 5.14.1 appears
grep -i "junit" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

---

## Real PR Examples

- [aum-reconciliation-v2/pull/730](https://github.com/bitsoex/aum-reconciliation-v2/pull/730) - JUnit 5.14.1 resolution strategy for multiple modules

---

## Troubleshooting

### Tests Still Fail After Fix

1. **Clean build cache**:

   ```bash
   ./gradlew clean --no-daemon
   rm -rf ~/.gradle/caches/modules-2/files-2.1/org.junit*
   ```

2. **Verify all modules are fixed**: Check each module with Spock tests has the resolution strategy

3. **Check for other plugins forcing versions**: Some plugins may also force JUnit versions

### Version Mismatch Persists

If `grep -i junit` still shows mixed versions:

1. Add explicit enforced platform import using version catalog:

   ```groovy
   dependencies {
       testImplementation enforcedPlatform(libs.junit.bom)
   }
   ```

2. Check for `configurations.all` vs `configurations.configureEach` - use `all` for immediate application

---

## Related Commands & Golden Paths

- **Improve Test Setup**: `java/commands/improve-test-setup.md` - Testing library upgrades and JaCoCo config
- **Improve Test Coverage**: `java/commands/improve-test-coverage.md` - Write tests to improve coverage
- **Mutation Testing**: `java/commands/improve-test-quality-with-mutation-testing.md` - Find weak tests
- **Spring Boot Upgrade**: `java/golden-paths/spring-boot-3.5-upgrade.md`
- **Upgrades Index**: `java/golden-paths/java-upgrades-golden-paths.md`
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Testing library versions

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/golden-paths/junit-version-alignment.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
