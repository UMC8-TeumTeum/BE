FROM amazoncorretto:21 AS build
WORKDIR /app

ENV GRADLE_USER_HOME=/home/gradle/.gradle

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew dependencies --no-daemon || true

COPY src src
COPY config/application-prod.properties config/

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew bootJar --no-daemon


FROM amazoncorretto:21
WORKDIR /home/app

RUN mkdir -p /home/app/config

COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/config/application-prod.properties /home/app/config/

RUN chown -R 1000:1000 /home/app

ENV HOME=/home/app

USER 1000
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dspring.config.additional-location=/home/app/config/", "-jar", "app.jar"]

