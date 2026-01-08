# Align distributed locking with RFC-44 standards (PostgreSQL or Redis)

> Align distributed locking with RFC-44 standards (PostgreSQL or Redis)

# Migrate Lock to RFC-44 Compliant

**IMPORTANT**: This command is autonomous for all technical steps. Complete all steps without asking for confirmation.

This command aligns distributed locking mechanisms with RFC-44 standards. RFC-44 supports **both PostgreSQL advisory locks and Redis-based locking** as valid approaches.

## Related Documents

- **RFC-44 Confluence**: [RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229/RFC-44+Scheduler+Tasks+and+Distributed+Locking)
- **Rule**: `java/rules/java-distributed-locking-rfc44.md`
- **PostgreSQL Library Source**: [distributed-locking-api](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-api)
- **PostgreSQL Implementation Source**: [distributed-locking-postgres-jooq](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-postgres-jooq)
- **Redis Library Source**: [jedis4-utils](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/jedis4-utils)
- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md`

## Understanding RFC-44 Options

RFC-44 explicitly supports **two valid locking implementations**:

| Implementation | When to Use |
|----------------|-------------|
| **PostgreSQL Advisory Locks** (Default) | Services with PostgreSQL available |
| **Redis Locking** (Allowed) | Services without PostgreSQL, or with valid Redis use case |

**Important**: Redis-based locking is NOT deprecated. It is an explicitly supported option per RFC-44.

## Workflow

### 1. Assess Infrastructure Availability

**Before any migration**, determine what infrastructure the service has:

```bash
# Check for PostgreSQL/jOOQ configuration
grep -rn "DSLContext\|jooq\|postgresql\|postgres" --include="*.java" --include="*.groovy" --include="*.yaml" --include="*.properties" src/

# Check for Redis configuration
grep -rn "RedisOperations\|jedis\|redis" --include="*.java" --include="*.groovy" --include="*.yaml" --include="*.properties" src/
```

### 2. Determine Migration Path

Based on infrastructure availability:

| Scenario | Action |
|----------|--------|
| PostgreSQL available, no Redis | Migrate to PostgreSQL advisory locks |
| Redis available, no PostgreSQL | Keep Redis, ensure using `jedis4-utils` |
| Both available | **Default**: PostgreSQL. **Alternative**: Redis is acceptable with justification |
| Fabric8 leader election | **Always migrate** to PostgreSQL or Redis |
| Incubated in-repo libraries | **Always migrate** to `jvm-generic-libraries` |

### 3. Identify Legacy Patterns

Search for patterns that need attention:

```bash
# Redis-based locking (valid, but check if PostgreSQL migration is preferred)
grep -rn "tryAutoclosingLock\|RedisLock\|isAcquired" --include="*.java" --include="*.groovy" .

# Fabric8 leader election (MUST migrate)
grep -rn "OnGrantedEvent\|OnRevokedEvent\|isLeader" --include="*.java" --include="*.groovy" .

# Incubated in-repo distributed-locking libraries (MUST migrate)
grep -rn "distributed-locking-api\|distributed-locking-postgres" --include="*.gradle" --include="*.toml" .

# Check for incubated library directories
find . -type d -name "distributed-locking*" 2>/dev/null
```

### 4. Create or Find Jira Ticket

**Before any code changes**, create or find a Jira ticket using the Atlassian MCP tools:

Use `mcp_atlassian_searchJiraIssuesUsingJql` to check for existing tickets:

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "RFC-44" ORDER BY created DESC
```

If none found, create with `mcp_atlassian_createJiraIssue`:

- **Summary**: `Align [repo-name] with RFC-44 distributed locking standards`
- **Parent**: Current Sprint/Cycle KTLO Epic

### 5. Create Feature Branch

```bash
git fetch --all && git pull origin main
git checkout -b "refactor/${JIRA_KEY}-rfc44-distributed-locking"
```

---

## Path A: PostgreSQL Advisory Locks Migration

Use this path when PostgreSQL is available and is the chosen implementation.

### A1. Add Dependencies to Version Catalog

Add to `gradle/libs.versions.toml`:

```toml
[versions]
distributed-locking-api = "2.0.0"
distributed-locking-postgres-jooq = "2.0.0"

[libraries]
distributed-locking-api = { module = "com.bitso.commons:distributed-locking-api", version.ref = "distributed-locking-api" }
distributed-locking-postgres-jooq = { module = "com.bitso.commons:distributed-locking-postgres-jooq", version.ref = "distributed-locking-postgres-jooq" }
```

