# Java/Gradle Quality Gate Setup

Complete configuration for enabling mise and hk in Java/Gradle projects.

## Prerequisites

- JDK 17+ installed
- Gradle wrapper (`./gradlew`) available
- Spotless plugin configured in build.gradle (recommended)

## Complete hk.pkl Configuration

```pkl
amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

// Global settings
fail_fast = true
exclude = List("build/", ".gradle/", "*.min.js", "node_modules/")

// Java-specific linters
local javaLinters = new Mapping<String, Step> {
  ["spotless-check"] {
    glob = List("**/*.java", "**/*.kt", "**/*.gradle", "**/*.gradle.kts")
    check = "./gradlew spotlessCheck --quiet"
    fix = "./gradlew spotlessApply --quiet"
    workspace_indicator = "build.gradle"
  }
  ["checkstyle"] {
    glob = List("**/*.java")
    check = "./gradlew checkstyleMain checkstyleTest --quiet"
    workspace_indicator = "build.gradle"
    profile = "ci"  // Only in CI
  }
}

// Security linters
local securityLinters = new Mapping<String, Step> {
  ["no-commit-to-branch"] = Builtins.no_commit_to_branch
  ["detect-private-key"] = Builtins.detect_private_key
  ["check-merge-conflict"] = Builtins.check_merge_conflict
}

// Hygiene linters
local hygieneLinters = new Mapping<String, Step> {
  ["trailing-whitespace"] = Builtins.trailing_whitespace
  ["newlines"] = Builtins.newlines
  ["check-added-large-files"] = Builtins.check_added_large_files
}

// YAML/Config linters
local configLinters = new Mapping<String, Step> {
  ["yamllint"] = Builtins.yamllint
  ["actionlint"] = Builtins.actionlint
}

hooks {
  ["pre-commit"] {
    stash = "git"
    steps {
      // Security first
      ...securityLinters

      // Hygiene
      ...hygieneLinters

      // Java (fast checks only)
      ["spotless-check"] = javaLinters["spotless-check"]

      // Config files
      ...configLinters
    }
  }

  ["pre-push"] {
    steps {
      // Full Gradle checks
      ["gradle-check"] {
        glob = List("**/*.java", "**/*.kt")
        check = "./gradlew check --quiet"
        workspace_indicator = "build.gradle"
      }

      // Tests
      ["gradle-test"] {
        glob = List("**/*.java", "**/*.kt")
        check = "./gradlew test --quiet"
        workspace_indicator = "build.gradle"
        profile = "slow"  // Skip with HK_PROFILE=!slow
      }
    }
  }

  ["ci"] {
    steps {
      // All checks including slow ones
      ...securityLinters
      ...hygieneLinters
      ...javaLinters
      ...configLinters

      ["gradle-build"] {
        check = "./gradlew build --quiet"
        workspace_indicator = "build.gradle"
      }
    }
  }
}
```

## Complete mise.toml Configuration

```toml
min_version = "2026.2.5"

[env]
# hk integration
HK_MISE = "1"
HK_LOG = "warn"
HK_LOG_FILE = ".hk-logs/hk.log"

# Hook mode
BITSO_MISE_MODE = "full"

# Java settings
JAVA_HOME = "{{env.JAVA_HOME}}"

[tools]
java = "17"

[tasks]
# Linting
lint = "hk check"
"lint:fix" = "hk fix"

# Gradle tasks
check = "./gradlew check"
test = "./gradlew test"
build = "./gradlew build"
spotless = "./gradlew spotlessApply"

# Hooks
"hook:precommit" = "hk run pre-commit"
"hook:prepush" = "hk run pre-push"
"hook:ci" = "hk run ci"
```

## Spotless Configuration

If not already configured, add Spotless to `build.gradle`:

```groovy
plugins {
    id 'com.diffplug.spotless' version '6.25.0'
}

spotless {
    java {
        target 'src/**/*.java'
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlin {
        target 'src/**/*.kt'
        ktlint()
    }

    groovyGradle {
        target '*.gradle', '**/*.gradle'
        greclipse()
    }
}
```

## Gradle Daemon Considerations

For faster hook execution, ensure Gradle daemon is running:

```bash
# Start daemon
./gradlew --daemon

# Check daemon status
./gradlew --status
```

Add to `gradle.properties`:

```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

## Monorepo Configuration

For monorepos with multiple Gradle projects:

```pkl
["spotless-check"] {
  glob = List("**/*.java", "**/*.kt")
  check = "./gradlew spotlessCheck --quiet -p {{workspace}}"
  fix = "./gradlew spotlessApply --quiet -p {{workspace}}"
  workspace_indicator = "build.gradle"
}
```

## Common Issues

### Gradle Wrapper Not Found

```bash
# Generate wrapper
gradle wrapper --gradle-version 8.5

# Make executable
chmod +x gradlew
```

### Spotless Fails on Generated Files

Add exclusions in build.gradle:

```groovy
spotless {
    java {
        target 'src/**/*.java'
        targetExclude 'src/generated/**'
    }
}
```

### Slow Pre-commit

Move heavy checks to pre-push:

```pkl
["gradle-check"] {
  profile = "slow"  // Skip in pre-commit
}
```

Run with: `HK_PROFILE=!slow hk run pre-commit`
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/enable-quality-gate/references/java/setup.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

