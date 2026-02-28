# Troubleshooting Mutation Testing

## Contents

- [Tests Fail During Mutation Testing](#tests-fail-during-mutation-testing)
- [Integration Tests Timeout](#integration-tests-timeout)
- [Out of Memory Errors](#out-of-memory-errors)
- [Mutation Testing Takes Too Long](#mutation-testing-takes-too-long)
- [No Mutations Generated](#no-mutations-generated)
- [JUnit 5 Tests Not Detected](#junit-5-tests-not-detected)
- [Spock Tests Not Detected](#spock-tests-not-detected)
- ["No tests executed" Error](#no-tests-executed-error)
- [Surviving Mutants on Obvious Code](#surviving-mutants-on-obvious-code)

---
## Tests Fail During Mutation Testing

**Cause:** Tests may have order dependencies, timing issues, or require external resources.

**Fix:**

1. Run tests in isolation: `./gradlew test --tests "*FailingTest"`
2. Check for shared mutable state
3. Add `@Isolated` annotation if using Spock
4. For Testcontainers: default timeout (2 min + 1.5x) should handle most cases
5. If still timing out, check container startup time

## Integration Tests Timeout

**Cause:** Testcontainers or Spring context startup takes too long.

**Fix:** Target faster tests first:

```bash
# Unit tests only
./gradlew pitest -Ppitest.targetTests='*UnitSpec,*UnitTest'

# Exclude integration tests
./gradlew pitest -Ppitest.targetTests='!*IntegrationSpec,!*IT'
```

## Out of Memory Errors

**Cause:** Too many mutations or large test suite.

**Fix:**

```bash
./gradlew pitest -Ppitest.jvmArgs='-Xmx6g'
```

Or target specific packages:

```bash
./gradlew pitest -Ppitest.targetClasses='com.bitso.critical.*'
```

## Mutation Testing Takes Too Long

**Cause:** Large codebase or COMPREHENSIVE level.

**Fix:**

1. Start with QUICK level
2. Target specific modules: `./gradlew :module:pitest`
3. Increase threads: `-Ppitest.threads=8`
4. Use history files for incremental analysis

## No Mutations Generated

**Cause:** Exclusion patterns too broad or no code in target packages.

**Fix:**

1. Enable verbose: `-Ppitest.verbose=true`
2. Check exclusion patterns in `pitest.gradle`
3. Verify source files exist in expected packages

## JUnit 5 Tests Not Detected

**Cause:** Missing `pitest-junit5-plugin`.

**Fix:** Ensure `pitest.gradle` includes:

```groovy
pitest {
    junit5PluginVersion = '1.2.3'
}
```

## Spock Tests Not Detected

**Cause:** Spock tests must match the test pattern.

**Fix:** Check target tests pattern:

```bash
./gradlew pitest -Ppitest.targetTests='*Spec,*Test'
```

## "No tests executed" Error

**Cause:** Test class pattern doesn't match actual test classes.

**Fix:**

```bash
# Debug: list what PIT sees
./gradlew pitest -Ppitest.verbose=true

# Common patterns for Spock + JUnit
./gradlew pitest -Ppitest.targetTests='*Spec,*Test,*Tests'
```

## Surviving Mutants on Obvious Code

**Cause:** The test runs but doesn't assert on the specific behavior.

**Example:**

```java
// Code
return calculateTotal(items);

// Surviving mutant: "replaced return with null"
// Test that doesn't catch it:
assertNotNull(service.process(items));  // Too weak!

// Test that catches it:
assertEquals(100, service.process(items).getTotal());
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/mutation-testing/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

