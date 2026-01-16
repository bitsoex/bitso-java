---
applyTo: "gradle/**,**/build.gradle,**/settings.gradle,**/gradle.properties,**/gradlew,**/*.gradle,flyway/**,**/src/**"
description: Java Gradle Commands &amp; Debugging
---

# Java Gradle Commands & Debugging

## General Principles

### Core Philosophy: Minimize Context Waste

Always filter Gradle output to show **failures only**, not success messages. This approach
preserves token budget and keeps output focused on actionable information. Success messages
provide no value and waste context.

**Implementation**: Use `2>&1 | grep "FAILED"` or similar patterns to capture only
errors, failures, and warnings.

### Output to Temp Files for Debugging

**CRITICAL**: Always redirect build and test output to temp files. This allows debugging
failures without re-running commands:

```bash
# Pattern: Redirect to temp file, then grep for relevant info
./gradlew build 2>&1 | tee /tmp/gradle-build.log | grep -E "FAILED|Error" || echo "Build successful"

# On failure, examine the full log without re-running
grep -A 20 "FAILED" /tmp/gradle-build.log
grep -B 5 -A 10 "Exception" /tmp/gradle-build.log
```

**Benefits**:

- No need to re-run failed commands to see more context
- Full log available for deep debugging
- Filtered output still shown in terminal for quick feedback

### Skip Code Coverage Report for Faster Tests

**CRITICAL**: When running tests (except for coverage-specific tasks), always use
`-x codeCoverageReport` to skip the coverage report generation. This significantly
speeds up test execution:

```bash
# Standard test execution (FAST - skip coverage report)
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Tests passed"

# Specific test (FAST - skip coverage report)
./gradlew :module:test --tests "com.bitso.MyTest" -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Test passed"

# ONLY use coverage report when explicitly needed (e.g., /fix-test-coverage command)
./gradlew test jacocoTestReport 2>&1 | tee /tmp/coverage.log | grep -E "FAILED|coverage" || echo "Coverage generated"
```

**When to INCLUDE coverage report**:

- `/fix-test-coverage` command - coverage is the goal
- `jacocoTestCoverageVerification` - verifying coverage thresholds
- Final validation before PR merge

**When to EXCLUDE coverage report** (`-x codeCoverageReport`):

- Running tests to verify fixes
- TDD cycles (write test, run, fix, repeat)
- Quick validation during development
- `/fix-sonarqube-issues` command
- `/fix-dependabot-vulnerabilities` command

### Workflow Best Practices

1. **Fail fast with targeted execution**: Run specific tests or modules before full builds
   to identify issues quickly
2. **Progressive verbosity**: Start with filtered output, add `--stacktrace` if needed,
   then `-d` for deep debugging
3. **Module isolation**: Use `:module:task` syntax to work on specific components
   without rebuilding everything
4. **Verify before running**: Check task availability with `./gradlew tasks --all | grep "task-name"`
5. **Skip unnecessary work**: Use `-x test` or `-x codeCoverageReport` when not needed

## Command Categories & Usage Patterns

### Build Operations

Build commands form the foundation of Gradle workflows.

```bash
# Build all modules (show failures only, save full log)
./gradlew build 2>&1 | tee /tmp/gradle-build.log | grep -E "FAILED|Error|Exception|BUILD FAILED" || echo "Build successful"

# Build specific module (faster, targeted)
./gradlew :module:build 2>&1 | tee /tmp/gradle-build.log | grep -E "FAILED|Error" || echo "Build successful"

# On failure, examine full log
grep -A 20 "FAILED" /tmp/gradle-build.log
```

### Testing Commands

Run specific tests before full test suites to fail fast. **Always use `-x codeCoverageReport`**
unless coverage is explicitly needed.

```bash
# Run all tests (FAST - skip coverage, save log)
./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED|Error|Exception" || echo "All tests passed"

# Run specific test class (FAST)
./gradlew test --tests "com.bitso.MyTest" -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED|Error" || echo "Test passed"

# Run specific test method (FAST)
./gradlew test --tests "com.bitso.MyTest.testMethod" -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Test passed"

# Run module-specific test (FAST)
./gradlew :module:test --tests "com.bitso.MyTest" -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "FAILED" || echo "Test passed"

# On failure, examine full log
grep -B 5 -A 20 "FAILED" /tmp/test.log
grep -A 10 "Exception" /tmp/test.log
```

### Dependency Management

Verify project stability by identifying conflicts and outdated versions.

```bash
# Check for conflicts (failures only)
./gradlew :module:dependencyInsight --dependency org.springframework 2>&1 | grep -E "conflicts|ERROR" || echo "No conflicts"

# Show outdated dependencies
./gradlew dependencyUpdates 2>&1 | grep -E "can be upgraded"
```

### Code Generation

Code generation must complete successfully before builds can proceed.

```bash
# Generate protobuf code (errors only, save log)
./gradlew generateProto 2>&1 | tee /tmp/proto.log | grep -i error || echo "Proto generation successful"

# Generate JOOQ code (errors only, save log)
./gradlew jooqCodegen 2>&1 | tee /tmp/jooq.log | grep -i error || echo "JOOQ generation successful"
```

### Quality Analysis & Coverage

Run quality checks and verify coverage thresholds before commits.

```bash
# Check coverage meets requirements (violations only) - INCLUDES coverage report
./gradlew check 2>&1 | tee /tmp/check.log | grep -E "FAILED|coverage" || echo "Coverage verified"

# Run SonarQube analysis (issues only)
./gradlew sonarqube 2>&1 | tee /tmp/sonar.log | grep -E "ERROR|WARN|Quality Gate" || echo "SonarQube scan complete"

# Generate coverage report explicitly (for /fix-test-coverage)
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage.log | grep -E "FAILED|Error" || echo "Coverage report generated"
```

