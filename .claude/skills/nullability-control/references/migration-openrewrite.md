# Migration with OpenRewrite

Guide for migrating existing code to JSpecify nullability annotations using OpenRewrite.

## Contents

- [Overview](#overview)
- [What the Recipe Does](#what-the-recipe-does)
- [Setup](#setup)
- [Running the Migration](#running-the-migration)
- [Migration Workflow](#migration-workflow)
- [What Gets Migrated](#what-gets-migrated)
- [Individual Recipes](#individual-recipes)
- [Manual Steps After Migration](#manual-steps-after-migration)
- [Troubleshooting](#troubleshooting)

---
## Overview

OpenRewrite provides automated recipes for migrating nullability annotations to JSpecify format. The main recipe is `RecipeNullabilityBestPractices`.

## What the Recipe Does

The `org.openrewrite.java.recipes.RecipeNullabilityBestPractices` recipe:

1. **Migrates from OpenRewrite annotations to JSpecify**
2. **Removes redundant @Nonnull annotations** (since non-null is default in @NullMarked)
3. **Removes other non-null annotations**:
   - `@org.jetbrains.annotations.NotNull`
   - `@javax.annotation.Nonnull`
   - `@jakarta.annotation.Nonnull`
4. **Migrates to JSpecify @Nullable**
5. **Annotates methods that may return null with @Nullable**
6. **Moves @Nullable to return type position** (correct placement for type-use annotations)

## Setup

### Gradle Configuration

Add to `build.gradle`:

```groovy
plugins {
    id 'org.openrewrite.rewrite' version '7.25.0'
}

rewrite {
    activeRecipe('org.openrewrite.java.recipes.RecipeNullabilityBestPractices')
    setExportDatatables(true)
}

dependencies {
    rewrite('org.openrewrite.recipe:rewrite-rewrite:0.19.0')
}
```

### Maven Configuration

Add to `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openrewrite.maven</groupId>
            <artifactId>rewrite-maven-plugin</artifactId>
            <version>6.28.0</version>
            <configuration>
                <exportDatatables>true</exportDatatables>
                <activeRecipes>
                    <recipe>org.openrewrite.java.recipes.RecipeNullabilityBestPractices</recipe>
                </activeRecipes>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.openrewrite.recipe</groupId>
                    <artifactId>rewrite-rewrite</artifactId>
                    <version>0.19.0</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## Running the Migration

### Gradle

```bash
# Dry run - see what changes will be made
./gradlew rewriteDryRun

# Apply changes
./gradlew rewriteRun
```

### Maven

```bash
# Apply changes
mvn rewrite:run
```

### Command Line (No Build Changes)

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-rewrite:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.recipes.RecipeNullabilityBestPractices \
  -Drewrite.exportDatatables=true
```

## Migration Workflow

### Step 1: Add JSpecify Dependency

Before running migration, add JSpecify to your project:

```toml
# gradle/libs.versions.toml
[libraries]
jspecify = { module = "org.jspecify:jspecify", version = "1.0.0" }
```

```groovy
// build.gradle
dependencies {
    implementation libs.jspecify
}
```

### Step 2: Create package-info.java Files

Create @NullMarked package-info.java for each package (see [package-info-templates.md](package-info-templates.md)).

### Step 3: Run OpenRewrite Migration

```bash
./gradlew rewriteRun
```

### Step 4: Review Changes

Review the automated changes:

```bash
git diff
```

### Step 5: Fix Remaining Issues

Run your build with NullAway enabled to find remaining issues:

```bash
./gradlew build
```

### Step 6: Add Missing @Nullable Annotations

OpenRewrite may not catch all nullable cases. Manually add @Nullable where needed.

## What Gets Migrated

### Before

```java
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class UserService {
    @Nullable
    private User cachedUser;

    @Nonnull
    public String getName() {
        return "name";
    }

    @Nullable
    public User findUser(String id) {
        return cachedUser;
    }

    public void setUser(@Nonnull User user) {
        this.cachedUser = user;
    }
}
```

### After

```java
import org.jspecify.annotations.Nullable;

public class UserService {
    private @Nullable User cachedUser;

    public String getName() {  // @Nonnull removed (implicit)
        return "name";
    }

    public @Nullable User findUser(String id) {  // Moved to return type
        return cachedUser;
    }

    public void setUser(User user) {  // @Nonnull removed (implicit)
        this.cachedUser = user;
    }
}
```

## Individual Recipes

For more control, use individual recipes:

| Recipe | Purpose |
|--------|---------|
| `MigrateFromOpenRewriteAnnotations` | Migrate OpenRewrite-specific annotations |
| `MigrateToJSpecify` | Migrate to JSpecify annotations |
| `AnnotateNullableMethods` | Add @Nullable to methods that may return null |
| `NullableOnMethodReturnType` | Move @Nullable to correct position |

### Example: Just Migrate Annotations

```groovy
rewrite {
    activeRecipe('org.openrewrite.java.jspecify.MigrateToJSpecify')
}
```

## Manual Steps After Migration

1. **Create package-info.java files**: OpenRewrite doesn't create these automatically

2. **Review removed @Nonnull**: Ensure they were truly redundant

3. **Check generics**: OpenRewrite may not handle complex generic nullability

4. **Add missing @Nullable**: Review return values and parameters

5. **Run NullAway**: Enable NullAway to catch remaining issues

## Troubleshooting

### Recipe Not Found

Ensure the rewrite-rewrite dependency is added:

```groovy
dependencies {
    rewrite('org.openrewrite.recipe:rewrite-rewrite:0.19.0')
}
```

### Import Not Migrated

Some custom @Nullable annotations may need manual migration. Check for:

- Custom project annotations
- Uncommon third-party annotations

### Type-Use Position Issues

If @Nullable ends up in wrong position:

```java
// Wrong
public @Nullable String[] getItems() { }

// Correct for nullable array
public String @Nullable [] getItems() { }

// Correct for nullable elements
public @Nullable String[] getItems() { }
```

Review array and generic annotations manually.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/nullability-control/references/migration-openrewrite.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

