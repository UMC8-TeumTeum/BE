#!/usr/bin/env bash
set -Eeuo pipefail

if [[ "${ENABLE_PROMETHEUS:-false}" != "true" ]]; then
  echo "[prometheus] disabled"
  exit 0
fi

docker pull prom/prometheus:latest || true

sudo mkdir -p /opt/prometheus

if ! command -v envsubst >/dev/null 2>&1; then
  sudo apt-get update -y && sudo apt-get install -y gettext-base
fi

PROM_CONFIG_PATH="$(dirname "$0")/../config/prometheus-server.yaml"
echo "[prometheus] loading config from: $PROM_CONFIG_PATH"

SPRINGBOOT_PORT="${SPRINGBOOT_PORT}" envsubst < "$PROM_CONFIG_PATH" | sudo tee /opt/prometheus/prometheus.yaml >/dev/null

docker run -d --restart unless-stopped \
  --name prometheus \
  --network app-net \
  -p 127.0.0.1:9090:9090 \
  -v /opt/prometheus/prometheus.yaml:/etc/prometheus/prometheus.yaml:ro \
  -v prometheus-data:/prometheus \
  prom/prometheus:latest \
  --config.file=/etc/prometheus/prometheus.yaml \
  --storage.tsdb.retention.time=15d

for i in $(seq 1 20); do
  if curl -sSf http://127.0.0.1:9090/-/ready >/dev/null; then
    echo "[prometheus] ready"
    exit 0
  fi
  sleep 2
done

echo "[prometheus] not ready but continuing" >&2
