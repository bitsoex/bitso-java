---
title: Test Patterns
description: Common test patterns across technologies
---

# Test Patterns

Common test patterns for different technologies.

## Contents

- [JavaScript/TypeScript (Jest/Vitest)](#javascripttypescript-jestvitest)
- [Python (pytest)](#python-pytest)
- [Java (JUnit 5)](#java-junit-5)
- [Go (testing package)](#go-testing-package)
- [Coverage Commands](#coverage-commands)
- [Coverage Targets](#coverage-targets)
- [Related](#related)

---
## JavaScript/TypeScript (Jest/Vitest)

```typescript
import { describe, it, expect } from 'vitest';

describe('FunctionName', () => {
  it('should handle happy path', () => {
    const result = functionName(validInput);
    expect(result).toBe(expectedOutput);
  });

  it('should handle edge case', () => {
    expect(() => functionName(invalidInput)).toThrow();
  });

  it('should handle null input', () => {
    const result = functionName(null);
    expect(result).toBeNull();
  });
});

// Async testing
it('should handle async operations', async () => {
  const result = await asyncFunction();
  expect(result).toBeDefined();
});
```

## Python (pytest)

```python
import pytest

def test_function_happy_path():
    """Test normal operation."""
    result = function_name(valid_input)
    assert result == expected_output

def test_function_edge_case():
    """Test error handling."""
    with pytest.raises(ValueError):
        function_name(invalid_input)

def test_function_none_input():
    """Test null/none handling."""
    result = function_name(None)
    assert result is None

# Parameterized tests
@pytest.mark.parametrize("input,expected", [
    ("a", 1),
    ("b", 2),
    ("c", 3),
])
def test_function_multiple_inputs(input, expected):
    assert function_name(input) == expected
```

## Java (JUnit 5)

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FunctionNameTest {

    @Test
    void shouldHandleHappyPath() {
        var result = functionName(validInput);
        assertEquals(expectedOutput, result);
    }

    @Test
    void shouldHandleEdgeCase() {
        assertThrows(IllegalArgumentException.class,
            () -> functionName(invalidInput));
    }

    @Test
    void shouldHandleNullInput() {
        var result = functionName(null);
        assertNull(result);
    }
}

// Parameterized tests
@ParameterizedTest
@CsvSource({"a,1", "b,2", "c,3"})
void shouldHandleMultipleInputs(String input, int expected) {
    assertEquals(expected, functionName(input));
}
```

## Go (testing package)

```go
package main

import "testing"

func TestFunctionName(t *testing.T) {
    t.Run("happy path", func(t *testing.T) {
        result := FunctionName(validInput)
        if result != expectedOutput {
            t.Errorf("got %v, want %v", result, expectedOutput)
        }
    })

    t.Run("edge case", func(t *testing.T) {
        defer func() {
            if r := recover(); r == nil {
                t.Error("expected panic")
            }
        }()
        FunctionName(invalidInput)
    })
}

// Table-driven tests
func TestFunctionMultipleInputs(t *testing.T) {
    tests := []struct {
        input    string
        expected int
    }{
        {"a", 1},
        {"b", 2},
        {"c", 3},
    }
    for _, tt := range tests {
        t.Run(tt.input, func(t *testing.T) {
            if got := FunctionName(tt.input); got != tt.expected {
                t.Errorf("got %v, want %v", got, tt.expected)
            }
        })
    }
}
```

## Coverage Commands

| Technology | Command |
|------------|---------|
| **JavaScript/TypeScript** | `pnpm run test:coverage` or `npx vitest --coverage` |
| **Python** | `pytest --cov=. --cov-report=html` |
| **Java** | `./gradlew jacocoTestReport` |
| **Go** | `go test -cover ./...` or `go test -coverprofile=coverage.out ./...` |

### Viewing Reports

| Technology | Report Location |
|------------|-----------------|
| JavaScript/TypeScript | `coverage/lcov-report/index.html` |
| Python | `htmlcov/index.html` |
| Java | `build/reports/jacoco/test/html/index.html` |
| Go | Use `go tool cover -html=coverage.out` |

## Coverage Targets

| Type | Target | Priority |
|------|--------|----------|
| **Line coverage** | 80%+ | Required |
| **Branch coverage** | 70%+ | Required |
| **Critical paths** | 100% | Required |
| **Edge cases** | 90%+ | Recommended |
| **Error handling** | 100% | Required |

Focus testing effort on:
1. Business logic (highest priority)
2. Error handling paths
3. Edge cases and boundary conditions
4. Integration points

## Related

- [../SKILL.md](.claude/skills/testing-standards/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/testing-standards/references/test-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

