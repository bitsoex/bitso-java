---
title: Lock Patterns
description: RFC-44 compliant distributed locking patterns for scheduled jobs
---

# Lock Patterns

RFC-44 compliant distributed locking patterns for scheduled jobs.

## Contents

- [Basic Patterns](#basic-patterns)
- [Lock Key Naming](#lock-key-naming)
- [PostgreSQL Advisory Locks](#postgresql-advisory-locks)
- [Lock Acquisition Behavior](#lock-acquisition-behavior)
- [Error Handling](#error-handling)
- [Anti-Patterns](#anti-patterns)
- [Related](#related)

---
## Basic Patterns

### Try-with-resources (Recommended)

```java
@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
public void emitMetrics() {
    try (var lock = distributedLockManager.tryLock("metrics-lock")) {
        if (!lock.acquired()) {
            return;
        }
        doWork();
    } catch (Exception e) {
        log.error("Failed while trying to acquire a lock", e);
    }
}
```

### Vavr Functional Pattern

```java
@Scheduled(cron = "${job.cron:-}", zone = "UTC")
public void scheduledJob() {
    log.info("Starting job");
    Try.withResources(() -> distributedLockManager.tryLock("lock_key"))
        .of(lock -> Option.of(lock)
            .filter(DistributedLock::acquired)
            .onEmpty(() -> log.info("Job is already running"))
            .peek(lockAcquired -> doWork()));
    log.info("Job finished");
}
```

## Lock Key Naming

Use descriptive, unique lock keys:

| Pattern | Example |
|---------|---------|
| Job-based | `"metrics-emission-lock"` |
| Entity-based | `"order-processing-" + orderId` |
| Tenant-based | `"tenant-" + tenantId + "-sync"` |

## PostgreSQL Advisory Locks

### Session-Level Locks (Default)

Session-level locks are held until explicitly released or the session ends:

```java
@Bean
DistributedLockManager<Long> distributedLockManager(
        @Qualifier("write-dslcontext") DSLContext dslContext) {
    return new JooqPostgresSessionDistributedLockManager(dslContext);
}
```

### Transaction-Level Locks

Transaction-level locks are automatically released when the transaction ends:

```java
@Bean
DistributedLockManager<Long> distributedLockManager(
        @Qualifier("write-dslcontext") DSLContext dslContext) {
    return new JooqPostgresTransactionDistributedLockManager(dslContext);
}
```

Use transaction-level locks for high-concurrency scenarios where connection pooling may cause issues.

## Lock Acquisition Behavior

### Non-blocking (Default)

`tryLock()` returns immediately if lock cannot be acquired:

```java
try (var lock = distributedLockManager.tryLock("key")) {
    if (!lock.acquired()) {
        // Lock held by another instance
        return;
    }
    // Got the lock
}
```

### With Timeout

Some implementations support waiting for lock acquisition:

```java
try (var lock = distributedLockManager.tryLock("key", Duration.ofSeconds(5))) {
    if (!lock.acquired()) {
        // Timeout waiting for lock
        return;
    }
    // Got the lock after waiting
}
```

## Error Handling

### Graceful Lock Acquisition Failure

```java
public void scheduledTask() {
    DistributedLock lock = null;
    try {
        lock = distributedLockManager.tryLock("task-lock");
        if (!lock.acquired()) {
            log.debug("Task already running on another instance");
            return;
        }
        executeTask();
    } catch (Exception e) {
        log.error("Error during task execution", e);
    } finally {
        if (lock != null) {
            try {
                lock.close();
            } catch (Exception e) {
                log.warn("Error releasing lock", e);
            }
        }
    }
}
```

### Logging Best Practices

```java
try (var lock = distributedLockManager.tryLock("job-lock")) {
    if (!lock.acquired()) {
        // Use debug/info, not warn/error - this is expected behavior
        log.info("Lock not acquired, job running on another instance");
        return;
    }
    log.info("Lock acquired, starting job");
    doWork();
    log.info("Job completed successfully");
}
```

## Anti-Patterns

### ❌ Don't Use Locks for Long-Running Operations

```java
// ❌ BAD: Lock held for extended period
try (var lock = distributedLockManager.tryLock("batch-job")) {
    if (lock.acquired()) {
        processMassiveDataset(); // Takes 30 minutes
    }
}

// ✅ GOOD: Process in chunks, release lock between chunks
for (var chunk : getChunks()) {
    try (var lock = distributedLockManager.tryLock("batch-job")) {
        if (lock.acquired()) {
            processChunk(chunk);
        }
    }
}
```

### ❌ Don't Nest Locks

```java
// ❌ BAD: Nested locks can cause deadlocks
try (var lock1 = distributedLockManager.tryLock("lock-a")) {
    if (lock1.acquired()) {
        try (var lock2 = distributedLockManager.tryLock("lock-b")) {
            // Potential deadlock if other code acquires b then a
        }
    }
}
```

## Related

- [Migration Workflow](migration-workflow.md) - Migration guide
- [Redis Integration](redis-integration.md) - Redis-based locking
- [Troubleshooting](troubleshooting.md) - Common issues
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/distributed-locking-rfc-44/references/lock-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

