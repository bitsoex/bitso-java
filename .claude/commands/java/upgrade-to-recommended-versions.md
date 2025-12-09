# Upgrade Java services to recommended versions (Spring Boot 3.5.8, Gradle 8.14.3+, Java 21)

**Description:** Upgrade Java services to recommended versions (Spring Boot 3.5.8, Gradle 8.14.3+, Java 21)

# 🤖 📦 Upgrade to Recommended Versions

**URGENT**: Spring Boot 3.4.x reaches end-of-life by end of 2025. All projects should be upgraded to Spring Boot 3.5.8 before the code freeze.

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking the user for confirmation. Only stop if there is an unrecoverable error.

## Related Rules (Read First)

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first**
- **GitHub PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md` - Draft PR, CI monitoring, CodeRabbit
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Use `-x codeCoverageReport` for faster tests

## Target Recommended Versions (Priority Order)

### Priority 1: CRITICAL - Spring Boot 3.5.x Ecosystem

These upgrades are **blocking** - Spring Boot 3.4.x EOL is imminent.

| Component | Target Version | Notes |
|-----------|----------------|-------|
| **Spring Boot** | **3.5.8** | CRITICAL - 3.4.x EOL end of 2025 |
| **bitso-rds-iam-authn** | **2.0.0** | REQUIRED if using RDS IAM - Hikari 6 compatibility |
| **bitso-commons-redis** | **4.2.1** | REQUIRED if using Redis - Jedis 4 compatibility |

### Priority 2: HIGH - Build Infrastructure (Endurance Plugins)

Keep build plugins up to date for stability and security.

| Component | Target Version | Notes |
|-----------|----------------|-------|
| **Gradle** | **8.14.3+** | Build tool |
| **Develocity Plugin** | **0.2.8** | Build insights (`com.bitso.endurance:develocity`) |
| **Publish Plugin** | **0.3.6** | Publishing (`bitso.endurance:publish`) |
| **Service Plugin** | **0.2.4** | Service setup (`bitso.endurance:service`) |
| **Endurance Plugin** | **0.2.9** | Core (`com.bitso.endurance:endurance`) |
| **Java Module Plugin** | **0.4.5** | Module setup (`bitso.endurance:java-module`) |
| **FlyJooq Plugin** | **0.1.5** | Flyway/JOOQ (`com.bitso.endurance:flyjooq`) |
| **SonarQube Plugin** | **7.2.0.6526** | Code analysis (`org.sonarqube`) |

### Priority 3: MEDIUM - Security & Compatibility

| Component | Target Version | Notes |
|-----------|----------------|-------|
| **Java** | **21** | LTS version |
| **gRPC** | **1.76.0+** | Netty security fixes |
| **Protobuf** | **4.33.0+** | Wire format compatible |
| **commons-lang3** | **3.18.0+** | CVE-2025-48924 |
| **commons-beanutils** | **1.11.0** | Security fixes |

### Priority 4: LOW - Vulnerability Patches (Only if Present)

These are applied **only if the vulnerable dependency exists** in the project.

| Component | Target Version | Notes |
|-----------|----------------|-------|
| `lz4-java` | `at.yawk.lz4:1.10.1` | GAV substitution - only if lz4-java is in dependency tree |

## Behavior Guidelines

- **Never exit early** - Always continue until all upgrades are complete
- **Be proactive** - If something is missing, create it; if on wrong branch, switch
- **Validate locally** - Build and test must pass before pushing
- **Verify no downgrades** - Use dependency graph to ensure no newer versions are replaced with older

## Workflow

### 1. Create Jira Ticket (REQUIRED FIRST STEP)

**Before any code changes**, search for existing tickets then create if none found.

Use `mcp_atlassian_searchJiraIssuesUsingJql`:

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "Spring Boot 3.5" ORDER BY created DESC
```

If no existing ticket, use `mcp_atlassian_createJiraIssue`:

- **Summary**: `🤖 📦 Upgrade [repo-name] to Spring Boot 3.5.8`
- **Parent**: Current Sprint/Cycle KTLO Epic

### 2. Ensure Feature Branch with Jira Key

