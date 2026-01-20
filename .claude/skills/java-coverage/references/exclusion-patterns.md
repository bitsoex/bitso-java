# JaCoCo Exclusion Patterns

## Common Exclusions

| Pattern | Reason |
|---------|--------|
| `**/generated/**` | Generated code (JOOQ, protobuf) |
| `**/config/**` | Configuration classes |
| `**/*Config.class` | Spring @Configuration classes |
| `**/*Properties.class` | Configuration properties |
| `**/Application.class` | Main application class |
| `**/*Mapper.class` | MapStruct mappers |
| `**/*Builder.class` | Lombok builders |

## Gradle Configuration

```groovy
jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/generated/**',
                '**/config/**',
                '**/*Config.class',
                '**/*Properties.class',
                '**/Application.class',
                '**/*Mapper.class',
                '**/*MapperImpl.class'
            ])
        }))
    }
}
```

## Per-Rule Exclusions

For coverage verification:

```groovy
jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = 'CLASS'
            excludes = [
                '*.generated.*',
                '*.config.*',
                '*.dto.*'
            ]
            limit {
                counter = 'LINE'
                minimum = 0.80
            }
        }
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-coverage/references/exclusion-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

