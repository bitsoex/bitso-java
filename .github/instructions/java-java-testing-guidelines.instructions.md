---
applyTo: "**/*"
description: Java Testing Guidelines
---

# Testing Guidelines

This document outlines the guidelines for writing tests in this project.

## Framework and Naming

- Tests are written using the **Spock Framework** and **Groovy**.
- Test files must follow the naming pattern: `<ClassName>Spec.groovy`.
- Tests should follow this structure:

```groovy
   def "should do Y when X"() {
        given:
          // create test data
        when:
          // execute process
        then:
          // assert output
        where: // use test table if suited
   }
```

## Mocking

To mock dependencies we use Spock mocks. They are done in the following way:

For Spring Boot integration tests, use `@SpringBean` to inject the mock:

```groovy
@SpringBootTest
class SomeSpec extends Specification {
    @SpringBean
    SomeInterface someInterface = Mock()


   def "testname"() {
      given:
         // given
       when:
         // when
       then:
          1 * someInterface.someMethod("value called") >> responseClass
           1 * someInterface.someOtherMethod(_) >> { throw new RuntimeException() }
   }
}
```

For unit tests (non-Spring tests), instantiate the mock directly:

```groovy
class SomeSpec extends Specification {
    SomeInterface someInterface = Mock()


   def "testname"() {
      given:
         // given
          def underTest = new ClassUnderTest(someInterface) // Assuming ClassUnderTest depends on SomeInterface
       when:
         // when
       then:
        1 * someInterface.someMethod("value called") >> responseClass
        1 * someInterface.someOtherMethod(_) >> { throw new RuntimeException() }
   }
}
```

## Persistence Testing

- When testing persistence layers (e.g., repositories), always use a **real database instance**.
- Use **Testcontainers** with the **PostgreSQL** module to manage the database lifecycle.
- Utilize Spring Boot's `@ServiceConnection` annotation on the `PostgreSQLContainer` bean definition within your test configuration. This annotation automatically configures the application's `DataSource` to connect to the Testcontainers database instance, eliminating the need for manual configuration in `application.yml` or `application.yaml` for tests.

### Flyway for Test Schema Management

- **Dependencies:** Ensure you have the necessary Flyway dependencies in your `build.gradle` (typically `flyway-core` and `flyway-database-postgresql`).

- **Configuration:** Configure Flyway in your test `application.yml` (usually located in `src/test/resources/application.yml`):
  - Enable Flyway: `spring.flyway.enabled: true`
  - Specify migration locations using the `FLYWAY_MIGRATIONS_DIR` environment variable: `spring.flyway.locations: filesystem:\${FLYWAY_MIGRATIONS_DIR}/db/migrations` (adjust the sub-path `/db/migrations` as needed).
  - Consider setting `spring.flyway.postgresql.transactional-lock: false` if needed for specific PostgreSQL test scenarios.

- **Environment Variable:** The `FLYWAY_MIGRATIONS_DIR` environment variable must be set when running tests. This variable should point to the root directory containing your Flyway migration scripts (e.g., a top-level `flyway` directory in your project). You can typically set this in your `build.gradle` within the `test` task configuration:

    ```groovy
    test {
        useJUnitPlatform()
        doFirst {
            // Points to the root 'flyway' directory in the project
            environment('FLYWAY_MIGRATIONS_DIR', "${ rootDir.toString() }/flyway") 
        }
        // ... other test configurations
    }
    ```

- **Execution:** When tests run with the Spring context loaded (`@SpringBootTest`), Flyway will automatically execute the migrations against the Testcontainers database instance managed by `@ServiceConnection` before the tests start.

## gRPC Testing

### Server Testing

- Utilize the `grpc-spring-boot-starter` to spin up a real gRPC server instance for integration tests.
- Consider using **in-process gRPC** (`InProcessServerBuilder`) for faster and more lightweight server testing when appropriate.

### Client Testing

- When testing gRPC clients, always use an **in-process gRPC server** (`InProcessServerBuilder`).
- Create and start the in-process server within your test setup.
- Configure your gRPC client bean to connect to this in-process server during the test run.

## Context Management

Spring Boot test contexts are managed through a combination of dedicated configuration classes and base test classes. This approach promotes modularity and reusability of test setups.

### Test Configuration Classes

Create dedicated `@Configuration` classes for different test scopes (e.g., controller tests, integration tests). These classes define the Spring beans and configurations required for that specific type of test.

**Common Annotations and Practices:**

