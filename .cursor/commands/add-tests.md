# Generate missing tests for code that lacks coverage

Generate missing tests for code that lacks coverage

# Add Tests

Generate missing tests to improve code coverage.

## Purpose

This command invokes the **test-augmentation skill** to:
- Identify untested code paths
- Generate appropriate test cases
- Maintain or improve coverage thresholds

## Skill Location

```
.agent-skills/test-augmentation/
```

## Quick Workflow

### Step 1: Check Current Coverage

```bash
# JavaScript/TypeScript
pnpm run test:coverage

# Python
pytest --cov=. --cov-report=html

# Java
./gradlew jacocoTestReport
```

### Step 2: Identify Gaps

Review the coverage report to find:
- Uncovered files
- Untested functions
- Missing edge cases

### Step 3: Generate Tests

For each uncovered area:
1. Identify the function/class to test
2. Determine test cases (happy path, edge cases, errors)
3. Write tests following project patterns

## Test Patterns by Language

### JavaScript/TypeScript (Jest)

```typescript
describe('FunctionName', () => {
  it('should handle happy path', () => {
    const result = functionName(validInput);
    expect(result).toBe(expectedOutput);
  });

  it('should handle edge case', () => {
    expect(() => functionName(invalidInput)).toThrow();
  });
});
```

### Python (pytest)

```python
def test_function_happy_path():
    result = function_name(valid_input)
    assert result == expected_output

def test_function_edge_case():
    with pytest.raises(ValueError):
        function_name(invalid_input)
```

### Java (JUnit)

```java
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
```

## Coverage Targets

| Type | Target |
|------|--------|
| Line coverage | 80%+ |
| Branch coverage | 70%+ |
| Critical paths | 100% |

## Related Commands

| Command | Purpose |
|---------|---------|
| `/quality-check` | Run full quality gate |
| `/sync-docs` | Update documentation |

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full test augmentation documentation |
| `references/` | Technology-specific test patterns |

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/add-tests.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
