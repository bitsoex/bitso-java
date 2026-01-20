# Flyway SQL Migration Review Guidelines

**Applies to:** **/*.sql

# Flyway SQL Migration Review Guidelines

Review Flyway SQL migrations considering:

## Locking and Concurrency

- **Index creation**: Indexes must be created with `CREATE INDEX CONCURRENTLY`, preceded by `DROP INDEX CONCURRENTLY IF EXISTS`.
- **Constraints**: Constraints (CHECK, FOREIGN KEY, NOT NULL) should use `NOT VALID` and be validated in a separate migration.
- **Lock timeout**: Set a `lock_timeout` (e.g., 60s) explicitly when needed to prevent blocking production queries.

## 📚 Full Documentation

For complete guidelines, scripts, and references, see the skill:

```
.claude/skills/database-integration/SKILL.md
```

The skill includes:
- **SKILL.md** - Complete instructions and quick start
- **scripts/** - Executable automation scripts
- **references/** - Detailed documentation
- **assets/** - Templates and resources

> **Note**: This is a shallow reference. The full content is maintained in the skill to avoid duplication.


---
*This rule is part of the java category.*
*Source: java/rules/java-flyway-migrations.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-flyway-migrations.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
