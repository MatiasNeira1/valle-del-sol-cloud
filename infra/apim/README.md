# Azure API Management

1. Despliega la infraestructura Bicep sólo después de revisar costos y recibir autorización.
2. Importa `openapi.yaml` en la API `valle-del-sol`, conservando `/` como sufijo para que las rutas permanezcan `/api/...`. La especificación incluye `GET /api/me`.
3. Crea los cuatro Named Values de `named-values.example.json`. `api-audience` es el GUID Application (client) ID de la API sin `api://`; el scope de React sí es `api://<API_CLIENT_ID>/api.access`.
4. No marques como secretos los valores públicos de tenant/audience, pero restringe quién puede editarlos.
5. Aplica `policies/api-policy.xml` en el nivel de API.
6. Aplica `require-admin.xml` a `DELETE /api/reportes/eliminar/{id}`.
7. Aplica `require-responder.xml` a `PUT /api/reportes/editar/{id}` y `POST /api/notificaciones/enviar`.
8. Para `POST /api/reportes/crear`, usa una política equivalente que acepte `USER`, `STAFF`, `EMERGENCY_ENTITY` o `ADMIN` (la defensa equivalente ya existe en Spring).

`validate-jwt` genera 401 para token ausente, inválido, expirado, issuer o audience incorrectos. Las políticas de rol se ejecutan después de esa validación y generan 403. En producción elimina el origen localhost de la política y conserva únicamente `frontend-origin`.
