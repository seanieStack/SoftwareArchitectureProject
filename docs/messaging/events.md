# Events

We use RabbitMQ for async stuff. Topic exchanges, durable queues, json payloads.

## Topology

```
core.events
   - book.removed         (no consumer yet, just setup ready)

support.events
   - borrow.created       -> notifications.borrow.created
   - borrow.due-soon      -> notifications.borrow.due-soon
   - fine.created         -> notifications.fine.created
                                    |
                                    v
                            NotificationListener
                            (writes a row in notifications)
```

Config beans are in `RabbitMQConfig` in both services.

## Events

### book.removed

Published when a book is deleted or retired. Nobody listens yet.

```json
{ "bookId": 7, "isbn": "9780000000007", "title": "..." }
```

### borrow.created

```json
{ "borrowId": 12, "userId": 101, "bookId": 5, "deadline": "..." }
```

Listener creates a notif: "You borrowed book X. Due ..."

### borrow.due-soon

Same shape. Published by `BorrowScheduler` at 9am for things due in the next 24h.

Listener: "Reminder, due tomorrow"

### fine.created

```json
{ "fineId": 4, "borrowId": 12, "userId": 101, "amount": 0.50 }
```

Listener: "You have a new fine of EUR X.XX"

## Scheduled jobs

In support-service:

| Class | Cron | What |
|---|---|---|
| BorrowScheduler.notifyDueSoon | `0 0 9 * * *` (9am) | borrow.due-soon for things due in 24h |
| FineScheduler.processOverdueBorrows | `0 0 0 * * *` (midnight) | flip BORROWED to OVERDUE, create/grow fines |

Fines are EUR 0.50/day.

## Whats not great

- No DLQ. A bad message would just keep failing in a loop.
- No idempotency. Replaying an event makes a duplicate notif.
- No event versioning. Renaming a field in the POJO breaks things.

If we had more time wed add an outbox pattern so the event and the db write are atomic. For now if the publish fails after the borrow committed, the notif just doesnt go out.
