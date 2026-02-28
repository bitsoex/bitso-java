---
title: Troubleshooting
description: Common issues and solutions for distributed locking
---

# Troubleshooting

Common issues and solutions for distributed locking.

## Contents

- [PostgreSQL Issues](#postgresql-issues)
- [Redis Issues](#redis-issues)
- [Migration Issues](#migration-issues)
- [Performance Issues](#performance-issues)
- [Example PRs](#example-prs)
- [Related](#related)

---
## PostgreSQL Issues

### DSLContext Qualifier Not Found

**Symptom**:
```
No qualifying bean of type 'org.jooq.DSLContext' available
```

**Solution**: Find the correct DSLContext bean name:

```bash
grep -rn "DSLContext\|dslContext" --include="*.java" src/
```

Update the `@Qualifier` in your configuration:

```java
@Bean
DistributedLockManager<Long> distributedLockManager(
        @Qualifier("dslContext") DSLContext dslContext) {  // Adjust qualifier
    return new JooqPostgresSessionDistributedLockManager(dslContext);
}
```

### Connection Pool Warning

**Issue**: Session-level advisory locks can have issues with connection pools. If multiple instances try to acquire the same lock concurrently, connection pooling can cause unintended lock sharing.

**Solutions**:

1. **Use transaction-level locks**:
   ```java
   return new JooqPostgresTransactionDistributedLockManager(dslContext);
   ```

2. **Ensure proper connection handling**: Use try-with-resources to release connections promptly.

### Lock Not Released

**Symptom**: Locks remain held after job completion.

**Causes**:
- Not using try-with-resources
- Exception before lock release
- Connection not returned to pool

**Solution**: Always use try-with-resources:

```java
// ✅ Correct: Lock always released
try (var lock = distributedLockManager.tryLock("key")) {
    if (lock.acquired()) {
        doWork();
    }
}

// ❌ Wrong: Lock may not be released
var lock = distributedLockManager.tryLock("key");
if (lock.acquired()) {
    doWork();  // If exception here, lock not released
}
lock.close();
```

### LIMIT Clause with Advisory Locks

**⚠️ Warning**: Never use `LIMIT` clauses when acquiring advisory locks in a query.

**Dangerous Pattern**:
```sql
-- ❌ DANGEROUS: LIMIT not guaranteed before lock acquisition
SELECT pg_advisory_lock(id) FROM resources WHERE status = 'pending' LIMIT 100;
```

**Problem**: PostgreSQL may execute `pg_advisory_lock()` on more rows than the `LIMIT` specifies before filtering. This can result in:
- More locks acquired than expected
- Locks that won't be released until session ends
- Session resource exhaustion

**Safe Pattern**:
```java
// ✅ Safe: Select IDs first, then lock one at a time
List<Long> ids = dslContext
    .select(RESOURCES.ID)
    .from(RESOURCES)
    .where(RESOURCES.STATUS.eq("pending"))
    .limit(100)
    .fetch(RESOURCES.ID);

for (Long id : ids) {
    try (var lock = distributedLockManager.tryLock(id)) {
        if (lock.acquired()) {
            processResource(id);
        }
    }
}
```

## Redis Issues

### Lock Expiration During Operation

**Symptom**: Lock expires while operation is still running.

**Cause**: TTL too short for the operation duration.

**Solutions**:

1. Increase TTL:
   ```java
   redis.tryAutoclosingLock("key", 10_000, 7_200_000)  // 2 hour TTL
   ```

2. Break operation into smaller chunks:
   ```java
   for (var chunk : chunks) {
       try (var lock = distributedLockManager.tryLock("key")) {
           if (lock.acquired()) {
               processChunk(chunk);
           }
       }
   }
   ```

### Redis Connection Issues

**Symptom**: Lock acquisition fails intermittently.

**Solutions**:

1. Check Redis connectivity:
   ```bash
   redis-cli -h <host> -p <port> ping
   ```

2. Verify connection pool settings:
   ```yaml
   redis:
     pool:
       max-active: 8
       max-idle: 4
       min-idle: 1
   ```

## Migration Issues

### Import Errors After Migration

**Symptom**: Compilation errors after removing incubated libraries.

**Solution**: Verify you're using correct imports:

```java
// ✅ Correct imports from jvm-generic-libraries
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;

// ❌ Wrong: Old incubated library imports
// import your.project.libs.distributed.locking.DistributedLock;
```

### Tests Failing After Migration

**Symptom**: Tests fail with mocking errors.

**Solution**: Update mock setup:

```groovy
// ✅ Correct mock setup
def lock = Mock(DistributedLock) {
    acquired() >> true  // Use acquired(), not isAcquired()
}
distributedLockManager.tryLock(_) >> lock

// ❌ Wrong: Old Redis mock pattern
// redisLock.isAcquired() >> true  // Wrong method name
```

### Fabric8 Dependencies Still Present

**Symptom**: Build warnings about Fabric8 dependencies.

**Solution**: Remove all Fabric8 references:

```bash
# Find remaining references
grep -rn "fabric8\|kubernetes-fabric8" --include="*.gradle" --include="*.yaml" .
```

Remove from `build.gradle`:
```groovy
// Remove these
implementation 'org.springframework.cloud:spring-cloud-kubernetes-fabric8-leader'
```

Remove from `application.yaml`:
```yaml
# Remove this entire section
spring:
  cloud:
    kubernetes:
      leader:
        enabled: true
```

## Performance Issues

### High Lock Contention

**Symptom**: Many instances waiting for locks, job throughput drops.

**Solutions**:

1. **Use more granular locks**:
   ```java
   // ❌ Single lock for all orders
   distributedLockManager.tryLock("order-processing");

   // ✅ Per-order locks
   distributedLockManager.tryLock("order-" + orderId);
   ```

2. **Reduce lock duration**:
   ```java
   // Process smaller batches
   for (var batch : getBatches(100)) {
       try (var lock = distributedLockManager.tryLock("batch-" + batch.id())) {
           if (lock.acquired()) {
               processBatch(batch);
           }
       }
   }
   ```

### Lock Acquisition Latency

**Symptom**: `tryLock()` takes longer than expected.

**Solutions**:

1. Check database/Redis latency
2. Monitor connection pool usage
3. Consider using non-blocking lock check

## Example PRs

These PRs demonstrate successful RFC-44 alignment:

| Repository | PR | Migration Type |
|------------|-----|----------------|
| balance-history | [PR 389](https://github.com/bitsoex/balance-history/pull/389) | Remove incubated library |
| balance-checker-v2 | [PR 245](https://github.com/bitsoex/balance-checker-v2/pull/245) | Add distributed locking |
| card-reconciliation | [PR 435](https://github.com/bitsoex/card-reconciliation/pull/435) | Postgres advisory locks |
| proof-of-solvency | [PR 503](https://github.com/bitsoex/proof-of-solvency/pull/503) | Replace Redis with Postgres |

## Related

- [Migration Workflow](migration-workflow.md) - Full migration guide
- [Lock Patterns](lock-patterns.md) - Common locking patterns
- [Redis Integration](redis-integration.md) - Redis-based locking
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/distributed-locking-rfc-44/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

