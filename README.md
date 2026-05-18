# E-Library Project

A digital library system that allows students and staff to browse, borrow, and manage books and academic resources online. This is build as a full-stack microservices application for CS4135 - Software Architectures.

## Project Overview
E-Library is microservice based library platform designed for use by students, staff and admins. Users will be able to browse, search, filter the books available, borrow and return books and recieve notifications for due dates and overdue borrows. Admins will be able to manage (add and remove) books and user accounts.

The project is build with a react frontend which communicates and a spring cloud gateway, which routes requests to the 2 backend spring microservices.

## Architecture Overview

<img width="441" height="601" alt="Untitled Diagram drawio" src="https://github.com/user-attachments/assets/9c45266a-ad42-499c-8e5b-61f4f8830a72" />

## Tech Stack

### Frontend

| Technology | Version |
|---|---|
|React|19.x|
|TypeScript|5.9|
|Vite|7.x|
|Redux Toolkit|2.11.x|
|React Router|7.x|
|Tailwind CSS|4.x|
|Zod|4.x|
|ESLint|9.x|

### Backend
|Technology|Version|
|---|---|
|Spring Boot|4.0.2|
|Java|21|
|Maven|3.9.x|
|Spring Security + JWT (JJWT)|0.12.6|
|Spring Cloud Gateway|2025.1.0|
|Spring Cloud Netflix Eureka|2025.1.0|
|Spring Cloud Config|2025.1.0|
|Spring Data JPA|4.0.x|
|PostgreSQL|17|
|RabbitMQ|4.x|
|Lombok|latest|

## Getting Started

### Prerequisites

- Java 21
- Node.js 22 or higher
- Maven (or use the included `mvnw` wrapper)
- PostgreSQL (or use H2 for local development)

### Clone the Repository

```bash
git clone https://github.com/seanieStack/SoftwareArchitectureProject
cd e-library
```

## Environment Variables

Copy `.env.example` to `.env` and fill in your values before running via Docker Compose:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `DOCKER_ORG` | Your Docker Hub username (for production builds) |
| `CORE_DB_USER` | PostgreSQL user for core-db |
| `CORE_DB_PASSWORD` | PostgreSQL password for core-db |
| `CORE_DB_NAME` | Database name for core-db |
| `SUPPORT_DB_USER` | PostgreSQL user for support-db |
| `SUPPORT_DB_PASSWORD` | PostgreSQL password for support-db |
| `SUPPORT_DB_NAME` | Database name for support-db |
| `RABBITMQ_USER` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `JWT_SECRET` | Long random secret used to sign JWTs (min 32 chars) |
| `INTERNAL_SECRET` | Shared secret for service-to-service internal calls |
| `FRONTEND_BASE_URL` | Public frontend URL (e.g. `http://localhost:5173`) |
| `VITE_BACKEND_URL` | Backend URL used by the Vite dev server proxy |

All services are wired together automatically when using Docker Compose. For local development without Compose, each service reads these values from the environment or falls back to safe defaults defined in its `application.yml`.

## Running the Project

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend — core-service

```bash
cd backend/core-service
./mvnw spring-boot:run
```

### Backend — support-service

```bash
cd backend/support-service
./mvnw spring-boot:run
```

### API Gateway

```bash
cd backend/api-gateway
./mvnw spring-boot:run
```

The gateway starts on port **8080** and is the single entry point for all API requests. It requires `eureka-server` and `config-service` to be running first.

> **Recommended:** Use Docker Compose to start all services together:
> ```bash
> docker compose up --build
> ```

