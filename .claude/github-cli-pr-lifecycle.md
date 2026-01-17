# GitHub CLI PR Lifecycle


# GitHub CLI PR Lifecycle

Best practices for AI agents using GitHub CLI (`gh`) to manage pull requests, CI checks, CodeRabbit reviews, and reviewer interactions.

## Quick Reference

- Use 🤖 emoji in all AI-assisted commits and PRs
- Auto-assign PRs using `gh api user --jq '.login'`
- Use echo wrapper for `gh` commands: `echo "..." ; gh command ; echo "Done"`
- Always specify `--repo owner/repo`

## 📚 Full Documentation

For complete guidelines, scripts, and references, see the skill:

```
.agent-skills/pr-lifecycle/SKILL.md
```

The skill includes:
- **SKILL.md** - Complete instructions and quick start
- **scripts/** - Executable automation scripts
- **references/** - Detailed documentation
- **assets/** - Templates and resources

> **Note**: This is a shallow reference. The full content is maintained in the skill to avoid duplication.


---
*This rule is part of the java category.*
*Source: global/rules/github-cli-pr-lifecycle.md*

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/rules/github-cli-pr-lifecycle.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
