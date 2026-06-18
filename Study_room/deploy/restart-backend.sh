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

SERVER_PORT="${SERVER_PORT:-8080}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-study_room}"
DB_USER="${DB_USER:-root}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m}"
LOG_FILE="$ROOT/deploy/app.log"
PID_FILE="$ROOT/deploy/app.pid"

JAR="$ROOT/backend/target/study-room-backend-1.0.0.jar"
if [[ ! -f "$JAR" ]]; then
  echo "未找到 jar，请先执行: bash Study_room/deploy/build-backend.sh"
  exit 1
fi

SPRING_ARGS=(--server.port="$SERVER_PORT")
if [[ -n "${DB_PASSWORD:-}" ]]; then
  SPRING_ARGS+=(
    --spring.datasource.url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
    --spring.datasource.username="$DB_USER"
    --spring.datasource.password="$DB_PASSWORD"
  )
fi

if [[ -f "$PID_FILE" ]]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "==> 停止旧进程 $OLD_PID"
    kill "$OLD_PID" || true
    sleep 2
  fi
fi

echo "==> 启动自习室后端 ..."
nohup java $JAVA_OPTS -jar "$JAR" "${SPRING_ARGS[@]}" > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
sleep 12

if curl -sf "http://127.0.0.1:${SERVER_PORT}/api/auth/login" \
  -X POST -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}' >/dev/null; then
  echo "✓ 自习室后端已启动，端口 $SERVER_PORT"
  echo "  日志: $LOG_FILE"
else
  echo "启动可能失败，请查看日志: tail -f $LOG_FILE"
  exit 1
fi
