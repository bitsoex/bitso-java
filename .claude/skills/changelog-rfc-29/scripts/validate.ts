#!/usr/bin/env node
/**
 * Changelog Validator
 * 
 * Validates CHANGELOG.md format against Common Changelog specification
 * with our Unreleased section extension.
 */

import fs from 'fs';
import path from 'path';
import { parseChangelog, type Change, type ChangeGroup, type Release } from './parse.ts';
import { isValidVersion, areVersionsSorted, compareVersions } from './semver.ts';
import {
  VALID_CATEGORIES,
  ISO_DATE_REGEX,
  startsWithImperativeVerb,
  truncate,
  TRUNCATE_LENGTH
} from './constants.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface ValidationResult {
  passed: boolean;
  errors: string[];
  warnings: string[];
}

export interface ValidationOptions {
  isUnreleased?: boolean;
}

interface ReleaseLines {
  version: string;
  date: string | null;
  lines: string[];
}

// ═══════════════════════════════════════════════════════════════════════════
// Helper Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Create a validation result.
 */
function createResult(errors: string[], warnings: string[] = []): ValidationResult {
  return {
    passed: errors.length === 0,
    errors,
    warnings
  };
}

/**
 * Check if the passed array is the full valid categories list
 * (used to skip order checking when not tracking previous categories).
 */
function isFullCategoriesList(categories: string[]): boolean {
  return VALID_CATEGORIES.every(c => categories.includes(c));
}

/**
 * Check if category is out of order compared to previous categories.
 */
function isCategoryOutOfOrder(category: string, previousCategories: string[]): boolean {
  const currentIndex = VALID_CATEGORIES.indexOf(category as typeof VALID_CATEGORIES[number]);
  return previousCategories.some(prev => 
    VALID_CATEGORIES.indexOf(prev as typeof VALID_CATEGORIES[number]) > currentIndex
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// Validation Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Validate a single change item.
 */
export function validateChange(change: Change, options: ValidationOptions = {}): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Must have at least one reference (except for Unreleased entries per our extension)
  // See references/unreleased-extension.md for rationale
  if (!change.references?.length && !options.isUnreleased) {
    errors.push(`Change "${truncate(change.text, TRUNCATE_LENGTH)}" has no reference (PR or commit required)`);
  }
  
  // Check for imperative mood
  if (change.text.length > 0 && !startsWithImperativeVerb(change.text)) {
    warnings.push(
      `Change "${truncate(change.text, 30)}" may not use imperative mood ` +
      '(should start with verb like "Add", "Fix", etc.)'
    );
  }
  
  return createResult(errors, warnings);
}

/**
 * Validate a change group.
 */
export function validateChangeGroup(
  group: ChangeGroup, 
  validCategoriesOrPrevious: string[] = [], 
  options: ValidationOptions = {}
): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Check valid category name
  if (!VALID_CATEGORIES.includes(group.category as typeof VALID_CATEGORIES[number])) {
    errors.push(`Invalid category "${group.category}". Must be one of: ${VALID_CATEGORIES.join(', ')}`);
    return createResult(errors, warnings);
  }
  
  // Check category order only when we have previous categories (not the full valid list)
  // Passing the full valid categories list skips order checking
  const shouldCheckOrder = validCategoriesOrPrevious.length > 0 && !isFullCategoriesList(validCategoriesOrPrevious);
  if (shouldCheckOrder && isCategoryOutOfOrder(group.category, validCategoriesOrPrevious)) {
    errors.push(`Category "${group.category}" is out of order. Expected order: ${VALID_CATEGORIES.join(' > ')}`);
  }
  
  // Validate each change
  for (const change of group.changes) {
    const result = validateChange(change, options);
    errors.push(...result.errors);
    warnings.push(...result.warnings);
  }
  
  return createResult(errors, warnings);
}

/**
 * Validate release groups with order tracking.
 */
function validateGroups(groups: ChangeGroup[], options: ValidationOptions = {}): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  const previousCategories: string[] = [];
  
  for (const group of groups) {
    const result = validateChangeGroup(group, previousCategories, options);
    errors.push(...result.errors);
    warnings.push(...result.warnings);
    previousCategories.push(group.category);
  }
  
  return createResult(errors, warnings);
}

/**
 * Validate a release entry.
 */
