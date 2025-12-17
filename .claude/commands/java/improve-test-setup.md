# Upgrade testing libraries and configure JaCoCo for Java projects - NO production code changes

**Description:** Upgrade testing libraries and configure JaCoCo for Java projects - NO production code changes

# 🤖 🧪 Improve Test Setup

**IMPORTANT**: This command focuses on **testing infrastructure only**. Only test dependencies and test configuration should be modified.

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first**
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - JaCoCo configuration reference
- **JUnit Version Alignment**: `java/golden-paths/junit-version-alignment.md` - Resolving JUnit conflicts
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns

## Prerequisites

1. **Gradle 8.14+** (Java 21) or **Gradle 9.2.1+** (Java 25) - Check with `./gradlew --version`
2. **Java 21 or Java 25** - Project must use Java 21 or 25
3. **Access to `gradle/libs.versions.toml`** - Version catalog must exist

**For Java 25 projects**, see also: `java/commands/prepare-to-java-25.md` and `java/golden-paths/java-25-upgrade.md`

## Target Testing Library Versions (December 2025)

### Java 21 Projects (Gradle 8.x)

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | `2.4-groovy-4.0` | Stable release (Dec 11, 2025) |
| **JUnit Jupiter** | `5.14.1` | Released Oct 2025 |
| **JUnit Platform Launcher** | `1.14.1` | Released Oct 2025 |
| **JaCoCo** | `0.8.14` | Released Oct 2025 |
| **SonarQube Gradle Plugin** | `7.2.0.6526` | Released Dec 2025 |
| **Pitest** | `1.22.0` | Core library |
| **Pitest Gradle Plugin** | `1.19.0-rc.2` | Released Oct 2025 |
| **Pitest JUnit5 Plugin** | `1.2.3` | For JUnit 5 support |
| **Testcontainers** | `1.21.4` | Stable 1.x; use `2.0.3` for 2.x repos |
| **Groovy** | `4.0.29` | Required for Spock 2.4-groovy-4.0 |

### Java 25 Projects (Gradle 9.x)

| Library | Version | Notes |
|---------|---------|-------|
| **Spock Framework** | `2.4-groovy-5.0` | Must match Groovy 5.x |
| **JUnit Jupiter** | `5.14.1` | Released Oct 2025 |
| **JUnit Platform Launcher** | `1.14.1` | **Required** for Gradle 9 |
| **JaCoCo** | `0.8.14` | Released Oct 2025 |
| **SonarQube Gradle Plugin** | `7.2.1.6560` | Gradle 9 compatible |
| **Spotless** | `8.1.0` | Gradle 9 compatible |
| **Lombok Plugin** | `9.1.0` | Freefair plugin for Gradle 9 |
| **Testcontainers** | `1.21.4` | Stable 1.x; use `2.0.3` for 2.x repos |
| **Groovy** | `5.0.3` | Required for Java 25 bytecode |
| **Lombok** | `1.18.42` | Required for Java 25 bytecode |
| **Flyway Plugin** | `11.19.0` | Gradle 9 compatible |
| **jOOQ Plugin** | `10.1.1` | Gradle 9 compatible |
| **Protobuf Plugin** | `0.9.5` | Gradle 9 compatible |

**⚠️ CRITICAL for Java 25**: Never use `groovy-all` - rely on spock-core transitives. See `java/golden-paths/java-25-upgrade.md`.

## Workflow

### 1. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, create a Jira ticket for tracking:

Use `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 🧪 Improve test setup for [repo-name]`
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Description**: Include target library versions and scope

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 2. Create Branch with Jira Key

```bash
# JIRA_KEY is the actual ticket key from Step 1 (e.g., PROJ-123)
git checkout -b test/${JIRA_KEY}-improve-test-setup
```

### 3. Check Current Testing Library Versions

```bash
# Check version catalog for current versions
grep -E "(spock|junit|jacoco|testcontainers|pitest|sonar)" gradle/libs.versions.toml

# Check JaCoCo version in use
./gradlew dependencies --configuration testRuntimeClasspath | grep -i jacoco
```

### 4. Update Testing Library Versions in Version Catalog

Update `gradle/libs.versions.toml` with the latest testing library versions:

#### Java 21 Projects (Gradle 8.x)

