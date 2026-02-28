#!/bin/bash
# Start script for Cursor cloud agent environment
# Runs at the beginning of each session.
#
# Responsibilities:
#   0. Set cloud agent marker (BITSO_CLOUD_AGENT)
#   1. Credential cleanup (remove residual tokens from prior sessions)
#   2. Git identity reset (ensure commits come from AiAgentsBitso)
#   3. Co-author trailer guard (cloud-agent-only commit-msg hook)
#   4. Docker daemon startup
#
# Based on working configs from: https://forum.cursor.com/t/background-agent-docker-in-docker/104112

# =============================================================================
# 0. Cloud Agent Marker
# =============================================================================
# Export BITSO_CLOUD_AGENT so it is available in this script and in all child
# processes (including beforeShellExecution hooks like secret-guard.sh).
# install.sh persists it to /etc/environment and ~/.bashrc, but start.sh runs
# as a non-interactive shell where those files may not yet be sourced.
export BITSO_CLOUD_AGENT=1

# Ensure mise and mise-managed tools (hk) are in PATH.
# start.sh is non-interactive so .bashrc isn't sourced automatically.
export PATH="$HOME/.local/bin:$HOME/.local/share/mise/shims:$PATH"

# Trust mise.toml so mise tasks work without interactive confirmation
if command -v mise &> /dev/null; then
    mise trust 2>/dev/null || true
fi

# =============================================================================
# 1. Credential & Token Cleanup
# =============================================================================
# Run the scrub script immediately, then start a background loop that re-runs
# it every 30 seconds. Cursor injects ghs_ tokens into ~/.gitconfig AFTER
# start.sh completes, so a one-shot scrub is insufficient. Cron is not
# available in the cloud agent container, so we use a background loop instead.

echo ">>> Running credential scrub..."
SCRUB_SCRIPT="$HOME/.local/bin/scrub-credentials.sh"
if [ ! -f "$SCRUB_SCRIPT" ]; then
    REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo "")
    SCRUB_SCRIPT="${REPO_ROOT:-.}/.cursor/scripts/scrub-credentials.sh"
fi
SCRUB_LOOP_PIDFILE="/tmp/bitso-scrub-loop.pid"
if [ -f "$SCRUB_SCRIPT" ]; then
    bash "$SCRUB_SCRIPT"

    # Background scrub loop — catches tokens injected after start.sh
    EXISTING_PID="$(cat "$SCRUB_LOOP_PIDFILE" 2>/dev/null || true)"
    if [ -n "$EXISTING_PID" ] \
       && kill -0 "$EXISTING_PID" 2>/dev/null \
       && ps -p "$EXISTING_PID" -o args= 2>/dev/null | grep -Fq "scrub-credentials.sh"; then
        echo ">>> Background credential scrub loop already running"
    else
        (while true; do sleep 30; bash "$SCRUB_SCRIPT"; done) &
        echo "$!" > "$SCRUB_LOOP_PIDFILE"
        disown
        echo ">>> Background credential scrub loop started (every 30s)"
    fi
else
    echo ">>> WARNING: scrub-credentials.sh not found, skipping credential cleanup"
fi

echo ">>> Credential cleanup complete"

# =============================================================================
# 2. Git Identity & Auth Reset
# =============================================================================
# Force the git author/committer to the service account so commits and PRs
# are always attributed to AiAgentsBitso, regardless of prior session state.
# Then set up gh as the git credential helper so git authenticates through
# GITHUB_TOKEN instead of any embedded tokens.

git config --global user.name "AIAgentsBitso"
git config --global user.email "ai.agents@bitso.com"
echo ">>> Git identity set to AIAgentsBitso <ai.agents@bitso.com>"

if command -v gh &> /dev/null; then
    if gh auth setup-git >/dev/null 2>&1; then
        echo ">>> gh credential helper configured"
    else
        echo ">>> WARNING: failed to configure gh credential helper"
    fi
fi

# =============================================================================
# 3. Co-Author Trailer Guard (cloud-agent-only)
# =============================================================================
# Cursor overrides core.hooksPath to its own directory:
#   ~/.cursor/agent-hooks/<base64-encoded-workspace-path>/
# It uses a ".dispatcher" script that chains *.cursor* hooks, including
# "commit-msg.cursor.co-author" which injects the invoking user's personal
# GitHub noreply email as a Co-authored-by trailer.
#
# TIMING: Cursor creates agent-hooks ~30s AFTER start.sh runs, so
# synchronous patching fails (the directory doesn't exist yet). We solve
# this by launching a background watcher that polls until the hooks
# directory appears, patches it, then exits.
#
# Strategy:
#   a) Background watcher: poll for agent-hooks, overwrite co-author hook,
#      install guard hook, then exit
#   b) Synchronous fallback: install into .git-hooks/ (already exists in repo)

WORKSPACE_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo "")
GUARD_SCRIPT="${WORKSPACE_ROOT}/.cursor/scripts/check-coauthor-trailer.sh"

