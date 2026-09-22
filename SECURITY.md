# Seguridad

- Rota las credenciales Oracle y reemite el wallet/certificados encontrados. `.gitignore` no revoca secretos ni limpia historial.
- Audita el repositorio real desde su raíz y usa `git rm --cached` sólo tras respaldar y confirmar rutas exactas.
- Guarda secretos Azure en Key Vault/GitHub Secrets y usa federación OIDC en GitHub Actions.
- No registres tokens, contraseñas ni cuerpos con datos personales.
