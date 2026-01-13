# Flyway SQL Migration Review Guidelines

**Applies to:** **/*.sql

# Flyway SQL Migration Review Guidelines

Review Flyway SQL migrations considering:

## Locking and Concurrency

- **Index creation**: Indexes must be created with `CREATE INDEX CONCURRENTLY`, preceded by `DROP INDEX CONCURRENTLY IF EXISTS`.
- **Constraints**: Constraints (CHECK, FOREIGN KEY, NOT NULL) should use `NOT VALID` and be validated in a separate migration.
- **Lock timeout**: Set a `lock_timeout` (e.g., 60s) explicitly when needed to prevent blocking production queries.

## Column Type Changes

Do not change column types directly unless proven safe. Instead follow this process:

1. Add new column
2. Backfill in batches with COMMIT inside loop
3. Migrate reads/writes
4. Drop old column

## Migration Separation

- **Avoid combining DDL and DML**: Do not combine DDL and DML in the same migration. Split into separate migrations.
- **Data backfilling**: When backfilling data:
  - Use batching (e.g., `LIMIT 100`)
  - Commit after each batch
  - Optionally throttle with `pg_sleep()`

## Column/Table Renaming

Do not rename columns/tables in use. Instead:

1. Add new name
2. Dual write
3. Switch reads
4. Drop old name

## Best Practices

- **Reversible migrations**: Prefer reversible migrations. If destructive changes are needed, preserve data first.
- **Schema changes separation**: Ensure no application code is tied to schema changes in the same PR.
- **Postgres version**: Confirm Postgres 14.18 behaviors (e.g., index rebuilds, transactional DDL) are accounted for.

---
*This rule is part of the java category.*
*Source: java/rules/java-flyway-migrations.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-flyway-migrations.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
