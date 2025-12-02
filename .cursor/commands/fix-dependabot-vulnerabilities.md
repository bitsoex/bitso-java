# Fix Dependabot security vulnerabilities in Java/Gradle projects

Fix Dependabot security vulnerabilities in Java/Gradle projects

# Fix Dependabot Vulnerabilities (Java)

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking the user for confirmation. Only stop if there is an unrecoverable error.

## Related Rules (Read First)

Before applying fixes, understand the project's dependency management approach:

- **Version Management**: `java/rules/java-versions-and-dependencies.md` - BOMs, version catalog, constraints
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md` - Build configuration standards
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Debugging and verification commands

## Behavior Guidelines

- **Never exit early** - Always continue until all vulnerabilities are fixed
- **Be proactive** - If something is missing, create it; if on wrong branch, switch
- **Self-sufficient** - Handle all prerequisites automatically
- **Resilient** - Retry on transient failures, work around blockers

## Critical: Understanding Dependency Verification

**DO NOT use `dependencyInsight` alone** - it only shows one resolution path and can cause false positives. Always verify with the dependency graph plugin.

**The dependency graph plugin reports ALL versions** that appear in ANY configuration to GitHub. Even if Gradle's conflict resolution picks the newer version at runtime, the OLD vulnerable version will still be reported to GitHub and **will fail the dependency-review check**.

Example of what causes failures:

```text
# dependency-list.txt shows BOTH versions
commons-compress:1.23.0   <-- This WILL be reported to GitHub
commons-compress:1.27.1   <-- This too

# dependency-review action will show:
+ org.apache.commons:commons-compress@1.23.0  <-- VULNERABILITY DETECTED
```

**Force rules alone are NOT enough** - they only affect runtime resolution, not what gets reported to GitHub. Use the fix strategy hierarchy below.

## Workflow

### 1. Ensure Feature Branch

**CRITICAL**: If on `main`, automatically create and switch to a feature branch.

```bash
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" = "main" ]; then
    git stash --include-untracked 2>/dev/null || true
    git fetch --all
    git pull origin main
    BRANCH_NAME="fix/dependabot-security-$(date +%Y%m%d-%H%M%S)"
    git checkout -b "$BRANCH_NAME"
    echo "Created branch: $BRANCH_NAME"
fi
```

### 2. Setup Dependency Graph Plugin

Check if `gradle/dependency-graph-init.gradle` exists. If not, create it:

```groovy
/**
 * Init script for GitHub Dependency Graph Gradle Plugin
 * @see https://github.com/gradle/github-dependency-graph-gradle-plugin
 */
initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath "org.gradle:github-dependency-graph-gradle-plugin:1.4.0"
    }
}

apply plugin: org.gradle.dependencygraph.simple.SimpleDependencyGraphPlugin
```

### 3. Get Dependabot Alerts

```bash
REPO=$(gh repo view --json nameWithOwner -q '.nameWithOwner')
gh api repos/$REPO/dependabot/alerts --jq '.[] | select(.state == "open") | {number, severity: .security_advisory.severity, package: .dependency.package.name, patched_version: .security_vulnerability.first_patched_version.identifier, cve: .security_advisory.cve_id}'
```

### 4. Generate Dependency Graph (Before Fix)

```bash
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Check what versions will be reported to GitHub
grep -i "vulnerable-package" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

**If you see multiple versions of the same package, ALL of them will be reported to GitHub.**

### 5. Apply Fix - Use Correct Strategy (Hierarchy)

**Always prefer higher-level solutions.** Use this hierarchy:

#### Document Your Strategy Choice (IMPORTANT)

When applying fixes, always add comments in the code explaining:

1. **Why this strategy was chosen** over alternatives higher in the hierarchy
2. **Why alternatives weren't suitable** (e.g., "BOM doesn't manage this dependency", "Direct dependency, not transitive")
3. **The specific CVE being addressed**

Example comments:

