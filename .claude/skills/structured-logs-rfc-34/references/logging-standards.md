# Structured Logs Standard (RFC-34)

## Contents

- [Key Principles](#key-principles)
- [Implementation with Logback](#implementation-with-logback)
- [Structured Arguments](#structured-arguments)
- [Required Log Fields](#required-log-fields)
- [Best Practices](#best-practices)
- [Log Analysis](#log-analysis)
- [Single-Line Paragraphs](#single-line-paragraphs)

---
## Key Principles

- Use structured JSON format for logs to improve searchability and analysis
- Ensure logs have consistent format with predefined fields
- Include relevant business data as separate fields rather than embedding in message text
- Follow field naming conventions to ensure consistency across services

## Implementation with Logback

### Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-logging'
implementation 'net.logstash.logback:logstash-logback-encoder:${latest_version}'
```

### Configuration

Create `logback-spring.xml` in resources directory with JSON encoder configuration.
Configure to use different appenders based on environment (structured JSON for non-local environments).

## Structured Arguments

Use structured arguments to add fields to JSON logs:

```java
import static net.logstash.logback.argument.StructuredArguments.kv;
import static net.logstash.logback.argument.StructuredArguments.v;
import static net.logstash.logback.argument.StructuredArguments.e;

// Key-value pairs
log.info("Transaction processed",
         kv("transaction_id", txn.getId()),
         kv("user_id", user.getId()));

// Value in message, key-value in JSON (explicit key required)
log.info("Processing order {}", v("order_id", orderId));

// Entries for multiple values
log.info("User action", e(Map.of("action", "login", "user_id", userId)));
```

## Required Log Fields

All logs must include these fields:

| Field | Description | Source |
|-------|-------------|--------|
| `@timestamp` | Log timestamp | Logback |
| `message` | Log message text | Code |
| `logger` | Logger name | Logback |
| `thread_name` | Thread name | Logback |
| `level` | Log level | Code |
| `dd.service` | Service name | Environment |
| `dd.env` | Environment | Environment |
| `dd.version` | Service version | Environment |

## Best Practices

### Add Business Identifiers as Separate Fields

```java
// ✅ Good - searchable fields
log.info("Payment processed",
         kv("payment_id", paymentId),
         kv("user_id", userId),
         kv("amount", amount),
         kv("currency", currency));

// ❌ Bad - embedded in message
log.info("Payment {} processed for user {} with amount {} {}",
         paymentId, userId, amount, currency);
```

### Keep Log Messages Clear and Concise

```java
// ✅ Good - clear action
log.info("Order created", kv("order_id", orderId));

// ❌ Bad - verbose
log.info("The system has successfully created a new order in the database", kv("order_id", orderId));
```

### Use Appropriate Log Levels

| Level | Usage |
|-------|-------|
| `ERROR` | Errors requiring immediate attention |
| `WARN` | Unexpected conditions that don't prevent operation |
| `INFO` | Significant business events |
| `DEBUG` | Detailed diagnostic information |
| `TRACE` | Very detailed tracing information |

### Structure Objects Properly

```java
// ✅ Good - structured object
log.info("User details", kv("user", Map.of(
    "id", user.getId(),
    "email", user.getEmail(),
    "status", user.getStatus()
)));

// ❌ Bad - toString()
log.info("User details: {}", user.toString());
```

### Use snake_case for Field Names

```java
// ✅ Good
kv("transaction_id", txnId);
kv("user_email", email);

// ❌ Bad
kv("transactionId", txnId);
kv("userEmail", email);
```

### Include Enough Context

```java
// ✅ Good - enough context
log.error("Failed to process payment",
          kv("payment_id", paymentId),
          kv("user_id", userId),
          kv("error_type", e.getClass().getSimpleName()),
          kv("error_message", e.getMessage()));

// ❌ Bad - not enough context
log.error("Payment failed");
```

## Log Analysis

Utilize log provider features like Splunk `spath` and `table` commands for analysis:

```splunk
index=my_service
| spath transaction_id
| table _time, level, message, transaction_id, user_id
```

## Single-Line Paragraphs

Ensure single-line paragraphs for better readability in log aggregation systems.

```java
// ✅ Good
log.info("Processing order. Validating items and calculating total.", kv("order_id", orderId));

// ❌ Bad - multi-line will break log parsing
log.info("Processing order.\nValidating items.\nCalculating total.", kv("order_id", orderId));
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/structured-logs-rfc-34/references/logging-standards.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

