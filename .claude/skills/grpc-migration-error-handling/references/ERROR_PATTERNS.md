# Error Handling Implementation Patterns

## Contents

- [Pattern 1: Using google.rpc.Status with Details (Recommended)](#pattern-1-using-googlerpcstatus-with-details-recommended)
- [Pattern 2: Using Metadata (Simple Approach)](#pattern-2-using-metadata-simple-approach)
- [Pattern 3: Using FailureHelper Utility (REQUIRED for New Services)](#pattern-3-using-failurehelper-utility-required-for-new-services)
- [Error Extraction on Client Side](#error-extraction-on-client-side)
- [Error Code Mapping Reference](#error-code-mapping-reference)
- [Proto Error Message Design](#proto-error-message-design)

---
## Pattern 1: Using google.rpc.Status with Details (Recommended)

This is the recommended pattern for most use cases. It provides rich error details using `google.rpc.Status`.

```java
import com.google.rpc.Status;
import com.google.rpc.Code;
import com.google.protobuf.Any;
import io.grpc.protobuf.StatusProto;

public class AccountServiceV2Handler extends AccountServiceV2Grpc.AccountServiceV2ImplBase {

  @Override
  public void createAccount(CreateAccountRequest request,
                           StreamObserver<CreateAccountResponse> responseObserver) {
    try {
      // Business validation
      if (!isValidEmail(request.getEmail())) {
        AccountError error = AccountError.newBuilder()
          .setCode(AccountErrorCode.INVALID_EMAIL)
          .setMessage("Email format is invalid")
          .setField("email")
          .build();

        Status status = Status.newBuilder()
          .setCode(Code.FAILED_PRECONDITION.getNumber())
          .setMessage("Account validation failed")
          .addDetails(Any.pack(error))
          .build();

        responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        return;
      }

      // Success case
      Account account = accountService.create(request);
      CreateAccountResponse response = CreateAccountResponse.newBuilder()
        .setAccount(account)
        .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (DependencyUnavailableException e) {
      // Infrastructure error
      Status status = Status.newBuilder()
        .setCode(Code.INTERNAL.getNumber())
        .setMessage("Database unavailable")
        .build();

      responseObserver.onError(StatusProto.toStatusRuntimeException(status));
    }
  }
}
```

### When to Use
- Default choice for new versioned service implementations
- When you need to include structured business error details
- When consumers need to programmatically handle specific error types

### Key Points
- Use `com.google.rpc.Status` (not `io.grpc.Status`)
- Use `Code.FAILED_PRECONDITION` for business errors
- Use `Code.INTERNAL` for infrastructure errors
- Pack business error details using `Any.pack()`

## Pattern 2: Using Metadata (Simple Approach)

A simpler pattern when you don't need the full `google.rpc.Status` structure.

```java
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;

public class TransferServiceV2Handler extends TransferServiceV2Grpc.TransferServiceV2ImplBase {

  private static final Metadata.Key<TransferError> ERROR_KEY =
      ProtoUtils.keyForProto(TransferError.getDefaultInstance());

  @Override
  public void transfer(TransferRequest request,
                      StreamObserver<TransferResponse> responseObserver) {
    if (request.getAmount() <= 0) {
      TransferError error = TransferError.newBuilder()
        .setCode(TransferErrorCode.INVALID_AMOUNT)
        .setMessage("Amount must be positive")
        .build();

      Metadata metadata = new Metadata();
      metadata.put(ERROR_KEY, error);

      responseObserver.onError(
        Status.FAILED_PRECONDITION
          .withDescription("Invalid transfer amount")
          .asRuntimeException(metadata)
      );
      return;
    }

    // Success case
    Transfer transfer = transferService.execute(request);
    TransferResponse response = TransferResponse.newBuilder()
      .setTransfer(transfer)
      .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
```

### When to Use
- Simpler error handling needs
- When you only have one error type per method
- When consumers extract errors from metadata

### Key Points
- Uses `io.grpc.Status` directly
- Error details in metadata (trailers)
- Requires defining `Metadata.Key` for each error type

## Pattern 3: Using FailureHelper Utility (REQUIRED for New Services)

**⚠️ MANDATORY: Create this utility class in your grpc module before implementing new versioned services**

If `com.bitso.commons:grpc` FailureHelper is not available, create a local version in your grpc module:

### Step 1: Create FailureHelper in grpc module

```java
// Location: {grpc-module}/src/main/java/com/bitso/{service}/grpc/util/FailureHelper.java

package com.bitso.{service}.grpc.util;

import com.bitso.commons.protobuf.DataCommonsProto;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.lite.ProtoLiteUtils;

/**
 * Utility class for consistent gRPC error handling across all new versioned services.
 * Provides standardized error creation and extraction methods.
 */
public class FailureHelper {

    private static final Metadata.Key<DataCommonsProto.Failure> FAILURE_DETAILS_KEY =
        Metadata.Key.of(
            "bitso-failure-detail-bin",
            ProtoLiteUtils.metadataMarshaller(DataCommonsProto.Failure.getDefaultInstance()));

    /**
     * Creates a StatusRuntimeException with failure details in metadata.
     * Use this for business errors (FAILED_PRECONDITION) and infrastructure errors (INTERNAL).
     *
     * @param code The gRPC status code (INTERNAL, FAILED_PRECONDITION, UNKNOWN)
     * @param failure The failure details to include in metadata
     * @return StatusRuntimeException ready to be thrown via responseObserver.onError()
     */
    public static StatusRuntimeException createStatusRuntimeException(
            Status.Code code, DataCommonsProto.Failure failure) {
        Metadata metadata = new Metadata();
        metadata.put(FAILURE_DETAILS_KEY, failure);
        return code.toStatus().withDescription(failure.getCode()).asRuntimeException(metadata);
    }

    /**
     * Extracts failure details from a throwable (typically StatusRuntimeException).
     * Use this on the client side to get structured error information.
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

### Step 2: Use FailureHelper in New Versioned Service Handlers

```java
import com.bitso.{service}.grpc.util.FailureHelper;
import com.bitso.commons.protobuf.DataCommonsProto;

public class PaymentServiceV2Handler extends PaymentServiceV2Grpc.PaymentServiceV2ImplBase {

  @Override
  public void processPayment(PaymentRequest request,
                            StreamObserver<PaymentResponse> responseObserver) {
    try {
      Payment payment = paymentService.process(request);

      PaymentResponse response = PaymentResponse.newBuilder()
        .setPayment(payment)
        .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (BusinessException e) {
      // Use FailureHelper for consistent error handling
      DataCommonsProto.Failure failure = DataCommonsProto.Failure.newBuilder()
        .setCode(e.getErrorCode())
        .setReason(e.getMessage())
        .build();

      responseObserver.onError(
        FailureHelper.createStatusRuntimeException(Status.Code.FAILED_PRECONDITION, failure)
      );
    } catch (Exception e) {
      // Infrastructure errors
      DataCommonsProto.Failure failure = DataCommonsProto.Failure.newBuilder()
        .setCode("INTERNAL_ERROR")
        .setReason("An unexpected error occurred")
        .build();

      responseObserver.onError(
        FailureHelper.createStatusRuntimeException(Status.Code.INTERNAL, failure)
      );
    }
  }
}
```

### When to Use
- **ALWAYS** for new versioned service implementations
- Provides consistent error handling across all services
- Enables clients to extract structured error details

### Key Points
- **Create FailureHelper BEFORE implementing new versioned handlers** - don't inline error handling code
- Uses metadata trailers for error details (not google.rpc.Status details)
- Consistent key name `bitso-failure-detail-bin` across all services
- Clients use `extractFailure()` to get structured error information

### Client-Side Error Extraction

```java
try {
  PaymentResponse response = stub.processPayment(request);
} catch (StatusRuntimeException e) {
  DataCommonsProto.Failure failure = FailureHelper.extractFailure(e);
  if (!failure.getCode().isEmpty()) {
    // Handle business error with structured details
    log.error("Business error: {} - {}", failure.getCode(), failure.getReason());
  } else {
    // Handle infrastructure error
    log.error("Infrastructure error: {}", e.getStatus().getDescription());
  }
}
```

## Error Extraction on Client Side

### Extracting from google.rpc.Status

```java
try {
  AccountResponse response = stub.createAccount(request);
  // Process success
} catch (StatusRuntimeException e) {
  Status.Code code = e.getStatus().getCode();

  switch (code) {
    case FAILED_PRECONDITION:
      // Business error - extract details
      com.google.rpc.Status status = StatusProto.fromThrowable(e);
      if (status != null) {
        for (Any detail : status.getDetailsList()) {
          if (detail.is(AccountError.class)) {
            AccountError error = detail.unpack(AccountError.class);
            handleBusinessError(error);
          }
        }
      }
      break;

    case INTERNAL:
      // Infrastructure error
      handleInfrastructureError(e);
      break;

    case UNKNOWN:
      // Unknown downstream error
      handleUnknownError(e);
      break;

    default:
      throw e;
  }
}
```

### Extracting from Metadata

```java
private static final Metadata.Key<TransferError> ERROR_KEY =
    ProtoUtils.keyForProto(TransferError.getDefaultInstance());

try {
  TransferResponse response = stub.transfer(request);
} catch (StatusRuntimeException e) {
  Metadata trailers = Status.trailersFromThrowable(e);
  if (trailers != null) {
    TransferError error = trailers.get(ERROR_KEY);
    if (error != null) {
      handleTransferError(error);
    }
  }
}
```

## Error Code Mapping Reference

| Business Scenario | gRPC Code | Details |
|------------------|-----------|---------|
| Validation failure | `FAILED_PRECONDITION` | Include field-level errors |
| Business rule violation | `FAILED_PRECONDITION` | Include business error code |
| Resource not found | `FAILED_PRECONDITION` | Include resource identifier |
| Database error | `INTERNAL` | Log details, generic message |
| External service failure | `INTERNAL` or `UNKNOWN` | Depends on error type |
| Timeout | `INTERNAL` | Include operation context |

## Proto Error Message Design

```protobuf
// Define error codes as enum
enum AccountErrorCode {
  ACCOUNT_ERROR_UNSPECIFIED = 0;
  INVALID_EMAIL = 1;
  DUPLICATE_ACCOUNT = 2;
  ACCOUNT_SUSPENDED = 3;
}

// Define error message with all relevant fields
message AccountError {
  AccountErrorCode code = 1;
  string message = 2;
  string field = 3;           // Optional: which field caused error
  map<string, string> metadata = 4;  // Optional: additional context
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration-error-handling/references/ERROR_PATTERNS.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

