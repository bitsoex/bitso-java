---
description: Jira Ticket Workflow for AI Agent Commands
alwaysApply: true
tags:
  - java
---

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

### Step 0: Discover User's Jira Project Key (REQUIRED)

**DO NOT use hardcoded project keys.** Discover the user's project key dynamically before any ticket operations.

#### Method 1: Query User's Recent Tickets (Preferred)

Use `mcp_atlassian_searchJiraIssuesUsingJql` to find the user's recent tickets:

```text
reporter = currentUser() ORDER BY created DESC
```

Or by assignee:

```text
assignee = currentUser() ORDER BY updated DESC
```

Parse the returned ticket keys to extract the project key prefix (the part before the hyphen and number).

#### Method 2: Check User's Recent Merged PRs

Search for the user's recent merged PRs in the current repository:

```bash
# Get recent merged PRs by current user
gh pr list --author @me --state merged --limit 5 --json title,body

# Look for Jira key patterns in titles like:
# - [PROJ-123] description
# - PROJ-123 - description
# - https://bitsomx.atlassian.net/browse/PROJ-123
```

Extract the project key prefix from any found ticket references.

#### Method 3: Check Current Repository's Recent PRs

```bash
# Get recent PRs in the repo
gh pr list --state merged --limit 10 --json title,body

# Extract Jira keys from titles/bodies
# Common patterns: [PROJ-XXX], PROJ-XXX, /browse/PROJ-XXX
```

#### Method 4: Ask the User (Fallback)

If discovery fails, ask the user:

```text
I couldn't automatically determine your Jira project key. 
What is your team's Jira project key?
```

#### Use Discovered Key

Once discovered, store and use the project key consistently:

```bash
# JIRA_PROJECT_KEY should be set from discovery, never hardcoded
# Example discovered values: ALTS, MMCC, SBP, COREXP, etc.
```

### Step 1: Search for Existing Open Tickets (REQUIRED)

**Before creating a new ticket**, search for existing open tickets that may already cover the work:

Use `mcp_atlassian_searchJiraIssuesUsingJql` with these queries:

**For Dependabot vulnerabilities (search by CVE):**

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND (summary ~ "CVE-XXXX-XXXXX" OR description ~ "CVE-XXXX-XXXXX") ORDER BY created DESC
```

**For SonarQube issues (search by rule and repo):**

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "SonarQube" AND summary ~ "[repo-name]" ORDER BY created DESC
```

**For general KTLO work in the same repo:**

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND parent = "KTLO-EPIC-KEY" AND summary ~ "[repo-name]" ORDER BY created DESC
```

**Validation checklist before creating a new ticket:**

- [ ] Discovered user's Jira project key (Step 0)
- [ ] Searched for existing tickets with same CVE/vulnerability identifier
- [ ] Searched for existing tickets with same SonarQube rule in same repo
- [ ] Searched for any open ticket under current KTLO epic for same repo
- [ ] Verified project key matches user's discovered key
- [ ] Verified matching tickets are in open/in-progress state (not done/closed)

**Only create a new ticket if NO matching open ticket exists.**

If an existing ticket is found:

1. Use that ticket's key for the branch name
2. Add a comment to the ticket noting the new work being done
3. Skip to Step 3 (Create Branch)

### Step 2: Identify Current Sprint/Cycle Epic

Find the most recent KTLO or Tech Debt epic for the team:

```bash
# Using Atlassian MCP - search for current cycle KTLO epic
# Example: "[C6]: AI Enablement KTLO" for cycle 6
```

Use `mcp_atlassian_searchJiraIssuesUsingJql` to find the current epic:

```text
project = "PROJECT_KEY" AND issuetype = Epic AND summary ~ "KTLO" ORDER BY created DESC
```

### Step 3: Create the Ticket (Only If No Existing Ticket Found)

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

### Step 4: Create Branch with Jira Key

After ticket creation (or finding existing ticket), create a branch using the actual Jira key:

```bash
# Format: type/JIRA-KEY-short-description[-part-N]
# JIRA_KEY is the actual ticket key from Step 1 or Step 3 (e.g., PROJ-123)

