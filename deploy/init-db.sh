#!/bin/bash
# 初始化 MySQL 数据库（首次部署执行一次）
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

SQL_FILE="$ROOT/server/sql/schema.sql"
if [[ ! -f "$SQL_FILE" ]]; then
  echo "找不到 $SQL_FILE"
  exit 1
fi

echo "==> 导入数据库脚本到 ${DB_NAME} ..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" < "$SQL_FILE"
echo "✓ 数据库初始化完成"
