# Java Services Standards

## Contents

- [Tech Stack](#tech-stack) (L13-L23)
- [Project Organization](#project-organization) (L24-L67)
- [Package Guidelines](#package-guidelines) (L68-L75)
- [Dependency Management](#dependency-management) (L76-L116)
- [Lombok Configuration](#lombok-configuration) (L117-L145)
- [MapStruct Configuration](#mapstruct-configuration) (L146-L167)

---
## Tech Stack

| Component | Version/Tool | Notes |
|-----------|--------------|-------|
| Java | Last LTS version (Java 21) | |
| Gradle | 8.14.3+ | Compatible with Java 21 |
| Spring Boot | **3.5.9** | 3.4.x EOL end of 2025 |
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
gradle = "8.14.3"
java = "21"
grpc = "1.77.0"
protobuf = "4.33.0"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }

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
lombok = { id = "io.freefair.lombok", version = "8.14" }
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

Use MapStruct for mapping objects. Mappers should:
- Have a `INSTANCE` variable: `public static MyConverter INSTANCE = Mappers.getMapper(MyConverter.class)`
- Be abstract classes with public abstract methods
- No component model needed

Default mapper configuration:

```java
@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public abstract class MyMapper {
    public static final MyMapper INSTANCE = Mappers.getMapper(MyMapper.class);
    
    public abstract TargetDto toDto(SourceEntity entity);
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-standards/references/services.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

