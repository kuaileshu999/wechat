#!/bin/bash
# 服务器一键更新（解压 study-room-update.tar.gz 后在 Study_room 目录执行）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f deploy/study-room-backend-1.0.0.jar ]]; then
  echo "错误: 请先在 /root/wechat/Study_room 解压 study-room-update.tar.gz"
  exit 1
fi

# 同步 deploy 脚本到当前目录
cp -f deploy/restart-backend.sh deploy/server-update.sh deploy/common.sh deploy/ 2>/dev/null || true

# 确保 .env 存在
if [[ ! -f deploy/.env ]]; then
  if [[ -f deploy/.env.example ]]; then
    cp deploy/.env.example deploy/.env
    echo "已创建 deploy/.env，请确认 DB_PASSWORD 正确后重新运行本脚本"
    exit 1
  fi
  echo "错误: 缺少 deploy/.env，请先创建并设置 DB_PASSWORD"
  exit 1
fi

# shellcheck disable=SC1090
source deploy/.env

echo "=========================================="
echo " 自习室一键更新"
echo " 目录: $ROOT"
echo "=========================================="

bash deploy/server-update.sh

echo ""
echo "验证:"
sleep 2
curl -s -o /dev/null -w "  登录接口: HTTP %{http_code}\n" \
  "http://127.0.0.1:${SERVER_PORT:-8080}/api/auth/login" \
  -X POST -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'
echo "  前端文件: $(basename "$(ls frontend/dist/assets/index-*.js 2>/dev/null | head -1)" 2>/dev/null || echo '未找到')"
echo ""
echo "浏览器 Ctrl+F5 刷新: http://${DEPLOY_HOST:-120.26.194.111}/"
