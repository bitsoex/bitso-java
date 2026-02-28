# Troubleshooting Dependabot Fixes

## Contents

- [dependency-review CI Check Fails](#dependency-review-ci-check-fails)
- [Old Versions Still Appear After Substitution](#old-versions-still-appear-after-substitution)
- [Build Fails After Security Fix](#build-fails-after-security-fix)
- [Tests Fail After Security Fix](#tests-fail-after-security-fix)
- [Package is Discontinued](#package-is-discontinued)
- [Never Downgrade Pre-existing Versions](#never-downgrade-pre-existing-versions)

---
## dependency-review CI Check Fails

### Symptom

CI shows:

```text
+ org.apache.commons:commons-compress@1.23.0  <-- VULNERABILITY DETECTED
```

### Cause

Old version is still being reported to GitHub even though runtime resolution uses new version.

### Solution

1. Use `dependencySubstitution` instead of `force`
2. Verify with dependency graph (see [dependency-graph.md](dependency-graph.md))
3. If still failing, add `exclude` rules

## Old Versions Still Appear After Substitution

### Check 1: Rule is in allprojects block

```groovy
allprojects {  // MUST be here
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Check 2: All configurations are covered

Some configurations may not be included in `configureEach`:

```groovy
allprojects {
    configurations.all {  // Try .all instead of .configureEach
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Check 3: Buildscript dependencies

Buildscript classpath is separate:

```groovy
buildscript {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

## Build Fails After Security Fix

### Cause 1: Breaking changes in new version

Check release notes for the updated dependency. May need code changes.

### Cause 2: Version conflict

Another dependency may require the old version. Use the dependency graph to verify, then `dependencyInsight` to debug:

```bash
# PRIMARY: Check dependency graph for all versions
./gradlew -I gradle/dependency-graph-init.gradle \
    :ForceDependencyResolutionPlugin_resolveAllDependencies
grep "commons-compress" build/reports/dependency-graph-snapshots/dependency-list.txt

# DEBUGGING: Trace why a specific version was selected
./gradlew dependencyInsight --dependency commons-compress --configuration compileClasspath
```

### Solution

Use dependency substitution with a compatible version, or add both old and new coordinates to substitution.

## Tests Fail After Security Fix

### Quick test run (skip coverage)

```bash
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log
grep -E "FAILED|Error" /tmp/test.log | head -20
```

### Analyze failures

```bash
grep -B 5 -A 20 "FAILED" /tmp/test.log
```

Common causes:

- Behavior change in new library version
- Test using deprecated API removed in new version
- Configuration class changes

## Package is Discontinued

### Symptom

`patched_version` is null in Dependabot alert.

### Solution

Check advisory references for community fork. Use GAV substitution:

```groovy
substitute module("org.lz4:lz4-java")
    using module("at.yawk.lz4:lz4-java:1.10.1")
    because "org.lz4 discontinued, using maintained fork"
```

See the vulnerability golden paths rule (distributed as `.cursor/rules/java-vulnerability-golden-paths.mdc`) for known cases.

## Never Downgrade Pre-existing Versions

If you encounter a situation where you want to downgrade:

| Scenario | Allowed |
|----------|---------|
| Upgrade a vulnerable library | ✅ Yes |
| Downgrade a version YOUR PR introduced | ✅ Yes |
| Downgrade a version that existed before your PR | ❌ No |
| Pin BOM-managed dependency to older version | ❌ No |

Add a warning comment instead:

```groovy
// WARNING: Potential incompatibility with X.
// See Redis-Jedis compatibility guidance in java/golden-paths/
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-vulnerabilities/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

