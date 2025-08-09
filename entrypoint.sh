#!/usr/bin/env sh
set -e
exec java -Duser.timezone=Asia/Seoul -Dspring.profiles.active=prod -jar app.jar
