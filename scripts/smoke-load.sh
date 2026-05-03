#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
REQUESTS="${REQUESTS:-200}"
CONCURRENCY="${CONCURRENCY:-20}"
ROUTE_ID="${ROUTE_ID:-}"

if [[ -z "$ROUTE_ID" ]]; then
  ROUTE_ID="$(
    curl -fsSL "${BASE_URL}/api/proxy/routes" \
      | sed -nE 's/^\[\{"id":([0-9]+).*/\1/p' \
      | head -1
  )"
fi

run_one() {
  local n="$1"
  local path
  case $((n % 5)) in
    0) path="/routes" ;;
    1) path="/api/proxy/routes" ;;
    2)
      if [[ -n "${ROUTE_ID:-}" ]]; then
        path="/api/proxy/routes/${ROUTE_ID}"
      else
        path="/api/proxy/routes"
      fi
      ;;
    3) path="/api/proxy/routes?search=%D0%BC%D1%83%D0%B7%D0%B5%D0%B8" ;;
    *) path="/export" ;;
  esac

  curl -sS -o /dev/null \
    -w "%{http_code} %{time_total} ${path}\n" \
    "${BASE_URL}${path}"
}

export BASE_URL
export ROUTE_ID
export -f run_one

tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

seq 1 "$REQUESTS" | xargs -n 1 -P "$CONCURRENCY" bash -c 'run_one "$0"' > "$tmp"

total="$(wc -l < "$tmp" | tr -d ' ')"
bad="$(awk '$1 < 200 || $1 >= 400 { count++ } END { print count + 0 }' "$tmp")"
avg="$(awk '{ sum += $2 } END { if (NR) printf "%.3f", sum / NR; else print "0.000" }' "$tmp")"
max="$(awk 'BEGIN { max = 0 } { if ($2 > max) max = $2 } END { printf "%.3f", max }' "$tmp")"

echo "requests=${total} concurrency=${CONCURRENCY} bad=${bad} avg_seconds=${avg} max_seconds=${max}"
awk '$1 < 200 || $1 >= 400 { print }' "$tmp" | head -20

test "$bad" -eq 0
