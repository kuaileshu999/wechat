#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ -f "$DEPLOY_DIR/common.sh" ]]; then
  # shellcheck disable=SC1091
  source "$DEPLOY_DIR/common.sh"
elif [[ -f "$REPO_ROOT/deploy/common.sh" ]]; then
  # shellcheck disable=SC1091
  source "$REPO_ROOT/deploy/common.sh"
else
  echo "错误: 未找到 common.sh"
  exit 1
fi

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

setup_java_home

echo "==> 打包自习室后端 (JAVA_HOME=$JAVA_HOME) ..."
cd "$ROOT/backend"
mvn -q package -DskipTests
echo "✓ 产物: $ROOT/backend/target/study-room-backend-1.0.0.jar"
