#!/bin/bash
# Credential Scrubber for Cursor Cloud Agent Environments
#
# Removes residual GitHub tokens that Cursor may inject into the environment.
# Designed to run both on-demand (from start.sh) and as a cron job (every minute)
# to catch tokens injected after start.sh completes.
#
# What it cleans:
#   - Git remote URLs with embedded access tokens (ghs_*, ghp_*)
#   - Global git config insteadOf rewrite rules containing tokens
#   - Plaintext credential stores (~/.git-credentials, ~/.netrc)
#
# Usage:
#   bash scrub-credentials.sh          # run once
#   * * * * * /path/to/scrub-credentials.sh  # cron (every minute)

set -uo pipefail

LOG="/tmp/scrub-credentials.log"
CHANGED=false

log() {
    echo "$(date -u '+%Y-%m-%dT%H:%M:%SZ') $1" >> "$LOG"
}

# 1. Sanitize git remote URLs — strip embedded access tokens
#    Pattern: https://x-access-token:<token>@github.com/org/repo
#    Desired: https://github.com/org/repo
for REMOTE in $(git remote 2>/dev/null); do
    URL=$(git remote get-url "$REMOTE" 2>/dev/null) || continue
    if echo "$URL" | grep -qE 'https://[^@]+@github\.com'; then
        CLEAN_URL=$(echo "$URL" | sed -E 's|https://[^@]+@github\.com|https://github.com|')
        git remote set-url "$REMOTE" "$CLEAN_URL"
        log "Sanitized remote '$REMOTE'"
        CHANGED=true
    fi
done

# 2. Remove global git URL rewrite rules that embed tokens
#    These look like: url.https://x-access-token:<tok>@github.com/.insteadOf
for KEY in $(git config --global --list 2>/dev/null \
    | grep -iE '^url\.https?://.*@github\.com.*\.insteadof=' \
    | cut -d= -f1); do
    git config --global --unset-all "$KEY" 2>/dev/null || true
    log "Removed gitconfig rewrite rule: $KEY"
    CHANGED=true
done

# Also remove the url section headers themselves if they contain tokens
for SECTION in $(git config --global --list 2>/dev/null \
    | grep -iE '^url\.https?://[^.]*@' \
    | cut -d. -f1-2 | sort -u); do
    git config --global --remove-section "$SECTION" 2>/dev/null || true
    log "Removed gitconfig section: $SECTION"
    CHANGED=true
done

# 3. Clear plaintext credential stores
for FILE in ~/.git-credentials ~/.netrc; do
    if [ -f "$FILE" ]; then
        rm -f "$FILE"
        log "Removed $FILE"
        CHANGED=true
    fi
done

if [ "$CHANGED" = "true" ]; then
    # Re-establish git credential helper via gh CLI so git operations
    # authenticate through GITHUB_TOKEN instead of embedded tokens.
    if command -v gh &> /dev/null; then
        if gh auth setup-git >/dev/null 2>&1; then
            log "Re-established gh credential helper"
        else
            log "WARNING: Failed to re-establish gh credential helper"
        fi
    fi
    log "Scrub completed — credentials removed"
fi