```toml
[versions]
# Testing Libraries
spock = "2.4-groovy-4.0"
junit-jupiter = "5.14.1"
junit-platform = "1.14.1"
jacoco = "0.8.14"
testcontainers = "1.21.4"
groovy = "4.0.29"

# Mutation Testing
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

# Code Quality (test infrastructure)
sonar-plugin = "7.2.0.6526"

[libraries]
# JUnit BOM
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }

# Spock
spock-core = { module = "org.spockframework:spock-core", version.ref = "spock" }
spock-spring = { module = "org.spockframework:spock-spring", version.ref = "spock" }

# Testcontainers BOM
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
sonarqube = { id = "org.sonarqube", version.ref = "sonar-plugin" }
```

#### Java 25 Projects (Gradle 9.x)

```toml
[versions]
# Testing Libraries - Java 25 compatible
spock = "2.4-groovy-5.0"
junit-jupiter = "5.14.1"
junit-platform = "1.14.1"
jacoco = "0.8.14"
testcontainers = "1.21.4"  # or "2.0.3" for 2.x repos
groovy = "5.0.3"
lombok = "1.18.42"

# Mutation Testing
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

# Code Quality - Gradle 9 compatible
sonar-plugin = "7.2.1.6560"
spotless-palantir = "2.74.0"

# Build Plugins - Gradle 9 compatible
flyway-plugin = "11.19.0"
jooq-plugin = "10.1.1"
protobuf-plugin = "0.9.5"

[libraries]
# JUnit BOM
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit-jupiter" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }

# Spock - NEVER use groovy-all, rely on spock-core transitives
spock-core = { module = "org.spockframework:spock-core", version.ref = "spock" }
spock-spring = { module = "org.spockframework:spock-spring", version.ref = "spock" }

# Testcontainers BOM
testcontainers-bom = { module = "org.testcontainers:testcontainers-bom", version.ref = "testcontainers" }

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
sonarqube = { id = "org.sonarqube", version.ref = "sonar-plugin" }
lombok = { id = "io.freefair.lombok", version = "9.1.0" }
spotless = { id = "com.diffplug.spotless", version = "8.1.0" }
```

**⚠️ CRITICAL for Java 25**: Never use `groovy-all` dependency. The `spock-core` artifact correctly brings in Groovy 5.0.3 transitively.

**IMPORTANT**: Only update **test** dependencies. Do not modify production dependencies with this command.

### 5. Configure JUnit Version Alignment

To avoid JUnit version conflicts (especially with `bitso.java.module` plugin), add resolution strategy to root `build.gradle`.

**IMPORTANT**: Always reference versions from the version catalog. Never hardcode version strings.

```groovy
subprojects {
    plugins.withType(JavaPlugin).configureEach {
        configurations.configureEach {
            resolutionStrategy.eachDependency { details ->
                if (details.requested.group == 'org.junit.jupiter') {
                    details.useVersion libs.versions.junit.jupiter.get()
                }
                if (details.requested.group == 'org.junit.platform') {
                    details.useVersion libs.versions.junit.platform.get()
                }
            }
        }
        
        dependencies {
            testRuntimeOnly libs.junit.platform.launcher
        }
    }
}
```

Or in each module's `build.gradle`:

```groovy
// Force JUnit version alignment across all configurations using version catalog
configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == 'org.junit.jupiter') {
            details.useVersion libs.versions.junit.jupiter.get()
        }
        if (details.requested.group == 'org.junit.platform') {
            details.useVersion libs.versions.junit.platform.get()
        }
    }
}

dependencies {
    testImplementation platform(libs.junit.bom)
    testImplementation libs.junit.jupiter
    testImplementation libs.spock.core
}

test {
    useJUnitPlatform()
}
```

See `java/golden-paths/junit-version-alignment.md` for detailed guidance.

### 6. Configure JaCoCo Plugin

Create or update `gradle/jacoco.gradle`:

