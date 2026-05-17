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
|Redux Toolkit|planned|
|Axios| planned|
|ESLint|9.x|

### Backend
|Technology|Version|
|---|---|
|Spring Boot|4.0.2|
|Java|21|
|Maven|3.9.12|
|Spring Security + JWT|planned|
|Spring Cloud Gateway|planned|
|Spring Data JPA|planned|
|PostgreSQL|planned|
|springdoc-openapi|planned|
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

- not implemented

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

### API Gateway _(once implemented)_

- not implemented

More details available in [wiki](https://github.com/seanieStack/SoftwareArchitectureProject/wiki/Setup-Instructions).

## API Documentation

- not implemented

## Authentication & Authorization

- not implemented


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
| name (id) | role |
| name (id) | role |
| name (id) | role |

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
    
