# Gradle Setup for NullAway

Complete configuration for Error Prone and NullAway in Gradle projects.

## Contents

- [Version Catalog Configuration](#version-catalog-configuration)
- [Settings Configuration](#settings-configuration)
- [Build Configuration](#build-configuration)
- [Multi-Module Projects](#multi-module-projects)
- [Advanced Configuration Options](#advanced-configuration-options)
- [JDK Compatibility](#jdk-compatibility)
- [Lombok Compatibility](#lombok-compatibility)
- [Troubleshooting](#troubleshooting)

---
## Version Catalog Configuration

Add to `gradle/libs.versions.toml`:

```toml
[versions]
errorProne = "2.46.0"
nullaway = "0.13.1"
jspecify = "1.0.0"

[libraries]
error-prone-core = { module = "com.google.errorprone:error_prone_core", version.ref = "errorProne" }
nullaway = { module = "com.uber.nullaway:nullaway", version.ref = "nullaway" }
jspecify = { module = "org.jspecify:jspecify", version.ref = "jspecify" }

[plugins]
errorprone = { id = "net.ltgt.errorprone", version = "4.3.0" }
```

## Settings Configuration

The Error Prone plugin must be registered in `settings.gradle` for version catalog plugin aliases to work:

```groovy
// settings.gradle
pluginManagement {
    plugins {
        // ... other plugins ...
        id 'net.ltgt.errorprone' version '4.3.0'
    }
}
```

## Build Configuration

### Basic Setup (Recommended)

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
    // Required for Error Prone 2.46.0+ on JDK 21
    options.compilerArgs += ["-XDaddTypeAnnotationsToSymbol=true"]

    options.errorprone {
        // CRITICAL: Disable all Error Prone checks first
        // Error Prone enables many strict checks by default that fail most existing code:
        // - StringCaseLocaleUsage (requires Locale on toUpperCase/toLowerCase)
        // - JavaTimeDefaultTimeZone (requires explicit timezone on LocalDateTime.now())
        // - ReturnValueIgnored (requires handling return values)
        // - And many more...
        disableAllChecks = true

        // Enable only NullAway at ERROR level
        check("NullAway", CheckSeverity.ERROR)

        // Use OnlyNullMarked mode (recommended, requires NullAway 0.12.3+)
        option("NullAway:OnlyNullMarked", "true")
    }
}
```

> **Why `disableAllChecks = true`?**
>
> Error Prone includes dozens of checks enabled by default. Many existing Java codebases
> will fail these checks immediately. By disabling all checks first, you can focus
> exclusively on NullAway for null safety. Optionally enable other Error Prone checks
> later once NullAway is fully adopted.

### Alternative: AnnotatedPackages Mode

For projects not fully migrated to @NullMarked:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "com.bitso")
    }
}
```

### Disabling NullAway on Test Code

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
    }

    // Disable on test code if desired
    if (name.toLowerCase().contains("test")) {
        options.errorprone {
            disable("NullAway")
        }
    }
}
```

## Multi-Module Projects

For multi-module Gradle projects, see [multi-module-setup.md](multi-module-setup.md) for the complete guide.

**Key requirement**: The Error Prone plugin must be declared in each subproject's `plugins {}` block. The `subprojects { apply plugin: ... }` approach does not work due to configuration ordering issues.

## Advanced Configuration Options

### JSpecify Mode (Experimental)

For full JSpecify semantics including generics checking:

```groovy
option("NullAway:JSpecifyMode", "true")
```

**Note**: Requires JDK 22+ or JDK 21.0.8+ with `-XDaddTypeAnnotationsToSymbol=true`.

### Handling Generated Code

Exclude generated code from NullAway analysis:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:TreatGeneratedAsUnannotated", "true")

        // Exclude specific paths
        excludedPaths = ".*/build/generated/.*"
    }
}
```

### Assertions Support

Enable assertions as null checks:

```groovy
option("NullAway:AssertsEnabled", "true")
```

### Test Library Assertions

Handle test assertion libraries (e.g., AssertJ):

```groovy
option("NullAway:HandleTestAssertionLibraries", "true")
```

### Contract Checking

Enable checking of @Contract annotations:

```groovy
option("NullAway:CheckContracts", "true")
```

### Custom Cast Method

Specify your castToNonNull method:

```groovy
option("NullAway:CastToNonNullMethod", "com.bitso.util.NullUtils.castToNonNull")
```

## JDK Compatibility

| JDK Version | Support Level |
|-------------|---------------|
| 17 | Basic support |
| 21 | **Requires** `-XDaddTypeAnnotationsToSymbol=true` (Error Prone 2.46.0+) |
| 22+ | Full JSpecify support |

**Critical for JDK 21**: Error Prone 2.46.0+ requires this compiler flag. Without it, the build fails with:

```
-XDaddTypeAnnotationsToSymbol=true is required by Error Prone on JDK 21
```

Add to `build.gradle`:

```groovy
tasks.withType(JavaCompile).configureEach {
    // Required for Error Prone 2.46.0+ on JDK 21
    options.compilerArgs += ["-XDaddTypeAnnotationsToSymbol=true"]

    options.errorprone {
        disableAllChecks = true
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
    }
}
```

## Lombok Compatibility

Lombok modifies the in-memory AST, which can cause compatibility issues. For best results:

### Required: Add lombok.config

Create `lombok.config` in your project root:

```properties
lombok.addLombokGeneratedAnnotation = true
```

This makes Lombok add `@lombok.Generated` to generated code, which NullAway will skip.

### Lombok's @NonNull - Keep It

Lombok's `@NonNull` generates **runtime null checks** (throws `NullPointerException` if null). This is complementary to JSpecify's compile-time analysis:

```java
// Good: Keep Lombok @NonNull for runtime protection
public record User(@lombok.NonNull String id, @lombok.NonNull String name) {}

// The lombok.config ensures NullAway skips Lombok-generated code
// JSpecify @NullMarked provides compile-time null safety analysis
```

**Key distinction**:

| Annotation | Purpose | When Used |
|------------|---------|-----------|
| `lombok.NonNull` | Runtime null check (throws NPE) | Keep for defensive programming |
| `javax.annotation.Nonnull` | Compile-time annotation (legacy) | **Remove** - redundant in @NullMarked |
| `org.jspecify.annotations.Nullable` | Compile-time null safety | Use for nullable types |

## Troubleshooting

### Error-Prone Not Running

Ensure the Error Prone plugin is applied:

```groovy
plugins {
    alias(libs.plugins.errorprone)
}
```

### NullAway Not Finding Annotated Code

Check that:

1. `@NullMarked` package-info.java exists
2. `OnlyNullMarked` option is set
3. JSpecify dependency is on classpath

### Version Conflicts

Use dependency resolution to force consistent versions:

```groovy
configurations.configureEach {
    resolutionStrategy {
        force libs.error.prone.core
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/gradle-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

