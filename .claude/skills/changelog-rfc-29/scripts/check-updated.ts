#!/usr/bin/env node
/**
 * Changelog Update Checker
 * 
 * Checks if CHANGELOG.md was updated in staged files.
 * Used by pre-commit hooks to enforce changelog updates.
 */

import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';
import { DEFAULT_EXCLUSION_PATTERNS, MAX_FILES_TO_SHOW, type ExclusionPatterns } from './constants.ts';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface CheckResult {
  passed: boolean;
  message: string;
  errors: string[];
}

export interface CheckOptions {
  stagedFiles?: string[];
  skipBranchCheck?: boolean;
}

// ═══════════════════════════════════════════════════════════════════════════
// Exclusion Patterns
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Load exclusion patterns from skill assets.
 */
function loadExclusionPatterns(): ExclusionPatterns {
  const exclusionsPath = path.join(__dirname, '..', 'assets', 'exclusion-patterns.json');
  
  if (fs.existsSync(exclusionsPath)) {
    return JSON.parse(fs.readFileSync(exclusionsPath, 'utf-8')) as ExclusionPatterns;
  }
  
  return DEFAULT_EXCLUSION_PATTERNS;
}

/**
 * Check if a file path matches exclusion patterns.
 */
export function isExcludedPath(filePath: string, exclusions: ExclusionPatterns): boolean {
  const allPatterns = Object.values(exclusions.patterns ?? exclusions).flat();
  
  for (const pattern of allPatterns) {
    // Directory pattern (ends with /)
    if (pattern.endsWith('/')) {
      if (filePath.startsWith(pattern) || filePath.includes(`/${pattern}`)) {
        return true;
      }
    }
    // Exact file match or pattern match
    else if (filePath === pattern || filePath.endsWith(`/${pattern}`) || filePath.endsWith(pattern)) {
      return true;
    }
  }
  
  return false;
}

// ═══════════════════════════════════════════════════════════════════════════
// Git Operations
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Get list of staged files from git.
 */
export async function getStagedFiles(rootDir: string): Promise<string[]> {
  try {
    const result = execSync('git diff --cached --name-only --diff-filter=ACMR', {
      cwd: rootDir,
      encoding: 'utf-8',
      stdio: ['pipe', 'pipe', 'pipe']
    });
    
    return result.split('\n').filter(Boolean);
  } catch {
    return [];
  }
}

/**
 * Get default branch name.
 */
function getDefaultBranch(rootDir: string): string {
  try {
    const result = execSync('git symbolic-ref refs/remotes/origin/HEAD 2>/dev/null', {
      cwd: rootDir,
      encoding: 'utf-8'
    }).trim();
    return result.replace('refs/remotes/origin/', '');
  } catch {
    // Try to detect main vs master
    try {
      execSync('git rev-parse --verify origin/main 2>/dev/null', { cwd: rootDir });
      return 'main';
    } catch {
      return 'master';
    }
  }
}

/**
 * Get files changed in current branch compared to default branch.
 */
function getChangedFilesInBranch(rootDir: string): string[] {
  try {
    const defaultBranch = getDefaultBranch(rootDir);
    
    const mergeBase = execSync(`git merge-base HEAD origin/${defaultBranch}`, {
      cwd: rootDir,
      encoding: 'utf-8'
    }).trim();
    
    const result = execSync(`git diff --name-only ${mergeBase}...HEAD`, {
      cwd: rootDir,
      encoding: 'utf-8'
    });
    
    return result.split('\n').filter(Boolean);
  } catch {
    return [];
  }
}

/**
 * Check if CHANGELOG.md has already been updated in the current branch.
 */
function changelogAlreadyUpdatedInBranch(rootDir: string): boolean {
  return getChangedFilesInBranch(rootDir).includes('CHANGELOG.md');
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Check
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Check if file is CHANGELOG.md.
 */
function isChangelogFile(filePath: string): boolean {
  return filePath === 'CHANGELOG.md' || filePath.endsWith('/CHANGELOG.md');
}

/**
 * Build error messages for missing changelog update.
 */
function buildErrorMessages(significantFiles: string[]): string[] {
  const messages = [
    'CHANGELOG.md must be updated when modifying significant files.',
    'Staged files requiring changelog update:',
    ...significantFiles.slice(0, MAX_FILES_TO_SHOW).map(f => `  - ${f}`)
  ];
  
  if (significantFiles.length > MAX_FILES_TO_SHOW) {
    messages.push(`  ... and ${significantFiles.length - MAX_FILES_TO_SHOW} more`);
  }
  
  messages.push(
    '',
    'Add your changes to the [Unreleased] section in CHANGELOG.md',
    'See: .skills/changelog/SKILL.md for format guidelines'
  );
  
  return messages;
}

/**
 * Check if CHANGELOG.md needs to be updated based on staged files.
 * 
 * Key behavior:
 * - If CHANGELOG.md is staged in this commit -> pass
 * - If CHANGELOG.md was already updated earlier in this branch -> pass
 * - If only excluded files are staged -> pass
 * - Otherwise -> require changelog update
 */
export async function checkChangelogUpdated(
  rootDir: string, 
  options: CheckOptions = {}
): Promise<CheckResult> {
  const stagedFiles = options.stagedFiles ?? await getStagedFiles(rootDir);
  
  // No staged files
  if (stagedFiles.length === 0) {
    return { passed: true, message: 'No staged files', errors: [] };
  }
  
  // CHANGELOG.md staged in this commit
  if (stagedFiles.some(isChangelogFile)) {
    return { passed: true, message: 'CHANGELOG.md is updated', errors: [] };
  }
  
  // CHANGELOG.md already updated in branch
  if (!options.skipBranchCheck && changelogAlreadyUpdatedInBranch(rootDir)) {
    return { passed: true, message: 'CHANGELOG.md already updated in this branch', errors: [] };
  }
  
  // Filter to significant files
  const exclusions = loadExclusionPatterns();
  const significantFiles = stagedFiles.filter(f => !isExcludedPath(f, exclusions));
  
  // Only excluded files staged
  if (significantFiles.length === 0) {
    return { passed: true, message: 'Only excluded files staged (no changelog update required)', errors: [] };
  }
  
  // Significant files staged but CHANGELOG.md not updated
  return {
    passed: false,
    message: 'CHANGELOG.md not updated',
    errors: buildErrorMessages(significantFiles)
  };
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/check-updated.ts
// To modify, edit the source file and run the distribution workflow

