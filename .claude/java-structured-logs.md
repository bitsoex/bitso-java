# Java Structured Logs

**Applies to:** All files

<!-- https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4235756447/RFC-34+Structured+Logs -->
# Structured Logs Standard (RFC-34)

## Key Principles

- Use structured JSON format for logs to improve searchability and analysis
- Ensure logs have consistent format with predefined fields
- Include relevant business data as separate fields rather than embedding in message text
- Follow field naming conventions to ensure consistency across services

## Implementation Options

### Using Logback

- Add dependencies:

  ```groovy
  implementation 'org.springframework.boot:spring-boot-starter-logging'
  implementation 'net.logstash.logback:logstash-logback-encoder:${latest_version}'
  ```

- Create `logback-spring.xml` in resources directory with JSON encoder configuration
- Configure to use different appenders based on environment (structured JSON for non-local environments)

## Structured Arguments

- Use structured arguments to add fields to JSON logs:

  ```java
  import static net.logstash.logback.argument.StructuredArguments.kv;
  
  log.info("Transaction processed", 
           kv("transaction_id", txn.getId()), 
           kv("user_id", user.getId()));
  ```

## Required Log Fields

- Timestamp (`@timestamp`)
- Message (`message`)
- Logger name (`logger`)
- Thread name (`thread_name`)
- Log level (`level`)
- Service name (`dd.service`)
- Environment (`dd.env`)
- Version (`dd.version`)

## Best Practices

- Add business identifiers (IDs) as separate fields instead of embedding in messages
- Keep log message text clear and concise
- Use appropriate log levels consistently
- Include enough context to understand the event without requiring additional queries
- Use snake_case for field names
- Ensure single-line paragraphs for better readability
- For logs containing objects, properly structure them rather than using toString()
- Utilize log provider features like splunk `spath` and `table` commands for analysis

---
*This rule is part of the java category.*
*Source: java/rules/java-structured-logs.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-structured-logs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
