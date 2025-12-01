FROM amazoncorretto:21

RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /home/app

COPY build/libs/*.jar app.jar
RUN mkdir -p /home/app/config && chown -R app:app /home/app

USER app

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

