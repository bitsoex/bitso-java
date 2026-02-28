# Troubleshooting Local Development

## Contents

- [Bitso CLI Issues](#bitso-cli-issues)
- [Connectivity Issues](#connectivity-issues)
- [Sandbox Issues](#sandbox-issues)
- [Telepresence Conflicts](#telepresence-conflicts)

---

## Bitso CLI Issues

| Problem | Solution |
|---------|----------|
| `bitso: command not found` | Install: `brew tap bitsoex/bitso && brew install bitso` |
| `signadot CLI not found in $PATH` | Install: `brew install signadot/tap/signadot-cli` |
| Upgrade command `brew upgrade bitso-cli` fails | Correct command is `brew upgrade bitso` (no `-cli` suffix) |
| Privileges app doesn't open | Ensure Privileges.app is installed on your Mac. Contact IT if missing |
| AWS SSO login loop | Run `aws sso login --profile bitso-stage` manually to debug |

## Connectivity Issues

| Problem | Solution |
|---------|----------|
| `ping <service>.stage.svc.cluster.local` times out | Ping uses ICMP, which is not supported. Use HTTP/gRPC: `curl http://service.stage.svc.cluster.local:8080/health` |
| `curl` to service fails with connection refused | Use the correct port (typically 8080 for HTTP, 8201 for gRPC), not port 80 |
| Connection times out | Check VPN/network connection. Verify kubectl access: `kubectl cluster-info` |
| Services not resolving after connect | Check `signadot local status` or `bitso env status` for connection health. Verify `/etc/hosts` was updated |
| VPN interface not detected | Add `macOSVPNInterface` to `~/.signadot/config.yaml` (find interface with `ifconfig`) |

## Sandbox Issues

| Problem | Solution |
|---------|----------|
| "workload discovery failed" / "cannot get resource" | Missing RBAC permissions. Report in #tribe-platform-support |
| "not found" when adding a service | Service name doesn't match a Deployment or Rollout in the namespace. Check: `kubectl get rollouts,deployments -n stage` |
| Port auto-detection picks wrong ports | Use `--port` to specify explicitly: `bitso env local -s stocks --port 8080` |
| Sandbox apply times out | Default timeout is 3 minutes. Check cluster health and try again |
| Routing key not working | Ensure header name is exactly `ot-baggage-bitso` and value matches the key printed by the command |
| `DEADLINE_EXCEEDED` or `CANCELED` on downstream calls | Check for Telepresence conflicts (see below). Ensure you're using Bitso CLI v1.9.1+ |
| Port conflict with sibling service | Use `--port remote:local` to map to a different local port |

## Telepresence Conflicts

Bitso CLI v1.9.1+ detects running Telepresence instances. If you see errors related to Telepresence:

1. Disconnect Telepresence completely:

   ```bash
   telepresence quit
   ```

2. Verify it's fully stopped:

   ```bash
   telepresence status  # should show "not running" or error
   ```

3. Then connect with Bitso CLI:

   ```bash
   bitso env connect
   ```

Running both Telepresence and Signadot Local simultaneously causes networking conflicts, random timeouts, and `DEADLINE_EXCEEDED` errors on downstream gRPC calls.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-local-dev/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

