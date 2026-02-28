# SQS Message Isolation

> Source: [Bitso Confluence - SQS Message Isolation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5180588215)

## Contents

- [Overview](#overview)
- [Library: sqs-client](#library-sqs-client)
- [Producing to SQS](#producing-to-sqs)
- [Consuming from SQS](#consuming-from-sqs)
- [Configuration](#configuration)
- [Log Examples](#log-examples)

---
## Overview

When services run in Signadot sandboxes, SQS message isolation ensures that messages from sandbox services are only processed by other sandbox services, and baseline messages are only processed by baseline services.

The `sqs-client` library provides Spring Boot auto-configuration for isolation in stage and local environments.

## Library: sqs-client

**Maven coordinates**: `com.bitso.aws.sqs:sqs-client`

### Importing

Add to `gradle/libs.versions.toml`:

```toml
bitso-sqs-client = { module = "com.bitso.aws.sqs:sqs-client", version = "3.0.3" }
```

Add to service's `build.gradle`:

```groovy
dependencies {
    implementation libs.bitso.sqs.client
}
```

## Producing to SQS

### With Datadog instrumentation (default)

The routing key is embedded automatically in the `AWSTraceHeader` attribute by Datadog:

```
Root=1-6877f0a8-000000006d32ed3643e739d5;Parent=6607a6e72015738e;Sampled=0;bitso=qnwwnw3vt7t12
```

The `bitso=<routing-key>` part contains the routing key for message filtering.

### Without Datadog instrumentation

The `sqs-client` library detects the missing instrumentation and wraps SQS clients with `SignadotSqsClientDecorator` via bean post-processors. Messages are tagged with an `ot-baggage-bitso` message attribute containing the routing key.

## Consuming from SQS

The library uses a chain of extractors to find routing keys, checked in priority order:

### Extractor chain

| Priority | Extractor | Source |
|----------|-----------|--------|
| 1 | `TraceHeaderExtractor` | `AWSTraceHeader` system attribute (regex: `bitso=([^;]+)`) |
| 2 | `DirectAttributeExtractor` | `ot-baggage-bitso` message attribute |
| 3 | `SnsMessageExtractor` | SNS message body wrapper (for SQS subscribed to SNS) |

The chain stops at the first extractor that finds a routing key.

### Filtering behavior

**Sandbox mode** (routing key set):
- Processes messages where any extractor finds a matching routing key
- Filters out messages with no routing key or a different routing key

**Baseline mode** (no routing key):
- If `filtering-enabled: true`: Filters out messages where any extractor finds a routing key
- If `filtering-enabled: false`: Processes all messages

### Polling with filter

```java
List<Message> filteredMessages = client.pollWithFilter(20, 10);
```

## Configuration

### Spring Boot auto-configuration

The library auto-configures for stage and local environments:

```java
@AutoConfiguration
@ConditionalOnExpression(
    "T(java.util.List).of('stage', 'local').contains('${ENVIRONMENT:${environment:}}')")
@EnableConfigurationProperties({SignadotProperties.class})
public class SignadotAutoConfiguration {
    // Registers: TraceHeaderExtractor (P1), DirectAttributeExtractor (P2),
    // SnsMessageExtractor (P3), SignadotRoutingKeyExtractorChain,
    // SignadotMessageFilterStrategy, SignadotBeanPostProcessor
}
```

### application.yml

```yaml
signadot:
  sandbox:
    baseline-header:
      filtering-enabled: ${ENABLE_BASELINE_HEADER_FILTERING:true}
```

### Baseline header filtering

| Value | Behavior |
|-------|----------|
| `true` (default) | Baseline filters out messages with routing headers |
| `false` | Baseline processes all messages regardless of routing headers |

## Log Examples

### Sandbox producer (Datadog active)

```
DEBUG SignadotSqsClientDecorator - Active trace found (traceId=...), trace context will propagate automatically
INFO  SqsMessageSender - Successfully sent message to SQS with ID: 340cc4c4-...
```

### Sandbox producer (no Datadog)

```
DEBUG SignadotSqsClientDecorator - Trace ID is zero or null, no active trace exists. Adding Signadot attributes.
DEBUG SignadotAttributeUtils - Created Signadot attributes with routing key: cf6ksf4rshqk4
DEBUG MessageAttributeUtils - Created string attribute: ot-baggage-bitso = cf6ksf4rshqk4
```

### Sandbox consumer (filtering out)

```
DEBUG TraceHeaderExtractor - found routing key: cf6ksf4rshqk4
DEBUG SignadotMessageFilterStrategy - Filtering out message ... - routing key mismatch. Expected: abc123, Got: cf6ksf4rshqk4
```

### Baseline consumer (filtering enabled)

```
DEBUG TraceHeaderExtractor - found routing key: cf6ksf4rshqk4
DEBUG SignadotMessageFilterStrategy - Filtering out message ... - routing key found for baseline service
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/message-isolation-sqs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

