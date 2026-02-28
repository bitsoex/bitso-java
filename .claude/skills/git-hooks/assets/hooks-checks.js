#!/usr/bin/env node
/**
 * AI Agents Quality Checks - Informative Mode
 *
 * This script runs quality checks and reports issues without blocking.
 * It detects the project type and runs appropriate checks.
 *
 * Usage:
 *   node hooks-checks.js pre-commit
 *   node hooks-checks.js pre-push
 *
 * Environment:
 *   AI_AGENTS_HOOKS_QUIET=1    - Suppress output when no issues found
 *   AI_AGENTS_HOOKS_VERBOSE=1  - Show detailed output
 */

import { execSync, spawnSync } from 'child_process';
import fs from 'fs';
import path from 'path';

// Configuration
const HOOK_TYPE = process.argv[2] || 'pre-commit';
const ROOT_DIR = process.cwd();
const QUIET = process.env.AI_AGENTS_HOOKS_QUIET === '1';
const VERBOSE = process.env.AI_AGENTS_HOOKS_VERBOSE === '1';

// Colors
const useColors = process.stdout.isTTY && !process.env.NO_COLOR;
const colors = {
  red: useColors ? '\x1b[31m' : '',
  yellow: useColors ? '\x1b[33m' : '',
  green: useColors ? '\x1b[32m' : '',
  dim: useColors ? '\x1b[2m' : '',
  bold: useColors ? '\x1b[1m' : '',
  reset: useColors ? '\x1b[0m' : '',
};

// Issue tracking
const issues = [];

/**
 * Log a message
 */
function log(message) {
  if (!QUIET) {
    console.log(message);
  }
}

/**
 * Log verbose message
 */
function verbose(message) {
  if (VERBOSE) {
    console.log(`${colors.dim}${message}${colors.reset}`);
  }
}

/**
 * Add an issue to the report
 */
function addIssue(category, message, fix = null) {
  issues.push({ category, message, fix });
}

/**
 * Detect project type based on files present
 */
function detectProjectType() {
  const types = [];

  if (fs.existsSync(path.join(ROOT_DIR, 'build.gradle')) ||
      fs.existsSync(path.join(ROOT_DIR, 'build.gradle.kts')) ||
      fs.existsSync(path.join(ROOT_DIR, 'settings.gradle')) ||
      fs.existsSync(path.join(ROOT_DIR, 'settings.gradle.kts'))) {
    types.push('gradle');
  }

  if (fs.existsSync(path.join(ROOT_DIR, 'package.json'))) {
    types.push('node');
  }

  if (fs.existsSync(path.join(ROOT_DIR, 'requirements.txt')) ||
      fs.existsSync(path.join(ROOT_DIR, 'pyproject.toml')) ||
      fs.existsSync(path.join(ROOT_DIR, 'setup.py'))) {
    types.push('python');
  }

  if (fs.existsSync(path.join(ROOT_DIR, 'go.mod'))) {
    types.push('go');
  }

  return types;
}

/**
 * Run a command and return result
 */
function runCommand(command, args = [], options = {}) {
  verbose(`Running: ${command} ${args.join(' ')}`);

  try {
    const result = spawnSync(command, args, {
      cwd: ROOT_DIR,
      encoding: 'utf-8',
      timeout: 120000, // 2 minute timeout
      ...options,
    });

    return {
      success: result.status === 0,
      stdout: result.stdout || '',
      stderr: result.stderr || '',
      status: result.status,
    };
  } catch (error) {
    return {
      success: false,
      stdout: '',
      stderr: error.message,
      status: -1,
    };
  }
}

/**
 * Check if a command exists
 */
function commandExists(command) {
  try {
    execSync(`command -v ${command}`, { encoding: 'utf-8', stdio: 'pipe' });
    return true;
  } catch {
    return false;
  }
}

// =============================================================================
// Gradle/Java Checks
// =============================================================================

function checkGradle() {
  verbose('Checking Gradle project...');

  const gradleCmd = fs.existsSync(path.join(ROOT_DIR, 'gradlew')) ? './gradlew' : 'gradle';

  // Check if Spotless is available
  const tasksResult = runCommand(gradleCmd, ['tasks', '--all', '-q']);
  const hasSpotless = tasksResult.stdout.includes('spotless');

  if (hasSpotless) {
    const spotlessResult = runCommand(gradleCmd, ['spotlessCheck', '-q']);
    if (!spotlessResult.success) {
      addIssue('Formatting', 'Code formatting issues detected', `${gradleCmd} spotlessApply`);
    }
  }

  // Check compilation
  if (HOOK_TYPE === 'pre-push') {
    const compileResult = runCommand(gradleCmd, ['compileJava', '-q']);
    if (!compileResult.success) {
      addIssue('Compilation', 'Java compilation errors', `${gradleCmd} compileJava`);
    }

    // Check tests
    const testResult = runCommand(gradleCmd, ['test', '-q']);
    if (!testResult.success) {
      addIssue('Tests', 'Test failures detected', `${gradleCmd} test`);
    }
  }
}

