---
name: greenflag-dependabot
description: >
  Manage Dependabot PRs efficiently during Greenflag KTLO weeks. Covers squad-level
  grouping, catalog search for library ownership, and routine dependency updates.
  Use for non-security dependency updates; for CVE fixes, use fix-vulnerabilities skill.
compatibility: Java/Gradle projects with Dependabot enabled
metadata:
  version: "1.0.0"
  category: workflow
  tags:
    - dependabot
    - greenflag
    - ktlo
    - dependencies
    - gradle
---

# Greenflag Dependabot

Efficient dependency update management during Greenflag KTLO weeks.

## When to use this skill

- You're on Greenflag duty and need to process Dependabot PRs
- Triaging dependency updates by squad ownership
- Understanding which team owns a Bitso library
- Processing routine (non-security) version updates
- Reviewing squad-grouped Dependabot PRs

**For security vulnerabilities**: Use the `fix-vulnerabilities` skill instead (available in both Java and Node.js).

## Skill Contents

### Sections

- [When to use this skill](#when-to-use-this-skill)
- [Quick Start](#quick-start)
- [Greenflag Context](#greenflag-context)
- [Recent Improvements](#recent-improvements)
- [References](#references)
- [Related Skills](#related-skills)

### Available Resources

**📚 references/** - Detailed documentation
- [catalog search](references/catalog-search.md)
- [greenflag workflow](references/greenflag-workflow.md)
- [squad groups](references/squad-groups.md)

**🔧 scripts/** - Automation scripts
- [detect context](scripts/detect-context.ts)
- [generate changelog](scripts/generate-changelog.ts)
- [lib](scripts/lib)
- [list dependabot prs](scripts/list-dependabot-prs.ts)

---

## Quick Start

### 1. List Open Dependabot PRs (Context-Aware)

The skill automatically detects your context and filters PRs accordingly:

```bash
# Auto-detect context and list relevant PRs
mise run greenflag-dependabot:list-dependabot-prs

# Show all PRs (ignore context)
mise run greenflag-dependabot:list-dependabot-prs -- --all

# Filter to specific repo
mise run greenflag-dependabot:list-dependabot-prs -- --repo payments-api

# Output as JSON
mise run greenflag-dependabot:list-dependabot-prs -- --json
```

**Context detection priority:**
1. **Workspace repo** - If you're in a Bitso repo, shows only that repo's PRs
2. **User's squad** - Uses `git config user.email` to find your squad in estate-catalog, then shows PRs for your squad's repos
3. **All PRs** - Fallback if no context detected

### Manual filtering (alternative)

```bash
# List all open Dependabot PRs
gh pr list --author app/dependabot --state open

# Filter by squad group in PR title
gh pr list --author app/dependabot --state open | grep "squad-name"
```

### 2. Identify Library Ownership

Search the `bitso-gradle-catalogs/repos/` folder to find which repo publishes a library:

```bash
# Find where a Bitso library is published from
grep -r "com.bitso.library-name" bitso-gradle-catalogs/repos/
```

### 3. Review and Merge

For routine updates (minor/patch versions):

1. Check the PR diff for breaking changes
2. Verify CI passes
3. Merge if safe

For major versions or complex updates:

1. Check release notes
2. Consider creating a dedicated ticket
3. Coordinate with owning squad if needed

### 4. Generate Changelog for PR

When reviewing a Dependabot PR, generate a changelog showing what changed between versions:

```bash
# From PR number (auto-extracts library and versions)
mise run greenflag-dependabot:generate-changelog -- --pr 12345

# From library and version range
mise run greenflag-dependabot:generate-changelog -- \
  --lib business-account-models-api --from 2.3.0 --to 2.4.0

# Post as PR comment
mise run greenflag-dependabot:generate-changelog -- --pr 12345 --post-comment
```

The script:
1. Uses version history from `bitso-gradle-catalogs` to get commit hashes
2. Fetches commits between versions from the source repo
3. Filters to relevant changes (lib folder, gradle folder)
4. Generates formatted Markdown with commit links

### 5. Batch Similar Updates

Group related PRs for efficient review:

```bash
# List PRs by a specific group
gh pr list --author app/dependabot --state open --json number,title | \
  jq '.[] | select(.title | contains("jvm-generic-libraries"))'
```

## Greenflag Context

The [Greenflag process](https://bitsomx.atlassian.net/wiki/spaces/BAB/pages/4241653840/Greenflag+Bitso) assigns an engineer to handle unplanned work during on-call weeks.

**KTLO tasks include:**
- Upgrading dependencies
- Addressing security vulnerabilities
- Fixing simple incident remediation tasks
- Improving documentation

**Dependency updates fall under KTLO** - routine version bumps should be processed during Greenflag weeks to keep the codebase healthy.

### Priority Order

1. **Security alerts** - Use `fix-vulnerabilities` skill (highest priority)
2. **Breaking/Major updates** - May need coordination with product team
3. **Minor/Patch updates** - Safe to batch and merge during KTLO

## Recent Improvements

### Spring Major Version Blocking (PR #8691)

Spring major version updates (Spring Boot 4, Spring Framework 7) are now blocked. This prevents Dependabot from opening PRs for major upgrades that require coordinated migration efforts.

**Blocked patterns:** `org.springframework*` major versions

### Squad-Level Grouping (PR #8703)

Dependabot PRs are now grouped by squad ownership:

- `jvm-generic-libraries` - Shared platform libraries
- 22 squad-specific groups (e.g., `asset-management-squad`, `blackbird-squad`)
- External dependency groups (spring, grpc, aws, testing, logging)

This makes it easier to understand which team should review each PR.

## References

| Reference | Description |
|-----------|-------------|
| [references/greenflag-workflow.md](references/greenflag-workflow.md) | Weekly workflow for Greenflag engineers |
| [references/squad-groups.md](references/squad-groups.md) | Using squad-level Dependabot groups |
| [references/catalog-search.md](references/catalog-search.md) | Searching bitso-gradle-catalogs |

## Related Skills

| Skill | Purpose |
|-------|---------|
| `fix-vulnerabilities` | Security vulnerability fixes (CVEs) - available in Java and Node.js |
| `dependency-management` | Version catalogs and BOMs (Java) |
| [jira-integration](.claude/skills/jira-integration/SKILL.md) | Creating tickets for complex updates |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

