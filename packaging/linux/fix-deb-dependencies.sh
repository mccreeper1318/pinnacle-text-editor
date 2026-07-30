#!/usr/bin/env bash
set -euo pipefail

DEB_FILE="${1:?Usage: fix-deb-dependencies.sh path/to/package.deb app-version}"
APP_VERSION="${2:?Usage: fix-deb-dependencies.sh path/to/package.deb app-version}"
WORK_DIR="$(mktemp -d)"
OUTPUT_FILE="${DEB_FILE}.fixed"
trap 'rm -rf "$WORK_DIR" "$OUTPUT_FILE"' EXIT

dpkg-deb --raw-extract "$DEB_FILE" "$WORK_DIR"
CONTROL_FILE="$WORK_DIR/DEBIAN/control"

# Debian calls the libjpeg.so.62 provider libjpeg62-turbo, while Ubuntu and
# Linux Mint call it libjpeg62. Either package satisfies this runtime.
if grep -q 'libjpeg62-turbo' "$CONTROL_FILE" && ! grep -q 'libjpeg62 | libjpeg62-turbo' "$CONTROL_FILE"; then
    sed -i 's/libjpeg62-turbo/libjpeg62 | libjpeg62-turbo/g' "$CONTROL_FILE"
fi

# Use a Debian-compatible prerelease version while retaining the full
# application version inside the packaged Java launcher.
BASE_VERSION="${APP_VERSION%%-*}"
if [[ "$APP_VERSION" == *-* ]]; then
    PRERELEASE="${APP_VERSION#*-}"
    DEBIAN_VERSION="${BASE_VERSION}~${PRERELEASE//-/.}"
else
    DEBIAN_VERSION="$BASE_VERSION"
fi
sed -i -E "s/^Version: .*/Version: ${DEBIAN_VERSION}/" "$CONTROL_FILE"

# jpackage creates the MIME association but may omit the file argument from
# the generated desktop entry. Add %f so opening a .txt file from the desktop
# or file manager passes that exact file to the editor.
while IFS= read -r -d '' DESKTOP_FILE; do
    if grep -q '^Exec=' "$DESKTOP_FILE" && ! grep -Eq '^Exec=.*%[fFuU]' "$DESKTOP_FILE"; then
        sed -i -E 's|^(Exec=.*)$|\1 %f|' "$DESKTOP_FILE"
    fi
done < <(find "$WORK_DIR" -type f -name '*.desktop' -print0)

dpkg-deb --root-owner-group --build "$WORK_DIR" "$OUTPUT_FILE"
mv "$OUTPUT_FILE" "$DEB_FILE"
