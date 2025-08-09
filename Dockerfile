FROM openjdk:21-jdk-slim

RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /home/app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY --chmod=755 entrypoint.sh /entrypoint.sh

USER app
ENTRYPOINT ["/entrypoint.sh"]
