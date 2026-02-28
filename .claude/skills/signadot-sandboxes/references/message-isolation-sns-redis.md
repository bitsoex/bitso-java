# SNS and Redis Streams Isolation

> Sources:
> - [Bitso Confluence - SNS Message Isolation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5525602356)
> - [Bitso Confluence - Redis Streams Isolation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5192974366)

## SNS Message Isolation

### Overview

In Bitso's event-driven architecture, many services publish messages to SNS topics, which fan out to multiple downstream subscribers. When a service publishes an SNS message using the official AWS SNS SDK, the Datadog Java agent automatically instruments the publish call.

### Automatic instrumentation

The DD instrumentation:

- Creates or continues a trace for the publish operation
- Injects Datadog trace context into SNS message attributes
- Adds Signadot routing keys into the same attributes when Signadot is active

**No application code changes are needed.** Datadog ensures routing attributes are present if the publisher is instrumented.

### Subscriber types

#### SQS queues (dominant pattern, ~97% of subscriptions)

The [sqs-client](https://github.com/bitsoex/artifacts/packages/1830816) library handles this:

- Inspects SNS-to-SQS message attributes
- Reads the Signadot routing key
- Filters messages that don't belong to the current sandbox/routing context

See [message-isolation-sqs.md](message-isolation-sqs.md) for details.

#### HTTP endpoints (webhooks)

Rarely used (only 1 topic today, e.g., Circle external callbacks). **Not currently supported** for sandbox isolation.

For webhook testing with traffic mirroring, see [testing-guide.md](testing-guide.md#webhooks-and-traffic-mirroring).

---

## Redis Streams Isolation

### Challenge

Redis Streams don't natively support message headers or attributes. Including a routing key requires modifying the message payload itself, which introduces several issues:

- **JSON parser compatibility**: Parsers may fail on unexpected fields
- **Generic library complexity**: Not straightforward to provide methods for all use cases
- **Maintenance burden**: Grows substantially with more services and use cases

### Recommended approach: Dedicated test topics

For Redis Stream-related changes, **create a dedicated test topic (stream)** for testing within a Signadot sandbox.

**Advantages:**

- No library dependency required
- No ongoing maintenance burden
- Clean isolation between test and production streams
- Redis Stream changes are typically infrequent

### Process

1. Create a new Redis Stream topic specifically for testing
2. Run your sandbox tests against the dedicated topic
3. Delete the topic once testing is complete

### Future contributions

If you identify a recurring need for a dedicated library to handle routing keys within Redis Stream payloads, initiate a technical discussion with the RISE team before development. All generic libraries reside in [jvm-generic-libraries](https://github.com/bitsoex/jvm-generic-libraries).
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/message-isolation-sns-redis.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

