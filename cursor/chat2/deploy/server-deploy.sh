#!/bin/bash
# 在服务器上执行：拉取代码后构建并启动服务
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

export MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
export MYSQL_DATABASE="${MYSQL_DATABASE:-message_takeover}"

if ! command -v docker >/dev/null 2>&1; then
  echo "请先安装 Docker 和 Docker Compose"
  exit 1
fi

echo "==> 启动 Docker 服务"
docker compose -f docker-compose.prod.yml up -d --build

echo "==> 等待 MySQL 就绪"
for i in $(seq 1 30); do
  if docker exec mt-mysql mysqladmin ping -h 127.0.0.1 -uroot -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null; then
    break
  fi
  sleep 2
done

TUTOR_COUNT=$(docker exec mt-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N -e \
  "SELECT COUNT(*) FROM message_takeover.tutor" 2>/dev/null || echo 0)

if [[ "$TUTOR_COUNT" == "0" ]]; then
  echo "==> 数据库为空，导入演示数据"
  bash "$ROOT/deploy/import-data.sh"
else
  echo "数据库已有 ${TUTOR_COUNT} 条辅导记录，跳过数据导入"
fi

echo ""
echo "部署完成"
docker compose -f docker-compose.prod.yml ps
echo "访问: http://$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')/"
