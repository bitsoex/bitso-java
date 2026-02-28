---
title: Test Utilities
description: Common test utility patterns and base classes for Java testing
---

# Test Utilities

Common test utility patterns for Java testing with Spock and JUnit 5.

## Contents

- [Testing Bundles](#testing-bundles)
- [Spock Base Specification](#spock-base-specification)
- [Mock Helpers](#mock-helpers)
- [Time Testing Utilities](#time-testing-utilities)
- [Exception Testing](#exception-testing)
- [Async Testing](#async-testing)
- [Related](#related)

---
## Testing Bundles

Define bundles in `gradle/libs.versions.toml` for common dependency groups:

```toml
[bundles]
# Spock testing (always used together)
testing-spock = ["spock-core", "spock-spring"]

# Spring Boot test + Spock (most common)
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]

# Integration testing with Testcontainers
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]
testing-integration-kafka = ["testcontainers-spock", "testcontainers-postgresql", "testcontainers-kafka"]
testing-integration-aws = ["testcontainers-spock", "testcontainers-localstack"]

# Flyway for database migrations
testing-flyway = ["flyway-core", "flyway-database-postgresql"]
```

Usage in modules:

```groovy
dependencies {
    // Most common setup
    testImplementation libs.bundles.testing.spring
    testImplementation libs.bundles.testing.integration
    testImplementation libs.bundles.testing.flyway

    // For Kafka services
    testImplementation libs.bundles.testing.integration.kafka
}
```

## Spock Base Specification

Create a base specification for common test patterns:

```groovy
package com.bitso.test

import spock.lang.Specification
import spock.lang.Subject

abstract class BaseSpec extends Specification {

    // Common test data builders
    def buildTestUser(Map overrides = [:]) {
        new User(
            id: overrides.id ?: UUID.randomUUID(),
            email: overrides.email ?: "test@example.com",
            name: overrides.name ?: "Test User"
        )
    }

    // Common assertions
    void assertSuccess(Result result) {
        assert result.success
        assert result.error == null
    }

    void assertError(Result result, String expectedMessage) {
        assert !result.success
        assert result.error.contains(expectedMessage)
    }
}
```

## Mock Helpers

### Creating Consistent Mock Responses

```groovy
class ServiceSpec extends Specification {

    ExternalService externalService = Mock()

    def "should handle external service responses"() {
        given: "mock returns success"
        externalService.call(_) >> successResponse()

        when:
        def result = underTest.process()

        then:
        result.success
    }

    // Helper methods for mock responses
    private ExternalResponse successResponse() {
        new ExternalResponse(status: "OK", data: "test")
    }

    private ExternalResponse errorResponse(String message) {
        new ExternalResponse(status: "ERROR", error: message)
    }
}
```

### Argument Capturing

```groovy
def "should capture and verify arguments"() {
    given:
    def capturedArg = null

    when:
    service.save(new Entity(name: "test"))

    then:
    1 * repository.save(_) >> { args ->
        capturedArg = args[0]
        args[0]  // return the saved entity
    }

    and:
    capturedArg.name == "test"
}
```

## Time Testing Utilities

### Freezing Time

```groovy
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class TimeBasedServiceSpec extends Specification {

    Clock fixedClock = Clock.fixed(
        Instant.parse("2025-01-15T10:00:00Z"),
        ZoneId.of("UTC")
    )

    def "should use fixed time in tests"() {
        given:
        def service = new TimeBasedService(fixedClock)

        when:
        def result = service.getCurrentTime()

        then:
        result == Instant.parse("2025-01-15T10:00:00Z")
    }
}
```

## Exception Testing

### Asserting Exceptions in Spock

```groovy
def "should throw specific exception with message"() {
    given:
    mockService.call(_) >> { throw new ServiceException("error") }

    when:
    service.execute()

    then:
    def ex = thrown(ProcessingException)
    ex.message == "Failed to process: error"
    ex.cause instanceof ServiceException
}

def "should not throw for valid input"() {
    when:
    service.validate(validInput)

    then:
    noExceptionThrown()
}
```

## Async Testing

Use Awaitility for async operations:

```groovy
import static org.awaitility.Awaitility.await
import static java.util.concurrent.TimeUnit.SECONDS

def "should eventually complete async operation"() {
    when:
    service.startAsyncJob()

    then:
    await().atMost(5, SECONDS).until {
        service.isJobComplete()
    }
}
```

## Related

- [Spock Patterns](spock-patterns.md) - Advanced Spock patterns
- [Testcontainers Setup](testcontainers-setup.md) - Container configuration
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/test-utilities.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

