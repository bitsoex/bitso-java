# AWS SDK v2 Migration Patterns

Detailed code migration patterns for common AWS services.

## Contents

- [S3 Client Migration](#s3-client-migration) (L17-L51)
- [SQS Client Migration](#sqs-client-migration) (L52-L77)
- [SNS Client Migration](#sns-client-migration) (L78-L103)
- [Lambda Client Migration](#lambda-client-migration) (L104-L132)
- [Credential Provider Migration](#credential-provider-migration) (L133-L148)
- [Dependency Substitution Examples](#dependency-substitution-examples) (L149-L189)
- [Version Catalog Setup](#version-catalog-setup) (L190-L208)
- [Troubleshooting](#troubleshooting) (L209-L246)

---
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
<!-- Source: bitsoex/ai-code-instructions → java/skills/aws-v2/references/migration-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

