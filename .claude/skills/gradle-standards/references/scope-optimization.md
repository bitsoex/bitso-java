---
title: Dependency Scope Optimization
description: Optimize dependency scopes using the dependency-analysis plugin
---

# Dependency Scope Optimization

Use the [Dependency Analysis Gradle Plugin](https://github.com/autonomousapps/dependency-analysis-gradle-plugin) to identify over-broad scopes and optimize dependency declarations.

## Contents

- [Why Scope Matters](#why-scope-matters)
- [Setup](#setup)
- [Commands](#commands)
- [Scope Selection Guide](#scope-selection-guide)
- [Common Migrations](#common-migrations)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)
- [Related](#related)

---
## Why Scope Matters

Using the correct dependency scope prevents:

1. **Unnecessary transitives**: `implementation` hides transitives from consumers, reducing classpath pollution
2. **Binary compatibility issues**: `api` exposes types to consumers, creating API contracts
3. **Build time waste**: `compileOnly` and `runtimeOnly` reduce unnecessary compilation
4. **Classpath conflicts**: Minimal scopes reduce version conflict surface area

### Impact Example

```groovy
// ❌ Using implementation for everything
// Consumer gets: your-lib.jar (1MB)
// Consumer's classpath: potentially 50+ transitives

// ✅ Using minimal scopes
// Consumer gets: your-lib.jar (1MB)
// Consumer's classpath: only declared api dependencies
```

## Setup

### Add Plugin

```groovy
// build.gradle (root)
plugins {
    id 'com.autonomousapps.dependency-analysis' version '3.5.1'
}
```

### For Multi-Module Projects

```groovy
// build.gradle (root)
plugins {
    id 'com.autonomousapps.dependency-analysis' version '3.5.1'
}

// Plugin automatically applies to standard subprojects.
// For projects with custom plugins (e.g., bitso.java.module),
// you may need to apply explicitly in subprojects:
subprojects {
    plugins.withType(JavaPlugin) {
        apply plugin: 'com.autonomousapps.dependency-analysis'
    }
}
```

### Plugin Version in settings.gradle

```groovy
// settings.gradle (pluginManagement block)
plugins {
    id 'com.autonomousapps.dependency-analysis' version dependencyAnalysisPluginVersion
}

// gradle.properties
dependencyAnalysisPluginVersion=3.5.1
```

## Commands

### Analyze All Projects

```bash
# Generate comprehensive advice
./gradlew buildHealth

# View report at: build/reports/dependency-analysis/build-health-report.txt
```

### Analyze Specific Module

```bash
./gradlew :my-module:projectHealth

# View advice for single module
cat my-module/build/reports/dependency-analysis/advice-all-variants.txt
```

### Understand a Suggestion

```bash
# Why is a change suggested for this dependency?
./gradlew :my-module:reason --id com.fasterxml.jackson.core:jackson-databind

# Shows: where used, current scope, suggested scope, reasoning
```

### Apply Fixes Automatically

```bash
# Apply all suggestions (REVIEW CHANGES CAREFULLY!)
./gradlew fixDependencies

# Then review:
git diff build.gradle
```

## Scope Selection Guide

| Scope | Use When | Exposed to Consumers |
|-------|----------|---------------------|
| `implementation` | Internal use only | No |
| `api` | Type exposed in public API | Yes |
| `compileOnly` | Compile-time only (Lombok, annotations) | No |
| `runtimeOnly` | Runtime only (drivers, logging impl) | No |
| `testImplementation` | Test dependencies | N/A |
| `testRuntimeOnly` | Test runtime (engines) | N/A |
| `annotationProcessor` | Annotation processing | No |

### Decision Tree

```
Is the dependency type part of your public API?
├─ YES → Use `api`
└─ NO → Is it needed at compile time?
        ├─ NO → Use `runtimeOnly`
        └─ YES → Is it only for annotation processing?
                ├─ YES → Use `compileOnly` + `annotationProcessor`
                └─ NO → Use `implementation`
```

## Common Migrations

### Lombok (Compile-Time Only)

```groovy
// ❌ BEFORE
implementation 'org.projectlombok:lombok:1.18.38'

// ✅ AFTER
compileOnly libs.lombok
annotationProcessor libs.lombok
testCompileOnly libs.lombok
testAnnotationProcessor libs.lombok
```

### Database Drivers (Runtime Only)

```groovy
// ❌ BEFORE
implementation 'org.postgresql:postgresql:42.7.7'

// ✅ AFTER
runtimeOnly libs.postgresql
```

### Logging (API vs Implementation)

```groovy
// ❌ BEFORE
implementation 'org.slf4j:slf4j-api:2.0.16'
implementation 'ch.qos.logback:logback-classic:1.5.18'

// ✅ AFTER
implementation libs.slf4j.api          // API needed at compile time
runtimeOnly libs.logback.classic       // Implementation at runtime only
```

### MapStruct (Annotation Processor)

```groovy
// ❌ BEFORE
implementation 'org.mapstruct:mapstruct:1.6.6'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.6'

// ✅ AFTER
implementation libs.mapstruct          // Runtime types needed
compileOnly libs.mapstruct             // Annotations at compile time
annotationProcessor libs.mapstruct.processor
```

### Jackson (Internal vs API)

```groovy
// If ObjectMapper is internal
implementation libs.jackson.databind

// If you expose Jackson types in your API
api libs.jackson.databind
```

### Spring Boot in Library Module

```groovy
// If you're a library exposing Spring components
api libs.spring.boot.starter.web

// If you're an application using Spring internally
implementation libs.spring.boot.starter.web
```

## Configuration

### Basic Configuration

```groovy
dependencyAnalysis {
    issues {
        all {
            onAny {
                severity('warn')  // 'fail', 'warn', or 'ignore'
            }
        }
    }
}
```

### Stricter for Production

```groovy
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies { severity('fail') }
            onIncorrectConfiguration { severity('fail') }
            onUsedTransitiveDependencies { severity('warn') }
        }
    }
}
```

### Bundle Related Dependencies

```groovy
dependencyAnalysis {
    structure {
        bundle('jackson') {
            primary('com.fasterxml.jackson.core:jackson-databind')
            includeGroup('com.fasterxml.jackson.core')
            includeGroup('com.fasterxml.jackson.datatype')
        }
    }
}
```

### Exclude False Positives

```groovy
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                // Exclude runtime-only dependencies that appear unused
                exclude('org.postgresql:postgresql')
            }
        }
    }
}
```

## Troubleshooting

### "Dependency declared but not used"

1. Check if it's used only at runtime (use `runtimeOnly`)
2. Check if it's used by annotation processors
3. Check if it's a false positive due to reflection
4. If truly unused, remove it

### "Should be api, not implementation"

1. Check if the type appears in your public API (method signatures, return types)
2. For library modules, use `api` if exposed
3. For application modules, `implementation` is usually correct

### "Used transitive dependency should be declared"

1. Add explicit declaration for visibility
2. Helps with version management via catalog
3. Makes dependency upgrades explicit

### Skip Analysis Temporarily

```bash
# Skip for one build
./gradlew build -Ddependency.analysis.skip=true
```

### Debug Analysis

```bash
# Verbose output
./gradlew buildHealth --info
```

## Related

- [Template: dependency-analysis.gradle](../../../templates/dependency-analysis.gradle)
- [optimization.md](optimization.md) - Build optimization overview
- [../SKILL.md](.claude/skills/gradle-standards/SKILL.md) - Gradle standards overview
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/scope-optimization.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

