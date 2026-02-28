# Greenflag Workflow for Dependency Updates

Weekly workflow for Greenflag engineers managing Dependabot PRs.

## Contents

- [Daily Routine](#daily-routine)
- [Triage Process](#triage-process)
- [Merge Strategies](#merge-strategies)
- [When to Escalate](#when-to-escalate)

---
## Daily Routine

### Morning Check (10-15 min)

1. **Check Dependabot dashboard**

   ```bash
   # List all open Dependabot PRs for your repos
   gh pr list --author app/dependabot --state open --repo bitsoex/{repo}
   ```

2. **Quick security scan**

   ```bash
   # Check for security alerts (highest priority)
   gh api repos/bitsoex/{repo}/dependabot/alerts --jq '.[] | select(.state == "open") | .security_advisory.severity'
   ```

3. **Identify quick wins** - Minor/patch updates with passing CI

### Batch Processing (30-60 min)

- Group PRs by squad or dependency type
- Review and merge in batches
- Document any issues in Slack

## Triage Process

### Step 1: Categorize the Update

| Category | Action | Example |
|----------|--------|---------|
| **Security** | Use fix-vulnerabilities skill | CVE alerts |
| **Patch** | Quick review, merge if CI passes | 1.2.3 → 1.2.4 |
| **Minor** | Check release notes, merge if safe | 1.2.x → 1.3.0 |
| **Major** | Create ticket, coordinate | 1.x → 2.0 |

### Step 2: Check Squad Ownership

For internal Bitso libraries:

```bash
# Find the publishing repository
grep -r "com.bitso.{library}" bitso-gradle-catalogs/repos/

# Check the squad group in PR title
# e.g., "Bump spring-boot in the asset-management-squad group"
```

### Step 3: Verify CI Status

```bash
# Check PR status
gh pr view {pr-number} --json statusCheckRollup

# Review failing checks
gh pr checks {pr-number}
```

### Step 4: Make Decision

- **Green CI + Patch/Minor** → Merge
- **Green CI + Major** → Review changelog, consider ticket
- **Failing CI** → Investigate, may need fixes

## Merge Strategies

### Safe Auto-Merge (Patch Updates)

For well-tested dependencies with patch updates:

```bash
# Enable auto-merge for a PR
gh pr merge {pr-number} --auto --squash
```

### Batch Merge (Same Squad)

When multiple PRs affect the same squad's libraries:

1. Review all related PRs together
2. Check for version conflicts
3. Merge in dependency order (base libs first)

### Manual Review (Major Updates)

For major version updates:

1. Read the release notes/changelog
2. Check for breaking changes
3. Create a Jira ticket if complex
4. Coordinate with the owning squad

## When to Escalate

### Create a Jira Ticket

- Major version updates requiring migration
- Updates that break CI
- Updates affecting multiple services
- Security vulnerabilities requiring coordination

### Notify Squad

- When their library is involved
- When you're unsure about compatibility
- For breaking API changes

### Defer to Next Cycle

- Large migration efforts
- Updates requiring code changes
- Blocked by other work

Use the `jira-integration` skill to create tickets when escalating.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/references/greenflag-workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

