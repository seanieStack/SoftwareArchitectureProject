# API Reference

Everything goes through the gateway at `http://localhost:8080`.

All requests are json. Use `Authorization: Bearer <token>` for everything except `/api/auth/**`.

For an interactive version see http://localhost:8080/swagger-ui.html

## Status codes

| Code | What |
|---|---|
| 200 | ok |
| 201 | created |
| 204 | done, no body |
| 400 | bad request / validation |
| 401 | no/bad token |
| 403 | not admin |
| 404 | not found |
| 409 | conflict (eg duplicate isbn) |
| 503 | downstream service down |

## Auth

### POST /api/auth/register

```json
{ "fullName": "Alice", "email": "alice@studentmail.ul.ie", "password": "atleast8" }
```

201 returns the same shape as login.

Email must end in `@studentmail.ul.ie` or `@ul.ie`. ADMIN cant be self-registered.

### POST /api/auth/login

```json
{ "email": "student@studentmail.ul.ie", "password": "student123" }
```

200:
```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "uuid",
  "user": { "id": 1, "email": "...", "fullName": "...", "userType": "student" }
}
```

### POST /api/auth/refresh

```json
{ "refreshToken": "uuid" }
```

Returns same shape with a new refresh token (rotation).

### POST /api/auth/forgot-password

```json
{ "email": "..." }
```

Always 200 even if the email doesnt exist (so it dosent leak who is registered).

### POST /api/auth/reset-password

```json
{ "token": "uuid", "newPassword": "newone" }
```

## Books

### GET /api/books

Array of books. Each one:
```json
{
  "id": 5,
  "title": "Clean Architecture Foundations",
  "isbn": "9780000000005",
  "authors": ["Robert C. Martin"],
  "categories": ["Software Engineering"],
  "totalCopies": 2,
  "availableCopies": 2,
  "active": true,
  "createdAt": "...",
  "updatedAt": "..."
}
```

### GET /api/books/search?keyword=&author=&category=

All optional. Keyword matches title and isbn.

### GET /api/books/{id}

One book.

### GET /api/books/{id}/availability

```json
{ "bookId": 5, "title": "...", "available": true, "availableCopies": 2 }
```

### POST /api/books (ADMIN)

Same body as `BookRequest`. Returns 201 with the book.

### PUT /api/books/{id} (ADMIN)

Full replace.

### PATCH /api/books/{id}/retire (ADMIN)

Shrinks total copies to whats currently borrowed. If nothings borrowed it just deletes the book.

### DELETE /api/books/{id} (ADMIN)

409 if any copies are out, use retire instead.

## Borrows

### GET /api/borrows/user/{userId}

Array of borrows.

### GET /api/borrows/{borrowId}

One borrow.

### POST /api/borrows

```json
{ "userId": 101, "bookId": 5, "deadline": "2026-05-26T14:01:09" }
```

Returns 201 with the borrow. Errors:
- 404 if book doesnt exist
- 400 if its already borrowed
- 503 if core is down

### PATCH /api/borrows/{borrowId}/return

Returns 200 with the updated borrow.

## Fines

### GET /api/fines/user/{userId}?acknowledged=

If `acknowledged=true` only returns unacknowledged ones (a bit confusing tbh).

### GET /api/fines/{fineId}

One fine.

### PATCH /api/fines/{fineId}/acknowledge

Marks acknowledged. 204.

### PATCH /api/fines/{fineId}/pay

Marks paid. 204. (Note: paidAt isnt set, theres a todo in the entity)

## Notifications

### GET /api/notifications/user/{userId}?unreadOnly=true|false

### PATCH /api/notifications/{notificationId}/read

204.

## Admin (core)

### GET /api/admin/counts

```json
{ "activeLoans": 0, "totalBooks": 96, "registeredUsers": 7 }
```

`activeLoans` is always 0 because loans live in support-service, didnt have time to wire it up properly.

### GET /api/admin/users

Array of users.

### PATCH /api/admin/users/{userId}/role

```json
{ "role": "STAFF" }
```

### DELETE /api/admin/users/{userId}

204.

## Admin (support)

### GET /api/admin/analytics

```json
{
  "totalBorrows": 124,
  "activeBorrows": 31,
  "overdueBorrows": 4,
  "totalFinesCollected": 17,
  "unpaidFines": 9
}
```
