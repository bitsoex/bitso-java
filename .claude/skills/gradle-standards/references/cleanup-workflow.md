---
title: Cleanup Workflow
description: Step-by-step guide for identifying and removing unused dependencies
---

# Cleanup Workflow

Step-by-step guide for identifying and removing unused dependencies using Gradle Lint Plugin or static analysis.

## Contents

- [Approach Selection](#approach-selection)
- [Gradle Lint Plugin Method](#gradle-lint-plugin-method)
- [Static Analysis Method](#static-analysis-method)
- [Related](#related)

---
## Approach Selection

### Preferred: Gradle Lint Plugin

The Nebula Gradle Lint Plugin provides the most accurate analysis.

**Use when**:
- You want the most accurate unused dependency detection
- You need to find undeclared dependencies
- You want auto-fix capability
- The project can be modified to add the plugin

### Fallback: Static Analysis

Bash scripts that grep through build files.

**Use when**:
- Quick check without modifying the project
- Plugin integration is not possible
- Only checking version catalog entries

## Gradle Lint Plugin Method

### 1. Setup the Plugin

Add to `settings.gradle`:

```groovy
pluginManagement {
    plugins {
        id 'nebula.lint' version '21.1.3'  // For Gradle 9.x
        // id 'nebula.lint' version '20.6.2'  // For Gradle 8.x
    }
}
```

Add to root `build.gradle`:

```groovy
plugins {
    id 'nebula.lint'
}

allprojects {
    apply plugin: 'nebula.lint'
    gradleLint.rules = ['unused-dependency', 'undeclared-dependency']
}
```

### 2. Run Lint Analysis

```bash
./gradlew lintGradle 2>&1 | tee /tmp/lint.log

# Generate detailed report
./gradlew generateGradleLintReport
cat build/reports/gradleLint/*.txt
```

### 3. Understand Results

| Message | Meaning | Action |
|---------|---------|--------|
| "unused and can be removed" | Truly unused | **Safe to remove** |
| "artifact is empty" | Spring Boot starter | **Ignore** |
| "classes required by code directly" | Undeclared dependency | **Add explicitly** |
| "can be moved to runtimeOnly" | Not needed at compile time | **Review** |

### 4. Common False Positives

| Library | Why |
|---------|-----|
| `lombok` | Needs both `compileOnly` and `annotationProcessor` |
| `spring-boot-starter-*` | Empty by design |
| `*-bom` | Manages versions only |
| `groovy` / `spock-core` | Test framework infrastructure |

### 5. Run Auto-Fix

```bash
./gradlew fixGradleLint
```

### 6. Clean Up Hardcoded Versions

Auto-fix adds dependencies with hardcoded versions. Replace with catalog references:

**Before (added by auto-fix)**:
```groovy
implementation 'io.vavr:vavr:0.10.7'
```

**After (using version catalog)**:
```groovy
implementation libs.vavr
```

### 7. Restore Lombok (if removed)

Auto-fix may incorrectly remove Lombok's `compileOnly`:

```groovy
// Both declarations are required
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

### 8. Verify Build

```bash
./gradlew clean build -x test 2>&1 | tee /tmp/build.log | grep -E "BUILD|FAILED"
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "BUILD|FAILED"
```

## Static Analysis Method

For quick checks without plugin integration:

```bash
# Find unreferenced libraries in version catalog
cat gradle/libs.versions.toml | grep -E "^\[libraries\]" -A 1000 | \
    grep -E "^[a-z]" | cut -d'=' -f1 | tr '-' '.' | while read lib; do
        if ! grep -rq "libs\.$lib" --include="*.gradle" .; then
            echo "Possibly unused: $lib"
        fi
    done
```

## Related

- [Unused Detection](unused-detection.md) - Finding unused dependencies
- [Optimization](optimization.md) - Build optimization techniques
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/cleanup-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

