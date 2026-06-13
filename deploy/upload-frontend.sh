#!/bin/bash
# 在本地 Mac 执行：构建前端并上传到服务器（CentOS 7 无法在服务器上 npm build）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"
FRONTEND="$ROOT/cursor/wechat-hosting"

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

DEPLOY_HOST="${DEPLOY_HOST:-120.26.194.111}"
DEPLOY_USER="${DEPLOY_USER:-root}"
REMOTE_DIR="${PROJECT_ROOT:-/root/wechat}/cursor/wechat-hosting"
NGINX_ROOT="${NGINX_ROOT:-/www/wwwroot/wechat}"

echo "==> 本地构建前端 ..."
cd "$FRONTEND"
npm install
npm run build

if [[ ! -d "$FRONTEND/dist" ]]; then
  echo "构建失败：dist 目录不存在"
  exit 1
fi

if ! grep -rq "分配账号" "$FRONTEND/dist"; then
  echo "警告: dist 中未找到「分配账号」，请确认代码已更新"
fi

echo "==> 上传到 ${DEPLOY_USER}@${DEPLOY_HOST}:${REMOTE_DIR}/dist ..."
ssh "${DEPLOY_USER}@${DEPLOY_HOST}" "mkdir -p ${REMOTE_DIR}/dist"
rsync -avz --delete "$FRONTEND/dist/" "${DEPLOY_USER}@${DEPLOY_HOST}:${REMOTE_DIR}/dist/"

echo ""
echo "✓ 上传完成"
echo "  消息托管访问: http://${DEPLOY_HOST}/wechat/"
echo "  请确认 Nginx 已配置 deploy/baota-nginx.conf"
echo "  浏览器 Ctrl+F5 强刷"
echo ""
