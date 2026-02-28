# Route Groups and Multi-Sandbox Testing

> Source: [Bitso Confluence - Testing across multiple sandboxes](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5262344199)

## Contents

- [Overview](#overview)
- [How Route Groups Work](#how-route-groups-work)
- [Using PR Route Groups](#using-pr-route-groups)
- [Route Group Example](#route-group-example)
- [Accessing Route Groups](#accessing-route-groups)

---
## Overview

Signadot sandboxes are isolated from both the baseline and other sandboxes. To test changes across multiple PRs (especially across different repositories), use **Route Groups**.

A Route Group is a "virtual sandbox" that groups multiple sandboxes into a single routing context.

## How Route Groups Work

### Routing keys

- **Sandbox**: Has its own unique routing key (13 chars, e.g., `zx4ygbjd5k4s4`)
- **Route group**: Has its own routing key, but instead of forked services, has **rules** to match sandboxes

When you provide the route group's routing key as a header, requests are routed to the forked services of any sandboxes matching the route group's rules.

### Label-based matching

Route groups use [labels](https://www.signadot.com/docs/reference/sandboxes/labels) (key-value pairs on sandboxes) and [match rules](https://www.signadot.com/docs/reference/route-groups/sandbox-matching) to determine which sandboxes are included.

RISE automatically creates a route group for each PR, configured to match on the `branch` label of sandboxes.

## Using PR Route Groups

RISE automatically creates a route group matching on the `branch` label. This means:

**All PRs with the same branch name will share the same route group.**

### Steps

1. Create PRs in different repos using the **exact same branch name**
2. Each PR gets its own sandbox (as usual)
3. A route group is created matching all sandboxes with that branch label
4. Access the combined environment using the route group's routing key

### Important

- Each PR still has its own sandbox with a distinct routing key for isolated testing
- The route group provides an additional routing key for combined testing
- Adding a new PR with the same branch name automatically joins the route group

## Route Group Example

Two PRs with the same branch name `dependabot/gradle/grpc-1.73.0`:

| Repository | PR Number | Sandbox | Sandbox Routing Key |
|------------|-----------|---------|---------------------|
| `stocks` | 416 | `stocks-416` | (unique per sandbox) |
| `bff-services` | 605 | `bff-services-605` | (unique per sandbox) |

Both share the route group `dependabot-gradle-grpc-1-73-0` (derived from branch name) with its own routing key.

The route group name is derived from the branch name with sanitization (slashes replaced with hyphens, etc.).

If an additional PR is created in a different repo with the same branch name, it automatically becomes part of the same route group.

## Accessing Route Groups

### Finding the routing key

1. **PR comments**: The `signadot bot` includes the route group name and link
2. **Signadot dashboard**: Click the route group link to see details, matched sandboxes, and routing key
3. **Signadot browser extension**: Search by route group name or routing key

### Browser extension

1. Click the Signadot extension button
2. Log in with Okta
3. Search for the route group name
4. Select the route group -- the extension injects the routing key automatically

### cURL / API clients

Use the route group routing key the same way as a sandbox routing key:

```bash
curl -H 'ot-baggage-bitso: <route-group-routing-key>' \
  https://stage.example.com/api/endpoint
```

### Signadot dashboard

At [app.signadot.com](https://app.signadot.com), navigate to the route group to see:
- Matched sandboxes
- Routing key
- Specification YAML
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/route-groups-and-multi-sandbox.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

