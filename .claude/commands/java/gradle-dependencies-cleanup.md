# Identify and remove unused dependencies using Gradle Lint Plugin or static analysis

**Description:** Identify and remove unused dependencies using Gradle Lint Plugin or static analysis

# 🤖 📦 Gradle Dependencies Cleanup

**IMPORTANT**: This command helps identify unused dependencies in Gradle projects. It performs analysis and should be validated before removing entries.

## Overview

This command analyzes Gradle projects to find:

1. **Unused dependencies** - Dependencies declared but not used by your code
2. **Undeclared dependencies** - Dependencies used directly but not declared (relying on transitives)
3. **Unused entries in version catalogs** - Libraries or versions declared but never referenced
4. **Duplicate or conflicting version definitions** - Same dependency with different versions

## Approach Selection

### Preferred: Gradle Lint Plugin (Recommended)

The Nebula Gradle Lint Plugin provides the most accurate analysis by integrating with the Gradle build system. It understands the actual compile-time dependencies and can identify issues that static analysis cannot.

**Use when**:

- You want the most accurate unused dependency detection
- You need to find undeclared dependencies (code using transitives)
- You want auto-fix capability (with caution)
- The project can be modified to add the plugin

### Fallback: Static Analysis Scripts

Bash scripts that grep through build files to find unreferenced entries in version catalogs. Faster but less accurate.

**Use when**:

- Quick check without modifying the project
- Plugin integration is not possible
- Only checking version catalog entries (`libs.versions.toml`)

## Related Rules (Read First)

- **Gradle Lint Plugin**: `java/rules/java-gradle-lint-plugin.md` - Plugin configuration and usage
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Understanding version centralization
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md` - Build configuration standards
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Debugging and verification commands

## Prerequisites

Ensure you have:

- Gradle project with `gradle/libs.versions.toml` (modern) or `versions.gradle` (legacy)
- Access to all `build.gradle` files in the project
- Gradle wrapper configured (`gradlew`)

---

## Method 1: Gradle Lint Plugin (Preferred)

### 1. Create Feature Branch

```bash
git fetch --all
git checkout main
git pull origin main
git checkout -b chore/gradle-dependencies-cleanup
```

### 2. Setup the Plugin

Use the setup script to integrate the Gradle Lint Plugin:

```bash
# Run the setup script
bash /path/to/ai-code-instructions/java/scripts/gradle-lint-setup.sh

# Or for a specific project
bash /path/to/ai-code-instructions/java/scripts/gradle-lint-setup.sh /path/to/project
```

The script will:

- Detect your Gradle version
- Add the appropriate plugin version (21.1.3 for Gradle 9.x, 20.6.2 for Gradle 8.x)
- Configure `settings.gradle` and `build.gradle`
- Set up JVM memory for lint analysis

### 3. Run Lint Analysis

```bash
# Using the analysis script (recommended)
bash /path/to/ai-code-instructions/java/scripts/gradle-lint-analyze.sh

# Or directly with Gradle
./gradlew lintGradle 2>&1 | tee /tmp/lint.log

