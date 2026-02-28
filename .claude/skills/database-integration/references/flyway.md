# Flyway SQL Migration Guidelines

Review Flyway SQL migrations with these considerations.

## Contents

- [Locking and Concurrency](#locking-and-concurrency)
- [Column Type Changes](#column-type-changes)
- [Migration Separation](#migration-separation)
- [Column/Table Renaming](#columntable-renaming)
- [Best Practices](#best-practices)
- [Migration File Naming](#migration-file-naming)

---
## Locking and Concurrency

### Index Creation

Indexes must be created with `CREATE INDEX CONCURRENTLY`, preceded by `DROP INDEX CONCURRENTLY IF EXISTS`:

```sql
DROP INDEX CONCURRENTLY IF EXISTS idx_users_email;
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
```

### Constraints

Constraints (CHECK, FOREIGN KEY, NOT NULL) should use `NOT VALID` and be validated in a separate migration:

```sql
-- Migration 1: Add constraint without validation
ALTER TABLE orders ADD CONSTRAINT fk_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) NOT VALID;

-- Migration 2: Validate constraint (separate migration)
ALTER TABLE orders VALIDATE CONSTRAINT fk_user_id;
```

### Lock Timeout

Set a `lock_timeout` explicitly when needed to prevent blocking production queries:

```sql
SET lock_timeout = '60s';
ALTER TABLE users ADD COLUMN status VARCHAR(20);
```

## Column Type Changes

Do not change column types directly unless proven safe. Instead:

1. Add new column
2. Backfill in batches with COMMIT inside loop
3. Migrate reads/writes
4. Drop old column

```sql
-- Migration 1: Add new column
ALTER TABLE users ADD COLUMN email_new VARCHAR(255);

-- Migration 2: Backfill (run as separate script with batching)
-- In application code or separate job

-- Migration 3: Switch over (after application is updated)
ALTER TABLE users DROP COLUMN email;
ALTER TABLE users RENAME COLUMN email_new TO email;
```

## Migration Separation

### Avoid Combining DDL and DML

Do not combine DDL and DML in the same migration. Split into separate migrations.

```sql
-- ❌ Bad: DDL and DML together
ALTER TABLE users ADD COLUMN status VARCHAR(20);
UPDATE users SET status = 'active';

-- ✅ Good: Separate migrations
-- V1__add_status_column.sql
ALTER TABLE users ADD COLUMN status VARCHAR(20);

-- V2__set_default_status.sql (or handled by application)
UPDATE users SET status = 'active' WHERE status IS NULL;
```

### Data Backfilling

When backfilling data:
- Use batching (e.g., `LIMIT 100`)
- Commit after each batch
- Optionally throttle with `pg_sleep()`

```sql
-- Batched update (run from application with explicit transactions)
-- Note: COMMIT cannot be used inside DO blocks in PostgreSQL
-- Use application-level loop with separate transactions per batch

-- Single batch update (call repeatedly from application)
UPDATE users
SET status = 'active'
WHERE id IN (
    SELECT id FROM users
    WHERE status IS NULL
    LIMIT 1000
);
-- COMMIT; -- Execute from application after each batch
```

## Column/Table Renaming

Do not rename columns/tables in use. Instead:

1. Add new name
2. Dual write
3. Switch reads
4. Drop old name

```sql
-- Migration 1: Add new column
ALTER TABLE users ADD COLUMN full_name VARCHAR(255);

-- Application: Update to dual-write both columns
-- Application: Switch reads to new column

-- Migration 2: Drop old column (after application is updated)
ALTER TABLE users DROP COLUMN name;
```

## Best Practices

### Reversible Migrations

Prefer reversible migrations. If destructive changes are needed, preserve data first:

```sql
-- Before dropping, create backup
CREATE TABLE users_backup AS SELECT * FROM users;

-- Then proceed with change
ALTER TABLE users DROP COLUMN legacy_field;
```

### Schema Changes Separation

Ensure no application code is tied to schema changes in the same PR.

### Postgres Version Compatibility

Confirm PostgreSQL 14.18 behaviors are accounted for:
- Index rebuilds
- Transactional DDL
- Concurrent index creation limitations

## Migration File Naming

Follow Flyway naming convention:

```
V{version}__{description}.sql

Examples:
V1__create_users_table.sql
V2__add_email_index.sql
V3__add_status_column.sql
```

Use double underscore (`__`) between version and description.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/database-integration/references/flyway.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

