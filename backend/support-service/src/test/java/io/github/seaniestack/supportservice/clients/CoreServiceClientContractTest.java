package io.github.seaniestack.supportservice.clients;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CoreServiceClientContractTest {

    private static final String INTERNAL_SECRET = "test-internal-secret";

    private final MockWebServer coreService = new MockWebServer();

    @AfterEach
    void tearDown() throws IOException {
        coreService.shutdown();
    }

    @Test
    @DisplayName("CONTRACT-BOOK-EXISTS-200 consumer calls the agreed exists endpoint with the internal secret")
    void bookExists_usesAgreedEndpointAndHeader() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(200));
        CoreServiceClient client = newClient();

        boolean exists = client.bookExists(42L);

        assertThat(exists).isTrue();
        RecordedRequest request = coreService.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/api/internal/books/42/exists");
        assertThat(request.getHeader("X-Internal-Secret")).isEqualTo(INTERNAL_SECRET);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-BORROW-200 consumer calls the agreed borrow endpoint with the internal secret")
    void borrowBook_usesAgreedEndpointAndHeader() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(200));
        CoreServiceClient client = newClient();

        client.borrowBook(42L);

        RecordedRequest request = coreService.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getPath()).isEqualTo("/api/internal/books/42/borrow");
        assertThat(request.getHeader("X-Internal-Secret")).isEqualTo(INTERNAL_SECRET);
    }

    @Test
    @DisplayName("CONTRACT-BOOK-RETURN-200 consumer calls the agreed return endpoint with the internal secret")
    void returnBook_usesAgreedEndpointAndHeader() throws Exception {
        coreService.start();
        coreService.enqueue(new MockResponse().setResponseCode(200));
        CoreServiceClient client = newClient();

        client.returnBook(42L);

        RecordedRequest request = coreService.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getPath()).isEqualTo("/api/internal/books/42/return");
        assertThat(request.getHeader("X-Internal-Secret")).isEqualTo(INTERNAL_SECRET);
    }

    private CoreServiceClient newClient() {
        return new CoreServiceClient(
                new CoreServiceClientProperties(
                        coreService.url("/").toString(),
                        Duration.ofMillis(200),
                        Duration.ofMillis(200),
                        1,
                        Duration.ofMillis(10),
                        50,
                        2,
                        2,
                        Duration.ofSeconds(5)
                ),
                INTERNAL_SECRET
        );
    }
}
