FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY ms-alerta-incendios/pom.xml ms-alerta-incendios/pom.xml
COPY ms-monitoreo-geo/pom.xml ms-monitoreo-geo/pom.xml
COPY ms-notificaciones/pom.xml ms-notificaciones/pom.xml
COPY api-gateway/src api-gateway/src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -pl api-gateway -am clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/api-gateway/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 CMD wget -q -O - http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
