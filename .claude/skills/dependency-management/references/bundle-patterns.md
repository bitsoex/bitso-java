---
title: Bundle Patterns
description: Dependency bundle definitions and usage patterns
---

# Bundle Patterns

Dependency bundles for cleaner, more maintainable build files.

## Contents

- [Why Use Bundles](#why-use-bundles)
- [Testing Bundles](#testing-bundles)
- [Service Bundles](#service-bundles)
- [Code Generation Bundles](#code-generation-bundles)
- [gRPC Bundles](#grpc-bundles)
- [Observability Bundles](#observability-bundles)
- [Bundle Selection Guide](#bundle-selection-guide)
- [Related](#related)

---
## Why Use Bundles

Bundles group related libraries that are commonly used together.

| Benefit | Description |
|---------|-------------|
| **Cleaner build files** | One line instead of many |
| **Consistency** | Same library set across all modules |
| **Easy updates** | Change bundle once, applies everywhere |
| **Self-documenting** | Bundle name describes purpose |

```groovy
// ❌ Verbose: 7+ lines
testImplementation libs.spring.boot.starter.test
testImplementation libs.spock.core
testImplementation libs.spock.spring
testImplementation libs.testcontainers.spock
testImplementation libs.testcontainers.postgresql

// ✅ Clean: 2 lines with bundles
testImplementation libs.bundles.testing.spring
testImplementation libs.bundles.testing.integration
```

## Testing Bundles

### Basic Testing

```toml
[bundles]
# JUnit testing
testing = ["junit-jupiter", "junit-platform-launcher"]

# Spock testing (most common at Bitso)
testing-spock = ["spock-core", "spock-spring"]

# Full testing - JUnit + Spock
testing-full = ["junit-jupiter", "junit-platform-launcher", "spock-core", "spock-spring"]
```

### Spring Boot Testing

```toml
[bundles]
# Spring Boot test starter + Spock (most common pattern)
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]
```

### Integration Testing

```toml
[bundles]
# Testcontainers with PostgreSQL (most common)
testing-integration = ["testcontainers-spock", "testcontainers-postgresql"]

# With Kafka
testing-integration-kafka = ["testcontainers-spock", "testcontainers-postgresql", "testcontainers-kafka"]

# With LocalStack (AWS)
testing-aws = ["testcontainers-spock", "testcontainers-localstack"]

# Database migrations
testing-flyway = ["flyway-core", "flyway-database-postgresql"]
```

## Service Bundles

### Spring Boot Services

```toml
[bundles]
# Web service essentials (actuator almost always included)
spring-boot-service = ["spring-boot-starter-web", "spring-boot-starter-actuator"]

# Kafka messaging
spring-boot-kafka = ["spring-kafka", "spring-kafka-test"]
```

### Usage

```groovy
dependencies {
    implementation libs.bundles.spring.boot.service

    // Kafka if needed
    implementation libs.bundles.spring.boot.kafka
}
```

## Code Generation Bundles

```toml
[bundles]
# Lombok + MapStruct (very common combination)
codegen = ["lombok", "mapstruct"]

# Annotation processors (with binding for interop)
codegen-processors = ["lombok", "mapstruct-processor", "lombok-mapstruct-binding"]
```

### Usage

```groovy
dependencies {
    implementation libs.bundles.codegen
    annotationProcessor libs.bundles.codegen.processors
}
```

## gRPC Bundles

```toml
[bundles]
# gRPC client/server essentials
grpc-core = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub"]

# gRPC with API (for advanced usage)
grpc-full = ["grpc-netty-shaded", "grpc-protobuf", "grpc-stub", "grpc-api"]

# gRPC testing
grpc-testing = ["grpc-testing", "grpc-inprocess"]
```

### Usage

```groovy
dependencies {
    implementation libs.bundles.grpc.core
    implementation libs.bundles.protobuf

    testImplementation libs.bundles.grpc.testing
}
```

## Observability Bundles

```toml
[bundles]
# OpenTelemetry essentials
otel = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp"]

# OpenTelemetry with gRPC instrumentation
otel-grpc = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc"]

# Full observability (gRPC + JDBC + Kafka)
otel-full = ["opentelemetry-spring-boot-starter", "opentelemetry-exporter-otlp", "opentelemetry-grpc", "opentelemetry-jdbc", "opentelemetry-kafka"]
```

### Usage

```groovy
dependencies {
    // Basic observability
    implementation libs.bundles.otel

    // For gRPC services
    implementation libs.bundles.otel.grpc
}
```

## Bundle Selection Guide

Choose bundles based on your module type:

| Module Type | Recommended Bundles |
|-------------|---------------------|
| **Web Service** | `spring-boot-service`, `otel`, `testing-spring`, `testing-integration` |
| **Library** | `codegen`, `testing-spock` |
| **gRPC Service** | `grpc-core`, `otel-grpc`, `grpc-testing` |
| **Kafka Consumer** | `spring-boot-kafka`, `testing-integration-kafka` |
| **Database Module** | `testing-flyway`, `testing-integration` |

### Typical gRPC Service

```groovy
dependencies {
    // Core
    implementation libs.bundles.spring.boot.service
    implementation libs.bundles.grpc.core
    implementation libs.bundles.protobuf

    // Code generation
    implementation libs.bundles.codegen
    annotationProcessor libs.bundles.codegen.processors

    // Observability
    implementation libs.bundles.otel.grpc

    // Testing
    testImplementation libs.bundles.testing.spring
    testImplementation libs.bundles.testing.integration
    testImplementation libs.bundles.grpc.testing
}
```

## Related

- [version-centralization.md](version-centralization.md) - Version management
- [../SKILL.md](.claude/skills/dependency-management/SKILL.md) - Main skill documentation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/dependency-management/references/bundle-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

