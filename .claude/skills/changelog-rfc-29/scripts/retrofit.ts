#!/usr/bin/env node
/**
 * Changelog Retrofit Script
 *
 * Generates CHANGELOG.md from git history by extracting PR commits,
 * categorizing them based on conventional commit prefixes, and
 * formatting according to Common Changelog specification.
 */

import { execSync } from 'child_process';
import {
  VALID_CATEGORIES,
  TICKET_REGEX,
  PR_PART_REGEX,
  CONVENTIONAL_COMMIT_REGEX,
  PR_NUMBER_REGEX,
  capitalizeFirst,
  removeEmojis,
  getCategoryFromPrefix,
  GIT_LOG_MAX_BUFFER,
  type ChangelogCategory
} from './constants.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface GitLogEntry {
  hash: string;
  subject: string;
  author: string;
  date?: string;
}

export interface CategorizedCommit {
  category: ChangelogCategory;
  breaking: boolean;
  text: string;
}

export interface ChangeEntry {
  text: string;
  prNumber: number | null;
  url: string;
  breaking: boolean;
  date?: string;
  hash: string;
}

export interface CategorizedChanges {
  Changed: ChangeEntry[];
  Added: ChangeEntry[];
  Removed: ChangeEntry[];
  Fixed: ChangeEntry[];
  [key: string]: ChangeEntry[];
}

export interface RetrofitOptions {
  gitLog?: GitLogEntry[];
  version: string;
  date: string;
  repoUrl: string;
  rootDir?: string;
}

export interface RetrofitResult {
  markdown: string;
  prCount: number;
  categorized: CategorizedChanges;
}

// ═══════════════════════════════════════════════════════════════════════════
// Text Cleaning
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Clean up a PR title by removing emojis, ticket IDs, prefixes, and noise.
 */
