FROM amazoncorretto:21

RUN yum makecache --refresh && \
    yum -y install shadow-utils && \
    groupadd -r app && \
    useradd -r -g app app && \
    yum clean all

WORKDIR /home/app

COPY build/libs/*.jar app.jar
RUN mkdir -p /home/app/config && chown -R app:app /home/app

USER app

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
