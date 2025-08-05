f [[ -n "$OPEN_API_KEY_BASE64" ]]; then
  export OPEN_API_KEY=$(echo "$OPEN_API_KEY_BASE64" | base64 -d)
fi

# Run the app
exec java -Duser.timezone=Asia/Seoul -Dspring.profiles.active=prod -jar /app.jar
