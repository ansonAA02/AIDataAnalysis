# Agent.md

## 1. Purpose

This file provides working instructions for coding agents operating in this repository.

The goal is to help agents understand:

- what this project is
- how the system is structured
- what the current priorities are
- how changes should be made safely
- what quality bar is expected before handoff

## 2. Project Overview

Project name:

- `AIDataAnalysis`

Current product positioning:

- An AI finance analytics platform for management reporting and business decision support
- Current state is a strong MVP, but not yet a company-mainstream production system

Primary business capabilities:

- Dashboard overview for finance metrics
- AI summary and AI Q&A
- Budget variance analysis
- Financial report generation
- Risk signal presentation

Current tech stack:

- Backend: Spring Boot 3.4.5, Java 21, Spring Data JPA, Redis, Spring AI
- Frontend: Vue 3, Vite, TypeScript, Axios, ECharts, Vitest
- Database: MySQL
- Local runtime: Docker Compose
- Production runtime: Docker Compose + Caddy

## 3. Repository Structure

Top-level folders:

- `backend/`: Spring Boot backend service
- `frontend/`: Vue frontend application
- `docs/`: design, plan, review, and readiness documents
- `docker-compose.yml`: local development infrastructure and optional dev containers
- `docker-compose.prod.yml`: production-oriented compose file
- `Caddyfile`: reverse proxy configuration

Backend structure summary:

- `backend/src/main/java/com/aifinance/common`: shared response model, health, root endpoints, web config
- `backend/src/main/java/com/aifinance/finance`: finance domain, repositories, DTOs, services
- `backend/src/main/java/com/aifinance/dashboard`: dashboard APIs and DTOs
- `backend/src/main/java/com/aifinance/ai`: AI config, DTOs, provider abstraction, controller, service
- `backend/src/main/java/com/aifinance/report`: report APIs and orchestration
- `backend/src/main/resources/db`: schema and seed data

Frontend structure summary:

- `frontend/src/api`: API client modules
- `frontend/src/components`: reusable UI components
- `frontend/src/views`: route-level pages
- `frontend/src/router`: route definitions
- `frontend/src/composables`: shared composition utilities
- `frontend/src/types`: shared type definitions
- `frontend/src/__tests__`: frontend tests

Documentation summary:

- `docs/superpowers/specs`: product and system design
- `docs/superpowers/plans`: implementation plan
- `docs/superpowers/reviews`: issue remediation and hardening plans
- `docs/company-readiness`: company-readiness copies or simplified operational docs

## 4. Current Engineering Reality

Agents must assume the following current realities are important:

- The project has existing business value and test assets, so changes must be incremental
- The repository may already contain unrelated local edits
- The system is being pushed toward a company-mainstream quality bar
- Correctness, maintainability, and delivery stability are more important than adding flashy features

Known strategic gaps:

- Frontend build and dependency consistency need hardening
- AI sync/stream contract needs unification
- Active financial period handling must be centralized
- Backend validation, exception handling, observability, and security are incomplete
- Runtime async execution needs production-grade control

Reference documents:

- `docs/superpowers/reviews/2026-05-26-system-issues-remediation.md`
- `docs/superpowers/reviews/2026-05-26-system-hardening-plan.md`
- `docs/superpowers/specs/2026-05-10-ai-finance-platform-design.md`
- `docs/superpowers/plans/2026-05-10-ai-finance-platform-implementation-plan.md`

## 5. Agent Working Principles

### 5.1 Protect existing work

- Never revert unrelated user changes
- Assume the worktree may be dirty
- If a file has unrelated edits, read carefully before modifying it
- Prefer additive or localized changes over broad rewrites

### 5.2 Fix root causes

- Do not patch symptoms only
- If tests fail because implementation drifted, align the contract instead of muting the test
- If multiple files duplicate protocol logic, extract shared logic instead of copying another implementation

### 5.3 Keep architecture moving in the right direction

- Backend should move toward clearer cross-cutting governance
- Frontend should move toward shared service boundaries and less view-level transport logic
- Business period selection should never remain a hidden magic constant

