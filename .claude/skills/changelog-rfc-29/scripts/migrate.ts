#!/usr/bin/env node
/**
 * Changelog Migration Script
 * 
 * Converts existing changelogs from various formats to Common Changelog.
 * Supports Keep a Changelog, conventional changelog, and custom formats.
 */

import fs from 'fs';
import { fileURLToPath } from 'url';
import {
  CATEGORY_MAP,
  VALID_CATEGORIES,
  FORMAT_INDICATORS,
  hasReference,
  capitalizeFirst,
  type ChangelogCategory
} from './constants.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

type ImperativePattern = [RegExp, string | ((match: string, ...args: string[]) => string)];

interface ChangelogEntry {
  text: string;
  original?: string;
}

interface ParsedRelease {
  version: string;
  date: string | null;
  categories: Record<string, ChangelogEntry[]>;
}

interface ParsedChangelogData {
  header: string;
  releases: ParsedRelease[];
}

export interface MigrationOptions {
  input: string;
  output?: string;
  repoUrl?: string;
  dryRun?: boolean;
}

export interface MigrationResult {
  success: boolean;
  error?: string;
  message?: string;
  skipped?: boolean;
  originalFormat?: string;
  releaseCount?: number;
  todoCount?: number;
  output?: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Imperative Mood Conversion
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Patterns to convert past tense to imperative mood.
 */
const IMPERATIVE_PATTERNS: ImperativePattern[] = [
  [/^Added\s+/i, 'Add '],
  [/^Fixed\s+/i, 'Fix '],
  [/^Removed\s+/i, 'Remove '],
  [/^Changed\s+/i, 'Change '],
  [/^Updated\s+/i, 'Update '],
  [/^The\s+(\w+)\s+now\s+/i, (_: string, noun: string) => `Make ${noun} `],
  [/^We\s+have\s+added\s+/i, 'Add '],
  [/^This\s+release\s+includes\s+/i, '']
];

/**
 * Convert entry text to imperative mood.
 */
function toImperativeMood(text: string): string {
  let result = text;
  for (const [pattern, replacement] of IMPERATIVE_PATTERNS) {
    if (typeof replacement === 'string') {
      result = result.replace(pattern, replacement);
    } else {
      result = result.replace(pattern, replacement as (substring: string, ...args: string[]) => string);
    }
  }
  return result.length > 0 ? capitalizeFirst(result) : result;
}

// ═══════════════════════════════════════════════════════════════════════════
// Format Detection
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Detect the format of an existing changelog.
 */
export function detectFormat(content: string): string {
  const lower = content.toLowerCase();
  
  // Check for Common Changelog
  if (lower.includes(FORMAT_INDICATORS.commonChangelog[0]!) && 
      lower.includes(FORMAT_INDICATORS.commonChangelog[1]!)) {
    return 'common-changelog';
  }
  
  // Check for Keep a Changelog
  if ((lower.includes(FORMAT_INDICATORS.keepAChangelog[0]!) && 
       lower.includes(FORMAT_INDICATORS.keepAChangelog[1]!)) ||
      lower.includes(FORMAT_INDICATORS.keepAChangelogAlt)) {
    return 'keep-a-changelog';
  }
  
  // Check for conventional changelog patterns
  if (content.match(/^#{1,3}\s*(feat|fix|chore|refactor)\s*[:(]/mi)) {
    return 'conventional';
  }
  
  // Generic semver markdown
  if (content.match(/^#{1,2}\s*\[?\d+\.\d+/m)) {
    return 'semver-markdown';
  }
  
  return 'custom';
}

// ═══════════════════════════════════════════════════════════════════════════
// Parsing
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse a changelog into structured data.
 */
export function parseChangelog(content: string): ParsedChangelogData {
  const lines = content.split('\n');
  const releases: ParsedRelease[] = [];
  let currentRelease: ParsedRelease | null = null;
  let currentCategory: ChangelogCategory | null = null;
  let header = '';
  let headerComplete = false;
  
  for (const line of lines) {
    // Version heading: ## [1.0.0] - DATE, ## 1.0.0 - DATE, ## v1.0.0 - DATE, or ## [Unreleased]
    // Supports both bracketed [x.y.z] and non-bracketed x.y.z or vx.y.z formats
    const versionMatch = line.match(
      /^##\s*(?:\[(.+?)\]|(v?\d+\.\d+(?:\.\d+)?(?:-[0-9A-Za-z.-]+)?))(?:\s*-\s*(\d{4}-\d{2}-\d{2}))?/
    );
    if (versionMatch) {
      const version = versionMatch[1] ?? versionMatch[2] ?? '';
      const date = versionMatch[3] ?? null;
      headerComplete = true;
      if (currentRelease) {
        releases.push(currentRelease);
      }
      currentRelease = {
        version,
        date,
        categories: {}
      };
      currentCategory = null;
      continue;
    }
    
    // Category heading: ### Added, ### Changed, etc.
    const categoryMatch = line.match(/^###\s*(\w+)/);
    if (categoryMatch && currentRelease) {
      currentCategory = CATEGORY_MAP[categoryMatch[1]!.toLowerCase()] ?? 'Changed';
      if (!currentRelease.categories[currentCategory]) {
        currentRelease.categories[currentCategory] = [];
      }
      continue;
    }
    
    // Entry: - Some change description
    const entryMatch = line.match(/^[-*]\s+(.+)$/);
    if (entryMatch && currentRelease && currentCategory) {
      currentRelease.categories[currentCategory]!.push({
        text: entryMatch[1]!,
        original: line
      });
      continue;
    }
    
    // Collect header content before first release
    if (!headerComplete && line.trim()) {
      header += `${line}\n`;
    }
  }
  
  if (currentRelease) {
    releases.push(currentRelease);
  }
  
  return { header, releases };
}

// ═══════════════════════════════════════════════════════════════════════════
// Entry Conversion
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Check if entry is a breaking change.
 */
function isBreakingEntry(entry: ChangelogEntry): boolean {
  return entry.text.toLowerCase().includes('breaking') || 
         (entry.original?.toLowerCase().includes('breaking') ?? false);
}

/**
 * Convert an entry to Common Changelog format.
 */
function convertEntry(entry: ChangelogEntry, _category: string): string {
  let { text } = entry;
  
  // Handle deprecated entries specially
  if (entry.original?.toLowerCase().includes('deprecat')) {
    if (!text.toLowerCase().startsWith('deprecate')) {
      text = `Deprecate ${text.replace(/^deprecated?\s*/i, '')}`;
    }
  }
  
  // Convert to imperative mood
  text = toImperativeMood(text);
  
  // Add breaking prefix for breaking changes
  if (isBreakingEntry(entry) && !text.startsWith('**Breaking:**')) {
    text = text.replace(/\*?\*?breaking:?\*?\*?\s*/i, '');
    text = `**Breaking:** ${text}`;
  }
  
  // Warn if no reference (can't auto-add)
  if (!hasReference(text)) {
    text += ' <!-- TODO: Add PR/commit reference -->';
  }
  
  return `- ${text}`;
}

// ═══════════════════════════════════════════════════════════════════════════
// Markdown Generation
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Generate Common Changelog formatted markdown.
 */
export function generateCommonChangelog(
  parsed: ParsedChangelogData, 
  options: { repoUrl?: string } = {}
): string {
  const { repoUrl = '' } = options;
  
  const lines: string[] = [
    '# Changelog',
    '',
    '> This project follows [Common Changelog](https://common-changelog.org/) with an',
    '> [Unreleased section extension](.skills/changelog/references/unreleased-extension.md).',
    ''
  ];
  
  for (const release of parsed.releases) {
    // Version header
    if (release.version.toLowerCase() === 'unreleased') {
      lines.push('## [Unreleased]');
    } else if (release.date) {
      lines.push(`## [${release.version}] - ${release.date}`);
    } else {
      lines.push(`## [${release.version}]`);
    }
    lines.push('');
    
    // Categories in correct order
    for (const category of VALID_CATEGORIES) {
      const entries = release.categories[category];
      if (!entries?.length) { continue; }
      
      lines.push(`### ${category}`, '');
      for (const entry of entries) {
        lines.push(convertEntry(entry, category));
      }
      lines.push('');
    }
  }
  
  // Add reference links at bottom
  if (repoUrl && parsed.releases.length > 0) {
    lines.push('');
    for (const release of parsed.releases) {
      if (release.version.toLowerCase() === 'unreleased') {
        const latestVersion = parsed.releases.find(r => r.version !== 'Unreleased');
        if (latestVersion) {
          lines.push(`[Unreleased]: ${repoUrl}/compare/v${latestVersion.version}...HEAD`);
        }
      } else {
        lines.push(`[${release.version}]: ${repoUrl}/releases/tag/v${release.version}`);
      }
    }
  }
  
  return lines.join('\n');
}

// ═══════════════════════════════════════════════════════════════════════════
// Migration
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Migrate a changelog file to Common Changelog format.
 */
export async function migrateChangelog(options: MigrationOptions): Promise<MigrationResult> {
  const { input, output = input, repoUrl = '', dryRun = false } = options;
  
  if (!fs.existsSync(input)) {
    return { success: false, error: `File not found: ${input}` };
  }
  
  const content = fs.readFileSync(input, 'utf-8');
  const format = detectFormat(content);
  
  if (format === 'common-changelog') {
    return { success: true, message: 'Already in Common Changelog format', skipped: true };
  }
  
  const parsed = parseChangelog(content);
  const converted = generateCommonChangelog(parsed, { repoUrl });
  
  // Count entries needing attention
  const todoCount = (converted.match(/<!-- TODO:/g) ?? []).length;
  
  if (!dryRun) {
    fs.writeFileSync(output, converted);
  }
  
  return {
    success: true,
    originalFormat: format,
    releaseCount: parsed.releases.length,
    todoCount,
    output: dryRun ? converted : output
  };
}

// ═══════════════════════════════════════════════════════════════════════════
// CLI
// ═══════════════════════════════════════════════════════════════════════════

/**
 * CLI entry point.
 */
async function main(): Promise<void> {
  const args = process.argv.slice(2);
  
  if (args.includes('--help') || args.length === 0) {
    console.log(`
Changelog Migration Script

Usage:
  node migrate.js --input CHANGELOG.md [--output CHANGELOG.new.md] [--repo-url URL] [--dry-run]

Options:
  --input      Input changelog file path (required)
  --output     Output file path (defaults to input, overwrites)
  --repo-url   Repository URL for reference links
  --dry-run    Print output without writing file

Examples:
  node migrate.js --input CHANGELOG.md --dry-run
  node migrate.js --input CHANGELOG.md --output CHANGELOG.new.md
  node migrate.js --input CHANGELOG.md --repo-url https://github.com/org/repo
`);
    process.exit(0);
  }
  
  const getArg = (name: string): string | null => {
    const idx = args.indexOf(name);
    return idx !== -1 && args[idx + 1] ? args[idx + 1]! : null;
  };
  
  const input = getArg('--input');
  const output = getArg('--output');
  const repoUrl = getArg('--repo-url');
  const dryRun = args.includes('--dry-run');
  
  if (!input) {
    console.error('Error: --input is required');
    process.exit(1);
  }
  
  console.log(`Migrating: ${input}`);
  console.log(`Format detection: ${detectFormat(fs.readFileSync(input, 'utf-8'))}`);
  
  const result = await migrateChangelog({
    input,
    output: output ?? input,
    repoUrl: repoUrl ?? '',
    dryRun
  });
  
  if (!result.success) {
    console.error(`Error: ${result.error}`);
    process.exit(1);
  }
  
  if (result.skipped) {
    console.log('Already in Common Changelog format, skipping.');
    process.exit(0);
  }
  
  console.log(`Original format: ${result.originalFormat}`);
  console.log(`Releases converted: ${result.releaseCount}`);
  
  if (result.todoCount && result.todoCount > 0) {
    console.log(`Entries needing attention: ${result.todoCount}`);
    console.log('Search for "<!-- TODO:" in the output to find them.');
  }
  
  if (dryRun) {
    console.log('\n--- DRY RUN OUTPUT ---\n');
    console.log(result.output);
  } else {
    console.log(`Output written to: ${result.output}`);
  }
}

// Run if executed directly
if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch(error => {
    console.error('Fatal error:', error);
    process.exit(1);
  });
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/migrate.ts
// To modify, edit the source file and run the distribution workflow

