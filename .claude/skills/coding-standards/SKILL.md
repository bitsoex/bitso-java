---
name: coding-standards
description: >
  Enforces consistent coding standards across the codebase including naming conventions,
  code organization, and documentation requirements. Use when writing new code, reviewing
  pull requests, or refactoring existing code.
compatibility: Works with any codebase
metadata:
  version: "1.0"
---

# Coding Standards

This skill provides guidance on maintaining consistent coding standards across your codebase.

## When to use this skill

- Writing new code (classes, functions, modules)
- Reviewing pull requests for standards compliance
- Refactoring existing code
- Setting up new projects
- Onboarding new team members

## Instructions

### Step 1: Assess Current State

Before applying standards, understand the current codebase:

1. Review existing naming conventions in the project
2. Check for existing style guides or linter configurations
3. Identify any project-specific conventions

### Step 2: Apply Naming Conventions

Use the naming conventions script to check for violations:

```bash
./scripts/check-naming-conventions.sh
```

For detailed naming rules, see [references/naming-conventions.md](references/naming-conventions.md).

### Step 3: Organize Code

Follow code organization principles:

1. Group related functionality together
2. Keep files focused on a single responsibility
3. Use consistent module/package structure

For detailed guidelines, see [references/code-organization.md](references/code-organization.md).

### Step 4: Add Documentation

Ensure proper documentation:

1. Add file headers where required
2. Document public APIs
3. Include usage examples for complex functions

Use the template at [assets/templates/file-header-template.txt](assets/templates/file-header-template.txt).

For documentation standards, see [references/documentation-standards.md](references/documentation-standards.md).

## Available Scripts

| Script | Description |
|--------|-------------|
| `scripts/check-naming-conventions.sh` | Validates naming conventions across the codebase |

## Reference Materials

| Document | Description |
|----------|-------------|
| [naming-conventions.md](references/naming-conventions.md) | Naming rules for variables, functions, classes |
| [code-organization.md](references/code-organization.md) | File and module organization guidelines |
| [documentation-standards.md](references/documentation-standards.md) | Documentation requirements and formats |

## Examples

### Example 1: Checking a Java Project

```bash
# Run naming convention check
./scripts/check-naming-conventions.sh src/main/java

# Expected output:
# Checking naming conventions in src/main/java...
# ✓ 42 files checked
# ✓ All naming conventions followed
```

### Example 2: Fixing Common Issues

When the script finds issues:

```
src/main/java/UserManager.java:15 - Variable 'Str' should be lowercase
src/main/java/utils/Helper.java:8 - Function 'GetData' should be camelCase
```

Fix by:

1. Rename `Str` to `str` or a more descriptive name
2. Rename `GetData` to `getData`
