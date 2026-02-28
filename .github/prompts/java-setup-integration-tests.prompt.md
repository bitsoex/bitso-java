# Create or migrate to proper integration test setup with Testcontainers and mocked external dependencies

> Create or migrate to proper integration test setup with Testcontainers and mocked external dependencies

# Setup Java Integration Tests

Create or migrate to a proper integration test setup with real domain services, Testcontainers, and mocked external dependencies. Prefer GrpcMock/WireMock for external gRPC/REST services; use @MockitoBean/@SpringBean only for ProtoShims/custom RPCs where network-level mocking is not feasible.

## Skill Location

Full documentation: [java-setup-integration-tests](.claude/skills/java-setup-integration-tests/SKILL.md)

## Migration Workflow

Complete step-by-step guide: [migration-workflow.md](.claude/skills/java-setup-integration-tests/references/migration-workflow.md)

The migration workflow provides a comprehensive 6-phase approach:
1. **PHASE 1: ANALYSIS** - Understand service structure and dependencies
2. **PHASE 2: INFRASTRUCTURE SETUP** - Create BaseIntegrationSpec and containers
3. **PHASE 3: SERVICE-TYPE SPECIFIC SETUP** - gRPC, REST, or Kafka patterns
4. **PHASE 4: MIGRATE EXISTING TESTS** - Transform old patterns to new
5. **PHASE 5: VALIDATION** - Verify tests pass and coverage
6. **PHASE 6: REVIEW AND REPORT** - Generate migration report

## Quick Start

1. Follow [migration-workflow.md](.claude/skills/java-setup-integration-tests/references/migration-workflow.md) for complete guidance
2. Jump to specific patterns: [grpc-handler-testing](.claude/skills/java-setup-integration-tests/references/grpc-handler-testing.md), [rest-controller-testing](.claude/skills/java-setup-integration-tests/references/rest-controller-testing.md), [kafka-testing](.claude/skills/java-setup-integration-tests/references/kafka-testing.md), [base-spec-patterns](.claude/skills/java-setup-integration-tests/references/base-spec-patterns.md)

## When to Use

| Scenario | Action |
|----------|--------|
| New service needs integration tests | Use migration workflow to create infrastructure |
| Tests mock too much (bean mocking gRPC clients) | Migrate to GrpcMock/WireMock for external services |
| Tests use `@DirtiesContext` everywhere | Replace with proper cleanup patterns |
| Tests connect to real external services | Add GrpcMock (gRPC) or WireMock (REST) |
| Tests are flaky due to shared state | Implement proper isolation |

## Related

- **Java Testing**: [java-testing](.claude/skills/java-testing/SKILL.md)
- **Test Standards**: [testing-standards](.claude/skills/testing-standards/SKILL.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/java-setup-integration-tests.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
