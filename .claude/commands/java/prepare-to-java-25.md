# Prepare Java projects for Java 25 compatibility (Gradle 9, Groovy 5, updated plugins) - LOCAL VALIDATION ONLY

**Description:** Prepare Java projects for Java 25 compatibility (Gradle 9, Groovy 5, updated plugins) - LOCAL VALIDATION ONLY

# 🤖 📦 Prepare to Java 25

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking for confirmation.

This command **prepares** Java projects for Java 25 compatibility by upgrading dependencies and validating locally. Java 25 requires significant dependency upgrades including Gradle 9.x, Groovy 5.x, and updated build plugins.

## ⚠️ CRITICAL: Do NOT Commit Toolchain Change

This command is for **local validation only**. The Java 25 toolchain change should **NOT** be committed until all tests pass locally.

**Workflow:**

1. `/prepare-to-java-25` - Upgrade dependencies, validate locally with Java 25 toolchain
2. `/upgrade-to-java-25` - After validation passes, commit the toolchain change

The toolchain configuration is necessary for local validation but should only be committed when the project is fully ready for Java 25.

## Related Documents

- **Golden Path**: `java/golden-paths/java-25-upgrade.md` - Detailed upgrade patterns and troubleshooting
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Version catalog strategy
- **Upgrade to Recommended**: `java/commands/upgrade-to-recommended-versions.md` - Spring Boot 3.5.x upgrade (do this first if on older Spring Boot)

## Prerequisites

1. **Spring Boot 3.5.x** - Project should already be on Spring Boot 3.5.x before Java 25 upgrade
2. **sdkman** - For managing Java versions: `curl -s "https://get.sdkman.io" | bash`
3. **Java 25 Temurin** - Install via sdkman: `sdk install java 25.0.1-tem`

## Target Versions for Java 25

### Primary Versions (Groovy 5.x - Recommended)

| Component | Version | Notes |
|-----------|---------|-------|
| **Java** | **25** | Target JDK version |
| **Gradle** | **9.2.1** | Major version bump required |
| **Groovy** | **5.0.3** | Java 25 requires Groovy 5.x |
| **Spock** | **2.4-groovy-5.0** | Must match Groovy version |
| **Lombok** | **1.18.42** | Required for Java 25 bytecode |
| **Lombok Plugin** | **9.1.0** | Freefair plugin for Gradle 9 |
| **Spotless** | **8.1.0** | Updated for Gradle 9 |
| **palantir-java-format** | **2.74.0** | Java 25 bytecode support (use with Spotless) |
| **SonarQube Plugin** | **7.2.2.6593** | Updated for Gradle 9 |
| **Testcontainers** | **1.21.4** or **2.0.3** | Use same major version as project |
| **ByteBuddy** | **1.17.5+** | Minimum for Java 25 ASM support |
| **Flyway Plugin** | **11.19.0** | Gradle 9 compatible |
| **jOOQ Plugin** | **10.1.1** | Gradle 9 compatible |
| **Protobuf Plugin** | **0.9.5** | Gradle 9 compatible |

### Fallback Versions (Groovy 4.x - Last 4.x Generation)

Only use these if Groovy 5.x causes compatibility issues:

| Component | Version | Notes |
|-----------|---------|-------|
| **Groovy** | **4.0.29** | Last version of 4.x generation |
| **Spock** | **2.4-groovy-4.0** | Matches Groovy 4.x |

**Note**: Groovy 5.x is strongly recommended. Only fall back to Groovy 4.x if you encounter specific compatibility issues that cannot be resolved.

## ⚠️ CRITICAL: DO NOT DOWNGRADE

These versions should **NOT** be changed unless explicitly requested:

- **protobuf**: Keep existing version (3.x → 4.x is breaking)
- **grpc**: Keep existing version
- **Spring Boot**: Keep at 3.5.x (already upgraded)

## Workflow

### 1. Create Jira Ticket

Search for existing tickets, create if none found:

- **Summary**: `🤖 📦 Upgrade [repo-name] to Java 25`
- **Parent**: Current Sprint/Cycle KTLO Epic

### 2. Install Java 25 via sdkman

