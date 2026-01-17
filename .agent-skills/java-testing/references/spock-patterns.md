# Spock Framework Patterns

## Data-Driven Tests

Use `where:` blocks for multiple test cases:

```groovy
def "should validate email format"() {
    expect:
        EmailValidator.isValid(email) == expected
    where:
        email               | expected
        "user@example.com"  | true
        "invalid"           | false
        ""                  | false
        null                | false
}
```

## Mock Verification

### Cardinality

```groovy
then:
    1 * mock.method()      // exactly once
    0 * mock.method()      // never called
    (1..3) * mock.method() // 1-3 times
    _ * mock.method()      // any number of times
```

### Argument Matching

```groovy
then:
    1 * mock.save(_)                    // any argument
    1 * mock.save({ it.name == "Bob" }) // closure matcher
    1 * mock.save(!null)                // not null
    1 * mock.save(_ as User)            // type check
```

## Exception Testing

```groovy
def "should throw exception for invalid input"() {
    when:
        service.process(null)
    then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("must not be null")
}
```

## Shared Setup

```groovy
@Shared
DatabaseHelper db = new DatabaseHelper()

def setupSpec() {
    db.init()
}

def cleanupSpec() {
    db.cleanup()
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-testing/references/spock-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

