#!/bin/bash
# Script de build pour tous les modules RC
# Usage: ./build-all.sh

set -e

echo "========================================"
echo "  Build RC Modules"
echo "========================================"

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

# Ordre de build: drivers d'abord, puis business, puis aggregator
MODULES=(
    "mpudriver"
    "mpubusiness"
    "urmdriver"
    "urmbusiness"
    "sensorsbusiness"
)

for MODULE in "${MODULES[@]}"; do
    echo ""
    echo "[BUILD] $MODULE"
    echo "----------------------------------------"

    if [ -f "$BASE_DIR/$MODULE/pom.xml" ]; then
        cd "$BASE_DIR/$MODULE"
        mvn clean install -q
        echo "[OK] $MODULE"
    else
        echo "[SKIP] $MODULE - pas de pom.xml"
    fi
done

echo ""
echo "========================================"
echo "  BUILD REUSSI"
echo "========================================"

cd "$BASE_DIR"
