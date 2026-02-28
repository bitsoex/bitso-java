#!/usr/bin/env node
/**
 * List Dependabot PRs with context-aware filtering
 *
 * Automatically detects context and filters PRs accordingly:
 * - If in a Bitso repo: shows PRs for that repo only
 * - If user's squad is known: shows PRs for squad's repos
 * - Otherwise: shows all open Dependabot PRs
 *
 * Usage:
 *   mise run greenflag-dependabot:list-dependabot-prs
 *   mise run greenflag-dependabot:list-dependabot-prs -- --all
 *   mise run greenflag-dependabot:list-dependabot-prs -- --repo NAME
 *   mise run greenflag-dependabot:list-dependabot-prs -- --squad NAME
 *   mise run greenflag-dependabot:list-dependabot-prs -- --json
 */

import { execSync, spawnSync } from "child_process";

import { findReposBySquad } from "./lib/catalog-utils.js";

const ORG = "bitsoex";

interface PR {
  number: number;
  title: string;
  url: string;
  repo: string;
  createdAt: string;
  labels: string[];
}

interface Context {
  workspaceRepo: string | null;
  userSquad: string | null;
  squadRepos: string[];
}

// Detect context (same logic as detect-context.ts)
function detectContext(): Context {
  let workspaceRepo: string | null = null;
  let userSquad: string | null = null;
  const squadRepos: string[] = [];

  // Get workspace repo
  try {
    const remote = execSync("git remote get-url origin", {
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
    }).trim();

    const match = remote.match(/github\.com[:/]([^/]+)\/([^/.]+)/);
    if (match?.[1] && match[2] && match[1].toLowerCase() === ORG.toLowerCase()) {
      workspaceRepo = match[2];
    }
  } catch {
    // Not in a git repo
  }

  // Get user email and squad
  try {
    const email = execSync("git config user.email", {
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
    }).trim();

    if (email) {
      // Derive filename from email (first.last -> first-last-*.cue pattern)
      const emailName = email.split("@")[0] ?? "";
      const nameParts = emailName.split(".").join("-");

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
          userSquad = teamMatch[1];
        }
      }

      // Get squad's repos
      if (userSquad) {
        try {
          const squadContent = execSync(
            `gh api repos/${ORG}/estate-catalog/contents/catalog/squads/${userSquad}.cue --jq '.content' | base64 -d`,
            { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 10000 }
          );

          const ownReposMatch = squadContent.match(/ownRepos:\s*\[([^\]]+)\]/);
          if (ownReposMatch?.[1]) {
            // Extract all quoted strings from the repos list
            const repoPathMatches = ownReposMatch[1].match(/"([^"]+)"/g) ?? [];
            for (const quotedPath of repoPathMatches) {
              // Remove quotes and extract repo name
              const repoPath = quotedPath.replace(/"/g, "");
              squadRepos.push(repoPath.replace(`${ORG}/`, ""));
            }
          }
        } catch {
          // Squad file not found
        }

        // Also check bitso-gradle-catalogs (auto-clones if needed)
        const catalogRepos = findReposBySquad(userSquad);
        for (const repo of catalogRepos) {
          if (!squadRepos.includes(repo)) {
            squadRepos.push(repo);
          }
        }
      }
    }
  } catch {
    // Email/squad detection failed
  }

  return { workspaceRepo, userSquad, squadRepos };
}

// Fetch PRs from GitHub
function fetchPRs(repos: string[]): PR[] {
  const allPRs: PR[] = [];
  // Validate and filter repo names to prevent command injection
  const safeRepos = repos.filter((repo) => {
    if (!/^[\w.-]+$/.test(repo)) {
      console.error(`⚠️  Skipping invalid repo name: ${repo}`);
      return false;
    }
    return true;
  });

  for (const repo of safeRepos) {
    try {
      // Use spawnSync with array args to prevent shell command injection (CodeQL js/shell-command-injection-from-environment)
      const result = spawnSync(
        "gh",
        ["pr", "list", "--author", "app/dependabot", "--state", "open", "--repo", `${ORG}/${repo}`, "--json", "number,title,url,createdAt,labels"],
        { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 30000 }
      );
      const output = result.stdout;

      const prs = JSON.parse(output || "[]") as Array<{
        number: number;
        title: string;
        url: string;
        createdAt: string;
        labels: Array<{ name: string }>;
      }>;

      for (const pr of prs) {
        allPRs.push({
          number: pr.number,
          title: pr.title,
          url: pr.url,
          repo,
          createdAt: pr.createdAt,
          labels: pr.labels.map((l) => l.name),
        });
      }
    } catch {
      console.error(`⚠️  Failed to fetch PRs for ${repo}`);
    }
  }

  return allPRs.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
}

