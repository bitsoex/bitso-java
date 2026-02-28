# Integration Tests Migration Workflow

Complete step-by-step guide for migrating existing integration tests or creating new integration test infrastructure from scratch.

## Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Configuration Centralization Principle](#configuration-centralization-principle)
- [1.1 Discover Service Structure](#11-discover-service-structure)
- [1.2 Identify Service Type](#12-identify-service-type)
- [1.3 Classify Dependencies](#13-classify-dependencies)
- [1.4 Identify Required Infrastructure](#14-identify-required-infrastructure)
- [1.5 Create Analysis Report](#15-create-analysis-report)
- [1.6 Discover Integration Tests in Scope](#16-discover-integration-tests-in-scope)
- [1.7 CHECKPOINT](#17-checkpoint)
- [2.1 Add Dependencies](#21-add-dependencies)
- [2.2 Create application-test.yml](#22-create-application-testyml)
- [2.3 Create BaseIntegrationSpec](#23-create-baseintegrationspec)
- [3A: gRPC Services](#3a-grpc-services)
- [3A-EXT: GrpcMock for External gRPC Services (DEFAULT)](#3a-ext-grpcmock-for-external-grpc-services-default)
- [3B: REST APIs](#3b-rest-apis)
- [3C: Kafka Consumers](#3c-kafka-consumers)
- [3D: Managing Infrastructure Lifecycle](#3d-managing-infrastructure-lifecycle)
- [Remove (Old Pattern)](#remove-old-pattern)
- [Create (New Pattern)](#create-new-pattern)
- [Key Transformations](#key-transformations)
- [Controller Unit Tests → Integration Tests](#controller-unit-tests--integration-tests)
- [Configuration Consolidation Steps](#configuration-consolidation-steps)
- [Run Tests](#run-tests)
- [Verify](#verify)
- [6.1 Handle Production Bugs](#61-handle-production-bugs)
- [6.2 Spawn Senior Java Engineer Review](#62-spawn-senior-java-engineer-review)
- [6.3 Generate Migration Report](#63-generate-migration-report)
- [Optional: Container Reuse (Local Development Only)](#optional-container-reuse-local-development-only)
- [Analysis (Phase 1)](#analysis-phase-1)
- [Infrastructure (Phase 2)](#infrastructure-phase-2)
- [GrpcMock Setup (if testing client resilience)](#grpcmock-setup-if-testing-client-resilience)
- [Service-Type Setup](#service-type-setup)
- [Tests](#tests)
- [Cleanup (if migrating)](#cleanup-if-migrating)
- [Migration Tracking (Phase 4)](#migration-tracking-phase-4)
- [Review and Report (Phase 6)](#review-and-report-phase-6)

---
## Overview

This workflow guides you through analyzing a service, setting up proper integration test infrastructure, and migrating existing tests. Follow the phases in order for best results.

## Prerequisites

Before starting, ensure you have:
- **JDK 17+** (or JDK 21+ for latest Spring Boot)
- **Docker** running (required for Testcontainers)
- **Gradle 8.x+** (for version catalog support)

## Configuration Centralization Principle

**PREFER:** All test configuration consolidated in `BaseIntegrationSpec`
**AVOID:** Custom `@TestConfiguration` classes, multiple context configs, per-test bean definitions

```
┌─────────────────────────────────────────────────────────────────────┐
│  GOOD: Centralized Configuration                                    │
│                                                                     │
│  BaseIntegrationSpec                                                │
│  ├── All @MockitoBean/@SpringBean declarations                      │
│  ├── All Testcontainers (static)                                    │
│  ├── @DynamicPropertySource for all properties                      │
│  ├── Common test beans (@Bean methods or @Import)                   │
│  ├── setup() with mock resets                                       │
│  └── cleanup() with data clearing                                   │
│                                                                     │
│  ConcreteTestSpec extends BaseIntegrationSpec                       │
│  └── Only test methods, no configuration                            │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│  BAD: Scattered Configuration                                       │
│                                                                     │
│  TestContext.java           ← Separate config class                 │
│  CustomTestConfig.java      ← Another config class                  │
│  TestSpec.groovy                                                    │
│  ├── @Import([TestContext, CustomTestConfig, ...])                  │
│  ├── @MockitoBean (duplicated in each test)                         │
│  └── @TestConfiguration inner class                                 │
└─────────────────────────────────────────────────────────────────────┘
```

**Why Centralize?**
- Single source of truth for test infrastructure
- Easier to maintain and update mocks
- Prevents "which config do I need?" confusion
- Avoids Spring context explosion from different config combinations
- Makes it clear what's mocked vs real across all tests

---

# PHASE 1: ANALYSIS

**CRITICAL: Understand the service before making changes.**

## 1.1 Discover Service Structure

```bash
# Find the main application class
find . -path "*/src/main/java/*" -name "*Application.java" | head -5

# Find context/configuration classes (these define beans)
find . -path "*/src/main/java/*" -name "*Context.java" | head -10
find . -path "*/src/main/java/*" -name "*Configuration.java" | head -10

# Find existing test setup (if migrating)
find . -path "*/src/test/*" -name "TestContext.groovy" -o -name "TestContext.java" | head -5
find . -path "*/src/test/*" -name "*Spec.groovy" -o -name "*Test.java" | head -20

# Identify service type
find . -path "*/src/main/java/*" -name "*Handler.java" | head -5      # gRPC
find . -path "*/src/main/java/*" -name "*Controller.java" | head -5   # REST
find . -path "*/src/main/java/*" -name "*Listener.java" | head -5     # Kafka
```

## 1.2 Identify Service Type

| Type | Indicators | Test Approach |
|------|------------|---------------|
| **gRPC** | `*Handler.java`, `*.proto` files | In-process gRPC with blocking stubs |
| **REST** | `*Controller.java`, `@RestController` | `MockMvc` or `TestRestTemplate` |
| **Kafka Consumer** | `*Listener.java`, `@KafkaListener` | `@EmbeddedKafka` (preferred) or Kafka Testcontainer |
| **Batch/Scheduled** | `@Scheduled`, `*Job.java` | Direct method invocation |
| **Mixed** | Multiple types | Separate base specs per type |

## 1.3 Classify Dependencies

Read each Context/Configuration class and classify every bean:

### EXTERNAL Dependencies (MOCK THESE)

**Mock strategy hierarchy: GrpcMock/WireMock > @MockitoBean/@SpringBean**

Always prefer GrpcMock (for gRPC) or WireMock (for REST) over bean mocking. This tests the **real client wrapper** code (serialization, error mapping, retries). Use @MockitoBean/@SpringBean **only** for ProtoShims/custom RPCs where GrpcMock/WireMock is not feasible.

| Indicator | Examples | Mock Strategy |
|-----------|----------|---------------|
| Class ends in `*Client`, `*GrpcClient` | `OrderClient`, `WalletGrpcClient` | **GrpcMock** - point client to GrpcMock server |
| Uses HTTP client internally | `ExchangeService`, `BitsoFloatService` | **WireMock** - point client to WireMock server |
| Has `host/port` configuration (gRPC) | `@Value("${service.host}")` | **GrpcMock** - override via `@DynamicPropertySource` |
| Has `host/port` configuration (REST) | `@Value("${api.url}")` | **WireMock** - override via `@DynamicPropertySource` |
| Class ends in `*Model` with `ProtoShim` | `UserModel`, `TradeModel` | **@MockitoBean/@SpringBean** - custom RPC, not mockable via GrpcMock |
| Kafka producer | `KafkaTemplate`, `*Producer` | **@MockitoBean/@SpringBean** - no network mock available |

### INTERNAL Dependencies (DO NOT MOCK)

| Indicator | Examples | Why Real |
|-----------|----------|----------|
| Orchestrates other beans | `TransferService`, `ConversionService` | Business logic |
| Uses local repositories | `QuoteService`, `OrderService` | Local DB/Redis |
| Handlers/Controllers | `ConversionHandler`, `UserController` | Test subject |
| Utilities | `MetricUtils`, validators, mappers | No I/O |

### INFRASTRUCTURE (Special Handling)

| Type | Examples | Action |
|------|----------|--------|
| Background loop consumers | `MessagesLoopClient` | Mock OR manage lifecycle |
| Redis stream consumers | `@Named("balanceUpdate")` beans | Mock OR start/stop in setup/cleanup |
| Kafka listeners (when not testing them) | `@KafkaListener` beans | Disable via properties |
| Scheduled tasks | `@Scheduled` methods | Disable or control timing |

## 1.4 Identify Required Infrastructure

| If Service Uses | You Need |
|-----------------|----------|
| PostgreSQL / jOOQ | `PostgreSQLContainer` + Flyway |
| Redis / Jedis | `GenericContainer` with Valkey image |
| Kafka | `@EmbeddedKafka` (preferred) or `KafkaContainer` |
| S3 | LocalStack or mock |
| External gRPC | **GrpcMock** (preferred) - `@MockitoBean`/`@SpringBean` only for ProtoShims |
| External REST | **WireMock** (preferred) - `@MockitoBean`/`@SpringBean` only if not feasible |

## 1.5 Create Analysis Report

**Output this report before proceeding:**

```markdown
## Analysis Report for {service-name}

### Service Type
- [ ] gRPC Service
- [ ] REST API
- [ ] Kafka Consumer
- [ ] Batch/Scheduled Job
- [ ] Mixed

### Service Module Path
`bitso-services/{service-name}`

### Main Application Class
`com.bitso.{service}.{Service}Application`

### External Dependencies (to be mocked):

**Via GrpcMock (preferred - tests real client wrapper):**
1. `ExternalClient1` - gRPC client to other-service → GrpcMock stub
2. `OrderClient` - gRPC client to orders → GrpcMock stub

**Via @MockitoBean/@SpringBean (only for ProtoShims/custom RPCs):**
3. `UserModel` - ProtoShim wrapper (custom RPC, not mockable via GrpcMock)
4. `TradeModel` - ProtoShim with server streaming

**Via @MockitoBean/@SpringBean (no network mock available):**
5. `KafkaProducer` - sends events to Kafka

### Internal Dependencies (use real implementations):
1. `QuoteService` - business logic orchestrator
2. `ConversionRepository` - local Redis repository

### Infrastructure Required:
- [ ] PostgreSQL Testcontainer
- [ ] Redis/Valkey Testcontainer (GenericContainer)
- [ ] @EmbeddedKafka

### Infrastructure Beans to Mock or Manage:
1. `MessagesLoopClient("balanceUpdate")` - background consumer

### Test Entry Points:
- gRPC: `QuoteServiceGrpc.QuoteServiceBlockingStub`
- REST: `MockMvc` for `/api/v1/quotes`
- Kafka: Send to topic via `@EmbeddedKafka`

### Existing Tests to Migrate (if any):
1. `QuoteHandlerSpec.groovy` → `QuoteHandlerIntegrationSpec.groovy`
```

## 1.6 Discover Integration Tests in Scope

**CRITICAL: Find ALL integration tests that need migration.**

This step is essential for ensuring complete migration. Every test using @SpringBootTest MUST be migrated to extend BaseIntegrationSpec or its subclasses.

```bash
# Find all tests using @SpringBootTest
grep -r "@SpringBootTest" --include="*.groovy" --include="*.java" \
  bitso-services/{service-name}/src/test/ | cut -d: -f1 | sort -u

# Also check for @ContextConfiguration (older pattern)
grep -r "@ContextConfiguration" --include="*.groovy" --include="*.java" \
  bitso-services/{service-name}/src/test/ | cut -d: -f1 | sort -u
```

**Naming Convention:**

| Language | Required Suffix | Example |
|----------|-----------------|---------|
| **Java** | `IntegrationTest` | `QuoteHandlerIntegrationTest.java` |
| **Groovy** | `IntegrationSpec` | `QuoteHandlerIntegrationSpec.groovy` |

**Create Migration Tracking Table:**

```markdown
## Integration Tests to Migrate

**GOAL: Migrate ALL tests listed below. No test should remain with PENDING status at completion.**

| # | Current Name | New Name | Base Class | Custom Configs | Status | Notes |
|---|--------------|----------|------------|----------------|--------|-------|
| 1 | `QuoteHandlerSpec` | `QuoteHandlerIntegrationSpec` | `Specification` | `TestContext` | PENDING | Needs rename, consolidate config |
| 2 | `OrderServiceTest` | `OrderServiceIntegrationTest` | `SpringBootTest` | None | PENDING | Java test |
| 3 | `PaymentIntegrationSpec` | *(no change)* | `Specification` | `PaymentTestConfig` | PENDING | Move beans to base |
| ... | ... | ... | ... | ... | ... | ... |

Legend:
- PENDING: Not yet migrated
- MIGRATED: Successfully migrated (and renamed if needed)
- SKIPPED: Cannot migrate (explain why in Notes - requires user approval)
- BLOCKED: Blocked by production bug (create ticket, document in Notes)

Custom Configs Column:
- List any @Import, @ContextConfiguration, or @TestConfiguration used
- These should be consolidated into BaseIntegrationSpec during migration

**IMPORTANT:** At the end of Phase 4, ALL tests must be in MIGRATED, SKIPPED, or BLOCKED status. No PENDING tests should remain.
```

## 1.7 CHECKPOINT

**Ask user to confirm:**
1. Analysis is correct
2. List of tests to migrate is complete
3. Proceed with infrastructure setup

---

# PHASE 2: INFRASTRUCTURE SETUP

**Detailed guides:**
- [testcontainers-setup](testcontainers-setup.md) - Container patterns and options
- [base-spec-patterns](base-spec-patterns.md) - BaseIntegrationSpec patterns

## 2.1 Add Dependencies

Update `bitso-services/{service-name}/build.gradle`:

```groovy
dependencies {
    // Core test dependencies
    testImplementation "org.springframework.boot:spring-boot-starter-test"
    testImplementation libs.fromVersionsGradle.org.spockframework.spock.core.spock24
    testImplementation libs.fromVersionsGradle.org.spockframework.spock.spring.spock24

    // Repository module (if exists)
    testImplementation project(':bitso-libs:{service-name}:repository')

    // Testcontainers
    // NOTE: spring-boot-testcontainers enables @ServiceConnection and jdbc:tc: URL pattern
    // Prefer jdbc:tc: URL pattern in application-test.yml for simpler setup
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:spock'

    // Redis/Valkey container - prefer Valkey image with GenericContainer or RedisContainer
    // Option 1: GenericContainer with Valkey (simpler, recommended)
    // Option 2: testImplementation 'com.redis.testcontainers:testcontainers-redis' (cleaner API)

    // Kafka - use @EmbeddedKafka (preferred, faster)
    testImplementation 'org.springframework.kafka:spring-kafka-test'

    // Flyway (if using PostgreSQL)
    testImplementation 'org.flywaydb:flyway-core'
    testImplementation 'org.flywaydb:flyway-database-postgresql'

    // GrpcMock - WireMock-like stubbing for external gRPC services
    // Use when testing real client resilience patterns (retries, circuit breakers, timeouts)
    testImplementation 'org.grpcmock:grpcmock-core:0.14.0'
    testImplementation 'org.grpcmock:grpcmock-junit5:0.14.0'

    // Awaitility for async assertions
    testImplementation 'org.awaitility:awaitility'
}
```

## 2.2 Create application-test.yml

Create `src/test/resources/application-test.yml`:

```yaml
spring:
  main:
    allow-bean-definition-overriding: true

  # PostgreSQL - PREFERRED: Use jdbc:tc: URL pattern (auto-creates containers)
  # This is simpler than explicit PostgreSQLContainer in BaseIntegrationSpec
  datasources:
    write-datasource:
      url: jdbc:tc:postgresql:14.5:///testdb
      username: test
      password: test
      driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    read-datasource:
      url: jdbc:tc:postgresql:14.5:///testdb
      username: test
      password: test
      driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver

  # Flyway (if using explicit container instead of jdbc:tc:)
  # flyway:
  #   enabled: true
  #   locations: filesystem:../../flyway/db/migrations
  #   clean-disabled: false  # TEST CONFIG ONLY - allows DROP ALL TABLES

  # Kafka (if using)
  kafka:
    consumer:
      auto-offset-reset: earliest

# gRPC configuration
grpc:
  server:
    port: -1  # Disable socket server for in-process testing
    in-process-name: test-grpc-server
  client:
    inProcess:
      address: in-process:test-grpc-server
      negotiation-type: PLAINTEXT

# Disable background consumers (adjust to your service)
{service-name}:
  kafka:
    consumers:
      {consumer-name}:
        enabled: false

# Optional: Debug logging for troubleshooting
logging:
  level:
    org.springframework: INFO
    com.bitso.{servicename}: DEBUG  # Use package name format (no hyphens)
```

## 2.3 Create BaseIntegrationSpec

Create `src/test/groovy/com/bitso/{servicename}/BaseIntegrationSpec.groovy`:

**Choose your mock approach:**

### IMPORTANT: Mock Framework Differences

| Approach | Annotation | Mock Syntax | Verification | When to Use |
|----------|------------|-------------|--------------|-------------|
| **Mockito** | `@MockitoBean` | `when(mock.method()).thenReturn(value)` | `verify(mock).method()` | Spring Boot standard |
| **Spock** | `@SpringBean` | `mock.method() >> value` | `1 * mock.method()` | Spock native |

**WARNING:** You cannot mix syntaxes! With `@MockitoBean`, Spock's `>>` syntax will NOT work.

### Option A: Using @MockitoBean (Recommended for Spring Boot 3.4+)

```groovy
package com.bitso.{servicename}

import com.bitso.{servicename}.{ServiceName}Application
// Import ALL external clients identified in Phase 1
import com.bitso.external.ExternalClient1
import com.bitso.external.ExternalClient2

import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean

// Testcontainers - Use GenericContainer with Valkey image (preferred)
// Alternative: 'com.redis.testcontainers:testcontainers-redis' for RedisContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.JedisPooled
import spock.lang.Specification

import jakarta.inject.Named
import java.time.Duration

import static org.mockito.Mockito.reset

/**
 * Base integration test specification.
 *
 * ALL test configuration is centralized here:
 * - External client mocks (@MockitoBean)
 * - Testcontainers (PostgreSQL, Redis)
 * - Common test beans (MeterRegistry, ObjectMapper)
 * - Property configuration (@DynamicPropertySource)
 * - Mock reset (setup) and data cleanup (cleanup)
 *
 * IMPORTANT: Do NOT use @Transactional on test classes or methods.
 * Let transactions commit naturally to test real behavior.
 */
@SpringBootTest(classes = [{ServiceName}Application])
@ActiveProfiles("test")
class BaseIntegrationSpec extends Specification {

    // ============================================
    // TEST BEANS - ONLY ADD IF SPRING BOOT DOESN'T AUTO-CONFIGURE
    // ============================================
    // Spring Boot auto-configures most beans (MeterRegistry, ObjectMapper, etc.)
    // Only add @TestConfiguration if you get NoSuchBeanDefinitionException
    //
    // PREFER: Rely on Spring Boot auto-configuration
    // AVOID: Adding beans that Spring Boot already provides
    //
    // If you DO need custom beans, add them here (not in separate files):
    //
    // @TestConfiguration
    // static class TestBeans {
    //     @Bean
    //     @Primary
    //     SomeCustomBean customBean() {
    //         return new SomeCustomBean()
    //     }
    // }

    // ============================================
    // EXTERNAL CLIENTS - MOCK STRATEGY
    //
    // PREFER: GrpcMock (gRPC) or WireMock (REST) for external services
    //         → Tests real client wrappers, resilience, error codes
    //         → See Section 3A-EXT for GrpcMock setup
    //
    // ONLY USE @MockitoBean for:
    //   - ProtoShims/custom RPCs (not mockable via GrpcMock)
    //   - Kafka producers (no network mock available)
    //
    // With @MockitoBean use Mockito syntax: when().thenReturn()
    // Spock's >> syntax does NOT work with @MockitoBean!
    // ============================================

    // ProtoShim beans - can't use GrpcMock (custom RPC protocol)
    @MockitoBean
    UserModel userModel              // ProtoShim

    @MockitoBean
    TradeModel tradeModel            // ProtoShim - server streaming

    // Standard gRPC clients → use GrpcMock instead (see Section 3A-EXT)
    // Do NOT mock these with @MockitoBean:
    // ExternalClient1 → point to GrpcMock via @DynamicPropertySource
    // ExternalClient2 → point to GrpcMock via @DynamicPropertySource

    // For multiple beans of same type, use @Qualifier or @Named:
    // @MockitoBean
    // @Qualifier("primaryClient")
    // ExternalClient primaryClient
    //
    // NOTE: @Named is from jakarta.inject, @Qualifier is from Spring
    // Prefer @Named when matching production beans that use @Named annotations
    // Prefer @Qualifier for Spring-specific beans or gRPC stubs

    // ============================================
    // TESTCONTAINERS
    // Use static containers shared across all tests
    // NOTE: Static containers are NOT annotated with @Shared because they're
    // initialized in a static block, not by Spock's lifecycle
    // ============================================

    // Valkey/Redis - Use GenericContainer with Valkey image
    // Valkey is a Redis-compatible fork, preferred for new projects
    static final GenericContainer<?> REDIS_CONTAINER

    static {
        try {
            // Valkey (Redis-compatible)
            REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("valkey/valkey:7.2-alpine"))
                    .withExposedPorts(6379)
                    .withStartupTimeout(Duration.ofMinutes(2))
            REDIS_CONTAINER.start()

            if (!REDIS_CONTAINER.isRunning()) {
                throw new IllegalStateException("Valkey container failed to start")
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to start Valkey container. Ensure Docker is running.", e)
        }
    }

    // Helper methods for container properties
    protected static String getRedisHost() {
        return REDIS_CONTAINER.getHost()
    }

    protected static Integer getRedisPort() {
        return REDIS_CONTAINER.getFirstMappedPort()
    }

    // For cleanup - inject DSLContext instead of creating new one
    @Autowired(required = false)
    DSLContext dslContext

    @Autowired(required = false)
    @Named("{serviceName}Redis")
    JedisPooled jedisPooled

    def setup() {
        // IMPORTANT: Reset all bean mocks before each test (ProtoShims only)
        // Filter nulls to handle optional mocks in subclasses
        def mocksToReset = [userModel, tradeModel].findAll { it != null }
        if (mocksToReset) {
            reset(*mocksToReset)  // Groovy spread operator - equivalent to reset(mock1, mock2, ...)
        }

        // Clean data from previous tests (in setup to ensure clean state even after test failures)
        jedisPooled?.flushAll()
        cleanupDatabase()

        // Set up default mock behaviors if needed
        setupDefaultMockBehaviors()
    }

    def cleanup() {
        // Reset GrpcMock stubs (if using GrpcMock for external services)
        // resetGrpcMock()
    }

    /**
     * Override in subclasses to set up default mock behaviors.
     * Use Mockito syntax: when(mock.method()).thenReturn(value)
     */
    protected void setupDefaultMockBehaviors() {
        // Example:
        // when(externalClient1.healthCheck()).thenReturn(true)
    }

    /**
     * Truncate database tables for test isolation.
     * Order tables to respect foreign key constraints (children first).
     */
    private void cleanupDatabase() {
        if (dslContext == null) return

        try {
            dslContext.transaction { configuration ->
                def ctx = configuration.dsl()

                // Truncate in correct order (children before parents)
                // Example:
                // ctx.truncate(CONVERSION_ORDER).cascade().execute()
                // ctx.truncate(CONVERSION).cascade().execute()
            }
        } catch (Exception e) {
            // Log but don't fail - next test might still work
            System.err.println("Database cleanup failed: " + e.getMessage())
        }
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        // ============================================
        // FLYWAY (if using PostgreSQL)
        // NOTE: Using Groovy closure syntax { } below (idiomatic for .groovy files)
        // For Java files, use lambda syntax: () -> getRedisHost()
        // ============================================

        // ============================================
        // REDIS/VALKEY
        // ============================================
        registry.add("redis.{service-name}.host", { getRedisHost() })
        registry.add("redis.{service-name}.port", { getRedisPort() })
        registry.add("redis.{service-name}-ro.host", { getRedisHost() })
        registry.add("redis.{service-name}-ro.port", { getRedisPort() })

        // ============================================
        // POSTGRESQL - Use jdbc:tc: in application-test.yml instead
        // Only use explicit container if you need Flyway with custom migrations
        // ============================================
        // If using explicit PostgreSQLContainer, add properties here:
        // registry.add("spring.datasource.url", { POSTGRES_CONTAINER.jdbcUrl })
        // registry.add("spring.datasource.username", { POSTGRES_CONTAINER.username })
        // registry.add("spring.datasource.password", { POSTGRES_CONTAINER.password })
    }
}
```

### Option B: Using @SpringBean (Spock Native)

**IMPORTANT: Spring Context Caching with @SpringBean**

When `@SpringBean` mocks are declared ONLY in the base spec (as recommended), all test
classes share the same Spring context because they have identical mock definitions.
This is WHY we centralize mocks in the base class - it enables context caching.

If you were to add `@SpringBean` declarations in individual test classes, each would
get a separate Spring context (defeating the purpose of shared containers).

```groovy
package com.bitso.{servicename}

import com.bitso.{servicename}.{ServiceName}Application
// Import ALL external clients identified in Phase 1
import com.bitso.external.ExternalClient1
import com.bitso.external.ExternalClient2

import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

// Testcontainers - Use GenericContainer with Valkey image
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

import org.spockframework.spring.SpringBean
import redis.clients.jedis.JedisPooled
import spock.lang.Specification

import jakarta.inject.Named
import java.time.Duration
import java.util.UUID

/**
 * Base integration test specification using Spock's @SpringBean.
 *
 * ALL test configuration is centralized here:
 * - External client mocks (@SpringBean)
 * - Testcontainers (PostgreSQL, Redis)
 * - Common test beans (MeterRegistry, ObjectMapper)
 * - Property configuration (@DynamicPropertySource)
 *
 * With @SpringBean, use Spock mock syntax: mock.method() >> value
 * Mocks are automatically fresh for each test specification.
 *
 * IMPORTANT: Do NOT use @Transactional on test classes or methods.
 *
 * NOTE: We do NOT use @Testcontainers annotation because containers are
 * manually started in static block (for sharing across all test classes).
 */
@SpringBootTest(classes = [{ServiceName}Application])
@ActiveProfiles("test")
class BaseIntegrationSpec extends Specification {

    // ============================================
    // TEST BEANS - ONLY ADD IF SPRING BOOT DOESN'T AUTO-CONFIGURE
    // ============================================
    // Spring Boot auto-configures most beans. Only add @TestConfiguration
    // if you get NoSuchBeanDefinitionException. Keep beans here, not in separate files.

    // ============================================
    // EXTERNAL CLIENTS - MOCK STRATEGY
    //
    // PREFER: GrpcMock (gRPC) or WireMock (REST) for external services
    //         → Tests real client wrappers, resilience, error codes
    //         → See Section 3A-EXT for GrpcMock setup
    //
    // ONLY USE @SpringBean for:
    //   - ProtoShims/custom RPCs (not mockable via GrpcMock)
    //   - Kafka producers (no network mock available)
    //
    // With @SpringBean use Spock syntax: mock.method() >> value
    // ============================================

    // ProtoShim beans - can't use GrpcMock (custom RPC protocol)
    @SpringBean
    UserModel userModel = Mock()         // ProtoShim

    @SpringBean
    TradeModel tradeModel = Mock()       // ProtoShim - server streaming

    // Standard gRPC clients → use GrpcMock instead (see Section 3A-EXT)
    // Do NOT mock these with @SpringBean:
    // ExternalClient1 → point to GrpcMock via @DynamicPropertySource
    // ExternalClient2 → point to GrpcMock via @DynamicPropertySource

    // ============================================
    // TESTCONTAINERS
    // NOTE: Static containers are NOT annotated with @Shared because they're
    // initialized in a static block, not by Spock's lifecycle
    // ============================================

    static final GenericContainer<?> REDIS_CONTAINER

    static {
        try {
            // Valkey (Redis-compatible)
            REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("valkey/valkey:7.2-alpine"))
                    .withExposedPorts(6379)
                    .withStartupTimeout(Duration.ofMinutes(2))
            REDIS_CONTAINER.start()

            if (!REDIS_CONTAINER.isRunning()) {
                throw new IllegalStateException("Valkey container failed to start")
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to start Valkey container. Ensure Docker is running.", e)
        }
    }

    // Helper methods for container properties
    protected static String getRedisHost() {
        return REDIS_CONTAINER.getHost()
    }

    protected static Integer getRedisPort() {
        return REDIS_CONTAINER.getFirstMappedPort()
    }

    @Autowired(required = false)
    DSLContext dslContext

    @Autowired(required = false)
    @Named("{serviceName}Redis")
    JedisPooled jedisPooled

    def setup() {
        // @SpringBean mocks are automatically fresh for each test specification
        // NO reset() needed - Spock manages mock lifecycle (unlike Mockito)

        // IMPORTANT: Clean data from previous tests (mocks are fresh, but DB/Redis data persists!)
        jedisPooled?.flushAll()
        cleanupDatabase()

        // Set up default behaviors here if needed:
        setupDefaultMockBehaviors()
    }

    def cleanup() {
        // No cleanup needed here - setup() handles pre-test data cleanup
        // This ensures each test starts with a clean state
        // (Spock @SpringBean mocks are automatically fresh per specification)
    }

    /**
     * Override in subclasses to set up default mock behaviors.
     * Use Spock syntax: mock.method() >> value
     */
    protected void setupDefaultMockBehaviors() {
        // Example:
        // externalClient1.healthCheck() >> true
    }

    private void cleanupDatabase() {
        if (dslContext == null) return

        try {
            dslContext.transaction { configuration ->
                def ctx = configuration.dsl()
                // ctx.truncate(TABLE_NAME).cascade().execute()
            }
        } catch (Exception e) {
            System.err.println("Database cleanup failed: " + e.getMessage())
        }
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        // Use Groovy closures { } for lazy evaluation
        registry.add("redis.{service-name}.host", { getRedisHost() })
        registry.add("redis.{service-name}.port", { getRedisPort() })
        registry.add("redis.{service-name}-ro.host", { getRedisHost() })
        registry.add("redis.{service-name}-ro.port", { getRedisPort() })

        // PostgreSQL - Use jdbc:tc: in application-test.yml instead (preferred)
    }
}
```

---

# PHASE 3: SERVICE-TYPE SPECIFIC SETUP

Choose the section(s) that apply to your service. Each section has a detailed skill guide.

**IMPORTANT:** If your service calls external gRPC services, set up GrpcMock FIRST (Section 3A-EXT). This is the default approach for mocking external gRPC dependencies. Use @MockitoBean/@SpringBean only for ProtoShims/custom RPCs where GrpcMock is not feasible.

| Service Type | Reference Guide | Quick Setup |
|--------------|-----------------|-------------|
| **External gRPC Clients** (do this first) | [grpcmock-setup](grpcmock-setup.md) | **GrpcMock server + stub traits (DEFAULT for external gRPC)** |
| gRPC Handlers | [grpc-handler-testing](grpc-handler-testing.md) | BaseGrpcIntegrationSpec + GrpcClientTestConfig |
| REST Controllers | [rest-controller-testing](rest-controller-testing.md) | BaseRestIntegrationSpec + @AutoConfigureMockMvc |
| Kafka Consumers | [kafka-testing](kafka-testing.md) | @EmbeddedKafka + KafkaTestUtils |

---

## 3A: gRPC Services

> **Full guide:** [grpc-handler-testing](grpc-handler-testing.md)

### Create BaseGrpcIntegrationSpec

```groovy
package com.bitso.{servicename}.grpc

import com.bitso.{servicename}.BaseIntegrationSpec
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

import java.util.UUID

/**
 * Base for gRPC handler integration tests.
 * Uses in-process gRPC transport (no network, no port conflicts).
 *
 * CRITICAL: Each test class MUST have a unique gRPC in-process server name.
 * Static server names from application-test.yml will cause collisions when
 * multiple test classes run in parallel. Use UUID for uniqueness.
 *
 * NOTE on @DynamicPropertySource inheritance:
 * Spring calls ALL @DynamicPropertySource methods in the class hierarchy
 * as long as they have DIFFERENT method names. Parent's registerContainerProperties()
 * and this class's registerGrpcProperties() are both called automatically.
 *
 * NOTE on @Import: We use @Import here specifically for test stub beans
 * (GrpcClientTestConfig). This is acceptable because it's test infrastructure,
 * not scattered test configuration. The guide recommends avoiding @Import for
 * mocks and test config, but stub beans are a special case.
 */
@ActiveProfiles("test")
@Import([GrpcClientTestConfig])  // Exception: test stub beans only
@ImportAutoConfiguration([
    GrpcServerAutoConfiguration,
    GrpcServerFactoryAutoConfiguration,
    GrpcClientAutoConfiguration
])
class BaseGrpcIntegrationSpec extends BaseIntegrationSpec {

    @DynamicPropertySource
    static void registerGrpcProperties(DynamicPropertyRegistry registry) {
        // Generate unique gRPC server name per test class to avoid collisions
        // IMPORTANT: Do NOT use static final - UUID must be generated per test class
        def serverName = "test-grpc-" + UUID.randomUUID().toString()

        // Parent's registerContainerProperties() is called automatically by Spring
        // (different method names = both methods are invoked)
        registry.add("grpc.server.inProcessName", { serverName })
        registry.add("grpc.client.inProcess.address", { "in-process:${serverName}" })
    }
}
```

### Create GrpcClientTestConfig

```java
package com.bitso.{servicename}.grpc;

import com.bitso.{servicename}.protos.{Service1}Grpc;
import com.bitso.{servicename}.protos.{Service2}Grpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.client.inject.GrpcClientBeans;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC client stubs for tests.
 * IMPORTANT: Only stubs, NOT client wrappers.
 *
 * Autowire in tests using @Qualifier to avoid conflicts with production beans:
 *   @Autowired
 *   @Qualifier("{service1}Stub")
 *   {Service1}Grpc.{Service1}BlockingStub stub
 */
@Configuration(proxyBeanMethods = false)
@GrpcClientBeans({
    @GrpcClientBean(
        clazz = {Service1}Grpc.{Service1}BlockingStub.class,
        beanName = "{service1}Stub",
        client = @GrpcClient("inProcess")
    ),
    @GrpcClientBean(
        clazz = {Service2}Grpc.{Service2}BlockingStub.class,
        beanName = "{service2}Stub",
        client = @GrpcClient("inProcess")
    )
})
public class GrpcClientTestConfig {
}
```

### Example gRPC Test (with @MockitoBean)

```groovy
package com.bitso.{servicename}.grpc

import com.bitso.{servicename}.protos.QuoteRequest
import com.bitso.{servicename}.protos.QuoteServiceGrpc
import com.bitso.external.ExternalClient
import com.bitso.external.Rate
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

class QuoteHandlerIntegrationSpec extends BaseGrpcIntegrationSpec {

    @Autowired
    @Qualifier("quoteServiceStub")  // Matches beanName in GrpcClientTestConfig
    QuoteServiceGrpc.QuoteServiceBlockingStub stub

    // externalClient1 is inherited from BaseIntegrationSpec (mocked via @MockitoBean)
    // No need to declare it here - just use it directly

    def "successful quote request"() {
        given: "external client returns data"
            // MUST use Mockito syntax with @MockitoBean
            when(externalClient1.getRates(any())).thenReturn(Either.right(testRates()))

        when: "requesting a quote via gRPC"
            def request = QuoteRequest.newBuilder()
                .setUserId(1L)
                .setAmount("100.00")
                .build()
            def response = stub.getQuote(request)

        then: "quote is returned"
            response.hasQuote()
            response.quote.amount == "100.00"
    }

    def "validation error returns INVALID_ARGUMENT"() {
        when: "requesting with invalid amount"
            def request = QuoteRequest.newBuilder()
                .setAmount("-1")
                .build()
            stub.getQuote(request)

        then:
            def e = thrown(StatusRuntimeException)
            e.status.code == Status.Code.INVALID_ARGUMENT
    }

    private static List<Rate> testRates() {
        [Rate.builder().pair("btc_usd").bid(50000).ask(50100).build()]
    }
}
```

### Example gRPC Test (with @SpringBean)

```groovy
package com.bitso.{servicename}.grpc

import com.bitso.{servicename}.protos.QuoteRequest
import com.bitso.{servicename}.protos.QuoteServiceGrpc
import com.bitso.external.ExternalClient
import com.bitso.external.Rate
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class QuoteHandlerIntegrationSpec extends BaseGrpcIntegrationSpec {

    @Autowired
    @Qualifier("quoteServiceStub")  // Matches beanName in GrpcClientTestConfig
    QuoteServiceGrpc.QuoteServiceBlockingStub stub

    // externalClient1 is inherited from BaseIntegrationSpec (mocked via @SpringBean)
    // No need to declare it here - just use it directly

    def "successful quote request"() {
        given: "external client returns data"
            // Use Spock syntax with @SpringBean
            externalClient1.getRates(_) >> Either.right(testRates())

        when: "requesting a quote via gRPC"
            def request = QuoteRequest.newBuilder()
                .setUserId(1L)
                .setAmount("100.00")
                .build()
            def response = stub.getQuote(request)

        then: "quote is returned"
            response.hasQuote()
            response.quote.amount == "100.00"
    }

    def "validation error returns INVALID_ARGUMENT"() {
        when: "requesting with invalid amount"
            def request = QuoteRequest.newBuilder()
                .setAmount("-1")
                .build()
            stub.getQuote(request)

        then:
            def e = thrown(StatusRuntimeException)
            e.status.code == Status.Code.INVALID_ARGUMENT
    }

    private static List<Rate> testRates() {
        [Rate.builder().pair("btc_usd").bid(50000).ask(50100).build()]
    }
}
```

---

## 3A-EXT: GrpcMock for External gRPC Services (DEFAULT)

> **Full guide:** [grpcmock-setup](grpcmock-setup.md)

**GrpcMock is the DEFAULT approach for ALL external gRPC services.** It tests real client wrappers, resilience patterns, error codes, and serialization. Use @MockitoBean/@SpringBean only for ProtoShims/custom RPCs where GrpcMock is not feasible.

### When to Use GrpcMock vs @SpringBean/@MockitoBean

| Scenario | Use GrpcMock (DEFAULT) | Use @SpringBean/@MockitoBean |
|----------|------------------------|------------------------------|
| Standard gRPC service (any) | ✓ | |
| Simple request/response | ✓ | |
| Testing retry logic | ✓ | |
| Testing circuit breakers | ✓ | |
| Testing timeout handling | ✓ | |
| Testing real gRPC error codes | ✓ | |
| Multiple external gRPC services | ✓ (centralized) | |
| Testing client wrapper logic | ✓ | |
| **ProtoShim / custom RPC** (e.g., UserModel, TradeModel) | | ✓ |

### Architecture with GrpcMock

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Test JVM Process                              │
│                                                                      │
│  ┌─────────────────┐     ┌─────────────────────────────────────┐    │
│  │ Test Code       │     │      Spring Boot Context            │    │
│  │                 │     │                                     │    │
│  │ stubSuccess()   │     │  Handler / Controller               │    │
│  │ stubAlwaysFail()│     │         │                           │    │
│  │ verifyGrpc()    │     │         ▼                           │    │
│  │                 │     │  ┌─────────────────────┐            │    │
│  │                 │     │  │   Domain Service    │            │    │
│  │                 │     │  │   (REAL impl)       │            │    │
│  │                 │     │  │         │           │            │    │
│  │                 │     │  │         ▼           │            │    │
│  │                 │     │  │   gRPC Client       │ ────────►  │    │
│  │                 │     │  │   (REAL wrapper)    │            │    │
│  │                 │     │  └─────────────────────┘            │    │
│  │                 │     │                                     │    │
│  └─────────────────┘     └─────────────────────────────────────┘    │
│                                        │                             │
│                                        ▼                             │
│                          ┌─────────────────────────┐                │
│                          │      GrpcMock Server    │                │
│                          │   (WireMock for gRPC)   │                │
│                          │                         │                │
│                          │  - Stub responses       │                │
│                          │  - Simulate failures    │                │
│                          │  - Add delays           │                │
│                          │  - Verify calls         │                │
│                          └─────────────────────────┘                │
└─────────────────────────────────────────────────────────────────────┘
```

### Step 1: Add GrpcMock Dependencies

```groovy
dependencies {
    // GrpcMock - WireMock-like stubbing for gRPC
    testImplementation 'org.grpcmock:grpcmock-core:0.14.0'
    testImplementation 'org.grpcmock:grpcmock-junit5:0.14.0'
}
```

### Step 2: Create GrpcMockServerExtension Trait

Create `src/test/groovy/com/bitso/{servicename}/integration/grpcmock/GrpcMockServerExtension.groovy`:

```groovy
package com.bitso.{servicename}.integration.grpcmock

import io.grpc.MethodDescriptor
import io.grpc.Status
import org.grpcmock.GrpcMock

import static org.grpcmock.GrpcMock.*

/**
 * Extension trait for GrpcMock integration in Spock tests.
 * Provides WireMock-like stubbing capabilities for gRPC services.
 */
trait GrpcMockServerExtension {

    private static GrpcMock grpcMockServer

    /**
     * Starts the GrpcMock server on a random available port.
     */
    void startGrpcMock() {
        if (grpcMockServer == null) {
            grpcMockServer = GrpcMock.grpcMock().build()
            grpcMockServer.start()
        }
    }

    /**
     * Stops the GrpcMock server.
     */
    void stopGrpcMock() {
        if (grpcMockServer != null) {
            grpcMockServer.stop()
            grpcMockServer = null
        }
    }

    /**
     * Resets all stubs and verifications between tests.
     * IMPORTANT: Call this in cleanup() to prevent test pollution.
     */
    void resetGrpcMock() {
        grpcMockServer?.resetAll()
    }

    /**
     * Gets the port the GrpcMock server is running on.
     */
    int getGrpcMockPort() {
        return grpcMockServer?.getPort() ?: -1
    }

    /**
     * Gets the address string for configuring gRPC clients.
     */
    String getGrpcMockAddress() {
        return "localhost:${grpcMockPort}"
    }

    /**
     * Stubs a unary gRPC method to return a successful response.
     */
    void stubSuccess(MethodDescriptor method, Object resp) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(response(resp)))
    }

    /**
     * Stubs a method to always fail with a status.
     */
    void stubAlwaysFail(MethodDescriptor method, Status status) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(statusException(status)))
    }

    /**
     * Stubs a method to fail N times then succeed (for testing retries).
     */
    void stubFailThenSucceed(MethodDescriptor method, Status failureStatus, int failureCount, Object successResponse) {
        def builder = unaryMethod(method).willReturn(statusException(failureStatus))
        (1..<failureCount).each {
            builder.nextWillReturn(statusException(failureStatus))
        }
        builder.nextWillReturn(response(successResponse))
        GrpcMock.stubFor(builder)
    }

    /**
     * Stubs a method to return response with delay (for testing timeouts).
     */
    void stubWithDelay(MethodDescriptor method, Object resp, long delayMs) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(response(resp).withFixedDelay(delayMs)))
    }

    /**
     * Stubs a server streaming gRPC method to return multiple responses.
     */
    void stubServerStreamingSuccess(MethodDescriptor method, List<?> responses) {
        def args = responses.collect { response(it) }
        GrpcMock.stubFor(serverStreamingMethod(method).willReturn(*args))
    }

    /**
     * Verifies a gRPC method was called.
     */
    void verifyGrpc(MethodDescriptor method, Object countMatcher) {
        GrpcMock.verifyThat(calledMethod(method), countMatcher)
    }
}
```

### Step 3: Create Service-Specific Stub Helpers

Create traits for each external service to provide typed stub builders.

Example `src/test/groovy/com/bitso/{servicename}/integration/grpcmock/stubs/ConsumerWalletStubs.groovy`:

```groovy
package com.bitso.{servicename}.integration.grpcmock.stubs

import com.bitso.consumer.wallet.proto.CurrencyBalances
import com.bitso.consumer.wallet.proto.UserBalances
import com.bitso.consumer.wallet.proto.service.ConsumerWalletServiceGrpc
import com.bitso.consumer.wallet.proto.service.GetBalancesResponse

/**
 * GrpcMock stub helpers for ConsumerWalletService.
 */
trait ConsumerWalletStubs {

    def getConsumerWalletGetBalancesMethod() {
        return ConsumerWalletServiceGrpc.getGetBalancesMethod()
    }

    GetBalancesResponse buildGetBalancesResponse(Long userId, Map<String, Map<String, String>> balances) {
        def userBalancesBuilder = UserBalances.newBuilder().setUserId(userId)

        balances.each { currency, balanceMap ->
            def currencyBalances = CurrencyBalances.newBuilder()
            balanceMap.each { balanceType, amount ->
                currencyBalances.putBalances(balanceType, amount)
            }
            userBalancesBuilder.putCurrenciesBalances(currency, currencyBalances.build())
        }

        return GetBalancesResponse.newBuilder()
            .setUserBalances(userBalancesBuilder.build())
            .build()
    }

    GetBalancesResponse buildSimpleBalancesResponse(Long userId, Map<String, BigDecimal> totals) {
        def balances = totals.collectEntries { currency, amount ->
            [(currency): [TOTAL: amount.toPlainString(), AVAILABLE: amount.toPlainString(), LOCKED: "0"]]
        }
        return buildGetBalancesResponse(userId, balances)
    }

    GetBalancesResponse buildEmptyBalancesResponse(Long userId) {
        return GetBalancesResponse.newBuilder()
            .setUserBalances(UserBalances.newBuilder().setUserId(userId).build())
            .build()
    }
}
```

### Step 4: Create Composite Stub Helpers Trait

```groovy
package com.bitso.{servicename}.integration.grpcmock

import com.bitso.{servicename}.integration.grpcmock.stubs.ConsumerWalletStubs
import com.bitso.{servicename}.integration.grpcmock.stubs.ExchangeRateStubs
// ... import other stub traits

/**
 * Composite trait providing all GrpcMock stub helpers.
 */
trait GrpcMockStubHelpers implements
        ConsumerWalletStubs,
        ExchangeRateStubs {
        // Add other service stub traits here
}
```

### Step 5: Update BaseIntegrationSpec to Use GrpcMock

```groovy
@SpringBootTest(classes = [{ServiceName}Application])
@ActiveProfiles("test")
abstract class BaseIntegrationSpec extends Specification
        implements GrpcMockServerExtension, GrpcMockStubHelpers {

    private static GrpcMock GRPC_MOCK_SERVER

    // ============================================
    // BEAN MOCKS - ONLY for ProtoShims/custom RPCs
    // where GrpcMock is NOT feasible
    // ============================================
    @SpringBean
    UserModel userModel = Mock()        // ProtoShim - custom RPC

    @SpringBean
    TradeModel tradeModel = Mock()      // ProtoShim - server streaming

    static {
        // Start GrpcMock server - must be before Spring context
        GRPC_MOCK_SERVER = GrpcMock.grpcMock().build()
        GRPC_MOCK_SERVER.start()
        GrpcMock.configureFor(GRPC_MOCK_SERVER)

        // Register default stubs needed for Spring context startup
        // Some beans (e.g., BookPathFinder) make gRPC calls during initialization
        registerDefaultGrpcStubs()

        Runtime.getRuntime().addShutdownHook(new Thread({
            GRPC_MOCK_SERVER.stop()
        }))
    }

    // Override trait methods to use static server
    @Override
    void resetGrpcMock() {
        GRPC_MOCK_SERVER.resetAll()
    }

    @Override
    int getGrpcMockPort() {
        return GRPC_MOCK_SERVER.getPort()
    }

    protected static int getGrpcMockPortStatic() {
        return GRPC_MOCK_SERVER.getPort()
    }

    def setup() {
        flushRedis()
        cleanDatabase()
        // Re-register default stubs after resetGrpcMock() in cleanup
        registerDefaultGrpcStubs()
        setupDefaultMockBehaviors()
    }

    def cleanup() {
        // Reset GrpcMock stubs between tests
        resetGrpcMock()
        flushRedis()
        cleanDatabase()
    }

    /**
     * Default GrpcMock stubs needed for Spring context startup and between tests.
     * Override/extend in subclasses if needed.
     * NOTE: This is called in static block (before Spring) AND in setup() (after reset).
     */
    protected static void registerDefaultGrpcStubs() {
        // Example: stubs needed for beans that make gRPC calls during initialization
        // stubSuccess(getBookSettingsMethod(), buildDefaultBookSettingsResponse())
    }

    /**
     * Default bean mock behaviors (for @SpringBean ProtoShim mocks).
     */
    protected void setupDefaultMockBehaviors() {
        userModel.getUser(_) >> Optional.empty()
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        // Point ALL external gRPC clients to GrpcMock
        def grpcMockAddress = "localhost:${getGrpcMockPortStatic()}"

        // Register each external gRPC client to use GrpcMock
        [
            'consumerwallet',
            'exchangerate',
            'accounts-management',
            'orders',
            // Add all other external gRPC client names...
        ].each { clientName ->
            registry.add("grpc.client.${clientName}.address", { grpcMockAddress })
            registry.add("grpc.client.${clientName}.negotiation-type", { "PLAINTEXT" })
        }

        // Redis, PostgreSQL properties...
    }
}
```

**Key patterns in this setup:**
- **GrpcMock handles ALL standard gRPC services** - real client wrappers connect to the GrpcMock server
- **@SpringBean only for ProtoShims** (UserModel, TradeModel) where GrpcMock is not feasible
- **`registerDefaultGrpcStubs()`** runs in static block (for Spring startup) AND in `setup()` (after cleanup resets stubs)
- **Iterative client registration** - loop over client names to avoid repetitive property registration

### Step 6: Example Test Using GrpcMock

```groovy
import static org.grpcmock.GrpcMock.times  // For verification count matchers

class BalanceServiceIntegrationSpec extends BaseRestIntegrationSpec {

    def "should return user balances from ConsumerWallet"() {
        given: "ConsumerWallet returns balances"
        stubSuccess(
            getConsumerWalletGetBalancesMethod(),
            buildSimpleBalancesResponse(1L, [mxn: 1000.00, btc: 0.5])
        )

        when: "requesting combined balance"
        def result = mockMvc.perform(get("/api/v1/balances")
            .header("Authorization", "Bearer test-token"))

        then: "balances are returned"
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.balances').isArray())
    }

    def "should handle ConsumerWallet timeout with circuit breaker"() {
        given: "ConsumerWallet responds slowly"
        stubWithDelay(
            getConsumerWalletGetBalancesMethod(),
            buildEmptyBalancesResponse(1L),
            10000  // 10 second delay - will trigger timeout
        )

        when: "requesting balance"
        def result = mockMvc.perform(get("/api/v1/balances"))

        then: "returns fallback or error"
        result.andExpect(status().is5xxServerError())
    }

    def "should retry on UNAVAILABLE status"() {
        given: "ConsumerWallet fails twice then succeeds"
        stubFailThenSucceed(
            getConsumerWalletGetBalancesMethod(),
            io.grpc.Status.UNAVAILABLE,
            2,  // Fail twice
            buildSimpleBalancesResponse(1L, [mxn: 1000.00])
        )

        when: "requesting balance"
        def result = mockMvc.perform(get("/api/v1/balances"))

        then: "succeeds after retries"
        result.andExpect(status().isOk())

        and: "method was called 3 times (2 failures + 1 success)"
        verifyGrpc(getConsumerWalletGetBalancesMethod(), times(3))
    }
}
```

### File Structure with GrpcMock

```
src/test/groovy/com/bitso/{servicename}/
├── integration/
│   ├── BaseIntegrationSpec.groovy
│   ├── BaseRestIntegrationSpec.groovy
│   ├── BaseGrpcIntegrationSpec.groovy
│   └── grpcmock/
│       ├── GrpcMockServerExtension.groovy
│       ├── GrpcMockStubHelpers.groovy
│       └── stubs/
│           ├── ConsumerWalletStubs.groovy
│           ├── ExchangeRateStubs.groovy
│           ├── AccountsManagementStubs.groovy
│           └── ...
└── {feature}/
    └── *IntegrationSpec.groovy
```

---

## 3B: REST APIs

> **Full guide:** [rest-controller-testing](rest-controller-testing.md)

### Create BaseRestIntegrationSpec

**CRITICAL: REST tests also need unique gRPC server names!**

Even though you're testing REST controllers with MockMvc, if your service has gRPC enabled (common in mixed services), the full Spring context will start a gRPC server. Multiple REST test classes starting in parallel can collide on the same gRPC in-process server name.

**Solution:** Use unique gRPC server names per test class, just like gRPC handler tests.

```groovy
package com.bitso.{servicename}.rest

import com.bitso.{servicename}.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc

import java.util.UUID

/**
 * Base for REST controller integration tests.
 *
 * IMPORTANT: Even REST tests need unique gRPC server names if the service has gRPC enabled.
 * When @SpringBootTest loads the full context, it will start the gRPC server (if configured).
 * Multiple test classes with the same in-process server name will collide.
 */
@AutoConfigureMockMvc
class BaseRestIntegrationSpec extends BaseIntegrationSpec {

    @Autowired
    MockMvc mockMvc

    @DynamicPropertySource
    static void registerGrpcProperties(DynamicPropertyRegistry registry) {
        // Generate unique gRPC server name per test class to avoid collisions
        // Parent's registerContainerProperties() is called automatically by Spring
        def serverName = "test-grpc-" + UUID.randomUUID().toString()
        registry.add("grpc.server.inProcessName", { serverName })
        registry.add("grpc.client.inProcess.address", { "in-process:${serverName}" })
    }
}
```

### Example REST Test (with @MockitoBean)

```groovy
package com.bitso.{servicename}.rest

import com.bitso.external.ExternalClient
import com.bitso.external.User
import com.bitso.failure.Failure
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerIntegrationSpec extends BaseRestIntegrationSpec {

    // externalClient1 is inherited from BaseIntegrationSpec (mocked via @MockitoBean)
    // No need to declare it here - just use it directly

    def "GET /api/v1/users/{id} returns user"() {
        given: "external client returns user data"
            when(externalClient1.getUser(1L)).thenReturn(Either.right(testUser()))

        when: "requesting user"
            def result = mockMvc.perform(get("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON))

        then: "user is returned"
            result.andExpect(status().isOk())
                  .andExpect(jsonPath('$.id').value(1))
                  .andExpect(jsonPath('$.name').value("Test User"))
    }

    def "GET /api/v1/users/{id} returns 404 when not found"() {
        given: "external client returns not found"
            when(externalClient1.getUser(999L)).thenReturn(Either.left(Failure.notFound()))

        when:
            def result = mockMvc.perform(get("/api/v1/users/999")
                .accept(MediaType.APPLICATION_JSON))

        then:
            result.andExpect(status().isNotFound())
    }

    private static User testUser() {
        User.builder().id(1L).name("Test User").build()
    }
}
```

---

## 3C: Kafka Consumers

> **Full guide:** [kafka-testing](kafka-testing.md)

### Option 1: @EmbeddedKafka (Recommended - Faster, Simpler)

```groovy
package com.bitso.{servicename}.kafka

import com.bitso.{servicename}.BaseIntegrationSpec
import org.awaitility.Awaitility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import static org.awaitility.Awaitility.await

/**
 * Base for Kafka listener integration tests using @EmbeddedKafka.
 *
 * @EmbeddedKafka is faster to start than KafkaContainer and runs in-memory.
 */
@EmbeddedKafka(
    partitions = 1,
    topics = [
        "{service-name}-topic",
        "{service-name}-topic-dlq"
    ],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:0"  // Random port via listener config (replaces deprecated @EmbeddedKafka(port=0))
    ]
)
class BaseKafkaIntegrationSpec extends BaseIntegrationSpec {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        // EmbeddedKafka automatically sets spring.kafka.bootstrap-servers
        // Enable the consumer we're testing
        registry.add("{service-name}.kafka.consumers.{consumer}.enabled", { "true" })
    }
}
```

### Option 2: KafkaContainer (More Realistic, Heavier)

```groovy
package com.bitso.{servicename}.kafka

import com.bitso.{servicename}.BaseIntegrationSpec
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base for Kafka tests using Testcontainer.
 * Use when you need more realistic Kafka behavior.
 */
class BaseKafkaContainerIntegrationSpec extends BaseIntegrationSpec {

    static final KafkaContainer KAFKA_CONTAINER

    static {
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
        KAFKA_CONTAINER.start()
    }

    KafkaTemplate<String, String> kafkaTemplate

    def setup() {
        super.setup()
        def props = [
            (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG): KAFKA_CONTAINER.bootstrapServers,
            (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG): StringSerializer,
            (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG): StringSerializer
        ]
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props))
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", { KAFKA_CONTAINER.bootstrapServers })
    }
}
```

### Example Kafka Test (with Awaitility)

```groovy
package com.bitso.{servicename}.kafka

import com.bitso.{servicename}.repository.OrderRepository
import com.bitso.external.ExternalClient
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired

import java.util.concurrent.TimeUnit

import static org.awaitility.Awaitility.await
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

class OrderEventListenerIntegrationSpec extends BaseKafkaIntegrationSpec {

    @Autowired
    OrderRepository orderRepository  // Real - uses Testcontainers

    // externalClient1 is inherited from BaseIntegrationSpec (mocked via @MockitoBean)
    // No need to declare it here - just use it directly

    def "processes order created event"() {
        given: "external client configured"
            when(externalClient1.notifyFulfillment(any())).thenReturn(Either.right(null))

        when: "order event is published"
            def event = '{"orderId": "123", "status": "CREATED"}'
            // Use timeout to prevent hanging - Groovy handles checked exceptions implicitly
            // This can throw InterruptedException, ExecutionException, TimeoutException
            kafkaTemplate.send("{service-name}-topic", event).get(10, TimeUnit.SECONDS)

        then: "order is persisted (using Awaitility for async)"
            await().atMost(5, TimeUnit.SECONDS).untilAsserted {
                def order = orderRepository.findById("123")
                assert order.isPresent()
                assert order.get().status == "CREATED"
            }
    }
}
```

---

## 3D: Managing Infrastructure Lifecycle

If your service has background consumers that are autowired (not mocked), manage their lifecycle:

```groovy
class BaseIntegrationSpec extends Specification {

    // Autowire if testing with real consumers
    @Autowired(required = false)
    RedisStreamStarter redisStreamStarter

    @Autowired(required = false)
    @Named("balanceUpdate")
    MessagesLoopClient balanceUpdateClient

    def setup() {
        // Reset mocks (if using @MockitoBean)
        reset(externalClient1, externalClient2)

        // Start infrastructure consumers
        redisStreamStarter?.startListen(null)
        balanceUpdateClient?.start()
    }

    def cleanup() {
        // Stop infrastructure consumers
        redisStreamStarter?.stop()
        balanceUpdateClient?.stop()

        // Clean data
        jedisPooled?.flushAll()
        cleanupDatabase()
    }
}
```

---

# PHASE 4: MIGRATE EXISTING TESTS

**GOAL: Migrate ALL tests discovered in Phase 1.6. Every test using @SpringBootTest must extend BaseIntegrationSpec or its subclasses.**

If migrating existing tests, follow this transformation:

## Remove (Old Pattern)

```groovy
// DELETE
@ContextConfiguration(classes = [TestContext])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional  // REMOVE - let transactions commit
class HandlerSpec extends Specification {
    @Inject SomeService service = Mock()  // Mocking domain service
    ManagedChannel channel                 // Manual channel
    ServiceClient client                   // Client wrapper

    def setup() {
        channel = ManagedChannelBuilder.forAddress('localhost', port).build()
    }
    def cleanup() {
        channel.shutdown()
    }
}
```

## Create (New Pattern)

```groovy
// CREATE
class HandlerIntegrationSpec extends BaseGrpcIntegrationSpec {
    @Autowired ServiceGrpc.ServiceBlockingStub stub  // Direct stub

    // externalClient1/externalClient2 inherited from base class (no @Autowired needed!)
    // @MockitoBean/@SpringBean already injects the mock - don't re-declare

    // setup() inherited - resets mocks + cleans data
    // cleanup() inherited
}
```

## Key Transformations

| Old | New |
|-----|-----|
| `@ContextConfiguration(classes = [TestContext])` | `extends Base*IntegrationSpec` |
| `@Import([CustomConfig1, CustomConfig2])` | Move beans to `BaseIntegrationSpec` |
| `@TestConfiguration` inner class | Move to `BaseIntegrationSpec` or delete |
| Separate `TestContext.java` | Consolidate into `BaseIntegrationSpec` |
| `@DirtiesContext` | Remove - use proper cleanup |
| `@Transactional` | Remove - let commits happen |
| `Mock(DomainService)` | Real implementation |
| `@MockitoBean ExternalGrpcClient` | **GrpcMock** - point client to GrpcMock server |
| `@SpringBean ExternalGrpcClient = Mock()` | **GrpcMock** - point client to GrpcMock server |
| `@MockitoBean ProtoShimModel` | Keep - ProtoShims can't use GrpcMock |
| `ManagedChannelBuilder` | In-process via config |
| Client wrapper | Direct stub/MockMvc |
| Manual channel cleanup | Automatic |
| No mock reset | `reset()` in setup (for remaining bean mocks) |
| No data cleanup | `cleanup()` clears DB/Redis + `resetGrpcMock()` |

## Controller Unit Tests → Integration Tests

**IMPORTANT:** Tests originally written as controller-layer unit tests must be converted to true integration tests.

### Identifying Controller Unit Tests

Controller unit tests typically:
- Use `@SpringBootTest` but mock internal services
- Verify service method calls (e.g., `1 * service.method() >> result`)
- Test controller logic in isolation

### The Problem

When using `@SpringBootTest`, the full Spring context loads with REAL service implementations. If your test expects `1 * service.method()` but the real service is loaded, the verification will fail with `0 invocations`.

### The Solution: Convert to Integration Test Style

**Before (Controller Unit Test):**

```groovy
class JourneyControllerSpec extends BaseRestIntegrationSpec {
    @SpringBean
    IBAFacade ibaFacade = Mock()  // Mocking internal service

    @SpringBean
    BuyIntentRepository buyIntentRepository = Mock()  // Mocking repository

    def "calculator returns IBA info"() {
        given:
            1 * ibaFacade.getMaxRateForUserAndCurrency(...) >> maxRateResponse

        when:
            def result = mockMvc.perform(get("/buy_journey/v1/calculator"))

        then:
            // Implicitly verifies ibaFacade was called (Spock verification)
    }
}
```

**After (True Integration Test):**

```groovy
class JourneyControllerIntegrationSpec extends BaseRestIntegrationSpec {
    // Remove all internal service mocks - use real implementations
    // Only keep AuthenticationService (test infrastructure) and external mocks from base

    def "calculator returns IBA info"() {
        given:
            // Configure EXTERNAL dependencies (from BaseIntegrationSpec)
            exchangeService.getRates(_) >> Either.right(testRates())
            accountsManagementService.getBalance(_) >> Either.right(testBalance())

        when:
            def result = mockMvc.perform(get("/buy_journey/v1/calculator")
                .param("to_asset", "btc"))

        then: "verify HTTP response ONLY - not internal service calls"
            result.andExpect(status().isOk())
                  .andExpect(jsonPath('$.payload.to_asset.code').value("btc"))
                  .andExpect(jsonPath('$.payload.iba_info').exists())
    }
}
```

### Key Changes:

| Old Pattern | New Pattern |
|-------------|-------------|
| Mock internal services (`IBAFacade`, `BuyIntentRepository`) | Use real implementations |
| Verify service calls (`1 * service.method()`) | Verify HTTP responses only (`status().isOk()`, `jsonPath()`) |
| Test controller logic in isolation | Test full request-response flow |
| Many small mocks | Only mock external dependencies |

### Why This Matters:

- **Integration tests verify behavior, not implementation** - HTTP status codes, response structure, error messages
- **Internal refactoring doesn't break tests** - You can change service structure without updating tests
- **Tests catch more bugs** - Real service interactions reveal issues that mocks hide
- **Tests match production** - Same code path as production requests

### Conversion Checklist:

For each controller test:
- [ ] Remove `@SpringBean` mocks for internal services (keep external mocks from base)
- [ ] Remove all service call verifications (`1 * service.method()`)
- [ ] Change assertions to verify HTTP response only (`status()`, `jsonPath()`, `content()`)
- [ ] Keep only test infrastructure mocks (`AuthenticationService`) if needed
- [ ] Use external mocks inherited from `BaseIntegrationSpec`

## Configuration Consolidation Steps

When migrating tests with custom configuration:

1. **Identify all configs:**
   ```bash
   # Find all @Import usages in tests
   grep -r "@Import" --include="*.groovy" --include="*.java" src/test/

   # Find all @TestConfiguration
   grep -r "@TestConfiguration" --include="*.groovy" --include="*.java" src/test/

   # Find TestContext classes
   find src/test/ -name "*Context*.java" -o -name "*Context*.groovy"
   ```

2. **Analyze each config class:**
    - What beans does it create?
    - Are they test utilities (keep) or mock overrides (move to base)?
    - Are they duplicated across test classes?

3. **Consolidate into BaseIntegrationSpec:**
   ```groovy
   @SpringBootTest(classes = [{ServiceName}Application])
   @ActiveProfiles("test")
   // NO @Import needed - everything is in this class
   class BaseIntegrationSpec extends Specification {

       // Mocks (previously scattered across configs)
       @MockitoBean
       ExternalClient1 externalClient1

       // Test beans (previously in TestContext.java)
       @TestConfiguration
       static class TestBeans {
           @Bean
           @Primary
           MeterRegistry meterRegistry() {
               return new SimpleMeterRegistry()
           }
       }
   }
   ```

4. **Delete orphaned config files** after all tests are migrated

---

# PHASE 5: VALIDATION

## Run Tests

```bash
# All integration tests
./gradlew :bitso-services:{service-name}:test --tests '*IntegrationSpec*'

# Specific test
./gradlew :bitso-services:{service-name}:test --tests 'QuoteHandlerIntegrationSpec'
```

## Verify

- [ ] All tests pass
- [ ] No external network calls (mocks used)
- [ ] Real domain logic exercised
- [ ] Testcontainers start successfully
- [ ] Test isolation works (no interference)
- [ ] No `@DirtiesContext` needed
- [ ] No `@Transactional` on test classes

---

# PHASE 6: REVIEW AND REPORT

## 6.1 Handle Production Bugs

If migration uncovers bugs in production code (e.g., tests that only passed because they mocked too much):

**STOP and ask user:**

```markdown
## Production Bug Discovered

**Test:** `OrderHandlerSpec.testCreateOrder`
**Bug:** Order validation logic has null pointer exception when `userId` is missing.
         Previously hidden because `UserService` was mocked.

**Evidence:** [stack trace or failing assertion]

**Options:**
1. **Fix now** - Fix the production bug as part of this migration
2. **Document for later** - Create a ticket and continue migration (test will be marked BLOCKED)

Which approach do you prefer?
```

Wait for user decision before proceeding.

## 6.2 Spawn Senior Java Engineer Review

**After all migrations complete, spawn a review agent:**

```markdown
Use the Task tool to spawn a senior Java engineer agent:

Prompt: "Review the integration test migration changes in {service-name}.
Check for:
1. Correct base class inheritance
2. Proper mock usage (@MockitoBean vs @SpringBean consistency)
3. No leftover @DirtiesContext or @Transactional
4. Proper cleanup in tests
5. All external dependencies properly mocked
6. No unnecessary mocks of internal services

Focus on files: [list of migrated test files]

Report any CRITICAL issues that must be fixed before merge."
```

**Review Classification:**

| Severity | Action |
|----------|--------|
| **CRITICAL** | Must fix before completing migration |
| **HIGH** | Should fix, but can proceed |
| **MEDIUM** | Optional improvement |
| **LOW** | Style/preference |

**If CRITICAL issues found:** Fix them and re-run review.

## 6.3 Generate Migration Report

**Output this report upon completion:**

```markdown
# Integration Test Migration Report

## Service: {service-name}
## Date: {current-date}

### Summary
- **Total tests discovered:** X
- **Successfully migrated:** Y
- **Skipped (not applicable):** Z
- **Blocked by production bugs:** W
- **Controller unit tests converted to integration style:** N

### Migrated Tests

| Test Class | Old Base | New Base | Conversion Type | Status |
|------------|----------|----------|-----------------|--------|
| `QuoteHandlerSpec` | `Specification` | `BaseGrpcIntegrationSpec` | Standard migration | ✅ MIGRATED |
| `OrderServiceSpec` | `Specification` | `BaseIntegrationSpec` | Standard migration | ✅ MIGRATED |
| `JourneyControllerSpec` | `Specification` | `BaseRestIntegrationSpec` | Controller unit → integration | ✅ CONVERTED |

Conversion Types:
- **Standard migration:** Test already used proper integration test style
- **Controller unit → integration:** Test was unit test style (mocked internal services), converted to verify HTTP only

### Tests Not Migrated

| Test Class | Reason |
|------------|--------|
| `UnitTestSpec` | Not an integration test (no @SpringBootTest) |
| `LegacySpec` | Uses deprecated framework, needs manual review |

### Production Bugs Discovered

| Test | Bug Description | Resolution |
|------|-----------------|------------|
| `OrderHandlerSpec` | NPE in validation | Fixed in commit abc123 |
| `PaymentSpec` | Race condition | Ticket JIRA-456 created |

### Review Results
- **Reviewer:** Senior Java Engineer Agent
- **Critical Issues:** 0
- **High Issues:** 2 (fixed)
- **Recommendations:** [list]

### Next Steps
1. [ ] Run full test suite: `./gradlew :bitso-services:{service-name}:test`
2. [ ] Verify CI pipeline passes
3. [ ] Address any remaining HIGH issues
4. [ ] Delete old test infrastructure (if applicable)
```

---

# TROUBLESHOOTING

| Problem | Cause | Solution |
|---------|-------|----------|
| `Flyway DataSource missing` | No flyway properties | Set `spring.flyway.url/user/password` or use `jdbc:tc:` URL |
| RdsIamDataSource AWS error | IAM auth enabled | Set `RDS_FORCE_USE_PASSWORD=true` |
| Tests hang | Background consumers | Mock them or manage lifecycle |
| `NOGROUP` Redis error | Stream consumer running | Mock the consumer bean |
| Flyway migrations not found | Wrong path | Check relative path from service module |
| Port already in use | gRPC binding | Set `grpc.server.port: -1` |
| Bean conflicts | Multiple same type | Use `@Primary` or `@Qualifier` |
| Tests interfere | Shared state | Add `reset()` + cleanup + `resetGrpcMock()` |
| OOM | Too many containers | Use static shared containers |
| Slow tests | Context reloads | Avoid `@DirtiesContext` |
| No MeterRegistry | Auto-config not triggered | Check Spring Boot actuator dependency or add bean in BaseIntegrationSpec |
| ObjectMapper issues | Missing modules | Check Spring Boot web dependency or add bean in BaseIntegrationSpec |
| Mock not reset | Mockito mocks persist | Add `reset()` in setup() |
| GrpcMock stubs persist | Missing reset | Add `resetGrpcMock()` in cleanup() |
| GrpcMock not found | Server not started | Start GrpcMock in static block before Spring context |
| gRPC client connects wrong | Wrong address | Check `@DynamicPropertySource` points to GrpcMock port |
| Async assertion fails | Timing issue | Use Awaitility instead of sleep() |
| Spock `>>` not working | Using `@MockitoBean` | Switch to `when().thenReturn()` or use `@SpringBean` |
| Kafka send hangs | No timeout | Add `.get(10, TimeUnit.SECONDS)` |
| `@Transactional` hides bugs | Rollback masks issues | Remove `@Transactional` from tests |
| gRPC server name collision | Multiple tests use same static `in-process-name` | Use UUID-based unique server names in `@DynamicPropertySource` |
| REST tests fail with gRPC errors | REST tests also load gRPC context | Add unique gRPC server names to BaseRestIntegrationSpec |
| Service call verifications fail (0 invocations) | Test mocks internal service but real one loads | Convert to integration test: remove internal mocks, verify HTTP only |

## Optional: Container Reuse (Local Development Only)

For faster local iteration, enable Testcontainers reuse:

1. Create `~/.testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

1. Add `.withReuse(true)` to containers:

```groovy
POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.1-alpine")
    .withDatabaseName("{service}_test")
    .withReuse(true)  // Reuse container across test runs
```

**WARNING**: Do NOT enable in CI/CD - containers must be fresh for reproducibility.

---

# FILE STRUCTURE

After setup:

```
src/test/
├── groovy/com/bitso/{servicename}/
│   ├── integration/
│   │   ├── BaseIntegrationSpec.groovy        # Core infrastructure + GrpcMock + test beans
│   │   ├── BaseGrpcIntegrationSpec.groovy    # If testing own gRPC handlers
│   │   ├── BaseRestIntegrationSpec.groovy    # If testing REST controllers
│   │   ├── BaseKafkaIntegrationSpec.groovy   # If testing Kafka consumers
│   │   └── grpcmock/                         # If using GrpcMock for external services
│   │       ├── GrpcMockServerExtension.groovy
│   │       ├── GrpcMockStubHelpers.groovy
│   │       └── stubs/
│   │           ├── ConsumerWalletStubs.groovy
│   │           ├── ExchangeRateStubs.groovy
│   │           └── ...                       # One trait per external service
│   └── {feature}/
│       └── *IntegrationSpec.groovy           # Feature-specific tests
├── java/com/bitso/{servicename}/
│   ├── grpc/
│   │   └── GrpcClientTestConfig.java         # If gRPC (only for stub beans)
│   └── support/
│       └── Test*Fixtures.java                # Test data builders
└── resources/
    └── application-test.yml

# NOTES:
# - No TestContext.java - prefer Spring Boot auto-configuration
# - Use jdbc:tc: URL pattern in application-test.yml for PostgreSQL
# - Use GrpcMock for testing real client resilience patterns
```

---

# CHECKLIST

## Analysis (Phase 1)

- [ ] Identified service type (gRPC/REST/Kafka/Mixed)
- [ ] Found all Context/Configuration classes
- [ ] Classified external vs internal dependencies
- [ ] Identified required infrastructure
- [ ] Created analysis report
- [ ] **Discovered ALL @SpringBootTest tests in scope**
- [ ] **Created migration tracking table**
- [ ] Got user confirmation on analysis AND test list

## Infrastructure (Phase 2)

- [ ] Added dependencies to build.gradle (including `testcontainers-spock`, `grpcmock-core` if needed)
- [ ] Created application-test.yml with `jdbc:tc:` URL pattern for PostgreSQL
- [ ] Created BaseIntegrationSpec with:
    - [ ] **GrpcMock for standard external gRPC services** (DEFAULT - tests real client wrappers)
    - [ ] **@MockitoBean/@SpringBean ONLY for ProtoShims/custom RPCs** (where GrpcMock is not feasible)
    - [ ] Required Testcontainers (Valkey for Redis)
    - [ ] `@DynamicPropertySource` pointing all gRPC clients to GrpcMock server
    - [ ] `setup()` with `registerDefaultGrpcStubs()` + mock reset (if @MockitoBean) or default behaviors
    - [ ] `cleanup()` with `resetGrpcMock()` + Redis flushAll + database cleanup
    - [ ] **NO separate TestContext.java** (rely on Spring Boot auto-config)

## GrpcMock Setup (if testing client resilience)

- [ ] Added `grpcmock-core` and `grpcmock-junit5` dependencies
- [ ] Created `GrpcMockServerExtension` trait
- [ ] Created service-specific stub traits (e.g., `ConsumerWalletStubs`)
- [ ] Created composite `GrpcMockStubHelpers` trait
- [ ] Static GrpcMock server started before Spring context
- [ ] All external gRPC clients point to GrpcMock address via `@DynamicPropertySource`
- [ ] `resetGrpcMock()` called in `cleanup()`

## Service-Type Setup

- [ ] gRPC: BaseGrpcIntegrationSpec + GrpcClientTestConfig with unique UUID-based server names
- [ ] REST: BaseRestIntegrationSpec with MockMvc AND unique gRPC server names (even for REST tests!)
- [ ] Kafka: BaseKafkaIntegrationSpec with @EmbeddedKafka

## Tests

- [ ] **ALL tests from Phase 1.6 tracking table migrated, skipped (with reason), or blocked (with ticket)**
- [ ] **No tests remain in PENDING status**
- [ ] Controller unit tests converted to integration test style (verify HTTP responses only, not service calls)
- [ ] Created/migrated test specs
- [ ] Using real domain services
- [ ] Only mocking external clients
- [ ] Using correct mock syntax (Mockito vs Spock)
- [ ] Using Awaitility for async assertions
- [ ] Using timeout on Kafka sends
- [ ] NO `@Transactional` on test classes
- [ ] All migrated tests pass

## Cleanup (if migrating)

- [ ] Deleted old TestContext.java and other config files
- [ ] Deleted old spec files
- [ ] Removed `@DirtiesContext`
- [ ] Removed `@Transactional`
- [ ] **No separate @TestConfiguration classes** (use Spring Boot auto-config)
- [ ] **No @Import annotations remain in test classes**
- [ ] **All config consolidated in BaseIntegrationSpec** (if truly needed)

## Migration Tracking (Phase 4)

- [ ] **CRITICAL: ALL tests in scope migrated or documented as skipped/blocked**
- [ ] **CRITICAL: No tests remain with PENDING status - every test must be MIGRATED, SKIPPED, or BLOCKED**
- [ ] Migration tracking table updated with final status for every test
- [ ] SKIPPED tests have clear reason and user approval
- [ ] BLOCKED tests have ticket number and description
- [ ] Production bugs discovered were handled (fixed or ticketed)
- [ ] Controller unit tests converted to integration test style (verify HTTP only)

## Review and Report (Phase 6)

- [ ] **Senior Java Engineer agent spawned for review**
- [ ] **All CRITICAL issues from review fixed**
- [ ] HIGH issues addressed or documented
- [ ] **Migration Report generated with:**
    - [ ] List of migrated tests
    - [ ] List of skipped/blocked tests with reasons
    - [ ] Production bugs discovered and their resolution
    - [ ] Review results summary
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/migration-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

