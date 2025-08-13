#!/usr/bin/env bash
set -Eeuo pipefail

ensure_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    echo "[common] docker not found" >&2
    exit 1
  fi
  docker network inspect app-net >/dev/null 2>&1 || docker network create app-net
  docker volume inspect redis-data >/dev/null 2>&1 || docker volume create redis-data
  docker volume inspect grafana-data >/dev/null 2>&1 || docker volume create grafana-data
  docker volume inspect prometheus-data >/dev/null 2>&1 || docker volume create prometheus-data
}

cleanup_old() {
  docker rm -f spring-app redis-container grafana prometheus 2>/dev/null || true
  docker system prune -af || true
}