```groovy
// Security fix for CVE-2025-48924 (commons-lang3)
// Strategy: Dependency substitution used because:
// - BOM update (Strategy 1): Spring Boot BOM doesn't manage commons-lang3 directly
// - Version catalog (Strategy 2): Already at 3.18.0, but transitive 3.16.0 still appears
// - Substitution removes old version from dependency graph entirely
substitute(module("org.apache.commons:commons-lang3"))
    .using(module("org.apache.commons:commons-lang3:3.18.0"))
    .because("Security fix for CVE-2025-48924")
```

This documentation helps future maintainers understand the reasoning and makes it easier to clean up when BOMs are updated.

#### Strategy 1: Update BOM/Platform Version (PREFERRED)

If the vulnerable dependency is managed by a BOM (Spring Boot, gRPC, Protobuf, etc.), update the BOM version first. This is the cleanest solution.

```toml
# gradle/libs.versions.toml
[versions]
spring-boot = "3.5.8"    # Includes patched tomcat, jackson, logback
grpc = "1.76.0"          # Includes patched netty, protobuf
protobuf = "4.33.0"      # Patched protobuf-java
```

Check what BOMs manage in `build.gradle`:

```groovy
// These BOMs manage transitive dependencies automatically
allprojects {
    dependencies {
        constraints {
            implementation platform(libs.spring.boot.dependencies)
            implementation platform(libs.grpc.bom)
            implementation platform(libs.protobuf.bom)
        }
    }
}
```

**When to use**: Vulnerable package is a transitive of Spring Boot, gRPC, Protobuf, or other BOM-managed framework.

#### Strategy 2: Update Direct Dependency in Version Catalog

If it's a direct dependency (not transitive), update in `gradle/libs.versions.toml`:

```toml
[versions]
# Security fix for CVE-XXXX-XXXXX
commons-lang3 = "3.18.0"    # Updated from 3.14.0
commons-compress = "1.27.1" # Updated from 1.26.0
```

**When to use**: The vulnerable package is declared directly in a module's `build.gradle`.

#### Strategy 3: Dependency Substitution (For Transitive Not in BOM)

When the vulnerable dependency is transitive but NOT managed by any BOM:

```groovy
// In root build.gradle - applies to ALL configurations
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // Substitute old version with new - removes old from graph entirely
            substitute module("org.apache.commons:commons-compress")
                using module("org.apache.commons:commons-compress:1.27.1")
                because "Security fix for CVE-2024-25710, CVE-2024-26308"

            // For artifact replacement (e.g., jdk15on -> jdk18on)
            substitute module("org.bouncycastle:bcprov-jdk15on")
                using module("org.bouncycastle:bcprov-jdk18on:1.81")
                because "Security fix - migrate to jdk18on"
        }
    }
}
```

**When to use**: Transitive dependency not covered by any BOM, or when you need to replace one artifact with another (different group/artifact).

#### Strategy 4: Dependency Constraints

For enforcing minimum versions across all configurations:

```groovy
// In root build.gradle
allprojects {
    dependencies {
        constraints {
            // Enforce minimum version for security
            implementation("org.apache.commons:commons-compress:1.27.1") {
                because "Security fix for CVE-2024-25710"
            }
            implementation("org.apache.commons:commons-lang3:3.18.0") {
                because "Security fix for CVE-2025-48924"
            }
        }
    }
}
```

**When to use**: When you want to set a floor version without forcing an exact version.

#### Strategy 5: Force Rules (Use With Caution)

Force rules affect runtime resolution but may not remove old versions from the dependency graph:

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy {
            // Force specific versions - use with substitution for complete fix
            force libs.commons.lang3
            force libs.commons.compress
        }
    }
}
```

**⚠️ Warning**: Force rules alone may NOT be enough for dependency-review to pass. Combine with substitution if old versions still appear in the dependency graph.

#### Strategy 6: Exclude + Add (Last Resort)

When nothing else works:

```groovy
configurations.configureEach {
    exclude group: "org.apache.commons", module: "commons-compress"
}

dependencies {
    implementation "org.apache.commons:commons-compress:1.27.1"
}
```

**When to use**: Only when substitution doesn't work or there are complex resolution conflicts.

### 6. Validate Build and Tests

**CRITICAL**: Build and test failures are unrecoverable errors. Stop execution if they fail.

```bash
# Validate build succeeds - halt on failure
if ! ./gradlew clean build 2>&1 | tee /tmp/build.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Build failed - cannot proceed"
    grep -E "FAILED|Error|Exception" /tmp/build.log | head -20
    exit 1
