---
applyTo: "gradle/**,**/build.gradle,**/settings.gradle,**/gradle.properties"
description: Gradle configuration standards, multi-module setup, build patterns, and common commands for Java projects
---

# Gradle Build Configuration

Standards for Gradle configuration, multi-module setup, and build patterns in Java projects.

## Versions & Dependencies

For detailed version management, see: [java/rules/java-versions-and-dependencies.md](java-versions-and-dependencies.md)

**CRITICAL**: All versions MUST be centralized. Never hardcode versions in `build.gradle` files.

### Version Centralization Requirements

| Allowed | Not Allowed |
|---------|-------------|
| `libs.spring.boot.starter.web` | `"org.springframework.boot:spring-boot-starter-web:3.5.8"` |
| `libs.bitso.commons.redis` | `"com.bitso.commons:redis:3.1.0"` |
| Version in `libs.versions.toml` | Version defined inline in build.gradle |

### Anti-Patterns: NEVER Do This

```groovy
// ❌ NEVER: Hardcode versions directly
dependencies {
    implementation "com.bitso.commons:redis:3.1.0"
    implementation "org.springframework.boot:spring-boot-starter-web:3.5.8"
}

// ❌ NEVER: Define versions as local variables
def redisVersion = "3.1.0"
dependencies {
    implementation "com.bitso.commons:redis:${redisVersion}"
}

// ❌ NEVER: Mix centralized and hardcoded
dependencies {
    implementation libs.spring.boot.starter.web  // Good
    implementation "com.bitso.commons:redis:3.1.0"  // Bad!
}
```

### Correct Pattern

```groovy
// ✅ ALWAYS: Use version catalog
dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.bitso.commons.redis
}
```

### Why This Matters

Hardcoded versions cause runtime failures like:

```
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams redis.clients.jedis.params.SetParams.px(long)'
```

This happens when Spring Boot upgrades Jedis but hardcoded library versions expect the old API.

See `java/golden-paths/redis-jedis-compatibility.md` for real examples.

**Benefits of centralization:**

- Single source of truth for versions
- Easy to update across all modules
- Type-safe dependency management
- Prevent version conflicts and NoSuchMethodError

### 2. Apply Plugins at Root Level

### Main `build.gradle` Layout

```groovy
plugins {
    id 'idea'
    id 'eclipse'
    id 'java'
    id 'jacoco'
    id 'org.sonarqube'
}

// Java version enforcement
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// SonarQube configuration
sonar {
    properties {
        property 'sonar.projectName', "Project Name"
        property 'sonar.projectKey', 'project-key'
        property 'sonar.coverage.jacoco.xmlReportPaths', 
            "$projectDir.path/build/reports/jacoco/test/jacocoTestReport.xml"
    }
}

// Repositories (all projects)
allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/bitsoex/packages")
            credentials {
                username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// Dependency management
allprojects {
    dependencies {
        constraints {
            implementation platform(libs.spring.boot.dependencies)
            implementation platform(libs.grpc.bom)
            implementation platform(libs.protobuf.bom)
        }
    }
}

// Apply shared configurations
allprojects {
    subprojects {
        afterEvaluate {
            // Check if project has tests
            def hasTests = !new FileNameFinder().getFileNames(
                "${project.projectDir}/src/test",
                '**/*.java,**/*.groovy'
            ).isEmpty()

            if (hasTests) {
                logger.info(">> Applying JaCoCo to ${project.name}")
                project.apply(from: "${rootDir.toString()}/gradle/jacoco.gradle")
            }
        }
    }
}
```

**For detailed JaCoCo configuration**, see: [java/rules/java-jacoco-coverage.md](java-jacoco-coverage.md)

## Multi-Module Project Structure

### Recommended Layout

```text
project-root/
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties  (8.14.3+)
│   ├── libs.versions.toml
│   ├── jacoco.gradle
│   ├── protobuf.gradle
│   └── jooq.gradle
├── build.gradle                       (root configuration)
├── settings.gradle                    (module registry)
├── gradlew
├── gradlew.bat
├── bitso-libs/
│   ├── domain/
│   │   └── build.gradle
│   ├── api/
│   │   └── build.gradle
│   ├── persistence/
│   │   └── build.gradle
│   ├── service/
│   │   └── build.gradle
│   └── [other libs]/
└── bitso-services/
    ├── service-a/
    │   └── build.gradle
    ├── service-b/
    │   └── build.gradle
    └── [other services]/
```

### Settings File (`settings.gradle`)

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url "https://maven.pkg.github.com/bitsoex/packages" }
    }
    plugins {
        id 'org.springframework.boot' version '3.5.7'
        id 'io.spring.dependency-management' version '1.1.7'
        id 'com.google.protobuf' version '0.9.5'
        id 'nu.studer.jooq' version '10.1.1'
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = 'project-name'

include 'bitso-libs:domain'
include 'bitso-libs:api'
include 'bitso-libs:persistence'
include 'bitso-services:service-a'
include 'bitso-services:service-b'
```

## Common Module Patterns

### Service Module (`build.gradle`)

```groovy
plugins {
    id 'java'
    id 'groovy'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'jacoco'
}

dependencies {
    implementation project(':bitso-libs:domain')
    implementation project(':bitso-libs:api')
    
    implementation libs.bundles.spring.boot.web
    implementation libs.bundles.grpc.all
    
    testImplementation libs.bundles.testing
}

test {
    useJUnitPlatform()
}

// JaCoCo applied automatically via root build.gradle
```

### Library Module (`build.gradle`)

```groovy
plugins {
    id 'java-library'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'jacoco'
}

dependencies {
    // Transitive dependencies visible to consumers
    api libs.spring.boot.starter.data.jpa
    
    // Internal dependencies
    implementation libs.lombok
    
    testImplementation libs.junit.jupiter
}
```

## Shared Configuration Files

### Protocol Buffers (`gradle/protobuf.gradle`)

**For comprehensive protobuf linting and configuration**, see: [java/rules/java-protobuf-linting.md](java-protobuf-linting.md)

Basic template:

```groovy
apply plugin: 'com.google.protobuf'

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().each { task ->
            task.plugins {
                grpc {}
            }
        }
    }
}
```

### JOOQ (`gradle/jooq.gradle`)

**For comprehensive JOOQ setup with Flyway**, see: [java/rules/java-jooq.md](java-jooq.md)

Basic template:

```groovy
apply plugin: 'nu.studer.jooq'

