#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

export DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD CORS_ORIGINS

echo "==> 打包后端 (profile=prod) ..."
cd "$ROOT/server"
mvn -q package -DskipTests

JAR=$(ls -1 target/message-hosting-*.jar 2>/dev/null | head -1)
if [[ -z "$JAR" ]]; then
  echo "打包失败，未找到 jar"
  exit 1
fi
echo "✓ 后端 jar: $ROOT/server/$JAR"
