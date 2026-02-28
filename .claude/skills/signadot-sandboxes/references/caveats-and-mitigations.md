# Caveats and Mitigations

> Source: [Bitso Confluence - Sandboxes - Caveats & Mitigations](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5231640657) (internal, requires Bitso Confluence access)

## Contents

- [How Signadot Routes Sandbox Traffic](#how-signadot-routes-sandbox-traffic)
- [Routing and Propagation](#routing-and-propagation)
- [Caching Layers](#caching-layers)
- [Shared Data Stores](#shared-data-stores)
- [Background Jobs and Consumers](#background-jobs-and-consumers)
- [External Integrations](#external-integrations)
- [Observability](#observability)
- [When NOT to Use a Sandbox](#when-not-to-use-a-sandbox)
- [Spring Boot Sandbox Profile](#spring-boot-sandbox-profile)

---
## How Signadot Routes Sandbox Traffic

A sandbox runs PR workloads alongside the stage baseline. Isolation is accomplished by request routing using a sandbox routing key (header/baggage) that must be propagated across every hop (HTTP, gRPC, and async flows).

If a hop drops the key, that call escapes to baseline.

## Routing and Propagation

**Symptoms**: Part of the call-graph "escapes" to baseline; inconsistent results.
**Why**: Missing/stripped routing key at some hop or async boundary.

**Mitigations:**
- Ensure every edge (API gateways, HTTP filters, gRPC interceptors) forwards the routing key header
- For async (Kafka, etc.), producers must copy the routing key into message headers; consumers should read only matching messages or use sandbox-specific consumer groups

See [context-propagation.md](context-propagation.md) and [message-isolation-kafka.md](message-isolation-kafka.md).

## Caching Layers

**Risks**: Cache pollution across sandboxes/baseline; stale responses hide changes.

**Mitigations:**
- Prefer disabling cache in sandbox (or very low TTL)
- If caching is required, prefix keys with the sandbox ID/routing key: `sd:${SIGNADOT_ROUTING_KEY}:${cacheName}`
- Bypass HTTP/CDN caches for sandbox traffic based on the routing header/baggage

## Shared Data Stores

**Risks**: Writes that affect everyone (permissions, DDL, seeds), noisy test data, pool exhaustion.

**Mitigations:**
- Disable automatic schema migrations in sandbox; only baseline runs them
- Avoid destructive backfills/mass updates from sandboxes; prefer idempotent, scoped writes
- Tune DB connection pools (multiple PRs increase connections); confirm DB max-connections and Hikari settings

## Background Jobs and Consumers

**Risks**: Duplicate processing, cron storms, global side effects.

**Mitigations:**
- In the `sandbox` profile, disable Spring `@Scheduled`, Quartz, Spring Batch, and message consumers by default
- If a job/consumer is essential for a test, run it with sandbox-suffixed consumer groups and guardrails

See [message-isolation-kafka.md](message-isolation-kafka.md) for consumer group isolation examples.

## External Integrations

**Risks**: Emails, payments, webhooks, third-party mutations executed from sandboxes.

**Mitigations:**
- Point sandbox traffic to test accounts/stubs/sandboxes of third parties
- Ensure webhook callbacks preserve the routing key so the flow remains inside the sandbox
- If not possible to preserve routing, disable webhooks for sandboxes and use the development cluster

## Observability

**Goals**: Make sandboxes verbose and attributable without polluting baseline signals.

**Suggestions:**
- Increase logging to `DEBUG` only in sandbox
- Optionally add a gRPC header-logging interceptor that **allowlists** safe headers (e.g., routing key) and **redacts** auth/PII-related headers
- Tag logs/traces/metrics with `sandbox=<routing-key>` (watch cardinality)

## When NOT to Use a Sandbox

Operations that must mutate shared global state should be tested in the **development** cluster:

- Permission migrations
- Role flips
- Destructive backfills
- Global schema changes

These will be addressed as isolation improves toward the ~99% target.

## Spring Boot Sandbox Profile

### application-sandbox.yaml

```yaml
# Observability
logging.level.root: DEBUG
management.tracing.sampling.probability: 1.0
management.metrics.tags.sandbox: "${SIGNADOT_ROUTING_KEY:baseline}"

# Disable global side-effect engines
spring.task.scheduling.enabled: false

# Cache with short TTL
sandbox.cache.ttl-seconds: 5

# Isolate Kafka consumer group (if running consumers)
spring.kafka.consumer.group-id: "${spring.application.name}-${SIGNADOT_ROUTING_KEY:baseline}"
```

### Java config (sandbox profile only)

```java
@Configuration
@Profile("sandbox")
public class SandboxProfileConfig {

    private static final Logger log = LoggerFactory.getLogger(SandboxProfileConfig.class);

    /** gRPC server interceptor that logs allowlisted headers only (sandbox only). */
    @Bean
    public ServerInterceptor sandboxGrpcHeaderLogger() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    ServerCallHandler<ReqT, RespT> next) {
                Metadata safe = new Metadata();
                Metadata.Key<String> routingKey =
                    Metadata.Key.of("signadot-routing-key", Metadata.ASCII_STRING_MARSHALLER);
                if (headers.containsKey(routingKey)) {
                    safe.put(routingKey, headers.get(routingKey));
                }
                log.info("[SANDBOX] gRPC {} headers: {}",
                    call.getMethodDescriptor().getFullMethodName(), safe);
                return next.startCall(call, headers);
            }
        };
    }

    /** Redis CacheManager with short TTL and sandbox-aware key prefix. */
    @Bean
    public RedisCacheManager sandboxRedisCacheManager(
            RedisConnectionFactory cf,
            @Value("${sandbox.cache.ttl-seconds:5}") long ttlSeconds,
            @Value("${SIGNADOT_ROUTING_KEY:baseline}") String routingKey) {

        RedisCacheConfiguration cfg = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(ttlSeconds))
            .computePrefixWith(cacheName -> "sd:" + routingKey + ":" + cacheName + "::")
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(cf).cacheDefaults(cfg).build();
    }

    /** Extra guardrails/logging on startup. */
    @Bean
    public ApplicationRunner sandboxStartup(LoggingSystem loggingSystem) {
        return args -> {
            loggingSystem.setLogLevel("root", LogLevel.DEBUG);
            log.warn("[SANDBOX] Sandbox profile active. "
                + "Migrations, batch jobs, and schedulers are disabled.");
        };
    }
}
```

### Important notes

- Multiple services from a single PR can run in the same sandbox; ensure all enable the `sandbox` profile
- Caches (e.g., Amplitude cache layer) should be disabled or short-TTL in sandbox
- If a scenario must change shared global state, use the development cluster
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/caveats-and-mitigations.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

