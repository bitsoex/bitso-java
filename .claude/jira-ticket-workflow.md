# Jira Ticket Workflow for AI Agent Commands

**Applies to:** All files

# Jira Ticket Workflow for AI Agent Commands

Best practices for AI agents to create and manage Jira tickets when performing automated work like fixing vulnerabilities, resolving SonarQube issues, or improving test coverage.

## Emoji Conventions for AI-Assisted Work

All AI-assisted work must be clearly identifiable through standardized emoji prefixes:

| Work Type | Emoji | Example Commit/PR Title |
|-----------|-------|------------------------|
| AI-assisted (all) | 🤖 | Required in ALL AI commits/PRs |
| Security/Vulnerability | 🛡️ | `🤖 🛡️ fix(security): resolve critical CVE-2024-xxxxx` |
| Code Quality/SonarQube | ✅ | `🤖 ✅ fix(quality): resolve BLOCKER SonarQube issues` |
| Test Coverage | 🧪 | `🤖 🧪 test: improve coverage for PaymentService` |
| Dependency Updates | 📦 | `🤖 📦 chore(deps): update Spring Boot to 3.5.8` |
| Documentation | 📝 | `🤖 📝 docs: update API documentation` |
| Performance | ⚡ | `🤖 ⚡ perf: optimize database queries` |
| Refactoring | ♻️ | `🤖 ♻️ refactor: simplify error handling` |

**Format**: `🤖 [TYPE_EMOJI] type(scope): description` (note the space after 🤖)

## Workflow: Create Jira Ticket Before Work

AI agents must create a Jira ticket BEFORE starting any work. This ensures traceability and proper sprint/cycle tracking.

### Step 1: Identify Current Sprint/Cycle Epic

Find the most recent KTLO or Tech Debt epic for the team:

```bash
# Using Atlassian MCP - search for current cycle KTLO epic
# Example: "[C6]: AI Enablement KTLO" for cycle 6
```

Use `mcp_atlassian_searchJiraIssuesUsingJql` to find the current epic:

```text
project = "PROJECT_KEY" AND issuetype = Epic AND summary ~ "KTLO" ORDER BY created DESC
```

### Step 2: Create the Ticket

Use `mcp_atlassian_createJiraIssue` with:

- **Project**: Current team's project key
- **Issue Type**: Task (or appropriate type)
- **Parent**: Current Sprint/Cycle KTLO Epic
- **Summary**: Include emoji prefix and clear description
- **Description**: Detail the work to be done

**Ticket Summary Format**:

```text
🤖🛡️ Fix [SEVERITY] Dependabot vulnerabilities in [repo-name]
🤖✅ Resolve [SEVERITY] SonarQube issues in [repo-name]
🤖🧪 Improve test coverage for [module/class]
🤖📦 Update [dependency] to [version]
```

**Ticket Description Template**:

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

### Step 3: Create Branch with Jira Key

After ticket creation, create a branch using the Jira key:

```bash
# Format: type/JIRA-KEY-short-description[-part-N]
git checkout -b fix/EN-52-critical-vulnerabilities
git checkout -b fix/EN-52-critical-vulnerabilities-part-2  # For multiple iterations
```

**Branch Naming Convention**:

| Type | Format | Example |
|------|--------|---------|
| Security fix | `fix/JIRA-KEY-severity-vulnerabilities` | `fix/EN-52-critical-vulnerabilities` |
| Quality fix | `fix/JIRA-KEY-severity-sonar-issues` | `fix/EN-53-blocker-sonar-issues` |
| Test coverage | `test/JIRA-KEY-coverage-module` | `test/EN-54-coverage-payment-service` |
| Dependency | `chore/JIRA-KEY-update-dependency` | `chore/EN-55-update-spring-boot` |

### Step 4: Commit with Jira Key and Emojis

All commits must reference the Jira key and include appropriate emojis:

```bash
git commit -m "🤖🛡️ fix(security): [EN-52] resolve critical CVE-2024-xxxxx

- Updated commons-compress to 1.27.1
- Added dependency substitution for transitive deps

Severity: CRITICAL
Strategy: Dependency substitution (BOM doesn't manage this)"
```

**Commit Message Format**:

```text
🤖[TYPE_EMOJI] type(scope): [JIRA-KEY] short description

- Detail 1
- Detail 2

[Additional context if needed]
```

### Step 5: Create PR with Jira Key and Emojis

PR titles and bodies must include the Jira key and emojis:

**PR Title Format**:

```text
🤖🛡️ [EN-52] fix(security): resolve critical Dependabot vulnerabilities
🤖✅ [EN-53] fix(quality): resolve BLOCKER SonarQube issues
🤖🧪 [EN-54] test: improve coverage for PaymentService
```

**PR Body Template**:

```markdown
## 🤖 AI-Assisted Changes

Jira: [EN-52](https://bitsomx.atlassian.net/browse/EN-52)

## Summary
[Brief description of changes]

## Changes
- [ ] Change 1
- [ ] Change 2

## Severity Level
[CRITICAL | HIGH | MEDIUM | LOW]

## Validation
- [ ] Build passes locally
- [ ] Tests pass locally
- [ ] [Specific validation for this type of work]

## AI Agent Details
- **Agent**: Cursor/Copilot
- **Command**: /fix-dependabot-vulnerabilities
- **Iteration**: 1 of N (if applicable)
```

## Severity-Based Processing

AI agents must process issues by severity level, one level at a time:

### Vulnerability Severity Order

1. **CRITICAL** - Fix all critical vulnerabilities first
2. **HIGH** - Only after no CRITICAL remain
3. **MEDIUM/MODERATE** - Only after no HIGH remain
4. **LOW** - Only after no MEDIUM remain

### SonarQube Severity Order

1. **BLOCKER** - Fix all blockers first
2. **CRITICAL** - Only after no BLOCKER remain
3. **MAJOR** - Only after no CRITICAL remain
4. **MINOR** - Only after no MAJOR remain
5. **INFO** - Only after no MINOR remain

### Processing Rules

1. **Query current severity**: Check what's the highest severity present
2. **Focus on one level**: Only fix issues of that severity
3. **Atomic commits**: Each commit should address related issues
4. **Create new ticket for next level**: After completing one severity, create a new ticket for the next

**Example Workflow**:

```text
1. Query: Found 2 CRITICAL, 5 HIGH, 10 MEDIUM vulnerabilities
2. Create ticket: "🤖🛡️ Fix CRITICAL vulnerabilities in repo-name"
3. Fix only CRITICAL issues
4. Commit, push, create PR
5. After merge, create new ticket: "🤖🛡️ Fix HIGH vulnerabilities in repo-name"
6. Repeat until all severities addressed
```

## Integration with Existing Commands

This workflow integrates with:

- `/fix-dependabot-vulnerabilities` - Security vulnerability fixes
- `/fix-sonarqube-issues` - Code quality fixes
- `/fix-test-coverage` - Test coverage improvements
- `/package-audit` - Node.js dependency audit

Each command should:

1. Check for existing ticket or create one
2. Follow severity-based processing
3. Use appropriate emoji conventions
4. Reference Jira key in all commits/PRs

## Best Practices

1. **One severity per PR**: Keep PRs focused and reviewable
2. **Batch related fixes**: Group similar issues in one commit
3. **Clear descriptions**: Document what was fixed and why
4. **Link everything**: Jira ticket ↔ Branch ↔ Commits ↔ PR
5. **Update ticket status**: Move ticket through workflow as work progresses

---
*This rule is part of the java category.*
*Source: global/rules/jira-ticket-workflow.md*
