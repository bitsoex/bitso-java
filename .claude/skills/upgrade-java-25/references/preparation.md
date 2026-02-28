# Java 25 Preparation Guide

Detailed steps for preparing and validating Java 25 upgrade.

## Contents

- [Prerequisites Checklist](#prerequisites-checklist)
- [Install Java 25 via sdkman](#install-java-25-via-sdkman)
- [Configure Cloudflare Certificates](#configure-cloudflare-certificates)
- [Upgrade Gradle Wrapper](#upgrade-gradle-wrapper)
- [Analyze Dependencies](#analyze-dependencies)
- [Add JUnit Platform Launcher](#add-junit-platform-launcher)
- [Add groovy-json (If Using JsonSlurper)](#add-groovy-json-if-using-jsonslurper)
- [Remove Hardcoded Toolchain from Submodules](#remove-hardcoded-toolchain-from-submodules)
- [Validation Commands](#validation-commands)
- [Troubleshooting](#troubleshooting)
- [Version Compatibility Matrix](#version-compatibility-matrix)

---
## Prerequisites Checklist

- [ ] Spring Boot 3.5.x (upgrade first if needed)
- [ ] sdkman installed
- [ ] Java 25 Temurin installed
- [ ] Cloudflare certificates configured (for Bitso network)

## Install Java 25 via sdkman

```bash
# Install Java 25 Temurin
sdk install java 25.0.1-tem

# Use Java 25 for this session
sdk use java 25.0.1-tem

# Verify
java -version
# Expected: openjdk version "25.0.1" 2025-10-14
```

## Configure Cloudflare Certificates

Java 25 needs Cloudflare CA certificates for Bitso's network:

```bash
JAVA_HOME=$(sdk home java 25.0.1-tem)

sudo keytool -importcert -trustcacerts \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -file ~/cloudflare-certificates/2025_cloudflare_ca_certificate.pem \
    -alias cloudflare-ca-2025 \
    -noprompt

# Verify
keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | grep cloudflare
```

## Upgrade Gradle Wrapper

```bash
./gradlew wrapper --gradle-version=9.2.1
```

## Analyze Dependencies

```bash
# Check Groovy version being used
./gradlew :module:dependencyInsight --dependency groovy --configuration testCompileClasspath

# Check Spock version
./gradlew :module:dependencyInsight --dependency spock --configuration testCompileClasspath
```

## Add JUnit Platform Launcher

Required for Gradle 9:

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        dependencies {
            testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
        }
    }
}
```

## Add groovy-json (If Using JsonSlurper)

```groovy
testImplementation libs.groovy.json
```

## Remove Hardcoded Toolchain from Submodules

Check and remove:

```bash
grep -r "JavaLanguageVersion.of(21)" --include="*.gradle" .
```

## Validation Commands

```bash
# Build without tests first
./gradlew clean build -x test

# Run tests
./gradlew test

# Verify Groovy version
./gradlew :module:dependencies --configuration testCompileClasspath 2>&1 | grep -i groovy
# Should show groovy:5.0.3, NOT groovy:4.0.29
```

## Troubleshooting

### ByteBuddy Java 25 Incompatibility

**Error:**

```text
java.lang.IllegalStateException: Could not invoke proxy:
Type not available on current VM: net.bytebuddy.jar.asmjdkbridge.JdkClassWriter
```

**Fix:** Upgrade ByteBuddy to 1.17.5+

### Groovy Transitive Conflict

**Symptom:** `Unsupported class file major version 69`

**Cause:** Using `groovy-all:5.0.3` which has broken transitive dependencies

**Fix:** Remove `groovy-all`, use `spock-core` only

### Missing groovy.json Classes

**Error:** `unable to resolve class groovy.json.JsonSlurper`

**Fix:** Add `groovy-json` explicitly

### Spock Version Mismatch

**Error:** `NoClassDefFoundError` in Spock tests

**Fix:** Ensure Spock suffix matches Groovy version:

| Groovy | Spock |
|--------|-------|
| 5.0.x | `2.4-groovy-5.0` |
| 4.0.x | `2.4-groovy-4.0` |

## Version Compatibility Matrix

| Component | Java 21 | Java 25 |
|-----------|---------|---------|
| Gradle | 8.14.3 | 9.2.1+ |
| Groovy | 4.0.x | 5.0.3 |
| Spock | 2.4-groovy-4.0 | 2.4-groovy-5.0 |
| Lombok | 1.18.x | 1.18.42 |
| Lombok Plugin | 8.14.2 | 9.2.0 |
| ByteBuddy | 1.14.x | 1.17.5+ |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/upgrade-java-25/references/preparation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

