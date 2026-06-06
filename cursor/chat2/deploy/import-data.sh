#!/bin/bash
# 导入演示数据：优先使用 SQL 备份，否则用 Python 脚本生成
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
SQL_FILE="${1:-$ROOT/data/message_takeover_full.sql}"

if [[ -f "$SQL_FILE" ]]; then
  echo "从备份导入: $SQL_FILE"
  docker exec -i mt-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < "$SQL_FILE"
  echo "✓ SQL 导入完成"
  exit 0
fi

echo "未找到 SQL 备份，使用 Python 脚本生成演示数据（约 1~2 分钟）"
if ! command -v python3 >/dev/null 2>&1; then
  echo "请安装 python3，或将 data/message_takeover_full.sql 放到项目目录后重试"
  exit 1
fi

pip3 install -q -r "$ROOT/scripts/requirements.txt" 2>/dev/null || true
python3 "$ROOT/scripts/generate_bulk_data.py" \
  --host 127.0.0.1 --port 3306 --user root --password "$MYSQL_ROOT_PASSWORD"
echo "✓ 演示数据生成完成"
