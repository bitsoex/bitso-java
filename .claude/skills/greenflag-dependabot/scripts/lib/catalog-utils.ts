/**
 * Catalog utilities for version history parsing and lookup
 */

import { execFileSync, execSync } from "child_process";
import { readFileSync, existsSync, readdirSync, mkdirSync, rmSync } from "fs";
import { join } from "path";
import { tmpdir } from "os";

const ORG = "bitsoex";
const CATALOGS_REPO = "ai-code-instructions";
const CATALOGS_SUBDIR = "bitso-gradle-catalogs/repos";

/**
 * Find the repository root directory
 */
function findRepoRoot(): string {
  try {
    return execSync("git rev-parse --show-toplevel", { encoding: "utf-8" }).trim();
  } catch {
    return process.cwd();
  }
}

/**
 * Get path for cloned catalogs in temp directory
 */
function getClonedCatalogsPath(): string {
  return join(tmpdir(), "greenflag-dependabot-catalogs");
}

/**
 * Ensure bitso-gradle-catalogs is available
 * First checks local repo, then clones if needed
 */
export function ensureCatalogsAvailable(): string {
  // First, check if catalogs exist in current repo (e.g., ai-code-instructions)
  const localPath = join(findRepoRoot(), CATALOGS_SUBDIR);
  if (existsSync(localPath)) {
    return localPath;
  }

  // Not in local repo, check/clone to temp directory
  const clonePath = getClonedCatalogsPath();
  const catalogsPath = join(clonePath, CATALOGS_SUBDIR);

  if (existsSync(catalogsPath)) {
    // Already cloned, fetch updates
    try {
      execFileSync("git", ["fetch", "--quiet"], {
        cwd: clonePath,
        encoding: "utf-8",
        stdio: ["pipe", "pipe", "pipe"],
        timeout: 30000,
      });
      execFileSync("git", ["reset", "--hard", "--quiet", "origin/main"], {
        cwd: clonePath,
        encoding: "utf-8",
        stdio: ["pipe", "pipe", "pipe"],
        timeout: 10000,
      });
    } catch {
      // Ignore fetch errors, use cached version
    }
    return catalogsPath;
  }

  // If a previous clone failed, clear the temp dir
  if (existsSync(clonePath) && !existsSync(catalogsPath)) {
    rmSync(clonePath, { recursive: true, force: true });
  }

  // Clone the repository
  console.error("📥 Cloning bitso-gradle-catalogs...");
  mkdirSync(clonePath, { recursive: true });

  try {
    execFileSync(
      "git",
      [
        "clone",
        "--quiet",
        "--depth=1",
        "--filter=blob:none",
        "--sparse",
        `https://github.com/${ORG}/${CATALOGS_REPO}.git`,
        clonePath,
      ],
      { encoding: "utf-8", timeout: 60000, stdio: ["pipe", "pipe", "pipe"] }
    );

    // Sparse checkout only the catalogs directory
    execFileSync("git", ["sparse-checkout", "set", CATALOGS_SUBDIR], {
      cwd: clonePath,
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
      timeout: 30000,
    });

    console.error("✅ Catalogs cloned successfully");
  } catch (e) {
    console.error(`❌ Failed to clone catalogs: ${e}`);
    throw new Error("Failed to clone bitso-gradle-catalogs", { cause: e });
  }

  return catalogsPath;
}

/**
 * Get the catalogs directory path (with auto-clone if needed)
 */
function getCatalogsDir(): string {
  return ensureCatalogsAvailable();
}

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface VersionHistoryEntry {
  version: string;
  hash: string;
  date: string;
}

