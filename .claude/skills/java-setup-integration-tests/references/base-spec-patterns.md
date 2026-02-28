# BaseIntegrationSpec Patterns

Create base integration test specifications with proper mock setup and cleanup.

## Contents

- [When to use](#when-to-use)
- [External Dependency Mock Strategy](#external-dependency-mock-strategy)
- [Bean Mock Framework Choice](#bean-mock-framework-choice)
- [Option A: @SpringBean (Recommended for Spock)](#option-a-springbean-recommended-for-spock)
- [Option B: @MockitoBean](#option-b-mockitobean)
- [Key Patterns](#key-patterns)
- [Multiple Beans of Same Type](#multiple-beans-of-same-type)
- [Cleanup Order](#cleanup-order)

---

## When to use

- Creating a new BaseIntegrationSpec
- Choosing between GrpcMock/WireMock and @MockitoBean/@SpringBean
- Setting up mock reset strategies
- Configuring test cleanup

## External Dependency Mock Strategy

**Always prefer GrpcMock (gRPC) or WireMock (REST) over bean mocking.** Use @MockitoBean/@SpringBean **only** for ProtoShims/custom RPCs where GrpcMock/WireMock is not feasible.

| External Dependency | Mock Strategy | Why |
|---------------------|---------------|-----|
| Standard gRPC service | **GrpcMock** (always) | Tests real client wrapper, resilience, error codes |
| Standard REST service | **WireMock** (always) | Tests real HTTP client, error handling |
| ProtoShim / custom RPC | **@MockitoBean/@SpringBean** | Not mockable via GrpcMock |
| Kafka producer | **@MockitoBean/@SpringBean** | No network mock available |

See [grpcmock-setup.md](grpcmock-setup.md) for GrpcMock configuration.

## Bean Mock Framework Choice

When bean mocking IS needed (ProtoShims/custom RPCs only):

| Approach | Annotation | Mock Syntax | Verification | Reset Needed |
|----------|------------|-------------|--------------|--------------|
| **Mockito** | `@MockitoBean` | `when(mock.method()).thenReturn(value)` | `verify(mock).method()` | Yes |
| **Spock** | `@SpringBean` | `mock.method() >> value` | `1 * mock.method()` | No |

**WARNING:** You cannot mix syntaxes! With `@MockitoBean`, Spock's `>>` syntax will NOT work.

## Option A: @SpringBean (Recommended for Spock)

```groovy
@SpringBootTest
@ActiveProfiles("test")
abstract class BaseIntegrationSpec extends Specification {

    // ONLY mock ProtoShims/custom RPCs with @SpringBean
    // Standard gRPC clients → use GrpcMock instead
    @SpringBean
    UserModel userModel = Mock()        // ProtoShim - custom RPC

    def setup() {
        // NO reset needed - Spock manages mock lifecycle
        setupDefaultMockBehaviors()
    }

    def cleanup() {
        jedisPooled?.flushAll()
    }

    protected void setupDefaultMockBehaviors() {
        userModel.getUser(_) >> Optional.empty()
    }
}
```

**Spring Context Caching with @SpringBean:**

When `@SpringBean` mocks are declared ONLY in the base spec (as recommended), all test
classes share the same Spring context because they have identical mock definitions.
This is WHY we centralize mocks in the base class - it enables context caching.

If you add `@SpringBean` declarations in individual test classes, each would
get a separate Spring context (defeating the purpose of shared containers).

## Option B: @MockitoBean

```groovy
import static org.mockito.Mockito.reset
import static org.mockito.Mockito.when

@SpringBootTest
@ActiveProfiles("test")
abstract class BaseIntegrationSpec extends Specification {

    // ONLY mock ProtoShims/custom RPCs with @MockitoBean
    // Standard gRPC clients → use GrpcMock instead
    @MockitoBean
    UserModel userModel                 // ProtoShim - custom RPC

    def setup() {
        // IMPORTANT: Must reset mocks manually
        reset(userModel)
        setupDefaultMockBehaviors()
    }

    def cleanup() {
        jedisPooled?.flushAll()
    }

    protected void setupDefaultMockBehaviors() {
        when(userModel.getUser(any())).thenReturn(Optional.empty())
    }
}
```

## Key Patterns

| Pattern | Purpose |
|---------|---------|
| Centralize mocks in base class | Enables Spring context caching |
| Reset mocks in setup() | Prevents test interference (@MockitoBean only) |
| Clean data in cleanup() | Ensures test isolation |
| Use static containers | Share across test classes |
| Avoid @DirtiesContext | Use proper cleanup instead |

## Multiple Beans of Same Type

For multiple beans of the same type, use `@Qualifier` or `@Named`:

```groovy
// @MockitoBean approach
@MockitoBean
@Qualifier("primaryClient")
ExternalClient primaryClient

@MockitoBean
@Named("secondaryClient")  // Jakarta CDI (JSR-330)
ExternalClient secondaryClient

// @SpringBean approach
@SpringBean
@Qualifier("primaryClient")
ExternalClient primaryClient = Mock()

@SpringBean
@Named("secondaryClient")
ExternalClient secondaryClient = Mock()
```

**NOTE:** `@Named` is from `jakarta.inject`, `@Qualifier` is from Spring.
Prefer `@Named` when matching production beans that use `@Named` annotations.
Prefer `@Qualifier` for Spring-specific beans or gRPC stubs.

## Cleanup Order

The cleanup order depends on your service's data relationships:

```groovy
def cleanup() {
    // Reset GrpcMock stubs (if using GrpcMock for external services)
    resetGrpcMock()

    // Clear Redis data
    jedisPooled?.flushAll()

    // Clear database data (respect foreign key constraints)
    cleanupDatabase()
}

private void cleanupDatabase() {
    if (dslContext == null) return

    dslContext.transaction { configuration ->
        def ctx = configuration.dsl()
        // Truncate in correct order (children before parents)
        // ctx.truncate(CHILD_TABLE).cascade().execute()
        // ctx.truncate(PARENT_TABLE).cascade().execute()
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/base-spec-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

