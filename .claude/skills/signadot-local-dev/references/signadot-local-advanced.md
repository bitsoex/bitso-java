# Signadot Local Advanced Usage

Advanced Signadot CLI commands for proxy, override, and traffic recording. These go beyond what Bitso CLI provides.

## Contents

- [Local Proxy](#local-proxy)
- [Local Override](#local-override)
- [Traffic Recording](#traffic-recording)
- [Traffic Inspection](#traffic-inspection)
- [Configuration](#configuration)

---

## Local Proxy

Proxy cluster services to local ports with automatic routing key injection.

```bash
signadot local proxy --sandbox <name> --map <remote>@<local>
signadot local proxy --routegroup <name> --map <remote>@<local>
signadot local proxy --cluster <name> --map <remote>@<local>
```

### Map format

```
--map <scheme>://<host>:<port>@<host>:<port>
```

Left of `@`: URL resolved in the cluster. Right of `@`: local bind address. Schemes: `http`, `grpc`, `tcp` (tcp has no routing key injection).

### Examples

```bash
# Proxy sandbox service to localhost
signadot local proxy --sandbox feature-x \
  --map http://backend.stage.svc:8000@localhost:8001

# Raw TCP (no routing key injection)
signadot local proxy --cluster stage \
  --map tcp://postgres.db.svc:5432@localhost:5432

# Use in test scripts
export BACKEND=localhost:8001
signadot local proxy --sandbox feature-x \
  --map http://backend.stage.svc:8000@$BACKEND &
pid=$!
npm test
kill $pid
```

## Local Override

Intercept HTTP/gRPC traffic destined for a sandbox workload and route it to a local service. Requires CLI v1.3.0+ and Operator v1.2.0+.

```bash
signadot local override \
  --sandbox <sandbox> \
  --workload <workload-name> \
  --workload-port <port> \
  --with <local-address>
```

### How it works

1. All HTTP/gRPC requests to the sandbox workload are routed to your local service first
2. If your local service responds with `sd-override: true` header (HTTP) or metadata key (gRPC), that response is sent back to the client
3. If `sd-override: true` is absent, the request falls through to the sandbox workload
4. If your local service is unavailable, all requests fall through automatically

### Inverse behavior with --except-status

Override all traffic EXCEPT when your local service returns specific status codes:

```bash
signadot local override \
  --sandbox my-sandbox \
  --workload my-workload \
  --workload-port 8080 \
  --with localhost:9999 \
  --except-status 404,503
```

### Detached mode

```bash
signadot local override \
  --sandbox my-sandbox \
  --workload my-workload \
  --workload-port 8080 \
  --with localhost:9999 \
  --detach
```

### Manage overrides

```bash
signadot local override list
signadot local override delete <name> --sandbox <sandbox>
```

## Traffic Recording

Record HTTP/gRPC request/response traffic flowing through a sandbox. Requires CLI v1.3.0+ and Operator v1.2.0+.

```bash
signadot traffic record --sandbox <sandbox-name>
```

This is `signadot traffic`, NOT `signadot local`. Traffic recording does not require `signadot local connect`.

### Options

| Flag | Description |
|------|-------------|
| `--inspect` | Launch interactive TUI instead of log output |
| `--clean` | Erase previously recorded traffic before recording |
| `--out-dir <dir>` | Custom output directory |
| `--short --to-file <file>` | Record only the activity log (no request/response bodies) |

### Examples

```bash
# Record with live TUI
signadot traffic record --sandbox my-sandbox --inspect

# Record to clean directory
signadot traffic record --sandbox my-sandbox --clean --out-dir ./traffic-data

# Activity log only
signadot traffic record --sandbox my-sandbox --short --to-file ./activity.json
```

## Traffic Inspection

Browse previously recorded traffic in an interactive TUI:

```bash
signadot traffic inspect
```

## Configuration

### Connection types

Configure in `~/.signadot/config.yaml` under each connection:

| Type | Description | Config |
|------|-------------|--------|
| **PortForward** (default) | Uses kubectl port-forwarding | Requires `kubeContext` |
| **ControlPlaneProxy** | Routes through Signadot control plane | No kubectl needed. Add `type: ControlPlaneProxy` |
| **ProxyAddress** | Exposed SOCKS5 proxy (e.g. VPN) | Add `type: ProxyAddress` and `proxyAddress: <host>:<port>` |

```yaml
# Example: ControlPlaneProxy (simplest, no kubectl required)
local:
  connections:
  - cluster: stage
    type: ControlPlaneProxy
```

### Unprivileged mode

```bash
signadot local connect --cluster stage --unprivileged
```

Limited functionality: no `/etc/hosts` updates or system networking changes.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-local-dev/references/signadot-local-advanced.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

