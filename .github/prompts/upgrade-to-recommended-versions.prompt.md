# Upgrade Java services to recommended versions (Spring Boot 3.5.9, Gradle 8.14.3+, Java 21)

> Upgrade Java services to recommended versions (Spring Boot 3.5.9, Gradle 8.14.3+, Java 21)

# 🤖 📦 Upgrade to Recommended Versions

> **📚 Full documentation**: See the [spring-boot-3-5 skill](../skills/spring-boot-3-5/SKILL.md) for complete upgrade patterns and troubleshooting.

**URGENT**: Spring Boot 3.4.x reached end-of-life (EOL) in 2025. All projects should be upgraded to Spring Boot 3.5.9.

## Target Versions

| Component | Version | Notes |
|-----------|---------|-------|
| **Spring Boot** | **3.5.9** | CRITICAL - 3.4.x reached EOL (end of 2025) |
| **Spring Cloud** | **2025.0.0** | Required for Spring Boot 3.5.x |
| **Gradle** | **8.14.3** | Latest 8.x patch (Gradle 9.x for Java 25) |
| **JUnit** | **5.14.2** | Testing (via BOM) |
| **JaCoCo** | **0.8.15** | Code coverage |

## Quick Commands

```bash
# Check current versions
./gradlew dependencies --configuration runtimeClasspath | grep spring-boot

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.14.3
```

## Workflow Summary

1. Create/find Jira ticket
2. Update Spring Boot version in libs.versions.toml
3. Update Spring Cloud to 2025.0.0
4. Run build and fix deprecations
5. Update internal libraries (redis, rds-iam-authn)
6. Test and push

## Related Golden Paths

- [Spring Boot 3.5 Upgrade](../golden-paths/spring-boot-3.5-upgrade.md)
- [JUnit Alignment](../golden-paths/junit-version-alignment.md)
- [Redis/Jedis Compatibility](../golden-paths/redis-jedis-compatibility.md)

## Related

- [Full Skill](../skills/spring-boot-3-5/SKILL.md)
- [Troubleshooting](../skills/spring-boot-3-5/references/troubleshooting.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-recommended-versions.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
