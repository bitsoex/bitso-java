---
applyTo: "bitso-services/**/*.java,bitso-libs/**/*.java"
description: Java Services Standards
---

# Java Services Guidelines

<!-- https://bitsomx.atlassian.net/wiki/spaces/RET/pages/4974247989/Java+services+standard -->

## Tech Stack

- Java: Last LTS version (Java 21)
- Gradle: 8.14.3+ (compatible with Java 21)
- Spring Boot: **3.5.8** for all services (3.4.x EOL end of 2025)
- Database Access: JOOQ for accessing database
- Databases: Postgres and Redis
- Inter-service Communication: gRPC
- Use MapStruct for mapping objects. MapStruct mappers should have a `INSTANCE` variable like this one `public static MyConverter INSTANCE = Mappers.getMapper(MyConverter.class)`, no component model needed. Mappers will be abstract classes with public abstract methods. The default configuration for a mapper is:

```java
@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
```

## Project Organization

Projects should be organized with domain-based modules:

```text
root-project/
├── build.gradle
├── settings.gradle
├── docs/
│   ├── api/rest/openapi.yaml   # Open API Spec for the endpoints of the domain
│   ├── api/grpc/<subdomain>.md # gRPC documentation in Markdown (automatically generated in pipelines)
│   ├── how-tos/                # How tos of the service (e.g, how to run the service locally)
│   ├── runbooks/               # Runbooks (e.g, how to run a specific process, what to do if some alert gets triggered, etc)
│   ├── <domain-name>/          # General documentation for your domain (e.g, concepts, architechture high-level overview)
├── bitso-libs/
│   ├── <subdomain-1>
│   │   ├── src/
│   │   │   ├── main/java/com/bitso/<subdomain-1>/
│   │   │   │   ├── api/            # gRPC implementations
│   │   │   │   │   ├── handlers/   # gRPC service implementations
│   │   │   │   │   └── mappers/    # Mappers from proto to domain and domain to proto (using mapstruct)
│   │   │   │   ├── bff/            # Only for BFF service
│   │   │   │   │   ├── controllers/ # Rest controllers
│   │   │   │   │   ├── mappers/     # Mappers from domain to DTO and DTO to domain
│   │   │   │   │   └── dtos/
│   │   │   │   ├── client/         # Implementations of gRPC clients for services that you are consuming
│   │   │   │   │   ├── <service-name>/
│   │   │   │   │   |   ├── dtos/                  # DTOs retrieved by the Client
│   │   │   │   │   |   └── ServiceNameClient.java # Client implementation
│   │   │   │   ├── config/         # Spring Configuration classes (all beans of the submodule must be defined here)
│   │   │   │   ├── persistence/    # Data access logic
│   │   │   │   │   ├── jooq/
│   │   │   │   │   |   ├── mappers/                  # Mappers from jooq record to domain and domain to record
│   │   │   │   │   |   └── PostgresXRepository.java # Jooq Postgres repository implementation
│   │   │   │   │   └── XRepository.java     # Repository interface
│   │   │   │   ├── domain/         # Domain object definitions for the subdomain
│   │   │   │   ├── integration/    # Third-party integration
│   │   │   │   ├── messaging/      # Message queue interactions
│   │   │   │   └── service/        # Core business logic
│   │   │   │   │   ├── <service-name>/
│   │   │   │   │   |   ├── Default<service-name>Service  # Default implementation of the service
│   │   │   │   │   |   └── <service-name>Service.java  # Service interface
│   ├── <subdomain-1-proto>         # Protobuf definitions of the subdomain
        ├── build.gradle
│   │   ├── src/
│   │   │   ├── main/resources/com/bitso/<domain-name>/<subdomain-1>
│   │   │   │   ├── <domain>_<subdomain>_service_v1.proto            # gRPC service definition and requests and response messages
│   │   │   │   └── <subdomain>.proto        # domain objects definiton
│   ├── <subdomain-2>
│   ├── <subdomain-2-proto>
│   └── ...
├── bitso-services/
    └── <domain>/
        ├── build.gradle
        └── src/
            └── main/java/com/bitso/<domain>/
                ├── config/            # Spring configurations used in different domains (datasource, jooq)
                └── Service application
```

- Each domain and subdomain should be a separate Gradle subproject
- APIs should be organized under the `api` package for gRPC implementations
- BFFs should have a dedicated `bff` package with REST controllers
- Client implementations should be in the `client` package
- Data access should be in the `persistence` package using JOOQ

## Dependency Management

- Use Spring Dependency Management plugin for dependency management
- All libraries must be placed in `libs.versions.toml`
- Only define version variables for dependencies that have multiple packages with repeteated versions
- For `bitso-libs` modules, disable bootJar generation:

```groovy
bootJar {
    enabled = false
}
jar {
    enabled = true
}
```

Example of `libs.versions.toml`:

```toml
[versions]
spring-boot = "3.5.8"
gradle = "8.14.3"
java = "21"
grpc = "1.76.0"
protobuf = "4.33.0"
sonarqube = "7.2.0.6526"

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version = "1.1.7" }
sonarqube = { id = "org.sonarqube", version.ref = "sonarqube" }

[libraries]
internal-bitso-library = { module = "com.bitso.internal:library", version = "1.0.0" }
not-managed-by-spring-library = { module = "org.ecma:library", version = "1.0.0" }
managed-by-spring-library = { module = "org.springframework:library" } # Versions managed by spring don't need an explicit version
```

**For Spring Boot 3.5.x upgrades**, use `/upgrade-to-recommended-versions` command. See `java/commands/upgrade-to-recommended-versions.md` for the complete workflow including:

- Priority-ordered upgrades
- Side-by-side library upgrades (bitso-rds-iam-authn 2.0.0, bitso-commons-redis 4.2.1)
- Endurance plugin versions
- Dependency graph verification

Spring Dependency Management setup:

```groovy
plugins {
    alias libs.plugins.spring.boot
    alias libs.plugins.spring.dependency.management
}
```

## Lombok configuration

To use lombok, add the io.freefair.lombok plugin to your configuration. It injects the latest lombok as a dependency in the proper places, so you don't have to add that dependency yourself:

`libs.versions.toml`

```toml
[plugins]
lombok = { id = "io.freefair.lombok", version = "8.14" }
```

And then in your modules:

```groovy
plugins {
    alias libs.plugins.lombok
}
```

If you are using mapstruct, then you also need the lombok-mapstruct binding library. The lombok plugin detects other annotation processors and makes sure that lombok runs first so that the others can see its modifications.
To add the lombok-mapstruct binding library:

```groovy
    annotationProcessor libs.lombok.mapstruct.binding
```

`libs.versions.toml`

```toml
lombok-mapstruct-binding = { module = 'org.projectlombok:lombok-mapstruct-binding', version = '0.2.0' }
```
