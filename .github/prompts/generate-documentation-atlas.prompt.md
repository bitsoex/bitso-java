# Generate AI-friendly documentation for Java services using Atlas MCP

> Generate AI-friendly documentation for Java services using Atlas MCP

# AI Documentation Generation for RAG System

Generate RFC-37 compliant documentation for services in this repository.

## Skill Location

Full documentation: [doc-generation-rfc-37](.claude/skills/doc-generation-rfc-37/SKILL.md)

## Quick Start

1. Identify services in `bitso-services/` directory
2. Check existing documentation for changes needed
3. Apply patterns from skill references:
   - `references/atlas-mcp-usage.md` - MCP queries for Atlas
   - `references/documentation-workflow.md` - Generation workflow
   - `references/templates.md` - RFC-37 templates
   - `references/troubleshooting.md` - Common issues
4. Only update when code changes (idempotency)

## Key Actions

- Create `docs/<service>/` folder per service
- Generate `overview.md` and `architecture.md`
- Document concepts in `concepts/` folder
- Document features in `features/` folder
- Include Mermaid diagrams for architecture

## Idempotency Rules

- Check existing docs before updating
- Only update when underlying code changed
- Preserve existing accurate content
- Don't rewrite just to rephrase

## Related

- **RFC-37**: Service Documentation Standardization
- **Doc Sync**: `global/skills/doc-sync/SKILL.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/generate-documentation-atlas.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
