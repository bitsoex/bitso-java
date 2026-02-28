#!/usr/bin/env node
/**
 * Monitor CodeRabbit review status and wait for approval
 *
 * This script polls the PR status and determines next actions:
 * - Checks for open CodeRabbit comments that need addressing
 * - Checks for CodeRabbit review/approval status
 * - Waits appropriate time before requesting review or approval
 *
 * Usage:
 *   node monitor-approval.ts --pr 123 [--repo owner/repo] [--interval 60] [--max-wait 3600]
 *
 * Options:
 *   --pr          PR number (required, or auto-detected from current branch)
 *   --repo        Repository in owner/repo format (default: from git remote)
 *   --interval    Poll interval in seconds (default: 60)
 *   --max-wait    Maximum wait time in seconds (default: 3600 = 1 hour)
 *   --dry-run     Show what would be done without taking action
 *
 * Exit codes:
 *   0 - CodeRabbit approved the PR
 *   1 - Error or timeout
 *   2 - Open comments need to be addressed
 */

import {
  gh,
  detectPRInfo,
  getMinutesSinceLastCommit,
  requestCodeRabbitReview,
  requestCodeRabbitApproval,
  isActionableComment,
  printHeader,
  parseCommonArgs,
  sleep,
  CODERABBIT_BOT,
  CODERABBIT_BOT_LOGIN,
  COAUTHOR,
  REVIEW_WAIT_MINUTES,
  APPROVAL_WAIT_MINUTES,
  type PRInfo
} from './lib/utils.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

interface MonitorOptions extends PRInfo {
  interval: number;
  maxWait: number;
  dryRun: boolean;
}

interface ReviewStatus {
  state: 'APPROVED' | 'CHANGES_REQUESTED' | 'COMMENTED' | 'DISMISSED' | 'PENDING' | null;
  submittedAt: string | null;
}

interface PRStatus {
  minutesSinceCommit: number;
  openComments: number;
  reviewStatus: ReviewStatus;
  ciPassing: boolean;
}

type Action =
  | { type: 'wait'; reason: string; nextCheckIn: number }
  | { type: 'address-comments'; reason: string; commentCount: number }
  | { type: 'request-review'; reason: string }
  | { type: 'request-approval'; reason: string }
  | { type: 'approved'; reason: string }
  | { type: 'error'; reason: string };

// ═══════════════════════════════════════════════════════════════════════════
// PR Status Fetching
// ═══════════════════════════════════════════════════════════════════════════

function getOpenCodeRabbitComments(info: PRInfo): number {
  const query = `
    query($owner: String!, $repo: String!, $pr: Int!) {
      repository(owner: $owner, name: $repo) {
        pullRequest(number: $pr) {
          reviewThreads(first: 100) {
            nodes {
              isResolved
              comments(first: 1) {
                nodes {
                  author { login }
                  body
                }
              }
            }
          }
        }
      }
    }
  `;

  const [owner, repoName] = info.repo.split('/');
  const result = gh([
    'api', 'graphql',
    '-f', `query=${query}`,
    '-F', `owner=${owner}`,
    '-F', `repo=${repoName}`,
    '-F', `pr=${info.pr}`,
    '--jq', '.data.repository.pullRequest.reviewThreads.nodes'
  ]);

  if (!result.success) {
    return 0;
  }

  try {
    const threads = JSON.parse(result.stdout) as Array<{
      isResolved: boolean;
      comments: { nodes: Array<{ author: { login: string } | null; body: string }> };
    }>;

    const botLogins = new Set([CODERABBIT_BOT, CODERABBIT_BOT_LOGIN]);
    return threads.filter(t => {
      if (t.isResolved) {
        return false;
      }
      const firstComment = t.comments.nodes[0];
      if (!firstComment?.author) {
        return false;
      }
      if (!botLogins.has(firstComment.author.login)) {
        return false;
      }
      return isActionableComment(firstComment.body);
    }).length;
  } catch {
    return 0;
  }
}

