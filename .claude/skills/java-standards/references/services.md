# Java Services Standards

## Contents

- [Tech Stack](#tech-stack)
- [Project Organization](#project-organization)
- [Package Guidelines](#package-guidelines)
- [Dependency Management](#dependency-management)
- [Lombok Configuration](#lombok-configuration)
- [MapStruct Configuration](#mapstruct-configuration)

---
## Tech Stack

| Component | Version/Tool | Notes |
|-----------|--------------|-------|
| Java | Last LTS version (Java 21) | |
| Gradle | **9.2.1** | Recommended for all projects |
| Spring Boot | **3.5.9** | Latest (min 3.5.9) - preparing for Spring Boot 4 |
| Database Access | jOOQ | For accessing database |
| Databases | PostgreSQL, Redis | |
| Inter-service Communication | gRPC | |

## Project Organization

Projects should be organized with domain-based modules:

```text
root-project/
├── build.gradle
├── settings.gradle
├── docs/
│   ├── api/rest/openapi.yaml   # Open API Spec
│   ├── api/grpc/<subdomain>.md # gRPC documentation
│   ├── how-tos/                # How-tos
│   ├── runbooks/               # Runbooks
│   └── <domain-name>/          # Domain documentation
├── bitso-libs/
│   ├── <subdomain>
│   │   └── src/main/java/com/bitso/<subdomain>/
│   │       ├── api/            # gRPC implementations
│   │       │   ├── handlers/   # gRPC service implementations
│   │       │   └── mappers/    # Proto to domain mappers
│   │       ├── bff/            # Only for BFF service
│   │       │   ├── controllers/
│   │       │   ├── mappers/
│   │       │   └── dtos/
│   │       ├── client/         # gRPC clients
│   │       ├── config/         # Spring Configuration
│   │       ├── persistence/    # Data access logic
│   │       │   ├── jooq/
│   │       │   │   ├── mappers/
│   │       │   │   └── PostgresXRepository.java
│   │       │   └── XRepository.java
│   │       ├── domain/         # Domain objects
│   │       ├── integration/    # Third-party integration
│   │       ├── messaging/      # Message queue
│   │       └── service/        # Core business logic
│   └── <subdomain-proto>       # Protobuf definitions
└── bitso-services/
    └── <domain>/
        ├── build.gradle
        └── src/main/java/com/bitso/<domain>/
            ├── config/         # Spring configurations
            └── Application.java
```

## Package Guidelines

- Each domain and subdomain should be a separate Gradle subproject
- APIs should be organized under the `api` package for gRPC implementations
- BFFs should have a dedicated `bff` package with REST controllers
- Client implementations should be in the `client` package
- Data access should be in the `persistence` package using jOOQ

## Dependency Management

Use Spring Dependency Management plugin:

```groovy
plugins {
    alias libs.plugins.spring.boot
    alias libs.plugins.spring.dependency.management
}
```

All libraries must be placed in `libs.versions.toml`:

```toml
[versions]
spring-boot = "3.5.9"
gradle = "9.2.1"
java = "21"
grpc = "1.78.0"
protobuf = "4.33.4"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
protobuf = { id = "com.google.protobuf", version = "0.9.6" }

[libraries]
managed-by-spring-library = { module = "org.springframework:library" } # No version needed
not-managed-library = { module = "org.ecma:library", version = "1.0.0" }
```

For `bitso-libs` modules, disable bootJar:

```groovy
bootJar {
    enabled = false
}
jar {
    enabled = true
}
```

## Lombok Configuration

Add the io.freefair.lombok plugin:

```toml
# libs.versions.toml
[plugins]
lombok = { id = "io.freefair.lombok", version = "9.2.0" }
```

```groovy
// build.gradle
plugins {
    alias libs.plugins.lombok
}
```

For MapStruct integration, add the binding library:

```groovy
annotationProcessor libs.lombok.mapstruct.binding
```

```toml
# libs.versions.toml
[libraries]
lombok-mapstruct-binding = { module = 'org.projectlombok:lombok-mapstruct-binding', version = '0.2.0' }
```

## MapStruct Configuration

Use MapStruct with Spring's component model for dependency injection. Mappers should:
- Use `componentModel = MappingConstants.ComponentModel.SPRING` for Spring DI integration
- Be interfaces (preferred) or abstract classes
- Be injected via constructor, not accessed via static instances

**Avoid the singleton pattern** (`Mappers.getMapper()` or static `INSTANCE` fields) because:
- Spring-managed beans are easier to mock in unit tests
- Aligns with Spring's dependency injection patterns
- Allows injecting other Spring beans into mappers when needed

Default mapper configuration:

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

Usage in services:

```java
@RequiredArgsConstructor
public class MyService {
    private final MyMapper myMapper;
    
    public TargetDto convert(SourceEntity entity) {
        return myMapper.toDto(entity);
    }
}
```

When a mapper references other mappers via `uses`, add `injectionStrategy = InjectionStrategy.CONSTRUCTOR`:

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
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-standards/references/services.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

