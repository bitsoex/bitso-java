#!/usr/bin/env node
/**
 * GitHub Releases Import Script
 * 
 * Fetches GitHub releases and converts them to Common Changelog format.
 * Useful for repositories that have release history but no changelog file.
 */

import { execSync } from 'child_process';
import fs from 'fs';
import { fileURLToPath } from 'url';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface GitHubRelease {
  tag_name: string;
  name: string | null;
  body: string | null;
  published_at: string | null;
  prerelease: boolean;
}

interface ReleaseEntry {
  text: string;
  prNumber: string | null;
}

interface CategorizedEntries {
  Changed: ReleaseEntry[];
  Added: ReleaseEntry[];
  Removed: ReleaseEntry[];
  Fixed: ReleaseEntry[];
  [key: string]: ReleaseEntry[];
}

export interface ImportOptions {
  repo: string;
  output?: string;
  includePrerelease?: boolean;
  dryRun?: boolean;
}

export interface ImportResult {
  success: boolean;
  error?: string;
  releaseCount?: number;
  todoCount?: number;
  output?: string;
}

interface ConvertOptions {
  repoUrl?: string;
  includePrerelease?: boolean;
}

// ═══════════════════════════════════════════════════════════════════════════
// Constants
// ═══════════════════════════════════════════════════════════════════════════

/** Common Changelog category order */
const CATEGORY_ORDER = ['Changed', 'Added', 'Removed', 'Fixed'] as const;

/** Keywords that indicate a category */
const CATEGORY_KEYWORDS: Record<string, string[]> = {
  Added: ['add', 'new', 'feature', 'implement', 'create', 'introduce', 'support'],
  Fixed: ['fix', 'bug', 'patch', 'resolve', 'correct', 'repair', 'security'],
  Removed: ['remove', 'delete', 'drop', 'deprecate', 'eliminate'],
  Changed: ['change', 'update', 'modify', 'refactor', 'improve', 'enhance', 'upgrade']
};

// ═══════════════════════════════════════════════════════════════════════════
// Release Fetching
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Fetch releases from GitHub.
 */
