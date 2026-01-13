# Disable VS Code MCP Discovery to Prevent Configuration Conflicts

> Disable VS Code MCP Discovery to Prevent Configuration Conflicts

# Disable VS Code MCP Discovery

## Why Disable MCP Discovery?

VS Code's MCP (Model Context Protocol) discovery feature scans configuration files from multiple applications (Claude Desktop, Windsurf, Cursor). However:

- **Configuration Conflicts**: MCP configurations are similar but have subtle differences between tools, which can cause errors
- **Dedicated Setups**: Each tool (VS Code, Claude Desktop, Cursor, Windsurf) has its own dedicated MCP setup, making cross-discovery unnecessary
- **Unexpected Behavior**: Auto-discovered servers may conflict with your intended VS Code configuration

**Recommendation**: Disable MCP discovery to prevent conflicts.

## Current Status

Check what's currently enabled:

```bash
cat ~/Library/Application\ Support/Code/User/settings.json | grep -A 5 "chat.mcp.discovery.enabled"
```

## Disable All MCP Discovery

This script tries sed first, and falls back to jq if sed fails:

```bash
#!/bin/bash
set -e

SETTINGS_FILE=~/Library/Application\ Support/Code/User/settings.json
BACKUP_FILE="${SETTINGS_FILE}.backup"

# Backup original file
cp "$SETTINGS_FILE" "$BACKUP_FILE"
echo "✅ Backup created: $BACKUP_FILE"

# Try sed approach first
if sed -i '' '/"chat.mcp.discovery.enabled": {/,/}/ {
  s/"claude-desktop": true/"claude-desktop": false/
  s/"windsurf": true/"windsurf": false/
  s/"cursor-global": true/"cursor-global": false/
  s/"cursor-workspace": true/"cursor-workspace": false/
}' "$SETTINGS_FILE" 2>/dev/null; then
    # Verify the change worked
    if grep -q '"claude-desktop": false' "$SETTINGS_FILE"; then
        echo "✅ MCP discovery successfully disabled using sed"
        exit 0
    fi
fi

# Sed failed or didn't apply changes, restore backup and try jq
echo "⚠️  sed approach failed, trying jq..."
cp "$BACKUP_FILE" "$SETTINGS_FILE"

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "❌ Error: jq is not installed and sed failed. Install jq with: brew install jq"
    exit 1
fi

# Use jq approach
TEMP_FILE="${SETTINGS_FILE}.tmp"

if jq '.["chat.mcp.discovery.enabled"] // empty | length' "$SETTINGS_FILE" > /dev/null 2>&1; then
    jq '.["chat.mcp.discovery.enabled"] = {
        "claude-desktop": false,
        "windsurf": false,
        "cursor-global": false,
        "cursor-workspace": false
    }' "$SETTINGS_FILE" > "$TEMP_FILE"
    
    # Atomic move
    mv "$TEMP_FILE" "$SETTINGS_FILE"
    
    # Verify the change
    if jq -e '.["chat.mcp.discovery.enabled"]["claude-desktop"] == false' "$SETTINGS_FILE" > /dev/null; then
        echo "✅ MCP discovery successfully disabled using jq"
    else
        echo "❌ Error: Failed to verify changes"
        cp "$BACKUP_FILE" "$SETTINGS_FILE"
        exit 1
    fi
else
    echo "❌ Error: chat.mcp.discovery.enabled key not found in settings.json"
    cp "$BACKUP_FILE" "$SETTINGS_FILE"
    exit 1
fi
```

## Verify Changes

```bash
cat ~/Library/Application\ Support/Code/User/settings.json | grep -A 5 "chat.mcp.discovery.enabled"
```

Expected output:

```json
"chat.mcp.discovery.enabled": {
    "claude-desktop": false,
    "windsurf": false,
    "cursor-global": false,
    "cursor-workspace": false
}
```

## Reload VS Code

- Open Command Palette: `Cmd+Shift+P`
- Run: `Developer: Reload Window`

## Re-enable Later (If Needed)

### Option 1: VS Code UI

1. Go to: **Code > Settings > Settings**
2. Search for: `chat.mcp.discovery.enabled`
3. Toggle individual sources as needed

### Option 2: Manual Edit

Edit `~/Library/Application Support/Code/User/settings.json` and change `false` to `true` for desired sources.

### Option 3: Restore Backup

```bash
cp ~/Library/Application\ Support/Code/User/settings.json.backup ~/Library/Application\ Support/Code/User/settings.json
```

<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → global/commands/disable-vscode-mcp-discovery.md -->
<!-- To modify, edit the source file and run the distribution workflow -->
