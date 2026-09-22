# Backend Valle del Sol

Reactor Maven Java 21 con `api-gateway`, `ms-alerta-incendios`, `ms-monitoreo-geo` y `ms-notificaciones`. Usa Spring Boot 4.0.5, Spring Cloud 2025.1.2, Resource Server JWT, PostgreSQL y Flyway.

Desde esta carpeta ejecuta `./mvnw clean test`, `./mvnw clean package` o `docker compose up --build`. Copia primero `.env.example` a `.env` y completa valores reales. Sólo el gateway publica `${GATEWAY_PORT:-8080}`; los microservicios permanecen en la red Docker.

Consulta la documentación raíz para Entra, APIM y Azure.