export function fetchReleases(repo: string): GitHubRelease[] {
  try {
    const result = execSync(
      `gh api "repos/${repo}/releases?per_page=100" --jq '.[] | {tag_name, name, body, published_at, prerelease}'`,
      { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 }
    );
    
    // Parse JSON lines output
    const releases: GitHubRelease[] = [];
    const lines = result.trim().split('\n');
    
    for (const line of lines) {
      if (line.trim()) {
        try {
          releases.push(JSON.parse(line) as GitHubRelease);
        } catch {
          // Skip malformed lines
        }
      }
    }
    
    return releases;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(`Error fetching releases: ${message}`);
    return [];
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// Parsing Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse version from tag name.
 */
function parseVersion(tagName: string): string {
  // Remove common prefixes
  return tagName.replace(/^v/, '').replace(/^version[-_]?/i, '');
}

/**
 * Parse date from published timestamp.
 */
function parseDate(publishedAt: string | null): string | null {
  if (!publishedAt) { return null; }
  return publishedAt.split('T')[0] ?? null;
}

/**
 * Categorize a changelog entry based on its content.
 */
function categorizeEntry(text: string): string {
  const lower = text.toLowerCase();
  
  // Check conventional commit prefix
  const conventionalMatch = lower.match(/^(feat|fix|chore|refactor|docs|style|perf|test|build|ci)[\s(:]/);
  if (conventionalMatch) {
    const prefix = conventionalMatch[1];
    if (prefix === 'feat') { return 'Added'; }
    if (prefix === 'fix') { return 'Fixed'; }
    return 'Changed';
  }
  
  // Check for category keywords
  for (const [category, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    for (const keyword of keywords) {
      // Check if text starts with keyword or has it after common prefixes
      if (lower.startsWith(keyword) || lower.match(new RegExp(`^[-*•]\\s*${keyword}`, 'i'))) {
        return category;
      }
    }
  }
  
  // Default to Changed
  return 'Changed';
}

/**
 * Parse release body into changelog entries.
 */
function parseReleaseBody(body: string | null): CategorizedEntries {
  const categories: CategorizedEntries = {
    Changed: [],
    Added: [],
    Removed: [],
    Fixed: []
  };

  if (!body || body.trim().length === 0) {
    return categories;
  }
  
  // Split into lines and process
  const lines = body.split('\n');
  let currentCategory: string | null = null;
  
  for (let line of lines) {
    line = line.trim();
    
    // Skip empty lines
    if (!line) { continue; }
    
    // Check for markdown headers indicating categories
    const headerMatch = line.match(/^#{1,3}\s*(\w+)/);
    if (headerMatch) {
      const headerText = headerMatch[1]!.toLowerCase();
      for (const [cat, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
        if (keywords.some(k => headerText.includes(k))) {
          currentCategory = cat;
          break;
        }
      }
      continue;
    }
    
    // Check for list items
    const listMatch = line.match(/^[-*•]\s+(.+)$/);
    if (listMatch) {
      const text = listMatch[1]!;
      const category = currentCategory ?? categorizeEntry(text);
      
      // Clean up the text
      let cleanText = text
        // Remove PR references at end (we'll add proper ones)
        .replace(/\s*\(#\d+\)\s*$/, '')
        .replace(/\s*by\s+@[\w-]+\s*$/, '')
        // Remove conventional commit prefix
        .replace(/^(feat|fix|chore|refactor|docs|style|perf|test|build|ci)[\s(:]+/i, '')
        .trim();
      
      // Capitalize first letter
      if (cleanText.length > 0) {
        cleanText = cleanText.charAt(0).toUpperCase() + cleanText.slice(1);
      }
      
      // Extract PR number if present
      const prMatch = text.match(/#(\d+)/);
      const prNumber = prMatch ? prMatch[1]! : null;
      
      categories[category]?.push({
        text: cleanText,
        prNumber
      });
      continue;
    }
    
    // Handle prose paragraphs (not in a list)
    if (line.length > 20 && !line.startsWith('#')) {
      // Try to extract meaningful content
      const category = categorizeEntry(line);
      categories[category]?.push({
        text: line.substring(0, 100) + (line.length > 100 ? '...' : ''),
        prNumber: null
      });
    }
  }
  
  return categories;
}

// ═══════════════════════════════════════════════════════════════════════════
// Changelog Conversion
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Convert releases to Common Changelog format.
 */
export function convertToChangelog(releases: GitHubRelease[], options: ConvertOptions = {}): string {
  const { repoUrl = '', includePrerelease = false } = options;
  
  // Filter out prereleases if not wanted
  const filteredReleases = includePrerelease 
    ? releases 
    : releases.filter(r => !r.prerelease);
  
  // Sort by date (newest first)
  filteredReleases.sort((a, b) => {
    const dateA = a.published_at ?? '';
    const dateB = b.published_at ?? '';
    return dateB.localeCompare(dateA);
  });
  
  const lines: string[] = [
    '# Changelog',
    '',
    '> This project follows [Common Changelog](https://common-changelog.org/) with an',
    '> [Unreleased section extension](.skills/changelog/references/unreleased-extension.md).',
    '',
    '_Imported from GitHub releases._',
    '',
    '## [Unreleased]',
    ''
  ];
  
  for (const release of filteredReleases) {
    const version = parseVersion(release.tag_name);
    const date = parseDate(release.published_at);
    
    // Version header
    if (date) {
      lines.push(`## [${version}] - ${date}`);
    } else {
      lines.push(`## [${version}]`);
    }
    lines.push('');
    
    // Parse and categorize body
    const categories = parseReleaseBody(release.body);
    
    // Check if we have any entries
    const hasEntries = CATEGORY_ORDER.some(cat => 
      categories[cat] && categories[cat].length > 0
    );
    
    if (!hasEntries) {
      // If no structured entries, add a placeholder with the release name
      const releaseName = release.name ?? release.tag_name;
      lines.push('### Changed');
      lines.push('');
      lines.push(`- Release ${releaseName} <!-- TODO: Add details from git history -->`);
      lines.push('');
      continue;
    }
    
    // Output categories in order
    for (const category of CATEGORY_ORDER) {
      const entries = categories[category];
      if (!entries || entries.length === 0) { continue; }
      
      lines.push(`### ${category}`);
      lines.push('');
      
      for (const entry of entries) {
        let line = `- ${entry.text}`;
        
        if (entry.prNumber && repoUrl) {
          line += ` ([#${entry.prNumber}](${repoUrl}/pull/${entry.prNumber}))`;
        } else if (entry.prNumber) {
          line += ` (#${entry.prNumber})`;
        } else {
          line += ' <!-- TODO: Add PR/commit reference -->';
        }
        
        lines.push(line);
      }
      lines.push('');
    }
  }
  
  // Add reference links
  if (repoUrl && filteredReleases.length > 0) {
    lines.push('');
    const latestVersion = parseVersion(filteredReleases[0]!.tag_name);
    lines.push(`[Unreleased]: ${repoUrl}/compare/v${latestVersion}...HEAD`);
    
    for (const release of filteredReleases) {
      const version = parseVersion(release.tag_name);
      lines.push(`[${version}]: ${repoUrl}/releases/tag/${release.tag_name}`);
    }
  }
  
  return lines.join('\n');
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Import Function
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Import releases and generate changelog.
 */
export async function importReleases(options: ImportOptions): Promise<ImportResult> {
  const { repo, output, includePrerelease = false, dryRun = false } = options;
  
  console.log(`Fetching releases from ${repo}...`);
  const releases = fetchReleases(repo);
  
  if (releases.length === 0) {
    return { success: false, error: 'No releases found' };
  }
  
  console.log(`Found ${releases.length} releases`);
  
  const repoUrl = `https://github.com/${repo}`;
  const changelog = convertToChangelog(releases, { repoUrl, includePrerelease });
  
  // Count entries needing attention
  const todoCount = (changelog.match(/<!-- TODO:/g) ?? []).length;
  
  if (!dryRun && output) {
    fs.writeFileSync(output, changelog);
  }
  
  return {
    success: true,
    releaseCount: releases.length,
    todoCount,
    output: dryRun ? changelog : output
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
GitHub Releases Import Script

Usage:
  node import-releases.js --repo owner/repo [--output CHANGELOG.md] [--include-prerelease] [--dry-run]

Options:
  --repo               Repository in "owner/repo" format (required)
  --output             Output file path (required unless --dry-run)
  --include-prerelease Include prerelease versions
  --dry-run            Print output without writing file

Examples:
  node import-releases.js --repo bitsoex/my-repo --output CHANGELOG.md
  node import-releases.js --repo bitsoex/my-repo --dry-run
  node import-releases.js --repo bitsoex/my-repo --output CHANGELOG.md --include-prerelease
`);
    process.exit(0);
  }
  
  const getArg = (name: string): string | null => {
    const idx = args.indexOf(name);
    return idx !== -1 && args[idx + 1] ? args[idx + 1]! : null;
  };
  
  const repo = getArg('--repo');
  const output = getArg('--output');
  const includePrerelease = args.includes('--include-prerelease');
  const dryRun = args.includes('--dry-run');
  
  if (!repo) {
    console.error('Error: --repo is required');
    process.exit(1);
  }
  
  if (!output && !dryRun) {
    console.error('Error: --output is required (or use --dry-run)');
    process.exit(1);
  }
  
  const result = await importReleases({
    repo,
    output: output ?? undefined,
    includePrerelease,
    dryRun
  });
  
  if (!result.success) {
    console.error(`Error: ${result.error}`);
    process.exit(1);
  }
  
  console.log(`Releases imported: ${result.releaseCount}`);
  
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
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/import-releases.ts
// To modify, edit the source file and run the distribution workflow

