#!/usr/bin/env sh
set -eu
GRADLE_VERSION=8.13
DIST_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/auralis-bootstrap"
DIST_DIR="$DIST_ROOT/gradle-$GRADLE_VERSION"
ZIP_FILE="$DIST_ROOT/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  mkdir -p "$DIST_ROOT"
  echo "Downloading Gradle $GRADLE_VERSION for Auralis Player..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
  else
    wget -O "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  fi
  unzip -q -o "$ZIP_FILE" -d "$DIST_ROOT"
fi
exec "$DIST_DIR/bin/gradle" "$@"
