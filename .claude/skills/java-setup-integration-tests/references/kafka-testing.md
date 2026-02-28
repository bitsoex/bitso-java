# Testing Kafka Consumers with EmbeddedKafka

Test Kafka message processing using Spring EmbeddedKafka.

## Contents

- [When to use](#when-to-use)
- [Quick Start](#quick-start)
- [Key Patterns](#key-patterns)
- [Testing DLQ](#testing-dlq)
- [Using Awaitility for Async Assertions](#using-awaitility-for-async-assertions)
- [Testing CDC Events](#testing-cdc-events)
- [KafkaContainer Alternative](#kafkacontainer-alternative)
- [Important Notes](#important-notes)

---
## When to use

- Testing Kafka consumers/listeners
- Testing CDC event processing
- Testing DLQ behavior
- End-to-end Kafka message flow tests

## Quick Start

### 1. Configure EmbeddedKafka

```groovy
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["my-input-topic", "my-input-topic-dlq", "my-output-topic"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:0"]
)
// Optional: Add kraft = true for Kafka 3.3+ (KRaft mode, no ZooKeeper)
// @EmbeddedKafka(kraft = true, ...)
abstract class BaseKafkaIntegrationSpec extends BaseIntegrationSpec {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        // Enable the consumer we're testing
        registry.add("{service-name}.kafka.consumers.{consumer}.enabled", { "true" })
    }
}
```

### 2. Write Test

```groovy
class KafkaIntegrationSpec extends BaseKafkaIntegrationSpec {

    def "should process message and publish output"() {
        given: "a consumer for output events"
        def consumer = createOutputConsumer()
        def inputEvent = new InputEvent(id: 123L, data: "test")

        when: "publishing input event"
        kafkaTemplate.send(
            MessageBuilder.withPayload(inputEvent)
                .setHeader(KafkaHeaders.TOPIC, "my-input-topic")
                .build()
        ).get(10, TimeUnit.SECONDS)

        then: "output event is produced"
        def records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10))
        !records.isEmpty()
        records.iterator().next().value().id == 123L

        cleanup:
        consumer?.close()
    }

    private KafkaConsumer<String, OutputEvent> createOutputConsumer() {
        def props = KafkaTestUtils.consumerProps(
            "test-group-${UUID.randomUUID()}",
            "false",
            embeddedKafkaBroker
        )
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*")

        def consumer = new KafkaConsumer<>(props)
        consumer.subscribe(["my-output-topic"])

        // Wait for partition assignment
        while (consumer.assignment().isEmpty()) {
            consumer.poll(Duration.ofMillis(100))
        }
        return consumer
    }
}
```

## Key Patterns

| Pattern | Purpose |
|---------|---------|
| `@EmbeddedKafka` | In-memory Kafka broker |
| Unique consumer group | Prevents cross-test interference |
| `AUTO_OFFSET_RESET=latest` | Only read new messages |
| Wait for partition assignment | Avoid missing messages |

## Testing DLQ

```groovy
def "should send to DLQ after failure"() {
    given:
    def dlqConsumer = createDlqConsumer()
    def badEvent = new InputEvent(id: -1L)  // Will fail processing

    when:
    kafkaTemplate.send(buildMessage(badEvent)).get(10, TimeUnit.SECONDS)

    then:
    def dlqRecords = KafkaTestUtils.getRecords(dlqConsumer, Duration.ofSeconds(15))
    !dlqRecords.isEmpty()

    cleanup:
    dlqConsumer?.close()
}

private KafkaConsumer<String, InputEvent> createDlqConsumer() {
    def props = KafkaTestUtils.consumerProps(
        "dlq-test-group-${UUID.randomUUID()}",
        "false",
        embeddedKafkaBroker
    )
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*")

    def consumer = new KafkaConsumer<>(props)
    consumer.subscribe(["my-input-topic-dlq"])

    while (consumer.assignment().isEmpty()) {
        consumer.poll(Duration.ofMillis(100))
    }
    return consumer
}
```

## Using Awaitility for Async Assertions

```groovy
import static org.awaitility.Awaitility.await

def "processes order created event"() {
    given: "external client configured"
    when(externalClient1.notifyFulfillment(any())).thenReturn(Either.right(null))

    when: "order event is published"
    def event = '{"orderId": "123", "status": "CREATED"}'
    kafkaTemplate.send("{service-name}-topic", event).get(10, TimeUnit.SECONDS)

    then: "order is persisted"
    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
        def order = orderRepository.findById("123")
        assert order.isPresent()
        assert order.get().status == "CREATED"
    }
}
```

## Testing CDC Events

```groovy
def "should process CDC update event"() {
    given:
    def cdcEvent = buildCdcEvent(
        operation: "UPDATE",
        before: [status: "PENDING"],
        after: [status: "COMPLETED"]
    )

    when:
    kafkaTemplate.send("cdc-topic", cdcEvent).get(10, TimeUnit.SECONDS)

    then:
    await().atMost(5, TimeUnit.SECONDS).untilAsserted {
        def record = repository.findById(cdcEvent.id)
        assert record.isPresent()
        assert record.get().status == "COMPLETED"
    }
}
```

## KafkaContainer Alternative

For more realistic behavior, use Testcontainers:

```groovy
static final KafkaContainer KAFKA_CONTAINER

static {
    KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    KAFKA_CONTAINER.start()
}

@DynamicPropertySource
static void registerKafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", { KAFKA_CONTAINER.bootstrapServers })
}
```

## Important Notes

- Always use `.get(timeout, TimeUnit)` on kafkaTemplate.send() to avoid hanging tests
- Use unique consumer groups per test to prevent interference
- Wait for partition assignment before producing messages
- Use Awaitility instead of Thread.sleep() for async assertions
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/kafka-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

