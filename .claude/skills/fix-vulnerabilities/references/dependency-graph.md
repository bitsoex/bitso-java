# Dependency Graph Setup and Verification

The dependency graph plugin reports ALL versions to GitHub. Understanding this is critical for fixing `dependency-review` failures.

## Contents

- [Why Dependency Graph Matters](#why-dependency-graph-matters)
- [Setup Dependency Graph Plugin](#setup-dependency-graph-plugin)
- [Generate Dependency Graph](#generate-dependency-graph)
- [Verify Fix](#verify-fix)
- [Find Which Configuration Brings Old Version](#find-which-configuration-brings-old-version)
- [Common Issues](#common-issues)

---
## Why Dependency Graph Matters

**The problem**: `dependencyInsight` only shows one resolution path. The dependency graph plugin reports ALL versions in ANY configuration to GitHub.

**What causes failures**:

```text
# dependency-list.txt shows BOTH versions
commons-compress:1.23.0   <-- This WILL be reported to GitHub
commons-compress:1.27.1   <-- This too

# dependency-review action will show:
+ org.apache.commons:commons-compress@1.23.0  <-- VULNERABILITY DETECTED
```

**Force rules alone are NOT enough** - they only affect runtime resolution, not what gets reported.

## Setup Dependency Graph Plugin

Check if `gradle/dependency-graph-init.gradle` exists. If not, create it:

```groovy
/**
 * Init script for GitHub Dependency Graph Gradle Plugin
 * @see https://github.com/gradle/github-dependency-graph-gradle-plugin
 */
initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath "org.gradle:github-dependency-graph-gradle-plugin:1.4.0"
    }
}

apply plugin: org.gradle.dependencygraph.simple.SimpleDependencyGraphPlugin
```

## Generate Dependency Graph

```bash
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies 2>&1 | tee /tmp/dep-graph.log
```

## Verify Fix

Check what versions will be reported to GitHub:

```bash
# Check specific package
grep -i "commons-compress" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

**Good output** (single patched version):

```text
org.apache.commons:commons-compress:1.27.1
```

**Bad output** (old version still present - WILL FAIL):

```text
org.apache.commons:commons-compress:1.23.0   <-- OLD VERSION
org.apache.commons:commons-compress:1.27.1
```

## Find Which Configuration Brings Old Version

If old versions still appear:

```bash
grep -B5 "commons-compress:1.23" build/reports/dependency-graph-snapshots/dependency-graph.json
```

This shows which configuration or dependency is bringing in the old version.

## Common Issues

### Old versions still appear after substitution

Check if the rule is in `allprojects` block:

```groovy
allprojects {  // MUST be here, not just in root
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Buildscript dependencies

Buildscript classpath needs separate handling:

```groovy
buildscript {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Settings.gradle dependencies

If `settings.gradle` shows in dependency-review:

```groovy
// settings.gradle
dependencyResolutionManagement {
    components {
        // Substitution rules here
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-vulnerabilities/references/dependency-graph.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

