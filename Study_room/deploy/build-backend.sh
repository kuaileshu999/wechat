#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

# shellcheck disable=SC1091
source "$REPO_ROOT/deploy/common.sh"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

setup_java_home

echo "==> 打包自习室后端 (JAVA_HOME=$JAVA_HOME) ..."
cd "$ROOT/backend"
mvn -q package -DskipTests
echo "✓ 产物: $ROOT/backend/target/study-room-backend-1.0.0.jar"
