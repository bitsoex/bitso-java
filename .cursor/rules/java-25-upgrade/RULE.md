---
description: Proven patterns for upgrading Java projects to Java 25 compatibility
alwaysApply: false
globs: gradle/libs.versions.toml,build.gradle,settings.gradle,gradle.properties
tags:
  - java
---

# Java 25 Upgrade Golden Path

This document provides proven patterns for upgrading Java projects to Java 25 compatibility, based on real-world experience from the motorsito java25 branch.

## Related Documents

- **Command**: `java/commands/prepare-to-java-25.md` - Step-by-step upgrade workflow
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Version catalog strategy
- **Golden Paths Index**: `java/golden-paths/java-upgrades-golden-paths.md` - All upgrade patterns

## Quick Reference: Java 25 Target Versions

| Component | Version | Why |
|-----------|---------|-----|
| **Gradle** | **9.2.1** | Java 25 toolchain support |
| **Groovy** | **5.0.3** | Java 25 bytecode compatibility |
| **Spock** | **2.4-groovy-5.0** | Matches Groovy 5.x |
| **Lombok** | **1.18.42** | Java 25 bytecode support |
| **Lombok Plugin** | **9.1.0** | Freefair plugin for Gradle 9 |
| **Spotless** | **8.1.0** | Gradle 9 compatibility |
| **SonarQube** | **7.2.2.6593** | Gradle 9 compatibility |
| **Testcontainers 1.x** | **1.21.4** | Latest 1.x version |
| **Testcontainers 2.x** | **2.0.3** | Docker socket fix |
| **ByteBuddy** | **1.17.5+** | ASM 9.8 for Java 25 |
| **Flyway Plugin** | **11.19.0** | Gradle 9 compatibility |
| **jOOQ Plugin** | **10.1.1** | Gradle 9 compatibility |
| **Protobuf Plugin** | **0.9.5** | Gradle 9 compatibility |
| **palantir-java-format** | **2.74.0** | Java 25 bytecode support (use with Spotless) |
| **Develocity Plugin** | **0.2.8** | Gradle 9 compatibility |
| **Netty BOM** | **4.1.128.Final** | Match Spring Boot 3.5.8 |
| **groovy-json** | **5.0.3** | Add explicitly for JsonSlurper/JsonOutput |

## Real-World Reference: motorsito java25 Branch

The motorsito repository successfully upgraded to Java 25:

- **Branch**: <https://github.com/bitsoex/motorsito/tree/java25>
- **Key commits**:
  - `f1893d0a` - Initial Java 25 and Gradle 9 upgrade
  - `f70c1c2a` - Lombok library version for Java 25
  - `13627e5b` - Groovy 5.0.3 and Spock 2.4-groovy-5.0
  - `9b6c7132` - Gradle wrapper 9.2.1

### motorsito Final Configuration

```toml
# gradle/libs.versions.toml (motorsito java25 branch)
[versions]
spock = "2.4-groovy-5.0"
testcontainers = "2.0.3"  # or "1.21.4" for 1.x projects
groovy = "5.0.3"
lombok = "1.18.42"

[plugins]
lombok = "io.freefair.lombok:9.1.0"
spotless = "com.diffplug.spotless:8.1.0"
sonarqube = "org.sonarqube:7.2.2.6593"
```

**Lombok Configuration**: The Freefair Lombok plugin needs Lombok 1.18.42 for Java 25.

1. **Add versions to version catalog** (`gradle/libs.versions.toml`):

```toml
[versions]
lombok = "1.18.42"
lombokPlugin = "9.1.0"

[plugins]
lombok = { id = "io.freefair.lombok", version.ref = "lombokPlugin" }
```

1. **Update plugin version in `settings.gradle`** (reference from version catalog):

```groovy
pluginManagement {
    plugins {
        id 'io.freefair.lombok' version "${lombokPluginVersion}"  // From gradle.properties
    }
}
```

1. **Configure Lombok version globally in root `build.gradle`**:

