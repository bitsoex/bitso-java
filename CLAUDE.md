# Claude AI Assistant Rules

This repository uses AI-assisted development with specific coding guidelines and rules.

## Available Rules

### Never work directly on main. Always verify your branch and create a new one if needed before making changes.
**File:** [`.claude/branch-protection-workflow.md`](.claude/branch-protection-workflow.md)
**Description:** Never work directly on main. Always verify your branch and create a new one if needed before making changes.
**Scope:** **/*

### CodeRabbit Setup
**File:** [`.claude/coderabbit-setup.md`](.claude/coderabbit-setup.md)
**Description:** CodeRabbit Setup
**Scope:** 

### General AI Coding Principles
**File:** [`.claude/general-principles.md`](.claude/general-principles.md)
**Description:** General AI Coding Principles
**Scope:** 

### GitHub CLI PR Lifecycle
**File:** [`.claude/github-cli-pr-lifecycle.md`](.claude/github-cli-pr-lifecycle.md)
**Description:** GitHub CLI PR Lifecycle
**Scope:** 

### Jira Ticket Workflow for AI Agent Commands
**File:** [`.claude/jira-ticket-workflow.md`](.claude/jira-ticket-workflow.md)
**Description:** Jira Ticket Workflow for AI Agent Commands
**Scope:** **/commands/**

### Markdown Documentation Review Guidelines
**File:** [`.claude/markdown-review.md`](.claude/markdown-review.md)
**Description:** Markdown Documentation Review Guidelines
**Scope:** **/*.md

### MCP Setup
**File:** [`.claude/mcp-setup.md`](.claude/mcp-setup.md)
**Description:** MCP Setup
**Scope:** 

### Quality checks for code changes
**File:** [`.claude/quality-gate.md`](.claude/quality-gate.md)
**Description:** Quality checks for code changes
**Scope:** **/*.ts, **/*.tsx, **/*.js, **/*.jsx, **/*.java, **/*.py, **/*.go

### Java Code Review Standards
**File:** [`.claude/java-code-review-standards.md`](.claude/java-code-review-standards.md)
**Description:** Java Code Review Standards
**Scope:** **/*.java

### Fix strategy hierarchy for Dependabot vulnerabilities in Java projects
**File:** [`.claude/java-dependabot-security.md`](.claude/java-dependabot-security.md)
**Description:** Fix strategy hierarchy for Dependabot vulnerabilities in Java projects
**Scope:** gradle/libs.versions.toml, build.gradle

### RFC-44 compliant distributed locking with PostgreSQL advisory locks or Redis
**File:** [`.claude/java-distributed-locking-rfc44.md`](.claude/java-distributed-locking-rfc44.md)
**Description:** RFC-44 compliant distributed locking with PostgreSQL advisory locks or Redis
**Scope:** **/*Lock*.java, **/*Scheduler*.java, **/application*.yml

### Flyway SQL Migration Review Safety Issues Guidelines
**File:** [`.claude/java-flyway-migrations-safety-issues.md`](.claude/java-flyway-migrations-safety-issues.md)
**Description:** Flyway SQL Migration Review Safety Issues Guidelines
**Scope:** **/*.sql

### Flyway SQL Migration Review Guidelines
**File:** [`.claude/java-flyway-migrations.md`](.claude/java-flyway-migrations.md)
**Description:** Flyway SQL Migration Review Guidelines
**Scope:** **/*.sql

### Gradle configuration standards, multi-module setup, and build patterns
**File:** [`.claude/java-gradle-best-practices.md`](.claude/java-gradle-best-practices.md)
**Description:** Gradle configuration standards, multi-module setup, and build patterns
**Scope:** gradle/**, **/build.gradle, **/settings.gradle, **/gradle.properties

### Common Gradle commands for building, testing, and dependency management
**File:** [`.claude/java-gradle-commands.md`](.claude/java-gradle-commands.md)
**Description:** Common Gradle commands for building, testing, and dependency management
**Scope:** **/build.gradle, **/settings.gradle, gradlew

### Nebula Gradle Lint plugin rules and violation fixes
**File:** [`.claude/java-gradle-lint-plugin.md`](.claude/java-gradle-lint-plugin.md)
**Description:** Nebula Gradle Lint plugin rules and violation fixes
**Scope:** **/build.gradle, **/settings.gradle

