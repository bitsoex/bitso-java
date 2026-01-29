#!/usr/bin/env node
/**
 * Semver Utilities
 * 
 * Parse, validate, compare, and bump semantic versions.
 * Follows semver 2.0.0 specification: https://semver.org/
 */

import { SEMVER_REGEX } from './constants.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface ParsedVersion {
  major: number;
  minor: number;
  patch: number;
  prerelease: string | null;
  build: string | null;
  raw: string;
}

export type BumpType = 'major' | 'minor' | 'patch';

export interface NextVersionOptions {
  hasBreaking?: boolean;
  hasFeatures?: boolean;
  hasFixes?: boolean;
}

// ═══════════════════════════════════════════════════════════════════════════
// Parsing Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse a version string into components.
 * 
 * @example
 * parseVersion('1.2.3')
 * // => { major: 1, minor: 2, patch: 3, prerelease: null, build: null, raw: '1.2.3' }
 * 
 * parseVersion('2.0.0-rc.1+build.456')
 * // => { major: 2, minor: 0, patch: 0, prerelease: 'rc.1', build: 'build.456', raw: '2.0.0-rc.1+build.456' }
 * 
 * parseVersion('v1.0.0')  // Invalid - has 'v' prefix
 * // => null
 */
export function parseVersion(version: string): ParsedVersion | null {
  if (typeof version !== 'string') {
    return null;
  }
  
  const match = version.match(SEMVER_REGEX);
  if (!match) {
    return null;
  }
  
  return {
    major: parseInt(match[1]!, 10),
    minor: parseInt(match[2]!, 10),
    patch: parseInt(match[3]!, 10),
    prerelease: match[4] ?? null,
    build: match[5] ?? null,
    raw: version
  };
}

/**
 * Check if a version string is valid semver.
 * 
 * @example
 * isValidVersion('1.0.0')      // => true
 * isValidVersion('2.0.0-rc.1') // => true
 * isValidVersion('v1.0.0')     // => false (has 'v' prefix)
 * isValidVersion('1.0')        // => false (missing patch)
 */
export function isValidVersion(version: string): boolean {
  return parseVersion(version) !== null;
}

/**
 * Check if a version is a prerelease.
 * 
 * @example
 * isPrerelease('1.0.0')      // => false
 * isPrerelease('1.0.0-rc.1') // => true
 * isPrerelease('2.0.0-beta') // => true
 */
export function isPrerelease(version: string): boolean {
  const parsed = parseVersion(version);
  return parsed !== null && parsed.prerelease !== null;
}

/**
 * Get the prerelease tag from a version.
 * 
 * @example
 * getPrereleaseTag('1.0.0-rc.1')   // => 'rc'
 * getPrereleaseTag('2.0.0-beta.3') // => 'beta'
 * getPrereleaseTag('1.0.0')        // => null
 */
export function getPrereleaseTag(version: string): string | null {
  const parsed = parseVersion(version);
  if (!parsed || !parsed.prerelease) {
    return null;
  }
  // Extract the alphabetic prefix before any numeric part
  const match = parsed.prerelease.match(/^([a-zA-Z]+)/);
  return match ? match[1]! : null;
}

// ═══════════════════════════════════════════════════════════════════════════
// Comparison Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Compare two version strings for sorting.
 * 
 * Returns negative if a > b (a is newer)
 * Returns positive if a < b (b is newer)
 * Returns 0 if equal
 * 
 * Prerelease versions have lower precedence than release versions:
 * 1.0.0-rc.1 < 1.0.0
 * 
 * @example
 * // Sort newest first
 * ['1.0.0', '2.0.0', '1.5.0'].sort(compareVersions)
 * // => ['2.0.0', '1.5.0', '1.0.0']
 * 
 * compareVersions('2.0.0', '1.0.0')     // => -1 (2.0.0 is newer)
 * compareVersions('1.0.0', '2.0.0')     // => 1  (2.0.0 is newer)
 * compareVersions('1.0.0-rc.1', '1.0.0') // => 1  (1.0.0 is newer, rc is prerelease)
 */
export function compareVersions(a: string, b: string): number {
  const va = parseVersion(a);
  const vb = parseVersion(b);
  
  // Invalid versions sort last
  if (!va && !vb) {
    return 0;
  }
  if (!va) {
    return 1;
  }
  if (!vb) {
    return -1;
  }
  
  // Compare major.minor.patch
  if (va.major !== vb.major) {
    return vb.major - va.major; // Negative if a is newer
  }
  if (va.minor !== vb.minor) {
    return vb.minor - va.minor;
  }
  if (va.patch !== vb.patch) {
    return vb.patch - va.patch;
  }
  
  // Prerelease versions have lower precedence than release
  // A version without prerelease is newer than one with prerelease
  if (va.prerelease && !vb.prerelease) {
    return 1; // b is newer (no prerelease)
  }
  if (!va.prerelease && vb.prerelease) {
    return -1; // a is newer (no prerelease)
  }
  
  // Both have prerelease or both don't
  if (va.prerelease && vb.prerelease) {
    return comparePrerelease(va.prerelease, vb.prerelease);
  }
  
  return 0;
}

