#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
POM_PATH="$ROOT_DIR/stockbacktester/pom.xml"
LOCAL_M2="$ROOT_DIR/.m2"

if [[ ! -f "$POM_PATH" ]]; then
  echo "Could not find pom.xml at: $POM_PATH" >&2
  exit 1
fi

echo "Compiling project..."
mvn -q -f "$POM_PATH" -Dmaven.repo.local="$LOCAL_M2" -DskipTests compile

echo "Starting Manual Trading Terminal..."
mvn -q -f "$POM_PATH" -Dmaven.repo.local="$LOCAL_M2" -Dexec.mainClass=com.backtester.Main exec:java