// =============================================================================
// Node.js Checks
// =============================================================================

function checkNode() {
  verbose('Checking Node.js project...');

  // Detect package manager
  let pm = 'npm';
  if (fs.existsSync(path.join(ROOT_DIR, 'pnpm-lock.yaml'))) {
    pm = 'pnpm';
  } else if (fs.existsSync(path.join(ROOT_DIR, 'yarn.lock'))) {
    pm = 'yarn';
  }

  // Read package.json for available scripts
  let packageJson;
  try {
    packageJson = JSON.parse(fs.readFileSync(path.join(ROOT_DIR, 'package.json'), 'utf-8'));
  } catch {
    return;
  }

  const scripts = packageJson.scripts || {};

  // Linting
  if (scripts.lint) {
    const lintResult = runCommand(pm, ['run', 'lint', '--silent']);
    if (!lintResult.success) {
      const fixCmd = scripts['lint:fix'] ? `${pm} run lint:fix` : `${pm} run lint -- --fix`;
      addIssue('Linting', 'ESLint errors detected', fixCmd);
    }
  }

  // Type checking (TypeScript)
  if (fs.existsSync(path.join(ROOT_DIR, 'tsconfig.json'))) {
    if (scripts.typecheck) {
      const tscResult = runCommand(pm, ['run', 'typecheck', '--silent']);
      if (!tscResult.success) {
        addIssue('TypeScript', 'Type errors detected', `${pm} run typecheck`);
      }
    } else if (commandExists('npx')) {
      const tscResult = runCommand('npx', ['tsc', '--noEmit']);
      if (!tscResult.success) {
        addIssue('TypeScript', 'Type errors detected', 'npx tsc --noEmit');
      }
    }
  }

  // Tests (pre-push only)
  if (HOOK_TYPE === 'pre-push' && scripts.test) {
    const testResult = runCommand(pm, ['test']);
    if (!testResult.success) {
      addIssue('Tests', 'Test failures detected', `${pm} test`);
    }
  }
}

// =============================================================================
// Python Checks
// =============================================================================

function checkPython() {
  verbose('Checking Python project...');

  // Check for common linters
  if (commandExists('ruff')) {
    const ruffResult = runCommand('ruff', ['check', '.']);
    if (!ruffResult.success) {
      addIssue('Linting', 'Ruff linting issues', 'ruff check . --fix');
    }
  } else if (commandExists('flake8')) {
    const flake8Result = runCommand('flake8', ['.']);
    if (!flake8Result.success) {
      addIssue('Linting', 'Flake8 linting issues', 'flake8 .');
    }
  }

  // Check formatting
  if (commandExists('black')) {
    const blackResult = runCommand('black', ['--check', '.']);
    if (!blackResult.success) {
      addIssue('Formatting', 'Black formatting issues', 'black .');
    }
  }

  // Tests (pre-push only)
  if (HOOK_TYPE === 'pre-push' && commandExists('pytest')) {
    const pytestResult = runCommand('pytest', ['-q']);
    if (!pytestResult.success) {
      addIssue('Tests', 'Pytest failures', 'pytest');
    }
  }
}

// =============================================================================
// Go Checks
// =============================================================================

function checkGo() {
  verbose('Checking Go project...');

  if (!commandExists('go')) {
    return;
  }

  // Format check - use find to get all .go files recursively
  const fmtResult = runCommand('sh', ['-c', 'find . -name "*.go" -not -path "./vendor/*" | xargs gofmt -l 2>/dev/null']);
  if (fmtResult.stdout.trim()) {
    addIssue('Formatting', 'Go formatting issues', 'gofmt -w $(find . -name "*.go" -not -path "./vendor/*")');
  }

  // Vet
  const vetResult = runCommand('go', ['vet', './...']);
  if (!vetResult.success) {
    addIssue('Linting', 'Go vet issues', 'go vet ./...');
  }

  // Build check
  const buildResult = runCommand('go', ['build', './...']);
  if (!buildResult.success) {
    addIssue('Build', 'Go build errors', 'go build ./...');
  }

  // Tests (pre-push only)
  if (HOOK_TYPE === 'pre-push') {
    const testResult = runCommand('go', ['test', './...']);
    if (!testResult.success) {
      addIssue('Tests', 'Go test failures', 'go test ./...');
    }
  }
}

// =============================================================================
// Output
// =============================================================================

