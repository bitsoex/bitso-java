---
title: Documentation Workflow
description: Step-by-step workflow for generating AI-friendly documentation
---

# Documentation Workflow

Step-by-step workflow for generating AI-friendly documentation following RFC-37.

## Contents

- [Pre-Generation Checks](#pre-generation-checks)
- [Generation Steps](#generation-steps)
- [Quality Checklist](#quality-checklist)
- [Idempotency Guidelines](#idempotency-guidelines)
- [Related](#related)

---
## Pre-Generation Checks

### 1. Check Existing Documentation

```bash
# List existing docs
ls -la docs/

# Check for existing service docs
ls -la docs/<service-name>/
```

### 2. Identify All Services

```bash
# Find all deployable services
ls -d bitso-services/*/

# Check each service's build.gradle
cat bitso-services/<service>/build.gradle
```

### 3. Compare with Codebase

For each existing document:
1. Read the document
2. Check if underlying code changed
3. Only update if code changed

## Generation Steps

### Step 1: Create Directory Structure

```bash
mkdir -p docs/<service-name>/concepts
mkdir -p docs/<service-name>/features
```

### Step 2: Generate Overview.md

Query Atlas for business context:

```
"What is the business purpose of <service-name>?"
"What are the core responsibilities of <service-name>?"
"What domain concepts does <service-name> use?"
```

Apply template from `templates.md` with gathered information.

### Step 3: Generate Architecture.md

Query for architecture details:

```
"What is the architecture of <service-name>?"
"What services does <service-name> depend on?"
"What databases does <service-name> use?"
```

Include Mermaid diagrams for visual representation.

> **Confluence collision avoidance**: Use a service-prefixed H1 title (e.g., `# Stocks Architecture` instead of `# Architecture`). Generic titles collide when another page with the same name exists in the Confluence space, causing `mark` sync to fail with `unexpected ancestry tree`. See the Architecture.md template in [templates.md](templates.md) and the [Troubleshooting guide](troubleshooting.md#confluence-title-collision) for details.

### Step 4: Document Concepts

For each major domain concept:

```
"Explain the <concept> in <service-name>"
"What components handle <concept>?"
"What are the business rules for <concept>?"
```

Create separate `.md` files in `concepts/` folder.

### Step 5: Document Features

For each feature:

```
"How does <feature> work in <service-name>?"
"What are the gRPC endpoints for <feature>?"
"What are the error codes for <feature>?"
```

Create separate `.md` files in `features/` folder.

### Step 6: Generate Repository README

Create `docs/README.md` listing all services and structure.

## Quality Checklist

Before completing:

- [ ] Checked existing documentation for changes needed
- [ ] Only updated files where actual changes occurred
- [ ] One folder per service in `bitso-services/`
- [ ] Each service has `overview.md` and `architecture.md`
- [ ] Concepts documented in `concepts/` folder
- [ ] Features documented in `features/` folder
- [ ] All documents have proper frontmatter
- [ ] Code references point to actual files
- [ ] Diagrams included where helpful
- [ ] Writing is clear and focused on business logic
- [ ] No platform/common documentation included

## Idempotency Guidelines

### Decision Process

```plaintext
Question: Should I update orders/overview.md?

Check:
- ✓ Read existing orders/overview.md
- ✓ Check if new features added → NO
- ✓ Check if core responsibilities changed → NO
- ✓ Check if dependencies changed → NO
- ✓ Is current doc accurate? → YES

Decision: DO NOT UPDATE - documentation is accurate
```

### Partial Updates

```plaintext
Question: Should I update feature documentation?

Check:
- ✓ Read existing feature docs
- ✓ Check proto files for changes → YES, new RPC added
- ✓ Check if existing RPCs modified → NO

Decision: UPDATE - add documentation for new RPC only
          Keep existing RPC documentation unchanged
```

## Related

- [Atlas MCP Usage](atlas-mcp-usage.md) - Querying Atlas
- [Templates](templates.md) - RFC-37 document templates
- [Troubleshooting](troubleshooting.md) - Common issues
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-generation-rfc-37/references/documentation-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

