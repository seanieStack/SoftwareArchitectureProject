package io.github.seaniestack.api_gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/api/fallback/core")
    public Mono<ResponseEntity<Map<String, Object>>> coreFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "core-service is currently unavailable. Please try again later."
                )));
    }

    @RequestMapping("/api/fallback/support")
    public Mono<ResponseEntity<Map<String, Object>>> supportFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "support-service is currently unavailable. Please try again later."
                )));
    }
}
