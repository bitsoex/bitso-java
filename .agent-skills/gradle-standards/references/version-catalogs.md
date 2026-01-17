# Version Catalogs Guide

## Overview

Gradle Version Catalogs (`gradle/libs.versions.toml`) provide a centralized, type-safe way to manage dependency versions.

## File Structure

```toml
[versions]
# Define version variables
spring-boot = "3.5.8"
spock = "2.4-M4-groovy-4.0"
grpc = "1.65.1"
testcontainers = "1.20.0"

[libraries]
# Define library coordinates with version references
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-starter-test = { module = "org.springframework.boot:spring-boot-starter-test", version.ref = "spring-boot" }

# Spock testing libraries
spock-core = { module = "org.spockframework:spock-core", version.ref = "spock" }
spock-spring = { module = "org.spockframework:spock-spring", version.ref = "spock" }

# Libraries with inline versions (when not shared)
logstash-logback-encoder = { module = "net.logstash.logback:logstash-logback-encoder", version = "8.0" }

[bundles]
# Group related dependencies
testing-spring = ["spring-boot-starter-test", "spock-core", "spock-spring"]

[plugins]
# Plugin declarations
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
```

## Usage in build.gradle

```groovy
plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.bundles.testing.spring

    // Access version for string interpolation
    implementation "com.example:lib:${libs.versions.example.get()}"
}
```

## Best Practices

1. **Naming Convention**: Use kebab-case for library names
2. **Version References**: Use `version.ref` for shared versions
3. **Bundles**: Create bundles for commonly-used combinations
4. **Documentation**: Add comments for non-obvious dependencies
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/version-catalogs.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

