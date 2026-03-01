#!/usr/bin/env bash
set -euo pipefail

LABEL="tech.kayys.wayang"
WAYANG_HOME="${WAYANG_HOME:-$HOME/.wayang}"
WAYANG_GOLLEK_HOME="${WAYANG_GOLLEK_HOME:-$WAYANG_HOME/gollek}"
LEGACY_GOLLEK_HOME="${GOLLEK_HOME:-$HOME/.gollek}"
WAYANG_BIN="${WAYANG_BIN:-}"
PLIST_DIR="$HOME/Library/LaunchAgents"
PLIST_FILE="$PLIST_DIR/${LABEL}.plist"

if [ -z "$WAYANG_BIN" ]; then
  for candidate in \
    "$WAYANG_HOME/bin/wayang" \
    "$WAYANG_HOME/bin/wayang-standalone-osx-aarch_64" \
    "$WAYANG_HOME/bin/wayang-standalone-osx-x86_64" \
    "$HOME/.local/bin/wayang"; do
    if [ -x "$candidate" ]; then
      WAYANG_BIN="$candidate"
      break
    fi
  done
fi

if [ -z "$WAYANG_BIN" ] && command -v wayang >/dev/null 2>&1; then
  WAYANG_BIN="$(command -v wayang)"
fi

if [ -z "$WAYANG_BIN" ] || [ ! -x "$WAYANG_BIN" ]; then
  echo "Unable to find Wayang executable. Set WAYANG_BIN explicitly." >&2
  exit 1
fi

mkdir -p "$WAYANG_HOME" "$WAYANG_HOME/config" "$WAYANG_HOME/logs" "$WAYANG_HOME/plugins" "$WAYANG_HOME/secrets"
if ! mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage"; then
  WAYANG_GOLLEK_HOME="$LEGACY_GOLLEK_HOME"
  mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage"
fi
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
