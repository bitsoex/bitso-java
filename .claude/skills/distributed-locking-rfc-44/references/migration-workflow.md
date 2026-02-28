---
title: Migration Workflow
description: Step-by-step guide for migrating to RFC-44 compliant distributed locking
---

# Migration Workflow

Step-by-step guide for migrating to RFC-44 compliant distributed locking.

## Contents

- [Assessment Phase](#assessment-phase)
- [PostgreSQL Migration](#postgresql-migration)
- [Incubated Library Migration](#incubated-library-migration)
- [Fabric8 Leader Election Migration](#fabric8-leader-election-migration)
- [Validation](#validation)
- [Related](#related)

---
## Assessment Phase

### 1. Check Infrastructure Availability

```bash
# Check for PostgreSQL/jOOQ configuration
grep -rn "DSLContext\|jooq\|postgresql" --include="*.java" --include="*.yaml" src/

# Check for Redis configuration
grep -rn "RedisOperations\|jedis\|redis" --include="*.java" --include="*.yaml" src/
```

### 2. Identify Legacy Patterns

```bash
# Redis-based locking (valid, but check if PostgreSQL migration is preferred)
grep -rn "tryAutoclosingLock\|RedisLock\|isAcquired" --include="*.java" .

# Fabric8 leader election (MUST migrate)
grep -rn "OnGrantedEvent\|OnRevokedEvent\|isLeader" --include="*.java" .

# Incubated in-repo libraries (MUST migrate)
grep -rn "distributed-locking-api\|distributed-locking-postgres" --include="*.gradle" .
```

### 3. Determine Migration Path

| Scenario | Action |
|----------|--------|
| PostgreSQL available, no Redis | Migrate to PostgreSQL advisory locks |
| Redis available, no PostgreSQL | Keep Redis, ensure using `jedis4-utils` |
| Both available | **Default**: PostgreSQL. **Alternative**: Redis is acceptable with justification |
| Fabric8 leader election | **Always migrate** to PostgreSQL or Redis |
| Incubated in-repo libraries | **Always migrate** to `jvm-generic-libraries` |

## PostgreSQL Migration

### 1. Add Dependencies

```toml
# gradle/libs.versions.toml
[versions]
distributed-locking-api = "2.0.0"
distributed-locking-postgres-jooq = "2.0.0"

[libraries]
distributed-locking-api = { module = "com.bitso.commons:distributed-locking-api", version.ref = "distributed-locking-api" }
distributed-locking-postgres-jooq = { module = "com.bitso.commons:distributed-locking-postgres-jooq", version.ref = "distributed-locking-postgres-jooq" }
```

```groovy
// build.gradle
dependencies {
    api libs.distributed.locking.api
    implementation libs.distributed.locking.postgres.jooq
}
```

### 2. Create Configuration Bean

```java
@Configuration
public class DistributedLockConfiguration {
    @Bean
    DistributedLockManager<Long> distributedLockManager(
            @Qualifier("write-dslcontext") DSLContext dslContext) {
        return new JooqPostgresSessionDistributedLockManager(dslContext);
    }
}
```

### 3. Migrate Locking Code

**Before (Redis-based)**:

```java
@Autowired
@Qualifier("ephemeralRedis")
private RedisOperations<?, ?, ?, ?, ?> redis;

@Scheduled(cron = "${job.cron:-}", zone = "UTC")
public void scheduledJob() {
    Try.withResources(() -> redis.tryAutoclosingLock("lock_key", 10000, 3600000))
        .of(redisLock -> Option.of(redisLock)
            .filter(RedisLock::isAcquired)
            .onEmpty(() -> log.info("Job is already running"))
            .peek(lockAcquired -> doWork()));
}
```

**After (PostgreSQL Advisory Lock)**:

```java
@Autowired
private DistributedLockManager<Long> distributedLockManager;

@Scheduled(cron = "${job.cron:-}", zone = "UTC")
public void scheduledJob() {
    Try.withResources(() -> distributedLockManager.tryLock("lock_key"))
        .of(lock -> Option.of(lock)
            .filter(DistributedLock::acquired)
            .onEmpty(() -> log.info("Job is already running"))
            .peek(lockAcquired -> doWork()));
}
```

### 4. Update Imports

```java
// Remove
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;

// Add
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;
```

### 5. Update Tests

**Before (Mocking Redis)**:

```groovy
class ScheduledJobSpec extends Specification {
    @SpringBean
    @Qualifier("ephemeralRedis")
    RedisOperations redis = Mock()

    def "should acquire lock and run job"() {
        given:
        def redisLock = Mock(RedisLock) { isAcquired() >> true }
        redis.tryAutoclosingLock(_, _, _) >> redisLock

        when:
        job.scheduledJob()

        then:
        // assertions
    }
}
```

**After (Mocking DistributedLockManager)**:

```groovy
class ScheduledJobSpec extends Specification {
    DistributedLockManager<Long> distributedLockManager = Mock()

    def "should acquire lock and run job"() {
        given:
        def lock = Mock(DistributedLock) { acquired() >> true }
        distributedLockManager.tryLock(_) >> lock

        when:
        job.scheduledJob()

        then:
        // assertions
    }
}
```

## Incubated Library Migration

If the project has incubated distributed-locking libraries:

### 1. Delete Incubated Modules

```bash
rm -rf bitso-libs/distributed-locking-api
rm -rf bitso-libs/distributed-locking-postgres-jooq
```

### 2. Remove from settings.gradle

```groovy
// Remove these lines
include 'bitso-libs:distributed-locking-api'
include 'bitso-libs:distributed-locking-postgres-jooq'
```

### 3. Update Dependencies

```groovy
dependencies {
    // Replace project(':bitso-libs:distributed-locking-api') with:
    api libs.distributed.locking.api
    implementation libs.distributed.locking.postgres.jooq
}
```

> **Note**: Package structure is identical, so imports don't need to change.

## Fabric8 Leader Election Migration

### 1. Remove Fabric8 Dependency

```groovy
// Remove
implementation 'org.springframework.cloud:spring-cloud-kubernetes-fabric8-leader'
```

### 2. Remove Configuration

```yaml
# Remove from application.yaml
spring:
  cloud:
    kubernetes:
      leader:
        enabled: true
```

### 3. Migrate Code

**Before (Fabric8 Leader Election)**:

```java
private boolean isLeader = false;

@EventListener(OnGrantedEvent.class)
public void onGranted(OnGrantedEvent e) { isLeader = true; }

@EventListener(OnRevokedEvent.class)
public void onRevoked(OnRevokedEvent e) { isLeader = false; }

@Scheduled(fixedRate = 5000)
public void task() {
    if (isLeader) { work(); }
}
```

**After (PostgreSQL or Redis)**:

```java
@Scheduled(fixedRate = 5000)
public void task() {
    try (var lock = distributedLockManager.tryLock("task-lock")) {
        if (lock.acquired()) { work(); }
    }
}
```

## Validation

```bash
# Build
./gradlew clean build -x test --no-daemon

# Run tests
./gradlew test --no-daemon 2>&1 | tee /tmp/test.log

# Check for failures
grep -E "FAILED|BUILD FAILED" /tmp/test.log && echo "Tests failed" || echo "Tests passed"
```

## Related

- [Lock Patterns](lock-patterns.md) - Detailed locking patterns
- [Redis Integration](redis-integration.md) - Redis locking option
- [Troubleshooting](troubleshooting.md) - Common issues
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/distributed-locking-rfc-44/references/migration-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

