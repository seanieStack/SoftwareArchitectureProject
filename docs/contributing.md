# Contributing

How we work on the project.

## Branches

Branch off `main`:

```
feature/<area>/<short-description>
fix/<area>/<short-description>
```

Examples:
- `feature/backend-core-service/added-cors-config`
- `fix/frontend/login-button-disappears`

## Commits

Just plain english, what changed. No need for conventional commits or anything.

```
added filter for availability search
fix: refresh token rotates properly now
```

Dont commit "wip" or "stuff".

## PRs

- Target `main`
- One PR per feature
- At least 1 LGTM from someone else, dont merge your own
- Tests should pass

## Style

### Backend
- Standard spring layout: controller -> service -> repository
- Use lombok where it cuts boilerplate
- DTOs are records
- Validation on the DTO with jakarta annotations
- All exceptions go through `GlobalExceptionHandler`

### Frontend
- `npm run lint` must pass
- Path constants in `src/constants/`, no inline `/api/...` strings
- Use the typed redux hooks

## Adding a new endpoint

Usual order:
1. DTO/Request
2. Service method
3. Controller with proper role check
4. Test (unit + integration)
5. Gateway route if its a new path prefix
6. Frontend constant in `constants/api.ts`
7. Update [api/reference.md](./api/reference.md)
