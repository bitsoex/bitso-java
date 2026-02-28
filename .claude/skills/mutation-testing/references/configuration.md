# PIT Configuration

## Contents

- [Setup](#setup)
- [Command Line Options](#command-line-options)
- [Common Exclusion Patterns](#common-exclusion-patterns)
- [CI Integration](#ci-integration)
- [History Files (Incremental Analysis)](#history-files-incremental-analysis)
- [Default Template Settings](#default-template-settings)

---
## Setup

### 1. Copy pitest.gradle template

```bash
cp java/templates/pitest.gradle gradle/pitest.gradle
```

### 2. Apply in build.gradle

```groovy
// Apply to all modules with tests
subprojects { Project subproject ->
    subproject.afterEvaluate {
        if (new File("${subproject.projectDir}/src/test").exists()) {
            project.apply(from: "${rootDir}/gradle/pitest.gradle")
        }
    }
}

// Or specific modules only
apply from: "${rootDir}/gradle/pitest.gradle"
```

### 3. Add version catalog entries

```toml
# gradle/libs.versions.toml
[versions]
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
```

## Command Line Options

```bash
# Target specific classes
./gradlew pitest -Ppitest.targetClasses='com.bitso.myservice.*'

# Target specific tests
./gradlew pitest -Ppitest.targetTests='*MyServiceSpec,*MyServiceTest'

# Choose mutator group
./gradlew pitest -Ppitest.mutators=STRONGER

# Increase threads
./gradlew pitest -Ppitest.threads=8

# Exclude specific classes
./gradlew pitest -Ppitest.excludedClasses='**/mapper/**,**/config/**'

# Increase memory
./gradlew pitest -Ppitest.jvmArgs='-Xmx6g'

# Verbose logging
./gradlew pitest -Ppitest.verbose=true
```

## Common Exclusion Patterns

```bash
# Generated code
-Ppitest.excludedClasses='**/generated/**,**/gensrc/**'

# Protobuf
-Ppitest.excludedClasses='**/proto/**,**/*Proto,com.google.**'

# MapStruct
-Ppitest.excludedClasses='**/mapper/**,**/*MapperImpl'

# Configuration
-Ppitest.excludedClasses='**/*Config,**/*Configuration,**/config/**'

# Spring application
-Ppitest.excludedClasses='**/*Application'

# DTOs
-Ppitest.excludedClasses='**/dto/**'

# JOOQ tables
-Ppitest.excludedClasses='**/tables/**'

# Combined common set
-Ppitest.excludedClasses='**/generated/**,**/proto/**,**/*MapperImpl,**/*Config,**/*Application,**/tables/**'
```

## CI Integration

Set mutation threshold for CI gates:

```groovy
pitest {
    mutationThreshold = 80  // Fail if < 80% mutations killed
}
```

## History Files (Incremental Analysis)

For faster re-runs:

```groovy
pitest {
    historyInputLocation = file("${buildDir}/pitest-history.bin")
    historyOutputLocation = file("${buildDir}/pitest-history.bin")
}
```

## Default Template Settings

The `pitest.gradle` template includes:

- **No default exclusions** - You control what to exclude
- **High timeout** - 2 minutes base + 1.5x multiplier for Testcontainers/Spring
- **4GB memory** - Sufficient for most projects
- **JUnit 5 plugin** - Automatic detection of JUnit 5 tests
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/mutation-testing/references/configuration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