/**
 * Compare prerelease identifiers following semver 2.0.0 spec.
 * 
 * Rules:
 * - Numeric identifiers are compared as integers
 * - Alphanumeric identifiers are compared lexically
 * - Numeric identifiers always have lower precedence than alphanumeric
 * - A larger set of identifiers has higher precedence (if all preceding are equal)
 */
function comparePrerelease(a: string, b: string): number {
  const aIds = a.split('.');
  const bIds = b.split('.');
  const len = Math.max(aIds.length, bIds.length);
  
  for (let i = 0; i < len; i++) {
    const aId = aIds[i];
    const bId = bIds[i];
    
    // Fewer identifiers = lower precedence (comes after in newest-first sort)
    if (aId === undefined) { return 1; }
    if (bId === undefined) { return -1; }
    
    const aIsNum = /^\d+$/.test(aId);
    const bIsNum = /^\d+$/.test(bId);
    
    if (aIsNum && bIsNum) {
      // Both numeric: compare as integers
      const diff = parseInt(bId, 10) - parseInt(aId, 10);
      if (diff !== 0) { return diff; }
    } else if (aIsNum && !bIsNum) {
      // Numeric < alphanumeric (numeric comes after in newest-first)
      return 1;
    } else if (!aIsNum && bIsNum) {
      return -1;
    } else {
      // Both alphanumeric: compare lexically
      const diff = bId.localeCompare(aId);
      if (diff !== 0) { return diff; }
    }
  }
  
  return 0;
}

// ═══════════════════════════════════════════════════════════════════════════
// Version Bumping
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Bump a version by type.
 * 
 * @example
 * bumpVersion('1.2.3', 'patch') // => '1.2.4'
 * bumpVersion('1.2.3', 'minor') // => '1.3.0'
 * bumpVersion('1.2.3', 'major') // => '2.0.0'
 * bumpVersion('1.0.0-rc.1', 'patch') // => '1.0.1' (strips prerelease)
 */
export function bumpVersion(version: string, type: BumpType): string {
  const v = parseVersion(version);
  if (!v) {
    throw new Error(`Invalid version: ${version}`);
  }
  
  switch (type) {
    case 'major':
      return `${v.major + 1}.0.0`;
    case 'minor':
      return `${v.major}.${v.minor + 1}.0`;
    case 'patch':
      return `${v.major}.${v.minor}.${v.patch + 1}`;
    default:
      throw new Error(`Unknown bump type: ${type}. Must be 'major', 'minor', or 'patch'`);
  }
}

/**
 * Sort an array of versions newest-first.
 * 
 * @example
 * sortVersions(['1.0.0', '3.0.0', '2.1.0', '2.0.0'])
 * // => ['3.0.0', '2.1.0', '2.0.0', '1.0.0']
 */
export function sortVersions(versions: string[]): string[] {
  return [...versions].sort(compareVersions);
}

/**
 * Check if versions are sorted newest-first.
 * 
 * @example
 * areVersionsSorted(['3.0.0', '2.0.0', '1.0.0']) // => true
 * areVersionsSorted(['1.0.0', '2.0.0', '3.0.0']) // => false
 */
export function areVersionsSorted(versions: string[]): boolean {
  const sorted = sortVersions(versions);
  return versions.every((v, i) => v === sorted[i]);
}

/**
 * Get the next version based on change type.
 * 
 * @example
 * suggestNextVersion('1.2.3', { hasBreaking: true })  // => '2.0.0'
 * suggestNextVersion('1.2.3', { hasFeatures: true })  // => '1.3.0'
 * suggestNextVersion('1.2.3', { hasFixes: true })     // => '1.2.4'
 */
export function suggestNextVersion(version: string, options: NextVersionOptions = {}): string {
  const { hasBreaking = false, hasFeatures = false, hasFixes = false } = options;
  
  if (hasBreaking) {
    return bumpVersion(version, 'major');
  }
  if (hasFeatures) {
    return bumpVersion(version, 'minor');
  }
  if (hasFixes) {
    return bumpVersion(version, 'patch');
  }
  
  // Default to patch if nothing specified
  return bumpVersion(version, 'patch');
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/semver.ts
// To modify, edit the source file and run the distribution workflow

