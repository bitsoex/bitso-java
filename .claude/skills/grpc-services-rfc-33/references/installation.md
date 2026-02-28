# gRPC Compliance Tools Installation

Install the tools required for protobuf linting and gRPC compliance validation.

## Tools

| Tool | Purpose | Binaries Installed |
|------|---------|-------------------|
| **Buf CLI** | Protobuf linting with standard and custom rules | `buf` |
| **Bitso gRPC Linter** | gRPC compliance validation and custom buf plugins | `grpc-compliance-validate-repository`, `buf-plugin-rpc-minimal-documentation`, `buf-plugin-no-failure-usage`, `buf-plugin-nested-payload-request-response`, `buf-plugin-method-extractor` |

## Cloud Agents

Pre-installed in Cursor Background Agent environments (no action needed).

## Local Installation

### Homebrew (recommended)

#### Prerequisites

- [Homebrew](https://brew.sh/) installed
- GitHub Personal Access Token with `read:packages` scope

#### Steps

```bash
# 1. Set GitHub token for private formulae
export HOMEBREW_GITHUB_API_TOKEN=your-personal-access-token

# 2. Add Bitso tap (if not already added)
brew tap bitsoex/homebrew-bitso

# 3. Install Bitso gRPC Linter (installs 5 binaries)
brew install bitso-grpc-linter

# 4. Install Buf CLI
brew install bufbuild/buf/buf
```

#### Verification

```bash
grpc-compliance-validate-repository --help
buf --version
```

### Docker (alternative)

The gRPC linter is also available as a Docker image:

```bash
docker pull ghcr.io/bitsoex/bitso-grpc-linter:latest
```

Run with:

```bash
docker run --rm -v "$(pwd):/workspace" \
  ghcr.io/bitsoex/bitso-grpc-linter:latest \
  --dir /workspace
```

## Troubleshooting

### HOMEBREW_GITHUB_API_TOKEN not set

```text
Error: Authentication required for private formula
```

Set the token before running `brew install`:

```bash
export HOMEBREW_GITHUB_API_TOKEN=your-token
```

### AirLock kills the binary

On macOS with AirLock, binaries installed via Homebrew are trusted. If you installed via a different method (e.g., `go install`), the binary may be killed with SIGKILL (exit code 137). Reinstall via Homebrew.

### Buf plugins not found

Ensure `bitso-grpc-linter` is installed (not just `buf`). The custom plugins (`buf-plugin-*`) are bundled with the gRPC linter package.

## Programmatic API

For environments with Node.js and mise (e.g., the `ai-code-instructions` repository itself), a TypeScript readiness check module is available:

```typescript
import { grpcCompliance } from '.scripts/lib/skills';

const status = await grpcCompliance.checkAllToolsInstalled();
if (!status.allInstalled) {
  const result = await grpcCompliance.installGrpcLinter('homebrew');
}
```

This API is not required for using the gRPC skills — the CLI tools work standalone once installed via any method above.
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/grpc-services-rfc-33/references/installation.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

