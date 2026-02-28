# Node.js/TypeScript Quality Gate Setup

Complete configuration for enabling mise and hk in Node.js/TypeScript projects.

## Prerequisites

- Node.js 22+ (preferably 24+) installed
- Package manager (npm, pnpm, or yarn)
- ESLint and/or Prettier configured (recommended)

## Complete hk.pkl Configuration

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

// Global settings
fail_fast = true
exclude = List("node_modules/", "dist/", "build/", "coverage/", "*.min.js", "*.bundle.js")

// Security linters
local securityLinters = new Mapping<String, Step> {
  ["no-commit-to-branch"] = Builtins.no_commit_to_branch
  ["detect-private-key"] = Builtins.detect_private_key
  ["check-merge-conflict"] = Builtins.check_merge_conflict
}

// Hygiene linters
local hygieneLinters = new Mapping<String, Step> {
  ["trailing-whitespace"] = Builtins.trailing_whitespace
  ["newlines"] = Builtins.newlines
  ["check-added-large-files"] = Builtins.check_added_large_files
}

// Node.js linters
local nodeLinters = new Mapping<String, Step> {
  ["eslint"] = Builtins.eslint
  ["prettier"] = Builtins.prettier
  ["tsc"] = Builtins.tsc
}

// Package linters
local packageLinters = new Mapping<String, Step> {
  ["sort-package-json"] = Builtins.sort_package_json
}

// Config linters
local configLinters = new Mapping<String, Step> {
  ["yamllint"] = Builtins.yamllint
  ["actionlint"] = Builtins.actionlint
}

hooks {
  ["pre-commit"] {
    fix = true  // Auto-fix issues
    stash = "git"  // Stash unstaged changes
    steps {
      // Security first
      ...securityLinters

      // Hygiene
      ...hygieneLinters

      // Node.js
      ...nodeLinters

      // Package
      ...packageLinters
    }
  }

  ["pre-push"] {
    steps {
      // Type checking
      ["tsc"] = Builtins.tsc

      // Tests
      ["test"] {
        glob = List("**/*.ts", "**/*.tsx", "**/*.js", "**/*.jsx")
        check = "npm test"
        workspace_indicator = "package.json"
      }

      // Test coverage
      ["test-coverage"] {
        glob = List("**/*.ts", "**/*.tsx", "**/*.js", "**/*.jsx")
        check = "npm run test:coverage"
        workspace_indicator = "package.json"
        profile = "slow"
      }
    }
  }

  ["ci"] {
    steps {
      ...securityLinters
      ...hygieneLinters
      ...nodeLinters
      ...packageLinters
      ...configLinters

      ["build"] {
        check = "npm run build"
        workspace_indicator = "package.json"
      }
    }
  }
}
```

## Complete mise.toml Configuration

```toml
min_version = "2026.2.5"

[env]
# hk integration
HK_MISE = "1"
HK_LOG = "warn"
HK_LOG_FILE = ".hk-logs/hk.log"

# Hook mode
BITSO_MISE_MODE = "full"

# Node options
NODE_OPTIONS = "--experimental-strip-types"

[tools]
node = "24"

[tasks]
# Linting
lint = "hk check"
"lint:fix" = "hk fix"

# npm tasks
test = "npm test"
"test:coverage" = "npm run test:coverage"
build = "npm run build"
typecheck = "npx tsc --noEmit"

# Hooks
"hook:precommit" = "hk run pre-commit"
"hook:prepush" = "hk run pre-push"
"hook:ci" = "hk run ci"
```

## ESLint Configuration

Recommended `.eslintrc.js`:

```javascript
module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  plugins: ['@typescript-eslint'],
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
  ],
  env: {
    node: true,
    es2022: true,
  },
  ignorePatterns: ['dist/', 'node_modules/', 'coverage/'],
};
```

## Prettier Configuration

Recommended `.prettierrc`:

```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "es5",
  "printWidth": 100,
  "tabWidth": 2
}
```

## TypeScript Configuration

Ensure `tsconfig.json` has:

```json
{
  "compilerOptions": {
    "strict": true,
    "noEmit": true,
    "skipLibCheck": true
  }
}
```

## Using Biome Instead of ESLint/Prettier

For projects using Biome:

```pkl
local nodeLinters = new Mapping<String, Step> {
  ["biome"] = Builtins.biome
  ["tsc"] = Builtins.tsc
}
```

## Monorepo Configuration

For monorepos with workspaces:

```pkl
["eslint"] = (Builtins.eslint) {
  workspace_indicator = "package.json"
  check = "eslint {{files}} --max-warnings 0"
}
```

## pnpm Configuration

For pnpm projects:

```toml
[tools]
node = "24"

[tasks]
test = "pnpm test"
build = "pnpm build"
```

## Common Issues

### ESLint Not Finding Config

```bash
# Ensure ESLint is installed
npm install -D eslint @typescript-eslint/parser @typescript-eslint/eslint-plugin
```

### TypeScript Errors in Tests

Add test files to tsconfig:

```json
{
  "include": ["src/**/*", "tests/**/*"]
}
```

### Slow Pre-commit

Use lint-staged for faster staged-only linting:

```pkl
["lint-staged"] {
  check = "npx lint-staged"
}
```

With `lint-staged.config.js`:

```javascript
module.exports = {
  '*.{js,jsx,ts,tsx}': ['eslint --fix', 'prettier --write'],
};
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/enable-quality-gate/references/nodejs/setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

