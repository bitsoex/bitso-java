# Update documentation to match code changes

**Description:** Update documentation to match code changes

# Sync Docs

Update documentation to reflect recent code changes.

## Purpose

This command invokes the **doc-sync skill** to:
- Identify documentation gaps
- Update API documentation
- Sync README with current state

## Skill Location

```
.agent-skills/doc-sync/
```

## Quick Workflow

### Step 1: Identify Changes

```bash
# See what files changed recently
git diff --name-only HEAD~5

# Or since last release
git diff --name-only v1.0.0..HEAD
```

### Step 2: Check Documentation Status

For each changed file, verify:
- [ ] Public functions have docstrings/JSDoc
- [ ] README reflects current functionality
- [ ] API docs are up to date
- [ ] Breaking changes are documented

### Step 3: Update Documentation

Follow patterns in `.agent-skills/doc-sync/references/` for:
- JSDoc (TypeScript/JavaScript)
- Javadoc (Java)
- Docstrings (Python)
- GoDoc (Go)

## Documentation Checklist

### Code Documentation

- [ ] All public APIs have documentation
- [ ] Parameters are described
- [ ] Return values are documented
- [ ] Exceptions/errors are listed

### Project Documentation

- [ ] README is accurate
- [ ] Installation instructions work
- [ ] Usage examples are current
- [ ] Configuration options are listed

### Change Documentation

- [ ] CHANGELOG updated (if applicable)
- [ ] Breaking changes noted
- [ ] Migration guide provided (if needed)

## Documentation Patterns

### JavaScript/TypeScript (JSDoc)

```typescript
/**
 * Brief description of the function.
 * 
 * @param input - Description of the input parameter
 * @returns Description of the return value
 * @throws {Error} When something goes wrong
 * 
 * @example
 * const result = myFunction('input');
 */
function myFunction(input: string): string {
  // ...
}
```

### Python (Docstrings)

```python
def my_function(input: str) -> str:
    """Brief description of the function.

    Args:
        input: Description of the input parameter

    Returns:
        Description of the return value

    Raises:
        ValueError: When something goes wrong

    Example:
        >>> result = my_function('input')
    """
    pass
```

### Java (Javadoc)

```java
/**
 * Brief description of the method.
 *
 * @param input description of the input parameter
 * @return description of the return value
 * @throws IllegalArgumentException when something goes wrong
 */
public String myMethod(String input) {
    // ...
}
```

## Related Commands

| Command | Purpose |
|---------|---------|
| `/quality-check` | Run full quality gate |
| `/add-tests` | Generate missing tests |

## Skill Contents

| Resource | Description |
|----------|-------------|
| `SKILL.md` | Full doc-sync documentation |
| `references/` | Technology-specific documentation patterns |

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/sync-docs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
