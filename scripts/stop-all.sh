#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/runtime"

echo "[1/3] Stopping backend..."
if [[ -f "$RUNTIME_DIR/backend.pid" ]]; then
  kill "$(cat "$RUNTIME_DIR/backend.pid")" 2>/dev/null || true
  rm -f "$RUNTIME_DIR/backend.pid"
fi
pkill -f "AegisAgentApplication" 2>/dev/null || true

echo "[2/3] Stopping DeepPavlov API..."
docker compose -f "$ROOT_DIR/ai/deeppavlov/docker-compose.yml" down

echo "[3/3] Stopping OpenSearch stack..."
docker compose -f "$ROOT_DIR/ops/docker-compose.yml" down

echo "[OK] Stopped all components."
