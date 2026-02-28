# Container Cleanup Patterns

Database and Redis cleanup strategies for test isolation.

## Contents

- [Database Cleanup with jOOQ](#database-cleanup-with-jooq)
- [Database Cleanup with DataSource](#database-cleanup-with-datasource)
- [Redis Cleanup](#redis-cleanup)
- [Full Cleanup Example](#full-cleanup-example)
- [Cleanup Order](#cleanup-order)
- [Using setup() vs cleanup()](#using-setup-vs-cleanup)

---
## Database Cleanup with jOOQ

```groovy
@Autowired(required = false)
DSLContext dslContext

// Call in setup() - see "Using setup() vs cleanup()" section below
private void cleanupDatabase() {
    if (dslContext == null) return

    dslContext.transaction { configuration ->
        def ctx = configuration.dsl()
        // Truncate children first, then parents (respect foreign keys)
        ctx.truncate(CHILD_TABLE).cascade().execute()
        ctx.truncate(PARENT_TABLE).cascade().execute()
    }
}
```

## Database Cleanup with DataSource

```groovy
@Autowired
@Qualifier("writeDataSource")
DataSource dataSource

private void cleanupDatabase() {
    def dslContext = using(dataSource, POSTGRES)

    dslContext.transaction { configuration ->
        def ctx = configuration.dsl()
        ctx.truncate(CONVERSION_ORDER).cascade().execute()
        ctx.truncate(CONVERSION).cascade().execute()
    }
}
```

## Redis Cleanup

```groovy
@Autowired(required = false)
@Named("{serviceName}Redis")
JedisPooled jedisPooled

// Call jedisPooled?.flushAll() in setup() - see "Using setup() vs cleanup()" section below
```

## Full Cleanup Example

```groovy
def cleanup() {
    // Reset GrpcMock stubs (if using GrpcMock for external services)
    resetGrpcMock()

    // Clear Redis data
    jedisPooled?.flushAll()

    // Clear database (if using explicit container)
    cleanupDatabase()
}
```

## Cleanup Order

The order matters when you have dependencies:

1. **GrpcMock** - Reset stubs first (no data dependencies)
2. **Redis** - Clear cache data
3. **Database** - Clear persistent data (children before parents)

## Using setup() vs cleanup()

| Approach | Pros | Cons |
|----------|------|------|
| **cleanup()** | Clean state after test | Failed test may leave dirty state |
| **setup()** | Clean state before test | First test sees previous run's data |
| **Both** | Maximum isolation | Slightly slower |

**Recommendation:** Use `setup()` for data cleanup to ensure clean state even if previous test failed.

```groovy
def setup() {
    // Reset mocks (@MockitoBean only)
    reset(externalClient1, externalClient2)

    // Clean data from previous tests
    jedisPooled?.flushAll()
    cleanupDatabase()

    // Set up defaults
    setupDefaultMockBehaviors()
}

def cleanup() {
    // Reset GrpcMock stubs (if using)
    resetGrpcMock()
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/container-cleanup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

