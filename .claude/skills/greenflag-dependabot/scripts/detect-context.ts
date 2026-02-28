#!/usr/bin/env node
/**
 * Detect context for Greenflag Dependabot PR filtering
 *
 * This script detects:
 * 1. Current workspace repo (if in a Bitso repo)
 * 2. Current user's squad (from git email + estate-catalog)
 * 3. Repos owned by the user's squad
 *
 * Usage:
 *   mise run greenflag-dependabot:detect-context
 *   mise run greenflag-dependabot:detect-context -- --json
 */

import { execSync } from "child_process";

import { findReposBySquad } from "./lib/catalog-utils.js";

const ORG = "bitsoex";

interface Context {
  workspaceRepo: string | null;
  userEmail: string | null;
  userSquad: string | null;
  squadRepos: string[];
  suggestedFilter: string;
}

// Get current repo from git remote
function getWorkspaceRepo(): string | null {
  try {
    const remote = execSync("git remote get-url origin", {
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
    }).trim();

    // Parse repo name from remote URL
    // Formats: git@github.com:bitsoex/repo.git, https://github.com/bitsoex/repo.git
    const match = remote.match(/github\.com[:/]([^/]+)\/([^/.]+)/);
    if (match && match[1] && match[2] && match[1].toLowerCase() === ORG.toLowerCase()) {
      return match[2];
    }
    return null;
  } catch {
    return null;
  }
}

// Get user email from git config
function getUserEmail(): string | null {
  try {
    return execSync("git config user.email", {
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
    }).trim();
  } catch {
    return null;
  }
}

// Get user's squad from estate-catalog via GitHub API
function getUserSquad(email: string): string | null {
  try {
    // Use GitHub code search to find the file containing the email
    const searchResult = execSync(
      `gh api "search/code?q=${encodeURIComponent(`"${email}"`)}+repo:${ORG}/estate-catalog+path:catalog/people" --jq '.items[0].path // empty'`,
      { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 30000 }
    ).trim();

    if (!searchResult) {
      // Fallback: derive filename from email (first.last -> first-last-*.cue pattern)
      const emailName = email.split("@")[0] ?? ""; // e.g., "daniel.figueiredo"
      const nameParts = emailName.split(".").join("-"); // e.g., "daniel-figueiredo"

      // Try to find matching file
      const peopleList = execSync(
        `gh api repos/${ORG}/estate-catalog/contents/catalog/people --jq '.[].name'`,
        { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 30000 }
      );

      const matchingFile = peopleList
        .split("\n")
        .filter(Boolean)
        .find((f) => f.includes(nameParts) && f.endsWith(".cue"));

      if (matchingFile) {
        const content = execSync(
          `gh api repos/${ORG}/estate-catalog/contents/catalog/people/${matchingFile} --jq '.content' | base64 -d`,
          { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 10000 }
        );

        const teamMatch = content.match(/team:\s*"([^"]+)"/);
        if (teamMatch?.[1]) {
          return teamMatch[1];
        }
      }
      return null;
    }

    // Fetch the found file
    const content = execSync(
      `gh api repos/${ORG}/estate-catalog/contents/${searchResult} --jq '.content' | base64 -d`,
      { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 10000 }
    );

    const teamMatch = content.match(/team:\s*"([^"]+)"/);
    if (teamMatch?.[1]) {
      return teamMatch[1];
    }

    return null;
  } catch {
    return null;
  }
}

// Get repos owned by a squad from estate-catalog
function getSquadRepos(squad: string): string[] {
  const repos: string[] = [];

  try {
    // First, get ownRepos from squad definition
    const squadContent = execSync(
      `gh api repos/${ORG}/estate-catalog/contents/catalog/squads/${squad}.cue --jq '.content' | base64 -d`,
      { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 10000 }
    );

    const ownReposMatch = squadContent.match(/ownRepos:\s*\[([^\]]+)\]/);
    if (ownReposMatch?.[1]) {
      const reposList = ownReposMatch[1];
      // Extract all quoted strings from the repos list
      const repoPathMatches = reposList.match(/"([^"]+)"/g) ?? [];
      for (const quotedPath of repoPathMatches) {
        // Remove quotes and extract repo name
        const repoPath = quotedPath.replace(/"/g, "");
        if (repoPath.startsWith(`${ORG}/`)) {
          repos.push(repoPath.replace(`${ORG}/`, ""));
        } else {
          repos.push(repoPath);
        }
      }
    }
  } catch {
    // Squad file not found
  }

  // Also check bitso-gradle-catalogs for repos with this squad (auto-clones if needed)
  const catalogRepos = findReposBySquad(squad);
  for (const repo of catalogRepos) {
    if (!repos.includes(repo)) {
      repos.push(repo);
    }
  }

  return repos;
}

// Generate suggested gh pr list filter
function generateFilter(context: Context): string {
  const baseCmd = "gh pr list --author app/dependabot --state open";

  if (context.workspaceRepo) {
    // In a specific repo - filter to this repo only
    return `${baseCmd} --repo ${ORG}/${context.workspaceRepo}`;
  }

  if (context.userSquad) {
    // Not in a repo, but know user's squad - filter by squad group in title
    return `${baseCmd} --search "in:title ${context.userSquad}"`;
  }

  // Fallback to listing all
  return baseCmd;
}

function main() {
  const jsonOutput = process.argv.includes("--json");

  console.error("🔍 Detecting Greenflag context...\n");

  const workspaceRepo = getWorkspaceRepo();
  const userEmail = getUserEmail();

  let userSquad: string | null = null;
  let squadRepos: string[] = [];

  if (userEmail) {
    console.error(`📧 User email: ${userEmail}`);
    userSquad = getUserSquad(userEmail);

    if (userSquad) {
      console.error(`👥 User squad: ${userSquad}`);
      squadRepos = getSquadRepos(userSquad);
      if (squadRepos.length > 0) {
        console.error(`📦 Squad repos: ${squadRepos.join(", ")}`);
      }
    }
  }

  if (workspaceRepo) {
    console.error(`📂 Workspace repo: ${workspaceRepo}`);
  }

  const context: Context = {
    workspaceRepo,
    userEmail,
    userSquad,
    squadRepos,
    suggestedFilter: "",
  };

  context.suggestedFilter = generateFilter(context);

  console.error(`\n💡 Suggested filter:\n   ${context.suggestedFilter}\n`);

  if (jsonOutput) {
    console.log(JSON.stringify(context, null, 2));
  } else {
    // Output just the suggested command for easy copy-paste
    console.log(context.suggestedFilter);
  }
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/scripts/detect-context.ts
// To modify, edit the source file and run the distribution workflow

