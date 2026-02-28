#!/usr/bin/env node
/**
 * Changelog Constants
 * 
 * Centralized constants for regex patterns, category mappings, and magic values
 * used throughout the changelog skill scripts.
 */

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export type ChangelogCategory = 'Changed' | 'Added' | 'Removed' | 'Fixed';

export type ConventionalCommitPrefix = 
  | 'feat' | 'feature' | 'add'
  | 'fix' | 'bugfix' | 'hotfix'
  | 'refactor' | 'chore' | 'docs' | 'style' | 'perf' | 'build' | 'ci' | 'test' | 'revert'
  | 'remove' | 'deprecate';

export interface ReferencePatterns {
  prOrIssue: RegExp;
  commit: RegExp;
  githubUrl: RegExp;
  jira: RegExp;
}

export interface ExclusionPatterns {
  patterns: Record<string, string[]>;
}

export interface FormatIndicators {
  commonChangelog: string[];
  keepAChangelog: string[];
  keepAChangelogAlt: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Regex Patterns
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Semver regex - matches MAJOR.MINOR.PATCH with optional prerelease and build.
 * Follows semver 2.0.0 spec: no leading zeros in numeric identifiers.
 * Does NOT accept 'v' prefix (Common Changelog requirement).
 */
export const SEMVER_REGEX = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|[A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9]\d*|[A-Za-z-][0-9A-Za-z-]*))*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$/;

/** ISO date format: YYYY-MM-DD */
export const ISO_DATE_REGEX = /^\d{4}-\d{2}-\d{2}$/;

/** Version heading pattern: ## [VERSION] - DATE or ## [VERSION] */
export const VERSION_HEADING_REGEX = /^##\s+\[([^\]]+)\](?:\s+-\s+(\d{4}-\d{2}-\d{2}))?/;

