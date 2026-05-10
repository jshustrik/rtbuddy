#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
SERVICES="${SERVICES:-auth-service usrsys-service make-service review-service export-service routesview-service}"
GRADLE_ARGS=(test --no-daemon --max-workers=1)
TEST_IMAGE="${TEST_IMAGE:-eclipse-temurin@sha256:aae0b1494a5637b2c1b933080088ccc196dec7ffb83ce1cd524211ea4f640ff4}"
DOCKER_GRADLE_OPTS="${DOCKER_GRADLE_OPTS:--Dorg.gradle.jvmargs=-Xmx384m -Dkotlin.daemon.jvm.options=-Xmx256m -Dorg.gradle.workers.max=1}"

run_gradle() {
  local service="$1"
  echo "==> $service"
  if command -v java >/dev/null 2>&1 && java -version >/dev/null 2>&1; then
    (cd "$ROOT_DIR/$service" && ./gradlew "${GRADLE_ARGS[@]}")
  else
    docker run --rm \
      -v "$ROOT_DIR/$service:/workspace" \
      -v routebuddy-gradle-cache:/root/.gradle \
      -e "GRADLE_OPTS=$DOCKER_GRADLE_OPTS" \
      -w /workspace \
      "$TEST_IMAGE" \
      sh -lc 'chmod +x ./gradlew && ./gradlew test --no-daemon --max-workers=1'
  fi
}

for service in $SERVICES; do
  run_gradle "$service"
done
