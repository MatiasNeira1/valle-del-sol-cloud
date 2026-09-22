# Configuración manual de Microsoft Entra ID

## Registrar la API

1. En **Entra ID > App registrations**, crea `valle-del-sol-api`, single tenant.
2. Conserva Application (client) ID y Directory (tenant) ID; no son secretos.
3. En **Manifest**, configura `api.requestedAccessTokenVersion` en `2`. Usar un endpoint `/v2.0` desde el cliente no cambia por sí solo la versión del access token; si este campo queda en `null`, Entra emite por defecto un token v1 que el backend de este proyecto rechazará.
4. En **Expose an API**, define `api://<BACKEND_CLIENT_ID>` y el scope delegado `api.access`.
5. En el manifest crea roles habilitados para `User`: `ADMIN`, `STAFF`, `EMERGENCY_ENTITY`, `USER`. Cada rol requiere un GUID real.

Para los access tokens v2 hay dos valores relacionados, pero distintos:

- React solicita `VITE_API_SCOPE=api://<BACKEND_CLIENT_ID>/api.access`.
- El claim `aud` del access token v2 es `<BACKEND_CLIENT_ID>` (el GUID sin `api://`). `AZURE_API_AUDIENCE`, el parámetro Bicep `apiAudience` y el Named Value APIM `api-audience` deben contener exactamente ese GUID.

El issuer esperado es `https://login.microsoftonline.com/<TENANT_ID>/v2.0`. Gateway y los tres microservicios validan firma, vigencia, issuer y el mismo audience; APIM debe usar esos mismos valores.

## Registrar la SPA

1. Crea `valle-del-sol-spa`, single tenant.
2. En **Authentication > Single-page application**, registra `http://localhost:5173` y `https://<SWA>.azurestaticapps.net`.
3. No crees un client secret: React es cliente público.
4. Agrega el permiso delegado `api.access` de la API y concede consentimiento cuando corresponda.
5. Completa `frontend/.env`: tenant, client ID SPA, authority, redirect URI, scope `api://<BACKEND_CLIENT_ID>/api.access` y URL APIM.

MSAL Browser usa Authorization Code con PKCE. `acquireTokenSilent` obtiene el access token y Axios lo envía como `Authorization: Bearer ...`; la aplicación no lo guarda manualmente. Después del login, React obtiene identidad y roles de `GET /api/me`; no usa los roles del ID token de la SPA y no asigna `USER` por defecto.

## Usuarios y roles de demostración

1. Crea/invita cuatro cuentas de demostración sin documentar contraseñas.
2. En **Enterprise applications > valle-del-sol-api > Users and groups**, asigna una a cada rol.
3. Prueba al menos un usuario `ADMIN`, uno `USER` y uno sin rol. El tercero debe autenticarse, pero `/api/me` debe devolver `roles: []` y la interfaz debe mostrar acceso insuficiente.
4. Cierra sesión y vuelve a entrar tras cambios de asignación.

El alta organizacional la realiza un administrador. Si usas Entra External ID, configura su user flow y coloca su URL en `VITE_ENTRA_SIGN_UP_URL`; `/register` redirige allí. La SPA no debe crear usuarios privilegiados. Para automatizarlo se requiere una API confidencial protegida que invoque Microsoft Graph con permisos mínimos.

## Comprobar un token real sin publicarlo

Inicia sesión desde la SPA y confirma en Network que la petición APIM lleva Bearer. En Postman usa OAuth 2.0 Authorization Code with PKCE, endpoints v2.0 del tenant, client ID SPA, callback registrada y el scope de la API.

Inspecciona el payload sólo de forma local (por ejemplo, con el depurador del navegador o un script local que decodifique Base64URL) y muestra únicamente estos campos redactados:

```text
ver = 2.0
iss = https://login.microsoftonline.com/<TENANT_ID>/v2.0
aud = <BACKEND_CLIENT_ID>
scp = api.access
roles = [ADMIN] # según el usuario de prueba
```

La decodificación visual no valida firma ni vigencia: la comprobación efectiva es una llamada a `/api/me` a través de APIM y Spring. No pegues el token en decodificadores web públicos, tickets, capturas, logs o archivos; no imprimas el header `Authorization`. Borra cualquier variable temporal al terminar. No declares la integración con Entra como operativa hasta completar esta prueba con un access token real del tenant.

En PowerShell 7 puedes inspeccionar únicamente esos claims sin escribir el token en el historial ni enviarlo a terceros:

```powershell
$secureToken = Read-Host 'Access token (entrada oculta)' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureToken)
try {
  $token = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
  $part = $token.Split('.')[1].Replace('-', '+').Replace('_', '/')
  if ($part.Length % 4 -eq 2) { $part += '==' }
  if ($part.Length % 4 -eq 3) { $part += '=' }
  $claims = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($part)) | ConvertFrom-Json
  [pscustomobject]@{ ver = $claims.ver; iss = $claims.iss; aud = $claims.aud; scp = $claims.scp; roles = $claims.roles }
} finally {
  [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
  Remove-Variable token, part, claims, secureToken -ErrorAction SilentlyContinue
}
```

Referencia: [Microsoft Learn — Access token claims reference](https://learn.microsoft.com/en-us/entra/identity-platform/access-token-claims-reference).
