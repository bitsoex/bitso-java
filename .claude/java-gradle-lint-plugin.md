# Java Gradle Lint Plugin

**Applies to:** All files

# Gradle Lint Plugin Configuration

The Nebula Gradle Lint Plugin provides static analysis for Gradle builds, identifying unused dependencies, undeclared dependencies, and other build hygiene issues.

## Overview

**Repository**: <https://github.com/nebula-plugins/gradle-lint-plugin>

**Key Features**:

- `unused-dependency` - Finds declared but unused dependencies
- `undeclared-dependency` - Finds used but undeclared dependencies (relying on transitives)
- Auto-fix capability (use with caution)
- Detailed reports in text/HTML/XML formats

## Version Compatibility

| Gradle Version | Plugin Version | Notes |
|----------------|----------------|-------|
| 9.x | 21.1.3 | Latest, full support |
| 8.x | 20.6.2 | Stable for Gradle 8 |

## Setup

### Quick Setup (Script)

Use the setup script from ai-code-instructions:

```bash
bash /path/to/ai-code-instructions/java/scripts/gradle-lint-setup.sh
```

### Manual Setup

#### 1. Add version to `gradle.properties`

```properties
# Gradle Lint Plugin - https://github.com/nebula-plugins/gradle-lint-plugin
gradleLintPluginVersion=21.1.3

# Ensure adequate JVM memory
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

#### 2. Register in `settings.gradle`

```groovy
pluginManagement {
    plugins {
        id 'nebula.lint' version gradleLintPluginVersion
    }
}
```

#### 3. Configure in root `build.gradle`

```groovy
plugins {
    id 'nebula.lint'
}

gradleLint {
    // Dependency hygiene rules
    rules = [
        'unused-dependency',      // Find unused declared dependencies
        'undeclared-dependency',  // Find used but undeclared dependencies
    ]
    
    // Output format: text, html, or xml
    reportFormat = 'text'
    
    // Don't fail build on violations (warnings only)
    criticalRules = []
    
    // Don't run lint automatically on every build
    // Run manually with: ./gradlew lintGradle
    alwaysRun = false
}

// Apply lint plugin to all subprojects
allprojects {
    apply plugin: 'nebula.lint'
}
```

## Available Tasks

| Task | Description |
|------|-------------|
| `lintGradle` | Run lint analysis and show violations |
| `generateGradleLintReport` | Generate detailed report in `build/reports/gradleLint/` |
| `fixGradleLint` | Auto-fix violations (use with caution!) |

## Running Analysis

```bash
# Run lint and show violations
./gradlew lintGradle 2>&1 | tee /tmp/lint.log

# Generate detailed report
./gradlew generateGradleLintReport

