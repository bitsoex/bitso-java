# Use PIT mutation testing to identify weak tests and improve test quality in Java projects

**Description:** Use PIT mutation testing to identify weak tests and improve test quality in Java projects

# Improve Test Quality with Mutation Testing

Use PIT (Pitest) mutation testing to identify tests that pass but don't actually verify behavior correctly. Mutation testing introduces small changes (mutations) to your code and checks if your tests detect them. Tests that fail to detect mutations ("surviving mutants") indicate weak test assertions.

## Prerequisites

1. **Gradle 8.x+** - Check with `./gradlew --version`
2. **JaCoCo configured** - See `java/rules/java-jacoco-coverage.md`
3. **Tests pass** - All tests must pass before mutation testing
4. **Java 21** - Project must use Java 21

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first**
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - Coverage configuration
- **Gradle Commands**: `java/rules/java-gradle-commands.md`

## Mutation Testing Intensity Levels

Choose the appropriate level based on your goal:

| Level | Mutator Group | Mutators | Use Case | Time Factor vs QUICK |
|-------|---------------|----------|----------|----------------------|
| **QUICK** | `DEFAULTS` | ~11 | Initial analysis, CI gates, fast feedback | 1x (baseline) |
| **STANDARD** | `STRONGER` | ~14 | Regular improvement cycles, PR validation | 1.2-1.5x |
| **COMPREHENSIVE** | `ALL` | ~30+ | Deep analysis, critical business logic | 3-5x |

**Time Factor Notes:**

- Time multiplier is based on PIT analysis time, not total Gradle execution time
- First run includes Gradle/JVM startup overhead (~10-30s); subsequent runs are faster
- Projects with Testcontainers/Spring integration tests add container startup time per mutation batch
- Mutation count grows ~1.4x from QUICK→STANDARD and ~4-5x from QUICK→COMPREHENSIVE

### QUICK Level (DEFAULTS)

Best for initial exploration and CI integration:

- CONDITIONALS_BOUNDARY - Changes `<` to `<=`, etc.
- INCREMENTS - Changes `i++` to `i--`
- INVERT_NEGS - Removes negation from numbers
- MATH - Replaces `+` with `-`, `*` with `/`, etc.
- NEGATE_CONDITIONALS - Changes `==` to `!=`
- VOID_METHOD_CALLS - Removes void method calls
- EMPTY_RETURNS - Returns empty collections/strings
- FALSE_RETURNS - Returns false for boolean methods
- TRUE_RETURNS - Returns true for boolean methods
- NULL_RETURNS - Returns null for object methods
- PRIMITIVE_RETURNS - Returns 0 for numeric methods

### STANDARD Level (STRONGER)

Adds to QUICK:

- REMOVE_CONDITIONALS_EQUAL_ELSE - Forces else branch
- EXPERIMENTAL_SWITCH - Mutates switch statements

### COMPREHENSIVE Level (ALL)

Adds to STANDARD:

- INLINE_CONSTS - Mutates inline constants
- CONSTRUCTOR_CALLS - Replaces constructors with null
- NON_VOID_METHOD_CALLS - Removes non-void method calls
- REMOVE_INCREMENTS - Removes increment operations
- EXPERIMENTAL_ARGUMENT_PROPAGATION - Swaps method arguments
- EXPERIMENTAL_BIG_INTEGER - Mutates BigInteger operations
- EXPERIMENTAL_MEMBER_VARIABLE - Resets member variables
- EXPERIMENTAL_NAKED_RECEIVER - Removes method chain calls
- ABS (Negation) - Negates numeric values
- AOR (Arithmetic Operator Replacement) - All arithmetic swaps
- AOD (Arithmetic Operator Deletion) - Removes operands
- CRCR (Constant Replacement) - Multiple constant mutations
- OBBN (Bitwise Operator) - Mutates bitwise operations
- ROR (Relational Operator Replacement) - All comparison swaps
- UOI (Unary Operator Insertion) - Inserts unary operators

