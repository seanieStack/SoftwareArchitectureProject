# Communication Patterns

We use 3 ways for things to talk to eachother.

| What | When | How |
|---|---|---|
| Public REST through gateway | Browser to backend | HTTPS + JSON |
| Internal REST | support-service -> core-service | HTTP inside docker network |
| Async events | Notifications | RabbitMQ |

## 1. Through the gateway

The browser only talks to `api-gateway:8080`. Routes are in `application.yml` like:

```yaml
- id: support-service-borrows
  uri: lb://support-service
  predicates:
    - Path=/api/borrows/**
  filters:
    - name: CircuitBreaker
      args:
        name: support-service-cb
        fallbackUri: forward:/api/fallback/support
```

`lb://` means Eureka looks up the service. Every route has a circuit breaker that falls back to a 503 if the downstream is dead.

## 2. Service to service

`support-service` cant read core-services db so when it needs to know if a book exists it calls:

- `GET /api/internal/books/{id}/exists`
- `PATCH /api/internal/books/{id}/borrow`
- `PATCH /api/internal/books/{id}/return`

These are protected by an `X-Internal-Secret` header. The gateway doesnt route to them.

`CoreServiceClient` wraps the calls in resilience4j retry + circuit breaker. It ignores `BookNotFoundException` for the breaker because a 404 isnt really a failure.

## 3. Events with RabbitMQ

For things that can be slow / fire and forget. Two exchanges:

- `core.events` - publishes `book.removed` (no consumer yet)
- `support.events` - publishes `borrow.created`, `borrow.due-soon`, `fine.created`

`NotificationListener` in support-service listens and writes a row to `notifications`.

## Why both sync and async

Some things you need an answer for right now:
- Does this book exist? (sync, can fail the request)
- Decrement available copies (sync, user wants to know if it worked)

Other things can lag a bit:
- Tell the user they borrowed a book (async, dosent matter if its delayed)
- Send a "due tomorrow" reminder (async, scheduled)

## Stuff thats not great

- No outbox pattern. If the borrow commits but the event publish fails, the notif is lost.
- No idempotency. Replaying an event would create a duplicate notif.
- No event versions, so renaming a field breaks consumers.

We know about these, just out of scope for the project.
