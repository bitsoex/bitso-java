#!/usr/bin/env node
/**
 * Generate Changelog for Dependabot PRs
 *
 * This script generates a changelog comment for Dependabot PRs by:
 * 1. Parsing the PR to identify library being updated and version range
 * 2. Finding version history from bitso-gradle-catalogs
 * 3. Fetching commits between versions from the source repository
 * 4. Filtering to relevant changes (lib folder, gradle folder)
 * 5. Generating a formatted changelog comment
 *
 * Usage:
 *   mise run greenflag-dependabot:generate-changelog -- --pr <number>
 *   mise run greenflag-dependabot:generate-changelog -- --pr <number> --post-comment
 *   mise run greenflag-dependabot:generate-changelog -- --lib <library> --from <version> --to <version>
 */

import { execFileSync } from "child_process";

import { findLibraryInCatalogs, getHashForVersion } from "./lib/catalog-utils.js";
import {
  fetchCommitsBetween,
  getFilesChangedBetween,
  findRelevantFiles,
  filterCommitsByFiles,
  type CommitInfo,
} from "./lib/github-api.js";

// Reuse constants from changelog-rfc-29 skill
const EMOJI_REGEX = new RegExp(
  "[\\u{1F300}-\\u{1F9FF}]|[\\u{2600}-\\u{26FF}]|[\\u{2700}-\\u{27BF}]|[\\u{1F600}-\\u{1F64F}]|[\\u{1F680}-\\u{1F6FF}]|[\\u{1F1E0}-\\u{1F1FF}]|[\\u{FE00}-\\u{FE0F}]|[\\u{200D}]|[\\u{2B50}]|[\\u{2705}]|[\\u{2728}]|[\\u{2764}]|[\\u{1FA00}-\\u{1FAFF}]",
  "gu"
);
const TICKET_REGEX = /\[?[A-Z]{2,}-\d+]?[:\s-]*/gi;
const CONVENTIONAL_COMMIT_REGEX = /^(\w+)(?:\([^)]*\))?(!)?:\s*(.+)$/;

const ORG = "bitsoex";

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