## Workflow

### 1. Check Readiness

Run the readiness check script:

```bash
# From the ai-code-instructions repo
bash java/scripts/check-pitest-readiness.sh
```

Or manually verify:

```bash
# Verify Gradle version
./gradlew --version

# Verify tests pass
./gradlew test -x codeCoverageReport

# Verify JaCoCo is configured
./gradlew tasks --all | grep -i jacoco
```

### 2. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, create a Jira ticket for tracking:

Use `mcp_atlassian_createJiraIssue`:

- **Summary**: `Improve test quality with mutation testing for [module/repo-name]`
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Description**: Include target module and intensity level

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 3. Create Branch with Jira Key

```bash
# JIRA_KEY is the actual ticket key from Step 2 (e.g., PROJ-123)
MODULE="payment-service"  # Target module or "all"
LEVEL="standard"  # quick, standard, or comprehensive

git checkout -b test/${JIRA_KEY}-mutation-testing-${MODULE}
```

### 4. Apply PIT Configuration

Copy the PIT Gradle configuration to your project:

```bash
# Copy pitest.gradle template to your project's gradle directory
cp /path/to/ai-code-instructions/java/templates/pitest.gradle gradle/pitest.gradle
```

Then apply it in your root `build.gradle`:

```groovy
// In root build.gradle, add to subprojects block
subprojects { Project subproject ->
    subproject.afterEvaluate {
        if (new File("${subproject.projectDir}/src/test").exists()) {
            project.apply(from: "${rootDir}/gradle/pitest.gradle")
        }
    }
}
```

Or apply to specific modules only:

```groovy
// In specific module's build.gradle
apply from: "${rootDir}/gradle/pitest.gradle"
```

### 5. Add Version Catalog Entries (Recommended)

Add to `gradle/libs.versions.toml`:

```toml
[versions]
pitest = "1.22.0"
pitest-plugin = "1.19.0-rc.2"
pitest-junit5 = "1.2.3"

[plugins]
pitest = { id = "info.solidsoft.pitest", version.ref = "pitest-plugin" }
```

### 6. Run Mutation Testing

#### Full Project (All Modules)

```bash
# QUICK level - fastest, good for initial analysis
./gradlew pitest -Ppitest.mutators=DEFAULTS 2>&1 | tee /tmp/pitest.log

# STANDARD level - recommended for regular use
./gradlew pitest -Ppitest.mutators=STRONGER 2>&1 | tee /tmp/pitest.log

# COMPREHENSIVE level - thorough, slower
./gradlew pitest -Ppitest.mutators=ALL 2>&1 | tee /tmp/pitest.log
```

#### Single Module

```bash
# Run on specific module
./gradlew :bitso-libs:payment-service:pitest -Ppitest.mutators=STRONGER
```

#### Specific Package/Class

```bash
# Target specific package
./gradlew pitest \
    -Ppitest.targetClasses='com.bitso.payment.service.*' \
    -Ppitest.mutators=STRONGER

# Target specific class
./gradlew pitest \
    -Ppitest.targetClasses='com.bitso.payment.service.PaymentProcessor' \
    -Ppitest.targetTests='*PaymentProcessorSpec,*PaymentProcessorTest' \
    -Ppitest.mutators=STRONGER
```

#### With Increased Threads (Faster)

```bash
# Use more threads for faster execution
./gradlew pitest -Ppitest.threads=4 -Ppitest.mutators=STRONGER
```

### 7. Analyze Results

Reports are generated in `build/reports/pitest/`:

```bash
# Open HTML report
open build/reports/pitest/index.html

# Or for specific module
open bitso-libs/payment-service/build/reports/pitest/index.html
```

#### Understanding the Report

