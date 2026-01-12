---
name: skill-generator
description: >
  Generate new Agent Skills following the agentskills.io specification and Bitso conventions.
  Use when creating new skills for the ai-code-instructions repository to ensure consistent
  structure, proper frontmatter, and integration with the existing architecture.
compatibility: Requires Node.js 24+; requires knowledge of Agent Skills specification and Bitso's technology hierarchy
metadata:
  version: "1.0"
  targeting:
    include:
      - repo: "bitsoex/ai-code-instructions"
---

# Skill Generator

This skill provides expert guidance for creating new Agent Skills that comply with the [agentskills.io specification](https://agentskills.io/specification) and integrate properly with Bitso's AI code instructions architecture.

## When to use this skill

- Creating a new skill for the ai-code-instructions repository
- Ensuring skill compliance with the Agent Skills specification
- Understanding Bitso's skill targeting and technology hierarchy
- Migrating existing documentation to skill format

## Instructions

### Step 1: Determine Skill Placement

Choose the appropriate technology directory based on scope:

| Directory | Use Case |
|-----------|----------|
| `global/skills/` | Skills for all repositories |
| `java/skills/` | Java-specific skills |
| `nodejs/skills/` | Node.js-specific skills |
| `javascript/skills/` | Shared JS/TS skills |
| `reactjs/skills/` | React-specific skills |
| `python/skills/` | Python-specific skills |
| `go/skills/` | Go-specific skills |

### Step 2: Create Directory Structure

Create the skill directory with required and optional components:

```bash
# Minimum structure
mkdir -p {technology}/skills/{skill-name}
touch {technology}/skills/{skill-name}/SKILL.md

# Full structure (optional)
mkdir -p {technology}/skills/{skill-name}/{scripts,references,assets}
```

### Step 3: Write SKILL.md

Create the SKILL.md file following the Agent Skills specification:

```yaml
---
name: my-skill-name
description: >
  A clear description of what this skill does and when to use it.
  Include keywords that help agents identify relevant tasks.
compatibility: Environment requirements (optional)
metadata:
  version: "1.0"
  # Optional: targeting for specific repos
  targeting:
    include:
      - repo: "bitsoex/specific-repo"
---

# Skill Title

## When to use this skill

- Scenario 1
- Scenario 2

## Instructions

### Step 1: First Step

Detailed instructions...

### Step 2: Second Step

More instructions...

## Available Scripts

| Script | Description |
|--------|-------------|
| `scripts/example.sh` | What it does |

## Examples

### Example 1: Common Use Case

```bash
# Example command or code
```
```

### Step 4: Add Supporting Files (Optional)

#### Node.js Implementation (`.scripts/lib/skills/`)

For skills that need programmatic functionality, create a Node.js module:

```javascript
// .scripts/lib/skills/my-skill.js
const { logger, colors } = require('./utils');

async function runMyCheck(targetDir) {
  logger.info('Running my skill check...');
  // Implementation
  return { passed: true };
}

module.exports = { runMyCheck };
```

Then register it in `.scripts/lib/skills/index.js` and `.scripts/skills-cli.js`.

#### References (`references/`)

Add detailed documentation for complex topics:

```markdown
# references/detailed-guide.md

Detailed information that would bloat the main SKILL.md...
```

#### Assets (`assets/`)

Include templates, configurations, or static resources:

```
assets/
├── templates/
│   └── example-template.txt
└── config/
    └── default-config.json
```

### Step 5: Validate the Skill

Run validation to ensure the skill follows all requirements:

```bash
# Validate all skills in repository
npm run skills:validate

# Or use the CLI directly
node .scripts/skills-cli.js skill-generator validate
```

Validation checks:
1. **Name format**: Lowercase letters, numbers, hyphens only (1-64 chars), must match directory name
2. **Description**: Required, non-empty, max 1024 characters
3. **Compatibility**: Required, non-empty, max 500 characters
4. **Metadata.version**: Required, non-empty semantic version
5. **No author field**: `metadata.author` is forbidden
6. **References structure**: Global skills use technology subfolders (java/, typescript/, etc.)
7. **Broken links**: All internal references point to existing files

### Step 6: Test the Skill

1. Run validation: `npm run skills:validate`
2. Review the SKILL.md for clarity
3. Check that referenced files exist
4. Run tests: `npm test`

## Agent Skills Specification Reference

For the complete specification, see [references/agent-skills-spec.md](references/agent-skills-spec.md).

### Required Frontmatter Fields

| Field | Required | Constraints |
|-------|:--------:|-------------|
| `name` | Yes | 1-64 chars, lowercase, hyphens allowed, no consecutive hyphens |
| `description` | Yes | 1-1024 chars, describes what skill does and when to use it |
| `compatibility` | Yes | 1-500 chars, describes environment requirements |
| `metadata.version` | Yes | Semantic version string (e.g., "1.0", "2.1.0") |

### Forbidden Frontmatter Fields

| Field | Reason |
|-------|--------|
| `metadata.author` | Ownership is tracked via git history |

### Optional Frontmatter Fields

| Field | Purpose |
|-------|---------|
| `license` | License name or reference |
| `metadata.targeting` | Repo/path targeting configuration |
| `allowed-tools` | Pre-approved tools (experimental) |

### Version Bump Policy

When modifying a skill's content, you must bump the `metadata.version`:

| Change Type | Version Bump | Example |
|-------------|--------------|---------|
| Bug fixes, typos | Patch | 1.0.0 → 1.0.1 |
| New content, features | Minor | 1.0.0 → 1.1.0 |
| Breaking changes, restructuring | Major | 1.0.0 → 2.0.0 |

**Enforcement:**
- Pre-commit/pre-push: Version presence is validated
- PR-level: Version bump is checked when skill content changes (via CI)

### Best Practices

1. **Keep SKILL.md focused**: Under 500 lines, move details to references
2. **Use imperative form**: "Run the script" not "You should run"
3. **Include examples**: Show common use cases
4. **Document prerequisites**: What's needed before using the skill
5. **Reference other skills**: Link to related skills when appropriate
6. **Bump version on changes**: Update `metadata.version` when modifying content

## Bitso-Specific Conventions

### Technology Hierarchy

Skills inherit through the technology tree:

```
global
├── java
├── python
├── go
└── javascript
    ├── nodejs
    ├── reactjs
    └── react-native
```

A skill in `javascript/skills/` is available to nodejs, reactjs, and react-native repos.

### Targeting Specific Repositories

Use `metadata.targeting` to limit skill distribution:

```yaml
metadata:
  targeting:
    include:
      - repo: "bitsoex/specific-repo"
        paths: ["/"]  # Optional: limit to specific paths
    exclude:
      - repo: "bitsoex/excluded-repo"
```

### Frontmatter Format Compatibility

The ai-code-instructions system supports both legacy and Agent Skills formats:

```yaml
# Agent Skills format (preferred)
metadata:
  alwaysApply: false
  globs:
    - "**/*.java"
  targeting:
    include:
      - repo: "bitsoex/example"

# Legacy format (still supported)
alwaysApply: false
globs:
  - "**/*.java"
targeting:
  include:
    - repo: "bitsoex/example"
```

### Integration with Quality Gateway

Skills can be referenced by the quality-gateway orchestrator. To integrate:

1. Place the skill in `global/skills/` or appropriate technology directory
2. Add lifecycle hooks if needed (pre-edit, post-edit, on-stop)
3. Include scripts that the gateway can invoke

## Examples

### Example 1: Creating a Global Skill

```bash
# Create structure
mkdir -p global/skills/my-new-skill/scripts
cd global/skills/my-new-skill

# Create SKILL.md
cat > SKILL.md << 'EOF'
---
name: my-new-skill
description: >
  Description of what this skill does and when to use it.
compatibility: Works with any codebase
metadata:
  version: "1.0"
---

# My New Skill

## When to use this skill

- Use case 1
- Use case 2

## Instructions

### Step 1: Do Something

Instructions here...

## Examples

### Example: Basic Usage

\`\`\`bash
# Example command
\`\`\`
EOF

# Make scripts executable
chmod +x scripts/*.sh 2>/dev/null || true
```

### Example 2: Targeted Skill for Specific Repos

```yaml
---
name: payments-workflow
description: >
  Specialized workflow for payment processing services.
  Use when working with payment-related code changes.
metadata:
  targeting:
    include:
      - repo: "bitsoex/payments-api"
      - repo: "bitsoex/payments-gateway"
---
```

### Example 3: Technology-Specific Skill

```bash
# Place in java/skills/ for Java-only distribution
mkdir -p java/skills/spring-boot-patterns
```

## Troubleshooting

### Skill not appearing in target repo

1. Check targeting configuration
2. Verify repo is in `repo_map.json` or `repo-overrides.json`
3. Ensure technology hierarchy is correct
4. Run `npm run convert` to regenerate output

### Validation errors

1. Check name format (lowercase, hyphens only)
2. Verify description is non-empty
3. Ensure YAML frontmatter is valid
4. Confirm directory name matches skill name

### Scripts not working

1. Check Node.js module is properly exported in `.scripts/lib/skills/index.js`
2. Check skill is registered in `.scripts/skills-cli.js`
3. Run with DEBUG=1 for verbose output: `DEBUG=1 npm run skills:validate`
