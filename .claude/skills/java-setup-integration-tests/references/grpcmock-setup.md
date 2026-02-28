# GrpcMock Setup for External gRPC Services

Set up GrpcMock to test real client resilience patterns (retries, circuit breakers, timeouts).

## Contents

- [When to Use GrpcMock vs Bean Mocking](#when-to-use-grpcmock-vs-bean-mocking)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Key Patterns](#key-patterns)
- [Example Test](#example-test)
- [File Structure](#file-structure)

---

## When to Use GrpcMock vs Bean Mocking

**GrpcMock is the DEFAULT for all external gRPC services.** Use bean mocking only when GrpcMock is not feasible.

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

**Why GrpcMock is always preferred:**
- Tests the **real client wrapper** (serialization, error mapping, configuration)
- Catches bugs hidden by bean mocking (wrong error codes, missing headers)
- Single GrpcMock server handles ALL external gRPC services efficiently
- Realistic network behavior without real network calls

## Architecture

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
│                          └─────────────────────────┘                │
└─────────────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Add Dependencies

```groovy
dependencies {
    testImplementation 'org.grpcmock:grpcmock-core:0.14.0'
    testImplementation 'org.grpcmock:grpcmock-junit5:0.14.0'
}
```

### 2. Create GrpcMockServerExtension Trait

```groovy
package com.bitso.{servicename}.integration.grpcmock

import io.grpc.MethodDescriptor
import io.grpc.Status
import org.grpcmock.GrpcMock

import static org.grpcmock.GrpcMock.*

trait GrpcMockServerExtension {

    abstract void resetGrpcMock()
    abstract int getGrpcMockPort()

    void stubSuccess(MethodDescriptor method, Object resp) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(response(resp)))
    }

    void stubAlwaysFail(MethodDescriptor method, Status status) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(statusException(status)))
    }

    void stubFailThenSucceed(MethodDescriptor method, Status failureStatus, int failureCount, Object successResponse) {
        def builder = unaryMethod(method).willReturn(statusException(failureStatus))
        (1..<failureCount).each {
            builder.nextWillReturn(statusException(failureStatus))
        }
        builder.nextWillReturn(response(successResponse))
        GrpcMock.stubFor(builder)
    }

    void stubWithDelay(MethodDescriptor method, Object resp, long delayMs) {
        GrpcMock.stubFor(unaryMethod(method).willReturn(response(resp).withFixedDelay(delayMs)))
    }

    void stubServerStreamingSuccess(MethodDescriptor method, List<?> responses) {
        def args = responses.collect { response(it) }
        GrpcMock.stubFor(serverStreamingMethod(method).willReturn(*args))
    }

    void verifyGrpc(MethodDescriptor method, Object countMatcher) {
        GrpcMock.verifyThat(calledMethod(method), countMatcher)
    }
}
```

### 3. Integrate with BaseIntegrationSpec

```groovy
@SpringBootTest
abstract class BaseIntegrationSpec extends Specification
        implements GrpcMockServerExtension, GrpcMockStubHelpers {

    private static GrpcMock GRPC_MOCK_SERVER

    static {
        // Start GrpcMock server - must be before Spring context
        GRPC_MOCK_SERVER = GrpcMock.grpcMock().build()
        GRPC_MOCK_SERVER.start()
        GrpcMock.configureFor(GRPC_MOCK_SERVER)

        Runtime.getRuntime().addShutdownHook(new Thread({
            GRPC_MOCK_SERVER.stop()
        }))
    }

    @Override
    void resetGrpcMock() { GRPC_MOCK_SERVER.resetAll() }

    @Override
    int getGrpcMockPort() { return GRPC_MOCK_SERVER.getPort() }

    protected static int getGrpcMockPortStatic() {
        return GRPC_MOCK_SERVER.getPort()
    }

    def cleanup() {
        resetGrpcMock()  // IMPORTANT: Reset between tests
        jedisPooled?.flushAll()
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        def addr = "localhost:${getGrpcMockPortStatic()}"

        // Point ALL external gRPC clients to GrpcMock
        registry.add("grpc.client.consumerwallet.address", { addr })
        registry.add("grpc.client.consumerwallet.negotiation-type", { "PLAINTEXT" })

        registry.add("grpc.client.exchangerate.address", { addr })
        registry.add("grpc.client.exchangerate.negotiation-type", { "PLAINTEXT" })
    }
}
```

## Key Patterns

| Pattern | Method | Use Case |
|---------|--------|----------|
| **Success** | `stubSuccess(method, response)` | Normal happy path |
| **Always Fail** | `stubAlwaysFail(method, Status.INTERNAL)` | Circuit breaker testing |
| **Fail then Succeed** | `stubFailThenSucceed(method, status, 2, resp)` | Retry testing |
| **With Delay** | `stubWithDelay(method, resp, 10000)` | Timeout testing |
| **Server Streaming** | `stubServerStreamingSuccess(method, [resp1, resp2])` | Server streaming responses |
| **Verify** | `verifyGrpc(method, times(3))` | Call count verification |

## Example Test

```groovy
import static org.grpcmock.GrpcMock.times

def "should retry on UNAVAILABLE status"() {
    given: "service fails twice then succeeds"
    stubFailThenSucceed(
        getConsumerWalletGetBalancesMethod(),
        io.grpc.Status.UNAVAILABLE,
        2,
        buildSimpleBalancesResponse(1L, [mxn: 1000.00])
    )

    when: "requesting balance"
    def result = mockMvc.perform(get("/api/v1/balances"))

    then: "succeeds after retries"
    result.andExpect(status().isOk())

    and: "method called 3 times"
    verifyGrpc(getConsumerWalletGetBalancesMethod(), times(3))
}
```

## File Structure

```
src/test/groovy/com/bitso/{servicename}/
├── integration/
│   ├── BaseIntegrationSpec.groovy
│   └── grpcmock/
│       ├── GrpcMockServerExtension.groovy
│       ├── GrpcMockStubHelpers.groovy
│       └── stubs/
│           ├── ConsumerWalletStubs.groovy
│           ├── ExchangeRateStubs.groovy
│           └── ...
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/grpcmock-setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

