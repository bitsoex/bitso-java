#!/usr/bin/env node
/**
 * Export CodeRabbit PR comments to local JSON file for processing
 * 
 * Usage:
 *   node export-comments.js --pr 123 [--repo owner/repo]
 * 
 * Output:
 *   .tmp/coderabbit-pr-{number}-{timestamp}.json
 */

import { spawnSync } from 'child_process';
import fs from 'fs';
import path from 'path';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

interface ParseOptions {
  pr: number | null;
  repo: string | null;
}

interface CommentNode {
  id: string;
  author: { login: string } | null;
  body: string;
  createdAt: string;
}

interface ReviewThread {
  id: string;
  isResolved: boolean;
  path: string;
  line: number | null;
  comments: {
    nodes: CommentNode[];
  };
}

interface GraphQLResponse {
  data: {
    repository: {
      pullRequest: {
        reviewThreads: {
          nodes: ReviewThread[];
        };
      };
    };
  };
}

interface ProcessedComment {
  id: string;
  path: string;
  line: number | null;
  severity: string;
  title: string;
  body: string;
  createdAt: string;
  replyCount: number;
  status: string;
  resolution: string | null;
}

interface ExportData {
  source: string;
  pr: number;
  repo: string;
  exportedAt: string;
  summary: {
    total: number;
    critical: number;
    major: number;
    minor: number;
  };
  comments: ProcessedComment[];
  commitTemplates: {
    single: string;
    batch: string;
  };
  coauthor: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Constants
// ═══════════════════════════════════════════════════════════════════════════

const COAUTHOR = 'Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>';

// ═══════════════════════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════════════════════

function parseArgs(): ParseOptions {
  const args = process.argv.slice(2);
  const options: ParseOptions = {
    pr: null,
    repo: null
  };
  
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--pr' && args[i + 1]) {
      const prNum = parseInt(args[i + 1]!, 10);
      if (isNaN(prNum)) {
        console.error(`Error: Invalid PR number: ${args[i + 1]}`);
        process.exit(1);
      }
      options.pr = prNum;
      i++;
    } else if (args[i] === '--repo' && args[i + 1]) {
      options.repo = args[i + 1]!;
      i++;
    }
  }
  
  // Auto-detect repo if not provided
  if (!options.repo) {
    const result = spawnSync('gh', ['repo', 'view', '--json', 'nameWithOwner', '--jq', '.nameWithOwner'], {
      encoding: 'utf-8'
    });
    if (result.status === 0) {
      options.repo = result.stdout.trim();
    }
  }
  
  // Auto-detect PR if not provided
  if (!options.pr) {
    const result = spawnSync('gh', ['pr', 'view', '--json', 'number', '--jq', '.number'], {
      encoding: 'utf-8'
    });
    if (result.status === 0) {
      options.pr = parseInt(result.stdout.trim(), 10);
    }
  }
  
  return options;
}

function extractSeverity(body: string): string {
  if (body.includes('🔴 Critical') || body.includes('_🔴 Critical_')) { return 'critical'; }
  if (body.includes('🟠 Major') || body.includes('_🟠 Major_')) { return 'major'; }
  if (body.includes('🟡 Minor') || body.includes('_🟡 Minor_')) { return 'minor'; }
  return 'unknown';
}

function extractTitle(body: string): string {
  const match = body.match(/\*\*(.+?)\*\*/);
  return match ? match[1]!.slice(0, 80) : body.split('\n')[0]!.slice(0, 80);
}

function fetchComments(options: { pr: number; repo: string }): GraphQLResponse {
  const [owner, repo] = options.repo.split('/');
  
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
          comments(first: 10) {
            nodes {
              id
              author { login }
              body
              createdAt
            }
          }
        }
      }
    }
  }
}`;

  const result = spawnSync('gh', [
    'api', 'graphql',
    '-f', `query=${query}`,
    '-f', `owner=${owner}`,
    '-f', `repo=${repo}`,
    '-F', `pr=${options.pr}`
  ], {
    encoding: 'utf-8',
    maxBuffer: 10 * 1024 * 1024
  });
  
  if (result.status !== 0) {
    console.error('Failed to fetch comments:', result.stderr);
    process.exit(1);
  }
  
  try {
    return JSON.parse(result.stdout) as GraphQLResponse;
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e);
    console.error('Failed to parse GraphQL response:', message);
    process.exit(1);
  }
}

function processComments(data: GraphQLResponse): ProcessedComment[] {
  const threads = data.data?.repository?.pullRequest?.reviewThreads?.nodes ?? [];
  
  return threads
    .filter(t => !t.isResolved)
    .filter(t => t.comments?.nodes?.[0]?.author?.login === 'coderabbitai')
    .map(t => ({
      id: t.id,
      path: t.path,
      line: t.line,
      severity: extractSeverity(t.comments.nodes[0]!.body),
      title: extractTitle(t.comments.nodes[0]!.body),
      body: t.comments.nodes[0]!.body,
      createdAt: t.comments.nodes[0]!.createdAt,
      replyCount: t.comments.nodes.length - 1,
      status: 'pending',
      resolution: null
    }))
    .sort((a, b) => {
      const order: Record<string, number> = { critical: 0, major: 1, minor: 2, unknown: 3 };
      return (order[a.severity] ?? 3) - (order[b.severity] ?? 3);
    });
}

function generateCommitTemplate(comments: ProcessedComment[], single = false): string {
  if (single && comments.length === 1) {
    const c = comments[0]!;
    return `🤖 fix: address CodeRabbit PR feedback

