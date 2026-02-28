#!/usr/bin/env node
/**
 * Changelog Parser
 * 
 * Parses CHANGELOG.md into structured data following Common Changelog format.
 */

import {
  VERSION_HEADING_REGEX,
  PR_LINK_REGEX,
  PR_STANDALONE_REGEX,
  COMMIT_LINK_REGEX,
  BREAKING_PREFIX_REGEX,
  REFERENCE_CLEANUP_PATTERNS
} from './constants.ts';

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface Reference {
  type: 'pr' | 'commit' | 'issue';
  number?: number;
  hash?: string;
  url: string;
}

export interface Change {
  text: string;
  references: Reference[];
  breaking: boolean;
}

export interface ChangeGroup {
  category: string;
  changes: Change[];
}

export interface Release {
  version: string;
  date: string | null;
  notice: string | null;
  groups: ChangeGroup[];
}

export interface ParsedChangelog {
  title: string;
  releases: Release[];
}

export interface ParsedVersion {
  version: string;
  date: string | null;
}

// ═══════════════════════════════════════════════════════════════════════════
// Reference Extraction
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Extract references (PR, commit, issue) from a change line.
 */
export function extractReferences(line: string): Reference[] {
  const references: Reference[] = [];
  
  // Extract PR markdown links: [#123](url)
  const prLinkRegex = new RegExp(PR_LINK_REGEX.source, 'g');
  let match;
  while ((match = prLinkRegex.exec(line)) !== null) {
    references.push({
      type: 'pr',
      number: parseInt(match[1]!, 10),
      url: match[2]!
    });
  }
  
  // Extract standalone PR references: (#123) without markdown link
  const prStandaloneRegex = new RegExp(PR_STANDALONE_REGEX.source, 'g');
  while ((match = prStandaloneRegex.exec(line)) !== null) {
    const prNumber = parseInt(match[1]!, 10);
    // Avoid duplicates
    if (!references.some(r => r.number === prNumber)) {
      references.push({
        type: 'pr',
        number: prNumber,
        url: ''
      });
    }
  }
  
  // Extract commit references: [`abc1234`](url)
  const commitRegex = new RegExp(COMMIT_LINK_REGEX.source, 'g');
  while ((match = commitRegex.exec(line)) !== null) {
    references.push({
      type: 'commit',
      hash: match[1]!,
      url: match[2]!
    });
  }
  
  return references;
}

/**
 * Parse version heading to extract version and date.
 */
export function parseVersion(heading: string): ParsedVersion | null {
  const match = heading.match(VERSION_HEADING_REGEX);
  
  if (!match) {
    return null;
  }
  
  return {
    version: match[1]!,
    date: match[2] ?? null
  };
}

/**
 * Remove all reference patterns from change text.
 */
function removeReferences(text: string): string {
  let result = text;
  for (const pattern of REFERENCE_CLEANUP_PATTERNS) {
    result = result.replace(pattern, '');
  }
  return result.trim();
}

/**
 * Check if text indicates a breaking change.
 */
function isBreakingChange(text: string): boolean {
  return BREAKING_PREFIX_REGEX.test(text) || text.includes('**Breaking:**');
}

/**
 * Clean change text by removing breaking prefix and references.
 */
function cleanChangeText(text: string): string {
  return removeReferences(text.replace(BREAKING_PREFIX_REGEX, ''));
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Parser
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse a changelog content into structured data.
 */
export function parseChangelog(content: string): ParsedChangelog {
  const lines = content.split('\n');
  const result: ParsedChangelog = {
    title: 'Changelog',
    releases: []
  };
  
  let currentRelease: Release | null = null;
  let currentGroup: ChangeGroup | null = null;
  let noticeLines: string[] = [];
  
  for (const line of lines) {
    const trimmedLine = line.trim();
    
    // Parse title: # Changelog
    if (trimmedLine.startsWith('# ') && !trimmedLine.startsWith('## ')) {
      result.title = trimmedLine.slice(2);
      continue;
    }
    
    // Parse version heading: ## [VERSION] - DATE
    if (trimmedLine.startsWith('## ')) {
      // Save current release if exists
      if (currentRelease) {
        finalizeRelease(currentRelease, currentGroup, noticeLines);
        result.releases.push(currentRelease);
      }
      
      const versionInfo = parseVersion(trimmedLine);
      if (versionInfo) {
        currentRelease = {
          version: versionInfo.version,
          date: versionInfo.date,
          notice: null,
          groups: []
        };
        currentGroup = null;
        noticeLines = [];
      }
      continue;
    }
    
    // Skip if no release context
    if (!currentRelease) {
      continue;
    }
    
    // Parse notice: _italic text_ before categories
    if (trimmedLine.startsWith('_') && trimmedLine.endsWith('_') && !currentGroup) {
      noticeLines.push(trimmedLine.slice(1, -1));
      continue;
    }
    
    // Parse category heading: ### Changed
    if (trimmedLine.startsWith('### ')) {
      // Save current group if exists
      if (currentGroup && currentGroup.changes.length > 0) {
        currentRelease.groups.push(currentGroup);
      }
      
      // Save notice if collected
      if (noticeLines.length > 0 && !currentRelease.notice) {
        currentRelease.notice = noticeLines.join(' ').trim();
        noticeLines = [];
      }
      
      currentGroup = {
        category: trimmedLine.slice(4),
        changes: []
      };
      continue;
    }
    
    // Parse change item: - Some change
    if (trimmedLine.startsWith('- ') && currentGroup) {
      const changeText = trimmedLine.slice(2);
      
      currentGroup.changes.push({
        text: cleanChangeText(changeText),
        references: extractReferences(changeText),
        breaking: isBreakingChange(changeText)
      });
    }
  }
  
  // Save final release
  if (currentRelease) {
    finalizeRelease(currentRelease, currentGroup, noticeLines);
    result.releases.push(currentRelease);
  }
  
  return result;
}

/**
 * Finalize a release by adding remaining group and notice.
 */
function finalizeRelease(
  release: Release, 
  group: ChangeGroup | null, 
  noticeLines: string[]
): void {
  if (group && group.changes.length > 0) {
    release.groups.push(group);
  }
  if (noticeLines.length > 0 && !release.notice) {
    release.notice = noticeLines.join(' ').trim();
  }
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/changelog-rfc-29/scripts/parse.ts
// To modify, edit the source file and run the distribution workflow

