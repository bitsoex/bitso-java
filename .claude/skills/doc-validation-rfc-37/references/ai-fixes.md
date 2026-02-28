# AI-Assisted Documentation Fixes

Use AI assistants to automatically fix documentation violations.

## Contents

- [Overview](#overview)
- [Setup Atlassian MCP](#setup-atlassian-mcp)
- [Workflow](#workflow)
- [Common Fixes](#common-fixes)
- [Best Practices](#best-practices)
- [Example Session](#example-session)
- [Troubleshooting](#troubleshooting)
- [See Also](#see-also)

---
## Overview

This workflow combines:
- Bitso documentation linter (detects issues)
- Atlassian MCP (reads Confluence for context)
- AI assistant (applies fixes)

## Setup Atlassian MCP

Add to your IDE's MCP configuration:

**Cursor**: `~/.cursor/mcp.json`
**VS Code**: `~/.vscode/mcp.json`

```json
{
  "mcpServers": {
    "atlassian": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "https://mcp.atlassian.com/v1/sse"]
    }
  }
}
```

### First-Time Authentication

1. Restart your editor after adding config
2. Ask the AI to access Confluence (triggers OAuth)
3. Browser opens for Atlassian authorization
4. Grant access, return to editor
5. Credentials stored for future use

## Workflow

### Step 1: Run the Linter

```bash
doclinter --repo-path . --verbose
```

Example output:

```text
Overall Compliance Score: 75.0%

Violations Summary:
  • Total Violations: 8
  • Errors: 3
  • Warnings: 5

ERROR: docs/README.md:1 - META_CONFLUENCE_CONFIG_INCOMPLETE
ERROR: docs/api/auth.md:15 - FORMAT_CODE_LANG_MISSING
```

### Step 2: Ask AI to Fix

**Fix all violations:**

```text
Please fix all documentation violations reported by the linter.
Use the Confluence documentation as a reference for the correct patterns.
```

**Fix by category:**

```text
Fix all META_CONFLUENCE_CONFIG_INCOMPLETE violations by creating
a mark.toml file with proper space and parents configuration.
```

**Guided fixes:**

```text
Show me the first violation and explain what needs to be fixed.
Let me review before you apply the fix.
```

### Step 3: Iterate Until Clean

```bash
doclinter --repo-path . --verbose
# Should show: Overall Compliance Score: 100%
```

## Common Fixes

### Missing Confluence Configuration

AI prompt:

```text
Create a mark.toml file in docs/ with:
- space = "MM" (or appropriate space)
- parents = the correct hierarchy for this service
```

### Missing Code Block Language

AI prompt:

```text
Fix FORMAT_CODE_LANG_MISSING violations by adding appropriate
language identifiers to all code blocks (bash, json, yaml, etc.)
```

### Duplicate Page Names

AI prompt:

```text
Fix PAGE_NAME_DUPLICATE violations by making page titles more
specific. Prefix with service name or add context.
```

### Missing Local Execution

AI prompt:

```text
Create docs/how-tos/local-execution.md using the template from
the doc-validation-rfc-37 skill. Include prerequisites, setup,
build, run, and verification steps.
```

## Best Practices

1. **Be specific**: Tell AI exactly which violations to fix
2. **Provide context**: Mention service name, team, purpose
3. **Review changes**: Always review before committing
4. **Work in batches**: Fix one category at a time
5. **Re-run linter**: Verify fixes after each batch

## Example Session

```text
User: Run doclinter --repo-path . --verbose and show me the violations

AI: [runs command, shows 5 violations]

User: Fix the FORMAT_CODE_LANG_MISSING violations

AI: [adds language to 3 code blocks]

User: Now fix the META_CONFLUENCE_CONFIG_INCOMPLETE violation

AI: [creates docs/mark.toml with proper config]

User: Run the linter again to verify

AI: [shows 100% compliance]
```

## Troubleshooting

### AI can't access Confluence

1. Check MCP server is running (MCP servers panel)
2. Restart editor
3. Re-trigger OAuth by asking AI to read Confluence
4. Check for browser pop-up blockers

### AI makes incorrect fixes

1. Provide more context about the service
2. Reference the specific rule documentation
3. Ask AI to explain reasoning before applying

### Persistent violations

1. Check if `.doclinterrc.yml` has overrides
2. Verify file paths match expectations
3. Ask AI to explain the specific violation in detail

## See Also

- [Official AI Fixes Guide](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/how-tos/how-to-fix-documentation-using-ai.md)
- [Atlassian MCP Documentation](https://github.com/atlassian/mcp-server)
- [Validation Rules Reference](validation-rules.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/references/ai-fixes.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