More details available in [wiki](https://github.com/seanieStack/SoftwareArchitectureProject/wiki/Setup-Instructions).

## API Documentation

All requests go through the API Gateway at `http://localhost:8080`.

### Auth (`core-service`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Log in, returns access + refresh tokens |
| POST | `/api/auth/refresh` | Public | Exchange a refresh token for a new access token |
| POST | `/api/auth/forgot-password` | Public | Request a password-reset email |
| POST | `/api/auth/reset-password` | Public | Reset password using emailed token |

### Books (`core-service`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/books` | Required | List all books |
| GET | `/api/books/search?keyword=&author=&category=` | Required | Search/filter books |
| GET | `/api/books/{bookId}` | Required | Get a single book |
| GET | `/api/books/{bookId}/availability` | Required | Check book availability |
| POST | `/api/books` | ADMIN | Create a book |
| PUT | `/api/books/{bookId}` | ADMIN | Update a book |
| DELETE | `/api/books/{bookId}` | ADMIN | Delete a book |

### Borrows (`support-service`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/borrows/user/{userId}` | Required | List all borrows for a user |
| GET | `/api/borrows/{borrowId}` | Required | Get a single borrow record |
| POST | `/api/borrows` | Required | Borrow a book |
| PATCH | `/api/borrows/{borrowId}/return` | Required | Return a borrowed book |

### Fines (`support-service`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/fines/user/{userId}?acknowledged=` | Required | List fines for a user (optional unacknowledged filter) |
| GET | `/api/fines/{fineId}` | Required | Get a single fine |
| PATCH | `/api/fines/{fineId}/acknowledge` | Required | Acknowledge a fine |
| PATCH | `/api/fines/{fineId}/pay` | Required | Pay a fine |

### Notifications (`support-service`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/notifications/user/{userId}?unreadOnly=true` | Required | List notifications for a user |
| PATCH | `/api/notifications/{notificationId}/read` | Required | Mark a notification as read |

## Authentication & Authorization

The project uses **JWT-based stateless authentication** enforced at the API Gateway layer.

### Flow

1. The client calls `POST /api/auth/login` (public, bypasses JWT check) and receives an `accessToken` and `refreshToken`.
2. All subsequent requests must include the token in the `Authorization` header:
   ```
   Authorization: Bearer <accessToken>
   ```
3. The **API Gateway** (`JwtAuthenticationFilter`) validates the token on every request before routing it downstream. If the token is missing, expired, or invalid, the gateway returns `401 Unauthorized` immediately.
4. On a valid token, the gateway strips any caller-supplied identity headers (prevents spoofing) and injects two verified headers for downstream services:
   - `X-User-Id` — the authenticated user's ID
   - `X-User-Role` — the user's role (`ROLE_USER` or `ROLE_ADMIN`)
5. Downstream services (`core-service`, `support-service`) trust these headers via `GatewayHeaderAuthFilter` and apply Spring Security rules accordingly.

### Roles

| Role | Access |
|---|---|
| `ROLE_USER` | Browse books, borrow/return, view own fines and notifications |
| `ROLE_ADMIN` | All user access + create/update/delete books, manage users, view analytics |

### Token Refresh

When the access token expires, call `POST /api/auth/refresh` with the refresh token to obtain a new access token without re-authenticating.


## Deployment

The project ships with a full production deployment pipeline targeting an **Oracle Cloud Always Free** ARM instance (Ubuntu 22.04).

### Architecture

```
Internet
   │
   ▼
Oracle Cloud VM (public IP)
   │
   ├─ NGINX (reverse proxy + TLS termination)
   │      ├─ elibrary.example.com  → frontend container  (port 3000)
   │      └─ api.elibrary.example.com → api-gateway container (port 8080)
   │
   └─ Docker Compose (internal network)
          ├─ frontend          (NGINX serving built React SPA)
          ├─ api-gateway
          ├─ core-service      → core-db (PostgreSQL, internal only)
          ├─ support-service   → support-db (PostgreSQL, internal only)
          ├─ eureka-server
          ├─ config-service    (mounts ~/config-repo)
          └─ rabbitmq          (internal only)
```

### Files

| File | Purpose |
|---|---|
| `docker-compose.prod.yml` | Production Compose — pulls images from Docker Hub, no ports exposed for databases/brokers |
| `.github/workflows/production-release.yml` | CI/CD pipeline — runs tests, builds all services in parallel, pushes to Docker Hub, deploys via SSH |
| `nginx/elibrary.conf` | NGINX reverse proxy config with HTTPS and security headers |
| `scripts/setup-vm.sh` | One-time Oracle VM provisioning script |
| `frontend/Dockerfile` | Multi-stage build: Vite compile → NGINX static server |
| `backend/*/Dockerfile` | Multi-stage build for each Spring Boot service: Maven compile (JDK Alpine) → lean runtime (JRE Alpine) |

### Step 1 — Oracle Cloud VM

1. Create an Ampere (ARM) instance with Ubuntu 22.04, at least 4 OCPUs / 12 GB RAM.
2. Add VCN Ingress Rules for ports **22**, **80**, and **443**.
3. SSH into the VM and run:

```bash
chmod +x scripts/setup-vm.sh && sudo ./scripts/setup-vm.sh
```

Edit the variables at the top of the script first (`DOMAIN`, `API_DOMAIN`, `CONFIG_REPO_URL`).

### Step 2 — GitHub Secrets

Go to **Settings → Secrets and variables → Actions** and add:

| Secret | Value |
|---|---|
| `DOCKER_USERNAME` | Your Docker Hub username |
| `DOCKER_PASSWORD` | Your Docker Hub access token |
| `PROD_SERVER_IP` | Public IP of the Oracle VM |
| `PROD_SERVER_USER` | SSH user (usually `ubuntu`) |
| `PROD_SSH_KEY` | Contents of your `.pem` private key |
| `CORE_DB_USER` | PostgreSQL user for core-db |
| `CORE_DB_PASSWORD` | PostgreSQL password for core-db |
| `CORE_DB_NAME` | Database name for core-db |
| `SUPPORT_DB_USER` | PostgreSQL user for support-db |
| `SUPPORT_DB_PASSWORD` | PostgreSQL password for support-db |
| `SUPPORT_DB_NAME` | Database name for support-db |
| `RABBITMQ_USER` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `JWT_SECRET` | Long random secret used to sign JWTs |
| `INTERNAL_SECRET` | Shared secret for service-to-service internal calls |
| `FRONTEND_BASE_URL` | Public frontend URL, for example `https://elibrary.example.com` |

### Step 3 — Deploy

Push to `main`. The GitHub Actions pipeline will:

1. Build all 5 Spring Boot services **in parallel** using a matrix strategy.
2. Build the React frontend with a multi-stage Docker build (Vite → NGINX).
3. Push all images to Docker Hub.
4. SSH into the Oracle VM, pull the new images, and restart changed containers only.

### DNS

Point two A records at the VM's public IP in your registrar:

```
@    →  <Oracle VM IP>    # elibrary.example.com
api  →  <Oracle VM IP>    # api.elibrary.example.com
```

## Team & Roles

| Name | Role |
|---|---|
| Seanie Stack (22374302) | Backend Core Service Developer |
| Jason Cushen (22342516) | Frontend Developer & Auth Service |
| Ugochukwu Egbokwu (22359974) | Backend Developer & DevOps |
| Leonardo Ilascu (22353046) | Backend Developer & Testing |
| Mark Callan (22363246) | Team Lead & DevOps | 
More details available in [wiki](https://github.com/seanieStack/SoftwareArchitectureProject/wiki/Team-Organization-And-Roles)


## Contributing Guidelines

### Branching Strategy

Use feature branches off `main`. Branch names should follow this convention:

```
feature/<location>/<short-description>
fix/<location>/<short-description>
```

Example: `feature/backend-core-service/added-cors-config`, `fix/frontend/login-button-disappears-after-login`

### Commits

Write meaningful messages about all changes in commit:
```
reworked cors config to allow request to /admin
added filter for availiblity
```

### Pull requests
- PRs must target `main`
- Requires atleast lgtm from someone who understands / works on your service
- Do not merge your own PRs without review

### Code style
- Backend: follow standard spring conventions and use lombok to reduce boilerplate
- Frontend: ESLint is required. Run `npm run lint` before requesting a pr
- No hardcoded secrets, use proper .env conventions