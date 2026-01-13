# Migrate from AWS SDK v1 (com.amazonaws) to AWS SDK v2 (software.amazon.awssdk) in Java/Gradle projects

> Migrate from AWS SDK v1 (com.amazonaws) to AWS SDK v2 (software.amazon.awssdk) in Java/Gradle projects

# 🤖 ☁️ Upgrade to AWS SDK v2

**IMPORTANT**: This command is fully autonomous. Complete all steps without asking the user for confirmation. Only stop if there is an unrecoverable error.

## Context

AWS SDK for Java v1 (`com.amazonaws`) is deprecated and reaches end-of-life. All projects must migrate to AWS SDK for Java v2 (`software.amazon.awssdk`). This command handles the migration autonomously.

## Related Rules (Read First)

Before applying fixes, understand the project's dependency management approach:

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - **MUST search for existing tickets first, then create if none found**
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - BOMs, version catalog, constraints
- **Gradle Best Practices**: `java/rules/java-gradle-best-practices.md` - Build configuration standards
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Debugging and verification commands

## Behavior Guidelines

- **Never exit early** - Always continue until migration is complete or blocked
- **Be proactive** - If something is missing, create it; if on wrong branch, switch
- **Self-sufficient** - Handle all prerequisites automatically
- **Resilient** - Retry on transient failures, work around blockers
- **Iterative** - Build and test after each change, fix issues before proceeding

## Migration Strategy (Hierarchy)

**Always prefer higher-level solutions. Follow this order:**

| Priority | Strategy | When to Use |
|----------|----------|-------------|
| 1 | **Update library that brings v1** | Preferred - a newer version of the library uses v2 |
| 2 | **Update BOM version** | If v1 comes from Spring Boot or other BOM-managed deps |
| 3 | **Dependency substitution** | Replace v1 artifact with v2 equivalent |
| 4 | **Direct code migration** | Only if no library update available |

## Workflow

### 1. Discover User's Jira Project Key (REQUIRED FIRST STEP)

**DO NOT use hardcoded project keys.** Discover the user's project key dynamically before any ticket operations.

#### Method 1: Query User's Recent Tickets (Preferred)

Use `mcp_atlassian_searchJiraIssuesUsingJql` to find the user's recent tickets:

```text
reporter = currentUser() ORDER BY created DESC
```

Or by assignee:

```text
assignee = currentUser() ORDER BY updated DESC
```

Parse the returned ticket keys to extract the project key prefix (the part before the hyphen and number).

#### Method 2: Check User's Recent Merged PRs

```bash
# Get recent merged PRs by current user
gh pr list --author @me --state merged --limit 5 --json title,body

# Look for Jira key patterns in titles like:
# - [PROJ-123] description
# - PROJ-123 - description
# - https://bitsomx.atlassian.net/browse/PROJ-123
```

Extract the project key prefix from any found ticket references.

#### Method 3: Check Current Repository's Recent PRs

```bash
# Get recent PRs in the repo
gh pr list --state merged --limit 10 --json title,body

# Extract Jira keys from titles/bodies
# Common patterns: [PROJ-XXX], PROJ-XXX, /browse/PROJ-XXX
```

#### Method 4: Ask the User (Fallback)

If discovery fails, ask the user:

```text
I couldn't automatically determine your Jira project key.
What is your team's Jira project key?
```

### 2. Search for Existing Ticket or Create One

**Before any code changes**, search for existing ticket or create one.

Use `mcp_atlassian_searchJiraIssuesUsingJql` to search (using discovered PROJECT_KEY):

```text
project = "PROJECT_KEY" AND status NOT IN (Done, Closed, Resolved) AND summary ~ "AWS SDK" AND summary ~ "[repo-name]" ORDER BY created DESC
```

If no ticket exists, find the current KTLO epic:

```text
project = "PROJECT_KEY" AND issuetype = Epic AND summary ~ "KTLO" ORDER BY created DESC
```

Then create a ticket using `mcp_atlassian_createJiraIssue`:

