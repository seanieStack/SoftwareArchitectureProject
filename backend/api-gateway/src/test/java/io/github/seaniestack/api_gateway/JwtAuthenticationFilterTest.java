package io.github.seaniestack.api_gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha";

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(new JwtService(SECRET));
    }

    // ── Protected path — valid token ──────────────────────────────────────────

    @Test
    void filter_validToken_chainsAndForwardsIdentityHeaders() {
        String token = buildToken("5", "ADMIN", 60_000);
        CapturingChain chain = new CapturingChain();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
        HttpHeaders forwarded = chain.capturedRequest().getHeaders();
        assertThat(forwarded.getFirst("X-User-Id")).isEqualTo("5");
        assertThat(forwarded.getFirst("X-User-Role")).isEqualTo("ADMIN");
    }

    @Test
    void filter_validToken_overwritesSpoofedIdentityHeaders() {
        String token = buildToken("5", "STUDENT", 60_000);
        CapturingChain chain = new CapturingChain();

        // Caller tries to spoof admin identity
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/borrows/user/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-User-Id", "999")
                .header("X-User-Role", "ADMIN")
                .build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
        HttpHeaders forwarded = chain.capturedRequest().getHeaders();
        // Must use values from the verified token, not the caller's headers
        assertThat(forwarded.getFirst("X-User-Id")).isEqualTo("5");
        assertThat(forwarded.getFirst("X-User-Role")).isEqualTo("STUDENT");
    }

    // ── Protected path — invalid / missing token ──────────────────────────────

    @Test
    void filter_missingAuthorizationHeader_returns401() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/books").build();
        MockServerWebExchange exchange = exchange(request);

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_bearerPrefixWithNoToken_returns401() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        MockServerWebExchange exchange = exchange(request);

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_invalidToken_returns401() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.real.token")
                .build();
        MockServerWebExchange exchange = exchange(request);

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_expiredToken_returns401() {
        String expiredToken = buildToken("1", "STUDENT", -5_000); // expired 5 s ago
        CapturingChain chain = new CapturingChain();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/fines/user/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = exchange(request);

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_tokenSignedWithWrongSecret_returns401() {
        String badToken = buildTokenWithSecret("1", "ADMIN", 60_000, "a-completely-different-secret-key-yes");
        CapturingChain chain = new CapturingChain();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + badToken)
                .build();
        MockServerWebExchange exchange = exchange(request);

        filter.filter(exchange, chain).block();

        assertThat(chain.wasCalled()).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Public paths — token not required ────────────────────────────────────

    @Test
    void filter_authLoginPath_chainsWithoutToken() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/login").build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void filter_authRegisterPath_chainsWithoutToken() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/auth/register").build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void filter_actuatorHealthPath_chainsWithoutToken() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void filter_optionsRequest_chainsWithoutToken() {
        CapturingChain chain = new CapturingChain();
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, "/api/books")
                .build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
    }

    @Test
    void filter_publicPath_stripsIncomingSpoofHeaders() {
        CapturingChain chain = new CapturingChain();

        // Caller sends spoofed headers to a public endpoint — must be stripped
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login")
                .header("X-User-Id", "1")
                .header("X-User-Role", "ADMIN")
                .build();

        filter.filter(exchange(request), chain).block();

        assertThat(chain.wasCalled()).isTrue();
        HttpHeaders forwarded = chain.capturedRequest().getHeaders();
        assertThat(forwarded.get("X-User-Id")).isNullOrEmpty();
        assertThat(forwarded.get("X-User-Role")).isNullOrEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static MockServerWebExchange exchange(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }

    private static String buildToken(String subject, String role, long expirationOffsetMs) {
        return buildTokenWithSecret(subject, role, expirationOffsetMs, SECRET);
    }

    private static String buildTokenWithSecret(String subject, String role,
                                                long expirationOffsetMs, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationOffsetMs)))
                .signWith(key)
                .compact();
    }

    /**
     * Captures the exchange passed to the chain so tests can inspect
     * what headers the filter forwarded downstream.
     */
    static class CapturingChain implements GatewayFilterChain {
        private final AtomicReference<ServerWebExchange> captured = new AtomicReference<>();

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            captured.set(exchange);
            return Mono.empty();
        }

        boolean wasCalled() {
            return captured.get() != null;
        }

        ServerHttpRequest capturedRequest() {
            return captured.get().getRequest();
        }
    }
}
