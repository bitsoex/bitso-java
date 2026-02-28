# Improving Tests to Kill Mutants

For each surviving mutant, understand what changed and add specific assertions.

## Contents

- [Common Patterns](#common-patterns)
- [Prioritization](#prioritization)
- [Best Practices](#best-practices)
- [Equivalent Mutants](#equivalent-mutants)

---
## Common Patterns

### Pattern A: Missing Boundary Condition

**Surviving mutant:** `changed conditional boundary: < to <=`

```groovy
// BEFORE: Weak test
def "should validate age"() {
    expect:
    validator.isAdult(25) == true  // Only tests happy path
}

// AFTER: Boundary-aware test
def "should validate age at boundaries"() {
    expect:
    validator.isAdult(age) == expected

    where:
    age | expected
    17  | false    // Below boundary
    18  | true     // At boundary
    19  | true     // Above boundary
}
```

### Pattern B: Missing Return Value Verification

**Surviving mutant:** `replaced return value with null/empty/0`

```groovy
// BEFORE: Only checks not null
def "should calculate discount"() {
    expect:
    service.calculateDiscount(100, "VIP") != null
}

// AFTER: Verifies exact value
def "should calculate VIP discount as 20%"() {
    expect:
    service.calculateDiscount(100, "VIP") == 20
}
```

### Pattern C: Missing Negative Case

**Surviving mutant:** `negated conditional: == to !=`

```groovy
// BEFORE: Only tests when condition is true
def "should apply premium rate when eligible"() {
    given:
    customer.premiumEligible = true

    expect:
    calculator.getRate(customer) == 0.05
}

// AFTER: Also test when condition is false
def "should apply standard rate when not eligible"() {
    given:
    customer.premiumEligible = false

    expect:
    calculator.getRate(customer) == 0.10
}
```

### Pattern D: Missing Math Verification

**Surviving mutant:** `replaced multiplication with division`

```groovy
// BEFORE: Too weak
def "should return discount for premium users"() {
    expect:
    calculator.calculateDiscount(100, true) > 0
}

// AFTER: Exact assertion
def "should return 10% discount for premium users"() {
    expect:
    calculator.calculateDiscount(100, true) == 10
}
```

## Prioritization

### High Priority (fix first)

- Business logic methods (calculations, validations, rules)
- Methods with multiple surviving mutants
- `CONDITIONALS_BOUNDARY` mutants (off-by-one bugs)
- `NEGATE_CONDITIONALS` mutants (missing negative cases)

### Low Priority (fix later or skip)

- Logging statements (`VOID_METHOD_CALLS` on loggers)
- Builder/DTO methods with trivial logic
- Framework callbacks hard to unit test

## Best Practices

**DO:**

- Add specific assertions that verify exact values
- Test boundary conditions explicitly (`<=` vs `<`)
- Add negative test cases for each positive case
- Use parameterized tests for boundary values
- Group related assertions in the same test

**DON'T:**

- Duplicate test logic across methods
- Create tests that only check for exceptions
- Add assertions that don't exercise the mutated code
- Create overly broad tests

## Equivalent Mutants

Some mutants are semantically equivalent and can't be killed. Examples:

- Removing a call to `toString()` on an already-String value
- Changing increment timing when result isn't used

It's OK to skip these - don't chase 100% mutation score.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/mutation-testing/references/improving-tests.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

