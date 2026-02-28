# Severity-Based Processing

Process vulnerabilities by severity, one level at a time, creating separate PRs for each.

## Contents

- [Processing Order](#processing-order)
- [Workflow](#workflow)
- [Determine Current Severity](#determine-current-severity)
- [Get Alerts for Target Severity](#get-alerts-for-target-severity)
- [Branch Naming](#branch-naming)
- [After Fixing Current Severity](#after-fixing-current-severity)

---
## Processing Order

| Priority | Severity | When to Process |
|----------|----------|-----------------|
| 1 | CRITICAL | Always process first if any exist |
| 2 | HIGH | Only after NO CRITICAL remain |
| 3 | MEDIUM/MODERATE | Only after NO HIGH remain |
| 4 | LOW | Only after NO MEDIUM remain |

## Workflow

1. Query all open vulnerabilities
2. Identify highest severity present
3. Fix ONLY that severity level
4. Create separate ticket/PR for next severity level

## Determine Current Severity

```bash
REPO=$(gh repo view --json nameWithOwner -q '.nameWithOwner')

# Count by severity (use --paginate to get all alerts, not just first page)
gh api --paginate repos/$REPO/dependabot/alerts --jq '[.[] | select(.state == "open")] | group_by(.security_advisory.severity) | map({severity: .[0].security_advisory.severity, count: length})'

# Check for CRITICAL first (use --paginate for accurate counts)
CRITICAL_COUNT=$(gh api --paginate repos/$REPO/dependabot/alerts --jq '[.[] | select(.state == "open" and .security_advisory.severity == "critical")] | length')

if [ "$CRITICAL_COUNT" -gt 0 ]; then
    TARGET_SEVERITY="critical"
elif [ "$(gh api --paginate repos/$REPO/dependabot/alerts --jq '[.[] | select(.state == "open" and .security_advisory.severity == "high")] | length')" -gt 0 ]; then
    TARGET_SEVERITY="high"
elif [ "$(gh api --paginate repos/$REPO/dependabot/alerts --jq '[.[] | select(.state == "open" and .security_advisory.severity == "medium")] | length')" -gt 0 ]; then
    TARGET_SEVERITY="medium"
else
    TARGET_SEVERITY="low"
fi

echo "Fixing $TARGET_SEVERITY severity vulnerabilities"
```

## Get Alerts for Target Severity

```bash
# Get only alerts of target severity with details (--paginate ensures all alerts are retrieved)
gh api --paginate repos/$REPO/dependabot/alerts --jq ".[] | select(.state == \"open\" and .security_advisory.severity == \"$TARGET_SEVERITY\") | {
  number,
  package: .dependency.package.name,
  patched_version: .security_vulnerability.first_patched_version.identifier,
  cve: .security_advisory.cve_id,
  summary: .security_advisory.summary
}"
```

## Branch Naming

Include severity in branch name:

```bash
BRANCH_NAME="fix/${JIRA_KEY}-${TARGET_SEVERITY}-vulnerabilities"
git checkout -b "$BRANCH_NAME"
```

## After Fixing Current Severity

Check remaining vulnerabilities and create ticket for next severity:

```bash
# Check remaining (use --paginate to get all alerts)
gh api --paginate repos/$REPO/dependabot/alerts --jq '[.[] | select(.state == "open")] | group_by(.security_advisory.severity) | map({severity: .[0].security_advisory.severity, count: length})'
```

If remaining vulnerabilities exist at lower severity, create a new Jira ticket and repeat the process.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-vulnerabilities/references/severity-processing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