# Generate detailed report
./gradlew generateGradleLintReport
cat build/reports/gradleLint/*.txt
```

### 4. Understand the Results

#### Violation Types

| Message | Meaning | Action |
|---------|---------|--------|
| "unused and can be removed" | Truly unused | **Safe to remove** |
| "artifact is empty" | Spring Boot starter | **Ignore** - starters aggregate dependencies |
| "classes required by code directly" | Undeclared dependency | **Add explicitly** or ignore |
| "can be moved to runtimeOnly" | Not needed at compile time | **Review** - may be annotation processor |

#### Common False Positives

| Library | Why |
|---------|-----|
| `lombok` | Annotation processor - needs both `compileOnly` and `annotationProcessor` |
| `spring-boot-starter-*` | Empty by design - aggregates dependencies |
| `*-bom` | Bill of Materials - manages versions only |
| `groovy` / `spock-core` | Test framework infrastructure |

### 5. Run Auto-Fix

Run the auto-fix to apply changes automatically:

```bash
./gradlew fixGradleLint
```

### 6. Clean Up Hardcoded Versions

**IMPORTANT**: Auto-fix adds dependencies with hardcoded versions. Replace them with version catalog references.

**Find hardcoded versions added by auto-fix:**

```bash
# Find hardcoded dependencies (not using libs.*)
grep -rn "implementation '[a-zA-Z]" --include="build.gradle" . | grep -v "libs\." | grep ":"
grep -rn "testImplementation '[a-zA-Z]" --include="build.gradle" . | grep -v "libs\." | grep ":"
```

**Replace with version catalog references:**

Before (added by auto-fix):

```groovy
implementation 'io.vavr:vavr:0.10.7'
implementation 'org.slf4j:slf4j-api:2.0.17'
```

After (using version catalog):

```groovy
implementation libs.vavr
implementation libs.slf4j.api
```

**Add missing libraries to version catalog:**

If a library doesn't exist in `gradle/libs.versions.toml`, add it:

```toml
[libraries]
vavr = { module = "io.vavr:vavr", version = "0.10.7" }
slf4j-api = { module = "org.slf4j:slf4j-api", version.ref = "slf4j" }
```

### 7. Restore Lombok (if removed)

Auto-fix may incorrectly remove Lombok's `compileOnly`. Check and restore if needed:

```bash
# Check for missing Lombok compileOnly
grep -rn "annotationProcessor.*lombok" --include="build.gradle" . | while read line; do
    file=$(echo "$line" | cut -d: -f1)
    if ! grep -q "compileOnly.*lombok" "$file"; then
        echo "Missing compileOnly lombok in: $file"
    fi
done
```

Both declarations are required:

```groovy
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

### 8. Verify Build Still Works

```bash
# Clean build to verify nothing breaks
./gradlew clean build -x test 2>&1 | tee /tmp/build.log | grep -E "BUILD|FAILED"

# Run tests to catch runtime issues
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "BUILD|FAILED"
```

### 9. Commit and Create PR

```bash
git add -A
git commit -m "🤖 📦 chore(deps): cleanup unused dependencies with Gradle Lint

- Integrated Nebula Gradle Lint Plugin
- Removed N unused dependencies
- Added M undeclared dependencies explicitly
- Validated build and tests pass

Generated with the Quality Agent by the /gradle-dependencies-cleanup command."

git push -u origin $(git branch --show-current)

gh pr create \
    --title "🤖 📦 chore(deps): cleanup unused dependencies" \
    --body "## 🤖 AI-Assisted Dependency Cleanup

## Summary
Used Gradle Lint Plugin to identify and clean up unused dependencies.

## Changes
- Integrated Nebula Gradle Lint Plugin (v21.1.3 or v20.6.2)
- Removed N truly unused dependencies
- Added M undeclared dependencies explicitly for reproducible builds
- Cleaned up version catalog entries

## Analysis Method
Gradle Lint Plugin with rules: \`unused-dependency\`, \`undeclared-dependency\`

## Validation
- [x] Lint analysis completed
- [x] Build passes without removed entries
- [x] Tests pass

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /gradle-dependencies-cleanup

Generated with the Quality Agent by the /gradle-dependencies-cleanup command."
```

---

## Method 2: Static Analysis (Fallback)

Use this method when you cannot integrate the Gradle Lint Plugin.

### 1. Run Analysis Script

```bash
# Analyze version catalog and versions.gradle
bash /path/to/ai-code-instructions/java/scripts/analyze-unused-deps.sh

# Or just the version catalog
bash /path/to/ai-code-instructions/java/scripts/analyze-unused-deps.sh --toml-only

# Or just versions.gradle
bash /path/to/ai-code-instructions/java/scripts/analyze-unused-deps.sh --versions-only
```

#### Sample Output

```text
=======================================================================
  GRADLE UNUSED DEPENDENCIES ANALYZER
  Project: /path/to/your-project
=======================================================================

--- Analyzing libs.versions.toml ---

  Potentially Unused Libraries:
    ❌ unused-lib-name (libs.unused.lib.name)
    ❌ another-unused (libs.another.unused)

  Found 2 potentially unused libraries

  Possibly Indirect Dependencies (verify before removing):
    ⚠ lombok (may be used via annotation processor, BOM, or test framework)
    ⚠ junit-bom (may be used via annotation processor, BOM, or test framework)

  Used Libraries: 87
  Likely Unused: 2
  Needs Review: 2
```

### 2. Alternative: Gradle Task Method

Copy the template from `java/templates/unused-dependencies.gradle`:

```bash
cp /path/to/ai-code-instructions/java/templates/unused-dependencies.gradle gradle/
```

Register in root `build.gradle`:

```groovy
apply from: "${rootDir}/gradle/unused-dependencies.gradle"
```

Run:

```bash
./gradlew analyzeUnusedDependencies --no-daemon
```

### 3. Review and Validate

Follow the same validation steps as Method 1:

1. Review each finding before removal
2. Search the entire project for the entry
3. Verify build works after changes
4. Run tests

---

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

---

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

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok'           // Needed for compilation
    annotationProcessor 'org.projectlombok:lombok'   // Needed for processing
}
```

---

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

---

## Troubleshooting

### Plugin Not Found

```text
Plugin [id: 'nebula.lint'] was not found
```

Ensure plugin is registered in `settings.gradle` pluginManagement block.

### Out of Memory

Increase JVM memory in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
```

### Build Fails After Removal

A removed entry was actually used. Re-add it and investigate why lint didn't detect usage.

### Lombok Errors After Auto-Fix

```text
error: package lombok does not exist
```

Revert and keep both declarations:

```groovy
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

---

## Related

- **Gradle Lint Plugin Rule**: `java/rules/java-gradle-lint-plugin.md`
- **Version Management**: `java/rules/java-versions-and-dependencies.md`
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md`
- **Dependabot Vulnerabilities**: `java/commands/fix-dependabot-vulnerabilities.md`
- **Setup Script**: `java/scripts/gradle-lint-setup.sh`
- **Analysis Script**: `java/scripts/gradle-lint-analyze.sh`
- **Static Analysis Script**: `java/scripts/analyze-unused-deps.sh`