| Status | Meaning | Action |
|--------|---------|--------|
| **Killed** | Test detected the mutation | Good - no action needed |
| **Survived** | Test missed the mutation | **Improve test assertions** |
| **No Coverage** | No test covers this code | Write new tests |
| **Timed Out** | Mutation caused infinite loop | Usually OK (mutation broke loop) |
| **Non Viable** | Invalid bytecode generated | Ignore - PIT issue |

#### Finding Surviving Mutants

The HTML report shows:

- Package-level mutation scores
- Class-level breakdown
- Line-by-line mutation details
- Which mutator created each mutation

**Focus on SURVIVED mutations** - these indicate weak tests.

### 8. Improve Tests to Kill Mutants

For each surviving mutant:

1. **Read the mutation description** - understand what changed
2. **Identify why the test passed** - usually missing assertion
3. **Add specific assertion** - verify the exact behavior

#### Example: Surviving Mutant Fix

**Original code:**

```java
public int calculateDiscount(int price, boolean isPremium) {
    if (isPremium) {
        return price * 10 / 100;  // 10% discount
    }
    return 0;
}
```

**Surviving mutant:** `replaced integer multiplication with division`

**Weak test (mutation survives):**

```groovy
def "should return discount for premium users"() {
    expect:
    calculator.calculateDiscount(100, true) > 0  // Too weak!
}
```

**Strong test (mutation killed):**

```groovy
def "should return 10% discount for premium users"() {
    expect:
    calculator.calculateDiscount(100, true) == 10  // Exact assertion
}
```

### 9. Verify Improvement

After improving tests, re-run mutation testing:

```bash
# Re-run at same level to verify
./gradlew pitest -Ppitest.mutators=STRONGER

# Compare mutation scores
# Target: > 80% mutation score for critical code
```

### 10. Commit with Emojis and Jira Key

```bash
# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

git add -A
git commit -m "test: [$JIRA_KEY] improve test quality via mutation testing

- Improved PaymentProcessor tests (mutation score: 65% -> 89%)
- Added specific assertions for discount calculations
- Fixed boundary condition tests for price validation

Mutation testing level: STANDARD (STRONGER mutators)
Surviving mutants fixed: 12

Generated with the Quality Agent by the /improve-test-quality-with-mutation-testing command."
```

### 11. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

# JIRA_KEY is the actual ticket key (e.g., PROJ-123)

gh pr create --draft \
    --title "test: [$JIRA_KEY] improve test quality via mutation testing" \
    --body "## AI-Assisted Test Quality Improvements

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Mutation Testing Results

| Module | Before | After | Improvement |
|--------|--------|-------|-------------|
| payment-service | 65% | 89% | +24% |

## Testing Level
STANDARD (STRONGER mutators)

## Changes Made
- Strengthened assertions in PaymentProcessorSpec
- Added boundary condition tests
- Fixed edge case coverage

## Surviving Mutants Fixed
12 total across 4 test files

## Validation
- [x] All tests pass locally
- [x] Mutation score improved
- [ ] CI passes

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /improve-test-quality-with-mutation-testing

Generated with the Quality Agent by the /improve-test-quality-with-mutation-testing command."
```

## Configuration Options

### pitest.gradle Configuration

The `gradle/pitest.gradle` template supports these properties via command line:

```bash
# Target specific classes
./gradlew pitest -Ppitest.targetClasses='com.bitso.myservice.*'

# Target specific tests
./gradlew pitest -Ppitest.targetTests='*MyServiceSpec,*MyServiceTest'

# Choose mutator group: DEFAULTS, STRONGER, or ALL
./gradlew pitest -Ppitest.mutators=STRONGER

# Increase threads for faster execution
./gradlew pitest -Ppitest.threads=8

# Exclude specific classes (no defaults - you choose what to exclude)
./gradlew pitest -Ppitest.excludedClasses='**/mapper/**,**/config/**,**/generated/**'

# Increase memory for large projects
./gradlew pitest -Ppitest.jvmArgs='-Xmx6g'

