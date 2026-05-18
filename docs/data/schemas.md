# Schemas + Seed Data

For the higher level relationship stuff see [data-model.md](../architecture/data-model.md). This file is the columns.

Both dbs use `ddl-auto: update` so JPA makes the tables on first boot. Then `seed.sql` runs.

## core-db tables

### users
- id (pk)
- email (unique, 320)
- password_hash (bcrypt)
- full_name (200)
- role (STUDENT/STAFF/ADMIN as string)
- created_at

### refresh_tokens
- id (pk)
- token (uuid, unique)
- user_id (fk)
- expires_at, created_at

### password_reset_tokens
- same shape as refresh_tokens

### books
- id (pk)
- title
- isbn (unique)
- total_copies (>= 1)
- available_copies (0 to total_copies)
- active
- created_at, updated_at

### authors / categories
- id, name

### book_authors / book_categories
Join tables.

### book_copies
Defined but not actually used yet, was going to be per-copy tracking but didnt have time.

## support-db tables

### borrows
- id (pk)
- user_id (refers to core users, no fk)
- book_id (refers to core books, no fk)
- status (BORROWED/OVERDUE/RETURNED)
- borrowed_at, deadline, returned_at

### fines
- id (pk)
- borrow_id, user_id
- amount (numeric)
- issued_at
- paid, acknowledged
- paid_at (todo: actually set this when paying)

### notifications
- id (pk)
- user_id
- type (BORROW_CREATED/BORROW_DUE_SOON/FINE_CREATED)
- message
- read
- created_at

## Seed

`core-service/src/main/resources/seed.sql`:

- 12 authors (Bob Martin, Kent Beck, Cormen etc)
- 9 categories
- 96 books (ids 5-100) spread across categories with mixed availability
- 7 users (test accounts + a few sample students)

Test users:

| Email | Password | Role |
|---|---|---|
| student@studentmail.ul.ie | student123 | STUDENT |
| staff@ul.ie | staff123 | STAFF |
| admin@ul.ie | Admin1Pass! | ADMIN |
| admin1@ul.ie | Admin1Pass! | ADMIN |
| alice.walsh@studentmail.ul.ie | student123 | STUDENT |
| bob.ryan@studentmail.ul.ie | student123 | STUDENT |
| carol.murphy@studentmail.ul.ie | student123 | STUDENT |

All `ON CONFLICT DO NOTHING` so re-running is safe. Sequence values get bumped past the seeded ids so new inserts dont collide.

`support-service/src/main/resources/seed.sql` adds example borrows / fines / notifs so the dashboards arnt empty.

## Why no flyway/liquibase

For a uni project ddl-auto is fine. Wouldnt do it in production.
