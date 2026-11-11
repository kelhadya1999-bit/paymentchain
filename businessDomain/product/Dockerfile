FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=target/*.jar

RUN addgroup -S devopsc \
    && adduser -S -D -G devopsc -s /sbin/nologin admin

WORKDIR /app

COPY --chown=admin:devopsc ${JAR_FILE} app.jar

USER admin

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]