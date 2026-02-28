# Container Reuse (Local Development)

Speed up local test runs by reusing containers.

## Setup

### 1. Create ~/.testcontainers.properties

```properties
testcontainers.reuse.enable=true
```

### 2. Add .withReuse(true)

```groovy
POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.1-alpine")
        .withDatabaseName("{service}_test")
        .withReuse(true)  // Reuse across test runs
```

## Important

**WARNING**: Do NOT enable in CI/CD - containers must be fresh for reproducibility.

## How It Works

- First run: Container starts normally
- Subsequent runs: Reuses existing container if matching config
- Container persists after JVM exit
- Manual cleanup: `docker rm` to remove reused containers

## Benefits

- Faster test startup (skip container initialization)
- Consistent test data (if not cleaning between runs)
- Reduced Docker overhead

## Caveats

- Only works locally (not in CI)
- Container state persists (may need manual cleanup)
- Config changes require container restart
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/container-reuse.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

