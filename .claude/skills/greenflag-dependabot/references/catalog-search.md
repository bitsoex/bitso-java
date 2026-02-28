# Catalog Search

Using bitso-gradle-catalogs to understand dependency ownership and versions.

## Contents

- [Overview](#overview)
- [Directory Structure](#directory-structure)
- [Search Commands](#search-commands)
- [Common Queries](#common-queries)

---
## Overview

The `bitso-gradle-catalogs/` folder in ai-code-instructions contains version catalogs for:

| Component | Managed By | Purpose |
|-----------|------------|---------|
| `libs.versions.toml` | Dependabot | External third-party dependencies |
| `repos/*.versions.toml` | mise tasks | Internal Bitso library versions |
| `recommended-versions.versions.toml` | Auto-sync | Curated versions for AI agents |

**Why use this?**
- Find which repo publishes a specific library
- Check current recommended versions
- Understand library ownership by squad (via `squad` metadata)

### Squad Metadata

Each repo catalog includes squad ownership from estate-catalog:

```toml
[metadata]
repository = "bitsoex/business-account"
last_updated = "2026-02-03"
squad = "experience-squad"
```

This makes it easy to identify which squad owns a library.

## Directory Structure

```text
bitso-gradle-catalogs/
├── libs.versions.toml                  # External deps (Dependabot)
├── recommended-versions.versions.toml  # AI reference versions
└── repos/                              # Per-repo catalogs
    ├── business-account.versions.toml
    ├── bitso-common.versions.toml
    ├── payments-api.versions.toml
    └── {repo-name}.versions.toml       # 100+ repo catalogs
```

### File Format

Each catalog uses standard TOML format:

```toml
[metadata]
repository = "bitsoex/business-account"
last_updated = "2026-02-03"
squad = "experience-squad"

[libraries]
business-account-api = { group = "com.bitso.business", name = "business-account-api", version = "2.1.0" }
# history: 2.0.0 (abc1234, 2025-12-15)
```

## Search Commands

### Find Library Owner

```bash
# Search for a specific library
grep -r "com.bitso.payments" bitso-gradle-catalogs/repos/

# Find all libraries from a squad pattern
grep -rh "com.bitso.identity" bitso-gradle-catalogs/repos/ | sort -u
```

### Check Current Version

```bash
# Get version from a specific repo catalog
grep "library-name" bitso-gradle-catalogs/repos/repo-name.versions.toml

# Check recommended version
grep "library-name" bitso-gradle-catalogs/recommended-versions.versions.toml
```

### List All Libraries from a Repo

```bash
# Show all libraries in a repo's catalog
grep -A1 "\[libraries\]" bitso-gradle-catalogs/repos/repo-name.versions.toml
```

### View Version History

```bash
# Show version history comments
grep -A1 "version-name" bitso-gradle-catalogs/repos/repo-name.versions.toml
# Output: version = "2.0.0"
# history: 1.9.0 (abc1234, 2025-01-15), 1.8.0 (def5678, 2025-01-01)
```

## Common Queries

### When reviewing a Dependabot PR

1. **Find the source repo:**

   ```bash
   grep -rl "com.bitso.{library-from-pr}" bitso-gradle-catalogs/repos/
   ```

2. **Check the owning squad:**
   Read the `squad` field from the repo catalog's `[metadata]` section to identify ownership (e.g., `squad = "payments-squad"`)

3. **Verify version freshness:**

   ```bash
   grep "last_updated" bitso-gradle-catalogs/repos/{repo}.versions.toml
   ```

### Find all dependencies from a squad

```bash
# Example: find all identity squad libraries
ls bitso-gradle-catalogs/repos/ | grep -i identity
cat bitso-gradle-catalogs/repos/identity-*.versions.toml
```

### Check external dependency versions

```bash
# View external dependencies managed by Dependabot
cat bitso-gradle-catalogs/libs.versions.toml | grep spring
cat bitso-gradle-catalogs/libs.versions.toml | grep aws
```

### Validate catalogs

```bash
# Run validation
mise run catalogs:validate

# Test Dependabot config locally
mise run catalogs:test-dependabot
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/references/catalog-search.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

