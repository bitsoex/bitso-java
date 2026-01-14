<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/multi-module.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

# Multi-Module Gradle Projects

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
