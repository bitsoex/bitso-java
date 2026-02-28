# Testcontainers Setup

## Dependencies

```toml
# In gradle/libs.versions.toml
[libraries]
testcontainers-spock = { module = "org.testcontainers:spock", version.ref = "testcontainers" }
testcontainers-postgresql = { module = "org.testcontainers:postgresql", version.ref = "testcontainers" }
testcontainers-kafka = { module = "org.testcontainers:kafka", version.ref = "testcontainers" }

[bundles]
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]
```

## PostgreSQL Setup

```groovy
@Configuration
class IntegrationTestConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:14-alpine")
                .withDatabaseName("test_db")
                .withUsername("testuser")
                .withPassword("testpass");
    }
}
```

## With Flyway

```yaml
# src/test/resources/application.yml
spring:
  flyway:
    enabled: true
    locations:
      - filesystem:${FLYWAY_MIGRATIONS_DIR}/db/migrations
```

```groovy
// build.gradle
test {
    doFirst {
        environment('FLYWAY_MIGRATIONS_DIR', "${rootDir}/flyway")
    }
}
```

## Base Test Class

```groovy
@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@Testcontainers
abstract class BaseIntegrationTest extends Specification {
    // Common setup
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/testcontainers-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

