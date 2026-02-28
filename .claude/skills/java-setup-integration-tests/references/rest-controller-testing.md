# Testing REST Controllers with MockMvc

Test your service's REST controllers using Spring MockMvc.

## Contents

- [When to use](#when-to-use)
- [Quick Start](#quick-start)
- [Key Patterns](#key-patterns)
- [Common Matchers](#common-matchers)
- [Testing Error Responses](#testing-error-responses)
- [Testing with Request Body](#testing-with-request-body)
- [Testing File Upload](#testing-file-upload)

---

## When to use

- Testing REST endpoints
- Setting up MockMvc integration tests
- JSON path assertions
- Testing error responses

## Quick Start

### 1. Create BaseRestIntegrationSpec

```groovy
package com.bitso.{servicename}.rest

import com.bitso.{servicename}.BaseIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

/**
 * Base for REST controller integration tests.
 */
@AutoConfigureMockMvc
class BaseRestIntegrationSpec extends BaseIntegrationSpec {

    @Autowired
    MockMvc mockMvc
}
```

### 2. Write Test

```groovy
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserControllerIntegrationSpec extends BaseRestIntegrationSpec {

    def "GET /api/v1/users/{id} returns user"() {
        given:
        externalClient.getUser(1L) >> testUser()

        when:
        def result = mockMvc.perform(
            get("/api/v1/users/1")
                .header("Authorization", "Bearer test-token")
                .accept(MediaType.APPLICATION_JSON)
        )

        then:
        result.andExpect(status().isOk())
              .andExpect(jsonPath('$.id').value(1))
              .andExpect(jsonPath('$.name').value("Test User"))
    }

    def "POST /api/v1/users creates user"() {
        given:
        def body = '{"name": "New User"}'

        when:
        def result = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )

        then:
        result.andExpect(status().isCreated())
              .andExpect(jsonPath('$.id').exists())
    }

    def "returns 404 when not found"() {
        given:
        externalClient.getUser(999L) >> null

        when:
        def result = mockMvc.perform(get("/api/v1/users/999"))

        then:
        result.andExpect(status().isNotFound())
    }
}
```

## Key Patterns

| Pattern | Purpose |
|---------|---------|
| `@AutoConfigureMockMvc` | Enables MockMvc |
| `jsonPath('$.field')` | Assert JSON response |
| `status().isOk()` | Assert HTTP status |
| Single quotes in Groovy | Required for jsonPath strings |

## Common Matchers

```groovy
import static org.hamcrest.Matchers.*

// Collection assertions
jsonPath('$.items', hasSize(5))
jsonPath('$.items[0].name').value("first")

// String assertions
jsonPath('$.name', containsString("test"))
jsonPath('$.name', startsWith("Test"))

// Numeric assertions
jsonPath('$.amount', greaterThan(0))
jsonPath('$.count', lessThanOrEqualTo(100))

// Existence assertions
jsonPath('$.optional').doesNotExist()
jsonPath('$.required').exists()

// Null assertions
jsonPath('$.nullable').value(nullValue())
jsonPath('$.notNull').value(notNullValue())
```

## Testing Error Responses

```groovy
def "returns 400 for invalid request"() {
    when:
    def result = mockMvc.perform(
        post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content('{"name": ""}')  // Empty name
    )

    then:
    result.andExpect(status().isBadRequest())
          .andExpect(jsonPath('$.error').value("validation_error"))
          .andExpect(jsonPath('$.message').exists())
}

def "returns 401 for missing auth"() {
    when:
    def result = mockMvc.perform(get("/api/v1/secure"))

    then:
    result.andExpect(status().isUnauthorized())
}

def "returns 403 for insufficient permissions"() {
    when:
    def result = mockMvc.perform(
        get("/api/v1/admin")
            .header("Authorization", "Bearer user-token")
    )

    then:
    result.andExpect(status().isForbidden())
}
```

## Testing with Request Body

```groovy
def "updates user"() {
    given:
    def body = new ObjectMapper().writeValueAsString([
        name: "Updated Name",
        email: "updated@example.com"
    ])

    when:
    def result = mockMvc.perform(
        put("/api/v1/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
    )

    then:
    result.andExpect(status().isOk())
          .andExpect(jsonPath('$.name').value("Updated Name"))
}
```

## Testing File Upload

```groovy
def "uploads file"() {
    given:
    def file = new MockMultipartFile(
        "file",
        "test.csv",
        "text/csv",
        "id,name\n1,Test".bytes
    )

    when:
    def result = mockMvc.perform(
        multipart("/api/v1/upload").file(file)
    )

    then:
    result.andExpect(status().isOk())
          .andExpect(jsonPath('$.processed').value(1))
}
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/rest-controller-testing.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

