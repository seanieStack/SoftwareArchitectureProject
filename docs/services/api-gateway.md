# api-gateway

Port 8080. Spring Cloud Gateway (webflux).

The only thing the browser is supposed to talk to. Routes things to the right service and checks the JWT.

## What it does

1. Strips `X-User-Id` and `X-User-Role` from incoming requests (so noone can fake being admin).
2. Verifys the JWT.
3. Puts the verified `X-User-Id` and `X-User-Role` back on so downstream services dont have to parse the token.
4. Routes by path to the right service via `lb://`.
5. Wraps each route in a circuit breaker with a fallback.

## Routes

| Path | Goes to | Notes |
|---|---|---|
| `/api/auth/**` | core-service | public, no JWT needed |
| `/api/books/**` | core-service | reads = any user, writes = admin |
| `/api/admin/users/**` | core-service | admin only |
| `/api/admin/analytics` | support-service | admin only |
| `/api/borrows/**` | support-service | auth needed |
| `/api/fines/**` | support-service | auth needed |
| `/api/notifications/**` | support-service | auth needed |

## JWT filter

In `JwtAuthenticationFilter.java`. Runs at highest precedence so it goes before everything else.

Public paths that skip the JWT check:
- `/api/auth/**`
- `/actuator/**`
- swagger stuff
- `OPTIONS` preflight

On a bad token it returns 401 with a json body.

## CORS

In `CorsConfig.java`. Uses patterns so dev works on any port and on LAN ips, handy when running vite with `--host`.

## Config

| Env var | What |
|---|---|
| `JWT_SECRET` | HMAC secret, MUST match core-service |
| `EUREKA_URI` | Where eureka is |
| `CONFIG_SERVER_URI` | Where config server is |

## Tests

`backend/api-gateway/src/test/java/...`:
- JwtServiceTest
- JwtAuthenticationFilterTest
- FallbackControllerTest
