#!/usr/bin/env node
/**
 * Check Stack Status
 * 
 * Checks CI and CodeRabbit status for all PRs in a stack.
 * 
 * Usage:
 *   node check-stack-status.js --prs 79,80,81 --repo bitsoex/ai-code-instructions
 *   node check-stack-status.js --prs 79,80,81  # Uses current repo
 */

import { execSync, spawnSync } from 'child_process';

// Parse arguments
const args = process.argv.slice(2);
let prNumbers = [];
let repo = '';

for (let i = 0; i < args.length; i++) {
  if (args[i] === '--prs' && args[i + 1]) {
    prNumbers = args[i + 1].split(',').map(n => parseInt(n.trim(), 10));
    i++;
  } else if (args[i] === '--repo' && args[i + 1]) {
    repo = args[i + 1];
    i++;
  }
}

if (prNumbers.length === 0) {
  console.error('Usage: node check-stack-status.js --prs 79,80,81 [--repo owner/repo]');
  process.exit(1);
}

// Get repo from git remote if not specified
if (!repo) {
  try {
    const remoteUrl = execSync('git remote get-url origin', { encoding: 'utf-8' }).trim();
    const match = remoteUrl.match(/github\.com[:/](.+?)(?:\.git)?$/);
    if (match) {
      repo = match[1];
    }
  } catch {
    console.error('Could not determine repository. Use --repo owner/repo');
    process.exit(1);
  }
}

console.log(`\n📚 Checking stack status for ${repo}\n`);
console.log('='.repeat(70));

for (const prNumber of prNumbers) {
  console.log(`\n📋 PR #${prNumber}`);
  console.log('-'.repeat(40));
  
  // Get PR info
  try {
    const prInfo = JSON.parse(
      execSync(`gh pr view ${prNumber} --repo ${repo} --json title,state,isDraft`, { encoding: 'utf-8' })
    );
    
    console.log(`   Title: ${prInfo.title}`);
    console.log(`   State: ${prInfo.state}`);
    console.log(`   Draft: ${prInfo.isDraft}`);
  } catch (error) {
    console.log(`   ❌ Could not fetch PR info: ${error.message}`);
    continue;
  }
  
  // Get CI status
  console.log('\n   CI Checks:');
  try {
    const checks = execSync(`gh pr checks ${prNumber} --repo ${repo}`, { encoding: 'utf-8' });
    const lines = checks.trim().split('\n');
    let allPassing = true;
    let pending = false;
    
    for (const line of lines) {
      if (line.includes('fail')) {
        console.log(`      ❌ ${line.split('\t')[0]}`);
        allPassing = false;
      } else if (line.includes('pending')) {
        console.log(`      ⏳ ${line.split('\t')[0]}`);
        pending = true;
      }
    }
    
    if (allPassing && !pending) {
      console.log('      ✅ All checks passing');
    } else if (pending) {
      console.log('      ⏳ Some checks still running');
    }
  } catch {
    console.log('      ⚠️  Could not fetch CI status');
  }
  
  // Get CodeRabbit status
  console.log('\n   CodeRabbit:');
  try {
    const reviews = JSON.parse(
      execSync(`gh pr view ${prNumber} --repo ${repo} --json reviews`, { encoding: 'utf-8' })
    );
    
    const coderabbitReviews = reviews.reviews.filter(r => r.author.login === 'coderabbitai');
    if (coderabbitReviews.length > 0) {
      const lastReview = coderabbitReviews[coderabbitReviews.length - 1];
      let stateEmoji = '💬';
      if (lastReview.state === 'APPROVED') {
        stateEmoji = '✅';
      } else if (lastReview.state === 'CHANGES_REQUESTED') {
        stateEmoji = '❌';
      }
      console.log(`      ${stateEmoji} Last review: ${lastReview.state}`);
    } else {
      console.log('      ⏳ No review yet');
    }
    
    // Count open comments
    const [owner, repoName] = repo.split('/');
    const graphqlQuery = `
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
      '-f', `query=${graphqlQuery}`,
      '-f', `owner=${owner}`,
      '-f', `repo=${repoName}`,
      '-F', `pr=${prNumber}`
    ], { encoding: 'utf-8' });
    
    if (result.status === 0) {
      const data = JSON.parse(result.stdout);
      const threads = data.data.repository.pullRequest.reviewThreads.nodes;
      const openCoderabbitThreads = threads.filter(t => 
        !t.isResolved && 
        t.comments.nodes[0]?.author?.login === 'coderabbitai'
      );
      
      if (openCoderabbitThreads.length > 0) {
        console.log(`      ⚠️  ${openCoderabbitThreads.length} unresolved comment(s)`);
      } else {
        console.log('      ✅ All comments resolved');
      }
    }
  } catch {
    console.log('      ⚠️  Could not fetch CodeRabbit status');
  }
}

console.log(`\n${'='.repeat(70)}`);
console.log('\n✨ Stack status check complete\n');
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/stacked-prs/scripts/check-stack-status.js
// To modify, edit the source file and run the distribution workflow