function getCodeRabbitReviewStatus(info: PRInfo): ReviewStatus {
  const result = gh([
    'api', `repos/${info.repo}/pulls/${info.pr}/reviews`,
    '--jq', `map(select(.user.login == "${CODERABBIT_BOT}" or .user.login == "${CODERABBIT_BOT_LOGIN}")) | last | {state, submitted_at}`
  ]);

  if (!result.success || !result.stdout || result.stdout === 'null') {
    return { state: null, submittedAt: null };
  }

  try {
    const review = JSON.parse(result.stdout) as { state: string; submitted_at: string };
    return {
      state: review.state as ReviewStatus['state'],
      submittedAt: review.submitted_at
    };
  } catch {
    return { state: null, submittedAt: null };
  }
}

function getCIStatus(info: PRInfo): boolean {
  // Check for any non-passing CI checks (excluding CodeRabbit and null names)
  // NEUTRAL is considered passing (e.g., CodeQL with no security issues)
  const result = gh([
    'pr', 'checks', String(info.pr),
    '--repo', info.repo,
    '--json', 'name,state',
    '--jq', '.[] | select(.name != null and .name != "" and .name != "CodeRabbit" and .state != "SUCCESS" and .state != "SKIPPED" and .state != "NEUTRAL") | .name'
  ]);
  return result.success && result.stdout.trim() === '';
}

function getPRStatus(info: PRInfo): PRStatus {
  return {
    minutesSinceCommit: getMinutesSinceLastCommit(),
    openComments: getOpenCodeRabbitComments(info),
    reviewStatus: getCodeRabbitReviewStatus(info),
    ciPassing: getCIStatus(info)
  };
}

// ═══════════════════════════════════════════════════════════════════════════
// Decision Logic
// ═══════════════════════════════════════════════════════════════════════════

function determineAction(status: PRStatus): Action {
  // If already approved, we're done
  if (status.reviewStatus.state === 'APPROVED') {
    return { type: 'approved', reason: 'CodeRabbit has approved the PR' };
  }

  // If there are open comments, they need to be addressed first
  if (status.openComments > 0) {
    return {
      type: 'address-comments',
      reason: `${status.openComments} open CodeRabbit comment(s) need to be addressed`,
      commentCount: status.openComments
    };
  }

  // If CI is not passing, wait
  if (!status.ciPassing) {
    return {
      type: 'wait',
      reason: 'CI checks are not passing yet',
      nextCheckIn: 60
    };
  }

  // Check timing for review request
  if (status.minutesSinceCommit < REVIEW_WAIT_MINUTES) {
    const waitTime = REVIEW_WAIT_MINUTES - status.minutesSinceCommit;
    return {
      type: 'wait',
      reason: `Waiting for CodeRabbit automatic review (${waitTime} min remaining)`,
      nextCheckIn: Math.min(waitTime * 60, 60)
    };
  }

  // If no review yet after 15 min, request one
  if (!status.reviewStatus.state || status.reviewStatus.state === 'DISMISSED') {
    if (status.minutesSinceCommit >= REVIEW_WAIT_MINUTES) {
      return {
        type: 'request-review',
        reason: `No CodeRabbit review after ${status.minutesSinceCommit} minutes`
      };
    }
  }

  // If reviewed but not approved, check timing for approval request
  if (status.reviewStatus.state === 'COMMENTED' || status.reviewStatus.state === 'CHANGES_REQUESTED') {
    if (status.minutesSinceCommit >= APPROVAL_WAIT_MINUTES) {
      return {
        type: 'request-approval',
        reason: `CodeRabbit reviewed but not approved after ${status.minutesSinceCommit} minutes`
      };
    }

    const waitTime = APPROVAL_WAIT_MINUTES - status.minutesSinceCommit;
    return {
      type: 'wait',
      reason: `Waiting before requesting approval (${waitTime} min remaining)`,
      nextCheckIn: Math.min(waitTime * 60, 60)
    };
  }

  // Default: wait and check again
  return {
    type: 'wait',
    reason: 'Monitoring for CodeRabbit activity',
    nextCheckIn: 60
  };
}

// ═══════════════════════════════════════════════════════════════════════════
// Actions
// ═══════════════════════════════════════════════════════════════════════════

