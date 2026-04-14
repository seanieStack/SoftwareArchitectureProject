package io.github.seaniestack.coreservice.clients;

import io.github.seaniestack.coreservice.dtos.LoanCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class SupportServiceClient {

    private final RestClient restClient;

    public SupportServiceClient(@Value("${support-service.url:http://localhost:8082}") String baseUrl,
                                @Value("${internal.secret}") String internalSecret) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Secret", internalSecret)
                .build();
    }

    public long getActiveLoans() {
        log.debug("Getting active loans from support-service");
        try {
            LoanCount count = restClient.get()
                    .uri("/api/internal/activeLoans")
                    .retrieve()
                    .body(LoanCount.class);
            log.debug("Loan count: {}", count);
            return count.count();
        } catch (Exception e) {
            log.error("Failed to reach support-service for book {}", e.getMessage());
            throw new SupportServiceUnavailableException("Core service unavailable", e);
        }
    }

    public static class SupportServiceUnavailableException extends RuntimeException {
        public SupportServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
