---
description: Java Rest Api Guidelines
alwaysApply: true
tags:
  - java
  - api
  - rest
---

# REST API Guidelines (Spring Service)

## API Guidelines

Follow Bitso API Guidelines when developing public-facing APIs. Use the api-guidelines MCP server to get the latest guidelines.

## Authentication

This rule outlines the standard procedure for integrating Bitso authentication into Spring-based REST services.

1. **Dependency**: Add the `com.bitso:api-base-spring-webapi` library to your `build.gradle`. Check the [latest version here](https://github.com/bitsoex/artifacts/packages/1866196).

    ```groovy
    implementation libs.bitso.api.base.spring.webapi
    ```

2. **Configuration (Recommended: Spring gRPC)**: Configure the gRPC client in `application.yml`:

    ```yaml
    grpc:
      client:
        user-security:
          address: dns:/${USER_SECURITY_HOST:localhost}:${GRPC_PORT:8201}
          negotiation-type: PLAINTEXT
    ```

    Define the `AuthenticationService` bean:

    ```java
    @Configuration
    public class UserSecurityContextConfiguration {
        @Bean
        @Primary
        public AuthenticationService authenticationService(
            final @GrpcClient("user-security") AuthorizationServiceV1BlockingStub authorizationServiceV1BlockingStub,
            final @Qualifier("userSecurityResilienceConfig") ResilienceConfiguration config
        ) {
            return new ProtoShimAuthenticationService(config, authorizationServiceV1BlockingStub);
        }
    }
    ```

    *Reference: [user-management service](https://github.com/bitsoex/user-management)*

3. **Component Scan**: Ensure your main application class scans Bitso components:

    ```java
    @SpringBootApplication
    @ComponentScan("com.bitso.*") // Ensure this package is scanned
    public class YourApplication {
        // ... main method ...
    }
    ```

### Usage

1. **Annotate Controllers/Methods**: Use the `@WebAPI` annotation on controller classes or specific methods that require authentication.
2. **Access User Info**: Inject `WebAuthenticationContext` to access authenticated user details.

```java
@RestController("/")
public class ExampleController {

    @Autowired
    SpringHttpResponseFactory responseFactory;

    @Autowired
    WebAuthenticationContext authenticationContext; // Inject to get user info

    @GetMapping("/private")
    @WebAPI(WebAPIType.PRIVATE) // Apply authentication
    public ResponseEntity<?> privateEndpoint() {
        // Access user ID if authenticated
        Long userId = authenticationContext.getPrincipalId();
        return responseFactory.ok(userId);
    }
}
```

### Testing

When testing controllers annotated with `@WebAPI`, you need to provide an alternative implementation for the `AuthenticationService` bean. To do that, create a `BypassAuthenticationService` that will look as following:

```java
public class BypassAuthenticationService implements AuthenticationService {

    private static final User DEFAULT_USER;

    static {
        DEFAULT_USER = new User();
        DEFAULT_USER.setId(1L);
    }

    private User user = DEFAULT_USER; // Default user

    /**
     * Sets the user to be returned in the AuthorizationData.
     * @param user The user to set.
     * @return This BypassAuthenticationService instance for chaining.
     */
    public BypassAuthenticationService setUser(User user) {
        this.user = user;
        return this;
    }

    /**
     * Gets the currently set user.
     * @return The current user.
     */
    public User getUser() {
        return this.user;
    }
    
    @Override
    public Either<WebAPIError, AuthorizationData> authorizeRequest(AuthorizationRequest request) {
        AuthorizationData successResponse = AuthorizationData.builder()
                .user(this.user)
                .authenticatedUser(Optional.of(this.user))
                .build();
        return Either.right(successResponse);
    }
}

```

This class will be replicated in every subdomain gradle module, before using it, check if it is already available.

```groovy
// Example Groovy/Spock test configuration using Spring Boot Test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExampleControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc
    @SpringBean
    AuthenticationService authenticationService = new BypassAuthenticationService()


    def "private endpoint should return user ID"() {
        when:
        def response = mockMvc.perform(get("/private"))
                .andExpect(status().isOk())
                .andReturn()

        then:
        // Assert based on the default user (ID 1L) or the specific user you set
        response.response.contentAsString == '1'
    }
}
```

## Documentation (OpenAPI Specification)

All the endpoints from a subdomain should be documented under `./docs/api/rest/openapi.yaml`

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/rules/java-rest-api-guidelines.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
