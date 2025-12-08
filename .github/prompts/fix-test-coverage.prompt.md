# Fix test coverage issues in Java projects - DO NOT use -x codeCoverageReport

> Fix test coverage issues in Java projects - DO NOT use -x codeCoverageReport

# 🤖 🧪 Fix Test Coverage

**IMPORTANT**: This is the ONE command where coverage reports are essential. **DO NOT use `-x codeCoverageReport`** in this command - coverage is the goal.

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first (Step 1), then create if none found**
- **JaCoCo plugin in all modules** - See `java/rules/java-jacoco-coverage.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Note: This command is the exception to `-x codeCoverageReport`

## Prerequisites

1. **JaCoCo plugin in all modules** - See `java/rules/java-jacoco-coverage.md`
2. **Gradle 8.14+** - Check with `./gradlew --version`
3. **Module has tests** - At least one test file in `src/test`

## Workflow

### 1. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, create a Jira ticket for tracking:

Use `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 🧪 Improve test coverage for [module/class] in [repo-name]`
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Description**: Include current coverage % and target

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 2. Create Branch with Jira Key

```bash
JIRA_KEY="EN-XX"  # From step 1
MODULE="payment-service"  # Target module

git checkout -b test/${JIRA_KEY}-coverage-${MODULE}
```

### 3. Verify JaCoCo Configuration (Pre-flight Check)

**IMPORTANT**: Run these checks BEFORE executing any test commands to ensure proper coverage reporting.

#### Check 1: JaCoCo Plugin Applied

```bash
# Verify jacoco tasks exist
./gradlew tasks --all | grep -i jacoco

# Expected output should include:
# - jacocoTestReport
# - jacocoTestCoverageVerification
```

If missing, JaCoCo plugin is not applied. See `java/rules/java-jacoco-coverage.md` for setup.

#### Check 2: Verify Report Formats Enabled

```bash
# Check that all three formats are enabled in gradle/jacoco.gradle
grep -A10 "reports {" gradle/jacoco.gradle | grep -E "(xml|html|csv).required"

# Expected (all three):
# xml.required = true (or xml.required.set(true))
# html.required = true (or html.required.set(true))
# csv.required = true (or csv.required.set(true))
```

If any missing, update `gradle/jacoco.gradle`:

```groovy
jacocoTestReport {
    reports {
        xml.required.set(true)   // For CI/CD tools
        html.required.set(true)  // For line-level analysis
        csv.required.set(true)   // For class-level grep
    }
}
```

#### Check 3: Verify classDumpDir Configuration

```bash
# Check classDumpDir is set (prevents coverage issues)
grep -i "classDumpDir" gradle/jacoco.gradle

# Expected:
# classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
```

If missing, add to `gradle/jacoco.gradle`:

```groovy
test {
    jacoco {
        enabled = true
        destinationFile = layout.buildDirectory.file("jacoco/test.exec").get().asFile
        classDumpDir = layout.buildDirectory.dir("jacoco/classpathdumps").get().asFile
    }
}
```

> ✓ Only proceed to next steps after all checks pass

### 4. Check for Existing Coverage Reports

Before running tests, check if coverage reports already exist:

```bash
# Find existing CSV reports (from previous test runs)
find . -path "*/build/reports/jacoco/test/jacocoTestReport.csv" 2>/dev/null

# Check report age (if > 1 day old, regenerate)
find . -path "*/build/reports/jacoco/test/jacocoTestReport.csv" -mtime +1 2>/dev/null
```

If recent reports exist, skip to step 6. Otherwise, generate fresh reports.

### 5. Generate Coverage Reports

**⚠️ DO NOT use `-x codeCoverageReport` here - coverage is the goal!**

Run tests and generate JaCoCo reports for target module(s):

```bash
# Single module (INCLUDE coverage report - this is the goal!)
./gradlew :module-name:clean :module-name:test :module-name:jacocoTestReport 2>&1 | tee /tmp/coverage.log

# All modules (slower but comprehensive)
./gradlew clean test jacocoTestReport 2>&1 | tee /tmp/coverage.log

# Check for errors
echo $?  # 0 = success, non-zero = failures

# On failure, check the log
grep -A 10 "FAILED" /tmp/coverage.log
```

**Verify reports generated:**

```bash
ls -lh bitso-libs/module-name/build/reports/jacoco/test/
# Expected: jacocoTestReport.csv, jacocoTestReport.xml, html/
```

### 6. Identify Low Coverage Classes (CSV Analysis)

Use CSV report to find classes needing coverage improvements:

**Find classes with < 80% line coverage:**

```bash
MODULE_CSV="bitso-libs/module-name/build/reports/jacoco/test/jacocoTestReport.csv"

