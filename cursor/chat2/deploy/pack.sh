#!/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RELEASE="$ROOT/deploy/release"

echo "==> 1/4 编译后端"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home 2>/dev/null || echo '')}"
cd "$ROOT/backend"
mvn -q -DskipTests package

echo "==> 2/4 构建前端"
cd "$ROOT/frontend"
npm ci
npm run build

echo "==> 3/4 导出数据库"
bash "$ROOT/deploy/export-db.sh"

echo "==> 4/4 打包发布目录"
rm -rf "$RELEASE/app"
mkdir -p "$RELEASE/app"

rsync -a \
  --exclude node_modules \
  --exclude target \
  --exclude deploy/release \
  --exclude .git \
  "$ROOT/backend" "$ROOT/frontend" "$ROOT/sql" "$ROOT/docker-compose.prod.yml" \
  "$ROOT/deploy/nginx.conf" "$RELEASE/app/"

mkdir -p "$RELEASE/app/sql/init"
cp "$RELEASE/sql/message_takeover_full.sql" "$RELEASE/app/sql/init/01_data.sql"

echo "✓ 发布包已生成: deploy/release/"
echo "  - app/                 应用代码"
echo "  - sql/message_takeover_full.sql  数据库全量备份"
