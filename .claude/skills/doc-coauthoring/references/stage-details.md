---
title: Stage Details
description: Expanded per-stage instructions and edge case handling for the doc co-authoring workflow
---

# Stage Details

Expanded instructions for each stage of the doc co-authoring workflow. Read this when handling edge cases or when the condensed SKILL.md instructions need more detail.

## Contents

- [Stage 1 Deep Dive](#stage-1-deep-dive)
- [Stage 2 Deep Dive](#stage-2-deep-dive)
- [Stage 3 Deep Dive](#stage-3-deep-dive)
- [Edge Cases](#edge-cases)

---
## Stage 1 Deep Dive

### Handling Existing Documents

If the user mentions editing an existing document:

1. Read the current state of the document
2. Check for images without alt-text -- explain that when others use AI to understand the doc, images without alt-text are invisible
3. If images exist without alt-text, offer to generate descriptive alt-text (ask user to paste each image)

### Handling External References

When the user mentions team channels, shared documents, or external systems:

- If Confluence MCP is available: offer to read the referenced page directly
- If Jira MCP is available: offer to pull issue details
- If no integrations: ask user to paste the relevant content

### When to Stop Gathering Context

Context gathering is sufficient when:
- Edge cases and trade-offs can be discussed without needing basics explained
- Questions demonstrate understanding of the domain
- The user has no more context to provide

Signs it is time to move on:
- User responds with "that's everything" or similar
- Answers to clarifying questions are mostly "yes, you got it"
- The user starts asking about the document structure

### Optimizing Context Gathering for Speed

If the user seems time-pressured:
- Reduce initial questions to the 3 most critical
- Accept less context and note where assumptions are being made
- Offer to revisit gaps during refinement

## Stage 2 Deep Dive

### Section Ordering Strategy

| Document Type | Start With | End With |
|---------------|-----------|----------|
| Decision doc | Core proposal/decision | Executive summary |
| Technical spec | Technical approach | Overview, timeline |
| RFC | Problem statement, proposed solution | Alternatives, migration |
| ADR | Decision and rationale | Context, consequences |
| Runbook | Step-by-step procedures | Prerequisites, troubleshooting |

### Brainstorming Effectively

When generating options for a section:

1. Draw from context already provided (the user may have forgotten they mentioned something relevant)
2. Consider the audience -- what would they expect to see?
3. Include both obvious and non-obvious items
4. Think about what questions the section should answer

For complex sections, organize brainstormed items into categories before presenting.

### Handling Freeform Feedback

When the user gives feedback like "looks good but..." instead of numbered selections:

1. Extract what they want kept (anything not mentioned is implicitly kept)
2. Extract what they want changed
3. Summarize the interpretation back to them before applying
4. Apply changes and confirm

### Learning User Style

Track these preferences across sections:

- Level of detail preferred (concise vs. thorough)
- Tone (formal vs. conversational)
- Structure preferences (bullet lists vs. prose)
- What they consistently remove (helps predict for next sections)
- What they consistently add (helps include proactively)

### Multi-Author Documents

If the user mentions collaborating with others:

- Suggest a clear ownership model per section
- Offer to maintain a "decisions log" tracking key choices and rationale
- Recommend keeping the doc in a shared location (Confluence, Google Docs)
- Note sections that need input from specific people

## Stage 3 Deep Dive

### Generating Effective Reader Questions

Good test questions:

1. **Discovery questions**: "What is [the main topic]?" -- tests if the doc explains itself
2. **Action questions**: "How do I [main procedure]?" -- tests if instructions are clear
3. **Decision questions**: "Why was [approach] chosen over alternatives?" -- tests rationale
4. **Edge case questions**: "What happens when [unusual scenario]?" -- tests completeness
5. **Scope questions**: "Does this cover [related but out-of-scope topic]?" -- tests boundaries

### Sub-Agent Testing Protocol

When using sub-agents for testing:

```
For each question:
1. Create sub-agent with ONLY the document content and the question
2. Do NOT include any conversation context
3. Ask the sub-agent to:
   - Answer the question based solely on the document
   - Rate confidence (high/medium/low)
   - Note any ambiguities encountered
   - List assumed knowledge not in the document
4. Compare sub-agent answer with intended answer
5. Flag any incorrect or low-confidence answers
```

### Common Issues Found During Testing

| Issue | Fix Strategy |
|-------|-------------|
| Reader misunderstands terminology | Add glossary or inline definitions |
| Reader cannot find the answer | Improve section headings and structure |
| Reader gets wrong answer | Clarify ambiguous phrasing |
| Reader needs assumed knowledge | Add prerequisites or context section |
| Reader overwhelmed by detail | Move detail to appendix |

## Edge Cases

### User Wants to Skip Stages

- **Skip Context Gathering**: Proceed but note where assumptions are made. Revisit during refinement.
- **Skip Refinement**: Draft a complete document based on gathered context. Ask for a single round of feedback.
- **Skip Reader Testing**: Acceptable for internal or low-stakes docs. Recommend it for anything shared widely.

### Very Long Documents

For documents exceeding 20 sections:

1. Group sections into 3-5 logical parts
2. Work through one part at a time
3. Do a cross-part consistency check after each part
4. Reserve final review for the complete document

### Technical Documents with Code

When the document includes code examples:

1. Test that code examples actually work (run them if possible)
2. Verify version numbers and API references are current
3. Include expected output for non-obvious code
4. Consider whether code should be in the doc or linked externally

### Updating Existing Documents

When revising rather than creating:

1. Read the full existing document first
2. Identify what is outdated vs. what remains valid
3. Preserve the existing structure where possible
4. Track changes for the user's review
5. Re-run Reader Testing on changed sections

### Time-Pressured Writing

When the user needs the doc quickly:

1. Focus on the critical sections first
2. Use placeholder text for non-essential sections
3. Skip brainstorming -- go directly to drafting
4. Reduce Reader Testing to 2-3 key questions
5. Mark sections that need follow-up review
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-coauthoring/references/stage-details.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

