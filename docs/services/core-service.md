# core-service

Port 8081. Owns books, users, auth.

## What it has

- `controllers/` - BookController, AdminController, AuthController, InternalBookController
- `services/` - BookService, AdminService, AuthService, RefreshTokenService, PasswordResetService
- `entities/` - Book, Author, Category, BookCopy
- `auth/domain/` - User, UserRole, RefreshToken, PasswordResetToken
- `security/` - JwtService, SecurityConfig, GatewayHeaderAuthFilter, InternalSecretFilter
- `messaging/` - BookEventPublisher (publishes `book.removed`)

## Endpoints

Through the gateway. See [api reference](../api/reference.md) for the bodies.

| Method | Path | Auth |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/refresh` | public |
| POST | `/api/auth/forgot-password` | public |
| POST | `/api/auth/reset-password` | public |
| GET | `/api/books`, `/search`, `/{id}`, `/{id}/availability` | any user |
| POST/PUT/PATCH/DELETE | `/api/books/**` | admin |
| GET/PATCH/DELETE | `/api/admin/users/**` | admin |
| GET | `/api/admin/counts` | admin |

## Internal endpoints

These are NOT routed through the gateway. Only called by support-service.

- `GET /api/internal/books/{id}/exists`
- `PATCH /api/internal/books/{id}/borrow`
- `PATCH /api/internal/books/{id}/return`

Protected by `X-Internal-Secret` header. `InternalSecretFilter` and the controller both check it (belt + braces).

## Auth flow

```
register/login --> AuthService
                       |
                       v
                  bcrypt verify -> issue JWT (1h) + refresh token (7d)
```

Refresh token rotates every time you use it. Password reset revokes all refresh tokens for that user.

Roles come from email:
- @studentmail.ul.ie -> STUDENT
- @ul.ie -> STAFF
- anything else -> 400, registration rejected
- ADMIN cant be self-registered

## Book invariants

Enforced in the entity:
- `totalCopies >= 1`
- `0 <= availableCopies <= totalCopies`
- Cant delete a book if any copies are out (gives a 409). Use `/retire` instead which shrinks totalCopies.

## Env vars

| Var | Default | What |
|---|---|---|
| `JWT_SECRET` | dev fallback | must match api-gateway |
| `JWT_EXPIRATION_MS` | 3600000 (1h) | access token life |
| `INTERNAL_SECRET` | dev fallback | must match support-service |
| `RABBITMQ_*` | localhost | rabbit conn |
| `CORE_DB_*` | from .env | db creds |
| `EUREKA_URI` | localhost:8761 | |
