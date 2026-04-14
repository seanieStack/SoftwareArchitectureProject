package io.github.seaniestack.supportservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seaniestack.supportservice.messaging.EventPublisher;
import io.github.seaniestack.supportservice.messaging.events.BorrowCreatedEvent;
import io.github.seaniestack.supportservice.repositories.BorrowRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BorrowFlowIntegrationTest {

    private static MockWebServer coreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BorrowRepository borrowRepository;

    @LocalServerPort
    private int port;

    @MockitoBean
    private EventPublisher eventPublisher;

    private int startingRequestCount;
    private HttpClient httpClient;

    @BeforeAll
    static void beforeAll() throws Exception {
        coreService = new MockWebServer();
        coreService.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        coreService.shutdown();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("core-service.url", () -> coreService.url("/").toString());
    }

    @BeforeEach
    void setUp() throws Exception {
        borrowRepository.deleteAll();
        while (coreService.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
            // Drain requests left behind by timeout scenarios so assertions stay deterministic.
        }
        startingRequestCount = coreService.getRequestCount();
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    @DisplayName("SCN-BORROW-001 creates a borrow when catalogue confirms the book exists")
    void createBorrow_persistsBorrowAndPublishesEvent() throws Exception {
        coreService.enqueue(new MockResponse().setResponseCode(200));
        LocalDateTime deadline = LocalDateTime.of(2026, 4, 30, 12, 0);

        HttpResponse<String> response = sendBorrowRequest(Map.of(
                "userId", 17,
                "bookId", 42,
                "deadline", deadline.toString()
        ));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"userId\":17");
        assertThat(response.body()).contains("\"bookId\":42");
        assertThat(response.body()).contains("\"status\":\"BORROWED\"");

        assertThat(borrowRepository.findAll()).hasSize(1);
        assertThat(coreService.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/api/books/42");
        verify(eventPublisher).publishBorrowCreated(argThat((BorrowCreatedEvent event) ->
                event.userId().equals(17L) && event.bookId().equals(42L) && event.deadline().equals(deadline)));
    }

    @Test
    @DisplayName("SCN-BORROW-002 rejects a borrow when catalogue reports the book is missing")
    void createBorrow_returns404WhenCatalogueDoesNotKnowBook() throws Exception {
        coreService.enqueue(new MockResponse().setResponseCode(404));

        HttpResponse<String> response = sendBorrowRequest(Map.of(
                "userId", 17,
                "bookId", 999,
                "deadline", LocalDateTime.of(2026, 4, 30, 12, 0).toString()
        ));

        assertThat(response.statusCode()).isEqualTo(404);

        assertThat(borrowRepository.findAll()).isEmpty();
        assertThat(coreService.takeRequest(1, TimeUnit.SECONDS).getPath()).isEqualTo("/api/books/999");
        verify(eventPublisher, never()).publishBorrowCreated(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("SCN-BORROW-003 returns 503 after retries when catalogue is unavailable")
    void createBorrow_returns503WhenCatalogueTimesOut() throws Exception {
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));
        coreService.enqueue(new MockResponse().setHeadersDelay(400, TimeUnit.MILLISECONDS).setResponseCode(200));

        HttpResponse<String> response = sendBorrowRequest(Map.of(
                "userId", 17,
                "bookId", 42,
                "deadline", LocalDateTime.of(2026, 4, 30, 12, 0).toString()
        ));

        assertThat(response.statusCode()).isEqualTo(503);

        assertThat(coreService.getRequestCount() - startingRequestCount).isEqualTo(3);
        assertThat(borrowRepository.findAll()).isEmpty();
        verify(eventPublisher, never()).publishBorrowCreated(org.mockito.ArgumentMatchers.any());
    }

    private HttpResponse<String> sendBorrowRequest(Map<String, Object> body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/borrows"))
                .header("Content-Type", "application/json")
                .header("X-User-Id", "17")
                .header("X-User-Role", "STUDENT")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