/** PR reference in markdown link: [#123](url) */
export const PR_LINK_REGEX = /\[#(\d+)\]\(([^)]+)\)/g;

/** Standalone PR reference: (#123) without markdown link */
export const PR_STANDALONE_REGEX = /\(#(\d+)\)(?!\])/g;

/** Commit reference in markdown: [`abc1234`](url) */
export const COMMIT_LINK_REGEX = /\[`([a-f0-9]{7,40})`\]\(([^)]+)\)/g;

/** Breaking change prefix pattern */
export const BREAKING_PREFIX_REGEX = /\*\*Breaking:\*\*\s*/;

/** Any reference cleanup patterns for extracting clean text */
export const REFERENCE_CLEANUP_PATTERNS: RegExp[] = [
  /\s*\(\[#\d+\]\([^)]+\)\)/g, // PR markdown link
  /\s*\(\[`[a-f0-9]+`\]\([^)]+\)\)/g, // Commit markdown link
  /\s*\(#\d+\)/g // Standalone PR
];

/** Emoji unicode ranges for removal (comprehensive) */
export const EMOJI_REGEX = /[\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]|[\u{1F600}-\u{1F64F}]|[\u{1F680}-\u{1F6FF}]|[\u{1F1E0}-\u{1F1FF}]|[\u{FE00}-\u{FE0F}]|[\u{200D}]|[\u{2B50}]|[\u{2705}]|[\u{2728}]|[\u{2764}]|[\u{1FA00}-\u{1FAFF}]/gu;

/** Jira/ticket reference pattern: [EN-123], (EN-123), EN-123: */
export const TICKET_REGEX = /\[?[A-Z]{2,}-\d+]?[:\s-]*/gi;

/** PR part references: Part 1/4 -, PR 2/3:, (1/2), 1/4 - */
export const PR_PART_REGEX = /\(?(?:Part|PR)?\s*\d+\/\d+\)?[:\s-]*/gi;

/** Conventional commit prefix: type(scope)!: message */
export const CONVENTIONAL_COMMIT_REGEX = /^(\w+)(?:\([^)]*\))?(!)?:\s*(.+)$/;

/** PR number extraction from subject: (#123) */
export const PR_NUMBER_REGEX = /\(#(\d+)\)/;

/** Category heading pattern: ### Category */
export const CATEGORY_HEADING_REGEX = /^###\s+(\w+)/;

/** Changelog entry pattern: - Some text or * Some text */
export const ENTRY_REGEX = /^[-*]\s+(.+)$/;

/** Check if text has any reference (PR, commit, issue, Jira) */
export const HAS_REFERENCE_PATTERNS: ReferencePatterns = {
  prOrIssue: /\(?#\d+\)?/,
  commit: /\(?`?[a-f0-9]{7,40}`?\)?/i,
  githubUrl: /https?:\/\/github\.com\/[^/]+\/[^/]+\/(pull|issues|commit)/,
  jira: /\(?[A-Z]{2,}-\d+\)?/
};

// ═══════════════════════════════════════════════════════════════════════════
// Category Constants
// ═══════════════════════════════════════════════════════════════════════════

/** Valid categories in Common Changelog (in correct order) */
export const VALID_CATEGORIES: ChangelogCategory[] = ['Changed', 'Added', 'Removed', 'Fixed'];

/** Conventional commit prefix to Common Changelog category mapping */
export const PREFIX_TO_CATEGORY: Record<string, ChangelogCategory> = {
  // Feature additions
  feat: 'Added',
  feature: 'Added',
  add: 'Added',
  // Bug fixes
  fix: 'Fixed',
  bugfix: 'Fixed',
  hotfix: 'Fixed',
  // Changes
  refactor: 'Changed',
  chore: 'Changed',
  docs: 'Changed',
  style: 'Changed',
  perf: 'Changed',
  build: 'Changed',
  ci: 'Changed',
  test: 'Changed',
  revert: 'Changed',
  // Removals
  remove: 'Removed',
  deprecate: 'Removed'
};

/** Keep a Changelog category to Common Changelog mapping */
export const KEEP_CHANGELOG_CATEGORY_MAP: Record<string, ChangelogCategory> = {
  added: 'Added',
  changed: 'Changed',
  deprecated: 'Changed', // Merge into Changed
  removed: 'Removed',
  fixed: 'Fixed',
  security: 'Fixed' // Security fixes → Fixed
};

/** Combined category mapping (all formats) */
export const CATEGORY_MAP: Record<string, ChangelogCategory> = {
  ...KEEP_CHANGELOG_CATEGORY_MAP,
  ...Object.fromEntries(
    Object.entries(PREFIX_TO_CATEGORY).map(([k, v]) => [k.toLowerCase(), v])
  ) as Record<string, ChangelogCategory>,
  breaking: 'Changed'
};

// ═══════════════════════════════════════════════════════════════════════════
// Imperative Verbs
// ═══════════════════════════════════════════════════════════════════════════

/** Valid imperative mood verbs for changelog entries */
export const IMPERATIVE_VERBS: string[] = [
  // Core actions
  'Add', 'Remove', 'Fix', 'Update', 'Change', 'Refactor', 'Improve',
  'Implement', 'Support', 'Enable', 'Disable', 'Create', 'Delete',
  // Code organization
  'Rename', 'Move', 'Merge', 'Split', 'Deprecate', 'Drop', 'Bump',
  // Documentation
  'Document', 'Clarify', 'Simplify', 'Optimize', 'Migrate', 'Convert',
  // Feature work
  'Introduce', 'Extract', 'Inline', 'Replace', 'Revert', 'Restore',
  'Enhance', 'Extend', 'Reduce', 'Increase', 'Decrease', 'Allow',
  // Control flow
  'Prevent', 'Ensure', 'Validate', 'Normalize', 'Standardize', 'Use',
  'Set', 'Get', 'Make', 'Handle', 'Process', 'Parse', 'Generate',
  // Build/run
  'Build', 'Run', 'Execute', 'Load', 'Save', 'Read', 'Write',
  // UI
  'Show', 'Hide', 'Display', 'Render', 'Format', 'Transform',
  // Collections
  'Include', 'Exclude', 'Filter', 'Sort', 'Group', 'Aggregate',
  // Setup
  'Initialize', 'Configure', 'Setup', 'Install', 'Uninstall',
  'Upgrade', 'Downgrade', 'Pin', 'Unpin', 'Lock', 'Unlock',
  // Security
  'Expose', 'Protect', 'Secure', 'Encrypt', 'Decrypt', 'Sign',
  'Verify', 'Authenticate', 'Authorize', 'Grant', 'Revoke',
  // Lifecycle
  'Start', 'Stop', 'Pause', 'Resume', 'Restart', 'Reset',
  // Cleanup
  'Clean', 'Clear', 'Flush', 'Purge', 'Prune', 'Trim',
  // Testing
  'Check', 'Test', 'Assert', 'Confirm', 'Reject', 'Accept',
  // Operations
  'Apply', 'Reapply', 'Undo', 'Redo', 'Cancel', 'Abort',
  'Skip', 'Ignore', 'Override', 'Bypass', 'Force', 'Require',
  'Recommend', 'Suggest', 'Prefer', 'Default', 'Fallback'
];

/** Set of lowercase imperative verbs for fast lookup */
export const IMPERATIVE_VERBS_SET: Set<string> = new Set(
  IMPERATIVE_VERBS.map(v => v.toLowerCase())
);

// ═══════════════════════════════════════════════════════════════════════════
// Format Indicators
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Format detection indicators for changelog files.
 * Split to avoid CodeQL URL substring sanitization false positive.
 */
export const FORMAT_INDICATORS: FormatIndicators = {
  commonChangelog: ['common-changelog', '.org'],
  keepAChangelog: ['keepachangelog', '.com'],
  keepAChangelogAlt: 'keep a changelog'
};

// ═══════════════════════════════════════════════════════════════════════════
// Default Exclusion Patterns
// ═══════════════════════════════════════════════════════════════════════════

/** Default file patterns excluded from changelog update requirement */
export const DEFAULT_EXCLUSION_PATTERNS: ExclusionPatterns = {
  patterns: {
    infrastructure: ['.github/', '.gitignore', '.nvmrc', 'pnpm-lock.yaml'],
    tests: ['tests/'],
    generated: ['.tmp/', '.stryker-tmp/', 'coverage/', 'reports/', 'output/', '.claude/skills/', '.claude/skills/', '.cursor/'],
    config: ['.c8rc.json', '.coderabbit.yaml', '.doclinterrc.yml', 'vitest.config.js', 'eslint.config.js']
  }
};

// ═══════════════════════════════════════════════════════════════════════════
// Magic Numbers
// ═══════════════════════════════════════════════════════════════════════════

/** Maximum characters to show in truncated change text for error messages */
export const TRUNCATE_LENGTH = 50;

/** Maximum number of files to show in staged files error message */
export const MAX_FILES_TO_SHOW = 10;

/** Minimum commit hash length for validation */
export const MIN_COMMIT_HASH_LENGTH = 7;

/** Maximum commit hash length (full SHA-1) */
export const MAX_COMMIT_HASH_LENGTH = 40;

/** Git log max buffer size (10MB) */
export const GIT_LOG_MAX_BUFFER = 10 * 1024 * 1024;

// ═══════════════════════════════════════════════════════════════════════════
// Helper Functions
// ═══════════════════════════════════════════════════════════════════════════

/** Check if text starts with an imperative verb. */
export function startsWithImperativeVerb(text: string): boolean {
  const firstWord = text.split(/\s+/)[0];
  return firstWord ? IMPERATIVE_VERBS_SET.has(firstWord.toLowerCase()) : false;
}

/** Check if text has any reference (PR, commit, issue, Jira). */
export function hasReference(text: string): boolean {
  return Object.values(HAS_REFERENCE_PATTERNS).some(pattern => pattern.test(text));
}

/** Remove emojis from text. */
export function removeEmojis(text: string): string {
  return text.replace(EMOJI_REGEX, '').trim();
}

/** Capitalize first letter of string. */
export function capitalizeFirst(str: string): string {
  if (!str) { return str; }
  return str.charAt(0).toUpperCase() + str.slice(1);
}

/** Truncate text to specified length with ellipsis. */
export function truncate(text: string, length: number = TRUNCATE_LENGTH): string {
  if (text.length <= length) { return text; }
  return `${text.slice(0, length)}...`;
}

/** Get category from conventional commit prefix. */
export function getCategoryFromPrefix(prefix: string): ChangelogCategory {
  return PREFIX_TO_CATEGORY[prefix.toLowerCase()] ?? 'Changed';
}

/** Map any category name to valid Common Changelog category. */
export function normalizeCategory(category: string): ChangelogCategory {
  return CATEGORY_MAP[category.toLowerCase()] ?? 'Changed';
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/constants.ts
// To modify, edit the source file and run the distribution workflow

