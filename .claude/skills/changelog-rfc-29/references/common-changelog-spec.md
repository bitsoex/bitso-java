# Common Changelog Specification

> Source: [common-changelog.org](https://common-changelog.org/) and [GitHub](https://github.com/vweevers/common-changelog)

## Contents

- [Overview](#overview)
- [Guiding Principles](#guiding-principles)
- [File Format](#file-format)
- [Release Format](#release-format)
- [Notice](#notice)
- [Change Groups](#change-groups)
- [Change Items](#change-items)
- [What to Exclude](#what-to-exclude)
- [What to Include](#what-to-include)
- [Imperative Mood Examples](#imperative-mood-examples)
- [Reference Links](#reference-links)

---
## Overview

Common Changelog is a style guide for changelogs, adapted from and a stricter subset of Keep a Changelog. It embraces the guiding principle that changelogs must be written by humans and for humans.

## Guiding Principles

1. Changelogs are for humans
2. Communicate the impact of changes
3. Sort content by importance
4. Skip content that isn't important
5. Link each change to further information

## File Format

- Filename must be `CHANGELOG.md`
- Content must be Markdown
- Must start with a first-level heading: `# Changelog`
- Releases must be sorted newest-first (semver order)

## Release Format

A release must start with a second-level heading:

```markdown
## [VERSION] - YYYY-MM-DD
```

Where:
- `VERSION` is semver-valid (without "v" prefix)
- `DATE` is in ISO 8601 format (YYYY-MM-DD)

Example:

```markdown
## [1.0.1] - 2019-08-24
```

## Notice

A release may have a notice - a single-sentence paragraph with emphasis:

```markdown
## [2.0.0] - 2020-07-23

_If you are upgrading: please see [`UPGRADING.md`](UPGRADING.md)._
```

## Change Groups

Change groups start with a third-level heading containing a category:

```markdown
### <category>
```

Valid categories (in order):
1. `Changed` - changes in existing functionality
2. `Added` - new functionality
3. `Removed` - removed functionality
4. `Fixed` - bug fixes

## Change Items

Each change is a list item that must:
1. Start with a present-tense verb (imperative mood)
2. Include at least one reference (PR, commit, issue)
3. Be self-describing (as if no category heading exists)

Example:

```markdown
- Fix infinite loop ([#194](https://github.com/owner/name/issues/194))
```

### Breaking Changes

Breaking changes must be prefixed with `**Breaking:** `:

```markdown
### Changed

- **Breaking:** emit `close` event after `end`
```

### References

References must be:
- Written after the change description
- Wrapped in parentheses
- Markdown links

Formats:

```markdown
# Commits
([`53bd922`](https://github.com/owner/name/commit/53bd922))

# Pull Requests or Issues
([#194](https://github.com/owner/name/issues/194))

# External tickets
([JIRA-837](https://example.atlassian.net/browse/JIRA-837))
```

### Authors

Author names are optional and written after references:

```markdown
- Fix infinite loop ([#194](link)) (Alice Meerkat)
```

## What to Exclude

Exclude maintenance changes not interesting to consumers:
- Dotfile changes (`.gitignore`, `.github/`)
- Changes to development-only dependencies
- Minor code style changes
- Formatting changes in documentation

## What to Include

Always include:
- Refactorings (may have unintentional side effects)
- Changes to supported runtime environments
- Code style changes using new language features
- New documentation for previously undocumented features

## Imperative Mood Examples

Use:
- `Add`, `Remove`, `Fix`, `Update`, `Change`
- `Refactor`, `Bump`, `Document`, `Deprecate`
- `Support`, `Enable`, `Disable`, `Improve`

Instead of:

```markdown
### Added
- Support of CentOS  ❌
- `write()` method   ❌
```

Write:

```markdown
### Added
- Support CentOS     ✓
- Add `write()` method  ✓
```

## Reference Links

Use reference-style links at the bottom for readability:

```markdown
## [1.0.1] - 2019-08-24

### Fixed

- Prevent segmentation fault upon `close()`

[1.0.1]: https://github.com/owner/name/releases/tag/v1.0.1
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/references/common-changelog-spec.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

