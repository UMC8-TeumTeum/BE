FROM openjdk:21-jdk-slim

RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /home/app

COPY build/libs/*.jar app.jar
COPY --chmod=755 entrypoint.sh /entrypoint.sh
RUN mkdir -p /home/app/config && chown -R app:app /home/app

USER app
ENTRYPOINT ["/entrypoint.sh"]