```groovy
logger.info(">> Apply and configure JaCoCo plugin for ${project.name}")
apply plugin: 'jacoco'

jacoco {
    // Reference version from version catalog - never hardcode
    toolVersion = libs.versions.jacoco.get()
}

test {
    finalizedBy jacocoTestReport
    jacoco {
        enabled = true
        destinationFile = layout.buildDirectory.file("jacoco/test.exec").get().asFile
        classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
    }
}

def excludeDirectoriesFromJacoco = { classDirectories ->
    classDirectories.setFrom(files(classDirectories.files.collect {
        fileTree(dir: it, exclude: [
                'com/google/**',
                '**/generated/**',
                '**/gensrc/**',
                '**/dto/**',
                '**/proto/**',
                '**/mapper/**',
                '**/*MapperImpl.class',
                '**/config/**',
                '**/*Application.class',
                '**/context/**'
        ])
    }))
}

jacocoTestReport {
    dependsOn test
    afterEvaluate {
        excludeDirectoriesFromJacoco(classDirectories)
    }
    reports {
        xml.required.set(true)   // For CI/CD tools and SonarQube
        html.required.set(true)  // For line-level analysis
        csv.required.set(true)   // For command-line grep/awk analysis
    }
}

// Coverage verification with minimum thresholds (82%)
jacocoTestCoverageVerification {
    dependsOn jacocoTestReport
    afterEvaluate {
        excludeDirectoriesFromJacoco(classDirectories)
    }
    violationRules {
        rule {
            element = 'BUNDLE'
            limit {
                counter = 'INSTRUCTION'
                minimum = 0.82
            }
            limit {
                counter = 'BRANCH'
                minimum = 0.82
            }
        }
    }
}

check.dependsOn jacocoTestCoverageVerification

// Aggregate coverage report from all subprojects
tasks.register("codeCoverageReport", JacocoReport) {
    subprojects { subproject ->
        subproject.plugins.withType(JacocoPlugin).configureEach {
            subproject.tasks.matching({ t -> t.extensions.findByType(JacocoTaskExtension) })
                .configureEach { testTask ->
                    if (testTask.extensions.getByType(JacocoTaskExtension).isEnabled()) {
                        sourceSets subproject.sourceSets.main
                        executionData(testTask)
                    }
                }
            subproject.tasks.matching({ t -> t.extensions.findByType(JacocoTaskExtension) })
                .forEach {
                    rootProject.tasks.codeCoverageReport.dependsOn(it)
                }
        }
    }
}
```

### 7. Apply JaCoCo to All Modules

In root `build.gradle`:

```groovy
allprojects {
    subprojects {
        afterEvaluate {
            // Check if project has tests
            def hasTests = !new FileNameFinder().getFileNames(
                "${project.projectDir}/src/test", 
                '**/*.java,**/*.groovy'
            ).isEmpty()
            
            if (hasTests) {
                logger.info(">> Applying JaCoCo plugin to ${project.name}")
                project.apply(from: "${rootDir.toString()}/gradle/jacoco.gradle")
            } else {
                logger.warn(">> Project '${project.path}' has NO tests! Skipping JaCoCo")
            }
        }
    }
}
```

### 8. Configure SonarQube Integration

In root `build.gradle`:

```groovy
plugins {
    // Use version catalog alias - never hardcode plugin versions
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property 'sonar.projectName', "Your Project Name"
        property 'sonar.projectKey', 'your-project-key'
        property 'sonar.coverage.jacoco.xmlReportPaths', 
            "$projectDir.path/build/reports/jacoco/test/jacocoTestReport.xml"
        property 'sonar.log.level', 'DEBUG'
    }
}
```

### 9. Verify Setup

Run verification commands:

```bash
# Verify JaCoCo tasks exist
./gradlew tasks --all | grep -i jacoco

# Verify JUnit version alignment
./gradlew :module-name:dependencies --configuration testRuntimeClasspath | grep -i junit

# Expected: All JUnit components at 5.14.1 / 1.14.1
# +--- org.junit:junit-bom:5.14.1
# |    +--- org.junit.jupiter:junit-jupiter:5.14.1 (c)
# |    +--- org.junit.platform:junit-platform-launcher:1.14.1 (c)

# Run tests with coverage
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/test-setup.log

# Verify reports generated
ls -lh build/reports/jacoco/test/
# Expected: jacocoTestReport.csv, jacocoTestReport.xml, html/

# Verify all three report formats
test -f build/reports/jacoco/test/jacocoTestReport.csv && echo "✓ CSV report exists"
test -f build/reports/jacoco/test/jacocoTestReport.xml && echo "✓ XML report exists"
test -d build/reports/jacoco/test/html && echo "✓ HTML report exists"
```

### 10. Commit with Emojis and Jira Key

```bash
# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

git add -A
git commit -m "🤖 🧪 chore: [$JIRA_KEY] improve test setup

- Upgraded Spock to 2.4-groovy-4.0
- Upgraded JUnit to 5.14.1 with platform launcher 1.14.1
- Upgraded JaCoCo to 0.8.14
- Configured JaCoCo with XML, HTML, and CSV reports
- Added JUnit version alignment resolution strategy
- Updated SonarQube plugin to 7.2.0.6526

Generated with the Quality Agent by the /improve-test-setup command."
```

### 11. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

