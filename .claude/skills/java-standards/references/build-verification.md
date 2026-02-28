# Build Verification

After updating Java or Groovy code, verify your changes before committing.

## Contents

- [Quick Verification](#quick-verification)
- [Detailed Commands](#detailed-commands)
- [Troubleshooting Build Failures](#troubleshooting-build-failures)
- [Related Resources](#related-resources)

---
## Quick Verification

```bash
# Run tests to verify changes
./gradlew test 2>&1 | grep -E "FAILED|Error" || echo "All tests passed"

# Or run full build with tests
./gradlew build 2>&1 | grep -E "FAILED|Error" || echo "Build successful"
```

## Detailed Commands

### Run All Tests

```bash
./gradlew test
```

### Run Specific Module Tests

```bash
./gradlew :module-name:test
```

### Run Tests Without Coverage (Faster)

```bash
./gradlew test -x codeCoverageReport
```

### Build Without Tests

```bash
./gradlew build -x test
```

### Clean Build

```bash
./gradlew clean build
```

## Troubleshooting Build Failures

### Compilation Errors

Check the error output for:
- Missing imports
- Type mismatches
- Missing dependencies

### Test Failures

Run specific failing tests for detailed output:

```bash
./gradlew test --tests "com.bitso.MyTest" --info
```

### Dependency Issues

Check dependency tree:

```bash
./gradlew dependencies --configuration runtimeClasspath
```

### Cache Issues

Clear Gradle cache if experiencing strange behavior:

```bash
./gradlew clean
rm -rf ~/.gradle/caches/modules-2/files-2.1/com.bitso*
```

## Related Resources

For comprehensive build commands and troubleshooting, see the gradle-standards skill.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-standards/references/build-verification.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

