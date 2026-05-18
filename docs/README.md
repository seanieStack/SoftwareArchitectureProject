# E-Library Docs

This is our docs for the CS4135 Software Architectures project. Its a small library app where students can borrow books, get fines if they are late and admins can manage everything.

We split it into a few microservices because thats what the assignment asks for.

## Where to look

| File | Whats in it |
|---|---|
| [Getting Started](./getting-started.md) | How to run it on your machine |
| [Architecture](./architecture/overview.md) | The big picture |
| [Services](./services/) | One page per service |
| [API Reference](./api/reference.md) | All the endpoints |
| [Auth](./security/auth.md) | How login and JWTs work |
| [Messaging](./messaging/events.md) | RabbitMQ stuff |
| [Data Model](./data/schemas.md) | Tables and seed data |
| [Contributing](./contributing.md) | Branch names + PR rules |

## The shape of it

```
[ React SPA ] --> [ API Gateway ] --> core-service     (books, users, auth)
                                  --> support-service  (borrows, fines, notifs)
                                              |
                                              +-> RabbitMQ
                                              +-> Postgres x 2
```

Eureka does service discovery and Config Service holds shared config.

Deployment isnt covered here, somone else on the team is doing that part.