```groovy
subprojects {
    plugins.withId('io.freefair.lombok') {
        lombok {
            version = libs.versions.lombok.get()  // Use version catalog
        }
    }
}
```

```groovy
// build.gradle (motorsito java25 branch)
allprojects {
    // Configure Java 25 toolchain for all Java projects
    plugins.withType(JavaPlugin).configureEach {
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
                vendor = JvmVendorSpec.ADOPTIUM
            }
        }
    }
}
```

## Upgrade Patterns

### Pattern 1: Gradle 9 Plugin Migration

Gradle 9 plugin versions must be updated. **Always use centralized versioning** - never hardcode versions in `settings.gradle` or `build.gradle`.

**In `gradle.properties`** (for plugins that don't support version catalog):

```properties
sonarqubePluginVersion=7.2.1.6560
lombokPluginVersion=9.1.0
springBootVersion=3.5.8
develocityPluginVersion=0.2.8
```

**In `settings.gradle` pluginManagement block** (reference properties):

```groovy
pluginManagement {
    plugins {
        id 'io.freefair.lombok' version "${lombokPluginVersion}"
        id 'org.springframework.boot' version "${springBootVersion}"
        id 'bitso.develocity' version "${develocityPluginVersion}"
        id 'org.sonarqube' version "${sonarqubePluginVersion}"
    }
}
```

**In `gradle/libs.versions.toml`:**

```toml
[plugins]
# ❌ Old versions (Gradle 8.x)
# lombok = "io.freefair.lombok:8.14.2"
# spotless = "com.diffplug.spotless:6.25.0"
# sonarqube = "org.sonarqube:6.3.1.5724"

# ✅ New versions (Gradle 9.x)
lombok = "io.freefair.lombok:9.1.0"
spotless = "com.diffplug.spotless:8.1.0"
sonarqube = "org.sonarqube:7.2.2.6593"
flyway = { id = "org.flywaydb.flyway", version = "11.19.0" }
protobuf = "com.google.protobuf:0.9.5"
```

**In `gradle.properties`:**

```properties
sonarqubePluginVersion=7.2.1.6560
bitsoPublishPluginVersion=0.3.6
jacocoVersion=0.8.14
```

### Pattern 2: Groovy 5.x Migration

Java 25 requires Groovy 5.x for proper bytecode support:

```toml
[versions]
# ❌ Old (Java 21)
# groovy = "4.0.29"
# spock = "2.4-groovy-4.0"

# ✅ New (Java 25)
groovy = "5.0.3"
spock = "2.4-groovy-5.0"
```

**Important**: The Spock version suffix must match the Groovy major version:

| Groovy | Spock |
|--------|-------|
| 4.0.x | `2.4-groovy-4.0` |
| 5.0.x | `2.4-groovy-5.0` |

### ⚠️ CRITICAL: Do NOT Use `groovy-all`

**Never use `groovy-all` dependency with Groovy 5.x**. The `groovy-all` artifact in Groovy 5.0.3 has broken transitive dependencies that reference Groovy 4.0.29 sub-modules, causing class incompatibilities.

```groovy
// ❌ NEVER: groovy-all has broken transitive deps to Groovy 4.x
testImplementation 'org.apache.groovy:groovy-all:5.0.3'

// ✅ CORRECT: Let spock-core bring in Groovy 5.0.3 transitively
testImplementation libs.spock.core
testImplementation libs.spock.spring  // if using Spring
```

The `spock-core:2.4-groovy-5.0` artifact correctly brings in `groovy:5.0.3` as a transitive dependency. This is the proven pattern used by motorsito.

**Note**: If tests use `groovy.json.JsonSlurper` or `groovy.json.JsonOutput`, you need to add `groovy-json` explicitly since spock-core only brings the base `groovy` module:

```groovy
testImplementation libs.spock.core  // Brings groovy:5.0.3
testImplementation libs.groovy.json // For JsonSlurper/JsonOutput
```

With version catalog:

```toml
[libraries]
groovy-json = { module = "org.apache.groovy:groovy-json", version.ref = "groovy" }
```

### Pattern 3: Override Spring Boot BOM Managed Versions

Spring Boot's BOM manages Groovy, JUnit, and other dependencies. Override them using Spring Boot's property mechanism:

```groovy
// In root build.gradle
allprojects {
    // Force Groovy 5.0.3 for Java 25 compatibility with Spock 2.4-groovy-5.0
    ext['groovy.version'] = libs.versions.groovy.get()
    // Force JUnit 5.14.1 for Gradle 9 compatibility
    ext['junit-jupiter.version'] = libs.versions.junitJupiter.get()
}
```

**Important**: The `ext['property.version']` pattern is the ONLY reliable way to override Spring Boot BOM constraints. Neither `resolutionStrategy.force` nor `dependencySubstitution` work against Spring Boot's dependency management plugin.

With version catalog:

```toml
[versions]
groovy = "5.0.3"
spock = "2.4-groovy-5.0"
```

**Verification:**

```bash
./gradlew :module:dependencies --configuration testCompileClasspath 2>&1 | grep -i groovy
# Should show groovy:5.0.3, NOT groovy:4.0.29
```

### Pattern 5: ByteBuddy for Java 25 ASM Support

If your project uses Mockito or other libraries that depend on ByteBuddy:

```toml
[versions]
# Minimum version for Java 25 ASM support
bytebuddy = "1.17.5"

[libraries]
bytebuddy = { module = "net.bytebuddy:byte-buddy", version.ref = "bytebuddy" }
bytebuddy-agent = { module = "net.bytebuddy:byte-buddy-agent", version.ref = "bytebuddy" }
```

**Why 1.17.5?** This version includes ASM 9.8 which adds support for Java 25 class file format (version 69).

### Pattern 6: Java Toolchain Configuration

Configure the Java 25 toolchain in `build.gradle`:

```groovy
allprojects {
    plugins.withType(JavaPlugin).configureEach {
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(25)
                vendor = JvmVendorSpec.ADOPTIUM
            }
        }
    }
}
```

### Pattern 7: JUnit Platform Launcher for Gradle 9

Gradle 9 requires explicit JUnit Platform Launcher dependency. **Let Spring Boot BOM manage the version** (do not hardcode version):

**In `gradle/libs.versions.toml`:**

```toml
[libraries]
# No version specified - let Spring Boot BOM manage it
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
```

**In root `build.gradle`** (add globally):

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly libs.junit.platform.launcher
        }
    }
}
```

**Note**: The Spring Boot BOM will provide the correct compatible version. Do NOT specify a version unless you have a specific compatibility issue.

### Pattern 8: Testcontainers Version Upgrade

Upgrade to the latest version of your current major:

```toml
[versions]
# For projects on Testcontainers 1.x
testcontainers = "1.21.4"

