# Validation Rules Reference

Complete reference for all rules implemented by the bitso-documentation-linter. For the most current documentation, see the [official validation rules](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/bitso-documentation-linter/concepts/validation-rules.md).

## Contents

- [Rule Summary](#rule-summary)
- [Severity Levels](#severity-levels)
- [FORMAT_CODE_LANG_MISSING](#format_code_lang_missing)
- [META_PAGE_TITLE_MISSING](#meta_page_title_missing)
- [DIR_STRUCTURE_MISMATCH](#dir_structure_mismatch)
- [API_DOC_NAMING_MULTI_SERVICE_INVALID](#api_doc_naming_multi_service_invalid)
- [DIR_API_SUBDIR_MISSING](#dir_api_subdir_missing)
- [DIR_SERVICE_SETUP_INCOMPLETE](#dir_service_setup_incomplete)
- [MIN_DOC_LOCAL_EXEC_MISSING](#min_doc_local_exec_missing)
- [META_CONFLUENCE_CONFIG_INCOMPLETE](#meta_confluence_config_incomplete)
- [PAGE_NAME_DUPLICATE](#page_name_duplicate)
- [PAGE_ALREADY_EXISTS_IN_CONFLUENCE](#page_already_exists_in_confluence)
- [Configuration](#configuration)
- [Architecture: One Checker Per Rule](#architecture-one-checker-per-rule)
- [See Also](#see-also)

---
## Rule Summary

| Rule ID | Default | Category | Description |
|---------|---------|----------|-------------|
| `FORMAT_CODE_LANG_MISSING` | WARNING | Format | Code blocks must specify language |
| `META_PAGE_TITLE_MISSING` | ERROR | Metadata | Documents must have H1 titles |
| `DIR_STRUCTURE_MISMATCH` | ERROR | Structure | Must follow RFC-37 directory structure |
| `API_DOC_NAMING_MULTI_SERVICE_INVALID` | ERROR | Structure | API files must match service names |
| `DIR_API_SUBDIR_MISSING` | WARNING | Structure | API subdirs should exist |
| `DIR_SERVICE_SETUP_INCOMPLETE` | ERROR | Structure | Services need concepts/ and getting-started/ |
| `MIN_DOC_LOCAL_EXEC_MISSING` | ERROR | Content | Local execution docs required |
| `META_CONFLUENCE_CONFIG_INCOMPLETE` | ERROR | Metadata | Confluence config required |
| `PAGE_NAME_DUPLICATE` | ERROR | Metadata | Page names must be unique per Space |
| `PAGE_ALREADY_EXISTS_IN_CONFLUENCE` | ERROR | Integration | No conflicts with existing pages |

## Severity Levels

- **ERROR**: Fails the linting process, blocks CI/CD (exit code 1)
- **WARNING**: Should be addressed, doesn't fail the process
- **INFO**: Best practice recommendations

All severities can be customized per project through configuration.

---

## FORMAT_CODE_LANG_MISSING

**Severity**: WARNING
**Category**: Format Validation

Code blocks must specify a programming language for syntax highlighting.

### Why It Matters

- **Readability**: Syntax highlighting makes code easier to read
- **Professional Appearance**: Properly highlighted code looks more polished
- **Tool Integration**: Many documentation platforms rely on language specification

### Bad

````markdown
```
$ ./gradlew clean build
```
````

### Good

````markdown
```bash
$ ./gradlew clean build
```
````

### Common Language Identifiers

- `bash`, `shell`, `sh` - Shell commands
- `java`, `kotlin` - JVM languages
- `javascript`, `typescript`, `js`, `ts` - JS/TS
- `python`, `py` - Python
- `go` - Go
- `yaml`, `yml`, `json` - Configuration
- `sql` - SQL queries
- `proto` - Protocol Buffers
- `markdown`, `md` - Markdown
- `xml`, `html`, `css` - Web markup/styles
- `dockerfile` - Docker files

### Configuration

```yaml
rules:
  FORMAT_CODE_LANG_MISSING:
    enabled: true
    severity: "warning"  # Can be changed to "error" or "info"
```

---

## META_PAGE_TITLE_MISSING

**Severity**: ERROR
**Category**: Metadata Validation

Every document must have a title from an H1 heading or metadata comment.

### Why It Matters

- **Navigation**: Titles are used to generate navigation menus
- **Confluence Integration**: Titles become Confluence page names
- **Search**: Titles improve searchability and content discovery

### Bad

```markdown
This document has no title.

## Some Section
```

### Good

**Option 1: H1 Heading**

```markdown
# User Authentication API

This document describes the authentication endpoints.
```

**Option 2: Metadata Title**

```markdown
<!-- Title: User Authentication API -->

## Overview
```

**Option 3: H1 with Confluence Metadata**

```markdown
<!-- Space: ENG -->
<!-- Parent: API Documentation -->

# User Authentication API

This document describes the authentication endpoints.
```

### Best Practices

1. **Place H1 at the top**: Title should be the first substantial content
2. **Descriptive titles**: Use clear, descriptive titles
3. **Consistent naming**: Follow consistent naming patterns
4. **Unique titles**: Ensure titles are unique to avoid Confluence conflicts

---

## DIR_STRUCTURE_MISMATCH

**Severity**: ERROR
**Category**: Structure Validation

Repository must follow RFC-37 directory structure.

### Required Directories

- `docs/decisions/` - Architecture Decision Records
- `docs/runbooks/` - Operational procedures
- `docs/how-tos/` - Step-by-step guides

### Expected Structure

```
docs/
├── decisions/
├── runbooks/
├── how-tos/
└── <service-name>/
    ├── concepts/
    └── getting-started/
```

### Why It Matters

- **Consistency**: Standard structure across all Bitso repositories
- **Discoverability**: Developers know where to find documentation types
- **Tool Integration**: Automated tools can rely on predictable structure

---

## API_DOC_NAMING_MULTI_SERVICE_INVALID

**Severity**: ERROR
**Category**: Structure Validation

In multi-service repositories, API documentation files must be named after the service.

### Bad (multi-service repo)

```
docs/api/rest/
├── api.md           ❌ Generic name
├── endpoints.md     ❌ Not a service name
```

### Good (multi-service repo)

```
docs/api/rest/
├── user-service.md     ✅ Matches service
├── payment-service.md  ✅ Matches service
```

**Note**: Single-service repos are exempt from this rule.

---

## DIR_API_SUBDIR_MISSING

**Severity**: WARNING
**Category**: Structure Validation

If `docs/api/` exists, it should have subdirectories for API types.

### Expected

```
docs/api/
├── async/     # Event-driven APIs
├── grpc/      # gRPC APIs
└── rest/      # REST APIs
```

**Note**: Warning severity because not all services expose all API types (CLI apps, cron jobs, async-only services).

---

## DIR_SERVICE_SETUP_INCOMPLETE

**Severity**: ERROR
**Category**: Structure Validation

Each service directory must have required subdirectories.

### Required

```
docs/<service-name>/
├── concepts/        # Architecture, design, dependencies
└── getting-started/ # Quick start tutorials
```

### Why It Matters

- **Onboarding**: New developers need getting-started guides
- **Understanding**: Concepts documentation explains service design
- **Completeness**: Ensures minimum documentation standards per service

---

## MIN_DOC_LOCAL_EXEC_MISSING

**Severity**: ERROR
**Category**: Content Validation

Repository must have local execution documentation.

### Valid Locations

1. **Global**: `docs/how-tos/local-execution.md`
2. **Per-service**: `docs/how-tos/<service>-local-execution.md`

### Required Content

- Prerequisites
- Environment setup
- Build instructions
- Run instructions
- Verification steps
- Troubleshooting

### Why It Matters

- **Developer Onboarding**: New team members need clear setup instructions
- **Productivity**: Reduces time spent figuring out how to run projects
- **Knowledge Sharing**: Prevents setup knowledge from being siloed

---

## META_CONFLUENCE_CONFIG_INCOMPLETE

**Severity**: ERROR
**Category**: Metadata Validation

Repository must have Confluence configuration for documentation mirroring.

### Option 1: mark.toml (Recommended)

```toml
# docs/mark.toml
space = "MM"
parents = "Fleet/Squad/Engineering docs/service"
```

### Option 2: Per-file metadata

```markdown
<!-- Space: MM -->
<!-- Parent: Fleet -->
<!-- Parent: Squad -->
<!-- Parent: Engineering docs -->

# My Document
```

### Option 3: Hybrid (mark.toml provides space)

```toml
# docs/mark.toml
space = "MM"
```

```markdown
<!-- Parent: Custom Parent -->

# My Document
```

### Invalid Configuration

A `mark.toml` with only `parents` (no `space`) is **architecturally invalid** because parent hierarchies are specific to a Confluence space.

### Related Violations

- `META_CONFLUENCE_SPACE_MISSING`: Per-file violation for missing Space
- `META_CONFLUENCE_PARENT_MISSING`: Per-file violation for missing Parent

---

## PAGE_NAME_DUPLICATE

**Severity**: ERROR
**Category**: Metadata Validation

Page names must be unique within each Confluence Space.

### Bad

```
docs/
├── service-a/concepts/
│   └── overview.md     # Title: "Overview"
└── service-b/concepts/
    └── overview.md     # Title: "Overview" ❌ DUPLICATE
```

### Good

```
docs/
├── service-a/concepts/
│   └── overview.md     # Title: "Service A Overview"
└── service-b/concepts/
    └── overview.md     # Title: "Service B Overview"
```

### Naming Strategies

- **Service prefix**: "User Service Architecture"
- **Functional**: "API Authentication Guide"
- **Hierarchical**: "User Service: Getting Started"

---

## PAGE_ALREADY_EXISTS_IN_CONFLUENCE

**Severity**: ERROR
**Category**: Confluence Integration

Page names must not conflict with existing Confluence pages in the same Space at a different location.

### How It Works

1. Checks if a page with the same title exists in the Space
2. Retrieves the parent hierarchy from Confluence (ancestors)
3. Compares with the expected hierarchy from your documentation
4. **Only reports a violation if the hierarchies differ**

### When It Triggers

- Page with same title exists in the Space
- But under a different parent hierarchy

### Example Violation

```text
ERROR: docs/my-feature.md:1 - PAGE_ALREADY_EXISTS_IN_CONFLUENCE
  Page 'My Feature' already exists in Confluence Space 'MM' at a DIFFERENT location:
  https://bitsomx.atlassian.net/wiki/spaces/MM/pages/123456/My+Feature.
  Your documentation expects hierarchy 'API Reference', but the existing page has
  a different parent hierarchy. This will cause a conflict when publishing.
```

### How to Fix

1. Choose a different, more specific page title
2. Verify the existing page isn't a duplicate of your content
3. Update your hierarchy to match the existing page
4. Coordinate with the existing page owner

### Configuration Required

This rule requires Confluence API configuration:

```bash
export CONFLUENCE_BASE_URL="https://bitsomx.atlassian.net/wiki"
export ATLASSIAN_USERNAME="your-email@bitso.com"
export ATLASSIAN_API_TOKEN="your-token"
export CONFLUENCE_ENABLED="true"
```

Or in `.doclinterrc.yml`:

```yaml
confluenceApi:
  baseUrl: "https://bitsomx.atlassian.net/wiki"
  email: "your-email@bitso.com"
  apiToken: "your-token"
  enabled: true
```

---

## Configuration

All rules can be customized in `.doclinterrc.yml`:

### Change Severity

```yaml
rules:
  FORMAT_CODE_LANG_MISSING:
    severity: "error"  # Promote from warning to error
  META_PAGE_TITLE_MISSING:
    severity: "warning"  # Demote from error to warning
```

### Disable Rule

```yaml
rules:
  DIR_API_SUBDIR_MISSING:
    enabled: false  # Completely disable
```

### Ignore Specific Files

```yaml
ignoreViolations:
  - ruleId: "FORMAT_CODE_LANG_MISSING"
    files:
      - "docs/legacy/*.md"
      - "*.temp.md"
  - ruleId: "META_PAGE_TITLE_MISSING"
    files:
      - "docs/fragments/*.md"
```

## Architecture: One Checker Per Rule

The linter follows a **one-to-one relationship** between checkers and rules:

**Document-Level Checkers** (`Checker` interface):
- `CodeBlockLangChecker`
- `PageTitleChecker`

**Repository-Level Checkers** (`PostProcessChecker` interface):
- `DirectoryStructureChecker`
- `APIFileNamingChecker`
- `APISubdirectoryChecker`
- `ServiceDirectoryChecker`
- `LocalExecutionChecker`
- `PageNameDuplicateChecker`
- `ConfluenceMetadataChecker`
- `ConfluencePageExistsChecker`

## See Also

- [Official Validation Rules Documentation](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/bitso-documentation-linter/concepts/validation-rules.md)
- [Configuration Guide](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/bitso-documentation-linter/concepts/configuration.md)
- [CLI Options](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/bitso-documentation-linter/concepts/cli-options.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/references/validation-rules.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