### 5.4 Prefer safe delivery over large refactors

- Small, verifiable improvements are preferred over sweeping rewrites
- If a larger refactor is needed, split it into reviewable steps
- Preserve current behavior unless the change intentionally corrects broken behavior

## 6. Change Priorities

When multiple improvements are possible, prefer this order:

1. Build failures and broken delivery chain
2. Business correctness issues
3. Contract drift between runtime and tests
4. Broken navigation or obvious UX defects
5. Backend validation and exception governance
6. Health, observability, and operational readiness
7. Security hardening
8. Long-term structural cleanup

Examples of high-priority fixes:

- Frontend cannot build
- AI returns data for the wrong period
- Route points to a missing page
- Tests no longer reflect real transport behavior

## 7. Backend Guidance

Backend modification rules:

- Keep controller logic thin
- Put business rules in services
- Keep provider-specific AI logic inside the AI provider layer
- Avoid scattering environment or transport assumptions across modules

Backend preferred direction:

- Add request validation using standard Spring validation
- Add global exception handling
- Keep a uniform API response and error contract
- Isolate async execution behind dedicated executors
- Improve health and readiness semantics before adding new platform complexity

Backend caution points:

- Do not move AI provider details into controller code
- Do not introduce silent fallback behavior that hides real failures
- Do not add database schema drift without a migration plan if touching persistent structure

## 8. Frontend Guidance

Frontend modification rules:

- Keep pages focused on composition and UI state
- Move transport details into `api/` or shared composables/services
- Reuse shared types and avoid duplicate response models
- Make loading, error, and empty states explicit

Frontend preferred direction:

- Centralize active period state
- Unify AI sync and stream client boundaries
- Keep routes and visible navigation aligned
- Prefer feature-oriented structure when expanding larger areas

Frontend caution points:

- Do not hard-code business IDs like `periodId = 4` in user-facing features
- Do not implement raw streaming protocol parsing in multiple views
- Do not leave dead links in the main navigation

## 9. Testing Guidance

Testing rules:

- Verify substantive changes
- Add or update focused tests when behavior, contract, or regression risk changes
- Avoid noisy tests that merely restate implementation details

Preferred testing approach:

- Frontend tests should mock shared client boundaries, not random internal implementation details
- Backend tests should verify behavior, not just framework wiring
- If a contract changes, update both implementation and tests together

Minimum expectation before handoff:

- Relevant changed area should be validated
- If full test execution is not possible, document exactly what was and was not verified

## 10. Documentation Guidance

Documentation rules:

- Update docs when behavior, contracts, or operational commands change
- Keep remediation and hardening documents aligned with actual implementation progress
- Prefer concise, actionable wording over vague planning language

When to update docs:

- New runtime commands
- New environment variables
- New route or page behavior
- API contract changes
- Security or deployment changes

## 11. Local Run And Verification

Typical local infrastructure:

- MySQL and Redis are defined in `docker-compose.yml`
- Optional `backend-dev` and `frontend-dev` containers exist under `profiles: dev`

Typical verification areas:

- Frontend install/build/test
- Backend test/startup
- Docker Compose configuration validity
- AI configuration and environment variable flow

Important note:

- If environment-dependent commands cannot run in the current machine context, report the gap clearly instead of pretending verification succeeded

## 12. Quality Bar

A change is considered good when it is:

- correct
- scoped
- reviewable
- documented if needed
- validated as much as reasonably possible

A change is not good if it:

- introduces hidden coupling
- duplicates logic unnecessarily
- bypasses existing abstractions without reason
- leaves broken tests unexplained
- silently changes business behavior without clear intent

## 13. Current Target Direction

Agents should align with the following target direction for this repository:

- Move the system from MVP engineering state to company-mainstream engineering state
- Prioritize delivery stability, contract correctness, and shared architecture
- Prepare the codebase for stronger observability, security, and governance

In short:

- stabilize first
- standardize second
- harden third
- expand after the foundation is reliable
