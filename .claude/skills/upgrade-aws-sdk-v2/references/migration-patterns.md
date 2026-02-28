# AWS SDK v2 Migration Patterns

Detailed code migration patterns for common AWS services.

## Contents

- [MSK IAM Authentication](#msk-iam-authentication)
- [S3 Client Migration](#s3-client-migration)
- [SQS Client Migration](#sqs-client-migration)
- [SNS Client Migration](#sns-client-migration)
- [Lambda Client Migration](#lambda-client-migration)
- [Credential Provider Migration](#credential-provider-migration)
- [Dependency Substitution Examples](#dependency-substitution-examples)
- [Version Catalog Setup](#version-catalog-setup)
- [Troubleshooting](#troubleshooting)

---
## MSK IAM Authentication

**CRITICAL**: When migrating to AWS SDK v2, you MUST also update `aws-msk-iam-auth` if your project uses Kafka with MSK IAM authentication.

### Version Compatibility Matrix

| AWS SDK | aws-msk-iam-auth | Notes |
|---------|------------------|-------|
| v1 (com.amazonaws) | 1.1.9 | Works with AWS SDK v1 |
| v2 (software.amazon.awssdk) | **2.3.5** | REQUIRED for AWS SDK v2 |

### The Problem

After migrating to AWS SDK v2, Kafka producer initialization fails with:

```
NoClassDefFoundError: com/amazonaws/auth/AWSCredentialsProvider
```

### Root Cause

`aws-msk-iam-auth:1.1.9` depends on AWS SDK v1 (`com.amazonaws.auth.AWSCredentialsProvider`), which is no longer on the classpath after migration to SDK v2.

### Solution

Update `aws-msk-iam-auth` from `1.1.9` to `2.3.5`:

```toml
# gradle/libs.versions.toml
[versions]
aws-msk-iam-auth = "2.3.5"  # Updated for AWS SDK v2 compatibility

[libraries]
aws-msk-iam-auth = { module = "software.amazon.msk:aws-msk-iam-auth", version.ref = "aws-msk-iam-auth" }
```

```groovy
// build.gradle
dependencies {
    implementation libs.aws.msk.iam.auth
}
```

### Verification

Check that no AWS SDK v1 dependencies remain:

```bash
./gradlew dependencies --configuration runtimeClasspath | grep "com.amazonaws"
```

## S3 Client Migration

### v1 (OLD)

```java
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
    .withRegion(region)
    .build();

s3Client.putObject(new PutObjectRequest(bucket, key, file));
```

### v2 (NEW)

```java
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.sync.RequestBody;

S3Client s3Client = S3Client.builder()
    .region(Region.of(region))
    .build();

s3Client.putObject(PutObjectRequest.builder()
    .bucket(bucket)
    .key(key)
    .build(),
    RequestBody.fromFile(file));
```

## SQS Client Migration

### v1 (OLD)

```java
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

AmazonSQS sqsClient = AmazonSQSClientBuilder.standard().build();
sqsClient.sendMessage(new SendMessageRequest(queueUrl, messageBody));
```

### v2 (NEW)

```java
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

SqsClient sqsClient = SqsClient.builder().build();
sqsClient.sendMessage(SendMessageRequest.builder()
    .queueUrl(queueUrl)
    .messageBody(messageBody)
    .build());
```

## SNS Client Migration

### v1 (OLD)

```java
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();
snsClient.publish(new PublishRequest(topicArn, message));
```

### v2 (NEW)

```java
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

SnsClient snsClient = SnsClient.builder().build();
snsClient.publish(PublishRequest.builder()
    .topicArn(topicArn)
    .message(message)
    .build());
```

## Lambda Client Migration

### v1 (OLD)

```java
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;

AWSLambda lambdaClient = AWSLambdaClientBuilder.standard().build();
lambdaClient.invoke(new InvokeRequest()
    .withFunctionName(functionName)
    .withPayload(payload));
```

### v2 (NEW)

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

## Credential Provider Migration

### v1 (OLD)

```java
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.AWSCredentialsProvider;
```

### v2 (NEW)

```java
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
```

## Dependency Substitution Examples

Add to root `build.gradle`:

```groovy
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

## Version Catalog Setup

> **Note**: Check the [AWS SDK for Java v2 releases](https://github.com/aws/aws-sdk-java-v2/releases) for the latest version before implementing.

```toml
# gradle/libs.versions.toml
[versions]
# Check https://github.com/aws/aws-sdk-java-v2/releases for latest version
aws-sdk-v2 = "2.40.10"

[libraries]
aws-s3 = { module = "software.amazon.awssdk:s3", version.ref = "aws-sdk-v2" }
aws-sqs = { module = "software.amazon.awssdk:sqs", version.ref = "aws-sdk-v2" }
aws-sns = { module = "software.amazon.awssdk:sns", version.ref = "aws-sdk-v2" }
aws-lambda = { module = "software.amazon.awssdk:lambda", version.ref = "aws-sdk-v2" }
```

Using a version catalog variable allows centralized updates across all AWS SDK v2 dependencies.

## Troubleshooting

### "Cannot find symbol" errors after migration

The v2 API is different from v1. Check patterns above for correct v2 usage.

### Old v1 versions still in dependency graph

Add more specific substitution rules for each v1 artifact found.

### Test failures with v1 mocking

Update test utilities to use v2 types:

```java
// OLD: Mocking v1 client
@Mock AmazonS3 s3Client;

// NEW: Mocking v2 client
@Mock S3Client s3Client;
```

### Runtime credential errors

v2 uses different credential providers:

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
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/upgrade-aws-sdk-v2/references/migration-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