fi
echo "✅ Build successful"

# Validate tests pass - halt on failure
if ! ./gradlew test 2>&1 | tee /tmp/test.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Tests failed - cannot proceed"
    grep -E "FAILED|Error" /tmp/test.log | head -20
    exit 1
fi
echo "✅ All tests passed"
```

### 7. Verify Fix with Dependency Graph (CRITICAL)

**This determines what GitHub will see. Old versions here = dependency-review failure.**

```bash
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# CRITICAL: Verify ONLY patched versions appear
grep -i "commons-compress" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
grep -i "commons-lang3" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

**Expected output (GOOD):**

```text
org.apache.commons:commons-compress:1.27.1
```

**Bad output (WILL FAIL dependency-review):**

```text
org.apache.commons:commons-compress:1.23.0   <-- OLD VERSION STILL PRESENT
org.apache.commons:commons-compress:1.27.1
```

If old versions still appear:

1. Check which configuration brings them: `grep -B5 "commons-compress:1.23" build/reports/dependency-graph-snapshots/dependency-graph.json`
2. Add more specific substitution rules or excludes
3. Regenerate and verify again

### 8. Commit Changes

Include the fix strategy reasoning in the commit message:

```bash
git add -A
git commit -m "fix(security): resolve Dependabot vulnerabilities

Fixes:
- [package]: X.X.X -> Y.Y.Y (CVE-XXXX-XXXXX)

Strategy used: [BOM update | Version catalog | Substitution | etc.]
Reason: [Why this strategy was chosen over higher-priority alternatives]

Example:
- commons-lang3: Substitution used because BOM doesn't manage this dependency
  and version catalog update alone didn't remove transitive 3.16.0

Verified via dependency graph - only patched versions reported"
```

### 9. Push and Create Draft PR

#### PR Description Accuracy (CRITICAL)

Only include information you have **verified**. Do not guess or assume:

- **CVE numbers**: Only include CVEs you confirmed from Dependabot alerts (step 3)
- **Old versions**: Only include versions you confirmed exist in the dependency graph (step 4)
- **New versions**: Only include versions you confirmed are being used after the fix (step 7)

```bash
git push -u origin $(git branch --show-current)
```

Create PR with **only verified information** and **strategy reasoning**:

```bash
gh pr create --draft \
    --title "fix(security): resolve Dependabot vulnerabilities" \
    --body "## Security Fixes

| Package | Old Version | New Version | CVE | Severity |
|---------|-------------|-------------|-----|----------|
| [PACKAGE] | [OLD_VERSION] | [NEW_VERSION] | [CVE_ID] | [SEVERITY] |

## Fix Strategy & Reasoning

| Package | Strategy Used | Why This Strategy |
|---------|---------------|-------------------|
| [PACKAGE] | [Strategy N] | [Why alternatives weren't suitable] |

**Example:**
| Package | Strategy Used | Why This Strategy |
|---------|---------------|-------------------|
| logback-core | Version catalog (Strategy 2) | Direct dependency managed by Spring Boot BOM - updated BOM version |
| tomcat-embed-core | Version catalog (Strategy 2) | Constrained via gradle.properties - Spring Boot BOM will include in next version |
| commons-lang3 | Substitution (Strategy 3) | BOM doesn't manage this; version catalog alone didn't remove transitive 3.16.0 |

## Validation
- [x] Build passes locally
- [x] Tests pass locally
- [x] Verified via dependency graph - ONLY patched versions appear
- [ ] dependency-review check passes

## References
- Dependabot alerts: [link to security/dependabot]"
```

**DO NOT include in PR body:**

- CVE numbers you haven't verified from Dependabot alerts
- Version numbers you haven't confirmed from dependency graph output
- References to PRs or issues that don't exist
- Assumptions about which packages are affected

### 10. Monitor CI - Check dependency-review Output

**Note**: Do not wait indefinitely. Use a timeout to prevent blocking.

