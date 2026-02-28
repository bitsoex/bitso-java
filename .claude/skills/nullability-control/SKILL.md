---
name: nullability-control
description: >
  Java null safety using NullAway and JSpecify annotations. Covers Gradle setup,
  package-info.java creation with @NullMarked, annotation patterns, and migration
  from legacy annotations using OpenRewrite.
compatibility: Java 17+ projects using Gradle with Error Prone
metadata:
  version: "1.0.0"
  technology: java
  category: static-analysis
  tags:
    - java
    - nullability
    - nullaway
    - jspecify
    - static-analysis
---

# Nullability Control

Compile-time null safety for Java using [NullAway](https://github.com/uber/NullAway) and [JSpecify](https://jspecify.dev/) annotations.

## When to use this skill

- Setting up null safety for new Java projects
- Migrating existing code to JSpecify annotations
- Fixing NullAway errors and warnings
- Creating package-info.java files with @NullMarked
- Understanding null safety annotation patterns

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill)
- [Quick Start](#quick-start)
- [Key Principles](#key-principles)
- [Gradual Adoption Strategy](#gradual-adoption-strategy)
- [Package-info.java Pattern](#package-infojava-pattern)
- [Common Patterns](#common-patterns)
- [References](#references)
- [Lombok Compatibility](#lombok-compatibility)
- [Validation Results](#validation-results)
- [Related Skills](#related-skills)

### Available Resources

**📚 references/** - Detailed documentation
- [annotation guide](references/annotation-guide.md)
- [common errors](references/common-errors.md)
- [gradle setup](references/gradle-setup.md)
- [migration openrewrite](references/migration-openrewrite.md)
- [multi module setup](references/multi-module-setup.md)
- [package info templates](references/package-info-templates.md)
- [suppressing warnings](references/suppressing-warnings.md)

**📦 assets/** - Templates and resources
- [templates](assets/templates)

---

## Quick Start

### 1. Add Dependencies to Version Catalog

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

> **Note**: Also register the plugin in `settings.gradle` under `pluginManagement { plugins { ... } }`
>
> **Versions verified**: January 2026. Check for updates at [NullAway releases](https://github.com/uber/NullAway/releases) and [Error Prone releases](https://github.com/google/error-prone/releases).

### 2. Configure Error Prone + NullAway

In each module's `build.gradle` (not root project for multi-module):

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
    // Required for Error Prone 2.46.0+ on JDK 21 (OpenJDK only - not supported on Oracle JDK)
    options.compilerArgs += ["-XDaddTypeAnnotationsToSymbol=true"]

    options.errorprone {
        // CRITICAL: Disable all Error Prone checks first to prevent build failures
        // Error Prone enables many strict checks by default that fail on existing code
        disableAllChecks = true
        // Then enable only NullAway
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
    }
}
```

**Important Notes**:

- `disableAllChecks = true` is essential for existing projects. Error Prone enables many strict checks by default (`StringCaseLocaleUsage`, `JavaTimeDefaultTimeZone`, `ReturnValueIgnored`, etc.) that will fail most existing codebases.
- For multi-module projects, apply the Error Prone plugin in each subproject's `build.gradle`, not via the root `subprojects {}` block. See [multi-module-setup.md](references/multi-module-setup.md) for details.

### 3. Create package-info.java for Each Package

Every package needs a `package-info.java` file:

```java
@NullMarked
package com.example.mypackage;

import org.jspecify.annotations.NullMarked;
```

See [package-info-templates.md](references/package-info-templates.md) for complete templates.

## Key Principles

| Principle | Description |
|-----------|-------------|
| **Non-null by default** | In @NullMarked code, all types are non-null unless annotated @Nullable |
| **Use JSpecify** | Use `org.jspecify.annotations.Nullable`, not javax or JetBrains |
| **Package-level marking** | Apply @NullMarked to packages via package-info.java |
| **OnlyNullMarked config** | Use `-XepOpt:NullAway:OnlyNullMarked=true` (NullAway 0.12.3+) |
| **Explicit nullability** | Only annotate @Nullable; non-null is implicit |
| **Type-use placement** | Place @Nullable directly before the type (e.g., `@Nullable String`) |

**JDK Compatibility Note**: For full JSpecify support with generics, use JDK 22+ or JDK 21.0.8+ with `-XDaddTypeAnnotationsToSymbol=true`.

## Gradual Adoption Strategy

With `OnlyNullMarked` mode, NullAway only checks code in `@NullMarked` packages. This enables gradual adoption:

1. **Add NullAway to build** - Build passes immediately (no `@NullMarked` code yet)
2. **Add `package-info.java` to one package** - Fix null-safety issues in that package
3. **Repeat for each package** - Continue until all packages are covered

This approach allows incremental migration without blocking the build.

## Package-info.java Pattern

Every package in your codebase should have a `package-info.java`:

```java
@NullMarked
package com.bitso.myservice.domain;

import org.jspecify.annotations.NullMarked;
```

### Why This Matters

- NullAway only checks code marked as `@NullMarked`
- Without package-info.java, code is treated as unannotated
- Class-level @NullMarked is an alternative but package-level is preferred

### Creating package-info.java Files

For each package directory:

```bash
# Find all packages without package-info.java
find src/main/java -type d -exec sh -c '
  if [ -n "$(ls -A "$1"/*.java 2>/dev/null)" ] && [ ! -f "$1/package-info.java" ]; then
    echo "$1"
  fi
' _ {} \;
```

## Common Patterns

### Nullable Parameters

```java
public void process(@Nullable String input) {
    if (input != null) {
        // Safe to use input here
        System.out.println(input.length());
    }
}
```

### Nullable Return Values

```java
public @Nullable User findUser(String id) {
    return userMap.get(id);  // May return null
}
```

### Nullable Fields

```java
public class Request {
    private @Nullable String optionalField;
    private String requiredField;  // Non-null by default
}
```

### Collections with Nullable Elements

```java
// List that may contain null elements
List<@Nullable String> items;

// Nullable list (the list itself may be null)
@Nullable List<String> maybeList;
```

## References

| Reference | Description |
|-----------|-------------|
| [gradle-setup.md](references/gradle-setup.md) | Complete Gradle configuration |
| [multi-module-setup.md](references/multi-module-setup.md) | Multi-module project patterns |
| [package-info-templates.md](references/package-info-templates.md) | Package annotation templates |
| [annotation-guide.md](references/annotation-guide.md) | Full annotation reference |
| [migration-openrewrite.md](references/migration-openrewrite.md) | OpenRewrite migration guide |
| [common-errors.md](references/common-errors.md) | Error messages and solutions |
| [suppressing-warnings.md](references/suppressing-warnings.md) | Warning suppression patterns |

## Lombok Compatibility

Lombok works well with NullAway but requires configuration.

### Required Configuration

Create `lombok.config` at project root:

```properties
lombok.addLombokGeneratedAnnotation = true
```

This makes NullAway skip Lombok-generated code (constructors, getters, setters, builders).

### Lombok @NonNull Annotations

**Keep Lombok `@NonNull` annotations** - they serve a different purpose:

- Lombok's `@NonNull` generates **runtime null checks** (throws `NullPointerException`)
- JSpecify's `@Nullable` provides **compile-time null safety** (NullAway checks)
- These are complementary: Lombok for runtime protection, JSpecify for compile-time analysis

```java
// Good: Keep Lombok @NonNull for runtime checks
public record User(@NonNull String id, @NonNull String name) {}

// The lombok.config ensures NullAway doesn't analyze generated code
```

### Migration Strategy

| Annotation | Action |
|------------|--------|
| `lombok.NonNull` | **Keep** - provides runtime null checks |
| `javax.annotation.Nonnull` | **Remove** - redundant in `@NullMarked` code |
| `javax.annotation.Nullable` | **Replace** with `org.jspecify.annotations.Nullable` |
| `org.jetbrains.annotations.NotNull` | **Remove** - redundant in `@NullMarked` code |
| `org.jetbrains.annotations.Nullable` | **Replace** with `org.jspecify.annotations.Nullable` |

## Validation Results

This skill has been validated against multiple real-world multi-module Spring Boot projects with various configurations including protobuf, gRPC, JOOQ, and Lombok.

### Key Findings

1. **`disableAllChecks = true` is essential** - Error Prone has many strict checks enabled by default that fail most existing code
2. **JDK 21 requires `-XDaddTypeAnnotationsToSymbol=true`** - Error Prone 2.46.0+ fails without this flag on JDK 21
3. **Apply plugin per-subproject** - The Error Prone plugin must be in each module's `plugins {}` block, not via root `subprojects {}`
4. **Register plugin in settings.gradle** - Add `id 'net.ltgt.errorprone' version '4.1.0'` in `pluginManagement { plugins { } }`
5. **Lombok compatibility** - Works well; add `lombok.config` with `lombok.addLombokGeneratedAnnotation = true`
6. **Keep Lombok `@NonNull`** - These provide runtime null checks; only remove `javax.annotation.Nonnull` and similar
7. **Legacy annotation migration** - Remove `@Nonnull` from legacy packages (javax, JetBrains) since non-null is default in `@NullMarked` code
8. **OnlyNullMarked mode** - Enables gradual adoption; build passes immediately with no @NullMarked code

## Related Skills

| Skill | Purpose |
|-------|---------|
| [gradle-standards](.claude/skills/gradle-standards/SKILL.md) | Gradle configuration and version catalogs |
| [java-standards](.claude/skills/java-standards/SKILL.md) | General Java standards |
| [fix-sonarqube](.claude/skills/fix-sonarqube/SKILL.md) | SonarQube issues including null pointer rules |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

