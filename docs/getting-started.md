# Getting Started

## What you need

- Java 21
- Node 22 or higher
- Docker + Compose
- Maven (or just use `./mvnw`)

You dont need to install Postgres or RabbitMQ, docker will do that for you.

## Setup

```bash
git clone https://github.com/seanieStack/SoftwareArchitectureProject
cd SoftwareArchitectureProject
cp .env.example .env
```

The defaults in `.env` are fine for local stuff.

## Option 1: Just docker

```bash
docker compose up --build
```

It takes about a min for everything to start up because the services depend on each other.

Then open:
- Frontend: http://localhost:5173
- Gateway: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Eureka: http://localhost:8761
- RabbitMQ: http://localhost:15672 (admin/admin)

## Option 2: Infra in docker, services on host

Better if you want to debug the spring services.

```bash
docker compose up core-db support-db rabbitmq eureka-server config-service

# then in another terminal
cd backend/core-service && ./mvnw spring-boot:run
# same for support-service and api-gateway

cd frontend && npm install && npm run dev
```

## Test accounts

The seed sql makes these for you:

| Email | Password | Role |
|---|---|---|
| student@studentmail.ul.ie | student123 | STUDENT |
| staff@ul.ie | staff123 | STAFF |
| admin@ul.ie | Admin1Pass! | ADMIN |

Theres also some sample students and ~100 books seeded in.

## Tests

```bash
cd backend/core-service && ./mvnw test
cd backend/support-service && ./mvnw test
cd backend/api-gateway && ./mvnw test
cd frontend && npm run lint
```

## Stuff that goes wrong

| Problem | Why |
|---|---|
| Connection refused on 5432 | Postgres still starting, just wait |
| 401 on everything | JWT_SECRET different between gateway and core |
| Cant borrow a book | INTERNAL_SECRET doesnt match |
| No notifications appearing | Check rabbitmq is up at localhost:15672 |
| Service shows UP in eureka but 503s | Probaly still warming up |