# View report
cat build/reports/gradleLint/*.txt
```

## Understanding Results

### Violation Types

#### `unused-dependency` - Dependency is Declared but Not Used

| Message | Meaning | Action |
|---------|---------|--------|
| "this dependency is unused and can be removed" | Truly unused | **Safe to remove** |
| "artifact is empty" | Spring Boot starter | **Ignore** - starters are dependency aggregators |
| "classes required by your code directly" | Used via transitive | **False positive** - keep it |
| "can be moved to runtimeOnly" | Not needed at compile time | **Review** - may be needed for annotation processing |

#### `undeclared-dependency` - Used But Not Explicitly Declared

```text
undeclared-dependency: one or more classes in com.example:lib:1.0 are required by your code directly
```

**Action**: Add as explicit dependency to make build more reproducible.

### Common False Positives

| Library | Why It's a False Positive |
|---------|---------------------------|
| `org.projectlombok:lombok` | Used via annotation processor, not direct code |
| `spring-boot-starter-*` | Empty artifacts by design - they aggregate dependencies |
| `*-bom` | Bill of Materials - manages versions, no code |
| `groovy` / `spock-core` | Used by test framework infrastructure |
| `slf4j-api` | Often pulled transitively by logging implementation |

## Auto-Fix Workflow

The recommended workflow is to **run auto-fix first**, then **clean up the results** to use version catalog references.

### Step 1: Run Auto-Fix

```bash
./gradlew fixGradleLint
```

### Step 2: Find Hardcoded Versions Added by Auto-Fix

The plugin adds dependencies with hardcoded versions. Find them:

```bash
# Find hardcoded versions in build.gradle files (added by fixGradleLint)
grep -rn "implementation '[a-zA-Z]" --include="build.gradle" . | grep -v "libs\." | grep ":"
grep -rn "testImplementation '[a-zA-Z]" --include="build.gradle" . | grep -v "libs\." | grep ":"
```

### Step 3: Replace with Version Catalog References

For each hardcoded dependency found, replace with version catalog reference:

**Before (added by fixGradleLint):**

```groovy
dependencies {
    implementation 'io.vavr:vavr:0.10.7'
    implementation 'org.slf4j:slf4j-api:2.0.17'
}
```

**After (using version catalog):**

```groovy
dependencies {
    implementation libs.vavr
    implementation libs.slf4j.api
}
```

### Step 4: Add Missing Libraries to Version Catalog

If a library doesn't exist in `gradle/libs.versions.toml`, add it:

```toml
[libraries]
vavr = { module = "io.vavr:vavr", version = "0.10.7" }
slf4j-api = { module = "org.slf4j:slf4j-api", version.ref = "slf4j" }
```

### Step 5: Restore Lombok (if removed)

Check if Lombok's `compileOnly` was incorrectly removed:

```bash
# Check for missing Lombok compileOnly
grep -rn "annotationProcessor.*lombok" --include="build.gradle" . | while read line; do
    file=$(echo "$line" | cut -d: -f1)
    if ! grep -q "compileOnly.*lombok" "$file"; then
        echo "Missing compileOnly lombok in: $file"
    fi
done
```

If removed, restore it. Both are required:

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'           // Compile-time visibility
    annotationProcessor 'org.projectlombok:lombok'   // Annotation processing
}
```

### Step 6: Verify Build

```bash
./gradlew build -x test
```

## Known Auto-Fix Behaviors

### Behavior 1: Hardcoded Versions

The plugin adds dependencies with hardcoded versions instead of version catalog references. **Always replace these** with `libs.*` references after running auto-fix.

### Behavior 2: Lombok Removal

The plugin may incorrectly remove `compileOnly 'org.projectlombok:lombok'`. Always verify Lombok configuration after auto-fix.

### Behavior 3: Spring Boot Starters

The plugin flags starters as "empty artifacts" but does NOT remove them (just warns). These warnings are safe to ignore.

## Best Practices

### DO

- Run `lintGradle` regularly to monitor dependency hygiene
- Add undeclared dependencies explicitly for reproducible builds
- Remove truly unused dependencies (marked "can be removed")
- Use version catalog (`libs.versions.toml`) for version management

### DON'T

- Blindly run `fixGradleLint` without reviewing changes
- Remove Spring Boot starters flagged as "empty"
- Remove Lombok's `compileOnly` declaration
- Remove dependencies flagged as "required by your code" (false positive)

## Integration with CI

For automated analysis in CI/CD:

```yaml
# Example GitHub Actions step
- name: Run Gradle Lint
  run: |
    ./gradlew lintGradle 2>&1 | tee lint-output.log
    # Check for critical issues
    if grep -q "error" lint-output.log; then
      echo "Lint errors found"
      exit 1
    fi
```

## Troubleshooting

### Plugin Not Found

```text
Plugin [id: 'nebula.lint'] was not found
```

**Fix**: Ensure plugin is registered in `settings.gradle` pluginManagement block.

### Out of Memory

```text
Java heap space / GC overhead limit exceeded
```

**Fix**: Increase JVM memory in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

### Build Fails After fixGradleLint

```text
error: package lombok does not exist
```

**Fix**: Revert the Lombok changes:

```bash
git checkout -- path/to/build.gradle
```

Then add back:

```groovy
compileOnly 'org.projectlombok:lombok'
```

## Related

- **Setup Script**: `java/scripts/gradle-lint-setup.sh`
- **Analysis Script**: `java/scripts/gradle-lint-analyze.sh`
- **Cleanup Command**: `java/commands/gradle-dependencies-cleanup.md`
- **Version Management**: `java/rules/java-versions-and-dependencies.md`
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md`

---
*This rule is part of the java category.*
*Source: java/rules/java-gradle-lint-plugin.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-gradle-lint-plugin.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
