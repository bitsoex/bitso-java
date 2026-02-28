---
name: signadot-local-dev
description: >
  Local development workflow connecting your machine to the stage Kubernetes cluster
  using Bitso CLI and Signadot Local. Use when running services locally against cluster
  dependencies, setting up inbound/outbound connectivity, replacing Telepresence,
  or troubleshooting local-to-cluster networking.
compatibility: Requires Bitso CLI v1.9.1+ (brew upgrade bitso); Signadot CLI v1.4.0+ (installed automatically); stage cluster access
metadata:
  version: "1.0.0"
---

# Signadot Local Development

Connect your local machine to the stage Kubernetes cluster for full bidirectional connectivity. Run services locally that can both reach cluster services and receive cluster traffic.

## When to use this skill

- Connecting to the stage cluster for local development
- Running a service locally that needs cluster dependencies (databases, other services)
- Routing cluster traffic to your local machine for debugging
- Migrating from Telepresence to Bitso CLI / Signadot Local
- Troubleshooting connectivity, port mappings, or DNS resolution issues
- Working with multiple local services simultaneously

## Skill Contents

### Sections

- [Quick Start](#quick-start)
- [Outbound Connectivity](#outbound-connectivity)
- [Inbound Connectivity](#inbound-connectivity)
- [Migration from Telepresence](#migration-from-telepresence)
- [Network Caveats](#network-caveats)
- [Advanced: Signadot CLI Directly](#advanced-signadot-cli-directly)
- [Related](#related)

### Available Resources

- [bitso env commands](references/bitso-env-commands.md) -- Full flags reference for all `bitso env` commands
- [signadot local advanced](references/signadot-local-advanced.md) -- Proxy, override, traffic recording via Signadot CLI
- [troubleshooting](references/troubleshooting.md) -- Common issues and solutions

---

## Quick Start

```bash
# 1. Upgrade to latest Bitso CLI
brew upgrade bitso

# 2. Connect to stage (outbound: your machine can reach cluster services)
bitso env connect

# 3. Route cluster traffic to your local service (inbound)
bitso env local -s <your-service>

# 4. Source the auto-generated env file and start your service
source .env.<your-service>.stage.local
./gradlew bootRun  # or your service start command
```

That's it. No extra config, no YAML files, no manual port-forward setup.

## Outbound Connectivity

`bitso env connect` establishes a connection so your local machine can reach **any service** inside the stage cluster by its internal hostname.

```bash
bitso env connect
```

This single command handles automatically:

1. Signadot CLI check and authentication (browser SSO if expired)
2. Config bootstrap (`~/.signadot/config.yaml`)
3. Admin privileges (macOS Privileges app)
4. Kubernetes context for the environment
5. AWS SSO login if session is expired
6. Signadot Local tunnel establishment

### What you can do once connected

```bash
# HTTP services
curl http://orders.stage.svc.cluster.local:8080/actuator/health

# gRPC services
grpcurl -plaintext -d '{"service":""}' \
  stocks.stage.svc.cluster.local:8201 grpc.health.v1.Health/Check
```

Your local service can resolve and call any `*.stage.svc.cluster.local` hostname as if it were running inside the cluster.

### Disconnect

```bash
bitso env disconnect
```

### Check status

```bash
bitso env status
```

## Inbound Connectivity

`bitso env local` creates a per-engineer sandbox that routes cluster traffic **to your local machine**. Any request carrying your routing key is forwarded to your local ports instead of the cluster service.

### Add a service

```bash
bitso env local -s stocks
```

This command:
1. Connects to the environment (if not already connected)
2. Discovers the workload in the cluster (Rollout or Deployment)
3. Creates or updates your personal sandbox with local port mappings
4. Generates a `.env` file with the service's environment variables
5. Prints the routing key for testing

### Multiple services

```bash
# All at once
bitso env local -s stocks -s orders

# Or incrementally (sandbox updates in place)
bitso env local -s stocks
bitso env local -s orders
```

### Custom port mappings

```bash
# Format: remote[:local]
bitso env local -s stocks --port 8080 --port 9090:9091
```

### Sandbox TTL

```bash
bitso env local -s stocks --ttl 12h   # default: 14 days
```

### Remove services

```bash
# Remove one service (sandbox continues with remaining services)
bitso env local stop -s stocks

# Remove all services (deletes the entire sandbox)
bitso env local stop
```

Prefer removing services individually to avoid burning through the Signadot sandbox creation quota.

### Full workflow

```bash
# 1. Set up sandbox (also handles outbound connectivity)
bitso env local -s stocks

# 2. Source env file and start your service
source .env.stocks.stage.local
./gradlew bootRun

# 3. Test with routing key
curl -H 'ot-baggage-bitso: <routing-key>' \
  http://stocks.stage.svc.cluster.local:8080/health

# 4. Clean up when done
bitso env local stop -s stocks
bitso env disconnect
```

## Migration from Telepresence

Telepresence is retired as of April 1, 2026. Bitso CLI v1.9.1+ detects running Telepresence instances and warns about conflicts.

| Telepresence | Bitso CLI Equivalent |
|---|---|
| `telepresence connect` | `bitso env connect` |
| `telepresence intercept <svc>` | `bitso env local -s <svc>` |
| `telepresence leave <svc>` | `bitso env local stop -s <svc>` |
| `telepresence quit` | `bitso env disconnect` |
| `telepresence status` | `bitso env status` |
| Manual port-forwards | Automatic via `bitso env connect` (full cluster DNS) |
| One service at a time | Multiple services: `bitso env local -s svc1 -s svc2` |
| Manual env var export | Auto-generated `.env` files per service |

### Key improvements over Telepresence

- **One command does everything** -- auth, AWS SSO, kubectl context, and connectivity handled automatically
- **Full cluster DNS** -- resolve any `*.svc.cluster.local` hostname without individual port-forwards
- **Multi-service sandboxes** -- work on multiple services simultaneously, add or remove incrementally
- **Auto-generated .env files** -- `bitso env local` exports env vars for each service automatically
- **Clean disconnect** -- `bitso env disconnect` tears everything down cleanly

## Network Caveats

- **Ping does not work**: Signadot Local uses a Kubernetes port-forward (TCP only). ICMP/ping is not supported. Use HTTP or gRPC calls to verify connectivity.
- **Use service ports**: Services expose specific ports (typically `8080` for HTTP, `8201` for gRPC), not port 80. Check the service's Deployment/Rollout for the correct port.
- **VPN interface**: If your cluster is behind a VPN on a non-default interface, configure it in `~/.signadot/config.yaml`:

```yaml
local:
  connections:
  - cluster: stage
    outbound:
      macOSVPNInterface: utun6  # find with ifconfig
```

## Advanced: Signadot CLI Directly

Bitso CLI wraps the Signadot CLI for convenience. For advanced use cases, use the Signadot CLI directly:

```bash
# Direct connect (requires manual config)
sudo signadot local connect --cluster stage

# Status
signadot local status

# Disconnect
signadot local disconnect
```

For proxy, override, and traffic recording, see [signadot local advanced](references/signadot-local-advanced.md).

## Related

- [signadot-sandboxes skill](.claude/skills/signadot-sandboxes/SKILL.md) -- PR sandbox lifecycle, context propagation, message isolation
- [signadot-mcp skill](.claude/skills/signadot-mcp/SKILL.md) -- AI-assisted sandbox management via MCP
- [Bitso CLI v1.8.0 -- Outbound Connectivity](https://bitsomx.atlassian.net/wiki/spaces/PT/pages/5866749960)
- [Bitso CLI v1.9.0 -- Inbound Connectivity](https://bitsomx.atlassian.net/wiki/spaces/PT/pages/5911216185)
- [Signadot Local Development docs](https://www.signadot.com/local-development)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-local-dev/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

