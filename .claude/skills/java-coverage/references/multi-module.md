# Multi-Module JaCoCo Aggregation

## Aggregated Report Task

Add to root `build.gradle`:

```groovy
task jacocoRootReport(type: JacocoReport) {
    description = 'Aggregate JaCoCo coverage reports from all subprojects'

    dependsOn subprojects*.test

    additionalSourceDirs.from(subprojects*.sourceSets*.main*.allSource*.srcDirs)
    sourceDirectories.from(subprojects*.sourceSets*.main*.allSource*.srcDirs)
    classDirectories.from(subprojects*.sourceSets*.main*.output)
    executionData.from(subprojects*.jacocoTestReport*.executionData)

    reports {
        xml.required = true
        html.required = true
        xml.outputLocation = file("${buildDir}/reports/jacoco/aggregate/jacocoTestReport.xml")
        html.outputLocation = file("${buildDir}/reports/jacoco/aggregate/html")
    }
}
```

## Running

```bash
./gradlew jacocoRootReport
# Report at: build/reports/jacoco/aggregate/html/index.html
```

## SonarQube Integration

```groovy
sonar {
    properties {
        property 'sonar.coverage.jacoco.xmlReportPaths',
            "${buildDir}/reports/jacoco/aggregate/jacocoTestReport.xml"
    }
}
```

## Filtering Subprojects

Only include projects with tests:

```groovy
def projectsWithTests = subprojects.findAll { project ->
    new File("${project.projectDir}/src/test").exists()
}

task jacocoRootReport(type: JacocoReport) {
    dependsOn projectsWithTests*.test
    // ... rest of configuration
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-coverage/references/multi-module.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

