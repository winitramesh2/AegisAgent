#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

"$ROOT_DIR/scripts/start-all.sh"
echo
"$ROOT_DIR/scripts/check-all.sh"
