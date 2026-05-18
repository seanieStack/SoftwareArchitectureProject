# Auth

We use JWT for the browser, and a shared secret for service-to-service calls.

## Roles

- `STUDENT` - `@studentmail.ul.ie`
- `STAFF` - `@ul.ie`
- `ADMIN` - cant be self-registered, set it in the db or have an admin promote

## Tokens

### Access token (JWT)

- HS256 with `JWT_SECRET`
- 1 hour life
- Claims: `sub` = user id, `role` = STUDENT/STAFF/ADMIN, `iat`, `exp`

Only these claims, everything else (email, name) comes back in the login response.

### Refresh token

- UUID, stored in `refresh_tokens` table
- 7 day life
- Rotates every refresh (old one is deleted, new one inserted)
- Only 1 active per user (old one is wiped on login too)

### Reset token

- UUID, 1 hour life, single use
- When you reset all your refresh tokens get revoked

## How a request gets through

```
Browser sends: Authorization: Bearer <jwt>
                    |
                    v
            API Gateway
            - strip X-User-Id / X-User-Role (always!)
            - verify JWT
            - if good, inject verified X-User-Id and X-User-Role
                    |
                    v
            core-service or support-service
            - GatewayHeaderAuthFilter reads the headers
            - builds SecurityContext with ROLE_<role>
            - controller checks hasRole(...)
```

The header strip is important. Without it someone could just send `X-User-Role: ADMIN` and get in. The gateway always wipes those headers, even on public paths.

## Service to service

Support calls core for inventory stuff. Goes direct, not through the gateway.

`InternalSecretFilter` rejects `/api/internal/**` unless the `X-Internal-Secret` header matches `INTERNAL_SECRET`. The controller also checks it again (belt + braces).

`CoreServiceClient` adds the header automatically:

```java
RestClient.builder()
    .defaultHeader("X-Internal-Secret", internalSecret)
    .build();
```

## Frontend bit

`http/client.ts` `authorizedFetch`:
1. Attaches the bearer token
2. On 401 trys to refresh once
3. If refresh fails, logout and go to /login

## Secrets to set

| Secret | Used by |
|---|---|
| `JWT_SECRET` | core (issuer), gateway (verifier). MUST MATCH |
| `INTERNAL_SECRET` | core (filter), support (client). MUST MATCH |

Both have dev fallbacks for local. Production secrets are handled by the deploy workstream.
