---
title: Resolution Strategies
description: Dependency conflict resolution and substitution patterns
---

# Resolution Strategies

Dependency conflict resolution, version forcing, and substitution patterns.

## Contents

- [Dependency Verification](#dependency-verification)
- [Resolution Strategy](#resolution-strategy)
- [Version Forcing](#version-forcing)
- [Substitution Rules](#substitution-rules)
- [Override Transitive Versions](#override-transitive-versions)
- [Related](#related)

---
## Dependency Verification

### Check What Version Is Used

```bash
# Show full dependency tree
./gradlew dependencies

# Show specific module dependencies
./gradlew :module:dependencies

# Show transitive dependency path
./gradlew :module:dependencyInsight --dependency org.springframework
```

### Verify No Conflicts

```bash
# Check for version conflicts
./gradlew dependencyInsight --dependency commons-lang3

# Show dependency tree with conflicts highlighted
./gradlew dependencies --warning-mode all
```

### Find Why a Dependency Exists

```bash
# Trace why a dependency is included
./gradlew dependencyInsight --dependency jackson-databind --configuration compileClasspath
```

## Resolution Strategy

Configure resolution strategy in root `build.gradle`:

```groovy
configurations.configureEach {
    resolutionStrategy {
        // Fail on version conflicts (strict mode)
        failOnVersionConflict()

        // Or prefer highest version
        // preferProjectModules()

        // Cache dynamic versions for 24 hours
        cacheDynamicVersionsFor 24, 'hours'

        // Cache changing modules for 0 minutes (always check)
        cacheChangingModulesFor 0, 'minutes'
    }
}
```

### JUnit Version Alignment

Force JUnit 5 version alignment (common issue):

```groovy
configurations.configureEach {
    resolutionStrategy.eachDependency { details ->
        // Align all JUnit 5 modules to same version
        if (details.requested.group == 'org.junit.jupiter') {
            details.useVersion libs.versions.junit.jupiter.get()
        }
        if (details.requested.group == 'org.junit.platform') {
            details.useVersion libs.versions.junit.platform.get()
        }
    }
}
```

## Version Forcing

Force specific versions for security or compatibility:

```groovy
configurations.configureEach {
    resolutionStrategy {
        // Force specific versions
        force libs.commons.lang3
        force libs.commons.compress
        force libs.jakarta.el
        force libs.bouncycastle.bcprov.jdk18on
    }
}
```

### Force via Constraints

Alternative approach using constraints:

```groovy
dependencies {
    constraints {
        implementation(libs.commons.lang3) {
            because 'CVE-2025-48924 fix'
        }
        implementation(libs.jackson.databind) {
            because 'Security patch for SSRF vulnerability'
        }
    }
}
```

## Substitution Rules

Substitute one dependency for another:

### Replace Module

```groovy
configurations.configureEach {
    resolutionStrategy {
        dependencySubstitution {
            // Replace old module with new one
            substitute module('org.old:deprecated-lib')
                using module('org.new:replacement-lib:1.0.0')
        }
    }
}
```

### Bitso Library Substitution (Common Pattern)

```groovy
configurations.configureEach {
    resolutionStrategy.eachDependency { details ->
        // Always use version catalog for Bitso commons libraries
        if (details.requested.group == 'com.bitso.commons') {
            switch (details.requested.name) {
                case 'redis':
                    details.useVersion libs.versions.bitso.commons.redis.get()
                    details.because 'Centralized in version catalog'
                    break
                case 'kafka':
                    details.useVersion libs.versions.bitso.commons.kafka.get()
                    break
            }
        }
    }
}
```

### Exclude Transitives

```groovy
dependencies {
    implementation(libs.some.library) {
        // Exclude specific transitive
        exclude group: 'org.old', module: 'deprecated-lib'
    }
}
```

## Override Transitive Versions

Sometimes transitive dependencies conflict. Override only when necessary:

```groovy
dependencies {
    // Override transitive version with reason
    implementation(libs.transitive.dep) {
        because 'Fix for security issue CVE-2025-XXXX'
    }
}
```

### Via Constraints (Preferred)

```groovy
dependencies {
    constraints {
        // Force minimum version across all configurations
        implementation('org.example:transitive-lib:2.0.0') {
            because 'Security fix required'
        }
    }
}
```

### Check Override Applied

```bash
./gradlew dependencyInsight --dependency transitive-lib
```

Verify the output shows your forced version, not the original transitive.

## Related

- [version-centralization.md](version-centralization.md) - Version policies
- [security-updates.md](security-updates.md) - Security-related forcing
- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/resolution-strategies.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