export function validateRelease(release: Release): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Unreleased section: validate groups but don't require references
  // See references/unreleased-extension.md for rationale
  if (release.version === 'Unreleased') {
    const groupsResult = validateGroups(release.groups, { isUnreleased: true });
    return createResult(groupsResult.errors, groupsResult.warnings);
  }
  
  // Check semver version (without 'v' prefix)
  if (!isValidVersion(release.version)) {
    errors.push(`Invalid version "${release.version}". Must be valid semver without 'v' prefix (e.g., "2.0.0")`);
  }
  
  // Check date format
  if (!release.date) {
    errors.push(`Release ${release.version} is missing date`);
  } else if (!ISO_DATE_REGEX.test(release.date)) {
    errors.push(`Invalid date format "${release.date}" for version ${release.version}. Must be YYYY-MM-DD`);
  }
  
  // Validate groups
  const groupsResult = validateGroups(release.groups);
  errors.push(...groupsResult.errors);
  warnings.push(...groupsResult.warnings);
  
  return createResult(errors, warnings);
}

/**
 * Check for duplicate versions.
 */
function findDuplicates(versions: string[]): string[] {
  const counts: Record<string, number> = {};
  for (const v of versions) {
    counts[v] = (counts[v] ?? 0) + 1;
  }
  return Object.entries(counts)
    .filter(([, count]) => count > 1)
    .map(([version]) => version);
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Validation
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Validate CHANGELOG.md file.
 */
export async function validateChangelog(rootDir: string): Promise<ValidationResult> {
  const errors: string[] = [];
  const warnings: string[] = [];
  const changelogPath = path.join(rootDir, 'CHANGELOG.md');
  
  // Check file exists
  if (!fs.existsSync(changelogPath)) {
    return createResult(['CHANGELOG.md not found']);
  }
  
  const content = fs.readFileSync(changelogPath, 'utf-8');
  
  // Check header
  if (!content.trim().startsWith('# Changelog')) {
    errors.push('CHANGELOG.md must start with "# Changelog" header');
  }
  
  // Parse and validate
  const parsed = parseChangelog(content);
  
  if (parsed.releases.length === 0) {
    errors.push('CHANGELOG.md must have at least one release entry or Unreleased section');
  }
  
  // Validate each release
  for (const release of parsed.releases) {
    const result = validateRelease(release);
    errors.push(...result.errors);
    warnings.push(...result.warnings);
  }
  
  // Check version ordering and duplicates
  const versions = parsed.releases
    .map(r => r.version)
    .filter(v => v !== 'Unreleased');
  
  if (versions.length > 1 && !areVersionsSorted(versions)) {
    errors.push(`Releases are not sorted newest-first. Found order: ${versions.join(', ')}`);
  }
  
  const duplicates = findDuplicates(versions);
  if (duplicates.length > 0) {
    errors.push(`Duplicate versions found: ${duplicates.join(', ')}`);
  }
  
  // Unreleased should be first if present
  if (parsed.releases.length > 0 && parsed.releases[0]?.version !== 'Unreleased') {
    if (parsed.releases.some(r => r.version === 'Unreleased')) {
      warnings.push('Unreleased section should be at the top of the changelog');
    }
  }
  
  return createResult(errors, warnings);
}

/**
 * Check if releases in a changelog are sorted newest-first.
 */
export function checkReleasesAreSorted(content: string): boolean {
  const parsed = parseChangelog(content);
  const versions = parsed.releases
    .map(r => r.version)
    .filter(v => v !== 'Unreleased');
  
  return versions.length <= 1 || areVersionsSorted(versions);
}

/**
 * Sort releases in a changelog newest-first.
 */
export function sortReleases(content: string): string {
  const lines = content.split('\n');
  const headerLines: string[] = [];
  const releases: ReleaseLines[] = [];
  let currentRelease: ReleaseLines | null = null;
  let inHeader = true;
  
  for (const line of lines) {
    const releaseMatch = line.match(/^##\s+\[([^\]]+)\](?:\s+-\s+(\d{4}-\d{2}-\d{2}))?/);
    
    if (releaseMatch) {
      if (currentRelease) {
        releases.push(currentRelease);
      }
      inHeader = false;
      currentRelease = {
        version: releaseMatch[1]!,
        date: releaseMatch[2] ?? null,
        lines: [line]
      };
    } else if (currentRelease) {
      currentRelease.lines.push(line);
    } else if (inHeader) {
      headerLines.push(line);
    }
  }
  
  if (currentRelease) {
    releases.push(currentRelease);
  }
  
  // Separate Unreleased from version releases
  const unreleased = releases.find(r => r.version === 'Unreleased');
  const versionReleases = releases.filter(r => r.version !== 'Unreleased');
  
  // Sort version releases newest-first
  versionReleases.sort((a, b) => compareVersions(a.version, b.version));
  
  // Rebuild content
  const result = [...headerLines];
  
  if (unreleased) {
    result.push(...unreleased.lines);
  }
  
  for (const release of versionReleases) {
    result.push(...release.lines);
  }
  
  return result.join('\n');
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/validate.ts
// To modify, edit the source file and run the distribution workflow

