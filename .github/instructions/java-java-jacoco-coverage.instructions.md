---
applyTo: "**/*"
description: JaCoCo plugin setup, troubleshooting, and best practices for accurate code coverage reporting
---

# JaCoCo Code Coverage Configuration

Best practices for JaCoCo code coverage plugin setup, configuration, and troubleshooting in Java/Gradle projects.

## Overview

JaCoCo measures code coverage during test execution. Accurate coverage requires proper plugin
configuration in all modules, especially service and library modules with Spring integration.

## Prerequisites

### 1. Gradle & JaCoCo Versions

- **Gradle**: 8.14.3+ (see `java/rules/java-gradle-best-practices.md`)
- **JaCoCo**: 0.8.14+ (minimum)
- Define in `gradle/libs.versions.toml`:

  ```groovy
  [versions]
  jacoco = "0.8.14"
  ```

## Module Setup

### Step 1: Add JaCoCo Plugin

Every module measuring coverage must declare the plugin in `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot'  // if Spring Boot module
    id 'io.spring.dependency-management'
    id 'jacoco'  // Add this line
}
```

**Where to add:**

- All service modules: `bitso-services/*/build.gradle`
- All library modules: `bitso-libs/*/build.gradle`
- Library modules especially need it: `domain`, `api`, `persistence`, `service`, `client`, `bff`

### Step 2: Create Shared JaCoCo Configuration

Create `gradle/jacoco.gradle` in project root (applied globally to all modules with tests):

```groovy
logger.info(">> Apply and configure JaCoCo plugin for ${project.name}")
apply plugin: 'jacoco'

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
        xml.required = true
        html.required = true
        csv.required = true
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

### Step 3: Apply Globally in Main Build File

In root `build.gradle`, apply `jacoco.gradle` to all subprojects that have tests:

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

### Step 4: Configure SonarQube Integration

In root `build.gradle`, add SonarQube properties to use JaCoCo reports:

```groovy
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

## Common Issues & Solutions

### Plugin Not Applied Automatically

**Symptom:** Some modules not get JaCoCo plugin despite having tests

**Cause:** The `afterEvaluate` block in `build.gradle` may not detect tests correctly

**Fix:**

1. Verify `gradle/jacoco.gradle` is applied in root `build.gradle`
2. Check that root applies to `allprojects > subprojects`
3. Verify `src/test` directory exists with test files
4. Run with verbose logging: `./gradlew build -i | grep "Applying JaCoCo"`

### Low or Missing Coverage

**Symptom:** Module shows 0% or incorrect coverage despite having tests

**Cause:**

- JaCoCo plugin misconfigured or not enabled
- Test execution file (`test.exec`) not generated
- Coverage verification rule too strict

**Fix:**

1. Verify plugin applied: `./gradlew tasks | grep jacoco`
2. Run tests: `./gradlew clean test jacocoTestReport`
3. Check exec file: `ls build/jacoco/test.exec` (must exist)
4. Inspect HTML report: `build/reports/jacoco/test/html/index.html`
5. Lower verification threshold temporarily to test: Change `minimum = 0.82` to `minimum = 0.0`

### Coverage Not Aggregating Across Subprojects

**Symptom:** `codeCoverageReport` task doesn't include all subproject coverage data

**Cause:**

- Subprojects don't have `jacocoTestReport` tasks
- Plugin not applied to all modules with tests
- Aggregate task not configured correctly

**Fix:**

1. Verify all modules have plugin: `./gradlew :subproject:tasks | grep jacoco` for each
2. Generate all reports: `./gradlew test jacocoTestReport`
3. Then aggregate: `./gradlew codeCoverageReport`
4. Check: `build/reports/jacoco/codeCoverageReport/html/index.html`

### JaCoCo Report Generation Fails

**Symptom:** `jacocoTestReport` task fails with file path errors

**Cause:**

- Incorrect file paths for `destinationFile` or `classDumpDir`
- Build directory doesn't exist yet
- Permission issues

**Fix:**

1. Verify paths in `gradle/jacoco.gradle`:

   ```groovy
   destinationFile = layout.buildDirectory.file("jacoco/test.exec").get().asFile
   classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
   ```

2. Run verbose: `./gradlew jacocoTestReport --info`
3. Check build directory created: `ls -la build/jacoco/`
4. Ensure write permissions: `chmod 755 build/`

### Coverage Verification Rule Violations

**Symptom:** Build fails with "Jacoco coverage is below 82%"

**Cause:** Code coverage doesn't meet minimum threshold (82% instruction + branch)

**Fix:**

