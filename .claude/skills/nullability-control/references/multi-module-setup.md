# Multi-Module Gradle Project Setup

Guidance for adding NullAway to multi-module Gradle projects.

## Contents

- [Key Insight](#key-insight)
- [Recommended Approach](#recommended-approach)
- [Why Not Apply in Root Project?](#why-not-apply-in-root-project)
- [Shared Configuration Pattern](#shared-configuration-pattern)
- [Gradual Adoption Strategy](#gradual-adoption-strategy)
- [Module-by-Module Rollout](#module-by-module-rollout)
- [Verifying Setup](#verifying-setup)
- [Common Issues](#common-issues)

---
## Key Insight

The Error Prone plugin must be applied **in each subproject's build.gradle**, not via the root project's `subprojects {}` block. This is due to Gradle's plugin configuration ordering.

## Recommended Approach

### Step 1: Add Dependencies to Version Catalog

In `gradle/libs.versions.toml`:

```toml
[versions]
error-prone = "2.46.0"
nullaway = "0.13.1"
jspecify = "1.0.0"

[libraries]
error-prone-core = { module = "com.google.errorprone:error_prone_core", version.ref = "error-prone" }
nullaway = { module = "com.uber.nullaway:nullaway", version.ref = "nullaway" }
jspecify = { module = "org.jspecify:jspecify", version.ref = "jspecify" }

[plugins]
errorprone = { id = "net.ltgt.errorprone", version = "4.3.0" }
```

### Step 2: Apply Plugin in Each Subproject

In each subproject's `build.gradle`:

```groovy
plugins {
    id 'java'
    alias(libs.plugins.errorprone)
}

dependencies {
    // NullAway + Error Prone
    errorprone libs.error.prone.core
    errorprone libs.nullaway
    implementation libs.jspecify
}

import net.ltgt.gradle.errorprone.CheckSeverity

tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        // CRITICAL: Disable all Error Prone checks first
        disableAllChecks = true
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
    }
}
```

> **Note**: `disableAllChecks = true` is essential. Error Prone enables many strict checks
> by default that will fail most existing codebases. See [gradle-setup.md](gradle-setup.md)
> for details.

## Why Not Apply in Root Project?

Applying Error Prone via `subprojects {}` causes configuration ordering issues:

```groovy
// ❌ DOES NOT WORK - errorprone configuration not yet available
subprojects {
    plugins.withId('java') {
        apply plugin: 'net.ltgt.errorprone'
        dependencies {
            errorprone libs.error.prone.core  // Fails: method not found
        }
    }
}
```

The `errorprone` configuration is created when the plugin is applied, but the dependencies block executes before the plugin is fully initialized in the subproject context.

## Shared Configuration Pattern

If you want to reduce duplication, create a shared Gradle file:

### gradle/nullaway.gradle

> **Warning**: Shared Gradle files have limitations with version catalog imports.
> If you encounter `unable to resolve class CheckSeverity` errors, use the inline
> approach in each build.gradle instead.

```groovy
// Note: The plugin must already be applied in the subproject's plugins block
dependencies {
    def libs = rootProject.libs
    errorprone libs.error.prone.core
    errorprone libs.nullaway
    implementation libs.jspecify
}

import net.ltgt.gradle.errorprone.CheckSeverity

tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        disableAllChecks = true
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
    }
}
```

### Each subproject's build.gradle

```groovy
plugins {
    id 'java'
    alias(libs.plugins.errorprone)  // Still need to declare plugin
}

apply from: "${rootDir}/gradle/nullaway.gradle"
```

## Gradual Adoption Strategy

With `OnlyNullMarked` mode, NullAway only checks code in `@NullMarked` packages:

1. **Phase 1**: Add NullAway configuration to all subprojects
   - Build will pass immediately (no code is @NullMarked yet)

2. **Phase 2**: Add `package-info.java` to one package at a time
   - Fix any null-safety issues in that package
   - Commit and move to next package

3. **Phase 3**: Continue until all packages are @NullMarked

## Module-by-Module Rollout

For large projects, enable NullAway one module at a time:

```groovy
// In subproject build.gradle
plugins {
    id 'java'
    alias(libs.plugins.errorprone)  // Plugin must still be declared
}

def enableNullAway = project.hasProperty('nullaway.enable') ||
                     project.name in ['api', 'domain']

if (enableNullAway) {
    apply from: "${rootDir}/gradle/nullaway.gradle"
}
```

## Verifying Setup

After configuration, run:

```bash
# Should complete without NullAway errors (no @NullMarked code yet)
./gradlew compileJava

# Add a test package-info.java and rebuild
echo '@NullMarked
package com.example.test;

import org.jspecify.annotations.NullMarked;' > src/main/java/com/example/test/package-info.java

./gradlew compileJava
# Now NullAway will check the test package
```

## Common Issues

### "Could not find method errorprone()"

The Error Prone plugin is not applied before dependencies are declared. Apply the plugin in each subproject's `plugins {}` block.

### Build Passes But No NullAway Checks

This is expected with `OnlyNullMarked` mode when no packages have `@NullMarked`. Add `package-info.java` files to enable checking.

### Plugin Not Found

Add the plugin to settings.gradle or ensure it's in the version catalog:

```groovy
// settings.gradle (if not using version catalog)
pluginManagement {
    plugins {
        id 'net.ltgt.errorprone' version '4.1.0'
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/multi-module-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

