# Enterprise Risk Lab

Secure web system as an enterprise risk control: React frontend + Spring Boot OAuth 2.0
Resource Server, authenticated with Auth0 and deployed behind HTTPS.

> Deployed URL: _(pendiente — Fase 5)_

## Structure

```
.
├── backend/     Spring Boot API (OAuth 2.0 Resource Server)
└── frontend/    Vite + React SPA (Auth0 client)
```

## Status by phase

| Phase | Description | State |
|-------|-------------|-------|
| 0 | Scaffolding (frontend + backend skeletons) | done |
| 1 | Backend API (`/api/risk-status`) | done |
| 2 | Frontend consumes API locally | pending |
| 3 | Auth0 + JWT validation | pending |
| 4 | Public HTTP deploy (Duck DNS + Apache) | pending |
| 5 | HTTPS with Let's Encrypt | pending |
| 6 | Hardening, logging and deliverables | pending |

## Local development

Backend:

```bash
cd backend
./mvnw spring-boot:run   # http://localhost:8080
```

Frontend:

```bash
cd frontend
cp .env.local.example .env.local   # fill in Auth0 values (later phases)
npm install
npm run dev                        # http://localhost:5173
```

## Security notes

- Never commit `.env.local`, server credentials, Auth0 secrets or Duck DNS tokens.
- The SPA uses the Auth0 client ID only — never a client secret.
- CORS is restricted to the exact development and production origins.
- Access tokens are sensitive: do not display, log or store them manually.