```bash
CURRENT_BRANCH=$(git branch --show-current)
# JIRA_KEY is the actual ticket key from Step 1 (e.g., PROJ-123)

if [ "$CURRENT_BRANCH" = "main" ]; then
    git stash --include-untracked 2>/dev/null || true
    git fetch --all
    git pull origin main
    BRANCH_NAME="chore/${JIRA_KEY}-upgrade-spring-boot-3.5.8"
    git checkout -b "$BRANCH_NAME"
fi
```

### 3. Detect Current State

```bash
# Current Spring Boot version
grep -E "spring-boot\s*=" gradle/libs.versions.toml 2>/dev/null || \
grep -E "org.springframework.boot" build.gradle

# Current Gradle version
cat gradle/wrapper/gradle-wrapper.properties | grep distributionUrl

# Check for bitso-rds-iam-authn (CRITICAL for Spring Boot 3.5.x)
grep -rE "rds-iam-authn|RdsIamDataSource" . --include="*.gradle" --include="*.toml" 2>/dev/null

# Check for commons-redis (CRITICAL for Spring Boot 3.5.x)
grep -rE "com\.bitso\.commons.*redis|bitso-commons-redis" . --include="*.gradle" --include="*.toml" 2>/dev/null

# Check for Endurance plugins
grep -E "bitso\.endurance|com\.bitso\.endurance" settings.gradle build.gradle 2>/dev/null

# Check for SonarQube plugin
grep -E "org\.sonarqube" build.gradle 2>/dev/null

# Check for lz4-java in dependency tree (only patch if present)
./gradlew dependencies --configuration runtimeClasspath 2>/dev/null | grep -i "lz4-java" || echo "lz4-java not in dependency tree"
```

### 4. Update Version Catalog (`gradle/libs.versions.toml`)

**IMPORTANT**: Only add versions for dependencies that exist in the project.

```toml
[versions]
# Priority 1: CRITICAL - Spring Boot ecosystem
java = "21"
spring-boot = "3.5.8"

# Priority 2: HIGH - Build infrastructure
gradle = "8.14.3"
sonarqube = "7.2.0.6526"

# Priority 3: MEDIUM - Security (if dependencies exist)
grpc = "1.76.0"
protobuf = "4.33.0"
commons-lang3 = "3.18.0"

# Bitso internal libraries (ONLY if used in project)
# bitso-rds-iam-authn = "2.0.0"    # Uncomment if using RDS IAM auth
# bitso-commons-redis = "4.2.1"    # Uncomment if using Redis

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
sonarqube = { id = "org.sonarqube", version.ref = "sonarqube" }
```

### 5. Update Endurance Plugins (`settings.gradle`)

If using Endurance plugins, ensure latest versions:

```groovy
pluginManagement {
    plugins {
        // Endurance plugins - use latest versions
        id 'com.bitso.endurance.develocity' version '0.2.8'
        id 'bitso.endurance.publish' version '0.3.6'
        id 'bitso.endurance.service' version '0.2.4'
        id 'com.bitso.endurance.endurance' version '0.2.9'
        id 'bitso.endurance.java-module' version '0.4.5'
        id 'com.bitso.endurance.flyjooq' version '0.1.5'
    }
}
```

### 6. Update Gradle Wrapper

```bash
./gradlew wrapper --gradle-version=8.14.3
cat gradle/wrapper/gradle-wrapper.properties | grep distributionUrl
```

### 7. Update bitso-rds-iam-authn (CRITICAL if using RDS IAM)

**Why**: Spring Boot 3.5.x uses Hikari 6. The library PR [jvm-generic-libraries#651](https://github.com/bitsoex/jvm-generic-libraries/pull/651) explains:

> Hikari 6 gets the password from a Credentials object, instead of directly, so we need to override `getCredentials()`.

```toml
# gradle/libs.versions.toml
[versions]
bitso-rds-iam-authn = "2.0.0"

[libraries]
bitso-rds-iam-authn = { module = "com.bitso.aws.rds-iam-authn:spring-boot", version.ref = "bitso-rds-iam-authn" }
```

### 8. Update bitso-commons-redis (CRITICAL if using Redis)

**Why**: Spring Boot 3.5.x upgrades Jedis to version 4.

```toml
# gradle/libs.versions.toml
[versions]
bitso-commons-redis = "4.2.1"

[libraries]
bitso-commons-redis = { module = "com.bitso.commons:redis", version.ref = "bitso-commons-redis" }
```

### 9. Apply lz4-java Substitution (ONLY if in dependency tree)

**IMPORTANT**: Only apply this if lz4-java appears in the dependency tree. Check first:

```bash
./gradlew dependencies --configuration runtimeClasspath | grep -i "lz4-java"
```

If present, add substitution in root `build.gradle`:

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute module("org.lz4:lz4-java")
                using module("at.yawk.lz4:lz4-java:1.10.1")
                because "org.lz4:lz4-java is discontinued"
        }
    }
}
```

### 10. CRITICAL: Verify No Version Downgrades

**After all version updates**, verify that no force/constraint/substitution is replacing a newer version with an older one.

```bash
# Generate dependency graph and check for version conflicts
./gradlew dependencies --configuration runtimeClasspath > /tmp/deps-before.txt 2>&1

