#!/usr/bin/env bash
set -Eeuo pipefail

if [[ "${ENABLE_GRAFANA:-false}" != "true" ]]; then
  echo "[grafana] disabled"
  exit 0
fi

if [[ -z "${GRAFANA_ADMIN_PASSWORD:-}" ]]; then
  echo "[grafana] GRAFANA_ADMIN_PASSWORD is required" >&2
  exit 1
fi

docker pull grafana/grafana:latest || true

sudo mkdir -p /opt/grafana/provisioning/datasources
sudo cp -f "$(dirname "$0")/../config/grafana/datasources/"*.yaml /opt/grafana/provisioning/datasources/
sudo chmod -R a+r /opt/grafana/provisioning

docker rm -f grafana 2>/dev/null || true

docker run -d --restart unless-stopped \
  --name grafana \
  --network app-net \
  -p 127.0.0.1:3000:3000 \
  -e GF_SECURITY_ADMIN_USER=admin \
  -e GF_SECURITY_ADMIN_PASSWORD="${GRAFANA_ADMIN_PASSWORD}" \
  -e AWS_REGION="${REGION}" \
  -e GF_PATHS_PROVISIONING=/etc/grafana/provisioning \
  -e GF_SERVER_DOMAIN="${DOMAIN:-localhost}" \
  -e GF_SERVER_ROOT_URL="${DOMAIN:+https://${DOMAIN}}" \
  --memory=512m --cpus=0.3 \
  --health-cmd='wget -qO- http://127.0.0.1:3000/api/health || exit 1' \
  --health-interval=30s --health-retries=3 --health-timeout=5s \
  -v grafana-data:/var/lib/grafana \
  -v /opt/grafana/provisioning:/etc/grafana/provisioning:ro \
  grafana/grafana:latest

for i in $(seq 1 12); do
  if docker inspect --format '{{.State.Health.Status}}' grafana 2>/dev/null | grep -q healthy; then
    echo "[grafana] healthy"
    exit 0
  fi
  sleep 5
done

echo "[grafana] not healthy yet but continuing" >&2

