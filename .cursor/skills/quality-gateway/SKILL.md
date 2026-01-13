<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/quality-gateway/SKILL.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

---
name: quality-gateway
description: >
  Orchestrates quality checks at key lifecycle points: before edits, after edits, and before completion.
  Coordinates sub-skills (test-augmentation, doc-sync, coding-standards, security-review) to ensure
  consistent code quality. Use when making significant code changes or before completing a task.
compatibility: Requires Node.js 24+; works with any codebase
metadata:
  version: "0.1"
---

# Quality Gateway

> **Placeholder**: This skill will be fully developed during the content migration phase.

## When to use this skill

- Before starting significant code changes (pre-edit)
- After completing code changes (post-edit)
- Before marking a task as complete (on-stop)

## Sub-Skills

The quality gateway orchestrates these sub-skills:

| Sub-Skill | Purpose |
|-----------|---------|
| `test-augmentation` | Validates test coverage |
| `doc-sync` | Validates documentation |
| `coding-standards` | Enforces code style |
| `security-review` | Checks for vulnerabilities |

## Lifecycle Hooks

| Hook | When | Purpose |
|------|------|---------|
| `pre-edit` | Before changes | Capture baseline metrics |
| `post-edit` | After changes | Validate changes meet standards |
| `on-stop` | Before completion | Final quality gate |

## TODO

- [ ] Define orchestration logic for sub-skills
- [ ] Implement baseline capture for pre-edit
- [ ] Define quality thresholds and gates
- [ ] Integrate with IDE lifecycle events
