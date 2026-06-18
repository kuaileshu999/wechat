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
if [[ "${SKIP_FRONTEND_BUILD:-}" == "1" ]]; then
  echo "跳过（SKIP_FRONTEND_BUILD=1）"
elif [[ -d "$ROOT/deploy/dist" ]] && [[ -f "$ROOT/deploy/dist/index.html" ]]; then
  echo "使用已上传的 deploy/dist（服务器 Node 版本过旧时请在本机构建后上传）"
  rm -rf "$ROOT/frontend/dist"
  cp -r "$ROOT/deploy/dist" "$ROOT/frontend/dist"
else
  if ! node -v >/dev/null 2>&1; then
    echo "错误: 服务器无法运行 node，请在本机构建前端并上传："
    echo "  bash deploy/package-update.sh"
    echo "  scp study-room-update.tar.gz root@服务器:/root/wechat/"
    echo "  cd /root/wechat/Study_room && tar -xzf ../study-room-update.tar.gz && bash deploy/server-update.sh"
    exit 1
  fi
  bash "$ROOT/deploy/build-frontend.sh"
fi

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
