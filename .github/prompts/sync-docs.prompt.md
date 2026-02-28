# Update documentation to match code changes

> Update documentation to match code changes

# Sync Docs

Update documentation to reflect recent code changes.

## Skill Location

Full documentation: [doc-validation-rfc-37](.claude/skills/doc-validation-rfc-37/SKILL.md)

## Quick Start

1. Identify what code changed: `git diff --name-only HEAD~5`
2. Apply patterns from the skill:
   - [doc-validation-rfc-37](.claude/skills/doc-validation-rfc-37/SKILL.md) - Main instructions
   - [references/java](.claude/skills/doc-validation-rfc-37/references/java) - Java patterns
   - [references/typescript](.claude/skills/doc-validation-rfc-37/references/typescript) - TypeScript patterns
   - [references/python](.claude/skills/doc-validation-rfc-37/references/python) - Python patterns
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
- **RFC-37**: [doc-generation-rfc-37](.claude/skills/doc-generation-rfc-37/SKILL.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/sync-docs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