### RFC-33 retry, bulkhead, circuit breaker, and deadline propagation for gRPC clients
**File:** [`.claude/java-grpc-resilience.md`](.claude/java-grpc-resilience.md)
**Description:** RFC-33 retry, bulkhead, circuit breaker, and deadline propagation for gRPC clients
**Scope:** **/application*.yml, **/*GrpcClient*.java

### RFC-33 compliant gRPC service implementation patterns
**File:** [`.claude/java-grpc-services.md`](.claude/java-grpc-services.md)
**Description:** RFC-33 compliant gRPC service implementation patterns
**Scope:** **/*Handler.java, **/*GrpcService*.java, **/*.proto

### JaCoCo setup, multi-module aggregation, and coverage thresholds
**File:** [`.claude/java-jacoco-coverage.md`](.claude/java-jacoco-coverage.md)
**Description:** JaCoCo setup, multi-module aggregation, and coverage thresholds
**Scope:** **/jacoco.gradle, **/build.gradle

### jOOQ code generation, Spring configuration, and read/write splitting patterns
**File:** [`.claude/java-jooq.md`](.claude/java-jooq.md)
**Description:** jOOQ code generation, Spring configuration, and read/write splitting patterns
**Scope:** **/jooq/**, **/build.gradle, **/JooqConfiguration.java

### RFC-19 breaking change detection and RFC-33 compliant Buf linting for protobuf contracts
**File:** [`.claude/java-protobuf-linting.md`](.claude/java-protobuf-linting.md)
**Description:** RFC-19 breaking change detection and RFC-33 compliant Buf linting for protobuf contracts
**Scope:** **/*.proto, **/buf.yaml, **/buf.gen.yaml

### RFC-30 naming/versioning and RFC-39 API best practices for Java REST services
**File:** [`.claude/java-rest-api-guidelines.md`](.claude/java-rest-api-guidelines.md)
**Description:** RFC-30 naming/versioning and RFC-39 API best practices for Java REST services
**Scope:** **/*Controller.java, **/openapi.yaml

### Java Run Build After Changes
**File:** [`.claude/java-run-build-after-changes.md`](.claude/java-run-build-after-changes.md)
**Description:** Java Run Build After Changes
**Scope:** 

### RFC-37 documentation structure for Java services
**File:** [`.claude/java-service-documentation.md`](.claude/java-service-documentation.md)
**Description:** RFC-37 documentation structure for Java services
**Scope:** docs/**, **/mark.toml

### Tech stack, project organization, and coding standards for Bitso Java services
**File:** [`.claude/java-services-standards.md`](.claude/java-services-standards.md)
**Description:** Tech stack, project organization, and coding standards for Bitso Java services
**Scope:** **/*.java, **/build.gradle

### Using SonarQube MCP server for Java code quality analysis
**File:** [`.claude/java-sonarqube-mcp.md`](.claude/java-sonarqube-mcp.md)
**Description:** Using SonarQube MCP server for Java code quality analysis
**Scope:** **/*.java

### SonarQube plugin configuration for Java/Gradle projects
**File:** [`.claude/java-sonarqube-setup.md`](.claude/java-sonarqube-setup.md)
**Description:** SonarQube plugin configuration for Java/Gradle projects
**Scope:** **/build.gradle, gradle.properties

### RFC-34 structured logging with JSON format and key-value pairs
**File:** [`.claude/java-structured-logs.md`](.claude/java-structured-logs.md)
**Description:** RFC-34 structured logging with JSON format and key-value pairs
**Scope:** **/*.java, **/logback*.xml

### Testing patterns with JUnit, Spock, and Testcontainers
**File:** [`.claude/java-testing-guidelines.md`](.claude/java-testing-guidelines.md)
**Description:** Testing patterns with JUnit, Spock, and Testcontainers
**Scope:** **/*Test.java, **/*Spec.groovy, **/test/**

### Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
**File:** [`.claude/java-versions-and-dependencies.md`](.claude/java-versions-and-dependencies.md)
**Description:** Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle

### Proven fix patterns for common Dependabot vulnerabilities in Java/Gradle projects
**File:** [`.claude/java-vulnerability-golden-paths.md`](.claude/java-vulnerability-golden-paths.md)
**Description:** Proven fix patterns for common Dependabot vulnerabilities in Java/Gradle projects
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle


## Usage

These rules are automatically applied when using Claude AI assistant in this repository. Each rule file contains specific guidelines for different aspects of the codebase.

## Rule Categories

- **Java**: java specific coding guidelines (31 rules)

---

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions -->
<!-- To modify, edit the source files and run the distribution workflow -->
