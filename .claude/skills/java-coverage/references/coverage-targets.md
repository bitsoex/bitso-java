---
title: Coverage Targets
description: Coverage target percentages and verification configuration
---

# Coverage Targets

Coverage target percentages and verification configuration.

## Contents

- [Standard Targets](#standard-targets)
- [Verification Configuration](#verification-configuration)
- [Per-Area Targets](#per-area-targets)
- [Exclusion Patterns](#exclusion-patterns)
- [Coverage Reports](#coverage-reports)
- [Troubleshooting](#troubleshooting)
- [Related](#related)

---
## Standard Targets

| Metric | Target | Notes |
|--------|--------|-------|
| **Instruction Coverage** | 82% | Minimum for all projects |
| **Branch Coverage** | 82% | Minimum for all projects |
| **Line Coverage** | 80% | Secondary metric |
| **Class Coverage** | 90% | At least one test per class |

## Verification Configuration

### Standard Verification

```groovy
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
```

### Class-Level Minimums

For stricter enforcement, add class-level rules:

```groovy
violationRules {
    rule {
        element = 'BUNDLE'
        limit {
            counter = 'INSTRUCTION'
            minimum = 0.82
        }
    }

    rule {
        element = 'CLASS'
        excludes = ['*.generated.*', '*.config.*', '*.dto.*']
        limit {
            counter = 'LINE'
            minimum = 0.70  // Lower threshold for individual classes
        }
    }
}
```

## Per-Area Targets

Different parts of the codebase may have different targets:

| Area | Target | Rationale |
|------|--------|-----------|
| **Core Domain** | 90%+ | Critical business logic |
| **Services** | 85%+ | Main application logic |
| **API Handlers** | 80%+ | Request handling |
| **Utilities** | 75%+ | Helper functions |
| **Configuration** | N/A | Excluded |
| **Generated Code** | N/A | Excluded |

## Exclusion Patterns

Standard exclusions in JaCoCo configuration:

```groovy
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
```

## Coverage Reports

### Report Formats

Always generate all three formats:

```groovy
jacocoTestReport {
    reports {
        xml.required.set(true)   // For CI/CD tools and SonarQube
        html.required.set(true)  // For line-level analysis
        csv.required.set(true)   // For command-line grep/awk analysis
    }
}
```

### Report Locations

| Format | Location | Use Case |
|--------|----------|----------|
| HTML | `build/reports/jacoco/test/html/` | Visual inspection |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` | SonarQube |
| CSV | `build/reports/jacoco/test/jacocoTestReport.csv` | Scripting |

## Troubleshooting

### Coverage Not Increasing

**Symptom**: Tests pass but coverage doesn't improve.

**Causes**:
- Tests hitting already-covered code
- Testing mocked objects instead of real implementation
- Testing excluded classes

**Solution**: Use HTML report to identify specific uncovered lines.

### Verification Fails

**Symptom**: `jacocoTestCoverageVerification` task fails.

**Solution**: Continue adding tests. Focus on:
1. Classes with lowest coverage first
2. Branch coverage (yellow lines)
3. Exception handling paths

### False Low Coverage

**Symptom**: Coverage seems lower than expected.

**Causes**:
- Generated code included in calculation
- Test classes included in calculation
- Build artifacts from different runs mixed

**Solution**: Ensure proper exclusions and run clean build:
```bash
./gradlew clean test jacocoTestReport
```

## Related

- [Improvement Workflow](improvement-workflow.md) - Step-by-step guide
- [Prioritization](prioritization.md) - What to test first
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-coverage/references/coverage-targets.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

