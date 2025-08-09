#!/usr/bin/env sh
set -e
cd /home/app

if [ -n "$FIREBASE_CREDENTIALS_BASE64" ]; then
  mkdir -p config
  printf %s "$FIREBASE_CREDENTIALS_BASE64" | base64 -d > config/firebase-adminsdk.json
  export GOOGLE_APPLICATION_CREDENTIALS="/home/app/config/firebase-adminsdk.json"
fi

exec java -Duser.timezone=Asia/Seoul -Dspring.profiles.active=prod -jar app.jar
