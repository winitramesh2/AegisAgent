#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/runtime"

mkdir -p "$RUNTIME_DIR"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
else
  echo "[WARN] .env not found. Using defaults from application.yml"
fi

echo "[1/3] Starting OpenSearch stack..."
docker compose -f "$ROOT_DIR/ops/docker-compose.yml" up -d --wait --wait-timeout 120

echo "[2/3] Starting DeepPavlov API..."
docker compose -f "$ROOT_DIR/ai/deeppavlov/docker-compose.yml" up -d deeppavlov-api

echo "[3/3] Starting backend..."
if [[ -f "$RUNTIME_DIR/backend.pid" ]]; then
  kill "$(cat "$RUNTIME_DIR/backend.pid")" 2>/dev/null || true
fi
pkill -f "AegisAgentApplication" 2>/dev/null || true

cd "$ROOT_DIR/backend"
nohup mvn spring-boot:run > "$RUNTIME_DIR/backend.log" 2>&1 &
echo $! > "$RUNTIME_DIR/backend.pid"

echo "[OK] Started all components."
echo "- Backend log: $RUNTIME_DIR/backend.log"
echo "- Backend PID: $(cat "$RUNTIME_DIR/backend.pid")"