```bash
PR_NUMBER=$(gh pr view --json number -q '.number')
REPO=$(gh repo view --json nameWithOwner -q '.nameWithOwner')

# Poll for dependency-review status with timeout (max 20 minutes)
MAX_ITERATIONS=24  # 24 iterations × 50 seconds = 20 minutes
ITERATION=0

echo "⏳ Waiting for CI checks to complete..."
while [ $ITERATION -lt $MAX_ITERATIONS ]; do
    ITERATION=$((ITERATION + 1))
    
    # Get dependency-review check status
    DEPENDENCY_STATUS=$(gh pr checks $PR_NUMBER --repo $REPO --json name,bucket \
        --jq '.[] | select(.name | test("dependency"; "i")) | .bucket' 2>/dev/null || echo "pending")
    
    if [ "$DEPENDENCY_STATUS" = "pass" ]; then
        echo "✅ dependency-review passed!"
        break
    elif [ "$DEPENDENCY_STATUS" = "fail" ]; then
        echo "❌ dependency-review failed - check output below"
        gh run view --repo $REPO --json jobs --jq '.jobs[] | select(.name | test("dependency"; "i"))'
        break
    fi
    
    echo "  [$ITERATION/$MAX_ITERATIONS] Status: $DEPENDENCY_STATUS - waiting 50s..."
    sleep 50
done

if [ $ITERATION -ge $MAX_ITERATIONS ] && [ "$DEPENDENCY_STATUS" != "pass" ]; then
    echo "⚠️  CI checks did not complete after $MAX_ITERATIONS iterations"
    echo "Current status: $DEPENDENCY_STATUS"
    echo "Check manually: gh pr checks $PR_NUMBER --repo $REPO"
fi
```

**Expected CI Results:**

| Check | Expected | Notes |
|-------|----------|-------|
| `dependency-review` | ✅ SUCCESS | **Must pass** - if fails, old versions still in graph |
| `Build` | ✅ SUCCESS | Must pass |
| `Stage Readiness` | ❌ FAILURE | Expected - manual intervention after review |

### 11. If dependency-review Fails

Check the action output for lines like:

```text
+ org.apache.commons:commons-compress@1.23.0  <-- This is the problem
```

This means the old version is still being reported. Go back to step 5 and:

1. Use `dependencySubstitution` instead of `force`
2. Add `exclude` rules for the old versions
3. Regenerate dependency graph and verify

### 12. Finalize PR

Once `dependency-review` passes:

```bash
gh pr ready $PR_NUMBER
```

## Fix Strategy Comparison

| Strategy | When to Use | Runtime | Dependency Graph | Complexity |
|----------|-------------|---------|------------------|------------|
| **BOM Update** | Transitive managed by BOM | ✅ | ✅ | Low |
| **Version Catalog** | Direct dependency | ✅ | ✅ | Low |
| **Substitution** | Transitive not in BOM | ✅ | ✅ | Medium |
| **Constraints** | Enforce minimum version | ✅ | ⚠️ May vary | Medium |
| **Force** | Quick fix (combine with above) | ✅ | ❌ May show both | Low |
| **Exclude + Add** | Last resort | ✅ | ✅ | High |

**Key insight**: BOM and version catalog updates are cleanest. Substitution removes old versions completely. Force alone may not fix dependency-review failures.

## Troubleshooting

### Old versions still appear after substitution

Check if the rule is in `allprojects` block:

```groovy
allprojects {  // MUST be here, not just in root
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Buildscript dependencies

Buildscript classpath needs separate handling:

```groovy
buildscript {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution { ... }
    }
}
```

### Settings.gradle dependencies

If `settings.gradle` shows in dependency-review, add to `settings.gradle`:

```groovy
dependencyResolutionManagement {
    components {
        // Substitution rules here
    }
}
```

## Related

- **Plugin Docs**: [GitHub Dependency Graph Gradle Plugin](https://github.com/gradle/github-dependency-graph-gradle-plugin)
- **Dependabot Security Rule**: `java/rules/java-dependabot-security.md`
- **Version Management**: `java/rules/java-versions-and-dependencies.md`
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md`
- **Gradle Commands**: `java/rules/java-gradle-commands.md`
