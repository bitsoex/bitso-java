# Annotation Guide

Complete reference for JSpecify nullability annotations with NullAway.

## Contents

- [Core Annotations](#core-annotations)
- [Type-Use Placement](#type-use-placement)
- [Non-Null (Default)](#non-null-default)
- [@NonNull Annotation](#nonnull-annotation)
- [Initialization Annotations](#initialization-annotations)
- [Contract Annotations](#contract-annotations)
- [Migration from Other Annotations](#migration-from-other-annotations)
- [Best Practices](#best-practices)

---
## Core Annotations

### @NullMarked

Indicates that nullability annotations are meaningful in the annotated scope.

```java
// On package (preferred)
@NullMarked
package com.bitso.service;

// On class
@NullMarked
public class MyService { }

// On method (for specific methods in unmarked code)
@NullMarked
public void process(String input) { }
```

### @NullUnmarked

Opts out of null checking for a scope within @NullMarked code.

```java
// On class
@NullUnmarked
public class LegacyAdapter { }

// On method
@NullUnmarked
public void legacyMethod(Object input) { }
```

### @Nullable

Indicates a type that may be null.

```java
import org.jspecify.annotations.Nullable;

// Parameter
public void process(@Nullable String input) { }

// Return value
public @Nullable User findUser(String id) { }

// Field
private @Nullable Cache cache;

// Type argument
List<@Nullable String> items;

// Array element
String @Nullable [] array;
```

## Type-Use Placement

JSpecify @Nullable is a type-use annotation. Placement matters:

### Parameters

```java
// Nullable parameter
void method(@Nullable String param) { }
```

### Return Types

```java
// Nullable return
@Nullable String getOptionalValue() { }
```

### Fields

```java
// Nullable field
private @Nullable Connection connection;
```

### Generics

```java
// List that can contain null elements
List<@Nullable String> items;

// Nullable list (list itself may be null)
@Nullable List<String> maybeList;

// Both nullable
@Nullable List<@Nullable String> maybeListWithNulls;
```

### Arrays

```java
// Array may be null, elements are non-null
String @Nullable [] nullableArray;

// Array is non-null, elements may be null
@Nullable String[] arrayWithNullableElements;

// Both nullable
@Nullable String @Nullable [] bothNullable;
```

### Nested Types

```java
// Outer may be null
@Nullable Map<String, List<Integer>> nullableMap;

// Values may be null
Map<String, @Nullable List<Integer>> mapWithNullableValues;

// List elements may be null
Map<String, List<@Nullable Integer>> mapWithListsOfNullableInts;
```

## Non-Null (Default)

In @NullMarked code, unannotated types are non-null:

```java
@NullMarked
public class Service {
    private String name;           // Non-null
    private @Nullable String alias; // Nullable

    public String getName() {       // Returns non-null
        return name;
    }

    public @Nullable String getAlias() {  // May return null
        return alias;
    }

    public void setName(String name) {    // Parameter must be non-null
        this.name = name;
    }
}
```

## @NonNull Annotation

JSpecify doesn't have @NonNull because non-null is the default in @NullMarked code. However, for interop with other tools:

```java
// These are redundant in @NullMarked code
public void method(@NonNull String input) { }  // Redundant
public void method(String input) { }           // Same meaning
```

## Initialization Annotations

### @Initializer

Marks a method as an initializer for field initialization:

```java
@NullMarked
public class Component {
    private Service service;

    @Initializer
    public void onCreate() {
        service = new Service();
    }
}
```

### @MonotonicNonNull

Field that starts null but once set, never becomes null again:

```java
import com.uber.nullaway.annotations.MonotonicNonNull;

@NullMarked
public class LazyLoader {
    private @MonotonicNonNull Resource resource;

    public Resource getResource() {
        if (resource == null) {
            resource = loadResource();
        }
        return resource;  // Safe - once set, never null
    }
}
```

## Contract Annotations

### @Contract

JetBrains @Contract annotations for method contracts:

```java
import org.jetbrains.annotations.Contract;

// If parameter is not null, return is not null
@Contract("!null -> !null")
public @Nullable String process(@Nullable String input) { }

// If parameter is null, throws
@Contract("null -> fail")
public void requireNonNull(@Nullable Object obj) { }

// Returns true only if parameter is not null
@Contract("null -> false")
public boolean isValid(@Nullable String s) { }
```

### @EnsuresNonNull

Postcondition annotation:

```java
import com.uber.nullaway.annotations.EnsuresNonNull;

@EnsuresNonNull("cache")
public void initCache() {
    cache = new Cache();
}
```

### @RequiresNonNull

Precondition annotation:

```java
import com.uber.nullaway.annotations.RequiresNonNull;

@RequiresNonNull("cache")
public void useCache() {
    cache.get("key");
}
```

## Migration from Other Annotations

| Legacy Annotation | JSpecify Replacement |
|-------------------|---------------------|
| `javax.annotation.Nullable` | `org.jspecify.annotations.Nullable` |
| `org.jetbrains.annotations.Nullable` | `org.jspecify.annotations.Nullable` |
| `androidx.annotation.Nullable` | `org.jspecify.annotations.Nullable` |
| `javax.annotation.Nonnull` | Remove (implicit in @NullMarked) |
| `org.jetbrains.annotations.NotNull` | Remove (implicit in @NullMarked) |
| `@javax.annotation.ParametersAreNonnullByDefault` | `@NullMarked` on package |

## Best Practices

1. **Prefer package-level @NullMarked**: Apply to package-info.java, not each class

2. **Only annotate @Nullable**: Non-null is default; @NonNull is redundant

3. **Place @Nullable before type**: `@Nullable String`, not `String @Nullable`

4. **Document nullable return values**: Explain when null is returned

5. **Avoid null in collections**: Prefer empty collections over null

6. **Use Optional for returns**: Consider Optional<T> instead of @Nullable T for API clarity
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/annotation-guide.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

