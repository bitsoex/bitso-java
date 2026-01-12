# Code Organization

This document provides guidelines for organizing code within files, modules, and projects.

## File Organization

### General Structure

Files should follow a consistent structure:

1. **Header/License** (if required)
2. **Imports/Dependencies**
3. **Constants/Configuration**
4. **Type definitions** (interfaces, types)
5. **Main content** (class, functions)
6. **Exports** (if applicable)

### Import Organization

Group imports in this order (with blank lines between groups):

1. Standard library / built-in modules
2. Third-party dependencies
3. Internal/project modules

**Java Example:**

```java
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.bitso.payments.model.Payment;
import com.bitso.payments.repository.PaymentRepository;
```

**TypeScript Example:**

```typescript
import { useState, useEffect } from 'react';

import axios from 'axios';
import { useQuery } from '@tanstack/react-query';

import { UserService } from '@/services/user-service';
import { Button } from '@/components/Button';
```

## Class Organization

### Java/Kotlin Classes

Order class members as follows:

1. Static constants
2. Static fields
3. Instance fields
4. Constructors
5. Static factory methods
6. Public methods
7. Protected methods
8. Private methods
9. Inner classes

### TypeScript/JavaScript Classes

Order class members as follows:

1. Static properties
2. Instance properties
3. Constructor
4. Static methods
5. Public methods
6. Private methods

## Module/Package Structure

### Layered Architecture

Organize code by architectural layer:

```
src/
├── controllers/     # HTTP/API handlers
├── services/        # Business logic
├── repositories/    # Data access
├── models/          # Domain entities
├── dto/             # Data transfer objects
├── utils/           # Helper functions
└── config/          # Configuration
```

### Feature-Based Organization

For larger projects, organize by feature:

```
src/
├── users/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── User.java
├── payments/
│   ├── PaymentController.java
│   ├── PaymentService.java
│   └── ...
└── shared/
    ├── utils/
    └── config/
```

## Single Responsibility

### File Size Guidelines

- **Classes**: Aim for <300 lines
- **Functions/Methods**: Aim for <50 lines
- **Files**: Aim for <500 lines

If a file exceeds these guidelines, consider splitting it.

### Signs a File Should Be Split

1. Multiple unrelated responsibilities
2. Frequent merge conflicts
3. Difficult to understand at a glance
4. Hard to name accurately

## Test Organization

### Test File Location

| Strategy | Example |
|----------|---------|
| Co-located | `src/UserService.test.ts` next to `UserService.ts` |
| Separate directory | `test/UserService.test.ts` for `src/UserService.ts` |
| Mirror structure | `src/main/java/...` → `src/test/java/...` |

### Test File Naming

- Java: `*Test.java` or `*Spec.groovy`
- JavaScript/TypeScript: `*.test.ts`, `*.spec.ts`
- Python: `test_*.py` or `*_test.py`

## Configuration Files

Keep configuration files at the project root:

```
project/
├── .github/
├── .gitignore
├── package.json / build.gradle
├── tsconfig.json / settings.gradle
├── README.md
└── src/
```

## Dependencies

### Dependency Direction

- Higher-level modules should not depend on lower-level modules
- Both should depend on abstractions
- Avoid circular dependencies

### Allowed Dependencies

```
Controllers → Services → Repositories → Models
     ↓            ↓            ↓
   DTOs        Models       Entities
```

### Forbidden Dependencies

- Repositories → Controllers
- Models → Services
- Utils → Business logic (utils should be pure)
