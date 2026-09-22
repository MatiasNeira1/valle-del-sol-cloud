FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY ms-alerta-incendios/pom.xml ms-alerta-incendios/pom.xml
COPY ms-monitoreo-geo/pom.xml ms-monitoreo-geo/pom.xml
COPY ms-notificaciones/pom.xml ms-notificaciones/pom.xml
COPY ms-alerta-incendios/src ms-alerta-incendios/src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -pl ms-alerta-incendios -am clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/ms-alerta-incendios/target/*.jar app.jar
USER app
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 CMD wget -q -O - http://127.0.0.1:8081/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
