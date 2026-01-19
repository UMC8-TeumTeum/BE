FROM amazoncorretto:21 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar --no-daemon



FROM amazoncorretto:21
WORKDIR /home/app

RUN useradd -u 1000 -m app
COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /home/app/config && chown -R app:app /home/app

USER app
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
