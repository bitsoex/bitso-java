# REST API Guidelines

## Contents

- [API Guidelines](#api-guidelines)
- [Authentication](#authentication)
- [Usage](#usage)
- [Testing](#testing)
- [Documentation](#documentation)

---
## API Guidelines

Follow Bitso API Guidelines when developing public-facing APIs. Use the api-guidelines MCP server to get the latest guidelines.

## Authentication

### Dependency

Add the `com.bitso:api-base-spring-webapi` library:

```groovy
implementation libs.bitso.api.base.spring.webapi
```

### Configuration (Spring gRPC)

Configure the gRPC client in `application.yml`:

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

### Component Scan

Ensure your main application class scans Bitso components:

```java
@SpringBootApplication
@ComponentScan("com.bitso.*")
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## Usage

### Annotate Controllers/Methods

Use the `@WebAPI` annotation on controller classes or specific methods that require authentication:

```java
@RestController("/")
public class ExampleController {

    @Autowired
    SpringHttpResponseFactory responseFactory;

    @Autowired
    WebAuthenticationContext authenticationContext;

    @GetMapping("/private")
    @WebAPI(WebAPIType.PRIVATE)
    public ResponseEntity<?> privateEndpoint() {
        Long userId = authenticationContext.getPrincipalId();
        return responseFactory.ok(userId);
    }
}
```

## Testing

When testing controllers annotated with `@WebAPI`, provide an alternative implementation for the `AuthenticationService` bean:

### BypassAuthenticationService

```java
public class BypassAuthenticationService implements AuthenticationService {

    private static final User DEFAULT_USER;

    static {
        DEFAULT_USER = new User();
        DEFAULT_USER.setId(1L);
    }

    private User user = DEFAULT_USER;

    public BypassAuthenticationService setUser(User user) {
        this.user = user;
        return this;
    }

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

### Test Configuration

```groovy
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
        response.response.contentAsString == '1'
    }
}
```

## Documentation

All endpoints from a subdomain should be documented under `./docs/api/rest/openapi.yaml`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/rest-api/references/guidelines.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

