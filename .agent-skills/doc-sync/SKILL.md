<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-sync/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: doc-sync
description: >
  Validates documentation is synchronized with code changes. Integrates with RFC-37
  doclinter for structure validation and pre-push hooks to enforce documentation
  updates. Use after making significant code changes or when documentation gaps exist.
compatibility: Works with any codebase; enhanced features with bitso-documentation-linter
metadata:
  version: "1.0"
---

# Doc Sync

This skill ensures documentation stays synchronized with code changes. It provides validation tools, pre-push checks, and integration with RFC-37 documentation standards.

## When to use this skill

- After making code changes that affect architecture or APIs
- When documentation may be out of sync with code
- During code review to verify documentation completeness
- To check for broken links in documentation
- When setting up documentation validation in a repository

## Quick Start

### 1. Check Documentation Status

```bash
# Via skills CLI
node .scripts/skills-cli.js doc-sync validate

# Or directly
node -e "import('./.scripts/lib/skills/doc-sync.js').then(m => m.validateDocs('.'))"
```

### 2. Pre-Push Hook Integration

Add documentation validation to your pre-push hook:

```bash
# In .git-hooks/pre-push or hooks-checks.js
# Checks if docs need updating when architecture files change
```

## Documentation Validation Checks

| Check | What It Does | Severity |
|-------|-------------|----------|
| **README presence** | Verifies README.md exists | Error |
| **Broken links** | Checks internal markdown links | Warning |
| **API docs** | Looks for generated API documentation | Info |
| **Freshness** | Compares doc timestamps to code | Warning |
| **RFC-37 structure** | Validates `docs/` folder structure | Error |

## Integration with RFC-37

For repositories following RFC-37 documentation standards:

```bash
# Install the linter
brew tap bitsoex/homebrew-bitso
brew install bitso-documentation-linter

# Run RFC-37 validation
doclinter --repo-path . --verbose

# Preview Confluence structure
doclinter tree --repo-path .
```

See the [rfc-37-documentation skill](../rfc-37-documentation/SKILL.md) for full RFC-37 compliance.

## Pre-Push Documentation Check

The system can verify documentation is updated when significant files change:

### Architecture Files That Trigger Doc Checks

```javascript
const ARCHITECTURE_FILES = [
  'technology-hierarchy.json',
  'repo-overrides.json',
  'managed-paths.json',
  '.scripts/convert-rules.js',
  '.scripts/targeting.js',
  '.scripts/safe-sync.js',
  '.scripts/ci-distribute.js',
  '.github/workflows/ci.yaml'
];
```

### Expected Documentation Updates

When architecture files change, update one of:

- `docs/ai-ide-management/concepts/architecture.md`
- `docs/ai-ide-management/how-tos/targeting-and-inheritance.md`
- `docs/ai-ide-management/overview.md`
- `README.md`

### Skipping the Check

```bash
# For a single commit (when docs truly aren't needed)
AI_AGENTS_SKIP_DOCS_CHECK=1 git commit -m "chore: minor config tweak"

# Or document why in commit message
git commit -m "fix: typo in config

No docs update needed - cosmetic change only"
```

## Available Functions

The doc-sync module exports these functions:

```javascript
import { 
  validateDocs,      // Run all documentation checks
  checkReadme,       // Verify README presence
  checkBrokenLinks,  // Find broken internal links
  checkApiDocs,      // Look for API documentation
  checkFreshness,    // Compare doc/code timestamps
  DOC_TOOLS          // Recommended tools by project type
} from './.scripts/lib/skills/doc-sync.js';
```

### Example Usage

```javascript
import { validateDocs } from './.scripts/lib/skills/doc-sync.js';

const result = await validateDocs('.', { quiet: false });

console.log('Passed:', result.passed);
console.log('Issues:', result.issues);
console.log('Warnings:', result.warnings);
console.log('Recommendations:', result.recommendations);
```

## Documentation Tools by Project Type

| Project | Tools | Commands |
|---------|-------|----------|
| **Java/Gradle** | Javadoc, Checkstyle | `./gradlew javadoc` |
| **Node.js** | TypeDoc, JSDoc, markdownlint | `npx typedoc`, `npx markdownlint "**/*.md"` |
| **Python** | Sphinx, pydocstyle, MkDocs | `sphinx-build`, `pydocstyle` |
| **Go** | godoc, go doc | `go doc -all` |

## Best Practices

### 1. Document As You Code

- Update docs in the same PR as code changes
- Use the pre-push hook to catch missing updates
- Keep README.md as the single source of truth

### 2. Structure Documentation

Follow RFC-37 structure for consistency:

```
docs/
├── decisions/          # ADRs (Architecture Decision Records)
├── how-tos/           # Step-by-step guides
├── runbooks/          # Operational procedures
└── {service}/         # Service-specific docs
    ├── concepts/      # Architecture, design
    └── getting-started/
```

### 3. Link Related Docs

Use relative links between documentation files. For example, link from one reference file to another using relative paths like `./other-file.md` or `../other-folder/file.md`.

### 4. Keep Docs Fresh

- Run freshness checks periodically
- Update docs when APIs change
- Review docs during code reviews

## References

| Reference | Description |
|-----------|-------------|
| [references/java/javadoc-patterns.md](references/java/javadoc-patterns.md) | Java documentation patterns |
| [references/typescript/jsdoc-patterns.md](references/typescript/jsdoc-patterns.md) | TypeScript documentation patterns |
| [references/python/docstring-patterns.md](references/python/docstring-patterns.md) | Python documentation patterns |
| [references/go/godoc-patterns.md](references/go/godoc-patterns.md) | Go documentation patterns |

## Related Skills

| Skill | Purpose |
|-------|---------|
| [rfc-37-documentation](../rfc-37-documentation/SKILL.md) | RFC-37 structure and Confluence mirroring |
| [quality-gateway](../quality-gateway/SKILL.md) | Quality gate orchestration |
| [git-hooks](../git-hooks/SKILL.md) | Pre-commit/pre-push hook setup |

## Documentation in This Repository

For ai-code-instructions documentation, see `docs/ai-ide-management/`:

- `catalogues/` - Catalogues of rules, commands, MCP, skills
- `concepts/` - Architecture and design concepts
- `how-tos/` - Step-by-step guides
- `mcp/` - MCP configuration docs
- `overview.md` - Main entry point
