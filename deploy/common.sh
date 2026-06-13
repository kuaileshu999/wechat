#!/bin/bash
# 部署脚本公共函数

# 解析并导出 JAVA_HOME（Maven / java 命令需要）
setup_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
    return 0
  fi

  if command -v java >/dev/null 2>&1; then
    local java_bin
    java_bin=$(readlink -f "$(command -v java)" 2>/dev/null || command -v java)
    JAVA_HOME=$(cd "$(dirname "$java_bin")/.." && pwd)
    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
    return 0
  fi

  local candidate
  for candidate in \
    /usr/lib/jvm/java-17-openjdk* \
    /usr/lib/jvm/java-21-openjdk* \
    /usr/lib/jvm/java-17* \
    /usr/java/jdk-17* \
    /usr/java/jdk-21* \
    /www/server/java/jdk_*; do
    if [[ -x "${candidate}/bin/java" ]]; then
      JAVA_HOME="${candidate}"
      export JAVA_HOME
      export PATH="$JAVA_HOME/bin:$PATH"
      return 0
    fi
  done

  echo "错误: 未找到 Java JDK 17+"
  echo "  1. 宝塔 → 软件商店 → 安装「Java 项目管理器」或 OpenJDK 17"
  echo "  2. 或在 deploy/.env 中设置: JAVA_HOME=/usr/lib/jvm/java-17-openjdk"
  exit 1
}

require_maven() {
  if ! command -v mvn >/dev/null 2>&1; then
    echo "错误: 未找到 mvn 命令"
    echo "  宝塔安装 Maven，或执行: yum install -y maven"
    exit 1
  fi
}
