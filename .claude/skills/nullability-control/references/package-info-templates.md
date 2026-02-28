# Package-info.java Templates

Templates for creating `package-info.java` files with @NullMarked annotation.

## Contents

- [Standard Template](#standard-template)
- [Template with Documentation](#template-with-documentation)
- [Test Package Template](#test-package-template)
- [Opting Out: @NullUnmarked](#opting-out-nullunmarked)
- [Creating package-info.java Files](#creating-package-infojava-files)
- [Multi-Module Projects](#multi-module-projects)
- [Verification](#verification)
- [Class-Level Alternative](#class-level-alternative)

---
## Standard Template

For every package in your codebase:

```java
@NullMarked
package com.bitso.myservice.domain;

import org.jspecify.annotations.NullMarked;
```

## Template with Documentation

For public API packages:

```java
/**
 * Domain model classes for the user service.
 *
 * <p>This package is null-marked: all types are non-null by default.
 * Use {@link org.jspecify.annotations.Nullable @Nullable} to indicate
 * parameters, return values, or fields that may be null.
 */
@NullMarked
package com.bitso.userservice.domain;

import org.jspecify.annotations.NullMarked;
```

## Test Package Template

For test packages:

```java
@NullMarked
package com.bitso.myservice.domain;

import org.jspecify.annotations.NullMarked;
```

**Note**: Test packages should also be @NullMarked for consistency, unless you've disabled NullAway on test code.

## Opting Out: @NullUnmarked

For packages you want to exclude from null checking:

```java
/**
 * Legacy code not yet migrated to null safety.
 */
@NullUnmarked
package com.bitso.legacy;

import org.jspecify.annotations.NullUnmarked;
```

## Creating package-info.java Files

### Find Packages Without package-info.java

```bash
# List all packages missing package-info.java
find src/main/java -type d -exec sh -c '
  if [ -n "$(ls -A "$1"/*.java 2>/dev/null)" ] && [ ! -f "$1/package-info.java" ]; then
    pkg=$(echo "$1" | sed "s|src/main/java/||" | tr "/" ".")
    echo "Missing: $pkg ($1)"
  fi
' _ {} \;
```

### Generate package-info.java for All Packages

```bash
# Generate package-info.java for all packages
find src/main/java -type d -exec sh -c '
  if [ -n "$(ls -A "$1"/*.java 2>/dev/null)" ] && [ ! -f "$1/package-info.java" ]; then
    pkg=$(echo "$1" | sed "s|src/main/java/||" | tr "/" ".")
    cat > "$1/package-info.java" << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
    echo "Created: $1/package-info.java"
  fi
' _ {} \;
```

### For Test Packages

```bash
# Generate for test packages
find src/test/java -type d -exec sh -c '
  if [ -n "$(ls -A "$1"/*.java 2>/dev/null)" ] && [ ! -f "$1/package-info.java" ]; then
    pkg=$(echo "$1" | sed "s|src/test/java/||" | tr "/" ".")
    cat > "$1/package-info.java" << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
    echo "Created: $1/package-info.java"
  fi
' _ {} \;
```

## Multi-Module Projects

For multi-module Gradle projects, run in each module:

```bash
# From project root
for module in $(find . -name "src" -type d | xargs -I{} dirname {}); do
  echo "Processing: $module"
  (cd "$module" && find src -type d -exec sh -c '
    if [ -n "$(ls -A "$1"/*.java 2>/dev/null)" ] && [ ! -f "$1/package-info.java" ]; then
      # Determine package name based on source root
      src_root=$(echo "$1" | sed -E "s|(src/(main\|test)/java)/.*|\1|")
      pkg=$(echo "$1" | sed "s|$src_root/||" | tr "/" ".")
      cat > "$1/package-info.java" << EOF
@NullMarked
package $pkg;

import org.jspecify.annotations.NullMarked;
EOF
      echo "Created: $1/package-info.java"
    fi
  ' _ {} \;)
done
```

## Verification

Verify all packages have package-info.java:

```bash
# Count Java files vs package-info.java files
echo "Java source directories: $(find src -name "*.java" -not -name "package-info.java" | xargs -I{} dirname {} | sort -u | wc -l)"
echo "package-info.java files: $(find src -name "package-info.java" | wc -l)"
```

## Class-Level Alternative

If package-info.java is not feasible, apply @NullMarked at class level:

```java
@NullMarked
public class MyService {
    // Class is null-marked
}
```

**Note**: Package-level is preferred as it covers all classes automatically.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/package-info-templates.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

