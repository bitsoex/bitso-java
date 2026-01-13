# Write tests to improve code coverage for Java projects - NO production code changes

> Write tests to improve code coverage for Java projects - NO production code changes

# 🤖 🧪 Improve Test Coverage

**IMPORTANT**: This command focuses on **writing tests only**. Only test code should be added or modified.

## Prerequisites

Before running this command, ensure test infrastructure is set up by running `/improve-test-setup` first. This command builds upon proper JaCoCo configuration and testing library versions.

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first**
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - Report analysis
- **Test Setup**: `java/commands/improve-test-setup.md` - Testing infrastructure setup

## Coverage Target

- **Minimum**: 82% instruction and branch coverage
- **Goal**: Achieve and maintain coverage thresholds

## Workflow

### 1. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, create a Jira ticket for tracking:

Use `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 🧪 Improve test coverage for [repo-name]`
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Description**: Include current coverage % and target

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 2. Create Branch with Jira Key

```bash
# JIRA_KEY is the actual ticket key from Step 1 (e.g., PROJ-123)
git checkout -b test/${JIRA_KEY}-improve-test-coverage
```

### 3. Generate Coverage Reports

```bash
# Run tests with coverage - DO NOT skip coverage report
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage-run.log

# Verify reports generated
ls -lh build/reports/jacoco/test/
```

**⚠️ CRITICAL**: NEVER use `-x codeCoverageReport` or skip coverage tasks. This leads to incomplete coverage data.

### 4. Analyze Coverage Reports

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
# Open HTML report for detailed analysis
open build/reports/jacoco/test/html/index.html
```

The HTML report shows:

- **Red lines**: Not covered - need tests
- **Yellow lines**: Partially covered branches - need additional test cases
- **Green lines**: Fully covered

### 5. Identify Coverage Gaps

Focus on these areas (in priority order):

1. **Business Logic Classes**: Services, handlers, processors
2. **Edge Cases**: Error handling, boundary conditions
3. **Branch Coverage**: If/else, switch statements, ternary operators
4. **Exception Paths**: Try/catch blocks, error scenarios

**Exclude from coverage** (already configured in JaCoCo):

- Generated code (`**/generated/**`, `**/proto/**`)
- DTOs and mappers (`**/dto/**`, `**/*MapperImpl.class`)
- Configuration classes (`**/config/**`)
- Application entry points (`**/*Application.class`)

### 6. Write Tests (Spock Preferred)

#### Spock Test Template (Preferred)

```groovy
package com.bitso.yourservice

import spock.lang.Specification
import spock.lang.Subject

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
        
        and: "dependency returns expected result"
        mockDependency.process(_) >> new Result(success: true)

        when: "service is called"
        def result = service.execute(input)

        then: "returns expected output"
        result.success
        result.data == "expected"
    }

    def "should handle error case"() {
        given: "invalid input"
        def input = new InputDto(value: null)

        when: "service is called"
        service.execute(input)

        then: "throws expected exception"
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("value cannot be null")
    }

    def "should cover all branches"() {
        expect: "correct behavior for each case"
        service.categorize(value) == expected

        where: "testing all branches"
        value | expected
        0     | "zero"
        1     | "positive"
        -1    | "negative"
        100   | "positive"
        -100  | "negative"
    }
}
```

#### JUnit 5 Test Template (Alternative)

```java
package com.bitso.yourservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class YourServiceTest {

    @Mock
    private DependencyClass mockDependency;

    private YourService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new YourService(mockDependency);
    }

    @Test
    @DisplayName("should handle normal case")
    void shouldHandleNormalCase() {
        // Given
        var input = new InputDto("test");
        when(mockDependency.process(any())).thenReturn(new Result(true));

        // When
        var result = service.execute(input);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("expected");
    }

    @Test
    @DisplayName("should throw exception for invalid input")
    void shouldThrowExceptionForInvalidInput() {
        // Given
        var input = new InputDto(null);

        // When/Then
        assertThatThrownBy(() -> service.execute(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("value cannot be null");
    }

    @ParameterizedTest
    @CsvSource({
        "0, zero",
        "1, positive",
        "-1, negative",
        "100, positive",
        "-100, negative"
    })
    @DisplayName("should cover all branches")
    void shouldCoverAllBranches(int value, String expected) {
        assertThat(service.categorize(value)).isEqualTo(expected);
    }
}
```

### 7. Integration Tests (When Needed)

For components that interact with external systems, use Testcontainers:

```groovy
package com.bitso.yourservice

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class YourRepositoryIntegrationSpec extends Specification {

    @Shared
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")

    def "should persist and retrieve entity"() {
        given: "a repository connected to test database"
        def dataSource = createDataSource(postgres)
        def repository = new YourRepository(dataSource)
        
        and: "an entity to save"
        def entity = new YourEntity(name: "test")

        when: "entity is saved"
        repository.save(entity)
        
        and: "retrieved by name"
        def found = repository.findByName("test")

        then: "entity is found"
        found.isPresent()
        found.get().name == "test"
    }
}
```

### 8. Run Tests and Verify Coverage

```bash
# Run all tests with coverage
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage-verify.log

# Check for test failures
grep -E "(FAILED|PASSED)" /tmp/coverage-verify.log

# Verify coverage thresholds
./gradlew jacocoTestCoverageVerification

# Check new coverage percentage
cat build/reports/jacoco/test/jacocoTestReport.csv | \
    awk -F',' 'NR>1 {missed+=$4; covered+=$5} END {
        total=missed+covered;
        if(total>0) print "Coverage: " covered/total*100 "%"
    }'
