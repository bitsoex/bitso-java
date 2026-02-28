# RFC-37: Service Documentation Standardization

Summary of [RFC-37](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4485840987/RFC-37+Service+documentation+standardization) - Bitso's service documentation standardization.

## Contents

- [Problem Statement](#problem-statement)
- [Solution](#solution)
- [Standard Directory Structure](#standard-directory-structure)
- [Required Documentation](#required-documentation)
- [Confluence Configuration](#confluence-configuration)
- [Validation with bitso-documentation-linter](#validation-with-bitso-documentation-linter)
- [Writing Guidelines](#writing-guidelines)
- [CI/CD Integration](#cicd-integration)
- [References](#references)

---
## Problem Statement

Common documentation problems at Bitso:

1. **Hard to find**: Documentation scattered across multiple locations
2. **Outdated**: Confluence pages not maintained
3. **Incomplete**: Missing local execution instructions, architecture docs
4. **Siloed knowledge**: Information in Slack, engineers' heads, or tribal knowledge
5. **No standard structure**: Each team organizes docs differently
6. **Duplicated content**: Same information in multiple places

## Solution

**Store documentation as Markdown in the service repository**, organized in a standard directory structure, and automatically mirror to Confluence.

### Key Principles

1. **Docs as Code**: Documentation lives with the code it describes
2. **Standard Structure**: All repos follow the same directory organization
3. **Automated Sync**: Markdown files mirror to Confluence automatically
4. **Required Minimums**: Every service must have certain documentation

## Standard Directory Structure

```
docs/
├── api/                    # API documentation
│   ├── async/              # Event-driven APIs (Kafka, SQS, etc.)
│   ├── grpc/               # gRPC APIs
│   └── rest/               # REST APIs
├── decisions/              # Architecture Decision Records (ADRs)
├── how-tos/                # Step-by-step guides
│   └── local-execution.md  # REQUIRED: How to run locally
├── runbooks/               # Operational procedures
└── <service-name>/         # Service-specific docs
    ├── concepts/           # Architecture, design, C4 diagrams
    ├── getting-started/    # Quick start tutorials
    └── <feature>/          # Feature-specific documentation
```

## Required Documentation

Every service MUST have at minimum:

### 1. Local Execution Documentation

**Location**: `docs/how-tos/local-execution.md`

**Must include**:
- Prerequisites and system requirements
- Environment setup and configuration
- How to build the application
- How to run the service locally
- How to verify the setup works
- How to test the happy path of each feature
- Troubleshooting common issues

### 2. Service Concepts

**Location**: `docs/<service>/concepts/`

**Must include**:
- Service architecture (C4 diagrams recommended)
- Key components and their relationships
- Dependencies and integrations
- Data flows

## Confluence Configuration

Documentation mirrors to Confluence under the service's engineering docs section.

### Using mark.toml (Recommended)

```toml
base-url = "https://bitsomx.atlassian.net/wiki"
title-from-h1 = true
drop-h1 = true
space = "MM"
parents = "Fleet Money Movement/Gearbox Squad/Engineering docs/my-service"
mermaid-provider = "mermaid-go"
mermaid-scale = 1.0
edit-lock = false
changes-only = true
files = "**/*.md"
```

### Understanding the `parents` Field

- Defines the Confluence page hierarchy path
- Format: `"Parent Level 1/Parent Level 2/.../Your Service Home"`
- Path is relative to the Confluence space root
- All pages from `docs/` publish under this hierarchy

**Examples**:
- Backend services: `"Fleet Money Movement/Gearbox Squad/Engineering docs/reconciliation-engine"`
- Frontend apps: `"Fleet Money Movement/Frontend Squad/Engineering docs/trading-app"`
- Shared libraries: `"Fleet Money Movement/Shared/Libraries/common-utils"`

### Using Per-File Metadata

Add metadata comments to each file:

```markdown
<!-- Space: MM -->
<!-- Parent: Fleet Money Movement -->
<!-- Parent: Gearbox Squad -->
<!-- Parent: Engineering docs -->
<!-- Parent: my-service -->
<!-- Parent: How-to Guides -->

# My Document Title
```

## Validation with bitso-documentation-linter

The [bitso-documentation-linter](https://github.com/bitsoex/bitso-documentation-linter) validates documentation against RFC-37:

```bash
# Install
brew tap bitsoex/homebrew-bitso
brew install bitso-documentation-linter

# Validate
doclinter --repo-path . --verbose

# Preview Confluence tree
doclinter tree --repo-path .
```

### Key Validation Rules

| Rule | What It Checks |
|------|----------------|
| `DIR_STRUCTURE_MISMATCH` | Required directories exist |
| `MIN_DOC_LOCAL_EXEC_MISSING` | Local execution docs present |
| `DIR_SERVICE_SETUP_INCOMPLETE` | Service has concepts/ and getting-started/ |
| `META_CONFLUENCE_CONFIG_INCOMPLETE` | Confluence config is complete |
| `PAGE_NAME_DUPLICATE` | No duplicate page titles |
| `PAGE_ALREADY_EXISTS_IN_CONFLUENCE` | No conflicts with existing Confluence pages |

## Writing Guidelines

### Markdown Compatibility

1. **Unique page names**: Prefix with service/context
   - ❌ `# Overview`
   - ✅ `# User Service Overview`

2. **Single-line paragraphs**: Don't break across lines (Confluence requirement)

3. **Diagrams as code**: Use Mermaid when possible

4. **Code blocks with language**: Always specify language for syntax highlighting

### Best Practices

1. **Keep docs close to code**: Update docs when changing code
2. **Review docs in PRs**: Include docs in code review
3. **Link between docs**: Use relative links
4. **Version-specific content**: Document for current version, archive old docs

## CI/CD Integration

Add documentation linting to your pipeline:

```yaml
# GitHub Actions example
- name: Lint Documentation
  run: |
    doclinter --repo-path . --format=json
```

For Confluence API validation:

```yaml
- name: Lint Documentation with Confluence Check
  env:
    CONFLUENCE_BASE_URL: ${{ secrets.CONFLUENCE_BASE_URL }}
    ATLASSIAN_USERNAME: ${{ secrets.ATLASSIAN_USERNAME }}
    ATLASSIAN_API_TOKEN: ${{ secrets.ATLASSIAN_API_TOKEN }}
    CONFLUENCE_ENABLED: "true"
  run: |
    doclinter --repo-path .
```

## References

- [RFC-37 Full Document](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4485840987/RFC-37+Service+documentation+standardization)
- [Software Application Documentation At Bitso](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4938596720/Software+Application+Documentation+At+Bitso)
- [bitso-documentation-linter GitHub](https://github.com/bitsoex/bitso-documentation-linter)
- [Linter Local Execution Guide](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/how-tos/local-execution.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/references/rfc-37.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

