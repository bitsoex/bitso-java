---
name: security-review
description: >
  Performs security analysis on code changes including credential scanning, input validation,
  authentication checks, and dependency vulnerability assessment. Use when reviewing code
  for security issues or before completing security-sensitive changes.
compatibility: Works with any codebase; enhanced with SonarQube MCP for comprehensive analysis
metadata:
  version: "0.1"
---

# Security Review

> **Placeholder**: This skill will be fully developed during the content migration phase.

## When to use this skill

- When reviewing code for security vulnerabilities
- Before completing changes to authentication/authorization code
- When adding new dependencies
- During security-focused code reviews

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

## TODO

- [ ] Define security check rules by technology
- [ ] Integrate with SonarQube MCP
- [ ] Add credential pattern detection
- [ ] Define severity levels and thresholds