Thread: ${c.id}
- [${c.path}${c.line ? `:${c.line}` : ''}]: ${c.title.slice(0, 50)}

${COAUTHOR}`;
  }
  
  const bySeverity = {
    critical: comments.filter(c => c.severity === 'critical'),
    major: comments.filter(c => c.severity === 'major'),
    minor: comments.filter(c => c.severity === 'minor')
  };
  
  let highest = 'minor';
  if (bySeverity.critical.length > 0) {
    highest = 'critical';
  } else if (bySeverity.major.length > 0) {
    highest = 'major';
  }
  
  const threads = comments.slice(0, 5).map(c => 
    `- ${c.id}: ${c.title.slice(0, 40)}`
  ).join('\n');
  
  return `🤖 fix: address ${comments.length} ${highest} CodeRabbit issues

Threads:
${threads}

${COAUTHOR}`;
}

function saveResults(options: { pr: number; repo: string }, comments: ProcessedComment[]): string {
  const tmpDir = path.join(process.cwd(), '.tmp');
  if (!fs.existsSync(tmpDir)) {
    fs.mkdirSync(tmpDir, { recursive: true });
  }
  
  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '-').slice(0, 15);
  const outputFile = path.join(tmpDir, `coderabbit-pr-${options.pr}-${timestamp}.json`);
  
  const data: ExportData = {
    source: 'pr',
    pr: options.pr,
    repo: options.repo,
    exportedAt: new Date().toISOString(),
    summary: {
      total: comments.length,
      critical: comments.filter(c => c.severity === 'critical').length,
      major: comments.filter(c => c.severity === 'major').length,
      minor: comments.filter(c => c.severity === 'minor').length
    },
    comments,
    commitTemplates: {
      single: generateCommitTemplate(comments, true),
      batch: generateCommitTemplate(comments, false)
    },
    coauthor: COAUTHOR
  };
  
  fs.writeFileSync(outputFile, JSON.stringify(data, null, 2));
  return outputFile;
}

// ═══════════════════════════════════════════════════════════════════════════
// Main
// ═══════════════════════════════════════════════════════════════════════════

function main(): void {
  const options = parseArgs();
  
  if (!options.pr || !options.repo) {
    console.error('Usage: node export-comments.js --pr 123 [--repo owner/repo]');
    if (!options.pr) {
      console.error('Error: Could not determine PR number. Use --pr to specify.');
    }
    if (!options.repo) {
      console.error('Error: Could not determine repository. Use --repo owner/repo to specify.');
    }
    process.exit(1);
  }
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Export CodeRabbit PR Comments                      ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`PR: #${options.pr}`);
  console.log(`Repo: ${options.repo}`);
  console.log('');
  
  // Fetch comments
  console.log('Fetching comments...');
  const data = fetchComments({ pr: options.pr, repo: options.repo });
  
  // Process
  const comments = processComments(data);
  
  // Save
  const outputFile = saveResults({ pr: options.pr, repo: options.repo }, comments);
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Export Complete                                    ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`Total comments: ${comments.length}`);
  console.log(`  - Critical: ${comments.filter(c => c.severity === 'critical').length}`);
  console.log(`  - Major: ${comments.filter(c => c.severity === 'major').length}`);
  console.log(`  - Minor: ${comments.filter(c => c.severity === 'minor').length}`);
  console.log('');
  console.log(`Output: ${outputFile}`);
  console.log('');
  
  // Show pending by severity
  if (comments.length > 0) {
    console.log('Pending issues:');
    console.log('─'.repeat(60));
    for (const c of comments.slice(0, 10)) {
      let sev = '🟡';
      if (c.severity === 'critical') {
        sev = '🔴';
      } else if (c.severity === 'major') {
        sev = '🟠';
      }
      console.log(`${sev} ${c.path}:${c.line ?? 'file'} - ${c.title.slice(0, 40)}`);
    }
    if (comments.length > 10) {
      console.log(`... and ${comments.length - 10} more`);
    }
    console.log('─'.repeat(60));
  }
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-interactions/scripts/export-comments.ts
// To modify, edit the source file and run the distribution workflow