function cleanPRTitle(text: string): string {
  let cleaned = text;

  // Remove emojis
  cleaned = removeEmojis(cleaned);

  // Remove common emoji shortcodes
  cleaned = cleaned.replace(/:[a-z_]+:/gi, '');

  // Remove PR references at end
  cleaned = cleaned.replace(/\s*\(?(?:PR\s*)?#\d+\)?$/gi, '');
  cleaned = cleaned.replace(/\s*\(#\d+\)/g, '');

  // Remove Jira/ticket references
  cleaned = cleaned.replace(TICKET_REGEX, '');

  // Remove PR part references
  cleaned = cleaned.replace(PR_PART_REGEX, '');

  // Remove conventional commit prefix if still present
  cleaned = cleaned.replace(CONVENTIONAL_COMMIT_REGEX, '$3');

  // Remove common noise words at start
  cleaned = cleaned.replace(/^(wip|draft|tmp|temp)[:\s-]*/i, '');

  // Remove leading/trailing noise characters
  cleaned = cleaned.replace(/^[\s\-:[\]()]+|[\s\-:[\]()]+$/g, '');

  // Collapse multiple spaces and trim
  cleaned = cleaned.replace(/\s+/g, ' ').trim();

  return cleaned.length > 0 ? capitalizeFirst(cleaned) : text.trim();
}

/**
 * Check for breaking change indicators in text.
 */
function isBreaking(text: string): boolean {
  return text.includes('!:') ||
         text.includes('BREAKING') ||
         text.toLowerCase().includes('breaking change');
}

// ═══════════════════════════════════════════════════════════════════════════
// Commit Categorization
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Categorize a commit based on its subject.
 */
export function categorizeCommit(subject: string): CategorizedCommit {
  // Clean emojis and ticket references first
  const cleanSubject = removeEmojis(subject)
    .replace(/^\[?[A-Z]{2,}-\d+]?[:\s-]*/i, '')
    .trim();

  const breaking = isBreaking(cleanSubject);

  // Parse conventional commit prefix
  const match = cleanSubject.match(CONVENTIONAL_COMMIT_REGEX);

  if (match) {
    const [, prefix, bangBreaking, message] = match;
    const category = getCategoryFromPrefix(prefix!);

    // Clean nested prefixes from message
    const cleanedMessage = message!.trim().replace(CONVENTIONAL_COMMIT_REGEX, '$3');

    return {
      category,
      breaking: breaking || !!bangBreaking,
      text: cleanPRTitle(cleanedMessage)
    };
  }

  // No conventional prefix - infer from content
  const cleaned = cleanPRTitle(subject);
  const lower = cleaned.toLowerCase();

  // Infer category from action words
  const categoryMap: Array<{ patterns: string[]; category: ChangelogCategory }> = [
    { patterns: ['add ', ' add '], category: 'Added' },
    { patterns: ['fix ', ' fix '], category: 'Fixed' },
    { patterns: ['remove ', ' remove '], category: 'Removed' }
  ];

  for (const { patterns, category } of categoryMap) {
    if (patterns.some(p => lower.startsWith(p.trim()) || lower.includes(p))) {
      return { category, breaking, text: cleaned };
    }
  }

  return { category: 'Changed', breaking, text: cleaned };
}

// ═══════════════════════════════════════════════════════════════════════════
// Git Log Processing
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Extract PR commits from git log, excluding automation commits.
 */
export function extractPRCommits(gitLog: GitLogEntry[]): GitLogEntry[] {
  return gitLog.filter(commit => {
    // Exclude automation commits
    if (commit.author === 'estate-catalog[bot]') {
      return false;
    }

    // Must have PR reference matching (#N)
    return PR_NUMBER_REGEX.test(commit.subject);
  });
}

/**
 * Extract PR number from commit subject.
 */
function extractPRNumber(subject: string): number | null {
  const match = subject.match(PR_NUMBER_REGEX);
  return match ? parseInt(match[1]!, 10) : null;
}

/**
 * Get git log from repository.
 */
export function getGitLog(rootDir: string): GitLogEntry[] {
  try {
    // Use null byte delimiter to avoid issues with pipe characters in subjects
    const result = execSync(
      'git log --format="%H%x00%s%x00%an%x00%aI" --reverse',
      { cwd: rootDir, encoding: 'utf-8', maxBuffer: GIT_LOG_MAX_BUFFER }
    );

    return result.split('\n').filter(Boolean).map(line => {
      const [hash = '', subject = '', author = '', date = ''] = line.split('\0');
      return { hash, subject, author, date };
    });
  } catch (error) {
    // Fail fast with descriptive error instead of silently returning empty array
    const message = error instanceof Error ? error.message : String(error);
    throw new Error(`Failed to retrieve git log from ${rootDir}: ${message}`, { cause: error });
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// Markdown Generation
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Format categorized changes as markdown.
 */
export function formatAsMarkdown(
  categorized: CategorizedChanges,
  options: { version: string; date: string; repoUrl?: string }
): string {
  const { version, date, repoUrl = '' } = options;

  const lines: string[] = [
    '# Changelog',
    '',
    '> This project follows [Common Changelog](https://common-changelog.org/) with an',
    '> [Unreleased section extension](.skills/changelog/references/unreleased-extension.md) for in-progress work.',
    '',
    '## [Unreleased]',
    '',
    `## [${version}] - ${date}`,
    '',
    '_Initial versioned release. Retroactively documents all changes since repository creation._',
    ''
  ];

  for (const category of VALID_CATEGORIES) {
    const changes = categorized[category];
    if (!changes?.length) { continue; }

    lines.push(`### ${category}`, '');

    // Sort by date (newest first) and put breaking changes first
    const sorted = [...changes].sort((a, b) => {
      if (a.date && b.date) { return b.date.localeCompare(a.date); }
      return 0;
    });

    const breaking = sorted.filter(c => c.breaking);
    const nonBreaking = sorted.filter(c => !c.breaking);

    for (const change of [...breaking, ...nonBreaking]) {
      const prefix = change.breaking ? '**Breaking:** ' : '';
      const prLink = change.url
        ? `([#${change.prNumber}](${change.url}))`
        : `(#${change.prNumber})`;

      lines.push(`- ${prefix}${change.text} ${prLink}`);
    }

    lines.push('');
  }

  // Add reference links
  if (repoUrl) {
    lines.push(`[Unreleased]: ${repoUrl}/compare/v${version}...HEAD`);
    lines.push(`[${version}]: ${repoUrl}/releases/tag/v${version}`);
  }

  return lines.join('\n');
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Retrofit Function
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Generate changelog from git history.
 */
export async function retrofitChangelog(options: RetrofitOptions): Promise<RetrofitResult> {
  const { version, date, repoUrl, rootDir } = options;

  const gitLog = options.gitLog ?? getGitLog(rootDir!);
  const prCommits = extractPRCommits(gitLog);

  // Initialize categories
  const categorized: CategorizedChanges = {
    Changed: [],
    Added: [],
    Removed: [],
    Fixed: []
  };

  // Categorize commits
  for (const commit of prCommits) {
    const { category, breaking, text } = categorizeCommit(commit.subject);
    const prNumber = extractPRNumber(commit.subject);

    // Remove PR reference from end of text
    const cleanText = text.replace(/\s*\(#\d+\)\s*$/, '').trim();

    categorized[category].push({
      text: cleanText,
      prNumber,
      url: repoUrl ? `${repoUrl}/pull/${prNumber}` : '',
      breaking,
      date: commit.date,
      hash: commit.hash
    });
  }

  const markdown = formatAsMarkdown(categorized, { version, date, repoUrl });

  return {
    markdown,
    prCount: prCommits.length,
    categorized
  };
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/retrofit.ts
// To modify, edit the source file and run the distribution workflow