```bash
# Install Java 25 Temurin
sdk install java 25.0.1-tem

# Use Java 25 for this session
sdk use java 25.0.1-tem

# Verify
java -version
# Expected: openjdk version "25.0.1" 2025-10-14
```

### 3. Configure Cloudflare Certificates (Bitso Network)

Java 25 needs Cloudflare CA certificates to download dependencies through Bitso's network:

```bash
# Find the Java 25 installation path
JAVA_HOME=$(sdk home java 25.0.1-tem)

# Import Cloudflare certificate into Java 25 truststore
sudo keytool -importcert -trustcacerts \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -file ~/cloudflare-certificates/2025_cloudflare_ca_certificate.pem \
    -alias cloudflare-ca-2025 \
    -noprompt

# Verify import
keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | grep cloudflare
```

If you don't have the Cloudflare certificate:

1. Download from [Confluence: Configure Cloudflare CA certificate](https://bitsomx.atlassian.net/wiki/spaces/SEA/pages/4358963539/Configure+Cloudflare+CA+certificate+to+applications)
2. Save to `~/cloudflare-certificates/2025_cloudflare_ca_certificate.pem`

### 4. Create Feature Branch

```bash
git fetch --all && git pull origin main
git checkout -b "chore/${JIRA_KEY}-upgrade-java-25"
```

### 5. Upgrade Gradle Wrapper to 9.2.1

```bash
./gradlew wrapper --gradle-version=9.2.1
```

### 6. Analyze Dependencies with Dependency Graph

Before making changes, understand where dependencies come from:

```bash
# Run dependency insight for specific libraries
./gradlew :module:dependencyInsight --dependency groovy --configuration testCompileClasspath
./gradlew :module:dependencyInsight --dependency spock --configuration testCompileClasspath
```

This helps identify transitive conflicts and dependencies that need resolution.

### 7. Update Version Catalog (`gradle/libs.versions.toml`)

Update or add these versions. **All versions MUST be defined in the version catalog** - never hardcode versions directly:

```toml
[versions]
# Java 25 compatible versions
spock = "2.4-groovy-5.0"
lombok = "1.18.42"

# Testcontainers - use same major version as project
# testcontainers = "1.21.4"  # For projects on 1.x
testcontainers = "2.0.3"     # For projects on 2.x

# If ByteBuddy is defined (used by Mockito, etc.)
bytebuddy = "1.17.5"

[plugins]
# Updated plugins for Gradle 9
lombok = "io.freefair.lombok:9.1.0"
spotless = "com.diffplug.spotless:8.1.0"
sonarqube = "org.sonarqube:7.2.1.6560"
```

### ⚠️ CRITICAL: Do NOT Use `groovy-all`

**Never add `groovy-all` dependency**. The `groovy-all:5.0.3` artifact has broken transitive dependencies that reference Groovy 4.0.29 sub-modules.

```groovy
// ❌ NEVER: groovy-all has broken transitive deps
testImplementation 'org.apache.groovy:groovy-all:5.0.3'

// ✅ CORRECT: Let spock-core bring in Groovy 5.0.3 transitively
testImplementation libs.spock.core
testImplementation libs.spock.spring  // if using Spring
```

If the project currently has `groovy-all`, **remove it**.

### Add `groovy-json` for JsonSlurper/JsonOutput

If tests use `groovy.json.JsonSlurper` or `groovy.json.JsonOutput`, add `groovy-json` explicitly:

```toml
[libraries]
groovy-json = { module = "org.apache.groovy:groovy-json", version.ref = "groovy" }
```

```groovy
testImplementation libs.groovy.json
```

### Override Spring Boot's Groovy Version

Spring Boot's BOM manages Groovy and pulls in 4.x versions. Override using Spring Boot's property mechanism:

```groovy
// In root build.gradle
allprojects {
    // Force Groovy 5.0.3 for Java 25 compatibility with Spock 2.4-groovy-5.0
    ext['groovy.version'] = libs.versions.groovy.get()
}
```

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

**Lombok Configuration**: The Freefair Lombok plugin requires Lombok 1.18.42 for Java 25 support. Configure it in the version catalog:

```toml
[versions]
lombok = "1.18.42"
```

Then reference in `build.gradle`:

```groovy
lombok {
    version = libs.versions.lombok.get()
}
```

### 8. Update Java Toolchain in `build.gradle` (LOCAL VALIDATION ONLY)

**⚠️ This change is for local validation only - do NOT commit until all tests pass.**

```groovy
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

### 9. Add JUnit Platform Launcher (Required for Gradle 9)

In modules that use `useJUnitPlatform()`:

```groovy
dependencies {
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

Or globally in root `build.gradle`:

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        }
    }
}
```

### 10. Update ByteBuddy (If Defined)

If your project explicitly defines ByteBuddy (common with Mockito):

```toml
[versions]
bytebuddy = "1.17.5"

