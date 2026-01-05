# Claude AI Assistant Rules

This repository uses AI-assisted development with specific coding guidelines and rules.

## Available Rules

### Never work directly on main. Always verify your branch and create a new one if needed before making changes.
**File:** [`.claude/branch-protection-workflow.md`](.claude/branch-protection-workflow.md)
**Description:** Never work directly on main. Always verify your branch and create a new one if needed before making changes.
**Scope:** Always applies

### Coderabbit Setup
**File:** [`.claude/coderabbit-setup.md`](.claude/coderabbit-setup.md)
**Description:** Coderabbit Setup
**Scope:** Always applies

### General AI Coding Principles
**File:** [`.claude/general-principles.md`](.claude/general-principles.md)
**Description:** General AI Coding Principles
**Scope:** Always applies

### Github Cli Pr Lifecycle
**File:** [`.claude/github-cli-pr-lifecycle.md`](.claude/github-cli-pr-lifecycle.md)
**Description:** Github Cli Pr Lifecycle
**Scope:** Always applies

### Jira Ticket Workflow for AI Agent Commands
**File:** [`.claude/jira-ticket-workflow.md`](.claude/jira-ticket-workflow.md)
**Description:** Jira Ticket Workflow for AI Agent Commands
**Scope:** Always applies

### Markdown Documentation Review Guidelines
**File:** [`.claude/markdown-review.md`](.claude/markdown-review.md)
**Description:** Markdown Documentation Review Guidelines
**Scope:** **/*.md

### Mcp Setup
**File:** [`.claude/mcp-setup.md`](.claude/mcp-setup.md)
**Description:** Mcp Setup
**Scope:** Always applies

### Java Code Review Standards
**File:** [`.claude/java-code-review-standards.md`](.claude/java-code-review-standards.md)
**Description:** Java Code Review Standards
**Scope:** **/*.java

### Java Dependabot Security Vulnerability Management
**File:** [`.claude/java-dependabot-security.md`](.claude/java-dependabot-security.md)
**Description:** Java Dependabot Security Vulnerability Management
**Scope:** gradle/libs.versions.toml, build.gradle, gradle/dependency-graph-init.gradle

### RFC-44 Distributed Locking Standards for Java Services
**File:** [`.claude/java-distributed-locking-rfc44.md`](.claude/java-distributed-locking-rfc44.md)
**Description:** RFC-44 Distributed Locking Standards for Java Services
**Scope:** **/*.java, **/*.groovy, **/build.gradle, **/libs.versions.toml

### Flyway SQL Migration Review Guidelines
**File:** [`.claude/java-flyway-migrations.md`](.claude/java-flyway-migrations.md)
**Description:** Flyway SQL Migration Review Guidelines
**Scope:** **/*.sql

### Gradle configuration standards, multi-module setup, build patterns, and common commands for Java projects
**File:** [`.claude/java-gradle-best-practices.md`](.claude/java-gradle-best-practices.md)
**Description:** Gradle configuration standards, multi-module setup, build patterns, and common commands for Java projects
**Scope:** gradle/**, **/build.gradle, **/settings.gradle, **/gradle.properties

### Java Gradle Commands &amp; Debugging
**File:** [`.claude/java-gradle-commands.md`](.claude/java-gradle-commands.md)
**Description:** Java Gradle Commands &amp; Debugging
**Scope:** Always applies

### Java Gradle Lint Plugin
**File:** [`.claude/java-gradle-lint-plugin.md`](.claude/java-gradle-lint-plugin.md)
**Description:** Java Gradle Lint Plugin
**Scope:** Always applies

### Java GRPC Resilience
**File:** [`.claude/java-grpc-resilience.md`](.claude/java-grpc-resilience.md)
**Description:** Java GRPC Resilience
**Scope:** Always applies

### Java GRPC Services
**File:** [`.claude/java-grpc-services.md`](.claude/java-grpc-services.md)
**Description:** Java GRPC Services
**Scope:** Always applies

### JaCoCo plugin setup, troubleshooting, and best practices for accurate code coverage reporting
**File:** [`.claude/java-jacoco-coverage.md`](.claude/java-jacoco-coverage.md)
**Description:** JaCoCo plugin setup, troubleshooting, and best practices for accurate code coverage reporting
**Scope:** Always applies

