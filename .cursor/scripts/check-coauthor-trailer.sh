#!/bin/bash
# Co-Author Trailer Guard (commit-msg hook)
#
# Rejects commits whose Co-authored-by trailers contain emails not in the
# strict allowlist. Only three specific co-author emails are permitted in
# cloud agent commits. Everything else — personal @bitso.com, GitHub
# noreply, estate-catalog bots, dependabot, etc. — is blocked.
#
# Allowed emails (exact match only):
#   - ai.agents@bitso.com                                        (Bitso AI service account)
#   - cursoragent@cursor.com                                     (Cursor's default agent email)
#   - 136622811+coderabbitai[bot]@users.noreply.github.com       (CodeRabbit bot)
#
# This guard runs exclusively in Cursor cloud agent environments. It is
# installed by start.sh into Cursor's agent-hooks and .git-hooks/commit-msg.
# It is NOT part of hk.pkl so local developer workflows are unaffected.
#
# Usage: check-coauthor-trailer.sh <commit-msg-file>

set -uo pipefail

COMMIT_MSG_FILE="${1:-}"

if [ -z "$COMMIT_MSG_FILE" ] || [ ! -f "$COMMIT_MSG_FILE" ]; then
    exit 0
fi

COAUTHORS=$(grep -i '^Co-authored-by:' "$COMMIT_MSG_FILE" | sed -n 's/.*<\([^>]*\)>.*/\1/p') || true
[ -z "$COAUTHORS" ] && exit 0

ALLOWED_EMAILS="ai.agents@bitso.com cursoragent@cursor.com 136622811+coderabbitai[bot]@users.noreply.github.com"

VIOLATIONS=""
while IFS= read -r EMAIL; do
    MATCHED=false
    for ALLOWED in $ALLOWED_EMAILS; do
        if [ "$EMAIL" = "$ALLOWED" ]; then
            MATCHED=true
            break
        fi
    done
    if [ "$MATCHED" = false ]; then
        VIOLATIONS="${VIOLATIONS}  ${EMAIL}\n"
    fi
done <<< "$COAUTHORS"

if [ -n "$VIOLATIONS" ]; then
    echo "ERROR: Unauthorized email(s) found in Co-authored-by trailers:"
    echo -e "$VIOLATIONS"
    echo "Only these co-author emails are allowed in cloud agent commits:"
    echo "  - ai.agents@bitso.com"
    echo "  - cursoragent@cursor.com"
    echo "  - 136622811+coderabbitai[bot]@users.noreply.github.com"
    echo ""
    echo "Remove or replace the unauthorized trailer(s)."
    exit 1
fi

exit 0
