#!/bin/bash
# systemd 前台启动（Restart=always 时使用）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

# shellcheck disable=SC1091
source "$ROOT/deploy/common.sh"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

setup_java_home

export DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD CORS_ORIGINS

SERVER_PORT="${SERVER_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m}"

JAR=$(ls -1 "$ROOT/server/target/message-hosting-"*.jar 2>/dev/null | head -1)
if [[ -z "$JAR" ]]; then
  echo "未找到 jar: $ROOT/server/target/message-hosting-*.jar" >&2
  exit 1
fi

cd "$ROOT"
exec java $JAVA_OPTS \
  -Dspring.profiles.active=prod \
  -jar "$JAR" \
  --server.port="$SERVER_PORT"
