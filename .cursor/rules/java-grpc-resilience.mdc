---
description: Java GRPC Resilience
alwaysApply: true
tags:
  - java
---

<!-- https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4236050559/Creating+gRPC+services -->

# Java gRPC Resilience

gRPC resilience is configured using a custom `grpc-resilience-starter` library

You can find it's documentation here

# grpc-resilience-starter

This library provides a set of utilities and auto configurations to enhance the resilience of gRPC
services. It extends
the `grpc-spring-boot-starter` library from `grpc-ecosystem`
to provide a set of features that are commonly used in microservices.
The supported features are:

- Deadline Propagation
- Retry
- Bulkhead
- Circuit Breaker

## Usage

To leverage the features provided by this library, you need to add the following dependency to your
project:

```groovy
implementation "com.bitso.commons:grpc-resilience-starter:{latest-version}"
```

With the dependency in place, you just need to configure the wanted resilience patterns in the
clients, and the library will auto-configure everything that is needed.

## Deadline Propagation

Deadlines are implemented using the native gRPC features. They are configured using
the service-config structure. To add a deadline to
a service, you need to add the following configuration to the `application.yml` file:

```yaml
grpc:
 client:
  test:
   address: in-process:test
   service-config:
    method-config:
     -   name:
       -   service: "com.bitso.resilienceservice.grpc.ResilienceService"
        method: "WithDelay"
      timeout: PT1S
     -   name:
       -   service: "com.bitso.resilienceservice.grpc.ResilienceService"
        method: "WithMSDelay"
      timeout: 100ms
```

The `name` field is an array of objects that specify the service and method that the configuration
will be applied to. The `timeout` field specifies the deadline that will be propagated to the
server.

