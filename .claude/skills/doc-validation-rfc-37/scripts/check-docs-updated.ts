#!/usr/bin/env node
/**
 * Documentation Update Validator
 *
 * Checks if documentation was updated when significant files change.
 * Designed to run during pre-push to compare branch against main/master.
 *
 * Usage:
 *   npx tsx check-docs-updated.ts [--base <branch>]
 *
 * Options:
 *   --base <branch>   Base branch to compare against (default: auto-detect main/master)
 *   --strict          Exit with error code 1 if docs not updated (default: warning only)
 *
 * Environment:
 *   AI_AGENTS_SKIP_DOCS_CHECK=1   Skip this check entirely
 *
 * Exit codes:
 *   0 - Success or warning (non-strict mode)
 *   1 - Failure (strict mode and docs not updated)
 */

import { execSync } from 'child_process';

// ═══════════════════════════════════════════════════════════════════════════
// Configuration
// ═══════════════════════════════════════════════════════════════════════════

// Exact files that are considered "significant" changes requiring doc updates
const SIGNIFICANT_FILES_EXACT: string[] = [
  // Architecture and configuration
  'technology-hierarchy.json',
  'repo-overrides.json',
  'managed-paths.json',

  // Core scripts
  '.scripts/cli/tools/convert-rules.ts',
  '.scripts/lib/core/targeting.ts',
  '.scripts/cli/tools/safe-sync.ts',
  '.scripts/cli/ci/ci-distribute.ts',
  '.scripts/lib/core/managed-paths.ts',
  '.scripts/cli/tools/conflict-detector.ts',

  // CI/CD
  '.github/workflows/ci.yaml',
];

// Patterns for significant file detection (regex patterns for glob-like matching)
const SIGNIFICANT_PATTERNS: RegExp[] = [
  /^global\/skills\/[^/]+\/SKILL\.md$/, // New or updated skills
  /^global\/skills\/[^/]+\/assets\//, // Skill assets
  /^global\/skills\/git-hooks\/assets\//, // Git hooks assets
  /^\.scripts\/lib\/skills\//, // Skill implementations
];

// Documentation files/patterns that satisfy the requirement
const DOC_PATTERNS: RegExp[] = [
  /^docs\//,
  /^README\.md$/,
  /^CONTRIBUTING\.md$/,
  /^global\/skills\/[^/]+\/SKILL\.md$/, // SKILL.md itself counts as documentation
];

// ═══════════════════════════════════════════════════════════════════════════
// Output Formatting
// ═══════════════════════════════════════════════════════════════════════════

const useColors = process.stdout.isTTY && !process.env.NO_COLOR;

const colors: Record<string, string> = {
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  dim: '\x1b[2m',
  reset: '\x1b[0m'
};

const c = (color: string, text: string): string =>
  useColors ? `${colors[color]}${text}${colors.reset}` : text;

// ═══════════════════════════════════════════════════════════════════════════
// Git Helpers
// ═══════════════════════════════════════════════════════════════════════════

function getDefaultBranch(): string {
  // Try to detect the default branch
  try {
    // Check if main exists
    execSync('git rev-parse --verify main', { encoding: 'utf-8', stdio: 'pipe' });
    return 'main';
  } catch {
    try {
      // Check if master exists
      execSync('git rev-parse --verify master', { encoding: 'utf-8', stdio: 'pipe' });
      return 'master';
    } catch {
      // Fallback to HEAD~1
      return 'HEAD~1';
    }
  }
}

