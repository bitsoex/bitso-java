# Go Hook Patterns

Pre-commit and pre-push hook patterns for Go projects.

## Pre-Commit Hook

```bash
#!/bin/bash
set -e

echo "🔍 Go Pre-Commit Checks"

# Format
echo "   • Formatting..."
UNFORMATTED=$(gofmt -l .)
if [ -n "$UNFORMATTED" ]; then
  gofmt -w .
  echo "$UNFORMATTED" | xargs -r git add
fi

# go vet
echo "   • Static analysis..."
go vet ./...

# golangci-lint
echo "   • Linting..."
if command -v golangci-lint &> /dev/null; then
  golangci-lint run --fast
fi

# Build
echo "   • Building..."
go build ./...

# Tests
echo "   • Testing..."
go test ./... -short

echo "✅ Pre-commit checks passed"
```

## Pre-Push Hook

```bash
#!/bin/bash
set -e

echo "🔍 Go Pre-Push Checks"

# Full lint
if command -v golangci-lint &> /dev/null; then
  golangci-lint run
fi

# Tests with race detector
go test ./... -race

# Coverage
go test ./... -coverprofile=coverage.out
COVERAGE=$(go tool cover -func=coverage.out | grep total | awk '{print $3}' | sed 's/%//')
echo "Coverage: ${COVERAGE}%"

# Security (warns but doesn't block)
if command -v gosec &> /dev/null; then
  if ! gosec -quiet ./...; then
    echo "⚠️  Security issues found - review before pushing"
  fi
fi

rm -f coverage.out
echo "✅ Pre-push checks passed"
```

## golangci-lint Configuration

```yaml
# .golangci.yml
run:
  timeout: 5m

linters:
  enable:
    - errcheck
    - gosimple
    - govet
    - ineffassign
    - staticcheck
    - gofmt
    - goimports
    - gosec

issues:
  exclude-rules:
    - path: _test\.go
      linters:
        - errcheck
        - gosec
```

## Makefile

```makefile
.PHONY: fmt vet lint test

fmt:
	gofmt -w .

vet:
	go vet ./...

lint:
	golangci-lint run

test:
	go test ./... -v

pre-commit: fmt vet lint test
```

## Tools

| Tool | Purpose |
|------|---------|
| `gofmt` | Formatter |
| `go vet` | Static analysis |
| `golangci-lint` | Meta-linter |
| `gosec` | Security scanner |
| `govulncheck` | Vulnerability checker |

## Install Tools

```bash
go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest
go install github.com/securego/gosec/v2/cmd/gosec@latest
go install golang.org/x/vuln/cmd/govulncheck@latest
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/git-hooks/references/go/hook-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

