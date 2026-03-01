#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_GRPC_JAR="$HOME/.m2/repository/tech/kayys/wayang/wayang-schema-grpc/1.0.0-SNAPSHOT/wayang-schema-grpc-1.0.0-SNAPSHOT.jar"

if [ ! -f "$SCHEMA_GRPC_JAR" ]; then
  echo "Required dependencies not installed. Running build-all.sh first..."
  WAYANG_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
  chmod +x "$WAYANG_ROOT/build-all.sh"
  "$WAYANG_ROOT/build-all.sh"
fi

cd "$SCRIPT_DIR"
"$SCRIPT_DIR/mvnw" quarkus:dev -Dquarkus.http.port=8081 -DskipTests -U