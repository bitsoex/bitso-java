# gRPC Resilience Patterns

gRPC resilience is configured using the `grpc-resilience-starter` library.

## Contents

- [Usage](#usage)
- [Deadline Propagation](#deadline-propagation)
- [Retry Configuration](#retry-configuration)
- [Bulkhead Configuration](#bulkhead-configuration)
- [Circuit Breaker Configuration](#circuit-breaker-configuration)
- [Service Config Reference](#service-config-reference)

---
## Usage

Add the dependency:

```groovy
implementation "com.bitso.commons:grpc-resilience-starter:{latest-version}"
```

## Deadline Propagation

Deadlines are configured using the service-config structure:

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      service-config:
        method-config:
          - name:
              - service: "com.bitso.myservice.grpc.MyService"
                method: "WithDelay"
            timeout: PT1S
          - name:
              - service: "com.bitso.myservice.grpc.MyService"
                method: "WithMSDelay"
            timeout: 100ms
```

Deadlines propagate in cascade - if a client sets a deadline and the server calls another service, the deadline propagates to the next service. gRPC will use the smallest deadline.

Both ISO-8601 (`PT1S`) and Spring SIMPLE (`100ms`) durations are supported.

### Default Configuration and Overrides

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      service-config:
        method-config:
          # Default for all services and methods
          - name: []
            timeout: PT1S
          # Specific configuration for methods
          - name:
              - service: "com.bitso.myservice.grpc.MyService"
                method: "SpecificMethod"
            timeout: PT2S
          # Default for entire service
          - name:
              - service: "com.bitso.otherservice.grpc.OtherService"
            timeout: PT3S
```

## Retry Configuration

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      service-config:
        method-config:
          - name: []
            timeout: PT1S
            retry-policy:
              max-attempts: 3
              initial-backoff: PT1S
              max-backoff: PT5S
              backoff-multiplier: 1.5
              retryable-status-codes:
                - UNAVAILABLE
```

### Retry Configuration Reference

| Name | Description | Type | Example |
|------|-------------|------|---------|
| `max-attempts` | Maximum attempts for a single RPC (initial call + retries) | `int` | `2`, `10` |
| `initial-backoff` | Initial backoff interval for the first retry | `Duration` | `PT1S` |
| `backoff-multiplier` | Multiplier for exponential backoff | `Double` | `1.5` |
| `max-backoff` | Maximum backoff interval | `Duration` | `PT5S` |
| `retryable-status-codes` | gRPC status codes that are retryable | `List<Code>` | `UNAVAILABLE` |

## Bulkhead Configuration

Bulkheads limit concurrent calls to prevent resource exhaustion:

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      bulkhead:
        max-concurrent-calls: 10
        max-wait-duration: PT1S
```

If a request cannot acquire permission, `Status.UNAVAILABLE` is returned with `BulkheadFullException`.

### Bulkhead Configuration Reference

| Name | Description | Type | Example |
|------|-------------|------|---------|
| `max-concurrent-calls` | Maximum concurrent calls | `int` | `10` |
| `max-wait-duration` | Maximum time to wait for permission | `Duration` | `PT1S` |
| `writable-stack-trace-enabled` | Enable writable stack traces | `boolean` | `false` |
| `fair-call-handling-enabled` | Use FIFO ordering for calls | `boolean` | `false` |

## Circuit Breaker Configuration

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      circuit-breaker:
        slow-call-duration-threshold: 5s
        sliding-window-size: 10
        slow-call-rate-threshold: 40
        failure-rate-threshold: 5
        wait-duration-in-open-state: 30s
        minimum-number-of-calls: 15
        sliding-window-type: TIME_BASED
        permitted-number-of-calls-in-half-open-state: 3
```

If the circuit is open, `Status.UNAVAILABLE` is returned with `CallNotPermittedException`.

### Circuit Breaker Configuration Reference

| Name | Description | Type | Example |
|------|-------------|------|---------|
| `sliding-window-size` | Size of sliding window for recording outcomes | `int` | `10` |
| `sliding-window-type` | Type of sliding window | `SlidingWindowType` | `TIME_BASED`, `COUNT_BASED` |
| `permitted-number-of-calls-in-half-open-state` | Permitted calls when half open | `int` | `10` |
| `minimum-number-of-calls` | Minimum calls before calculating error rate | `int` | `15` |
| `wait-duration-in-open-state` | Duration to stay open before half open | `Duration` | `30s` |
| `failure-rate-threshold` | Failure rate threshold (%) to open circuit | `float` | `5` |
| `slow-call-rate-threshold` | Slow call rate threshold (%) | `float` | `40` |
| `slow-call-duration-threshold` | Duration threshold for slow calls | `Duration` | `5s` |
| `automatic-transition-from-open-to-half-open-enabled` | Auto-transition to half open | `boolean` | `true` |

## Service Config Reference

| Name | Description | Type |
|------|-------------|------|
| `method-config[].name[]` | Service/method to apply configuration | `Name` |
| `method-config[].wait-for-ready` | Wait for connection before sending | `boolean` |
| `method-config[].timeout` | Default deadline for calls | `Duration` |
| `method-config[].max-request-message-bytes` | Maximum request payload size | `int` |
| `method-config[].max-response-message-bytes` | Maximum response payload size | `int` |
| `method-config[].retry-policy` | Retry configuration | `RetryPolicy` |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-services-rfc-33/references/resilience.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

