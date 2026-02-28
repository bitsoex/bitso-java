---
name: grpc-migration
description: >
  Orchestrator for migrating CustomRPC to standard gRPC. Coordinates assessment,
  contract distribution, and error handling migrations.
compatibility: Java projects using CustomRPC with Failure entities; requires grpc-compliance-validate-repository
metadata:
  version: "1.0.0"
  technology: java
  category: migration
  tags:
    - java
    - grpc
    - migration
    - rfc-33
    - customrpc
dependencies: grpc-compliance-validate-repository
---

# gRPC Migration Orchestrator

Orchestrator for migrating CustomRPC with Failure entities to standard gRPC error handling.

## When to Use This Skill

Use this skill when you need to:
- Migrate from CustomRPC implementations with `Failure` entities to standard gRPC error handling
- Split proto modules into proto-only (published) and `-generated` (internal) modules
- Move proto files from `src/main/proto/` to `src/main/resources/{package/path}/`
- Create V2 gRPC services with proper error handling
- Validate gRPC compliance using `grpc-compliance-validate-repository`
- Ensure RFC-33 compliance for gRPC services

## Skill Contents

### Sections

- [When to Use This Skill](#when-to-use-this-skill)
- [Prerequisites](#prerequisites)
- [Migration Workflow](#migration-workflow)
- [Available Sub-Skills](#available-sub-skills)
- [Quick Start](#quick-start)
- [Benefits After Migration](#benefits-after-migration)
- [Performance Considerations](#performance-considerations)
- [Pre-Migration Checklist](#pre-migration-checklist)
- [⚠️ Critical Anti-Patterns to Avoid](#critical-anti-patterns-to-avoid)
- [Best Practices](#best-practices)
- [Resources](#resources)

### Available Resources

**📚 references/** - Detailed documentation
- [SHARED TEMPLATES](references/SHARED_TEMPLATES.md)

---

## Prerequisites

The `grpc-compliance-validate-repository` command must be available before starting any migration.

**Cloud agents**: Pre-installed (no action needed).

**Local setup**:

```bash
export HOMEBREW_GITHUB_API_TOKEN=your-token
brew tap bitsoex/homebrew-bitso
brew install bitso-grpc-linter
brew install bufbuild/buf/buf
```

**Verify**: `grpc-compliance-validate-repository --help`

See [../grpc-services-rfc-33/references/installation.md](.claude/skills/grpc-services-rfc-33/references/installation.md) for details.

## Migration Workflow

```
1. ASSESSMENT
   Run compliance validator → Document ERRORS
   Use: grpc-migration-assessment
   ↓
2. CONTRACT DISTRIBUTION (for each proto module with ERRORS)
   - Split into proto-only and -generated modules
   - Move proto files to src/main/resources/{package/path}/
   - Update all internal references
   Use: grpc-migration-contract-distribution
   ↓
3. ERROR HANDLING (for each service with Failure responses)
   - Create V2 service (e.g., TransferServiceV2)
   - Move methods to V2 (no V2 suffix on method names)
   - Compile protos and implement Java handlers
   - Mark original methods with @replacedBy
   Use: grpc-migration-error-handling
   ↓
4. VALIDATION
   Run compliance validator again → Verify ZERO errors
   Run full build and tests → Create comprehensive PR
```

## Available Sub-Skills

| Sub-Skill | Purpose | When to Use |
|-----------|---------|-------------|
| `grpc-migration-assessment` | Initial compliance check | First step - identify all issues |
| `grpc-migration-contract-distribution` | Proto module restructuring | Proto modules with compilation issues |
| `grpc-migration-error-handling` | V2 service creation | Services using Failure entity |

## Quick Start

### Step 1: Run Assessment

```bash
grpc-compliance-validate-repository --dir .
```

Document all ERRORS found:
- Proto modules with incorrect distribution
- Services using `Failure` entity
- Proto files in wrong location

### Step 2: Prioritize

1. **Start with fewest dependencies** - Migrate leaf modules first
2. **Group related changes** - Proto module + service migration together
3. **Test incrementally** - Validate after each module migration

### Step 3: Execute Migration

For each module/service:
1. Apply the appropriate sub-skill
2. Run compliance validation
3. Verify ERRORS count decreases
4. Continue until ZERO errors remain

### Step 4: Final Validation (MANDATORY BUILD LOOP)

**⚠️ CRITICAL: Do NOT create PR until build passes**

```bash
# 1. Run compliance check
grpc-compliance-validate-repository --dir .   # Zero errors

# 2. Run build - MUST PASS
./gradlew clean build

# 3. IF BUILD FAILS:
#    - Analyze error output
#    - Fix compilation issues (imports, dependencies, typos)
#    - Fix missing proto generations
#    - REPEAT until build passes

# 4. Run tests
./gradlew test                                 # Tests pass

# 5. ONLY THEN create PR
```

**Build failure resolution loop:**
```
BUILD FAILED → Analyze error → Fix issue → ./gradlew build → REPEAT until SUCCESS
```

Common build failures after migration:
- Missing imports in new V2 handlers
- Wrong dependency references (still pointing to old module)
- Proto not regenerated after changes
- FailureHelper not created/imported

## Benefits After Migration

**Observability:**
- Errors properly labeled in distributed tracing (no longer all "OK")
- Default framework metrics work correctly
- Simplified incident management with standard error codes

**Maintainability:**
- No transitive dependency conflicts
- Consumer control over gRPC versions
- Proper semantic versioning prevents breaking changes
- Clear separation between technical and business errors

**Resiliency:**
- Built-in timeouts and retries
- Per-operation configuration possible
- Retry behavior based on standard error codes

## Performance Considerations

**Proto Compilation Time:**
- Development: First `generateProto` may take 10-30 seconds
- CI/CD: Include `generateProto` in build pipeline before compile
- Caching: Gradle caches generated code; only regenerates when protos change

**Runtime Performance:**
- No performance impact: Standard gRPC has same performance as CustomRPC
- Error handling: `google.rpc.Status` with details has negligible overhead
- Observability gain: Better tracing/metrics outweigh any minimal overhead

## CRITICAL: MAJOR Version Bump Required

**gRPC migration is a BREAKING CHANGE.** You MUST bump the MAJOR version in the protobuf module's `gradle.properties` BEFORE creating a PR.

```properties
# {proto-module}/gradle.properties
# Before: version=1.2.3
# After:  version=2.0.0
```

**Why this is mandatory:**
- External consumers depend on the published proto-only module
- Migration changes the contract distribution model (consumers must now compile protos themselves)
- Adding V2 services signals consumers need to migrate their error handling
- Semantic versioning requires MAJOR bump for breaking changes

**Failure to bump MAJOR version will:**
- Break external consumers silently
- Cause version conflicts in downstream services
- Make rollback and debugging extremely difficult
- Violate RFC-33 compliance

## Pre-Migration Checklist

Before starting migration, verify your plan does NOT:
- [ ] Rename any existing service (adding V1 suffix to existing names)
- [ ] Change the wire name of any existing RPC method
- [ ] Remove any existing service without a deprecation period

The goal is **additive changes only** for service definitions.

## ⚠️ Critical Anti-Patterns to Avoid

### NEVER Rename Existing Services

**The original service name must NEVER change.** Renaming an existing service (e.g., adding V1 suffix) is a breaking change that will cause client failures.

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| Rename `FooService` → `FooServiceV1` | Keep `FooService` unchanged |
| Then create `FooServiceV2` | Add new `FooServiceV2` alongside |

**Why this matters:**
- Clients are calling `FooService` - if you rename it to `FooServiceV1`, all calls fail
- gRPC service names are part of the wire protocol (full method name includes service name)
- This is NOT like deprecating an API - it's an immediate hard break

**Example of the WRONG approach (from real PR):**
```protobuf
// ❌ WRONG: Original service was renamed
service SavingsPartnerManagementServiceV1 { ... }  // Was SavingsPartnerManagementService
service SavingsPartnerManagementServiceV2 { ... }  // New
// Result: All existing clients break immediately!
```

**Example of the CORRECT approach:**
```protobuf
// ✅ CORRECT: Original service preserved
service SavingsPartnerManagementService { ... }  // Unchanged, may have @deprecated methods
service SavingsPartnerManagementServiceV2 { ... }  // New service for new patterns
// Result: Existing clients continue working, new clients use V2
```

## Best Practices

1. **NEVER rename existing services** - Keep original service name, create NEW V2 service alongside
2. **ALWAYS bump MAJOR version** - gRPC migration is a BREAKING CHANGE (e.g., 1.2.3 → 2.0.0)
3. **Always create V2 services** (not V2 methods) for cleaner separation
4. **Proto files must follow package structure** in `src/main/resources/`
5. **Run compliance validation** after each migration step
6. **Compile protos before implementing handlers** to get generated stubs
7. **Use google.rpc.Status** for business error details
8. **Only publish proto-only modules**, never `-generated` modules
9. **Test external consumer workflow** before finalizing migration
10. **Document breaking changes** clearly in PR and changelog
11. **Coordinate with consumers** before publishing breaking changes
12. **NEVER create PR with broken build** - run `./gradlew clean build` and fix ALL errors before PR
13. **Create FailureHelper utility class** in the grpc module for consistent error handling
14. **Use project-specific packages for NEW protos** - don't use generic `protos.model` for new V2 services

## Resources

- `references/SHARED_TEMPLATES.md` - PR and commit message templates

### Related Skills

- `/grpc-migration-assessment` - Detailed assessment guidance
- `/grpc-migration-contract-distribution` - Proto module migration
- `/grpc-migration-error-handling` - V2 service creation
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

