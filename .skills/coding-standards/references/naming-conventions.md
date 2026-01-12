# Naming Conventions

This document defines naming conventions for different programming languages and file types.

## General Principles

1. **Be descriptive**: Names should clearly indicate purpose
2. **Be consistent**: Follow the same pattern throughout the codebase
3. **Avoid abbreviations**: Use full words unless the abbreviation is widely understood
4. **Consider searchability**: Use names that are easy to search for

## Language-Specific Conventions

### Java

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserService`, `PaymentProcessor` |
| Interfaces | PascalCase | `Repository`, `EventListener` |
| Methods | camelCase | `getUserById`, `processPayment` |
| Variables | camelCase | `userName`, `orderCount` |
| Constants | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Packages | lowercase | `com.bitso.payments`, `org.example.utils` |

### JavaScript / TypeScript

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserController`, `ApiClient` |
| Functions | camelCase | `fetchUserData`, `handleSubmit` |
| Variables | camelCase | `isLoading`, `userData` |
| Constants | SCREAMING_SNAKE_CASE or camelCase | `API_BASE_URL`, `defaultConfig` |
| Files | kebab-case or camelCase | `user-service.ts`, `userService.ts` |
| React Components | PascalCase | `UserProfile.tsx`, `Button.tsx` |

### Python

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `UserRepository`, `DataProcessor` |
| Functions | snake_case | `get_user_by_id`, `process_data` |
| Variables | snake_case | `user_name`, `order_count` |
| Constants | SCREAMING_SNAKE_CASE | `MAX_CONNECTIONS`, `DEFAULT_TIMEOUT` |
| Modules | snake_case | `user_service.py`, `data_utils.py` |
| Private members | Leading underscore | `_internal_method`, `_private_var` |
| Private modules | Leading underscore + snake_case | `_internal.py`, `_helpers.py` |

### Shell Scripts

| Element | Convention | Example |
|---------|------------|---------|
| Files | kebab-case | `deploy-service.sh`, `run-tests.sh` |
| Variables | SCREAMING_SNAKE_CASE | `PROJECT_ROOT`, `CONFIG_FILE` |
| Functions | snake_case | `setup_environment`, `run_build` |

## File and Directory Naming

### Source Files

- Match the primary class/component name
- Use language-appropriate casing
- Be specific about purpose

### Directories

| Type | Convention | Example |
|------|------------|---------|
| Source directories | lowercase or kebab-case | `src`, `lib`, `user-management` |
| Test directories | Match source structure | `test`, `tests`, `__tests__` |
| Config directories | lowercase | `config`, `.github` (GitHub workflows) |

## Common Anti-Patterns to Avoid

1. **Single-letter variables** (except loop counters)
   - Bad: `x`, `d`, `s`
   - Good: `user`, `date`, `service`

2. **Hungarian notation** (unless project standard)
   - Bad: `strName`, `intCount`, `boolIsActive`
   - Good: `name`, `count`, `isActive`

3. **Meaningless names**
   - Bad: `data`, `info`, `item`, `thing`
   - Good: `userData`, `orderInfo`, `productItem`

4. **Overly long names**
   - Bad: `getUserByIdFromDatabaseAndValidatePermissions`
   - Good: `getValidatedUser` (extract steps to separate methods)

## Acronyms and Abbreviations

- Treat acronyms as words for casing
- Java/JS: `HttpClient`, `XmlParser`, `userId`
- Only use widely understood abbreviations: `id`, `url`, `api`, `http`
