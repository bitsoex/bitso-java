# Project Discovery

Discover the user's Jira project key dynamically. **Never hardcode project keys.**

## Method 1: Query User's Recent Tickets (Preferred)

Use `mcp_atlassian_searchJiraIssuesUsingJql` to find the user's recent tickets:

```text
reporter = currentUser() ORDER BY created DESC
```

Or by assignee:

```text
assignee = currentUser() ORDER BY updated DESC
```

Parse the returned ticket keys to extract the project key prefix (the part before the hyphen and number).

## Method 2: Check User's Recent Merged PRs

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

## Method 3: Check Current Repository's Recent PRs

```bash
# Get recent PRs in the repo
gh pr list --state merged --limit 10 --json title,body

# Extract Jira keys from titles/bodies
# Common patterns: [PROJ-XXX], PROJ-XXX, /browse/PROJ-XXX
```

## Method 4: Ask the User (Fallback)

If discovery fails, ask the user:

```text
I couldn't automatically determine your Jira project key.
What is your team's Jira project key?
```

## Use Discovered Key

Once discovered, store and use the project key consistently:

```bash
# JIRA_PROJECT_KEY should be set from discovery, never hardcoded
# Example discovered values: ALTS, MMCC, SBP, COREXP, etc.
```

## Common Project Key Patterns

- 3-5 uppercase letters
- Examples: `PROJ`, `ALTS`, `MMCC`, `SBP`, `COREXP`
- Always followed by hyphen and number: `PROJ-123`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-integration/references/project-discovery.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

