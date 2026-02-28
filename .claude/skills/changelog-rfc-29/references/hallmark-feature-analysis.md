# Hallmark Feature Analysis

Analysis of [hallmark](https://github.com/vweevers/hallmark/) and [remark-common-changelog](https://github.com/vweevers/remark-common-changelog) features to improve our changelog skill.

## Contents

- [Key Features from Hallmark](#key-features-from-hallmark)
- [Semver Understanding](#semver-understanding)
- [Validation Rules](#validation-rules)
- [Test Patterns](#test-patterns)
- [Implementation Recommendations](#implementation-recommendations)
- [Related](#related)

---
## Key Features from Hallmark

### What Hallmark Provides

| Feature | Description | Our Status | Priority |
|---------|-------------|------------|----------|
| Semver parsing | Validate and compare versions | Implemented | High |
| Semver sorting | Auto-sort releases newest-first | Implemented | High |
| Release sorting | Reorder releases in correct order | Implemented | Medium |
| Definition sorting | Sort reference links at bottom | Not implemented | Medium |
| Empty release detection | Warn if release has no content | Partial | Medium |
| Fixture-based tests | Input/output file comparison | Implemented | High |

### Out of Scope

The following features are intentionally out of scope for this skill:

| Feature | Reason |
|---------|--------|
| Git tags/releases creation | Handled in publishing pipelines per project type |
| Git trailers parsing | Adds complexity without clear benefit |
| Jira autolink references | Jira integration handled elsewhere |
| remark AST transformation | Our string-based approach is sufficient |

### Focus Areas

1. **Semver understanding** - Parse, validate, compare, bump versions
2. **Better test patterns** - Fixture-based testing like Hallmark
3. **Validation improvements** - Sorting, empty detection, format rules

## Semver Understanding

### Semver Parsing

Hallmark uses semver for version validation and comparison.

```javascript
// Valid versions (no 'v' prefix)
'1.0.0'      // Major.Minor.Patch
'2.1.0-rc.1' // With prerelease
'3.0.0-beta.2+build.123' // With build metadata

// Invalid
'v1.0.0'     // Has 'v' prefix
'1.0'        // Missing patch
'1'          // Missing minor and patch
```

### Version Comparison

Releases must be sorted newest-first. Semver comparison rules:

```javascript
// Compare versions
function compareVersions(a, b) {
  const partsA = a.split('.').map(n => parseInt(n, 10));
  const partsB = b.split('.').map(n => parseInt(n, 10));
  
  for (let i = 0; i < 3; i++) {
    if (partsA[i] > partsB[i]) return -1; // a is newer
    if (partsA[i] < partsB[i]) return 1;  // b is newer
  }
  return 0; // equal
}

// Sort releases newest-first
releases.sort((a, b) => compareVersions(a.version, b.version));
```

### Version Bumping

Given a current version, calculate the next version:

```javascript
function bumpVersion(current, type) {
  const [major, minor, patch] = current.split('.').map(n => parseInt(n, 10));
  
  switch (type) {
    case 'major':
      return `${major + 1}.0.0`;
    case 'minor':
      return `${major}.${minor + 1}.0`;
    case 'patch':
      return `${major}.${minor}.${patch + 1}`;
    default:
      throw new Error(`Unknown bump type: ${type}`);
  }
}

// Examples
bumpVersion('1.2.3', 'patch') // => '1.2.4'
bumpVersion('1.2.3', 'minor') // => '1.3.0'
bumpVersion('1.2.3', 'major') // => '2.0.0'
```

### Prerelease Handling

```javascript
function isPrerelease(version) {
  return version.includes('-');
}

function getPrereleaseTag(version) {
  const match = version.match(/-([a-zA-Z]+)/);
  return match ? match[1] : null;
}

// Examples
isPrerelease('1.0.0')        // false
isPrerelease('1.0.0-rc.1')   // true
getPrereleaseTag('1.0.0-rc.1')   // 'rc'
getPrereleaseTag('2.0.0-beta.3') // 'beta'
```

### Implementation: semver.js

Create `.skills/changelog/scripts/semver.ts`:

```javascript
export function parseVersion(version) {
  const match = version.match(/^(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z0-9.-]+))?(?:\+(.+))?$/);
  if (!match) return null;
  
  return {
    major: parseInt(match[1], 10),
    minor: parseInt(match[2], 10),
    patch: parseInt(match[3], 10),
    prerelease: match[4] || null,
    build: match[5] || null,
    raw: version
  };
}

export function isValidVersion(version) {
  return parseVersion(version) !== null;
}

export function compareVersions(a, b) {
  const va = parseVersion(a);
  const vb = parseVersion(b);
  
  if (!va || !vb) return 0;
  
  // Compare major.minor.patch
  if (va.major !== vb.major) return vb.major - va.major;
  if (va.minor !== vb.minor) return vb.minor - va.minor;
  if (va.patch !== vb.patch) return vb.patch - va.patch;
  
  // Prerelease versions have lower precedence
  if (va.prerelease && !vb.prerelease) return 1;
  if (!va.prerelease && vb.prerelease) return -1;
  
  return 0;
}

export function bumpVersion(version, type) {
  const v = parseVersion(version);
  if (!v) throw new Error(`Invalid version: ${version}`);
  
  switch (type) {
    case 'major': return `${v.major + 1}.0.0`;
    case 'minor': return `${v.major}.${v.minor + 1}.0`;
    case 'patch': return `${v.major}.${v.minor}.${v.patch + 1}`;
    default: throw new Error(`Unknown type: ${type}`);
  }
}
```

## Validation Rules

### Hallmark's Linting Rules

| Rule | Description | Fixable | We Have |
|------|-------------|---------|---------|
| `title` | Must start with "# Changelog" | Yes | Yes |
| `release-heading-depth` | Releases must be h2 | No | Yes |
| `release-heading` | Format: `[version] - date` | No | Yes |
| `release-version` | Semver without v prefix | No | Yes |
| `release-version-link` | Version must be linked | Yes | No |
| `release-date` | Format YYYY-MM-DD | No | Yes |
| `latest-release-first` | Newest release at top | Yes | No |
| `latest-definition-first` | Newest definition at top | Yes | No |
| `unique-release` | No duplicate versions | No | No |
| `no-empty-release` | Release must have content | Yes | Partial |
| `group-heading` | Must be h3, text only | No | Yes |
| `group-heading-type` | Changed/Added/Removed/Fixed | No | Yes |
| `no-empty-group` | Group must have entries | No | No |
| `filename` | Must be CHANGELOG.md | No | Yes |

### Rules to Add

**High Priority:**

1. **Release sorting** - Detect and fix out-of-order releases
2. **Unique version check** - No duplicate version numbers
3. **Empty group detection** - Warn about empty category sections

**Medium Priority:**

1. **Definition sorting** - Sort reference links at bottom
2. **Link reference style** - Prefer `[1.0.0]` over inline links
3. **Empty release warning** - Flag releases without content

### Sorting Implementation

```javascript
function sortReleases(parsed) {
  // Sort by semver, newest first
  parsed.releases.sort((a, b) => {
    if (a.version === 'Unreleased') return -1;
    if (b.version === 'Unreleased') return 1;
    return compareVersions(a.version, b.version);
  });
  
  return parsed;
}

function checkReleasesAreSorted(parsed) {
  const versions = parsed.releases
    .filter(r => r.version !== 'Unreleased')
    .map(r => r.version);
  
  const sorted = [...versions].sort(compareVersions);
  
  return versions.every((v, i) => v === sorted[i]);
}
```

## Test Patterns

### Hallmark's Testing Approach

Hallmark uses fixture-based testing with input/output file pairs:

```
test/fixture/
  00-various-input.md    # Markdown with issues
  00-various-output.md   # Expected after fix
  01-empty-input.md      # Empty changelog
  01-empty-output.md     # After fix adds header
  02-minimum.md          # Valid minimal changelog
  03-duplicate-version.md # Has duplicate versions
```

### Test Structure

```javascript
import test from 'tape';
import fs from 'fs';
import path from 'path';

test('fixes various issues', (t) => {
  const input = fs.readFileSync('test/fixture/00-various-input.md', 'utf-8');
  const expected = fs.readFileSync('test/fixture/00-various-output.md', 'utf-8');
  
  const result = fixChangelog(input);
  
  t.is(result.content, expected);
  t.same(result.messages, [
    'Release date must have format YYYY-MM-DD'
  ]);
  t.end();
});
```

### Fixture Files We Should Add

Create `tests/fixtures/changelog/` directory:

| Fixture | Purpose |
|---------|---------|
| `valid-changelog.md` | Valid changelog, no errors |
| `with-unreleased.md` | Has Unreleased section |
| `multiple-releases.md` | Multiple version entries |
| `invalid-bad-version.md` | Invalid version format |
| `invalid-bad-date.md` | Invalid date format |
| `invalid-bad-category.md` | Invalid category name |
| `invalid-wrong-order.md` | Releases not sorted |
| `invalid-no-refs.md` | Entries without references |
| `unsorted-input.md` | Out of order releases |
| `unsorted-output.md` | After sorting fix |

### Test Examples to Implement

```javascript
// tests/changelog-validation.test.js
import { describe, it, expect } from 'vitest';
import { validateChangelog } from '../global/skills/changelog/scripts/validate.ts';
import fs from 'fs';
import path from 'path';

const fixturesDir = 'tests/fixtures/changelog';

describe('validateChangelog', () => {
  it('passes for valid changelog', async () => {
    const result = await validateChangelog(fixturesDir, {
      changelogPath: path.join(fixturesDir, 'valid-changelog.md')
    });
    expect(result.passed).toBe(true);
    expect(result.errors).toHaveLength(0);
  });
  
  it('detects invalid version format', async () => {
    const result = await validateChangelog(fixturesDir, {
      changelogPath: path.join(fixturesDir, 'invalid-bad-version.md')
    });
    expect(result.passed).toBe(false);
    expect(result.errors).toContain(expect.stringContaining('version'));
  });
  
  it('detects wrong category order', async () => {
    const result = await validateChangelog(fixturesDir, {
      changelogPath: path.join(fixturesDir, 'invalid-bad-category.md')
    });
    expect(result.passed).toBe(false);
    expect(result.errors).toContain(expect.stringContaining('order'));
  });
});

describe('semver utilities', () => {
  it('parses valid versions', () => {
    expect(parseVersion('1.0.0')).toEqual({
      major: 1, minor: 0, patch: 0,
      prerelease: null, build: null, raw: '1.0.0'
    });
  });
  
  it('rejects invalid versions', () => {
    expect(parseVersion('v1.0.0')).toBeNull();
    expect(parseVersion('1.0')).toBeNull();
  });
  
  it('compares versions correctly', () => {
    expect(compareVersions('2.0.0', '1.0.0')).toBeLessThan(0);
    expect(compareVersions('1.0.0', '2.0.0')).toBeGreaterThan(0);
    expect(compareVersions('1.0.0', '1.0.0')).toBe(0);
  });
  
  it('bumps versions correctly', () => {
    expect(bumpVersion('1.2.3', 'patch')).toBe('1.2.4');
    expect(bumpVersion('1.2.3', 'minor')).toBe('1.3.0');
    expect(bumpVersion('1.2.3', 'major')).toBe('2.0.0');
  });
});
```

### Current Test Coverage

Our existing tests in `tests/changelog.test.js` provide comprehensive coverage:

| Test Area | Test Count | Status |
|-----------|------------|--------|
| Parsing (`parse.js`) | 13 tests | Complete |
| Validation (`validate.js`) | 12 tests | Complete |
| Check Updated (`check-updated.js`) | 5 tests | Complete |
| Coverage (`coverage.js`) | 9 tests | Complete |
| Retrofit (`retrofit.js`) | 9 tests | Complete |
| Integration | 2 tests | Complete |
| Wrapper Module | 7 tests | Complete |

Our existing fixtures in `tests/fixtures/changelog/`:

- `valid-changelog.md` - Valid format, passes all rules
- `with-unreleased.md` - Has Unreleased section
- `multiple-releases.md` - 5 releases for order testing
- `invalid-bad-category.md` - Wrong category name
- `invalid-bad-version.md` - Invalid semver format
- `invalid-bad-date.md` - Wrong date format
- `invalid-no-refs.md` - Missing PR references
- `invalid-wrong-order.md` - Categories not in order
- `invalid-no-header.md` - Missing # Changelog header

### Tests to Add

Missing test coverage for new features:

```javascript
// tests/semver.test.js
describe('semver utilities', () => {
  describe('parseVersion', () => {
    it('parses simple version', () => {
      expect(parseVersion('1.0.0')).toEqual({
        major: 1, minor: 0, patch: 0,
        prerelease: null, build: null, raw: '1.0.0'
      });
    });
    
    it('parses prerelease version', () => {
      const v = parseVersion('2.0.0-rc.1');
      expect(v.prerelease).toBe('rc.1');
    });
    
    it('rejects invalid versions', () => {
      expect(parseVersion('v1.0.0')).toBeNull();
      expect(parseVersion('1.0')).toBeNull();
      expect(parseVersion('not-a-version')).toBeNull();
    });
  });
  
  describe('compareVersions', () => {
    it('sorts major versions', () => {
      expect(compareVersions('2.0.0', '1.0.0')).toBeLessThan(0);
    });
    
    it('sorts minor versions', () => {
      expect(compareVersions('1.2.0', '1.1.0')).toBeLessThan(0);
    });
    
    it('sorts patch versions', () => {
      expect(compareVersions('1.0.2', '1.0.1')).toBeLessThan(0);
    });
    
    it('prereleases sort after release', () => {
      expect(compareVersions('1.0.0-rc.1', '1.0.0')).toBeGreaterThan(0);
    });
  });
  
  describe('bumpVersion', () => {
    it('bumps patch', () => {
      expect(bumpVersion('1.2.3', 'patch')).toBe('1.2.4');
    });
    
    it('bumps minor and resets patch', () => {
      expect(bumpVersion('1.2.3', 'minor')).toBe('1.3.0');
    });
    
    it('bumps major and resets minor/patch', () => {
      expect(bumpVersion('1.2.3', 'major')).toBe('2.0.0');
    });
  });
});

// tests/changelog-sorting.test.js
describe('release sorting', () => {
  it('detects unsorted releases', async () => {
    const { checkReleasesAreSorted } = await importValidateModule();
    const content = readFixture('unsorted-releases.md');
    expect(checkReleasesAreSorted(content)).toBe(false);
  });
  
  it('fixes release order', async () => {
    const { sortReleases } = await importValidateModule();
    const input = readFixture('unsorted-input.md');
    const expected = readFixture('unsorted-output.md');
    expect(sortReleases(input)).toBe(expected);
  });
});
```

## Implementation Recommendations

### Phase 1: Semver Utilities (High Value, Low Effort)

1. Create `global/skills/changelog/scripts/semver.ts`
2. Implement `parseVersion`, `compareVersions`, `bumpVersion`
3. Add comprehensive tests for semver functions
4. Update `validate.js` to use proper semver validation

### Phase 2: Enhanced Validation (Medium Value, Medium Effort)

1. Add release sorting detection and auto-fix
2. Add duplicate version detection
3. Add empty group detection
4. Add definition sorting

### Phase 3: Fixture-Based Tests (High Value, Medium Effort)

1. Review existing test fixtures for completeness
2. Add missing fixture pairs (input/output)
3. Add tests for each validation rule
4. Add tests for fix functionality

### Deferred (Out of Scope)

- Git tags/releases creation (handled in publishing pipelines)
- Git trailers parsing
- Jira autolink references
- remark AST transformation

## Related

- [Common Changelog Specification](common-changelog-spec.md)
- [Migration Guide](migration-guide.md)
- [Changelog SKILL.md](.claude/skills/changelog-rfc-29/SKILL.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/references/hallmark-feature-analysis.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

