#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

check_url() {
  local name="$1"
  local url="$2"
  local body

  if body="$(curl -s --max-time 10 "$url")"; then
    echo "[OK] $name -> $url"
    echo "     $body"
  else
    echo "[FAIL] $name -> $url"
  fi
}

echo "=== Container Status (ops) ==="
docker compose -f "$ROOT_DIR/ops/docker-compose.yml" ps || true

echo "=== Container Status (ai/deeppavlov) ==="
docker compose -f "$ROOT_DIR/ai/deeppavlov/docker-compose.yml" ps || true

echo "=== Endpoint Health ==="
check_url "Backend health" "http://localhost:8080/actuator/health"
check_url "JIRA validation" "http://localhost:8080/api/admin/jira/validate"
check_url "DeepPavlov health" "http://localhost:8000/health"
check_url "OpenSearch" "http://localhost:9200"
check_url "Dashboards" "http://localhost:5601/api/status"
check_url "Mailpit" "http://localhost:8025/api/v1/messages"
