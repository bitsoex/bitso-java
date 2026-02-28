/**
 * Shared utilities for CodeRabbit workflow scripts
 */

import { spawnSync } from 'child_process';

// ═══════════════════════════════════════════════════════════════════════════
// Constants
// ═══════════════════════════════════════════════════════════════════════════

export const CODERABBIT_BOT = 'coderabbitai[bot]';
export const CODERABBIT_BOT_LOGIN = 'coderabbitai';
export const COAUTHOR = 'Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>';

// Timing thresholds (in minutes)
export const REVIEW_WAIT_MINUTES = 15;
export const APPROVAL_WAIT_MINUTES = 30;

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface CommandResult {
  stdout: string;
  stderr: string;
  success: boolean;
  exitCode: number;
}

export interface PRInfo {
  pr: number;
  repo: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Shell Command Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Execute a gh CLI command and return structured result
 */
export function gh(args: string[], options?: { maxBuffer?: number }): CommandResult {
  const result = spawnSync('gh', args, {
    encoding: 'utf-8',
    maxBuffer: options?.maxBuffer ?? 10 * 1024 * 1024
  });
  return {
    stdout: result.stdout?.trim() ?? '',
    stderr: result.stderr?.trim() ?? '',
    success: result.status === 0,
    exitCode: result.status ?? 1
  };
}

/**
 * Execute a git command and return structured result
 */
export function git(args: string[]): CommandResult {
  const result = spawnSync('git', args, { encoding: 'utf-8' });
  return {
    stdout: result.stdout?.trim() ?? '',
    stderr: result.stderr?.trim() ?? '',
    success: result.status === 0,
    exitCode: result.status ?? 1
  };
}

// ═══════════════════════════════════════════════════════════════════════════
// Repository Detection
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Get repository name from git remote
 */
export function getRepoFromGit(): string | null {
  const result = git(['remote', 'get-url', 'origin']);
  if (!result.success) {
    return null;
  }

  const match = result.stdout.match(/github\.com[:/](.+?)(?:\.git)?$/);
  return match?.[1] ?? null;
}

/**
 * Get repository name using gh CLI
 */
export function getRepoFromGh(): string | null {
  const result = gh(['repo', 'view', '--json', 'nameWithOwner', '--jq', '.nameWithOwner']);
  return result.success ? result.stdout : null;
}

/**
 * Get current PR number from gh CLI
 */
export function getCurrentPR(): number | null {
  const result = gh(['pr', 'view', '--json', 'number', '--jq', '.number']);
  if (!result.success) {
    return null;
  }
  const num = parseInt(result.stdout, 10);
  return isNaN(num) ? null : num;
}

/**
 * Auto-detect PR and repo, returning both or null
 */
export function detectPRInfo(prArg?: number | null, repoArg?: string | null): PRInfo | null {
  const repo = repoArg ?? getRepoFromGh() ?? getRepoFromGit();
  const pr = prArg ?? getCurrentPR();

  if (!repo || !pr) {
    return null;
  }

  return { pr, repo };
}

// ═══════════════════════════════════════════════════════════════════════════
// Git Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Get the timestamp of the last commit
 */
export function getLastCommitTime(): Date {
  const result = git(['log', '-1', '--format=%ct']);
  if (!result.success) {
    return new Date();
  }
  return new Date(parseInt(result.stdout, 10) * 1000);
}

/**
 * Get the short hash of the last commit
 */
export function getLastCommitHash(): string {
  const result = git(['rev-parse', '--short', 'HEAD']);
  return result.success ? result.stdout : 'latest';
}

/**
 * Get minutes elapsed since last commit
 */
export function getMinutesSinceLastCommit(): number {
  const lastCommitTime = getLastCommitTime();
  const now = new Date();
  return Math.floor((now.getTime() - lastCommitTime.getTime()) / 60000);
}

// ═══════════════════════════════════════════════════════════════════════════
// GitHub API Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Reply to a PR review thread using GraphQL
 */
export function replyToThread(threadId: string, body: string): CommandResult {
  const mutation = `
mutation($threadId: ID!, $body: String!) {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: $threadId,
    body: $body
  }) {
    comment { id }
  }
}`;

  return gh([
    'api', 'graphql',
    '-f', `query=${mutation}`,
    '-f', `threadId=${threadId}`,
    '-f', `body=${body}`
  ]);
}

/**
 * Add a comment to a PR
 */
export function commentOnPR(pr: number, body: string, repo?: string | null): CommandResult {
  const args = ['pr', 'comment', String(pr), '--body', body];
  if (repo) {
    args.push('--repo', repo);
  }
  return gh(args);
}

/**
 * Request CodeRabbit review on a PR
 */
export function requestCodeRabbitReview(pr: number, repo?: string | null): CommandResult {
  return commentOnPR(pr, '@coderabbitai review', repo);
}

/**
 * Request CodeRabbit approval on a PR
 */
export function requestCodeRabbitApproval(pr: number, repo?: string | null): CommandResult {
  return commentOnPR(pr, '@coderabbitai approve', repo);
}

// ═══════════════════════════════════════════════════════════════════════════
// Severity Helpers
// ═══════════════════════════════════════════════════════════════════════════

export type Severity = 'critical' | 'major' | 'minor' | 'unknown';

/**
 * Extract severity from CodeRabbit comment body
 */
export function extractSeverity(body: string): Severity {
  if (body.includes('🔴 Critical') || body.includes('_🔴 Critical_')) {
    return 'critical';
  }
  if (body.includes('🟠 Major') || body.includes('_🟠 Major_')) {
    return 'major';
  }
  if (body.includes('🟡 Minor') || body.includes('_🟡 Minor_')) {
    return 'minor';
  }
  return 'unknown';
}

/**
 * Check if a comment body indicates an actionable issue
 */
export function isActionableComment(body: string): boolean {
  return body.includes('⚠️ Potential issue') ||
         body.includes('🔴 Critical') ||
         body.includes('🟠 Major') ||
         body.includes('🟡 Minor');
}

/**
 * Get severity emoji
 */
export function getSeverityEmoji(severity: Severity): string {
  switch (severity) {
    case 'critical':
      return '🔴';
    case 'major':
      return '🟠';
    case 'minor':
      return '🟡';
    default:
      return '⚪';
  }
}

/**
 * Get severity sort order (lower = higher priority)
 */
export function getSeverityOrder(severity: Severity): number {
  const order: Record<Severity, number> = {
    critical: 0,
    major: 1,
    minor: 2,
    unknown: 3
  };
  return order[severity];
}

// ═══════════════════════════════════════════════════════════════════════════
// Console Output Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Print a styled header box
 */
export function printHeader(title: string): void {
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log(`║           ${title.padEnd(50)}║`);
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
}

/**
 * Print a horizontal separator
 */
export function printSeparator(char = '─', length = 60): void {
  console.log(char.repeat(length));
}

// ═══════════════════════════════════════════════════════════════════════════
// Argument Parsing Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse common CLI arguments (--pr, --repo, --dry-run)
 */
export function parseCommonArgs(argv: string[]): {
  pr: number | null;
  repo: string | null;
  dryRun: boolean;
  file: string | null;
  interval: number;
  maxWait: number;
  remaining: string[];
} {
  const result = {
    pr: null as number | null,
    repo: null as string | null,
    dryRun: false,
    file: null as string | null,
    interval: 60,
    maxWait: 3600,
    remaining: [] as string[]
  };

  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    switch (arg) {
      case '--pr':
        result.pr = parseInt(argv[++i] ?? '', 10) || null;
        break;
      case '--repo':
        result.repo = argv[++i] ?? null;
        break;
      case '--dry-run':
        result.dryRun = true;
        break;
      case '--file':
        result.file = argv[++i] ?? null;
        break;
      case '--interval': {
        const value = Number.parseInt(argv[++i] ?? '', 10);
        if (!Number.isFinite(value) || value <= 0) {
          throw new Error('--interval must be a positive integer (seconds)');
        }
        result.interval = value;
        break;
      }
      case '--max-wait': {
        const value = Number.parseInt(argv[++i] ?? '', 10);
        if (!Number.isFinite(value) || value <= 0) {
          throw new Error('--max-wait must be a positive integer (seconds)');
        }
        result.maxWait = value;
        break;
      }
      default:
        result.remaining.push(arg!);
    }
  }

  return result;
}

// ═══════════════════════════════════════════════════════════════════════════
// Async Helpers
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Sleep for a number of seconds
 */
export function sleep(seconds: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, seconds * 1000));
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/scripts/lib/utils.ts
// To modify, edit the source file and run the distribution workflow

