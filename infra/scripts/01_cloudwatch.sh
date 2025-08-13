#!/usr/bin/env bash
set -Eeuo pipefail

if [[ "${ENABLE_CLOUDWATCH:-false}" != "true" ]]; then
  echo "[cloudwatch] disabled"
  exit 0
fi


if ! command -v /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl >/dev/null 2>&1; then
  echo "[cloudwatch] installing..."
  curl -fsSL -o /tmp/amazon-cloudwatch-agent.deb \
    "https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb"
  sudo dpkg -i /tmp/amazon-cloudwatch-agent.deb
fi

sudo install -d -m 755 -o cwagent -g cwagent /opt/aws/amazon-cloudwatch-agent/etc


sudo cp -f "$(dirname "$0")/../config/cloudwatch-agent.json" \
  /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json


if ! id -nG cwagent | grep -qw adm; then
  sudo usermod -aG adm cwagent || true
fi

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json \
  -s

sudo systemctl enable amazon-cloudwatch-agent
sudo systemctl restart amazon-cloudwatch-agent || true
echo "[cloudwatch] ready"
