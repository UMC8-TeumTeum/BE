#!/usr/bin/env bash
set -Eeuo pipefail

docker run -d --restart unless-stopped \
  --name redis-container \
  --network app-net \
  -v redis-data:/data \
  "redis:${REDIS_VERSION}" \
  redis-server --requirepass "${REDIS_PASSWORD}"

echo "[redis] started"