# Look for version conflicts (-> indicates resolution)
grep -E "\->" /tmp/deps-before.txt | head -50

# Check for specific libraries being downgraded
# Pattern: "X.Y.Z -> A.B.C" where A.B.C < X.Y.Z indicates a downgrade
./gradlew dependencyInsight --configuration runtimeClasspath --dependency spring-boot 2>&1 | head -30
./gradlew dependencyInsight --configuration runtimeClasspath --dependency hikari 2>&1 | head -30
./gradlew dependencyInsight --configuration runtimeClasspath --dependency jedis 2>&1 | head -30

# Check for force rules that might cause downgrades
grep -rE "force\s*\(" build.gradle */build.gradle 2>/dev/null
grep -rE "resolutionStrategy" build.gradle */build.gradle 2>/dev/null

# Check constraints that might pin old versions
grep -rE "constraints\s*\{" build.gradle */build.gradle 2>/dev/null
```

**If any force/constraint pins an older version, REMOVE IT:**

```groovy
// ❌ REMOVE: Force rules that pin older versions
configurations.configureEach {
    resolutionStrategy {
        // REMOVE lines like:
        // force 'com.zaxxer:HikariCP:5.0.1'  // <-- Remove if upgrading to Spring Boot 3.5.x (uses Hikari 6)
        // force 'redis.clients:jedis:4.3.0'  // <-- Remove if upgrading to Spring Boot 3.5.x (uses Jedis 5)
    }
}
```

### 11. Validate Build and Tests Locally

```bash
# Build validation
if ! ./gradlew clean build -x codeCoverageReport 2>&1 | tee /tmp/build.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Build failed"
    grep -E "FAILED|Error|Exception" /tmp/build.log | head -30
    exit 1
fi
echo "✅ Build successful"

# Test validation
if ! ./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Tests failed"
    grep -B 5 -A 20 "FAILED" /tmp/test.log
    exit 1
fi
echo "✅ All tests passed"
```

### 12. Final Dependency Graph Verification

**After successful build**, verify the final dependency versions are correct:

```bash
# Verify Spring Boot version
./gradlew dependencyInsight --dependency org.springframework.boot:spring-boot --configuration runtimeClasspath 2>&1 | grep -E "3\.5\.8"

# Verify Hikari version (should be 6.x for Spring Boot 3.5.x)
./gradlew dependencyInsight --dependency com.zaxxer:HikariCP --configuration runtimeClasspath 2>&1 | grep -E "version"

# Verify no lz4-java from org.lz4 (if substitution was applied)
./gradlew dependencies --configuration runtimeClasspath 2>&1 | grep -E "org\.lz4:lz4-java" && echo "⚠️ org.lz4:lz4-java still present!" || echo "✅ lz4-java substitution verified"

# Generate final dependency report
./gradlew dependencies --configuration runtimeClasspath > /tmp/deps-final.txt 2>&1
echo "Final dependency tree saved to /tmp/deps-final.txt"
```

### 13. Commit Changes

```bash
git add -A
git commit -m "🤖 📦 chore(deps): [$JIRA_KEY] upgrade to Spring Boot 3.5.8

Upgrades:
- Spring Boot: X.X.X -> 3.5.8
- Gradle: X.X.X -> 8.14.3
- Endurance plugins to latest versions
- SonarQube plugin: X.X.X -> 7.2.0.6526
- [Add other upgrades made]

Side-by-side upgrades for Spring Boot 3.5.x:
- bitso-rds-iam-authn: X.X -> 2.0.0 (Hikari 6 compatibility)
- bitso-commons-redis: X.X -> 4.2.1 (Jedis 4 compatibility)