interface ChangelogResult {
  library: string;
  repo: string;
  fromVersion: string;
  toVersion: string;
  fromHash: string;
  toHash: string;
  commits: CommitInfo[];
  relevantCommits: CommitInfo[];
  relevantFiles: string[];
  markdown: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════════════════════

function removeEmojis(text: string): string {
  return text.replace(EMOJI_REGEX, "").trim();
}

function capitalizeFirst(str: string): string {
  if (!str) {
    return str;
  }
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function cleanCommitMessage(text: string): string {
  let cleaned = removeEmojis(text);
  cleaned = cleaned.replace(TICKET_REGEX, "");
  cleaned = cleaned.replace(/\s*\(#\d+\)/g, "");

  const match = cleaned.match(CONVENTIONAL_COMMIT_REGEX);
  if (match?.[3]) {
    cleaned = match[3];
  }

  cleaned = cleaned.replace(/^[\s\-:[\]()]+|[\s\-:[\]()]+$/g, "");
  cleaned = cleaned.replace(/\s+/g, " ").trim();

  return cleaned.length > 0 ? capitalizeFirst(cleaned) : text.trim();
}

// ═══════════════════════════════════════════════════════════════════════════
// Markdown Generation
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Generate markdown changelog for the version update
 */
function generateMarkdown(result: ChangelogResult): string {
  const lines: string[] = [];

  lines.push(`## 📦 Changelog: ${result.library}`);
  lines.push("");
  lines.push(`**Repository:** [${result.repo}](https://github.com/${ORG}/${result.repo})`);
  lines.push(`**Version:** ${result.fromVersion} → ${result.toVersion}`);
  lines.push(
    `**Compare:** [View diff](https://github.com/${ORG}/${result.repo}/compare/${result.fromHash}...${result.toHash})`
  );
  lines.push("");

  // Show relevant files changed
  if (result.relevantFiles.length > 0) {
    lines.push(`### 📁 Relevant Files Changed (${result.relevantFiles.length})`);
    lines.push("");
    const filesToShow = result.relevantFiles.slice(0, 15);
    for (const file of filesToShow) {
      lines.push(`- \`${file}\``);
    }
    if (result.relevantFiles.length > 15) {
      lines.push(`- _...and ${result.relevantFiles.length - 15} more files_`);
    }
    lines.push("");
  }

  if (result.relevantCommits.length === 0) {
    lines.push("_No relevant commits found for this library folder._");
  } else {
    lines.push(`### Changes (${result.relevantCommits.length} commits)`);
    lines.push("");

    for (const commit of result.relevantCommits) {
      const cleanMsg = cleanCommitMessage(commit.message);
      lines.push(
        `- [\`${commit.hash}\`](https://github.com/${ORG}/${result.repo}/commit/${commit.hash}) ${cleanMsg}`
      );
    }
  }

  lines.push("");
  lines.push("---");
  lines.push(
    "_Generated by greenflag-dependabot skill using version history from bitso-gradle-catalogs._"
  );

  return lines.join("\n");
}

// ═══════════════════════════════════════════════════════════════════════════
// PR Analysis
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Extract library and version info from Dependabot PR
 */
function parseDependabotPR(
  prNumber: number
): { library: string; fromVersion: string; toVersion: string } | null {
  try {
    const prInfo = execFileSync(
      "gh",
      ["pr", "view", String(prNumber), "--json", "title,body", "--jq", "{title: .title, body: .body}"],
      { encoding: "utf-8", timeout: 30000, stdio: ["pipe", "pipe", "pipe"] }
    );

    const { title, body } = JSON.parse(prInfo) as { title: string; body: string };

    // Parse title: "Bump library-name from X.Y.Z to A.B.C in the group-name group"
    const titleMatch = title.match(/Bump\s+([^\s]+)\s+from\s+([^\s]+)\s+to\s+([^\s]+)/i);
    if (titleMatch?.[1] && titleMatch[2] && titleMatch[3]) {
      return {
        library: titleMatch[1],
        fromVersion: titleMatch[2],
        toVersion: titleMatch[3],
      };
    }

    // Try body for more detailed info
    const bodyMatch = body?.match(
      /Updates?\s+`([^`]+)`\s+from\s+([^\s]+)\s+to\s+([^\s]+)/i
    );
    if (bodyMatch?.[1] && bodyMatch[2] && bodyMatch[3]) {
      return {
        library: bodyMatch[1],
        fromVersion: bodyMatch[2],
        toVersion: bodyMatch[3],
      };
    }

    return null;
  } catch {
    return null;
  }
}

// ═══════════════════════════════════════════════════════════════════════════
// Main
// ═══════════════════════════════════════════════════════════════════════════

interface Args {
  pr?: number;
  lib?: string;
  from?: string;
  to?: string;
  postComment: boolean;
}

function parseArgs(): Args {
  const args = process.argv.slice(2);
  const result: Args = { postComment: false };

  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === "--pr" && args[i + 1]) {
      result.pr = parseInt(args[++i] ?? "0", 10);
    } else if (arg === "--lib" && args[i + 1]) {
      result.lib = args[++i];
    } else if (arg === "--from" && args[i + 1]) {
      result.from = args[++i];
    } else if (arg === "--to" && args[i + 1]) {
      result.to = args[++i];
    } else if (arg === "--post-comment") {
      result.postComment = true;
    }
  }

  return result;
}

async function main() {
  const args = parseArgs();

  console.error("🔍 Generating changelog for Dependabot PR...\n");

  let library: string;
  let fromVersion: string;
  let toVersion: string;

  if (args.pr) {
    console.error(`📋 Analyzing PR #${args.pr}...`);
    const prInfo = parseDependabotPR(args.pr);
    if (!prInfo) {
      console.error("❌ Could not parse library info from PR");
      process.exit(1);
    }
    ({ library, fromVersion, toVersion } = prInfo);
  } else if (args.lib && args.from && args.to) {
    library = args.lib;
    fromVersion = args.from;
    toVersion = args.to;
  } else {
    console.error("Usage:");
    console.error("  mise run greenflag-dependabot:generate-changelog -- --pr <number>");
    console.error(
      "  mise run greenflag-dependabot:generate-changelog -- --lib <library> --from <version> --to <version>"
    );
    process.exit(1);
  }

  console.error(`📦 Library: ${library}`);
  console.error(`📈 Version: ${fromVersion} → ${toVersion}`);

  // Find library in catalogs
  const catalogInfo = findLibraryInCatalogs(library);
  if (!catalogInfo) {
    console.error(`❌ Library not found in bitso-gradle-catalogs`);
    process.exit(1);
  }

  console.error(`📂 Repository: ${catalogInfo.repo}`);
  if (catalogInfo.squad) {
    console.error(`👥 Squad: ${catalogInfo.squad}`);
  }

  // Get commit hashes for versions
  let fromHash = getHashForVersion(catalogInfo.history, fromVersion);
  let toHash = getHashForVersion(catalogInfo.history, toVersion);

  if (!fromHash) {
    console.error(`⚠️  No hash found for version ${fromVersion}, using version tag`);
    fromHash = `v${fromVersion}`;
  }
  if (!toHash) {
    console.error(`⚠️  No hash found for version ${toVersion}, using version tag`);
    toHash = `v${toVersion}`;
  }

  console.error(`🔗 Comparing: ${fromHash}...${toHash}`);

  // Fetch commits between versions
  const commits = fetchCommitsBetween(catalogInfo.repo, fromHash, toHash);
  console.error(`📝 Found ${commits.length} commits`);

  // Get files changed in the range
  const changedFiles = getFilesChangedBetween(catalogInfo.repo, fromHash, toHash);
  console.error(`📄 ${changedFiles.length} files changed`);

  // Find relevant files (gradle/, bitso-libs/{library}/, etc.)
  const relevantFiles = findRelevantFiles(changedFiles, library);
  console.error(`📁 ${relevantFiles.length} relevant files for library`);

  // Filter commits by files changed (using local git for fast lookups)
  console.error(`🔍 Filtering commits by files changed...`);
  const relevantCommits = filterCommitsByFiles(commits, library, catalogInfo.repo, fromHash, toHash);
  console.error(`✨ ${relevantCommits.length} relevant commits for library`);

  // Generate markdown
  const result: ChangelogResult = {
    library,
    repo: catalogInfo.repo,
    fromVersion,
    toVersion,
    fromHash,
    toHash,
    commits,
    relevantCommits,
    relevantFiles,
    markdown: "",
  };
  result.markdown = generateMarkdown(result);

  console.error("");

  // Output or post comment
  if (args.postComment && args.pr) {
    console.error(`💬 Posting comment to PR #${args.pr}...`);
    try {
      execFileSync("gh", ["pr", "comment", String(args.pr), "--body-file", "-"], {
        input: result.markdown,
        encoding: "utf-8",
        stdio: ["pipe", "pipe", "pipe"],
      });
      console.error("✅ Comment posted!");
    } catch (e) {
      console.error(`❌ Failed to post comment: ${e}`);
      console.log(result.markdown);
    }
  } else {
    console.log(result.markdown);
  }
}

main().catch(console.error);
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/scripts/generate-changelog.ts
// To modify, edit the source file and run the distribution workflow

