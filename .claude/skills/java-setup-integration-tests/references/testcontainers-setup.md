# Testcontainers Setup

Configure Testcontainers for PostgreSQL and Redis/Valkey in integration tests.

## Contents

- [When to use](#when-to-use)
- [Option 1: jdbc:tc URL Pattern (Recommended)](#option-1-jdbctc-url-pattern-recommended)
- [Option 2: Explicit Container](#option-2-explicit-container)
- [Valkey/Redis Setup](#valkeyredis-setup)
- [Key Patterns](#key-patterns)
- [Dependencies](#dependencies)

---
## When to use

- Setting up database containers for tests
- Configuring Redis/Valkey for tests
- Choosing between jdbc:tc and explicit containers

## Option 1: jdbc:tc URL Pattern (Recommended)

Simplest approach - Spring Boot auto-creates containers.

**application-test.yml:**

```yaml
spring:
  main:
    allow-bean-definition-overriding: true

  datasources:
    write-datasource:
      url: jdbc:tc:postgresqlext:14.5:///testdb
      username: test
      password: test
      driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    read-datasource:
      url: jdbc:tc:postgresqlext:14.5:///testdb
      username: test
      password: test
      driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
```

**NOTE:** `postgresqlext` is a Bitso custom PostgreSQL image with extensions. Use `postgresql` for standard image.

## Option 2: Explicit Container

Use when you need custom configuration or Flyway control.

```groovy
static final PostgreSQLContainer<?> POSTGRES_CONTAINER

static {
    // Basic pattern - Testcontainers provides descriptive errors on failure
    POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.1-alpine")
            .withDatabaseName("{service}_test")
            .withUsername("test")
            .withPassword("test")
    POSTGRES_CONTAINER.start()
}

// Optional: Add explicit error handling for clearer failure messages
// static {
//     try {
//         POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.1-alpine")
//                 .withDatabaseName("{service}_test")
//         POSTGRES_CONTAINER.start()
//         if (!POSTGRES_CONTAINER.isRunning()) {
//             throw new IllegalStateException("PostgreSQL container failed to start")
//         }
//     } catch (Exception e) {
//         throw new IllegalStateException("Failed to start PostgreSQL. Ensure Docker is running.", e)
//     }
// }

@DynamicPropertySource
static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", { POSTGRES_CONTAINER.jdbcUrl })
    registry.add("spring.datasource.username", { POSTGRES_CONTAINER.username })
    registry.add("spring.datasource.password", { POSTGRES_CONTAINER.password })
}
```

## Valkey/Redis Setup

Use GenericContainer with Valkey image (Redis-compatible, preferred for new projects).

```groovy
static final GenericContainer<?> REDIS_CONTAINER

static {
    REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("valkey/valkey:7.2-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(Duration.ofMinutes(2))
    REDIS_CONTAINER.start()
}

// Helper methods
protected static String getRedisHost() {
    return REDIS_CONTAINER.getHost()
}

protected static Integer getRedisPort() {
    return REDIS_CONTAINER.getFirstMappedPort()
}

@DynamicPropertySource
static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("redis.{service-name}.host", { getRedisHost() })
    registry.add("redis.{service-name}.port", { getRedisPort() })
    registry.add("redis.{service-name}-ro.host", { getRedisHost() })
    registry.add("redis.{service-name}-ro.port", { getRedisPort() })
}
```

## Key Patterns

| Pattern | When to Use |
|---------|-------------|
| **jdbc:tc:** | Simple setup, auto-managed containers |
| **Explicit Container** | Custom config, Flyway, explicit cleanup |
| **Valkey** | Preferred over Redis for new projects |
| **Static containers** | Share across test classes for speed |

## Dependencies

```groovy
dependencies {
    // Core testcontainers
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:testcontainers'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:spock'

    // Flyway (if using PostgreSQL)
    testImplementation 'org.flywaydb:flyway-core'
    testImplementation 'org.flywaydb:flyway-database-postgresql'
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/testcontainers-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

