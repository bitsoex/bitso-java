# Shared Templates for gRPC Migration

## Contents

- [Commit Message Template](#commit-message-template)
- [PR Title Format](#pr-title-format)
- [PR Description Template](#pr-description-template)
- [Assessment-Only Commit Template](#assessment-only-commit-template)
- [Contract Distribution Only Commit Template](#contract-distribution-only-commit-template)
- [Error Handling Only Commit Template](#error-handling-only-commit-template)

---
## Commit Message Template

```bash
git commit -m "$(cat <<'EOF'
chore: Migrate [module] to standard gRPC patterns

Error Handling:
- Create V2 services (original service name UNCHANGED - never rename to V1)
- Use project-specific package for NEW V2 protos (not protos.model)
- Migrate methods to V2 (no V2 suffix on method names)
- Remove Failure entity from V2 responses
- Create FailureHelper utility class in grpc module
- Implement google.rpc.Status for business errors
- Add @replacedby ServiceNameV2.methodName comments
- Implement Java handlers for V2 services using FailureHelper

Contract Distribution:
- Move proto files to src/main/resources/{package/path}/
- Split into proto-only and -generated modules
- Remove compilation from proto-only artifact
- Update N internal references
- Ensure only proto-only module is published
- Bump MAJOR version (breaking change)

Validation:
- grpc-compliance-validate-repository passes (ZERO errors)
- Full build and tests successful

Breaking change: External consumers need migration
EOF
)"
```

## PR Title Format

```
chore: Migrate [service/module] to standard gRPC error handling and contract distribution
```

## PR Description Template

```markdown
## Summary
- Standard gRPC error handling (removes Failure entity)
- Proper contract distribution (proto-only artifacts)

## Error Handling Migration
- Created V2 services: [list service names]
- Used project-specific package for new V2 protos (not `protos.model`)
- Created FailureHelper utility class in grpc module
- Migrated methods without V2 suffix
- Marked original methods with @replacedby
- Implemented standard gRPC Status codes
- Added business error details via google.rpc.Status
- Implemented Java handlers for V2 services using FailureHelper

## Contract Distribution Migration
- Split modules: [list module names]
- Moved proto files to `src/main/resources/{package/path}/`
- Updated N internal references
- Bumped MAJOR version: [old] → [new]

## Breaking Changes
External consumers must:
- Migrate to V2 services for new error handling
- Compile protos themselves (no compiled code in artifacts)

## Migration Guide for Consumers

### Service Migration
```java
// Before: Using original service with oneOf Failure
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

// After: Using V2 service with StatusRuntimeException
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

### Proto Compilation Setup
```gradle
plugins {
    id 'com.google.protobuf' version '0.9.4'
}

dependencies {
    implementation 'com.bitso.platform:account-protos:2.0.0'
    implementation 'io.grpc:grpc-stub:1.60.0'
    implementation 'io.grpc:grpc-protobuf:1.60.0'
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.0" }
    plugins { grpc { artifact = "io.grpc:protoc-gen-grpc-java:1.60.0" } }
    generateProtoTasks { all()*.plugins { grpc {} } }
}
```

## Validation Checklist
- [ ] `grpc-compliance-validate-repository --dir .` passes (ZERO errors)
- [ ] **⚠️ `./gradlew clean build` succeeds (MANDATORY - fix ALL errors before PR)**
- [ ] `./gradlew test` passes
- [ ] **⚠️ Original service names unchanged (no retroactive V1 suffixing)**
- [ ] FailureHelper utility class created in grpc module
- [ ] External consumers notified of breaking changes
```

## Assessment-Only Commit Template

```bash
git commit -m "$(cat <<'EOF'
docs: Add gRPC migration assessment for [module/service]

Assessment results:
- N proto modules requiring contract distribution migration
- N services requiring V2 error handling migration
- Prioritized migration order documented

No code changes - assessment only.
EOF
)"
```

## Contract Distribution Only Commit Template

```bash
git commit -m "$(cat <<'EOF'
chore: Migrate [module] to proper contract distribution

Changes:
- Move proto files to src/main/resources/{package/path}/
- Split into proto-only and -generated modules
- Remove protobuf plugin from proto-only module
- Update N internal references
- Bump MAJOR version: [old] → [new]

Breaking change: External consumers must compile protos themselves
EOF
)"
```

## Error Handling Only Commit Template

```bash
git commit -m "$(cat <<'EOF'
chore: Migrate [service] to standard gRPC error handling

Changes:
- Create [ServiceName]V2 service (original [ServiceName] remains UNCHANGED)
- Create FailureHelper utility class in grpc module
- Move methods to V2 (no V2 suffix on method names)
- Remove Failure entity from V2 responses
- Implement google.rpc.Status with business error details
- Mark original methods with @replacedby
- Implement Java handlers for V2 service using FailureHelper

⚠️ Original service name preserved - NOT renamed to V1

Build verified: ./gradlew clean build passes

Breaking change: Consumers should migrate to V2 services
EOF
)"
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration/references/SHARED_TEMPLATES.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

