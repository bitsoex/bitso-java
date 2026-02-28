# Confluence Metadata and Integration

Complete guide for configuring Confluence integration with the bitso-documentation-linter.

## Contents

- [Overview](#overview)
- [Configuration Options](#configuration-options)
- [Confluence API Validation](#confluence-api-validation)
- [Previewing the Tree Structure](#previewing-the-tree-structure)
- [Validation Rules](#validation-rules)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [References](#references)

---
## Overview

Documentation is mirrored to Confluence using [mark](https://github.com/kovetskiy/mark), which reads configuration from `mark.toml` or per-file metadata comments. The bitso-documentation-linter validates this configuration before publishing.

## Configuration Options

### Option 1: mark.toml (Recommended)

Create `docs/mark.toml` at the root of your docs folder:

```toml
base-url = "https://bitsomx.atlassian.net/wiki"
title-from-h1 = true
drop-h1 = true
space = "MM"
parents = "Fleet Money Movement/Gearbox Squad/Engineering docs/my-service"
mermaid-provider = "mermaid-go"
mermaid-scale = 1.0
edit-lock = false
log-level = "debug"
changes-only = true
files = "**/*.md"
```

#### Configuration Fields

| Field | Description | Required | Example |
|-------|-------------|----------|---------|
| `base-url` | Confluence base URL | Yes | `"https://bitsomx.atlassian.net/wiki"` |
| `space` | Confluence space key | Yes | `"MM"` |
| `parents` | Page hierarchy path | Yes | `"Fleet/Squad/Engineering docs/service"` |
| `title-from-h1` | Use H1 heading as page title | Recommended | `true` |
| `drop-h1` | Remove H1 from body (since it's the title) | Recommended | `true` |
| `mermaid-provider` | Mermaid rendering engine | Optional | `"mermaid-go"` |
| `mermaid-scale` | Mermaid diagram scale | Optional | `1.0` |
| `edit-lock` | Lock pages for editing | Optional | `false` |
| `log-level` | Logging verbosity | Optional | `"debug"` |
| `changes-only` | Only publish changed files | Optional | `true` |
| `files` | File glob pattern | Optional | `"**/*.md"` |

### Understanding the `parents` Field

The `parents` field defines the Confluence page hierarchy:

```
parents = "Fleet Money Movement/Gearbox Squad/Engineering docs/my-service"
```

This means:

```
Confluence Space (MM)
└── Fleet Money Movement
    └── Gearbox Squad
        └── Engineering docs
            └── my-service          ← Your docs publish here
                ├── How-to Guides
                │   └── Local Execution
                ├── Concepts
                │   └── Architecture
                └── ...
```

#### Common Patterns

**Backend Services**:

```toml
parents = "Fleet Money Movement/Gearbox Squad/Engineering docs/reconciliation-engine"
```

**Frontend Applications**:

```toml
parents = "Fleet Money Movement/Frontend Squad/Engineering docs/trading-app"
```

**Shared Libraries**:

```toml
parents = "Fleet Money Movement/Shared/Libraries/common-utils"
```

**Cross-Squad Services**:

```toml
parents = "Fleet Money Movement/Platform/Infrastructure/api-gateway"
```

### Option 2: Per-File Metadata

Add metadata comments to each markdown file:

```markdown
<!-- Space: MM -->
<!-- Parent: Fleet Money Movement -->
<!-- Parent: Gearbox Squad -->
<!-- Parent: Engineering docs -->
<!-- Parent: my-service -->
<!-- Parent: How-to Guides -->

# Local Execution Guide

This is the content...
```

#### Metadata Tags

| Tag | Description | Example |
|-----|-------------|---------|
| `<!-- Space: KEY -->` | Confluence space key | `<!-- Space: MM -->` |
| `<!-- Parent: Name -->` | Parent page (can repeat) | `<!-- Parent: Engineering docs -->` |
| `<!-- Title: Name -->` | Override page title | `<!-- Title: My Custom Title -->` |

#### Parent Order

Parents are ordered from **root to immediate parent**:

```markdown
<!-- Parent: Level 1 (closest to space root) -->
<!-- Parent: Level 2 -->
<!-- Parent: Level 3 -->
<!-- Parent: Level 4 (immediate parent of this page) -->

# This Page Title
```

### Option 3: Hybrid Approach

Use `mark.toml` for common settings, files for overrides:

```toml
# docs/mark.toml
base-url = "https://bitsomx.atlassian.net/wiki"
title-from-h1 = true
drop-h1 = true
space = "MM"
# No parents - files provide their own
```

```markdown
<!-- Parent: Fleet Money Movement -->
<!-- Parent: Special Project -->
<!-- Parent: Custom Location -->

# My Special Document

This page goes to a different location than other docs.
```

## Confluence API Validation

The linter can check if pages already exist in Confluence before publishing.

### Configuration

**Environment Variables (Recommended for CI/CD)**:

```bash
export CONFLUENCE_BASE_URL="https://bitsomx.atlassian.net/wiki"
export ATLASSIAN_USERNAME="your-email@bitso.com"
export ATLASSIAN_API_TOKEN="your-token"
export CONFLUENCE_ENABLED="true"
```

**Configuration File** (`.doclinterrc.yml`):

```yaml
confluenceApi:
  baseUrl: "https://bitsomx.atlassian.net/wiki"
  email: "your-email@bitso.com"
  apiToken: "your-token"
  enabled: true
  timeoutSeconds: 30
  checkTimeoutSeconds: 60
```

### Getting an API Token

1. Go to [Atlassian API Tokens](https://id.atlassian.com/manage-profile/security/api-tokens)
2. Click **Create API token**
3. Give it a label (e.g., "Documentation Linter")
4. Copy the token and store securely

**⚠️ Never commit API tokens to version control.**

### How It Works

The `PAGE_ALREADY_EXISTS_IN_CONFLUENCE` rule:

1. Checks if a page with the same title exists in the Space
2. Retrieves the existing page's parent hierarchy
3. Compares with your documentation's expected hierarchy
4. Reports violation only if hierarchies differ

**Examples**:

| Your Hierarchy | Existing Page Location | Result |
|----------------|------------------------|--------|
| `API Reference > Auth` | Under `API Reference` | ✅ No violation |
| `User Guide > Auth` | Under `API Reference` | ❌ Violation |
| Root level | Root level | ✅ No violation |

## Previewing the Tree Structure

Before publishing, preview how your docs will organize in Confluence:

```bash
doclinter tree --repo-path .
```

**ASCII Output**:

```
Fleet Money Movement [Space: MM]
└── Gearbox Squad [Space: MM]
    └── Engineering docs [Space: MM]
        └── my-service [Space: MM]
            ├── Concepts [Space: MM]
            │   ├── Architecture Overview [Space: MM]
            │   └── Data Model [Space: MM]
            └── How-to Guides [Space: MM]
                └── Local Execution [Space: MM]

Summary:
- Total nodes: 8
- Root nodes: 1
```

**JSON Output** (for automation):

```bash
doclinter tree --repo-path . --format=json
```

## Validation Rules

| Rule | Description |
|------|-------------|
| `META_CONFLUENCE_CONFIG_INCOMPLETE` | mark.toml must have both `space` and `parents`, or files must have metadata |
| `META_CONFLUENCE_SPACE_MISSING` | File lacks Space metadata (when no mark.toml space) |
| `META_CONFLUENCE_PARENT_MISSING` | File lacks Parent metadata (when no mark.toml parents) |
| `PAGE_NAME_DUPLICATE` | Two files would create pages with the same title |
| `PAGE_ALREADY_EXISTS_IN_CONFLUENCE` | Page exists in Confluence at different location |

### Invalid Configurations

**mark.toml with only parents (no space)**:

```toml
# ❌ INVALID - parents without space is architecturally invalid
parents = "Fleet/Squad/Engineering docs"
# Missing: space = "MM"
```

Parent hierarchies are specific to a Confluence space. You cannot define parents without defining which space they belong to.

**No configuration at all**:

```markdown
# ❌ INVALID - no way to determine Confluence destination
# My Document

Content here...
```

## Best Practices

### 1. Use mark.toml for Consistency

Most repos should use `mark.toml` for all docs to go to the same location.

### 2. Unique Page Titles

Avoid generic titles that could conflict:

- ❌ `# Overview`
- ❌ `# Getting Started`
- ✅ `# User Service Overview`
- ✅ `# Payment Service: Getting Started`

### 3. Validate Before Publishing

```bash
# Local validation
doclinter --repo-path . --verbose

# With Confluence API check
CONFLUENCE_ENABLED=true doclinter --repo-path .

# Preview tree
doclinter tree --repo-path .
```

### 4. CI/CD Integration

```yaml
# GitHub Actions
- name: Validate Documentation
  env:
    CONFLUENCE_BASE_URL: ${{ secrets.CONFLUENCE_BASE_URL }}
    ATLASSIAN_USERNAME: ${{ secrets.ATLASSIAN_USERNAME }}
    ATLASSIAN_API_TOKEN: ${{ secrets.ATLASSIAN_API_TOKEN }}
    CONFLUENCE_ENABLED: "true"
  run: |
    doclinter --repo-path . --format=json
```

## Troubleshooting

### "META_CONFLUENCE_CONFIG_INCOMPLETE"

**Cause**: Missing or incomplete Confluence configuration.

**Fix**:

1. Create `docs/mark.toml` with both `space` and `parents`
2. Or add `<!-- Space: -->` and `<!-- Parent: -->` to all files

### "PAGE_NAME_DUPLICATE"

**Cause**: Two files have the same H1 title.

**Fix**: Make titles more specific (add service name, context).

### "PAGE_ALREADY_EXISTS_IN_CONFLUENCE"

**Cause**: Page with same title exists in Confluence at different location.

**Fix**:
1. Choose different title
2. Or update your parents to match existing page location
3. Or coordinate with existing page owner

### API Connection Issues

```bash
# Check authentication
curl -u "email:token" \
  "https://bitsomx.atlassian.net/wiki/rest/api/content?spaceKey=MM&limit=1"
```

## References

- [mark (Confluence uploader)](https://github.com/kovetskiy/mark)
- bitso-documentation-linter (internal tool - contact Platform team for access)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/references/confluence-metadata.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

