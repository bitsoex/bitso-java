# Fix Dependabot security vulnerabilities in Java/Gradle projects

> Fix Dependabot security vulnerabilities in Java/Gradle projects

# Fix Dependabot Vulnerabilities (Java)

Resolve Dependabot security alerts using severity-based processing.

## Skill Location

```
java/skills/dependabot-security/
```

## Quick Start

1. **Create Jira ticket** before any code changes
2. **Process by severity**: CRITICAL > HIGH > MEDIUM > LOW
3. **Use dependency substitution** when BOM doesn't manage version
4. **Verify with dependency graph** - only patched versions should appear

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full workflow and procedures |
| `references/fix-strategies.md` | Fix strategy hierarchy with examples |
| `references/severity-processing.md` | Severity-based workflow |
| `references/dependency-graph.md` | Dependency graph verification |
| `references/troubleshooting.md` | Common issues and solutions |

## Related

- `java/rules/java-vulnerability-golden-paths.md` - Proven fix patterns
- `global/rules/jira-ticket-workflow.md` - Jira ticket creation
- [jira-integration](.claude/skills/jira-integration/SKILL.md) - Jira workflow skill

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/fix-dependabot-vulnerabilities.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
