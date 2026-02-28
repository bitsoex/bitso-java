# Suppressing Warnings

Patterns for suppressing NullAway warnings when needed.

## Contents

- [When to Suppress](#when-to-suppress)
- [Suppression Methods](#suppression-methods)
- [castToNonNull Utility](#casttononnull-utility)
- [Common Suppression Scenarios](#common-suppression-scenarios)
- [Auto-Suppression](#auto-suppression)
- [Best Practices](#best-practices)

---
## When to Suppress

Suppress warnings only when:

- NullAway cannot prove a value is non-null but you know it is
- Dealing with legacy code during gradual migration
- Framework/library limitations prevent proper annotation

Avoid suppression when:

- There's a genuine null safety issue
- The code can be refactored to be null-safe
- Adding proper null checks is straightforward

## Suppression Methods

### @SuppressWarnings("NullAway")

Suppress all NullAway warnings on a method, field, or class:

```java
// On method
@SuppressWarnings("NullAway")
public void legacyMethod() {
    // NullAway warnings suppressed
}

// On field
@SuppressWarnings("NullAway")
private Object unsafeField;

// On class
@SuppressWarnings("NullAway")
public class LegacyClass {
    // All NullAway warnings suppressed
}
```

### @SuppressWarnings("NullAway.Init")

Suppress only initialization-related warnings:

```java
// On field
@SuppressWarnings("NullAway.Init")
private Object lazyField;

// On constructor
@SuppressWarnings("NullAway.Init")
public MyClass() {
    // Field initialization warnings suppressed
}

// On class
@SuppressWarnings("NullAway.Init")
public class FrameworkManagedClass {
    private Service injectedService;  // Framework initializes
}
```

### @NullUnmarked

Opt out entire scope from null checking:

```java
// On method
@NullUnmarked
public void legacyMethod(Object input) {
    // No null checking in this method
}

// On class
@NullUnmarked
public class LegacyAdapter {
    // No null checking in this class
}
```

## castToNonNull Utility

For cases where you know a value is non-null but NullAway can't prove it:

### Create Utility Method

```java
public final class NullUtils {

    private NullUtils() {}

    /**
     * Cast a @Nullable value to @NonNull.
     * Use only when you are certain the value is non-null.
     */
    @SuppressWarnings("NullAway")
    public static <T> T castToNonNull(@Nullable T value) {
        if (value == null) {
            throw new NullPointerException("Unexpected null value");
        }
        return value;
    }

    /**
     * Cast without throwing (for gradual migration).
     */
    @SuppressWarnings("NullAway")
    public static <T> T castToNonNullSoft(@Nullable T value) {
        if (value == null) {
            // Log warning but don't throw
            logger.warn("Unexpected null value", new Exception());
        }
        return value;
    }
}
```

### Configure NullAway

Tell NullAway about your cast method:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        option("NullAway:CastToNonNullMethod", "com.bitso.util.NullUtils.castToNonNull")
    }
}
```

With this configuration, NullAway will warn if `castToNonNull` is called on an already non-null value.

### Usage

```java
// When you know map.get() won't return null
String value = castToNonNull(map.get(key));

// When iterating known keys
for (String key : knownKeys) {
    process(castToNonNull(map.get(key)));
}
```

## Common Suppression Scenarios

### Framework Injection

```java
@SuppressWarnings("NullAway.Init")
public class SpringController {
    @Autowired
    private UserService userService;  // Spring injects
}
```

### Lazy Initialization

```java
public class LazyLoader {
    @SuppressWarnings("NullAway.Init")
    private Resource resource;

    public Resource getResource() {
        if (resource == null) {
            resource = loadResource();
        }
        return resource;
    }
}
```

Better: Use `@MonotonicNonNull`:

```java
import com.uber.nullaway.annotations.MonotonicNonNull;

public class LazyLoader {
    private @MonotonicNonNull Resource resource;

    public Resource getResource() {
        if (resource == null) {
            resource = loadResource();
        }
        return resource;  // NullAway understands this is safe
    }
}
```

### Complex Map Lookups

```java
// When you know the key exists
if (hasSpecificKey()) {
    @SuppressWarnings("NullAway")
    String value = map.get(getSpecificKey());
    process(value);
}
```

Better: Store key in local variable:

```java
String key = getSpecificKey();
if (map.containsKey(key)) {
    String value = map.get(key);  // NullAway understands
    process(value);
}
```

### Legacy API Integration

```java
@SuppressWarnings("NullAway")
public void processLegacyData(LegacyDto dto) {
    // LegacyDto is from unannotated library
    // but we know these fields are never null
    process(dto.getName());
    process(dto.getValue());
}
```

## Auto-Suppression

For initial migration, NullAway can suggest suppressions:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.errorprone {
        option("NullAway:SuggestSuppressions", "true")
    }
}
```

With Error Prone's patching:

```bash
./gradlew compileJava -Perror-prone-apply-patches
```

Add comment to suppressions:

```groovy
option("NullAway:AutoFixSuppressionComment", "TODO:null-safety-migration")
```

## Best Practices

1. **Prefer fixes over suppressions**: Fix the null safety issue when possible

2. **Use narrowest scope**: Suppress on field/method, not class

3. **Use specific suppression**: `NullAway.Init` over `NullAway` when appropriate

4. **Document suppressions**: Add comment explaining why suppression is needed

5. **Track suppressions**: Use consistent comment for migration suppressions

6. **Review regularly**: Revisit suppressions during code reviews

```java
// Good: Specific suppression with explanation
@SuppressWarnings("NullAway.Init")  // Initialized by Spring @Autowired
private UserService userService;

// Bad: Blanket suppression without context
@SuppressWarnings("NullAway")
public class MyClass { }
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/suppressing-warnings.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