- `@Configuration`: Marks the class as a source of bean definitions.
- `@EnableAutoConfiguration`: Enables Spring Boot's auto-configuration mechanism.
  - Optionally use `exclude` to disable specific auto-configurations if needed (e.g., `DataSourceAutoConfiguration` for tests that don't require a database or manage it differently).
- **Integration Test Specific Beans**: For integration tests, include beans for test infrastructure like `DSLContext` (for jOOQ) or Testcontainers (e.g., `PostgreSQLContainer` with `@ServiceConnection`).

**Example Structure for a Controller Test Configuration:**

```groovy
package com.example.test.config

import com.example.service.ExternalService
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import spock.mock.DetachedMockFactory

@Configuration
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration.class]) // Exclude DB if not needed
@ComponentScan(["com.example.api.controller", "com.example.web.shared"]) // Scan controllers and shared web components
class ControllerTestConfiguration {
    def detachedMockFactory = new DetachedMockFactory()

    @Bean
    @Primary
    AuthenticationService testAuthenticationService() {
        // Return a test-specific or bypassed authentication service
        return new BypassAuthenticationService()
    }

    @Bean
    ExternalService mockExternalService() {
        return detachedMockFactory.Mock(ExternalService)
    }
}
```

**Example Structure for an Integration Test Configuration:**

```groovy
package com.example.test.config

import com.example.production.MainAppConfig // Import production config if needed
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.testcontainers.containers.PostgreSQLContainer

import javax.sql.DataSource

@Configuration
@Import(MainAppConfig.class) // Import main application configuration
@EnableAutoConfiguration
@ComponentScan(["com.example.services", "com.example.repositories"]) // Scan relevant packages
class IntegrationTestConfiguration {

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(dataSource, org.jooq.SQLDialect.POSTGRES);
    }

    @Bean
    @ServiceConnection // Manages connection to the testcontainer
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:14-alpine")
                .withDatabaseName("test_db")
                .withUsername("testuser")
                .withPassword("testpass");
    }

    @Bean
    @ServiceConnection
    public RedisContainer redisContainer() {
        return new RedisContainer("redis:7")
                .withExposedPorts(6379);
    }

    @Bean
    @Primary
    RedisOperations<?, ?, ?, ?, ?> redisOperations(RedisConnectionDetails redisConnectionDetails) {
        return new JedisWrapper(Metrics.globalRegistry, new JedisPoolConfig(), redisConnectionDetails.getStandalone().getHost(), redisConnectionDetails.getStandalone().getPort(), 1000);
    }


    // Other mock beans or test-specific configurations
}
```

### Base Test Classes

Create abstract base classes for your Spock specifications. These base classes are annotated with `@SpringBootTest` and use `@ContextConfiguration` to link to the appropriate test configuration class.

**Common Annotations and Practices:**

- `@SpringBootTest`: Bootstraps the Spring context for the test.
  - Use `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT` for controller tests that require a running web server.
- `@ContextConfiguration(classes = YourTestConfiguration.class)`: Specifies which `@Configuration` class to use for loading the context.
- `@Testcontainers`: If using Testcontainers, include this annotation at the class level of your base integration test or individual test specification.
- `@SpringBean`: Injects Spring-managed beans (or mocks defined with `@SpringBean`) into the test specification.

**Example Base Controller Test:**

```groovy
package com.example.test.base

import com.example.service.OrderService
import com.example.test.config.ControllerTestConfiguration
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
abstract class BaseControllerTest extends Specification {

    @SpringBean
    OrderService orderService = Mock() // Mock a service dependency

    // Common setup or helper methods for controller tests
}
```

**Example Base Integration Test:**

```groovy
package com.example.test.base

import com.example.test.config.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.spock.Testcontainers // If using Testcontainers
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@Testcontainers // Enable Testcontainers support for this test hierarchy
abstract class BaseIntegrationTest extends Specification {

    // Common setup or helper methods for integration tests
    // e.g., @Autowired DSLContext dslContext;
}
```

### Test Application Properties

Place a test-specific `application.yml` (or `application.properties`) file in `src/test/resources`. Spring Boot will automatically pick up this file when running tests, allowing you to override or set properties specifically for the test environment.

**Example `src/test/resources/application.yml`:**

```yaml
spring:
  application:
    name: 'my-app-test' # Differentiate test application name if needed
  flyway:
    enabled: true # Ensure Flyway is enabled for tests
    # Locations for Flyway migrations, often using an environment variable
    locations:
      - filesystem:${FLYWAY_MIGRATIONS_DIR}/db/migrations 
    postgresql:
      transactional-lock: false # May be needed for PostgreSQL test scenarios
```

This setup ensures that tests run in a controlled and isolated Spring environment, tailored to their specific needs.

## Code Review Guidelines for Tests

For test code, focus on:

- **Correctness of test assertions**: Ensure assertions properly validate expected behavior
- **Test coverage of edge cases**: Verify edge cases are adequately covered
- **Clear test naming and documentation**: Tests should have descriptive names and clear documentation
- **Code style leniency**: Be more lenient with code style and minor optimisations in test code
- **Data-driven tests**: Suggest using data-driven Spock tests with tables when appropriate
