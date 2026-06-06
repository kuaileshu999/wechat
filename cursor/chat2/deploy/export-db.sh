#!/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/deploy/release/sql"
mkdir -p "$OUT"

echo "导出数据库结构与数据..."
mysqldump -u root -p123456 \
  --single-transaction \
  --set-gtid-purged=OFF \
  --routines --triggers \
  --databases message_takeover \
  > "$OUT/message_takeover_full.sql"

echo "✓ 已导出: deploy/release/sql/message_takeover_full.sql"
ls -lh "$OUT/message_takeover_full.sql"
