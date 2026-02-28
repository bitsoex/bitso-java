/**
 * Git utilities for changelog generation
 * Uses local git commands after cloning for fast file-based filtering
 */

import { execFileSync } from "child_process";
import { existsSync, mkdirSync } from "fs";
import { join } from "path";
import { tmpdir } from "os";

const ORG = "bitsoex";

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface CommitInfo {
  hash: string;
  message: string;
  author: string;
  date: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Local Clone Management
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Get the path where repos are cloned for analysis
 */
function getCloneBasePath(): string {
  return join(tmpdir(), "greenflag-dependabot-clones");
}

/**
 * Clone or update a repository locally for analysis
 */
export function ensureRepoCloned(repo: string): string {
  const basePath = getCloneBasePath();
  if (!existsSync(basePath)) {
    mkdirSync(basePath, { recursive: true });
  }

  const repoPath = join(basePath, repo);

  if (existsSync(join(repoPath, ".git"))) {
    // Repo exists, fetch latest
    try {
      execFileSync("git", ["fetch", "--all", "--quiet"], {
        cwd: repoPath,
        encoding: "utf-8",
        stdio: ["pipe", "pipe", "pipe"],
      });
    } catch {
      // Ignore fetch errors, we'll work with what we have
    }
  } else {
    // Clone the repo (shallow clone with all branches)
    console.error(`📥 Cloning ${repo}...`);
    execFileSync(
      "git",
      ["clone", "--quiet", "--filter=blob:none", `https://github.com/${ORG}/${repo}.git`, repoPath],
      { encoding: "utf-8", timeout: 120000, stdio: ["pipe", "pipe", "pipe"] }
    );
  }

  return repoPath;
}

// ═══════════════════════════════════════════════════════════════════════════
// Git Log Commands
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Get commits between two hashes using local git
 */
export function fetchCommitsBetween(
  repo: string,
  fromHash: string,
  toHash: string
): CommitInfo[] {
  const repoPath = ensureRepoCloned(repo);

  try {
    // Use git log with format: hash|message|author|date
    const output = execFileSync(
      "git",
      ["log", "--pretty=format:%h|%s|%an|%as", `${fromHash}..${toHash}`],
      { cwd: repoPath, encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"] }
    );

    return output
      .split("\n")
      .filter(Boolean)
      .map((line) => {
        const [hash = "", message = "", author = "", date = ""] = line.split("|");
        return { hash, message, author, date };
      });
  } catch (e) {
    console.error(`Failed to get commits: ${e}`);
    return [];
  }
}

/**
 * Get files changed between two commits using local git
 */
export function getFilesChangedBetween(
  repo: string,
  fromHash: string,
  toHash: string
): string[] {
  const repoPath = ensureRepoCloned(repo);

  try {
    const output = execFileSync(
      "git",
      ["diff", "--name-only", `${fromHash}..${toHash}`],
      { cwd: repoPath, encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"] }
    );
    return output.split("\n").filter(Boolean);
  } catch {
    return [];
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// File Relevance
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Check if a file path is relevant to the library
 * Only matches actual code folders, not CI/CD or deployment config
 */
export function isRelevantFile(file: string, libraryName: string): boolean {
  const fileLower = file.toLowerCase();

  // Exclude CI/CD and deployment noise
  if (
    fileLower.startsWith(".github/") ||
    fileLower.startsWith("deployments/") ||
    fileLower.startsWith(".ci/") ||
    fileLower.includes("spinnaker")
  ) {
    return false;
  }

  // Check bitso-libs folder with library name
  if (fileLower.startsWith("bitso-libs/")) {
    // Extract the subfolder name from the path (e.g., "verification-idwall" from "bitso-libs/verification-idwall/...")
    const subfolderMatch = fileLower.match(/^bitso-libs\/([^/]+)/);
    const subfolder = subfolderMatch?.[1] ?? "";

    // Check if subfolder contains any part of the library name
    const libParts = libraryName.split("-").filter((p) => p.length > 2);
    for (const part of libParts) {
      if (subfolder.includes(part.toLowerCase())) {
        return true;
      }
    }
    return false; // In bitso-libs but not our library
  }

  // Check gradle folder (version catalogs, build config)
  if (fileLower.startsWith("gradle/")) {
    return true;
  }

  // Check gradle.properties (version bumps)
  if (fileLower === "gradle.properties") {
    return true;
  }

  // Check root build files
  if (fileLower === "build.gradle.kts" || fileLower === "build.gradle") {
    return true;
  }

  return false;
}

/**
 * Identify relevant files from the changed files list
 */
export function findRelevantFiles(changedFiles: string[], libraryName: string): string[] {
  return changedFiles.filter((file) => isRelevantFile(file, libraryName));
}

/**
 * Get commits with their files in a single git command (optimized)
 * Returns a map of commit hash -> files changed
 */
export function getCommitsWithFiles(
  repo: string,
  fromHash: string,
  toHash: string
): Map<string, string[]> {
  const repoPath = ensureRepoCloned(repo);
  const commitFiles = new Map<string, string[]>();

  try {
    // Get commits with files in one command using --name-only
    // Format: commit hash, then files, separated by blank lines
    const output = execFileSync(
      "git",
      ["log", "--pretty=format:COMMIT:%h", "--name-only", `${fromHash}..${toHash}`],
      { cwd: repoPath, encoding: "utf-8", maxBuffer: 10 * 1024 * 1024, stdio: ["pipe", "pipe", "pipe"] }
    );

    let currentHash = "";
    for (const line of output.split("\n")) {
      if (line.startsWith("COMMIT:")) {
        currentHash = line.replace("COMMIT:", "");
        commitFiles.set(currentHash, []);
      } else if (line.trim() && currentHash) {
        commitFiles.get(currentHash)?.push(line.trim());
      }
    }
  } catch (e) {
    console.error(`Failed to get commits with files: ${e}`);
  }

  return commitFiles;
}

/**
 * Filter commits by checking if they touch relevant files (gradle/, bitso-libs/{library}/)
 * Uses optimized single git command to get all files at once
 */
export function filterCommitsByFiles(
  commits: CommitInfo[],
  libraryName: string,
  repo: string,
  fromHash: string,
  toHash: string
): CommitInfo[] {
  // Get all commit files in one git command
  const commitFilesMap = getCommitsWithFiles(repo, fromHash, toHash);

  return commits.filter((commit) => {
    const files = commitFilesMap.get(commit.hash) ?? [];
    return files.some((file) => isRelevantFile(file, libraryName));
  });
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/scripts/lib/github-api.ts
// To modify, edit the source file and run the distribution workflow

