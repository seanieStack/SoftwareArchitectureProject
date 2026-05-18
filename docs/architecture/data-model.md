# Data Model

Each service has its own postgres. They cant FK to eachothers tables.

## Schema

JPA creates the schema with `ddl-auto: update`. Theres a `seed.sql` in each service that runs on first boot to put some test data in.

We didnt use flyway/liquibase because for a uni project the auto generated schema is fine. In a real product youd want migrations.

## core-db

```
users                refresh_tokens             password_reset_tokens
- id                 - id                       - id
- email (unique)     - token (uuid)             - token (uuid)
- password_hash      - user_id (fk users)       - user_id
- full_name          - expires_at               - expires_at
- role (enum)        - created_at               - created_at
- created_at

books                authors                    categories
- id                 - id                       - id
- title              - name                     - name
- isbn (unique)
- total_copies
- available_copies
- active
- created_at
- updated_at

book_authors (join)  book_categories (join)     book_copies (not used yet)
```

Roles: STUDENT, STAFF, ADMIN. Email decides which one you get:
- `@studentmail.ul.ie` -> STUDENT
- `@ul.ie` -> STAFF
- ADMIN cant be self-registered, you have to set it in the db or have an admin promote you.

## support-db

```
borrows              fines                       notifications
- id                 - id                        - id
- user_id            - borrow_id                 - user_id
- book_id            - user_id                   - type (enum)
- status             - amount                    - message
  (BORROWED |        - issued_at                 - read
   OVERDUE |         - paid                      - created_at
   RETURNED)         - acknowledged
- borrowed_at        - paid_at
- deadline
- returned_at
```

Borrow lifecycle:
```
BORROWED  ---return--->  RETURNED
   |
   v (deadline passed + nightly job)
OVERDUE   ---return--->  RETURNED
```

## Seed data

`core-service/seed.sql` puts in:
- 12 authors (Bob Martin, Kent Beck, Cormen etc)
- 9 categories (Software Eng, Algorithms, DevOps...)
- ~96 books spread across categories, some fully available, some borrowed, some all out
- A few test users (see [Getting Started](../getting-started.md))

All inserts use `ON CONFLICT DO NOTHING` so its safe to re-run.

`support-service/seed.sql` puts in example borrows/fines/notifs so the dashboards have content.
