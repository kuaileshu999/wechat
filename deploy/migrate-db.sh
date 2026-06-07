#!/bin/bash
# 执行数据库结构迁移（账号级分配等新字段）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-wechat}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"

MIGRATE_FILE="$ROOT/server/sql/migrate_account_allocation.sql"
if [[ ! -f "$MIGRATE_FILE" ]]; then
  echo "找不到 $MIGRATE_FILE"
  exit 1
fi

echo "==> 执行数据库迁移 ${DB_NAME} ..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$MIGRATE_FILE"
echo "✓ 数据库迁移完成"