# 3a. Background watcher — waits for Cursor to create agent-hooks, then patches
_patch_cursor_hooks() {
    local hooks_dir="$1"
    local guard_script="$2"

    local coauthor_hook="$hooks_dir/commit-msg.cursor.co-author"
    cat > "$coauthor_hook" << 'COAUTHOREOF'
#!/bin/bash
# @cursor-managed (overwritten by .cursor/start.sh)
# Replaces personal noreply with the service account co-author.

CO_AUTHOR='AIAgentsBitso <ai.agents@bitso.com>'

if grep -q "Co-authored-by:" "$1"; then
    exit 0
fi

echo "" >> "$1"
echo "" >> "$1"
echo "Co-authored-by: $CO_AUTHOR" >> "$1"

exit 0
COAUTHOREOF
    chmod +x "$coauthor_hook"

    if [ -f "$guard_script" ]; then
        local guard_hook="$hooks_dir/commit-msg.cursor.guard"
        cat > "$guard_hook" << GUARDEOF
#!/bin/bash
# Co-author allowlist guard (injected by .cursor/start.sh)
exec "$guard_script" "\$1"
GUARDEOF
        chmod +x "$guard_hook"
    fi
}

# Export the function so the subshell can use it
export -f _patch_cursor_hooks

(
    for _i in $(seq 1 24); do
        sleep 5
        _raw=$(git config core.hooksPath 2>/dev/null || echo "")
        # shellcheck disable=SC2088
        if [[ "$_raw" == "~/"* ]]; then
            _dir="$HOME/${_raw:2}"
        elif [[ -n "$_raw" && "$_raw" != /* ]]; then
            _dir="$WORKSPACE_ROOT/$_raw"
        else
            _dir="$_raw"
        fi
        if [ -n "$_dir" ] && [ -d "$_dir" ]; then
            _patch_cursor_hooks "$_dir" "$GUARD_SCRIPT"
            git config --global user.name "AIAgentsBitso"
            git config --global user.email "ai.agents@bitso.com"
            echo ">>> [hook-watcher] Hooks patched and git identity re-enforced after ${_i}x5s"
            exit 0
        fi
    done
    echo ">>> [hook-watcher] Cursor agent-hooks never appeared after 120s"
) >> /tmp/bitso-hook-watcher.log 2>&1 &
disown
echo ">>> Background hook watcher started (polls every 5s for up to 120s)"

# 3b. Synchronous fallback: install into .git-hooks/ for repos using hk
FALLBACK_HOOKS_DIR="${WORKSPACE_ROOT}/.git-hooks"
if [ -n "$FALLBACK_HOOKS_DIR" ] && [ -d "$FALLBACK_HOOKS_DIR" ] && [ -f "$GUARD_SCRIPT" ]; then
    COMMIT_MSG_HOOK="$FALLBACK_HOOKS_DIR/commit-msg"
    cat > "$COMMIT_MSG_HOOK" << 'HOOKEOF'
#!/bin/bash
# Git commit-msg hook — delegates to hk, then runs co-author trailer guard.
# This file is generated by .cursor/start.sh for cloud agent environments.

if command -v hk &> /dev/null; then
    hk run commit-msg "$@" || exit $?
fi

SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GUARD="$SCRIPT_DIR/.cursor/scripts/check-coauthor-trailer.sh"
if [ -x "$GUARD" ]; then
    "$GUARD" "$1" || exit $?
fi
HOOKEOF
    chmod +x "$COMMIT_MSG_HOOK"
    echo ">>> Co-author trailer guard installed in .git-hooks/commit-msg (fallback)"
fi

# =============================================================================
# 4. Docker Daemon Startup
# =============================================================================

echo ">>> Starting Docker daemon..."

# Ensure daemon.json config is in place
sudo mkdir -p /etc/docker
sudo cp .cursor/daemon.json /etc/docker/daemon.json 2>/dev/null || true

# Try service first (this works!)
sudo service docker start 2>/dev/null || true
sleep 3

# If service didn't work, start manually WITHOUT flags (config is in daemon.json)
if ! sudo docker info >/dev/null 2>&1; then
    echo ">>> Service start failed, trying manual start..."
    sudo pkill dockerd 2>/dev/null || true
    sleep 1
    # Don't pass flags here - they're already in daemon.json!
    # shellcheck disable=SC2024
    sudo dockerd > /tmp/dockerd.log 2>&1 &
    sleep 5
fi

# Fix permissions so non-root can use docker
# Note: 666 is permissive but acceptable in Cursor agent's isolated environment
sudo chmod 666 /var/run/docker.sock 2>/dev/null || true

# Verify
if docker info >/dev/null 2>&1; then
    echo ">>> Docker daemon started successfully"
    docker --version

    # Authenticate with GitHub Container Registry (ghcr.io) if not already authenticated
    if [ -n "${GITHUB_TOKEN:-}" ]; then
        # Check if already logged in to ghcr.io
        if ! docker login ghcr.io --get-login >/dev/null 2>&1; then
            echo ">>> Authenticating with ghcr.io..."
            GHCR_USER="${GITHUB_ACTOR:-oauth2accesstoken}"
            echo "$GITHUB_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin && \
                echo ">>> Successfully authenticated with ghcr.io" || \
                echo ">>> WARNING: Failed to authenticate with ghcr.io"
        else
            echo ">>> Already authenticated with ghcr.io"
        fi
    fi
else
    echo ">>> Docker daemon failed to start"
    echo ">>> Log:"
    cat /tmp/dockerd.log 2>/dev/null || echo "(no log)"
fi
