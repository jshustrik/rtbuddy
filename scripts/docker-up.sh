#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "==> build images"
docker compose build

echo "==> start stack"
docker compose up -d

echo "==> refresh gateway dns"
docker compose up -d --force-recreate --no-deps gateway

echo "==> wait gateway"
for _ in $(seq 1 60); do
  if curl -fsSL http://localhost:8080/ >/dev/null; then
    echo "OK http://localhost:8080"
    docker compose ps
    exit 0
  fi
  sleep 2
done

echo "Gateway not ready. Last logs:"
docker compose logs --tail=120 gateway routesview-service make-service auth-service usrsys-service review-service
exit 1
