---
name: doc-coauthoring
description: >
  Guide users through a structured workflow for co-authoring documentation, proposals, technical specs,
  decision docs, RFCs, or ADRs. Use when user wants to write documentation, create proposals, draft specs,
  write up decisions, or similar structured content. Trigger when user mentions writing docs, creating proposals,
  drafting specs, RFCs, ADRs, or any substantial documentation task.
compatibility: Works with any codebase; benefits from Confluence and Jira MCP integrations when available
metadata:
  version: "1.0"
---

# Doc Co-Authoring Workflow

A structured workflow for collaboratively creating documentation with users through three stages: Context Gathering, Refinement and Structure, and Reader Testing.

## When to Offer This Workflow

**Trigger conditions:**
- User mentions writing documentation: "write a doc", "draft a proposal", "create a spec"
- User mentions specific doc types: "PRD", "design doc", "decision doc", "RFC", "ADR"
- User seems to be starting a substantial writing task

**Initial offer:**
Offer the structured co-authoring workflow. Explain the three stages:

1. **Context Gathering** - User provides all relevant context while the agent asks clarifying questions
2. **Refinement and Structure** - Iteratively build each section through brainstorming and editing
3. **Reader Testing** - Test the doc with a fresh context to catch blind spots

Ask if they want to try this workflow or prefer to work freeform. If they decline, work freeform.

## Skill Contents

### Sections