Verified:
- No version downgrades via dependency graph analysis
- Build and tests pass locally

Generated with the Quality Agent by the /upgrade-to-recommended-versions command."
```

### 14. Push and Create Draft PR

```bash
git push -u origin $(git branch --show-current)

CURRENT_USER=$(gh api user --jq '.login')
PR_URL=$(gh pr create --draft \
    --title "🤖 📦 [$JIRA_KEY] chore(deps): upgrade to Spring Boot 3.5.8" \
    --body "## 🤖 AI-Assisted Dependency Upgrade

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Summary
Upgrades to recommended versions before code freeze. Spring Boot 3.4.x reaches EOL by end of 2025.

## Version Changes

| Component | From | To |
|-----------|------|-----|
| Spring Boot | X.X.X | 3.5.8 |
| Gradle | X.X.X | 8.14.3 |
| Endurance plugins | various | latest |
| SonarQube | X.X.X | 7.2.0.6526 |

## Side-by-Side Upgrades (Spring Boot 3.5.x)
- bitso-rds-iam-authn: 2.0.0 (Hikari 6)
- bitso-commons-redis: 4.2.1 (Jedis 4)

## Verification
- [x] No version downgrades (dependency graph verified)
- [x] Build passes locally
- [x] Tests pass locally
- [ ] Build CI passes
- [ ] Signadot CI passes

Generated with the Quality Agent by the /upgrade-to-recommended-versions command." \
    --repo $(gh repo view --json nameWithOwner -q '.nameWithOwner') 2>&1)

PR_NUMBER=$(echo "$PR_URL" | grep -oE '[0-9]+$')
gh pr edit $PR_NUMBER --add-assignee "$CURRENT_USER"
```

### 15. Monitor CI and Finalize

```bash
PR_NUMBER=$(gh pr view --json number -q '.number')
REPO=$(gh repo view --json nameWithOwner -q '.nameWithOwner')

# Wait for CI
MAX_ITERATIONS=24
ITERATION=0
while [ $ITERATION -lt $MAX_ITERATIONS ]; do
    ITERATION=$((ITERATION + 1))
    BUILD_STATUS=$(gh pr checks $PR_NUMBER --repo $REPO --json name,bucket \
        --jq '.[] | select(.name | test("build|Build"; "i")) | .bucket' 2>/dev/null | head -1 || echo "pending")
    
    echo "[$ITERATION/$MAX_ITERATIONS] Build: $BUILD_STATUS"
    
    if [ "$BUILD_STATUS" = "pass" ]; then
        echo "✅ CI passed!"
        gh pr ready $PR_NUMBER
        break
    elif [ "$BUILD_STATUS" = "fail" ]; then
        echo "❌ CI failed"
        gh pr checks $PR_NUMBER --repo $REPO
        break
    fi
    sleep 50
done
```

## Troubleshooting

### RDS IAM authentication fails after upgrade

**Cause**: Hikari 6 changed credential handling.

**Fix**: Upgrade `bitso-rds-iam-authn` to 2.0.0. See [PR #651](https://github.com/bitsoex/jvm-generic-libraries/pull/651).

### Redis connection fails after upgrade

**Cause**: Jedis 4 API changes.

**Fix**: Upgrade `bitso-commons-redis` to 4.2.1.

### Dependency version conflict / downgrade detected

**Cause**: Existing `force` or `constraint` rules pinning older versions.

**Fix**:

1. Search for force rules: `grep -rE "force\s*\(" build.gradle`
2. Remove any that pin versions older than what Spring Boot 3.5.x provides
3. Re-run dependency graph verification

### Build fails with "Could not resolve dependency"

**Fix**:

1. Check `gradle/libs.versions.toml` has the library
2. Verify Bitso Maven repository is configured in `settings.gradle`

## Reference PRs

- [jvm-generic-libraries#651](https://github.com/bitsoex/jvm-generic-libraries/pull/651) - rds-iam-authn Hikari 6 fix (explains the `getCredentials()` change)
- [hubble#1170](https://github.com/bitsoex/hubble/pull/1170) - Spring Boot 3.5.8 upgrade example
- [endurance#486](https://github.com/bitsoex/endurance/pull/486) - Develocity plugin upgrade

## Related

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md`
- **PR Lifecycle**: `global/rules/github-cli-pr-lifecycle.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md`
