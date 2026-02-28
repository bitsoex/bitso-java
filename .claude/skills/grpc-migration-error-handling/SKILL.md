---
name: grpc-migration-error-handling
description: >
  Migrate from Failure entity to standard gRPC Status error handling.
  Create next-version services with proper error codes.
compatibility: Java projects using CustomRPC Failure patterns; requires grpc-compliance-validate-repository
metadata:
  version: "1.0.0"
  technology: java
  category: migration
  tags:
    - java
    - grpc
    - error-handling
    - migration
---

# Error Handling Migration

Create next-version services with standard gRPC Status error handling.

## Purpose

Migrate services from CustomRPC implementations using `Failure` entity to standard gRPC error handling. This ensures:
- Proper error visibility in distributed tracing
- Standard gRPC Status codes
- RFC-33 compliance

## Skill Contents

### Sections

- [Purpose](#purpose)
- [Prerequisites](#prerequisites)
- [Determine the Next Version Number](#determine-the-next-version-number)
- [Key Principle: Create New Versioned Services (Not Versioned Methods)](#key-principle-create-new-versioned-services-not-versioned-methods)
- [Key Principle: Separate Proto Files for New Version Definitions](#key-principle-separate-proto-files-for-new-version-definitions)
- [Before and After](#before-and-after)
- [Standard gRPC Error Codes](#standard-grpc-error-codes)
- [Proto Package Naming for NEW Services](#proto-package-naming-for-new-services)
- [Migration Steps](#migration-steps)
- [Migration Checklist](#migration-checklist)
- [Common Pitfalls](#common-pitfalls)
- [Consumer Migration Guide](#consumer-migration-guide)
- [References](#references)

### Available Resources

**📚 references/** - Detailed documentation
- [ERROR PATTERNS](references/ERROR_PATTERNS.md)

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

## Determine the Next Version Number

Before starting, check what versioned services already exist to pick the correct next version suffix.

**How to determine V{N}:**
1. Search the proto files for existing versioned services (e.g., `AccountServiceV2`, `AccountServiceV3`)
2. Use the next available number: if V2 exists, create V3; if no versioned service exists, create V2
3. Apply the same suffix consistently to the service, response messages, proto file name, and handler class

**Example:** If `AccountService` and `AccountServiceV2` already exist, create `AccountServiceV3` — along with `AccountResponseV3`, `account_service_v3.proto`, and `AccountServiceV3Handler`.

**Throughout this document, `V{N}` means the next available version number.** Examples use V2 as a concrete illustration — substitute the actual version for your project.

## Key Principle: Create New Versioned Services (Not Versioned Methods)

**Why a new versioned service?**
- Cleaner separation between old and new error handling patterns
- Easier for consumers to migrate (just switch stub)
- Better service discovery (V{N} appears as separate service in gRPC tools)
- Method names remain clean without version suffix
- Simpler to eventually deprecate and remove old services

## Key Principle: Separate Proto Files for New Version Definitions

**Always place the new versioned service and its specific message definitions in a NEW proto file**, separate from the previous version's definitions.

**Why separate files?**
- **Clean removal**: deleting the old version is just deleting its file — no surgical edits inside a shared file
- **Proper package assignment**: the new version can use a project-specific package even if the old version uses a generic one (e.g., `protos.model`)
- **Independent evolution**: the new version can be modified without touching the old version at all
- **Clearer ownership**: each file has a single purpose (one version)

**What goes in the new version's file:**
- New versioned service definition
- Version-specific response messages (ones without `Failure`/`oneof`)

**What stays in the old version's file:**
- Old service definition (marked deprecated)
- Old version-specific response messages (marked deprecated)
- Shared types (request messages, payload types, enums) reused by the new version

The new version's file imports the old version's file (or its model file) to reference shared types.

## Before and After

> **Note:** Examples below use V2 as a concrete illustration. If V2 already exists in your project, substitute V3 (or the appropriate next version) throughout.

### Before (CustomRPC - DO NOT USE)

```protobuf
service TransferService {
  rpc Transfer(TransferRequest) returns (TransferResponse);
}

message TransferResponse {
  oneof result {
    Transfer transfer = 1;
    Failure failure = 2;  // DO NOT USE
  }
}
```

```java
// Old pattern
if (invalidAmount) {
  return TransferResponse.newBuilder()
    .setFailure(Failure.newBuilder()
      .setCode("INVALID_AMOUNT")
      .setMessage("Amount must be positive")
      .build())
    .build();
}
```

### After (Standard gRPC - REQUIRED)

**File: `transfer_service.proto` (old version — deprecated, kept for backwards compatibility)**

```protobuf
syntax = "proto3";
package com.bitso.transfer;

/**
 * @deprecated since v2.0.0, forRemoval (planned for v3.0.0).
 * Use TransferServiceV2 instead.
 */
service TransferService {
  option deprecated = true;

  // @replacedBy: TransferServiceV2.Transfer
  rpc Transfer(TransferRequest) returns (TransferResponse) {
    option deprecated = true;
  }
}

// Deprecated since v2.0.0. Planned for removal in v3.0.0.
// Use TransferResponseV2 instead.
message TransferResponse {
  option deprecated = true;

  oneof result {
    Transfer transfer = 1;
    Failure failure = 2;
  }
}

// Shared types — NOT deprecated, reused by V2
message TransferRequest { ... }
message Transfer { ... }
```

**File: `transfer_service_v2.proto` (new version — separate file)**

```protobuf
syntax = "proto3";
package com.bitso.transfer.v2;

option java_package = "com.bitso.transfer.v2";
option java_outer_classname = "TransferServiceV2Proto";

import "transfer_service.proto";

// V2 service with standard gRPC error handling
service TransferServiceV2 {
  rpc Transfer(com.bitso.transfer.TransferRequest) returns (TransferResponseV2);
}

message TransferResponseV2 {
  com.bitso.transfer.Transfer transfer = 1;  // No oneof, no Failure
}

// Business error details
enum TransferErrorCode {
  INVALID_AMOUNT = 0;
  INSUFFICIENT_FUNDS = 1;
}

message TransferError {
  TransferErrorCode code = 1;
  string message = 2;
}
```

```java
// New versioned service handler
public class TransferServiceV2Handler extends TransferServiceV2Grpc.TransferServiceV2ImplBase {

  @Override
  public void transfer(TransferRequest request, StreamObserver<TransferResponseV2> responseObserver) {
    if (invalidAmount) {
      TransferError error = TransferError.newBuilder()
        .setCode(TransferErrorCode.INVALID_AMOUNT)
        .setMessage("Amount must be positive")
        .build();

      // Use google.rpc.Status (not io.grpc.Status)
      Status status = Status.newBuilder()
        .setCode(Code.FAILED_PRECONDITION.getNumber())
        .setMessage("Business constraint violation")
        .addDetails(Any.pack(error))
        .build();

      responseObserver.onError(StatusProto.toStatusRuntimeException(status));
      return;
    }

    // Success case
    TransferResponseV2 response = TransferResponseV2.newBuilder()
      .setTransfer(transfer)
      .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
```

## Standard gRPC Error Codes

Use ONLY these error codes at Bitso:

| Error Code | When to Use |
|------------|-------------|
| `INTERNAL` | Infrastructure errors, dependency unavailable, system failures |
| `UNKNOWN` | Unknown errors from downstream services |
| `FAILED_PRECONDITION` | Business constraint violations, business logic errors |

**Key Principle:** gRPC errors are TECHNICAL. Business error details go in trailers/details using `google.rpc.Status`.

## Proto Package Naming for NEW Services

**⚠️ CRITICAL: Package naming differs for existing vs NEW protos**

### For EXISTING protos being moved (contract distribution only):
- Keep the existing package declaration unchanged for backwards compatibility
- Example: If original uses `package protos.model;`, keep it

### For NEW protos (new versioned services, new messages, new enums):
- **DO NOT** use generic `package protos.model;`
- Use project-specific package following Java conventions:
  - Pattern: `com.bitso.{service-name}.{version}` or `com.bitso.{service-name}.v{N}`
  - Example: `com.bitso.iba.rate.v2`, `com.bitso.transfer.v3`
- The `java_package` option should align with the actual package structure

**Determine project package convention by:**
1. Check existing Java packages in the codebase (e.g., `com.bitso.iba.grpc.service`)
2. Use the same root package for new proto contracts
3. Look for patterns in `java_package` options in existing protos

```protobuf
// ❌ WRONG - Generic package for NEW versioned services
syntax = "proto3";
package protos.model;
option java_package = "com.bitso.iba.model";

// ✅ CORRECT - Project-specific package for NEW versioned services
syntax = "proto3";
package com.bitso.iba.rate.v2;
option java_package = "com.bitso.iba.rate.v2";
```

**Proto file location must match package:**
- `package com.bitso.iba.rate.v2;` → `src/main/resources/com/bitso/iba/rate/v2/iba_rate.proto`

## Migration Steps

### Step 0: Create FailureHelper Utility Class (If Not Exists)

Before implementing the new versioned services, create a centralized `FailureHelper` in the grpc module:

```java
package com.bitso.{service}.grpc.util;

import com.bitso.commons.protobuf.DataCommonsProto;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.lite.ProtoLiteUtils;

/**
 * Utility class for consistent gRPC error handling.
 * Use this for all new versioned service error responses.
 */
public class FailureHelper {

    private static final Metadata.Key<DataCommonsProto.Failure> FAILURE_DETAILS_KEY =
        Metadata.Key.of(
            "bitso-failure-detail-bin",
            ProtoLiteUtils.metadataMarshaller(DataCommonsProto.Failure.getDefaultInstance()));

    /**
     * Creates a StatusRuntimeException with failure details in metadata.
     *
     * @param code The gRPC status code (INTERNAL, FAILED_PRECONDITION, UNKNOWN)
     * @param failure The failure details to include in metadata
     * @return StatusRuntimeException ready to be thrown
     */
    public static StatusRuntimeException createStatusRuntimeException(
            Status.Code code, DataCommonsProto.Failure failure) {
        Metadata metadata = new Metadata();
        metadata.put(FAILURE_DETAILS_KEY, failure);
        return code.toStatus().withDescription(failure.getCode()).asRuntimeException(metadata);
    }

    /**
     * Extracts failure details from a throwable (typically StatusRuntimeException).
     *
     * @param throwable The exception to extract failure from
     * @return The Failure details, or empty Failure if not present
     */
    public static DataCommonsProto.Failure extractFailure(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException statusRuntimeException) {
            if (statusRuntimeException.getTrailers() != null
                    && statusRuntimeException.getTrailers().get(FAILURE_DETAILS_KEY) != null) {
                return statusRuntimeException.getTrailers().get(FAILURE_DETAILS_KEY);
            }
        }
        return DataCommonsProto.Failure.newBuilder().build();
    }
}
```

**Location:** `{grpc-module}/src/main/java/com/bitso/{service}/grpc/util/FailureHelper.java`

### Step 1: Determine Version Number

Check existing proto files for versioned services:
- If no versioned service exists → use V2
- If `ServiceV2` exists → use V3
- If `ServiceV3` exists → use V4
- And so on

### Step 2: Create New Versioned Service in a Separate Proto File

Create a **new proto file** (e.g., `account_service_v{N}.proto`). Import the existing proto to reuse shared request/payload types.

```protobuf
// File: account_service_v2.proto (NEW file — separate from previous version)
syntax = "proto3";
package com.bitso.account.v2;

option java_package = "com.bitso.account.v2";
option java_outer_classname = "AccountServiceV2Proto";

import "account_service.proto";

service AccountServiceV2 {
  rpc CreateAccount(original.package.CreateAccountRequest) returns (CreateAccountResponseV2);
  rpc GetAccount(original.package.GetAccountRequest) returns (GetAccountResponseV2);
}

message CreateAccountResponseV2 {
  original.package.Account account = 1;  // No oneof, no Failure
}
```

**Do NOT add the new versioned service or its response messages to an existing proto file.** Shared types (request messages, payload types, enums) remain in the old file and are imported by the new one.

### Step 3: Deprecate Previous Service

Mark the previous service with both `@replacedBy` comments and protobuf `option deprecated = true`.
The comments provide human-readable migration guidance; the options generate `@Deprecated` annotations
in Java/Kotlin/Go stubs, giving gRPC clients **compile-time deprecation warnings**.

```protobuf
/**
 * @deprecated since v{CURRENT}, forRemoval (planned for v{NEXT_MAJOR}).
 * Use AccountServiceV{N} instead.
 */
service AccountService {
  option deprecated = true;

  // @replacedBy: AccountServiceV{N}.CreateAccount
  rpc CreateAccount(CreateAccountRequest) returns (CreateAccountResponse) {
    option deprecated = true;
  }
}

// Deprecated since v{CURRENT}. Planned for removal in v{NEXT_MAJOR}.
// Use CreateAccountResponseV{N} instead.
message CreateAccountResponse {
  option deprecated = true;

  oneof result {
    Account account = 1;
    Failure failure = 2;
  }
}
```

**Deprecation comment format:**
- Service javadoc: `@deprecated since v{CURRENT}, forRemoval (planned for v{NEXT_MAJOR}).`
- Message/RPC line comments: `Deprecated since v{CURRENT}. Planned for removal in v{NEXT_MAJOR}.`
- `{CURRENT}` = the version where the new service is introduced (from `gradle.properties`)
- `{NEXT_MAJOR}` = the next MAJOR version bump, when the old service will be deleted

**What to deprecate:**

| Element | When to deprecate |
|---------|-------------------|
| `service` | Always - the entire service is superseded by V{N} |
| `rpc` methods | Always - each method has a V{N} replacement |
| Request/response messages | Only if V{N} uses new message types (e.g., flattened contract) |
| Shared types (enums, nested messages) | Never - if reused by V{N} service, keep them non-deprecated |

### Step 4: Compile Protos

```bash
./gradlew generateProto
# OR
./gradlew :module-protos-generated:generateProto
```

### Step 5: Implement Java Handler

Create V{N} handler class extending generated base class. See `references/ERROR_PATTERNS.md` for implementation patterns.

### Step 6: Register New Versioned Service

Register the new handler in your gRPC server configuration.

### Step 7: MANDATORY - Bump MAJOR Version in Protobuf Module

**This step is NON-NEGOTIABLE.** Adding versioned services is a BREAKING CHANGE.

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
- Adding V2 services changes the error-handling contract
- Consumers must update their code to handle `StatusRuntimeException` instead of `Failure` entity
- Deprecating existing services signals a migration requirement
- Semantic versioning REQUIRES major bump for breaking changes

**Failure to bump MAJOR version will cause:**
- Consumers unaware they need to migrate to new error-handling
- Silent compatibility issues when consumers upgrade
- Production incidents due to unexpected error-handling behavior
- RFC-33 compliance violations

## Migration Checklist

### Pre-Implementation
- [ ] **Determine the correct version number** (check for existing V2, V3, etc.)
- [ ] **Create FailureHelper utility class** in grpc module if not exists
- [ ] Determine correct package naming for project (check existing Java packages)

### Proto Changes
- [ ] **Create a NEW proto file** (e.g., `service_v{N}.proto`) — do NOT add to an existing file
- [ ] Import the previous version's proto file to reuse shared types
- [ ] Create new versioned service (e.g., `AccountServiceV{N}`)
- [ ] **Use project-specific package for NEW protos** (NOT `protos.model`)
- [ ] Move methods to new versioned service (no version suffix on method names)
- [ ] Annotate previous methods with `@replacedBy: ServiceNameV{N}.methodName`
- [ ] Add `option deprecated = true` to old service, RPC methods, and superseded messages
- [ ] Add deprecation timeline comments (`since v{CURRENT}, forRemoval planned for v{NEXT_MAJOR}`)
- [ ] Remove `Failure` entity from new versioned response messages
- [ ] Remove `oneof` patterns in new versioned service responses
- [ ] Create new response messages without Failure (e.g., `AccountResponseV{N}`)
- [ ] Define error code enumerations for all business errors
- [ ] Place proto files in correct directory matching package structure

### Implementation
- [ ] Compile protos to generate Java stubs: `./gradlew generateProto`
- [ ] Implement Java handlers for new versioned service
- [ ] **Use FailureHelper utility** for error responses (not inline implementations)
- [ ] Implement standard gRPC Status codes (INTERNAL, UNKNOWN, FAILED_PRECONDITION)
- [ ] Use `google.rpc.Status` with `addDetails()` for business errors

### Version Management (MANDATORY)

- [ ] **CRITICAL: Bump MAJOR version** in **protobuf module's** `gradle.properties` (e.g., 1.2.3 → 2.0.0)
- [ ] Location: `{proto-module}/gradle.properties`
- [ ] **DO NOT proceed to PR creation without completing this step**

### Removal Planning
- [ ] Document the planned removal version in proto comments (next MAJOR version)
- [ ] Plan to delete old service, RPC methods, and superseded messages in the next MAJOR version bump
- [ ] When the next MAJOR version is released: remove deprecated service, handler, and messages from proto files

### Build Verification (MANDATORY)
- [ ] **Run `./gradlew clean build`**
- [ ] **Fix ALL compilation errors before proceeding**
- [ ] **Loop: build → fix → build until SUCCESS**

### Validation
- [ ] Verify errors appear in distributed tracing (not all marked as OK)
- [ ] Run `grpc-compliance-validate-repository --dir .`
- [ ] Run tests: `./gradlew test`
- [ ] **⚠️ Only create PR after build passes**

## Common Pitfalls

### Creating Versioned Methods Instead of Versioned Services

- **Correct**: Create `TransferServiceV{N}` with clean method names
- **Incorrect**: Don't add `transferV{N}()` to an existing service

### Placing New Version Definitions in the Same File as Previous Version

- **Correct**: Create a separate `*_v{N}.proto` file for the new versioned service and its response messages
- **Incorrect**: Adding the new versioned service/messages to the existing proto file
- If multiple versions share one proto file, removing an old version requires careful, targeted edits; if each version lives in its own `*_v{N}.proto` file, removing an old version is as simple as deleting that file
- The new version inherits the old file's package, preventing use of a project-specific package if the old file uses a generic one

### Using Generic `protos.model` Package for NEW Versioned Services

- **Correct**: Use project-specific package like `com.bitso.iba.rate.v2`
- **Incorrect**: Using `package protos.model;` for NEW versioned services
- **Note**: For EXISTING protos being moved, keep their original package

### Not Checking for Existing Versioned Services

- **Required**: Check if V2, V3, etc. already exist before choosing a version number
- **Incorrect**: Blindly creating V2 when the service already has a V2 — this causes naming conflicts

### Not Creating FailureHelper Utility Class

- **Required**: Create `FailureHelper` in grpc module BEFORE implementing handlers
- **Incorrect**: Inline error handling code in each service (duplicated boilerplate)
- Location: `{grpc-module}/src/main/java/com/bitso/{service}/grpc/util/FailureHelper.java`

### Forgetting to Compile Protos Before Implementing Handlers

- Always run `./gradlew generateProto` after proto changes
- Then implement Java handlers using generated stubs

### Creating PR with Broken Build

- **MANDATORY**: Run `./gradlew clean build` before creating PR
- **Fix ALL errors** before proceeding - loop until build passes
- Common issues: missing imports, wrong dependencies, proto not regenerated

### Only Using Comments for Deprecation (Missing `option deprecated`)

- **Correct**: Add both `@replacedBy` comments AND `option deprecated = true` to old service, RPCs, and superseded messages
- **Incorrect**: Only adding javadoc `@deprecated` comments without the protobuf option
- Comments are invisible to generated stubs - clients won't see compile-time warnings
- The `option deprecated = true` generates `@Deprecated` in Java/Kotlin/Go, providing programmatic deprecation signals

### Not Bumping MAJOR Version in Protobuf Module (CRITICAL ERROR)

- **gRPC migration is ALWAYS a breaking change** - MAJOR version bump is mandatory
- Adding new versioned services changes the error handling contract
- Consumers must update their code to handle new error patterns
- Using MINOR/PATCH violates semantic versioning and causes silent breakage
- **This mistake causes production incidents in downstream services**

## Consumer Migration Guide

```java
// Before: Using original service with oneof Failure
TransferServiceGrpc.TransferServiceBlockingStub stub =
    TransferServiceGrpc.newBlockingStub(channel);

try {
  TransferResponse response = stub.transfer(request);
  if (response.hasFailure()) {
    // Handle failure
  }
} catch (Exception e) {
  // Handle error
}

// After: Using new versioned service with StatusRuntimeException
TransferServiceV2Grpc.TransferServiceV2BlockingStub stubV2 =
    TransferServiceV2Grpc.newBlockingStub(channel);

try {
  TransferResponseV2 response = stubV2.transfer(request);  // Same method name
  // Process success
} catch (StatusRuntimeException e) {
  Status.Code code = e.getStatus().getCode();
  if (code == Status.Code.FAILED_PRECONDITION) {
    // Extract business error details from google.rpc.Status
    com.google.rpc.Status status = StatusProto.fromThrowable(e);
    if (status != null && !status.getDetailsList().isEmpty()) {
      for (Any detail : status.getDetailsList()) {
        if (detail.is(TransferError.class)) {
          TransferError error = detail.unpack(TransferError.class);
          // Handle business error
        }
      }
    }
  }
}
```

## References

- `references/ERROR_PATTERNS.md` - Detailed error handling implementation patterns
- RFC-33: gRPC Service Standards
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration-error-handling/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

