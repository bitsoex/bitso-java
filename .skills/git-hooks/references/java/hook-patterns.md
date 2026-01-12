# Java Hook Patterns

Pre-commit and pre-push hook patterns for Java projects using Gradle or Maven.

## Pre-Commit Hook (Gradle)

```bash
#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔍 Java Pre-Commit Checks${NC}"

# 1. Compile check (fast)
echo -e "${YELLOW}   • Compiling...${NC}"
if ! ./gradlew compileJava compileTestJava --quiet; then
  echo -e "${RED}   ✗ Compilation failed${NC}"
  exit 1
fi
echo -e "${GREEN}   ✓ Compilation passed${NC}"

# 2. Checkstyle/Spotless formatting check
echo -e "${YELLOW}   • Checking code style...${NC}"
if ! ./gradlew spotlessCheck --quiet 2>/dev/null; then
  echo -e "${YELLOW}   ⚠ Running spotlessApply...${NC}"
  ./gradlew spotlessApply --quiet
  
  # Stage only formatted Java files that were already staged
  git diff --name-only --cached --diff-filter=M | grep '\.java$' | xargs -r git add
  echo -e "${GREEN}   ✓ Code formatted and staged${NC}"
fi

echo -e "${GREEN}✅ Pre-commit checks passed${NC}"
```

## Pre-Push Hook (Gradle)

```bash
#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔍 Java Pre-Push Checks${NC}"

# 1. Full build
echo -e "${YELLOW}   • Building...${NC}"
if ! ./gradlew build -x test --quiet; then
  echo -e "${RED}   ✗ Build failed${NC}"
  exit 1
fi

# 2. All tests
echo -e "${YELLOW}   • Running all tests...${NC}"
if ! ./gradlew test; then
  echo -e "${RED}   ✗ Tests failed${NC}"
  exit 1
fi

# 3. Code coverage check (optional - warns but doesn't block)
echo -e "${YELLOW}   • Checking coverage...${NC}"
if ! ./gradlew jacocoTestCoverageVerification --quiet 2>/dev/null; then
  echo -e "${YELLOW}   ⚠ Coverage thresholds not met - review before pushing${NC}"
fi

echo -e "${GREEN}✅ Pre-push checks passed${NC}"
```

## Pre-Commit Hook (Maven)

```bash
#!/bin/bash
set -e

echo "🔍 Java Pre-Commit Checks (Maven)"

# Compile check
if ! mvn compile test-compile -q; then
  echo "✗ Compilation failed"
  exit 1
fi

# Spotless/Checkstyle
if ! mvn spotless:check -q 2>/dev/null; then
  mvn spotless:apply -q
  git diff --name-only --cached --diff-filter=M | grep '\.java$' | xargs -r git add
fi

echo "✅ Pre-commit checks passed"
```

## Gradle Tasks Reference

| Task | Purpose | Speed |
|------|---------|-------|
| `compileJava` | Compile main sources | Fast |
| `compileTestJava` | Compile test sources | Fast |
| `spotlessCheck` | Check formatting | Fast |
| `spotlessApply` | Auto-format code | Fast |
| `test` | Unit tests | Medium |
| `jacocoTestCoverageVerification` | Coverage threshold | Medium |

## Spotless Configuration

```groovy
plugins {
    id 'com.diffplug.spotless' version '8.1.0'
}

spotless {
    java {
        target 'src/**/*.java'
        googleJavaFormat()
        removeUnusedImports()
    }
}
```
