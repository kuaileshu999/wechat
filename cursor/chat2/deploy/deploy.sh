#!/bin/bash
# 一键部署：推送到 GitHub，并在服务器拉取部署
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT/deploy/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "请先复制 deploy/.env.example 为 deploy/.env"
  exit 1
fi
# shellcheck source=/dev/null
source "$ENV_FILE"

: "${DEPLOY_HOST:?请设置 DEPLOY_HOST}"
: "${DEPLOY_USER:=root}"
: "${DEPLOY_PATH:=/opt/message-takeover}"
: "${DEPLOY_PORT:=22}"
: "${GITHUB_REPO:?请设置 GITHUB_REPO}"

echo "==> 1/3 推送代码到 GitHub"
bash "$ROOT/deploy/push-github.sh"

REMOTE="${DEPLOY_USER}@${DEPLOY_HOST}"
echo ""
echo "==> 2/3 服务器拉取代码并部署 (${REMOTE})"
echo "    将提示输入 SSH 密码（无需密钥）"

ssh -p "$DEPLOY_PORT" -o PreferredAuthentications=password -o PubkeyAuthentication=no "$REMOTE" bash -s <<EOF
set -e
if [[ ! -d '$DEPLOY_PATH/.git' ]]; then
  git clone '$GITHUB_REPO' '$DEPLOY_PATH'
else
  cd '$DEPLOY_PATH'
  git fetch origin
  git reset --hard origin/${GITHUB_BRANCH:-main}
fi
cd '$DEPLOY_PATH'
export MYSQL_ROOT_PASSWORD='${MYSQL_ROOT_PASSWORD:-123456}'
export MYSQL_DATABASE='${MYSQL_DATABASE:-message_takeover}'
bash deploy/server-deploy.sh
EOF

echo ""
echo "==> 3/3 可选：上传数据库备份（约 96MB，需输入 SSH 密码）"
if [[ -t 0 ]]; then
  read -r -p "是否上传本地数据库备份到服务器？[y/N] " UPLOAD_DB
else
  UPLOAD_DB="${UPLOAD_DB:-N}"
fi
if [[ "$UPLOAD_DB" =~ ^[Yy]$ ]]; then
  bash "$ROOT/deploy/upload-db.sh"
fi

echo ""
echo "部署完成"
echo "  前端: http://${DEPLOY_HOST}/"
echo "  API:  http://${DEPLOY_HOST}/api/tutors"