jooq {
    version = libs.versions.jooq.get()
    edition = nu.studer.gradle.jooq.JooqEdition.OSS
}

jooqCodegen {
    configurations {
        main {
            generationTool {
                jdbc {
                    driver = 'org.postgresql.Driver'
                    url = project.properties.get('db.url', 'jdbc:postgresql://localhost:5432/db')
                    user = project.properties.get('db.user', 'postgres')
                    password = project.properties.get('db.password', 'password')
                }
                generator {
                    name = 'org.jooq.codegen.JavaGenerator'
                    database {
                        name = 'org.jooq.meta.postgres.PostgresDatabase'
                    }
                    target {
                        packageName = 'com.bitso.generated.jooq'
                        directory = 'src/main/java'
                    }
                }
            }
        }
    }
}
```

## Common Gradle Commands

For comprehensive Gradle commands, debugging, and troubleshooting, see: [java-gradle-commands](./java-gradle-commands.md)

Key commands summary:

- Build: `./gradlew build` - Assembles and tests all modules
- Test: `./gradlew test` - Runs test suite  
- Coverage: `./gradlew check` - Verifies coverage thresholds
- Proto Gen: `./gradlew generateProto` - Generates protobuf code
- JOOQ Gen: `./gradlew jooqCodegen` - Generates JOOQ code

**Best practice**: Always filter for failures only (`2>&1 | grep -E "FAILED"`) to preserve context.

## Build Configuration Best Practices

### 1. Use Gradle Wrapper

- ✅ Always commit `gradlew` and `gradle-wrapper.properties`
- ✅ Ensures consistent Gradle version across team
- ✅ No need to install Gradle locally
- ❌ Don't rely on system Gradle installation
- ✅ Define most plugins in root `build.gradle`
- ✅ Let subprojects inherit configuration
- ✅ Override only when necessary in specific modules
- ❌ Don't duplicate plugin declarations

### 3. Platform for BOM

- ✅ Use `platform()` for Spring Boot, gRPC, Protobuf BOMs
- ✅ Prevents version conflicts between transitive dependencies
- ✅ Centralized dependency version control
- ❌ Never use `enforcedPlatform()` - causes issues when publishing libraries
- ❌ Don't mix BOM management approaches

### 4. Repository Credentials

- ✅ Use environment variables or `gradle.properties`
- ✅ Never commit credentials to repository
- ✅ Use GitHub Actions for CI/CD credentials
- ❌ Don't hardcode tokens in build files

```groovy
// ✅ Correct
credentials {
    username = System.getenv("GITHUB_ACTOR")
    password = System.getenv("GITHUB_TOKEN")
}

// ❌ Wrong
credentials {
    username = "hardcoded-user"
    password = "hardcoded-token"
}
```

### 5. Build Property Configuration

Use `gradle.properties` for local overrides:

```properties
# gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.workers.max=4

# JVM settings
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC

# Database settings (local development)
db.url=jdbc:postgresql://localhost:5432/dev_db
db.user=postgres
db.password=password
```

### 6. Caching Strategy

Enable Gradle build cache for faster builds:

```groovy
// In gradle.properties or environment
org.gradle.caching=true
org.gradle.caching.debug=false
```

## Troubleshooting

### Gradle Build Fails with "Cannot resolve dependency"

- Check version catalog: `gradle/libs.versions.toml` has the dependency
- Verify BOM is declared: `platform(libs.bom)`
- Check repository credentials: GitHub token is valid

### Module Tests Don't Run

- Verify `src/test/java` directory exists
- Check `test { useJUnitPlatform() }` in module (if using JUnit 5)
- Run: `./gradlew :module:test --info`

### Build Cache Issues

- Clear cache: `./gradlew --stop` then `./gradlew clean`
- Disable cache temporarily: `./gradlew build --no-build-cache`
- Check task outputs are deterministic (same inputs → same outputs)

### Plugin Version Conflicts

- Use `pluginManagement` in `settings.gradle`
- Define plugin versions once in version catalog or settings
- Don't specify versions in plugins {} block of individual modules

## Related Rules

- **Version & Dependency Management**: [java/rules/java-versions-and-dependencies.md](java-versions-and-dependencies.md)
- **Redis/Jedis Compatibility**: [java/golden-paths/redis-jedis-compatibility.md](../golden-paths/redis-jedis-compatibility.md)
- **Spring Boot 3.5 Upgrade**: [java/golden-paths/spring-boot-3.5-upgrade.md](../golden-paths/spring-boot-3.5-upgrade.md)
- **JaCoCo Code Coverage**: [java/rules/java-jacoco-coverage.md](java-jacoco-coverage.md)
- **Java Testing Guidelines**: [java/rules/java-testing-guidelines.md](java-testing-guidelines.md)
- **Protocol Buffer Standards**: [java/rules/java-protobuf-linting.md](java-protobuf-linting.md)
- **JOOQ Best Practices**: [java/rules/java-jooq.md](java-jooq.md)
