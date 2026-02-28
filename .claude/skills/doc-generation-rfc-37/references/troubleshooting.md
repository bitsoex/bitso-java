---
title: Troubleshooting
description: Common issues and solutions for documentation generation
---

# Troubleshooting

Common issues and solutions for documentation generation.

## Contents

- [Atlas Query Issues](#atlas-query-issues)
- [Confluence Sync Issues](#confluence-sync-issues)
- [Documentation Structure Issues](#documentation-structure-issues)
- [Template Issues](#template-issues)
- [Idempotency Issues](#idempotency-issues)
- [Multi-Service Repositories](#multi-service-repositories)
- [Performance Issues](#performance-issues)
- [Related](#related)

---
## Confluence Sync Issues

### Confluence Title Collision

**Symptom**: `mark` sync fails with:

```
FATAL unable to resolve page
  └─ unexpected ancestry tree, did not find expected parent page in the tree
```

**Cause**: The H1 title in a document (e.g., `# Architecture`) matches an existing page name in the Confluence space under a different parent. When `title-from-h1 = true`, `mark` uses the H1 as the page title, and Confluence page titles must be unique within a space.

**Solutions**:
1. **Use a service-prefixed H1 title** (preferred): Change `# Architecture` to `# <Service Name> Architecture` (e.g., `# Stocks Architecture`)
2. **Add a `<!-- Title: -->` HTML comment**: Add `<!-- Title: <Service Name> Architecture -->` before the H1 to override the Confluence page title without changing the rendered heading
3. **Both**: Use a prefixed H1 and a `<!-- Title: -->` comment for maximum safety

```markdown
# Safe - unique title
<!-- Title: Stocks Architecture -->
# Stocks Architecture

# Collision-prone - generic title
# Architecture
```

**Prevention**: The Architecture.md template in [templates.md](templates.md) already includes a service-prefixed title. Always follow the template when generating new documentation.

## Atlas Query Issues

### No Results from Atlas

**Symptom**: Atlas returns no information for a service.

**Solutions**:
1. Verify service name is correct
2. Try alternative names (with/without suffix)
3. Break down into smaller questions
4. Use session continuity for context

```
# Instead of:
"Tell me about the order service"

# Try:
"What services exist in the orders domain?"
"What is the purpose of order-service?"
```

### Incomplete Information

**Symptom**: Atlas response is missing expected details.

**Solutions**:
1. Ask follow-up questions with session ID
2. Be more specific in queries
3. Query for specific aspects separately

```
# General query might miss details
"Describe the order service"

# Specific queries get better results
"What gRPC endpoints does order-service expose?"
"What databases does order-service use?"
"What are the main domain models in order-service?"
```

## Documentation Structure Issues

### Missing Service Directory

**Symptom**: `bitso-services/` doesn't exist or is empty.

**Solutions**:
1. Check if services are in a different location
2. Look for `bitso-jobs/` for batch jobs
3. Check repository structure documentation

### Conflicting Existing Docs

**Symptom**: Existing documentation conflicts with code.

**Solutions**:
1. Prioritize code over existing documentation
2. Update documentation to match code
3. Flag major discrepancies for human review

## Template Issues

### Frontmatter Validation Errors

**Symptom**: Frontmatter rejected by documentation system.

**Solutions**:
1. Check YAML syntax
2. Ensure required fields are present
3. Use consistent date format (YYYY-MM-DD)

```yaml
# ✅ Correct
---
service: order-service
updated: 2025-01-15
---

# ❌ Wrong
---
service: "order-service"  # Quotes unnecessary
updated: Jan 15, 2025     # Wrong format
---
```

### Mermaid Diagram Not Rendering

**Symptom**: Diagrams show as code blocks.

**Solutions**:
1. Check for proper fencing
2. Verify Mermaid syntax
3. Test in Mermaid live editor

```markdown
# ✅ Correct
\`\`\`mermaid
graph TB
    A --> B
\`\`\`

# ❌ Wrong - missing mermaid identifier
\`\`\`
graph TB
    A --> B
\`\`\`
```

## Idempotency Issues

### Unnecessary Updates

**Symptom**: Documentation changes on every generation.

**Causes**:
- Timestamp updates without content changes
- Formatting differences
- Regenerating accurate content

**Solutions**:
1. Only update `updated:` when content actually changes
2. Preserve existing formatting
3. Compare content before writing

### Missed Updates

**Symptom**: Documentation doesn't reflect code changes.

**Solutions**:
1. Check comparison logic
2. Ensure all code areas are checked
3. Add specific checks for proto files, config, etc.

## Multi-Service Repositories

### Service Confusion

**Symptom**: Documentation mixes content from multiple services.

**Solutions**:
1. Query specifically: "In the order-service, how does X work?"
2. Create clear service boundaries in docs
3. Cross-reference related services

### Shared Libraries

**Symptom**: Unclear how to document `bitso-libs/`.

**Solution**: Document from service perspective:
- Libraries are implementation details
- Focus on how services use libraries
- Reference library locations in service docs

```markdown
## Implementation Details

### Service Layer
- **Location**: `bitso-libs/orders/service/`
- **Key Classes**: `OrderServiceImpl`
```

## Performance Issues

### Slow Generation

**Symptom**: Documentation generation takes too long.

**Solutions**:
1. Generate incrementally (only changed services)
2. Use session continuity to reduce queries
3. Cache Atlas responses during generation

### Large Documentation Sets

**Symptom**: Too much documentation to maintain.

**Solutions**:
1. Focus on high-value documentation
2. Remove redundant content
3. Link to shared platform docs instead of duplicating

## Related

- [Documentation Workflow](documentation-workflow.md) - Full process
- [Atlas MCP Usage](atlas-mcp-usage.md) - Query strategies
- [Templates](templates.md) - RFC-37 templates
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-generation-rfc-37/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

