package io.github.seaniestack.supportservice.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CoreServiceClient {

    private final RestClient restClient;

    public CoreServiceClient(@Value("${core-service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean bookExists(Long bookId) {
        log.debug("Checking if book {} exists in core-service", bookId);
        try {
            //TODO: once core service is setup
//            restClient.get()
//                    .uri("/api/books/{bookId}", bookId)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
//                        throw new BookNotFoundException(bookId);
//                    })
//                    .toBodilessEntity();
            log.debug("Book {} exists", bookId);
            return true;
        } catch (BookNotFoundException e) {
            log.warn("Book {} not found in core-service", bookId);
            return false;
        } catch (Exception e) {
            log.error("Failed to reach core-service for book {}: {}", bookId, e.getMessage());
            throw new CoreServiceUnavailableException("Core service unavailable", e);
        }
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
