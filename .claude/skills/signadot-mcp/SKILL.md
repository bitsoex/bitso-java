---
name: signadot-mcp
description: >
  Use the Signadot MCP server for AI-assisted management of sandboxes, route groups,
  clusters, and workloads. Use when an AI agent needs to create, inspect, update, or
  delete Signadot resources, or when setting up the Signadot MCP integration.
compatibility: Requires Signadot CLI v1.4.0+; authenticated via signadot auth login
metadata:
  version: "1.0.0"
---

# Signadot MCP Integration

The Signadot MCP (Model Context Protocol) server enables AI agents to manage sandboxes and route groups directly from the IDE, without writing YAML specs or running CLI commands manually.

## When to use this skill

- Managing sandboxes or route groups from an AI agent
- Creating sandboxes with local mappings for local development
- Inspecting sandbox status, workloads, or routing keys
- Setting up the Signadot MCP server in your IDE
- Combining multiple sandboxes via route groups

## Skill Contents

### Sections

- [Prerequisites](#prerequisites)
- [MCP Server Setup](#mcp-server-setup)
- [Capabilities](#capabilities)
- [Common Prompts](#common-prompts)
- [Sandbox Management](#sandbox-management)
- [Route Group Management](#route-group-management)
- [Related](#related)

---

## Prerequisites

1. **Signadot CLI v1.4.0+** installed:

   ```bash
   brew tap signadot/tap
   brew install signadot-cli
   ```

2. **Authenticated** before first use:

   ```bash
   signadot auth login
   signadot auth status  # verify
   ```

## MCP Server Setup

The MCP server runs as a subprocess of the Signadot CLI:

```bash
signadot mcp
```

### IDE configuration

The MCP server is typically pre-configured in this repository. If not, add it to your MCP configuration:

```json
{
  "signadot": {
    "command": "signadot",
    "args": ["mcp"]
  }
}
```

Add to `.mcp.json` (Claude Code), `.cursor/mcp.json` (Cursor), or `.vscode/mcp.json` (VS Code).

## Capabilities

| Resource | Operations |
|----------|------------|
| **Sandboxes** | Create, list, retrieve, update (add forks, local mappings, virtual workloads) |
| **Route groups** | Create, list, retrieve, update (manage labels, endpoints) |
| **Clusters** | List available clusters |
| **Workloads** | Discover in-cluster workloads and endpoints |
| **Resource plugins** | List available resource plugins |
| **Devboxes** | List connected devboxes |
| **Authentication** | Check authentication status |
| **Help** | Get information about core Signadot concepts |

## Common Prompts

### Sandbox operations

- "List my active Signadot sandboxes"
- "Create a sandbox for the api-service with image myorg/api:v2.1.0"
- "Check the status of sandbox stocks-416"
- "Add a fork of payment-service to my existing sandbox"
- "Create a sandbox with a local mapping for my frontend service running on port 3000"
- "Set the LOG_LEVEL env var to debug on the api-service fork in my sandbox"

### Route group operations

- "Create a route group combining my frontend-redesign and backend-api sandboxes"
- "List all route groups"
- "Show me the routing key for route group feature-x"

### Discovery

- "What workloads are running in the stage cluster?"
- "List the available resource plugins"
- "Am I authenticated with Signadot?"

## Sandbox Management

The MCP server can perform fine-grained sandbox operations:

| Action | Description |
|--------|-------------|
| **Add or update fork workloads** | Resolve workloads, define patches, set environment variables |
| **Add or update local mappings** | Discover workloads, target ports, link to connected devboxes |
| **Add or update virtual workloads** | Create zero-cost placeholders pointing to baseline |
| **Resolve preview endpoints** | Get authenticated URLs that route to sandbox workloads |
| **Use resources** | Provision ephemeral resources via resource plugins |
| **Monitor status** | Check sandbox health and access information |

## Route Group Management

| Action | Description |
|--------|-------------|
| **Manage sandbox labels** | Match sandboxes to route groups via labels |
| **Modify existing route groups** | Update match criteria and endpoints |
| **Define preview endpoints** | Configure shared preview URLs |

## Related

- [signadot-sandboxes skill](.claude/skills/signadot-sandboxes/SKILL.md) -- Sandbox concepts, context propagation, message isolation
- [signadot-local-dev skill](.claude/skills/signadot-local-dev/SKILL.md) -- Local development with Bitso CLI
- [Signadot MCP docs](https://www.signadot.com/docs/integrations/mcp)
- [Signadot CLI overview](https://www.signadot.com/docs/reference/cli/overview)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/signadot-mcp/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

