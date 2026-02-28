---
name: grpc-migration-assessment
description: >
  Initial compliance assessment for gRPC migration. Identifies proto modules
  and services requiring migration.
compatibility: Java projects with gRPC services; requires grpc-compliance-validate-repository
metadata:
  version: "1.0.0"
  technology: java
  category: migration
  tags:
    - java
    - grpc
    - assessment
    - migration
---

# gRPC Migration Assessment

Initial compliance assessment for gRPC migration.

## Purpose

Run this assessment before starting any gRPC migration work to:
- Identify all compliance issues in the codebase
- Categorize ERRORS (must fix) vs WARNINGS (can defer)
- Create a prioritized migration plan
- Estimate scope of work

## Skill Contents

### Sections

- [Purpose](#purpose)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Migration Planning](#migration-planning)
- [Validation Commands Reference](#validation-commands-reference)
- [Expected Output](#expected-output)
- [Next Steps](#next-steps)

*This skill contains only the main documentation below.*

---

## Prerequisites

The `grpc-compliance-validate-repository` command must be available to run the compliance assessment.

**Cloud agents**: Pre-installed (no action needed).

**Local setup**:

```bash
export HOMEBREW_GITHUB_API_TOKEN=your-token
brew tap bitsoex/homebrew-bitso
brew install bitso-grpc-linter
```

**Verify**: `grpc-compliance-validate-repository --help`

See [../grpc-services-rfc-33/references/installation.md](.claude/skills/grpc-services-rfc-33/references/installation.md) for details.

## Quick Start

### Step 1: Run Compliance Validation

```bash
# Run compliance validator to identify all issues
grpc-compliance-validate-repository --dir .

# For verbose output with detailed explanations
grpc-compliance-validate-repository --dir . --verbose

# Output to file for tracking
grpc-compliance-validate-repository --dir . > compliance-report.txt
```

### Step 2: Document Findings

Document all ERRORS found:
- Proto modules with incorrect distribution (compiled code + dependencies)
- Services using `Failure` entity in responses
- Proto files in wrong location (`src/main/proto/` instead of `src/main/resources/`)

### Step 3: Categorize Issues

**ERRORS (must fix):**
- Proto modules with incorrect distribution (compiled code + dependencies)
- Proto files in wrong location (`src/main/proto/`)
- Response messages using `Failure` entity with `oneOf` patterns
- Missing `-generated` modules for internal proto compilation

**WARNINGS (can defer):**
- Non-critical style issues
- Optional best practice recommendations
- When creating new methods (such as V2), try to fix the warnings

## Migration Planning

### Prioritization Strategy

1. **Start with fewest dependencies** - Migrate leaf modules first
2. **Group related changes** - Proto module + service migration together
3. **Test incrementally** - Validate after each module migration

### Create Migration Checklist

For each proto module needing migration:
- [ ] Module name
- [ ] Current proto file location
- [ ] Package declaration
- [ ] Dependent modules/services

For each service needing V2 migration:
- [ ] Service name
- [ ] Methods using Failure entity
- [ ] Consumers to notify

## Validation Commands Reference

### Check Specific Module

```bash
grpc-compliance-validate-repository --dir ./path/to/module
```

### Build Validation

```bash
# Clean build to verify everything compiles
./gradlew clean build

# Build specific modules
./gradlew :module-name:build
```

### Dependency Analysis

```bash
# View dependency tree
./gradlew :module-name:dependencies

# Check for dependency conflicts
./gradlew :module-name:dependencyInsight --dependency grpc-stub
```

## Expected Output

After running assessment, you should have:
1. List of all ERRORS requiring immediate fix
2. List of WARNINGS to address opportunistically
3. Prioritized order for module migrations
4. Prioritized order for service V2 migrations
5. List of external consumers to notify

## Next Steps

Based on assessment results:
- **For proto module issues** → Use `grpc-migration-contract-distribution`
- **For Failure entity issues** → Use `grpc-migration-error-handling`
- **For full migration** → Use `grpc-migration` orchestrator
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration-assessment/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

