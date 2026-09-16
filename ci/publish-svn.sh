#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STAGE="$ROOT/target/release"
WC="$ROOT/target/svn-wc"

: "${SVN_URL:?SVN_URL tanimli degil}"
: "${SVN_USERNAME:?SVN_USERNAME tanimli degil}"
: "${SVN_PASSWORD:?SVN_PASSWORD tanimli degil}"
SVN_TAG="${SVN_TAG:-0}"

NAME="$(cat "$STAGE/RELEASE_NAME")"
SOURCE="$STAGE/$NAME"
TRUNK_URL="$SVN_URL/current"
TAG_URL="$SVN_URL/tags/$NAME"

SVN_COMMON=(--username "$SVN_USERNAME" --no-auth-cache --non-interactive
  --trust-server-cert-failures=unknown-ca,cn-mismatch,expired,not-yet-valid,other)

if svn help 2>/dev/null | grep -q -- '--password-from-stdin' \
  || svn help checkout 2>/dev/null | grep -q -- '--password-from-stdin'; then
  PASSWORD_FROM_STDIN=1
else
  PASSWORD_FROM_STDIN=0
fi

svn_run() {
  if [ "$PASSWORD_FROM_STDIN" = "1" ]; then
    svn "$@" "${SVN_COMMON[@]}" --password-from-stdin <<< "$SVN_PASSWORD"
  else
    svn "$@" "${SVN_COMMON[@]}" --password "$SVN_PASSWORD"
  fi
}

if ! svn_run ls "$TRUNK_URL" > /dev/null 2>&1; then
  svn_run mkdir --parents "$TRUNK_URL" -m "aft-api yayin dizini olusturuldu"
fi

if [ "$SVN_TAG" = "1" ] && svn_run ls "$TAG_URL" > /dev/null 2>&1; then
  echo "Etiket zaten var, yayin atlandi: $TAG_URL"
  exit 0
fi

rm -rf "$WC"
svn_run checkout "$TRUNK_URL" "$WC"

find "$WC" -mindepth 1 -maxdepth 1 ! -name '.svn' -exec rm -rf {} +
cp -a "$SOURCE/." "$WC/"

cd "$WC"
svn_run add --force . --auto-props --parents --depth infinity -q
MISSING="$(svn_run status | awk '/^!/ { $1=""; sub(/^ +/, ""); print }')"
if [ -n "$MISSING" ]; then
  printf '%s\n' "$MISSING" | while IFS= read -r entry; do
    [ -n "$entry" ] && svn_run delete --force "$entry" -q
  done
fi

PENDING="$(svn_run status -q)"
if [ -n "$PENDING" ]; then
  svn_run commit -m "aft-api $NAME calisir paket"
else
  echo "Degisiklik yok, commit atlandi."
fi

if [ "$SVN_TAG" = "1" ]; then
  svn_run copy --parents "$TRUNK_URL" "$TAG_URL" -m "aft-api $NAME etiketlendi"
  echo "SVN yayini tamam: $TAG_URL"
else
  echo "SVN yayini tamam: $TRUNK_URL (etiket olusturulmadi)"
fi
