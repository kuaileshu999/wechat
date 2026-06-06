#!/bin/bash
# 推送代码到 GitHub
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/deploy/.env"
if [[ -f "$ENV_FILE" ]]; then
  # shellcheck source=/dev/null
  source "$ENV_FILE"
fi

GITHUB_REPO="${GITHUB_REPO:-https://github.com/kuaileshu/chat2.git}"
BRANCH="${GITHUB_BRANCH:-main}"

if [[ ! -d .git ]]; then
  git init -b "$BRANCH"
fi

if ! git remote get-url origin >/dev/null 2>&1; then
  git remote add origin "$GITHUB_REPO"
fi

git add -A
if git diff --cached --quiet; then
  echo "没有新的变更需要提交"
else
  git commit -m "deploy: message takeover system"
fi

echo "==> 推送到 $GITHUB_REPO ($BRANCH)"
git push -u origin "$BRANCH"

echo "✓ 代码已推送到 GitHub"
