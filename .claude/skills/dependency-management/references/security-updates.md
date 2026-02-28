---
title: Security Updates
description: Managing vulnerable dependencies and security patches
---

# Security Updates

Managing vulnerable dependencies and applying security patches.

## Contents

- [Vulnerable Dependency Versions](#vulnerable-dependency-versions)
- [Forcing Security Patches](#forcing-security-patches)
- [Common CVE Fixes](#common-cve-fixes)
- [Spring Boot 3.5.x Requirements](#spring-boot-35x-requirements)
- [Verification](#verification)
- [Related](#related)

---
## Vulnerable Dependency Versions

Always use minimum secure versions in version catalog:

```toml
[versions]
# Security updates - minimum secure versions
commons-compress = "1.24.0"   # CVE-2023-42503 fix
commons-lang3 = "3.18.0"      # Security patches
jakarta-el = "2.0.1"          # CVE-2021-33813 fix
bouncycastle = "1.76"         # Cryptography security
snakeyaml = "2.0"             # CVE-2022-1471 fix
```

### Check for Known Vulnerabilities

```bash
# Using Gradle dependency check plugin
./gradlew dependencyCheckAnalyze

# Or with GitHub Dependabot
# Check .github/dependabot.yml configuration
```

## Forcing Security Patches

Force security-critical versions in root `build.gradle`:

```groovy
configurations.configureEach {
    resolutionStrategy {
        // Force specific versions for security
        force libs.commons.lang3
        force libs.commons.compress
        force libs.jakarta.el
        force libs.bouncycastle.bcprov.jdk18on
        force libs.bouncycastle.bcpkix.jdk18on
        force libs.snakeyaml
    }
}
```

### Using Constraints (Preferred)

```groovy
dependencies {
    constraints {
        implementation(libs.commons.lang3) {
            because 'CVE-2025-48924 fix - requires 3.18.0+'
        }
        implementation(libs.snakeyaml) {
            because 'CVE-2022-1471 - requires 2.0+'
        }
    }
}
```

## Common CVE Fixes

| Library | Minimum Version | CVE | Description |
|---------|-----------------|-----|-------------|
| **commons-compress** | 1.24.0 | CVE-2023-42503 | Zip slip vulnerability |
| **commons-lang3** | 3.18.0 | CVE-2025-48924 | Uncontrolled recursion in ClassUtils |
| **jakarta-el** | 2.0.1 | - | Security best practice |
| **snakeyaml** | 2.0 | CVE-2022-1471 | Arbitrary code execution |
| **jackson-databind** | 2.15.0 | CVE-2022-42003 | SSRF vulnerability |
| **log4j-core** | 2.17.1 | CVE-2021-44228 | Log4Shell |
| **spring-security** | 6.2.1 | CVE-2024-22243 | Auth bypass |

### Log4j Specific

```groovy
configurations.configureEach {
    resolutionStrategy {
        // Ensure Log4j 2.17.1+ everywhere
        eachDependency { details ->
            if (details.requested.group == 'org.apache.logging.log4j') {
                details.useVersion '2.17.1'
                details.because 'CVE-2021-44228 (Log4Shell) fix'
            }
        }
    }
}
```

## Spring Boot 3.5.x Requirements

**IMPORTANT**: Stay on latest or latest-1 patch of Spring Boot 3.5.x to prepare for Spring Boot 4.

For Spring Boot 3.5.x, these library upgrades are required:

| Library | Required Version | Reason |
|---------|------------------|--------|
| **bitso-commons-redis** | 4.2.1 | Jedis 6.x compatibility |
| **jedis4-utils** | 3.0.0 | Jedis 6.x locking support |
| **bitso-rds-iam-authn** | 2.0.0 | Spring Boot 3.5.x compatible |

**WARNING**: Failure to upgrade causes:

```
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams SetParams.px(long)'
```

### Upgrade Command

Use the upgrade command for complete workflow:

```text
/upgrade-to-recommended-versions
```

## Verification

### Check Security Versions Applied

```bash
# PRIMARY: Check lockfile for patched version
grep "commons-lang3" **/gradle.lockfile

# VERIFICATION: Run dependency graph to confirm
./gradlew -I gradle/dependency-graph-init.gradle \
    :ForceDependencyResolutionPlugin_resolveAllDependencies
grep "commons-lang3" build/reports/dependency-graph-snapshots/dependency-list.txt

# DEBUGGING: Trace why a version was selected
./gradlew dependencyInsight --dependency commons-lang3
```

### Verify Dependency Graph

After security updates, verify the dependency graph matches lockfile:

```bash
# Generate dependency graph
./gradlew -I gradle/dependency-graph-init.gradle \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Check for old vulnerable versions
grep -E "commons-lang3.*3\.(0|1[0-3])\." deps.txt
```

### CI Verification

Add to CI pipeline:

```yaml
- name: Check Security Dependencies
  run: |
    ./gradlew dependencyCheckAnalyze
    # Fail if HIGH severity vulnerabilities found
```

## Related

- [resolution-strategies.md](resolution-strategies.md) - Forcing versions
- [compatibility-matrices.md](compatibility-matrices.md) - Version tables
- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/security-updates.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

