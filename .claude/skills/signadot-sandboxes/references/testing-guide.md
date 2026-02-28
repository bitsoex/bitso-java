# Testing Guide

> Sources:
> - [Bitso Confluence - How to test CronJobs](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5195038804)

## Contents

- [Testing CronJobs](#testing-cronjobs)
- [Testing gRPC Services via Postman](#testing-grpc-services-via-postman)
- [Testing VirtualServices](#testing-virtualservices)
- [Webhooks and Traffic Mirroring](#webhooks-and-traffic-mirroring)

---
> **Additional sources:**
> - [Bitso Confluence - How to test VirtualServices](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5347410019)
> - [Bitso Confluence - How to Test Sandboxed Services via gRPC in Postman](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5346984799)
> - [Bitso Confluence - How to test Webhooks with traffic mirroring](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5757501443)

## Testing CronJobs

### Automatic testing

When a sandbox is created, Signadot automatically:

1. Creates resource blocks for each modified CronJob (named with initials + 4-char SHA, e.g., `ehlp760a`)
2. Creates ConfigMaps for each CronJob
3. Starts all modified CronJobs as a one-time Job via the `cronjob-test` resource plugin
4. Waits for the pod to be ready (up to 30 seconds)

**Success**: Pod starts within 30 seconds or completes successfully. Sandbox is ready.
**Failure**: Pod cannot start or fails quickly. Resource block enters failed status. Sandbox requires force delete.

### Manual testing

For CronJobs with long schedules (e.g., weekly), trigger execution manually:

**Komodor:**
1. Search for your job with PR suffix (e.g., `stocks-account-creator-495`) in `bitso-stage` cluster
2. Click "Run now" on the job page

**ArgoCD:**
1. Search for your job (e.g., `stocks-account-creator-495`)
2. Click on the CronJob kind and create the Job

### ConfigMap verification

ConfigMaps follow the same naming convention with PR suffix:
- Original: `stocks-account-creator-config`
- Sandbox: `stocks-account-creator-495-config`

### Checking entity names

View resource block logs in the Signadot dashboard to find CronJob and Job names:

```
Waiting for 'cronjob' 'stocks-settlement-recon-630' to appear...
Creating job 'stocks-settlement-recon-630-1755824873' from CronJob 'stocks-settlement-recon-630'...
Success: Pod for job 'stocks-settlement-recon-630-1755824873' is now running.
```

Use these names to find logs in Komodor, kubectl, or other K8s tools. Namespace is `stage`.

---

## Testing gRPC Services via Postman

Use Postman with Signadot local connect (via `bitso env connect`) for gRPC testing.

### Prerequisites

1. Postman (with gRPC support)
2. Signadot CLI installed and authenticated
3. Valid Signadot sandbox deployed in stage
4. Target gRPC service must have server reflection/introspection enabled

### Setup

1. **Configure local connection** (`~/.signadot/config.yaml`):

   ```yaml
   local:
     connections:
       - cluster: stage
         kubeContext: arn:aws:eks:us-east-2:722970091251:cluster/bitso-stage
   ```

2. **Connect to stage cluster**:

   ```bash
   bitso env connect
   bitso env status  # verify connection
   ```

### Postman configuration

1. **URL**: `<service-name>.stage.svc.cluster.local:<grpc-port>`
   Example: `bitso-transfer-service-v2.stage.svc.cluster.local:8201`

2. **Service definition**: Select "Use server reflection" (skip manual proto upload)

3. **Metadata tab**: Add the routing key header:

   | Key | Value |
   |-----|-------|
   | `baggage` | `sd-routing-key=<routing-key>` |

   Example: `sd-routing-key=8znq9wvxnmf68`

   _Note: Use `baggage` for OTel/W3C baggage-enabled gRPC flows. For standard HTTP flows at Bitso,
   use `ot-baggage-bitso: <routing-key>` unless your service explicitly supports W3C baggage._

### Troubleshooting

Access pod logs:

```bash
kubectl get pods -n stage | grep <sandbox-id>
kubectl logs -f <pod-name> -n stage
```

---

## Testing VirtualServices

Signadot sandboxes are network-isolated and don't provide full visibility into the service mesh. **Istio-related changes cannot be reliably verified within Signadot alone.**

### What needs DEV environment

- Traffic routing rules (VirtualServices)
- mTLS settings
- Destination rules
- Sidecar injection policies
- Mesh-wide policies

### Recommended workflow

1. **Verify Istio changes in DEV** -- Full mesh context available
2. **Merge Istio changes to Stage** -- Required before sandbox testing
3. **Test application changes in Signadot** -- Once Istio config is in place

### Alternative: Manual promotion

When using `manual` promotion for RISE deployment:

1. Merge changes to stage without promoting to production
2. Test in stage environment
3. Promote to production when verified

**Caveats:**
- Roll back changes if something does not work
- Changes must be promoted eventually to avoid backlog

### Coordination

When working in DEV, coordinate with other teams and:
- Apply changes in a controlled manner
- Use temporary or clearly named resources
- Revert/clean up configurations after validation

---

## Webhooks and Traffic Mirroring

External webhooks lack routing keys because external callers are unaware of internal routing logic. Traffic mirroring duplicates "keyless" traffic to a designated sandbox for validation.

### Configuration

Enable mirroring in the Estate Catalogue at the repository level:

```cue
ci: {
    sandbox: {
        enabled: true
        trafficMirroring: {
            routes: {
                "service-health-reports": "portals-new"
            }
        }
    }
}
```

- **Key**: Entity name
- **Value**: Route name

### How it works

Envoy filters are automatically generated based on the baseline version routing. Two filters handle external and internal traffic flows.

**Behavior:**
- Without routing key: Request hits baseline AND is mirrored to sandbox
- With routing key: Request is routed directly to sandbox (standard context propagation)

### Important notes

- If multiple sandboxes are active for the same repository, **all of them** receive mirrored traffic
- For internal traffic testing, you may need to edit `/etc/hosts` (see [Confluence guide](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/3699311145))
- Traffic mirroring enables testing external webhook callbacks using sandboxes
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/testing-guide.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

