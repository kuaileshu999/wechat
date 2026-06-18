#!/bin/bash
# 在服务器 /root/wechat/Study_room 目录下执行（解压 update 包后）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/deploy/.env"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

DB_PASSWORD="${DB_PASSWORD:-}"
MYSQL_CMD="mysql -uroot"
if [[ -n "$DB_PASSWORD" ]]; then
  MYSQL_CMD="mysql -uroot -p${DB_PASSWORD}"
fi

echo "==> 1/4 应用数据库补丁"
PATCH_DIRS=(
  "backend/src/main/resources/db"
  "deploy/db"
)
for dir in "${PATCH_DIRS[@]}"; do
  [[ -d "$dir" ]] || continue
  for f in "$dir"/patch_*.sql; do
    [[ -f "$f" ]] || continue
    case "$(basename "$f")" in
      patch_employee_drop_campus.sql) continue ;;
    esac
    echo "  - $f"
    if [[ -n "$DB_PASSWORD" ]]; then
      mysql -uroot -p"${DB_PASSWORD}" study_room < "$f" || true
    else
      mysql -uroot study_room < "$f" || true
    fi
  done
done

echo "==> 2/4 更新后端 jar"
mkdir -p backend/target
if [[ -f deploy/study-room-backend-1.0.0.jar ]]; then
  cp deploy/study-room-backend-1.0.0.jar backend/target/
fi

echo "==> 3/4 更新前端 dist"
if [[ -d deploy/dist ]]; then
  mkdir -p frontend
  rm -rf frontend/dist
  cp -r deploy/dist frontend/dist
  echo "  ✓ frontend/dist 已更新"
else
  echo "  ! 未找到 deploy/dist，跳过前端更新"
fi

echo "==> 4/4 重启后端"
bash deploy/restart-backend.sh

echo ""
echo "✓ 更新完成"
echo "  访问: http://${DEPLOY_HOST:-120.26.194.111}/"
