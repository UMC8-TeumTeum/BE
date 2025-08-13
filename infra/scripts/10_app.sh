#!/usr/bin/env bash
set -Eeuo pipefail

sudo mkdir -p /opt/app/config
printf %s "${FIREBASE_CREDENTIALS_BASE64}" | base64 -d | sudo tee /opt/app/config/firebase-adminsdk.json >/dev/null

OPEN_API_KEY_DECODED="$(printf %s "${OPEN_API_KEY_B64}" | base64 -d 2>/dev/null || true)"
if [[ -z "${OPEN_API_KEY_DECODED}" ]]; then
  echo "[app] OPEN_API_KEY base64 decode failed" >&2
  exit 1
fi

docker pull "${IMAGE}" || docker pull "$(echo "${IMAGE}" | sed 's/:.*$/:latest/')"

docker run -d --restart unless-stopped \
  --name spring-app \
  --network app-net \
  -p "${SPRINGBOOT_PORT}:${SPRINGBOOT_PORT}" \
  -e "SPRINGBOOT_PORT=${SPRINGBOOT_PORT}" \
  -e "SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}" \
  -e "DB_URL=${DB_URL}" \
  -e "DB_USERNAME=${DB_USERNAME}" \
  -e "DB_PASSWORD=${DB_PASSWORD}" \
  -e "AWS_ACCESS_KEY=${AWS_ACCESS_KEY}" \
  -e "AWS_SECRET_KEY=${AWS_SECRET_KEY}" \
  -e "S3_BUCKET=${S3_BUCKET}" \
  -e "S3_PREFIX=${S3_PREFIX}" \
  -e "OPEN_API_KEY=${OPEN_API_KEY_DECODED}" \
  -v /opt/app/config/firebase-adminsdk.json:/app/config/firebase-adminsdk.json:ro \
  "${IMAGE}"

echo "[app] deployed"
