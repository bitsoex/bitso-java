#!/bin/bash
# Secret Guard Hook (beforeShellExecution)
# Blocks commands that could expose secrets. Pure bash, no external deps.
#
# Cloud-agent-only: requires BITSO_CLOUD_AGENT=1 (set by install.sh in the
# Cursor cloud environment). When unset, all commands are allowed — this lets
# local developers use env, .zshrc, etc. without interference.
#
# Input:  JSON on stdin: {"command":"...","cwd":"...","hook_event_name":"..."}
# Output: {"permission":"allow"} or {"permission":"deny","user_message":"...","agent_message":"..."}

set -uo pipefail

# Skip entirely outside cloud agent environments
if [ "${BITSO_CLOUD_AGENT:-}" != "1" ]; then
    cat > /dev/null
    printf '{"permission":"allow"}\n'
    exit 0
fi

INPUT=$(cat)
COMMAND=""
if [[ "$INPUT" =~ \"command\"[[:space:]]*:[[:space:]]*\"([^\"]*) ]]; then
    COMMAND="${BASH_REMATCH[1]}"
fi

[ -z "$COMMAND" ] && printf '{"permission":"allow"}\n' && exit 0

DENY_REASON=""
check_pattern() {
    printf '%s\n' "$COMMAND" | grep -qiE "$1" && DENY_REASON="$2" && return 0
    return 1
}

check_pattern '^\s*env(\s|$)'                     'env dump' ||
check_pattern '^\s*printenv\b'                    'env dump' ||
check_pattern '^\s*export\s*$'                    'env dump' ||
check_pattern '^\s*set\s*$'                       'env dump' ||
check_pattern '\bdeclare\s+-[xp]'                 'env dump' ||
check_pattern '\benv\b.*\|'                       'env pipe' ||
check_pattern '\$\{?GITHUB_TOKEN\}?'              'GITHUB_TOKEN reference' ||
check_pattern '\$\{?GITHUB_ACTOR\}?'              'GITHUB_ACTOR reference' ||
check_pattern '\$\{?[A-Z0-9_]+_TOKEN\}?'          '_TOKEN variable reference' ||
check_pattern '\$\{?[A-Z0-9_]+_SECRET\}?'         '_SECRET variable reference' ||
check_pattern '\$\{?[A-Z0-9_]+_PASSWORD\}?'       '_PASSWORD variable reference' ||
check_pattern '\$\{?[A-Z0-9_]+_CREDENTIAL[S]?\}?' '_CREDENTIAL variable reference' ||
check_pattern '\$\{?[A-Z0-9_]+_API_KEY\}?'        '_API_KEY variable reference' ||
check_pattern '\$\{?[A-Z0-9_]+_PRIVATE_KEY\}?'    '_PRIVATE_KEY variable reference' ||
check_pattern '\.bashrc'                          'shell config file' ||
check_pattern '\.zshrc'                           'shell config file' ||
check_pattern '\.bash_profile'                    'shell config file' ||
check_pattern '\.bash_history'                    'shell history file' ||
check_pattern '\.zsh_history'                     'shell history file' ||
check_pattern '\.bash_aliases'                    'shell config file' ||
check_pattern '/etc/environment'                  'system env file' ||
check_pattern '/etc/profile'                      'system env file' ||
check_pattern '\.netrc'                           'credential file' ||
check_pattern '\.docker/config\.json'             'credential file' ||
check_pattern '\.git-credentials'                 'credential file' ||
check_pattern '\.ssh/'                            'SSH key directory' ||
check_pattern '\.aws/'                            'AWS credential directory' ||
check_pattern '\.config/gh/'                      'GitHub CLI credential directory' ||
check_pattern '\bgrep\b.*\bgithub\b'              'secret keyword search' ||
check_pattern '\bgrep\b.*\btoken\b'               'secret keyword search' ||
check_pattern '\bgrep\b.*\bsecret\b'              'secret keyword search' ||
check_pattern '\bgrep\b.*\bpassword\b'            'secret keyword search' ||
check_pattern '\bgrep\b.*\bcredential\b'          'secret keyword search' ||
check_pattern '\brg\b.*\bgithub\b'                'secret keyword search' ||
check_pattern '\brg\b.*\btoken\b'                 'secret keyword search' ||
check_pattern '\brg\b.*\bsecret\b'                'secret keyword search' ||
check_pattern '\brg\b.*\bpassword\b'              'secret keyword search' ||
check_pattern '/proc/[0-9]+/environ'              'process env access' ||
check_pattern '/proc/self/environ'                'process env access' ||
check_pattern '\bgh\s+auth\s+(token|status)\b'    'gh auth access' ||
check_pattern '\bgh\s+config\s+(get|list)\b'      'gh config access' ||
true

if [ -n "$DENY_REASON" ]; then
    MSG="[secret-guard] Blocked: ${DENY_REASON} — command not allowed to prevent secret exposure."
    printf '{"permission":"deny","user_message":"%s","agent_message":"%s"}\n' "$MSG" "$MSG"
    exit 0
fi

printf '{"permission":"allow"}\n'
exit 0
