#!/usr/bin/env node
/**
 * check-naming-conventions.ts
 * Validates naming conventions across the codebase
 *
 * Usage: node check-naming-conventions.js [directory]
 * Default directory: current directory (.)
 */

import fs from 'fs';
import path from 'path';

const searchDir = process.argv[2] ?? '.';
let errors = 0;
let filesChecked = 0;

console.log(`Checking naming conventions in ${searchDir}...`);
console.log('');

/**
 * Recursively find files matching a pattern.
 */
function findFiles(dir: string, extension: string): string[] {
  const results: string[] = [];
  
  try {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    
    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      
      // Skip node_modules, .git, and other common excludes
      if (entry.isDirectory()) {
        if (!['node_modules', '.git', 'output', 'coverage', '.stryker-tmp'].includes(entry.name)) {
          results.push(...findFiles(fullPath, extension));
        }
      } else if (entry.name.endsWith(extension)) {
        results.push(fullPath);
      }
    }
  } catch {
    // Directory not readable, skip
  }
  
  return results;
}

/**
 * Check for PascalCase class names in Java files.
 */
function checkJavaClasses(file: string): void {
  const basename = path.basename(file, '.java');
  
  // Class name should match filename and be PascalCase
  if (!/^[A-Z][a-zA-Z0-9]*$/.test(basename)) {
    console.log(`ERROR: ${file} - Class name '${basename}' should be PascalCase`);
    errors++;
  }
}

/**
 * Check for snake_case in Python files.
 */
function checkPythonModules(file: string): void {
  const basename = path.basename(file, '.py');
  
  // Module name should be snake_case (optionally with leading underscore for private)
  // Allow dunder modules like __init__, __main__, __version__
  const isDunderModule = /^__[a-z][a-z0-9_]*__$/.test(basename);
  const isSnakeCase = /^_?[a-z][a-z0-9_]*$/.test(basename);
  
  if (!isSnakeCase && !isDunderModule) {
    console.log(`ERROR: ${file} - Module name '${basename}' should be snake_case`);
    errors++;
  }
}

/**
 * Check for kebab-case in shell scripts.
 * Note: Shell script violations are advisory warnings only and do not affect
 * the exit code. This is intentional - shell scripts are being phased out.
 */
function checkShellScripts(file: string): void {
  const basename = path.basename(file, '.sh');
  
  // Script name should be kebab-case (advisory warning only)
  if (!/^[a-z][a-z0-9-]*$/.test(basename)) {
    console.log(`WARNING: ${file} - Script name '${basename}' should be kebab-case`);
  }
}

// Find and check Java files
const javaFiles = findFiles(searchDir, '.java');
for (const file of javaFiles) {
  checkJavaClasses(file);
  filesChecked++;
}

// Find and check Python files
const pyFiles = findFiles(searchDir, '.py');
for (const file of pyFiles) {
  checkPythonModules(file);
  filesChecked++;
}

// Find and check shell scripts
const shFiles = findFiles(searchDir, '.sh');
for (const file of shFiles) {
  checkShellScripts(file);
  filesChecked++;
}

console.log('');
console.log(`✓ ${filesChecked} files checked`);

if (errors === 0) {
  console.log('✓ All naming conventions followed');
  process.exit(0);
} else {
  console.log(`✗ ${errors} naming convention violations found`);
  process.exit(1);
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coding-standards/scripts/check-naming-conventions.ts
// To modify, edit the source file and run the distribution workflow

