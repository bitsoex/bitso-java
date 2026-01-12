# Documentation Standards

This document defines requirements for code documentation including comments, docstrings, and README files.

## When to Document

### Always Document

- Public APIs (classes, methods, functions)
- Complex algorithms or business logic
- Non-obvious code decisions
- Configuration options
- External dependencies and their purpose

### Don't Over-Document

- Self-explanatory code
- Obvious getters/setters
- Boilerplate code
- Implementation details that may change

## Code Comments

### Good Comments

```java
// Retry with exponential backoff to handle transient network failures
for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
    // ...
}
```

```python
# Cache user permissions for 5 minutes to reduce database load
# during high-traffic periods (see PERF-1234 for benchmarks)
@cached(ttl=300)
def get_user_permissions(user_id):
    ...
```

### Bad Comments

```java
// Increment i
i++;

// Get the user
User user = getUser();

// TODO: fix this later
```

## API Documentation

### Java (Javadoc)

```java
/**
 * Processes a payment transaction.
 *
 * <p>This method validates the payment, checks available balance,
 * and initiates the transfer. It is idempotent when called with
 * the same transaction ID.</p>
 *
 * @param request the payment request containing amount and recipient
 * @return the completed payment with transaction ID
 * @throws InsufficientFundsException if the account balance is too low
 * @throws InvalidRecipientException if the recipient account is invalid
 */
public Payment processPayment(PaymentRequest request) {
    // ...
}
```

### Python (Docstrings)

```python
def process_payment(request: PaymentRequest) -> Payment:
    """
    Process a payment transaction.

    This function validates the payment, checks available balance,
    and initiates the transfer. It is idempotent when called with
    the same transaction ID.

    Args:
        request: The payment request containing amount and recipient.

    Returns:
        The completed payment with transaction ID.

    Raises:
        InsufficientFundsError: If the account balance is too low.
        InvalidRecipientError: If the recipient account is invalid.
    """
    ...
```

### TypeScript (TSDoc)

```typescript
/**
 * Processes a payment transaction.
 *
 * This method validates the payment, checks available balance,
 * and initiates the transfer. It is idempotent when called with
 * the same transaction ID.
 *
 * @param request - The payment request containing amount and recipient
 * @returns The completed payment with transaction ID
 * @throws {InsufficientFundsError} If the account balance is too low
 * @throws {InvalidRecipientError} If the recipient account is invalid
 */
async function processPayment(request: PaymentRequest): Promise<Payment> {
  // ...
}
```

## File Headers

Use the template at `assets/templates/file-header-template.txt` for file headers.

### When to Use File Headers

- New source files in projects that require headers
- Files with specific licensing requirements
- Files that need copyright notices

### File Header Content

Include when relevant:

- Copyright notice
- License identifier
- Brief file description
- Author information (if required by project)

## README Files

### Project README

Every project should have a README.md with:

1. **Project name and description**
2. **Quick start** (installation, basic usage)
3. **Prerequisites** (required tools, versions)
4. **Development setup**
5. **Testing instructions**
6. **Deployment** (if applicable)
7. **Contributing guidelines** (or link to CONTRIBUTING.md)

### Module/Package README

For significant modules:

1. **Purpose** of the module
2. **Key classes/functions**
3. **Usage examples**
4. **Dependencies**

## Changelog

Maintain a CHANGELOG.md for versioned projects:

```markdown
# Changelog

## [1.2.0] - 2025-01-09

### Added
- New payment retry mechanism

### Changed
- Updated API response format

### Fixed
- Race condition in concurrent transfers

## [1.1.0] - 2024-12-15
...
```

## Documentation Anti-Patterns

### Avoid

1. **Stale comments**: Update comments when code changes
2. **Commented-out code**: Delete it, use version control
3. **Obvious comments**: `// Constructor` above a constructor
4. **Misleading comments**: Worse than no comments
5. **TODO without context**: Include ticket number or owner

### Examples of Bad Documentation

```java
// Created by John on 2020-03-15
// Modified by Jane on 2020-04-20
// Modified by Bob on 2020-05-01
// This does stuff
public void doStuff() {
    // old implementation
    // return oldMethod();
    
    // new implementation
    return newMethod(); // TODO: fix
}
```