git checkout -b fix/${JIRA_KEY}-critical-vulnerabilities
git checkout -b fix/${JIRA_KEY}-critical-vulnerabilities-part-2  # For multiple iterations
```

**Branch Naming Convention**:

| Type | Format | Example |
|------|--------|---------|
| Security fix | `fix/JIRA-KEY-severity-vulnerabilities` | `fix/PROJ-123-critical-vulnerabilities` |
| Quality fix | `fix/JIRA-KEY-severity-sonar-issues` | `fix/PROJ-456-blocker-sonar-issues` |
| Test coverage | `test/JIRA-KEY-coverage-module` | `test/PROJ-789-coverage-payment-service` |
| Dependency | `chore/JIRA-KEY-update-dependency` | `chore/PROJ-101-update-spring-boot` |

### Step 5: Commit with Jira Key and Emojis

All commits must reference the actual Jira key from ticket creation and include appropriate emojis:

```bash
# JIRA_KEY is the actual ticket key (e.g., PROJ-123)
git commit -m "🤖🛡️ fix(security): [${JIRA_KEY}] resolve critical CVE-2024-xxxxx

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

### Step 6: Create PR with Jira Key and Emojis

PR titles and bodies must include the Jira key and emojis:

**PR Title Format** (use actual Jira key from ticket creation, e.g., PROJ-123):

```text
🤖🛡️ [PROJ-123] fix(security): resolve critical Dependabot vulnerabilities
🤖✅ [PROJ-456] fix(quality): resolve BLOCKER SonarQube issues
🤖🧪 [PROJ-789] test: improve coverage for PaymentService
```

**PR Body Template**:

```markdown
## 🤖 AI-Assisted Changes

Jira: [PROJ-123](https://bitsomx.atlassian.net/browse/PROJ-123)

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

## Agent Attribution (REQUIRED)

All AI-assisted commits and PRs must include attribution indicating which agent and command generated the work:

| Command | Attribution Line |
|---------|-----------------|
| `/fix-dependabot-vulnerabilities` | Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command. |
| `/fix-test-coverage` | Generated with the Quality Agent by the /fix-test-coverage command. |
| `/fix-sonarqube-issues` | Generated with the Security Agent by the /fix-sonarqube-issues command. |

**Add this attribution to:**

- Commit message body (after the detailed changes)
- PR description (in the AI Agent Details section)

**Example commit with attribution** (use actual Jira key, e.g., PROJ-123):

```bash
git commit -m "🤖 🛡️ fix(security): [PROJ-123] resolve critical CVE-2024-xxxxx

- Updated commons-compress to 1.27.1
- Added dependency substitution for transitive deps

Generated with the Quality Agent by the /fix-dependabot-vulnerabilities command."
```

## Integration with Existing Commands

This workflow integrates with:

- `/fix-dependabot-vulnerabilities` - Security vulnerability fixes
- `/fix-sonarqube-issues` - Code quality fixes
- `/fix-test-coverage` - Test coverage improvements
- `/package-audit` - Node.js dependency audit

Each command should:

1. **Search for existing open tickets first** (Step 1)
2. Check for existing ticket or create one
3. Follow severity-based processing
4. Use appropriate emoji conventions
5. Reference Jira key in all commits/PRs
6. **Include agent attribution in commits and PRs**

## Best Practices

1. **One severity per PR**: Keep PRs focused and reviewable
2. **Batch related fixes**: Group similar issues in one commit
3. **Clear descriptions**: Document what was fixed and why
4. **Link everything**: Jira ticket ↔ Branch ↔ Commits ↔ PR
5. **Update ticket status**: Move ticket through workflow as work progresses

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/jira-ticket-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