- **Project**: Discovered project key (e.g., MMFX, GEARBOX, SBP, etc.)
- **Summary**: `🤖 ☁️ Migrate [repo-name] from AWS SDK v1 to v2`
- **Parent**: Current KTLO Epic key (discovered above)
- **Description**: Include AWS SDK v1 imports found in the repo

**See `global/rules/jira-ticket-workflow.md` for detailed ticket creation steps.**

### 3. Ensure Feature Branch with Jira Key

```bash
CURRENT_BRANCH=$(git branch --show-current)
# JIRA_KEY is the actual ticket key from Step 1 (e.g., DX-123)

if [ "$CURRENT_BRANCH" = "main" ]; then
    git stash --include-untracked 2>/dev/null || true
    git fetch --all
    git pull origin main
    BRANCH_NAME="chore/${JIRA_KEY}-migrate-aws-sdk-v2"
    git checkout -b "$BRANCH_NAME"
    echo "Created branch: $BRANCH_NAME"
fi
```

### 4. Identify AWS SDK v1 Usages

Search for all v1 imports in the project:

```bash
# Find all files with v1 imports
grep -r "import com.amazonaws" --include="*.java" . | grep -v "/build/" | sort -u

# Count by file
grep -rl "import com.amazonaws" --include="*.java" . | grep -v "/build/" | wc -l

# Common v1 packages to look for:
# - com.amazonaws.services.s3
# - com.amazonaws.services.sqs
# - com.amazonaws.services.sns
# - com.amazonaws.services.lambda
# - com.amazonaws.auth
# - com.amazonaws.regions
```

### 5. Identify Source of v1 Dependency

**CRITICAL**: Determine if v1 comes from a direct dependency or transitive.

#### A. Setup Dependency Graph Plugin

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

#### B. Find What Brings v1

```bash
# Check dependency tree for v1 SDK
./gradlew dependencies --configuration runtimeClasspath 2>&1 | grep -B5 "com.amazonaws"

# Use dependencyInsight for specific artifact
./gradlew dependencyInsight --dependency com.amazonaws:aws-java-sdk-s3 --configuration runtimeClasspath

# Generate full dependency graph
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Check what versions are reported
grep -i "com.amazonaws" build/reports/dependency-graph-snapshots/dependency-list.txt | sort -u
```

### 6. Apply Fix - Use Correct Strategy

#### Strategy 1: Update Library That Brings v1 (PREFERRED)

If v1 comes from a Bitso internal library, update to a newer version that uses v2:

```toml
# gradle/libs.versions.toml
[versions]
# Check jvm-generic-libraries for latest versions
bitso-commons-aws = "X.Y.Z"  # Update to version using SDK v2
```

**Known Libraries with v2 Support** (check jvm-generic-libraries releases):

| Library | v2-compatible Version | Notes |
|---------|----------------------|-------|
| `bitso-rds-iam-authn` | `2.0.0+` | RDS IAM authentication |
| `bitso-commons-aws` | Check latest | AWS utilities |
| `bitso-commons-s3` | Check latest | S3 operations |
| `bitso-aux-utils` | `5.2.1+` | Auxiliary utilities with AWS SDK v2 |

**Example from successful migration PRs:**

