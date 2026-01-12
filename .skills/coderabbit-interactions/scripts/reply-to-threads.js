#!/usr/bin/env node
/**
 * Reply to CodeRabbit PR threads based on processed JSON file
 * 
 * Usage:
 *   node reply-to-threads.js --file .tmp/coderabbit-pr-123-*.json
 * 
 * Only replies to comments with status !== 'pending'
 */

import { spawnSync } from 'child_process';
import fs from 'fs';

function parseArgs() {
  const args = process.argv.slice(2);
  const options = {
    file: null,
    dryRun: false
  };
  
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--file' && args[i + 1]) {
      options.file = args[i + 1];
      i++;
    } else if (args[i] === '--dry-run') {
      options.dryRun = true;
    }
  }
  
  return options;
}

function generateReplyBody(comment) {
  const commitHash = getLatestCommitHash();
  
  switch (comment.status) {
    case 'fixed':
      return `🤖 Fixed in ${commitHash}. ${comment.resolution || 'Applied suggested fix.'}`;
    
    case 'wontfix':
      return `🤖 Acknowledged. ${comment.resolution || 'Will address in a follow-up.'}`;
    
    case 'not-applicable':
      return `🤖 Not applicable - ${comment.resolution || 'code has changed.'}`;
    
    default:
      return null;
  }
}

function getLatestCommitHash() {
  const result = spawnSync('git', ['rev-parse', '--short', 'HEAD'], {
    encoding: 'utf-8'
  });
  return result.status === 0 ? result.stdout.trim() : 'latest';
}

function replyToThread(threadId, body, dryRun) {
  if (dryRun) {
    console.log(`  [DRY RUN] Would reply to ${threadId}:`);
    console.log(`    ${body.slice(0, 60)}...`);
    return true;
  }
  
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
  ], {
    encoding: 'utf-8'
  });
  
  if (result.status !== 0) {
    console.error(`  ❌ Failed to reply to ${threadId}:`, result.stderr);
    return false;
  }
  
  return true;
}

function main() {
  const options = parseArgs();
  
  if (!options.file) {
    console.error('Usage: node reply-to-threads.js --file .tmp/coderabbit-pr-*.json [--dry-run]');
    process.exit(1);
  }
  
  if (!fs.existsSync(options.file)) {
    console.error(`File not found: ${options.file}`);
    process.exit(1);
  }
  
  let data;
  try {
    data = JSON.parse(fs.readFileSync(options.file, 'utf-8'));
  } catch (err) {
    console.error(`Invalid JSON in ${options.file}: ${err.message}`);
    process.exit(1);
  }
  
  if (!data.pr || !Array.isArray(data.comments)) {
    console.error('Invalid file format: expected { pr, comments: [...] }');
    process.exit(1);
  }
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Reply to CodeRabbit Threads                        ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`Source: ${options.file}`);
  console.log(`PR: #${data.pr}`);
  console.log(`Mode: ${options.dryRun ? 'DRY RUN' : 'LIVE'}`);
  console.log('');
  
  // Filter comments that need replies
  const toReply = data.comments.filter(c => 
    c.status !== 'pending' && c.id
  );
  
  if (toReply.length === 0) {
    console.log('No comments to reply to (all still pending or no ID)');
    process.exit(0);
  }
  
  console.log(`Replying to ${toReply.length} threads:`);
  console.log('');
  
  let success = 0;
  let failed = 0;
  
  for (const comment of toReply) {
    const body = generateReplyBody(comment);
    if (!body) {
      console.log(`  ⏭ Skipping ${comment.id} (no reply needed)`);
      continue;
    }
    
    const status = comment.status === 'fixed' ? '✅' :
                   comment.status === 'wontfix' ? '📝' : '⏭';
    
    console.log(`${status} ${comment.path}:${comment.line || 'file'}`);
    
    const ok = replyToThread(comment.id, body, options.dryRun);
    if (ok) {
      success++;
    } else {
      failed++;
    }
  }
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Replies Complete                                   ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`Success: ${success}`);
  console.log(`Failed: ${failed}`);
  
  if (failed > 0) {
    process.exit(1);
  }
}

main();
