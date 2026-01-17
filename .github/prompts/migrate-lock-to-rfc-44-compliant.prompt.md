# Align distributed locking with RFC-44 standards (PostgreSQL or Redis)

> Align distributed locking with RFC-44 standards (PostgreSQL or Redis)

# Migrate Lock to RFC-44 Compliant

Align distributed locking mechanisms with RFC-44 standards.

## Skill Location

Full documentation: `.claude/skills/distributed-locking/`

## Quick Start

1. Assess infrastructure (PostgreSQL vs Redis availability)
2. Identify legacy patterns to migrate
3. Apply patterns from the skill:
   - `.claude/skills/distributed-locking/SKILL.md` - Main instructions
   - `.claude/skills/distributed-locking/references/migration-workflow.md` - Step-by-step guide
   - `.claude/skills/distributed-locking/references/lock-patterns.md` - RFC-44 patterns
   - `.claude/skills/distributed-locking/references/redis-integration.md` - Redis option
   - `.claude/skills/distributed-locking/references/troubleshooting.md` - Common issues
4. Validate build and tests

## Migration Paths

| Scenario | Action |
|----------|--------|
| PostgreSQL available | Use advisory locks (default) |
| Redis only | Use jedis4-utils |
| Fabric8 leader election | **Must migrate** |
| Incubated in-repo libs | **Must migrate** |

## Related

- **RFC-44**: [Confluence](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229)
- **Rule**: `java/rules/java-distributed-locking-rfc44.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/migrate-lock-to-rfc-44-compliant.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
