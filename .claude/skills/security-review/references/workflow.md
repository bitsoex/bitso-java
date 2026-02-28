# Security Review Workflow

Step-by-step security review process for any codebase.

## Contents

- [Review Steps](#review-steps)
- [Output Format](#output-format)
- [Example Finding](#example-finding)
- [Severity Levels](#severity-levels)

---
## Review Steps

### 1. Credential Scanning

- [ ] Search for hardcoded passwords, API keys, or tokens
- [ ] Check for exposed database connection strings
- [ ] Verify secrets are properly externalized

**Common patterns to search for:**

```text
password=
api_key=
secret=
token=
private_key
-----BEGIN RSA PRIVATE KEY-----
-----BEGIN OPENSSH PRIVATE KEY-----
```

### 2. Input Validation

- [ ] Review all user input handling
- [ ] Check for SQL injection vulnerabilities
- [ ] Verify XSS protection measures
- [ ] Check for command injection
- [ ] Verify path traversal protection

### 3. Authentication & Authorization

- [ ] Review authentication mechanisms
- [ ] Check authorization controls
- [ ] Verify session management
- [ ] Check for proper logout handling
- [ ] Review password storage (hashing, salting)

### 4. Data Protection

- [ ] Review data encryption at rest and in transit
- [ ] Check for sensitive data exposure
- [ ] Verify proper data sanitization
- [ ] Check for PII handling compliance
- [ ] Review logging (no sensitive data in logs)

### 5. Dependency Security

- [ ] Scan for known vulnerable dependencies
- [ ] Review third-party library usage
- [ ] Check for outdated packages
- [ ] Verify license compliance

**Tools:**

```bash
# npm
npm audit

# yarn
yarn audit

# Maven
mvn dependency-check:check

# Gradle
./gradlew dependencyCheckAnalyze
```

## Output Format

Provide findings in this format:

- **Finding**: Description of the issue
- **Severity**: Critical/High/Medium/Low
- **Recommendation**: How to fix the issue
- **File/Line**: Location of the issue

## Example Finding

```markdown
### Finding: Hardcoded API Key

- **Severity**: Critical
- **Location**: `src/config/api.ts:15`
- **Description**: API key is hardcoded in source code
- **Recommendation**: Move to environment variable
- **Code**: `const API_KEY = 'sk-xxx...'`
```

## Severity Levels

| Severity | Description | Action |
|----------|-------------|--------|
| Critical | Immediate exploitation risk | Fix before merge |
| High | Significant security risk | Fix in same sprint |
| Medium | Moderate security concern | Fix soon |
| Low | Minor security improvement | Fix when convenient |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/security-review/references/workflow.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

