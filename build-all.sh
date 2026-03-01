#!/bin/bash

# Build all Wayang and Gamelan modules in dependency order
# Run this before starting wayang-runtime-standalone for the first time

set -e

WAYANG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLATFORM_DIR="$(dirname "$WAYANG_DIR")"
GAMELAN_DIR="$PLATFORM_DIR/workflow-gamelan"

echo "================================================"
echo "  Building Wayang Platform - All Modules"
echo "================================================"

# --- 1. Gamelan SDK (exclude broken examples only) ---
if [ -d "$GAMELAN_DIR" ]; then
  echo ""
  echo ">> [1/3] Building Gamelan SDK (skipping examples)..."
  cd "$GAMELAN_DIR"
  # Use excludes for only the known-broken example modules
  mvn install -DskipTests -U \
    -pl "!examples/plugin-system-test,!examples/gamelan-test-client,!examples/gamelan-test-executor,!examples/gamelan-test-standalone,!examples/gamelan-plugin-example"
  echo "Gamelan SDK installed."
else
  echo "[WARN] Gamelan SDK not found at $GAMELAN_DIR, skipping."
fi

# --- 2. Wayang Core + Executors ---
echo ""
echo ">> [2/3] Building Wayang core and executor modules..."
cd "$WAYANG_DIR"
mvn install -DskipTests -U \
  -pl bom,core,\
executors/embedding,\
executors/guardrails,\
executors/prompt,\
executors/vector,\
executors/memory,\
executors/web-search,\
executors/tool \
  --also-make
echo "Wayang modules installed."

# --- 3. Done ---
echo ""
echo "================================================"
echo "  All modules installed successfully!"
echo "  You can now run:"
echo "  cd runtime/wayang-runtime-standalone && ./run-dev.sh"
echo "================================================"
