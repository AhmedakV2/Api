#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$APP_HOME/app.pid"
TIMEOUT="${AFT_STOP_TIMEOUT:-30}"

if [ ! -f "$PID_FILE" ]; then
  echo "PID dosyasi yok, uygulama calismiyor."
  exit 0
fi

PID="$(cat "$PID_FILE")"

if ! kill -0 "$PID" 2>/dev/null; then
  rm -f "$PID_FILE"
  echo "Surec zaten kapali, PID dosyasi temizlendi."
  exit 0
fi

kill "$PID"

for _ in $(seq 1 "$TIMEOUT"); do
  if ! kill -0 "$PID" 2>/dev/null; then
    rm -f "$PID_FILE"
    echo "Durduruldu."
    exit 0
  fi
  sleep 1
done

kill -9 "$PID" 2>/dev/null || true
rm -f "$PID_FILE"
echo "Zorla durduruldu."
