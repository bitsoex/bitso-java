#!/usr/bin/env node
/**
 * Batch Reply to CodeRabbit PR threads
 * 
 * Usage:
 *   node batch-reply.js replies.json
 */

import { spawnSync } from 'child_process';
import fs from 'fs';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

interface Reply {
  threadId: string;
  body: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Main
// ═══════════════════════════════════════════════════════════════════════════

const args = process.argv.slice(2);
const file = args[0];

if (!file) {
  console.error('Usage: node batch-reply.js replies.json');
  process.exit(1);
}

if (!fs.existsSync(file)) {
  console.error(`File not found: ${file}`);
  process.exit(1);
}

let replies: Reply[];
try {
  replies = JSON.parse(fs.readFileSync(file, 'utf-8')) as Reply[];
} catch (err) {
  const message = err instanceof Error ? err.message : String(err);
  console.error(`Invalid JSON in ${file}: ${message}`);
  process.exit(1);
}

if (!Array.isArray(replies)) {
  console.error('Expected replies.json to contain an array');
  process.exit(1);
}

console.log(`Processing ${replies.length} replies...`);

let failed = 0;
for (const { threadId, body } of replies) {
  process.stdout.write(`Replying to ${threadId}... `);
  
  const mutation = `
mutation($threadId: ID!, $body: String!) {
  addPullRequestReviewThreadReply(input: {
    pullRequestReviewThreadId: $threadId,
    body: $body
  }) {
    comment { id }
  }
}`;

  const result = spawnSync('gh', [
    'api', 'graphql',
    '-f', `query=${mutation}`,
    '-f', `threadId=${threadId}`,
    '-f', `body=${body}`
  ], { encoding: 'utf-8' });

  if (result.status !== 0) {
    console.log('FAILED');
    console.error(result.stderr);
    failed++;
  } else {
    console.log('OK');
  }
}

if (failed > 0) {
  console.error(`\n${failed} of ${replies.length} replies failed`);
  process.exit(1);
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/scripts/batch-reply.ts
// To modify, edit the source file and run the distribution workflow