# For projects on Testcontainers 2.x
testcontainers = "2.0.3"
```

**Important**: Don't change major versions (1.x ↔ 2.x) unless specifically planned, as this may require code changes.

### Pattern 9: Override bitso.java.module Plugin Versions

The `bitso.java.module` plugin (via `bitso.publish`) forces old Spock and Groovy versions that are incompatible with Java 25. Use `dependencySubstitution` which is stronger than `force`:

**In root `build.gradle`:**

```groovy
subprojects {
    // Use dependencySubstitution to override bitso.java.module plugin's versions
    // dependencySubstitution is stronger than force and works on direct dependencies
    afterEvaluate {
        configurations.configureEach {
            resolutionStrategy.dependencySubstitution {
                // Substitute any Spock version with Java 25 compatible version
                substitute module('org.spockframework:spock-core') using module("org.spockframework:spock-core:${libs.versions.spock.get()}") because 'Java 25 bytecode compatibility'
                substitute module('org.spockframework:spock-spring') using module("org.spockframework:spock-spring:${libs.versions.spock.get()}") because 'Java 25 bytecode compatibility'
                // Substitute old Groovy (codehaus) with new Groovy (apache) for Java 25
                substitute module('org.codehaus.groovy:groovy') using module("org.apache.groovy:groovy:${libs.versions.groovy.get()}") because 'Java 25 bytecode compatibility - migrate from codehaus to apache'
                substitute module('org.codehaus.groovy:groovy-json') using module("org.apache.groovy:groovy-json:${libs.versions.groovy.get()}") because 'Java 25 bytecode compatibility'
                substitute module('org.codehaus.groovy:groovy-xml') using module("org.apache.groovy:groovy-xml:${libs.versions.groovy.get()}") because 'Java 25 bytecode compatibility'
                substitute module('org.codehaus.groovy:groovy-templates') using module("org.apache.groovy:groovy-templates:${libs.versions.groovy.get()}") because 'Java 25 bytecode compatibility'
            }
            // Force JUnit versions for Gradle 9 and Java 25 compatibility
            resolutionStrategy.eachDependency { details ->
                if (details.requested.group == 'org.junit.jupiter') {
                    details.useVersion libs.versions.junitJupiter.get()
                    details.because 'Force JUnit 5.14.1 for Java 25 compatibility'
                }
                if (details.requested.group == 'org.junit.platform') {
                    details.useVersion libs.versions.junitPlatform.get()
                    details.because 'Force JUnit Platform 1.14.1 for Gradle 9'
                }
            }
        }
    }
}
```

**Required version catalog entries:**

```toml
[versions]
groovy = "5.0.3"
spock = "2.4-groovy-5.0"
junitJupiter = "5.14.1"
junitPlatform = "1.14.1"
```

**Why `dependencySubstitution`?**

- `resolutionStrategy.force` doesn't override constraints from `bitso.java.module`
- `dependencySubstitution` works on the module level, not just version
- It can substitute the entire artifact (e.g., `org.codehaus.groovy` → `org.apache.groovy`)

## Common Issues and Solutions

### Issue 1: ByteBuddy ASM Error

**Error:**

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available on current VM: net.bytebuddy.jar.asmjdkbridge.JdkClassWriter
```

