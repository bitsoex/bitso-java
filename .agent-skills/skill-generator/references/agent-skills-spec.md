<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/skill-generator/references/agent-skills-spec.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

# Agent Skills Specification Reference

This document summarizes the [Agent Skills specification](https://agentskills.io/specification) for quick reference when creating new skills.

## Overview

Agent Skills are a simple, open format for giving agents new capabilities and expertise. Skills are folders of instructions, scripts, and resources that agents can discover and use to perform tasks.

**Source**: [agentskills.io](https://agentskills.io/)

## Directory Structure

A skill is a directory containing at minimum a `SKILL.md` file:

```
skill-name/
├── SKILL.md          # Required: instructions + metadata
├── scripts/          # Optional: executable code
├── references/       # Optional: documentation
└── assets/           # Optional: templates, resources
```

## SKILL.md Format

The `SKILL.md` file must contain YAML frontmatter followed by Markdown content.

### Required Frontmatter

```yaml
---
name: skill-name
description: A description of what this skill does and when to use it.
---
```

### Full Frontmatter Example

```yaml
---
name: pdf-processing
description: Extract text and tables from PDF files, fill forms, merge documents.
license: Apache-2.0
compatibility: Requires pdfplumber, access to filesystem
metadata:
  author: example-org
  version: "1.0"
allowed-tools: Bash(pdfplumber:*) Read Write
---
```

## Field Specifications

### name (required)

| Constraint | Rule |
|------------|------|
| Length | 1-64 characters |
| Characters | Unicode lowercase alphanumeric and hyphens (`a-z`, `0-9`, `-`) |
| Start/End | Must not start or end with `-` |
| Consecutive | Must not contain `--` |
| Match | Must match parent directory name |

**Valid examples:**
- `pdf-processing`
- `data-analysis`
- `code-review`

**Invalid examples:**
- `PDF-Processing` (uppercase)
- `-pdf` (starts with hyphen)
- `pdf--processing` (consecutive hyphens)

### description (required)

| Constraint | Rule |
|------------|------|
| Length | 1-1024 characters |
| Content | Should describe what the skill does AND when to use it |
| Keywords | Include specific keywords for agent task matching |

**Good example:**
```yaml
description: Extracts text and tables from PDF files, fills PDF forms, and merges multiple PDFs. Use when working with PDF documents or when the user mentions PDFs, forms, or document extraction.
```

**Poor example:**
```yaml
description: Helps with PDFs.
```

### license (optional)

Short license name or reference to bundled license file.

```yaml
license: Apache-2.0
license: Proprietary. LICENSE.txt has complete terms
```

### compatibility (optional)

| Constraint | Rule |
|------------|------|
| Length | 1-500 characters if provided |
| Purpose | Indicate environment requirements |

```yaml
compatibility: Designed for Claude Code (or similar products)
compatibility: Requires git, docker, jq, and access to the internet
```

### metadata (optional)

Arbitrary key-value mapping for custom fields.

```yaml
metadata:
  author: example-org
  version: "1.0"
  category: documentation
```

### allowed-tools (optional, experimental)

Space-delimited list of pre-approved tools.

```yaml
allowed-tools: Bash(git:*) Bash(jq:*) Read
```

## Body Content

The Markdown body after frontmatter contains skill instructions. No format restrictions, but recommended sections include:

1. **Overview/Purpose** - What the skill does
2. **When to use** - Scenarios for activation
3. **Instructions** - Step-by-step procedures
4. **Examples** - Common use cases
5. **Available Scripts** - Bundled script documentation
6. **Configuration** - Customization options

### Writing Style

Use **imperative form** (commands/procedures), not second-person:

| ❌ Avoid | ✅ Preferred |
|---------|-------------|
| "You should first install..." | "To begin, install..." |
| "Make sure you have..." | "Prerequisites: ..." |
| "You'll need to create..." | "Create a directory..." |
| "Then you can run..." | "Execute the script..." |

## Optional Directories

### scripts/

Executable code that agents can run:
- Should be self-contained or clearly document dependencies
- Include helpful error messages
- Handle edge cases gracefully
- Common languages: Python, Bash, JavaScript

### references/

Additional documentation loaded on demand:
- `REFERENCE.md` - Detailed technical reference
- `FORMS.md` - Form templates or structured data
- Domain-specific files (`finance.md`, `legal.md`)

Keep individual files focused for efficient context usage.

### assets/

Static resources:
- Templates (document, configuration)
- Images (diagrams, examples)
- Data files (lookup tables, schemas)

## Progressive Disclosure

Skills use progressive disclosure to manage context efficiently:

| Stage | Content | Token Budget |
|-------|---------|--------------|
| **Discovery** | `name` + `description` | ~100 tokens |
| **Activation** | Full `SKILL.md` body | < 5000 tokens recommended |
| **Resources** | Referenced files | As needed |

### Guidelines

- Keep main `SKILL.md` under 500 lines
- Move detailed reference material to separate files
- Use relative paths for file references

## File References

Reference other files using relative paths from skill root:

```markdown
See [the reference guide](references/REFERENCE.md) for details.

Run the extraction script:
scripts/extract.py
```

Keep references one level deep from `SKILL.md`.

## Validation

Validate skills using the skills-ref reference library:

```bash
skills-ref validate ./my-skill
```

This checks:
- Valid YAML frontmatter
- Required fields present
- Naming conventions followed
- Directory name matches skill name

## Bitso-Specific Extensions

The ai-code-instructions system extends the Agent Skills spec with:

### Technology Targeting

```yaml
metadata:
  targeting:
    include:
      - repo: "bitsoex/specific-repo"
        paths: ["/src"]
    exclude:
      - repo: "bitsoex/other-repo"
```

### Additional Metadata Fields

```yaml
metadata:
  alwaysApply: false  # Include in all contexts
  globs:
    - "**/*.java"    # File pattern triggers
```

### Technology Hierarchy

Skills placed in technology directories inherit to child technologies:

```
global/skills/       → All repositories
├── java/skills/     → Java repositories only
└── javascript/skills/
    ├── nodejs/skills/    → Node.js repositories
    ├── reactjs/skills/   → React repositories
    └── react-native/skills/ → React Native repositories
```

## References

- [Agent Skills Overview](https://agentskills.io/)
- [Agent Skills Specification](https://agentskills.io/specification)
- [What are Skills?](https://agentskills.io/what-are-skills)
- [GitHub Repository](https://github.com/agentskills/agentskills)
