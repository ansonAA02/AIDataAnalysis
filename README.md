# AIDataAnalysis

AIDataAnalysis is an MVP for an AI-assisted finance analytics platform. It demonstrates a finance dashboard, budget variance analysis, risk alerts, management reports, and AI-powered financial Q&A over sample data.

The current system is not yet production-ready for real enterprise finance data. The company-readiness work is tracked in:

- [System issues](docs/company-readiness/system-issues.md)
- [Remediation plan](docs/company-readiness/remediation-plan.md)
- [Useful docs map](docs/company-readiness/useful-docs-map.md)

## Tech Stack

- Backend: Spring Boot 3.4.5, Java 21, Spring Data JPA, MySQL, Redis, Spring AI
- Frontend: Vue 3, Vite, TypeScript, Vue Router, Axios, ECharts, Vitest
- Runtime: Docker Compose, Caddy, MySQL, Redis

## Prerequisites

- Docker Desktop
- Node.js 22
- Java 21

The backend includes Maven Wrapper, so a local Maven installation is not required.

## Environment

Copy the example environment file and fill in local values:

```powershell
Copy-Item .env.example .env
```

For AI calls, set `DEEPSEEK_API_KEY` in `.env`. Without a real key, AI endpoints should be treated as degraded or test-only.

## Local Development

Start infrastructure:

```powershell
docker compose up -d mysql redis
```

Run the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Run the frontend:

```powershell
cd frontend
npm ci
npm run dev
```

Default URLs:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/health`
- MySQL: `localhost:3307`
- Redis: `localhost:6379`

## Tests and Builds

Frontend:

```powershell
cd frontend
npm ci
npm test
npm run build
```

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Linux/macOS:

```bash
cd backend
./mvnw test
```

## Docker

Development profile:

```powershell
docker compose --profile dev up -d
```

Production-style compose:

```powershell
docker compose -f docker-compose.prod.yml up -d --build
```

Before using production-style compose, replace all default passwords and set a strict `APP_CORS_ALLOWED_ORIGINS` value.

## Documentation

Current useful documentation:

- [Product/system design](docs/superpowers/specs/2026-05-10-ai-finance-platform-design.md)
- [System issues](docs/company-readiness/system-issues.md)
- [Remediation plan](docs/company-readiness/remediation-plan.md)
- [Docs map](docs/company-readiness/useful-docs-map.md)

## Current Readiness Notes

The project is moving from MVP toward a company-ready internal system. The next priorities are authentication/authorization, validation and error handling, database migration, AI governance, observability, and production deployment hardening.