**Solution:** Upgrade ByteBuddy to 1.17.5+

### Issue 2: Groovy Compilation Failures

**Error:** Various `GroovyCompilationError` messages

**Solution:**

1. First try Groovy 5.0.3 with code fixes
2. If not feasible, fall back to Groovy 4.0.29 (last 4.x version)

```toml
# Fallback option
[versions]
groovy = "4.0.29"
spock = "2.4-groovy-4.0"
```

### Issue 3: Spring Boot Forces Groovy 4.x

**Error:** `IncompatibleGroovyVersionException: Spock 2.4.0-groovy-5.0 is not compatible with Groovy 4.0.29`

**Cause:** Spring Boot's BOM manages Groovy and pulls in 4.x versions

**Solution:** Override using Spring Boot's property mechanism:

```groovy
// In root build.gradle
allprojects {
    ext['groovy.version'] = libs.versions.groovy.get()
}
```

With version catalog:

```toml
[versions]
groovy = "5.0.3"
```

### Issue 4: `groovy-all` Transitive Dependency Conflicts

**Error:** `Unsupported class file major version 69` or mixed Groovy 4.x/5.x classes

**Cause:** Using `groovy-all:5.0.3` which incorrectly references Groovy 4.0.29 sub-modules

**Solution:** Remove `groovy-all` and rely on `spock-core` transitives:

```groovy
// ❌ REMOVE this
testImplementation libs.groovy.all

// ✅ KEEP only spock-core (brings Groovy 5.0.3 transitively)
testImplementation libs.spock.core
```

**Verification:** Check dependencies to ensure no Groovy 4.x modules:

```bash
./gradlew :module:dependencies --configuration testCompileClasspath 2>&1 | grep -i groovy
# Should show only groovy:5.0.3, NOT groovy-ant:4.0.29 etc.
```

### Issue 5: Plugin Compatibility Errors

**Error:** `Plugin X is not compatible with Gradle 9`

