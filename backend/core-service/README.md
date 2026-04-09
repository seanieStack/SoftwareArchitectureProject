# Core Service

## Overview

This service is the Book Catalogue bounded context for the E-Library system and manages books, authors, and categories.

## Prerequisites

- Java 17+ (the current `pom.xml` targets Java 21, so use Java 21 for local builds)
- Maven
- Docker

## Environment Variables

| Variable | Example Value |
| --- | --- |
| `CORE_DB_USER` | `admin` |
| `CORE_DB_PASSWORD` | `admin` |
| `CORE_DB_NAME` | `core` |
| `RABBITMQ_USER` | `admin` |
| `RABBITMQ_PASSWORD` | `admin` |

## Running with Docker

Run this from the repository root:

```bash
docker compose --env-file .env.example up -d --build core-service
```

## Running Locally

Make sure `core-db` and `rabbitmq` are already running, then run this from `backend/core-service`:

```bash
./mvnw spring-boot:run
```

## Port

`8081`

## Key Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/books` | List all books |
| `GET` | `/api/books/search` | Search books by keyword, author, or category |
| `GET` | `/api/books/{id}` | Get one book by ID |
| `GET` | `/api/books/{id}/availability` | Check availability for one book |
| `POST` | `/api/books` | Create a new book |
| `PUT` | `/api/books/{id}` | Update an existing book |
| `DELETE` | `/api/books/{id}` | Delete a book |

## RabbitMQ Events

| Field | Value |
| --- | --- |
| Exchange | `core.events` |
| Routing Key | `book.removed` |
| Event | `BookRemovedEvent` |
