# config-service

Port 8888. Spring Cloud Config Server.

Holds shared config for the other services. Two modes:

- `native` - reads yaml from the classpath (bundled in the jar)
- `git` - clones the repo and reads yaml from it

Default is native. We use it because then the docker image is self contained.

## Files served

`backend/config-service/src/main/resources/config/`:

- `core-service.yml`
- `support-service.yml`
- `api-gateway.yml`
- `api-gateway-dev.yml`
- `api-gateway-prod.yml`

## How services use it

Each service has this in their `application.yml`:

```yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}
```

The `optional:` bit means if config-service is down the service still boots using its own application.yml. Avoids the chicken/egg problem.
