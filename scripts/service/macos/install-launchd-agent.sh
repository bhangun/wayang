#!/usr/bin/env bash
set -euo pipefail

LABEL="tech.kayys.wayang"
WAYANG_HOME="${WAYANG_HOME:-$HOME/.wayang}"
WAYANG_GOLLEK_HOME="${WAYANG_GOLLEK_HOME:-${GOLLEK_HOME:-$WAYANG_HOME/gollek}}"
WAYANG_BIN="${WAYANG_BIN:-$HOME/.local/bin/wayang}"
PLIST_DIR="$HOME/Library/LaunchAgents"
PLIST_FILE="$PLIST_DIR/${LABEL}.plist"

mkdir -p "$WAYANG_HOME" "$WAYANG_HOME/config" "$WAYANG_HOME/logs" "$WAYANG_HOME/plugins" "$WAYANG_HOME/secrets"
mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage"
mkdir -p "$PLIST_DIR"

cat > "$PLIST_FILE" << PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>${LABEL}</string>
  <key>ProgramArguments</key>
  <array>
    <string>${WAYANG_BIN}</string>
  </array>
  <key>EnvironmentVariables</key>
  <dict>
    <key>WAYANG_HOME</key>
    <string>${WAYANG_HOME}</string>
    <key>WAYANG_GOLLEK_HOME</key>
    <string>${WAYANG_GOLLEK_HOME}</string>
    <key>GOLLEK_HOME</key>
    <string>${WAYANG_GOLLEK_HOME}</string>
  </dict>
  <key>WorkingDirectory</key>
  <string>${WAYANG_HOME}</string>
  <key>RunAtLoad</key>
  <true/>
  <key>KeepAlive</key>
  <true/>
  <key>StandardOutPath</key>
  <string>${WAYANG_HOME}/logs/service.log</string>
  <key>StandardErrorPath</key>
  <string>${WAYANG_HOME}/logs/service-error.log</string>
</dict>
</plist>
PLIST

launchctl unload "$PLIST_FILE" >/dev/null 2>&1 || true
launchctl load "$PLIST_FILE"

echo "Wayang launch agent installed: $PLIST_FILE"
echo "Check status with: launchctl list | grep $LABEL"
