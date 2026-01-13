---
description: RFC-44 Distributed Locking Standards for Java Services
alwaysApply: false
globs: **/*.java,**/*.groovy,**/build.gradle,**/libs.versions.toml
tags:
  - java
---

# RFC-44: Distributed Locking Standards

This rule defines the standards for distributed locking in Java services, as established by [RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229/RFC-44+Scheduler+Tasks+and+Distributed+Locking).

## Quick Reference

| Approach | Status | Use Case |
|----------|--------|----------|
| PostgreSQL Advisory Locks (`distributed-locking-api`) | **Default/Recommended** | Services with PostgreSQL available |
| Redis via `jedis4-utils` | **Allowed** | Services without PostgreSQL, or with valid Redis use case |
| ShedLock | Allowed | Quick tasks (no immediate migration required) |
| Kubernetes CronJobs (`bitso-jobs`) | Allowed | Resource-intensive or long-running tasks |
| Fabric8 Leader Election | **Forbidden** | Do NOT use |
| In-repo incubated locking libs | **Deprecated** | Migrate to jvm-generic-libraries |

## Choosing Between PostgreSQL and Redis

RFC-44 explicitly supports **both PostgreSQL and Redis** for distributed locking. Use the following guidance:

### Use PostgreSQL Advisory Locks when

- Your service already has a PostgreSQL connection
- You want the default recommended approach
- You need the fastest lock acquisition (~0.01ms)

### Use Redis Locking when

- Your service does not have PostgreSQL available
- Your service already relies on Redis and the team has valid reasons to continue using it
- Both PostgreSQL and Redis are available, but the team justifies Redis for their use case

**Important**: Both implementations are valid per RFC-44. Redis-based locking is **NOT deprecated** - it is an explicitly supported option. The RFC-44 migration plan includes making `distributed-locking-redis` implement the `distributed-locking-api` interfaces.

## Code Review Guidance

During code review, determine what infrastructure the service uses and make suggestions accordingly:

### Decision Matrix for Code Reviewers

| Service Infrastructure | Recommendation |
|------------------------|----------------|
| PostgreSQL only (no Redis) | Suggest `distributed-locking-postgres-jooq` |
| Redis only (no PostgreSQL) | Current Redis locking (`jedis4-utils` or `RedisOperations`) is fine |
| Both PostgreSQL and Redis | User chooses; suggest PostgreSQL as default, but Redis is acceptable with justification |
| Neither | Suggest adding PostgreSQL for locking, or Redis if more appropriate for the use case |

### Key Points

- Using Redis for distributed locking is **NOT a violation** of RFC-44
- Redis is explicitly supported per the RFC
- Do NOT flag Redis locking as requiring migration if the team has a valid reason (e.g., service already uses Redis, no PostgreSQL available)
- Only suggest PostgreSQL migration if the service has PostgreSQL available AND the team is open to switching

## Approved Libraries

### PostgreSQL Implementation (Default)

For services with PostgreSQL, use the advisory locks library from `jvm-generic-libraries`:

```toml
# gradle/libs.versions.toml
[versions]
distributed-locking-api = "2.0.0"
distributed-locking-postgres-jooq = "2.0.0"

[libraries]
distributed-locking-api = { module = "com.bitso.commons:distributed-locking-api", version.ref = "distributed-locking-api" }
distributed-locking-postgres-jooq = { module = "com.bitso.commons:distributed-locking-postgres-jooq", version.ref = "distributed-locking-postgres-jooq" }
```

**Note**: Version 2.0.0 is built for Java 21 and is the recommended version.

### Redis Implementation

For services without PostgreSQL, use `jedis4-utils` from `jvm-generic-libraries`:

```toml
# gradle/libs.versions.toml
[versions]
jedis4-utils = "3.0.0"

[libraries]
jedis4-utils = { module = "com.bitso.commons:jedis4-utils", version.ref = "jedis4-utils" }
```

Refer to the [Jedis4-utils GitHub project](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/jedis4-utils) for usage details.

**Note**: `JedisLockingUtil` already implements the `DistributedLockManager<String>` interface from `distributed-locking-api`, providing a unified API across both PostgreSQL and Redis implementations.

### Performance

- **PostgreSQL Advisory Locks**: ~0.01ms to acquire locks
- **Redis Locking**: Sub-millisecond (varies by network latency)
- **ShedLock**: ~400ms to acquire locks

## Patterns to Flag in Code Review

### 1. Fabric8 Leader Election (Forbidden)

**Flag this code** - do NOT use for scheduled tasks:

```java
// FORBIDDEN: Leader election for locking
@EventListener(OnGrantedEvent.class)
public void onLeaderGranted(OnGrantedEvent event) {
    isLeader = true;
}

@EventListener(OnRevokedEvent.class)
public void onLeaderRevoked(OnRevokedEvent event) {
    isLeader = false;
}

// FORBIDDEN: Leader check in scheduled task
@Scheduled(fixedRate = 2000)
public void scheduledTask() {
    if (isLeader) {
        // ...
    }
}
```