export interface LibraryInfo {
  repo: string;
  history: VersionHistoryEntry[];
  squad?: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Version History Parsing
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse version history from catalog file comment
 * Format: # history: 3.1.0 (a3998bb, 2025-11-06), 3.0.0 (c85e9df, 2025-10-21)
 */
export function parseVersionHistory(historyLine: string): VersionHistoryEntry[] {
  const entries: VersionHistoryEntry[] = [];
  const regex = /(\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?)\s*\(([a-f0-9]+),\s*(\d{4}-\d{2}-\d{2})\)/g;

  let match;
  while ((match = regex.exec(historyLine)) !== null) {
    if (match[1] && match[2] && match[3]) {
      entries.push({
        version: match[1],
        hash: match[2],
        date: match[3],
      });
    }
  }

  return entries;
}

/**
 * Normalize library name for catalog lookup.
 * Handles various formats:
 *   - Maven: com.bitso:users-client or com.bitso.users:client
 *   - Gradle catalog key: com-bitso-users-client
 *   - Simple: users-client
 *
 * Returns array of {keyPattern, groupPattern, namePattern} for matching
 */
interface LibraryPattern {
  // Pattern to match catalog key (e.g., com-bitso-users-client)
  keyPattern: string;
  // Pattern to match group attribute (e.g., com.bitso.users or com.bitso)
  groupPattern?: string;
  // Pattern to match name attribute (e.g., client or users-client)
  namePattern?: string;
}

function getLibraryPatterns(libraryName: string): LibraryPattern[] {
  const patterns: LibraryPattern[] = [];

  // Always add the original as a key pattern
  patterns.push({ keyPattern: libraryName });

  // Convert Maven coordinates to catalog key format: com.bitso:users-client -> com-bitso-users-client
  const mavenNormalized = libraryName.replace(/[.:]/g, "-");
  if (mavenNormalized !== libraryName) {
    patterns.push({ keyPattern: mavenNormalized });
  }

  // If it's group:artifact format (Maven coordinates)
  if (libraryName.includes(":")) {
    const [group, artifact] = libraryName.split(":");
    if (group && artifact) {
      // Add pattern that requires BOTH group and name to match on same line
      patterns.push({
        keyPattern: `${group.replace(/\./g, "-")}-${artifact}`,
        groupPattern: group,
        namePattern: artifact,
      });
    }
  }

  return patterns;
}

/**
 * Check if a line matches a library pattern
 */
function lineMatchesPattern(line: string, pattern: LibraryPattern): boolean {
  // If we have group and name patterns, require BOTH to match on the same line
  // Don't fall back to keyPattern to avoid false positives
  if (pattern.groupPattern && pattern.namePattern) {
    const hasGroup = line.includes(`group = "${pattern.groupPattern}"`);
    const hasName = line.includes(`name = "${pattern.namePattern}"`);
    return hasGroup && hasName;
  }

  // Otherwise just check the key pattern
  return line.includes(pattern.keyPattern);
}

/**
 * Check if a catalog file contains a library (using normalized search)
 */
function catalogContainsLibrary(content: string, libraryName: string): boolean {
  const patterns = getLibraryPatterns(libraryName);
  const lines = content.split("\n");

  for (const line of lines) {
    for (const pattern of patterns) {
      if (lineMatchesPattern(line, pattern)) {
        return true;
      }
    }
  }

  return false;
}

/**
 * Find the library line in catalog content
 */
function findLibraryLine(lines: string[], libraryName: string): number {
  const patterns = getLibraryPatterns(libraryName);

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (!line) {
      continue;
    }

    for (const pattern of patterns) {
      if (lineMatchesPattern(line, pattern)) {
        return i;
      }
    }
  }

  return -1;
}

/**
 * Find library info from catalogs
 */
export function findLibraryInCatalogs(libraryName: string): LibraryInfo | null {
  const catalogsDir = getCatalogsDir();
  if (!existsSync(catalogsDir)) {
    console.error(`Catalogs directory not found: ${catalogsDir}`);
    return null;
  }

  try {
    // Search for library in all catalog files using safe file operations
    let catalogFile: string | null = null;
    const files = readdirSync(catalogsDir).filter((f) => f.endsWith(".versions.toml"));

    for (const file of files) {
      const filePath = join(catalogsDir, file);
      const content = readFileSync(filePath, "utf-8");
      if (catalogContainsLibrary(content, libraryName)) {
        catalogFile = filePath;
        break;
      }
    }

    if (!catalogFile) {
      return null;
    }

    const content = readFileSync(catalogFile, "utf-8");
    const lines = content.split("\n");

    // Extract repo from metadata
    const repoMatch = content.match(/repository\s*=\s*"([^"]+)"/);
    const repo = repoMatch?.[1]?.replace(`${ORG}/`, "") ?? "";
    if (!repo) {
      console.error("Catalog metadata missing repository");
      return null;
    }

    // Extract squad from metadata
    const squadMatch = content.match(/squad\s*=\s*"([^"]+)"/);
    const squad = squadMatch?.[1];

    // Find library entry and its history
    const lineIndex = findLibraryLine(lines, libraryName);
    if (lineIndex >= 0) {
      // Check next line for history comment
      const historyLine = lines[lineIndex + 1];
      if (historyLine?.startsWith("# history:")) {
        return {
          repo,
          history: parseVersionHistory(historyLine),
          squad,
        };
      }
    }

    return { repo, history: [], squad };
  } catch {
    return null;
  }
}

/**
 * Get commit hash for a specific version from history
 */
export function getHashForVersion(
  history: VersionHistoryEntry[],
  version: string
): string | null {
  const entry = history.find((h) => h.version === version);
  return entry?.hash ?? null;
}

/**
 * Find all repos owned by a squad from catalog files
 */
export function findReposBySquad(squadName: string): string[] {
  // Validate squad name
  if (!/^[\w-]+$/.test(squadName)) {
    console.error("Invalid squad name format");
    return [];
  }

  const repos: string[] = [];

  try {
    const catalogsDir = getCatalogsDir();
    if (!existsSync(catalogsDir)) {
      return repos;
    }

    const files = readdirSync(catalogsDir).filter((f) => f.endsWith(".versions.toml"));

    for (const file of files) {
      const filePath = join(catalogsDir, file);
      const content = readFileSync(filePath, "utf-8");

      // Check if this catalog belongs to the squad
      const squadMatch = content.match(/squad\s*=\s*"([^"]+)"/);
      if (squadMatch?.[1] === squadName) {
        // Extract repo name from filename (e.g., "accounts-management.versions.toml" -> "accounts-management")
        const repoName = file.replace(".versions.toml", "");
        if (!repos.includes(repoName)) {
          repos.push(repoName);
        }
      }
    }
  } catch {
    // Ignore errors
  }

  return repos;
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/scripts/lib/catalog-utils.ts
// To modify, edit the source file and run the distribution workflow

