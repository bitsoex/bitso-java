# Ticket Search

**Before creating a new ticket**, search for existing open tickets that may already cover the work.

## Search for Existing Tickets

Use `mcp_atlassian_searchJiraIssuesUsingJql` with these queries:

### For Dependabot Vulnerabilities (search by CVE)

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND (summary ~ "CVE-XXXX-XXXXX" OR description ~ "CVE-XXXX-XXXXX") ORDER BY created DESC
```

### For SonarQube Issues (search by rule and repo)

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "SonarQube" AND summary ~ "[repo-name]" ORDER BY created DESC
```

### For General KTLO Work in Same Repo

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND parent = "KTLO-EPIC-KEY" AND summary ~ "[repo-name]" ORDER BY created DESC
```

## Validation Checklist Before Creating

- [ ] Discovered user's Jira project key (Step 0)
- [ ] Searched for existing tickets with same CVE/vulnerability identifier
- [ ] Searched for existing tickets with same SonarQube rule in same repo
- [ ] Searched for any open ticket under current KTLO epic for same repo
- [ ] Verified project key matches user's discovered key
- [ ] Verified matching tickets are in open/in-progress state (not done/closed)

## If Existing Ticket Found

**Only create a new ticket if NO matching open ticket exists.**

If an existing ticket is found:

1. Use that ticket's key for the branch name
2. Add a comment to the ticket noting the new work being done
3. Skip to branch creation (Step 4)

## Find Current Sprint/Cycle Epic

Find the most recent KTLO or Tech Debt epic for the team:

```text
project = "PROJECT_KEY" AND issuetype = Epic AND summary ~ "KTLO" ORDER BY created DESC
```

Example: `[C6]: AI Enablement KTLO` for cycle 6
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/jira-integration/references/ticket-search.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