**Dependencies to flag**:

```groovy
// FORBIDDEN
implementation 'org.springframework.cloud:spring-cloud-kubernetes-fabric8-leader'
```

**Configuration to flag**:

```yaml
# FORBIDDEN
spring:
  cloud:
    kubernetes:
      leader:
        enabled: true
```

### 2. Incubated In-Repo Locking Libraries (Deprecated)

**Flag these packages** - they should be replaced with `com.bitso.commons` versions:

```java
// DEPRECATED: Incubated in-repo libraries (non-commons packages)
import com.bitso.distributed.locking.LockingUtil;
import com.bitso.distributed.locking.LockingHashUtil;
import com.bitso.distributed.locking.LockingException;
import com.bitso.distributed.locking.postgres.jooq.JooqPostgresSessionLockingUtil;
import com.bitso.distributed.locking.postgres.jooq.JooqPostgresTransactionLockingUtil;
```

**Note**: The correct package is `com.bitso.distributed.locking` from `com.bitso.commons:distributed-locking-api`, NOT from in-repo incubated libraries.

### 3. Redis Locking (Context-Dependent)

Redis locking is **NOT deprecated**. Only flag if ALL the following are true:

- Service has PostgreSQL available
- No valid justification for using Redis over PostgreSQL
- Team is not aware of the PostgreSQL option

If Redis usage is intentional and justified, it is compliant with RFC-44.

```java
// VALID if service doesn't have PostgreSQL or team has justified Redis use:
redis.tryAutoclosingLock("lock_key", LOCK_TIMEOUT.toMillis(), LOCK_TTL.toMillis())
jedisWrapper.tryAutoclosingLock(...)

// Imports are VALID for Redis-based services:
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;
```

## Recommended Implementation

### PostgreSQL: Spring Configuration

```java
import com.bitso.distributed.locking.DistributedLockManager;
import com.bitso.distributed.locking.postgres.jooq.JooqPostgresSessionDistributedLockManager;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributedLockConfiguration {
    @Bean
    DistributedLockManager<Long> distributedLockManager(@Qualifier("write-dslcontext") DSLContext dslContext) {
        return new JooqPostgresSessionDistributedLockManager(dslContext);
    }
}
```

### PostgreSQL: Usage in Scheduled Tasks

```java
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;
import io.vavr.control.Option;
import io.vavr.control.Try;

@Component
public class ScheduledTask {
    private final DistributedLockManager<Long> distributedLockManager;

    @Scheduled(cron = "${task.cron:-}", zone = "UTC")
    public void runTask() {
        log.info("Starting scheduled task");
        Try.withResources(() -> distributedLockManager.tryLock("task_lock_key"))
            .of(lock -> Option.of(lock)
                .filter(DistributedLock::acquired)
                .onEmpty(() -> log.info("Task is already running on another instance"))
                .peek(lockAcquired -> executeTask()));
        log.info("Scheduled task finished");
    }
}
```

### PostgreSQL: Alternative Usage Pattern (try-with-resources)

```java
@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
public void emitMetrics() {
    try (var lock = distributedLockManager.tryLock("metrics-emitter-lock")) {
        if (!lock.acquired()) {
            return;
        }
        // Execute critical section
        doWork();
    } catch (Exception e) {
        log.error("Failed while trying to acquire a lock", e);
    }
}
```

### PostgreSQL: Usage with Retry

```java
// Try to acquire the lock with retries
try (var lock = distributedLockManager.tryLock(
        "lock_key",
        Duration.ofSeconds(10),   // Maximum time to try
        Duration.ofMillis(500),   // Initial retry interval
        Duration.ofMillis(50)     // Amount to decrease retry interval on each attempt
)) {
    if (lock.acquired()) {
        performCriticalOperation();
    } else {
        handleLockFailure();
    }
}
```

### Redis: Spring Configuration

```java
import com.bitso.distributed.locking.DistributedLockManager;
import com.bitso.jedis.locking.JedisLockingUtil;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisDistributedLockConfiguration {
    @Bean
    DistributedLockManager<String> distributedLockManager(JedisPooled jedisPooled, MeterRegistry meterRegistry) {
        return new JedisLockingUtil(jedisPooled, meterRegistry);
    }
}
```

### Redis: Usage Pattern (Unified API)

`JedisLockingUtil` implements `DistributedLockManager<String>`, so usage is nearly identical to PostgreSQL:

```java
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;

@Component
public class ScheduledTask {
    private final DistributedLockManager<String> distributedLockManager;

    @Scheduled(cron = "${task.cron:-}", zone = "UTC")
    public void runTask() {
        log.info("Starting scheduled task");
        try (var lock = distributedLockManager.tryLock("lock_key")) {
            if (!lock.acquired()) {
                log.info("Task is already running on another instance");
                return;
            }
            executeTask();
        }
        log.info("Scheduled task finished");
    }
}
```

