# Resilience Testing with GrpcMock

Test retry logic, circuit breakers, and timeout handling.

## Contents

- [Testing Retry Logic](#testing-retry-logic)
- [Testing Timeout Handling](#testing-timeout-handling)
- [Testing Circuit Breaker](#testing-circuit-breaker)
- [gRPC Status Codes for Testing](#grpc-status-codes-for-testing)
- [Verification Matchers](#verification-matchers)
- [Testing Fallback Behavior](#testing-fallback-behavior)
- [Testing Bulkhead](#testing-bulkhead)

---

## Testing Retry Logic

```groovy
def "should retry on UNAVAILABLE status"() {
    given: "service fails twice then succeeds"
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

    and: "method called 3 times (2 failures + 1 success)"
    verifyGrpc(getConsumerWalletGetBalancesMethod(), times(3))
}
```

## Testing Timeout Handling

```groovy
def "should handle timeout with circuit breaker"() {
    given: "service responds slowly"
    stubWithDelay(
        getConsumerWalletGetBalancesMethod(),
        buildEmptyBalancesResponse(1L),
        10000  // 10 second delay - triggers timeout
    )

    when: "requesting balance"
    def result = mockMvc.perform(get("/api/v1/balances"))

    then: "returns error or fallback"
    result.andExpect(status().is5xxServerError())
}
```

## Testing Circuit Breaker

```groovy
def "should open circuit after repeated failures"() {
    given: "service always fails"
    stubAlwaysFail(
        getConsumerWalletGetBalancesMethod(),
        io.grpc.Status.INTERNAL
    )

    when: "making multiple requests"
    10.times {
        try {
            mockMvc.perform(get("/api/v1/balances"))
        } catch (Exception ignored) {}
    }

    then: "circuit breaker opens - fewer calls than requests"
    verifyGrpc(getConsumerWalletGetBalancesMethod(), atMost(8))
}
```

## gRPC Status Codes for Testing

| Status | When to Use |
|--------|-------------|
| `UNAVAILABLE` | Transient failure, triggers retry |
| `INTERNAL` | Server error, may trigger retry |
| `DEADLINE_EXCEEDED` | Timeout simulation |
| `RESOURCE_EXHAUSTED` | Rate limiting |
| `NOT_FOUND` | Missing resource |
| `INVALID_ARGUMENT` | Validation error |
| `PERMISSION_DENIED` | Auth failure |

## Verification Matchers

```groovy
import static org.grpcmock.GrpcMock.*

verifyGrpc(method, times(3))      // Exactly 3 times
verifyGrpc(method, atLeast(2))    // At least 2 times
verifyGrpc(method, atMost(5))     // At most 5 times
verifyGrpc(method, never())       // Never called
```

## Testing Fallback Behavior

```groovy
def "should return fallback when service unavailable"() {
    given: "service is down"
    stubAlwaysFail(
        getConsumerWalletGetBalancesMethod(),
        io.grpc.Status.UNAVAILABLE
    )

    when: "requesting balance"
    def result = mockMvc.perform(get("/api/v1/balances"))

    then: "returns cached/fallback data"
    result.andExpect(status().isOk())
          .andExpect(jsonPath('$.source').value("fallback"))
}
```

## Testing Bulkhead

```groovy
def "should reject requests when bulkhead full"() {
    given: "service responds slowly"
    stubWithDelay(getMethod(), buildResponse(), 5000)

    when: "making concurrent requests beyond bulkhead limit"
    def futures = (1..20).collect {
        CompletableFuture.supplyAsync {
            mockMvc.perform(get("/api/v1/endpoint")).andReturn()
        }
    }
    def results = futures.collect { it.join() }

    then: "some requests are rejected"
    def rejectedCount = results.count { it.response.status == 503 }
    rejectedCount > 0
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/grpcmock-resilience.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

