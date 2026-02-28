# Contract Distribution Build Configuration Examples

## Contents

- [Proto-Only Module (Published)](#proto-only-module-published)
- [Generated Module (Internal Only)](#generated-module-internal-only)
- [Service Module Using Generated Protos](#service-module-using-generated-protos)
- [Settings.gradle Configuration](#settingsgradle-configuration)
- [Version Management in gradle.properties](#version-management-in-gradleproperties)
- [Directory Structure Reference](#directory-structure-reference)
- [External Consumer Configuration](#external-consumer-configuration)
- [Build Optimization](#build-optimization)
- [Validation Commands](#validation-commands)

---
## Proto-Only Module (Published)

```gradle
// account-protos/build.gradle

plugins {
    id 'java-library'
    id 'bitso.publish'
}

// NO dependencies
// NO protobuf plugin
// NO compilation configuration

// Proto files in src/main/resources/ are automatically packaged

// The bitso.publish plugin handles:
// - Automatic versioning from gradle.properties
// - Publication to Artifactory
// - POM generation
```

## Generated Module (Internal Only)

```gradle
// account-protos-generated/build.gradle

plugins {
    id 'java'
    id 'com.google.protobuf' version '0.9.4'
}

dependencies {
    // Reference proto-only module to get .proto files
    implementation project(':account-protos')

    // gRPC and protobuf dependencies for compilation
    implementation 'io.grpc:grpc-stub:1.58.0'
    implementation 'io.grpc:grpc-protobuf:1.58.0'
    implementation 'com.google.protobuf:protobuf-java:3.24.0'

    // Common annotations
    compileOnly 'javax.annotation:javax.annotation-api:1.3.2'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.0"
    }

    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
    }

    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}

// DO NOT PUBLISH THIS MODULE
// It's for internal compilation only
// External consumers will compile protos themselves
```

## Service Module Using Generated Protos

```gradle
// account-service/build.gradle

plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

dependencies {
    // Use -generated module (NOT proto-only module)
    implementation project(':account-protos-generated')

    // Spring Boot and gRPC dependencies
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'net.devh:grpc-server-spring-boot-starter:2.15.0'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.grpc:grpc-testing:1.58.0'
}
```

## Settings.gradle Configuration

```gradle
// settings.gradle

rootProject.name = 'account-platform'

// Proto modules
include 'account-protos'
include 'account-protos-generated'  // NEW
include 'transfer-protos'
include 'transfer-protos-generated'  // NEW

// Service modules
include 'account-service'
include 'transfer-service'
```

## Version Management in gradle.properties

**CRITICAL: gRPC migration is a BREAKING CHANGE. MAJOR version bump is MANDATORY.**

```properties
# gradle.properties

# MANDATORY: Major version bump for gRPC migration (BREAKING CHANGE)
# Before migration: version=1.5.3
# After migration:  version=2.0.0 (NOT 1.6.0 or 1.5.4!)
version=2.0.0

# Group ID for publication
group=com.bitso.platform

# Java version
sourceCompatibility=21
targetCompatibility=21
```

**Why this matters:**
- Contract distribution migration breaks external consumers' build process
- Consumers must now compile protos themselves (major workflow change)
- Using MINOR/PATCH causes silent breakage and production incidents
- Semantic versioning requires MAJOR bump for breaking changes

## Directory Structure Reference

```
project-root/
├── settings.gradle
├── bitso-protos/
│   ├── account-protos/
│   │   ├── build.gradle
│   │   └── src/main/resources/
│   │       └── com/bitso/account/v1/
│   │           └── account.proto
│   ├── account-protos-generated/
│   │   └── build.gradle
│   ├── transfer-protos/
│   │   ├── build.gradle
│   │   └── src/main/resources/
│   │       └── com/bitso/transfer/v1/
│   │           └── transfer.proto
│   └── transfer-protos-generated/
│       └── build.gradle
└── bitso-services/
    └── account-service/
        └── build.gradle
```

## External Consumer Configuration

External consumers must configure their own proto compilation:

```gradle
plugins {
    id 'java'
    id 'com.google.protobuf' version '0.9.4'
}

dependencies {
    // New proto-only artifact (no compiled code)
    implementation 'com.bitso.platform:account-protos:2.0.0'

    // Consumer provides gRPC dependencies (their choice of version)
    implementation 'io.grpc:grpc-stub:1.60.0'
    implementation 'io.grpc:grpc-protobuf:1.60.0'
    implementation 'com.google.protobuf:protobuf-java:3.25.0'
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.0"
    }

    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.0"
        }
    }

    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}
```

## Build Optimization

```gradle
// In -generated module, enable parallel proto generation
protobuf {
    generateProtoTasks {
        all().each { task ->
            task.options {
                // Enable parallel compilation
                option 'java_multiple_files=true'
            }
        }
    }
}
```

## Validation Commands

```bash
# Generate proto stubs
./gradlew generateProto

# Generate for specific module
./gradlew :account-protos-generated:generateProto

# Clean generated code
./gradlew :account-protos-generated:clean

# Build validation
./gradlew clean build

# Verify JAR contents
jar tf account-protos-2.0.0.jar | grep .proto
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration-contract-distribution/references/BUILD_EXAMPLES.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