### Redis: Usage Pattern with Timeout and TTL

For more control over lock acquisition timeout and TTL:

```java
@Scheduled(cron = "${task.cron:-}", zone = "UTC")
public void runTask() {
    // Cast to JedisLockingUtil for extended API
    var jedisLocking = (JedisLockingUtil) distributedLockManager;
    
    try (var lock = jedisLocking.tryLock("lock_key", 
            Duration.ofSeconds(10).toMillis(),  // timeout to acquire
            Duration.ofHours(1).toMillis())) {  // lock TTL
        if (!lock.acquired()) {
            log.info("Task is already running on another instance");
            return;
        }
        executeTask();
    }
}
```

### Redis: Legacy Pattern (Still Valid)

The legacy `RedisOperations` pattern is still valid for existing code:

```java
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;

@Component
public class ScheduledTask {
    @Autowired
    @Qualifier("ephemeralRedis")
    private RedisOperations<?, ?, ?, ?, ?> redis;

    @Scheduled(cron = "${task.cron:-}", zone = "UTC")
    public void runTask() {
        Try.withResources(() -> redis.tryAutoclosingLock("lock_key", 10000, 3600000))
            .of(redisLock -> Option.of(redisLock)
                .filter(RedisLock::isAcquired)
                .peek(lockAcquired -> executeTask()));
    }
}
```

## Migration Checklist

### When migrating FROM Redis TO PostgreSQL (optional, only if desired)

- [ ] Replace `RedisOperations.tryAutoclosingLock()` with `DistributedLockManager.tryLock()`
- [ ] Replace `RedisLock.isAcquired()` with `DistributedLock.acquired()`
- [ ] Remove `@Qualifier("ephemeralRedis")` Redis injection for locking
- [ ] Add `distributed-locking-api` and `distributed-locking-postgres-jooq` dependencies
- [ ] Create `DistributedLockConfiguration` bean
- [ ] Update tests to mock `DistributedLockManager<Long>`
- [ ] Remove unused Redis dependencies if locking was the only use case

### When migrating FROM incubated libraries TO jvm-generic-libraries

- [ ] Remove in-repo `bitso-libs/distributed-locking-*` directories
- [ ] Update `settings.gradle` to remove incubated module includes
- [ ] Add `com.bitso.commons:distributed-locking-api` from version catalog
- [ ] Imports remain the same (`com.bitso.distributed.locking.*`)

## Example Migration PRs

These PRs demonstrate successful migrations to RFC-44 compliant locking:

| Repository | PR | Description |
|------------|-----|-------------|
| balance-history | [PR 389](https://github.com/bitsoex/balance-history/pull/389) | Remove incubated library, adopt jvm-generic-libraries |
| balance-checker-v2 | [PR 245](https://github.com/bitsoex/balance-checker-v2/pull/245) | Add distributed locking for scheduled metric emission |
| card-reconciliation | [PR 435](https://github.com/bitsoex/card-reconciliation/pull/435) | Implement Postgres advisory locks for file polling |
| aum-reconciliation-v2 | [PR 646](https://github.com/bitsoex/aum-reconciliation-v2/pull/646) | Add locking for delta computation background job |
| proof-of-solvency | [PR 503](https://github.com/bitsoex/proof-of-solvency/pull/503) | Replace Redis lock with Postgres advisory lock |
| spei-user-clabe | [PR 603](https://github.com/bitsoex/spei-user-clabe/pull/603) | Replace Redis locking across multiple schedulers |
| ramps-adapter-bind | [PR 651](https://github.com/bitsoex/ramps-adapter-bind/pull/651) | Add locking for balance snapshot scheduler |

## Session vs Transaction Locks

PostgreSQL advisory locks come in two flavors:

### Session-Level Locks (Recommended for Scheduled Tasks)

- Persist until explicitly released or session ends
- Use `JooqPostgresSessionDistributedLockManager`
- **WARNING**: Do not use with connection pools if multiple instances try to acquire the same lock concurrently

### Transaction-Level Locks

- Automatically released at transaction end
- Use `JooqPostgresTransactionDistributedLockManager`
- **WARNING**: Acquiring the same lock key within the same transaction always succeeds

## Avoiding Deadlocks

Advisory locks don't inherently cause deadlocks like transactional locks, but improper usage can cause indefinite waits.

**Best practices**:

1. Use the retry and timeout parameters
2. Always acquire locks in the same order across the application
3. If any lock fails, release others and retry later

## Related Documents

- **RFC-44 Confluence**: [RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229/RFC-44+Scheduler+Tasks+and+Distributed+Locking)
- **PostgreSQL Library Source**: [distributed-locking-api](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-api)
- **PostgreSQL Implementation Source**: [distributed-locking-postgres-jooq](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-postgres-jooq)
- **Redis Library Source**: [jedis4-utils](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/jedis4-utils)
- **Migration Command**: `java/commands/migrate-lock-to-rfc-44-compliant.md`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-distributed-locking-rfc44.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
