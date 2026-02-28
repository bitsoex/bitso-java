# Squad-Level Dependabot Groups

Understanding and using squad-level Dependabot grouping for efficient PR management.

## Contents

- [Overview](#overview)
- [Available Groups](#available-groups)
- [Using Groups for Triage](#using-groups-for-triage)
- [Group Configuration](#group-configuration)

---
## Overview

As of PR #8703, Dependabot PRs are grouped by squad ownership. This provides:

- **Better visibility** into which squad owns which dependencies
- **Easier batching** of related updates
- **Clear responsibility** for reviewing PRs

PR titles now include the group name:

```text
chore(deps): bump com.bitso.example in the asset-management-squad group
```

## Available Groups

### Internal Library Groups

| Group | Description |
|-------|-------------|
| `jvm-generic-libraries` | Shared platform libraries (com.bitso.commons, com.bitso.aux) |
| `asset-management-squad` | Asset management services |
| `blackbird-squad` | Blackbird team libraries |
| `captain-squad` | Captain team libraries |
| `compliance-squad` | Compliance and regulatory |
| `crypto-squad` | Cryptocurrency operations |
| `fiat-squad` | Fiat currency operations |
| `identity-squad` | Identity and KYC |
| `marketplace-squad` | Trading marketplace |
| `payments-squad` | Payment processing |
| `platform-squad` | Core platform infrastructure |
| `security-squad` | Security tools and libraries |
| *(22 total squad groups)* | See dependabot.yml for full list |

### External Dependency Groups

| Group | Description |
|-------|-------------|
| `spring` | Spring Boot, Spring Framework |
| `grpc` | gRPC and Protobuf |
| `aws` | AWS SDK libraries |
| `testing` | JUnit, Mockito, TestContainers |
| `logging` | Logback, SLF4J |
| `codegen` | Lombok, MapStruct |
| `plugins` | Gradle plugins |
| `default` | Catch-all for ungrouped deps |

## Using Groups for Triage

### Filter PRs by Group

```bash
# List PRs for a specific squad
gh pr list --author app/dependabot --state open | grep "asset-management-squad"

# JSON output for scripting
gh pr list --author app/dependabot --state open --json number,title | \
  jq '.[] | select(.title | contains("platform-squad"))'
```

### Prioritize by Group

**High Priority Groups:**
1. `security-squad` - Security-critical updates
2. `platform-squad` - Core infrastructure
3. `jvm-generic-libraries` - Widely used libraries

**Standard Priority:**
- All other squad groups
- External dependency groups

### Batch Review by Group

When reviewing internal library updates:

1. Filter to one group at a time
2. Review all PRs for that group together
3. Check for version conflicts
4. Merge in dependency order

## Group Configuration

Groups are defined in `estate-catalog` repository:

```text
types/dependabot/extensions.cue    # Group definitions
transforms/cicd/dependabot/dependabot.cue  # Ignore rules
```

### Adding New Groups

New squad groups can be added by:

1. Editing `types/dependabot/extensions.cue`
2. Adding patterns for the squad's libraries
3. Running CUE validation
4. Creating a PR to estate-catalog

### Blocked Updates

Some major versions are blocked to prevent unwanted upgrade PRs:

- `org.springframework*` major versions (Spring Boot 4, Framework 7)

See PR #8691 for details on blocking configuration.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/references/squad-groups.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

