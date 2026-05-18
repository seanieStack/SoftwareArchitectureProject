# support-service

Port 8082. Borrows, fines, notifications.

## What it has

- `controllers/` - BorrowController, FineController, NotificationController
- `admin/AdminAnalyticsController` - `/api/admin/analytics`
- `services/` - BorrowService, FineService, NotificationService
- `entities/` - Borrow, Fine, Notification (+ enums)
- `clients/CoreServiceClient` - HTTP calls to core, wrapped in resilience4j
- `scheduling/` - BorrowScheduler (9am), FineScheduler (midnight)
- `messaging/` - EventPublisher + NotificationListener
- `security/` - same gateway header filter as core

## Endpoints

| Method | Path |
|---|---|
| GET | `/api/borrows/user/{userId}` |
| GET | `/api/borrows/{id}` |
| POST | `/api/borrows` |
| PATCH | `/api/borrows/{id}/return` |
| GET | `/api/fines/user/{userId}?acknowledged=` |
| GET | `/api/fines/{id}` |
| PATCH | `/api/fines/{id}/acknowledge` |
| PATCH | `/api/fines/{id}/pay` |
| GET | `/api/notifications/user/{userId}?unreadOnly=` |
| PATCH | `/api/notifications/{id}/read` |
| GET | `/api/admin/analytics` (admin only) |

## Borrowing a book

1. Call core: does this book exist? (via CoreServiceClient)
2. Check we havnt already borrowed it (no double borrow)
3. Insert the borrow row, status BORROWED
4. Call core: decrement available_copies
5. Publish `borrow.created` event to rabbitmq

If step 4 fails the borrow is already in our db. We accept this, the project isnt big enough to need a saga.

## Fines

`FineService` runs at midnight from `FineScheduler`:

```
processNewlyOverdue:
  borrows with deadline < now and status=BORROWED:
    set status OVERDUE
    create fine if no unpaid one exists (EUR 0.50)
    publish fine.created

updateAccruingFines:
  for each OVERDUE:
    fine.amount = days_overdue x EUR 0.50
```

## Notifications

`NotificationListener` consumes 3 queues and writes rows to `notifications`:

- `borrow.created` -> "You borrowed book X. Due ..."
- `borrow.due-soon` -> "Reminder, due tomorrow"
- `fine.created` -> "You have a new fine of EUR X"

## Env vars

| Var | What |
|---|---|
| `INTERNAL_SECRET` | must match core |
| `CORE_SERVICE_URL` | where core is |
| `CORE_SERVICE_RETRY_*` | retry config |
| `CORE_SERVICE_CB_*` | circuit breaker config |
| `SUPPORT_DB_*` | db creds |
| `RABBITMQ_*` | rabbit conn |
