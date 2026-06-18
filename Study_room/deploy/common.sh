#!/bin/bash
# 自习室 deploy 公共函数

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
    /usr/lib/jvm/java-17-temurin-jdk \
    /usr/lib/jvm/java-17-openjdk* \
    /usr/lib/jvm/java-21-openjdk* \
    /usr/lib/jvm/java-17* \
    /usr/java/jdk-17* \
    /www/server/java/jdk_*; do
    if [[ -x "${candidate}/bin/java" ]]; then
      JAVA_HOME="${candidate}"
      export JAVA_HOME
      export PATH="$JAVA_HOME/bin:$PATH"
      return 0
    fi
  done

  echo "错误: 未找到 Java JDK 17+"
  echo "  在 deploy/.env 中设置: JAVA_HOME=/usr/lib/jvm/java-17-temurin-jdk"
  exit 1
}

require_maven() {
  if ! command -v mvn >/dev/null 2>&1; then
    echo "错误: 未找到 mvn 命令"
    exit 1
  fi
}
