# Kafka Message Isolation

> Source: [Bitso Confluence - Kafka message isolation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5167022150) and [Duplicate of Kafka message isolation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5712084993)

## Contents

- [Overview](#overview)
- [Library: msk-client](#library-msk-client)
- [Consumer Side: SignadotFilterStrategy](#consumer-side-signadotfilterstrategy)
- [Producer Side: SignadotKafkaProducerFactory](#producer-side-signadotkafkaproducerfactory)
- [Consumer Group Isolation](#consumer-group-isolation)
- [Application Configuration](#application-configuration)
- [Testing and Verification](#testing-and-verification)
- [Troubleshooting](#troubleshooting)

---
## Overview

Signadot Sandboxes run code in isolation, but services use shared Kafka topics. The `msk-client` library provides automatic message filtering based on Signadot routing keys with minimal code changes.

At Bitso, Datadog libraries handle context propagation for producers, but consumers need to know if a message should be processed or not.

## Library: msk-client

**Maven coordinates**: `com.bitso.aws.msk:msk-client`

### Importing

Add to `gradle/libs.versions.toml`:

```toml
[versions]
bitso-commons-aws-version = "3.1.0"

[libraries]
bitso-commons-aws = { module = "com.bitso.aws.msk:msk-client", version.ref = "bitso-commons-aws-version" }
```

Add to service's `build.gradle`:

```groovy
dependencies {
    implementation libs.bitso.commons.aws
}
```

## Consumer Side: SignadotFilterStrategy

`SignadotFilterStrategy` implements Spring Kafka's `RecordFilterStrategy` to selectively process consumer records based on the `ot-baggage-bitso` header.

### Behavior

**Sandbox consumer** (when `SIGNADOT_SANDBOX_ROUTING_KEY` is set):
- Processes messages where `ot-baggage-bitso` matches the routing key
- Filters out all other messages

**Baseline consumer** (when `SIGNADOT_SANDBOX_ROUTING_KEY` is NOT set):
- If `filtering-enabled: true`: Processes only messages WITHOUT `ot-baggage-bitso`
- If `filtering-enabled: false` (default): Processes all messages

### Integration

Use `SignadotConcurrentKafkaListenerContainerFactory` instead of the standard factory:

```java
@Configuration
public class KafkaConfiguration {

    @Bean("kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory,
            RecordFilterStrategy<String, byte[]> signadotFilterStrategy) {

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
            new SignadotConcurrentKafkaListenerContainerFactory<>(signadotFilterStrategy);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
```

**Key points:**
- `RecordFilterStrategy` is auto-configured by the library
- `@Profile("!production")` is recommended to limit to non-production environments
- The factory automatically applies filtering to all consumers using it

## Producer Side: SignadotKafkaProducerFactory

For cases where context propagation does not work (e.g., 3rd party vendors), the producer side stores routing key information in Redis.

### Integration

Use `SignadotKafkaProducerFactory` instead of the default:

```java
@Bean
ProducerFactory<String, byte[]> producerFactory(
        KafkaProperties kafkaProperties, RoutingKeyContext routingKeyContext) {
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    return new SignadotKafkaProducerFactory<>(props, routingKeyContext, runningEnvironment);
}
```

### Using RoutingKeyContext

Register the expected event key before producing:

```java
Order savedOrder = orderRepository.insertIdempotently(orderToSave);
routingKeyContext.add(TopicConsumer.TOPIC, savedOrder.getIdempotencyKey());
return savedOrder;
```

The key added must be from the event that will be received -- you need to know the event key before receiving it.

`RoutingKeyContext` is auto-configured and available only for lower environments (stage, local). In production, it is a bypass context.

## Consumer Group Isolation

Each Kafka listener **must** append the sandbox consumer group suffix to its group ID. This ensures sandbox and baseline consumers process messages independently.

### Configuration

Add to `application.yml`:

```yaml
signadot:
  sandbox:
    consumer-group-suffix: "${signadot.sandbox.routing.key:}"
```

### Listener pattern

```java
@KafkaListener(
    topics = {TOPIC_NAME, DLQ_TOPIC_NAME},
    groupId = "${spring.kafka.consumer.group-id:my-service-group}${signadot.sandbox.consumer-group-suffix}")
public void listen(final Message<MyEvent> message) {
    // listener logic
}
```

**Result:**
- Baseline: `my-service-group` (no suffix)
- Sandbox: `my-service-group-sandbox-abc123` (with routing key suffix)

## Application Configuration

### application.yml

```yaml
signadot:
  sandbox:
    baseline-header:
      filtering-enabled: ${BASELINE_HEADER_FILTERING_ENABLED:true}
    consumer-group-suffix: "${signadot.sandbox.routing.key:}"
    context-expiration-time-seconds: 3600
    # For local testing only. Signadot provides this automatically in sandboxes.
    # routing-key: "local-test-key"
```

### Test configuration (src/test/resources/application.yml)

```yaml
signadot:
  sandbox:
    consumer-group-suffix: ""
    baseline-header:
      filtering-enabled: false
```

Disable filtering in tests because tests run without actual Signadot sandboxes.

## Testing and Verification

### Step 1: Verify service startup logs

```
SignadotFilterStrategy initialized with SIGNADOT_SANDBOX_ROUTING_KEY: ""
SignadotFilterStrategy initialized with ENABLE_BASELINE_HEADER_FILTERING: true
```

### Step 2: Trigger an event with routing key

```bash
curl -X POST https://your-service-stage.bitso.com/api/endpoint \
  -H "Content-Type: application/json" \
  -H "ot-baggage-bitso: sandbox-test-123" \
  -d '{"your": "payload"}'
```

### Step 3: Verify in Kafka UI

Check the topic for messages with `ot-baggage-bitso` header matching the routing key.

### Step 4: Verify in Splunk

**Sandbox processing:**
```
index="k8s_stage" container_name="<entity>" source="*<entity>-<pr-number>*"
"ot-baggage-bitso" OR "<routing-key>"
```

**Baseline filtering:**
```
index="k8s_stage" container_name="<entity>" source="<entity>"
"SignadotFilterStrategy" OR "routing key"
```

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| Message not in Kafka UI | Producer not using SignadotKafkaProducerFactory | Replace default ProducerFactory |
| Sandbox not processing | Consumer group suffix missing | Add `${signadot.sandbox.consumer-group-suffix}` to groupId |
| Routing key mismatch | Header value doesn't match exactly | Verify header value in Kafka UI matches sandbox key |
| Baseline processing sandbox msgs | `filtering-enabled` is false | Set `baseline-header.filtering-enabled: true` |

### Working example

The [stocks repository](https://github.com/bitsoex/stocks) contains a complete working example of Kafka message isolation with both consumer and producer configurations.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/message-isolation-kafka.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

