#!/usr/bin/env node
/**
 * Export CodeRabbit PR comments to local JSON file for processing
 * 
 * Usage:
 *   node export-comments.ts --pr 123 [--repo owner/repo]
 * 
 * Output:
 *   .tmp/coderabbit-pr-{number}-{timestamp}.json
 */

import { spawnSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { 
  type Review, 
  type ReviewBodySection, 
  type ProcessedComment,
  processReviews 
} from './parse-review-body.ts';

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
  comments: { nodes: CommentNode[] };
}

interface GraphQLResponse {
  data: {
    repository: {
      pullRequest: {
        reviewThreads: { nodes: ReviewThread[] };
      };
    };
  };
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
    nitpick: number;
    outsideDiff: number;
    additional: number;
  };
  comments: ProcessedComment[];
  reviewBodySections: ReviewBodySection[];
  aiFixes: string | null;
  commitTemplates: { single: string; batch: string };
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
  const options: ParseOptions = { pr: null, repo: null };
  
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--pr' && args[i + 1]) {
      const prNum = parseInt(args[i + 1]!, 10);
      if (!isNaN(prNum)) { options.pr = prNum; i++; }
    } else if (args[i] === '--repo' && args[i + 1]) {
      options.repo = args[i + 1]!; i++;
    }
  }
  
  // Auto-detect repo
  if (!options.repo) {
    const result = spawnSync('gh', ['repo', 'view', '--json', 'nameWithOwner', '--jq', '.nameWithOwner'], { encoding: 'utf-8' });
    if (result.status === 0) {options.repo = result.stdout.trim();}
  }
  
  // Auto-detect PR
  if (!options.pr) {
    const result = spawnSync('gh', ['pr', 'view', '--json', 'number', '--jq', '.number'], { encoding: 'utf-8' });
    if (result.status === 0) {options.pr = parseInt(result.stdout.trim(), 10);}
  }
  
  return options;
}

function extractSeverity(body: string): string {
  if (body.includes('🔴 Critical') || body.includes('_🔴 Critical_')) {return 'critical';}
  if (body.includes('🟠 Major') || body.includes('_🟠 Major_')) {return 'major';}
  if (body.includes('🟡 Minor') || body.includes('_🟡 Minor_')) {return 'minor';}
  return 'unknown';
}

function extractTitle(body: string): string {
  const match = body.match(/\*\*(.+?)\*\*/);
  return match ? match[1]!.slice(0, 80) : body.split('\n')[0]!.slice(0, 80);
}

function fetchReviews(options: { pr: number; repo: string }): Review[] {
  const result = spawnSync('gh', ['api', `repos/${options.repo}/pulls/${options.pr}/reviews`, '--paginate'], 
    { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 });
  if (result.status !== 0) {return [];}
  try { return JSON.parse(result.stdout) as Review[]; } catch { return []; }
}

function fetchComments(options: { pr: number; repo: string }): GraphQLResponse {
  const [owner, repo] = options.repo.split('/');
  const query = `query($owner: String!, $repo: String!, $pr: Int!) {
    repository(owner: $owner, name: $repo) {
      pullRequest(number: $pr) {
        reviewThreads(first: 100) {
          nodes { id isResolved path line comments(first: 10) { nodes { id author { login } body createdAt } } }
        }
      }
    }
  }`;
  
  const result = spawnSync('gh', ['api', 'graphql', '-f', `query=${query}`, '-f', `owner=${owner}`, '-f', `repo=${repo}`, '-F', `pr=${options.pr}`],
    { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 });
  
  if (result.status !== 0) { console.error('Failed to fetch comments:', result.stderr); process.exit(1); }
  try { return JSON.parse(result.stdout) as GraphQLResponse; } 
  catch (e) { console.error('Failed to parse response:', e instanceof Error ? e.message : e); process.exit(1); }
}

function processThreadComments(data: GraphQLResponse): ProcessedComment[] {
  const threads = data.data?.repository?.pullRequest?.reviewThreads?.nodes ?? [];
  return threads
    .filter(t => !t.isResolved && t.comments?.nodes?.[0]?.author?.login === 'coderabbitai')
    .map(t => ({
      id: t.id, path: t.path, line: t.line,
      severity: extractSeverity(t.comments.nodes[0]!.body),
      title: extractTitle(t.comments.nodes[0]!.body),
      body: t.comments.nodes[0]!.body,
      createdAt: t.comments.nodes[0]!.createdAt,
      replyCount: t.comments.nodes.length - 1,
      status: 'pending', resolution: null,
      source: 'thread' as const, category: 'actionable' as const
    }))
    .sort((a, b) => {
      const order: Record<string, number> = { critical: 0, major: 1, minor: 2, unknown: 3, info: 4 };
      return (order[a.severity] ?? 3) - (order[b.severity] ?? 3);
    });
}

function generateCommitTemplate(comments: ProcessedComment[], single = false): string {
  if (single && comments.length === 1) {
    const c = comments[0]!;
    return `fix: address CodeRabbit PR feedback\n\nThread: ${c.id}\n- [${c.path}${c.line ? `:${c.line}` : ''}]: ${c.title.slice(0, 50)}\n\n${COAUTHOR}`;
  }
  
  const bySeverity = { critical: comments.filter(c => c.severity === 'critical'), major: comments.filter(c => c.severity === 'major'), minor: comments.filter(c => c.severity === 'minor') };
  let highest = 'minor';
  if (bySeverity.critical.length > 0) {
    highest = 'critical';
  } else if (bySeverity.major.length > 0) {
    highest = 'major';
  }
  const threads = comments.slice(0, 5).map(c => `- ${c.id}: ${c.title.slice(0, 40)}`).join('\n');
  return `fix: address ${comments.length} ${highest} CodeRabbit issues\n\nThreads:\n${threads}\n\n${COAUTHOR}`;
}

