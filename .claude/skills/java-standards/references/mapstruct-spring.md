# MapStruct Spring Component Model

Use Spring's dependency injection for MapStruct mappers instead of the singleton pattern.

## Key Principles

1. **Use Spring Component Model**: Configure mappers with `componentModel = MappingConstants.ComponentModel.SPRING`
2. **Avoid Singleton Pattern**: Do not use `Mappers.getMapper()` or static `INSTANCE` fields
3. **Inject via Constructor**: Use constructor injection for mapper dependencies

## Why Avoid Singleton Pattern

- **Testability**: Spring-managed beans are easier to mock in unit tests
- **Consistency**: Aligns with Spring's dependency injection patterns
- **Flexibility**: Allows injecting other Spring beans into mappers when needed

## Mapper Configuration

```java

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MyMapper {
    TargetDto toDto(SourceEntity entity);
}
```

## Mapper with Dependencies (uses)

When a mapper references other mappers via `uses`, add `injectionStrategy = InjectionStrategy.CONSTRUCTOR` to ensure
proper initialization order and testability. Constructor injection prevents circular dependency issues and makes
the mapper dependencies explicit:

```java

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {AddressMapper.class, PhoneMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {
    CustomerDto toDto(CustomerEntity entity);
}
```

## Usage in Services

```java

@RequiredArgsConstructor
public class MyService {
    private final MyMapper myMapper;

    public TargetDto convert(SourceEntity entity) {
        return myMapper.toDto(entity);
    }
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-standards/references/mapstruct-spring.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

