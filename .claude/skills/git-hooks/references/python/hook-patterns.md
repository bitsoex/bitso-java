# Python Hook Patterns

Pre-commit and pre-push hook patterns for Python projects.

## Pre-Commit Hook

```bash
#!/bin/bash
set -e

echo "🔍 Python Pre-Commit Checks"

# Activate venv if exists
[ -f "venv/bin/activate" ] && source venv/bin/activate
[ -f ".venv/bin/activate" ] && source .venv/bin/activate

# Black formatting
echo "   • Formatting..."
if ! black --check src/ tests/ 2>/dev/null; then
  black src/ tests/
  git diff --name-only --cached -- src/ tests/ | xargs git add 2>/dev/null || true
fi

# isort
if ! isort --check-only src/ tests/ 2>/dev/null; then
  isort src/ tests/
  git diff --name-only --cached -- src/ tests/ | xargs git add 2>/dev/null || true
fi

# Flake8
echo "   • Linting..."
flake8 src/ tests/

# mypy (optional)
echo "   • Type checking..."
mypy src/ --ignore-missing-imports 2>/dev/null || true

# Tests
echo "   • Testing..."
pytest tests/ -q --tb=no

echo "✅ Pre-commit checks passed"
```

## Pre-Push Hook

```bash
#!/bin/bash
set -e

echo "🔍 Python Pre-Push Checks"

# Activate venv
[ -f "venv/bin/activate" ] && source venv/bin/activate
[ -f ".venv/bin/activate" ] && source .venv/bin/activate

# Full lint
flake8 src/ tests/

# Type check
mypy src/ --strict 2>/dev/null || mypy src/ --ignore-missing-imports

# Tests with coverage
pytest tests/ --cov=src --cov-fail-under=80

# Security (blocking - fails push if issues found)
bandit -r src/ -ll

echo "✅ Pre-push checks passed"
```

## Using pre-commit Framework

```yaml
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/psf/black
    rev: 25.1.0
    hooks:
      - id: black

  - repo: https://github.com/pycqa/isort
    rev: 7.0.0
    hooks:
      - id: isort
        args: ["--profile", "black"]

  - repo: https://github.com/pycqa/flake8
    rev: 7.3.0
    hooks:
      - id: flake8

  - repo: https://github.com/pre-commit/mirrors-mypy
    rev: v1.19.1
    hooks:
      - id: mypy
```

## pyproject.toml

```toml
[tool.black]
line-length = 100

[tool.isort]
profile = "black"

[tool.pytest.ini_options]
testpaths = ["tests"]

[tool.coverage.report]
fail_under = 80
```

## Tools

| Tool | Purpose |
|------|---------|
| `black` | Code formatter |
| `isort` | Import sorter |
| `flake8` | Linter |
| `mypy` | Type checker |
| `pytest` | Test runner |
| `bandit` | Security scanner |
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/skills/git-hooks/references/python/hook-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