## Debugging & Troubleshooting

### Common Problems & Solutions

When issues occur, apply progressive verbosity: start with filtered output, add
`--stacktrace` if needed, then use `-d` for detailed debugging. **Always check
the temp log file first** before re-running with more verbosity.

#### Problem: Build Fails with Unclear Error

**Solution**:

```bash
# First, check the saved log file
grep -A 20 "ERROR\|FAILED" /tmp/gradle-build.log

# If more context needed, show full error with context (10 lines after error)
./gradlew build 2>&1 | tee /tmp/gradle-build.log | grep -A 10 "ERROR\|FAILED"

# With stack trace for deeper analysis
./gradlew build --stacktrace 2>&1 | tee /tmp/gradle-build.log | tail -50

# With debug logging for detailed trace
./gradlew build -d 2>&1 | tee /tmp/gradle-debug.log | grep -E "ERROR|WARNING|DEBUG" | tail -30
```

#### Problem: Tests Fail in CI but Pass Locally

**Solution**:

```bash
# Run with info logging to see test execution details (skip coverage for speed)
./gradlew test -x codeCoverageReport --info 2>&1 | tee /tmp/test-info.log | grep -E "FAILED|Error" -A 5

# Clean and rebuild to eliminate cache-related issues
./gradlew clean test -x codeCoverageReport --rerun-tasks 2>&1 | tee /tmp/test-clean.log | grep -E "FAILED|Error"

# Run specific test with verbose output to isolate the failure
./gradlew test --tests "com.bitso.MyTest" -x codeCoverageReport -i 2>&1 | tee /tmp/test-verbose.log | grep -E "FAILED\|Executing\|Error"

# Check the log for more context
grep -B 10 -A 20 "FAILED" /tmp/test-info.log
```

#### Problem: Dependency Conflict

**Solution**:

```bash
# Find conflicting versions (shows resolution chain)
./gradlew :module:dependencyInsight --dependency commons-lang3 2>&1

# Show full tree for specific module (helps visualize hierarchy)
./gradlew :module:dependencies 2>&1 | grep -A 5 "commons-lang3"
```

#### Problem: Protocol Buffers or JOOQ Generation Fails

**Solution**:

```bash
# Show generation errors with context
./gradlew generateProto --stacktrace 2>&1 | tee /tmp/proto-error.log | grep -E "ERROR|Exception" -A 10

# Check the full log
cat /tmp/proto-error.log | tail -100

# Check if generated files exist and are recent
ls -la src/main/java/generated/ 2>&1 | head -20

# Regenerate from scratch to eliminate stale artifacts
./gradlew clean generateProto 2>&1 | tee /tmp/proto-clean.log | grep -E "ERROR"
```

#### Problem: JaCoCo Coverage Report Missing

**Solution**:

```bash
# Verify JaCoCo tasks are available
./gradlew tasks --all 2>&1 | grep -i jacoco

# Check if coverage exec file was generated
ls build/jacoco/test.exec 2>&1

# Regenerate reports with explicit test run (INCLUDE coverage report here)
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/jacoco.log | grep -E "FAILED|ERROR"
```

## Quick Reference Guide

| Task | Command |
|------|---------|
| Build | `./gradlew build 2>&1 \| tee /tmp/build.log \| grep -E "FAILED"` |
| Test (fast) | `./gradlew test -x codeCoverageReport 2>&1 \| tee /tmp/test.log \| grep -E "FAILED"` |
| Test specific | `./gradlew test --tests "com.bitso.MyTest" -x codeCoverageReport 2>&1 \| tee /tmp/test.log \| grep -E "FAILED"` |
| Test with coverage | `./gradlew test jacocoTestReport 2>&1 \| tee /tmp/coverage.log \| grep -E "FAILED"` |
| Proto Gen | `./gradlew generateProto 2>&1 \| tee /tmp/proto.log \| grep -i error` |
| JOOQ Gen | `./gradlew jooqCodegen 2>&1 \| tee /tmp/jooq.log \| grep -i error` |
| Coverage Report | `./gradlew jacocoTestReport 2>&1 \| tee /tmp/jacoco.log \| grep -E "coverage"` |
| Deps Check | `./gradlew dependencyInsight --dependency org.x 2>&1` |
| Debug failure | `grep -A 20 "FAILED" /tmp/test.log` |

## When to Use `-x codeCoverageReport`

| Scenario | Use `-x codeCoverageReport`? | Reason |
|----------|------------------------------|--------|
| Quick test validation | ✅ Yes | Speed - coverage not needed |
| TDD cycle | ✅ Yes | Speed - iterating quickly |
| Fix SonarQube issues | ✅ Yes | Focus on quality, not coverage |
| Fix vulnerabilities | ✅ Yes | Focus on security, not coverage |
| Fix test coverage | ❌ No | Coverage is the goal |
| Pre-commit validation | ❌ No | Full validation needed |
| CI pipeline | ❌ No | Coverage metrics needed |

## Related Rules

- **Gradle Build Configuration**: [java-gradle-best-practices.md](java-gradle-best-practices.md)
- **Version & Dependency Management**: [java-versions-and-dependencies.md](java-versions-and-dependencies.md)
- **JaCoCo Code Coverage**: [java-jacoco-coverage.md](java-jacoco-coverage.md)
- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - Ticket creation for AI agents

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-gradle-commands.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
