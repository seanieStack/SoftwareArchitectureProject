package io.github.seaniestack.supportservice.clients;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoreServiceClientIntegrationTest {

    private final MockWebServer coreService = new MockWebServer();

    @AfterEach
    void tearDown() throws IOException {
        coreService.shutdown();
    }

    @Test
    void bookExists_returnsTrueWhenCoreServiceResponds200() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(200));
        CoreServiceClient client = newClient(3);

        boolean exists = client.bookExists(42L);

        assertThat(exists).isTrue();
        assertThat(coreService.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/api/books/42");
    }

    @Test
    void bookExists_returnsFalseWhenCoreServiceResponds404() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(404));
        CoreServiceClient client = newClient(3);

        boolean exists = client.bookExists(99L);

        assertThat(exists).isFalse();
        assertThat(coreService.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/api/books/99");
    }

    @Test
    void bookExists_retriesAndFallsBackWhenCoreServiceTimesOut() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));
        CoreServiceClient client = newClient(3);

        assertThatThrownBy(() -> client.bookExists(5L))
                .isInstanceOf(CoreServiceClient.CoreServiceUnavailableException.class);

        assertThat(coreService.getRequestCount()).isEqualTo(3);
    }

    @Test
    void bookExists_opensCircuitBreakerAfterRepeatedFailures() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(500));
        coreService.enqueue(new MockResponse().setResponseCode(500));
        CoreServiceClient client = newClient(1);

        assertThatThrownBy(() -> client.bookExists(7L))
                .isInstanceOf(CoreServiceClient.CoreServiceUnavailableException.class);
        assertThatThrownBy(() -> client.bookExists(7L))
                .isInstanceOf(CoreServiceClient.CoreServiceUnavailableException.class);
        assertThat(client.currentCircuitBreakerState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> client.bookExists(7L))
                .isInstanceOf(CoreServiceClient.CoreServiceUnavailableException.class);
        assertThat(coreService.getRequestCount()).isEqualTo(2);
    }

    private CoreServiceClient newClient(int retryMaxAttempts) {
        return new CoreServiceClient(
                new CoreServiceClientProperties(
                        coreService.url("/").toString(),
                        Duration.ofMillis(200),
                        Duration.ofMillis(200),
                        retryMaxAttempts,
                        Duration.ofMillis(10),
                        50,
                        2,
                        2,
                        Duration.ofSeconds(5)
                ),
                "test-internal-secret"
        );
    }
}
