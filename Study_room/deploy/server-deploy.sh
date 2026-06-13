#!/bin/bash
# 自习室系统 - 服务器一键部署
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/deploy/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "请先复制 Study_room/deploy/.env.example 为 Study_room/deploy/.env 并填写数据库密码"
  echo "  cp Study_room/deploy/.env.example Study_room/deploy/.env"
  exit 1
fi

# shellcheck disable=SC1090
source "$ENV_FILE"

echo "=========================================="
echo " 自习室系统 - 服务器部署"
echo " 项目目录: $ROOT"
echo "=========================================="

echo ""
echo "==> 1/3 构建后端"
bash "$ROOT/deploy/build-backend.sh"

echo ""
echo "==> 2/3 构建前端"
bash "$ROOT/deploy/build-frontend.sh"

echo ""
echo "==> 3/3 重启后端"
bash "$ROOT/deploy/restart-backend.sh"

DEPLOY_HOST="${DEPLOY_HOST:-120.26.194.111}"

echo ""
echo "=========================================="
echo " 自习室部署完成"
echo "=========================================="
echo ""
echo "【宝塔还需手动配置】"
echo "1. 网站根目录设为: $ROOT/frontend/dist"
echo "2. 网站 → 设置 → 配置文件，合并: $(cd "$ROOT/.." && pwd)/deploy/baota-nginx.conf"
echo "3. 消息托管需单独部署并改用 8090 端口: bash deploy/server-deploy.sh"
echo ""
echo "访问地址:"
echo "  自习室:     http://${DEPLOY_HOST}/"
echo "  消息托管:   http://${DEPLOY_HOST}/wechat/"
echo ""
echo "自习室账号: admin / Admin@123"
echo ""