[libraries]
bytebuddy = { module = "net.bytebuddy:byte-buddy", version.ref = "bytebuddy" }
bytebuddy-agent = { module = "net.bytebuddy:byte-buddy-agent", version.ref = "bytebuddy" }
```

**Why 1.17.5+?** ByteBuddy 1.17.5 added ASM 9.8 support for Java 25 bytecode. Earlier versions will fail with:

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available on current VM: net.bytebuddy.jar.asmjdkbridge.JdkClassWriter
```

### 11. Validate Build

```bash
# Ensure using Java 25
sdk use java 25.0.1-tem

# Build without tests first
./gradlew clean build -x test 2>&1 | tee /tmp/gradle-build-java25.log | grep -E "FAILED|Error" || echo "Build successful"

# Run tests
./gradlew test 2>&1 | tee /tmp/gradle-test-java25.log | grep -E "FAILED|Error" || echo "All tests passed"
```

### 12. Next Steps After Validation

**If all tests pass locally:**

Use `/upgrade-to-java-25` to commit the toolchain change and create the PR.

**If tests fail:**

1. Investigate failures using dependency graph analysis
2. Check for `groovy-all` usage (remove it)
3. Verify Spock/Groovy version alignment
4. Check for ByteBuddy version issues
5. Refer to troubleshooting section below

## Troubleshooting

### Gradle 9 Plugin Compatibility Errors

**Symptom:** Plugin fails to load or deprecated API errors

**Cause:** Plugins not compatible with Gradle 9

**Fix:** Update to Gradle 9 compatible versions:

```toml
[plugins]
lombok = "io.freefair.lombok:9.1.0"
spotless = "com.diffplug.spotless:8.1.0"
sonarqube = "org.sonarqube:7.2.1.6560"
```

### ByteBuddy Java 25 Incompatibility

**Symptom:**

```
java.lang.IllegalStateException: Could not invoke proxy: Type not available on current VM: net.bytebuddy.jar.asmjdkbridge.JdkClassWriter
```

**Cause:** ByteBuddy version doesn't support Java 25 bytecode

**Fix:** Upgrade ByteBuddy to 1.17.5+:

```toml
[versions]
bytebuddy = "1.17.5"
```

### Groovy 5.x Compilation Errors

**Symptom:** Groovy compilation fails with syntax or type errors

**Cause:** Groovy 5.x has stricter type checking and removed deprecated features

**Fix options:**

1. Update Groovy code to be compatible with Groovy 5.x
2. Fall back to Groovy 4.0.29 (last 4.x version):

```toml
[versions]
groovy = "4.0.29"
spock = "2.4-groovy-4.0"
```

### `groovy-all` Transitive Dependency Conflicts

**Symptom:** `Unsupported class file major version 69`, NullPointerException in Spock tests, or mixed Groovy 4.x/5.x class errors

**Cause:** Using `groovy-all:5.0.3` which has broken transitive dependencies referencing Groovy 4.0.29 sub-modules

**Verification:**

```bash
./gradlew :module:dependencies --configuration testCompileClasspath 2>&1 | grep -i groovy
# If you see groovy-ant:4.0.29, groovy-json:4.0.29 etc., groovy-all is the problem
```

**Fix:** Remove `groovy-all` and rely on `spock-core` transitives:

```groovy
// ❌ REMOVE this line
testImplementation libs.groovy.all

// ✅ KEEP only spock-core (brings Groovy 5.0.3 transitively)
testImplementation libs.spock.core
```

