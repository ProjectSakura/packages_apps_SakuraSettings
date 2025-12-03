#!/bin/bash

TARGET_DIR="$1"

if [ -z "$TARGET_DIR" ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

# Replace EXACT "com.sakura.support" with "com.sakura.settings"
# -r → recursive
# -I → ignore binary files so they do not get corrupted
# --literal → prevents regex interpretation
# --follow-symlinks if needed

grep -rI --files-with-matches "com.sakura.support" "$TARGET_DIR" | while read -r file; do
    echo "Patching: $file"
    sed -i 's/com\.sakura\.support/com.sakura.settings/g' "$file"
done