awk -F, 'NR>1 && ($8+$9)>0 {
  cov=$9/($8+$9)*100
  if(cov<80) printf "%-60s %5.1f%%\n", $2"."$3, cov
}' "$MODULE_CSV" | sort -k2 -n
```

**Find classes with 0% coverage:**

```bash
awk -F, 'NR>1 && $9==0 && ($8+$9)>0 {print $2"."$3}' "$MODULE_CSV"
```

**Find classes with uncovered branches:**

```bash
awk -F, 'NR>1 && $6>0 {
  printf "%-60s %3d uncovered branches\n", $2"."$3, $6
}' "$MODULE_CSV" | sort -k2 -nr
```

**CSV Format Reference** (from `java-jacoco-coverage.md`):

```text
GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
```

Columns: $1=GROUP, $2=PACKAGE, $3=CLASS, $4-$5=INSTRUCTION, $6-$7=BRANCH, $8-$9=LINE, $10-$11=COMPLEXITY, $12-$13=METHOD

### 7. Identify Uncovered Lines (HTML Analysis)

After identifying low-coverage classes from CSV, use HTML reports to find exact uncovered lines.

**Find uncovered/partially covered lines in a specific class:**

```bash
# Set target class (from CSV analysis above)
TARGET_CLASS="ErrorCodeMapper"

# Find HTML source file using portable find syntax (no bash-specific ** globstar)
HTML_FILE=$(find . -path "*/build/reports/jacoco/test/html/*" -name "${TARGET_CLASS}.java.html" 2>/dev/null | head -1)

if [ -z "$HTML_FILE" ]; then
  echo "Error: HTML report not found for $TARGET_CLASS"
  exit 1
fi

# Extract uncovered (nc) and partially covered (pc) line numbers
echo "=== Uncovered/Partially Covered Lines ==="
grep -E '<span class="(nc|pc)" id="L[0-9]+">' "$HTML_FILE" | \
  sed -E 's/.*id="L([0-9]+)".*/\1/' | \
  sort -n
```

**Show uncovered lines with source code:**

```bash
# Get uncovered line numbers
UNCOVERED_LINES=$(grep -E '<span class="nc" id="L[0-9]+">' "$HTML_FILE" | \
  sed -E 's/.*id="L([0-9]+)".*/\1/')

if [ -n "$UNCOVERED_LINES" ]; then
  # Find source file
  SOURCE_FILE=$(find . -path "*/src/main/**/${TARGET_CLASS}.java" 2>/dev/null | head -1)
  
  if [ -n "$SOURCE_FILE" ]; then
    echo "=== Uncovered Lines in $TARGET_CLASS ==="
    for line in $UNCOVERED_LINES; do
      echo "Line $line: $(sed -n "${line}p" "$SOURCE_FILE" | xargs)"
    done
  fi
else
  echo "✓ All lines covered!"
fi
```

**Example output:**

```text
=== Uncovered Lines in ErrorCodeMapper ===
Line 48: case "invalid-email-params" -> MakerCheckerErrorCodes.MAKER_CHECKER_ERROR_CODES_INVALID_EMAIL_PARAMS;
```

**HTML Coverage Classes:**

- `<span class="fc"` - **Fully covered** line (green)
- `<span class="pc"` - **Partially covered** line (yellow, branch partially covered)
- `<span class="nc"` - **Not covered** line (red)

### 8. Identify Tests for Target Class

To run only relevant tests when improving coverage, identify which test files cover the target class:

#### Strategy 1: Convention-based (fastest)

```bash
TARGET_CLASS="com.bitso.security.MyService"
CLASS_NAME=$(echo "$TARGET_CLASS" | awk -F. '{print $NF}')

# Find test files with matching name
find . -path "*/src/test/**/${CLASS_NAME}*" \( -name "*.java" -o -name "*.groovy" \)

# Common patterns:
# - MyServiceTest.java
# - MyServiceSpec.groovy (Spock)
# - MyServiceIntegrationTest.java
```

#### Strategy 2: Search test source code for class references

```bash
TARGET_CLASS="com.bitso.security.MyService"
grep -r "import.*${TARGET_CLASS}" --include="*Test.java" --include="*Spec.groovy" bitso-libs/module-name/src/test/
```

#### Strategy 3: Run specific test and check coverage

```bash
# Run single test class (INCLUDE coverage report!)
./gradlew :module-name:test --tests "*MyServiceSpec" :module-name:jacocoTestReport 2>&1 | tee /tmp/test-coverage.log

