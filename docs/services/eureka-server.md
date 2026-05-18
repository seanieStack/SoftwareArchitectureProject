# eureka-server

Port 8761. Service registry.

Every backend service registers with eureka on boot and the gateway uses it to resolve `lb://core-service` etc.

Config:

```yaml
eureka:
  client:
    register-with-eureka: false   # its the registry, dont register with itself
    fetch-registry: false
  server:
    enable-self-preservation: false
```

Self preservation is off so dead services get evicted fast. Fine for dev, probaly want it on in real life.

Dashboard: http://localhost:8761
