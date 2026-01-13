<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coding-standards/references/documentation-standards.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

# Documentation Standards

> **Work in Progress**: Full content tracked in EN-111. Use the interim guidance below.

## Interim Guidance

### Code Comments

- Explain **why**, not **what** - the code shows what
- Keep comments up-to-date with code changes
- Use TODO/FIXME with ticket references: `TODO(EN-123): description`
  - Java/TypeScript/JS: `// TODO(EN-123): description`
  - Python/Shell: `# TODO(EN-123): description`

### Documentation Requirements

| Document | When Required |
|----------|---------------|
| README.md | Every repository root |
| API docs | Public/shared APIs |
| Architecture docs | Complex systems |
| CHANGELOG.md | Published packages |

### Language-Specific Patterns

| Language | Documentation Style | Reference |
|----------|---------------------|-----------|
| Java | Javadoc | `global/skills/doc-sync/references/java/javadoc-patterns.md` |
| TypeScript | JSDoc/TSDoc | `global/skills/doc-sync/references/typescript/jsdoc-patterns.md` |
| Python | Docstrings | `global/skills/doc-sync/references/python/docstring-patterns.md` |
| Go | Godoc | `global/skills/doc-sync/references/go/godoc-patterns.md` |

### README Checklist

- [ ] Project description
- [ ] Prerequisites and setup
- [ ] Development workflow
- [ ] Testing instructions
- [ ] Deployment guide (if applicable)

## TODO

- [ ] Migrate full content from existing documentation rules
- [ ] Add template examples