# Enable verbose logging
./gradlew pitest -Ppitest.verbose=true
```

The template is configured with:

- **No default exclusions** - You control what to exclude
- **High timeout** - 2 minutes base + 1.5x multiplier to support Testcontainers/Spring integration tests
- **4GB memory** - Sufficient for most projects with integration tests

### Exclusion Patterns (Optional)

Use exclusions when you want to skip generated or boilerplate code. Common patterns:

```bash
# Generated code
-Ppitest.excludedClasses='**/generated/**,**/gensrc/**'

# Protobuf
-Ppitest.excludedClasses='**/proto/**,**/*Proto,com.google.**'

# MapStruct mappers
-Ppitest.excludedClasses='**/mapper/**,**/*MapperImpl'

# Configuration classes
-Ppitest.excludedClasses='**/*Config,**/*Configuration,**/config/**'

# Spring application
-Ppitest.excludedClasses='**/*Application'

# DTOs
-Ppitest.excludedClasses='**/dto/**'

# JOOQ tables
-Ppitest.excludedClasses='**/tables/**'

# Combined (common set for most projects)
-Ppitest.excludedClasses='**/generated/**,**/proto/**,**/*MapperImpl,**/*Config,**/*Application,**/tables/**'
```

### CI Integration

For CI gates, set a mutation threshold:

```groovy
pitest {
    mutationThreshold = 80  // Fail if < 80% mutations killed
}
```

## Troubleshooting

### Tests fail during mutation testing

**Cause:** Tests may have order dependencies, rely on specific timing, or require external resources (databases, containers).

**Fix:**

1. Run tests in isolation: `./gradlew test --tests "*FailingTest"`
2. Check for shared mutable state
3. Add `@Isolated` annotation if using Spock
4. For Testcontainers: The default timeout (2 min base + 1.5x) should handle most cases
5. If tests still timeout, check container startup time

### Integration tests timeout

**Cause:** Testcontainers or Spring context startup takes too long.

**Note:** The default configuration already uses high timeouts (2 min + 1.5x multiplier) to support integration tests. If you still see timeouts:

**Fix:** Target specific faster tests first:

```bash
# Run on unit tests only (faster feedback)
./gradlew pitest -Ppitest.targetTests='*UnitSpec,*UnitTest'

