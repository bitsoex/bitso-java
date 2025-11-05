# Fix code quality issues identified by CodeRabbit CLI

**Description:** Fix code quality issues identified by CodeRabbit CLI

# Fix CodeRabbit Issues

## Prerequisites

- CodeRabbit CLI installed (see `global/rules/coderabbit-setup.md`)
- CodeRabbit authentication configured and validated
- Project has git repository initialized
- Working with a feature branch (not main/master)

## Workflow

### Get Context

```bash
git branch --show-current
git status
```

Ensure you're on a feature branch with uncommitted or recent changes.

### Run CodeRabbit Analysis

Run CodeRabbit with `--prompt-only` mode for AI-optimized output:

```bash
coderabbit --prompt-only --type uncommitted
```

**Key flags explained:**

- `--prompt-only` - Optimizes output for AI agents (token-efficient, includes locations and suggestions)
- `--type uncommitted` - Reviews only uncommitted changes (faster, focused)
- `--base main` - Compare against main branch (use `--base develop` if needed)

**Expected duration:** CodeRabbit analysis typically takes 7-30+ minutes depending on code changes.

### Analyze Issues

Once CodeRabbit completes, review the findings:

1. **Read the summary** - CodeRabbit lists issues by category
2. **Prioritize by severity**:
   - 🔴 Critical/Security issues → Fix first
   - 🟡 High-severity issues → Fix next
   - 🟢 Nits/style issues → Fix last (optional)
3. **Note locations** - Each issue includes file path and line number

### Fix in Small Batches

Work on **3-5 issues at a time**:

1. **Read the file** - Use file path from CodeRabbit output
2. **Understand the issue** - Review the problem description and suggestion
3. **Apply the fix** - Implement the suggested change or similar fix
4. **Run tests** - Verify changes:

   ```bash
   npm test        # Node.js
   gradle test     # Java
   ```

5. **Move to next issue** - Repeat for next batch

### Commit Changes

After each batch of fixes:

```bash
git add -A
git commit -m "fix: address CodeRabbit issues

- Issue 1: Brief description of fix
- Issue 2: Brief description of fix
- Issue 3: Brief description of fix"
git push
```

### Re-run Analysis (Optional)

After fixing a batch, optionally re-run to verify progress:

```bash
coderabbit --prompt-only --type uncommitted
```

**Typical workflow:**

- Fix batch 1 → Re-run (check progress)
- Fix batch 2 → Re-run (verify improvements)
- All critical issues fixed → Done

## Guidelines

- **Always use `--prompt-only`** - Optimizes for AI integration
- **Use `--type uncommitted`** - Reviews current changes first
- **Work in small batches** - Max 3-5 issues per commit
- **Run tests locally** - Verify fixes before pushing
- **Commit frequently** - Small, focused commits
- **Focus on severity** - Critical/High > Medium > Low/Nits

## Troubleshooting

### CodeRabbit not finding issues

1. **Verify authentication**: `coderabbit auth status`
2. **Check git status**: `git status` - Must have changes tracked
3. **Verify base branch**: `git branch -a` - Confirm target branch exists
4. **Try different scope**:

   ```bash
   coderabbit --prompt-only --type all        # Include committed changes
   coderabbit --prompt-only --base develop    # Different base branch
   ```

### CodeRabbit CLI not installed

See **Prerequisites** → `global/rules/coderabbit-setup.md` for installation steps.

### Authentication issues

1. Run: `coderabbit auth status`
2. If not authenticated: `coderabbit auth login`
3. Follow the browser flow to authorize with GitHub
4. Return to terminal and paste token

### Slow performance

- Use `--type uncommitted` to focus on current changes
- Break changes into smaller feature branches
- Target specific files: `coderabbit --prompt-only --include "src/**/*.ts"`

## Integration with CI/CD

After fixing and pushing:

1. **PR validation** - CI runs CodeRabbit automatically
2. **Quality gates** - Check CI status before merging
3. **Re-review if needed** - Update PR with fixes if CI catches more issues

## Related

- **Setup**: `global/rules/coderabbit-setup.md`
- **Readiness Check**: `global/scripts/check-coderabbit-readiness.sh`
- **CodeRabbit Docs**: <https://docs.coderabbit.ai/cli/overview>
