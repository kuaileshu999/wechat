#!/bin/bash
# 在阿里云服务器上执行的一键部署脚本
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/deploy/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "请先复制 deploy/.env.example 为 deploy/.env 并填写数据库密码"
  echo "  cp deploy/.env.example deploy/.env"
  exit 1
fi

# shellcheck disable=SC1090
source "$ENV_FILE"

echo "=========================================="
echo " 消息托管系统 - 服务器部署"
echo " 项目目录: $ROOT"
echo "=========================================="

echo ""
echo "==> 1/4 检查数据库（首次部署请手动执行 bash deploy/init-db.sh）"
TABLE_COUNT=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -N -e \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}'" 2>/dev/null || echo 0)
if [[ "$TABLE_COUNT" == "0" ]]; then
  echo "数据库 ${DB_NAME} 无表，正在初始化..."
  bash "$ROOT/deploy/init-db.sh"
else
  echo "数据库已有 ${TABLE_COUNT} 张表，跳过初始化"
fi

echo ""
echo "==> 2/5 数据库结构迁移"
bash "$ROOT/deploy/migrate-db.sh"

echo ""
echo "==> 3/5 构建后端"
bash "$ROOT/deploy/build-backend.sh"

echo ""
echo "==> 4/5 构建前端"
bash "$ROOT/deploy/build-frontend.sh"

echo ""
echo "==> 5/5 重启后端"
bash "$ROOT/deploy/restart-backend.sh"

DEPLOY_HOST="${DEPLOY_HOST:-120.26.194.111}"
FRONTEND_DIST="$ROOT/cursor/wechat-hosting/dist"

echo ""
echo "=========================================="
echo " 部署完成"
echo "=========================================="
echo ""
echo "【宝塔还需手动配置一次】"
echo "1. 网站根目录设为自习室:"
echo "   $ROOT/Study_room/frontend/dist"
echo "2. 网站 → 设置 → 配置文件，合并:"
echo "   $ROOT/deploy/baota-nginx.conf"
echo "3. 阿里云安全组放行 80 端口"
echo ""
echo "访问地址:"
echo "  自习室:     http://${DEPLOY_HOST}/"
echo "  消息托管:   http://${DEPLOY_HOST}/wechat/"
echo ""
echo "消息托管测试账号: liting / 123456"
echo "自习室账号: admin / Admin@123"
echo ""
