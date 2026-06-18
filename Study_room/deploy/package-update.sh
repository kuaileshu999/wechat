#!/bin/bash
# 本地执行：打包可上传到服务器的更新包
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
OUT="$ROOT/study-room-update.tar.gz"

echo "==> 构建前端"
bash deploy/build-frontend.sh

echo "==> 构建后端"
bash deploy/build-backend.sh

echo "==> 打包"
TMP=$(mktemp -d)
mkdir -p "$TMP/deploy/dist" "$TMP/deploy/db" "$TMP/backend/src/main/resources/db"
cp backend/target/study-room-backend-1.0.0.jar "$TMP/deploy/"
cp -r frontend/dist/* "$TMP/deploy/dist/"
cp deploy/restart-backend.sh deploy/server-update.sh deploy/common.sh "$TMP/deploy/"
cp backend/src/main/resources/db/patch_*.sql "$TMP/deploy/db/"
cp backend/src/main/resources/db/patch_*.sql "$TMP/backend/src/main/resources/db/"

tar -czf "$OUT" -C "$TMP" deploy backend
rm -rf "$TMP"

echo "✓ 已生成: $OUT"
echo ""
echo "上传到服务器并更新："
echo "  scp $OUT root@120.26.194.111:/root/wechat/"
echo "  ssh root@120.26.194.111"
echo "  cd /root/wechat/Study_room && tar -xzf ../study-room-update.tar.gz && bash deploy/server-update.sh"
