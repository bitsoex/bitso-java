# Upgrade Java services to recommended versions (Spring Boot 3.5.9, Gradle 9.2.1, Java 21)

> Upgrade Java services to recommended versions (Spring Boot 3.5.9, Gradle 9.2.1, Java 21)

# 🤖 📦 Upgrade to Recommended Versions

> **📚 Full documentation**: See the [upgrade-spring-boot-3-5 skill](.claude/skills/upgrade-spring-boot-3-5/SKILL.md) for complete upgrade patterns and troubleshooting.

**IMPORTANT**: Stay on latest or latest-1 patch of Spring Boot 3.5.x to prepare for Spring Boot 4.

## Target Versions

| Component | Version | Notes |
|-----------|---------|-------|
| **Spring Boot** | **3.5.9** | Latest (min 3.5.9) - preparing for Spring Boot 4 |
| **Spring Cloud** | **2025.0.0** | Required for Spring Boot 3.5.x |
| **Gradle** | **9.2.1** | Recommended for all projects |
| **JUnit** | **5.14.2** | Testing (via BOM) |
| **JaCoCo** | **0.8.14** | Code coverage |

## Quick Commands

```bash
# Check current versions
./gradlew dependencies --configuration runtimeClasspath | grep spring-boot

# Update Gradle wrapper
./gradlew wrapper --gradle-version 9.2.1
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

- [Full Skill](.claude/skills/upgrade-spring-boot-3-5/SKILL.md)
- [Troubleshooting](.claude/skills/upgrade-spring-boot-3-5/references/troubleshooting.md)

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/commands/upgrade-to-recommended-versions.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
