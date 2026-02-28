#!/usr/bin/env node
/**
 * Parse CodeRabbit review body sections
 *
 * Extracts collapsible sections from the main review comment:
 * - Outside diff range comments
 * - Nitpick comments
 * - Additional comments
 * - AI fix instructions
 */

// ═══════════════════════════════════════════════════════════════════════════
// Types
// ═══════════════════════════════════════════════════════════════════════════

export interface ReviewBodySection {
  category: 'outside-diff' | 'nitpick' | 'additional' | 'actionable';
  title: string;
  count: number;
  files: { path: string; count: number; content: string }[];
}

export interface Review {
  id: number;
  body: string;
  state: string;
  submitted_at: string;
  user: { login: string };
}

export interface ProcessedComment {
  id: string;
  path: string;
  line: number | null;
  severity: string;
  title: string;
  body: string;
  createdAt: string;
  replyCount: number;
  status: string;
  resolution: string | null;
  source: 'thread' | 'review-body';
  category?: 'outside-diff' | 'nitpick' | 'additional' | 'actionable';
}

// ═══════════════════════════════════════════════════════════════════════════
// Parsing Functions
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Parse review body to extract collapsible sections
 */
export function parseReviewBody(body: string): { sections: ReviewBodySection[]; aiFixes: string | null } {
  const sections: ReviewBodySection[] = [];
  let aiFixes: string | null = null;

  const sectionPatterns: { pattern: RegExp; category: ReviewBodySection['category']; emoji: string }[] = [
    { pattern: /⚠️\s*Outside diff range comments?\s*\((\d+)\)/i, category: 'outside-diff', emoji: '⚠️' },
    { pattern: /🧹\s*Nitpick comments?\s*\((\d+)\)/i, category: 'nitpick', emoji: '🧹' },
    { pattern: /🔇\s*Additional comments?\s*\((\d+)\)/i, category: 'additional', emoji: '🔇' },
  ];

  for (const { pattern, category, emoji } of sectionPatterns) {
    const match = body.match(pattern);
    if (match) {
      const count = parseInt(match[1]!, 10);
      const section: ReviewBodySection = {
        category,
        title: `${emoji} ${category.replace('-', ' ')} (${count})`,
        count,
        files: []
      };

      // Extract file-level details from nested blockquotes
      let sectionPrefix = '🔇\\s*Additional';
      if (category === 'outside-diff') {
        sectionPrefix = '⚠️\\s*Outside diff';
      } else if (category === 'nitpick') {
        sectionPrefix = '🧹\\s*Nitpick';
      }
      const filePattern = new RegExp(
        `<summary>${sectionPrefix}[^<]*</summary>([\\s\\S]*?)</blockquote></details>`,
        'i'
      );
      const sectionContent = body.match(filePattern);
      if (sectionContent) {
        const fileMatches = sectionContent[1]!.matchAll(/<summary>([^<]+)\s*\((\d+)\)<\/summary><blockquote>([\s\S]*?)<\/blockquote>/gi);
        for (const fileMatch of fileMatches) {
          section.files.push({
            path: fileMatch[1]!.trim(),
            count: parseInt(fileMatch[2]!, 10),
            content: fileMatch[3]!.trim()
          });
        }
      }
      sections.push(section);
    }
  }

  // Extract AI fix suggestions
  const aiFixMatch = body.match(/<summary>🤖\s*Fix all issues with AI agents<\/summary>\s*```([^`]+)```/i);
  if (aiFixMatch) {
    aiFixes = aiFixMatch[1]!.trim();
  }

  return { sections, aiFixes };
}

/**
 * Convert review body sections to processed comments
 */
export function reviewBodySectionsToComments(
  sections: ReviewBodySection[],
  reviewId: string,
  createdAt: string
): ProcessedComment[] {
  const comments: ProcessedComment[] = [];

  for (const section of sections) {
    let severity = 'unknown';
    if (section.category === 'outside-diff') {severity = 'major';}
    else if (section.category === 'nitpick') {severity = 'minor';}
    else if (section.category === 'additional') {severity = 'info';}

    for (const file of section.files) {
      comments.push({
        id: `${reviewId}-${section.category}-${file.path}`,
        path: file.path,
        line: null,
        severity,
        title: `[${section.category}] ${file.path}`,
        body: file.content,
        createdAt,
        replyCount: 0,
        status: section.category === 'additional' ? 'info' : 'pending',
        resolution: null,
        source: 'review-body',
        category: section.category
      });
    }
  }
  return comments;
}

/**
 * Process reviews to extract comments and AI fixes
 */
export function processReviews(reviews: Review[]): {
  comments: ProcessedComment[];
  sections: ReviewBodySection[];
  aiFixes: string | null
} {
  const allComments: ProcessedComment[] = [];
  const allSections: ReviewBodySection[] = [];
  let latestAiFixes: string | null = null;

  const coderabbitReviews = reviews.filter(r => r.user?.login === 'coderabbitai[bot]');

  for (const review of coderabbitReviews) {
    const { sections, aiFixes } = parseReviewBody(review.body);
    if (aiFixes) {latestAiFixes = aiFixes;}

    for (const section of sections) {
      const existing = allSections.find(s => s.category === section.category);
      if (existing) {
        existing.count += section.count;
        existing.files.push(...section.files);
      } else {
        allSections.push(section);
      }
    }

    allComments.push(...reviewBodySectionsToComments(sections, String(review.id), review.submitted_at));
  }

  return { comments: allComments, sections: allSections, aiFixes: latestAiFixes };
}
// AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
// Source: bitsoex/ai-code-instructions → global/skills/coderabbit-workflow/scripts/parse-review-body.ts
// To modify, edit the source file and run the distribution workflow