# Check if target class coverage increased in CSV
grep "MyService" bitso-libs/module-name/build/reports/jacoco/test/jacocoTestReport.csv
```

### 9. Write Tests to Improve Coverage

Focus on high-value coverage gaps:

**Priority order:**

| Priority | Coverage Type | Example | Why |
|----------|--------------|---------|-----|
| High | Integration | Database queries, API endpoints | Real behavior, catches integration bugs |
| High | Branch coverage | Conditional logic, error handling | Catches edge cases |
| Medium | Exception paths | Error cases, validation | Ensures graceful failures |
| Low | Line coverage | Simple getters, obvious code | Low bug risk |

**Decision matrix:**

- **Complex logic + low coverage** → Write integration test (Spock preferred)
- **Simple util + 0% coverage** → Write unit test (JUnit acceptable)
- **Error handling untested** → Add negative test cases

#### Example: Add test for uncovered branch

```groovy
// Target class has uncovered branch: discount validation
def "should reject negative discount"() {
  given:
  def calculator = new PricingCalculator()
  
  when:
  calculator.calculateTotal(price: 100.0, discount: -10)
  
  then:
  thrown(IllegalArgumentException)
}
```

Reference `java/rules/java-testing-guidelines.md` for test patterns and best practices.

### 10. Verify Coverage Increased

Run only the relevant tests and check coverage delta:

```bash
# Save baseline
OLD_COV=$(awk -F, -v cls="MyService" '$3==cls {print $9/($8+$9)*100}' bitso-libs/module-name/build/reports/jacoco/test/jacocoTestReport.csv)

# Run specific test(s) that cover the class (INCLUDE coverage report!)
./gradlew :module-name:test --tests "*MyServiceSpec" :module-name:jacocoTestReport 2>&1 | tee /tmp/coverage-verify.log

# Check new coverage
NEW_COV=$(awk -F, -v cls="MyService" '$3==cls {print $9/($8+$9)*100}' bitso-libs/module-name/build/reports/jacoco/test/jacocoTestReport.csv)

echo "Coverage change: $OLD_COV% → $NEW_COV%"
```

**Manual verification:**

```bash
# Open HTML report for visual inspection
open bitso-libs/module-name/build/reports/jacoco/test/html/index.html
```

**Target:** 82% line coverage (minimum threshold). Focus on meaningful tests, not coverage chasing.

### 11. Commit with Emojis and Jira Key

```bash
JIRA_KEY="EN-XX"

git add -A
git commit -m "🤖 🧪 test: [$JIRA_KEY] improve coverage for MyService

- Added 3 test cases for negative discount edge cases
- Increased MyService line coverage from 65% to 81%
- Covers branch: discount < 0 validation

Generated with the Quality Agent by the /fix-test-coverage command."
```

### 12. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

JIRA_KEY="EN-XX"

gh pr create --draft \
    --title "🤖 🧪 [$JIRA_KEY] test: improve coverage for MyService" \
    --body "## 🤖 AI-Assisted Test Coverage Improvements

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Coverage Changes

| Class | Before | After | Delta |
|-------|--------|-------|-------|
| MyService | 65% | 81% | +16% |

## Tests Added
- testNegativeDiscount
- testZeroDiscount
- testMaxDiscount

## Validation
- [x] Tests pass locally
- [x] Coverage meets 82% threshold
- [ ] CI passes

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /fix-test-coverage

Generated with the Quality Agent by the /fix-test-coverage command.

## References
- JaCoCo report: [link]"
```

### 13. Verify Quality Gate

After commit, ensure coverage meets project threshold:

```bash
# Run coverage verification (enforces 82% minimum) - INCLUDE coverage!
./gradlew :module-name:jacocoTestCoverageVerification 2>&1 | tee /tmp/coverage-verify.log

# Check exit code
if [ $? -eq 0 ]; then
  echo "✓ Coverage meets 82% threshold"
else
  echo "✗ Coverage below threshold - add more tests"
  grep -A 5 "FAILED" /tmp/coverage-verify.log
fi
```

## Helper Scripts

### Script: Find Low Coverage Classes Across All Modules

