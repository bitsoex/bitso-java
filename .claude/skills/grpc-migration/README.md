# gRPC Migration Skill

This custom skill provides comprehensive guidance for migrating CustomRPC implementations to standard gRPC with RFC-33 compliant error handling and contract distribution.

## Skill Structure

```
grpc-migration/
├── Skill.md                                    # Orchestrator - coordinates workflow
├── README.md                                   # This file
├── resources/
│   └── SHARED_TEMPLATES.md                     # PR/commit message templates
└── skills/
    ├── assessment/
    │   └── Skill.md                            # Initial compliance assessment
    ├── contract-distribution/
    │   ├── Skill.md                            # Proto module migration
    │   └── resources/
    │       └── BUILD_EXAMPLES.md               # Gradle configuration examples
    └── error-handling/
        ├── Skill.md                            # V2 service migration
        └── resources/
            └── ERROR_PATTERNS.md               # Error handling patterns
```

## Available Skills

| Skill | Description | Use Case |
|-------|-------------|----------|
| `grpc-migration` | Orchestrator | Full migration workflow |
| `grpc-migration-assessment` | Initial assessment | Identify compliance issues |
| `grpc-migration-contract-distribution` | Proto modules | Split into proto-only/-generated |
| `grpc-migration-error-handling` | Error handling | Create V2 services |

## How to Use

### Full Migration

Use the orchestrator for complete migrations:
```
"Help me with grpc-migration"
```

### Focused Tasks

Use sub-skills for specific tasks:
```
"Help me with grpc-migration-assessment"
"Help me with grpc-migration-contract-distribution"
"Help me with grpc-migration-error-handling"
```

## Migration Workflow

```
1. ASSESSMENT
   grpc-migration-assessment
   ↓
2. CONTRACT DISTRIBUTION
   grpc-migration-contract-distribution
   ↓
3. ERROR HANDLING
   grpc-migration-error-handling
   ↓
4. VALIDATION
   Run compliance checks and tests
```

## Files Overview

### Skill.md (Orchestrator)
- Workflow coordination
- Sub-skill references
- Benefits and best practices
- Performance considerations

### skills/assessment/Skill.md
- Running compliance validation
- Categorizing ERRORS vs WARNINGS
- Creating migration plans
- Validation command reference

### skills/contract-distribution/Skill.md
- Proto file location requirements
- Module splitting (proto-only + generated)
- Migration steps and checklist
- Common pitfalls

### skills/contract-distribution/resources/BUILD_EXAMPLES.md
- Proto-only module configuration
- Generated module configuration
- Service module configuration
- External consumer setup

### skills/error-handling/Skill.md
- V2 service creation
- Standard gRPC error codes
- Migration steps and checklist
- Consumer migration guide

### skills/error-handling/resources/ERROR_PATTERNS.md
- Pattern 1: google.rpc.Status with details
- Pattern 2: Metadata-based errors
- Pattern 3: FailureHelper utility
- Client-side error extraction

### resources/SHARED_TEMPLATES.md
- Commit message templates
- PR description template
- Assessment-only commit
- Focused migration commits

## Key Principles

1. **⚠️ NEVER rename existing services** - original service name is immutable (never add V1 suffix retroactively)
2. **Create V2 services** (not V2 methods) for cleaner separation
3. **Proto files follow package structure** in `src/main/resources/{package/path}/`
4. **Always bump MAJOR version** for breaking changes
5. **Use standard gRPC Status codes** (INTERNAL, UNKNOWN, FAILED_PRECONDITION)
6. **Compile protos before implementing handlers** to get generated stubs
7. **Only publish proto-only modules**, never `-generated` modules

## Dependencies

- `grpc-compliance-validate-repository` tool (for validation)
- Gradle build system
- Java 21+
- gRPC and Protocol Buffers libraries

## Related Guidelines

- `.claude/java-grpc-services.md` - RFC-33 compliant gRPC service patterns
- `.claude/java-grpc-resilience.md` - gRPC resilience patterns
- RFC-33: gRPC Service Standards
- RFC-34: Structured Logging

## Updating This Skill

To update the skill:

1. **Orchestrator changes**: Edit `Skill.md`
2. **Assessment changes**: Edit `skills/assessment/Skill.md`
3. **Contract distribution**: Edit `skills/contract-distribution/Skill.md` or `resources/BUILD_EXAMPLES.md`
4. **Error handling**: Edit `skills/error-handling/Skill.md` or `resources/ERROR_PATTERNS.md`
5. **Templates**: Edit `resources/SHARED_TEMPLATES.md`

Test changes by invoking the relevant skill in a Claude Code session.

---

Created following the [Claude Custom Skills Guide](https://support.claude.com/en/articles/12512198-how-to-create-custom-skills)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-migration/README.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