Note that even that this is called `timeout`, it is actually a deadline. The deadline is propagated
to the server and the server will receive the deadline and will be able to check if the deadline has
expired. To understand the difference between them refer to
the [gRPC documentation](https://grpc.io/docs/guides/deadlines).

Deadlines are propagated in cascade, meaning that if the client sets a deadline, and the server
calls another service, the deadline will be propagated to the next service.
In case the server has configured a deadline for the downstream service, gRPC will propagate the
smallest deadline.

Both `ISO-8601` (i.e. `PT1S`) Durations and Spring `SIMPLE` (i.e. `100ms`) durations are supported.

> **_NOTE:_** We are calling it timeout because it is the term used in
> the [gRPC documentation](https://grpc.io/docs/guides/service-config).

### Default configuration and overrides

By using the [`service-config`](https://grpc.io/docs/guides/service-config) structure, we can
define a default configuration that will be applied to all services and methods and override it as
needed.
Each name entry must be unique across the entire ServiceConfig.

- If the 'method' field is empty, this MethodConfig specifies the defaults for all methods for the
specified service.
- If the 'service' field is empty, the 'method' field must be empty, and this MethodConfig specifies
the default for all methods (it's the default config).

When determining which MethodConfig to use for a given RPC, the most specific match will be used.

The following example shows how to define a default configuration and override it for a specific
method or service:

```yaml
grpc:
 client:
  test:
   address: in-process:test
   service-config:
    method-config:
     # Empty array will be the default configuration for all services and methods using the test channel
     -   name: [ ]
      timeout: PT1S
     # Specific configuration for the WithDelay and OtherMethod methods of the ResilienceService
     # Note that the service name is the full qualified name of the service, which includes the package defined in the proto file
     -   name:
       -   service: "com.bitso.resilienceservice.grpc.ResilienceService"
        method: "WithDelay"
       -   service: "com.bitso.resilienceservice.grpc.ResilienceService"
        method: "WithDelay"
      timeout: PT2S
     # Specific configuration for the OtherService gRPC service
     # This is useful in cases that you have multiple services using the same channel
     -   name:
       -   service: "com.bitso.otherservice.grpc.OtherService"
      timeout: PT3S
```

## Retry

Retries are also implemented using
the [`service-config`](https://grpc.io/docs/guides/service-config) structure. To add a retry to a
service, you need to add the following configuration to the `application.yml` file:

```yaml
grpc:
 client:
  test:
   address: in-process:test
   service-config:
    method-config:
     -   name: [ ]
      timeout: PT1S
      retry-policy:
       max-attempts: 3
       initial-backoff: PT1S
       max-backoff: PT5S
       backoff-multiplier: 1.5
       retryable-status-codes:
        - UNAVAILABLE
```

As deadlines, you can have a default configuration that apply to all methods and services and
override it as needed.

### Retry configuration reference

The `retry-policy` field has the following structure:

| Name                                  | Description                                                                                                                                         | Type                        | Example values                     |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|------------------------------------|
| `retry-policy.max-attempts`           | The maximum number of attempts for a single RPC. The initial call is considered an attempt, so maxAttempts=2 means 1 retry. Must be greater than 1. | `int`                       | `2`, `10`                          |
| `retry-policy.initial-backoff`        | The initial backoff interval for the first retry.                                                                                                   | `Duration`                  | `PT1S`, `PT1M`, `1s`               |
| `retry-policy.backoff-multiplier`     | The multiplier used to derive the exponential backoff interval.                                                                                     | `Double`                    | `0.5`, `1.5`                       |
| `retry-policy.max-backoff`            | The maximum backoff interval.                                                                                                                       | `Duration`                  | `PT1S`, `PT1M`, `1s`               |
| `retry-policy.retryable-status-codes` | The gRPC status codes that are retryable.                                                                                                           | `List<io.grpc.Status.Code>` | `UNAVAILABLE`, `DEADLINE_EXCEEDED` |

## service-config

Other than allowing to configure retries and deadline propagation, the service configuration has a
set of different configurations that can be applied to the service. The following table represents
all configurations that can be done using `service-config`:

| Name                                                        | Description                                                                                                                                                                                                                                                       | Type                                                                                                    | Example values        |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|-----------------------|
| `service-config.method-config[]`                            | Definition of a gRPC service configuration. Similar to [`gRPC service-config`](https://grpc.io/docs/guides/service-config) however not all features are supported                                                                                                | [`MethodConfig`](src/main/java/com/bitso/grpc/resiliency/configuration/ResilienceProperties.java#L67) | -                     |
| `service-config.method-config[].name[]`                     | A name of a service and method to apply the configuration. Evaluated according to what is documented in [Default configuration and overrides](#default-configuration-and-overrides) section                                                                       | [`Name`](src/main/java/com/bitso/grpc/resiliency/configuration/ResilienceProperties.java#L187)        | -                     |
| `service-config.method-config[].wait-for-ready`             | Whether RPCs sent to this method should wait until the connection is ready by default. If false, the RPC will abort immediately if there is a transient failure connecting to the server. Otherwise, gRPC will attempt to connect until the deadline is exceeded. | `boolean`                                                                                               | `true`, `false`       |
| `service-config.method-config[].timeout`                    | the default deadline for calls to this method config. This is a Java duration, and its value should look like `PT1S`                                                                                                                                              | `Duration`                                                                                              | `PT1S`, `PT1M`, `1s`  |
| `service-config.method-config[].max-request-message-bytes`  | The maximum allowed payload size for an individual request in bytes                                                                                                                                                                                               | `int`                                                                                                   | `1048576`             |
| `service-config.method-config[].max-response-message-bytes` | The maximum allowed payload size for an individual response in bytes                                                                                                                                                                                              | `int`                                                                                                   | `1048576`             |
| `service-config.method-config[].retry-policy`               | Check the [Retry configuration reference](#retry-configuration-reference)                                                                                                                                                                                         | [`RetryPolicy`](src/main/java/com/bitso/grpc/resiliency/configuration/ResilienceProperties.java#L193) | -                     |

> **_NOTE:_** All fields other than the `name` in the `service-config` are optional. If they are
> empty, the fallback will
> be the default configuration of the gRPC channel.

## Bulkhead

Bulkheads are implemented using `resilience4j` library, and they are evaluated using
the [BulkheadClientInterceptor](src/main/java/com/bitso/grpc/resiliency/bulkhead/BulkheadClientInterceptor.java).
To add a bulkhead to a service, you need to add the following configuration to the `application.yml`
file:

```yaml
grpc:
 client:
  test:
   address: in-process:test
   bulkhead:
    max-concurrent-calls: 10
    max-wait-duration: PT1S
```

If a request is halted due to not being able to acquire permission from the bulkhead,
a `Status.UNAVAILABLE` will be returned to the client with the cause being
a `BulkheadFullException`.

> **_NOTE:_** The bulkhead implementation used by the library is the SemaphoreBulkhead.

### Bulkhead configuration reference

| Name                                    | Description                                                                                                                                                                                                                                                                                                                                             | Type       | Example values        |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|-----------------------|
| `bulkhead.max-concurrent-calls`         | Maximum number of concurrent calls that can be executed by the bulkhead.                                                                                                                                                                                                                                                                                | `int`      | `10`                  |
| `bulkhead.max-wait-duration`            | Configures a maximum amount of time which the calling thread will wait to enter the bulkhead. If bulkhead has space available, entry is guaranteed and immediate. If bulkhead is full, calling threads will contest for space, if it becomes available. maxWaitDuration can be set to 0, meaning it will not wait for permission to enter the bulkhead. | `Duration` | `PT1S`, `PT1M`, `1s`  |
| `bulkhead.writable-stack-trace-enabled` | Enables writable stack traces. When set to false, Exception#getStackTrace() returns a zero length array.                                                                                                                                                                                                                                                | `boolean`  | `false`, `true`       |
| `bulkhead.fair-call-handling-enabled`   | Indicates whether FairSync or NonFairSync should be used in Semaphore. When set to true, a fair call handling strategy is used. It guarantees the order of incoming requests (FIFO) based on internal queue. When set to false, an non fair strategy will be used which does not guarantee any order of calls.                                          | `boolean`  | `false`, `true`       |

## Circuit Breaker

Circuit breakers are implemented using `resilience4j` library, and they are evaluated using
the [CircuitBreakerClientInterceptor](src/main/java/com/bitso/grpc/resiliency/circuitbreaker/CircuitBreakerClientInterceptor.java).
To add a circuit breaker to a service, you need to add the following configuration to
the `application.yml` file:

```yaml
grpc:
 client:
  test:
   address: in-process:test
   circuit-breaker:
    slow-call-duration-threshold: 5s
    sliding-window-size: '10'
    slow-call-rate-threshold: '40'
    failure-rate-threshold: '5'
    wait-duration-in-open-state: 30s
    minimum-number-of-calls: '15'
    sliding-window-type: TIME_BASED
    permitted-number-of-calls-in-half-open-state: '3'
```

If a request is halted due to an open circuit breaker, a `Status.UNAVAILABLE` will be returned to
the client with the cause being a `CallNotPermittedException`.

### Circuit Breaker configuration reference

| Name                                                                  | Description                                                                                                                                                                      | Type                        | Example values              |
|-----------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|-----------------------------|
| `circuit-breaker.sliding-window-size`                                 | The size of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.                                                                   | `int`                       | `10`                        |
| `circuit-breaker.sliding-window-type`                                 | The type of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.                                                                   | `SlidingWindowType`         | `TIME_BASED`, `COUNT_BASED` |
| `circuit-breaker.permitted-number-of-calls-in-half-open-state`        | The number of permitted calls when the CircuitBreaker is half open.                                                                                                              | `int`                       | `10`, `15`                  |
| `circuit-breaker.minimum-number-of-calls`                             | The minimum number of calls which are required (per sliding window period) before the CircuitBreaker can calculate the error rate.                                               | `int`                       | `10`, `15`                  |
| `circuit-breaker.wait-duration-in-open-state`                         | The duration the CircuitBreaker should stay open, before it switches to half open.                                                                                               | `Duration`                  | `PT1S`, `PT1M`, `1s`        |
| `circuit-breaker.failure-rate-threshold`                              | The failure rate threshold in percentage. When the failure rate is equal or greater than the threshold the CircuitBreaker transitions to open and starts short-circuiting calls. | `float`                     | `0.10`, `1`                 |
| `circuit-breaker.slow-call-rate-threshold`                            | The threshold in percentage above which the CircuitBreaker considers a call as slow.                                                                                             | `float`                     | `0.10`, `1`                 |
| `circuit-breaker.slow-call-duration-threshold`                        | The duration threshold above which calls are considered as slow.                                                                                                                 | `Duration`                  | `PT1S`, `PT1M`, `1s`        |
| `circuit-breaker.automatic-transition-from-open-to-half-open-enabled` | If enabled, the CircuitBreaker transitions to half open automatically after the waitDurationInOpenState.                                                                         | `boolean`                   | `false`, `true`             |
| `circuit-breaker.writable-stack-trace-enabled`                        | Enables writable stack traces.                                                                                                                                                   | `boolean`                   | `false`, `true`             |
| `circuit-breaker.ok-statuses`                                         | The list of status codes that are considered as 'OK' and do not count as failures. | `List<Integer>`             | `200`, `201`                |

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-grpc-resilience.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
