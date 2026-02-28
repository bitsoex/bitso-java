# gRPC Testing

## Server Testing

Use `grpc-spring-boot-starter` for integration tests:

```groovy
@SpringBootTest
class GrpcServerSpec extends Specification {
    @Autowired
    MyGrpcService service

    def "should handle request"() {
        given:
            def request = MyRequest.newBuilder()
                .setField("value")
                .build()
        when:
            def response = service.myMethod(request)
        then:
            response.result == "expected"
    }
}
```

## Client Testing with In-Process Server

```groovy
class GrpcClientSpec extends Specification {

    InProcessServer server
    ManagedChannel channel

    def setup() {
        def serverName = InProcessServerBuilder.generateName()

        server = InProcessServerBuilder
            .forName(serverName)
            .addService(new TestServiceImpl())
            .build()
            .start()

        channel = InProcessChannelBuilder
            .forName(serverName)
            .directExecutor()
            .build()
    }

    def cleanup() {
        channel.shutdownNow()
        server.shutdownNow()
    }

    def "should make grpc call"() {
        given:
            def request = MyRequest.newBuilder().setField("value").build()
            def stub = MyServiceGrpc.newBlockingStub(channel)
        when:
            def response = stub.myMethod(request)
        then:
            response.field == "expected"
    }
}
```

## Dependencies

```toml
[bundles]
grpc-testing = ["grpc-testing", "grpc-inprocess"]
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/grpc-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

