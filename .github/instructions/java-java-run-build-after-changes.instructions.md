---
applyTo: ""
description: Java Run Build After Changes
---

# Java Run Build After Changes

After updating Java or Groovy code, verify your changes:

```bash
# Run tests to verify changes
./gradlew test 2>&1 | grep -E "FAILED|Error" || echo "All tests passed"

# Or run full build with tests
./gradlew build 2>&1 | grep -E "FAILED|Error" || echo "Build successful"
```

If problems are found, fix them before committing.

**For comprehensive build commands and troubleshooting**, see: [java-gradle-commands](.cursor/rules/java-gradle-commands/java-gradle-commands.mdc)

## 📚 Full Documentation

For complete guidelines, scripts, and references, see the skill:

```
.claude/skills/java-standards/SKILL.md
```

The skill includes:
- **SKILL.md** - Complete instructions and quick start
- **scripts/** - Executable automation scripts
- **references/** - Detailed documentation
- **assets/** - Templates and resources

> **Note**: This is a shallow reference. The full content is maintained in the skill to avoid duplication.


<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-run-build-after-changes.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