function getChangedFiles(baseBranch: string): string[] {
  try {
    // Get the merge base to compare against
    const mergeBase = execSync(`git merge-base ${baseBranch} HEAD`, {
      encoding: 'utf-8',
      stdio: 'pipe'
    }).trim();

    // Get all files changed between merge base and HEAD
    const output = execSync(`git diff --name-only ${mergeBase} HEAD`, {
      encoding: 'utf-8',
      stdio: 'pipe'
    });

    return output.split('\n').filter(Boolean);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    console.error(c('yellow', `Warning: Could not get changed files: ${message}`));
    return [];
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// Analysis
// ═══════════════════════════════════════════════════════════════════════════

function isSignificantFile(file: string): boolean {
  // Check exact matches
  if (SIGNIFICANT_FILES_EXACT.includes(file)) {
    return true;
  }

  // Check patterns (regex for glob-like matching)
  return SIGNIFICANT_PATTERNS.some(pattern => pattern.test(file));
}

function isDocFile(file: string): boolean {
  return DOC_PATTERNS.some(pattern => pattern.test(file));
}

interface AnalysisResult {
  significantChanges: string[];
  docChanges: string[];
}

function analyzeChanges(changedFiles: string[]): AnalysisResult {
  const significantChanges: string[] = [];
  const docChanges: string[] = [];

  for (const file of changedFiles) {
    if (isSignificantFile(file)) {
      significantChanges.push(file);
    }
    if (isDocFile(file)) {
      docChanges.push(file);
    }
  }

  return { significantChanges, docChanges };
}

// ═══════════════════════════════════════════════════════════════════════════
// Main
// ═══════════════════════════════════════════════════════════════════════════

function main(): void {
  // Skip if disabled
  if (process.env.AI_AGENTS_SKIP_DOCS_CHECK === '1') {
    console.log(c('dim', '[doc-sync] Skipping documentation check (AI_AGENTS_SKIP_DOCS_CHECK=1)'));
    process.exit(0);
  }

  // Parse arguments
  const args = process.argv.slice(2);
  let baseBranch: string | null = null;
  let strict = false;

  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--base' && args[i + 1]) {
      baseBranch = args[i + 1]!;
      i++;
    } else if (args[i] === '--strict') {
      strict = true;
    }
  }

  // Auto-detect base branch if not specified
  if (!baseBranch) {
    baseBranch = getDefaultBranch();
  }

  console.log(c('dim', `[doc-sync] Checking documentation updates against ${baseBranch}...`));

  // Get changed files
  const changedFiles = getChangedFiles(baseBranch);

  if (changedFiles.length === 0) {
    console.log(c('dim', '[doc-sync] No files changed, skipping.'));
    process.exit(0);
  }

  // Analyze changes
  const { significantChanges, docChanges } = analyzeChanges(changedFiles);

  // No significant changes - all good
  if (significantChanges.length === 0) {
    console.log(c('green', '[doc-sync] ✓ No significant changes requiring documentation updates.'));
    process.exit(0);
  }

  // Significant changes with doc updates - all good
  if (docChanges.length > 0) {
    console.log(c('green', `[doc-sync] ✓ Documentation updated (${docChanges.length} file(s)).`));
    process.exit(0);
  }

  // Significant changes without doc updates - warning
  console.log('');
  console.log(c('yellow', '════════════════════════════════════════════════════════════════'));
  console.log(c('yellow', '  ⚠️  Documentation Update May Be Required'));
  console.log(c('yellow', '════════════════════════════════════════════════════════════════'));
  console.log('');
  console.log(c('blue', 'The following significant files are changed in this branch:'));
  console.log('');
  for (const file of significantChanges.slice(0, 10)) {
    console.log(`   ${file}`);
  }
  if (significantChanges.length > 10) {
    console.log(`   ... and ${significantChanges.length - 10} more`);
  }
  console.log('');
  console.log(c('blue', 'But no documentation files are being updated.'));
  console.log('');
  console.log('Consider updating one of these docs:');
  console.log('');
  console.log('   docs/ai-ide-management/concepts/      # For architecture changes');
  console.log('   docs/ai-ide-management/how-tos/       # For new features');
  console.log('   README.md                             # For overview changes');
  console.log('');
  console.log(c('yellow', 'If documentation update is not needed, you can:'));
  console.log('');
  console.log('   1. Skip for this push:');
  console.log('      AI_AGENTS_SKIP_DOCS_CHECK=1 git push');
  console.log('');
  console.log('   2. Or add docs in a follow-up commit before pushing.');
  console.log('');
  console.log(c('yellow', '════════════════════════════════════════════════════════════════'));
  console.log('');

  // Exit with error in strict mode
  if (strict) {
    console.log(c('red', '[doc-sync] Failing push due to --strict mode.'));
    process.exit(1);
  }

  // Warning only in non-strict mode
  process.exit(0);
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/scripts/check-docs-updated.ts
// To modify, edit the source file and run the distribution workflow

