package io.github.seaniestack.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FallbackControllerTest {

    private final FallbackController controller = new FallbackController();

    @Test
    void coreFallback_returns503() {
        ResponseEntity<Map<String, Object>> response = controller.coreFallback().block();

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().get("status"));
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals("core-service is currently unavailable. Please try again later.", response.getBody().get("message"));
    }

    @Test
    void supportFallback_returns503() {
        ResponseEntity<Map<String, Object>> response = controller.supportFallback().block();

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().get("status"));
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals("support-service is currently unavailable. Please try again later.", response.getBody().get("message"));
    }
}
