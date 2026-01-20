#!/bin/bash
# =============================================================================
# Bitso Git Hooks Bridge
# =============================================================================
# This is a BRIDGE hook that:
# 1. Runs our informative quality checks (never blocks)
# 2. Calls any existing hook systems (Husky, pre-commit, lefthook, etc.)
#
# The bridge ensures both our checks AND existing project hooks run.
# Our checks are informative (always exit 0), but existing hooks may block.
#
# Usage:
#   hooks-bootstrap.sh pre-commit
#   hooks-bootstrap.sh pre-push
# =============================================================================

# Don't use set -e - we need to always exit 0 for OUR checks (informative mode)
# But we DO propagate exit codes from existing hooks
set -uo pipefail

HOOK_TYPE="${1:-pre-commit}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output (if terminal supports it)
if [ -t 1 ] && [ -z "${NO_COLOR:-}" ]; then
    YELLOW='\033[1;33m'
    GREEN='\033[0;32m'
    DIM='\033[0;2m'
    NC='\033[0m' # No Color
else
    YELLOW=''
    GREEN=''
    DIM=''
    NC=''
fi

# Show setup required message
show_setup_message() {
    echo ""
    echo "============================================================"
    echo -e "${YELLOW}  Bitso Quality Checks - Setup Required${NC}"
    echo "============================================================"
    echo ""
    echo "  Node.js is required but not available."
    echo "  Please install Node.js 20+ using nvm (Bitso standard):"
    echo ""
    echo -e "  ${DIM}# Step 1: Install nvm (Node Version Manager)${NC}"
    echo "    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash"
    echo "    source ~/.nvm/nvm.sh"
    echo ""
    echo -e "  ${DIM}# Step 2: Install and use Node from .nvmrc${NC}"
    echo "    nvm use"
    echo ""
    echo -e "  ${DIM}# Alternative: fnm (Fast Node Manager)${NC}"
    echo "    brew install fnm && fnm install --lts"
    echo ""
    echo -e "  ${GREEN}Your commit will proceed.${NC} Checks will run once Node is available."
    echo "============================================================"
    echo ""
}

# Source ensure-node.sh to configure Node (loads nvm/fnm if available)
# Run in subshell first to test, then source for real if successful
if ! (source "$SCRIPT_DIR/ensure-node.sh" 2>/dev/null); then
    show_setup_message
    exit 0  # Never block - informative mode
fi

# Source for real to get Node in PATH
# shellcheck source=ensure-node.sh
source "$SCRIPT_DIR/ensure-node.sh" 2>/dev/null

# Verify Node is actually available now
if ! command -v node &>/dev/null; then
    show_setup_message
    exit 0
fi

# Run the JS-based checks
CHECKS_SCRIPT="$SCRIPT_DIR/hooks-checks.js"

if [ ! -f "$CHECKS_SCRIPT" ]; then
    echo -e "${YELLOW}[hooks] Warning: hooks-checks.js not found at $CHECKS_SCRIPT${NC}" >&2
    exit 0
fi

# Run checks - always exit 0 (informative mode)
node "$CHECKS_SCRIPT" "$HOOK_TYPE" || true

# =============================================================================
# BRIDGE: Call existing hook systems
# =============================================================================
# After our informative checks, we call any existing hooks so they still work.
# This ensures we don't break existing workflows (Husky, pre-commit, lefthook).
# Their exit codes are propagated - if they fail, the commit/push is blocked.

EXISTING_HOOK_EXIT=0

# Try Husky hooks (Node.js projects)
if [ -x "$REPO_ROOT/.husky/$HOOK_TYPE" ]; then
    echo -e "${DIM}[hooks] Running existing Husky hook...${NC}"
    "$REPO_ROOT/.husky/$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# Try pre-commit framework (Python-based, uses .pre-commit-config.yaml)
if [ -f "$REPO_ROOT/.pre-commit-config.yaml" ] && command -v pre-commit &>/dev/null; then
    echo -e "${DIM}[hooks] Running existing pre-commit hooks...${NC}"
    pre-commit run --hook-stage "$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# Try lefthook (Go-based, uses lefthook.yml)
if [ -f "$REPO_ROOT/lefthook.yml" ] && command -v lefthook &>/dev/null; then
    echo -e "${DIM}[hooks] Running existing lefthook hooks...${NC}"
    lefthook run "$HOOK_TYPE" || EXISTING_HOOK_EXIT=$?
fi

# Try native git hooks (in .git/hooks/)
NATIVE_HOOK="$REPO_ROOT/.git/hooks/$HOOK_TYPE"
if [ -x "$NATIVE_HOOK" ] && [ ! -L "$NATIVE_HOOK" ]; then
    # Only run if it's executable and not a symlink (avoid recursion)
    echo -e "${DIM}[hooks] Running native git hook...${NC}"
    "$NATIVE_HOOK" || EXISTING_HOOK_EXIT=$?
fi

# Propagate exit code from existing hooks
# Our checks are informative (don't block), but existing hooks can block
exit $EXISTING_HOOK_EXIT
# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → global/skills/git-hooks/assets/hooks-bootstrap.sh
# To modify, edit the source file and run the distribution workflow