gh pr create --draft \
    --title "🤖 🧪 [$JIRA_KEY] chore: improve test setup" \
    --body "## 🤖 AI-Assisted Test Setup Improvements

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Changes

### Testing Library Upgrades
| Library | Before | After |
|---------|--------|-------|
| Spock | X.X | 2.4-groovy-4.0 |
| JUnit Jupiter | X.X | 5.14.1 |
| JUnit Platform | X.X | 1.14.1 |
| JaCoCo | X.X | 0.8.14 |

### Configuration Changes
- [x] JaCoCo configured with XML, HTML, CSV reports
- [x] JUnit version alignment via resolution strategy
- [x] SonarQube integration updated

## Validation
- [x] Tests pass locally
- [x] JaCoCo reports generate correctly
- [ ] CI passes

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /improve-test-setup

Generated with the Quality Agent by the /improve-test-setup command.

## Scope Notice
⚠️ This PR only modifies **test dependencies and test configuration**. No production code or production libraries were changed."
```

## Troubleshooting

### JUnit Version Conflict

**Symptom:** `NoClassDefFoundError: org/junit/jupiter/api/extension/TestInstantiationAwareExtension`

**Cause:** Mixed JUnit versions (e.g., 5.10.1 from `bitso.java.module` plugin with 5.14.1)

**Fix:** Add resolution strategy as shown in Step 5. See `java/golden-paths/junit-version-alignment.md`.

### JaCoCo Reports Not Generated

**Symptom:** Missing CSV/XML/HTML reports after test run

**Cause:** Reports not enabled in configuration

**Fix:** Ensure all three formats are enabled:

```groovy
reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(true)
}
```

### Spock 2.4 Compatibility Issues

**Symptom:** Compilation errors after Spock upgrade

**Cause:** Spock 2.4 requires matching Groovy version

**Fix:** Ensure Groovy version is aligned:

```toml
# For Java 21 (Gradle 8.x)
[versions]
groovy = "4.0.29"
spock = "2.4-groovy-4.0"

# For Java 25 (Gradle 9.x)
[versions]
groovy = "5.0.3"
spock = "2.4-groovy-5.0"
```

**⚠️ CRITICAL for Java 25**: Never use `groovy-all` dependency. See `java/golden-paths/java-25-upgrade.md`.

### Testcontainers 2.x Breaking Changes

**Symptom:** `ClassNotFoundException` or package not found errors

**Cause:** Testcontainers 2.x renamed all modules with `testcontainers-` prefix

**Fix options:**

For 1.x (recommended for existing projects):

```toml
testcontainers = "1.21.4"
```

For 2.x (new projects or planned migrations):

```toml
testcontainers = "2.0.3"
```

**2.x Migration Notes:**

- Module names changed: `postgresql` → `testcontainers-postgresql`
- Package names changed: `org.testcontainers` → `org.testcontainers.containers`
- Some deprecated methods removed
- See [Testcontainers 2.0 Migration Guide](https://java.testcontainers.org/migration/2.0/) for details

## Best Practices

1. **Test-only changes**: Only modify test dependencies with this command
2. **Version catalog is mandatory**: **NEVER** hardcode version strings in build files. Always use `gradle/libs.versions.toml` for version management and reference via `libs.versions.X.get()` or `libs.X` in Gradle
3. **Resolution strategy**: Use JUnit version alignment with version catalog references to prevent conflicts
4. **All report formats**: Enable XML (CI), HTML (visual), CSV (scripting)
5. **classDumpDir**: Always configure to prevent coverage issues

### Version Catalog Usage Examples

```groovy
// ❌ NEVER DO THIS - hardcoded versions
details.useVersion '5.14.1'
testImplementation platform('org.junit:junit-bom:5.14.1')
jacoco { toolVersion = "0.8.14" }

// ✅ ALWAYS DO THIS - version catalog references
details.useVersion libs.versions.junit.jupiter.get()
testImplementation platform(libs.junit.bom)
jacoco { toolVersion = libs.versions.jacoco.get() }
```

## Next Steps

Would you like to run one of the related testing improvement commands?

- `/improve-test-coverage` - Write tests to improve coverage
- `/improve-test-quality-with-mutation-testing` - Use PIT mutation testing to find weak tests

## Related

- **Improve Test Coverage**: `java/commands/improve-test-coverage.md` - Write tests after setup
- **Mutation Testing**: `java/commands/improve-test-quality-with-mutation-testing.md` - Find weak tests
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - Complete JaCoCo reference
- **JUnit Alignment**: `java/golden-paths/junit-version-alignment.md` - Version conflict resolution
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns
