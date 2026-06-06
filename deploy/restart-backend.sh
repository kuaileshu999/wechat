#!/bin/bash
# 重启 Spring Boot（不依赖宝塔 Java 项目管理器时使用）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

SERVER_PORT="${SERVER_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m}"
LOG_FILE="$ROOT/deploy/app.log"
PID_FILE="$ROOT/deploy/app.pid"

JAR=$(ls -1 "$ROOT/server/target/message-hosting-"*.jar 2>/dev/null | head -1)
if [[ -z "$JAR" ]]; then
  echo "未找到 jar，请先执行: bash deploy/build-backend.sh"
  exit 1
fi

if [[ -f "$PID_FILE" ]]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "==> 停止旧进程 $OLD_PID"
    kill "$OLD_PID" || true
    sleep 2
  fi
fi

echo "==> 启动后端 ..."
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=prod \
  -jar "$JAR" \
  --server.port="$SERVER_PORT" \
  > "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"
sleep 3

if curl -sf "http://127.0.0.1:${SERVER_PORT}/api/teaching-groups" >/dev/null; then
  echo "✓ 后端已启动，端口 $SERVER_PORT"
  echo "  日志: $LOG_FILE"
else
  echo "启动可能失败，请查看日志: tail -f $LOG_FILE"
  exit 1
fi