- [When to Offer This Workflow](#when-to-offer-this-workflow)
- [Stage 1: Context Gathering](#stage-1-context-gathering)
- [Stage 2: Refinement and Structure](#stage-2-refinement-and-structure)
- [Stage 3: Reader Testing](#stage-3-reader-testing)
- [Final Review](#final-review)
- [Tips for Effective Guidance](#tips-for-effective-guidance)

### Available Resources

**📚 references/** - Detailed documentation
- [bitso templates](references/bitso-templates.md)
- [stage details](references/stage-details.md)

---

## Stage 1: Context Gathering

**Goal:** Close the gap between what the user knows and what the agent knows, enabling smart guidance later.

### Initial Questions

Start by asking for meta-context about the document:

1. What type of document is this? (e.g., technical spec, decision doc, proposal, RFC, ADR)
2. Who is the primary audience?
3. What is the desired impact when someone reads this?
4. Is there a template or specific format to follow?
5. Any other constraints or context?

Inform them they can answer in shorthand or dump information however works best.

### Gathering Context from Integrations

If Confluence or Jira MCP integrations are available:

- **Confluence**: Search for related existing docs, templates, or prior art
- **Jira**: Pull in requirements, acceptance criteria, or related tickets
- Mention these capabilities and ask if there are specific pages or tickets to reference

If no integrations are available, suggest the user paste relevant content directly.

### Info Dumping

Encourage the user to dump all context they have:

- Background on the project or problem
- Related team discussions or documents
- Why alternative solutions are not being used
- Organizational context (team dynamics, past incidents)
- Timeline pressures or constraints
- Technical architecture or dependencies
- Stakeholder concerns

Advise them not to worry about organizing it. Offer multiple ways to provide context:
- Stream-of-consciousness dump
- Links to Confluence pages or Jira tickets
- Pasted content from messages or documents

### Clarifying Questions

When the user signals they have done their initial dump, ask 5-10 numbered clarifying questions based on gaps in the context.

Inform them they can use shorthand to answer (e.g., "1: yes, 2: see PROJ-123, 3: no because backwards compat").

**Exit condition:** Sufficient context has been gathered when clarifying questions demonstrate understanding -- when edge cases and trade-offs can be discussed without needing basics explained.

**Transition:** Ask if there is more context to provide, or if it is time to move on to drafting.

### Bitso-Specific Document Types

For common Bitso doc types, suggest appropriate formats:

| Doc Type | Format | Template Source |
|----------|--------|----------------|
| RFC | RFC-37 format | See `doc-generation-rfc-37` skill |
| ADR | Decision record | Repository `docs/decisions/` directory |
| Technical spec | Design doc | Team-specific templates |
| Runbook | Operational guide | Repository `docs/runbooks/` directory |
| How-to | Step-by-step guide | Repository `docs/how-tos/` directory |

See [references/bitso-templates.md](references/bitso-templates.md) for details.

## Stage 2: Refinement and Structure

**Goal:** Build the document section by section through brainstorming, curation, and iterative refinement.

### Setup

Explain that the document will be built section by section. For each section:

1. Clarifying questions about what to include
2. Brainstorm 5-20 options
3. User indicates what to keep, remove, or combine
4. Draft the section
5. Refine through surgical edits

Start with whichever section has the most unknowns (usually the core proposal or technical approach). Leave summary sections for last.

### Create Document Scaffold

Create a markdown file with the appropriate name (e.g., `decision-doc.md`, `technical-spec.md`, `rfc-NNN.md`).

Include all section headers with `[To be written]` placeholder text.

For RFC-37 documents, follow the template from the `doc-generation-rfc-37` skill.

### For Each Section

#### 1. Clarifying Questions

Announce work will begin on the section. Ask 5-10 specific questions about what should be included.

#### 2. Brainstorming

Brainstorm 5-20 things that might be included, depending on complexity. Look for:
- Context shared earlier that might have been forgotten
- Angles or considerations not yet mentioned

Offer to brainstorm more if additional options are wanted.

#### 3. Curation

Ask which points to keep, remove, or combine. Request brief justifications to learn priorities.

Provide examples of shorthand:
- "Keep 1,4,7,9"
- "Remove 3 (duplicates 1)"
- "Combine 11 and 12"

If the user gives freeform feedback instead of numbered selections, extract preferences and proceed.

#### 4. Gap Check

Based on selections, ask if anything important is missing for this section.

#### 5. Drafting

Replace the placeholder text with the drafted content using file editing tools.

**Key instruction (include when drafting the first section):** Ask the user to indicate what to change rather than editing the doc directly. This helps the agent learn their style for future sections.

#### 6. Iterative Refinement

As the user provides feedback:
- Make surgical edits (never reprint the whole document)
- If the user edits directly, note their changes to learn preferences

Continue iterating until the user is satisfied with the section.

### Quality Checking

After 3 consecutive iterations with no substantial changes, ask if anything can be removed without losing important information.

### Near Completion

When 80%+ of sections are done, re-read the entire document and check for:
- Flow and consistency across sections
- Redundancy or contradictions
- Generic filler that does not carry weight
- Whether every sentence adds value

Provide feedback and final suggestions before moving to Reader Testing.

## Stage 3: Reader Testing

**Goal:** Test the document with a fresh context (no prior knowledge) to verify it works for readers.

### With Sub-Agent Support

If sub-agent capabilities are available (e.g., Claude Code, Cursor with Task tool):

1. **Predict Reader Questions**: Generate 5-10 questions readers would realistically ask
2. **Test with Sub-Agent**: For each question, invoke a sub-agent with just the document and the question
3. **Run Additional Checks**: Invoke sub-agent to check for ambiguity, false assumptions, contradictions
4. **Report and Fix**: List specific issues, then loop back to refinement for problematic sections

### Without Sub-Agent Support

Provide manual testing instructions:

1. **Predict Reader Questions**: Generate 5-10 questions
2. **Setup**: Ask the user to open a fresh conversation and paste or share the document
3. **Test**: Ask the fresh instance the generated questions, plus:
   - "What in this doc might be ambiguous or unclear?"
   - "What knowledge does this doc assume readers already have?"
   - "Are there any internal contradictions?"
4. **Iterate**: Fix gaps identified by the reader test

### Exit Condition

The document passes when reader questions are consistently answered correctly and no new gaps or ambiguities surface.

## Final Review

When Reader Testing passes:

1. Recommend the user do a final read-through themselves -- they own the document
2. Suggest double-checking facts, links, and technical details
3. Ask if the document achieves the impact they wanted

If for an RFC-37 document, run the `doc-validation-rfc-37` skill to verify structure compliance.

**Final tips:**
- Consider linking the conversation in an appendix so readers can see how the doc was developed
- Use appendices for depth without bloating the main document
- Update the doc as feedback is received from real readers

## Tips for Effective Guidance

**Tone:**
- Be direct and procedural
- Explain rationale briefly when it affects user behavior
- Do not try to "sell" the approach -- just execute it

**Handling Deviations:**
- If the user wants to skip a stage, accommodate and adjust
- If the user seems frustrated, acknowledge and suggest ways to move faster
- Always give the user agency to adjust the process

**Context Management:**
- If context is missing on something mentioned, proactively ask
- Do not let gaps accumulate -- address them as they come up

**Quality over Speed:**
- Do not rush through stages
- Each iteration should make meaningful improvements
- The goal is a document that works for readers
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-coauthoring/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

