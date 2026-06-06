#!/bin/bash
set -e
cd "$(dirname "$0")/.."

echo "安装 Python 依赖..."
pip3 install -q -r scripts/requirements.txt

echo "开始生成数据（约需数分钟，请耐心等待）..."
python3 scripts/generate_bulk_data.py "$@"
