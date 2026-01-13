---
applyTo: "**/*"
description: Java GRPC Services
---

<!-- https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4236050559/Creating+gRPC+services -->
# gRPC Services Standard

## Protobuf Contract Guidelines

- Service naming pattern: `{domain-name}{subdomain-name}{version}` (e.g., `UserProfileServiceV1`, `UserKYCServiceV1`)
- Proto file naming: use snake_case matching service name (e.g., `user_profile_service_v1.proto`, `user_kyc_service_v1.proto`)
- Each operation must have its own request/response message definitions
- Name request/response messages as `{OperationName}Request` and `{OperationName}Response`
- Use nested `Payload` message in requests/responses for business data
- Define an enum for all possible business error codes, the enum name format is `UserProfileServiceErrorCode`, `UserKYCServiceErrorCode`''
- You should always configure and execute linters when creating protobufs, check [protobuf-linting](.cursor/rules/java-protobuf-linting/RULE.md) for details on the linting process

## Documentation

- Document the gRPC service with a brief description above the `service` definition
- Document every `rpc` method with:
- Intent of the endpoint
- Specificities (idempotency, etc.)
- Possible business error codes
- Possible gRPC status codes
- Indication of which errors are retryable
An example of a valid documentation is
/**
 Create a withdrawal.
 This is an idempotent API, and clients must provide an idempotency key to ensure that the operation is not executed more than once.
 The idempotency key must be unique for each request.
 If the idempotency key is the same as other already processed request, the response of this API will be the same as the first call with this idempotency key.
 Possible error codes for this API are:
 | Error Code                     | gRPC Status Code    | Description                            | Retryable |
 | ------------------------------ | ------------------- | -------------------------------------- | --------- |
 | MY_SERVICE_USER_NOT_FOUND      | FAILED_PRECONDITION | The user_id provided does not exist    | No        |
 | MY_SERVICE_TAXONOMY_DISABLED   | FAILED_PRECONDITION | The taxonomy of the user is disabled   | No        |
 | MY_SERVICE_BLOCKED_FOR_ACCOUNT | FAILED_PRECONDITION | The account of the user is blocked     | No        |
 | MY_SERVICE_INVALID_FIELD       | FAILED_PRECONDITION | Some payload field provided is invalid | No        |

*/

- Document every `message` and its fields with clear descriptions
- For request messages, document field format and validation requirements

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
- Protobuf library must be under `bitso-libs/{subdomain}/protobuf` and it will only contain proto files
- The proto files must be in the resources dir
- Artifact name must be `<subdomain>-protobuf`
- The proto module should not build the protos nor have any dependency, it should just publish the library. The following build.gradle is how the module build.gradle should look like

```groovy
plugins {
 id 'bitso.publish'
}
```

and a `gradle.properties`(this file is only needed for modules that are published) for the module

```properties
groupId=com.bitso.{domain}
artifactId={sub-domain}-protobuf
version=1.0.0
```

## Server Implementation

- Use `com.bitso.commons:data:{version}` library to have access to the Failure class
- The failure proto is located in `com.bitso.commons:data-commons-protobuf:{version}` and should be built locally with the protobuf plugin
- Send errors in metadata using `FailureHelper.createStatusRuntimeException`
- The failure helper should be created if not exists, it should look like this

```java
public class FailureHelper {
 private static final Metadata.Key<DataCommonsProto.Failure> FAILURE_DETAILS_KEY = ProtoLiteUtils.metadataMarshaller(DataCommonsProto.Failure.getDefaultInstance());

 public static StatusRuntimeException createStatusRuntimeException(Status.Code code, DataCommonsProto.Failure failure) {
  Metadata metadata = new Metadata();
  metadata.put(FAILURE_DETAILS_KEY, failure);
  return code.toStatus().withDescription(failure.getCode()).asRuntimeException(metadata);
 }

 public static DataCommonsProto.Failure extractFailure(Throwable throwable) {
  if (throwable instanceof StatusRuntimeException statusRuntimeException) {
   if (statusRuntimeException.getTrailers() != null && statusRuntimeException.getTrailers().get(FAILURE_DETAILS_KEY) != null) {
    return statusRuntimeException.getTrailers().get(FAILURE_DETAILS_KEY);
   }
  }
  return DataCommonsProto.Failure.newBuilder().build();
 }
}
```

- Use enum names (not values) for error codes with `YourEnum.YOUR_VALUE.name()`
- Add environment variable `DD_GRPC_SERVER_ERROR_STATUSES: 2-8,10-16`
- Handle request cancellation using `io.grpc.Context.isCancelled()`
- The server implementation will be placed in `bitso-libs/{subdomain}/grpc` module

## Error Code Usage

- Use only these gRPC error codes:
- `INTERNAL`: Infrastructure errors
- `UNKNOWN`: Only used by gRPC core
- `FAILED_PRECONDITION`: Business errors

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

## Code generation

The generation of protobuf code is done using the protobuf gradle plugin. The configuration for code generation is as follows

### `/gradle/protobuf.gradle` (check for the existence of the file)

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
  artifact = "com.google.protobuf:protoc:${ libs.versions.protobuf.get() }"
 }

 plugins {
  grpc {
   artifact = "io.grpc:protoc-gen-grpc-java:${ libs.versions.grpc.get() }"
  }
 }

 generateProtoTasks {
  all()*.plugins {
   grpc {}
  }
 }
}

idea {
 module {
  sourceDirs += file("${ project.layout.buildDirectory }/generated/source/proto/")
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

clean {
 doFirst {
  delete "${ project.layout.buildDirectory }/generated/source/proto"
 }
}

```

### `/bitso-libs/<subdomain>/build.gradle`

```groovy
plugins {
 // other plugins ...
 alias(libs.plugins.protobuf)
}

apply from: "${ project.rootDir }/gradle/protobuf.gradle"

dependencies {
 protobuf project("bitso-libs:<subdomain>-proto")
}
```

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-grpc-services.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