function printReport() {
  if (issues.length === 0) {
    if (!QUIET) {
      log(`${colors.green}✓${colors.reset} ${colors.dim}${HOOK_TYPE}: All checks passed${colors.reset}`);
    }
    return;
  }

  log('');
  log('============================================================');
  log(`${colors.yellow}  Bitso Quality Checks (Informative)${colors.reset}`);
  log('============================================================');
  log('');
  log(`  ${HOOK_TYPE} checks found some issues:`);
  log('');

  for (const issue of issues) {
    log(`  ${colors.yellow}[!]${colors.reset} ${colors.bold}${issue.category}:${colors.reset} ${issue.message}`);
    if (issue.fix) {
      log(`      ${colors.dim}Run: ${issue.fix}${colors.reset}`);
    }
    log('');
  }

  log(`  ${colors.green}These are recommendations. Your commit will proceed.${colors.reset}`);
  log(`  ${colors.dim}For AI agents: Please address these issues before completing.${colors.reset}`);
  log('');
  log('============================================================');
  log('');
}

// =============================================================================
// Documentation Check (pre-push only)
// =============================================================================

function checkDocumentation() {
  verbose('Checking documentation updates...');

  // Get the base branch (main or master)
  let baseBranch = 'main';
  try {
    execSync('git rev-parse --verify main', { encoding: 'utf-8', stdio: 'pipe' });
  } catch {
    try {
      execSync('git rev-parse --verify master', { encoding: 'utf-8', stdio: 'pipe' });
      baseBranch = 'master';
    } catch {
      verbose('Could not detect base branch, skipping doc check');
      return;
    }
  }

  // Get changed files compared to base branch
  let changedFiles;
  try {
    const mergeBase = execSync(`git merge-base ${baseBranch} HEAD`, {
      encoding: 'utf-8',
      stdio: 'pipe'
    }).trim();

    const output = execSync(`git diff --name-only ${mergeBase} HEAD`, {
      encoding: 'utf-8',
      stdio: 'pipe'
    });

    changedFiles = output.split('\n').filter(Boolean);
  } catch {
    verbose('Could not get changed files for doc check');
    return;
  }

  if (changedFiles.length === 0) {
    return;
  }

  // Significant files that should trigger doc check
  const significantPatterns = [
    /^\.scripts\/ci-.*\.js$/,
    /^\.scripts\/convert-rules\.js$/,
    /^\.scripts\/targeting\.js$/,
    /^\.scripts\/safe-sync\.js$/,
    /^\.scripts\/managed-paths\.js$/,
    /^technology-hierarchy\.json$/,
    /^managed-paths\.json$/,
    /^global\/skills\/[^/]+\/SKILL\.md$/,
    /^global\/skills\/[^/]+\/assets\//,
    /^\.github\/workflows\/ci\.yaml$/,
  ];

  // Documentation patterns
  const docPatterns = [
    /^docs\//,
    /^README\.md$/,
    /^CONTRIBUTING\.md$/,
    /^global\/skills\/[^/]+\/SKILL\.md$/,
  ];

  // Check for significant changes
  const significantChanges = changedFiles.filter(file =>
    significantPatterns.some(p => p.test(file))
  );

  if (significantChanges.length === 0) {
    return;
  }

  // Check if docs were updated
  const docChanges = changedFiles.filter(file =>
    docPatterns.some(p => p.test(file))
  );

  if (docChanges.length > 0) {
    verbose(`Documentation updated: ${docChanges.length} file(s)`);
    return;
  }

  // Significant changes without doc updates
  addIssue(
    'Documentation',
    `${significantChanges.length} significant file(s) changed without documentation updates`,
    'Consider updating docs/ai-ide-management/ or README.md'
  );
}

// =============================================================================
// Main
// =============================================================================

function main() {
  verbose(`Running ${HOOK_TYPE} checks in ${ROOT_DIR}`);

  const projectTypes = detectProjectType();
  verbose(`Detected project types: ${projectTypes.join(', ') || 'none'}`);

  if (projectTypes.length === 0) {
    verbose('No recognized project type, skipping checks');
    // Still run doc check for pre-push
    if (HOOK_TYPE === 'pre-push') {
      checkDocumentation();
      printReport();
    }
    return;
  }

  // Run checks based on project type
  for (const type of projectTypes) {
    switch (type) {
      case 'gradle':
        checkGradle();
        break;
      case 'node':
        checkNode();
        break;
      case 'python':
        checkPython();
        break;
      case 'go':
        checkGo();
        break;
    }
  }

  // Documentation check for pre-push
  if (HOOK_TYPE === 'pre-push') {
    checkDocumentation();
  }

  printReport();
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/git-hooks/assets/hooks-checks.js
// To modify, edit the source file and run the distribution workflow

