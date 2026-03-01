#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="wayang"
WAYANG_HOME="${WAYANG_HOME:-$HOME/.wayang}"
WAYANG_GOLLEK_HOME="${WAYANG_GOLLEK_HOME:-${GOLLEK_HOME:-$WAYANG_HOME/gollek}}"
WAYANG_BIN="${WAYANG_BIN:-$HOME/.local/bin/wayang}"
SYSTEMD_USER_DIR="${XDG_CONFIG_HOME:-$HOME/.config}/systemd/user"
UNIT_FILE="${SYSTEMD_USER_DIR}/${SERVICE_NAME}.service"

if ! command -v systemctl >/dev/null 2>&1; then
  echo "systemctl is not available on this machine." >&2
  exit 1
fi

mkdir -p "$WAYANG_HOME" "$WAYANG_HOME/config" "$WAYANG_HOME/logs" "$WAYANG_HOME/plugins" "$WAYANG_HOME/secrets"
mkdir -p "$WAYANG_GOLLEK_HOME/models" "$WAYANG_GOLLEK_HOME/storage"
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
