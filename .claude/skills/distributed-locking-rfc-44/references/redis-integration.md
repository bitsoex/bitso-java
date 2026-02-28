---
title: Redis Integration
description: Redis-based distributed locking using jedis4-utils
---

# Redis Integration

Redis-based distributed locking using jedis4-utils from jvm-generic-libraries.

## Contents

- [When to Use Redis Locking](#when-to-use-redis-locking)
- [Setup](#setup)
- [Usage Patterns](#usage-patterns)
- [Lock Configuration](#lock-configuration)
- [Testing](#testing)
- [Migrating from Redis to PostgreSQL](#migrating-from-redis-to-postgresql)
- [Related](#related)

---
## When to Use Redis Locking

- Service does not have PostgreSQL available
- Team has justified continued use of Redis for locking
- Service already uses Redis extensively

> **Note**: Redis-based locking is a valid alternative per RFC-44, not deprecated.

## Setup

### 1. Add Dependencies

```toml
# gradle/libs.versions.toml
[versions]
jedis4-utils = "3.0.0"

[libraries]
jedis4-utils = { module = "com.bitso.commons:jedis4-utils", version.ref = "jedis4-utils" }
```

```groovy
// build.gradle
dependencies {
    implementation libs.jedis4.utils
}
```

### 2. Create Configuration Bean

`JedisLockingUtil` implements `DistributedLockManager<String>`, providing a unified API:

```java
import com.bitso.distributed.locking.DistributedLockManager;
import com.bitso.jedis.locking.JedisLockingUtil;
import io.micrometer.core.instrument.MeterRegistry;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisDistributedLockConfiguration {
    @Bean
    DistributedLockManager<String> distributedLockManager(
            JedisPooled jedisPooled, MeterRegistry meterRegistry) {
        return new JedisLockingUtil(jedisPooled, meterRegistry);
    }
}
```

## Usage Patterns

### Unified API (Recommended)

With the configuration above, usage is nearly identical to PostgreSQL:

```java
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;

@Component
public class ScheduledTask {
    private final DistributedLockManager<String> distributedLockManager;

    @Scheduled(cron = "${task.cron:-}", zone = "UTC")
    public void runTask() {
        try (var lock = distributedLockManager.tryLock("lock_key")) {
            if (!lock.acquired()) {
                log.info("Task is already running on another instance");
                return;
            }
            executeTask();
        }
    }
}
```

### Legacy Pattern (Still Valid)

The legacy `RedisOperations` pattern is still valid for existing code:

```java
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;

@Component
public class ScheduledTask {
    @Autowired
    @Qualifier("ephemeralRedis")
    private RedisOperations<?, ?, ?, ?, ?> redis;

    private final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    private final Duration LOCK_TTL = Duration.ofHours(1);

    @Scheduled(cron = "${task.cron:-}", zone = "UTC")
    public void runTask() {
        Try.withResources(() -> redis.tryAutoclosingLock(
                "lock_key",
                LOCK_TIMEOUT.toMillis(),
                LOCK_TTL.toMillis()))
            .of(redisLock -> Option.of(redisLock)
                .filter(RedisLock::isAcquired)
                .peek(lockAcquired -> executeTask()));
    }
}
```

## Lock Configuration

### TTL (Time-to-Live)

Redis locks require a TTL to prevent orphaned locks:

```java
// JedisLockingUtil uses default TTL
// For custom TTL, configure in constructor or use legacy pattern

// Legacy pattern with explicit TTL
redis.tryAutoclosingLock(
    "lock_key",
    10_000,     // Timeout: 10 seconds to acquire
    3_600_000   // TTL: 1 hour maximum lock hold time
)
```

### Lock Keys

Redis lock keys are strings. Use consistent naming:

```java
// Good: Descriptive and namespaced
distributedLockManager.tryLock("order-processing:batch-job")
distributedLockManager.tryLock("metrics:daily-aggregation")

// Avoid: Generic or collision-prone
distributedLockManager.tryLock("lock")
distributedLockManager.tryLock("job")
```

## Testing

### Mocking Redis Lock

```groovy
class ScheduledTaskSpec extends Specification {
    DistributedLockManager<String> distributedLockManager = Mock()

    def "should acquire lock and run task"() {
        given:
        def lock = Mock(DistributedLock) { acquired() >> true }
        distributedLockManager.tryLock(_) >> lock

        when:
        task.runTask()

        then:
        1 * taskExecutor.execute()
    }

    def "should skip when lock not acquired"() {
        given:
        def lock = Mock(DistributedLock) { acquired() >> false }
        distributedLockManager.tryLock(_) >> lock

        when:
        task.runTask()

        then:
        0 * taskExecutor.execute()
    }
}
```

## Migrating from Redis to PostgreSQL

If you later decide to migrate from Redis to PostgreSQL:

1. Replace the bean configuration
2. Keep the code unchanged (same `DistributedLockManager` interface)
3. Update tests if they depend on Redis-specific behavior

```java
// Before: Redis
@Bean
DistributedLockManager<String> distributedLockManager(
        JedisPooled jedisPooled, MeterRegistry meterRegistry) {
    return new JedisLockingUtil(jedisPooled, meterRegistry);
}

// After: PostgreSQL
@Bean
DistributedLockManager<Long> distributedLockManager(
        @Qualifier("write-dslcontext") DSLContext dslContext) {
    return new JooqPostgresSessionDistributedLockManager(dslContext);
}
```

> **Note**: The generic type parameter changes from `String` to `Long`, which may require minor code adjustments.

## Related

- [Migration Workflow](migration-workflow.md) - Full migration guide
- [Lock Patterns](lock-patterns.md) - Common locking patterns
- [Troubleshooting](troubleshooting.md) - Common issues
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/distributed-locking-rfc-44/references/redis-integration.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

