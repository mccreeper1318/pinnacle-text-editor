#!/usr/bin/env bash
set -euo pipefail

DEB_FILE="${1:?Usage: fix-deb-dependencies.sh path/to/package.deb}"
WORK_DIR="$(mktemp -d)"
OUTPUT_FILE="${DEB_FILE}.fixed"
trap 'rm -rf "$WORK_DIR" "$OUTPUT_FILE"' EXIT

dpkg-deb --raw-extract "$DEB_FILE" "$WORK_DIR"
CONTROL_FILE="$WORK_DIR/DEBIAN/control"

# Debian calls the libjpeg.so.62 provider libjpeg62-turbo, while Ubuntu and
# Linux Mint call it libjpeg62. Either package satisfies this runtime.
if grep -q 'libjpeg62-turbo' "$CONTROL_FILE"; then
    sed -i 's/libjpeg62-turbo/libjpeg62 | libjpeg62-turbo/g' "$CONTROL_FILE"
fi

dpkg-deb --root-owner-group --build "$WORK_DIR" "$OUTPUT_FILE"
mv "$OUTPUT_FILE" "$DEB_FILE"
