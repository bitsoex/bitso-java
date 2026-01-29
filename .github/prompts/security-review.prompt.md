# Security Review Workflow

> Security Review Workflow

# Security Review

Perform a security review of code changes.

## Purpose

This command invokes the **security-review skill** to analyze code for security vulnerabilities.

## Skill Location

[security-review](.claude/skills/security-review/SKILL.md)

## Quick Checklist

1. **Credential Scanning** - Search for hardcoded secrets
2. **Input Validation** - Check SQL injection, XSS, command injection
3. **Authentication & Authorization** - Review auth mechanisms
4. **Data Protection** - Check encryption and data handling
5. **Dependency Security** - Scan for vulnerable packages

## Output Format

For each finding, provide:

- **Finding**: Description of the issue
- **Severity**: Critical/High/Medium/Low
- **Recommendation**: How to fix
- **File/Line**: Location

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full security review documentation |
| `references/workflow.md` | Step-by-step review process |
| `references/java/security-patterns.md` | Java security patterns |
| `references/typescript/security-patterns.md` | TypeScript security patterns |
| `references/python/security-patterns.md` | Python security patterns |
| `references/go/security-patterns.md` | Go security patterns |

## Related

- `fix-vulnerabilities` - npm audit and vulnerability management (see java/nodejs technology-specific skill)
- `fix-sonarqube` - SonarQube issue fixing (see java/nodejs technology-specific skill)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/security-review.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
