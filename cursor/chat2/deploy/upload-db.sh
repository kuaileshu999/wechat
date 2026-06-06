#!/bin/bash
# 将本地数据库备份上传到服务器并导入（使用密码登录，无需密钥）
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck source=/dev/null
  source "$ENV_FILE"
fi

: "${DEPLOY_HOST:?请设置 DEPLOY_HOST}"
: "${DEPLOY_USER:=root}"
: "${DEPLOY_PATH:=/opt/message-takeover}"
: "${DEPLOY_PORT:=22}"

SQL_SRC="$ROOT/deploy/release/sql/message_takeover_full.sql"
if [[ ! -f "$SQL_SRC" ]]; then
  bash "$ROOT/deploy/export-db.sh"
fi

REMOTE="${DEPLOY_USER}@${DEPLOY_HOST}"
echo "上传数据库备份到 ${REMOTE}:${DEPLOY_PATH}/data/ ..."
ssh -p "$DEPLOY_PORT" -o PreferredAuthentications=password -o PubkeyAuthentication=no "$REMOTE" \
  "mkdir -p '$DEPLOY_PATH/data'"
scp -P "$DEPLOY_PORT" -o PreferredAuthentications=password -o PubkeyAuthentication=no \
  "$SQL_SRC" "$REMOTE:$DEPLOY_PATH/data/message_takeover_full.sql"

echo "在服务器上导入数据..."
ssh -p "$DEPLOY_PORT" -o PreferredAuthentications=password -o PubkeyAuthentication=no "$REMOTE" bash -s <<EOF
set -e
cd '$DEPLOY_PATH'
export MYSQL_ROOT_PASSWORD='${MYSQL_ROOT_PASSWORD:-123456}'
docker compose -f docker-compose.prod.yml down
docker volume rm message-takeover_mysql_data 2>/dev/null || docker volume rm \$(docker volume ls -q | grep mysql_data | head -1) 2>/dev/null || true
docker compose -f docker-compose.prod.yml up -d mysql
for i in \$(seq 1 30); do
  docker exec mt-mysql mysqladmin ping -h 127.0.0.1 -uroot -p"\$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null && break
  sleep 2
done
bash deploy/import-data.sh data/message_takeover_full.sql
docker compose -f docker-compose.prod.yml up -d --build
EOF

echo "✓ 数据库已上传并导入"