**Note**: Version 2.0.0 is built for Java 21.

### A2. Add Dependencies to build.gradle

Add to the appropriate module's `build.gradle`:

```groovy
dependencies {
    api libs.distributed.locking.api
    implementation libs.distributed.locking.postgres.jooq
}
```

### A3. Create DistributedLockConfiguration Bean

Create a new configuration class:

```java
package com.bitso.yourservice.config;

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

**Note**: Adjust the `@Qualifier` to match your DSLContext bean name (e.g., `"dslContext"`, `"write-dslcontext"`).

### A4. Migrate Locking Code

#### Before (Redis-based)

```java
@Autowired
@Qualifier("ephemeralRedis")
private RedisOperations<?, ?, ?, ?, ?> redis;

private final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
private final Duration LOCK_TTL = Duration.ofHours(1);

@Scheduled(cron = "${job.cron:-}", zone = "UTC")
public void scheduledJob() {
    log.info("Starting job");
    Try.withResources(() -> redis.tryAutoclosingLock("lock_key", LOCK_TIMEOUT.toMillis(), LOCK_TTL.toMillis()))
        .of(redisLock -> Option.of(redisLock)
            .filter(RedisLock::isAcquired)
            .onEmpty(() -> log.info("Job is already running"))
            .peek(lockAcquired -> doWork()));
    log.info("Job finished");
}
```

#### After (PostgreSQL Advisory Lock)

```java
@Autowired
private final DistributedLockManager<Long> distributedLockManager;

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

#### Alternative Pattern (try-with-resources)

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

### A5. Update Imports

Remove Redis imports (if migrating from Redis):

```java
// Remove these
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;
```

Add PostgreSQL imports:

```java
// Add these
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;
```

### A6. Update Tests

#### Before (Mocking Redis)

```groovy
class ScheduledJobSpec extends Specification {
    @SpringBean
    @Qualifier("ephemeralRedis")
    RedisOperations redis = Mock()

    def "should acquire lock and run job"() {
        given:
        def redisLock = Mock(RedisLock) {
            isAcquired() >> true
        }
        redis.tryAutoclosingLock(_, _, _) >> redisLock

        when:
        job.scheduledJob()

        then:
        // assertions
    }
}
```

#### After (Mocking DistributedLockManager)

```groovy
class ScheduledJobSpec extends Specification {
    DistributedLockManager<Long> distributedLockManager = Mock()

    def "should acquire lock and run job"() {
        given:
        def lock = Mock(DistributedLock) {
            acquired() >> true
        }
        distributedLockManager.tryLock(_) >> lock

        when:
        job.scheduledJob()

        then:
        // assertions
    }
}
```

---

## Path B: Redis Locking (Valid Alternative)

Use this path when:

- Service does not have PostgreSQL available
- Team has justified continued use of Redis for locking
- Service already uses Redis extensively

### B1. Ensure Using jedis4-utils

Verify the service is using `jedis4-utils` from `jvm-generic-libraries`:

```toml
# gradle/libs.versions.toml
[versions]
jedis4-utils = "3.0.0"

[libraries]
jedis4-utils = { module = "com.bitso.commons:jedis4-utils", version.ref = "jedis4-utils" }
```

### B2. Create Redis Lock Configuration (Recommended)

`JedisLockingUtil` implements `DistributedLockManager<String>`, providing a unified API:

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

### B3. Redis Locking Pattern (Unified API)

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

### B4. Redis Locking Pattern (Legacy - Still Valid)

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

---

## Path C: Incubated Library Migration (Always Required)

If the project has incubated distributed-locking libraries in `bitso-libs/` or similar, these **must** be migrated to `jvm-generic-libraries`.

### C1. Delete Incubated Modules

```bash
rm -rf bitso-libs/distributed-locking-api
rm -rf bitso-libs/distributed-locking-postgres-jooq
```

### C2. Remove from settings.gradle

```groovy
// Remove these lines
include 'bitso-libs:distributed-locking-api'
include 'bitso-libs:distributed-locking-postgres-jooq'
```

### C3. Update Dependencies

Update `build.gradle` to use centralized libraries:

```groovy
dependencies {
    // Replace project(':bitso-libs:distributed-locking-api') with:
    api libs.distributed.locking.api
    implementation libs.distributed.locking.postgres.jooq
}
```

### C4. Imports Remain the Same

The package structure is identical, so imports don't need to change:

```java
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;
```

---

## Path D: Fabric8 Leader Election Migration (Always Required)

Fabric8 leader election is **forbidden** per RFC-44 and must be migrated.

