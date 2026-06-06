#!/bin/bash
export JAVA_HOME=$(/usr/libexec/java_home)
cd "$(dirname "$0")/backend"
mvn -q -DskipTests package && java -jar target/message-takeover-1.0.0.jar
