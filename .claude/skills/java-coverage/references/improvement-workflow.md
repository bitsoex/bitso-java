---
title: Coverage Improvement Workflow
description: Step-by-step guide for improving test coverage in Java projects
---

# Coverage Improvement Workflow

Step-by-step guide for writing tests to improve code coverage.

## Contents

- [Prerequisites](#prerequisites)
- [Coverage Target](#coverage-target)
- [Workflow](#workflow)
- [Related](#related)

---
## Prerequisites

Ensure test infrastructure is set up by running `/improve-test-setup` first.

## Coverage Target

- **Minimum**: 82% instruction and branch coverage
- **Goal**: Achieve and maintain coverage thresholds

## Workflow

### 1. Generate Coverage Reports

```bash
# Run tests with coverage - DO NOT skip coverage report
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage-run.log

# Verify reports generated
ls -lh build/reports/jacoco/test/
```

> **⚠️ CRITICAL**: NEVER use `-x codeCoverageReport` or skip coverage tasks.

### 2. Analyze Coverage Reports

#### CSV Analysis (Quick Overview)

```bash
# Find classes with lowest coverage
cat build/reports/jacoco/test/jacocoTestReport.csv | \
    awk -F',' 'NR>1 {
        total=$4+$5;
        if(total>0) {
            pct=$5/total*100;
            print pct"%", $2"."$3, "("$5"/"total" instructions)"
        }
    }' | sort -n | head -20
```

#### HTML Analysis (Detailed Line-by-Line)

```bash
open build/reports/jacoco/test/html/index.html
```

The HTML report shows:
- **Red lines**: Not covered - need tests
- **Yellow lines**: Partially covered branches - need additional test cases
- **Green lines**: Fully covered

### 2.5. Quick Coverage Check via SonarQube MCP (Alternative)

Instead of running local JaCoCo builds, use SonarQube MCP for faster coverage analysis:

```text
# Find files with lowest coverage (no local build required)
search_files_by_coverage: projectKey: "my-service"

# Get line-by-line coverage for a specific file
get_file_coverage_details: key: "my-service:src/main/java/com/bitso/payment/PaymentService.java"

# Check project-level coverage
get_component_measures: component: "my-service", metricKeys: ["coverage"]
```

MCP reflects the last CI analysis, so it shows the current state without requiring a local build. Use this to quickly identify which files need tests before investing in local JaCoCo runs.

### 3. Identify Coverage Gaps

Focus on these areas (in priority order):

1. **Business Logic Classes**: Services, handlers, processors
2. **Edge Cases**: Error handling, boundary conditions
3. **Branch Coverage**: If/else, switch statements, ternary operators
4. **Exception Paths**: Try/catch blocks, error scenarios

### 4. Write Tests

See [Spock Patterns](.claude/skills/java-testing/references/spock-patterns.md) for test templates.

#### Basic Spock Test Template

```groovy
class YourServiceSpec extends Specification {
    @Subject
    YourService service

    def mockDependency = Mock(DependencyClass)

    def setup() {
        service = new YourService(mockDependency)
    }

    def "should handle normal case"() {
        given: "valid input"
        def input = new InputDto(value: "test")
        mockDependency.process(_) >> new Result(success: true)

        when: "service is called"
        def result = service.execute(input)

        then: "returns expected output"
        result.success
    }

    def "should cover all branches"() {
        expect: "correct behavior for each case"
        service.categorize(value) == expected

        where: "testing all branches"
        value | expected
        0     | "zero"
        1     | "positive"
        -1    | "negative"
    }
}
```

### 5. Run Tests and Verify Coverage

```bash
# Run all tests with coverage
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage-verify.log

# Verify coverage thresholds
./gradlew jacocoTestCoverageVerification

# Check new coverage percentage
cat build/reports/jacoco/test/jacocoTestReport.csv | \
    awk -F',' 'NR>1 {missed+=$4; covered+=$5} END {
        total=missed+covered;
        if(total>0) print "Coverage: " covered/total*100 "%"
    }'
```

### 6. Iterate Until Coverage Met

Repeat steps 2-5 until:
- All tests pass
- Coverage meets 82% threshold
- No new test failures introduced

## Related

- [Prioritization](prioritization.md) - What to test first
- [Coverage Targets](coverage-targets.md) - Target percentages by area
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-coverage/references/improvement-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