```

### 9. Iterate Until Coverage Met

Repeat steps 4-8 until:

- All tests pass
- Coverage meets 82% threshold
- No new test failures introduced

### 10. Commit with Emojis and Jira Key

```bash
# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

git add -A
git commit -m "🤖 🧪 chore: [$JIRA_KEY] improve test coverage

- Added tests for [ClassName1] - covers [scenario]
- Added tests for [ClassName2] - covers [scenario]
- Improved branch coverage for [area]
- Coverage improved from X% to Y%

Generated with the Quality Agent by the /improve-test-coverage command."
```

### 11. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

gh pr create --draft \
    --title "🤖 🧪 [$JIRA_KEY] chore: improve test coverage" \
    --body "## 🤖 AI-Assisted Test Coverage Improvements

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Coverage Changes

| Metric | Before | After |
|--------|--------|-------|
| Instruction Coverage | X% | Y% |
| Branch Coverage | X% | Y% |

## Tests Added

### Unit Tests
- \`YourServiceSpec\` - Tests for [functionality]
- \`AnotherServiceSpec\` - Tests for [functionality]

### Integration Tests (if any)
- \`YourRepositoryIntegrationSpec\` - Database operations

## Validation
- [x] All tests pass locally
- [x] Coverage meets 82% threshold
- [ ] CI passes

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /improve-test-coverage

Generated with the Quality Agent by the /improve-test-coverage command.

## Scope Notice
⚠️ This PR only adds **test code**. No production code or production libraries were changed."
```

## Test Writing Best Practices

### Focus on Behavior, Not Implementation

```groovy
// ❌ Bad: Tests implementation details
def "should call repository.save()"() {
    when:
    service.createUser(dto)
    
    then:
    1 * repository.save(_)
}

// ✅ Good: Tests behavior
def "should persist new user"() {
    when:
    def user = service.createUser(dto)
    
    then:
    user.id != null
    service.findById(user.id).isPresent()
}
```

### Test Edge Cases

```groovy
def "should handle edge cases"() {
    expect:
    service.process(input) == expected

    where:
    input       | expected
    null        | Optional.empty()
    ""          | Optional.empty()
    " "         | Optional.empty()
    "valid"     | Optional.of("VALID")
    "VALID"     | Optional.of("VALID")
}
```

### Test Exception Scenarios

```groovy
def "should throw specific exception with message"() {
    given:
    mockService.call(_) >> { throw new ServiceException("error") }

    when:
    service.execute()

    then:
    def ex = thrown(ProcessingException)
    ex.message == "Failed to process: error"
    ex.cause instanceof ServiceException
}
```

### Use Data Tables for Branch Coverage

```groovy
def "should cover all status transitions"() {
    expect:
    service.canTransition(from, to) == allowed

    where:
    from        | to          | allowed
    "PENDING"   | "APPROVED"  | true
    "PENDING"   | "REJECTED"  | true
    "APPROVED"  | "COMPLETED" | true
    "APPROVED"  | "PENDING"   | false  // Cannot go back
    "REJECTED"  | "APPROVED"  | false  // Cannot resurrect
    "COMPLETED" | "PENDING"   | false  // Final state
}
```

## Troubleshooting

### Tests Pass but Coverage Not Increasing

**Cause**: Tests may be hitting already-covered code

**Fix**: Use HTML report to identify specific uncovered lines:

```bash
open build/reports/jacoco/test/html/index.html
```

Navigate to the class and look for red/yellow lines.

### Coverage Verification Fails

**Symptom**: `jacocoTestCoverageVerification` task fails

**Cause**: Coverage below 82% threshold

**Fix**: Continue adding tests until threshold is met. Focus on:

1. Classes with lowest coverage first
2. Branch coverage (yellow lines in HTML)
3. Exception handling paths

### Flaky Tests

**Symptom**: Tests pass sometimes, fail other times

**Cause**: Usually timing issues or shared state

**Fix**:

- Use `@Timeout` for async operations
- Ensure test isolation (no shared mutable state)
- Use `Awaitility` for async assertions

## Next Steps

Would you like to run one of the related testing improvement commands?

- `/improve-test-setup` - Upgrade testing libraries and configure JaCoCo
- `/improve-test-quality-with-mutation-testing` - Use PIT mutation testing to find weak tests

## Related

- **Test Setup**: `java/commands/improve-test-setup.md` - Testing infrastructure setup
- **Mutation Testing**: `java/commands/improve-test-quality-with-mutation-testing.md` - Find weak tests
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - Report analysis

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/improve-test-coverage.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
