# Installing the Bitso Documentation Linter

Three installation methods are available: Homebrew (recommended), Docker, or Dev Container.

## Contents

- [Homebrew (Recommended)](#homebrew-recommended)
- [Docker](#docker)
- [Dev Container (For Development)](#dev-container-for-development)
- [Verifying Installation](#verifying-installation)
- [Troubleshooting](#troubleshooting)
- [Node.js API](#nodejs-api)
- [See Also](#see-also)

---
## Homebrew (Recommended)

Best for end users on macOS/Linux.

### Prerequisites

- Homebrew installed
- GitHub Personal Access Token with `read:packages` scope

### Steps

```bash
# 1. Set GitHub token
export HOMEBREW_GITHUB_API_TOKEN=your-token

# 2. Add tap
brew tap bitsoex/homebrew-bitso

# 3. Update and install
brew update
brew install bitso-documentation-linter

# 4. Verify
doclinter version
```

### Creating a GitHub Token

1. Go to [GitHub Settings > Developer Settings > Personal Access Tokens](https://github.com/settings/tokens)
2. Create token with `read:packages` scope
3. Copy and use as `HOMEBREW_GITHUB_API_TOKEN`

### Updating

```bash
brew update
brew upgrade bitso-documentation-linter
```

## Docker

Best for cross-platform or CI/CD environments.

### Prerequisites

- Docker installed and running
- GitHub token for private registry (optional)

### Steps

```bash
# 1. Login (if needed)
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# 2. Pull
docker pull ghcr.io/bitsoex/bitso-documentation-linter:latest

# 3. Run
docker run --rm -v $(pwd):/workspace ghcr.io/bitsoex/bitso-documentation-linter:latest \
  --repo-path /workspace
```

### Creating an Alias

Add to `~/.bashrc` or `~/.zshrc`:

```bash
alias doclinter='docker run --rm -v $(pwd):/workspace ghcr.io/bitsoex/bitso-documentation-linter:latest'
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'
services:
  doclinter:
    image: ghcr.io/bitsoex/bitso-documentation-linter:latest
    volumes:
      - .:/workspace
    command: --repo-path /workspace --verbose
```

Run: `docker-compose run --rm doclinter`

## Dev Container (For Development)

Best for contributors modifying the linter.

### Prerequisites

- Docker Desktop
- VS Code or Cursor with Dev Containers extension

### Steps

1. Clone the linter repository
2. Open in VS Code
3. Press `Cmd+Shift+P` → "Dev Containers: Reopen in Container"
4. Wait for container build (5-10 min first time)

The container includes:
- Go 1.25 (or latest stable)
- All development tools (gopls, golangci-lint, etc.)
- Pre-commit hooks
- Test coverage reporting

### Quick Start in Dev Container

```bash
make dev       # Full development workflow
make test      # Run tests
./bin/doclinter --repo-path . --verbose
```

## Verifying Installation

```bash
# Check version
doclinter version

# Check help
doclinter --help

# Run on current directory
doclinter --repo-path . --verbose

# Preview Confluence tree
doclinter tree --repo-path .
```

## Troubleshooting

### "doclinter not found"

```bash
# Homebrew
which doclinter
brew reinstall bitso-documentation-linter

# Docker
docker image inspect ghcr.io/bitsoex/bitso-documentation-linter:latest
```

### "permission denied"

```bash
chmod +x $(which doclinter)
```

### Authentication errors

```bash
# Homebrew
echo $HOMEBREW_GITHUB_API_TOKEN  # Should be set

# Docker
docker login ghcr.io -u YOUR_USERNAME
```

## Node.js API

For programmatic access, use the skills module:

```javascript
import { rfc37 } from './.scripts/lib/skills/index.ts';

// Check if installed
const status = await rfc37.checkLinterInstalled();
console.log(status);  // { installed: true, method: 'homebrew', version: '...' }

// Run validation
const result = await rfc37.validate('./my-repo');
console.log(result);  // { passed: true, errors: [], warnings: [] }

// Run linter
const lintResult = await rfc37.runLinter('./my-repo', { verbose: true });
console.log(lintResult);  // { passed: true, output: '...' }
```

## See Also

- [Official Installation Guide](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/how-tos/local-execution.md)
- [Architecture Overview](https://github.com/bitsoex/bitso-documentation-linter/blob/main/docs/bitso-documentation-linter/concepts/architecture-overview.md)
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/doc-validation-rfc-37/references/installation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