function executeRequestReview(pr: number, repo: string, dryRun: boolean): boolean {
  console.log('📝 Requesting CodeRabbit review...');
  if (dryRun) {
    console.log('   [DRY-RUN] Would run: gh pr comment', pr, '--repo', repo, '--body "@coderabbitai review"');
    return true;
  }

  const result = requestCodeRabbitReview(pr, repo);
  return result.success;
}

function executeRequestApproval(pr: number, repo: string, dryRun: boolean): boolean {
  console.log('✅ Requesting CodeRabbit approval...');
  if (dryRun) {
    console.log('   [DRY-RUN] Would run: gh pr comment', pr, '--repo', repo, '--body "@coderabbitai approve"');
    return true;
  }

  const result = requestCodeRabbitApproval(pr, repo);
  return result.success;
}

// ═══════════════════════════════════════════════════════════════════════════
// Main
// ═══════════════════════════════════════════════════════════════════════════

function parseOptions(): MonitorOptions {
  const args = parseCommonArgs(process.argv.slice(2));
  const info = detectPRInfo(args.pr, args.repo);

  if (!info) {
    console.error('Usage: node monitor-approval.ts --pr <number> [--repo owner/repo] [--interval 60] [--max-wait 3600] [--dry-run]');
    console.error('Could not auto-detect PR or repository. Please provide --pr and/or --repo.');
    process.exit(1);
  }

  return {
    ...info,
    interval: args.interval,
    maxWait: args.maxWait,
    dryRun: args.dryRun
  };
}

async function main(): Promise<void> {
  const options = parseOptions();
  const startTime = Date.now();

  printHeader('CodeRabbit Approval Monitor');

  console.log(`PR: #${options.pr}`);
  console.log(`Repo: ${options.repo}`);
  console.log(`Poll interval: ${options.interval}s`);
  console.log(`Max wait: ${options.maxWait}s`);
  if (options.dryRun) {
    console.log('Mode: DRY-RUN (no actions will be taken)');
  }
  console.log('');

  let iteration = 0;

  while (true) {
    iteration++;
    const elapsed = Math.floor((Date.now() - startTime) / 1000);

    if (elapsed > options.maxWait) {
      console.log(`\n⏱️  Maximum wait time (${options.maxWait}s) exceeded. Exiting.`);
      process.exit(1);
    }

    console.log(`\n─── Check #${iteration} (${elapsed}s elapsed) ───`);

    const status = getPRStatus(options);
    console.log(`  Last commit: ${status.minutesSinceCommit} min ago`);
    console.log(`  Open comments: ${status.openComments}`);
    console.log(`  Review status: ${status.reviewStatus.state ?? 'none'}`);
    console.log(`  CI passing: ${status.ciPassing ? 'yes' : 'no'}`);

    const action = determineAction(status);
    console.log(`  Action: ${action.type}`);
    console.log(`  Reason: ${action.reason}`);

    switch (action.type) {
      case 'approved':
        console.log('\n🎉 CodeRabbit has approved the PR!');
        console.log(`\nCo-author for commits:\n${COAUTHOR}`);
        process.exit(0);
        break;

      case 'address-comments':
        console.log(`\n⚠️  ${action.commentCount} comment(s) need to be addressed.`);
        console.log('Run: node global/skills/coderabbit-workflow/scripts/export-comments.ts --pr', options.pr);
        process.exit(2);
        break;

      case 'request-review':
        if (executeRequestReview(options.pr, options.repo, options.dryRun)) {
          console.log('   Review requested successfully');
        } else {
          console.log('   Failed to request review');
        }
        break;

      case 'request-approval':
        if (executeRequestApproval(options.pr, options.repo, options.dryRun)) {
          console.log('   Approval requested successfully');
        } else {
          console.log('   Failed to request approval');
        }
        break;

      case 'wait': {
        const waitSeconds = Math.min(action.nextCheckIn, options.interval);
        console.log(`  Waiting ${waitSeconds}s...`);
        await sleep(waitSeconds);
        break;
      }

      case 'error':
        console.error(`\n❌ Error: ${action.reason}`);
        process.exit(1);
    }
  }
}

main().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/scripts/monitor-approval.ts
// To modify, edit the source file and run the distribution workflow

