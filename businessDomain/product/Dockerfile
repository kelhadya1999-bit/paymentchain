# JRE 21 sobre Alpine (imagen ligera, sin JDK)
FROM eclipse-temurin:21-jre-alpine

# JAR que se pasa al construir: docker build --build-arg JAR_FILE=target/mi-app.jar ...
# (el valor por defecto sirve si solo hay un jar en target/)
ARG JAR_FILE=target/*.jar

# Rootless: grupo "devopsc" y usuario "admin" perteneciente a ese grupo (sin shell ni contraseña)
RUN addgroup -S devopsc \
    && adduser -S -D -G devopsc -s /sbin/nologin admin

WORKDIR /app

# El jar queda a nombre de admin:devopsc
COPY --chown=admin:devopsc ${JAR_FILE} app.jar

# A partir de aquí el contenedor NO se ejecuta como root
USER admin

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]