---
name: doc-sync
description: >
  Validates documentation is synchronized with code changes. Checks for README presence,
  API documentation, code comments, and documentation freshness. Use after making code
  changes or when documentation updates are needed.
compatibility: Works with any codebase with Markdown documentation
metadata:
  version: "0.1"
---

# Doc Sync

> **Placeholder**: This skill will be fully developed during the content migration phase.

## When to use this skill

- After making code changes that affect public APIs
- When documentation may be out of sync with code
- During code review to verify documentation completeness

## Instructions

See the technology-specific references in the `references/` folder for documentation patterns.

## References

| Technology | Reference |
|------------|-----------|
| Java | `references/java/javadoc-patterns.md` |
| TypeScript | `references/typescript/jsdoc-patterns.md` |
| Python | `references/python/docstring-patterns.md` |
| Go | `references/go/godoc-patterns.md` |

## TODO

- [ ] Define documentation validation checks
- [ ] Integrate with quality-gateway lifecycle
- [ ] Add technology-specific validation rules