function saveResults(options: { pr: number; repo: string }, comments: ProcessedComment[], sections: ReviewBodySection[], aiFixes: string | null): string {
  const tmpDir = path.join(process.cwd(), '.tmp');
  if (!fs.existsSync(tmpDir)) {fs.mkdirSync(tmpDir, { recursive: true });}
  
  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '-').slice(0, 15);
  const outputFile = path.join(tmpDir, `coderabbit-pr-${options.pr}-${timestamp}.json`);
  const actionable = comments.filter(c => c.category !== 'additional');
  
  const data: ExportData = {
    source: 'pr', pr: options.pr, repo: options.repo, exportedAt: new Date().toISOString(),
    summary: {
      total: comments.length,
      critical: comments.filter(c => c.severity === 'critical').length,
      major: comments.filter(c => c.severity === 'major').length,
      minor: comments.filter(c => c.severity === 'minor').length,
      nitpick: comments.filter(c => c.category === 'nitpick').length,
      outsideDiff: comments.filter(c => c.category === 'outside-diff').length,
      additional: comments.filter(c => c.category === 'additional').length
    },
    comments, reviewBodySections: sections, aiFixes,
    commitTemplates: { single: generateCommitTemplate(actionable, true), batch: generateCommitTemplate(actionable, false) },
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
    console.error('Usage: node export-comments.ts --pr 123 [--repo owner/repo]');
    process.exit(1);
  }
  
  console.log('\n╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Export CodeRabbit PR Comments                      ║');
  console.log('╚══════════════════════════════════════════════════════════════╝\n');
  console.log(`PR: #${options.pr}\nRepo: ${options.repo}\n`);
  
  // Fetch inline thread comments
  console.log('Fetching inline thread comments...');
  const threadComments = processThreadComments(fetchComments({ pr: options.pr, repo: options.repo }));
  
  // Fetch review body comments
  console.log('Fetching review body sections...');
  const { comments: reviewBodyComments, sections, aiFixes } = processReviews(fetchReviews({ pr: options.pr, repo: options.repo }));
  
  // Combine and sort
  const allComments = [...threadComments, ...reviewBodyComments].sort((a, b) => {
    const order: Record<string, number> = { critical: 0, major: 1, minor: 2, unknown: 3, info: 4 };
    return (order[a.severity] ?? 3) - (order[b.severity] ?? 3);
  });
  
  const outputFile = saveResults({ pr: options.pr, repo: options.repo }, allComments, sections, aiFixes);
  
  console.log('\n╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Export Complete                                    ║');
  console.log('╚══════════════════════════════════════════════════════════════╝\n');
  console.log(`Total comments: ${allComments.length}\n`);
  console.log('  📝 Inline threads:');
  console.log(`    - Critical: ${threadComments.filter(c => c.severity === 'critical').length}`);
  console.log(`    - Major: ${threadComments.filter(c => c.severity === 'major').length}`);
  console.log(`    - Minor: ${threadComments.filter(c => c.severity === 'minor').length}\n`);
  console.log('  📋 Review body sections:');
  for (const s of sections) {
    let emoji = '🔇';
    if (s.category === 'outside-diff') {
      emoji = '⚠️';
    } else if (s.category === 'nitpick') {
      emoji = '🧹';
    }
    console.log(`    ${emoji} ${s.category}: ${s.count} (${s.files.length} files)`);
  }
  if (aiFixes) {console.log('    🤖 AI fix instructions: included');}
  console.log(`\nOutput: ${outputFile}\n`);
  
  // Show actionable items
  const actionable = allComments.filter(c => c.category !== 'additional');
  if (actionable.length > 0) {
    console.log(`Actionable issues:\n${'─'.repeat(60)}`);
    for (const c of actionable.slice(0, 15)) {
      let sev = '🟡';
      if (c.severity === 'critical') {
        sev = '🔴';
      } else if (c.severity === 'major') {
        sev = '🟠';
      } else if (c.category === 'nitpick') {
        sev = '🧹';
      } else if (c.category === 'outside-diff') {
        sev = '⚠️';
      }
      const src = c.source === 'review-body' ? `[${c.category}]` : '';
      console.log(`${sev} ${c.path}:${c.line ?? 'file'} ${src} - ${c.title.slice(0, 35)}`);
    }
    if (actionable.length > 15) {
      console.log(`... and ${actionable.length - 15} more`);
    }
    console.log('─'.repeat(60));
  }
  
  if (aiFixes) {
    console.log(`\n🤖 AI Fix Instructions:\n${'─'.repeat(60)}`);
    const truncated = aiFixes.length > 500 ? `${aiFixes.slice(0, 500)}...\n(Full in ${outputFile})` : aiFixes;
    console.log(truncated);
    console.log('─'.repeat(60));
  }
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/scripts/export-comments.ts
// To modify, edit the source file and run the distribution workflow

