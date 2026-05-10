#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

secret_hex() {
  local bytes="$1"
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex "$bytes"
  else
    od -An -N "$bytes" -tx1 /dev/urandom | tr -d ' \n'
  fi
}

ensure_env() {
  local key="$1"
  local value="$2"
  if ! grep -q "^${key}=" .env 2>/dev/null; then
    printf '%s=%s\n' "$key" "$value" >> .env
  fi
}

if [ ! -f .env ]; then
  : > .env
fi

ensure_env "POSTGRES_PASSWORD" "$(secret_hex 24)"
ensure_env "JWT_SECRET" "$(secret_hex 64)"
ensure_env "JWT_EXPIRATION_MS" "3600000"
ensure_env "INTERNAL_SERVICE_TOKEN" "$(secret_hex 32)"
ensure_env "YANDEX_MAPS_API_KEY" "${YANDEX_MAPS_API_KEY:-}"

if [ -n "${YANDEX_MAPS_API_KEY:-}" ]; then
  tmp_env="$(mktemp)"
  awk -v value="$YANDEX_MAPS_API_KEY" '
    BEGIN { updated = 0 }
    /^YANDEX_MAPS_API_KEY=/ {
      print "YANDEX_MAPS_API_KEY=" value
      updated = 1
      next
    }
    { print }
    END {
      if (!updated) print "YANDEX_MAPS_API_KEY=" value
    }
  ' .env > "$tmp_env"
  mv "$tmp_env" .env
fi

if ! grep -q '^YANDEX_MAPS_API_KEY=.' .env; then
  echo "WARN: YANDEX_MAPS_API_KEY is empty in .env; maps may not load."
fi

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
