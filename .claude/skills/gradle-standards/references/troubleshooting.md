# Gradle Troubleshooting

## Common Issues

### "Cannot resolve dependency"

1. Check version catalog has the dependency
2. Verify BOM is declared with `platform()`
3. Check GitHub token is valid

```bash
./gradlew dependencies --configuration runtimeClasspath | grep <dependency>
```

### Module Tests Don't Run

1. Verify `src/test/java` exists
2. Check `test { useJUnitPlatform() }` in module
3. Run with info: `./gradlew :module:test --info`

### Build Cache Issues

```bash
./gradlew --stop
./gradlew clean
./gradlew build --no-build-cache
```

### Plugin Version Conflicts

1. Use `pluginManagement` in `settings.gradle`
2. Define versions in version catalog
3. Don't specify versions in plugins {} of individual modules

## NoSuchMethodError

Often caused by version conflicts:

```java
java.lang.NoSuchMethodError: 'redis.clients.jedis.params.SetParams...'
```

**Solution**: Check for hardcoded versions overriding BOM:

```bash
./gradlew dependencies --configuration runtimeClasspath | grep jedis
```

See `java/golden-paths/redis-jedis-compatibility.md`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/gradle-standards/references/troubleshooting.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

