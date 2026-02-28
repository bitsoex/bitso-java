# Unreleased Section Extension

## Contents

- [What is this?](#what-is-this)
- [Why Common Changelog doesn't include Unreleased](#why-common-changelog-doesnt-include-unreleased)
- [Why we use it anyway](#why-we-use-it-anyway)
- [How to use Unreleased](#how-to-use-unreleased)
- [Future migration](#future-migration)
- [Validation](#validation)
- [Related](#related)

---
## What is this?

This document explains our intentional deviation from the Common Changelog specification by including an `[Unreleased]` section at the top of our changelogs.

## Why Common Changelog doesn't include Unreleased

The Common Changelog specification explicitly does NOT include an Unreleased section because:

1. **References can't be added**: A commit or PR describing itself in Unreleased cannot add self-references until after the fact
2. **Contributor burden**: First-time contributors can't be expected to update the changelog
3. **Bird's-eye view needed**: Writing changelogs requires seeing the whole picture, not individual changes

## Why we use it anyway

Despite these valid concerns, we use an Unreleased section for the following reasons:

### 1. Stacked PR Workflow

Our development often involves multiple PRs that are part of a larger feature:
- PR 1/4: Add infrastructure
- PR 2/4: Add feature A
- PR 3/4: Add feature B
- PR 4/4: Integration and cleanup

With stacked PRs, each PR adds to the Unreleased section rather than creating intermediate versions that would never be published.

### 2. Version Timing

Creating version entries before release workflows are established results in:
- Versions that don't correspond to actual releases
- Confusion about what version users should reference
- Misalignment between git tags and changelog entries

The Unreleased section provides a safe staging area until we're ready to cut a proper release.

### 3. Team Adoption

As this workflow is introduced across teams:
- Developers need time to learn the format
- Automation needs to be tested and refined
- The Unreleased section allows changes to accumulate without premature versioning

## How to use Unreleased

### Adding changes

When your PR is merged, add an entry to the Unreleased section:

```markdown
## [Unreleased]

### Added

- Add your new feature ([#123](https://github.com/org/repo/pull/123))
```

### Promoting to a release

When ready to release:

1. Change `[Unreleased]` to `[X.Y.Z] - YYYY-MM-DD`
2. Add a new empty `[Unreleased]` section above it
3. Update reference links at the bottom

Before:

```markdown
## [Unreleased]

### Added

- Add feature X ([#100](link))

## [1.0.0] - 2025-01-01
```

After:

```markdown
## [Unreleased]

## [1.1.0] - 2026-01-20

### Added

- Add feature X ([#100](link))

## [1.0.0] - 2025-01-01
```

## Future migration

Once release processes are formalized, we may:
1. Remove the Unreleased section requirement
2. Adopt a release-driven changelog workflow
3. Integrate with automated release tooling

This deviation is documented and intentional - not an oversight.

## Validation

Our changelog validation:
- Accepts `[Unreleased]` as a valid "version"
- Does not require a date for Unreleased
- Still validates all other Common Changelog rules

## Related

- [Common Changelog Specification](common-changelog-spec.md)
- [Changelog SKILL.md](.claude/skills/changelog-rfc-29/SKILL.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/references/unreleased-extension.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

