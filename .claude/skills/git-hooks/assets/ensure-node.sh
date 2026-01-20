#!/bin/bash
# Ensures Node.js is available, installing if necessary
# Returns 0 if Node is available, 1 if installation failed
#
# This script is designed to be sourced by hooks-bootstrap.sh
# It follows the Bitso standard Node.js setup workflow:
# 1. Check if Node is already available and meets version requirement
# 2. Look for .nvmrc (standard at Bitso) and use `nvm use`
# 3. Try to activate fnm (Fast Node Manager) as alternative
# 4. (Skip auto-install in hooks - prompt user instead)
#
# Reference: bitso-web, react-web, mobile-multiplatform all use .nvmrc + nvm
#
# NOTE: This script is sourced, so we avoid `set -e` which would affect the parent shell
# and potentially break the "always exit 0" guarantee of informative hooks.

# Don't use set -e since this is sourced and we want the parent to handle errors
set -uo pipefail

REQUIRED_NODE_VERSION="20"  # Minimum major version (Bitso standard)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find repo root by looking for .git directory
# This handles both source location (global/skills/git-hooks/assets/)
# and distributed location (.git-hooks/ or .claude/skills/git-hooks/assets/)
find_repo_root() {
    local dir="$SCRIPT_DIR"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.git" ]; then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    # Fallback to parent of script dir
    echo "$(cd "$SCRIPT_DIR/.." && pwd)"
}
REPO_ROOT="$(find_repo_root)"

log() { echo "[hooks] $*" >&2; }

# Check if Node exists and meets version requirement
check_node() {
    if ! command -v node &>/dev/null; then return 1; fi
    local version
    version=$(node -v | sed 's/v//' | cut -d. -f1)
    [ "$version" -ge "$REQUIRED_NODE_VERSION" ]
}

# Try to activate fnm (Fast Node Manager)
try_fnm() {
    if command -v fnm &>/dev/null; then
        eval "$(fnm env --shell bash)" 2>/dev/null || true
        if fnm use --install-if-missing 2>/dev/null; then
            return 0
        fi
        # Try installing LTS if no .node-version or .nvmrc
        fnm install --lts 2>/dev/null && fnm use lts-latest 2>/dev/null && return 0
    fi
    return 1
}

# Load nvm into the current shell
load_nvm() {
    export NVM_DIR="${NVM_DIR:-$HOME/.nvm}"
    
    # Try to load nvm from various locations
    if [ -s "$NVM_DIR/nvm.sh" ]; then
        # shellcheck source=/dev/null
        source "$NVM_DIR/nvm.sh"
    elif [ -s "/usr/local/opt/nvm/nvm.sh" ]; then
        # Homebrew nvm location (Intel Mac)
        # shellcheck source=/dev/null
        source "/usr/local/opt/nvm/nvm.sh"
    elif [ -s "/opt/homebrew/opt/nvm/nvm.sh" ]; then
        # Homebrew nvm location (Apple Silicon)
        # shellcheck source=/dev/null
        source "/opt/homebrew/opt/nvm/nvm.sh"
    fi
    
    command -v nvm &>/dev/null
}

# Try to activate nvm with .nvmrc (Bitso standard workflow)
# This mirrors the standard `nvm use` command used in bitso-web, react-web, mobile-multiplatform
try_nvm() {
    if ! load_nvm; then
        return 1
    fi
    
    # Check if repo has .nvmrc (standard at Bitso)
    if [ -f "$REPO_ROOT/.nvmrc" ]; then
        log "Found .nvmrc, running 'nvm use'..."
        # Use --silent to avoid noise, install if version not available
        if nvm use 2>/dev/null; then
            return 0
        fi
        # Version not installed, install it
        log "Installing Node version from .nvmrc..."
        nvm install 2>/dev/null && nvm use 2>/dev/null && return 0
    fi
    
    # No .nvmrc, try LTS
    nvm use --lts 2>/dev/null || {
        nvm install --lts 2>/dev/null && nvm use --lts 2>/dev/null
    }
    return 0
}

# Install Node via Homebrew
# NOTE: Auto-install functions (install_via_brew, install_nvm) have been removed
# because they can take minutes and block commits/pushes, breaking the
# "informative mode" guarantee. Users should install Node manually.

# Main logic
ensure_node() {
    # First check if Node is already available
    if check_node; then
        return 0
    fi
    
    log "Node.js not found or version too old (need v$REQUIRED_NODE_VERSION+). Attempting to configure..."
    
    # Try nvm first (Bitso standard - all major repos use .nvmrc + nvm)
    # This matches the workflow in bitso-web, react-web, mobile-multiplatform
    if try_nvm && check_node; then
        log "Node.js configured via nvm"
        return 0
    fi
    
    # Try fnm as alternative (some developers prefer it)
    if try_fnm && check_node; then
        log "Node.js configured via fnm"
        return 0
    fi
    
    # NOTE: We intentionally skip auto-installing via brew/nvm in git hooks
    # because those can take minutes and block commits/pushes.
    # The hooks are informative - if Node isn't available, we just skip checks.
    # Users should install Node manually following the setup message.
    
    log "Node.js not available. Checks will be skipped."
    return 1
}

# Run if sourced or executed directly
ensure_node
# AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY
# Source: bitsoex/ai-code-instructions → global/skills/git-hooks/assets/ensure-node.sh
# To modify, edit the source file and run the distribution workflow

