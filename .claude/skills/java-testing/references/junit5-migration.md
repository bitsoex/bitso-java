---
title: JUnit 5 Migration
description: Patterns for migrating from JUnit 4 to JUnit 5 and resolving version conflicts
---

# JUnit 5 Migration

Patterns for migrating from JUnit 4 to JUnit 5 and resolving version conflicts.

## Contents

- [Version Conflict Resolution](#version-conflict-resolution)
- [JUnit 4 to 5 Migration](#junit-4-to-5-migration)
- [Using Testing Bundles](#using-testing-bundles)
- [Related](#related)

---
## Version Conflict Resolution

### Common Issue: NoClassDefFoundError

**Symptom**:
```
NoClassDefFoundError: org/junit/jupiter/api/extension/TestInstantiationAwareExtension
```

**Cause**: Mixed JUnit versions (e.g., 5.10.1 from plugin with 5.14.1 declared)

**Fix**: Add resolution strategy in root `build.gradle`:

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

### Platform Launcher Requirement

Gradle 9+ requires explicit `junit-platform-launcher`:

```groovy
dependencies {
    testRuntimeOnly libs.junit.platform.launcher
}
```

## JUnit 4 to 5 Migration

### Annotation Changes

| JUnit 4 | JUnit 5 |
|---------|---------|
| `@Before` | `@BeforeEach` |
| `@After` | `@AfterEach` |
| `@BeforeClass` | `@BeforeAll` |
| `@AfterClass` | `@AfterAll` |
| `@Ignore` | `@Disabled` |
| `@RunWith` | `@ExtendWith` |
| `@Rule` | `@ExtendWith` or `@RegisterExtension` |
| `@Test(expected = X)` | `assertThrows(X, () -> ...)` |

### Before (JUnit 4)

```java
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class MyServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        // setup
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidation() {
        service.validate(null);
    }
}
```

### After (JUnit 5)

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class MyServiceTest {

    @BeforeEach
    void setUp() {
        // setup
    }

    @Test
    @DisplayName("should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        assertThrows(IllegalArgumentException.class,
            () -> service.validate(null));
    }
}
```

### Parameterized Tests

JUnit 5 provides native parameterized test support:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CalculatorTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void shouldAcceptPositiveNumbers(int value) {
        assertTrue(calculator.isPositive(value));
    }

    @ParameterizedTest
    @CsvSource({
        "0, zero",
        "1, positive",
        "-1, negative"
    })
    void shouldCategorize(int value, String expected) {
        assertEquals(expected, calculator.categorize(value));
    }
}
```

## Using Testing Bundles

Simplify dependency declarations with bundles:

```groovy
dependencies {
    // ✅ Use bundles
    testImplementation libs.bundles.testing.spring
    testImplementation libs.bundles.testing.integration

    // ❌ Avoid verbose individual declarations
    // testImplementation libs.spring.boot.starter.test
    // testImplementation libs.spock.core
    // testImplementation libs.spock.spring
}
```

## Related

- [Test Setup Workflow](test-setup-workflow.md) - Full setup guide
- [Spock Patterns](spock-patterns.md) - Spock-specific patterns
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/junit5-migration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