**Solution:** Update plugins to Gradle 9 compatible versions (see Pattern 1)

### Issue 6: Missing groovy.json Classes

**Error:**

```
unable to resolve class groovy.json.JsonSlurper
unable to resolve class groovy.json.JsonOutput
```

**Cause:** The `spock-core` artifact only brings the base `groovy` module, not `groovy-json`

**Solution:** Add `groovy-json` explicitly:

```groovy
testImplementation libs.spock.core
testImplementation libs.groovy.json  // For JsonSlurper/JsonOutput
```

With version catalog:

```toml
[libraries]
groovy-json = { module = "org.apache.groovy:groovy-json", version.ref = "groovy" }
```

### Issue 7: Certificate Errors

**Error:** `PKIX path building failed`

**Solution:** Import Cloudflare CA certificate into Java 25 truststore:

```bash
JAVA_HOME=$(sdk home java 25.0.1-tem)
sudo keytool -importcert -trustcacerts \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -file ~/cloudflare-certificates/2025_cloudflare_ca_certificate.pem \
    -alias cloudflare-ca-2025 \
    -noprompt
```

### Issue 8: Gradle 9 Deprecated APIs

**Error:** `Could not set unknown property 'archivesBaseName'` or `org/gradle/api/plugins/JavaPluginConvention`

**Cause:** Gradle 9 removed deprecated APIs like `archivesBaseName` and `JavaPluginConvention`

**Solutions:**

1. **Replace `archivesBaseName`:**

```groovy
// ❌ Old (Gradle 8.x)
jar {
    archivesBaseName = 'myproject'
}

// ✅ New (Gradle 9.x)
base {
    archivesName = 'myproject'
}
```

1. **Replace `FileNameFinder`:**

```groovy
// ❌ Old
new FileNameFinder().getFileNames(...)

// ✅ New
new groovy.ant.FileNameFinder().getFileNames(...)
```

1. **Update plugins using deprecated APIs** - Flyway, jOOQ, etc. need latest versions

### Issue 9: Spock Version Mismatch

**Error:** `NoClassDefFoundError` in Spock tests

**Solution:** Ensure Spock suffix matches Groovy version:

- Groovy 5.x → `spock = "2.4-groovy-5.0"`
- Groovy 4.x → `spock = "2.4-groovy-4.0"`

### Issue 10: IDEA Plugin testSourceDirs Removed

**Error:** `Could not get unknown property 'testSourceDirs' for object of type org.gradle.plugins.ide.idea.internal.IdeaModuleInternal`

**Cause:** Gradle 9 removed `testSourceDirs` from the IDEA plugin

**Solution:** Remove the manual IDEA configuration - modern plugins auto-configure:

```groovy
// ❌ Old (Gradle 8.x)
apply plugin: 'java-test-fixtures'
idea {
    module {
        testSourceDirs += file('src/testFixtures/java/')
    }
}

// ✅ New (Gradle 9.x)
apply plugin: 'java-test-fixtures'
// The java-test-fixtures plugin automatically configures IDE support
```

### Issue 11: Testcontainers @Container Annotation Conflict

**Error:** `No explicit/default value found for annotation attribute 'value' in @spock.lang.Subject$Container`

**Cause:** Wrong import - `spock.lang.Subject.Container` instead of `org.testcontainers` annotation

**Solution:** Fix the import:

```groovy
// ❌ Wrong
import org.testcontainers.spock.Testcontainers
import spock.lang.Subject.Container  // WRONG!

// ✅ Correct - Use JUnit Jupiter annotations or remove Spock annotations
// The @Container annotation is managed automatically by Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class MyTest {
    def container = new GenericContainer(DockerImageName.parse("redis:7")).withExposedPorts(6379)
}
```

## Upgrade Checklist

Use this checklist when upgrading a project to Java 25:

### Environment Setup

- [ ] Install Java 25 via sdkman: `sdk install java 25.0.1-tem`
- [ ] Import Cloudflare certificate into Java 25 truststore

