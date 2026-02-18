#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_SRC="$ROOT_DIR/core/wayang-error-spi/src/main/java"
OUTPUT_FILE="$ROOT_DIR/docs/error-codes.md"
BUILD_DIR="${TMPDIR:-/tmp}/wayang-error-codes"

mkdir -p "$BUILD_DIR"

JAVAC_BIN=""
JAVA_BIN=""

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/javac" && -x "${JAVA_HOME}/bin/java" ]]; then
  JAVAC_BIN="${JAVA_HOME}/bin/javac"
  JAVA_BIN="${JAVA_HOME}/bin/java"
else
  JAVAC_BIN="$(command -v javac || true)"
  JAVA_BIN="$(command -v java || true)"
fi

if [[ -z "$JAVAC_BIN" || -z "$JAVA_BIN" ]]; then
  echo "Error: javac/java not found. Please set JAVA_HOME or add Java to PATH." >&2
  exit 1
fi

"$JAVAC_BIN" \
  -d "$BUILD_DIR" \
  "$API_SRC/tech/kayys/wayang/error/ErrorCode.java" \
  "$API_SRC/tech/kayys/wayang/error/ErrorCodeDoc.java"

"$JAVA_BIN" -cp "$BUILD_DIR" tech.kayys.wayang.error.ErrorCodeDoc > "$OUTPUT_FILE"

echo "Wrote $OUTPUT_FILE"
