---
description: Java Run Build After Changes
alwaysApply: true
tags:
  - java
---

# Run Build After Code Changes

After updating Java or Groovy code, verify your changes:

```bash
# Run tests to verify changes
./gradlew test 2>&1 | grep -E "FAILED|Error" || echo "All tests passed"

# Or run full build with tests
./gradlew build 2>&1 | grep -E "FAILED|Error" || echo "Build successful"
```

If problems are found, fix them before committing.

**For comprehensive build commands and troubleshooting**, see: [java-gradle-commands](./java-gradle-commands.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-run-build-after-changes.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