// Fetch all Dependabot PRs across the org (limited)
function fetchAllPRs(limit = 50): PR[] {
  try {
    const output = execSync(
      `gh search prs --author app/dependabot --state open --owner ${ORG} --limit ${limit} --json number,title,url,createdAt,repository`,
      { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 60000 }
    );

    const prs = JSON.parse(output || "[]") as Array<{
      number: number;
      title: string;
      url: string;
      createdAt: string;
      repository: { name: string };
    }>;

    return prs.map((pr) => ({
      number: pr.number,
      title: pr.title,
      url: pr.url,
      repo: pr.repository.name,
      createdAt: pr.createdAt,
      labels: [],
    }));
  } catch (e) {
    console.error(`⚠️  Failed to fetch PRs: ${e}`);
    return [];
  }
}

function parseArgs(): { all: boolean; repo: string | null; squad: string | null; json: boolean } {
  const args = process.argv.slice(2);
  const getValue = (flag: string): string | null => {
    const idx = args.indexOf(flag);
    if (idx === -1) {
      return null;
    }
    const value = args[idx + 1];
    return value && !value.startsWith("--") ? value : null;
  };
  return {
    all: args.includes("--all"),
    repo: getValue("--repo"),
    squad: getValue("--squad"),
    json: args.includes("--json"),
  };
}

function main() {
  const args = parseArgs();

  console.error("🔍 Greenflag Dependabot PRs\n");

  let repos: string[] = [];
  let filterDescription: string;
  let useSearch = false;

  if (args.all) {
    console.error("📋 Fetching all open Dependabot PRs...\n");
    filterDescription = "All repos";
    useSearch = true;
  } else if (args.repo) {
    repos = [args.repo];
    filterDescription = `Repo: ${args.repo}`;
  } else if (args.squad) {
    // Get squad's repos using same logic as detectContext
    console.error(`👥 Filtering by squad: ${args.squad}`);

    // Validate squad name to prevent command injection
    if (!/^[\w-]+$/.test(args.squad)) {
      console.error("❌ Invalid squad name format");
      process.exit(1);
    }

    // First try estate-catalog
    try {
      const squadContent = execSync(
        `gh api repos/${ORG}/estate-catalog/contents/catalog/squads/${args.squad}.cue --jq '.content' | base64 -d`,
        { encoding: "utf-8", stdio: ["pipe", "pipe", "pipe"], timeout: 10000 }
      );
      const ownReposMatch = squadContent.match(/ownRepos:\s*\[([^\]]+)\]/);
      if (ownReposMatch?.[1]) {
        const repoPathMatches = ownReposMatch[1].match(/"([^"]+)"/g) ?? [];
        for (const quotedPath of repoPathMatches) {
          const repoPath = quotedPath.replace(/"/g, "");
          repos.push(repoPath.replace(`${ORG}/`, ""));
        }
      }
    } catch {
      console.error(`⚠️  Could not fetch repos from estate-catalog for squad: ${args.squad}`);
    }

    // Also check bitso-gradle-catalogs for repos with this squad (auto-clones if needed)
    const catalogRepos = findReposBySquad(args.squad);
    for (const repo of catalogRepos) {
      if (!repos.includes(repo)) {
        repos.push(repo);
      }
    }

    if (repos.length === 0) {
      console.error(`⚠️  No repos found for squad: ${args.squad}, falling back to search`);
      useSearch = true;
    }
    filterDescription = `Squad: ${args.squad} (${repos.length} repos)`;
  } else {
    // Auto-detect context
    const context = detectContext();

    if (context.workspaceRepo) {
      repos = [context.workspaceRepo];
      filterDescription = `Workspace repo: ${context.workspaceRepo}`;
      console.error(`📂 Detected workspace repo: ${context.workspaceRepo}`);
    } else if (context.userSquad && context.squadRepos.length > 0) {
      repos = context.squadRepos;
      filterDescription = `Squad: ${context.userSquad} (${repos.length} repos)`;
      console.error(`👥 Detected squad: ${context.userSquad}`);
      console.error(`📦 Squad repos: ${repos.join(", ")}`);
    } else {
      console.error("ℹ️  No context detected, showing all PRs");
      filterDescription = "All repos";
      useSearch = true;
    }
  }

  console.error(`\n🎯 Filter: ${filterDescription}\n`);

  let prs: PR[];
  if (useSearch) {
    prs = fetchAllPRs();
  } else {
    prs = fetchPRs(repos);
  }

  if (args.json) {
    console.log(JSON.stringify({ filter: filterDescription, prs }, null, 2));
  } else if (prs.length === 0) {
    console.log("✅ No open Dependabot PRs found!");
  } else {
    console.log(`Found ${prs.length} open Dependabot PR(s):\n`);
    for (const pr of prs) {
      const age = Math.floor((Date.now() - new Date(pr.createdAt).getTime()) / (1000 * 60 * 60 * 24));
      console.log(`  #${pr.number} [${pr.repo}] ${pr.title}`);
      console.log(`     ${pr.url} (${age}d old)`);
    }
  }
}

main();
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/greenflag-dependabot/scripts/list-dependabot-prs.ts
// To modify, edit the source file and run the distribution workflow