### Java Jooq
**File:** [`.claude/java-jooq.md`](.claude/java-jooq.md)
**Description:** Java Jooq
**Scope:** Always applies

### Java Protobuf Linting
**File:** [`.claude/java-protobuf-linting.md`](.claude/java-protobuf-linting.md)
**Description:** Java Protobuf Linting
**Scope:** Always applies

### Java Rest Api Guidelines
**File:** [`.claude/java-rest-api-guidelines.md`](.claude/java-rest-api-guidelines.md)
**Description:** Java Rest Api Guidelines
**Scope:** Always applies

### Java Run Build After Changes
**File:** [`.claude/java-run-build-after-changes.md`](.claude/java-run-build-after-changes.md)
**Description:** Java Run Build After Changes
**Scope:** Always applies

### Java Service Documentation
**File:** [`.claude/java-service-documentation.md`](.claude/java-service-documentation.md)
**Description:** Java Service Documentation
**Scope:** bitso-services/**/*.java, bitso-libs/**/*.java

### Java Services Standards
**File:** [`.claude/java-services-standards.md`](.claude/java-services-standards.md)
**Description:** Java Services Standards
**Scope:** bitso-services/**/*.java, bitso-libs/**/*.java

### SonarQube MCP Tools Reference
**File:** [`.claude/java-sonarqube-mcp.md`](.claude/java-sonarqube-mcp.md)
**Description:** SonarQube MCP Tools Reference
**Scope:** **/*.java

### Java SonarQube Setup Guide
**File:** [`.claude/java-sonarqube-setup.md`](.claude/java-sonarqube-setup.md)
**Description:** Java SonarQube Setup Guide
**Scope:** **/*.java

### Java Structured Logs
**File:** [`.claude/java-structured-logs.md`](.claude/java-structured-logs.md)
**Description:** Java Structured Logs
**Scope:** Always applies

### Java Testing Guidelines
**File:** [`.claude/java-testing-guidelines.md`](.claude/java-testing-guidelines.md)
**Description:** Java Testing Guidelines
**Scope:** Always applies

### Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
**File:** [`.claude/java-versions-and-dependencies.md`](.claude/java-versions-and-dependencies.md)
**Description:** Version catalog strategy, dependency management, BOMs, and version constraints for Java projects
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle

### Proven fix patterns for common Dependabot vulnerabilities in Java/Gradle projects
**File:** [`.claude/java-vulnerability-golden-paths.md`](.claude/java-vulnerability-golden-paths.md)
**Description:** Proven fix patterns for common Dependabot vulnerabilities in Java/Gradle projects
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle

### Proven patterns for upgrading Java projects to Java 25 compatibility
**File:** [`.claude/java-25-upgrade.md`](.claude/java-25-upgrade.md)
**Description:** Proven patterns for upgrading Java projects to Java 25 compatibility
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle, gradle.properties

### Index of proven upgrade patterns for Java/Gradle projects
**File:** [`.claude/java-upgrades-golden-paths.md`](.claude/java-upgrades-golden-paths.md)
**Description:** Index of proven upgrade patterns for Java/Gradle projects
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle, gradle.properties

### Resolving JUnit version conflicts in Java/Gradle projects
**File:** [`.claude/junit-version-alignment.md`](.claude/junit-version-alignment.md)
**Description:** Resolving JUnit version conflicts in Java/Gradle projects
**Scope:** build.gradle, gradle/libs.versions.toml

### Proven patterns for resolving Redis/Jedis version conflicts during Spring Boot upgrades
**File:** [`.claude/redis-jedis-compatibility.md`](.claude/redis-jedis-compatibility.md)
**Description:** Proven patterns for resolving Redis/Jedis version conflicts during Spring Boot upgrades
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle, gradle.properties

### Proven upgrade patterns for Spring Boot 3.5.x with all required side-by-side library upgrades
**File:** [`.claude/spring-boot-3.5-upgrade.md`](.claude/spring-boot-3.5-upgrade.md)
**Description:** Proven upgrade patterns for Spring Boot 3.5.x with all required side-by-side library upgrades
**Scope:** gradle/libs.versions.toml, build.gradle, settings.gradle, gradle.properties


## Usage

These rules are automatically applied when using Claude AI assistant in this repository. Each rule file contains specific guidelines for different aspects of the codebase.

## Rule Categories

- **Java**: java specific coding guidelines (34 rules)

---
