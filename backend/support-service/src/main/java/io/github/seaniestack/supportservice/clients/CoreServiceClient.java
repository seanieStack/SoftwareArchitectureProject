package io.github.seaniestack.supportservice.clients;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Slf4j
@Component
public class CoreServiceClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public CoreServiceClient(CoreServiceClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.url())
                .requestFactory(requestFactory)
                .build();

        this.circuitBreaker = CircuitBreaker.of(
                "core-service-client",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(properties.circuitBreakerFailureRateThreshold())
                        .slidingWindowSize(properties.circuitBreakerSlidingWindowSize())
                        .minimumNumberOfCalls(properties.circuitBreakerMinimumNumberOfCalls())
                        .waitDurationInOpenState(properties.circuitBreakerWaitDurationInOpenState())
                        .ignoreException(BookNotFoundException.class::isInstance)
                        .build()
        );

        this.retry = Retry.of(
                "core-service-client",
                RetryConfig.custom()
                        .maxAttempts(properties.retryMaxAttempts())
                        .waitDuration(properties.retryWaitDuration())
                        .retryOnException(this::shouldRetry)
                        .build()
        );

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("core-service circuit breaker transition: {}", event.getStateTransition()));
        this.retry.getEventPublisher()
                .onRetry(event -> log.warn(
                        "Retrying core-service call attempt {} due to {}",
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable() == null ? "unknown error" : event.getLastThrowable().getClass().getSimpleName()
                ));
    }

    public boolean bookExists(Long bookId) {
        log.debug("Checking if book {} exists in core-service", bookId);
        Supplier<Boolean> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(retry, () -> fetchBookExists(bookId))
        );

        try {
            return decorated.get();
        } catch (BookNotFoundException e) {
            log.warn("Book {} not found in core-service", bookId);
            return false;
        } catch (CallNotPermittedException e) {
            throw fallbackUnavailable("core-service circuit breaker is open", e);
        } catch (Exception e) {
            throw fallbackUnavailable("Failed to reach core-service for book " + bookId, e);
        }
    }

    CircuitBreaker.State currentCircuitBreakerState() {
        return circuitBreaker.getState();
    }

    private boolean fetchBookExists(Long bookId) {
        restClient.get()
                .uri("/api/books/{bookId}", bookId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (request, response) -> {
                    throw new BookNotFoundException(bookId);
                })
                .toBodilessEntity();

        log.debug("Book {} exists", bookId);
        return true;
    }

    private boolean shouldRetry(Throwable throwable) {
        return !(throwable instanceof BookNotFoundException)
                && !(throwable instanceof CallNotPermittedException);
    }

    private CoreServiceUnavailableException fallbackUnavailable(String message, Exception cause) {
        log.error("{}; applying fallback 503 response", message, cause);
        return new CoreServiceUnavailableException("Core service unavailable", cause);
    }

    public static class BookNotFoundException extends RuntimeException {
        public BookNotFoundException(Long bookId) {
            super("Book not found. Id: " + bookId);
        }
    }

    public static class CoreServiceUnavailableException extends RuntimeException {
        public CoreServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
