#!/usr/bin/env node
/**
 * Changelog PR Coverage Validator
 * 
 * Validates that all merged PRs from git history are documented in CHANGELOG.md.
 */

import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface GitLogEntry {
  hash: string;
  subject: string;
  author: string;
}

export interface CoverageResult {
  passed: boolean;
  missingPRs: number[];
  extraPRs: number[];
  message: string;
  errors: string[];
  warnings: string[];
}

export interface CoverageOptions {
  gitPRs?: number[];
  changelogPRs?: number[];
}

// ═══════════════════════════════════════════════════════════════════════════
// PR Extraction Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Extract PR numbers from git history.
 */
export function extractPRsFromGitHistory(gitLog: GitLogEntry[]): number[] {
  const prs = new Set<number>();
  
  for (const commit of gitLog) {
    // Exclude automation commits
    if (commit.author === 'estate-catalog[bot]') {
      continue;
    }
    
    // Extract PR numbers from subject - match #N anywhere
    const matches = commit.subject.matchAll(/#(\d+)/g);
    for (const match of matches) {
      prs.add(parseInt(match[1]!, 10));
    }
  }
  
  return Array.from(prs).sort((a, b) => a - b);
}

/**
 * Extract PR numbers from CHANGELOG.md content.
 */
export function extractPRsFromChangelog(content: string): number[] {
  const prs = new Set<number>();
  
  // Match [#N](url) format
  const linkPattern = /\[#(\d+)\]/g;
  let match;
  while ((match = linkPattern.exec(content)) !== null) {
    prs.add(parseInt(match[1]!, 10));
  }
  
  // Match (#N) format (standalone)
  const standalonePattern = /\(#(\d+)\)(?!\])/g;
  while ((match = standalonePattern.exec(content)) !== null) {
    prs.add(parseInt(match[1]!, 10));
  }
  
  return Array.from(prs).sort((a, b) => a - b);
}

/**
 * Get git log from repository.
 */
function getGitLog(rootDir: string): GitLogEntry[] {
  try {
    const result = execSync(
      'git log --format="%H|%s|%an"',
      { cwd: rootDir, encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 }
    );
    
    return result.split('\n').filter(Boolean).map(line => {
      const [hash = '', subject = '', author = ''] = line.split('|');
      return { hash, subject, author };
    });
  } catch {
    return [];
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Validation Function
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Validate that all PRs from git history are in CHANGELOG.md.
 */
export async function validateCoverage(
  rootDir: string, 
  options: CoverageOptions = {}
): Promise<CoverageResult> {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Get PRs from git history
  let { gitPRs } = options;
  if (!gitPRs) {
    const gitLog = getGitLog(rootDir);
    gitPRs = extractPRsFromGitHistory(gitLog);
  }
  
  // Get PRs from changelog
  let { changelogPRs } = options;
  if (!changelogPRs) {
    const changelogPath = path.join(rootDir, 'CHANGELOG.md');
    if (fs.existsSync(changelogPath)) {
      const content = fs.readFileSync(changelogPath, 'utf-8');
      changelogPRs = extractPRsFromChangelog(content);
    } else {
      changelogPRs = [];
    }
  }
  
  // Handle empty case
  if (gitPRs.length === 0 && changelogPRs.length === 0) {
    return {
      passed: true,
      missingPRs: [],
      extraPRs: [],
      message: 'No PRs found in repository',
      errors: [],
      warnings: []
    };
  }
  
  // Find missing PRs (in git but not in changelog)
  const gitPRSet = new Set(gitPRs);
  const changelogPRSet = new Set(changelogPRs);
  
  const missingPRs = gitPRs.filter(pr => !changelogPRSet.has(pr));
  const extraPRs = changelogPRs.filter(pr => !gitPRSet.has(pr));
  
  // Missing PRs is a failure
  if (missingPRs.length > 0) {
    errors.push(
      `${missingPRs.length} PR(s) from git history are missing from CHANGELOG.md:`,
      ...missingPRs.slice(0, 20).map(pr => `  - #${pr}`),
      missingPRs.length > 20 ? `  ... and ${missingPRs.length - 20} more` : ''
    );
  }
  
  // Extra PRs is a warning (might be from external repos)
  if (extraPRs.length > 0) {
    warnings.push(
      `${extraPRs.length} PR(s) in CHANGELOG.md not found in git history:`,
      ...extraPRs.slice(0, 10).map(pr => `  - #${pr}`),
      extraPRs.length > 10 ? `  ... and ${extraPRs.length - 10} more` : ''
    );
  }
  
  const passed = missingPRs.length === 0;
  
  return {
    passed,
    missingPRs,
    extraPRs,
    message: passed 
      ? `All ${gitPRs.length} PR(s) are documented in CHANGELOG.md`
      : `${missingPRs.length} PR(s) missing from CHANGELOG.md`,
    errors: errors.filter(Boolean),
    warnings: warnings.filter(Boolean)
  };
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/coverage.ts
// To modify, edit the source file and run the distribution workflow

