package io.github.seaniestack.supportservice.clients;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "core-service")
public record CoreServiceClientProperties(
        String url,
        Duration connectTimeout,
        Duration readTimeout,
        int retryMaxAttempts,
        Duration retryWaitDuration,
        float circuitBreakerFailureRateThreshold,
        int circuitBreakerSlidingWindowSize,
        int circuitBreakerMinimumNumberOfCalls,
        Duration circuitBreakerWaitDurationInOpenState
) {
}
