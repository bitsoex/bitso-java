# General AI Coding Principles

These principles apply to all repositories regardless of technology stack.

## Code Quality Standards

### Clarity and Readability

- Write self-documenting code with clear variable and function names
- Use consistent formatting and indentation
- Add comments only when the code's intent isn't obvious

### Security First

- Never hardcode sensitive information (passwords, API keys, tokens)
- Validate all inputs from external sources
- Follow the principle of least privilege

### Performance Considerations

- Optimize for readability first, performance second
- Profile before optimizing
- Use appropriate data structures and algorithms

### Error Handling

- Fail fast and provide meaningful error messages
- Handle exceptions gracefully
- Log errors with sufficient context for debugging

## Development Practices

### Dependency Version Management

- **Never downgrade pre-existing versions** - If a version already exists in the repo before your PR, do not replace it with an older version
- **Only adjust versions your PR introduces** - If you upgrade a library and it causes issues, try a different (but still newer) version
- **Never override BOM-managed versions with older versions** - BOMs (Bill of Materials) like Spring Boot manage transitive dependencies; trust them
- **When in doubt, warn instead of downgrade** - Add a comment explaining potential incompatibility rather than forcing an older version
- **Centralize all versions** - Use version catalogs or dedicated version files, never hardcode versions inline

See `java/rules/java-versions-and-dependencies.md` for Java-specific version management policies.

### Version Control

- Make atomic commits with descriptive messages
- Keep commits small and focused on a single change
- Use meaningful branch names

### Testing

- Write tests for critical functionality
- Maintain good test coverage
- Use descriptive test names that explain the expected behavior

### Documentation

- Keep README files up to date
- Document public APIs and complex algorithms
- Include setup and deployment instructions
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/coding-standards/references/general-principles.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

