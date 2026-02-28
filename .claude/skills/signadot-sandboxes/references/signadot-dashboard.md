# Signadot Dashboard

> Source: [Bitso Confluence - Signadot Dashboard](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/5258313745)

## Accessing the Dashboard

Go to [app.signadot.com/sandboxes](https://app.signadot.com/sandboxes).

**Login**: Use your work email address. The "Continue with Google" option will not work -- use Okta SSO.

## Finding a Sandbox

Sandbox names follow the pattern: `<repository-name>-<pr-number>`

Examples:
- `service-health-reports-543`
- `stocks-416`
- `bitso-transfer-53`

## Overview Tab

### Routing key and labels

The overview shows the sandbox routing key, labels, and three main sections.

### Workloads

Forked services deployed in the sandbox.

- **View Details**: Environment variables, Docker image, deployment configuration
- **View Logs**: Container logs for each workload

### Resources

Additional cluster resources including:
- ConfigMaps for each service
- Resource blocks for each CronJob and its ConfigMap

**Resource block naming**: Uses initials + 4-char SHA (max 20 characters):

| Original Name | Resource Block Name |
|---------------|---------------------|
| `entity-health-logs-processor` | `ehlp760a` |
| `service-health-metrics-processor` | `shmpf152` |
| `service-health-reports` | `shr4537` |

- **View Details**: K8s manifest showing how objects were created (large BLOB, use a good editor)
- **View Logs**: Logs from the `k8s apply` execution

### Matching Route Groups

If a branch with the same name exists in a different repo, those sandboxes belong to the same route group. Browse the matching route groups here.

## Other Tabs

### Specification

The full manifest YAML describing how the sandbox was created. Contains all information about resources and workloads.

### Workloads

Same as "View Details" in Overview > Workloads section.

### Logs

Same as "View Logs" in the Overview tab.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-sandboxes/references/signadot-dashboard.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