Save as `scripts/find-low-coverage.sh`:

```bash
#!/usr/bin/env bash
# Find classes with <80% line coverage across all modules

THRESHOLD=${1:-80}

find . -path "*/build/reports/jacoco/test/jacocoTestReport.csv" 2>/dev/null | while read csv; do
  MODULE=$(echo "$csv" | awk -F/ '{print $(NF-6)"/"$(NF-5)}')
  echo "=== $MODULE ==="
  
  awk -F, -v threshold="$THRESHOLD" '
    NR>1 && ($8+$9)>0 {
      cov=$9/($8+$9)*100
      if(cov<threshold) printf "  %-60s %5.1f%%\n", $2"."$3, cov
    }' "$csv" | sort -k2 -n
done
```

Usage:

```bash
chmod +x scripts/find-low-coverage.sh
./scripts/find-low-coverage.sh 70  # Find classes < 70%
```

### Script: Coverage Delta (Before/After)

Save as `scripts/coverage-delta.sh`:

```bash
#!/usr/bin/env bash
# Compare coverage before and after test changes

MODULE=$1
CSV="bitso-libs/$MODULE/build/reports/jacoco/test/jacocoTestReport.csv"

if [ ! -f "$CSV" ]; then
  echo "Error: No coverage report for $MODULE"
  exit 1
fi

awk -F, 'NR>1 && ($8+$9)>0 {
  cov=$9/($8+$9)*100
  printf "%-60s %5.1f%% (%d/%d lines)\n", $2"."$3, cov, $9, $8+$9
}' "$CSV" | sort -t: -k2 -n
```

Usage:

```bash
# Before changes
./scripts/coverage-delta.sh my-module > /tmp/before.txt

# After writing tests (INCLUDE coverage report!)
./gradlew :my-module:test :my-module:jacocoTestReport
./scripts/coverage-delta.sh my-module > /tmp/after.txt

# Compare
diff /tmp/before.txt /tmp/after.txt
```

## Best Practices

- **Create Jira ticket FIRST** before any code changes
- **DO NOT use `-x codeCoverageReport`** - coverage is the goal of this command
- **Incremental approach**: Fix 1-3 classes at a time, commit, repeat
- **Test quality over quantity**: Meaningful integration > 100% line coverage
- **Use CSV for automation**: Grep/awk for scripting, HTML for manual inspection
- **Focus on risk**: High-complexity classes need coverage more than simple DTOs
- **Convention over search**: Follow test naming patterns (\*Test.java, \*Spec.groovy)

## Common Issues

### No CSV report generated

**Cause:** CSV not enabled in `gradle/jacoco.gradle`

**Fix:**

```groovy
reports {
    xml.required = true
    html.required = true
    csv.required = true  // Add this line
}
```

### Module shows 0% despite having tests

**Cause:** JaCoCo plugin not applied or tests not running

**Fix:**

1. Verify plugin: `./gradlew :module-name:tasks | grep jacoco`
2. Run tests manually: `./gradlew :module-name:clean :module-name:test --info`
3. Check test results: `ls bitso-libs/module-name/build/test-results/test/`
4. Verify jacoco.gradle imported in root build.gradle

### Coverage not increasing despite new tests

**Cause:** Test not covering target class, or wrong test executed

**Fix:**

1. Verify test runs: `./gradlew :module-name:test --tests "*MyServiceSpec" --info | grep "MyServiceSpec"`
2. Check test imports target class: `grep "import.*MyService" src/test/**/MyServiceSpec.groovy`
3. Run with JaCoCo debug: `./gradlew :module-name:test --debug 2>&1 | grep -i jacoco`

### CSV parsing fails with special characters

**Cause:** Class/package names with commas or special chars (rare)

**Fix:**

Use XML parsing instead for those specific cases:

```bash
# Example: Extract coverage from XML using xmllint
xmllint --xpath '//class[@name="com/bitso/MyService"]//counter[@type="LINE"]/@covered' jacocoTestReport.xml
```

## Related

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **Required** - Ticket creation and emoji conventions
- **PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md` - PR creation with emojis
- **JaCoCo Code Coverage**: `java/rules/java-jacoco-coverage.md` - Complete JaCoCo configuration and CSV format reference
- **Testing Guidelines**: `java/rules/java-testing-guidelines.md` - Test patterns and best practices (Spock, JUnit, Testcontainers)
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Note: This command is the exception to `-x codeCoverageReport`
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md` - Build configuration standards
