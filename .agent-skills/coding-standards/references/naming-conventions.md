# Naming Conventions

> **Work in Progress**: Full content tracked in EN-111. Use the interim guidance below.

## Interim Guidance

### General Principles

- Use descriptive, meaningful names that convey purpose
- Be consistent within each codebase
- Avoid abbreviations unless widely understood

### Language-Specific Resources

| Language | Reference |
|----------|-----------|
| Java | `.cursor/rules/java-services-standards.mdc` |
| TypeScript | `.cursor/rules/typescript-standards.mdc` |
| Python | `.cursor/rules/` directory (Python rules) |
| Go | `.cursor/rules/` directory (Go rules) |

> **Note**: `.cursor/rules/` paths refer to distributed artifacts in consumer repos.

### Quick Reference

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserService`, `PaymentProcessor` |
| Functions | camelCase (JS/TS) or snake_case (Python) | `calculateTotal`, `calculate_total` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRIES`, `API_BASE_URL` |
| Variables | camelCase (JS/TS) or snake_case (Python) | `userId`, `user_id` |
| Booleans | Prefix with is/has/can | `isActive`, `hasPermission` |

## TODO

- [ ] Migrate full content from existing language rules
- [ ] Add technology-specific detailed examples
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coding-standards/references/naming-conventions.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

