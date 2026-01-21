# Multi-Module Gradle Projects

## Contents

- [Directory Structure](#directory-structure) (L12-L35)
- [settings.gradle](#settingsgradle) (L36-L47)
- [Root build.gradle](#root-buildgradle) (L48-L72)
- [Inter-Module Dependencies](#inter-module-dependencies) (L73-L82)
- [Dependency Locking for Multi-Module Projects](#dependency-locking-for-multi-module-projects) (L83-L136)

---
## Directory Structure

```text
project-root/
├── gradle/
│   ├── wrapper/
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml
├── build.gradle                (root)
├── settings.gradle             (module registry)
├── bitso-libs/
│   ├── domain/
│   │   └── build.gradle
│   ├── api/
│   │   └── build.gradle
│   └── persistence/
│       └── build.gradle
└── bitso-services/
    ├── service-a/
    │   └── build.gradle
    └── service-b/
        └── build.gradle
```

## settings.gradle

```groovy
rootProject.name = 'project-name'

include 'bitso-libs:domain'
include 'bitso-libs:api'
include 'bitso-libs:persistence'
include 'bitso-services:service-a'
include 'bitso-services:service-b'
```

## Root build.gradle

```groovy
plugins {
    id 'java'
    id 'jacoco'
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
```

## Inter-Module Dependencies

```groovy
// In bitso-services/service-a/build.gradle
dependencies {
    implementation project(':bitso-libs:domain')
    implementation project(':bitso-libs:api')
}
```

## Dependency Locking for Multi-Module Projects

**Critical**: Each submodule needs its own `gradle.lockfile` for reproducible builds.

### Enable Locking for All Modules

```groovy
// root build.gradle
allprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}
```

### Generate Lockfiles for ALL Modules

```bash
# ✅ CORRECT: Generates lockfiles for ALL submodules
./gradlew build --write-locks -x test

# ❌ WRONG: Only generates for ROOT project
./gradlew dependencies --write-locks
```

### Expected Lockfile Structure

```text
project-root/
├── gradle.lockfile                    # Root (jacoco, buildscript)
├── bitso-libs/
│   ├── domain/
│   │   └── gradle.lockfile            # Domain module
│   ├── api/
│   │   └── gradle.lockfile            # API module
│   └── persistence/
│       └── gradle.lockfile            # Persistence module
└── bitso-services/
    ├── service-a/
    │   └── gradle.lockfile            # Service A
    └── service-b/
        └── gradle.lockfile            # Service B
```

### Validate Lockfile Coverage

```bash
# Count should match (or be close to) number of build.gradle files
lockfiles=$(find . -name "gradle.lockfile" -type f | wc -l)
buildfiles=$(find . -name "build.gradle" -type f | wc -l)
echo "Lockfiles: $lockfiles / Build files: $buildfiles"
```

See [native-dependency-locking.md](native-dependency-locking.md#multi-module-projects) for complete multi-module locking guide.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/multi-module.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

