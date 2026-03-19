#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="wayang"
WAYANG_HOME="${WAYANG_HOME:-$HOME/.wayang}"
WAYANG_GOLLEK_HOME="${WAYANG_GOLLEK_HOME:-${GOLLEK_HOME:-$WAYANG_HOME/gollek}}"
LEGACY_GOLLEK_HOME="${GOLLEK_HOME:-$HOME/.gollek}"
WAYANG_BIN="${WAYANG_BIN:-}"
SYSTEMD_USER_DIR="${XDG_CONFIG_HOME:-$HOME/.config}/systemd/user"
UNIT_FILE="${SYSTEMD_USER_DIR}/${SERVICE_NAME}.service"

if ! command -v systemctl >/dev/null 2>&1; then
  echo "systemctl is not available on this machine." >&2
  exit 1
fi

if [ -z "$WAYANG_BIN" ]; then
  for candidate in \
    "$WAYANG_HOME/bin/wayang" \
    "$WAYANG_HOME/bin/wayang-standalone-linux-x86_64" \
    "$WAYANG_HOME/bin/wayang-standalone-linux-aarch_64" \
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
if ! mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage" 2>/dev/null; then
  if [ "$WAYANG_GOLLEK_HOME" != "$LEGACY_GOLLEK_HOME" ]; then
    echo "Unable to use $WAYANG_GOLLEK_HOME, falling back to $LEGACY_GOLLEK_HOME"
    WAYANG_GOLLEK_HOME="$LEGACY_GOLLEK_HOME"
    mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage"
  else
    echo "Unable to create Gollek directories at $WAYANG_GOLLEK_HOME" >&2
    exit 1
  fi
fi
mkdir -p "$SYSTEMD_USER_DIR"

cat > "$UNIT_FILE" << UNIT
[Unit]
Description=Wayang Standalone Service
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
Environment=WAYANG_HOME=${WAYANG_HOME}
Environment=WAYANG_GOLLEK_HOME=${WAYANG_GOLLEK_HOME}
Environment=GOLLEK_HOME=${WAYANG_GOLLEK_HOME}
WorkingDirectory=${WAYANG_HOME}
ExecStart=${WAYANG_BIN}
Restart=on-failure
RestartSec=5
StandardOutput=append:${WAYANG_HOME}/logs/service.log
StandardError=append:${WAYANG_HOME}/logs/service-error.log

[Install]
WantedBy=default.target
UNIT

systemctl --user daemon-reload
systemctl --user enable --now "$SERVICE_NAME"

echo "Wayang user service installed: $UNIT_FILE"
echo "Check status with: systemctl --user status $SERVICE_NAME"
