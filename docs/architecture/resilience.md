# Resilience

We use resilience4j for circuit breakers, retries and timeouts. Its in two places.

## At the gateway

Each route has its own breaker. If a downstream service starts failing, the breaker opens and the gateway sends back a 503 from the fallback controller.

Config in `api-gateway/application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      core-service-cb:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

So if 5 of the last 10 calls fail, the breaker opens for 10 seconds, then trys some test calls.

The fallback is `/api/fallback/core` (or `/support`) which returns a json 503.

## In CoreServiceClient

`support-service` calls core directly (not through gateway) so it needs its own breaker.

```java
.ignoreException(BookNotFoundException.class::isInstance)
```

This bit is important. A 404 isnt a failure (the book just doesnt exist), if we counted them as failures the breaker would open and break everyone.

Retries skip 404 too because retrying wont change anything.

When everything fails it throws `CoreServiceUnavailableException` which becomes a 503 to the caller.

## Healthchecks

Every service has `/actuator/health` and docker compose uses it for `depends_on`. The gateway wont start until core and support are healthy. Means cold start is slow but you dont get half-up states.

## What we dont have

- Bulkheads (no thread pool isolation)
- Rate limiting
- Timeouts on every db query

Out of scope.
