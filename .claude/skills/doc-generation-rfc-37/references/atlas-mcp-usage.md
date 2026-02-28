---
title: Atlas MCP Usage
description: Using MCP tools for querying Atlas and generating documentation
---

# Atlas MCP Usage

Using MCP tools for querying Atlas and gathering service information.

## Contents

- [Available MCP Tools](#available-mcp-tools)
- [Query Strategies](#query-strategies)
- [Integrating with Documentation](#integrating-with-documentation)
- [Best Practices](#best-practices)
- [Related](#related)

---
## Available MCP Tools

### mcp_atlas_ask_atlas

Query Atlas for service information, architecture, and code details.

**Parameters**:
- `question`: Your question about Bitso's services, code, or architecture
- `session_id`: Optional session ID for conversation continuity
- `include_steps`: Show agent's reasoning steps (default: False)

### Common Queries

#### Service Overview

```
"What is the purpose of the [service-name] service?"
"What are the main responsibilities of [service-name]?"
"Who owns the [service-name] service?"
```

#### Architecture

```
"What services does [service-name] depend on?"
"What is the blast radius if [service-name] goes down?"
"How do [service-a] and [service-b] interact?"
```

#### Code Details

```
"Find the OrderValidator class and explain how validation works"
"What are the main gRPC endpoints in [service-name]?"
"How does [feature] work in [service-name]?"
```

#### Dependencies

```
"Which services are involved in crypto deposits?"
"What databases does [service-name] use?"
"What external services does [service-name] call?"
```

## Query Strategies

### Start Broad, Then Narrow

```
# First: Get overview
"What does the payments-service do?"

# Then: Specific questions
"How does payment validation work in payments-service?"
"What happens when a payment fails in payments-service?"
```

### Use Session Continuity

```python
session_id = "doc-generation-session"

# First query
mcp_atlas_ask_atlas(
    question="What is the orders service?",
    session_id=session_id
)

# Follow-up with context
mcp_atlas_ask_atlas(
    question="How does it handle order matching?",
    session_id=session_id  # Maintains context
)
```

### Gather Specific Information

For documentation generation, query systematically:

1. **Business purpose**: "What business problem does X solve?"
2. **Core responsibilities**: "What are the main functions of X?"
3. **Dependencies**: "What services/databases does X depend on?"
4. **Key features**: "What are the main features of X?"
5. **Data models**: "What are the main data models in X?"

## Integrating with Documentation

### Overview.md Generation

```
# Query for overview content
"Describe the business purpose of [service-name]"
"What are the core responsibilities of [service-name]?"
"What domain concepts are used in [service-name]?"
```

### Architecture.md Generation

```
# Query for architecture content
"What is the architecture of [service-name]?"
"What external services does [service-name] call?"
"What databases does [service-name] use?"
"Describe the request processing flow in [service-name]"
```

### Feature Documentation

```
# Query for feature details
"How does [feature] work in [service-name]?"
"What are the gRPC endpoints for [feature]?"
"What error codes can [feature] return?"
```

## Best Practices

### Be Specific

```
# ❌ Too vague
"Tell me about orders"

# ✅ Specific
"How does the order-service handle limit order placement?"
```

### Provide Context

```
# ✅ With context
"In the clearing service, how are settlements processed for crypto trades?"
```

### Break Down Complex Questions

```
# ❌ Multiple questions at once
"How does authentication work and what are the user roles?"

# ✅ Separate queries
"How does authentication work in the auth service?"
"What user roles are supported by the auth service?"
```

## Related

- [Documentation Workflow](documentation-workflow.md) - Full generation process
- [Templates](templates.md) - RFC-37 document templates
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-generation-rfc-37/references/atlas-mcp-usage.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

