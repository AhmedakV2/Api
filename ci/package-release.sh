#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:-}"
BUILD="${2:-0}"

if [ -z "$VERSION" ]; then
  echo "Kullanim: package-release.sh <versiyon> <build-numarasi>"
  exit 1
fi

NAME="aft-api-${VERSION}-b${BUILD}"
STAGE="$ROOT/target/release"
DEST="$STAGE/$NAME"

JAR="$(find "$ROOT/target" -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n 1)"
if [ -z "$JAR" ]; then
  echo "Calistirilabilir jar bulunamadi. Once 'mvn package' calistirin."
  exit 1
fi

rm -rf "$STAGE"
mkdir -p "$DEST/bin" "$DEST/config" "$DEST/db/migration" "$DEST/logs"

cp "$JAR" "$DEST/app.jar"
cp "$ROOT/ci/release/bin/." "$DEST/bin/" -r
cp "$ROOT/ci/release/aft.env.example" "$DEST/config/aft.env.example"
cp "$ROOT/src/main/resources/application-prod.yml" "$DEST/config/application-prod.yml"
cp "$ROOT"/src/main/resources/db/migration/*.sql "$DEST/db/migration/"
chmod +x "$DEST"/bin/*.sh
: > "$DEST/logs/.gitkeep"

GIT_COMMIT_SHORT="$(git -C "$ROOT" rev-parse --short HEAD 2>/dev/null || echo bilinmiyor)"
GIT_BRANCH_NAME="$(git -C "$ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo bilinmiyor)"

cat > "$DEST/BUILD-INFO.txt" <<INFO
paket        : $NAME
versiyon     : $VERSION
build        : $BUILD
commit       : $GIT_COMMIT_SHORT
dal          : $GIT_BRANCH_NAME
derleme      : $(date -u '+%Y-%m-%d %H:%M:%S UTC')
jdk          : $(java -version 2>&1 | grep -i -m 1 version)
jar          : $(basename "$JAR")
INFO

( cd "$DEST" && find . -type f ! -name SHA256SUMS -print0 | sort -z | xargs -0 sha256sum > SHA256SUMS )

echo "$NAME" > "$STAGE/RELEASE_NAME"
echo "Yayin paketi hazir: $DEST"
