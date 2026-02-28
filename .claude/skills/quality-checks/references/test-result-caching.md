# Test Result Caching

This reference explains how to save and reuse test results to avoid running tests multiple times.

## Contents

- [Overview](#overview)
- [Naming Convention](#naming-convention)
- [Helper Functions](#helper-functions)
- [Usage by Tool](#usage-by-tool)
- [In CI (GitHub Actions)](#in-ci-github-actions)
- [Environment Variable](#environment-variable)
- [In JavaScript Scripts](#in-javascript-scripts)
- [Directory Structure Example](#directory-structure-example)
- [Best Practices](#best-practices)
- [Cleaning Up Old Files](#cleaning-up-old-files)
- [Related](#related)

---
## Overview

Running tests can be expensive (time, resources). Instead of running tests multiple times just to grep different parts of the output, save results to `.tmp/` and grep from the file.

## Naming Convention

Test results use this naming pattern for **easy identification**:

```text
.tmp/{tool}-{type}-{context}-{datetime}.txt
```

Components:
- `{tool}` - The test runner (pnpm, gradle, pytest, go)
- `{type}` - The type of output (test, lint, coverage)
- `{context}` - PR number or branch name (for traceability)
- `{datetime}` - Timestamp in YYYYMMDD-HHMMSS format

**Examples:**
- `.tmp/pnpm-test-pr83-20260113-181500.txt`
- `.tmp/gradle-test-main-20260113-120000.txt`
- `.tmp/pytest-coverage-pr45-20260113-093000.txt`

## Helper Functions

### Get Current Context

```bash
# Get PR number (in CI) or branch name (locally)
get_context() {
  if [ -n "$GITHUB_HEAD_REF" ]; then
    # In GitHub Actions PR
    echo "pr${PR_NUMBER:-unknown}"
  elif [ -n "$CI" ]; then
    # In CI but not a PR
    echo "${GITHUB_REF_NAME:-main}"
  else
    # Local development - use branch name
    git rev-parse --abbrev-ref HEAD 2>/dev/null | tr '/' '-' || echo "local"
  fi
}
```

### Generate Output Path

```bash
# Generate standardized output path
get_test_output_path() {
  local tool="${1:-pnpm}"
  local type="${2:-test}"
  local context=$(get_context)
  local timestamp=$(date +%Y%m%d-%H%M%S)
  echo ".tmp/${tool}-${type}-${context}-${timestamp}.txt"
}
```

## Usage by Tool

### Node.js (pnpm/npm)

```bash
# Get paths
CONTEXT=$(git rev-parse --abbrev-ref HEAD | tr '/' '-')
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
OUTPUT=".tmp/pnpm-test-${CONTEXT}-${TIMESTAMP}.txt"

# Run tests and save output
pnpm test 2>&1 | tee "$OUTPUT"

# Also update "latest" symlink for quick access
ln -sf "$(basename $OUTPUT)" .tmp/pnpm-test-latest.txt

# Then grep from file instead of running again
grep "FAIL" "$OUTPUT"
tail -20 "$OUTPUT"
```

### Gradle

```bash
CONTEXT=$(git rev-parse --abbrev-ref HEAD | tr '/' '-')
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
OUTPUT=".tmp/gradle-test-${CONTEXT}-${TIMESTAMP}.txt"

# Run tests and save output
./gradlew test 2>&1 | tee "$OUTPUT"
ln -sf "$(basename $OUTPUT)" .tmp/gradle-test-latest.txt

# Then grep from file
grep "FAILED" "$OUTPUT"
```

### Python

```bash
CONTEXT=$(git rev-parse --abbrev-ref HEAD | tr '/' '-')
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
OUTPUT=".tmp/pytest-test-${CONTEXT}-${TIMESTAMP}.txt"

# Run tests and save output
pytest 2>&1 | tee "$OUTPUT"
ln -sf "$(basename $OUTPUT)" .tmp/pytest-test-latest.txt

# Then grep from file
grep "FAILED" "$OUTPUT"
```

### Go

```bash
CONTEXT=$(git rev-parse --abbrev-ref HEAD | tr '/' '-')
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
OUTPUT=".tmp/go-test-${CONTEXT}-${TIMESTAMP}.txt"

# Run tests and save output
go test ./... 2>&1 | tee "$OUTPUT"
ln -sf "$(basename $OUTPUT)" .tmp/go-test-latest.txt

# Then grep from file
grep "FAIL" "$OUTPUT"
```

## In CI (GitHub Actions)

```yaml
- name: Run tests
  env:
    PR_NUMBER: ${{ github.event.pull_request.number }}
  run: |
    CONTEXT="pr${PR_NUMBER:-main}"
    TIMESTAMP=$(date +%Y%m%d-%H%M%S)
    OUTPUT=".tmp/pnpm-test-${CONTEXT}-${TIMESTAMP}.txt"

    pnpm test 2>&1 | tee "$OUTPUT"

    # Upload as artifact for later analysis
    echo "TEST_OUTPUT=$OUTPUT" >> $GITHUB_OUTPUT

- name: Upload test results
  uses: actions/upload-artifact@v4
  with:
    name: test-results
    path: .tmp/*-test-*.txt
```

## Environment Variable

Use `AI_AGENTS_TEST_OUTPUT_DIR` to customize the output directory:

```bash
export AI_AGENTS_TEST_OUTPUT_DIR=".tmp"
```

## In JavaScript Scripts

```javascript
import { execSync } from 'child_process';
import fs from 'fs';

function getContext() {
  // Try PR number from env
  if (process.env.PR_NUMBER) {
    return `pr${process.env.PR_NUMBER}`;
  }
  // Try branch name
  try {
    const branch = execSync('git rev-parse --abbrev-ref HEAD', { encoding: 'utf-8' }).trim();
    return branch.replace(/\//g, '-');
  } catch {
    return 'local';
  }
}

function runTests(tool = 'pnpm', testCmd = 'pnpm test') {
  const context = getContext();
  const now = new Date();
  const yyyymmdd = now.toISOString().slice(0, 10).replace(/-/g, '');
  const hhmmss = now.toISOString().slice(11, 19).replace(/:/g, '');
  const timestamp = `${yyyymmdd}-${hhmmss}`;
  const outputPath = `.tmp/${tool}-test-${context}-${timestamp}.txt`;
  const latestPath = `.tmp/${tool}-test-latest.txt`;

  try {
    const output = execSync(testCmd, { encoding: 'utf-8' });
    fs.writeFileSync(outputPath, output);
    fs.writeFileSync(latestPath, output);
    console.log(`Test output saved to ${outputPath}`);
    return { success: true, output, path: outputPath };
  } catch (error) {
    const output = error.stdout || error.message;
    fs.writeFileSync(outputPath, output);
    fs.writeFileSync(latestPath, output);
    console.log(`Test output (with failures) saved to ${outputPath}`);
    return { success: false, output, path: outputPath };
  }
}
```

## Directory Structure Example

```text
.tmp/
├── .gitkeep
│
├── # Node.js test results
├── pnpm-test-pr83-20260113-181500.txt
├── pnpm-test-pr83-20260113-182000.txt
├── pnpm-test-latest.txt → pnpm-test-pr83-20260113-182000.txt
│
├── # Gradle test results
├── gradle-test-feat-EN-115-20260113-140000.txt
├── gradle-test-latest.txt → gradle-test-feat-EN-115-20260113-140000.txt
│
├── # Coverage results
├── pnpm-coverage-pr83-20260113-181500.txt
├── coverage-output.txt
│
├── # Pre-commit/pre-push logs (already using this pattern)
├── pre-commit-20260113-181606.txt
└── pre-push-20260113-181616.txt
```

## Best Practices

1. **Include context** - Always include PR number or branch name
2. **Include timestamp** - For comparing multiple runs and auditing
3. **Create latest symlink** - Easy access to most recent run
4. **Use `tee`** - See output AND save it simultaneously
5. **Grep from files** - Don't re-run tests just to see different output
6. **Clean up old files** - They're git-ignored, periodically remove old ones

## Cleaning Up Old Files

```bash
# Remove test outputs older than 7 days
find .tmp -name "*-test-*.txt" -mtime +7 -delete

# Keep only last 10 of each type
ls -t .tmp/pnpm-test-*.txt | tail -n +11 | xargs rm -f
```

## Related

- [Quality Gateway SKILL.md](.claude/skills/quality-checks/SKILL.md)
- [Test Augmentation](.claude/skills/test-augmentation/SKILL.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/quality-checks/references/test-result-caching.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

