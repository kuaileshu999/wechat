#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND="$ROOT/cursor/wechat-hosting"

echo "==> 构建前端 ..."
cd "$FRONTEND"
npm install
npm run build
echo "✓ 前端产物: $FRONTEND/dist"
