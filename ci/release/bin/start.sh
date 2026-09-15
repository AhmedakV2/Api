#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$APP_HOME/app.pid"
LOG_DIR="$APP_HOME/logs"
ENV_FILE="${AFT_ENV_FILE:-$APP_HOME/config/aft.env}"
PROFILE="${AFT_PROFILE:-prod}"
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "Uygulama zaten calisiyor (PID $(cat "$PID_FILE"))."
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Ortam dosyasi bulunamadi: $ENV_FILE"
  echo "config/aft.env.example dosyasini kopyalayip doldurun."
  exit 1
fi

set -a
. "$ENV_FILE"
set +a

if [ -z "${AFT_JWT_SECRET:-}" ]; then
  echo "AFT_JWT_SECRET tanimli degil."
  exit 1
fi

mkdir -p "$LOG_DIR"

nohup "$JAVA_BIN" \
  -XX:MaxRAMPercentage="${AFT_MAX_RAM_PERCENT:-75}" \
  -Dspring.profiles.active="$PROFILE" \
  -Dspring.config.additional-location="file:$APP_HOME/config/" \
  ${AFT_JAVA_OPTS:-} \
  -jar "$APP_HOME/app.jar" \
  >> "$LOG_DIR/aft-api.out" 2>&1 &

echo $! > "$PID_FILE"
echo "Baslatildi. PID $(cat "$PID_FILE"), log: $LOG_DIR/aft-api.out"