- [jvm-generic-libraries#762](https://github.com/bitsoex/jvm-generic-libraries/pull/762) - Upgrade aws sdk
- [jvm-generic-libraries#758](https://github.com/bitsoex/jvm-generic-libraries/pull/758) - Migrate SQS Client
- [jvm-generic-libraries#738](https://github.com/bitsoex/jvm-generic-libraries/pull/738) - aux utils update

#### Strategy 2: Dependency Substitution

If v1 cannot be removed by updating libraries, substitute v1 with v2:

```groovy
// In root build.gradle
// Version should be in gradle/libs.versions.toml:
// [versions]
// aws-sdk-v2 = "2.40.10"

allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // S3
            substitute module("com.amazonaws:aws-java-sdk-s3")
                using module("software.amazon.awssdk:s3:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
            
            // SQS
            substitute module("com.amazonaws:aws-java-sdk-sqs")
                using module("software.amazon.awssdk:sqs:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
            
            // SNS
            substitute module("com.amazonaws:aws-java-sdk-sns")
                using module("software.amazon.awssdk:sns:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
            
            // Lambda
            substitute module("com.amazonaws:aws-java-sdk-lambda")
                using module("software.amazon.awssdk:lambda:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
            
            // Core
            substitute module("com.amazonaws:aws-java-sdk-core")
                using module("software.amazon.awssdk:aws-core:${libs.versions.aws.sdk.v2.get()}")
                because "Migrate to AWS SDK v2"
        }
    }
}
```

Add to version catalog:

```toml
# gradle/libs.versions.toml
[versions]
aws-sdk-v2 = "2.40.10"

[libraries]
aws-s3 = { module = "software.amazon.awssdk:s3", version.ref = "aws-sdk-v2" }
aws-sqs = { module = "software.amazon.awssdk:sqs", version.ref = "aws-sdk-v2" }
aws-sns = { module = "software.amazon.awssdk:sns", version.ref = "aws-sdk-v2" }
aws-lambda = { module = "software.amazon.awssdk:lambda", version.ref = "aws-sdk-v2" }
```

#### Strategy 3: Direct Code Migration

If code directly uses v1 APIs, migrate to v2. Common patterns:

##### S3 Client Migration

**v1 (OLD):**

```java
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
    .withRegion(region)
    .build();

s3Client.putObject(new PutObjectRequest(bucket, key, file));
```

**v2 (NEW):**

```java
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;

S3Client s3Client = S3Client.builder()
    .region(Region.of(region))
    .build();

s3Client.putObject(PutObjectRequest.builder()
    .bucket(bucket)
    .key(key)
    .build(), 
    RequestBody.fromFile(file));
```

##### SQS Client Migration

**v1 (OLD):**

```java
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().build();
sqsClient.sendMessage(new SendMessageRequest(queueUrl, messageBody));
```

**v2 (NEW):**

```java
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

SqsClient sqsClient = SqsClient.builder().build();
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(queueUrl)
    .messageBody(messageBody)
    .build());
```

##### SNS Client Migration

**v1 (OLD):**

```java
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();
snsClient.publish(new PublishRequest(topicArn, message));
```

**v2 (NEW):**

```java
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

SnsClient snsClient = SnsClient.builder().build();
snsClient.publish(PublishRequest.builder()
    .topicArn(topicArn)
    .message(message)
    .build());
```

##### Lambda Client Migration

**v1 (OLD):**

```java
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;

AWSLambda lambdaClient = AWSLambdaClientBuilder.standard().build();
lambdaClient.invoke(new InvokeRequest()
    .withFunctionName(functionName)
    .withPayload(payload));
```

**v2 (NEW):**

```java
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.core.SdkBytes;

LambdaClient lambdaClient = LambdaClient.builder().build();
lambdaClient.invoke(InvokeRequest.builder()
    .functionName(functionName)
    .payload(SdkBytes.fromUtf8String(payload))
    .build());
```

##### Credential Provider Migration

**v1 (OLD):**

```java
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.AWSCredentialsProvider;
```

**v2 (NEW):**

```java
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
```

### 7. Validate Build and Tests (ITERATE UNTIL SUCCESS)

**CRITICAL**: Build and test after each change. Fix issues before proceeding.

```bash
# Validate build succeeds
if ! ./gradlew clean build -x codeCoverageReport -x test 2>&1 | tee /tmp/build.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Build failed - checking errors"
    grep -E "FAILED|Error|Exception|cannot find symbol|incompatible types" /tmp/build.log | head -30
    # Fix the errors and retry
fi
echo "✅ Build successful"

# Validate tests pass
if ! ./gradlew test -x codeCoverageReport 2>&1 | tee /tmp/test.log | grep -E "BUILD SUCCESSFUL"; then
    echo "❌ Tests failed - checking errors"
    grep -E "FAILED|Error" /tmp/test.log | head -20
    # Fix the errors and retry
fi
echo "✅ All tests passed"
```

### 8. Verify v1 is Completely Removed

```bash
# Check no v1 imports remain in source code
if grep -r "import com.amazonaws" --include="*.java" . | grep -v "/build/" | grep -v "/test/"; then
    echo "❌ v1 imports still present in source code"
    # Continue fixing
fi

# Check dependency graph
./gradlew -I gradle/dependency-graph-init.gradle \
    --dependency-verification=off \
    --no-configuration-cache \
    --no-configure-on-demand \
    :ForceDependencyResolutionPlugin_resolveAllDependencies

# Verify no v1 in dependency graph
if grep -i "com.amazonaws" build/reports/dependency-graph-snapshots/dependency-list.txt | grep -v "^#"; then
    echo "❌ v1 still in dependency graph"
    grep -i "com.amazonaws" build/reports/dependency-graph-snapshots/dependency-list.txt
    # Add more substitution rules
fi

echo "✅ v1 completely removed"
```

### 9. Commit Changes

```bash
# JIRA_KEY is the actual ticket key from Step 1
git add -A
git commit -m "🤖 ☁️ chore(deps): [$JIRA_KEY] migrate from AWS SDK v1 to v2

Migration changes:
- Updated [library] to [version] (uses SDK v2)
- Added dependency substitution for transitive v1 deps
- Updated code imports from com.amazonaws to software.amazon.awssdk
- Updated client initialization patterns

Strategy: [Library update | Substitution | Code migration]

Verified:
- No com.amazonaws imports remain in source
- No v1 dependencies in dependency graph
- All tests pass

Generated with the Quality Agent by the /upgrade-to-aws-sdk-v2 command."
```

### 10. Push and Create PR

```bash
git push -u origin $(git branch --show-current)

# JIRA_KEY from step 1
gh pr create --draft \
    --title "🤖 ☁️ [$JIRA_KEY] chore(deps): migrate from AWS SDK v1 to v2" \
    --body "## 🤖 AI-Assisted AWS SDK Migration

Jira: [$JIRA_KEY](https://bitsomx.atlassian.net/browse/$JIRA_KEY)

## Summary
Migrates this repository from AWS SDK for Java v1 (\`com.amazonaws\`) to v2 (\`software.amazon.awssdk\`).

## Migration Strategy
[Describe which strategy was used: library update, substitution, or code migration]

## Changes

| Component | v1 | v2 |
|-----------|-----|-----|
| S3 Client | com.amazonaws.services.s3 | software.amazon.awssdk.services.s3 |
| SQS Client | com.amazonaws.services.sqs | software.amazon.awssdk.services.sqs |
| [Add others as applicable] |

## Validation
- [x] Build passes locally
- [x] Tests pass locally
- [x] No \`com.amazonaws\` imports in source code
- [x] No v1 dependencies in dependency graph
- [ ] CI checks pass

## AI Agent Details
- **Agent**: Quality Agent
- **Command**: /upgrade-to-aws-sdk-v2

Generated with the Quality Agent by the /upgrade-to-aws-sdk-v2 command.

## References
- [AWS SDK v2 Migration Guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/migration.html)
- Example PRs from other repos:
  - [jvm-generic-libraries#762](https://github.com/bitsoex/jvm-generic-libraries/pull/762)
  - [balance-history#360](https://github.com/bitsoex/balance-history/pull/360)
  - [orders#1065](https://github.com/bitsoex/orders/pull/1065)"
```

### 11. Monitor CI and Finalize

```bash
PR_NUMBER=$(gh pr view --json number -q '.number')

# Wait for CI
echo "⏳ Waiting for CI checks..."
sleep 60

# Check status
gh pr checks $PR_NUMBER

# When ready, mark as ready for review
gh pr ready $PR_NUMBER
```

## Example Migration PRs (Reference)

These PRs successfully migrated from v1 to v2 - use as reference:

| Repository | PR | Title |
|------------|-----|-------|
| jvm-generic-libraries | [#762](https://github.com/bitsoex/jvm-generic-libraries/pull/762) | [MMFX-1580] Upgrade aws sdk |
| jvm-generic-libraries | [#758](https://github.com/bitsoex/jvm-generic-libraries/pull/758) | PIA-3187: Migrate to AWS SDK for Java v2.x - SQS Client |
| card-reconciliation | [#473](https://github.com/bitsoex/card-reconciliation/pull/473) | Migrate aws sdk for card reconciliation services as PoC |
| crypto-fireblocks | [#562](https://github.com/bitsoex/crypto-fireblocks/pull/562) | MMCC-2814 - upgrade aws sdk to v2 |
| balance-history | [#360](https://github.com/bitsoex/balance-history/pull/360) | GEARBOX-4811: migrate aws sdk to v2 |
| orders | [#1065](https://github.com/bitsoex/orders/pull/1065) | AWS Java SDK Migration EOM-2321 EOM-2322 |
| ramps-adapter-bind | [#690](https://github.com/bitsoex/ramps-adapter-bind/pull/690) | MMFR-2446 Migrate to AWS Java SDK V2 |
| user-transaction-history | [#201](https://github.com/bitsoex/user-transaction-history/pull/201) | MMRP-1417-Migrate-to-AWS-SDK-for-Java-v2.x |
| transactions-updater | [#311](https://github.com/bitsoex/transactions-updater/pull/311) | MMRP-1448: Migrate to AWS Sdk v2.x |
| regulatory-reports | [#1288](https://github.com/bitsoex/regulatory-reports/pull/1288) | PIA-3182: Migrate to AWS SDK for Java v2.x |
| business-reports | [#265](https://github.com/bitsoex/business-reports/pull/265) | BUSIACC-6143: upgrading AWS SDK from v1.x to 2.x |
| circle-payments | [#175](https://github.com/bitsoex/circle-payments/pull/175) | Sbp 373 aws sdk v2 migration |

## Repositories Still Needing Migration

As of December 2025, these repositories still have AWS SDK v1:

| Repository | Files | Squad |
|------------|-------|-------|
| deposit-payoneer | 7 | fiat-rangers-squad |
| deposit-withdrawal-model | 5 | fiat-xforce-squad |
| card-settlement | 5 | jupiter-squad |
| jvm-generic-libraries | 4 | mercury-squad |
| webhooks | 2 | fx-squad |
| kyc-documents | 2 | cosmos-squad |
| brl-efx | 2 | fx-squad |
| ramps-adapter-bridge | 1 | fiat-rangers-squad |
| crypto-common-libs | 1 | crypto-core-squad |

## Troubleshooting

### "Cannot find symbol" errors after migration

The v2 API is different from v1. Check the code migration patterns above for correct v2 usage.

### Old v1 versions still appear in dependency graph

Add more specific substitution rules in root `build.gradle`:

```groovy
allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // Add substitution for each v1 artifact found
        }
    }
}
```

### Test failures after migration

Check if tests use v1 mocking. Update test utilities to use v2 types:

```java
// OLD: Mocking v1 client
@Mock AmazonS3 s3Client;

// NEW: Mocking v2 client
@Mock S3Client s3Client;
```

### Runtime errors with credential providers

v2 uses different credential providers. Check configuration:

```java
// v2 default credential provider
DefaultCredentialsProvider.create()

// v2 with profile
ProfileCredentialsProvider.create("profile-name")

// v2 with explicit credentials
StaticCredentialsProvider.create(
    AwsBasicCredentials.create(accessKey, secretKey)
)
```

## Related

- **Jira Ticket Workflow**: `global/rules/jira-ticket-workflow.md` - Ticket creation
- **Version Management**: `java/rules/java-versions-and-dependencies.md` - Version catalog
- **Gradle Commands**: `java/rules/java-gradle-commands.md` - Debugging commands
- [AWS SDK v2 Developer Guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [AWS SDK v2 Migration Guide](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/migration.html)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-aws-sdk-v2.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
