<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: java-testing
description: >
  Java testing guidelines using Spock Framework and Groovy. Covers test structure,
  mocking patterns, persistence testing with Testcontainers, gRPC testing, and
  Spring Boot test configurations. Use when writing or reviewing tests.
compatibility: Java projects using Spock Framework
metadata:
  version: "1.0.0"
  category: testing
  tags:
    - java
    - testing
    - spock
    - groovy
    - testcontainers
---

# Java Testing

Guidelines for writing tests in Java projects using the Spock Framework and Groovy.

## When to use this skill

- Writing new tests
- Reviewing test code
- Setting up test infrastructure
- Configuring Testcontainers
- Mocking dependencies in Spring Boot tests

## Quick Start

### 1. Test Structure

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

### 2. Test Naming

- Test files: `<ClassName>Spec.groovy`
- Test methods: `"should [expected behavior] when [condition]"`

## Key Patterns

| Pattern | When to Use |
|---------|-------------|
| **Spock Mock** | Unit tests without Spring context |
| **@SpringBean** | Spring integration tests with mocks |
| **@ServiceConnection** | Testcontainers database connection |
| **InProcessServer** | gRPC client/server testing |
| **Data Tables** | Multiple test cases with same structure |

## Mocking Patterns

### Unit Tests (No Spring)

```groovy
class SomeSpec extends Specification {
    SomeInterface someInterface = Mock()

    def "test method"() {
        given:
            def underTest = new ClassUnderTest(someInterface)
        when:
            // call method
        then:
            1 * someInterface.someMethod("value") >> responseClass
    }
}
```

### Spring Integration Tests

```groovy
@SpringBootTest
class SomeSpec extends Specification {
    @SpringBean
    SomeInterface someInterface = Mock()

    def "test method"() {
        when:
            // call method
        then:
            1 * someInterface.someMethod(_) >> { throw new RuntimeException() }
    }
}
```

## Persistence Testing

Use Testcontainers with `@ServiceConnection` for database tests:

```groovy
@Configuration
class IntegrationTestConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:14-alpine")
                .withDatabaseName("test_db")
    }
}
```

## References

| Reference | Description |
|-----------|-------------|
| [references/spock-patterns.md](references/spock-patterns.md) | Advanced Spock patterns |
| [references/testcontainers-setup.md](references/testcontainers-setup.md) | Testcontainers configuration |
| [references/grpc-testing.md](references/grpc-testing.md) | gRPC test patterns |

## Related Rules

- `java/rules/java-testing-guidelines.md` - Full testing reference
- `java/rules/java-flyway-migrations.md` - Flyway for test schema

## Related Skills

| Skill | Purpose |
|-------|---------|
| [java-coverage](../java-coverage/SKILL.md) | JaCoCo test coverage |
| [gradle-standards](../gradle-standards/SKILL.md) | Test dependency bundles |
