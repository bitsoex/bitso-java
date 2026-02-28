# Common NullAway Errors

Reference for NullAway error messages and how to fix them.

## Contents

- [Dereference Errors](#dereference-errors)
- [Return Type Errors](#return-type-errors)
- [Parameter Errors](#parameter-errors)
- [Field Assignment Errors](#field-assignment-errors)
- [Override Errors](#override-errors)
- [Initialization Errors](#initialization-errors)
- [Unboxing Errors](#unboxing-errors)
- [Map-Related Errors](#map-related-errors)
- [Quick Reference](#quick-reference)

---
## Dereference Errors

### dereferenced expression is @Nullable

**Error**: Code reads a field, writes a field, or invokes a method on an expression that might be null.

```java
// Error - in @NullMarked code, you must explicitly mark nullable types
@Nullable Object x = null;
x.toString();  // dereferencing x, which is @Nullable
```

**Fix**: Add a null check before the dereference:

```java
@Nullable Object x = null;
if (x != null) {
    x.toString();  // Safe - x is now non-null in this branch
}
```

Or use Optional:

```java
Optional.ofNullable(x)
    .map(Object::toString)
    .orElse("");
```

## Return Type Errors

### returning @Nullable expression from method with @NonNull return type

**Error**: Method returns a @Nullable expression but return type is non-null.

```java
// Error
Object method(@Nullable Object x) {
    return x;  // Return type is non-null
}
```

**Fix Option 1**: Make return type @Nullable:

```java
@Nullable Object method(@Nullable Object x) {
    return x;
}
```

**Fix Option 2**: Add null check:

```java
Object method(@Nullable Object x) {
    if (x != null) {
        return x;
    }
    return new Object();  // Default value
}
```

## Parameter Errors

### passing @Nullable parameter where @NonNull is required

**Error**: Passing a @Nullable value to a method expecting non-null.

```java
void requiresNonNull(Object x) { }

void caller(@Nullable Object y) {
    requiresNonNull(y);  // Error: y may be null
}
```

**Fix Option 1**: Add null check before call:

```java
void caller(@Nullable Object y) {
    if (y != null) {
        requiresNonNull(y);
    }
}
```

**Fix Option 2**: Make parameter @Nullable in the called method:

```java
void requiresNonNull(@Nullable Object x) {
    if (x != null) {
        // Use x
    }
}
```

## Field Assignment Errors

### assigning @Nullable expression to @NonNull field

**Error**: Assigning a @Nullable value to a non-null field.

```java
class Foo {
    Object myField;  // Non-null

    void writeToField(@Nullable Object z) {
        this.myField = z;  // Error: z may be null
    }
}
```

**Fix Option 1**: Make the field @Nullable:

```java
@Nullable Object myField;
```

**Fix Option 2**: Add null check:

```java
void writeToField(@Nullable Object z) {
    if (z != null) {
        this.myField = z;
    }
}
```

## Override Errors

### method returns @Nullable, but superclass method returns @NonNull

**Error**: Overriding method has weaker (more nullable) return type.

```java
class Super {
    Object getObj() { return new Object(); }
}

class Sub extends Super {
    @Override
    @Nullable Object getObj() { return null; }  // Error
}
```

**Fix**: Ensure override returns non-null:

```java
class Sub extends Super {
    @Override
    Object getObj() { return new Object(); }
}
```

### parameter is @NonNull, but parameter in superclass method is @Nullable

**Error**: Overriding method has stricter (less nullable) parameter.

```java
class Super {
    void handle(@Nullable Object obj) { }
}

class Sub extends Super {
    @Override
    void handle(Object obj) {  // Error: stricter than super
        obj.toString();
    }
}
```

**Fix**: Match superclass parameter nullability:

```java
class Sub extends Super {
    @Override
    void handle(@Nullable Object obj) {
        if (obj != null) {
            obj.toString();
        }
    }
}
```

## Initialization Errors

### @NonNull field not initialized

**Error**: A non-null field may not be initialized in all constructors.

```java
class C {
    Object f1;  // Non-null but not initialized

    C() {
        // f1 not initialized - Error
    }
}
```

**Fix Option 1**: Initialize in constructor:

```java
C() {
    this.f1 = new Object();
}
```

**Fix Option 2**: Initialize at declaration:

```java
Object f1 = new Object();
```

**Fix Option 3**: Make field @Nullable:

```java
@Nullable Object f1;
```

### read of @NonNull field before initialization

**Error**: Non-null field is read before it's initialized.

```java
class C {
    Object foo;

    C() {
        this.foo.toString();  // Error: foo not initialized yet
        this.foo = new Object();
    }
}
```

**Fix**: Initialize before reading:

```java
C() {
    this.foo = new Object();
    this.foo.toString();  // Now safe
}
```

## Unboxing Errors

### unboxing of a @Nullable value

**Error**: Unboxing a @Nullable boxed type can cause NPE.

```java
Integer i1 = null;
int i2 = i1 + 3;  // NullPointerException
```

**Fix**: Add null check:

```java
Integer i1 = null;
int i2 = (i1 != null) ? i1 + 3 : 0;
```

## Map-Related Errors

### Map.get() Returns @Nullable

NullAway treats `Map.get()` as returning @Nullable unless there's a `containsKey()` check:

```java
// Error
String value = map.get(key);
value.length();  // May be null

// Fix Option 1: containsKey check
if (map.containsKey(key)) {
    String value = map.get(key);
    value.length();  // Safe
}

// Fix Option 2: null check
String value = map.get(key);
if (value != null) {
    value.length();
}

// Fix Option 3: getOrDefault
String value = map.getOrDefault(key, "");
value.length();  // Safe
```

## Quick Reference

| Error | Common Fix |
|-------|------------|
| dereferenced @Nullable | Add null check or use Optional |
| returning @Nullable | Make return type @Nullable or return default |
| passing @Nullable | Add null check or make param @Nullable |
| assigning @Nullable to field | Make field @Nullable or check before assign |
| override weaker return | Match superclass nullability |
| override stricter param | Match superclass nullability |
| field not initialized | Initialize in constructor or make @Nullable |
| unboxing @Nullable | Add null check before unboxing |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/common-errors.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

