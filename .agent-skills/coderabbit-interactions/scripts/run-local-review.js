#!/usr/bin/env node
/**
 * Run CodeRabbit CLI locally and save findings to .tmp/
 * 
 * Usage:
 *   node run-local-review.js [--type uncommitted|committed|all] [--base main]
 * 
 * Output:
 *   .tmp/coderabbit-local-{timestamp}.json
 */

import { spawnSync } from 'child_process';
import fs from 'fs';
import path from 'path';

const COAUTHOR = 'Co-authored-by: coderabbitai[bot] <136622811+coderabbitai[bot]@users.noreply.github.com>';

function parseArgs() {
  const args = process.argv.slice(2);
  const options = {
    type: 'uncommitted',
    base: 'main'
  };
  
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--type' && args[i + 1]) {
      options.type = args[i + 1];
      i++;
    } else if (args[i] === '--base' && args[i + 1]) {
      options.base = args[i + 1];
      i++;
    }
  }
  
  return options;
}

function checkCodeRabbitInstalled() {
  const result = spawnSync('coderabbit', ['--version'], { encoding: 'utf-8', shell: true });
  if (result.status !== 0) {
    console.error('❌ CodeRabbit CLI not installed');
    console.error('   Install: curl -fsSL https://cli.coderabbit.ai/install.sh | sh');
    process.exit(1);
  }
  return result.stdout.trim();
}

function checkAuthenticated() {
  const result = spawnSync('coderabbit', ['auth', 'status'], { encoding: 'utf-8', shell: true });
  if (result.status !== 0 || result.stdout.includes('not authenticated')) {
    console.error('❌ CodeRabbit not authenticated');
    console.error('   Run: coderabbit auth login');
    process.exit(1);
  }
  return true;
}

function runReview(options) {
  console.log(`🔍 Running CodeRabbit review (type: ${options.type}, base: ${options.base})`);
  console.log('   This may take 7-30 minutes...');
  console.log('');
  
  const args = ['--prompt-only', '--type', options.type, '--base', options.base];
  
  const result = spawnSync('coderabbit', args, {
    encoding: 'utf-8',
    shell: true,
    maxBuffer: 10 * 1024 * 1024
  });
  
  return {
    success: result.status === 0,
    stdout: result.stdout,
    stderr: result.stderr,
    duration: Date.now()
  };
}

function parseFindings(output) {
  // Parse CodeRabbit --prompt-only output
  // Format varies but typically includes file paths and issue descriptions
  const findings = [];
  const lines = output.split('\n');
  
  let currentFile = null;
  // currentIssue is reserved for future enhanced parsing
  // const currentIssue = null;
  
  for (const line of lines) {
    // Look for file references
    const fileMatch = line.match(/^([a-zA-Z0-9_\-./]+\.(js|ts|java|py|go|md)):?/);
    if (fileMatch) {
      currentFile = fileMatch[1];
    }
    
    // Look for severity indicators
    if (line.includes('🔴') || line.includes('Critical') || line.includes('critical')) {
      findings.push({ file: currentFile, severity: 'critical', description: line.trim() });
    } else if (line.includes('🟠') || line.includes('Major') || line.includes('major')) {
      findings.push({ file: currentFile, severity: 'major', description: line.trim() });
    } else if (line.includes('🟡') || line.includes('Minor') || line.includes('minor')) {
      findings.push({ file: currentFile, severity: 'minor', description: line.trim() });
    }
  }
  
  return findings;
}

function saveResults(options, result, findings) {
  const tmpDir = path.join(process.cwd(), '.tmp');
  if (!fs.existsSync(tmpDir)) {
    fs.mkdirSync(tmpDir, { recursive: true });
  }
  
  const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '-').slice(0, 15);
  const outputFile = path.join(tmpDir, `coderabbit-local-${timestamp}.json`);
  
  const data = {
    source: 'cli',
    reviewedAt: new Date().toISOString(),
    options,
    success: result.success,
    summary: {
      total: findings.length,
      critical: findings.filter(f => f.severity === 'critical').length,
      major: findings.filter(f => f.severity === 'major').length,
      minor: findings.filter(f => f.severity === 'minor').length
    },
    findings,
    rawOutput: result.stdout,
    commitTemplate: generateCommitTemplate(findings),
    coauthor: COAUTHOR
  };
  
  fs.writeFileSync(outputFile, JSON.stringify(data, null, 2));
  return outputFile;
}

function generateCommitTemplate(findings) {
  const fileIssues = new Map();
  
  for (const f of findings) {
    if (f.file && !fileIssues.has(f.file)) {
      fileIssues.set(f.file, f.description.slice(0, 60));
    }
  }
  
  const bulletPoints = Array.from(fileIssues.entries())
    .slice(0, 5)
    .map(([file, desc]) => `- [${file}]: ${desc}`)
    .join('\n');
  
  return `🤖 fix: address CodeRabbit CLI review findings

${bulletPoints}

Reviewed-by: CodeRabbit CLI (local)
${COAUTHOR}`;
}

function main() {
  const options = parseArgs();
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           CodeRabbit Local Review                            ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  
  // Check prerequisites
  const version = checkCodeRabbitInstalled();
  console.log(`✓ CodeRabbit CLI: ${version}`);
  
  checkAuthenticated();
  console.log('✓ Authenticated');
  console.log('');
  
  // Run review
  const startTime = Date.now();
  const result = runReview(options);
  const duration = ((Date.now() - startTime) / 1000 / 60).toFixed(1);
  
  if (!result.success) {
    console.error('❌ Review failed');
    console.error(result.stderr);
    process.exit(1);
  }
  
  // Parse and save
  const findings = parseFindings(result.stdout);
  const outputFile = saveResults(options, result, findings);
  
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════╗');
  console.log('║           Review Complete                                    ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log('');
  console.log(`Duration: ${duration} minutes`);
  console.log(`Findings: ${findings.length} total`);
  console.log(`  - Critical: ${findings.filter(f => f.severity === 'critical').length}`);
  console.log(`  - Major: ${findings.filter(f => f.severity === 'major').length}`);
  console.log(`  - Minor: ${findings.filter(f => f.severity === 'minor').length}`);
  console.log('');
  console.log(`Output: ${outputFile}`);
  console.log('');
  
  if (findings.length > 0) {
    console.log('Commit template (after fixing):');
    console.log('─'.repeat(60));
    console.log(generateCommitTemplate(findings));
    console.log('─'.repeat(60));
  }
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-interactions/scripts/run-local-review.js
// To modify, edit the source file and run the distribution workflow

