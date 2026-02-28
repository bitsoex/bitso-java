# Enable Native Gradle dependency locking for reproducible builds and security visibility

> Enable Native Gradle dependency locking for reproducible builds and security visibility

# Lock Gradle Dependencies

Enable Native Gradle dependency locking for reproducible builds and security auditing.

## Skill Location

```
java/skills/gradle-standards/references/native-dependency-locking.md
```

## Quick Start

1. **Add resolveAndLockAll task** to root build.gradle (see skill reference)
2. **Generate lock files**: `./gradlew resolveAndLockAll --write-locks --refresh-dependencies --no-daemon --no-scan`
3. **Commit lockfiles**: `git add "**/gradle.lockfile"`

## Critical Rules

- **Never edit lockfiles directly** - Always regenerate with Gradle tasks
- **Use LENIENT mode** for modules with scope attribution issues
- **Forces must reference version catalog** - Single source of truth

## Skill Contents

| Resource | Description |
|----------|-------------|
| `references/native-dependency-locking.md` | Full setup, troubleshooting, security integration |

## Related

- `java/skills/dependabot-security/` - Security vulnerability workflow
- `java/skills/dependabot-security/references/dependency-graph.md` - CI visibility

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/lock-gradle-dependencies.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
