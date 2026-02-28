---
name: signadot-sandboxes
description: >
  Manage Signadot sandboxes for isolated PR testing in Kubernetes.
  Use when creating, debugging, or testing with Signadot sandboxes,
  configuring context propagation, message isolation, or route groups.
  Also use when working with sandbox YAML specs, templates, TTL, or CI/CD patterns.
compatibility: Requires Signadot CLI v1.4.0+; Signadot account with Okta SSO; stage cluster access
metadata:
  version: "2.0.0"
---

# Signadot Sandboxes

Signadot Sandboxes provide isolated, logical environments within the stage Kubernetes cluster for testing PR changes. Each sandbox contains only the changes from a specific PR and interacts with existing cluster resources through request routing and context propagation.

## When to use this skill

- Setting up or debugging a Signadot sandbox for a PR
- Configuring context propagation (`ot-baggage-bitso` header)
- Integrating message isolation (Kafka, SQS, SNS) in a Java service
- Testing across multiple sandboxes with route groups
- Troubleshooting sandbox routing or message filtering issues
- Understanding caveats around shared datastores and background jobs
- Working with sandbox YAML specs, templates, or CI/CD automation

## Skill Contents

### Sections

- [Quick Start](#quick-start)
- [Sandbox Lifecycle](#sandbox-lifecycle)
- [Sandbox CLI Management](#sandbox-cli-management)
- [Context Propagation](#context-propagation)
- [Message Isolation](#message-isolation)
- [Route Groups](#route-groups)
- [Key Caveats](#key-caveats)
- [Related](#related)

### Available Resources

- [caveats and mitigations](references/caveats-and-mitigations.md)
- [context propagation](references/context-propagation.md)
- [message isolation kafka](references/message-isolation-kafka.md)
- [message isolation sns redis](references/message-isolation-sns-redis.md)
- [message isolation sqs](references/message-isolation-sqs.md)
- [route groups and multi sandbox](references/route-groups-and-multi-sandbox.md)
- [sandboxes overview](references/sandboxes-overview.md)
- [signadot dashboard](references/signadot-dashboard.md)
- [testing guide](references/testing-guide.md)

---

## Quick Start

### Browser testing

Install the [Signadot browser extension](https://chromewebstore.google.com/detail/signadot/aigejiccjejdeiikegdjlofgcjhhnkim) to auto-inject routing keys, or use [ModHeader](https://chromewebstore.google.com/detail/modheader-modify-http-hea/idgpnmonknjnojddfkpgkljpfnnfcklj) to manually set the `ot-baggage-bitso` header.

### cURL

```bash
curl -H 'ot-baggage-bitso: <routing-key>' https://stage.example.com/api/endpoint
```

### gRPC (via bitso env connect)

```bash
bitso env connect

grpcurl -H 'baggage: sd-routing-key=<routing-key>' \
  service.stage.svc.cluster.local:8201 my.Service/Method
```

### Finding your routing key

1. **PR comments** -- The `Signadot bot` posts the routing key when the sandbox is created
2. **Signadot dashboard** -- At [app.signadot.com/sandboxes](https://app.signadot.com/sandboxes)
3. **Signadot CLI** -- `signadot sandbox get <sandbox-id>`

## Sandbox Lifecycle

Sandboxes are created automatically when a PR is opened in a repository with Signadot enabled in the Estate Catalogue (`sandbox: true` in the `.cue` file). The sandbox:

1. **Creates** after a successful build with new Docker images
2. **Deploys** forked services, CronJobs, and ConfigMaps on the stage cluster
3. **Names** itself as `<repo-name>-<pr-number>` (e.g., `stocks-416`)
4. **Provides** a unique routing key for traffic isolation
5. **Updates** automatically when new commits are pushed to the PR
6. **Deletes** automatically when the PR is closed/merged, or after 48h TTL

For full details on creation, deletion, and EC enablement, see [sandboxes overview](references/sandboxes-overview.md).

## Sandbox CLI Management

### Create or update a sandbox

Write a YAML spec file and apply it:

```yaml
name: my-sandbox
spec:
  cluster: stage
  description: Testing new feature
  forks:
  - forkOf:
      kind: Deployment
      namespace: stage
      name: my-app
    customizations:
      images:
      - image: example.com/my-app:dev-abcdef
        container: my-app
      env:
      - name: FEATURE_FLAG
        container: my-app
        operation: upsert
        value: "true"
  defaultRouteGroup:
    endpoints:
    - name: my-endpoint
      target: http://my-app.stage.svc:8080
```

```bash
signadot sandbox apply -f my-sandbox.yaml
```

### Templates

Use `@{variable}` directives, expanded at apply time with `--set`:

```bash
signadot sandbox apply -f sandbox-template.yaml \
  --set dev=jane --set cluster=stage --set tag=pr-42-abc123
```

Template features: `@{variable}` (string), `@{embed: file.txt}` (inline file), `@{var[yaml]}` (YAML expansion).

### List, get, delete

```bash
signadot sandbox list
signadot sandbox get my-sandbox -o json
signadot sandbox delete my-sandbox
```

### TTL (auto-deletion)

```yaml
spec:
  ttl:
    duration: "2h"
    offsetFrom: createdAt
```

### CI/CD patterns

```bash
# Create sandbox per PR
signadot sandbox apply -f sandbox.yaml \
  --set name="pr-${PR_NUMBER}" --set tag="${COMMIT_SHA}"

# Clean up on PR close
signadot sandbox delete "pr-${PR_NUMBER}"
```

## Context Propagation

Signadot uses header-based context propagation to route requests through sandboxed services. At Bitso, the **OpenTracing baggage** format is used:

- **Header**: `ot-baggage-bitso`
- **Value**: The sandbox routing key (e.g., `zx4ygbjd5k4s4`)

Datadog libraries automatically propagate this header across HTTP and gRPC calls for Java services. No code changes are needed for most services.

**Key points:**

- For services without OpenTelemetry SDK: DD Trace Agent forwards `ot-baggage-bitso` transparently
- For services with OpenTelemetry SDK: Custom configuration needed (see BFF services example)
- W3C Baggage format (`baggage: sd-routing-key=<key>`) is supported only for OTel-instrumented services

For Spring Boot examples (HTTP filters, gRPC interceptors) and verification via Datadog, see [context propagation](references/context-propagation.md).

## Message Isolation

Shared messaging infrastructure (Kafka, SQS, SNS) requires explicit isolation to prevent cross-contamination between sandbox and baseline.

### Kafka

Library: `com.bitso.aws.msk:msk-client`

- **Consumer side**: `SignadotFilterStrategy` filters messages by routing key in `ot-baggage-bitso` header
- **Producer side**: `SignadotKafkaProducerFactory` injects routing keys via `RoutingKeyContext`
- **Consumer groups**: Append `${signadot.sandbox.consumer-group-suffix}` to all group IDs

See [message isolation kafka](references/message-isolation-kafka.md) for complete integration guide.

### SQS

Library: `com.bitso.aws.sqs:sqs-client`

- Auto-configured via Spring Boot for stage/local environments
- Uses extractor chain: AWSTraceHeader > DirectAttribute > SnsMessageBody
- Producer side: `SignadotSqsClientDecorator` wraps SQS clients via bean post-processor

See [message isolation sqs](references/message-isolation-sqs.md) for details.

### SNS and Redis Streams

- **SNS**: DD Java agent auto-instruments SNS publish calls; downstream SQS filtering handles isolation
- **Redis Streams**: No header support; use **dedicated test topics** for sandbox testing

See [message isolation sns redis](references/message-isolation-sns-redis.md).

## Route Groups

Route groups combine multiple sandboxes into a single routing context for cross-repo testing. RISE (Release Integration Sandbox Engine) automatically creates a route group for each PR, matching on the `branch` label.

**To test across repos**: Use the same branch name in each PR. All matching sandboxes join the same route group automatically.

### Route group CLI management

```bash
signadot routegroup apply -f my-routegroup.yaml
signadot routegroup list -o json
signadot routegroup get my-routegroup
signadot routegroup delete my-routegroup
```

### Match criteria

- **Single label**: `match.label: {key: "k", value: "v"}`
- **Any (OR)**: `match.any: [{label: ...}, ...]`
- **All (AND)**: `match.all: [{label: ...}, ...]`
- Values support glob patterns: `value: "feature-*"`

For details on label matching and examples, see [route groups and multi-sandbox](references/route-groups-and-multi-sandbox.md).

## Key Caveats

Sandboxes share the stage cluster's datastores and infrastructure. Be aware of:

| Area | Risk | Mitigation |
|------|------|------------|
| **Routing** | Header dropped at a hop | Ensure all edges forward `ot-baggage-bitso` |
| **Caching** | Cache pollution across envs | Disable cache or prefix keys with routing key |
| **Databases** | Shared Postgres/Redis writes | Disable migrations in sandbox; use scoped writes |
| **Background jobs** | Duplicate processing | Disable schedulers in sandbox profile |
| **External APIs** | Payments, emails, webhooks | Use test accounts/stubs |
| **Istio** | VirtualServices not mesh-aware | Test Istio changes in DEV cluster |

**When NOT to use sandboxes**: Operations that mutate shared global state (permission migrations, destructive backfills). Use the development cluster instead.

For Spring Boot `sandbox` profile examples and full mitigation guide, see [caveats and mitigations](references/caveats-and-mitigations.md).

## Related

- [signadot-local-dev skill](.claude/skills/signadot-local-dev/SKILL.md) -- Local development with Bitso CLI and Signadot Local
- [signadot-mcp skill](.claude/skills/signadot-mcp/SKILL.md) -- AI-assisted sandbox management via MCP
- [Signadot official docs](https://www.signadot.com/docs/overview)
- [Bitso Sandboxes Confluence](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5089263857/Sandboxes)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