# Or exclude slow integration tests
./gradlew pitest -Ppitest.targetTests='!*IntegrationSpec,!*IT'
```

### Out of memory errors

**Cause:** Too many mutations or large test suite.

**Fix:**

```bash
./gradlew pitest -Ppitest.jvmArgs='-Xmx6g'
```

Or target specific packages:

```bash
./gradlew pitest -Ppitest.targetClasses='com.bitso.critical.*'
```

### Mutation testing takes too long

**Cause:** Large codebase or COMPREHENSIVE level.

**Fix:**

1. Start with QUICK level
2. Target specific modules: `./gradlew :module:pitest`
3. Increase threads: `-Ppitest.threads=8`
4. Use history files for incremental analysis:

   ```groovy
   pitest {
       historyInputLocation = file("${buildDir}/pitest-history.bin")
       historyOutputLocation = file("${buildDir}/pitest-history.bin")
   }
   ```

### No mutations generated

**Cause:** Exclusion patterns too broad or no code in target packages.

**Fix:**

1. Verify target classes: `-Ppitest.verbose=true`
2. Check exclusion patterns in `pitest.gradle`
3. Ensure source files exist in expected packages

### JUnit 5 tests not detected

**Cause:** Missing `pitest-junit5-plugin`.

**Fix:** Ensure `pitest.gradle` includes:

```groovy
pitest {
    junit5PluginVersion = '1.2.3'
}
```

## Best Practices

1. **Start with QUICK level** - Get fast feedback before deep analysis
2. **Focus on critical code** - Business logic, calculations, validations
3. **Prioritize SURVIVED mutants** - These indicate real test weaknesses
4. **Use specific assertions** - `== 10` not `> 0`
5. **Test boundary conditions** - Off-by-one errors are common
6. **Run incrementally** - Use history files for faster re-runs
7. **Set reasonable thresholds** - 80% is a good target for critical code
8. **Don't chase 100%** - Some equivalent mutants can't be killed

## AI Agent Iteration Guidance

When an AI agent executes this command, follow this structured approach:

### 1. Gather Information

**Read the PIT HTML report** (located in `build/reports/pitest/index.html`):

- Parse the mutation results to identify **SURVIVED** mutants
- Extract: class name, line number, mutator type, mutation description
- Group mutations by class/method for efficient batch fixes

**Analyze patterns** in surviving mutants:

- Same mutator type appearing repeatedly → systemic test weakness
- Same class with multiple survivors → focus test improvement there
- Specific mutation types (e.g., `CONDITIONALS_BOUNDARY`) → indicates missing boundary tests

### 2. Prioritize Mutations to Address

**High Priority** (fix first):

- Business logic methods (calculations, validations, rules)
- Methods with multiple surviving mutants (indicates weak coverage)
- `CONDITIONALS_BOUNDARY` mutants (often indicate off-by-one bugs)
- `NEGATE_CONDITIONALS` mutants (missing negative case tests)

**Low Priority** (fix later or skip):

- Logging statements (`VOID_METHOD_CALLS` on loggers)
- Builder/DTO methods with trivial logic
- Framework callbacks that are hard to unit test

### 3. Improve Tests Effectively

**DO:**

- Add specific assertions that verify exact values, not just non-null/non-empty
- Test boundary conditions explicitly (`<=` vs `<`, `>=` vs `>`)
- Add negative test cases for each positive case
- Use parameterized tests to cover multiple boundary values efficiently
- Group related assertions in the same test method when testing one behavior

**DON'T:**

- Duplicate test logic across multiple test methods
- Create tests that only check for exceptions without verifying behavior
- Add assertions that don't actually exercise the mutated code path
- Create overly broad tests that test too many things at once

### 4. Test Improvement Patterns

#### Pattern A: Missing Boundary Condition

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

#### Pattern B: Missing Return Value Verification

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

#### Pattern C: Missing Negative Case

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

### 5. Verify Improvements

After improving tests:

1. **Run tests first:** `./gradlew test -x codeCoverageReport`
2. **Re-run mutation testing at same level:** `./gradlew pitest -Ppitest.mutators=<LEVEL>`
3. **Compare mutation scores:** Document before/after percentages
4. **Iterate if needed:** Focus on remaining high-priority survivors

### 6. Avoid Common Pitfalls

| Pitfall | Problem | Solution |
|---------|---------|----------|
| Testing implementation details | Tests break on refactor | Test observable behavior only |
| Over-mocking | Tests don't catch real bugs | Use real objects when possible |
| Duplicate test logic | Maintenance burden | Extract shared setup/helpers |
| Ignoring equivalent mutants | Wasted effort | Some mutants are semantically equivalent - skip them |
| Too many assertions per test | Hard to diagnose failures | One logical assertion per test |

## Next Steps

Would you like to run one of the related testing improvement commands?

- `/improve-test-setup` - Upgrade testing libraries and configure JaCoCo
- `/improve-test-coverage` - Write tests to improve coverage

## Related

- **Test Setup**: `java/commands/improve-test-setup.md` - Testing infrastructure setup
- **Test Coverage**: `java/commands/improve-test-coverage.md` - Write tests to improve coverage
- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **Required** - Ticket creation
- **PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md` - PR creation with emojis
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Spock/JUnit patterns
- **JaCoCo Coverage**: `java/rules/java-jacoco-coverage.md` - Line coverage (complementary)
- **PIT Template**: `java/templates/pitest.gradle` - Gradle configuration template
- **Readiness Script**: `java/scripts/check-pitest-readiness.sh` - Environment validation

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/improve-test-quality-with-mutation-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
