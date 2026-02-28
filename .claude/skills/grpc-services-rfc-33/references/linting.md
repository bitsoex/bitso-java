# Protobuf Linting

When creating protobuf, run the linting process afterwards. If any linting errors are found, fix them based on the tool output.

## Contents

- [Tools](#tools)
- [Buf Configuration](#buf-configuration)
- [Custom Rules](#custom-rules)
- [grpc-classpath-linter (Gradle Plugin)](#grpc-classpath-linter-gradle-plugin)

---
## Tools

We use two linters for protobuf and gRPC:
1. **Buf** - Standard protobuf linting (Go executable)
2. **grpc-classpath-linter** - Gradle plugin for classpath validation (`com.bitso:grpc-classpath-linter:1.0.3`)

### Installation

**Cloud agents**: Both tools are pre-installed.

**Local setup** (Homebrew):

```bash
# Buf CLI
brew install bufbuild/buf/buf

# Bitso gRPC Linter (includes custom buf plugins)
export HOMEBREW_GITHUB_API_TOKEN=your-token
brew tap bitsoex/homebrew-bitso
brew install bitso-grpc-linter
```

See [installation.md](installation.md) for full details, Docker alternative, and troubleshooting.

## Buf Configuration

Create `buf.yaml` in your repository:

```yaml
version: v2
lint:
  use:
    - DIRECTORY_SAME_PACKAGE
    - PACKAGE_DEFINED
    - PACKAGE_DIRECTORY_MATCH
    - PACKAGE_SAME_DIRECTORY
    - COMMENT_FIELD
    - COMMENT_MESSAGE
    - COMMENT_SERVICE
    - RPC_PASCAL_CASE
    - SERVICE_PASCAL_CASE
    - MESSAGE_PASCAL_CASE
    - IMPORT_USED
    - FIELD_LOWER_SNAKE_CASE
    - ENUM_VALUE_UPPER_SNAKE_CASE
    - ENUM_PASCAL_CASE
    - ENUM_FIRST_VALUE_ZERO
    - ENUM_NO_ALLOW_ALIAS
    - RPC_REQUEST_RESPONSE_UNIQUE
    - FILE_LOWER_SNAKE_CASE
    - ENUM_VALUE_PREFIX
    - ENUM_ZERO_VALUE_SUFFIX
    # Custom linting rules
    - NO_FAILURE_USAGE_IN_RESPONSE
    - MINIMAL_RPC_DOCUMENTATION
    - NESTED_PAYLOAD_IN_REQUESTS_AND_RESPONSES
  ignore:
    # Ignore third-party protobufs
    - ./bitso-libs/shared/reconciliation-engine-core/protobuf/build/resources/main/data_commons.proto
modules:
  - path: ./bitso-libs/shared/reconciliation-engine-core/protobuf/build/resources/main
plugins:
  - plugin: buf-plugin-rpc-minimal-documentation
  - plugin: buf-plugin-no-failure-usage
  - plugin: buf-plugin-nested-payload-request-response
```

## Custom Rules

### RPC Minimal Documentation

Validates minimal documentation of an `rpc`. Expected format:

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
```

### No Failure Usage

Validates that failure is not used in responses to transit errors:

```protobuf
// ❌ INVALID - Do not use Failure in response
message ApiResponse {
  oneof response {
    protos.model.Failure failure = 1;
    int64 result = 2;
  }
}

// ✅ VALID - Use gRPC status codes and metadata for errors
```

### Nested Payload in Requests and Responses

Validates that all request/response messages have a nested `Payload` message for the actual payload.

## grpc-classpath-linter (Gradle Plugin)

### Setup

Add to `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven {
            name 'BitsoPackages'
            url 'https://maven.pkg.github.com/bitsoex/artifacts'
            credentials {
                username = bitsoGHRepoUser
                password = bitsoGHRepoPassword
            }
        }
        gradlePluginPortal()
    }

    plugins {
        id 'bitso.grpc-classpath-linter' version "1.0.3"
    }
}

plugins {
    id 'bitso.grpc-classpath-linter'
}
```

### Tasks

- `lintGrpcClasspath` - For services and jobs (under `bitso-services` and `bitso-jobs`)
- `lintProtobufClasspath` - For modules with `.proto` files

### Configuration

Environment variables:
- `IS_CI` - When `true`, generates output files
- `IS_DRY_RUN` - When `true`, only logs problems without failing

### lintGrpcClasspath Rules

#### GRPC_STARTER_IN_PROJECTS_WITH_GRPC

Checks that all projects with gRPC dependencies have `grpc-spring-boot-starter`.

#### ONLY_NETTY_SHADED_IN_CLASSPATH

Ensures only `netty-shaded` transport is in the classpath.

#### SERVICES_WITH_GRPC_CLIENT_STARTER_MUST_HAVE_GRPC_CLIENTS_WITH_RESILIENCY

Validates that services with `grpc-client-spring-boot-starter`:
- Have configurations under `grpc.client` property
- Have `grpc-resiliency-starter` dependency
- Have default deadline set for all clients

```yaml
grpc:
  client:
    my-service:
      address: in-process:test
      service-config:
        method-config:
          - name: []
            timeout: PT1S  # Required default deadline
```

#### SERVICES_WITH_GRPC_SERVER_STARTER_MUST_HAVE_GRPC_SERVER_PORT_8201

Ensures `grpc.server.port` is set to `8201` for gRPC servers.

### lintProtobufClasspath Rules

#### PROTOBUF_LIBRARIES_SHOULDNT_HAVE_BUILDED_CLASSES

Validates that modules with `.proto` files only export proto definitions, not generated classes.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-services-rfc-33/references/linting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

