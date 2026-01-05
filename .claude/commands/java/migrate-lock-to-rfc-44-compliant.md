# Migrate distributed locking to RFC-44 compliant PostgreSQL advisory locks

**Description:** Migrate distributed locking to RFC-44 compliant PostgreSQL advisory locks

# 🤖 ♻️ Migrate Lock to RFC-44 Compliant

**IMPORTANT**: This command is autonomous for all technical steps. Complete all steps without asking for confirmation.

This command migrates legacy distributed locking mechanisms (Redis-based, Fabric8 leader election, or incubated in-repo libraries) to the RFC-44 compliant PostgreSQL advisory locks.

## Related Documents

- **RFC-44 Confluence**: [RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229/RFC-44+Scheduler+Tasks+and+Distributed+Locking)
- **Rule**: `java/rules/java-distributed-locking-rfc44.md`
- **Library Source**: [distributed-locking-api](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-api)
- **Implementation Source**: [distributed-locking-postgres-jooq](https://github.com/bitsoex/jvm-generic-libraries/tree/master/libs/commons/distributed-locking-postgres-jooq)
- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md`

## Prerequisites

1. **PostgreSQL database available** - The service must have a PostgreSQL connection
2. **jOOQ configured** - DSLContext bean must be available
3. **Gradle project** - Using version catalog (`gradle/libs.versions.toml`)
4. **Jira ticket created or found** - Follow `global/rules/jira-ticket-workflow.md` for ticket creation

## Workflow

### 1. Create or Find Jira Ticket

**Before any code changes**, create or find a Jira ticket using the Atlassian MCP tools:

Use `mcp_atlassian_searchJiraIssuesUsingJql` to check for existing tickets:

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "RFC-44" ORDER BY created DESC
```

If none found, create with `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 ♻️ Migrate [repo-name] to RFC-44 compliant distributed locking`
- **Parent**: Current Sprint/Cycle KTLO Epic

### 2. Create Feature Branch

```bash
git fetch --all && git pull origin main
git checkout -b "refactor/${JIRA_KEY}-rfc44-distributed-locking"
```

### 3. Identify Legacy Locking Patterns

Search for deprecated patterns:

```bash
# Redis-based locking
grep -rn "tryAutoclosingLock\|RedisLock\|isAcquired" --include="*.java" --include="*.groovy" .

# Fabric8 leader election
grep -rn "OnGrantedEvent\|OnRevokedEvent\|isLeader" --include="*.java" --include="*.groovy" .

# Incubated in-repo distributed-locking libraries
grep -rn "distributed-locking-api\|distributed-locking-postgres" --include="*.gradle" --include="*.toml" .

# Check for incubated library directories
find . -type d -name "distributed-locking*" 2>/dev/null
```

### 4. Add Dependencies to Version Catalog

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

### 5. Add Dependencies to build.gradle

Add to the appropriate module's `build.gradle`:

```groovy
dependencies {
    api libs.distributed.locking.api
    implementation libs.distributed.locking.postgres.jooq
}
```

### 6. Create DistributedLockConfiguration Bean

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

### 7. Migrate Locking Code

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

### 8. Update Imports

Remove deprecated imports:

```java
// ❌ Remove these
import com.bitso.util.redis.RedisLock;
import com.bitso.util.redis.RedisOperations;
```

Add new imports:

```java
// ✅ Add these
import com.bitso.distributed.locking.DistributedLock;
import com.bitso.distributed.locking.DistributedLockManager;
```

### 9. Update Tests

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

### 10. Remove Incubated Libraries (If Present)

If the project has incubated distributed-locking libraries in `bitso-libs/` or similar:

1. **Delete the incubated modules**:

```bash
rm -rf bitso-libs/distributed-locking-api
rm -rf bitso-libs/distributed-locking-postgres-jooq
```

1. **Remove from `settings.gradle`**:

```groovy
// ❌ Remove these lines
include 'bitso-libs:distributed-locking-api'
include 'bitso-libs:distributed-locking-postgres-jooq'
```

1. **Update references in other modules** to use the centralized library.

### 11. Remove Unused Redis Dependencies (Optional)

If Redis was only used for locking, remove the dependency:

```groovy
// ❌ Remove if only used for locking
implementation libs.bitso.commons.redis
```

Also remove `@Qualifier("ephemeralRedis")` injections.

### 12. Build and Test

```bash
# Build
./gradlew clean build -x test --no-daemon

# Run tests
./gradlew test --no-daemon 2>&1 | tee /tmp/test.log

# Check for failures
grep -E "FAILED|BUILD FAILED" /tmp/test.log && echo "❌ Tests failed" || echo "✅ Tests passed"
```

### 13. Commit and Push

```bash
git add -A
git commit -m "🤖 ♻️ refactor: [$JIRA_KEY] migrate to RFC-44 compliant distributed locking

- Replaced Redis-based locking with PostgreSQL advisory locks
- Added distributed-locking-api and distributed-locking-postgres-jooq dependencies
- Created DistributedLockConfiguration bean
- Updated scheduled tasks to use DistributedLockManager
- Updated tests to mock DistributedLockManager

RFC-44: https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229

Generated with the Quality Agent by the /migrate-lock-to-rfc-44-compliant command."

git push -u origin $(git branch --show-current)
```

### 14. Create PR

```bash
gh pr create --draft \
    --title "🤖 ♻️ [$JIRA_KEY] refactor: migrate to RFC-44 compliant distributed locking" \
    --body "## 🤖 AI-Assisted Refactoring

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Summary

Migrates distributed locking to RFC-44 compliant PostgreSQL advisory locks.

## Changes

- Added \`distributed-locking-api\` and \`distributed-locking-postgres-jooq\` dependencies (v2.0.0)
- Created \`DistributedLockConfiguration\` Spring bean
- Replaced \`RedisOperations.tryAutoclosingLock()\` with \`DistributedLockManager.tryLock()\`
- Updated tests to mock \`DistributedLockManager<Long>\`

## RFC-44 Reference

[RFC-44: Scheduler Tasks and Distributed Locking](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4743987229)

## Validation

- [ ] Build passes locally
- [ ] Tests pass locally
- [ ] Reviewed migration against example PRs

## Example PRs

| Repository | PR | Description |
|------------|-----|-------------|
| balance-history | [PR 389](https://github.com/bitsoex/balance-history/pull/389) | Remove incubated library |
| proof-of-solvency | [PR 503](https://github.com/bitsoex/proof-of-solvency/pull/503) | Replace Redis lock |
| spei-user-clabe | [PR 603](https://github.com/bitsoex/spei-user-clabe/pull/603) | Replace Redis locking |

## AI Agent Details

- **Agent**: Quality Agent
- **Command**: /migrate-lock-to-rfc-44-compliant

Generated with the Quality Agent by the /migrate-lock-to-rfc-44-compliant command."
```

## Common Migration Scenarios

### Scenario 1: Scheduled Task with Redis Lock

**Before**:

```java
@Scheduled(cron = "${job.cron:-}")
public void job() {
    Try.withResources(() -> redis.tryAutoclosingLock("key", 10000, 3600000))
        .of(lock -> Option.of(lock).filter(RedisLock::isAcquired).peek(l -> work()));
}
```

**After**:

```java
@Scheduled(cron = "${job.cron:-}")
public void job() {
    Try.withResources(() -> distributedLockManager.tryLock("key"))
        .of(lock -> Option.of(lock).filter(DistributedLock::acquired).peek(l -> work()));
}
```

### Scenario 2: Simple try-with-resources

**Before**:

```java
try (RedisLock lock = redis.tryAutoclosingLock("key", timeout, ttl)) {
    if (lock.isAcquired()) {
        work();
    }
}
```

**After**:

```java
try (var lock = distributedLockManager.tryLock("key")) {
    if (lock.acquired()) {
        work();
    }
}
```

### Scenario 3: Fabric8 Leader Election Migration

**Before**:

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

**After**:

```java
@Scheduled(fixedRate = 5000)
public void task() {
    try (var lock = distributedLockManager.tryLock("task-lock")) {
        if (lock.acquired()) { work(); }
    }
}
```

### Scenario 4: Incubated In-Repo Library Migration

If your repo has `bitso-libs/distributed-locking-api` or similar:

1. Delete the local directories
2. Remove from `settings.gradle`
3. Update `build.gradle` to use `libs.distributed.locking.api`
4. Imports remain the same (`com.bitso.distributed.locking.*`)

See [balance-history PR 389](https://github.com/bitsoex/balance-history/pull/389) for a complete example.

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

These PRs demonstrate successful RFC-44 migrations:

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