### D1. Remove Fabric8 Dependency

```groovy
// Remove this
implementation 'org.springframework.cloud:spring-cloud-kubernetes-fabric8-leader'
```

### D2. Remove Configuration

Remove from `application.yaml`:

```yaml
# Remove this section
spring:
  cloud:
    kubernetes:
      leader:
        enabled: true
```

### D3. Migrate Code

#### Before (Fabric8 Leader Election)

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

#### After (PostgreSQL or Redis)

```java
@Scheduled(fixedRate = 5000)
public void task() {
    try (var lock = distributedLockManager.tryLock("task-lock")) {
        if (lock.acquired()) { work(); }
    }
}
```

---

## Build and Test

```bash
# Build
./gradlew clean build -x test --no-daemon

# Run tests
./gradlew test --no-daemon 2>&1 | tee /tmp/test.log

# Check for failures
grep -E "FAILED|BUILD FAILED" /tmp/test.log && echo "Tests failed" || echo "Tests passed"
```

## Commit and Push

```bash
git add -A
git commit -m "refactor: [$JIRA_KEY] align with RFC-44 distributed locking standards

- Aligned distributed locking with RFC-44 standards
- [Describe specific changes: PostgreSQL migration, Redis validation, incubated lib removal, etc.]

RFC-44: https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229

Generated with the Quality Agent by the /migrate-lock-to-rfc-44-compliant command."

git push -u origin $(git branch --show-current)
```

## Create PR

```bash
gh pr create --draft \
    --title "[$JIRA_KEY] refactor: align with RFC-44 distributed locking standards" \
    --body "## AI-Assisted Refactoring

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Summary

Aligns distributed locking with RFC-44 standards.

## RFC-44 Context

RFC-44 supports **both PostgreSQL advisory locks and Redis-based locking** as valid approaches:
- **PostgreSQL Advisory Locks**: Default recommendation for services with PostgreSQL
- **Redis Locking**: Valid alternative for services without PostgreSQL or with justified Redis use

## Changes

[Describe the specific changes made]

## RFC-44 Reference

[RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229)

## Validation

- [ ] Build passes locally
- [ ] Tests pass locally
- [ ] Reviewed migration against RFC-44 guidance

## AI Agent Details

- **Agent**: Quality Agent
- **Command**: /migrate-lock-to-rfc-44-compliant

Generated with the Quality Agent by the /migrate-lock-to-rfc-44-compliant command."
```

## Troubleshooting

### DSLContext Qualifier Not Found

If you get an error about missing DSLContext bean:

```bash
# Find the DSLContext bean name
grep -rn "DSLContext\|dslContext" --include="*.java" --include="*.groovy" src/
```

Update the `@Qualifier` in your configuration accordingly.

### Connection Pool Warning

**WARNING**: Session-level advisory locks can have issues with connection pools. If multiple instances try to acquire the same lock concurrently, connection pooling can cause unintended lock sharing.

For high-concurrency scenarios, consider:

1. Using transaction-level locks (`JooqPostgresTransactionDistributedLockManager`)
2. Using the retry mechanism with appropriate timeouts

### Lock Not Released

If locks are not being released, ensure you're using try-with-resources or explicitly calling `close()` on the lock.

## Example PRs

These PRs demonstrate successful RFC-44 alignment:

| Repository | PR | Migration Type |
|------------|-----|----------------|
| balance-history | [PR 389](https://github.com/bitsoex/balance-history/pull/389) | Remove incubated library, adopt jvm-generic-libraries |
| balance-checker-v2 | [PR 245](https://github.com/bitsoex/balance-checker-v2/pull/245) | Add distributed locking for scheduled metric emission |
| card-reconciliation | [PR 435](https://github.com/bitsoex/card-reconciliation/pull/435) | Implement Postgres advisory locks for file polling |
| aum-reconciliation-v2 | [PR 646](https://github.com/bitsoex/aum-reconciliation-v2/pull/646) | Add locking for delta computation background job |
| proof-of-solvency | [PR 503](https://github.com/bitsoex/proof-of-solvency/pull/503) | Replace Redis lock with Postgres advisory lock |
| spei-user-clabe | [PR 603](https://github.com/bitsoex/spei-user-clabe/pull/603) | Replace Redis locking across multiple schedulers |
| ramps-adapter-bind | [PR 651](https://github.com/bitsoex/ramps-adapter-bind/pull/651) | Add locking for balance snapshot scheduler |

## Related

- **Rule**: `java/rules/java-distributed-locking-rfc44.md`
- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md`
- **PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md`
