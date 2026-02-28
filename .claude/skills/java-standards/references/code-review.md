# Java Code Review Standards

When reviewing Java code, follow these standards.

## Contents

- [Java 21 Standards](#java-21-standards)
- [`var` Keyword](#var-keyword)
- [Coding Style](#coding-style)
- [`instanceof` Null Handling](#instanceof-null-handling)
- [Record Usage](#record-usage)
- [Stream API](#stream-api)
- [Optional Usage](#optional-usage)
- [Exception Handling](#exception-handling)

---
## Java 21 Standards

- Review code using Java 21 standards
- Suggest changes using only Java 21 APIs, avoiding Guava

## `var` Keyword

- Allow `var` when assigning a cast `null` value
- Use `var` when the type is obvious from the right-hand side
- Avoid `var` when it reduces code clarity

## Coding Style

- Multi-line expressions should place operators and ternary separators at the end of each line:

```java
// ✅ Correct
String result = condition
    ? "value1"
    : "value2";

String combined = firstValue
    + secondValue
    + thirdValue;

// ❌ Incorrect
String result = condition ?
    "value1" :
    "value2";
```

## `instanceof` Null Handling

- Ensure `instanceof` correctly handles null values (returns false)
- Use pattern matching with instanceof when appropriate:

```java
// ✅ Pattern matching
if (obj instanceof String s) {
    return s.length();
}

// ✅ Null-safe pattern
if (obj instanceof String s && !s.isEmpty()) {
    return s;
}
```

## Record Usage

- Prefer records for immutable data carriers
- Use records for DTOs and value objects

```java
public record UserDto(Long id, String name, String email) {}
```

## Stream API

- Prefer stream operations over imperative loops where it improves readability
- Avoid overly complex stream chains

```java
// ✅ Clear stream usage
var activeUsers = users.stream()
    .filter(User::isActive)
    .map(User::getName)
    .toList();

// ❌ Too complex - consider breaking up
var result = data.stream()
    .filter(x -> x.getValue() > threshold)
    .flatMap(x -> x.getItems().stream())
    .collect(Collectors.groupingBy(
        Item::getCategory,
        Collectors.mapping(Item::getName, Collectors.toSet())));
```

## Optional Usage

- Use Optional for return types that may have no value
- Avoid Optional as method parameters or fields
- Prefer `orElseThrow()` over `get()`

```java
// ✅ Correct
public Optional<User> findById(Long id) { ... }

// ✅ Handling Optional
user.orElseThrow(() -> new NotFoundException("User not found"));

// ❌ Avoid
public void process(Optional<String> value) { ... }
```

## Exception Handling

- Use specific exception types
- Include meaningful messages
- Avoid catching generic Exception unless rethrowing

```java
// ✅ Specific exception
throw new UserNotFoundException("User with id " + id + " not found");

// ❌ Generic
throw new RuntimeException("Error");
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-standards/references/code-review.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