1. Check current coverage: `build/reports/jacoco/test/html/index.html` → Coverage % column
2. Write tests to increase coverage for uncovered code paths
3. Verify excluded patterns are correct (generated code shouldn't count)
4. Update exclusion list in `gradle/jacoco.gradle` if needed
5. Temporarily lower threshold for debugging:

   ```groovy
   violationRules {
       rule {
           element = 'BUNDLE'
           limit {
               counter = 'INSTRUCTION'
               minimum = 0.70  // Temporary: lower to 70%
           }
       }
   }
   ```

### SonarQube Not Finding JaCoCo Reports

**Symptom:** SonarQube scan shows "0% coverage" despite JaCoCo reports generated

**Cause:**

- `sonar.coverage.jacoco.xmlReportPaths` points to wrong location
- SonarQube MCP server not configured for JaCoCo
- XML report not generated

**Fix:**

1. Verify XML report exists: `ls build/reports/jacoco/test/jacocoTestReport.xml`
2. Check SonarQube property in root `build.gradle`:

   ```groovy
   property 'sonar.coverage.jacoco.xmlReportPaths', 
       "$projectDir.path/build/reports/jacoco/test/jacocoTestReport.xml"
   ```

3. Run SonarQube scan with debug: `./gradlew sonarqube --info`
4. Verify MCP server is using this property (see `java/rules/java-sonarqube-setup.md`)

### Excluded Classes Still Appearing in Report

**Symptom:** Generated/config code showing in coverage when it should be excluded

**Cause:** Exclusion patterns incorrect or too specific

**Fix:**

1. Update exclusion list in `gradle/jacoco.gradle`:

   ```groovy
   def excludeDirectoriesFromJacoco = { classDirectories ->
       classDirectories.setFrom(files(classDirectories.files.collect {
           fileTree(dir: it, exclude: [
               '**/generated/**',
               '**/gensrc/**',
               '**/*Config.class',
               '**/*Application.class'
           ])
       }))
   }
   ```

2. Use broader patterns if needed: `**/config/**` instead of `**/Config.class`
3. Regenerate reports: `./gradlew clean test jacocoTestReport`

## Best Practices

### 1. JaCoCo Version Management

- **Use version catalog** for consistency (see `java/rules/java-gradle-best-practices.md`)
- **Minimum**: 0.8.14+
- **Define once**: In `gradle/libs.versions.toml`, not in individual build files

### 2. Exclude Generated Code

Always exclude from coverage:

- Generated sources: `**/generated/**`, `**/gensrc/**`
- Proto files: `**/proto/**`
- Config: `**/config/**`
- Application classes: `**/*Application.class`
- Mappers: `**/*MapperImpl.class`, `**/mapper/**`
- DTOs: `**/dto/**`

### 3. Multi-Report Formats

Generate all formats for different uses:

```groovy
reports {
    xml.required = true   // SonarQube, CI/CD tools
    html.required = true  // Local inspection, team review
    csv.required = true   // Command-line analysis, grep, scripting
}
```

**CSV Format Structure:**

The CSV report contains one row per class with columns:

```text
GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
```

Example row:

```csv
api,com.bitso.security.makerchecker.api.handlers,MakerCheckerGrpcServiceV1,0,826,0,50,0,172,0,62,0,37
```

**Useful CSV Queries:**

```bash
# Find classes with low line coverage (< 80%)
awk -F, 'NR>1 && ($8+$9)>0 {cov=$9/($8+$9)*100; if(cov<80) print $2"."$3": "cov"%"}' jacocoTestReport.csv

# List classes with 0% coverage
awk -F, 'NR>1 && $9==0 && ($8+$9)>0 {print $2"."$3}' jacocoTestReport.csv

# Find classes with uncovered branches
awk -F, 'NR>1 && $6>0 {print $2"."$3": "$6" uncovered branches"}' jacocoTestReport.csv

# Calculate overall line coverage %
awk -F, 'NR>1 {missed+=$8; covered+=$9} END {print covered/(missed+covered)*100"%"}' jacocoTestReport.csv
```

### 4. Coverage Verification (Optional)

Set minimum thresholds to enforce code quality:

```groovy
violationRules {
    rule {
        element = 'BUNDLE'
        limit {
            counter = 'INSTRUCTION'
            minimum = 0.82  // 82% minimum
        }
        limit {
            counter = 'BRANCH'
            minimum = 0.82
        }
    }
}
```

- Prevents low-quality PRs from merging
- Encourages writing tests alongside code

### 5. Aggregation for Multi-Module Projects

Register `codeCoverageReport` task in root:

- Runs after all `jacocoTestReport` tasks complete
- Generates single comprehensive report
- Command: `./gradlew codeCoverageReport`
- Report: `build/reports/jacoco/codeCoverageReport/html/index.html`

### 6. SonarQube Integration

Link JaCoCo reports to SonarQube in root `build.gradle`:

```groovy
property 'sonar.coverage.jacoco.xmlReportPaths', 
    "$projectDir.path/build/reports/jacoco/test/jacocoTestReport.xml"
```

- Ensures SonarQube reads correct XML location
- Enables quality gates based on coverage trends

## Verification Commands

Essential commands to verify JaCoCo setup:

```bash
# 1. Generate coverage for single module
./gradlew :module-name:jacocoTestReport

# 2. Generate all module coverage reports
./gradlew jacocoTestReport

# 3. Generate aggregated report (multi-module)
./gradlew codeCoverageReport

# 4. Verify coverage meets requirements
./gradlew check

# 5. List available JaCoCo tasks
./gradlew tasks --all | grep jacoco

# 6. Debug JaCoCo configuration
./gradlew :module-name:tasks -a --info | grep -i jacoco
```

## Related Resources

- **Improve Test Setup**: java/commands/improve-test-setup.md - Testing infrastructure and library upgrades
- **Improve Test Coverage**: java/commands/improve-test-coverage.md - Write tests to improve coverage
- **Mutation Testing**: java/commands/improve-test-quality-with-mutation-testing.md - Find weak tests
- **Gradle Configuration**: java/rules/java-gradle-best-practices.md
- **Java Testing Guidelines**: java/rules/java-testing-guidelines.md
- **Java Services Standards**: java/rules/java-services-standards.md
- **Fix SonarQube Issues**: java/commands/fix-sonarqube-issues.md
- **SonarQube Setup**: java/rules/java-sonarqube-setup.md
