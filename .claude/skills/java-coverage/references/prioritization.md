---
title: Coverage Prioritization
description: Guidelines for prioritizing what to test first for maximum coverage impact
---

# Coverage Prioritization

Guidelines for prioritizing what to test first for maximum coverage impact.

## Contents

- [Priority Order](#priority-order)
- [What to Exclude](#what-to-exclude)
- [Coverage Impact Analysis](#coverage-impact-analysis)
- [Test Writing Best Practices](#test-writing-best-practices)
- [Related](#related)

---
## Priority Order

### Priority 1: Business Logic Classes

Focus on core business logic first:

- **Services**: `*Service.java`, `*ServiceImpl.java`
- **Handlers**: `*Handler.java`, `*Processor.java`
- **Domain Logic**: `*Domain.java`, `*BusinessLogic.java`

These classes typically contain the most important logic and have the highest ROI for testing.

### Priority 2: Edge Cases and Error Handling

After happy paths, test edge cases:

- Null inputs
- Empty collections
- Boundary values (0, -1, MAX_VALUE)
- Invalid state transitions

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
}
```

### Priority 3: Branch Coverage

Target partially covered branches (yellow lines in HTML report):

- If/else statements
- Switch statements
- Ternary operators
- Short-circuit evaluation

```groovy
def "should cover all status transitions"() {
    expect:
    service.canTransition(from, to) == allowed

    where:
    from        | to          | allowed
    "PENDING"   | "APPROVED"  | true
    "PENDING"   | "REJECTED"  | true
    "APPROVED"  | "PENDING"   | false  // Cannot go back
    "REJECTED"  | "APPROVED"  | false  // Cannot resurrect
}
```

### Priority 4: Exception Paths

Test exception handling:

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

## What to Exclude

These are typically excluded from coverage and shouldn't be prioritized:

| Pattern | Reason |
|---------|--------|
| `**/generated/**` | Auto-generated code |
| `**/proto/**` | Protocol buffer generated code |
| `**/dto/**` | Data transfer objects |
| `**/*MapperImpl.class` | MapStruct generated mappers |
| `**/config/**` | Spring configuration classes |
| `**/*Application.class` | Application entry points |

## Coverage Impact Analysis

### High Impact (Test First)

- Large classes with low coverage
- Classes with complex branching logic
- Classes handling critical business rules

### Low Impact (Test Later)

- Small utility classes with simple logic
- Classes already at 80%+ coverage
- Getter/setter-only classes

### Finding High-Impact Classes

```bash
# Find largest uncovered classes by missed instructions
cat build/reports/jacoco/test/jacocoTestReport.csv | \
    awk -F',' 'NR>1 && $4>100 {print $4, $2"."$3}' | \
    sort -rn | head -10
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

### Use Data Tables for Branch Coverage

Data tables are highly effective for branch coverage:

```groovy
def "should validate input correctly"() {
    expect:
    validator.isValid(input) == valid

    where:
    input           | valid
    null            | false
    ""              | false
    "abc"           | false   // too short
    "abc123"        | true    // valid
    "ABC123"        | true    // case insensitive
    "abc123xyz789"  | false   // too long
}
```

## Related

- [Improvement Workflow](improvement-workflow.md) - Full workflow guide
- [Coverage Targets](coverage-targets.md) - Target percentages
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-coverage/references/prioritization.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

