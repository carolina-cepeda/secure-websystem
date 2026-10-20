# Enterprise Risk Lab

Sistema web seguro como control de riesgo empresarial: frontend en React + backend
Spring Boot como OAuth 2.0 Resource Server, autenticado con Auth0 y desplegado tras HTTPS.

> URL desplegada: https://enterprise-risk-lab.duckdns.org

## Estructura

```
.
├── backend/     API Spring Boot (OAuth 2.0 Resource Server)
├── frontend/    SPA Vite + React (cliente Auth0)
└── deploy/      Archivos para despliegue (systemd, Apache, guía)
```

## Desarrollo local

Backend:

```bash
cd backend
# Modo jwt: poner issuer/audience en variables o en application.properties
export AUTH0_ISSUER_URI="https://TU_TENANT.us.auth0.com/"
export AUTH0_AUDIENCE="https://enterprise-risk-api"
./mvnw spring-boot:run   # http://localhost:8080
```

Frontend:

```bash
cd frontend
cp .env.local.example .env.local
# Rellenar: VITE_AUTH0_DOMAIN, VITE_AUTH0_CLIENT_ID, VITE_AUTH0_AUDIENCE, VITE_API_URL
npm install
npm run dev                        # http://localhost:5173
```

## Configuración Auth0

En Auth0:

1. **API** `Enterprise Risk Lab API` → Identifier `https://enterprise-risk-api`, RS256. Añadir permiso `read:risk`.
2. **SPA** `Enterprise Risk Lab Frontend` → Allowed Callback/Logout/Web Origins:
   - `http://localhost:5173`
   - `https://enterprise-risk-lab.duckdns.org`
3. **APIs** de la SPA → **User-Delegated Access** → conceder `read:risk` (imprescindible; no basta con Client Access).
4. Crear un **usuario de prueba** en Authentication → Database.

## Despliegue

Ver `deploy/README-DEPLOY.md`. Archivos listos:
- `deploy/enterprise-risk-api.service` (systemd, modo jwt)
- `deploy/enterprise-risk.conf` (Apache :80 + reverse proxy /api/ a :8080)

## Checklist de verificación

- [ ] Las peticiones sin autenticación a `/api/risk-status` devuelven `401`.
- [ ] Un usuario autenticado puede llamar a `/api/risk-status` desde el frontend.
- [ ] El backend valida el issuer y el audience del JWT.
- [ ] La aplicación está disponible en `https://enterprise-risk-lab.duckdns.org`.
- [ ] El navegador muestra un certificado válido y el icono de candado HTTPS.
- [ ] El puerto `8080` no es accesible desde internet.
- [ ] `sudo certbot renew --dry-run` se ejecuta correctamente.

## Evidencias

- [ ] Captura de pantalla de una llamada autenticada exitosa a `/api/risk-status`.
- [ ] Captura o salida de comando mostrando el certificado válido.

## Reflexión de controles de riesgo

| Control | Riesgo reducido | Evidencia |
|---------|-----------------|-----------|
| Autenticación con Auth0 | Suplantación y gestión débil de credenciales | Flujo de login con Auth0 e identidad del usuario |
| Validación JWT en la API | Acceso no autorizado a la API | `401` sin token válido / validación de firma |
| Validación de audience | Reutilización de token para otra API | Solo se acepta el audience `https://enterprise-risk-api` |
| HTTPS con certificado válido | Exposición de credenciales/tokens en tránsito | Certificado de Let's Encrypt en el navegador |
| Proxy inverso con Apache | Exposición directa del puerto de la aplicación | Solo puertos 80/443 públicos; backend en `127.0.0.1:8080` |
| Servicio con usuario no privilegiado + logs | Falta de trazabilidad / exceso de privilegios | Servicio `enterprise-risk-api` con usuario `riskapi`, `journalctl -u enterprise-risk-api` |

## Notas de seguridad

- Nunca se commitea `.env.local`, credenciales del servidor, secretos de Auth0 ni tokens de Duck DNS.
- La SPA usa únicamente el Client ID de Auth0 — nunca un client secret.
- CORS se restringe a los orígenes exactos de desarrollo y producción.
- Los access tokens son sensibles: no se muestran, registran ni almacenan manualmente.
