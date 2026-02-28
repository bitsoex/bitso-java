# Automation Patterns for Stacked PRs

## Contents

- [Overview](#overview)
- [Core Polling Pattern](#core-polling-pattern)
- [PR Status Checker](#pr-status-checker)
- [Wait for PR Ready](#wait-for-pr-ready)
- [Process Entire Stack](#process-entire-stack)
- [Callback Pattern for Fixes](#callback-pattern-for-fixes)
- [Timeout and Retry Strategies](#timeout-and-retry-strategies)
- [Event-Driven Alternative](#event-driven-alternative)
- [Complete Example: Autonomous Stack Manager](#complete-example-autonomous-stack-manager)
- [Best Practices](#best-practices)

---
## Overview

For autonomous AI agents managing stacked PRs, use programmatic polling loops instead of manual status checks. This enables:

- Unattended operation
- Efficient resource usage
- Automatic progression through the stack
- Timeout-based failure handling

## Core Polling Pattern

```javascript
/**
 * Generic polling function with exponential backoff
 */
async function pollUntil(checkFn, options = {}) {
  const {
    maxAttempts = 30,
    initialIntervalMs = 30000,  // 30 seconds
    maxIntervalMs = 300000,     // 5 minutes
    backoffMultiplier = 1.5,
    onProgress = null
  } = options;

  let intervalMs = initialIntervalMs;

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    const result = await checkFn();

    if (result.done) {
      return { success: true, result: result.data, attempts: attempt };
    }

    if (onProgress) {
      onProgress({ attempt, maxAttempts, nextCheckIn: intervalMs, status: result.status });
    }

    if (attempt < maxAttempts) {
      await sleep(intervalMs);
      intervalMs = Math.min(intervalMs * backoffMultiplier, maxIntervalMs);
    }
  }

  return { success: false, reason: 'timeout', attempts: maxAttempts };
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
```

## PR Status Checker

```javascript
import { execSync, spawnSync } from 'child_process';

/**
 * Check comprehensive PR status
 */
async function checkPRStatus(prNumber, repo) {
  const status = {
    ciPassed: false,
    ciPending: false,
    coderabbitApproved: false,
    coderabbitPending: false,
    openComments: 0,
    isDraft: true,
    state: 'UNKNOWN'
  };

  try {
    // Check CI status
    const checks = execSync(
      `gh pr checks ${prNumber} --repo ${repo}`,
      { encoding: 'utf-8', stdio: ['pipe', 'pipe', 'pipe'] }
    );

    const lines = checks.trim().split('\n');
    const hasFailed = lines.some(l => l.includes('fail'));
    const hasPending = lines.some(l => l.includes('pending'));

    status.ciPassed = !hasFailed && !hasPending;
    status.ciPending = hasPending;

    // Check PR state
    const prInfo = JSON.parse(execSync(
      `gh pr view ${prNumber} --repo ${repo} --json state,isDraft,reviews`,
      { encoding: 'utf-8' }
    ));

    status.state = prInfo.state;
    status.isDraft = prInfo.isDraft;

    // Check CodeRabbit status
    const crReviews = prInfo.reviews.filter(r => r.author.login === 'coderabbitai');
    if (crReviews.length > 0) {
      const lastReview = crReviews[crReviews.length - 1];
      status.coderabbitApproved = lastReview.state === 'APPROVED';
      status.coderabbitPending = false;
    }

    // Check open comments
    status.openComments = await countOpenCodeRabbitComments(prNumber, repo);

  } catch (error) {
    console.error(`Error checking PR #${prNumber}: ${error.message}`);
  }

  return status;
}

/**
 * Count unresolved CodeRabbit comments
 */
async function countOpenCodeRabbitComments(prNumber, repo) {
  const [owner, repoName] = repo.split('/');

  const query = `
    query($owner: String!, $repo: String!, $pr: Int!) {
      repository(owner: $owner, name: $repo) {
        pullRequest(number: $pr) {
          reviewThreads(first: 100) {
            nodes {
              isResolved
              comments(first: 1) { nodes { author { login } } }
            }
          }
        }
      }
    }
  `;

  const result = spawnSync('gh', [
    'api', 'graphql',
    '-f', `query=${query}`,
    '-f', `owner=${owner}`,
    '-f', `repo=${repoName}`,
    '-F', `pr=${prNumber}`
  ], { encoding: 'utf-8' });

  if (result.status !== 0) {
    return -1; // Error
  }

  const data = JSON.parse(result.stdout);
  const threads = data.data.repository.pullRequest.reviewThreads.nodes;

  return threads.filter(t =>
    !t.isResolved &&
    t.comments.nodes[0]?.author?.login === 'coderabbitai'
  ).length;
}

/**
 * Get open CodeRabbit comments with details
 */
async function getOpenComments(prNumber, repo) {
  const [owner, repoName] = repo.split('/');

  const query = `
    query($owner: String!, $repo: String!, $pr: Int!) {
      repository(owner: $owner, name: $repo) {
        pullRequest(number: $pr) {
          reviewThreads(first: 100) {
            nodes {
              id
              isResolved
              path
              line
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

  const result = spawnSync('gh', [
    'api', 'graphql',
    '-f', `query=${query}`,
    '-f', `owner=${owner}`,
    '-f', `repo=${repoName}`,
    '-F', `pr=${prNumber}`
  ], { encoding: 'utf-8' });

  if (result.status !== 0) {
    return [];
  }

  const data = JSON.parse(result.stdout);
  const threads = data.data.repository.pullRequest.reviewThreads.nodes;

  return threads
    .filter(t => !t.isResolved && t.comments.nodes[0]?.author?.login === 'coderabbitai')
    .map(t => ({
      threadId: t.id,
      path: t.path,
      line: t.line,
      body: t.comments.nodes[0].body
    }));
}
```

## Wait for PR Ready

```javascript
/**
 * Wait for a PR to be ready for merge
 */
async function waitForPRReady(prNumber, repo, options = {}) {
  const {
    maxAttempts = 30,
    intervalMs = 60000,  // 1 minute
    onProgress = console.log
  } = options;

  return pollUntil(
    async () => {
      const status = await checkPRStatus(prNumber, repo);

      const ready = status.ciPassed &&
                    status.coderabbitApproved &&
                    status.openComments === 0;

      return {
        done: ready,
        data: status,
        status: `CI: ${status.ciPassed ? '✅' : status.ciPending ? '⏳' : '❌'}, ` +
                `CR: ${status.coderabbitApproved ? '✅' : '⏳'}, ` +
                `Comments: ${status.openComments}`
      };
    },
    { maxAttempts, initialIntervalMs: intervalMs, onProgress }
  );
}
```

## Process Entire Stack

```javascript
/**
 * Process all PRs in a stack, bottom to top
 */
async function processStack(prNumbers, repo, options = {}) {
  const {
    onPRStart = null,
    onPRComplete = null,
    onPRTimeout = null,
    fixCallback = null  // async (prNumber, comments) => void
  } = options;

  const results = [];

  for (const prNumber of prNumbers) {
    if (onPRStart) {
      onPRStart(prNumber);
    }

    // Check current status
    let status = await checkPRStatus(prNumber, repo);

    // If there are open comments and we have a fix callback, try to fix
    if (status.openComments > 0 && fixCallback) {
      const comments = await getOpenComments(prNumber, repo);
      await fixCallback(prNumber, comments);

      // Re-check after fixes
      status = await checkPRStatus(prNumber, repo);
    }

    // Wait for CI and CodeRabbit
    const waitResult = await waitForPRReady(prNumber, repo);

    if (waitResult.success) {
      if (onPRComplete) {
        onPRComplete(prNumber, waitResult.result);
      }
      results.push({ prNumber, success: true, status: waitResult.result });
    } else {
      if (onPRTimeout) {
        onPRTimeout(prNumber, waitResult.reason);
      }
      results.push({ prNumber, success: false, reason: waitResult.reason });
      break; // Stop processing stack on failure
    }
  }

  return results;
}
```

## Callback Pattern for Fixes

```javascript
/**
 * Example fix callback that addresses CodeRabbit comments
 */
async function autoFixCallback(prNumber, comments) {
  console.log(`Attempting to fix ${comments.length} comments for PR #${prNumber}`);

  for (const comment of comments) {
    // Analyze comment and determine fix
    const fix = analyzeAndFix(comment);

    if (fix.applied) {
      // Reply to thread
      await replyToThread(comment.threadId,
        `Fixed in commit ${fix.commitSha}. ${fix.description}. Thanks!`
      );
    } else {
      // Reply explaining why not fixed
      await replyToThread(comment.threadId,
        `${fix.reason}`
      );
    }
  }

  // Push all fixes
  execSync('git push origin HEAD');
}
```

## Timeout and Retry Strategies

### Exponential Backoff

```javascript
const backoffOptions = {
  initialIntervalMs: 30000,   // Start with 30s
  maxIntervalMs: 300000,      // Cap at 5 minutes
  backoffMultiplier: 1.5,     // Increase by 50% each time
  maxAttempts: 20             // ~2.5 hours total
};
```

### Fixed Interval with Jitter

```javascript
function intervalWithJitter(baseMs, jitterPercent = 0.1) {
  const jitter = baseMs * jitterPercent * (Math.random() - 0.5) * 2;
  return Math.max(1000, baseMs + jitter);
}
```

### Adaptive Polling

```javascript
// Poll more frequently when CI is close to completing
async function adaptivePolling(prNumber, repo) {
  const status = await checkPRStatus(prNumber, repo);

  if (status.ciPending) {
    return 30000;  // 30s while CI running
  } else if (status.coderabbitPending) {
    return 60000;  // 1m while CodeRabbit reviewing
  } else {
    return 120000; // 2m for general checks
  }
}
```

## Event-Driven Alternative

For environments with webhook support:

```javascript
/**
 * Register callbacks for PR events
 */
function onPREvent(eventType, callback) {
  // Implementation depends on webhook infrastructure
  webhookServer.on(`pull_request.${eventType}`, callback);
}

// Usage
onPREvent('check_run.completed', async (event) => {
  if (event.check_run.conclusion === 'success') {
    await checkAndProgressStack(event.pull_request.number);
  }
});

onPREvent('pull_request_review.submitted', async (event) => {
  if (event.review.user.login === 'coderabbitai' &&
      event.review.state === 'approved') {
    await markPRReady(event.pull_request.number);
  }
});
```

## Complete Example: Autonomous Stack Manager

```typescript
#!/usr/bin/env -S npx tsx
/**
 * Autonomous stack manager
 * Usage: npx tsx manage-stack.ts --prs 79,80,81 --repo owner/repo
 */

import { processStack, waitForPRReady } from './stack-utils';

const prNumbers = [79, 80, 81];
const repo = 'bitsoex/ai-code-instructions';

async function main() {
  console.log('🚀 Starting autonomous stack management...\n');

  const results = await processStack(prNumbers, repo, {
    onPRStart: (pr: number) => console.log(`\n📋 Processing PR #${pr}...`),
    onPRComplete: (pr: number, status: object) => console.log(`✅ PR #${pr} ready!`),
    onPRTimeout: (pr: number, reason: string) => console.log(`⏰ PR #${pr} timed out: ${reason}`),
    fixCallback: autoFixCallback
  });

  console.log('\n📊 Final Results:');
  for (const r of results) {
    console.log(`  PR #${r.prNumber}: ${r.success ? '✅' : '❌'}`);
  }
}

main().catch(console.error);
```

## Best Practices

1. **Always set timeouts** - Never poll indefinitely
2. **Use exponential backoff** - Be kind to APIs
3. **Log progress** - Enable monitoring and debugging
4. **Handle failures gracefully** - Don't crash on transient errors
5. **Respect rate limits** - GitHub API has limits
6. **Parallelize when possible** - Check multiple PRs simultaneously
7. **Use callbacks for fixes** - Separate checking from fixing logic
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/references/automation-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