### Dependency Analysis

- [ ] **Run dependency graph to understand current dependencies** (see below)
- [ ] Check for `groovy-all` usage (must be removed)
- [ ] Check for hardcoded Java 21 toolchain in submodules (must be removed)

### Build Tool Upgrades

- [ ] Upgrade Gradle wrapper to **9.2.1**
- [ ] Update Flyway plugin to **11.19.0** (if present)
- [ ] Update jOOQ plugin to **10.1.1** (if present)
- [ ] Update Protobuf plugin to **0.9.5** (if present)
- [ ] Update SonarQube plugin to **7.2.1.6560**
- [ ] Update Spotless plugin to **8.1.0** with palantir-java-format **2.74.0**

### Testing Libraries

- [ ] Update Spock to **2.4-groovy-5.0**
- [ ] Update Groovy to **5.0.3** (via Spring Boot property override or version catalog)
- [ ] **Remove `groovy-all` dependency if present** (use spock-core transitives)
- [ ] Add `groovy-json` if tests use JsonSlurper/JsonOutput
- [ ] Update Testcontainers to **1.21.4** (1.x) or **2.0.3** (2.x)
- [ ] Add JUnit Platform Launcher dependency (required for Gradle 9)

### Java/Lombok

- [ ] Update Lombok to **1.18.42**
- [ ] Update Lombok plugin to **9.1.0**
- [ ] Update ByteBuddy to **1.17.5+** (if defined)

### Gradle 9 Compatibility Fixes

- [ ] Replace `archivesBaseName` with `base { archivesName = ... }`
- [ ] Replace `FileNameFinder` with `groovy.ant.FileNameFinder`
- [ ] Fix `classDirectories` in jacoco.gradle (use afterEvaluate, not doFirst)
- [ ] Remove `project.exec` deprecation (use Exec task type)
- [ ] Check pitest plugin - remove `timeoutConstant` property if present

### Validation

- [ ] Configure Java 25 toolchain in build.gradle (**local validation only - do not commit**)
- [ ] Remove hardcoded Java 21 toolchain from ALL submodules
- [ ] Build without tests: `./gradlew clean build -x test`
- [ ] Run tests: `./gradlew test`
- [ ] **Only after all tests pass**: commit toolchain change with `/upgrade-to-java-25`

### Dependency Graph Analysis

Before making changes, understand where dependencies come from:

```bash
# Install dependency graph plugin
cp gradle/dependency-graph-init.gradle ~/.gradle/init.d/

# Run dependency insight for specific libraries
./gradlew :module:dependencyInsight --dependency groovy --configuration testCompileClasspath
./gradlew :module:dependencyInsight --dependency spock --configuration testCompileClasspath
```

This helps identify:

- Which dependencies bring in Groovy versions
- Whether `groovy-all` is being used (remove it!)
- Transitive conflicts that need resolution

## Fallback Strategy

If Groovy 5.x causes too many compatibility issues:

1. Keep all other Java 25 upgrades (Gradle 9, plugins, etc.)
2. Use Groovy 4.0.29 (last 4.x version) with Spock 2.4-groovy-4.0
3. Document the Groovy 5.x issues for future resolution

```toml
# Fallback configuration
[versions]
groovy = "4.0.29"
spock = "2.4-groovy-4.0"
# All other versions remain at Java 25 targets
```

## Version History

| Date | Change |
|------|--------|
| December 2025 | Initial Java 25 golden path based on motorsito java25 branch |

## References

- **motorsito java25 branch**: <https://github.com/bitsoex/motorsito/tree/java25>
- **ByteBuddy 1.17.5 release notes**: Added ASM 9.8 for Java 25 support
- **Gradle 9.0 release notes**: <https://docs.gradle.org/9.0/release-notes.html>
- **Groovy 5.0 release notes**: <https://groovy-lang.org/releasenotes/groovy-5.0.html>
- **Spock 2.4 release**: <https://github.com/spockframework/spock/releases>

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/golden-paths/java-25-upgrade.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
