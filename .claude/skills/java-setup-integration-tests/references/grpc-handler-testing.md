# Testing gRPC Handlers (In-Process)

Test your service's gRPC handlers using in-process transport.

## Contents

- [When to use](#when-to-use)
- [Quick Start](#quick-start)
- [Key Patterns](#key-patterns)
- [application-test.yml](#application-testyml)
- [Testing Error Codes](#testing-error-codes)

---
## When to use

- Testing your own gRPC handlers
- Setting up in-process gRPC server for tests
- Creating GrpcClientTestConfig with test stubs
- Testing gRPC error codes

## Quick Start

### 1. Create BaseGrpcIntegrationSpec

```groovy
package com.bitso.{servicename}.grpc

import com.bitso.{servicename}.BaseIntegrationSpec
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

import java.util.UUID

/**
 * Base for gRPC handler integration tests.
 * Uses in-process gRPC transport (no network, no port conflicts).
 *
 * NOTE on @DynamicPropertySource inheritance:
 * Spring calls ALL @DynamicPropertySource methods in the class hierarchy
 * as long as they have DIFFERENT method names.
 */
@ActiveProfiles("test")
@Import([GrpcClientTestConfig])  // Test stub beans only
@ImportAutoConfiguration([
    GrpcServerAutoConfiguration,
    GrpcServerFactoryAutoConfiguration,
    GrpcClientAutoConfiguration
])
class BaseGrpcIntegrationSpec extends BaseIntegrationSpec {

    @DynamicPropertySource
    static void registerGrpcProperties(DynamicPropertyRegistry registry) {
        def serverName = "test-grpc-" + UUID.randomUUID().toString()
        registry.add("grpc.server.inProcessName", { serverName })
        registry.add("grpc.server.port", { -1 })  // Disable socket server
        registry.add("grpc.client.inProcess.address", { "in-process:${serverName}" })
    }
}
```

### 2. Create GrpcClientTestConfig

```java
package com.bitso.{servicename}.grpc;

import com.bitso.{servicename}.protos.{Service1}Grpc;
import com.bitso.{servicename}.protos.{Service2}Grpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.client.inject.GrpcClientBeans;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC client stubs for tests.
 * IMPORTANT: Only stubs, NOT client wrappers.
 *
 * Autowire in tests using @Qualifier to avoid conflicts with production beans:
 *   @Autowired
 *   @Qualifier("{service1}Stub")
 *   {Service1}Grpc.{Service1}BlockingStub stub
 */
@Configuration(proxyBeanMethods = false)
@GrpcClientBeans({
    @GrpcClientBean(
        clazz = {Service1}Grpc.{Service1}BlockingStub.class,
        beanName = "{service1}Stub",
        client = @GrpcClient("inProcess")
    ),
    @GrpcClientBean(
        clazz = {Service2}Grpc.{Service2}BlockingStub.class,
        beanName = "{service2}Stub",
        client = @GrpcClient("inProcess")
    )
})
public class GrpcClientTestConfig {
}
```

### 3. Write Test

```groovy
class QuoteHandlerIntegrationSpec extends BaseGrpcIntegrationSpec {

    @Autowired
    @Qualifier("quoteServiceStub")
    QuoteServiceGrpc.QuoteServiceBlockingStub stub

    def "successful quote request"() {
        given: "external client returns data"
        // externalClient inherited from BaseIntegrationSpec
        externalClient.getRates(_) >> Either.right(testRates())

        when: "requesting a quote via gRPC"
        def request = QuoteRequest.newBuilder()
            .setUserId(1L)
            .setAmount("100.00")
            .build()
        def response = stub.getQuote(request)

        then: "quote is returned"
        response.hasQuote()
        response.quote.amount == "100.00"
    }

    def "validation error returns INVALID_ARGUMENT"() {
        when: "requesting with invalid amount"
        def request = QuoteRequest.newBuilder()
            .setAmount("-1")
            .build()
        stub.getQuote(request)

        then:
        def e = thrown(StatusRuntimeException)
        e.status.code == Status.Code.INVALID_ARGUMENT
    }
}
```

## Key Patterns

| Pattern | Purpose |
|---------|---------|
| In-process server | No network, no port conflicts |
| UUID in server name | Unique per test run |
| @Import for stubs | Only for test stub beans |
| @Qualifier for stubs | Avoid conflicts with production beans |

## application-test.yml

```yaml
grpc:
  server:
    port: -1  # Disable socket server
    in-process-name: test-grpc-server
  client:
    inProcess:
      address: in-process:test-grpc-server
      negotiation-type: PLAINTEXT
```

## Testing Error Codes

```groovy
def "returns NOT_FOUND for missing resource"() {
    when:
    stub.getResource(GetResourceRequest.newBuilder().setId(999L).build())

    then:
    def e = thrown(StatusRuntimeException)
    e.status.code == Status.Code.NOT_FOUND
}

def "returns PERMISSION_DENIED for unauthorized access"() {
    when:
    stub.getSecureResource(request)

    then:
    def e = thrown(StatusRuntimeException)
    e.status.code == Status.Code.PERMISSION_DENIED
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/grpc-handler-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

