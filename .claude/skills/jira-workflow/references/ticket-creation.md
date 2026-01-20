# Ticket Creation

Create a Jira ticket **only if no existing ticket was found** during search.

## Contents

- [Create Using Atlassian MCP](#create-using-atlassian-mcp) (L15-L24)
- [Ticket Summary Formats](#ticket-summary-formats) (L25-L33)
- [Ticket Description Template](#ticket-description-template) (L34-L50)
- [Example: Security Vulnerability Ticket](#example-security-vulnerability-ticket) (L51-L73)
- [Example: SonarQube Issue Ticket](#example-sonarqube-issue-ticket) (L74-L96)
- [After Ticket Creation](#after-ticket-creation) (L97-L101)

---
## Create Using Atlassian MCP

Use `mcp_atlassian_createJiraIssue` with:

- **Project**: Current team's project key (discovered dynamically)
- **Issue Type**: Task (or appropriate type)
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Summary**: Include emoji prefix and clear description
- **Description**: Detail the work to be done

## Ticket Summary Formats

```text
🤖🛡️ Fix [SEVERITY] Dependabot vulnerabilities in [repo-name]
🤖✅ Resolve [SEVERITY] SonarQube issues in [repo-name]
🤖🧪 Improve test coverage for [module/class]
🤖📦 Update [dependency] to [version]
```

## Ticket Description Template

```markdown
## Context
[Brief description of why this work is needed]

## Scope
- [ ] Item 1
- [ ] Item 2

## Severity Level
[CRITICAL | HIGH | MEDIUM | LOW]

## Automated by
AI Agent (Cursor/Copilot)
```

## Example: Security Vulnerability Ticket

**Summary:**
```
🤖🛡️ Fix CRITICAL Dependabot vulnerabilities in payment-service
```

**Description:**
```markdown
## Context
Dependabot has identified critical vulnerabilities that need immediate remediation.

## Scope
- [ ] CVE-2024-12345 in commons-compress
- [ ] CVE-2024-67890 in jackson-databind

## Severity Level
CRITICAL

## Automated by
AI Agent (Cursor)
```

## Example: SonarQube Issue Ticket

**Summary:**
```
🤖✅ Resolve BLOCKER SonarQube issues in order-service
```

**Description:**
```markdown
## Context
SonarQube analysis found BLOCKER-level code quality issues.

## Scope
- [ ] java:S2259 - Null pointer dereference in OrderService
- [ ] java:S1161 - Missing @Override annotation

## Severity Level
BLOCKER

## Automated by
AI Agent (Cursor)
```

## After Ticket Creation

1. Note the ticket key (e.g., `PROJ-123`)
2. Use this key for branch naming
3. Reference in all commits and PR
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-workflow/references/ticket-creation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

