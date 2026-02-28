# Context Propagation

> Source: [Bitso Confluence - Standardizing Context Propagation](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5137891509)

## Contents

- [Overview](#overview)
- [Header Format](#header-format)
- [How Propagation Works](#how-propagation-works)
- [Developer Quick Start](#developer-quick-start)
- [Service Integration Guidelines](#service-integration-guidelines)
- [Verification and Troubleshooting](#verification-and-troubleshooting)
- [FAQ](#faq)

---
## Overview

Context propagation ensures that a sandbox routing key flows through all downstream service calls in a distributed request. At Bitso, **OpenTracing baggage** is the current standard because the Datadog Java tracer provides incomplete W3C Baggage support across all integrations (only HTTP-to-HTTP).

## Header Format

| Environment | Header | Value |
|-------------|--------|-------|
| **Signadot sandbox** | `ot-baggage-bitso` | `<routing-key>` (e.g., `rg-12345`) |
| **Local dev (bitso env local)** | `ot-baggage-bitso` | `<routing-key>` (printed by `bitso env local`) |

The W3C Baggage format (`baggage: sd-routing-key=<key>`) is supported only for services instrumented with the OpenTelemetry libraries (e.g., [bff-services](https://github.com/bitsoex/bff-services/blob/main/bitso-libs/shared/open-telemetry/src/main/resources/application-otel.yaml)).

**Notes:**
- Keys are case-insensitive
- Values should avoid spaces and commas (use URL-safe encoding)

## How Propagation Works

### Services without OpenTelemetry SDK

The **Datadog Trace Agent** forwards OpenTracing context (`ot-baggage-bitso`) across service boundaries transparently. No code changes required.

### Services with OpenTelemetry SDK

The SDK propagates both W3C baggage and OpenTracing baggage context, but requires custom configurations. See [bff-services OTel config](https://github.com/bitsoex/bff-services/blob/main/bitso-libs/shared/open-telemetry/src/main/resources/application-otel.yaml) for an example.

### End-to-end flow

Set the header **once** at the edge (or on the originating request) and it flows through all downstream calls. In Datadog APM, you can filter traces using the `ot-baggage-bitso` facet to find all spans related to a routing key.

## Developer Quick Start

### Signadot (sandbox testing)

```bash
curl -H 'ot-baggage-bitso: rg-12345' https://stage.example.com/api/orders/123
```

### Local development (via bitso env connect)

```bash
bitso env connect
curl -H "ot-baggage-bitso: <routing-key>" \
  http://service-name.stage.svc.cluster.local:8080/endpoint
```

### Internal gRPC (via bitso env connect)

```bash
bitso env connect
grpcurl -H 'baggage: sd-routing-key=<routing-key>' \
  service-name.stage.svc.cluster.local:8201 my.Service/Method
```

## Service Integration Guidelines

### Spring Boot (HTTP)

Most services need no code changes. To **read** the context:

```java
@Component
public class BaggageLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {
        String baggage = req.getHeader("ot-baggage-bitso");
        if (baggage != null) {
            org.slf4j.MDC.put("ot-baggage-bitso", baggage);
        }
        try {
            chain.doFilter(req, res);
        } finally {
            org.slf4j.MDC.remove("ot-baggage-bitso");
        }
    }
}
```

**Outbound calls**: OkHttp, RestTemplate, and WebClient inject `ot-baggage-bitso` automatically via DD instrumentation. For custom clients:

```java
webClient.get()
    .uri(url)
    .headers(h -> h.add("ot-baggage-bitso", currentBaggage()))
    .retrieve()
    .bodyToMono(String.class);
```

### Spring Boot (gRPC)

OpenTelemetry gRPC interceptors handle baggage automatically. To read the value:

```java
@Bean
@Profile("sandbox")
public io.grpc.ServerInterceptor grpcBaggageLogger() {
    return (call, headers, next) -> {
        String baggage = headers.get(
            Metadata.Key.of("ot-baggage-bitso", Metadata.ASCII_STRING_MARSHALLER));
        if (baggage != null) {
            org.slf4j.LoggerFactory.getLogger("grpc")
                .info("ot-baggage-bitso={}", baggage);
        }
        return next.startCall(call, headers);
    };
}
```

## Verification and Troubleshooting

### Smoke test

Hit your service with `ot-baggage-bitso` set and confirm downstream services log the same value.

### Common issues

| Symptom | Cause | Fix |
|---------|-------|-----|
| Request escapes to baseline | A hop dropped the header | Check gateway filters, language clients, async consumers |
| OTel services don't propagate | Missing propagator config | Confirm `otel.propagators` includes `ot-baggage-bitso` |
| Header not visible in DD | DD displays as `bitso` facet | The `ot-baggage-` prefix is stripped by DD when converting to span attributes |

### Datadog verification

In Datadog APM, use the `bitso` facet in the left panel to filter traces/spans by routing key. This allows quick identification of all traffic flowing through specific sandboxes.

## FAQ

**Q: Do I need to change my code?**
A: Usually no. With the DD tracing setup, `ot-baggage-bitso` is propagated automatically. Only add code if you want to read it or pass it through custom clients/brokers.

**Q: Will this conflict with tracing?**
A: No. `ot-baggage-bitso` is part of the OpenTracing standard and coexists with `traceparent`/`tracestate`.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/context-propagation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

