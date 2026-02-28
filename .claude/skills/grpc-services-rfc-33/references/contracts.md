# gRPC Protobuf Contracts

## Contents

- [Service Naming](#service-naming)
- [Message Design](#message-design)
- [Documentation Requirements](#documentation-requirements)
- [Versioning](#versioning)
- [Contract Exposure](#contract-exposure)
- [Code Generation](#code-generation)
- [Server Implementation](#server-implementation)
- [Server Configuration](#server-configuration)
- [Client Implementation](#client-implementation)

---

## Service Naming

- Service naming pattern: `{domain-name}{subdomain-name}{version}` (e.g., `UserProfileServiceV1`, `UserKYCServiceV1`)
- Proto file naming: use snake_case matching service name (e.g., `user_profile_service_v1.proto`)

## Message Design

- Each operation must have its own request/response message definitions
- Name request/response messages as `{OperationName}Request` and `{OperationName}Response`
- Use nested `Payload` message in requests/responses for business data
- Define an enum for all possible business error codes

## Documentation Requirements

Document the gRPC service with a brief description above the `service` definition.

Document every `rpc` method with:
- Intent of the endpoint
- Specificities (idempotency, etc.)
- Possible business error codes
- Possible gRPC status codes
- Indication of which errors are retryable

### Example RPC Documentation

```protobuf
/**
  Create a withdrawal.
  This is an idempotent API, and clients must provide an idempotency key.
  
  Possible error codes for this API are:
  | Error Code                     | gRPC Status Code    | Description                            | Retryable |
  | ------------------------------ | ------------------- | -------------------------------------- | --------- |
  | MY_SERVICE_USER_NOT_FOUND      | FAILED_PRECONDITION | The user_id provided does not exist    | No        |
  | MY_SERVICE_TAXONOMY_DISABLED   | FAILED_PRECONDITION | The taxonomy of the user is disabled   | No        |
*/
rpc CreateWithdrawal(CreateWithdrawalRequest) returns (CreateWithdrawalResponse);
```

## Versioning

- Implement versioning at the package level
- Initial version must be named as `v1`
- Include version in service name
- Maintain backward compatibility
- For breaking changes, create a new package and service with new version

## Contract Exposure

- Expose only proto definitions to clients
- Publish library containing protobuf definitions in resources folder
- Path in resources should match package in protobuf contract
- Protobuf library must be under `bitso-libs/{subdomain}/protobuf`
- Artifact name must be `<subdomain>-protobuf`

### Proto Module build.gradle

```groovy
plugins {
    id 'bitso.publish'
}
```

### Proto Module gradle.properties

```properties
groupId=com.bitso.{domain}
artifactId={sub-domain}-protobuf
version=1.0.0
```

## Code Generation

Configure code generation with the protobuf Gradle plugin:

### gradle/protobuf.gradle

```groovy
apply plugin: 'com.google.protobuf'
apply plugin: 'java-library'

tasks.withType(Jar) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    api libs.grpc.protobuf
    api libs.grpc.api
    api libs.grpc.stub
    api libs.protobuf.java
    protobuf libs.bitso.commons.protobuf
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs('build/generated/source/proto/main/java')
            srcDirs('build/generated/source/proto/main/grpc')
        }
    }
}
```

## Server Implementation

- Use `com.bitso.commons:data:{version}` library for the Failure class
- The failure proto is located in `com.bitso.commons:data-commons-protobuf:{version}`
- Send errors in metadata using `FailureHelper.createStatusRuntimeException`
- Use enum names (not values) for error codes with `YourEnum.YOUR_VALUE.name()`
- Add environment variable `DD_GRPC_SERVER_ERROR_STATUSES: 2-8,10-16`
- Handle request cancellation using `io.grpc.Context.isCancelled()`

### FailureHelper Implementation

```java
public class FailureHelper {
    private static final Metadata.Key<DataCommonsProto.Failure> FAILURE_DETAILS_KEY =
        ProtoLiteUtils.metadataMarshaller(DataCommonsProto.Failure.getDefaultInstance());

    public static StatusRuntimeException createStatusRuntimeException(
            Status.Code code, DataCommonsProto.Failure failure) {
        Metadata metadata = new Metadata();
        metadata.put(FAILURE_DETAILS_KEY, failure);
        return code.toStatus().withDescription(failure.getCode()).asRuntimeException(metadata);
    }

    public static DataCommonsProto.Failure extractFailure(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException sre) {
            if (sre.getTrailers() != null && sre.getTrailers().get(FAILURE_DETAILS_KEY) != null) {
                return sre.getTrailers().get(FAILURE_DETAILS_KEY);
            }
        }
        return DataCommonsProto.Failure.newBuilder().build();
    }
}
```

## Server Configuration

- Use `grpc-spring-boot-starter` library
- Annotate handlers with `@GrpcService`
- Use port 8201 as default
- Implement gRPC Health Check Service integrated with `spring-boot-actuator`

## Client Implementation

- Generate protobuf code from published proto definitions
- Extract failures from errors using `FailureHelper.extractFailure`
- Instantiate stubs using `@GrpcClientBean` annotation
- Configure client properties in `application.yml`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-services-rfc-33/references/contracts.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