Also remove from version catalog if present:

```toml
# ❌ REMOVE this library definition
# groovy-all = { module = "org.apache.groovy:groovy-all", version.ref = "groovy" }
```

### Spock Version Mismatch

**Symptom:** `NoClassDefFoundError` or `IncompatibleClassChangeError` in Spock tests

**Cause:** Spock version doesn't match Groovy version

**Fix:** Ensure Spock suffix matches Groovy major version:

| Groovy Version | Spock Version |
|----------------|---------------|
| 5.0.x | `2.4-groovy-5.0` |
| 4.0.x | `2.4-groovy-4.0` |

### Testcontainers Docker Socket Issues

**Symptom:** Testcontainers fails to connect to Docker

**Cause:** Older Testcontainers versions have issues with Docker Unix socket on newer JVMs

**Fix:** Upgrade to latest version of your current major:

```toml
[versions]
# For projects on Testcontainers 1.x
testcontainers = "1.21.4"

# For projects on Testcontainers 2.x
testcontainers = "2.0.3"
```

**Important**: Don't change major versions (1.x ↔ 2.x) unless specifically planned, as this may require code changes.

### JUnit Platform Launcher Missing

**Symptom:** `NoClassDefFoundError: org/junit/platform/launcher/...`

**Cause:** Gradle 9 requires explicit JUnit Platform Launcher dependency

**Fix:** Add to test runtime:

```groovy
dependencies {
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### Missing groovy.json Classes

**Symptom:** `unable to resolve class groovy.json.JsonSlurper`

**Cause:** `spock-core` only brings base `groovy` module, not `groovy-json`

**Fix:** Add `groovy-json` explicitly:

```groovy
testImplementation libs.groovy.json
```

### Hardcoded Java 21 Toolchain in Submodules

**Symptom:** `'project :module' is only compatible with JVM runtime version 25 or newer`

**Cause:** Submodules have hardcoded Java 21 toolchain that must be removed

**Fix:** Remove hardcoded toolchain blocks from ALL submodules:

```groovy
// ❌ REMOVE this from submodule build.gradle files
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// ✅ REPLACE with comment - let root build.gradle set toolchain
// Java toolchain configured in root build.gradle for Java 25
```

### Cloudflare Certificate Errors

**Symptom:** `PKIX path building failed` or `unable to find valid certification path`

**Cause:** Java 25 truststore doesn't have Cloudflare CA certificate

**Fix:** Import certificate as shown in Step 3, or verify import:

```bash
keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | grep cloudflare
```

## Version Compatibility Matrix

| Component | Java 21 | Java 25 | Notes |
|-----------|---------|---------|-------|
| Gradle | 8.14.3 | 9.2.1+ | Major version required |
| Groovy | 4.0.x | 5.0.3 | Major version required |
| Spock | 2.4-groovy-4.0 | 2.4-groovy-5.0 | Match Groovy version |
| Lombok | 1.18.x | 1.18.42 | Java 25 bytecode support |
| Lombok Plugin | 8.14.2 | 9.1.0 | Freefair plugin for Gradle 9 |
| Spotless | 6.x | 8.1.0 | Gradle 9 compat |
| SonarQube | 6.x | 7.2.x | Gradle 9 compat |
| Testcontainers 1.x | 1.21.x | 1.21.4 | Latest 1.x version |
| Testcontainers 2.x | 2.0.2 | 2.0.3 | Docker socket fix |
| ByteBuddy | 1.14.x | 1.17.5+ | ASM 9.8 for Java 25 |

## Reference

- **motorsito java25 branch**: <https://github.com/bitsoex/motorsito/tree/java25>
- **ByteBuddy 1.17.5 release**: <https://github.com/raphw/byte-buddy/releases/tag/byte-buddy-1.17.5>
- **Gradle 9 release notes**: <https://docs.gradle.org/9.0/release-notes.html>
- **Groovy 5.0 release notes**: <https://groovy-lang.org/releasenotes/groovy-5.0.html>

## Related Commands

- `/upgrade-to-recommended-versions` - Upgrade to Spring Boot 3.5.x first
- `/improve-test-setup` - Configure JaCoCo and testing libraries
