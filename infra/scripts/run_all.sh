#!/usr/bin/env bash
set -Eeuo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

source "${BASE_DIR}/00_common.sh"

ensure_docker
cleanup_old
ensure_docker

echo "[run_all] 11_redis.sh"
bash "${BASE_DIR}/11_redis.sh"

echo "[run_all] 01_cloudwatch.sh"
bash "${BASE_DIR}/01_cloudwatch.sh" || true

echo "[run_all] 10_app.sh"
bash "${BASE_DIR}/10_app.sh"

echo "[run_all] 02_prometheus.sh"
bash "${BASE_DIR}/02_prometheus.sh" || true

echo "[run_all] 03_grafana.sh"
bash "${BASE_DIR}/03_grafana.sh" || true

docker ps
docker system df

USAGE=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
if [[ "${USAGE}" -ge 80 ]]; then
  echo "[run_all] disk ${USAGE}% - please cleanup or extend volume"
fi
