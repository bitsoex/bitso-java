# Update documentation to match code changes

> Update documentation to match code changes

# Sync Docs

Update documentation to reflect recent code changes.

## Skill Location

Full documentation: `.claude/skills/doc-sync/`

## Quick Start

1. Identify what code changed: `git diff --name-only HEAD~5`
2. Apply patterns from the skill:
   - `.claude/skills/doc-sync/SKILL.md` - Main instructions
   - `.claude/skills/doc-sync/references/java/` - Java patterns
   - `.claude/skills/doc-sync/references/typescript/` - TypeScript patterns
   - `.claude/skills/doc-sync/references/python/` - Python patterns
3. Update documentation in same PR as code changes
4. Run freshness checks if available

## Key Actions

- [ ] Verify public APIs have documentation
- [ ] Ensure README reflects current functionality  
- [ ] Update API docs if interfaces changed
- [ ] Document breaking changes
- [ ] Add/update code examples

## Related

- **Quality Gate**: `/quality-check` command
- **Test Generation**: `/add-tests` command
- **RFC-37**: `.claude/skills/rfc-37-documentation/SKILL.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/sync-docs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
