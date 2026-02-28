---
title: Documentation Templates
description: RFC-37 compliant templates for service documentation
---

# Documentation Templates

RFC-37 compliant templates for AI-generated service documentation.

## Contents

- [Overview.md Template](#overviewmd-template)
- [Architecture.md Template](#architecturemd-template)
- [Concept Template](#concept-template)
- [Feature Template](#feature-template)
- [Repository README Template](#repository-readme-template)
- [Related](#related)

---
> **Note**: For authoritative RFC-37 structure, validation rules, and Confluence configuration, see the [doc-validation-rfc-37](.claude/skills/doc-validation-rfc-37/SKILL.md) skill. This reference provides simplified templates optimized for AI content generation.

## Overview.md Template

```markdown
---
service: <service-name>
repository: <github-repo-name>
service_path: bitso-services/<service-name>
domain: <business-domain>
related_services: [service1, service2]
primary_language: Java
framework: Spring Boot
port: <grpc-port>
updated: <YYYY-MM-DD>
---

# Service Overview: <Service Name>

## Business Purpose

<2-3 sentences describing business problem and why service exists>

## Core Responsibilities

- **Responsibility 1**: <What it handles>
- **Responsibility 2**: <What it handles>

## Domain Concepts

- **Concept 1**: <Clear definition>
- **Concept 2**: <Clear definition>

## Service Boundaries

### In Scope
<What this service owns and handles>

### Out of Scope
<What other services handle>

## Key Use Cases

1. **Use Case 1**: <Description>
2. **Use Case 2**: <Description>

## Related Services

- **Service 1**: <How it relates>
- **Service 2**: <How it relates>
```

## Architecture.md Template

> **Important — Confluence title collisions**: The H1 title becomes the Confluence page title when `title-from-h1 = true`. Generic titles like `# Architecture` will collide if another page with that name exists in the space. Always use a service-prefixed title (e.g., `# Stocks Architecture`). As an extra safeguard you can add a `<!-- Title: <Service Name> Architecture -->` comment.

```markdown
---
service: <service-name>
service_path: bitso-services/<service-name>
section: architecture
tags: [architecture, dependencies]
updated: <YYYY-MM-DD>
---

<!-- Title: <Service Name> Architecture -->
# <Service Name> Architecture

## Service Architecture Overview

\`\`\`mermaid
graph TB
    Client[Clients] --> Service[<Service Name>]
    Service --> Dep1[Dependency 1]
    Service --> DB[(PostgreSQL)]
\`\`\`

## External Dependencies

### gRPC Services

#### <External Service Name>
- **Purpose**: <Why we depend on it>
- **Key RPCs Used**: <List>
- **Resilience**: Timeout=<X>s, Retries=<Y>

### Databases

#### PostgreSQL
- **Database**: `<db-name>`
- **Key Tables**: <List>
- **Access**: JOOQ with HikariCP

## Request Processing Flow

\`\`\`mermaid
sequenceDiagram
    participant Client
    participant Handler as gRPC Handler
    participant Service as Business Service
    participant DB as Database

    Client->>Handler: gRPC Request
    Handler->>Service: Call Business Logic
    Service->>DB: Query Data
    DB-->>Service: Return Data
    Service-->>Handler: Return Domain Model
    Handler-->>Client: gRPC Response
\`\`\`
```

## Concept Template

```markdown
---
concept: <concept-name>
service: <service-name>
tags: [concept, domain]
updated: <YYYY-MM-DD>
---

# Concept: <Concept Name>

## Overview

<What this concept represents>

## Domain Definition

<Business terms explanation>

## Components Involved

### Component 1: <Name>
- **Module**: `bitso-libs/<module>/`
- **Purpose**: <What it does>

## Business Rules

1. **Rule 1**: <Description>
2. **Rule 2**: <Description>

## Related Concepts

- **Concept 1**: <How they relate>
```

## Feature Template

```markdown
---
feature: <feature-name>
service: <service-name>
related_concepts: [concept1, concept2]
tags: [feature]
updated: <YYYY-MM-DD>
---

# Feature: <Feature Name>

## Overview

<Business/user perspective description>

## End-to-End Flow

\`\`\`mermaid
sequenceDiagram
    participant Client
    participant Service
    participant DB

    Client->>Service: Request
    Service->>DB: Query
    DB-->>Service: Data
    Service-->>Client: Result
\`\`\`

## gRPC API

**Proto File**: `bitso-libs/<module>-proto/.../v1.proto`

### RPC: `<MethodName>`

**Purpose**: <What this endpoint does>

**Request**: `<MethodName>Request`
\`\`\`protobuf
message <MethodName>Request {
  string field1 = 1;
}
\`\`\`

**Error Codes**:
- `ERROR_CODE_1` - <Description>

## Business Rules

1. **Rule 1**: <Description>
2. **Rule 2**: <Description>
```

## Repository README Template

```markdown
# AI Documentation

Documentation for services in this repository.

## Services

| Service | Description | Port |
|---------|-------------|------|
| `<service-1>` | <Brief description> | <port> |

## Structure

\`\`\`
docs/
├── README.md
└── <service>/
    ├── overview.md
    ├── architecture.md
    ├── concepts/
    └── features/
\`\`\`

## Quick Links

- [Service 1 Overview](.claude/skills/doc-generation-rfc-37/references/<service-1>/overview.md)
```

## Related

- [Documentation Workflow](documentation-workflow.md) - Full generation process
- [Atlas MCP Usage](atlas-mcp-usage.md) - Querying Atlas
- [RFC-37 Documentation Skill](.claude/skills/doc-validation-rfc-37/SKILL.md) - RFC-37 standards and validation
- [RFC-37 Validation Rules](.claude/skills/doc-validation-rfc-37/references/validation-rules.md) - Linter rules
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-generation-rfc-37/references/templates.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

