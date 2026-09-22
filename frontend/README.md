# Frontend Valle del Sol

SPA React 19/Vite autenticada con Microsoft Entra ID mediante MSAL y Authorization Code Flow con PKCE. Copia `.env.example` a `.env`, completa los datos reales y ejecuta `npm ci`, `npm run dev`, `npm run lint`, `npm test` y `npm run build`.

`VITE_API_URL` debe ser APIM en Azure o `http://localhost:8080` en local. Nunca debe apuntar a 8081, 8082 o 8083. No coloques client secrets en variables `VITE_*`.
