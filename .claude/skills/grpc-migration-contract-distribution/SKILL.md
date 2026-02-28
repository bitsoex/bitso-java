---
name: grpc-migration-contract-distribution
description: >
  Migrate proto modules to proper contract distribution.
  Split into proto-only and -generated modules.
compatibility: Java projects with protobuf modules; requires grpc-compliance-validate-repository
metadata:
  version: "1.0.0"
  technology: java
  category: migration
  tags:
    - java
    - grpc
    - protobuf
    - migration
---

# Contract Distribution Migration

Migrate proto modules to proper contract distribution with proto-only and -generated module split.

## Purpose

Migrate proto modules from incorrect distribution (compiled code + dependencies) to proper contract distribution (proto-only artifacts). This ensures:
- External consumers can compile protos with their own gRPC versions
- No transitive dependency conflicts
- RFC-33 compliance

## Skill Contents

### Sections

- [Purpose](#purpose)
- [Prerequisites](#prerequisites)
- [CRITICAL: Proto File Location and Package Naming](#critical-proto-file-location-and-package-naming)
- [Module Structure Transformation](#module-structure-transformation)
- [Migration Steps](#migration-steps)
- [Migration Checklist](#migration-checklist)
- [Common Pitfalls](#common-pitfalls)
- [Troubleshooting](#troubleshooting)
- [External Consumer Migration](#external-consumer-migration)
- [References](#references)

### Available Resources

**📚 references/** - Detailed documentation
- [BUILD EXAMPLES](references/BUILD_EXAMPLES.md)

---

## Prerequisites

The `grpc-compliance-validate-repository` command must be available for the migration checklist validation step.

**Cloud agents**: Pre-installed (no action needed).

**Local setup**:

```bash
export HOMEBREW_GITHUB_API_TOKEN=your-token
brew tap bitsoex/homebrew-bitso
brew install bitso-grpc-linter
```

**Verify**: `grpc-compliance-validate-repository --help`

See [../grpc-services-rfc-33/references/installation.md](.claude/skills/grpc-services-rfc-33/references/installation.md) for details.

## CRITICAL: Proto File Location and Package Naming

### File Location
Proto files MUST be in `src/main/resources/` following the package structure:

- **Correct**: `package com.bitso.account.v1;` → `src/main/resources/com/bitso/account/v1/account.proto`
- **Incorrect**: `src/main/proto/account.proto` or `src/main/resources/proto/account.proto`

### Package Naming: Existing vs NEW Protos

**⚠️ IMPORTANT: Different rules for existing vs new protos**

| Proto Type | Package Rule | Example |
|------------|--------------|---------|
| **EXISTING** (being moved) | Keep original package for backwards compatibility | Keep `package protos.model;` if that's what it has |
| **NEW** (V2 services, new messages) | Use project-specific package | `package com.bitso.{service}.v2;` |

**For EXISTING protos being moved:**
```protobuf
// Keep the existing package - DO NOT CHANGE
syntax = "proto3";
package protos.model;  // Keep this even though it's generic
option java_package = "com.bitso.iba.model";

// Just move to: src/main/resources/protos/model/
```

**For NEW protos (V2 services, new error types):**
```protobuf
// Use project-specific package - DO NOT use protos.model
syntax = "proto3";
package com.bitso.iba.rate.v2;  // Project-specific
option java_package = "com.bitso.iba.rate.v2";

// Place in: src/main/resources/com/bitso/iba/rate/v2/
```

**Determine project package convention by:**
1. Check existing Java packages in the codebase (e.g., `com.bitso.iba.grpc.service`)
2. Use the same root package structure for new proto contracts

## Module Structure Transformation

### Before (Incorrect)

```
project-root/
├── account-protos/
│   ├── build.gradle              # Has protobuf plugin + dependencies
│   └── src/main/proto/           # Wrong location
│       └── account.proto
```

### After (Correct)

```
project-root/
├── account-protos/
│   ├── build.gradle                       # NO protobuf plugin, NO dependencies
│   └── src/main/resources/                # Correct location
│       └── com/bitso/account/v1/          # Follows package structure
│           └── account.proto
├── account-protos-generated/
│   └── build.gradle                       # Has protobuf plugin (internal only)
```

## Migration Steps

### Step 1: Move Proto Files to Follow Package Structure

```bash
# Example: package com.bitso.account.v1;
mkdir -p proto-module/src/main/resources/com/bitso/account/v1
mv proto-module/src/main/proto/*.proto proto-module/src/main/resources/com/bitso/account/v1/
rm -rf proto-module/src/main/proto
```

### Step 2: Remove Protobuf Compilation from Proto-Only Module

Edit the proto-only module's `build.gradle`:
- Remove `com.google.protobuf` plugin
- Remove all gRPC/protobuf dependencies
- Remove `protobuf {}` configuration block

See `references/BUILD_EXAMPLES.md` for configuration templates.

### Step 3: Create New `-generated` Module

```bash
mkdir -p proto-module-generated/src/main/java
# Create build.gradle with protobuf compilation config
```

See `references/BUILD_EXAMPLES.md` for the complete generated module configuration.

### Step 4: Update All Project References

```gradle
// Before
dependencies {
    implementation project(':proto-module')
}

// After
dependencies {
    implementation project(':proto-module-generated')
}
```

### Step 5: Update settings.gradle

```gradle
include 'proto-module'
include 'proto-module-generated'  // NEW
```

### Step 6: MANDATORY - Bump MAJOR Version in Protobuf Module

**This step is NON-NEGOTIABLE.** Contract distribution migration is a BREAKING CHANGE.

Update the version in the **protobuf module's `gradle.properties`** file:

```bash
# File: {proto-module}/gradle.properties
# Before: version=1.2.3
# After:  version=2.0.0 (BREAKING CHANGE - MAJOR bump required)
```

```properties
# {proto-module}/gradle.properties
version=2.0.0
```

**Why MAJOR version bump is mandatory:**
- External consumers depend on the published proto-only module
- After migration, consumers must compile protos themselves (breaking workflow change)
- Proto files moved from compiled JAR to resources-only JAR
- gRPC/protobuf dependencies removed from published artifact
- Semantic versioning REQUIRES major bump for breaking changes

**Failure to bump MAJOR version will cause:**
- Silent breakage for external consumers
- Runtime ClassNotFoundException/NoSuchMethodError in downstream services
- Extremely difficult debugging and rollback scenarios
- RFC-33 compliance violations

## Migration Checklist

### Proto Module Updates
- [ ] Move proto files to `src/main/resources/{package/path}/` matching package declaration
- [ ] Delete old `src/main/proto/` directory
- [ ] Remove protobuf plugin from proto-only module
- [ ] Remove all gRPC/protobuf dependencies from proto-only module
- [ ] Remove `protobuf {}` configuration from proto-only module

### Generated Module Creation
- [ ] Create new `-generated` module directory
- [ ] Configure protobuf compilation in `-generated` module
- [ ] Make `-generated` depend on proto-only via `project(':proto-module')`
- [ ] Update `settings.gradle` to include `-generated` module

### Reference Updates
- [ ] Find and replace all `project(':proto-module')` with `project(':proto-module-generated')`
- [ ] Verify proto-only module has zero dependencies
- [ ] Ensure only proto-only module is published (not `-generated`)
- [ ] Remove API/protoshim artifacts if present

### Version Management (MANDATORY)
- [ ] **CRITICAL: Bump MAJOR version** in **protobuf module's** `gradle.properties` (e.g., 1.2.3 → 2.0.0)
- [ ] Location: `{proto-module}/gradle.properties`
- [ ] **DO NOT proceed to PR creation without completing this step**

### Build Verification (MANDATORY)
- [ ] Run `./gradlew clean build`
- [ ] **IF BUILD FAILS**: Analyze errors, fix issues, repeat until SUCCESS
- [ ] Common fixes: wrong project references, missing imports, proto not regenerated

### Validation
- [ ] Run `grpc-compliance-validate-repository --dir .` and verify no ERRORS
- [ ] Run `./gradlew test`
- [ ] **⚠️ Only create PR after build passes**

## Common Pitfalls

### Proto Files in Wrong Location

- **Correct**: `src/main/resources/com/bitso/account/v1/account.proto`
- **Incorrect**: `src/main/proto/account.proto`

### Not Bumping MAJOR Version (CRITICAL ERROR)

- **gRPC migration is ALWAYS a breaking change** - MAJOR version bump is mandatory
- Contract distribution changes break external consumers' build process
- Using MINOR/PATCH for contract changes violates semantic versioning
- **This mistake causes production incidents in downstream services**

### Publishing `-generated` Modules

- Only publish proto-only modules
- Never publish `-generated` modules to artifact repository

## Troubleshooting

### Proto files not found during compilation

1. Verify proto files are in `src/main/resources/{package/path}/`
2. Check package declaration matches directory structure
3. Ensure proto-only module is a dependency of -generated module

### Consumer can't compile protos

1. Verify proto-only module is published (not -generated)
2. Check proto files are in JAR resources:
   ```bash
   jar tf account-protos-2.0.0.jar | grep .proto
   ```

## External Consumer Migration

After migration, external consumers must:
1. Add protobuf plugin to their build
2. Add gRPC dependencies
3. Configure proto compilation

See `references/BUILD_EXAMPLES.md` for consumer configuration template.

## References

- `references/BUILD_EXAMPLES.md` - Complete Gradle configuration examples
- RFC-33: gRPC Service Standards
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration-contract-distribution/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

