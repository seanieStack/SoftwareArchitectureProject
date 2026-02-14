# E-Library Project

A digital library system that allows students and staff to browse, borrow, and manage books and academic resources online. This is build as a full-stack microservices application for CS4135 - Software Architectures.

## Project Overview
E-Library is microservice based library platform designed for use by students, staff and admins. Users will be able to browse, search, filter the books available, borrow and return books and recieve notifications for due dates and overdue borrows. Adims will be able to manage (add and remove) books and user accounts.

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

- not implemented

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
    
