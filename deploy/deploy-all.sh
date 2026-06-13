#!/bin/bash
# 在服务器 /root/wechat 下执行：一键部署自习室 + 消息托管
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f "$ROOT/Study_room/deploy/.env" ]]; then
  echo "缺少 Study_room/deploy/.env"
  echo "请先执行:"
  echo "  cp Study_room/deploy/.env.example Study_room/deploy/.env"
  echo "  vi Study_room/deploy/.env    # 填写 DB_PASSWORD"
  exit 1
fi

if [[ ! -f "$ROOT/deploy/.env" ]]; then
  echo "缺少 deploy/.env"
  echo "请先执行:"
  echo "  cp deploy/.env.example deploy/.env"
  echo "  vi deploy/.env             # 填写 DB_PASSWORD，确认 SERVER_PORT=8090"
  exit 1
fi

chmod +x "$ROOT/Study_room/deploy/"*.sh "$ROOT/deploy/"*.sh 2>/dev/null || true

echo "=========================================="
echo " 1/2 部署自习室"
echo "=========================================="
bash "$ROOT/Study_room/deploy/server-deploy.sh"

echo ""
echo "=========================================="
echo " 2/2 部署消息托管 (8090)"
echo "=========================================="
bash "$ROOT/deploy/server-deploy.sh"

echo ""
echo "=========================================="
echo " 3/3 宝塔 Nginx（需手动）"
echo "=========================================="
echo "1. 网站根目录改为:"
echo "   $ROOT/Study_room/frontend/dist"
echo ""
echo "2. 网站 → 设置 → 配置文件，合并:"
echo "   $ROOT/deploy/baota-nginx.conf"
echo ""
echo "3. 保存并重载 Nginx"
echo ""
echo "访问:"
echo "  自习室:   http://120.26.194.111/"
echo "  消息托管: http://120.26.194.111/wechat/"
echo ""
