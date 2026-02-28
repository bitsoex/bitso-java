---
name: security-review
description: >
  Performs security analysis on code changes including credential scanning, input validation,
  authentication checks, and dependency vulnerability assessment. Use when reviewing code
  for security issues or before completing security-sensitive changes.
compatibility: Works with any codebase; enhanced with SonarQube MCP for comprehensive analysis
metadata:
  version: "2.0.0"
---

# Security Review

Perform a security review of code changes.

## When to use this skill

- When reviewing code for security vulnerabilities
- Before completing changes to authentication/authorization code
- When adding new dependencies
- During security-focused code reviews
- When asked to perform a "security review" or "security audit"

## Skill Contents

### Available Resources

**📚 references/** - Detailed documentation
- [go](references/go)
- [java](references/java)
- [python](references/python)
- [typescript](references/typescript)
- [workflow](references/workflow.md)

---

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

## Security Checks

| Check | Description |
|-------|-------------|
| Credential scanning | Detect hardcoded secrets |
| Input validation | Verify user input is sanitized |
| Dependency audit | Check for vulnerable dependencies |
| Authentication | Review auth/authz implementations |

## References

| Technology | Reference |
|------------|-----------|
| Java | `references/java/security-patterns.md` |
| TypeScript | `references/typescript/security-patterns.md` |
| Python | `references/python/security-patterns.md` |
| Go | `references/go/security-patterns.md` |

## Related Skills

- [fix-vulnerabilities (Java)](.claude/skills/fix-vulnerabilities/SKILL.md) - Dependency vulnerability management
- [fix-vulnerabilities (Node.js)](.claude/skills/fix-vulnerabilities/SKILL.md) - npm audit and vulnerability management
- [fix-sonarqube (Java)](.claude/skills/fix-sonarqube/SKILL.md) - SonarQube issue fixing for Java
- [fix-sonarqube (Node.js)](.claude/skills/fix-sonarqube/SKILL.md) - SonarQube issue fixing for Node.js
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/security-review/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

