# Security Review Workflow

**Description:** Security Review Workflow

# Security Review Workflow

This command provides a step-by-step security review process for any codebase.

## Review Steps

### 1. Credential Scanning

- [ ] Search for hardcoded passwords, API keys, or tokens
- [ ] Check for exposed database connection strings
- [ ] Verify secrets are properly externalized

### 2. Input Validation

- [ ] Review all user input handling
- [ ] Check for SQL injection vulnerabilities
- [ ] Verify XSS protection measures

### 3. Authentication & Authorization

- [ ] Review authentication mechanisms
- [ ] Check authorization controls
- [ ] Verify session management

### 4. Data Protection

- [ ] Review data encryption at rest and in transit
- [ ] Check for sensitive data exposure
- [ ] Verify proper data sanitization

### 5. Dependency Security

- [ ] Scan for known vulnerable dependencies
- [ ] Review third-party library usage
- [ ] Check for outdated packages

## Output Format

Provide findings in this format:

- **Finding**: Description of the issue
- **Severity**: Critical/High/Medium/Low
- **Recommendation**: How to fix the issue
- **File/Line**: Location of the issue
