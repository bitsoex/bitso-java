# TypeScript Hook Patterns

Pre-commit and pre-push hook patterns for Node.js and TypeScript projects.

## Pre-Commit Hook

```bash
#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔍 TypeScript Pre-Commit Checks${NC}"

# 1. TypeScript type check
echo -e "${YELLOW}   • Type checking...${NC}"
if [ -f "tsconfig.json" ]; then
  if ! npx tsc --noEmit; then
    echo -e "${RED}   ✗ Type errors found${NC}"
    exit 1
  fi
  echo -e "${GREEN}   ✓ Types valid${NC}"
fi

# 2. ESLint
echo -e "${YELLOW}   • Linting...${NC}"
if ! npm run lint --silent 2>/dev/null; then
  echo -e "${RED}   ✗ Lint errors${NC}"
  exit 1
fi
echo -e "${GREEN}   ✓ Linting passed${NC}"

# 3. Prettier
echo -e "${YELLOW}   • Formatting...${NC}"
if ! npx prettier --check "src/**/*.{ts,tsx,js,jsx}" 2>/dev/null; then
  npx prettier --write "src/**/*.{ts,tsx,js,jsx}"
  git diff --name-only --cached -- 'src/' | grep -E '\.(ts|tsx|js|jsx)$' | xargs git add 2>/dev/null || true
  echo -e "${GREEN}   ✓ Formatted${NC}"
fi

# 4. Tests
echo -e "${YELLOW}   • Testing...${NC}"
if ! npm test --silent; then
  echo -e "${RED}   ✗ Tests failed${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Pre-commit checks passed${NC}"
```

## Pre-Push Hook

```bash
#!/bin/bash
set -e

echo "🔍 TypeScript Pre-Push Checks"

# Type check
npx tsc --noEmit

# Lint
npm run lint

# Tests with coverage
npm run test:coverage

# Build
npm run build --silent 2>/dev/null || true

echo "✅ Pre-push checks passed"
```

## Using lint-staged

```json
{
  "lint-staged": {
    "*.{ts,tsx}": ["eslint --fix", "prettier --write"],
    "*.{js,jsx}": ["eslint --fix", "prettier --write"],
    "*.{json,md}": ["prettier --write"]
  }
}
```

## npm Scripts

```json
{
  "scripts": {
    "lint": "eslint 'src/**/*.{ts,tsx}'",
    "lint:fix": "eslint 'src/**/*.{ts,tsx}' --fix",
    "format": "prettier --write 'src/**/*.{ts,tsx}'",
    "typecheck": "tsc --noEmit",
    "test": "jest",
    "test:coverage": "jest --coverage",
    "precommit": "npm run typecheck && npm run lint && npm test"
  }
}
```

## Performance Tips

- Use `--onlyChanged` for Jest to test only affected files
- Enable ESLint caching with `--cache`
- Use TypeScript incremental builds with `composite: true`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/git-hooks/references/typescript/hook-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

