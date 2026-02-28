# Bitso CLI Environment Commands Reference

## Contents

- [bitso env connect](#bitso-env-connect)
- [bitso env disconnect](#bitso-env-disconnect)
- [bitso env status](#bitso-env-status)
- [bitso env local](#bitso-env-local)
- [bitso env local stop](#bitso-env-local-stop)

---

## bitso env connect

Establish outbound connectivity to the stage cluster. Your local machine can reach any service by its Kubernetes hostname.

```bash
bitso env connect           # defaults to stage
bitso env connect -e stage  # explicit environment
bitso env connect -e dev    # connect to dev instead
```

Handles automatically: Signadot CLI check, Signadot authentication (browser SSO), config bootstrap, admin privileges, Kubernetes context, AWS SSO, Signadot Local tunnel.

If already connected, the command detects the existing connection and skips redundant steps.

## bitso env disconnect

Tear down the cluster connection cleanly.

```bash
bitso env disconnect
```

## bitso env status

Check current connection state, Signadot CLI version, authentication, and connection health.

```bash
bitso env status
bitso env status -e stage
```

Output includes: Signadot CLI version, authentication state, connection status, active sandboxes.

## bitso env local

Create or update a personal sandbox with local port mappings. Routes cluster traffic to your local machine.

```bash
bitso env local -s <service>
```

### Flags

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--service` | `-s` | (required) | Service name(s) to add (repeatable) |
| `--environment` | `-e` | `stage` | Target environment |
| `--namespace` | `-n` | (environment name) | Kubernetes namespace |
| `--port` | | (auto-detect) | Port mapping `remote[:local]` (repeatable, single -s only) |
| `--ttl` | | `14d` | Sandbox time-to-live (e.g. 14d, 12h, 30m) |

### Examples

```bash
# Single service
bitso env local -s stocks

# Multiple services at once
bitso env local -s stocks -s orders

# Custom ports
bitso env local -s stocks --port 8080 --port 9090:9091

# Custom TTL
bitso env local -s stocks --ttl 12h

# Different environment
bitso env local -e dev -s stocks
```

### Port conflict handling

| Scenario | Behavior |
|----------|----------|
| Port occupied by another sandbox service | Auto-increments to next free port (info message shown) |
| Port occupied by a local process (your service) | Proceeds normally -- Signadot routes to it |
| Explicit `--port` conflicts with sibling service | Error with suggestion to use a different port |

## bitso env local stop

Remove services from or delete the personal sandbox.

```bash
# Remove one service (sandbox continues with remaining)
bitso env local stop -s stocks

# Remove multiple services
bitso env local stop -s stocks -s orders

# Delete entire sandbox (no -s flag)
bitso env local stop
```

### Flags

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--service` | `-s` | (none = delete all) | Service(s) to remove (repeatable) |

Prefer removing services individually to avoid burning through the Signadot sandbox creation quota. Each sandbox can be updated many times, but creating a new one counts against the fair usage policy.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-local-dev/references/bitso-env-commands.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

