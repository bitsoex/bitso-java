# API Response Patterns

Detailed patterns for RFC-39 compliant API responses.

## Contents

- [Complete Response Classes](#complete-response-classes)
- [ResponseFactory](#responsefactory)
- [Global Exception Handler](#global-exception-handler)
- [Pagination Examples](#pagination-examples)

---
## Complete Response Classes

### ApiResponse

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final ApiError error;
    private final ApiMeta meta;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .meta(ApiMeta.now())
            .build();
    }
    
    public static <T> ApiResponse<T> success(T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .meta(ApiMeta.withPagination(pagination))
            .build();
    }
    
    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
            .success(false)
            .error(ApiError.of(code, message))
            .meta(ApiMeta.now())
            .build();
    }
    
    public static ApiResponse<Void> validationError(List<FieldError> details) {
        return ApiResponse.<Void>builder()
            .success(false)
            .error(ApiError.validation(details))
            .meta(ApiMeta.now())
            .build();
    }
}
```

### ApiError

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    
    private final String code;
    private final String message;
    private final List<FieldError> details;
    
    public static ApiError of(String code, String message) {
        return ApiError.builder()
            .code(code)
            .message(message)
            .build();
    }
    
    public static ApiError validation(List<FieldError> details) {
        return ApiError.builder()
            .code("VALIDATION_ERROR")
            .message("Invalid request parameters")
            .details(details)
            .build();
    }
}

@Data
@AllArgsConstructor
public class FieldError {
    private final String field;
    private final String message;
}
```

### ApiMeta

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMeta {
    
    private final String requestId;
    private final Instant timestamp;
    private final PaginationMeta pagination;
    
    public static ApiMeta now() {
        return ApiMeta.builder()
            .requestId(MDC.get("requestId"))
            .timestamp(Instant.now())
            .build();
    }
    
    public static ApiMeta withPagination(PaginationMeta pagination) {
        return ApiMeta.builder()
            .requestId(MDC.get("requestId"))
            .timestamp(Instant.now())
            .pagination(pagination)
            .build();
    }
}

@Data
@Builder
public class PaginationMeta {
    
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
    
    public static PaginationMeta from(Page<?> page) {
        return PaginationMeta.builder()
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
```

## ResponseFactory

```java
@Component
@RequiredArgsConstructor
public class ResponseFactory {
    
    public <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    public <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(data));
    }
    
    public ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity.noContent().build();
    }
    
    public <T> ResponseEntity<ApiResponse<List<T>>> paginated(Page<T> page) {
        PaginationMeta pagination = PaginationMeta.from(page);
        return ResponseEntity.ok(
            ApiResponse.success(page.getContent(), pagination)
        );
    }
    
    public ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("NOT_FOUND", message));
    }
    
    public ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("BAD_REQUEST", message));
    }
}
```

## Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {
        List<FieldError> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.validationError(details));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {
        List<FieldError> details = ex.getConstraintViolations()
            .stream()
            .map(v -> new FieldError(
                v.getPropertyPath().toString(),
                v.getMessage()))
            .toList();
        
        return ResponseEntity.badRequest()
            .body(ApiResponse.validationError(details));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("FORBIDDEN", "Access denied"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex,
            WebRequest request) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

## Pagination Examples

### Simple Pagination

```java
@GetMapping
public ResponseEntity<ApiResponse<List<OrderDto>>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    Page<OrderDto> orders = orderService.findAll(pageable);
    
    return responseFactory.paginated(orders);
}
```

### With Sorting

```java
@GetMapping
public ResponseEntity<ApiResponse<List<OrderDto>>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort) {
    
    Sort sortSpec = parseSortParam(sort);
    Pageable pageable = PageRequest.of(page, Math.min(size, 100), sortSpec);
    Page<OrderDto> orders = orderService.findAll(pageable);
    
    return responseFactory.paginated(orders);
}

private Sort parseSortParam(String sort) {
    String[] parts = sort.split(",");
    String property = parts[0];
    Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;
    return Sort.by(direction, property);
}
```

### With Filtering

```java
@GetMapping
public ResponseEntity<ApiResponse<List<OrderDto>>> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate toDate) {
    
    OrderFilter filter = OrderFilter.builder()
        .status(status)
        .fromDate(fromDate)
        .toDate(toDate)
        .build();
    
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    Page<OrderDto> orders = orderService.findAll(filter, pageable);
    
    return responseFactory.paginated(orders);
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/api-guidelines-rfc-39/references/response-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

