# CodeRabbit Commit Message Formats

All commits that address CodeRabbit feedback must include proper attribution to create an audit trail.

## Contents

- [The Co-Author Trailer (Required)](#the-co-author-trailer-required)
- [Format 1: Local CLI Review (No Thread)](#format-1-local-cli-review-no-thread)
- [Format 2: Single PR Thread Fix](#format-2-single-pr-thread-fix)
- [Format 3: Multiple Threads (Batch)](#format-3-multiple-threads-batch)
- [Format 4: Severity-Based Batch](#format-4-severity-based-batch)
- [Status-Specific Messages](#status-specific-messages)
- [Quick Reference](#quick-reference)
- [Why This Matters](#why-this-matters)

---
## The Co-Author Trailer (Required)

Every commit addressing CodeRabbit feedback MUST include:

```text
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

This trailer:

- Gives CodeRabbit credit for identifying the issue
- Enables GitHub to show CodeRabbit as a contributor
- Allows CodeRabbit to recognize its suggestions were applied
- Creates a searchable audit trail

## Format 1: Local CLI Review (No Thread)

When fixing issues found by running `coderabbit --prompt-only` locally:

```text
fix: address CodeRabbit CLI review findings

- [path/file.js]: Brief description of fix
- [path/other.js]: Brief description of fix

Reviewed-by: CodeRabbit CLI (local)
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

**Key elements:**

- `Reviewed-by: CodeRabbit CLI (local)` - Indicates this was a local review
- No thread ID - Local reviews don't have conversation threads
- File list - Shows which files were fixed

**Example:**

```bash
git commit -m "fix: address CodeRabbit CLI review findings

- [src/utils.js]: Fixed regex lastIndex reset
- [src/parser.js]: Added null check before access
- [tests/utils.test.js]: Updated test expectations

Reviewed-by: CodeRabbit CLI (local)
Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

## Format 2: Single PR Thread Fix

When fixing a specific issue from a PR review thread:

```text
fix: address CodeRabbit PR feedback

Thread: PRRT_kwDONTxxxxxxx
- [path/file.js:42]: Brief description of fix

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

**Key elements:**

- `Thread: PRRT_xxx` - Links to the specific review thread
- Line number - Exact location that was fixed
- Single issue focus - One thread per commit if atomic

**Example:**

```bash
git commit -m "fix: address CodeRabbit PR feedback

Thread: PRRT_kwDONTgMnM5Kh9kj
- [src/auth/validator.js:127]: Added input validation before SQL query

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

## Format 3: Multiple Threads (Batch)

When fixing multiple issues in one commit:

```text
fix: address N [severity] CodeRabbit issues

Threads:
- PRRT_kwDOxxxxxx: Brief description
- PRRT_kwDOyyyyyy: Brief description
- PRRT_kwDOzzzzzz: Brief description

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

**Key elements:**

- Count and severity in subject - `3 critical`, `5 major`, etc.
- Thread list - Each thread ID with its fix description
- Grouped by severity - Process critical first, then major, then minor

**Example:**

```bash
git commit -m "fix: address 3 critical CodeRabbit issues

Threads:
- PRRT_kwDONTgMnM5Kh9kj: Fixed SQL injection in auth module
- PRRT_kwDONTgMnM5Kh9ab: Added rate limiting to API endpoint
- PRRT_kwDONTgMnM5Kh9cd: Removed hardcoded credentials

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

## Format 4: Severity-Based Batch

When processing all issues of one severity:

```text
fix: address all [severity] CodeRabbit issues (N total)

Files changed:
- path/file1.js
- path/file2.js
- path/file3.js

Threads: [list of IDs]

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

**Example:**

```bash
git commit -m "fix: address all major CodeRabbit issues (6 total)

Files changed:
- src/api/handlers.js
- src/utils/validators.js
- src/models/user.js

Threads: PRRT_kwDO1, PRRT_kwDO2, PRRT_kwDO3, PRRT_kwDO4, PRRT_kwDO5, PRRT_kwDO6

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>"
```

## Status-Specific Messages

### For wontfix/acknowledged issues

If you're acknowledging but not fixing:

```text
docs: acknowledge CodeRabbit feedback

Thread: PRRT_kwDOxxxxxx
Status: Acknowledged (will address in follow-up)
Reason: [Explanation of why not fixing now]

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

### For not-applicable issues

If the code has changed or the suggestion doesn't apply:

```text
chore: mark CodeRabbit comment as not-applicable

Thread: PRRT_kwDOxxxxxx
Status: Not applicable
Reason: [Code was refactored/removed/changed]

Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>
```

## Quick Reference

| Scenario | Subject Line |
|----------|--------------|
| Local CLI review | `fix: address CodeRabbit CLI review findings` |
| Single PR thread | `fix: address CodeRabbit PR feedback` |
| Multiple threads | `fix: address N severity CodeRabbit issues` |
| All of one severity | `fix: address all severity CodeRabbit issues` |
| Acknowledged | `docs: acknowledge CodeRabbit feedback` |
| Not applicable | `chore: mark CodeRabbit comment as not-applicable` |

## Why This Matters

When CodeRabbit reviews the PR after you push:

1. **It sees the co-author trailer** - Knows its suggestions were applied
2. **It sees the thread ID** - Can correlate the commit to its comment
3. **It sees "Reviewed-by: CodeRabbit CLI"** - Knows you ran local review
4. **It can verify fixes** - Re-checks the code to confirm the issue is resolved

This creates a complete audit trail from issue detection to resolution.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/references/commit-formats.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

