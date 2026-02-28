---
title: Bitso Document Templates
description: Links to Bitso document templates and format guides for common document types
---

# Bitso Document Templates

Reference for Bitso-specific document formats and templates. Use this when the user needs a document that follows Bitso conventions.

## Contents

- [RFC Documents](#rfc-documents)
- [Architecture Decision Records](#architecture-decision-records)
- [Operational Documentation](#operational-documentation)
- [Documentation Structure](#documentation-structure)

---
## RFC Documents

RFCs at Bitso follow the RFC-37 format. Use the `doc-generation-rfc-37` skill for full guidance.

### Key sections

1. **Summary** - One-paragraph overview
2. **Motivation** - Why this change is needed
3. **Detailed Design** - Technical approach
4. **Alternatives Considered** - What else was evaluated and why it was rejected
5. **Migration Strategy** - How to transition (if applicable)
6. **Rollback Plan** - How to revert if issues arise

### Location

- RFCs live in the `docs/decisions/` directory of the relevant repository
- Number RFCs sequentially within the repository

### Validation

Run the `doc-validation-rfc-37` skill to verify compliance with RFC-37 structure.

## Architecture Decision Records

ADRs document significant architectural decisions.

### Key sections

1. **Title** - Short descriptive title with ADR number
2. **Status** - Proposed, Accepted, Deprecated, Superseded
3. **Context** - What is motivating this decision
4. **Decision** - What was decided
5. **Consequences** - What the positive, negative, and neutral results are

### Location

- ADRs live in `docs/decisions/` within the relevant repository
- Use format: `NNNN-title-of-decision.md`

### When to use ADR vs. RFC

| Use ADR | Use RFC |
|---------|---------|
| Single architectural choice | Complex multi-system changes |
| Team-level decision | Cross-team or org-wide changes |
| Quick to document (<1 page) | Needs detailed design (>1 page) |

## Operational Documentation

### Runbooks

Step-by-step guides for operational procedures:
- Location: `docs/runbooks/`
- Include: prerequisites, step-by-step commands, verification, rollback
- Test all commands before publishing

### How-To Guides

Task-oriented guides for common procedures:
- Location: `docs/how-tos/`
- Focus on a single task
- Include prerequisites and expected outcomes

### Concepts

Explanations of key system concepts:
- Location: `docs/concepts/`
- Explain the "why" and "what", not the "how"
- Include Mermaid diagrams for architecture

## Documentation Structure

Bitso repositories follow this standard `docs/` structure:

```
docs/
├── api/           # API documentation
├── concepts/      # System concepts and architecture
├── decisions/     # RFCs and ADRs
├── features/      # Feature documentation
├── getting-started/ # Onboarding guides
├── how-tos/       # Task-oriented guides
└── runbooks/      # Operational procedures
```

### Confluence Mirroring

Some repositories mirror documentation to Confluence. If the target repository uses Confluence mirroring:

1. Include `mark.toml` configuration for Confluence sync
2. Add Confluence metadata headers to pages
3. See the `doc-validation-rfc-37` skill for Confluence metadata requirements

### Mermaid Diagrams

Use Mermaid diagrams for:
- C4 architecture diagrams
- Sequence diagrams for API flows
- State diagrams for entity lifecycles
- Flowcharts for decision processes

See the `doc-generation-rfc-37` skill for Mermaid diagram guidelines.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-coauthoring/references/bitso-templates.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

