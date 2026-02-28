# Changelog Migration Guide

## Contents

- [Overview](#overview)
- [Migration Scenarios](#migration-scenarios)
- [Keep a Changelog to Common Changelog](#keep-a-changelog-to-common-changelog)
- [GitHub Releases to Changelog](#github-releases-to-changelog)
- [Git Tags Only](#git-tags-only)
- [Fresh Start (No History)](#fresh-start-no-history)
- [Rollout Strategy](#rollout-strategy)
- [Related](#related)

---
## Overview

This guide covers migrating repositories to the Common Changelog format. The migration approach depends on the repository's current state:

1. **Existing changelog** - Convert format while preserving history
2. **GitHub releases** - Import release notes as changelog entries
3. **Git tags only** - Retrofit from git commit history
4. **No versioning** - Start fresh with retrofit script

## Migration Scenarios

| Current State | Migration Effort | Approach |
|---------------|------------------|----------|
| Keep a Changelog format | Low | Minor format adjustments |
| Custom changelog format | Medium | Reformat entries, add references |
| GitHub releases with notes | High | Import and categorize release notes |
| Git tags, no changelog | Low | Run retrofit script |
| No versioning at all | None | Run retrofit, start with Unreleased |

## Keep a Changelog to Common Changelog

### Key Differences

| Aspect | Keep a Changelog | Common Changelog |
|--------|-----------------|------------------|
| Categories | Added, Changed, Deprecated, Removed, Fixed, Security | Changed, Added, Removed, Fixed |
| Category order | Any | Changed > Added > Removed > Fixed |
| References | Optional | Required (PR or commit) |
| Entry format | Free text | Imperative verb + reference |

### Conversion Steps

1. **Merge categories**:
   - `Deprecated` → `Changed` (with note about deprecation)
   - `Security` → `Fixed` (security fixes) or `Changed` (security improvements)

2. **Reorder categories** to: Changed, Added, Removed, Fixed

3. **Add references** to entries missing them:
   ```markdown
   # Before
   - Added new authentication method
   
   # After
   - Add new authentication method ([#123](https://github.com/org/repo/pull/123))
   ```

4. **Rewrite entries** in imperative mood:
   ```markdown
   # Before
   - The API now supports pagination
   
   # After
   - Add pagination support to API ([#456](https://github.com/org/repo/pull/456))
   ```

5. **Add header** if missing:
   ```markdown
   # Changelog
   
   > This project follows [Common Changelog](https://common-changelog.org/) with an
   > [Unreleased section extension](global/skills/changelog/references/unreleased-extension.md).
   ```

### Automated Conversion

Use the migrate script for automated conversion:

```bash
node global/skills/changelog/scripts/migrate.ts --input CHANGELOG.md --output CHANGELOG.new.md
```

Review the output and manually fix entries that couldn't be automatically converted.

### Example Conversion

**Before (Keep a Changelog):**
```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.2.0] - 2025-06-15

### Added
- New user dashboard

### Deprecated
- Old reporting API (use v2 instead)

### Security
- Fixed XSS vulnerability in input fields
```

**After (Common Changelog):**
```markdown
# Changelog

> This project follows [Common Changelog](https://common-changelog.org/).

## [1.2.0] - 2025-06-15

### Changed

- Deprecate old reporting API in favor of v2 ([#89](https://github.com/org/repo/pull/89))

### Added

- Add new user dashboard ([#85](https://github.com/org/repo/pull/85))

### Fixed

- Fix XSS vulnerability in input fields ([#90](https://github.com/org/repo/pull/90))
```

## GitHub Releases to Changelog

When a repository has GitHub releases but no changelog file, import the releases.

### Using the Import Script

```bash
node global/skills/changelog/scripts/import-releases.ts \
  --repo bitsoex/my-repo \
  --output CHANGELOG.md
```

### Manual Import Process

1. **Fetch releases** via GitHub API or CLI:
   ```bash
   gh release list --repo bitsoex/my-repo --limit 100
   ```

2. **For each release**, extract:
   - Version (tag name without 'v' prefix)
   - Date (published date)
   - Release notes (body)

3. **Categorize entries** from release notes:
   - Lines starting with "Add", "New", "Feature" → Added
   - Lines starting with "Fix", "Bug" → Fixed
   - Lines starting with "Remove", "Delete" → Removed
   - Everything else → Changed

4. **Add PR references** by cross-referencing git history:
   ```bash
   git log v1.0.0..v1.1.0 --oneline | grep '#'
   ```

### Handling Release Notes Formats

| Format | Conversion |
|--------|------------|
| Bullet points | Keep, add references |
| Prose paragraphs | Extract key changes, create bullets |
| Auto-generated | Use PR titles directly |
| Empty/minimal | Use git commit messages |

## Git Tags Only

When a repository has git tags but no changelog or releases.

### Retrofit from History

```bash
node global/skills/changelog/scripts/retrofit.ts \
  --version 1.0.0 \
  --date 2026-01-19 \
  --repo-url https://github.com/bitsoex/my-repo
```

### What Retrofit Does

1. Scans git history for PR merge commits
2. Extracts PR numbers and titles
3. Categorizes by conventional commit prefix (feat → Added, fix → Fixed)
4. Generates changelog with all PRs as entries

### Tag-Based Sectioning

If you want separate versions based on tags:

```bash
# Get commits between tags
git log v1.0.0..v1.1.0 --oneline --grep='#'

# Generate changelog section for each version range
```

## Fresh Start (No History)

For repositories with no versioning history.

### Steps

1. **Create initial changelog** from template:
   ```bash
   cp global/skills/changelog/assets/changelog-template.md CHANGELOG.md
   ```

2. **Run retrofit** to document existing history:
   ```bash
   pnpm run skills:changelog:retrofit
   ```

3. **Choose initial version**:
   - `1.0.0` - If the service is in production
   - `0.1.0` - If still in development
   - Match existing deployment version if known

4. **Add to version control**:
   ```bash
   git add CHANGELOG.md
   git commit -m "docs: add changelog with retroactive history"
   ```

### Starting with Unreleased Only

If you don't want to document past history:

```markdown
# Changelog

> This project follows [Common Changelog](https://common-changelog.org/).

## [Unreleased]

<!-- Future changes go here -->
```

## Rollout Strategy

### Phase 1: Pilot Repositories

Start with repositories that have existing versioning practices:

| Repository | Current State | Migration Approach |
|------------|---------------|-------------------|
| react-design-system | Keep a Changelog | Convert format |
| bitso-java | GitHub releases | Import releases |
| bitso-helm-chart | Git tags | Retrofit |

### Phase 2: Library Repositories

Migrate shared libraries and SDKs that benefit most from changelogs:
- `jvm-generic-libraries`
- `external-api-contract`
- `bitso-js`
- `bmjs`

### Phase 3: Service Repositories

Roll out to service repositories by technology:
1. Java services (largest group)
2. Node.js services
3. React applications

### Phase 4: Full Estate

Enable for all repositories with `managed: "repo-files"` in estate catalog.

### Enforcement Timeline

1. **Week 1-2**: Create changelogs for pilot repos
2. **Week 3-4**: Enable validation (warnings only)
3. **Week 5+**: Enable blocking validation in pre-push hooks

## Related

- [Common Changelog Specification](common-changelog-spec.md)
- [Unreleased Section Extension](unreleased-extension.md)
- [Changelog SKILL.md](.claude/skills/changelog-rfc-29/SKILL.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/references/migration-guide.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

