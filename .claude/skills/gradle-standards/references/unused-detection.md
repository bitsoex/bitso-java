---
title: Unused Detection
description: Methods for finding unused dependencies in Gradle projects
---

# Unused Detection

Methods for finding unused dependencies in Gradle projects.

## Contents

- [Using Gradle Lint Plugin](#using-gradle-lint-plugin)
- [Understanding False Positives](#understanding-false-positives)
- [Static Analysis Scripts](#static-analysis-scripts)
- [Comparison: Gradle Lint vs Static Analysis](#comparison-gradle-lint-vs-static-analysis)
- [Best Practices](#best-practices)
- [Related](#related)

---
## Using Gradle Lint Plugin

### Violation Types

| Violation | Description | Action |
|-----------|-------------|--------|
| `unused-dependency` | Dependency declared but not used | Remove |
| `undeclared-dependency` | Class used but not declared | Add explicitly |
| `duplicate-dependency-class` | Same class in multiple JARs | Resolve conflict |

### Running Analysis

```bash
# Full analysis
./gradlew lintGradle

# Generate report
./gradlew generateGradleLintReport

# View report
cat build/reports/gradleLint/*.txt
```

## Understanding False Positives

### Libraries Referenced via Plugins

Some libraries are used by Gradle plugins but not directly referenced:

```groovy
plugins {
    id 'org.springframework.boot'  // Uses spring-boot-dependencies
}
```

### Libraries Referenced via BOMs

Platform/BOM dependencies manage transitive versions:

```groovy
dependencies {
    implementation platform(libs.spring.boot.dependencies)
}
```

### Libraries in Buildscript Classpath

```groovy
buildscript {
    dependencies {
        classpath libs.some.plugin  // May be detected as unused
    }
}
```

### Annotation Processors

Both declarations are required:

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'           // For compilation
    annotationProcessor 'org.projectlombok:lombok'   // For processing
}
```

## Static Analysis Scripts

### Find Unused in Version Catalog

```bash
#!/bin/bash
# Find potentially unused libraries in libs.versions.toml

TOML_FILE="gradle/libs.versions.toml"

# Extract library names
grep -E "^[a-z][a-z0-9-]+ = \{" "$TOML_FILE" | cut -d'=' -f1 | tr -d ' ' | while read lib; do
    # Convert to Gradle accessor format (kebab-case to dot notation)
    accessor="libs.$(echo $lib | tr '-' '.')"

    # Search for usage in build files
    if ! grep -rq "$accessor" --include="*.gradle" --include="*.gradle.kts" .; then
        echo "Possibly unused: $lib"
    fi
done
```

### Find Unused Versions

```bash
#!/bin/bash
# Find unused version definitions

grep -E "^[a-z][a-z0-9-]+ = \"" gradle/libs.versions.toml | cut -d'=' -f1 | tr -d ' ' | while read ver; do
    if ! grep -q "version.ref = \"$ver\"" gradle/libs.versions.toml; then
        echo "Unused version: $ver"
    fi
done
```

## Comparison: Gradle Lint vs Static Analysis

| Feature | Gradle Lint Plugin | Static Analysis |
|---------|-------------------|-----------------|
| Accuracy | **High** - understands build | Moderate - pattern matching |
| Unused deps in code | **Yes** | No (only version catalog) |
| Undeclared deps | **Yes** | No |
| Auto-fix | Yes (with caveats) | No |
| Setup required | Yes (plugin integration) | No |
| Speed | Slower (runs Gradle) | Fast (bash/grep) |
| False positives | Fewer | More |

## Best Practices

### DO: Keep Dependencies Clean

- Remove truly unused dependencies
- Add undeclared dependencies explicitly
- Use version catalog for centralized management
- Run lint analysis regularly

### DON'T: Remove Potentially Used Entries

- Platform/BOM dependencies that manage transitives
- Security-forced versions (even if not directly referenced)
- Versions used by shared gradle scripts
- Annotation processor dependencies
- Spring Boot starters (empty artifacts are intentional)

## Related

- [Cleanup Workflow](cleanup-workflow.md) - Full cleanup process
- [Optimization](optimization.md) - Build optimization
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/unused-detection.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

